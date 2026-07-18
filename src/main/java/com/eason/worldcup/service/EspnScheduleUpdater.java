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

    @Value("${worldcup.espn-update.target-zone:Asia/Shanghai}")
    private String targetZone;

    @Value("${data-refresh.days-back:30}")
    private int daysBack;

    @Value("${data-refresh.days-forward:30}")
    private int daysForward;

    @Value("${data-refresh.target-zone:Asia/Shanghai}")
    private String refreshTargetZone;

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
            LocalDate today = LocalDate.now(ZoneId.of(refreshTargetZone));
            LocalDate startDate = today.minusDays(normalizeWindowDays(daysBack));
            LocalDate endDate = today.plusDays(normalizeWindowDays(daysForward));
            String content = downloadContent(buildScoreboardUrl(startDate, endDate));
            List<RemoteMatch> remoteMatches = parseMatches(content, zoneId);
            int updatedCount = mergeSchedules(schedules, remoteMatches);
            log.info(
                    "Updated {} World Cup schedule rows from ESPN scoreboard for {} to {}.",
                    updatedCount,
                    startDate,
                    endDate);
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
        LocalDate today = LocalDate.now(ZoneId.of(refreshTargetZone));
        LocalDate startDate = today.minusDays(normalizeWindowDays(daysBack));
        LocalDate endDate = today.plusDays(normalizeWindowDays(daysForward));
        List<RemoteMatch> remoteMatches = new ArrayList<>();
        addRemoteMatches(
                remoteMatches,
                championsLeagueQualifyingScoreboardUrlTemplate,
                startDate,
                endDate,
                zoneId);
        addRemoteMatches(
                remoteMatches,
                championsLeagueScoreboardUrlTemplate,
                startDate,
                endDate,
                zoneId);

        int updatedCount = mergeChampionsLeagueSchedules(schedules, remoteMatches);
        log.info(
                "Updated {} Champions League schedule rows from ESPN scoreboard for {} to {}.",
                updatedCount,
                startDate,
                endDate);
        return updatedCount;
    }

    private int normalizeWindowDays(int value) {
        return Math.max(0, Math.min(365, value));
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
        boolean beyondRegulation = isBeyondRegulationStatus(event.path("status"));

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
        if (regulationScore != null) {
            match.homeScore = regulationScore.homeScore;
            match.awayScore = regulationScore.awayScore;
        } else if (!beyondRegulation) {
            match.homeScore = fullHomeScore;
            match.awayScore = fullAwayScore;
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

    private boolean isAfterRegulation(JsonNode detail) {
        Integer displayMinute = readDisplayMinute(detail);
        if (displayMinute != null) {
            return displayMinute > 90;
        }

        JsonNode clockValue = detail.path("clock").path("value");
        return clockValue.isNumber() && clockValue.asDouble() > REGULATION_SECONDS;
    }

    private boolean isBeyondRegulationStatus(JsonNode status) {
        if (status.path("period").asInt(0) > 2) {
            return true;
        }
        JsonNode type = status.path("type");
        String statusText = (type.path("description").asText("") + " "
                + type.path("detail").asText("") + " "
                + type.path("shortDetail").asText(""))
                .toLowerCase(Locale.ROOT);
        return statusText.contains("extra time") || statusText.contains("penalt");
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
        } else if ("LIVE".equals(remoteMatch.status)) {
            schedule.setStatus("LIVE");
            schedule.setHomeScore(remoteMatch.homeScore);
            schedule.setAwayScore(remoteMatch.awayScore);
        } else if (!"COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
            schedule.setStatus("SCHEDULED");
            schedule.setHomeScore(null);
            schedule.setAwayScore(null);
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
        return ClubTeamNameTranslator.translate(Competition.CHAMPIONS_LEAGUE, teamName);
    }

    private String toChineseTeamName(String teamName) {
        return ClubTeamNameTranslator.translate(Competition.WORLD_CUP, teamName);
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
        return Map.of();
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
