package com.eason.worldcup.service;

import com.eason.worldcup.model.MatchSchedule;
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

    private String buildScoreboardUrl(LocalDate startDate, LocalDate endDate) {
        return scoreboardUrlTemplate
                .replace("{start}", startDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE))
                .replace("{end}", endDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
    }

    private String downloadContent(String url) throws IOException, InterruptedException {
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
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
        String homeTeamId = readTeamId(homeCompetitor);
        String awayTeamId = readTeamId(awayCompetitor);
        match.homeTeam = readTeamName(homeCompetitor);
        match.awayTeam = readTeamName(awayCompetitor);
        match.matchDate = targetDateTime.toLocalDate();
        match.kickoffTime = targetDateTime.toLocalTime().withSecond(0).withNano(0);
        match.venue = competition.path("venue").path("fullName").asText("");
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
        JsonNode clock = detail.path("clock");
        JsonNode clockValue = clock.path("value");
        if (clockValue.isNumber()) {
            return clockValue.asDouble() <= REGULATION_SECONDS / 2.0D;
        }

        String displayValue = clock.path("displayValue").asText("");
        Matcher matcher = CLOCK_MINUTE_PATTERN.matcher(displayValue);
        return matcher.find() && Integer.parseInt(matcher.group(1)) <= 45;
    }

    private boolean isAfterRegulation(JsonNode detail) {
        JsonNode clock = detail.path("clock");
        JsonNode clockValue = clock.path("value");
        if (clockValue.isNumber()) {
            return clockValue.asDouble() > REGULATION_SECONDS;
        }

        String displayValue = clock.path("displayValue").asText("");
        Matcher matcher = CLOCK_MINUTE_PATTERN.matcher(displayValue);
        return matcher.find() && Integer.parseInt(matcher.group(1)) > 90;
    }

    private int scoreTotal(Integer homeScore, Integer awayScore) {
        return (homeScore == null ? 0 : homeScore) + (awayScore == null ? 0 : awayScore);
    }

    private int mergeSchedules(List<MatchSchedule> schedules, List<RemoteMatch> remoteMatches) {
        Map<String, MatchSchedule> scheduleByTeams = schedules.stream()
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

    private MatchSchedule toSchedule(RemoteMatch remoteMatch) {
        MatchSchedule schedule = new MatchSchedule();
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

    private void applyRemoteMatch(MatchSchedule schedule, RemoteMatch remoteMatch) {
        schedule.setMatchDate(remoteMatch.matchDate);
        schedule.setKickoffTime(remoteMatch.kickoffTime);
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

        private String homeTeam;

        private String awayTeam;

        private LocalDate matchDate;

        private LocalTime kickoffTime;

        private String venue;

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
