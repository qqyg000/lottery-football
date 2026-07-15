package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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

@Service
public class SportteryMarketSelectionService {

    private static final Logger log = LoggerFactory.getLogger(SportteryMarketSelectionService.class);

    private static final int MAX_QUERY_DAYS = 30;

    private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36";

    private static final Map<Integer, Competition> COMPETITIONS_BY_LEAGUE_ID = Map.of(
            6, Competition.BRAZIL_SERIE_A,
            48, Competition.K_LEAGUE_1,
            50, Competition.MLS,
            51, Competition.NORWEGIAN_ELITESERIEN,
            58, Competition.SWEDISH_ALLSVENSKAN,
            69, Competition.CHAMPIONS_LEAGUE,
            70, Competition.EUROPA_LEAGUE,
            72, Competition.WORLD_CUP,
            2064839, Competition.FINNISH_VEIKKAUSLIIGA);

    private final ObjectMapper objectMapper;

    private final Map<String, SportteryMarketEntry> entriesByMatchId = new LinkedHashMap<>();

    private final Map<LocalDate, LocalDateTime> queriedAtByDate = new HashMap<>();

    private LocalDateTime calculatorQueriedAt;

    @Value("${sporttery.result-update.enabled:true}")
    private boolean enabled;

    @Value("${sporttery.result-update.api-url:https://webapi.sporttery.cn/gateway/uniform/football/getUniformMatchResultV1.qry}")
    private String apiUrl;

    @Value("${sporttery.result-update.calculator-api-url:https://webapi.sporttery.cn/gateway/uniform/football/getMatchCalculatorV1.qry}")
    private String calculatorApiUrl;

    @Value("${sporttery.result-update.odds-history-api-url:https://webapi.sporttery.cn/gateway/uniform/football/getOddsHistoryV1.qry}")
    private String oddsHistoryApiUrl;

    @Value("${sporttery.result-update.source-page-url:https://www.lottery.gov.cn/jc/zqsgkj/}")
    private String sourcePageUrl;

    @Value("${sporttery.result-update.calculator-source-page-url:https://www.sporttery.cn/jc/jsq/zqbf/}")
    private String calculatorSourcePageUrl;

    @Value("${sporttery.result-update.timeout-seconds:12}")
    private int timeoutSeconds;

    @Value("${sporttery.result-update.page-size:100}")
    private int pageSize;

    @Value("${sporttery.result-update.refresh-minutes:30}")
    private int refreshMinutes;

    @Value("${sporttery.result-update.future-days:2}")
    private int futureDays;

    @Value("${sporttery.result-update.target-zone:Asia/Shanghai}")
    private String targetZone;

    @Value("${sporttery.result-update.cache-path:config/sporttery-market-selections.json}")
    private String cachePath;

    private boolean cacheLoaded;

    public SportteryMarketSelectionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public synchronized int updateSelections(List<MatchSchedule> schedules) {
        List<MatchSchedule> supportedSchedules = filterSupportedSchedules(schedules);
        if (supportedSchedules.isEmpty()) {
            return 0;
        }

        ensureCacheLoaded();
        if (enabled) {
            refreshMarketEntries(buildLookupDates(supportedSchedules), false, containsUpcomingSchedule(supportedSchedules));
        }
        int matchedCount = applyMarketEntries(supportedSchedules, true);
        log.debug("Matched {} of {} displayed schedules with Sporttery market data", matchedCount, supportedSchedules.size());
        return matchedCount;
    }

    public synchronized int applyCachedSelections(List<MatchSchedule> schedules) {
        List<MatchSchedule> supportedSchedules = filterSupportedSchedules(schedules);
        if (supportedSchedules.isEmpty()) {
            return 0;
        }

        ensureCacheLoaded();
        int matchedCount = applyMarketEntries(supportedSchedules, false);
        log.debug("Matched {} of {} completed schedules with cached Sporttery data", matchedCount, supportedSchedules.size());
        return matchedCount;
    }

    public synchronized int forceRefresh(LocalDate referenceDate) {
        ensureCacheLoaded();
        if (!enabled) {
            return 0;
        }
        LocalDate effectiveReferenceDate = referenceDate == null
                ? LocalDate.now(resolveTargetZone())
                : referenceDate;
        refreshMarketEntries(buildLookupDates(effectiveReferenceDate), true, true);
        return entriesByMatchId.size();
    }

    private List<MatchSchedule> filterSupportedSchedules(List<MatchSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return List.of();
        }
        Set<Competition> supportedCompetitions = new HashSet<>(COMPETITIONS_BY_LEAGUE_ID.values());
        return schedules.stream()
                .filter(schedule -> schedule != null && schedule.getMatchDate() != null)
                .filter(schedule -> supportedCompetitions.contains(schedule.getCompetition()))
                .toList();
    }

    private boolean containsUpcomingSchedule(List<MatchSchedule> schedules) {
        LocalDate today = LocalDate.now(resolveTargetZone());
        LocalDate lastDate = today.plusDays(normalizedFutureDays());
        return schedules.stream()
                .map(MatchSchedule::getMatchDate)
                .anyMatch(date -> !date.isBefore(today) && !date.isAfter(lastDate));
    }

    private void refreshMarketEntries(
            TreeSet<LocalDate> lookupDates,
            boolean force,
            boolean refreshCalculator) {
        ZoneId zoneId = resolveTargetZone();
        LocalDateTime now = LocalDateTime.now(zoneId);
        TreeSet<LocalDate> datesToRefresh = new TreeSet<>();
        for (LocalDate date : lookupDates) {
            if (force || shouldRefresh(date, now)) {
                datesToRefresh.add(date);
            }
        }
        boolean shouldRefreshCalculator = refreshCalculator && (force || shouldRefreshCalculator(now));
        if (datesToRefresh.isEmpty() && !shouldRefreshCalculator) {
            return;
        }

        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        boolean cacheChanged = false;
        if (shouldRefreshCalculator) {
            try {
                List<SportteryMarketEntry> calculatorEntries = downloadCalculator(client, timeout);
                int storedCount = replaceUpcomingEntries(calculatorEntries, now.toLocalDate());
                calculatorQueriedAt = now;
                cacheChanged = true;
                log.info("Loaded {} current and upcoming Sporttery market rows", storedCount);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Sporttery calculator market query interrupted");
            } catch (Exception ex) {
                log.warn("Unable to query current Sporttery calculator markets: {}", ex.getMessage());
            }
        }
        for (DateRange dateRange : buildDateRanges(datesToRefresh)) {
            try {
                List<SportteryMarketEntry> downloadedEntries = downloadRange(client, dateRange, timeout);
                for (SportteryMarketEntry entry : downloadedEntries) {
                    entriesByMatchId.put(entry.getSportteryMatchId(), entry);
                }
                markRangeQueried(dateRange, now);
                cacheChanged = true;
                log.info(
                        "Loaded {} Sporttery market rows for {} to {}",
                        downloadedEntries.size(),
                        dateRange.startDate(),
                        dateRange.endDate());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn(
                        "Sporttery market query interrupted for {} to {}",
                        dateRange.startDate(),
                        dateRange.endDate());
                break;
            } catch (Exception ex) {
                log.warn(
                        "Unable to query Sporttery markets for {} to {}: {}",
                        dateRange.startDate(),
                        dateRange.endDate(),
                        ex.getMessage());
            }
        }
        if (cacheChanged) {
            saveCache(now);
        }
    }

    private ZoneId resolveTargetZone() {
        try {
            return ZoneId.of(targetZone);
        } catch (Exception ex) {
            log.warn("Invalid Sporttery target zone {}, using Asia/Shanghai", targetZone);
            return ZoneId.of("Asia/Shanghai");
        }
    }

    private TreeSet<LocalDate> buildLookupDates(List<MatchSchedule> schedules) {
        TreeSet<LocalDate> dates = new TreeSet<>();
        for (MatchSchedule schedule : schedules) {
            dates.addAll(buildLookupDates(schedule.getMatchDate()));
        }
        return dates;
    }

    private TreeSet<LocalDate> buildLookupDates(LocalDate referenceDate) {
        TreeSet<LocalDate> dates = new TreeSet<>();
        for (int offset = -1; offset <= normalizedFutureDays(); offset++) {
            dates.add(referenceDate.plusDays(offset));
        }
        return dates;
    }

    private int normalizedFutureDays() {
        return Math.max(0, Math.min(7, futureDays));
    }

    private boolean shouldRefresh(LocalDate date, LocalDateTime now) {
        LocalDateTime lastQueriedAt = queriedAtByDate.get(date);
        if (lastQueriedAt == null) {
            return true;
        }
        if (lastQueriedAt.toLocalDate().isAfter(date.plusDays(1))) {
            return false;
        }
        long ageMinutes = Duration.between(lastQueriedAt, now).toMinutes();
        return ageMinutes >= Math.max(1, refreshMinutes);
    }

    private boolean shouldRefreshCalculator(LocalDateTime now) {
        if (calculatorQueriedAt == null) {
            return true;
        }
        long ageMinutes = Duration.between(calculatorQueriedAt, now).toMinutes();
        return ageMinutes >= Math.max(1, refreshMinutes);
    }

    private List<DateRange> buildDateRanges(TreeSet<LocalDate> dates) {
        List<DateRange> ranges = new ArrayList<>();
        LocalDate rangeStart = null;
        LocalDate rangeEnd = null;
        for (LocalDate date : dates) {
            boolean continuesRange = rangeStart != null
                    && !date.isAfter(rangeEnd.plusDays(1))
                    && ChronoUnit.DAYS.between(rangeStart, date) < MAX_QUERY_DAYS;
            if (!continuesRange) {
                if (rangeStart != null) {
                    ranges.add(new DateRange(rangeStart, rangeEnd));
                }
                rangeStart = date;
            }
            rangeEnd = date;
        }
        if (rangeStart != null) {
            ranges.add(new DateRange(rangeStart, rangeEnd));
        }
        return ranges;
    }

    private List<SportteryMarketEntry> downloadRange(
            HttpClient client,
            DateRange dateRange,
            Duration timeout) throws IOException, InterruptedException {
        SportteryPage firstPage = downloadPage(client, dateRange, 1, timeout);
        List<SportteryMarketEntry> result = new ArrayList<>(firstPage.entries());
        for (int pageNo = 2; pageNo <= firstPage.totalPages(); pageNo++) {
            result.addAll(downloadPage(client, dateRange, pageNo, timeout).entries());
        }
        return result;
    }

    private List<SportteryMarketEntry> downloadCalculator(
            HttpClient client,
            Duration timeout) throws IOException, InterruptedException {
        String separator = calculatorApiUrl.contains("?") ? "&" : "?";
        JsonNode root = downloadJson(
                client,
                calculatorApiUrl + separator + "channel=c",
                "https://www.sporttery.cn",
                calculatorSourcePageUrl,
                timeout,
                "体彩在售接口");
        List<SportteryMarketEntry> entries = new ArrayList<>();
        for (JsonNode group : root.path("value").path("matchInfoList")) {
            for (JsonNode match : group.path("subMatchList")) {
                SportteryMarketEntry entry = parseCalculatorEntry(match);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    private void downloadLatestOdds(
            HttpClient client,
            SportteryMarketEntry entry,
            Duration timeout) throws IOException, InterruptedException {
        String separator = oddsHistoryApiUrl.contains("?") ? "&" : "?";
        JsonNode root = downloadJson(
                client,
                oddsHistoryApiUrl + separator + "matchId=" + entry.getSportteryMatchId(),
                "https://www.sporttery.cn",
                calculatorSourcePageUrl,
                timeout,
                "体彩赔率历史接口");
        JsonNode value = root.path("value");
        LatestMarketOdds normalOdds = parseLatestMarketOdds(value.path("hadList"));
        LatestMarketOdds handicapOdds = parseLatestMarketOdds(value.path("hhadList"));
        if (normalOdds != null) {
            entry.setNormalOdds(normalOdds.odds());
            entry.setNormalAvailable(true);
        }
        if (handicapOdds != null) {
            entry.setHandicapOdds(handicapOdds.odds());
            if (handicapOdds.handicap() != null) {
                entry.setHandicap(handicapOdds.handicap());
            }
        }
        entry.setOddsLookupCompleted(true);
    }

    private LatestMarketOdds parseLatestMarketOdds(JsonNode oddsList) {
        LatestMarketOdds latest = null;
        String latestUpdatedAt = "";
        for (JsonNode item : oddsList) {
            SportteryOdds odds = parseOdds(item);
            if (odds == null) {
                continue;
            }
            String updatedAt = odds.getUpdatedAt() == null ? "" : odds.getUpdatedAt();
            if (latest == null || updatedAt.compareTo(latestUpdatedAt) > 0) {
                latest = new LatestMarketOdds(
                        odds,
                        parseHandicap(item.path("goalLine").asText("")));
                latestUpdatedAt = updatedAt;
            }
        }
        return latest;
    }

    private SportteryPage downloadPage(
            HttpClient client,
            DateRange dateRange,
            int pageNo,
            Duration timeout) throws IOException, InterruptedException {
        String separator = apiUrl.contains("?") ? "&" : "?";
        int effectivePageSize = Math.max(1, Math.min(100, pageSize));
        String url = apiUrl + separator
                + "matchBeginDate=" + dateRange.startDate()
                + "&matchEndDate=" + dateRange.endDate()
                + "&leagueId="
                + "&pageSize=" + effectivePageSize
                + "&pageNo=" + pageNo
                + "&isFix=0"
                + "&matchPage=1"
                + "&pcOrWap=1";
        JsonNode root = downloadJson(
                client,
                url,
                "https://www.lottery.gov.cn",
                sourcePageUrl,
                timeout,
                "体彩赛果接口");
        return parsePage(root);
    }

    private JsonNode downloadJson(
            HttpClient client,
            String url,
            String origin,
            String referer,
            Duration timeout,
            String sourceName) throws IOException, InterruptedException {
        IOException lastException = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .timeout(timeout)
                        .header("Accept", "application/json,text/plain,*/*")
                        .header("Origin", origin)
                        .header("Referer", referer)
                        .header("User-Agent", BROWSER_USER_AGENT)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode());
                }
                JsonNode root = objectMapper.readTree(response.body());
                if (!"0".equals(root.path("errorCode").asText())) {
                    throw new IOException(sourceName + "返回失败：" + root.path("errorMessage").asText("未知错误"));
                }
                return root;
            } catch (IllegalArgumentException ex) {
                throw new IOException(sourceName + "地址无效：" + url, ex);
            } catch (IOException ex) {
                lastException = ex;
                if (attempt == 2) {
                    throw ex;
                }
            }
        }
        throw lastException == null ? new IOException(sourceName + "请求失败") : lastException;
    }

    private SportteryPage parsePage(JsonNode root) {
        JsonNode value = root.path("value");
        int totalPages = Math.max(1, value.path("pages").asInt(1));
        List<SportteryMarketEntry> entries = new ArrayList<>();
        for (JsonNode match : value.path("matchResult")) {
            SportteryMarketEntry entry = parseEntry(match);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return new SportteryPage(entries, totalPages);
    }

    private SportteryMarketEntry parseEntry(JsonNode match) {
        String sportteryMatchId = match.path("matchId").asText("");
        LocalDate matchDate = parseDate(match.path("matchDate").asText(""));
        Competition competition = parseCompetition(match);
        String homeTeam = firstNonBlank(
                match.path("allHomeTeam").asText(""),
                match.path("homeTeam").asText(""));
        String awayTeam = firstNonBlank(
                match.path("allAwayTeam").asText(""),
                match.path("awayTeam").asText(""));
        if (sportteryMatchId.isBlank()
                || matchDate == null
                || competition == null
                || homeTeam.isBlank()
                || awayTeam.isBlank()) {
            return null;
        }

        SportteryMarketEntry entry = new SportteryMarketEntry();
        entry.setSportteryMatchId(sportteryMatchId);
        entry.setSportteryMatchNumber(match.path("matchNumStr").asText(""));
        entry.setMatchDate(matchDate);
        entry.setCompetition(competition);
        entry.setHomeTeam(homeTeam);
        entry.setAwayTeam(awayTeam);
        entry.setCurrentSale(false);
        entry.setNormalOdds(parseOdds(match));
        entry.setNormalAvailable(entry.getNormalOdds() != null);
        entry.setHandicap(parseHandicap(match.path("goalLine").asText("")));
        applyFullTimeScore(entry, match.path("sectionsNo999").asText(""));
        return entry;
    }

    private SportteryMarketEntry parseCalculatorEntry(JsonNode match) {
        String sportteryMatchId = match.path("matchId").asText("");
        LocalDate matchDate = parseDate(match.path("matchDate").asText(""));
        Competition competition = parseCompetition(match);
        String homeTeam = firstNonBlank(
                match.path("homeTeamAllName").asText(""),
                match.path("homeTeamAbbName").asText(""));
        String awayTeam = firstNonBlank(
                match.path("awayTeamAllName").asText(""),
                match.path("awayTeamAbbName").asText(""));
        if (sportteryMatchId.isBlank()
                || matchDate == null
                || competition == null
                || homeTeam.isBlank()
                || awayTeam.isBlank()) {
            return null;
        }

        JsonNode normalMarket = match.path("had");
        JsonNode handicapMarket = match.path("hhad");
        SportteryMarketEntry entry = new SportteryMarketEntry();
        entry.setSportteryMatchId(sportteryMatchId);
        entry.setSportteryMatchNumber(match.path("matchNumStr").asText(""));
        entry.setMatchDate(matchDate);
        entry.setCompetition(competition);
        entry.setHomeTeam(homeTeam);
        entry.setAwayTeam(awayTeam);
        entry.setCurrentSale(true);
        entry.setNormalOdds(parseOdds(normalMarket));
        entry.setHandicapOdds(parseOdds(handicapMarket));
        entry.setNormalAvailable(entry.getNormalOdds() != null);
        entry.setHandicap(entry.getHandicapOdds() != null
                ? parseHandicap(handicapMarket.path("goalLine").asText(""))
                : null);
        entry.setOddsLookupCompleted(true);
        return entry;
    }

    private SportteryOdds parseOdds(JsonNode market) {
        if (market == null) {
            return null;
        }
        Double win = parseOddsValue(market.path("h").asText(""));
        Double draw = parseOddsValue(market.path("d").asText(""));
        Double lose = parseOddsValue(market.path("a").asText(""));
        if (win == null || draw == null || lose == null) {
            return null;
        }
        return new SportteryOdds(win, draw, lose, parseOddsUpdatedAt(market));
    }

    private Double parseOddsValue(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            double odds = Double.parseDouble(value.trim());
            return Double.isFinite(odds) && odds > 0.0D ? odds : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String parseOddsUpdatedAt(JsonNode market) {
        String updateDate = market.path("updateDate").asText("").trim();
        String updateTime = market.path("updateTime").asText("").trim();
        if (updateDate.isBlank()) {
            return updateTime.isBlank() ? null : updateTime;
        }
        return updateTime.isBlank() ? updateDate : updateDate + " " + updateTime;
    }

    private Competition parseCompetition(JsonNode match) {
        Competition competition = COMPETITIONS_BY_LEAGUE_ID.get(match.path("leagueId").asInt(-1));
        if (competition != null) {
            return competition;
        }
        String leagueName = firstNonBlank(
                match.path("leagueNameAbbr").asText(""),
                match.path("leagueAbbName").asText(""));
        return switch (leagueName) {
            case "世界杯" -> Competition.WORLD_CUP;
            case "欧冠" -> Competition.CHAMPIONS_LEAGUE;
            case "挪超" -> Competition.NORWEGIAN_ELITESERIEN;
            case "瑞超" -> Competition.SWEDISH_ALLSVENSKAN;
            case "芬超" -> Competition.FINNISH_VEIKKAUSLIIGA;
            case "欧罗巴" -> Competition.EUROPA_LEAGUE;
            case "巴甲" -> Competition.BRAZIL_SERIE_A;
            case "美职" -> Competition.MLS;
            case "韩职" -> Competition.K_LEAGUE_1;
            default -> null;
        };
    }

    private LocalDate parseDate(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Integer parseHandicap(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!normalized.matches("[+-]?\\d+")) {
            return null;
        }
        int handicap = Integer.parseInt(normalized);
        return handicap == 0 ? null : handicap;
    }

    private void applyFullTimeScore(SportteryMarketEntry entry, String scoreText) {
        String[] parts = scoreText == null ? new String[0] : scoreText.trim().split(":");
        if (parts.length != 2 || !parts[0].matches("\\d+") || !parts[1].matches("\\d+")) {
            return;
        }
        entry.setHomeScore(Integer.valueOf(parts[0]));
        entry.setAwayScore(Integer.valueOf(parts[1]));
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first.trim() : second == null ? "" : second.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void markRangeQueried(DateRange dateRange, LocalDateTime queriedAt) {
        LocalDate date = dateRange.startDate();
        while (!date.isAfter(dateRange.endDate())) {
            queriedAtByDate.put(date, queriedAt);
            date = date.plusDays(1);
        }
    }

    private int replaceUpcomingEntries(List<SportteryMarketEntry> downloadedEntries, LocalDate today) {
        entriesByMatchId.entrySet().removeIf(item -> Boolean.TRUE.equals(item.getValue().getCurrentSale()));
        LocalDate lastDate = today.plusDays(normalizedFutureDays());
        int storedCount = 0;
        for (SportteryMarketEntry entry : downloadedEntries) {
            if (entry.getMatchDate().isBefore(today) || entry.getMatchDate().isAfter(lastDate)) {
                continue;
            }
            entriesByMatchId.put(entry.getSportteryMatchId(), entry);
            storedCount++;
        }
        return storedCount;
    }

    private int applyMarketEntries(List<MatchSchedule> schedules, boolean lookupMissingOdds) {
        Map<Competition, List<SportteryMarketEntry>> entriesByCompetition = new HashMap<>();
        for (SportteryMarketEntry entry : entriesByMatchId.values()) {
            entriesByCompetition.computeIfAbsent(entry.getCompetition(), ignored -> new ArrayList<>()).add(entry);
        }

        int matchedCount = 0;
        Set<String> usedMatchIds = new HashSet<>();
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        HttpClient oddsClient = null;
        boolean oddsCacheChanged = false;
        boolean oddsLookupInterrupted = false;
        for (MatchSchedule schedule : schedules) {
            clearSelection(schedule);
            SportteryMarketEntry entry = findBestEntry(
                    schedule,
                    entriesByCompetition.getOrDefault(schedule.getCompetition(), List.of()),
                    usedMatchIds);
            if (entry == null) {
                continue;
            }
            if (lookupMissingOdds && enabled && !oddsLookupInterrupted && needsOddsLookup(entry)) {
                if (oddsClient == null) {
                    oddsClient = HttpClient.newBuilder()
                            .connectTimeout(timeout)
                            .version(HttpClient.Version.HTTP_1_1)
                            .build();
                }
                try {
                    downloadLatestOdds(oddsClient, entry, timeout);
                    oddsCacheChanged = true;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    oddsLookupInterrupted = true;
                    log.warn("Sporttery odds query interrupted for match {}", entry.getSportteryMatchId());
                } catch (Exception ex) {
                    log.warn(
                            "Unable to query Sporttery odds for match {}: {}",
                            entry.getSportteryMatchId(),
                            ex.getMessage());
                }
            }
            schedule.setSportteryMatchId(entry.getSportteryMatchId());
            schedule.setSportteryMatchNumber(entry.getSportteryMatchNumber());
            schedule.setSportteryNormalAvailable(entry.getNormalAvailable());
            schedule.setSportteryHandicap(entry.getHandicap());
            schedule.setSportteryNormalOdds(entry.getNormalOdds());
            schedule.setSportteryHandicapOdds(entry.getHandicapOdds());
            usedMatchIds.add(entry.getSportteryMatchId());
            matchedCount++;
        }
        if (oddsCacheChanged) {
            saveCache(LocalDateTime.now(resolveTargetZone()));
        }
        return matchedCount;
    }

    private boolean needsOddsLookup(SportteryMarketEntry entry) {
        if (Boolean.TRUE.equals(entry.getOddsLookupCompleted())) {
            return false;
        }
        return Boolean.TRUE.equals(entry.getNormalAvailable()) || entry.getHandicap() != null;
    }

    private void clearSelection(MatchSchedule schedule) {
        schedule.setSportteryMatchId(null);
        schedule.setSportteryMatchNumber(null);
        schedule.setSportteryNormalAvailable(null);
        schedule.setSportteryHandicap(null);
        schedule.setSportteryNormalOdds(null);
        schedule.setSportteryHandicapOdds(null);
    }

    private SportteryMarketEntry findBestEntry(
            MatchSchedule schedule,
            List<SportteryMarketEntry> candidates,
            Set<String> usedMatchIds) {
        SportteryMarketEntry bestEntry = null;
        int bestScore = -1;
        boolean ambiguous = false;
        for (SportteryMarketEntry candidate : candidates) {
            if (usedMatchIds.contains(candidate.getSportteryMatchId()) || !dateMatches(schedule, candidate)) {
                continue;
            }
            int matchScore = calculateMatchScore(schedule, candidate);
            if (matchScore > bestScore) {
                bestEntry = candidate;
                bestScore = matchScore;
                ambiguous = false;
            } else if (matchScore >= 0 && matchScore == bestScore) {
                ambiguous = true;
            }
        }
        if (bestScore >= 70 && !ambiguous) {
            return bestEntry;
        }
        return null;
    }

    private boolean dateMatches(MatchSchedule schedule, SportteryMarketEntry entry) {
        return Math.abs(ChronoUnit.DAYS.between(schedule.getMatchDate(), entry.getMatchDate())) <= 1L;
    }

    private int calculateMatchScore(MatchSchedule schedule, SportteryMarketEntry entry) {
        boolean homeTeamMatches = teamNamesMatch(
                schedule.getHomeTeamCn(),
                schedule.getHomeTeamEn(),
                entry.getHomeTeam());
        boolean awayTeamMatches = teamNamesMatch(
                schedule.getAwayTeamCn(),
                schedule.getAwayTeamEn(),
                entry.getAwayTeam());
        if (!homeTeamMatches || !awayTeamMatches) {
            return -1;
        }

        int score = 100;
        if (fullTimeScoreMatches(schedule, entry)) {
            score += 20;
        }
        long dateDistance = Math.abs(ChronoUnit.DAYS.between(schedule.getMatchDate(), entry.getMatchDate()));
        score += dateDistance == 0L ? 6 : 2;
        return score;
    }

    private boolean fullTimeScoreMatches(MatchSchedule schedule, SportteryMarketEntry entry) {
        return schedule.getHomeScore() != null
                && schedule.getAwayScore() != null
                && entry.getHomeScore() != null
                && entry.getAwayScore() != null
                && schedule.getHomeScore().equals(entry.getHomeScore())
                && schedule.getAwayScore().equals(entry.getAwayScore());
    }

    private boolean teamNamesMatch(String chineseName, String englishName, String sportteryName) {
        String sportteryCanonicalName = canonicalTeamName(sportteryName);
        String chineseCanonicalName = canonicalTeamName(chineseName);
        String translatedCanonicalName = canonicalTeamName(ClubTeamNameTranslator.translate(englishName));
        return canonicalNamesMatch(chineseCanonicalName, sportteryCanonicalName)
                || canonicalNamesMatch(translatedCanonicalName, sportteryCanonicalName);
    }

    private boolean canonicalNamesMatch(String source, String target) {
        if (source.isBlank() || target.isBlank()) {
            return false;
        }
        if (source.equals(target)) {
            return true;
        }
        return Math.min(source.length(), target.length()) >= 4
                && (source.contains(target) || target.contains(source));
    }

    private String canonicalTeamName(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT)
                .replace("足球俱乐部", "")
                .replace("俱乐部", "")
                .replaceAll("[\\s·•.．,，'’`´()（）\\[\\]【】\\-_/&]+", "")
                .replaceAll("^(FC|SC|CF)(?=\\p{IsHan})", "")
                .replaceAll("(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$", "");
        return switch (normalized) {
            case "尤尔加登", "佐加顿斯" -> "佐加顿斯";
            case "桑德菲杰", "桑纳菲尤尔" -> "桑纳菲尤尔";
            case "萨普斯堡", "萨尔普斯堡" -> "萨尔普斯堡";
            case "哈伊杜克斯普利特", "斯普利特海杜克" -> "斯普利特海杜克";
            case "杰尔ETO", "杰尔" -> "杰尔";
            case "阿特尔特比森", "比森阿泰尔" -> "比森阿泰尔";
            case "刚果民主共和国", "民主刚果", "刚果金" -> "刚果金";
            case "蔚山HD", "蔚山现代" -> "蔚山现代";
            case "浦项钢铁", "浦项制铁" -> "浦项制铁";
            case "尚州尚武", "金泉尚武" -> "金泉尚武";
            default -> normalized;
        };
    }

    private void ensureCacheLoaded() {
        if (cacheLoaded) {
            return;
        }
        cacheLoaded = true;
        Path path = resolveCachePath();
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        try {
            SportteryMarketCache cache = objectMapper.readValue(path.toFile(), SportteryMarketCache.class);
            for (SportteryMarketEntry entry : cache.getEntries()) {
                if (isValidCacheEntry(entry)) {
                    entriesByMatchId.put(entry.getSportteryMatchId(), entry);
                }
            }
            cache.getQueriedAtByDate().forEach((dateText, dateTimeText) -> {
                try {
                    queriedAtByDate.put(LocalDate.parse(dateText), LocalDateTime.parse(dateTimeText));
                } catch (DateTimeParseException ex) {
                    log.debug("Ignoring invalid Sporttery cache timestamp {}={}", dateText, dateTimeText);
                }
            });
            if (hasText(cache.getCalculatorQueriedAt())) {
                try {
                    calculatorQueriedAt = LocalDateTime.parse(cache.getCalculatorQueriedAt());
                } catch (DateTimeParseException ex) {
                    log.debug("Ignoring invalid Sporttery calculator cache timestamp {}", cache.getCalculatorQueriedAt());
                }
            }
        } catch (Exception ex) {
            log.warn("Unable to read Sporttery market cache {}: {}", path, ex.getMessage());
        }
    }

    private boolean isValidCacheEntry(SportteryMarketEntry entry) {
        return entry != null
                && hasText(entry.getSportteryMatchId())
                && entry.getMatchDate() != null
                && entry.getCompetition() != null
                && hasText(entry.getHomeTeam())
                && hasText(entry.getAwayTeam());
    }

    private void saveCache(LocalDateTime updatedAt) {
        Path path = resolveCachePath();
        if (path == null) {
            return;
        }
        SportteryMarketCache cache = new SportteryMarketCache();
        List<SportteryMarketEntry> entries = new ArrayList<>(entriesByMatchId.values());
        entries.sort(Comparator
                .comparing(SportteryMarketEntry::getMatchDate)
                .thenComparing(SportteryMarketEntry::getSportteryMatchId));
        cache.setEntries(entries);
        Map<String, String> queriedDates = new LinkedHashMap<>();
        queriedAtByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(item -> queriedDates.put(item.getKey().toString(), item.getValue().toString()));
        cache.setQueriedAtByDate(queriedDates);
        cache.setCalculatorQueriedAt(calculatorQueriedAt == null ? null : calculatorQueriedAt.toString());
        cache.setUpdatedAt(updatedAt.toString());

        Path tempFile = null;
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
                tempFile = Files.createTempFile(parent, "sporttery-market-", ".json");
            } else {
                tempFile = Files.createTempFile("sporttery-market-", ".json");
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), cache);
            moveCacheFile(tempFile, path);
        } catch (Exception ex) {
            log.warn("Unable to write Sporttery market cache {}: {}", path, ex.getMessage());
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    log.debug("Unable to remove temporary Sporttery cache {}", tempFile);
                }
            }
        }
    }

    private void moveCacheFile(Path tempFile, Path targetFile) throws IOException {
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path resolveCachePath() {
        if (cachePath == null || cachePath.isBlank()) {
            return null;
        }
        Path path = Path.of(cachePath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return Path.of(System.getProperty("user.dir")).resolve(path).normalize();
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {

    }

    private record SportteryPage(List<SportteryMarketEntry> entries, int totalPages) {

    }

    private record LatestMarketOdds(SportteryOdds odds, Integer handicap) {

    }

    public static class SportteryMarketCache {

        private String updatedAt;

        private String calculatorQueriedAt;

        private Map<String, String> queriedAtByDate = new LinkedHashMap<>();

        private List<SportteryMarketEntry> entries = new ArrayList<>();

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getCalculatorQueriedAt() {
            return calculatorQueriedAt;
        }

        public void setCalculatorQueriedAt(String calculatorQueriedAt) {
            this.calculatorQueriedAt = calculatorQueriedAt;
        }

        public Map<String, String> getQueriedAtByDate() {
            return queriedAtByDate == null ? new LinkedHashMap<>() : queriedAtByDate;
        }

        public void setQueriedAtByDate(Map<String, String> queriedAtByDate) {
            this.queriedAtByDate = queriedAtByDate == null ? new LinkedHashMap<>() : queriedAtByDate;
        }

        public List<SportteryMarketEntry> getEntries() {
            return entries == null ? new ArrayList<>() : entries;
        }

        public void setEntries(List<SportteryMarketEntry> entries) {
            this.entries = entries == null ? new ArrayList<>() : entries;
        }

    }

    public static class SportteryMarketEntry {

        private String sportteryMatchId;

        private String sportteryMatchNumber;

        private LocalDate matchDate;

        private Competition competition;

        private String homeTeam;

        private String awayTeam;

        private Boolean normalAvailable;

        private Integer handicap;

        private SportteryOdds normalOdds;

        private SportteryOdds handicapOdds;

        private Boolean oddsLookupCompleted;

        private Integer homeScore;

        private Integer awayScore;

        private Boolean currentSale;

        public String getSportteryMatchId() {
            return sportteryMatchId;
        }

        public void setSportteryMatchId(String sportteryMatchId) {
            this.sportteryMatchId = sportteryMatchId;
        }

        public String getSportteryMatchNumber() {
            return sportteryMatchNumber;
        }

        public void setSportteryMatchNumber(String sportteryMatchNumber) {
            this.sportteryMatchNumber = sportteryMatchNumber;
        }

        public LocalDate getMatchDate() {
            return matchDate;
        }

        public void setMatchDate(LocalDate matchDate) {
            this.matchDate = matchDate;
        }

        public Competition getCompetition() {
            return competition;
        }

        public void setCompetition(Competition competition) {
            this.competition = competition;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public void setHomeTeam(String homeTeam) {
            this.homeTeam = homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        public void setAwayTeam(String awayTeam) {
            this.awayTeam = awayTeam;
        }

        public Boolean getNormalAvailable() {
            return normalAvailable;
        }

        public void setNormalAvailable(Boolean normalAvailable) {
            this.normalAvailable = normalAvailable;
        }

        public Integer getHandicap() {
            return handicap;
        }

        public void setHandicap(Integer handicap) {
            this.handicap = handicap;
        }

        public SportteryOdds getNormalOdds() {
            return normalOdds;
        }

        public void setNormalOdds(SportteryOdds normalOdds) {
            this.normalOdds = normalOdds;
        }

        public SportteryOdds getHandicapOdds() {
            return handicapOdds;
        }

        public void setHandicapOdds(SportteryOdds handicapOdds) {
            this.handicapOdds = handicapOdds;
        }

        public Boolean getOddsLookupCompleted() {
            return oddsLookupCompleted;
        }

        public void setOddsLookupCompleted(Boolean oddsLookupCompleted) {
            this.oddsLookupCompleted = oddsLookupCompleted;
        }

        public Integer getHomeScore() {
            return homeScore;
        }

        public void setHomeScore(Integer homeScore) {
            this.homeScore = homeScore;
        }

        public Integer getAwayScore() {
            return awayScore;
        }

        public void setAwayScore(Integer awayScore) {
            this.awayScore = awayScore;
        }

        public Boolean getCurrentSale() {
            return currentSale;
        }

        public void setCurrentSale(Boolean currentSale) {
            this.currentSale = currentSale;
        }

    }

}
