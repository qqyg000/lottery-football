package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HeadToHeadOverviewResponse;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.HistoricalMatchType;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.UserConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PredictionServiceTest {

    private static final Map<Competition, ExpectedBacktestPeriod> EXPECTED_BACKTEST_PERIODS = Map.ofEntries(
            Map.entry(Competition.WORLD_CUP, period(
                    "2022-11-20", "2022-12-18", "2026-06-11", "2026-07-19")),
            Map.entry(Competition.EUROPEAN_CHAMPIONSHIP, period(
                    "2021-06-11", "2021-07-11", "2024-06-14", "2024-07-14")),
            Map.entry(Competition.COPA_AMERICA, period(
                    "2019-06-14", "2019-07-07", "2024-06-20", "2024-07-14")),
            Map.entry(Competition.CLUB_WORLD_CUP, period(
                    "2023-12-12", "2023-12-22", "2025-06-14", "2025-07-13")),
            Map.entry(Competition.EUROPA_LEAGUE, period(
                    "2025-07-10", "2026-05-20", "2026-07-09", "2027-05-26")),
            Map.entry(Competition.CHAMPIONS_LEAGUE, period(
                    "2025-07-08", "2026-05-30", "2026-07-07", "2027-06-05")),
            Map.entry(Competition.PREMIER_LEAGUE, period(
                    "2025-08-15", "2026-05-24", "2026-08-21", "2027-05-30")),
            Map.entry(Competition.LA_LIGA, period(
                    "2025-08-15", "2026-05-24", "2026-08-15", "2027-05-30")),
            Map.entry(Competition.SERIE_A, period(
                    "2025-08-23", "2026-05-24", "2026-08-22", "2027-05-30")),
            Map.entry(Competition.BUNDESLIGA, period(
                    "2025-08-22", "2026-05-16", "2026-08-28", "2027-05-22")),
            Map.entry(Competition.LIGUE_1, period(
                    "2025-08-15", "2026-05-16", "2026-08-20", "2027-05-29")),
            Map.entry(Competition.PRIMEIRA_LIGA, period(
                    "2025-08-08", "2026-05-17", "2026-08-07", "2027-05-16")),
            Map.entry(Competition.EREDIVISIE, period(
                    "2025-08-08", "2026-05-17", "2026-08-07", "2027-05-23")),
            Map.entry(Competition.ARGENTINE_PRIMERA_DIVISION, period(
                    "2025-01-24", "2025-12-13", "2026-01-25", "2026-12-13")),
            Map.entry(Competition.SWEDISH_ALLSVENSKAN, period(
                    "2025-03-29", "2025-11-09", "2026-04-04", "2026-11-29")),
            Map.entry(Competition.FINNISH_VEIKKAUSLIIGA, period(
                    "2025-04-05", "2025-11-09", "2026-04-04", "2026-11-08")),
            Map.entry(Competition.K_LEAGUE_1, period(
                    "2025-02-15", "2025-11-30", "2026-02-28", "2026-12-06")),
            Map.entry(Competition.SCOTTISH_FA_CUP, period(
                    "2025-08-09", "2026-05-23", "2026-08-01", "2027-05-22")));

    private final PredictionService predictionService = new PredictionService(null, null, null);

    @Test
    void shouldResolveBacktestFactorsByCompetition() {
        UserConfig.ModelFactors worldCupFactors = UserConfig.ModelFactors.defaults();
        UserConfig.ModelFactors premierLeagueFactors = UserConfig.ModelFactors.defaults();
        Map<Competition, UserConfig.ModelFactors> factorsByCompetition = Map.of(
                Competition.WORLD_CUP, worldCupFactors,
                Competition.PREMIER_LEAGUE, premierLeagueFactors);

        assertSame(worldCupFactors, predictionService.resolveBacktestModelFactors(
                Competition.WORLD_CUP,
                factorsByCompetition));
        assertSame(premierLeagueFactors, predictionService.resolveBacktestModelFactors(
                Competition.PREMIER_LEAGUE,
                factorsByCompetition));
        assertNull(predictionService.resolveBacktestModelFactors(
                Competition.LA_LIGA,
                factorsByCompetition));
    }

    @Test
    void shouldMatchSpreadsheetBacktestPeriods() {
        EXPECTED_BACKTEST_PERIODS.forEach((competition, expected) -> {
            PredictionService.CompetitionBacktestPeriod actual =
                    predictionService.resolveCompetitionBacktestPeriod(competition);
            assertEquals(expected.previousStartDate(), actual.previousStartDate(), competition.name());
            assertEquals(expected.previousEndDate(), actual.previousEndDate(), competition.name());
            assertEquals(expected.currentStartDate(), actual.currentStartDate(), competition.name());
            assertEquals(expected.currentEndDate(), actual.currentEndDate(), competition.name());
        });
    }

    @Test
    void shouldUsePreviousStartThroughCurrentEndWhenIncludingPreviousEdition() {
        EXPECTED_BACKTEST_PERIODS.forEach((competition, period) -> {
            LocalDate requestEndDate = period.currentEndDate().plusDays(2);
            assertFalse(isDateInRange(
                    competition, period.previousStartDate().minusDays(1), requestEndDate, true));
            assertTrue(isDateInRange(
                    competition, period.previousStartDate(), requestEndDate, true));
            assertTrue(isDateInRange(
                    competition, period.previousEndDate(), requestEndDate, true));
            assertTrue(isDateInRange(
                    competition, period.currentEndDate(), requestEndDate, true));
            assertTrue(isDateInRange(
                    competition, period.currentEndDate().plusDays(1), requestEndDate, true));
            assertFalse(isDateInRange(
                    competition, period.currentEndDate().plusDays(2), requestEndDate, true));
        });
    }

    @Test
    void shouldUseCurrentStartThroughCurrentEndForCurrentEdition() {
        EXPECTED_BACKTEST_PERIODS.forEach((competition, period) -> {
            LocalDate requestEndDate = period.currentEndDate().plusDays(2);
            assertFalse(isDateInRange(
                    competition, period.currentStartDate().minusDays(1), requestEndDate, false));
            assertTrue(isDateInRange(
                    competition, period.currentStartDate(), requestEndDate, false));
            assertTrue(isDateInRange(
                    competition, period.currentEndDate(), requestEndDate, false));
            assertTrue(isDateInRange(
                    competition, period.currentEndDate().plusDays(1), requestEndDate, false));
            assertFalse(isDateInRange(
                    competition, period.currentEndDate().plusDays(2), requestEndDate, false));
        });
    }

    @Test
    void shouldCapConfiguredEndDateAtRequestEndDate() {
        LocalDate requestEndDate = LocalDate.of(2026, 7, 21);
        assertTrue(isDateInRange(
                Competition.CHAMPIONS_LEAGUE, requestEndDate, requestEndDate, true));
        assertFalse(isDateInRange(
                Competition.CHAMPIONS_LEAGUE, requestEndDate.plusDays(1), requestEndDate, true));
    }

    @Test
    void shouldDeduplicateScheduleAliasesWithoutDependingOnOdds() {
        MatchSchedule sourceSchedule = completedSchedule("WC-001", "Congo DR");
        MatchSchedule sportterySchedule = completedSchedule("ODDS-001", "DR Congo");
        sportterySchedule.setSportteryMatchId("20260624-001");

        List<MatchSchedule> schedules = predictionService.deduplicateSchedules(
                List.of(sourceSchedule, sportterySchedule));

        assertEquals(1, schedules.size());
        assertSame(sportterySchedule, schedules.get(0));
    }

    @Test
    void shouldPreferSportteryTeamNamesForDisplay() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setHomeTeamCn("米竞技");
        schedule.setAwayTeamCn("巴伊亚");
        schedule.setSportteryHomeTeamName("米内罗竞技");
        schedule.setSportteryAwayTeamName("巴伊亚");

        assertEquals("米内罗竞技", predictionService.resolveDisplayTeamName(schedule, true));
        assertEquals("巴伊亚", predictionService.resolveDisplayTeamName(schedule, false));
    }

    @Test
    void shouldMapScheduleTeamNamesWhenSportteryNamesAreUnavailable() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CHAMPIONS_LEAGUE);
        schedule.setHomeTeamCn("AGF");
        schedule.setAwayTeamCn("波兹南");

        assertEquals("奥胡斯", predictionService.resolveDisplayTeamName(schedule, true));
        assertEquals("波兹南", predictionService.resolveDisplayTeamName(schedule, false));
    }

    @Test
    void shouldApplyRequestedMappingsToCardTeamNames() {
        Map<String, String> expectedMappings = Map.of(
                "布拉格斯巴达", "布斯巴达",
                "Zeleziarne Podbrezova", "Podbrezova",
                "Dukla Banska Bystrica", "Banska Bystrica",
                "FC Zbrojovka Brno", "布尔诺",
                "Lillestrøm", "利勒斯特",
                "腓特烈斯塔", "腓特烈",
                "霍森斯", "霍尔森斯",
                "布拉迪斯拉发", "布拉迪斯",
                "Hradec Kralove", "Kralove",
                "Mura", "穆拉");

        for (Map.Entry<String, String> mapping : expectedMappings.entrySet()) {
            MatchSchedule schedule = new MatchSchedule();
            schedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
            schedule.setHomeTeamCn(mapping.getKey());
            assertEquals(
                    mapping.getValue(),
                    predictionService.resolveDisplayTeamName(schedule, true));
        }
    }

    @Test
    void shouldIncludeSlovanLossWhenCardUsesSportteryAlias() {
        MatchSchedule target = schedule(
                "TARGET",
                LocalDate.of(2026, 8, 5),
                LocalTime.of(1, 0),
                "米亚尔比",
                "布拉迪斯",
                "SCHEDULED");
        target.setCompetition(Competition.CHAMPIONS_LEAGUE);
        target.setSportteryAwayTeamName("布拉迪斯拉发");
        HistoricalMatch slovanLoss = historicalMatch(
                LocalDate.of(2026, 5, 16),
                "布拉迪斯",
                "泽姆匹林米哈洛夫采",
                0,
                2);
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target),
                List.of(slovanLoss));
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.CHAMPIONS_LEAGUE,
                target.getMatchId(),
                10);

        assertEquals("布拉迪斯", service.resolveDisplayTeamName(target, false));
        assertEquals(1, overview.getAwayRecentMatches().size());
        assertEquals(LocalDate.of(2026, 5, 16), overview.getAwayRecentMatches().get(0).getMatchDate());
        assertEquals("布拉迪斯", overview.getAwayRecentMatches().get(0).getHomeTeamCn());
        assertEquals("泽姆匹林米哈洛夫采", overview.getAwayRecentMatches().get(0).getAwayTeamCn());
        assertEquals(0, overview.getAwayRecentMatches().get(0).getHomeScore());
        assertEquals(2, overview.getAwayRecentMatches().get(0).getAwayScore());
    }

    @Test
    void shouldDisplayAlavesHeadToHeadAndRecentMatchesForAccentedCardName() {
        MatchSchedule target = schedule(
                "ALAVES-TARGET",
                LocalDate.of(2026, 8, 10),
                LocalTime.of(2, 0),
                "Alavés",
                "Levante UD",
                "SCHEDULED");
        target.setCompetition(Competition.LA_LIGA);
        HistoricalMatch headToHead = historicalMatch(
                LocalDate.of(2024, 7, 28),
                "阿拉维斯",
                "莱万特",
                1,
                1);
        HistoricalMatch alavesRecent = historicalMatch(
                LocalDate.of(2026, 7, 18),
                "阿拉维斯",
                "埃瓦尔",
                1,
                1);
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target),
                List.of(headToHead, alavesRecent));
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.LA_LIGA,
                target.getMatchId(),
                10);

        assertEquals("阿拉维斯", service.resolveDisplayTeamName(target, true));
        assertEquals("莱万特", service.resolveDisplayTeamName(target, false));
        assertEquals(2, overview.getHomeRecentMatches().size());
        assertEquals(1, overview.getHeadToHeadMatches().size());
        assertEquals(1, overview.getAwayRecentMatches().size());
    }

    @Test
    void shouldDisplayTrabzonCardAndKeepMappedEuropaHeadToHeadMatches() {
        MatchSchedule target = schedule(
                "TRABZON-TARGET",
                LocalDate.of(2026, 8, 21),
                LocalTime.of(1, 0),
                "特拉布宗",
                "费伦茨",
                "SCHEDULED");
        target.setCompetition(Competition.EUROPA_LEAGUE);
        target.setSportteryHomeTeamName("特拉布宗体育");
        List<HistoricalMatch> europaHistory = List.of(
                historicalMatch(LocalDate.of(2022, 9, 9), "费伦茨", "特拉布宗", 3, 2),
                historicalMatch(LocalDate.of(2022, 11, 4), "特拉布宗", "费伦茨", 1, 0));
        europaHistory.forEach(match -> match.setSourceCompetition("欧罗巴"));
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target),
                europaHistory);
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.EUROPA_LEAGUE,
                target.getMatchId(),
                10);

        assertEquals("特拉布宗", service.resolveDisplayTeamName(target, true));
        assertEquals(2, overview.getHeadToHeadMatches().size());
        assertEquals("特拉布宗", overview.getHeadToHeadMatches().get(0).getHomeTeamCn());
        assertEquals(LocalDate.of(2022, 11, 4), overview.getHeadToHeadMatches().get(0).getMatchDate());
    }

    @Test
    void shouldDisplaySegundaHistoryWhenSportteryUsesDeportivoAlias() {
        MatchSchedule target = schedule(
                "DEPORTIVO-ELCHE-TARGET",
                LocalDate.of(2026, 8, 18),
                LocalTime.of(3, 0),
                "拉科鲁尼亚",
                "埃尔切",
                "SCHEDULED");
        target.setCompetition(Competition.LA_LIGA);
        List<HistoricalMatch> segundaHistory = List.of(
                historicalMatch(LocalDate.of(2018, 10, 13), "拉科", "埃尔切", 4, 0),
                historicalMatch(LocalDate.of(2019, 6, 5), "埃尔切", "拉科", 0, 0),
                historicalMatch(LocalDate.of(2019, 11, 10), "拉科", "埃尔切", 1, 3),
                historicalMatch(LocalDate.of(2020, 6, 24), "埃尔切", "拉科", 0, 1),
                historicalMatch(LocalDate.of(2024, 10, 14), "埃尔切", "拉科", 0, 0),
                historicalMatch(LocalDate.of(2025, 6, 2), "拉科", "埃尔切", 0, 4));
        segundaHistory.forEach(match -> match.setSourceCompetition("西乙"));
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target),
                segundaHistory);
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.LA_LIGA,
                target.getMatchId(),
                10);

        assertEquals("拉科", service.resolveDisplayTeamName(target, true));
        assertEquals(6, overview.getHomeRecentMatches().size());
        assertEquals(6, overview.getHeadToHeadMatches().size());
        assertEquals(6, overview.getAwayRecentMatches().size());
        assertEquals(LocalDate.of(2025, 6, 2), overview.getHeadToHeadMatches().get(0).getMatchDate());
        assertEquals("西乙", overview.getHeadToHeadMatches().get(0).getCompetitionName());
    }

    @Test
    void shouldUseHistoricalRegulationScoreWhenScheduleScoreDiffers() {
        MatchSchedule target = schedule(
                "TARGET",
                LocalDate.of(2026, 8, 1),
                LocalTime.of(20, 0),
                "圣吉联合",
                "Patro Eisden",
                "SCHEDULED");
        target.setCompetition(Competition.CLUB_FRIENDLY);
        MatchSchedule runtimeSchedule = completedSchedule(
                "FUTBOL24-CLUB_FRIENDLY-3383418",
                LocalDate.of(2026, 7, 24),
                LocalTime.of(19, 30),
                "圣吉联合",
                "Patro Eisden",
                4,
                1);
        runtimeSchedule.setCompetition(Competition.CLUB_FRIENDLY);
        HistoricalMatch verifiedMatch = historicalMatch(
                LocalDate.of(2026, 7, 24),
                "圣吉联合",
                "Patro Eisden",
                4,
                0);
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target, runtimeSchedule),
                List.of(verifiedMatch));
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.CLUB_FRIENDLY,
                target.getMatchId(),
                10);

        assertEquals(1, overview.getHeadToHeadMatches().size());
        assertEquals(4, overview.getHeadToHeadMatches().get(0).getHomeScore());
        assertEquals(0, overview.getHeadToHeadMatches().get(0).getAwayScore());
    }

    @Test
    void shouldDeduplicateAdjacentDateHistoryAcrossRuntimeAndBundledSources() {
        MatchSchedule target = schedule(
                "TARGET",
                LocalDate.of(2026, 8, 1),
                LocalTime.of(20, 0),
                "阿尔克马尔",
                "安德莱",
                "SCHEDULED");
        target.setCompetition(Competition.CHAMPIONS_LEAGUE);
        MatchSchedule runtimeSchedule = completedSchedule(
                "FUTBOL24-CLUB-FRIENDLY-3371034",
                LocalDate.of(2026, 7, 16),
                LocalTime.of(0, 30),
                "AZ Alkmaar",
                "Anderlecht",
                0,
                1);
        runtimeSchedule.setCompetition(Competition.CLUB_FRIENDLY);
        HistoricalMatch historicalMatch = historicalMatch(
                LocalDate.of(2026, 7, 15),
                "阿尔克马尔",
                "安德莱",
                0,
                1);
        historicalMatch.setMatchType(HistoricalMatchType.CLUB_FRIENDLY);
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target, runtimeSchedule),
                List.of(historicalMatch));
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.CHAMPIONS_LEAGUE,
                target.getMatchId(),
                10);

        assertEquals(1, overview.getHeadToHeadMatches().size());
        assertEquals(LocalDate.of(2026, 7, 15), overview.getHeadToHeadMatches().get(0).getMatchDate());
        assertEquals("阿尔克马", overview.getHeadToHeadMatches().get(0).getHomeTeamCn());
        assertEquals("安德莱", overview.getHeadToHeadMatches().get(0).getAwayTeamCn());
    }

    @Test
    void shouldBuildThreeColumnMatchHistoryBeforeTargetKickoff() {
        MatchSchedule target = schedule(
                "TARGET",
                LocalDate.of(2026, 7, 22),
                LocalTime.of(20, 0),
                "甲队",
                "乙队",
                "SCHEDULED");
        MatchSchedule sameDayHeadToHead = completedSchedule(
                "SAME-DAY-H2H",
                LocalDate.of(2026, 7, 22),
                LocalTime.of(18, 0),
                "乙队",
                "甲队",
                1,
                2);
        MatchSchedule homeRecent = completedSchedule(
                "HOME-RECENT",
                LocalDate.of(2026, 7, 20),
                LocalTime.of(19, 30),
                "甲队",
                "丙队",
                3,
                0);
        MatchSchedule awayRecent = completedSchedule(
                "AWAY-RECENT",
                LocalDate.of(2026, 7, 19),
                LocalTime.of(19, 30),
                "丁队",
                "乙队",
                0,
                1);
        MatchSchedule afterTarget = completedSchedule(
                "AFTER-TARGET",
                LocalDate.of(2026, 7, 22),
                LocalTime.of(21, 0),
                "甲队",
                "戊队",
                1,
                0);
        HistoricalMatch historicalHeadToHead = historicalMatch(
                LocalDate.of(2026, 7, 18),
                "甲队",
                "乙队",
                2,
                2);
        DataRepository dataRepository = new StubDataRepository(
                List.of(target),
                List.of(target, sameDayHeadToHead, homeRecent, awayRecent, afterTarget),
                List.of(historicalHeadToHead));
        PredictionService service = new PredictionService(dataRepository, null, null);

        HeadToHeadOverviewResponse overview = service.queryHeadToHeadOverview(
                Competition.WORLD_CUP,
                target.getMatchId(),
                10);

        assertEquals(3, overview.getHomeRecentMatches().size());
        assertEquals(2, overview.getHeadToHeadMatches().size());
        assertEquals(3, overview.getAwayRecentMatches().size());
        assertEquals("乙队", overview.getHeadToHeadMatches().get(0).getHomeTeamCn());
        assertEquals(LocalDate.of(2026, 7, 22), overview.getHeadToHeadMatches().get(0).getMatchDate());
        assertFalse(overview.getHomeRecentMatches().stream()
                .anyMatch(match -> LocalTime.of(21, 0).equals(match.getKickoffTime())));
    }

    private MatchSchedule completedSchedule(String matchId, String awayTeamEn) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.WORLD_CUP);
        schedule.setMatchId(matchId);
        schedule.setMatchDate(LocalDate.of(2026, 6, 24));
        schedule.setHomeTeamEn("Colombia");
        schedule.setAwayTeamEn(awayTeamEn);
        schedule.setStatus("COMPLETED");
        schedule.setHomeScore(1);
        schedule.setAwayScore(0);
        return schedule;
    }

    private MatchSchedule schedule(
            String matchId,
            LocalDate matchDate,
            LocalTime kickoffTime,
            String homeTeam,
            String awayTeam,
            String status) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.WORLD_CUP);
        schedule.setMatchId(matchId);
        schedule.setMatchDate(matchDate);
        schedule.setKickoffTime(kickoffTime);
        schedule.setHomeTeamCn(homeTeam);
        schedule.setAwayTeamCn(awayTeam);
        schedule.setStatus(status);
        return schedule;
    }

    private MatchSchedule completedSchedule(
            String matchId,
            LocalDate matchDate,
            LocalTime kickoffTime,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore) {
        MatchSchedule schedule = schedule(
                matchId,
                matchDate,
                kickoffTime,
                homeTeam,
                awayTeam,
                "COMPLETED");
        schedule.setHomeScore(homeScore);
        schedule.setAwayScore(awayScore);
        return schedule;
    }

    private HistoricalMatch historicalMatch(
            LocalDate matchDate,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore) {
        HistoricalMatch match = new HistoricalMatch();
        match.setMatchDate(matchDate);
        match.setSourceCompetition("测试赛事");
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        return match;
    }

    private static ExpectedBacktestPeriod period(
            String previousStartDate,
            String previousEndDate,
            String currentStartDate,
            String currentEndDate) {
        return new ExpectedBacktestPeriod(
                LocalDate.parse(previousStartDate),
                LocalDate.parse(previousEndDate),
                LocalDate.parse(currentStartDate),
                LocalDate.parse(currentEndDate));
    }

    private boolean isDateInRange(
            Competition competition,
            LocalDate matchDate,
            LocalDate backtestEndDate,
            boolean includePreviousEdition) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(competition);
        schedule.setMatchDate(matchDate);
        return predictionService.isWithinRecommendationBacktestRange(
                schedule,
                backtestEndDate,
                includePreviousEdition);
    }

    private static class StubDataRepository extends DataRepository {

        private final List<MatchSchedule> competitionSchedules;

        private final List<MatchSchedule> schedules;

        private final List<HistoricalMatch> historicalMatches;

        private StubDataRepository(
                List<MatchSchedule> competitionSchedules,
                List<MatchSchedule> schedules,
                List<HistoricalMatch> historicalMatches) {
            super(null, null, null, null, null, null);
            this.competitionSchedules = competitionSchedules;
            this.schedules = schedules;
            this.historicalMatches = historicalMatches;
        }

        @Override
        public List<MatchSchedule> getSchedules(Competition competition) {
            return competitionSchedules;
        }

        @Override
        public List<MatchSchedule> getSchedules() {
            return schedules;
        }

        @Override
        public List<HistoricalMatch> getHistoricalMatches() {
            return historicalMatches;
        }

        @Override
        public List<HistoricalMatch> getClubHistoricalMatches() {
            return historicalMatches;
        }

    }

    private record ExpectedBacktestPeriod(
            LocalDate previousStartDate,
            LocalDate previousEndDate,
            LocalDate currentStartDate,
            LocalDate currentEndDate) {

    }

}
