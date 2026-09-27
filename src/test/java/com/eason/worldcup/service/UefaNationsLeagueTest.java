package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import com.eason.worldcup.model.UserConfig;
import com.eason.worldcup.util.ApplicationTime;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class UefaNationsLeagueTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldKeepActualCompetitionAndGroupOnlyUefaNationsLeagueUnderWorldCup() {
        for (String name : List.of("欧国联 第1轮", "欧洲国家联赛", "UEFA Nations League A")) {
            assertEquals(Competition.UEFA_NATIONS_LEAGUE,
                    Competition.fromSourceCompetition(name, Competition.INTERNATIONAL_OFFICIAL));
        }
        assertTrue(Competition.WORLD_CUP.includes(Competition.UEFA_NATIONS_LEAGUE));
        assertFalse(Competition.WORLD_CUP.includes(Competition.INTERNATIONAL_OFFICIAL));
        assertEquals(Competition.INTERNATIONAL_OFFICIAL,
                Competition.fromSourceCompetition("CONCACAF Nations League", Competition.INTERNATIONAL_OFFICIAL));
    }

    @Test
    void shouldLoadBundledHistoryForCardsWithoutOddsAndAvoidDuplicates() {
        DataRepository repository = repository(List.of());
        HistoricalMatch historical = new HistoricalMatch();
        historical.setMatchDate(LocalDate.of(2018, 9, 7));
        historical.setSourceCompetition("欧国联");
        historical.setHomeTeam("德国");
        historical.setAwayTeam("法国");
        historical.setHomeScore(0);
        historical.setAwayScore(0);
        List<MatchSchedule> schedules = new ArrayList<>();
        repository.mergeNationsLeagueHistory(schedules, List.of(historical));
        repository.mergeNationsLeagueHistory(schedules, List.of(historical));
        assertEquals(1, schedules.size());
        ReflectionTestUtils.setField(repository, "schedules", schedules);
        assertEquals(schedules, repository.findSchedulesByDate(historical.getMatchDate(), Competition.WORLD_CUP));
        assertEquals(schedules, repository.getSchedules(Competition.WORLD_CUP));
        assertNull(schedules.get(0).getSportteryMatchId());
    }

    @Test
    void shouldMergeOfficialFullNamesWithHistoricalAbbreviationsDespiteGlobalSelfMappings() {
        for (var alias : Map.of("斯洛文尼亚", "斯洛文尼", "阿尔巴尼亚", "阿尔巴尼", "哈萨克", "哈萨克斯坦").entrySet()) {
            assertEquals(alias.getValue(), ClubTeamNameTranslator.translate(Competition.UEFA_NATIONS_LEAGUE, alias.getKey()));
        }
        MatchSchedule historical = schedule("HISTORY", LocalDate.of(2026, 9, 26));
        historical.setHomeTeamCn("斯洛文尼");
        historical.setAwayTeamCn("苏格兰");
        MatchSchedule market = schedule("MARKET", historical.getMatchDate());
        market.setHomeTeamCn("斯洛文尼亚");
        market.setAwayTeamCn("苏格兰");
        market.setSportteryMatchId("2041713");
        market.setSportteryNormalOdds(new SportteryOdds(2.25, 3.0, 2.85, "初盘"));
        List<MatchSchedule> merged = repository(List.of()).deduplicateSchedulesByFixture(List.of(historical, market));
        assertEquals(1, merged.size());
        assertEquals("斯洛文尼", merged.get(0).getHomeTeamCn());
        assertNotNull(merged.get(0).getSportteryNormalOdds());
    }

    @Test
    void shouldIncludeCurrentAndPreviousNationsEditionsAfterWorldCupFinal() {
        PredictionService service = new PredictionService(null, null, null);
        LocalDate start = Competition.UEFA_NATIONS_LEAGUE.getSeasonStartDate(ApplicationTime.today());
        MatchSchedule current = schedule("CURRENT", start.plusMonths(2));
        MatchSchedule previous = schedule("PREVIOUS", start.minusYears(2).plusMonths(2));
        MatchSchedule older = schedule("OLDER", start.minusYears(4).plusMonths(2));
        LocalDate end = start.plusYears(1);
        assertTrue(service.isWithinRecommendationBacktestRange(current, end, false));
        assertFalse(service.isWithinRecommendationBacktestRange(previous, end, false));
        assertTrue(service.isWithinRecommendationBacktestRange(previous, end, true));
        assertFalse(service.isWithinRecommendationBacktestRange(older, end, true));
        assertFalse(service.isWithinRecommendationBacktestRange(current, start, true));
        UserConfig.ModelFactors factors = UserConfig.ModelFactors.defaults();
        assertSame(factors, service.resolveBacktestModelFactors(Competition.UEFA_NATIONS_LEAGUE,
                Map.of(Competition.WORLD_CUP, factors)));
    }

    @Test
    void shouldKeepActualCompetitionNamesWhileGroupingCardsOverviewAndBacktestUnderWorldCup() {
        LocalDate today = ApplicationTime.today();
        MatchSchedule nations = schedule("NL-TEST", today);
        nations.setSportteryMatchId("2041721");
        nations.setSportteryNormalOdds(new SportteryOdds(2.0, 3.0, 4.0, "初盘"));
        MatchSchedule unrelated = schedule("OTHER", today);
        unrelated.setCompetition(Competition.EUROPEAN_CHAMPIONSHIP);
        DataRepository repository = repository(List.of(nations, unrelated));
        TeamStrengthService strengths = mock(TeamStrengthService.class);
        var expected = new TeamStrengthService.AdjustedExpectedGoals(1.2, 1.0, 2);
        when(strengths.calculatePreTournamentExpectedGoals(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expected);
        when(strengths.calculateCurrentExpectedGoals(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expected);
        PredictionService service = new PredictionService(repository, strengths, mock(SportteryMarketSelectionService.class));
        var query = service.queryByDate(Competition.WORLD_CUP, today, 1000,
                null, null, null, null, null, null, null);
        assertEquals(1, query.getMatches().size());
        assertEquals(Competition.WORLD_CUP, query.getMatches().get(0).getCompetition());
        assertEquals("欧国联", query.getMatches().get(0).getCompetitionName());
        assertEquals("欧国联 第1轮", query.getMatches().get(0).getGroupName());
        assertTrue(service.overview(Competition.WORLD_CUP).getScheduleDates().contains(today.toString()));
        var backtest = service.queryRecommendationBacktest(Set.of(Competition.WORLD_CUP), 1000,
                null, null, null, null, false);
        assertEquals(1, backtest.getCompletedMatchCount());
        assertEquals(1, backtest.getOddsMatchCount());
        assertEquals(Competition.WORLD_CUP, backtest.getMatches().get(0).getCompetition());
        assertEquals("欧国联", backtest.getMatches().get(0).getCompetitionName());
        assertEquals("欧国联 第1轮", backtest.getMatches().get(0).getGroupName());
        assertEquals(Competition.UEFA_NATIONS_LEAGUE, nations.getCompetition());

        HistoricalMatch history = new HistoricalMatch();
        history.setMatchDate(today.minusYears(2));
        history.setSourceCompetition("欧国联");
        history.setHomeTeam("德国");
        history.setAwayTeam("法国");
        ReflectionTestUtils.setField(repository, "historicalMatches", List.of(history));
        var headToHead = service.queryHeadToHeadOverview(Competition.WORLD_CUP, nations.getMatchId(), 15);
        assertEquals(1, headToHead.getHeadToHeadMatches().size());
        assertEquals("欧国联", headToHead.getHeadToHeadMatches().get(0).getCompetitionName());
    }

    @Test
    void shouldRefreshSportteryResultAndAllOddsForWorldCupSelection() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/result", exchange -> {
            byte[] body = """
                    {"errorCode":"0","value":{"pages":1,"matchResult":[{"matchId":2041721,"leagueId":127,
                      "leagueNameAbbr":"欧国联","matchDate":"2026-09-27","allHomeTeam":"英格兰",
                      "allAwayTeam":"西班牙","sectionsNo999":"2:3","matchNumStr":"周六016"}]}}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.createContext("/odds", exchange -> {
            byte[] body = """
                    {"errorCode":"0","value":{"hadList":[{"h":"3.46","d":"3.40","a":"1.83","updateDate":"2026-09-25"}],
                      "hhadList":[{"h":"1.72","d":"3.60","a":"3.65","goalLine":"+1"}],
                      "ttgList":[{"s0":"12","s1":"6","s2":"3.5","s3":"3.4","s4":"5","s5":"8","s6":"15","s7":"20"}]}}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            SportteryMarketSelectionService market = new SportteryMarketSelectionService(mapper);
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            ReflectionTestUtils.setField(market, "enabled", true);
            ReflectionTestUtils.setField(market, "apiUrl", base + "/result");
            ReflectionTestUtils.setField(market, "oddsHistoryApiUrl", base + "/odds");
            ReflectionTestUtils.setField(market, "sourcePageUrl", "https://www.lottery.gov.cn/jc/zqsgkj/");
            ReflectionTestUtils.setField(market, "calculatorSourcePageUrl", "https://www.sporttery.cn/jc/jsq/zqbf/");
            ReflectionTestUtils.setField(market, "timeoutSeconds", 3);
            ReflectionTestUtils.setField(market, "targetZone", "Asia/Shanghai");
            ReflectionTestUtils.setField(market, "cachePath", temporaryDirectory.resolve("market.json").toString());
            LocalDate date = LocalDate.of(2026, 9, 27);
            var response = market.refreshHistoricalRange(date, date, Set.of(Competition.WORLD_CUP));
            assertEquals(1, response.getOfficialMatchCount());
            assertEquals(1, response.getNormalOddsMatchCount());
            assertEquals(1, response.getHandicapOddsMatchCount());
            assertEquals(1, response.getTotalGoalsOddsMatchCount());
            MatchSchedule match = schedule("NL-ODDS", date);
            match.setHomeTeamCn("英格兰");
            match.setAwayTeamCn("西班牙");
            assertEquals(1, market.applyCachedSelections(List.of(match)));
            assertEquals(3.46, match.getSportteryNormalOdds().getWin());
            assertEquals(2, match.getHomeScore());
            assertEquals(3, match.getAwayScore());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldConvertShanghaiDatesAndExcludeExtraTimeGoalsDuringRefresh() throws Exception {
        ClubCompetitionScheduleUpdater updater = new ClubCompetitionScheduleUpdater(mapper);
        var source = new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                Competition.UEFA_NATIONS_LEAGUE, "9806", "欧国联", false);
        var match = mapper.readTree("""
                {"id":"2942266","round":"1/2","roundName":"Semi-Finals",
                 "home":{"name":"Netherlands"},"away":{"name":"England"},
                 "status":{"utcTime":"2019-06-06T18:45:00Z","finished":true,"scoreStr":"3 - 1",
                   "reason":{"short":"AET"}}}
                """);
        var details = mapper.readTree("""
                {"content":{"matchFacts":{"events":{"events":[
                  {"type":"Half","time":90,"halfStrShort":"FT","homeScore":1,"awayScore":1}]}}}}
                """);
        MatchSchedule schedule = updater.parseFotMobLeagueMatch(match, source, ZoneId.of("Asia/Shanghai"), details);
        assertEquals(LocalDate.of(2019, 6, 7), schedule.getMatchDate());
        assertEquals(LocalTime.of(2, 45), schedule.getKickoffTime());
        assertEquals(1, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
        assertTrue(schedule.isNeutral());
        assertTrue(source.supportsSeason(2018));
        assertFalse(source.supportsSeason(2014));
        assertFalse(source.supportsSeason(2019));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldLoadEveryDivisionAndPromotionPlayoffThroughRefreshSources() throws Exception {
        ClubCompetitionScheduleUpdater updater = new ClubCompetitionScheduleUpdater(mapper);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/league", exchange -> {
            byte[] body = """
                    {"details":{"selectedSeason":"2024/2025"},"fixtures":{"allMatches":[
                      {"id":"4679425","round":"final","home":{"name":"Turkiye"},"away":{"name":"Hungary"},
                       "status":{"utcTime":"2025-03-20T17:00:00Z","finished":true,"scoreStr":"3 - 1"}}]}}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            ReflectionTestUtils.setField(updater, "fotMobLeagueUrlTemplate",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/league?id={leagueId}&season={season}");
            List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                    (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>) ReflectionTestUtils.getField(
                            ClubCompetitionScheduleUpdater.class, "FOTMOB_SOURCES");
            List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> nationsSources = sources.stream()
                    .filter(source -> source.competition() == Competition.UEFA_NATIONS_LEAGUE).toList();
            assertEquals(7, nationsSources.size());
            for (var source : nationsSources) {
                List<MatchSchedule> matches = ReflectionTestUtils.invokeMethod(updater, "loadFotMobSeason",
                        HttpClient.newHttpClient(), source, 2024, ZoneId.of("Asia/Shanghai"),
                        Duration.ofSeconds(3), LocalDate.of(2014, 10, 22));
                assertEquals(1, matches.size());
                assertEquals(Competition.UEFA_NATIONS_LEAGUE, matches.get(0).getCompetition());
                assertEquals(LocalDate.of(2025, 3, 21), matches.get(0).getMatchDate());
                if (source.leagueId().startsWith("107")) {
                    assertFalse(matches.get(0).isNeutral());
                }
            }
            List<MatchSchedule> wrongSeason = ReflectionTestUtils.invokeMethod(updater, "loadFotMobSeason",
                    HttpClient.newHttpClient(), nationsSources.get(0), 2026, ZoneId.of("Asia/Shanghai"),
                    Duration.ofSeconds(3), LocalDate.of(2014, 10, 22));
            assertTrue(wrongSeason.isEmpty());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRefreshSelectedHistoricalDateWhenClickingUpdate() {
        DataRepository repository = mock(DataRepository.class);
        TeamStrengthService strengths = mock(TeamStrengthService.class);
        SportteryMarketSelectionService market = mock(SportteryMarketSelectionService.class);
        when(repository.getSchedules(Competition.WORLD_CUP)).thenReturn(List.of());
        PredictionService service = new PredictionService(repository, strengths, market);
        LocalDate date = LocalDate.of(2025, 6, 9);
        service.refreshData(Competition.WORLD_CUP, date);
        verify(market).forceRefresh(null);
        verify(market).refreshHistoricalRange(date, date, Set.of(Competition.WORLD_CUP));
        verify(strengths).rebuildModels();
    }

    private DataRepository repository(List<MatchSchedule> schedules) {
        DataRepository repository = new DataRepository(null, null, null, null, null, null);
        ReflectionTestUtils.setField(repository, "schedules", schedules);
        return repository;
    }

    private MatchSchedule schedule(String id, LocalDate date) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId(id);
        schedule.setMatchDate(date);
        schedule.setKickoffTime(LocalTime.of(2, 45));
        schedule.setCompetition(Competition.UEFA_NATIONS_LEAGUE);
        schedule.setGroupName("欧国联 第1轮");
        schedule.setHomeTeamCn("德国");
        schedule.setAwayTeamCn("法国");
        schedule.setHomeTeamEn("Germany");
        schedule.setAwayTeamEn("France");
        schedule.setHomeScore(0);
        schedule.setAwayScore(0);
        schedule.setStatus("COMPLETED");
        return schedule;
    }

}
