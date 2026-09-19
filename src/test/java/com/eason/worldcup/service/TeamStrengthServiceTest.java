package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HistoricalMatchType;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.MatchSchedule;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeamStrengthServiceTest {

    private static final double HOST_FACTOR = 1.20D;
    private static final double SEED_FACTOR = 1.80D;

    private final TeamStrengthService teamStrengthService = new TeamStrengthService(null);

    @Test
    void shouldUseQatarAs2022Host() {
        MatchSchedule schedule = worldCupSchedule(LocalDate.of(2022, 11, 21));

        assertEquals(HOST_FACTOR, teamStrengthService.getTournamentTeamFactor(
                schedule,
                "Qatar",
                HOST_FACTOR,
                SEED_FACTOR));
    }

    @Test
    void shouldUse2022PotOneTeamsAsSeededTeams() {
        MatchSchedule schedule = worldCupSchedule(LocalDate.of(2022, 12, 18));

        for (String team : List.of("Brazil", "Belgium", "France", "Argentina", "England", "Spain", "Portugal")) {
            assertEquals(SEED_FACTOR, teamStrengthService.getTournamentTeamFactor(
                    schedule,
                    team,
                    HOST_FACTOR,
                    SEED_FACTOR));
        }
    }

    @Test
    void shouldNotUse2026HostListFor2022WorldCup() {
        MatchSchedule schedule = worldCupSchedule(LocalDate.of(2022, 11, 23));

        for (String team : List.of("Canada", "Mexico", "United States")) {
            assertEquals(1.0D, teamStrengthService.getTournamentTeamFactor(
                    schedule,
                    team,
                    HOST_FACTOR,
                    SEED_FACTOR));
        }
    }

    @Test
    void shouldUse2026HostAndPotOneTeams() {
        MatchSchedule schedule = worldCupSchedule(LocalDate.of(2026, 6, 12));

        for (String team : List.of("Canada", "Mexico", "United States")) {
            assertEquals(HOST_FACTOR, teamStrengthService.getTournamentTeamFactor(
                    schedule,
                    team,
                    HOST_FACTOR,
                    SEED_FACTOR));
        }
        for (String team : List.of("Spain", "Argentina", "France", "England", "Brazil", "Portugal", "Netherlands", "Belgium", "Germany")) {
            assertEquals(SEED_FACTOR, teamStrengthService.getTournamentTeamFactor(
                    schedule,
                    team,
                    HOST_FACTOR,
                    SEED_FACTOR));
        }
        assertEquals(1.0D, teamStrengthService.getTournamentTeamFactor(
                schedule,
                "Qatar",
                HOST_FACTOR,
                SEED_FACTOR));
    }

    @Test
    void shouldIgnoreWorldCupFactorsForOtherCompetitionsAndUnknownEditions() {
        MatchSchedule leagueSchedule = worldCupSchedule(LocalDate.of(2026, 6, 12));
        leagueSchedule.setCompetition(Competition.PREMIER_LEAGUE);
        MatchSchedule unknownEditionSchedule = worldCupSchedule(LocalDate.of(2018, 6, 14));

        assertEquals(1.0D, teamStrengthService.getTournamentTeamFactor(
                leagueSchedule,
                "Canada",
                HOST_FACTOR,
                SEED_FACTOR));
        assertEquals(1.0D, teamStrengthService.getTournamentTeamFactor(
                unknownEditionSchedule,
                "Brazil",
                HOST_FACTOR,
                SEED_FACTOR));
    }

    @Test
    void shouldUseDefaultAndDynamicMatchTypeWeights() {
        assertEquals(1.0D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.OFFICIAL,
                null,
                null,
                null));
        assertEquals(0.5D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.INTERNATIONAL_FRIENDLY,
                null,
                null,
                null));
        assertEquals(0.3D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.CLUB_FRIENDLY,
                null,
                null,
                null));
        assertEquals(1.0D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.OFFICIAL,
                0.82D,
                0.44D,
                0.21D));
        assertEquals(0.44D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.INTERNATIONAL_FRIENDLY,
                0.82D,
                0.44D,
                0.21D));
        assertEquals(0.21D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.CLUB_FRIENDLY,
                0.82D,
                0.44D,
                0.21D));
        assertEquals(2.0D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.OFFICIAL,
                2.0D,
                -1.0D,
                0.21D));
        assertEquals(3.0D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.OFFICIAL,
                4.0D,
                -1.0D,
                0.21D));
        assertEquals(0.0D, teamStrengthService.resolveMatchTypeWeight(
                HistoricalMatchType.INTERNATIONAL_FRIENDLY,
                2.0D,
                -1.0D,
                0.21D));
    }

    @Test
    void shouldUseDynamicHomeTeamGoalFactorForNonNeutralMatches() {
        MatchSchedule leagueSchedule = worldCupSchedule(LocalDate.of(2026, 8, 7));
        leagueSchedule.setCompetition(Competition.EREDIVISIE);
        MatchSchedule worldCupSchedule = worldCupSchedule(LocalDate.of(2026, 6, 12));
        MatchSchedule neutralSchedule = worldCupSchedule(LocalDate.of(2026, 6, 12));
        neutralSchedule.setNeutral(true);

        assertEquals(1.06D, teamStrengthService.getHomeAdvantage(leagueSchedule, 1.06D));
        assertEquals(1.18D, teamStrengthService.getHomeAdvantage(worldCupSchedule, 1.18D));
        assertEquals(1.0D, teamStrengthService.getHomeAdvantage(neutralSchedule, 1.18D));
    }

    private MatchSchedule worldCupSchedule(LocalDate matchDate) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.WORLD_CUP);
        schedule.setMatchDate(matchDate);
        return schedule;
    }

    @Test
    void shouldShareReadOnlyClubHistoryWithoutLeakingFutureMatchesAndInvalidateAfterRefresh() {
        DataRepository repository = mock(DataRepository.class);
        TeamStrengthService service = new TeamStrengthService(repository);
        HistoricalMatch early = clubMatch(LocalDate.of(2026, 8, 1));
        HistoricalMatch later = clubMatch(LocalDate.of(2026, 8, 3));
        when(repository.getClubHistoricalMatches()).thenReturn(List.of(early, later));
        when(repository.getClubHistoricalMatches(Competition.PREMIER_LEAGUE)).thenReturn(List.of(early, later));
        when(repository.getSchedules()).thenReturn(List.of());
        when(repository.getSchedules(Competition.PREMIER_LEAGUE)).thenReturn(List.of());

        List<HistoricalMatch> before = ReflectionTestUtils.invokeMethod(
                service, "buildClubMatchesBefore", Competition.PREMIER_LEAGUE, LocalDate.of(2026, 8, 3));
        assertEquals(List.of(early), before);
        assertSame(before, ReflectionTestUtils.invokeMethod(
                service, "buildClubMatchesBefore", Competition.PREMIER_LEAGUE, LocalDate.of(2026, 8, 3)));
        assertThrows(UnsupportedOperationException.class, () -> before.add(later));
        assertEquals(List.of(early, later), ReflectionTestUtils.invokeMethod(
                service, "buildClubMatchesBefore", Competition.PREMIER_LEAGUE, LocalDate.of(2026, 8, 4)));

        when(repository.getClubHistoricalMatches(Competition.PREMIER_LEAGUE)).thenReturn(List.of(later));
        service.clearDynamicModelCaches();
        assertEquals(List.of(), ReflectionTestUtils.invokeMethod(
                service, "buildClubMatchesBefore", Competition.PREMIER_LEAGUE, LocalDate.of(2026, 8, 3)));
    }

    private HistoricalMatch clubMatch(LocalDate date) {
        HistoricalMatch match = new HistoricalMatch();
        match.setMatchDate(date);
        match.setTournament(Competition.PREMIER_LEAGUE.getDisplayName());
        match.setHomeTeam("Manchester City");
        match.setAwayTeam("Arsenal");
        return match;
    }

}
