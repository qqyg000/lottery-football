package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EspnScheduleUpdater {

    private static final Logger log = LoggerFactory.getLogger(EspnScheduleUpdater.class);

    private static final int REGULATION_SECONDS = 90 * 60;

    private static final Pattern CLOCK_MINUTE_PATTERN = Pattern.compile("^(\\d+)");

    private final ObjectMapper objectMapper;

    @Value("${worldcup.espn-update.enabled:true}")
    private boolean enabled;

    @Value("${worldcup.espn-update.scoreboard-url-template:https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard?limit=200&dates={start}-{end}}")
    private String scoreboardUrlTemplate;

    @Value("${worldcup.espn-update.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${worldcup.espn-update.target-zone:America/New_York}")
    private String targetZone;

    @Value("${worldcup.espn-update.days-back:30}")
    private int daysBack;

    @Value("${worldcup.espn-update.days-forward:21}")
    private int daysForward;

    @Value("${champions-league.espn-update.enabled:true}")
    private boolean championsLeagueEnabled;

    @Value("${champions-league.espn-update.scoreboard-url-template:https://site.api.espn.com/apis/site/v2/sports/soccer/uefa.champions/scoreboard?limit=1000&dates={start}-{end}}")
    private String championsLeagueScoreboardUrlTemplate;

    @Value("${champions-league.espn-update.qualifying-scoreboard-url-template:https://site.api.espn.com/apis/site/v2/sports/soccer/uefa.champions_qual/scoreboard?limit=500&dates={start}-{end}}")
    private String championsLeagueQualifyingScoreboardUrlTemplate;

    @Value("${champions-league.espn-update.timeout-seconds:10}")
    private int championsLeagueTimeoutSeconds;

    @Value("${champions-league.espn-update.target-zone:Asia/Shanghai}")
    private String championsLeagueTargetZone;

    @Value("${champions-league.espn-update.history-seasons:5}")
    private int championsLeagueHistorySeasons;

    public EspnScheduleUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public int updateSchedules(List<MatchSchedule> schedules) {
        if (!enabled) {
            log.info("ESPN schedule update is disabled.");
            return 0;
        }
        try {
            ZoneId zoneId = ZoneId.of(targetZone);
            LocalDate today = LocalDate.now(zoneId);
            LocalDate startDate = today.minusDays(Math.max(0, daysBack));
            LocalDate endDate = today.plusDays(Math.max(0, daysForward));
            String content = downloadContent(buildScoreboardUrl(startDate, endDate));
            List<RemoteMatch> remoteMatches = parseMatches(content, zoneId);
            int updatedCount = mergeSchedules(schedules, remoteMatches);
            log.info("Updated {} World Cup schedule rows from ESPN scoreboard.", updatedCount);
            return updatedCount;
        } catch (Exception ex) {
            log.warn("Failed to update World Cup schedule from ESPN scoreboard.", ex);
            return 0;
        }
    }

    public int updateChampionsLeagueSchedules(List<MatchSchedule> schedules) {
        if (!championsLeagueEnabled) {
            log.info("ESPN Champions League schedule update is disabled.");
            return 0;
        }

        ZoneId zoneId = ZoneId.of(championsLeagueTargetZone);
        int currentSeasonStartYear = resolveChampionsLeagueSeasonStartYear(LocalDate.now(zoneId));
        int seasonCount = Math.max(1, Math.min(5, championsLeagueHistorySeasons));
        List<RemoteMatch> remoteMatches = new ArrayList<>();
        for (int offset = seasonCount - 1; offset >= 0; offset--) {
            int seasonStartYear = currentSeasonStartYear - offset;
            addRemoteMatches(
                    remoteMatches,
                    championsLeagueQualifyingScoreboardUrlTemplate,
                    LocalDate.of(seasonStartYear, 7, 1),
                    LocalDate.of(seasonStartYear, 8, 31),
                    zoneId);
            addRemoteMatches(
                    remoteMatches,
                    championsLeagueScoreboardUrlTemplate,
                    LocalDate.of(seasonStartYear, 9, 1),
                    LocalDate.of(seasonStartYear + 1, 6, 30),
                    zoneId);
        }

        int updatedCount = mergeChampionsLeagueSchedules(schedules, remoteMatches);
        log.info("Updated {} Champions League schedule rows from ESPN scoreboard.", updatedCount);
        return updatedCount;
    }

    private int resolveChampionsLeagueSeasonStartYear(LocalDate today) {
        return today.getMonthValue() >= 7 ? today.getYear() : today.getYear() - 1;
    }

    private void addRemoteMatches(
            List<RemoteMatch> target,
            String urlTemplate,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId) {
        String url = buildScoreboardUrl(urlTemplate, startDate, endDate);
        try {
            String content = downloadContent(url, championsLeagueTimeoutSeconds);
            target.addAll(parseMatches(content, zoneId));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Champions League schedule update was interrupted for {} to {}.", startDate, endDate);
        } catch (Exception ex) {
            log.warn("Unable to load Champions League schedule for {} to {}: {}", startDate, endDate, ex.getMessage());
        }
    }

    private String buildScoreboardUrl(LocalDate startDate, LocalDate endDate) {
        return buildScoreboardUrl(scoreboardUrlTemplate, startDate, endDate);
    }

    private String buildScoreboardUrl(String urlTemplate, LocalDate startDate, LocalDate endDate) {
        return urlTemplate
                .replace("{start}", startDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE))
                .replace("{end}", endDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
    }

    private String downloadContent(String url) throws IOException, InterruptedException {
        return downloadContent(url, timeoutSeconds);
    }

    private String downloadContent(String url, int configuredTimeoutSeconds) throws IOException, InterruptedException {
        Duration timeout = Duration.ofSeconds(Math.max(1, configuredTimeoutSeconds));
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Unexpected HTTP status " + response.statusCode());
        }
        return response.body();
    }

    private List<RemoteMatch> parseMatches(String content, ZoneId zoneId) throws IOException {
        JsonNode root = objectMapper.readTree(content);
        List<RemoteMatch> matches = new ArrayList<>();
        for (JsonNode event : root.path("events")) {
            RemoteMatch remoteMatch = parseEvent(event, zoneId);
            if (remoteMatch != null) {
                matches.add(remoteMatch);
            }
        }
        return matches;
    }

    private RemoteMatch parseEvent(JsonNode event, ZoneId zoneId) {
        JsonNode competition = event.path("competitions").path(0);
        JsonNode competitors = competition.path("competitors");
        if (!competitors.isArray() || competitors.size() < 2) {
            return null;
        }

        JsonNode homeCompetitor = null;
        JsonNode awayCompetitor = null;
        for (JsonNode competitor : competitors) {
            String homeAway = competitor.path("homeAway").asText("");
            if ("home".equalsIgnoreCase(homeAway)) {
                homeCompetitor = competitor;
            } else if ("away".equalsIgnoreCase(homeAway)) {
                awayCompetitor = competitor;
            }
        }
        if (homeCompetitor == null || awayCompetitor == null) {
            return null;
        }

        String dateText = event.path("date").asText("");
        if (dateText.isBlank()) {
            return null;
        }
        ZonedDateTime targetDateTime = OffsetDateTime.parse(dateText).atZoneSameInstant(zoneId);

        JsonNode statusType = event.path("status").path("type");
        String state = statusType.path("state").asText("");
        boolean completed = statusType.path("completed").asBoolean(false);

        RemoteMatch match = new RemoteMatch();
        match.eventId = event.path("id").asText("");
        match.eventName = event.path("name").asText("");
        match.seasonSlug = event.path("season").path("slug").asText("");
        String homeTeamId = readTeamId(homeCompetitor);
        String awayTeamId = readTeamId(awayCompetitor);
        match.homeTeam = readTeamName(homeCompetitor);
        match.awayTeam = readTeamName(awayCompetitor);
        match.matchDate = targetDateTime.toLocalDate();
        match.kickoffTime = targetDateTime.toLocalTime().withSecond(0).withNano(0);
        match.venue = competition.path("venue").path("fullName").asText("");
        match.neutral = competition.path("neutralSite").asBoolean(false);
        match.status = completed ? "COMPLETED" : ("in".equalsIgnoreCase(state) ? "LIVE" : "SCHEDULED");
        Integer fullHomeScore = parseScore(homeCompetitor.path("score").asText(""));
        Integer fullAwayScore = parseScore(awayCompetitor.path("score").asText(""));
        ScorePair regulationScore = parseRegulationScore(competition, homeTeamId, awayTeamId, fullHomeScore, fullAwayScore);
        ScorePair halfTimeScore = parseHalfTimeScore(competition, homeTeamId, awayTeamId, fullHomeScore, fullAwayScore);
        if (regulationScore != null) {
            match.homeScore = regulationScore.homeScore;
            match.awayScore = regulationScore.awayScore;
        } else {
            match.homeScore = fullHomeScore;
            match.awayScore = fullAwayScore;
        }
        if (halfTimeScore != null) {
            match.halfTimeHomeScore = halfTimeScore.homeScore;
            match.halfTimeAwayScore = halfTimeScore.awayScore;
        }
        return match;
    }

    private String readTeamId(JsonNode competitor) {
        String teamId = competitor.path("team").path("id").asText("");
        if (teamId.isBlank()) {
            teamId = competitor.path("id").asText("");
        }
        return teamId;
    }

    private String readTeamName(JsonNode competitor) {
        JsonNode team = competitor.path("team");
        String displayName = team.path("displayName").asText("");
        if (displayName.isBlank()) {
            displayName = team.path("location").asText("");
        }
        return normalizeTeamName(displayName);
    }

    private Integer parseScore(String value) {
        if (value == null || !value.matches("\\d+")) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private ScorePair parseRegulationScore(
            JsonNode competition,
            String homeTeamId,
            String awayTeamId,
            Integer fullHomeScore,
            Integer fullAwayScore) {
        JsonNode details = competition.path("details");
        if (!details.isArray() || details.size() == 0) {
            return null;
        }

        int homeScore = 0;
        int awayScore = 0;
        boolean sawOpenPlayOrExtraTimeGoal = false;
        for (JsonNode detail : details) {
            if (!detail.path("scoringPlay").asBoolean(false)
                    || detail.path("shootout").asBoolean(false)
                    || detail.path("scoreValue").asInt(0) <= 0) {
                continue;
            }

            sawOpenPlayOrExtraTimeGoal = true;
            if (isAfterRegulation(detail)) {
                continue;
            }

            String scoringTeamId = detail.path("team").path("id").asText("");
            int scoreValue = detail.path("scoreValue").asInt(1);
            if (scoringTeamId.equals(homeTeamId)) {
                homeScore += scoreValue;
            } else if (scoringTeamId.equals(awayTeamId)) {
                awayScore += scoreValue;
            }
        }

        if (!sawOpenPlayOrExtraTimeGoal && scoreTotal(fullHomeScore, fullAwayScore) > 0) {
            return null;
        }
        return new ScorePair(homeScore, awayScore);
    }

    private ScorePair parseHalfTimeScore(
            JsonNode competition,
            String homeTeamId,
            String awayTeamId,
            Integer fullHomeScore,
            Integer fullAwayScore) {
        JsonNode details = competition.path("details");
        if (!details.isArray() || details.size() == 0) {
            return scoreTotal(fullHomeScore, fullAwayScore) == 0 ? new ScorePair(0, 0) : null;
        }

        int homeScore = 0;
        int awayScore = 0;
        boolean sawGoal = false;
        for (JsonNode detail : details) {
            if (!detail.path("scoringPlay").asBoolean(false)
                    || detail.path("shootout").asBoolean(false)
                    || detail.path("scoreValue").asInt(0) <= 0) {
                continue;
            }

            sawGoal = true;
            if (!isFirstHalf(detail)) {
                continue;
            }

            String scoringTeamId = detail.path("team").path("id").asText("");
            int scoreValue = detail.path("scoreValue").asInt(1);
            if (scoringTeamId.equals(homeTeamId)) {
                homeScore += scoreValue;
            } else if (scoringTeamId.equals(awayTeamId)) {
                awayScore += scoreValue;
            }
        }

        if (!sawGoal && scoreTotal(fullHomeScore, fullAwayScore) > 0) {
            return null;
        }
        return new ScorePair(homeScore, awayScore);
    }

    private boolean isFirstHalf(JsonNode detail) {
        Integer displayMinute = readDisplayMinute(detail);
        if (displayMinute != null) {
            return displayMinute <= 45;
        }

        JsonNode clockValue = detail.path("clock").path("value");
        return clockValue.isNumber() && clockValue.asDouble() <= REGULATION_SECONDS / 2.0D;
    }

    private boolean isAfterRegulation(JsonNode detail) {
        Integer displayMinute = readDisplayMinute(detail);
        if (displayMinute != null) {
            return displayMinute > 90;
        }

        JsonNode clockValue = detail.path("clock").path("value");
        return clockValue.isNumber() && clockValue.asDouble() > REGULATION_SECONDS;
    }

    private Integer readDisplayMinute(JsonNode detail) {
        String displayValue = detail.path("clock").path("displayValue").asText("");
        Matcher matcher = CLOCK_MINUTE_PATTERN.matcher(displayValue);
        if (!matcher.find()) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }

    private int scoreTotal(Integer homeScore, Integer awayScore) {
        return (homeScore == null ? 0 : homeScore) + (awayScore == null ? 0 : awayScore);
    }

    private int mergeSchedules(List<MatchSchedule> schedules, List<RemoteMatch> remoteMatches) {
        Map<String, MatchSchedule> scheduleByTeams = schedules.stream()
                .filter(schedule -> schedule.getCompetition() == Competition.WORLD_CUP)
                .collect(Collectors.toMap(
                        schedule -> buildTeamKey(schedule.getHomeTeamEn(), schedule.getAwayTeamEn()),
                        schedule -> schedule,
                        (left, right) -> left));

        int updatedCount = 0;
        for (RemoteMatch remoteMatch : remoteMatches) {
            MatchSchedule schedule = scheduleByTeams.get(buildTeamKey(remoteMatch.homeTeam, remoteMatch.awayTeam));
            if (schedule == null) {
                schedules.add(toSchedule(remoteMatch));
                scheduleByTeams.put(buildTeamKey(remoteMatch.homeTeam, remoteMatch.awayTeam), schedules.get(schedules.size() - 1));
                updatedCount++;
                continue;
            }

            applyRemoteMatch(schedule, remoteMatch);
            updatedCount++;
        }
        return updatedCount;
    }

    private int mergeChampionsLeagueSchedules(List<MatchSchedule> schedules, List<RemoteMatch> remoteMatches) {
        Map<String, MatchSchedule> schedulesById = schedules.stream()
                .filter(schedule -> schedule.getCompetition() == Competition.CHAMPIONS_LEAGUE)
                .collect(Collectors.toMap(
                        MatchSchedule::getMatchId,
                        schedule -> schedule,
                        (left, right) -> left));

        int updatedCount = 0;
        for (RemoteMatch remoteMatch : remoteMatches) {
            String matchId = buildChampionsLeagueMatchId(remoteMatch);
            MatchSchedule schedule = schedulesById.get(matchId);
            if (schedule == null) {
                schedule = toChampionsLeagueSchedule(remoteMatch, matchId);
                schedules.add(schedule);
                schedulesById.put(matchId, schedule);
            } else {
                applyRemoteMatch(schedule, remoteMatch);
                schedule.setGroupName(toChampionsLeagueStageName(remoteMatch.seasonSlug));
            }
            updatedCount++;
        }
        return updatedCount;
    }

    private String buildChampionsLeagueMatchId(RemoteMatch remoteMatch) {
        if (remoteMatch.eventId != null && !remoteMatch.eventId.isBlank()) {
            return "ESPN-UCL-" + remoteMatch.eventId;
        }
        return "ESPN-UCL-" + remoteMatch.matchDate + "-"
                + buildTeamKey(remoteMatch.homeTeam, remoteMatch.awayTeam).replace('|', '-');
    }

    private MatchSchedule toSchedule(RemoteMatch remoteMatch) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.WORLD_CUP);
        schedule.setMatchId(remoteMatch.eventId == null || remoteMatch.eventId.isBlank()
                ? "ESPN2026-" + buildTeamKey(remoteMatch.homeTeam, remoteMatch.awayTeam).replace('|', '-')
                : "ESPN2026-" + remoteMatch.eventId);
        schedule.setGroupName(inferKnockoutStage(remoteMatch));
        schedule.setHomeTeamCn(toChineseTeamName(remoteMatch.homeTeam));
        schedule.setAwayTeamCn(toChineseTeamName(remoteMatch.awayTeam));
        schedule.setHomeTeamEn(remoteMatch.homeTeam);
        schedule.setAwayTeamEn(remoteMatch.awayTeam);
        schedule.setNeutral(true);
        applyRemoteMatch(schedule, remoteMatch);
        return schedule;
    }

    private MatchSchedule toChampionsLeagueSchedule(RemoteMatch remoteMatch, String matchId) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CHAMPIONS_LEAGUE);
        schedule.setMatchId(matchId);
        schedule.setGroupName(toChampionsLeagueStageName(remoteMatch.seasonSlug));
        schedule.setHomeTeamCn(toChineseClubName(remoteMatch.homeTeam));
        schedule.setAwayTeamCn(toChineseClubName(remoteMatch.awayTeam));
        schedule.setHomeTeamEn(remoteMatch.homeTeam);
        schedule.setAwayTeamEn(remoteMatch.awayTeam);
        applyRemoteMatch(schedule, remoteMatch);
        return schedule;
    }

    private void applyRemoteMatch(MatchSchedule schedule, RemoteMatch remoteMatch) {
        schedule.setMatchDate(remoteMatch.matchDate);
        schedule.setKickoffTime(remoteMatch.kickoffTime);
        schedule.setNeutral(remoteMatch.neutral);
        if (remoteMatch.venue != null && !remoteMatch.venue.isBlank()) {
            schedule.setVenue(remoteMatch.venue);
        }

        if ("COMPLETED".equals(remoteMatch.status)) {
            schedule.setStatus("COMPLETED");
            schedule.setHomeScore(remoteMatch.homeScore);
            schedule.setAwayScore(remoteMatch.awayScore);
            schedule.setHalfTimeHomeScore(remoteMatch.halfTimeHomeScore);
            schedule.setHalfTimeAwayScore(remoteMatch.halfTimeAwayScore);
        } else if ("LIVE".equals(remoteMatch.status)) {
            schedule.setStatus("LIVE");
            schedule.setHomeScore(remoteMatch.homeScore);
            schedule.setAwayScore(remoteMatch.awayScore);
            schedule.setHalfTimeHomeScore(remoteMatch.halfTimeHomeScore);
            schedule.setHalfTimeAwayScore(remoteMatch.halfTimeAwayScore);
        } else if (!"COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
            schedule.setStatus("SCHEDULED");
            schedule.setHomeScore(null);
            schedule.setAwayScore(null);
            schedule.setHalfTimeHomeScore(null);
            schedule.setHalfTimeAwayScore(null);
        }
    }

    private String inferKnockoutStage(RemoteMatch remoteMatch) {
        String eventName = remoteMatch.eventName == null ? "" : remoteMatch.eventName;
        if (eventName.contains("Semifinal") && eventName.contains("Loser")) {
            return "三四名决赛";
        }
        if (eventName.contains("Semifinal") && eventName.contains("Winner")) {
            return "决赛";
        }
        if (eventName.contains("Quarterfinal")) {
            return "半决赛";
        }
        if (eventName.contains("Round of 16")) {
            return "8强淘汰赛";
        }
        if (eventName.contains("Round of 32")) {
            return "16强淘汰赛";
        }

        LocalDate date = remoteMatch.matchDate;
        if (!date.isBefore(LocalDate.of(2026, 7, 4)) && !date.isAfter(LocalDate.of(2026, 7, 7))) {
            return "16强淘汰赛";
        }
        if (!date.isBefore(LocalDate.of(2026, 7, 9)) && !date.isAfter(LocalDate.of(2026, 7, 11))) {
            return "8强淘汰赛";
        }
        if (!date.isBefore(LocalDate.of(2026, 7, 14)) && !date.isAfter(LocalDate.of(2026, 7, 15))) {
            return "半决赛";
        }
        if (date.equals(LocalDate.of(2026, 7, 18))) {
            return "三四名决赛";
        }
        if (date.equals(LocalDate.of(2026, 7, 19))) {
            return "决赛";
        }
        return "淘汰赛";
    }

    private String toChampionsLeagueStageName(String seasonSlug) {
        String normalized = seasonSlug == null ? "" : seasonSlug.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "first-round" -> "资格赛第一轮";
            case "second-round" -> "资格赛第二轮";
            case "third-round" -> "资格赛第三轮";
            case "playoff-round", "playoffs" -> "附加赛";
            case "league-phase", "group-stage" -> "联赛阶段";
            case "knockout-round-playoffs" -> "淘汰赛附加赛";
            case "round-of-16" -> "16强";
            case "quarterfinals" -> "四分之一决赛";
            case "semifinals" -> "半决赛";
            case "final" -> "决赛";
            default -> "欧冠";
        };
    }

    private String toChineseClubName(String teamName) {
        Map<String, String> teamNames = new HashMap<>();
        teamNames.put("AC Milan", "AC米兰");
        teamNames.put("Ajax Amsterdam", "阿贾克斯");
        teamNames.put("Ararat-Armenia", "阿拉拉特亚美尼亚");
        teamNames.put("Arsenal", "阿森纳");
        teamNames.put("AS Monaco", "摩纳哥");
        teamNames.put("Aston Villa", "阿斯顿维拉");
        teamNames.put("Atalanta", "亚特兰大");
        teamNames.put("Athletic Club", "毕尔巴鄂竞技");
        teamNames.put("Atletico Madrid", "马德里竞技");
        teamNames.put("Barcelona", "巴塞罗那");
        teamNames.put("Bayer Leverkusen", "勒沃库森");
        teamNames.put("Bayern Munich", "拜仁慕尼黑");
        teamNames.put("Benfica", "本菲卡");
        teamNames.put("Bodo/Glimt", "博德闪耀");
        teamNames.put("Bologna", "博洛尼亚");
        teamNames.put("Borac Banja Luka", "巴尼亚卢卡战士");
        teamNames.put("Borussia Dortmund", "多特蒙德");
        teamNames.put("Brest", "布雷斯特");
        teamNames.put("Celtic", "凯尔特人");
        teamNames.put("Chelsea", "切尔西");
        teamNames.put("Club Brugge", "布鲁日");
        teamNames.put("CSU Craiova", "克拉约瓦大学");
        teamNames.put("Dinamo Zagreb", "萨格勒布迪纳摩");
        teamNames.put("Drita Gjilan", "德里塔");
        teamNames.put("Egnatia", "埃格纳蒂亚");
        teamNames.put("Eintracht Frankfurt", "法兰克福");
        teamNames.put("F.C. København", "哥本哈根");
        teamNames.put("FC Atert Bissen", "阿特尔特比森");
        teamNames.put("Feyenoord Rotterdam", "费耶诺德");
        teamNames.put("FK Sutjeska", "苏捷斯卡");
        teamNames.put("FK Qarabag", "卡拉巴赫");
        teamNames.put("Flora", "塔林弗洛拉");
        teamNames.put("Floriana FC", "弗洛里亚纳");
        teamNames.put("Galatasaray", "加拉塔萨雷");
        teamNames.put("Girona", "赫罗纳");
        teamNames.put("Gyori ETO FC", "杰尔ETO");
        teamNames.put("Iberia 1999", "伊比利亚1999");
        teamNames.put("Inter D'Escaldes", "伊斯卡尔德斯国际");
        teamNames.put("Internazionale", "国际米兰");
        teamNames.put("Juventus", "尤文图斯");
        teamNames.put("Kairat Almaty", "阿拉木图凯拉特");
        teamNames.put("Kauno Zalgiris", "考诺萨基列斯");
        teamNames.put("KI Klaksvik", "克拉克斯维克");
        teamNames.put("KuPS Kuopio", "古比斯");
        teamNames.put("Larne", "拉恩");
        teamNames.put("Levski Sofia", "索菲亚列夫斯基");
        teamNames.put("Lille", "里尔");
        teamNames.put("Lincoln Red Imps", "林肯红魔");
        teamNames.put("Liverpool", "利物浦");
        teamNames.put("Manchester City", "曼城");
        teamNames.put("Marseille", "马赛");
        teamNames.put("ML Vitebsk", "维捷布斯克ML");
        teamNames.put("Napoli", "那不勒斯");
        teamNames.put("Newcastle United", "纽卡斯尔联");
        teamNames.put("Olympiacos", "奥林匹亚科斯");
        teamNames.put("Pafos", "帕福斯");
        teamNames.put("Paris Saint-Germain", "巴黎圣日耳曼");
        teamNames.put("Petrocub", "佩特罗库布");
        teamNames.put("PSV Eindhoven", "埃因霍温");
        teamNames.put("RB Leipzig", "RB莱比锡");
        teamNames.put("RB Salzburg", "萨尔茨堡红牛");
        teamNames.put("Real Madrid", "皇家马德里");
        teamNames.put("Red Star Belgrade", "贝尔格莱德红星");
        teamNames.put("Riga FC", "里加");
        teamNames.put("Sabah FK", "萨巴赫");
        teamNames.put("Shamrock Rovers", "沙姆洛克流浪");
        teamNames.put("Shakhtar Donetsk", "顿涅茨克矿工");
        teamNames.put("SK Sturm Graz", "格拉茨风暴");
        teamNames.put("Slavia Prague", "布拉格斯拉维亚");
        teamNames.put("Slovan Bratislava", "布拉迪斯拉发");
        teamNames.put("Sparta Prague", "布拉格斯巴达");
        teamNames.put("Sporting CP", "葡萄牙体育");
        teamNames.put("Tottenham Hotspur", "托特纳姆热刺");
        teamNames.put("The New Saints", "新圣徒");
        teamNames.put("Tre Fiori", "特雷菲奥里");
        teamNames.put("Union St.-Gilloise", "圣吉罗斯联合");
        teamNames.put("Vardar", "华达");
        teamNames.put("VfB Stuttgart", "斯图加特");
        teamNames.put("Vikingur Reykjavik", "雷克雅未克维京人");
        teamNames.put("Villarreal", "比利亚雷亚尔");
        teamNames.put("Young Boys", "伯尔尼年轻人");
        return teamNames.getOrDefault(teamName, ClubTeamNameTranslator.translate(teamName));
    }

    private String toChineseTeamName(String teamName) {
        Map<String, String> teamNames = new HashMap<>();
        teamNames.put("Mexico", "墨西哥");
        teamNames.put("South Africa", "南非");
        teamNames.put("South Korea", "韩国");
        teamNames.put("Czechia", "捷克");
        teamNames.put("Canada", "加拿大");
        teamNames.put("Bosnia and Herzegovina", "波黑");
        teamNames.put("Qatar", "卡塔尔");
        teamNames.put("Switzerland", "瑞士");
        teamNames.put("Brazil", "巴西");
        teamNames.put("Morocco", "摩洛哥");
        teamNames.put("Haiti", "海地");
        teamNames.put("Scotland", "苏格兰");
        teamNames.put("United States", "美国");
        teamNames.put("Paraguay", "巴拉圭");
        teamNames.put("Australia", "澳大利亚");
        teamNames.put("Turkey", "土耳其");
        teamNames.put("Germany", "德国");
        teamNames.put("Curacao", "库拉索");
        teamNames.put("Ivory Coast", "科特迪瓦");
        teamNames.put("Ecuador", "厄瓜多尔");
        teamNames.put("Netherlands", "荷兰");
        teamNames.put("Japan", "日本");
        teamNames.put("Sweden", "瑞典");
        teamNames.put("Tunisia", "突尼斯");
        teamNames.put("Belgium", "比利时");
        teamNames.put("Egypt", "埃及");
        teamNames.put("Iran", "伊朗");
        teamNames.put("New Zealand", "新西兰");
        teamNames.put("Spain", "西班牙");
        teamNames.put("Cape Verde", "佛得角");
        teamNames.put("Saudi Arabia", "沙特阿拉伯");
        teamNames.put("Uruguay", "乌拉圭");
        teamNames.put("France", "法国");
        teamNames.put("Senegal", "塞内加尔");
        teamNames.put("Iraq", "伊拉克");
        teamNames.put("Norway", "挪威");
        teamNames.put("Argentina", "阿根廷");
        teamNames.put("Algeria", "阿尔及利亚");
        teamNames.put("Austria", "奥地利");
        teamNames.put("Jordan", "约旦");
        teamNames.put("Portugal", "葡萄牙");
        teamNames.put("DR Congo", "刚果民主共和国");
        teamNames.put("Uzbekistan", "乌兹别克斯坦");
        teamNames.put("Colombia", "哥伦比亚");
        teamNames.put("England", "英格兰");
        teamNames.put("Croatia", "克罗地亚");
        teamNames.put("Ghana", "加纳");
        teamNames.put("Panama", "巴拿马");
        return teamNames.getOrDefault(teamName, teamName);
    }

    private String buildTeamKey(String homeTeam, String awayTeam) {
        return normalizeTeamName(homeTeam).toLowerCase(Locale.ROOT) + "|" + normalizeTeamName(awayTeam).toLowerCase(Locale.ROOT);
    }

    private String normalizeTeamName(String teamName) {
        String cleaned = teamName == null ? "" : teamName.trim().replaceAll("\\s+", " ");
        String ascii = Normalizer.normalize(cleaned, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        Map<String, String> aliases = teamAliases();
        return aliases.getOrDefault(ascii, ascii);
    }

    private Map<String, String> teamAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("USA", "United States");
        aliases.put("Czech Republic", "Czechia");
        aliases.put("Bosnia & Herzegovina", "Bosnia and Herzegovina");
        aliases.put("Bosnia-Herzegovina", "Bosnia and Herzegovina");
        aliases.put("Curacao", "Curacao");
        aliases.put("Cote d'Ivoire", "Ivory Coast");
        aliases.put("Korea Republic", "South Korea");
        aliases.put("IR Iran", "Iran");
        aliases.put("Cabo Verde", "Cape Verde");
        aliases.put("Congo DR", "DR Congo");
        aliases.put("Türkiye", "Turkey");
        aliases.put("Turkiye", "Turkey");
        return aliases;
    }

    private static class RemoteMatch {

        private String eventId;

        private String eventName;

        private String seasonSlug;

        private String homeTeam;

        private String awayTeam;

        private LocalDate matchDate;

        private LocalTime kickoffTime;

        private String venue;

        private boolean neutral;

        private String status;

        private Integer homeScore;

        private Integer awayScore;

        private Integer halfTimeHomeScore;

        private Integer halfTimeAwayScore;

    }

    private static class ScorePair {

        private final int homeScore;

        private final int awayScore;

        private ScorePair(int homeScore, int awayScore) {
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }

    }

}
