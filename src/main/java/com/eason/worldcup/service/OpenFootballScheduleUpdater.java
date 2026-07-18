package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
public class OpenFootballScheduleUpdater {

    private static final Logger log = LoggerFactory.getLogger(OpenFootballScheduleUpdater.class);

    private static final int TOURNAMENT_YEAR = 2026;

    private static final Pattern DATE_LINE_PATTERN = Pattern.compile(
            "^(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+([A-Za-z]+)\\s+(\\d{1,2})\\s*$");

    private static final Pattern MATCH_LINE_PATTERN = Pattern.compile(
            "^\\s*(\\d{1,2}:\\d{2})\\s+UTC([+-]\\d{1,2})\\s+(.+?)\\s+@\\s+(.+?)\\s*$");

    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "^(.+?)\\s+(\\d+)\\s*-\\s*(\\d+)(?:\\s*\\(([^)]*)\\))?\\s+(.+?)\\s*$");

    private static final Pattern UPCOMING_PATTERN = Pattern.compile("^(.+?)\\s+v\\s+(.+?)\\s*$");

    @Value("${worldcup.schedule-update.enabled:true}")
    private boolean enabled;

    @Value("${worldcup.schedule-update.source-url:https://raw.githubusercontent.com/openfootball/worldcup/master/2026--usa/cup.txt}")
    private String sourceUrl;

    @Value("${worldcup.schedule-update.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${worldcup.schedule-update.target-zone:Asia/Shanghai}")
    private String targetZone;

    @Value("${data-refresh.days-back:30}")
    private int daysBack;

    @Value("${data-refresh.days-forward:30}")
    private int daysForward;

    @Value("${data-refresh.target-zone:Asia/Shanghai}")
    private String refreshTargetZone;

    public int updateSchedules(List<MatchSchedule> schedules) {
        if (!enabled) {
            log.info("OpenFootball schedule update is disabled.");
            return 0;
        }
        try {
            String content = downloadContent();
            LocalDate today = LocalDate.now(ZoneId.of(refreshTargetZone));
            LocalDate startDate = today.minusDays(normalizeWindowDays(daysBack));
            LocalDate endDate = today.plusDays(normalizeWindowDays(daysForward));
            List<RemoteMatch> remoteMatches = parseMatches(content).stream()
                    .filter(match -> !match.matchDate.isBefore(startDate))
                    .filter(match -> !match.matchDate.isAfter(endDate))
                    .toList();
            int updatedCount = mergeSchedules(schedules, remoteMatches);
            log.info(
                    "Updated {} World Cup schedule rows from OpenFootball source {} for {} to {}.",
                    updatedCount,
                    sourceUrl,
                    startDate,
                    endDate);
            return updatedCount;
        } catch (Exception ex) {
            log.warn("Failed to update World Cup schedule from OpenFootball; using bundled CSV data. Source: {}", sourceUrl, ex);
            return 0;
        }
    }

    private String downloadContent() throws IOException, InterruptedException {
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl))
                .timeout(timeout)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Unexpected HTTP status " + response.statusCode());
        }
        return response.body();
    }

    private List<RemoteMatch> parseMatches(String content) {
        List<RemoteMatch> matches = new ArrayList<>();
        LocalDate currentDate = null;
        for (String line : content.split("\\R")) {
            Matcher dateMatcher = DATE_LINE_PATTERN.matcher(line.trim());
            if (dateMatcher.matches()) {
                currentDate = LocalDate.of(
                        TOURNAMENT_YEAR,
                        parseMonth(dateMatcher.group(1)),
                        Integer.parseInt(dateMatcher.group(2)));
                continue;
            }
            if (currentDate == null) {
                continue;
            }
            Matcher matchMatcher = MATCH_LINE_PATTERN.matcher(line);
            if (!matchMatcher.matches()) {
                continue;
            }
            RemoteMatch remoteMatch = parseMatchLine(
                    currentDate,
                    matchMatcher.group(1),
                    matchMatcher.group(2),
                    matchMatcher.group(3),
                    matchMatcher.group(4));
            if (remoteMatch != null) {
                matches.add(remoteMatch);
            }
        }
        return matches;
    }

    private RemoteMatch parseMatchLine(LocalDate sourceDate, String timeText, String offsetText, String matchText, String venue) {
        Matcher resultMatcher = RESULT_PATTERN.matcher(matchText.trim());
        if (resultMatcher.matches()) {
            String resultNote = resultMatcher.group(4);
            if (isExtraTimeResult(resultNote)) {
                return buildRemoteMatch(
                        sourceDate,
                        timeText,
                        offsetText,
                        resultMatcher.group(1),
                        resultMatcher.group(5),
                        venue,
                        true,
                        null,
                        null);
            }
            return buildRemoteMatch(
                    sourceDate,
                    timeText,
                    offsetText,
                    resultMatcher.group(1),
                    resultMatcher.group(5),
                    venue,
                    true,
                    Integer.parseInt(resultMatcher.group(2)),
                    Integer.parseInt(resultMatcher.group(3)));
        }

        Matcher upcomingMatcher = UPCOMING_PATTERN.matcher(matchText.trim());
        if (upcomingMatcher.matches()) {
            return buildRemoteMatch(
                    sourceDate,
                    timeText,
                    offsetText,
                    upcomingMatcher.group(1),
                    upcomingMatcher.group(2),
                    venue,
                    false,
                    null,
                    null);
        }

        log.debug("Skipped unrecognized OpenFootball match line: {}", matchText);
        return null;
    }

    private boolean isExtraTimeResult(String resultNote) {
        if (resultNote == null || resultNote.isBlank()) {
            return false;
        }
        String normalized = resultNote.toLowerCase(Locale.ROOT);
        return normalized.contains("a.e.t") || normalized.contains("extra time");
    }

    private RemoteMatch buildRemoteMatch(
            LocalDate sourceDate,
            String timeText,
            String offsetText,
            String homeTeam,
            String awayTeam,
            String venue,
            boolean completed,
            Integer homeScore,
            Integer awayScore) {
        LocalTime sourceTime = LocalTime.parse(timeText);
        ZoneOffset sourceOffset = ZoneOffset.of(normalizeOffset(offsetText));
        ZonedDateTime targetDateTime = LocalDateTime.of(sourceDate, sourceTime)
                .atOffset(sourceOffset)
                .atZoneSameInstant(ZoneId.of(targetZone));

        RemoteMatch match = new RemoteMatch();
        match.homeTeam = normalizeTeamName(homeTeam);
        match.awayTeam = normalizeTeamName(awayTeam);
        match.matchDate = targetDateTime.toLocalDate();
        match.kickoffTime = targetDateTime.toLocalTime();
        match.venue = venue.trim();
        match.completed = completed;
        match.homeScore = homeScore;
        match.awayScore = awayScore;
        return match;
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
                schedule = toSchedule(remoteMatch);
                schedules.add(schedule);
                scheduleByTeams.put(buildTeamKey(remoteMatch.homeTeam, remoteMatch.awayTeam), schedule);
                updatedCount++;
                continue;
            }
            schedule.setMatchDate(remoteMatch.matchDate);
            schedule.setKickoffTime(remoteMatch.kickoffTime);
            if (!remoteMatch.venue.isBlank()) {
                schedule.setVenue(remoteMatch.venue);
            }
            if (remoteMatch.completed) {
                schedule.setStatus("COMPLETED");
                schedule.setHomeScore(remoteMatch.homeScore);
                schedule.setAwayScore(remoteMatch.awayScore);
            } else if (!"COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
                schedule.setStatus("SCHEDULED");
                schedule.setHomeScore(null);
                schedule.setAwayScore(null);
            }
            updatedCount++;
        }
        return updatedCount;
    }

    private MatchSchedule toSchedule(RemoteMatch remoteMatch) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.WORLD_CUP);
        schedule.setMatchId("OPENFOOTBALL-2026-"
                + remoteMatch.matchDate
                + "-"
                + buildTeamKey(remoteMatch.homeTeam, remoteMatch.awayTeam).replace('|', '-'));
        schedule.setMatchDate(remoteMatch.matchDate);
        schedule.setKickoffTime(remoteMatch.kickoffTime);
        schedule.setGroupName(Competition.WORLD_CUP.getDisplayName());
        schedule.setHomeTeamCn(ClubTeamNameTranslator.translate(Competition.WORLD_CUP, remoteMatch.homeTeam));
        schedule.setAwayTeamCn(ClubTeamNameTranslator.translate(Competition.WORLD_CUP, remoteMatch.awayTeam));
        schedule.setHomeTeamEn(remoteMatch.homeTeam);
        schedule.setAwayTeamEn(remoteMatch.awayTeam);
        schedule.setVenue(remoteMatch.venue);
        schedule.setNeutral(true);
        schedule.setStatus(remoteMatch.completed ? "COMPLETED" : "SCHEDULED");
        schedule.setHomeScore(remoteMatch.homeScore);
        schedule.setAwayScore(remoteMatch.awayScore);
        return schedule;
    }

    private Month parseMonth(String monthText) {
        String normalized = monthText.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "jan", "january" -> Month.JANUARY;
            case "feb", "february" -> Month.FEBRUARY;
            case "mar", "march" -> Month.MARCH;
            case "apr", "april" -> Month.APRIL;
            case "may" -> Month.MAY;
            case "jun", "june" -> Month.JUNE;
            case "jul", "july" -> Month.JULY;
            case "aug", "august" -> Month.AUGUST;
            case "sep", "sept", "september" -> Month.SEPTEMBER;
            case "oct", "october" -> Month.OCTOBER;
            case "nov", "november" -> Month.NOVEMBER;
            case "dec", "december" -> Month.DECEMBER;
            default -> throw new IllegalArgumentException("Unsupported month: " + monthText);
        };
    }

    private String normalizeOffset(String offsetText) {
        int hours = Integer.parseInt(offsetText);
        return String.format(Locale.ROOT, "%+03d:00", hours);
    }

    private int normalizeWindowDays(int value) {
        return Math.max(0, Math.min(365, value));
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

        private String homeTeam;

        private String awayTeam;

        private LocalDate matchDate;

        private LocalTime kickoffTime;

        private String venue;

        private boolean completed;

        private Integer homeScore;

        private Integer awayScore;

    }

}
