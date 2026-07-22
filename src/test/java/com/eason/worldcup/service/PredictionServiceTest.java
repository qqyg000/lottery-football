package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HeadToHeadOverviewResponse;
import com.eason.worldcup.model.HistoricalMatch;
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
            Map.entry(Competition.BRAZIL_SERIE_A, period(
                    "2025-03-29", "2025-12-07", "2026-01-28", "2026-12-02")),
            Map.entry(Competition.PRIMEIRA_LIGA, period(
                    "2025-08-08", "2026-05-17", "2026-08-07", "2027-05-16")),
            Map.entry(Competition.EREDIVISIE, period(
                    "2025-08-08", "2026-05-17", "2026-08-07", "2027-05-23")),
            Map.entry(Competition.ARGENTINE_PRIMERA_DIVISION, period(
                    "2025-01-24", "2025-12-13", "2026-01-25", "2026-12-13")));

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
        assertEquals("波兹南莱赫", predictionService.resolveDisplayTeamName(schedule, false));
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
            super(null, null, null, null, null);
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

    }

    private record ExpectedBacktestPeriod(
            LocalDate previousStartDate,
            LocalDate previousEndDate,
            LocalDate currentStartDate,
            LocalDate currentEndDate) {

    }

}
