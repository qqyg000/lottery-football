package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import com.eason.worldcup.util.CompetitionDataPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ClubCompetitionScheduleUpdater {

    private static final Logger log = LoggerFactory.getLogger(ClubCompetitionScheduleUpdater.class);

    private static final int REGULATION_SECONDS = 90 * 60;

    private static final Pattern CLOCK_MINUTE_PATTERN = Pattern.compile("^(\\d+)");

    private static final Pattern SCORE_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*-\\s*(\\d+)\\s*$");

    private static final List<EspnLeagueSource> BASE_ESPN_SOURCES = List.of(
            new EspnLeagueSource(Competition.EUROPEAN_CHAMPIONSHIP, "uefa.euro"),
            new EspnLeagueSource(Competition.COPA_AMERICA, "conmebol.america"),
            new EspnLeagueSource(Competition.CLUB_WORLD_CUP, "fifa.cwc"),
            new EspnLeagueSource(Competition.EUROPA_LEAGUE, "uefa.europa"),
            new EspnLeagueSource(Competition.EUROPA_LEAGUE, "uefa.europa_qual"),
            new EspnLeagueSource(Competition.PREMIER_LEAGUE, "eng.1"),
            new EspnLeagueSource(Competition.LA_LIGA, "esp.1"),
            new EspnLeagueSource(Competition.SERIE_A, "ita.1"),
            new EspnLeagueSource(Competition.BUNDESLIGA, "ger.1"),
            new EspnLeagueSource(Competition.LIGUE_1, "fra.1"),
            new EspnLeagueSource(Competition.PRIMEIRA_LIGA, "por.1"),
            new EspnLeagueSource(Competition.EREDIVISIE, "ned.1"),
            new EspnLeagueSource(Competition.ARGENTINE_PRIMERA_DIVISION, "arg.1")
    );

    private static final List<EspnLeagueSource> SUPPLEMENTAL_ESPN_SOURCES = List.of(
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "uefa.euroq"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "uefa.nations"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "fifa.worldq.uefa"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "fifa.worldq.conmebol"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "fifa.worldq.concacaf"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "fifa.worldq.afc"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "fifa.worldq.caf"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "fifa.worldq.ofc"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "concacaf.gold"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "concacaf.nations.league"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "caf.nations"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "caf.nations_qual"),
            new EspnLeagueSource(Competition.INTERNATIONAL_OFFICIAL, "afc.asian.cup"),
            new EspnLeagueSource(Competition.INTERNATIONAL_FRIENDLY, "fifa.friendly"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "uefa.europa.conf"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "uefa.europa.conf_qual"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "uefa.super_cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "eng.fa"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "eng.league_cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "eng.charity"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "esp.copa_del_rey"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "esp.super_cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "ger.dfb_pokal"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "ger.super_cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "ita.coppa_italia"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "ita.super_cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "fra.coupe_de_france"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "fra.super_cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "por.taca.portugal"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "ned.cup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "ned.supercup"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "arg.copa"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "conmebol.libertadores"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "conmebol.sudamericana"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "conmebol.recopa"),
            new EspnLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "fifa.intercontinental_cup"),
            new EspnLeagueSource(Competition.CLUB_FRIENDLY, "club.friendly"),
            new EspnLeagueSource(Competition.CLUB_FRIENDLY, "global.champs_cup"),
            new EspnLeagueSource(Competition.CLUB_FRIENDLY, "friendly.emirates_cup"),
            new EspnLeagueSource(Competition.CLUB_FRIENDLY, "eng.asia_trophy"),
            new EspnLeagueSource(Competition.CLUB_FRIENDLY, "esp.joan_gamper")
    );

    private static final List<SportsDbLeagueSource> SPORTS_DB_SOURCES = List.of();

    private static final List<FotMobLeagueSource> FOTMOB_SOURCES = List.of(
            new FotMobLeagueSource(Competition.CLUB_FRIENDLY, "489", "俱乐部赛", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "180", "联赛杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "168", "瑞甲", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "172", "Play-offs 1/2", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "525", "亚冠精英", false, 2023),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "9116", "韩挑战联", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "9551", "韩国杯", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "262", "阿塞超", false),
            new FotMobLeagueSource(Competition.K_LEAGUE_1, "9080", "韩职", true),
            new FotMobLeagueSource(Competition.SWEDISH_ALLSVENSKAN, "67", "瑞超", true),
            new FotMobLeagueSource(Competition.FINNISH_VEIKKAUSLIIGA, "51", "芬超", true),
            new FotMobLeagueSource(Competition.EREDIVISIE, "57", "荷甲", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "10216", "欧协联", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "10615", "欧协联资格赛", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "40", "比甲", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "149", "比利时杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "164", "瑞士杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "69", "瑞士超", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "271", "保杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "270", "保超", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "126", "爱超", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "182", "塞超", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "183", "塞杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "176", "斯洛伐超", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "177", "斯洛伐杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "229", "卢森联", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "9527", "卢森杯", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "250", "法罗超", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "9523", "法罗杯", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "232", "黑山甲", false),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "215", "冰超", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "217", "冰岛杯", true),
            new FotMobLeagueSource(Competition.CLUB_OFFICIAL_OTHER, "116", "威尔士超", false)
    );

    private static final SofaScoreTournamentSource SOFA_SCORE_CLUB_FRIENDLY_SOURCE =
            new SofaScoreTournamentSource(Competition.CLUB_FRIENDLY, "853", "俱乐部友谊赛");

    private static final List<String> SOFA_SCORE_CLUB_FRIENDLY_TEAM_IDS = List.of("267828");

    private static final List<Futbol24LeagueSource> FUTBOL24_SOURCES = List.of(
            new Futbol24LeagueSource(Competition.CLUB_FRIENDLY, "472", "俱乐部友谊赛", false),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "525", "阿塞杯", true),
            new Futbol24LeagueSource(Competition.FINNISH_VEIKKAUSLIIGA, "322", "芬超", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "324", "芬兰杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "28", "丹超", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "297", "波超杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "107", "波甲", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "15", "奥甲", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "51", "苏超", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "133", "土超", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "537", "土耳其杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "33", "丹麦杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "92", "匈甲", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "531", "匈牙利杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "26", "克甲", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "75", "塞浦甲", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "269", "哈萨超", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "70", "威联杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "534", "塞杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "868", "卢森杯", true),
            new Futbol24LeagueSource(Competition.CLUB_OFFICIAL_OTHER, "291", "法罗杯", true)
    );

    private final ObjectMapper objectMapper;

    @Value("${club-competitions.schedule-update.enabled:true}")
    private boolean enabled;

    @Value("${club-competitions.schedule-update.espn-url-template:https://site.api.espn.com/apis/site/v2/sports/soccer/{league}/scoreboard?limit=1000&dates={start}-{end}}")
    private String espnUrlTemplate;

    @Value("${club-competitions.schedule-update.sportsdb-url-template:https://www.thesportsdb.com/api/v1/json/123/eventsround.php?id={leagueId}&r={round}&s={season}}")
    private String sportsDbUrlTemplate;

    @Value("${club-competitions.schedule-update.sportsdb-season-url-template:https://www.thesportsdb.com/api/v1/json/123/eventsseason.php?id={leagueId}&s={season}}")
    private String sportsDbSeasonUrlTemplate;

    @Value("${club-competitions.schedule-update.sportsdb-next-url-template:https://www.thesportsdb.com/api/v1/json/123/eventsnextleague.php?id={leagueId}}")
    private String sportsDbNextUrlTemplate;

    @Value("${club-competitions.schedule-update.sportsdb-past-url-template:https://www.thesportsdb.com/api/v1/json/123/eventspastleague.php?id={leagueId}}")
    private String sportsDbPastUrlTemplate;

    @Value("${club-competitions.schedule-update.fotmob-league-url-template:https://www.fotmob.com/api/data/leagues?id={leagueId}&ccode3=CHN&season={season}}")
    private String fotMobLeagueUrlTemplate;

    @Value("${club-competitions.schedule-update.sofascore-seasons-url-template:https://www.sofascore.com/api/v1/unique-tournament/{tournamentId}/seasons}")
    private String sofaScoreSeasonsUrlTemplate;

    @Value("${club-competitions.schedule-update.sofascore-events-url-template:https://www.sofascore.com/api/v1/unique-tournament/{tournamentId}/season/{seasonId}/events/{direction}/{page}}")
    private String sofaScoreEventsUrlTemplate;

    @Value("${club-competitions.schedule-update.sofascore-team-events-url-template:https://www.sofascore.com/api/v1/team/{teamId}/events/last/{page}}")
    private String sofaScoreTeamEventsUrlTemplate;

    @Value("${club-competitions.schedule-update.futbol24-live-url-template:https://api.futbol24.com/api/live/matches?_=0&date={date}&lang=en&sort=league}")
    private String futbol24LiveUrlTemplate;

    @Value("${club-competitions.schedule-update.futbol24-parallelism:16}")
    private int futbol24Parallelism;

    @Value("${club-competitions.schedule-update.timeout-seconds:15}")
    private int timeoutSeconds;

    @Value("${club-competitions.schedule-update.target-zone:Asia/Shanghai}")
    private String targetZone;

    @Value("${data-refresh.days-back:30}")
    private int daysBack;

    @Value("${data-refresh.days-forward:7}")
    private int daysForward;

    @Value("${data-refresh.target-zone:Asia/Shanghai}")
    private String refreshTargetZone;

    @Value("${club-competitions.schedule-update.parallelism:12}")
    private int parallelism;

    @Value("${club-competitions.schedule-update.sportsdb-future-rounds:6}")
    private int sportsDbFutureRounds;

    @Value("${club-competitions.schedule-update.cache-path:config/club-competition-schedules.json}")
    private String cachePath;

    private long lastSportsDbRequestNanos;

    private long lastSofaScoreRequestNanos;

    public ClubCompetitionScheduleUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public int updateSchedules(List<MatchSchedule> schedules) {
        return updateSchedules(schedules, false);
    }

    public int updateSchedules(List<MatchSchedule> schedules, boolean includeSupplementalSources) {
        if (!enabled) {
            log.info("Club competition schedule update is disabled.");
            return 0;
        }

        ZoneId zoneId = ZoneId.of(targetZone);
        LocalDate today = LocalDate.now(ZoneId.of(refreshTargetZone));
        LocalDate startDate = resolveRefreshStartDate(today);
        LocalDate endDate = resolveRefreshEndDate(today);
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        List<MatchSchedule> cachedSchedules = new ArrayList<>(loadCachedSchedules());
        FotMobScheduleBatch fotMobBatch = includeSupplementalSources
                ? loadFotMobSchedules(client, zoneId, timeout, startDate, endDate)
                : new FotMobScheduleBatch(List.of(), Set.of());
        cachedSchedules = removeReplacedCachedSchedules(
                cachedSchedules,
                fotMobBatch.loadedSeasons(),
                startDate,
                endDate);

        List<MatchSchedule> remoteSchedules = new ArrayList<>(cachedSchedules);
        remoteSchedules.addAll(loadEspnSchedules(
                client,
                zoneId,
                timeout,
                startDate,
                endDate,
                today,
                includeSupplementalSources));
        if (includeSupplementalSources) {
            List<MatchSchedule> futbol24Schedules = loadFutbol24Schedules(
                    client,
                    startDate,
                    endDate,
                    zoneId,
                    timeout);
            remoteSchedules.addAll(futbol24Schedules);
            boolean loadedClubFriendly = futbol24Schedules.stream()
                    .anyMatch(schedule -> schedule.getCompetition() == Competition.CLUB_FRIENDLY);
            if (!loadedClubFriendly) {
                remoteSchedules.addAll(loadSofaScoreSchedules(
                        client,
                        SOFA_SCORE_CLUB_FRIENDLY_SOURCE,
                        startDate,
                        today,
                        zoneId,
                        timeout));
            }
        }
        List<MatchSchedule> sportsDbSchedules = removeReplacedCachedSchedules(
                loadSportsDbSchedules(
                        client,
                        zoneId,
                        timeout,
                        fotMobBatch.loadedSeasons(),
                        startDate,
                        endDate),
                fotMobBatch.loadedSeasons(),
                startDate,
                endDate);
        remoteSchedules.addAll(sportsDbSchedules);
        remoteSchedules.addAll(fotMobBatch.schedules());
        remoteSchedules = deduplicateSchedulesByFixture(remoteSchedules);
        int updatedCount = mergeSchedules(schedules, remoteSchedules);
        saveCachedSchedules(remoteSchedules);
        log.info(
                "Loaded {} cached and refreshed schedule rows for configured competitions; refresh window {} to {}.",
                updatedCount,
                startDate,
                endDate);
        return updatedCount;
    }

    private List<MatchSchedule> loadEspnSchedules(
            HttpClient client,
            ZoneId zoneId,
            Duration timeout,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate supplementalEndDate,
            boolean includeSupplementalSources) {
        List<Supplier<List<MatchSchedule>>> tasks = new ArrayList<>();
        for (EspnLeagueSource source : BASE_ESPN_SOURCES) {
            tasks.add(() -> loadEspnScheduleRange(client, source, startDate, endDate, zoneId, timeout));
        }
        if (includeSupplementalSources) {
            for (EspnLeagueSource source : SUPPLEMENTAL_ESPN_SOURCES) {
                tasks.add(() -> loadEspnScheduleRange(
                        client,
                        source,
                        startDate,
                        supplementalEndDate,
                        zoneId,
                        timeout));
            }
        }
        List<MatchSchedule> result = executeTasks(tasks);
        if (result.isEmpty()) {
            log.warn("No configured competition schedules were returned by ESPN.");
        }
        return result;
    }

    private FotMobScheduleBatch loadFotMobSchedules(
            HttpClient client,
            ZoneId zoneId,
            Duration timeout,
            LocalDate startDate,
            LocalDate endDate) {
        if (FOTMOB_SOURCES.isEmpty()) {
            return new FotMobScheduleBatch(List.of(), Set.of());
        }
        List<MatchSchedule> result = new ArrayList<>();
        Set<CompetitionSeason> loadedSeasons = new HashSet<>();
        List<Supplier<List<MatchSchedule>>> tasks = new ArrayList<>();
        for (FotMobLeagueSource source : FOTMOB_SOURCES) {
            int previousYear = startDate.getYear() - 1;
            int firstSeason = source.usesCrossYearSeason(previousYear)
                    ? previousYear
                    : startDate.getYear();
            for (int season = firstSeason; season <= endDate.getYear(); season++) {
                int seasonValue = season;
                tasks.add(() -> loadFotMobSeason(
                        client,
                        source,
                        seasonValue,
                        zoneId,
                        timeout));
            }
        }
        for (MatchSchedule schedule : executeTasks(tasks, Math.min(8, tasks.size()))) {
            if (schedule.getMatchDate().isBefore(startDate)
                    || schedule.getMatchDate().isAfter(endDate)) {
                continue;
            }
            result.add(schedule);
            loadedSeasons.add(new CompetitionSeason(
                    schedule.getCompetition(),
                    schedule.getMatchDate().getYear()));
        }
        if (result.isEmpty()) {
            log.warn("No configured competition schedules were returned by FotMob; using cached data.");
        }
        return new FotMobScheduleBatch(result, loadedSeasons);
    }

    private List<MatchSchedule> loadFotMobSeason(
            HttpClient client,
            FotMobLeagueSource source,
            int season,
            ZoneId zoneId,
            Duration timeout) {
        String seasonValue = source.seasonValue(season);
        String url = fotMobLeagueUrlTemplate
                .replace("{leagueId}", source.leagueId())
                .replace("{season}", seasonValue);
        try {
            JsonNode root = downloadJsonWithRetry(client, url, timeout, 2);
            List<MatchSchedule> result = new ArrayList<>();
            for (JsonNode match : root.path("fixtures").path("allMatches")) {
                MatchSchedule schedule = parseFotMobLeagueMatch(match, source, zoneId);
                if (schedule != null) {
                    result.add(schedule);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception ex) {
            log.debug("Unable to load FotMob league {} season {}: {}",
                    source.leagueId(), season, ex.getMessage());
            return List.of();
        }
    }

    MatchSchedule parseFotMobLeagueMatch(JsonNode match, FotMobLeagueSource source, ZoneId zoneId) {
        Competition competition = source.competition();
        String eventId = match.path("id").asText("");
        String homeTeam = readFotMobTeamName(match.path("home"));
        String awayTeam = readFotMobTeamName(match.path("away"));
        JsonNode status = match.path("status");
        String utcTime = status.path("utcTime").asText("");
        if (eventId.isBlank() || homeTeam.isBlank() || awayTeam.isBlank() || utcTime.isBlank()) {
            return null;
        }
        if (status.path("cancelled").asBoolean(false)) {
            return null;
        }

        ZonedDateTime matchDateTime;
        try {
            matchDateTime = OffsetDateTime.parse(utcTime).atZoneSameInstant(zoneId);
        } catch (DateTimeParseException ex) {
            return null;
        }
        homeTeam = disambiguateFotMobTeamName(
                source,
                match.path("home"),
                matchDateTime.toLocalDate(),
                homeTeam);
        awayTeam = disambiguateFotMobTeamName(
                source,
                match.path("away"),
                matchDateTime.toLocalDate(),
                awayTeam);

        ScorePair score = parseScore(status.path("scoreStr").asText(""));
        boolean completed = status.path("finished").asBoolean(false) && score != null;
        boolean live = !completed && status.path("started").asBoolean(false);
        String round = match.path("round").asText("");
        if (round.isBlank()) {
            round = match.path("roundName").asText("");
        }

        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(competition);
        schedule.setMatchId(buildMatchId(
                "FOTMOB",
                competition,
                eventId,
                matchDateTime.toLocalDate(),
                homeTeam,
                awayTeam));
        schedule.setMatchDate(matchDateTime.toLocalDate());
        schedule.setKickoffTime(matchDateTime.toLocalTime().withSecond(0).withNano(0));
        schedule.setGroupName(round.isBlank() ? source.sourceCompetition() : source.sourceCompetition() + " 第" + round + "轮");
        schedule.setHomeTeamCn(ClubTeamNameTranslator.translate(competition, homeTeam));
        schedule.setAwayTeamCn(ClubTeamNameTranslator.translate(competition, awayTeam));
        schedule.setHomeTeamEn(homeTeam);
        schedule.setAwayTeamEn(awayTeam);
        schedule.setVenue("");
        schedule.setNeutral(false);
        schedule.setStatus(completed ? "COMPLETED" : (live ? "LIVE" : "SCHEDULED"));
        if (score != null && (completed || live)) {
            schedule.setHomeScore(score.homeScore);
            schedule.setAwayScore(score.awayScore);
        }
        return schedule;
    }

    private String disambiguateFotMobTeamName(
            FotMobLeagueSource source,
            JsonNode team,
            LocalDate matchDate,
            String teamName) {
        if ("270".equals(source.leagueId())
                && "10127".equals(team.path("id").asText(""))
                && matchDate.isBefore(LocalDate.of(2016, 6, 6))) {
            return "Litex Lovech";
        }
        return teamName;
    }

    private String readFotMobTeamName(JsonNode team) {
        String longName = team.path("longName").asText("");
        if (!longName.isBlank()) {
            return longName;
        }
        String name = team.path("name").asText("");
        if (!name.isBlank()) {
            return name;
        }
        return team.path("shortName").asText("");
    }

    private ScorePair parseScore(String value) {
        Matcher matcher = SCORE_PATTERN.matcher(value == null ? "" : value);
        if (!matcher.matches()) {
            return null;
        }
        return new ScorePair(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)));
    }

    List<MatchSchedule> removeReplacedCachedSchedules(
            List<MatchSchedule> schedules,
            Set<CompetitionSeason> replacedSeasons,
            LocalDate startDate,
            LocalDate endDate) {
        List<MatchSchedule> retainedSchedules = new ArrayList<>(schedules);
        if (replacedSeasons.isEmpty()) {
            return retainedSchedules;
        }
        retainedSchedules.removeIf(schedule -> schedule.getCompetition() != null
                && schedule.getMatchDate() != null
                && !schedule.getMatchDate().isBefore(startDate)
                && !schedule.getMatchDate().isAfter(endDate)
                && replacedSeasons.contains(new CompetitionSeason(
                        schedule.getCompetition(),
                        schedule.getMatchDate().getYear())));
        return retainedSchedules;
    }

    private List<MatchSchedule> filterSchedulesByDate(
            List<MatchSchedule> schedules,
            LocalDate startDate,
            LocalDate endDate) {
        List<MatchSchedule> result = new ArrayList<>();
        for (MatchSchedule schedule : schedules) {
            if (schedule.getMatchDate() == null
                    || schedule.getMatchDate().isBefore(startDate)
                    || schedule.getMatchDate().isAfter(endDate)) {
                continue;
            }
            result.add(schedule);
        }
        return result;
    }

    private List<MatchSchedule> loadEspnScheduleRange(
            HttpClient client,
            EspnLeagueSource source,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId,
            Duration timeout) {
        String url = espnUrlTemplate
                .replace("{league}", source.leagueSlug())
                .replace("{start}", startDate.format(DateTimeFormatter.BASIC_ISO_DATE))
                .replace("{end}", endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        try {
            JsonNode root = downloadJsonWithRetry(client, url, timeout, 2);
            List<MatchSchedule> result = new ArrayList<>();
            for (JsonNode event : root.path("events")) {
                MatchSchedule schedule = parseEspnEvent(event, source.competition(), zoneId);
                if (schedule != null) {
                    result.add(schedule);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception ex) {
            log.debug("Unable to load ESPN league {} for {} to {}: {}",
                    source.leagueSlug(), startDate, endDate, ex.getMessage());
            return List.of();
        }
    }

    MatchSchedule parseEspnEvent(JsonNode event, Competition competition, ZoneId zoneId) {
        JsonNode eventCompetition = event.path("competitions").path(0);
        JsonNode competitors = eventCompetition.path("competitors");
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
        ZonedDateTime matchDateTime = OffsetDateTime.parse(dateText).atZoneSameInstant(zoneId);
        String homeTeam = readEspnTeamName(homeCompetitor);
        String awayTeam = readEspnTeamName(awayCompetitor);
        if (homeTeam.isBlank() || awayTeam.isBlank()) {
            return null;
        }
        if (requiresKnownClubTeam(competition)
                && !ClubTeamNameTranslator.hasMapping(competition, homeTeam)
                && !ClubTeamNameTranslator.hasMapping(competition, awayTeam)) {
            return null;
        }

        JsonNode statusType = event.path("status").path("type");
        boolean completed = statusType.path("completed").asBoolean(false);
        boolean live = "in".equalsIgnoreCase(statusType.path("state").asText(""));
        boolean beyondRegulation = isBeyondRegulationStatus(event.path("status"));
        String eventId = event.path("id").asText("");
        String homeTeamId = readEspnTeamId(homeCompetitor);
        String awayTeamId = readEspnTeamId(awayCompetitor);
        Integer fullHomeScore = parseInteger(homeCompetitor.path("score").asText(""));
        Integer fullAwayScore = parseInteger(awayCompetitor.path("score").asText(""));
        ScorePair regulationScore = parseRegulationScore(
                eventCompetition,
                homeTeamId,
                awayTeamId,
                fullHomeScore,
                fullAwayScore);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(competition);
        schedule.setMatchId(buildMatchId("ESPN", competition, eventId, matchDateTime.toLocalDate(), homeTeam, awayTeam));
        schedule.setMatchDate(matchDateTime.toLocalDate());
        schedule.setKickoffTime(matchDateTime.toLocalTime().withSecond(0).withNano(0));
        schedule.setGroupName(toStageName(event.path("season").path("slug").asText(""), competition));
        schedule.setHomeTeamCn(ClubTeamNameTranslator.translate(competition, homeTeam));
        schedule.setAwayTeamCn(ClubTeamNameTranslator.translate(competition, awayTeam));
        schedule.setHomeTeamEn(homeTeam);
        schedule.setAwayTeamEn(awayTeam);
        schedule.setVenue(eventCompetition.path("venue").path("fullName").asText(""));
        schedule.setNeutral(eventCompetition.path("neutralSite").asBoolean(false));
        schedule.setStatus(completed ? "COMPLETED" : (live ? "LIVE" : "SCHEDULED"));
        if (completed || live) {
            if (regulationScore != null) {
                schedule.setHomeScore(regulationScore.homeScore);
                schedule.setAwayScore(regulationScore.awayScore);
            } else if (!beyondRegulation) {
                schedule.setHomeScore(fullHomeScore);
                schedule.setAwayScore(fullAwayScore);
            }
        }
        return schedule;
    }

    private List<MatchSchedule> loadSofaScoreSchedules(
            HttpClient client,
            SofaScoreTournamentSource source,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId,
            Duration timeout) {
        Map<String, MatchSchedule> schedules = new LinkedHashMap<>();
        for (String teamId : SOFA_SCORE_CLUB_FRIENDLY_TEAM_IDS) {
            for (MatchSchedule schedule : loadSofaScoreTeamWindow(
                    client,
                    source,
                    teamId,
                    startDate,
                    endDate,
                    zoneId,
                    timeout)) {
                schedules.put(schedule.getMatchId(), schedule);
            }
        }

        Map<Integer, String> seasonIds = loadSofaScoreSeasonIds(client, source, timeout);
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            String seasonId = seasonIds.get(year);
            if (seasonId == null) {
                continue;
            }
            for (String direction : List.of("last", "next")) {
                for (MatchSchedule schedule : loadSofaScoreSeasonWindow(
                        client,
                        source,
                        seasonId,
                        direction,
                        startDate,
                        endDate,
                        zoneId,
                        timeout)) {
                    schedules.put(schedule.getMatchId(), schedule);
                }
            }
        }
        if (schedules.isEmpty()) {
            log.warn("No club friendly schedules were returned by Sofascore; using cached data.");
        }
        return new ArrayList<>(schedules.values());
    }

    private List<MatchSchedule> loadSofaScoreTeamWindow(
            HttpClient client,
            SofaScoreTournamentSource source,
            String teamId,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId,
            Duration timeout) {
        List<MatchSchedule> result = new ArrayList<>();
        for (int page = 0; page < 5; page++) {
            String url = sofaScoreTeamEventsUrlTemplate
                    .replace("{teamId}", teamId)
                    .replace("{page}", Integer.toString(page));
            JsonNode root;
            try {
                root = downloadSofaScoreJson(client, url, timeout, 3);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return result;
            } catch (Exception ex) {
                log.debug("Unable to load Sofascore team {} page {}: {}", teamId, page, ex.getMessage());
                return result;
            }

            LocalDate oldestDate = null;
            for (JsonNode event : root.path("events")) {
                long eventTimestamp = event.path("startTimestamp").asLong(0);
                if (eventTimestamp > 0) {
                    LocalDate eventDate = Instant.ofEpochSecond(eventTimestamp).atZone(zoneId).toLocalDate();
                    oldestDate = oldestDate == null || eventDate.isBefore(oldestDate) ? eventDate : oldestDate;
                }
                MatchSchedule schedule = parseSofaScoreEvent(event, source, zoneId);
                if (schedule != null
                        && !schedule.getMatchDate().isBefore(startDate)
                        && !schedule.getMatchDate().isAfter(endDate)) {
                    result.add(schedule);
                }
            }
            if (!root.path("hasNextPage").asBoolean(false)
                    || (oldestDate != null && oldestDate.isBefore(startDate))) {
                break;
            }
        }
        return result;
    }

    private Map<Integer, String> loadSofaScoreSeasonIds(
            HttpClient client,
            SofaScoreTournamentSource source,
            Duration timeout) {
        String url = sofaScoreSeasonsUrlTemplate.replace("{tournamentId}", source.tournamentId());
        try {
            JsonNode root = downloadSofaScoreJson(client, url, timeout, 3);
            Map<Integer, String> result = new LinkedHashMap<>();
            for (JsonNode season : root.path("seasons")) {
                int year = parseSofaScoreSeasonYear(season.path("year").asText(""));
                String seasonId = season.path("id").asText("");
                if (year > 0 && !seasonId.isBlank()) {
                    result.put(year, seasonId);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Map.of();
        } catch (Exception ex) {
            log.debug("Unable to load Sofascore tournament {} seasons: {}",
                    source.tournamentId(), ex.getMessage());
            return Map.of();
        }
    }

    private int parseSofaScoreSeasonYear(String value) {
        Matcher matcher = Pattern.compile("(\\d{4})").matcher(value == null ? "" : value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private List<MatchSchedule> loadSofaScoreSeasonWindow(
            HttpClient client,
            SofaScoreTournamentSource source,
            String seasonId,
            String direction,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId,
            Duration timeout) {
        List<MatchSchedule> result = new ArrayList<>();
        for (int page = 0; page < 250; page++) {
            String url = sofaScoreEventsUrlTemplate
                    .replace("{tournamentId}", source.tournamentId())
                    .replace("{seasonId}", seasonId)
                    .replace("{direction}", direction)
                    .replace("{page}", Integer.toString(page));
            JsonNode root;
            try {
                root = downloadSofaScoreJson(client, url, timeout, 3);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return result;
            } catch (Exception ex) {
                log.debug("Unable to load Sofascore tournament {} season {} {} page {}: {}",
                        source.tournamentId(), seasonId, direction, page, ex.getMessage());
                return result;
            }

            LocalDate minimumDate = null;
            LocalDate maximumDate = null;
            for (JsonNode event : root.path("events")) {
                long eventTimestamp = event.path("startTimestamp").asLong(0);
                if (eventTimestamp > 0) {
                    LocalDate eventDate = Instant.ofEpochSecond(eventTimestamp).atZone(zoneId).toLocalDate();
                    minimumDate = minimumDate == null || eventDate.isBefore(minimumDate) ? eventDate : minimumDate;
                    maximumDate = maximumDate == null || eventDate.isAfter(maximumDate) ? eventDate : maximumDate;
                }
                MatchSchedule schedule = parseSofaScoreEvent(event, source, zoneId);
                if (schedule == null) {
                    continue;
                }
                LocalDate matchDate = schedule.getMatchDate();
                if (!matchDate.isBefore(startDate) && !matchDate.isAfter(endDate)) {
                    result.add(schedule);
                }
            }

            if (!root.path("hasNextPage").asBoolean(false)
                    || ("last".equals(direction) && maximumDate != null && maximumDate.isBefore(startDate))
                    || ("next".equals(direction) && minimumDate != null && minimumDate.isAfter(endDate))) {
                break;
            }
        }
        return result;
    }

    MatchSchedule parseSofaScoreEvent(
            JsonNode event,
            SofaScoreTournamentSource source,
            ZoneId zoneId) {
        String eventId = event.path("id").asText("");
        String homeTeam = event.path("homeTeam").path("name").asText("");
        String awayTeam = event.path("awayTeam").path("name").asText("");
        long startTimestamp = event.path("startTimestamp").asLong(0);
        if (eventId.isBlank() || homeTeam.isBlank() || awayTeam.isBlank() || startTimestamp <= 0) {
            return null;
        }
        Competition competition = source.competition();
        String tournamentId = event.path("tournament").path("uniqueTournament").path("id").asText("");
        if (!tournamentId.isBlank() && !source.tournamentId().equals(tournamentId)) {
            return null;
        }
        if (!ClubTeamNameTranslator.hasMapping(competition, homeTeam)
                && !ClubTeamNameTranslator.hasMapping(competition, awayTeam)) {
            return null;
        }

        String sourceStatus = event.path("status").path("type").asText("").toLowerCase(Locale.ROOT);
        if (sourceStatus.contains("cancel") || sourceStatus.contains("postpone")) {
            return null;
        }
        boolean completed = "finished".equals(sourceStatus);
        boolean live = sourceStatus.contains("progress") || sourceStatus.contains("live");
        ZonedDateTime matchDateTime = Instant.ofEpochSecond(startTimestamp).atZone(zoneId);
        Integer homeScore = readSofaScore(event.path("homeScore"));
        Integer awayScore = readSofaScore(event.path("awayScore"));

        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(competition);
        schedule.setMatchId(buildMatchId(
                "SOFASCORE", competition, eventId, matchDateTime.toLocalDate(), homeTeam, awayTeam));
        schedule.setMatchDate(matchDateTime.toLocalDate());
        schedule.setKickoffTime(matchDateTime.toLocalTime().withSecond(0).withNano(0));
        schedule.setGroupName(source.sourceCompetition());
        schedule.setHomeTeamCn(ClubTeamNameTranslator.translate(competition, homeTeam));
        schedule.setAwayTeamCn(ClubTeamNameTranslator.translate(competition, awayTeam));
        schedule.setHomeTeamEn(homeTeam);
        schedule.setAwayTeamEn(awayTeam);
        schedule.setVenue(event.path("venue").path("name").asText(""));
        schedule.setNeutral(event.path("neutral").asBoolean(false));
        schedule.setStatus(completed ? "COMPLETED" : (live ? "LIVE" : "SCHEDULED"));
        if ((completed || live) && homeScore != null && awayScore != null) {
            schedule.setHomeScore(homeScore);
            schedule.setAwayScore(awayScore);
        }
        return schedule;
    }

    private Integer readSofaScore(JsonNode score) {
        for (String field : List.of("normaltime", "current", "display")) {
            if (score.path(field).isInt() || score.path(field).isLong()) {
                return score.path(field).asInt();
            }
        }
        return null;
    }

    private List<MatchSchedule> loadFutbol24Schedules(
            HttpClient client,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId,
            Duration timeout) {
        List<Supplier<List<MatchSchedule>>> tasks = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate requestDate = date;
            tasks.add(() -> loadFutbol24Date(client, requestDate, zoneId, timeout));
        }
        List<MatchSchedule> result = executeTasks(tasks, futbol24Parallelism);
        if (result.isEmpty()) {
            log.warn("No configured competition schedules were returned by Futbol24; trying Sofascore for club friendlies.");
        }
        return result;
    }

    private List<MatchSchedule> loadFutbol24Date(
            HttpClient client,
            LocalDate date,
            ZoneId zoneId,
            Duration timeout) {
        String dateValue = date.atStartOfDay(zoneId)
                .toOffsetDateTime()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String url = futbol24LiveUrlTemplate.replace(
                "{date}",
                URLEncoder.encode(dateValue, StandardCharsets.UTF_8));
        try {
            Duration requestTimeout = Duration.ofSeconds(Math.min(6L, Math.max(1L, timeout.toSeconds())));
            JsonNode root = downloadJsonWithRetry(client, url, requestTimeout, 1);
            JsonNode live = root.path("live");
            JsonNode statuses = live.path("statuses");
            List<MatchSchedule> result = new ArrayList<>();
            var fields = live.path("matches").fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                MatchSchedule schedule = parseFutbol24Match(
                        field.getKey(),
                        field.getValue(),
                        statuses,
                        zoneId);
                if (schedule != null) {
                    result.add(schedule);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception ex) {
            log.debug("Unable to load Futbol24 matches for {}: {}", date, ex.getMessage());
            return List.of();
        }
    }

    MatchSchedule parseFutbol24Match(
            String eventId,
            JsonNode match,
            JsonNode statuses,
            ZoneId zoneId) {
        String leagueId = match.path("league_id").asText("");
        Futbol24LeagueSource source = FUTBOL24_SOURCES.stream()
                .filter(item -> item.leagueId().equals(leagueId))
                .findFirst()
                .orElse(null);
        if (source == null) {
            return null;
        }
        String statusId = match.path("status_id").asText("");
        JsonNode statusNode = statuses.path(statusId);
        String status = statusNode.path("name_short").asText("");
        String statusName = statusNode.path("name").asText("");
        String normalizedStatus = (status + " " + statusName).toUpperCase(Locale.ROOT);
        if (normalizedStatus.contains("CANCEL")
                || normalizedStatus.contains("POSTPON")
                || normalizedStatus.contains("ABANDON")) {
            return null;
        }
        boolean completed = statusNode.path("is_ended").asBoolean(false)
                || "FT".equalsIgnoreCase(status);
        boolean live = Set.of("1H", "2H", "HT", "LIVE", "ET", "PEN")
                .contains(status.toUpperCase(Locale.ROOT));
        String homeTeam = match.path("team1").path("name").asText("");
        String awayTeam = match.path("team2").path("name").asText("");
        String dateText = match.path("date").asText("");
        ScorePair score = parseScore(match.path("score1").asText(""));
        if (eventId == null || eventId.isBlank()
                || homeTeam.isBlank()
                || awayTeam.isBlank()
                || dateText.isBlank()
                || (completed && score == null)) {
            return null;
        }
        Competition competition = source.competition();
        if (!source.importsWholeCompetition()
                && !ClubTeamNameTranslator.hasMapping(competition, homeTeam)
                && !ClubTeamNameTranslator.hasMapping(competition, awayTeam)) {
            return null;
        }
        ZonedDateTime matchDateTime;
        try {
            matchDateTime = OffsetDateTime.parse(dateText).atZoneSameInstant(zoneId);
        } catch (DateTimeParseException ex) {
            return null;
        }

        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(competition);
        schedule.setMatchId(buildMatchId(
                "FUTBOL24", competition, eventId, matchDateTime.toLocalDate(), homeTeam, awayTeam));
        schedule.setMatchDate(matchDateTime.toLocalDate());
        schedule.setKickoffTime(matchDateTime.toLocalTime().withSecond(0).withNano(0));
        schedule.setGroupName(source.sourceCompetition());
        schedule.setHomeTeamCn(ClubTeamNameTranslator.translate(competition, homeTeam));
        schedule.setAwayTeamCn(ClubTeamNameTranslator.translate(competition, awayTeam));
        schedule.setHomeTeamEn(homeTeam);
        schedule.setAwayTeamEn(awayTeam);
        schedule.setVenue("");
        schedule.setNeutral(false);
        schedule.setStatus(completed ? "COMPLETED" : (live ? "LIVE" : "SCHEDULED"));
        if (score != null && (completed || live)) {
            schedule.setHomeScore(score.homeScore);
            schedule.setAwayScore(score.awayScore);
        }
        return schedule;
    }

    private boolean requiresKnownClubTeam(Competition competition) {
        return competition == Competition.CLUB_OFFICIAL_OTHER
                || competition == Competition.CLUB_FRIENDLY;
    }

    private String readEspnTeamId(JsonNode competitor) {
        String teamId = competitor.path("team").path("id").asText("");
        if (teamId.isBlank()) {
            teamId = competitor.path("id").asText("");
        }
        return teamId;
    }

    private String readEspnTeamName(JsonNode competitor) {
        JsonNode team = competitor.path("team");
        String displayName = team.path("displayName").asText("");
        if (!displayName.isBlank()) {
            return displayName;
        }
        return team.path("shortDisplayName").asText("");
    }

    private ScorePair parseRegulationScore(
            JsonNode competition,
            String homeTeamId,
            String awayTeamId,
            Integer fullHomeScore,
            Integer fullAwayScore) {
        JsonNode details = competition.path("details");
        if (!details.isArray() || details.isEmpty()) {
            return null;
        }

        int homeScore = 0;
        int awayScore = 0;
        boolean sawGoal = false;
        for (JsonNode detail : details) {
            if (!isScoringPlay(detail)) {
                continue;
            }

            sawGoal = true;
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

        if (!sawGoal && scoreTotal(fullHomeScore, fullAwayScore) > 0) {
            return null;
        }
        return new ScorePair(homeScore, awayScore);
    }

    private boolean isScoringPlay(JsonNode detail) {
        return detail.path("scoringPlay").asBoolean(false)
                && !detail.path("shootout").asBoolean(false)
                && detail.path("scoreValue").asInt(0) > 0;
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

    private List<MatchSchedule> loadSportsDbSchedules(
            HttpClient client,
            ZoneId zoneId,
            Duration timeout,
            Set<CompetitionSeason> replacedSeasons,
            LocalDate startDate,
            LocalDate endDate) {
        if (SPORTS_DB_SOURCES.isEmpty()) {
            return List.of();
        }
        int currentYear = LocalDate.now(zoneId).getYear();
        List<MatchSchedule> result = new ArrayList<>();
        for (SportsDbLeagueSource source : SPORTS_DB_SOURCES) {
            for (int season = startDate.getYear(); season <= endDate.getYear(); season++) {
                if (!replacedSeasons.contains(new CompetitionSeason(source.competition(), season))) {
                    result.addAll(loadSportsDbSeason(client, source, season, zoneId, timeout));
                }
            }

            if (replacedSeasons.contains(new CompetitionSeason(source.competition(), currentYear))) {
                continue;
            }

            SportsDbBatch pastBatch = loadSportsDbWindow(
                    client,
                    source,
                    sportsDbPastUrlTemplate,
                    zoneId,
                    timeout);
            result.addAll(pastBatch.schedules());
            SportsDbBatch nextBatch = loadSportsDbWindow(
                    client,
                    source,
                    sportsDbNextUrlTemplate,
                    zoneId,
                    timeout);
            result.addAll(nextBatch.schedules());

            int firstFutureRound = nextBatch.minimumRound();
            if (firstFutureRound > 0) {
                int lastFutureRound = Math.min(
                        source.maxRound(),
                        firstFutureRound + Math.max(1, sportsDbFutureRounds) - 1);
                for (int round = firstFutureRound; round <= lastFutureRound; round++) {
                    result.addAll(loadSportsDbRound(client, source, currentYear, round, zoneId, timeout));
                }
            }
        }
        if (result.isEmpty()) {
            log.warn("No Finnish or Korean league schedules were returned by TheSportsDB.");
        }
        return filterSchedulesByDate(result, startDate, endDate);
    }

    private List<MatchSchedule> loadSportsDbRound(
            HttpClient client,
            SportsDbLeagueSource source,
            int season,
            int round,
            ZoneId zoneId,
            Duration timeout) {
        String url = sportsDbUrlTemplate
                .replace("{leagueId}", source.leagueId())
                .replace("{season}", Integer.toString(season))
                .replace("{round}", Integer.toString(round));
        try {
            JsonNode root = downloadSportsDbJson(client, url, timeout);
            List<MatchSchedule> result = new ArrayList<>();
            for (JsonNode event : root.path("events")) {
                MatchSchedule schedule = parseSportsDbEvent(event, source.competition(), round, zoneId);
                if (schedule != null) {
                    result.add(schedule);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception ex) {
            log.debug("Unable to load TheSportsDB league {} season {} round {}: {}",
                    source.leagueId(), season, round, ex.getMessage());
            return List.of();
        }
    }

    private List<MatchSchedule> loadSportsDbSeason(
            HttpClient client,
            SportsDbLeagueSource source,
            int season,
            ZoneId zoneId,
            Duration timeout) {
        String url = sportsDbSeasonUrlTemplate
                .replace("{leagueId}", source.leagueId())
                .replace("{season}", Integer.toString(season));
        try {
            JsonNode root = downloadSportsDbJson(client, url, timeout);
            List<MatchSchedule> result = new ArrayList<>();
            for (JsonNode event : root.path("events")) {
                MatchSchedule schedule = parseSportsDbEvent(
                        event,
                        source.competition(),
                        event.path("intRound").asInt(0),
                        zoneId);
                if (schedule != null) {
                    result.add(schedule);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception ex) {
            log.debug("Unable to load TheSportsDB league {} season {}: {}",
                    source.leagueId(), season, ex.getMessage());
            return List.of();
        }
    }

    private SportsDbBatch loadSportsDbWindow(
            HttpClient client,
            SportsDbLeagueSource source,
            String urlTemplate,
            ZoneId zoneId,
            Duration timeout) {
        String url = urlTemplate.replace("{leagueId}", source.leagueId());
        try {
            JsonNode root = downloadSportsDbJson(client, url, timeout);
            List<MatchSchedule> result = new ArrayList<>();
            int minimumRound = Integer.MAX_VALUE;
            for (JsonNode event : root.path("events")) {
                int round = event.path("intRound").asInt(0);
                MatchSchedule schedule = parseSportsDbEvent(event, source.competition(), round, zoneId);
                if (schedule != null) {
                    result.add(schedule);
                    if (round > 0) {
                        minimumRound = Math.min(minimumRound, round);
                    }
                }
            }
            return new SportsDbBatch(result, minimumRound == Integer.MAX_VALUE ? 0 : minimumRound);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return SportsDbBatch.empty();
        } catch (Exception ex) {
            log.debug("Unable to load TheSportsDB league {} window: {}", source.leagueId(), ex.getMessage());
            return SportsDbBatch.empty();
        }
    }

    private MatchSchedule parseSportsDbEvent(JsonNode event, Competition competition, int requestedRound, ZoneId zoneId) {
        String homeTeam = event.path("strHomeTeam").asText("");
        String awayTeam = event.path("strAwayTeam").asText("");
        if (homeTeam.isBlank() || awayTeam.isBlank()) {
            return null;
        }
        ZonedDateTime matchDateTime = parseSportsDbDateTime(event, zoneId);
        if (matchDateTime == null) {
            return null;
        }

        Integer homeScore = parseInteger(event.path("intHomeScore").asText(""));
        Integer awayScore = parseInteger(event.path("intAwayScore").asText(""));
        String sourceStatus = event.path("strStatus").asText("");
        boolean completed = homeScore != null && awayScore != null;
        boolean live = !completed && isLiveStatus(sourceStatus);
        String eventId = event.path("idEvent").asText("");
        int round = event.path("intRound").asInt(requestedRound);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(competition);
        schedule.setMatchId(buildMatchId("SPORTSDB", competition, eventId, matchDateTime.toLocalDate(), homeTeam, awayTeam));
        schedule.setMatchDate(matchDateTime.toLocalDate());
        schedule.setKickoffTime(matchDateTime.toLocalTime().withSecond(0).withNano(0));
        schedule.setGroupName("第" + round + "轮");
        schedule.setHomeTeamCn(ClubTeamNameTranslator.translate(competition, homeTeam));
        schedule.setAwayTeamCn(ClubTeamNameTranslator.translate(competition, awayTeam));
        schedule.setHomeTeamEn(homeTeam);
        schedule.setAwayTeamEn(awayTeam);
        schedule.setVenue(event.path("strVenue").asText(""));
        schedule.setNeutral(false);
        schedule.setStatus(completed ? "COMPLETED" : (live ? "LIVE" : "SCHEDULED"));
        if (completed || live) {
            schedule.setHomeScore(homeScore);
            schedule.setAwayScore(awayScore);
        }
        return schedule;
    }

    private ZonedDateTime parseSportsDbDateTime(JsonNode event, ZoneId zoneId) {
        String timestamp = event.path("strTimestamp").asText("");
        if (!timestamp.isBlank()) {
            try {
                return OffsetDateTime.parse(timestamp).atZoneSameInstant(zoneId);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDateTime.parse(timestamp).atOffset(ZoneOffset.UTC).atZoneSameInstant(zoneId);
                } catch (DateTimeParseException nestedIgnored) {
                    log.debug("Unable to parse TheSportsDB timestamp {}", timestamp);
                }
            }
        }

        String dateText = event.path("dateEvent").asText("");
        if (dateText.isBlank()) {
            return null;
        }
        String timeText = event.path("strTime").asText("00:00:00");
        try {
            LocalDate date = LocalDate.parse(dateText);
            LocalTime time = timeText.isBlank() ? LocalTime.MIDNIGHT : LocalTime.parse(timeText);
            return ZonedDateTime.of(date, time, ZoneOffset.UTC).withZoneSameInstant(zoneId);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean isLiveStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        return !normalized.isBlank()
                && !"NS".equals(normalized)
                && !"TBD".equals(normalized)
                && !"PST".equals(normalized)
                && !"CANC".equals(normalized)
                && !"FT".equals(normalized);
    }

    private JsonNode downloadJson(HttpClient client, String url, Duration timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Accept", "application/json")
                .header("User-Agent", "lottery-football/1.0")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Unexpected HTTP status " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private JsonNode downloadSportsDbJson(HttpClient client, String url, Duration timeout) throws IOException, InterruptedException {
        throttleSportsDbRequests();
        return downloadJsonWithRetry(client, url, timeout, 3);
    }

    private JsonNode downloadSofaScoreJson(
            HttpClient client,
            String url,
            Duration timeout,
            int maxAttempts) throws IOException, InterruptedException {
        IOException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            throttleSofaScoreRequests();
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .timeout(timeout)
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Referer", "https://www.sofascore.com/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36")
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Unexpected HTTP status " + response.statusCode());
                }
                return objectMapper.readTree(response.body());
            } catch (IOException ex) {
                lastException = ex;
                if (attempt < maxAttempts) {
                    Thread.sleep(1_500L * attempt);
                }
            }
        }
        throw lastException == null ? new IOException("Unable to download Sofascore data") : lastException;
    }

    private synchronized void throttleSportsDbRequests() throws InterruptedException {
        long minimumIntervalNanos = Duration.ofMillis(550).toNanos();
        long elapsedNanos = System.nanoTime() - lastSportsDbRequestNanos;
        if (lastSportsDbRequestNanos > 0 && elapsedNanos < minimumIntervalNanos) {
            long waitNanos = minimumIntervalNanos - elapsedNanos;
            long waitMillis = Math.max(1L, Duration.ofNanos(waitNanos).toMillis());
            Thread.sleep(waitMillis);
        }
        lastSportsDbRequestNanos = System.nanoTime();
    }

    private synchronized void throttleSofaScoreRequests() throws InterruptedException {
        long minimumIntervalNanos = Duration.ofMillis(700).toNanos();
        long elapsedNanos = System.nanoTime() - lastSofaScoreRequestNanos;
        if (lastSofaScoreRequestNanos > 0 && elapsedNanos < minimumIntervalNanos) {
            long waitNanos = minimumIntervalNanos - elapsedNanos;
            long waitMillis = Math.max(1L, Duration.ofNanos(waitNanos).toMillis());
            Thread.sleep(waitMillis);
        }
        lastSofaScoreRequestNanos = System.nanoTime();
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
                    Thread.sleep(250L * attempt);
                }
            }
        }
        throw lastException == null ? new IOException("Unable to download TheSportsDB data") : lastException;
    }

    private List<MatchSchedule> loadCachedSchedules() {
        if (cachePath == null || cachePath.isBlank()) {
            return List.of();
        }
        Path path = Path.of(cachePath);
        if (!Files.isRegularFile(path)) {
            return List.of();
        }
        try {
            MatchSchedule[] cachedSchedules = objectMapper.readValue(path.toFile(), MatchSchedule[].class);
            List<MatchSchedule> normalizedSchedules = new ArrayList<>(List.of(cachedSchedules));
            normalizedSchedules.removeIf(schedule -> schedule == null
                    || CompetitionDataPolicy.isExcludedSourceCompetition(schedule.getGroupName()));
            for (MatchSchedule schedule : normalizedSchedules) {
                schedule.setCompetition(Competition.fromSourceCompetition(
                        schedule.getGroupName(),
                        schedule.getCompetition()));
            }
            return normalizedSchedules;
        } catch (Exception ex) {
            log.warn("Unable to read club competition schedule cache {}: {}", cachePath, ex.getMessage());
            return List.of();
        }
    }

    private void saveCachedSchedules(List<MatchSchedule> schedules) {
        if (cachePath == null || cachePath.isBlank() || schedules.isEmpty()) {
            return;
        }
        List<MatchSchedule> retainedSchedules = new ArrayList<>();
        for (MatchSchedule schedule : schedules) {
            if (schedule != null
                    && !CompetitionDataPolicy.isExcludedSourceCompetition(schedule.getGroupName())) {
                retainedSchedules.add(schedule);
            }
        }
        List<MatchSchedule> uniqueSchedules = deduplicateSchedulesByFixture(retainedSchedules);
        Path path = Path.of(cachePath);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), uniqueSchedules);
        } catch (Exception ex) {
            log.warn("Unable to write club competition schedule cache {}: {}", cachePath, ex.getMessage());
        }
    }

    private List<MatchSchedule> executeTasks(List<Supplier<List<MatchSchedule>>> tasks) {
        return executeTasks(tasks, parallelism);
    }

    private List<MatchSchedule> executeTasks(List<Supplier<List<MatchSchedule>>> tasks, int configuredParallelism) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        int threadCount = Math.max(1, Math.min(Math.min(32, configuredParallelism), tasks.size()));
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<CompletableFuture<List<MatchSchedule>>> futures = tasks.stream()
                    .map(task -> CompletableFuture.supplyAsync(task, executor))
                    .toList();
            List<MatchSchedule> result = new ArrayList<>();
            for (CompletableFuture<List<MatchSchedule>> future : futures) {
                result.addAll(future.join());
            }
            return result;
        } finally {
            executor.shutdownNow();
        }
    }

    private int mergeSchedules(List<MatchSchedule> schedules, List<MatchSchedule> remoteSchedules) {
        List<MatchSchedule> uniqueRemoteSchedules = deduplicateSchedulesByFixture(remoteSchedules);
        Map<String, MatchSchedule> existingSchedulesById = new LinkedHashMap<>();
        Map<String, MatchSchedule> existingSchedulesByFixture = new LinkedHashMap<>();
        for (MatchSchedule schedule : schedules) {
            normalizeScheduleTeamNames(schedule);
            if (schedule.getMatchId() != null && !schedule.getMatchId().isBlank()) {
                existingSchedulesById.put(schedule.getMatchId(), schedule);
            }
            existingSchedulesByFixture.putIfAbsent(buildFixtureIdentity(schedule), schedule);
        }

        for (MatchSchedule remoteSchedule : uniqueRemoteSchedules) {
            MatchSchedule existingSchedule = existingSchedulesById.get(remoteSchedule.getMatchId());
            if (existingSchedule == null) {
                existingSchedule = existingSchedulesByFixture.get(buildFixtureIdentity(remoteSchedule));
            }
            if (existingSchedule == null) {
                schedules.add(remoteSchedule);
                if (remoteSchedule.getMatchId() != null && !remoteSchedule.getMatchId().isBlank()) {
                    existingSchedulesById.put(remoteSchedule.getMatchId(), remoteSchedule);
                }
                existingSchedulesByFixture.put(buildFixtureIdentity(remoteSchedule), remoteSchedule);
            } else {
                applySchedule(existingSchedule, remoteSchedule);
            }
        }
        return uniqueRemoteSchedules.size();
    }

    List<MatchSchedule> deduplicateSchedulesByFixture(List<MatchSchedule> schedules) {
        Map<String, MatchSchedule> schedulesByFixture = new LinkedHashMap<>();
        if (schedules == null) {
            return List.of();
        }
        for (MatchSchedule schedule : schedules) {
            if (schedule == null) {
                continue;
            }
            normalizeScheduleTeamNames(schedule);
            schedulesByFixture.merge(
                    buildFixtureIdentity(schedule),
                    schedule,
                    this::preferSchedule);
        }
        return deduplicateSchedulesByTeamResult(new ArrayList<>(schedulesByFixture.values()));
    }

    private List<MatchSchedule> deduplicateSchedulesByTeamResult(List<MatchSchedule> schedules) {
        Map<String, MatchSchedule> schedulesByTeamResult = new HashMap<>();
        Set<MatchSchedule> duplicateSchedules = new HashSet<>();
        for (MatchSchedule schedule : schedules) {
            if (schedule.getHomeScore() == null
                    || schedule.getAwayScore() == null) {
                continue;
            }
            String homeKey = buildTeamResultIdentity(schedule, true);
            String awayKey = buildTeamResultIdentity(schedule, false);
            MatchSchedule existing = schedulesByTeamResult.get(homeKey);
            if (existing == null) {
                existing = schedulesByTeamResult.get(awayKey);
            }
            if (existing == null) {
                schedulesByTeamResult.put(homeKey, schedule);
                schedulesByTeamResult.put(awayKey, schedule);
                continue;
            }
            if (!isLikelyDuplicateSchedule(existing, schedule)) {
                schedulesByTeamResult.put(homeKey, schedule);
                schedulesByTeamResult.put(awayKey, schedule);
                continue;
            }

            MatchSchedule preferred = preferSchedule(existing, schedule);
            MatchSchedule duplicate = preferred == existing ? schedule : existing;
            duplicateSchedules.add(duplicate);
            for (String key : List.of(
                    buildTeamResultIdentity(existing, true),
                    buildTeamResultIdentity(existing, false),
                    homeKey,
                    awayKey)) {
                schedulesByTeamResult.put(key, preferred);
            }
        }
        return schedules.stream()
                .filter(schedule -> !duplicateSchedules.contains(schedule))
                .toList();
    }

    private String buildTeamResultIdentity(MatchSchedule schedule, boolean homeTeam) {
        int goalsFor = homeTeam ? schedule.getHomeScore() : schedule.getAwayScore();
        int goalsAgainst = homeTeam ? schedule.getAwayScore() : schedule.getHomeScore();
        String teamName = homeTeam ? schedule.getHomeTeamCn() : schedule.getAwayTeamCn();
        return schedule.getCompetition()
                + "|" + schedule.getMatchDate()
                + "|" + canonicalTeamName(teamName)
                + "|" + goalsFor
                + "|" + goalsAgainst;
    }

    private boolean isLikelyDuplicateSchedule(MatchSchedule left, MatchSchedule right) {
        if (hasSportteryIdentity(left) || hasSportteryIdentity(right)) {
            return true;
        }
        String leftOpponent = resolveDifferentOpponentName(left, right);
        String rightOpponent = resolveDifferentOpponentName(right, left);
        return areSimilarTeamNames(leftOpponent, rightOpponent);
    }

    private boolean hasSportteryIdentity(MatchSchedule schedule) {
        return schedule.getSportteryMatchId() != null && !schedule.getSportteryMatchId().isBlank();
    }

    private String resolveDifferentOpponentName(MatchSchedule source, MatchSchedule other) {
        String sourceHome = canonicalTeamName(source.getHomeTeamCn());
        String otherHome = canonicalTeamName(other.getHomeTeamCn());
        String otherAway = canonicalTeamName(other.getAwayTeamCn());
        if (sourceHome.equals(otherHome) || sourceHome.equals(otherAway)) {
            return source.getAwayTeamCn();
        }
        return source.getHomeTeamCn();
    }

    private boolean areSimilarTeamNames(String left, String right) {
        String leftName = canonicalTeamName(left);
        String rightName = canonicalTeamName(right);
        return leftName.length() >= 3
                && rightName.length() >= 3
                && (leftName.contains(rightName) || rightName.contains(leftName));
    }

    private void normalizeScheduleTeamNames(MatchSchedule schedule) {
        schedule.setHomeTeamCn(resolveTeamName(
                schedule.getCompetition(),
                schedule.getHomeTeamCn(),
                schedule.getHomeTeamEn()));
        schedule.setAwayTeamCn(resolveTeamName(
                schedule.getCompetition(),
                schedule.getAwayTeamCn(),
                schedule.getAwayTeamEn()));
    }

    private String resolveTeamName(
            Competition competition,
            String chineseName,
            String englishName) {
        String normalizedChineseName = chineseName == null ? "" : chineseName.trim();
        String normalizedEnglishName = englishName == null ? "" : englishName.trim();
        String translatedChineseName = ClubTeamNameTranslator.translate(
                competition,
                normalizedChineseName);
        String translatedEnglishName = ClubTeamNameTranslator.translate(
                competition,
                normalizedEnglishName);
        boolean englishAliasMapped = !normalizedEnglishName.isBlank()
                && !canonicalTeamName(normalizedEnglishName)
                        .equals(canonicalTeamName(translatedEnglishName));
        if (englishAliasMapped
                && !canonicalTeamName(translatedEnglishName)
                        .equals(canonicalTeamName(translatedChineseName))) {
            return translatedEnglishName;
        }
        return translatedChineseName.isBlank()
                ? translatedEnglishName
                : translatedChineseName;
    }

    private String buildFixtureIdentity(MatchSchedule schedule) {
        String homeTeam = canonicalTeamName(schedule.getHomeTeamCn());
        String awayTeam = canonicalTeamName(schedule.getAwayTeamCn());
        String firstTeam = homeTeam.compareTo(awayTeam) <= 0 ? homeTeam : awayTeam;
        String secondTeam = homeTeam.compareTo(awayTeam) <= 0 ? awayTeam : homeTeam;
        return schedule.getCompetition()
                + "|" + schedule.getMatchDate()
                + "|" + firstTeam
                + "|" + secondTeam;
    }

    private String canonicalTeamName(String teamName) {
        return Normalizer.normalize(teamName == null ? "" : teamName, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private MatchSchedule preferSchedule(MatchSchedule current, MatchSchedule candidate) {
        return scheduleQuality(candidate) >= scheduleQuality(current) ? candidate : current;
    }

    private int scheduleQuality(MatchSchedule schedule) {
        int quality = 0;
        if ("COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
            quality += 2;
        }
        if (schedule.getHomeScore() != null && schedule.getAwayScore() != null) {
            quality += 2;
        }
        if (schedule.getKickoffTime() != null) {
            quality++;
        }
        if (schedule.getVenue() != null && !schedule.getVenue().isBlank()) {
            quality++;
        }
        return quality;
    }

    private void applySchedule(MatchSchedule target, MatchSchedule source) {
        target.setCompetition(source.getCompetition());
        target.setMatchDate(source.getMatchDate());
        target.setKickoffTime(source.getKickoffTime());
        target.setGroupName(source.getGroupName());
        target.setHomeTeamCn(source.getHomeTeamCn());
        target.setAwayTeamCn(source.getAwayTeamCn());
        target.setHomeTeamEn(source.getHomeTeamEn());
        target.setAwayTeamEn(source.getAwayTeamEn());
        target.setVenue(source.getVenue());
        target.setNeutral(source.isNeutral());
        target.setStatus(source.getStatus());
        target.setHomeScore(source.getHomeScore());
        target.setAwayScore(source.getAwayScore());
    }

    private String buildMatchId(
            String provider,
            Competition competition,
            String eventId,
            LocalDate matchDate,
            String homeTeam,
            String awayTeam) {
        if (eventId != null && !eventId.isBlank()) {
            return provider + "-" + competition.name() + "-" + eventId;
        }
        String fixture = (homeTeam + "-" + awayTeam)
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return provider + "-" + competition.name() + "-" + matchDate + "-" + fixture;
    }

    private String toStageName(String seasonSlug, Competition competition) {
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
            default -> competition.getDisplayName();
        };
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int normalizeWindowDays(int value) {
        return Math.max(0, Math.min(365, value));
    }

    LocalDate resolveRefreshStartDate(LocalDate today) {
        return today.minusDays(normalizeWindowDays(daysBack));
    }

    LocalDate resolveRefreshEndDate(LocalDate today) {
        return today.plusDays(normalizeWindowDays(daysForward));
    }

    private record EspnLeagueSource(Competition competition, String leagueSlug) {

    }

    record FotMobLeagueSource(
            Competition competition,
            String leagueId,
            String sourceCompetition,
            boolean calendarYearSeason,
            Integer crossYearSeasonFrom) {

        FotMobLeagueSource(Competition competition, String leagueId, String sourceCompetition) {
            this(competition, leagueId, sourceCompetition, false, null);
        }

        FotMobLeagueSource(
                Competition competition,
                String leagueId,
                String sourceCompetition,
                boolean calendarYearSeason) {
            this(competition, leagueId, sourceCompetition, calendarYearSeason, null);
        }

        boolean usesCrossYearSeason(int seasonStartYear) {
            return !calendarYearSeason
                    && (crossYearSeasonFrom == null || seasonStartYear >= crossYearSeasonFrom);
        }

        String seasonValue(int seasonStartYear) {
            return usesCrossYearSeason(seasonStartYear)
                    ? seasonStartYear + "%2F" + (seasonStartYear + 1)
                    : String.valueOf(seasonStartYear);
        }

    }

    record SofaScoreTournamentSource(
            Competition competition,
            String tournamentId,
            String sourceCompetition) {

    }

    private record Futbol24LeagueSource(
            Competition competition,
            String leagueId,
            String sourceCompetition,
            boolean importsWholeCompetition) {

    }

    record CompetitionSeason(Competition competition, int season) {

    }

    private record FotMobScheduleBatch(
            List<MatchSchedule> schedules,
            Set<CompetitionSeason> loadedSeasons) {

    }

    private record SportsDbLeagueSource(Competition competition, String leagueId, int maxRound) {

    }

    private record SportsDbBatch(List<MatchSchedule> schedules, int minimumRound) {

        private static SportsDbBatch empty() {
            return new SportsDbBatch(List.of(), 0);
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

}
