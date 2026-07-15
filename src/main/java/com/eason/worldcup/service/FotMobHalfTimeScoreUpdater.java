package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Service
public class FotMobHalfTimeScoreUpdater {

    private static final Logger log = LoggerFactory.getLogger(FotMobHalfTimeScoreUpdater.class);

    private static final DateTimeFormatter FOTMOB_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private static final Set<String> CLUB_NAME_TOKENS = Set.of(
            "afc", "bk", "cf", "club", "fc", "ff", "fk", "football", "if", "sc", "sk");

    private static final Set<String> GENERIC_TEAM_TOKENS = Set.of(
            "athletic", "city", "club", "football", "sporting", "united");

    private final ObjectMapper objectMapper;

    @Value("${half-time-score.fotmob-update.enabled:true}")
    private boolean enabled;

    @Value("${half-time-score.fotmob-update.matches-url-template:https://www.fotmob.com/api/data/matches?date={date}}")
    private String matchesUrlTemplate;

    @Value("${half-time-score.fotmob-update.details-url-template:https://www.fotmob.com/api/data/matchDetails?matchId={matchId}}")
    private String detailsUrlTemplate;

    @Value("${half-time-score.fotmob-update.timeout-seconds:12}")
    private int timeoutSeconds;

    @Value("${half-time-score.fotmob-update.parallelism:8}")
    private int parallelism;

    @Value("${half-time-score.fotmob-update.target-zone:Asia/Shanghai}")
    private String targetZone;

    @Value("${half-time-score.fotmob-update.cache-path:config/half-time-scores.json}")
    private String cachePath;

    public FotMobHalfTimeScoreUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public int updateSchedules(List<MatchSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }

        Map<String, HalfTimeScoreCacheEntry> cacheEntries = loadCacheEntries();
        int cachedCount = applyCachedScores(schedules, cacheEntries);
        List<MatchSchedule> missingSchedules = findMissingSchedules(schedules);
        if (!enabled || missingSchedules.isEmpty()) {
            logCoverage(schedules, cachedCount, 0, missingSchedules.size());
            return cachedCount;
        }

        ZoneId zoneId = ZoneId.of(targetZone);
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        Map<LocalDate, List<FotMobMatch>> matchesByDate = loadMatchesByDate(
                client,
                buildLookupDates(missingSchedules),
                timeout);
        Map<MatchSchedule, FotMobMatch> scheduleMatches = matchSchedules(
                missingSchedules,
                matchesByDate,
                zoneId);
        Map<Long, ScorePair> halfTimeScores = loadHalfTimeScores(
                client,
                scheduleMatches.values(),
                timeout);

        int downloadedCount = 0;
        for (Map.Entry<MatchSchedule, FotMobMatch> item : scheduleMatches.entrySet()) {
            MatchSchedule schedule = item.getKey();
            FotMobMatch fotMobMatch = item.getValue();
            ScorePair halfTimeScore = halfTimeScores.get(fotMobMatch.matchId);
            if (!isValidHalfTimeScore(schedule, halfTimeScore)) {
                continue;
            }

            applyHalfTimeScore(schedule, halfTimeScore);
            HalfTimeScoreCacheEntry cacheEntry = toCacheEntry(schedule, fotMobMatch.matchId);
            cacheEntries.put(buildCacheKey(schedule), cacheEntry);
            downloadedCount++;
        }

        if (downloadedCount > 0) {
            saveCacheEntries(cacheEntries);
        }
        int unresolvedCount = findMissingSchedules(schedules).size();
        logCoverage(schedules, cachedCount, downloadedCount, unresolvedCount);
        return cachedCount + downloadedCount;
    }

    private List<MatchSchedule> findMissingSchedules(List<MatchSchedule> schedules) {
        return schedules.stream()
                .filter(schedule -> "COMPLETED".equalsIgnoreCase(schedule.getStatus()))
                .filter(schedule -> schedule.getMatchDate() != null)
                .filter(schedule -> schedule.getHomeScore() != null && schedule.getAwayScore() != null)
                .filter(schedule -> schedule.getHalfTimeHomeScore() == null
                        || schedule.getHalfTimeAwayScore() == null)
                .toList();
    }

    private int applyCachedScores(
            List<MatchSchedule> schedules,
            Map<String, HalfTimeScoreCacheEntry> cacheEntries) {
        if (cacheEntries.isEmpty()) {
            return 0;
        }

        Map<String, HalfTimeScoreCacheEntry> entriesByFixture = new HashMap<>();
        for (HalfTimeScoreCacheEntry entry : cacheEntries.values()) {
            entriesByFixture.put(buildFixtureKey(entry), entry);
        }

        int updatedCount = 0;
        for (MatchSchedule schedule : findMissingSchedules(schedules)) {
            HalfTimeScoreCacheEntry cacheEntry = cacheEntries.get(buildCacheKey(schedule));
            if (cacheEntry == null) {
                cacheEntry = entriesByFixture.get(buildFixtureKey(schedule));
            }
            if (!isValidCacheEntry(schedule, cacheEntry)) {
                continue;
            }

            applyHalfTimeScore(
                    schedule,
                    new ScorePair(cacheEntry.getHalfTimeHomeScore(), cacheEntry.getHalfTimeAwayScore()));
            updatedCount++;
        }
        return updatedCount;
    }

    private TreeSet<LocalDate> buildLookupDates(List<MatchSchedule> schedules) {
        TreeSet<LocalDate> dates = new TreeSet<>();
        for (MatchSchedule schedule : schedules) {
            dates.add(schedule.getMatchDate().minusDays(1));
            dates.add(schedule.getMatchDate());
            dates.add(schedule.getMatchDate().plusDays(1));
        }
        return dates;
    }

    private Map<LocalDate, List<FotMobMatch>> loadMatchesByDate(
            HttpClient client,
            Set<LocalDate> dates,
            Duration timeout) {
        List<Supplier<DateMatches>> tasks = new ArrayList<>();
        for (LocalDate date : dates) {
            tasks.add(() -> loadMatchesForDate(client, date, timeout));
        }

        Map<LocalDate, List<FotMobMatch>> result = new HashMap<>();
        for (DateMatches dateMatches : executeTasks(tasks)) {
            result.put(dateMatches.date, dateMatches.matches);
        }
        return result;
    }

    private DateMatches loadMatchesForDate(HttpClient client, LocalDate date, Duration timeout) {
        String url = matchesUrlTemplate.replace("{date}", date.format(FOTMOB_DATE_FORMATTER));
        try {
            JsonNode root = downloadJsonWithRetry(client, url, timeout, 2);
            List<FotMobMatch> matches = new ArrayList<>();
            for (JsonNode league : root.path("leagues")) {
                String leagueName = league.path("name").asText("");
                for (JsonNode match : league.path("matches")) {
                    FotMobMatch fotMobMatch = parseFotMobMatch(match, leagueName);
                    if (fotMobMatch != null) {
                        matches.add(fotMobMatch);
                    }
                }
            }
            return new DateMatches(date, matches);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new DateMatches(date, List.of());
        } catch (Exception ex) {
            log.debug("Unable to load FotMob matches for {}: {}", date, ex.getMessage());
            return new DateMatches(date, List.of());
        }
    }

    private FotMobMatch parseFotMobMatch(JsonNode match, String leagueName) {
        long matchId = match.path("id").asLong(0L);
        String homeTeam = readTeamName(match.path("home"));
        String awayTeam = readTeamName(match.path("away"));
        if (matchId <= 0L || homeTeam.isBlank() || awayTeam.isBlank()) {
            return null;
        }

        JsonNode status = match.path("status");
        boolean finished = status.path("finished").asBoolean(false);
        Integer homeScore = readInteger(match.path("home").path("score"));
        Integer awayScore = readInteger(match.path("away").path("score"));
        OffsetDateTime kickoffTime = parseOffsetDateTime(status.path("utcTime").asText(""));
        return new FotMobMatch(
                matchId,
                leagueName,
                homeTeam,
                awayTeam,
                homeScore,
                awayScore,
                kickoffTime,
                finished);
    }

    private String readTeamName(JsonNode team) {
        String longName = team.path("longName").asText("");
        if (!longName.isBlank()) {
            return longName;
        }
        return team.path("name").asText("");
    }

    private Integer readInteger(JsonNode value) {
        if (value == null || value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isInt() || value.isLong()) {
            return value.asInt();
        }
        String text = value.asText("");
        return text.matches("-?\\d+") ? Integer.valueOf(text) : null;
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Map<MatchSchedule, FotMobMatch> matchSchedules(
            List<MatchSchedule> schedules,
            Map<LocalDate, List<FotMobMatch>> matchesByDate,
            ZoneId zoneId) {
        Map<MatchSchedule, FotMobMatch> result = new LinkedHashMap<>();
        Set<Long> usedMatchIds = new HashSet<>();
        for (MatchSchedule schedule : schedules) {
            FotMobMatch bestMatch = findBestMatch(schedule, matchesByDate, zoneId, usedMatchIds);
            if (bestMatch != null) {
                result.put(schedule, bestMatch);
                usedMatchIds.add(bestMatch.matchId);
            }
        }
        return result;
    }

    private FotMobMatch findBestMatch(
            MatchSchedule schedule,
            Map<LocalDate, List<FotMobMatch>> matchesByDate,
            ZoneId zoneId,
            Set<Long> usedMatchIds) {
        FotMobMatch bestMatch = null;
        int bestScore = -1;
        for (int dateOffset = -1; dateOffset <= 1; dateOffset++) {
            LocalDate lookupDate = schedule.getMatchDate().plusDays(dateOffset);
            for (FotMobMatch candidate : matchesByDate.getOrDefault(lookupDate, List.of())) {
                if (usedMatchIds.contains(candidate.matchId)) {
                    continue;
                }
                int score = calculateMatchScore(schedule, candidate, zoneId);
                if (score > bestScore) {
                    bestMatch = candidate;
                    bestScore = score;
                }
            }
        }
        return bestScore >= 20 ? bestMatch : null;
    }

    private int calculateMatchScore(MatchSchedule schedule, FotMobMatch candidate, ZoneId zoneId) {
        if (!candidate.finished
                || !teamNamesMatch(schedule.getHomeTeamEn(), candidate.homeTeam)
                || !teamNamesMatch(schedule.getAwayTeamEn(), candidate.awayTeam)) {
            return -1;
        }

        int score = 20;
        if (canonicalTeamName(schedule.getHomeTeamEn()).equals(canonicalTeamName(candidate.homeTeam))) {
            score += 4;
        }
        if (canonicalTeamName(schedule.getAwayTeamEn()).equals(canonicalTeamName(candidate.awayTeam))) {
            score += 4;
        }
        if (schedule.getHomeScore().equals(candidate.homeScore)
                && schedule.getAwayScore().equals(candidate.awayScore)) {
            score += 10;
        }
        if (leagueMatches(schedule.getCompetition(), candidate.leagueName)) {
            score += 4;
        }
        if (candidate.kickoffTime != null && schedule.getKickoffTime() != null) {
            LocalDateTime scheduleTime = LocalDateTime.of(schedule.getMatchDate(), schedule.getKickoffTime());
            LocalDateTime candidateTime = candidate.kickoffTime.atZoneSameInstant(zoneId).toLocalDateTime();
            long minutes = Math.abs(Duration.between(scheduleTime, candidateTime).toMinutes());
            if (minutes <= 240L) {
                score += 4;
            }
        }
        return score;
    }

    private boolean leagueMatches(Competition competition, String leagueName) {
        String normalizedLeague = normalizeText(leagueName);
        return switch (competition) {
            case WORLD_CUP -> normalizedLeague.contains("worldcup");
            case CHAMPIONS_LEAGUE -> normalizedLeague.contains("championsleague");
            case NORWEGIAN_ELITESERIEN -> normalizedLeague.contains("eliteserien");
            case SWEDISH_ALLSVENSKAN -> normalizedLeague.contains("allsvenskan");
            case FINNISH_VEIKKAUSLIIGA -> normalizedLeague.contains("veikkausliiga");
            case EUROPA_LEAGUE -> normalizedLeague.contains("europaleague");
            case BRAZIL_SERIE_A -> normalizedLeague.contains("seriea");
            case MLS -> normalizedLeague.contains("majorleaguesoccer");
            case K_LEAGUE_1 -> normalizedLeague.contains("kleague1");
        };
    }

    private boolean teamNamesMatch(String sourceName, String candidateName) {
        String source = canonicalTeamName(sourceName);
        String candidate = canonicalTeamName(candidateName);
        if (source.isBlank() || candidate.isBlank()) {
            return false;
        }
        if (source.equals(candidate)) {
            return true;
        }
        if (Math.min(source.length(), candidate.length()) >= 3
                && (source.contains(candidate) || candidate.contains(source))) {
            return true;
        }

        Set<String> sourceTokens = meaningfulTeamTokens(sourceName);
        Set<String> candidateTokens = meaningfulTeamTokens(candidateName);
        sourceTokens.retainAll(candidateTokens);
        return sourceTokens.stream().anyMatch(token -> token.length() >= 4);
    }

    private String canonicalTeamName(String teamName) {
        List<String> tokens = tokenize(teamName);
        tokens.removeIf(CLUB_NAME_TOKENS::contains);
        String canonicalName = String.join("", tokens);
        return switch (canonicalName) {
            case "usa", "unitedstatesofamerica" -> "unitedstates";
            default -> canonicalName;
        };
    }

    private Set<String> meaningfulTeamTokens(String teamName) {
        Set<String> tokens = new HashSet<>(tokenize(teamName));
        tokens.removeAll(CLUB_NAME_TOKENS);
        tokens.removeAll(GENERIC_TEAM_TOKENS);
        return tokens;
    }

    private List<String> tokenize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        if (normalized.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(normalized.split("\\s+")));
    }

    private String normalizeText(String value) {
        return String.join("", tokenize(value));
    }

    private Map<Long, ScorePair> loadHalfTimeScores(
            HttpClient client,
            Iterable<FotMobMatch> matches,
            Duration timeout) {
        Map<Long, FotMobMatch> uniqueMatches = new LinkedHashMap<>();
        for (FotMobMatch match : matches) {
            uniqueMatches.put(match.matchId, match);
        }

        List<Supplier<HalfTimeLookup>> tasks = new ArrayList<>();
        for (FotMobMatch match : uniqueMatches.values()) {
            tasks.add(() -> loadHalfTimeScore(client, match, timeout));
        }

        Map<Long, ScorePair> result = new HashMap<>();
        for (HalfTimeLookup lookup : executeTasks(tasks)) {
            if (lookup.score != null) {
                result.put(lookup.matchId, lookup.score);
            }
        }
        return result;
    }

    private HalfTimeLookup loadHalfTimeScore(HttpClient client, FotMobMatch match, Duration timeout) {
        String url = detailsUrlTemplate.replace("{matchId}", Long.toString(match.matchId));
        try {
            JsonNode root = downloadJsonWithRetry(client, url, timeout, 2);
            return new HalfTimeLookup(match.matchId, parseHalfTimeScore(root, match));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new HalfTimeLookup(match.matchId, null);
        } catch (Exception ex) {
            log.debug("Unable to load FotMob match details {}: {}", match.matchId, ex.getMessage());
            return new HalfTimeLookup(match.matchId, null);
        }
    }

    private ScorePair parseHalfTimeScore(JsonNode root, FotMobMatch match) {
        JsonNode eventContainer = root.path("content").path("matchFacts").path("events");
        if (eventContainer.isObject()) {
            ScorePair score = parseHalfTimeEventList(eventContainer.path("events"));
            if (score != null) {
                return score;
            }
        } else if (eventContainer.isArray()) {
            for (JsonNode eventGroup : eventContainer) {
                ScorePair score = eventGroup.has("events")
                        ? parseHalfTimeEventList(eventGroup.path("events"))
                        : parseHalfTimeEventList(eventContainer);
                if (score != null) {
                    return score;
                }
            }
        }

        if (Integer.valueOf(0).equals(match.homeScore) && Integer.valueOf(0).equals(match.awayScore)) {
            return new ScorePair(0, 0);
        }
        return null;
    }

    private ScorePair parseHalfTimeEventList(JsonNode events) {
        if (!events.isArray()) {
            return null;
        }
        for (JsonNode event : events) {
            if (!"Half".equalsIgnoreCase(event.path("type").asText(""))) {
                continue;
            }
            String halfLabel = event.path("halfStrShort").asText("");
            String halfKey = event.path("halfStrKey").asText("");
            if (!"HT".equalsIgnoreCase(halfLabel)
                    && !"halftime_short".equalsIgnoreCase(halfKey)) {
                continue;
            }
            Integer homeScore = readInteger(event.path("homeScore"));
            Integer awayScore = readInteger(event.path("awayScore"));
            if (homeScore != null && awayScore != null) {
                return new ScorePair(homeScore, awayScore);
            }
        }

        int homeScore = 0;
        int awayScore = 0;
        boolean sawGoal = false;
        for (JsonNode event : events) {
            if (!"Goal".equalsIgnoreCase(event.path("type").asText(""))
                    || event.path("isPenaltyShootoutEvent").asBoolean(false)) {
                continue;
            }
            sawGoal = true;
            int minute = event.path("time").asInt(Integer.MAX_VALUE);
            if (minute > 45) {
                continue;
            }
            if (event.path("isHome").asBoolean(false)) {
                homeScore++;
            } else {
                awayScore++;
            }
        }
        if (sawGoal) {
            return new ScorePair(homeScore, awayScore);
        }
        return null;
    }

    private JsonNode downloadJsonWithRetry(
            HttpClient client,
            String url,
            Duration timeout,
            int maxAttempts) throws IOException, InterruptedException {
        IOException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return downloadJson(client, url, timeout);
            } catch (IOException ex) {
                lastException = ex;
                if (attempt < maxAttempts) {
                    Thread.sleep(300L * attempt);
                }
            }
        }
        throw lastException == null ? new IOException("Unable to download FotMob data") : lastException;
    }

    private JsonNode downloadJson(HttpClient client, String url, Duration timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Accept", "application/json, text/plain, */*")
                .header("User-Agent", "Mozilla/5.0 (compatible; lottery-football/1.0)")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Unexpected HTTP status " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private <T> List<T> executeTasks(List<Supplier<T>> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        int threadCount = Math.max(1, Math.min(Math.min(16, parallelism), tasks.size()));
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<CompletableFuture<T>> futures = tasks.stream()
                    .map(task -> CompletableFuture.supplyAsync(task, executor))
                    .toList();
            List<T> result = new ArrayList<>();
            for (CompletableFuture<T> future : futures) {
                result.add(future.join());
            }
            return result;
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean isValidCacheEntry(MatchSchedule schedule, HalfTimeScoreCacheEntry entry) {
        if (entry == null
                || entry.getHalfTimeHomeScore() == null
                || entry.getHalfTimeAwayScore() == null
                || entry.getFullTimeHomeScore() == null
                || entry.getFullTimeAwayScore() == null
                || !entry.getFullTimeHomeScore().equals(schedule.getHomeScore())
                || !entry.getFullTimeAwayScore().equals(schedule.getAwayScore())) {
            return false;
        }
        return isValidHalfTimeScore(
                schedule,
                new ScorePair(entry.getHalfTimeHomeScore(), entry.getHalfTimeAwayScore()));
    }

    private boolean isValidHalfTimeScore(MatchSchedule schedule, ScorePair score) {
        return score != null
                && score.homeScore >= 0
                && score.awayScore >= 0
                && schedule.getHomeScore() != null
                && schedule.getAwayScore() != null
                && score.homeScore <= schedule.getHomeScore()
                && score.awayScore <= schedule.getAwayScore();
    }

    private void applyHalfTimeScore(MatchSchedule schedule, ScorePair score) {
        schedule.setHalfTimeHomeScore(score.homeScore);
        schedule.setHalfTimeAwayScore(score.awayScore);
    }

    private Map<String, HalfTimeScoreCacheEntry> loadCacheEntries() {
        if (cachePath == null || cachePath.isBlank()) {
            return new LinkedHashMap<>();
        }
        Path path = Path.of(cachePath);
        if (!Files.isRegularFile(path)) {
            return new LinkedHashMap<>();
        }
        try {
            List<HalfTimeScoreCacheEntry> entries = objectMapper.readValue(
                    path.toFile(),
                    new TypeReference<>() {
                    });
            Map<String, HalfTimeScoreCacheEntry> result = new LinkedHashMap<>();
            for (HalfTimeScoreCacheEntry entry : entries) {
                if (entry.getCompetition() != null && entry.getMatchId() != null) {
                    result.put(buildCacheKey(entry.getCompetition(), entry.getMatchId()), entry);
                }
            }
            return result;
        } catch (Exception ex) {
            log.warn("Unable to read half-time score cache {}: {}", cachePath, ex.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private void saveCacheEntries(Map<String, HalfTimeScoreCacheEntry> cacheEntries) {
        if (cachePath == null || cachePath.isBlank() || cacheEntries.isEmpty()) {
            return;
        }
        List<HalfTimeScoreCacheEntry> entries = new ArrayList<>(cacheEntries.values());
        entries.sort(Comparator
                .comparing(HalfTimeScoreCacheEntry::getCompetition)
                .thenComparing(HalfTimeScoreCacheEntry::getMatchDate)
                .thenComparing(HalfTimeScoreCacheEntry::getMatchId));
        Path path = Path.of(cachePath);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), entries);
        } catch (Exception ex) {
            log.warn("Unable to write half-time score cache {}: {}", cachePath, ex.getMessage());
        }
    }

    private HalfTimeScoreCacheEntry toCacheEntry(MatchSchedule schedule, long fotMobMatchId) {
        HalfTimeScoreCacheEntry entry = new HalfTimeScoreCacheEntry();
        entry.setCompetition(schedule.getCompetition());
        entry.setMatchId(schedule.getMatchId());
        entry.setMatchDate(schedule.getMatchDate());
        entry.setHomeTeamEn(schedule.getHomeTeamEn());
        entry.setAwayTeamEn(schedule.getAwayTeamEn());
        entry.setFullTimeHomeScore(schedule.getHomeScore());
        entry.setFullTimeAwayScore(schedule.getAwayScore());
        entry.setHalfTimeHomeScore(schedule.getHalfTimeHomeScore());
        entry.setHalfTimeAwayScore(schedule.getHalfTimeAwayScore());
        entry.setFotMobMatchId(fotMobMatchId);
        entry.setSource("FotMob");
        entry.setUpdatedAt(LocalDateTime.now().toString());
        return entry;
    }

    private String buildCacheKey(MatchSchedule schedule) {
        return buildCacheKey(schedule.getCompetition(), schedule.getMatchId());
    }

    private String buildCacheKey(Competition competition, String matchId) {
        return competition.name() + "|" + matchId;
    }

    private String buildFixtureKey(MatchSchedule schedule) {
        return schedule.getCompetition().name() + "|" + schedule.getMatchDate()
                + "|" + canonicalTeamName(schedule.getHomeTeamEn())
                + "|" + canonicalTeamName(schedule.getAwayTeamEn())
                + "|" + schedule.getHomeScore()
                + "|" + schedule.getAwayScore();
    }

    private String buildFixtureKey(HalfTimeScoreCacheEntry entry) {
        if (entry.getCompetition() == null || entry.getMatchDate() == null) {
            return "";
        }
        return entry.getCompetition().name() + "|" + entry.getMatchDate()
                + "|" + canonicalTeamName(entry.getHomeTeamEn())
                + "|" + canonicalTeamName(entry.getAwayTeamEn())
                + "|" + entry.getFullTimeHomeScore()
                + "|" + entry.getFullTimeAwayScore();
    }

    private void logCoverage(
            List<MatchSchedule> schedules,
            int cachedCount,
            int downloadedCount,
            int unresolvedCount) {
        long completedCount = schedules.stream()
                .filter(schedule -> "COMPLETED".equalsIgnoreCase(schedule.getStatus()))
                .filter(schedule -> schedule.getHomeScore() != null && schedule.getAwayScore() != null)
                .count();
        long halfTimeCount = schedules.stream()
                .filter(schedule -> "COMPLETED".equalsIgnoreCase(schedule.getStatus()))
                .filter(schedule -> schedule.getHomeScore() != null && schedule.getAwayScore() != null)
                .filter(schedule -> schedule.getHalfTimeHomeScore() != null
                        && schedule.getHalfTimeAwayScore() != null)
                .count();
        log.info(
                "Half-time score coverage: {}/{}, applied {} cached rows, downloaded {} rows, unresolved {} rows",
                halfTimeCount,
                completedCount,
                cachedCount,
                downloadedCount,
                unresolvedCount);
    }

    private static class DateMatches {

        private final LocalDate date;

        private final List<FotMobMatch> matches;

        private DateMatches(LocalDate date, List<FotMobMatch> matches) {
            this.date = date;
            this.matches = matches;
        }

    }

    private static class FotMobMatch {

        private final long matchId;

        private final String leagueName;

        private final String homeTeam;

        private final String awayTeam;

        private final Integer homeScore;

        private final Integer awayScore;

        private final OffsetDateTime kickoffTime;

        private final boolean finished;

        private FotMobMatch(
                long matchId,
                String leagueName,
                String homeTeam,
                String awayTeam,
                Integer homeScore,
                Integer awayScore,
                OffsetDateTime kickoffTime,
                boolean finished) {
            this.matchId = matchId;
            this.leagueName = leagueName;
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
            this.kickoffTime = kickoffTime;
            this.finished = finished;
        }

    }

    private static class HalfTimeLookup {

        private final long matchId;

        private final ScorePair score;

        private HalfTimeLookup(long matchId, ScorePair score) {
            this.matchId = matchId;
            this.score = score;
        }

    }

    private static class ScorePair {

        private final int homeScore;

        private final int awayScore;

        private ScorePair(int homeScore, int awayScore) {
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }

    }

    public static class HalfTimeScoreCacheEntry {

        private Competition competition;

        private String matchId;

        private LocalDate matchDate;

        private String homeTeamEn;

        private String awayTeamEn;

        private Integer fullTimeHomeScore;

        private Integer fullTimeAwayScore;

        private Integer halfTimeHomeScore;

        private Integer halfTimeAwayScore;

        private Long fotMobMatchId;

        private String source;

        private String updatedAt;

        public Competition getCompetition() {
            return competition;
        }

        public void setCompetition(Competition competition) {
            this.competition = competition;
        }

        public String getMatchId() {
            return matchId;
        }

        public void setMatchId(String matchId) {
            this.matchId = matchId;
        }

        public LocalDate getMatchDate() {
            return matchDate;
        }

        public void setMatchDate(LocalDate matchDate) {
            this.matchDate = matchDate;
        }

        public String getHomeTeamEn() {
            return homeTeamEn;
        }

        public void setHomeTeamEn(String homeTeamEn) {
            this.homeTeamEn = homeTeamEn;
        }

        public String getAwayTeamEn() {
            return awayTeamEn;
        }

        public void setAwayTeamEn(String awayTeamEn) {
            this.awayTeamEn = awayTeamEn;
        }

        public Integer getFullTimeHomeScore() {
            return fullTimeHomeScore;
        }

        public void setFullTimeHomeScore(Integer fullTimeHomeScore) {
            this.fullTimeHomeScore = fullTimeHomeScore;
        }

        public Integer getFullTimeAwayScore() {
            return fullTimeAwayScore;
        }

        public void setFullTimeAwayScore(Integer fullTimeAwayScore) {
            this.fullTimeAwayScore = fullTimeAwayScore;
        }

        public Integer getHalfTimeHomeScore() {
            return halfTimeHomeScore;
        }

        public void setHalfTimeHomeScore(Integer halfTimeHomeScore) {
            this.halfTimeHomeScore = halfTimeHomeScore;
        }

        public Integer getHalfTimeAwayScore() {
            return halfTimeAwayScore;
        }

        public void setHalfTimeAwayScore(Integer halfTimeAwayScore) {
            this.halfTimeAwayScore = halfTimeAwayScore;
        }

        public Long getFotMobMatchId() {
            return fotMobMatchId;
        }

        public void setFotMobMatchId(Long fotMobMatchId) {
            this.fotMobMatchId = fotMobMatchId;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

    }

}
