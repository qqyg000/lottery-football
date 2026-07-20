package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private MatchSchedule worldCupSchedule(LocalDate matchDate) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.WORLD_CUP);
        schedule.setMatchDate(matchDate);
        return schedule;
    }

}
