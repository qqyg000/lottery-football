package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DataRepositoryTest {

    private final DataRepository repository = new DataRepository(null, null, null, null, null, null);

    @Test
    void shouldDeduplicateMappedFixtureAndKeepSportteryOdds() {
        MatchSchedule officialSchedule = completedSchedule(
                "ESPN-BRAZIL_SERIE_A-401841108",
                "米内罗竞技",
                "Botafogo");
        MatchSchedule sportterySchedule = completedSchedule(
                "HIS-401841108",
                "米竞技",
                "博塔弗戈");
        sportterySchedule.setSportteryMatchId("401841108");
        sportterySchedule.setSportteryNormalOdds(new SportteryOdds(1.90, 3.10, 3.80, "2026-05-11"));

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(
                officialSchedule,
                sportterySchedule));

        assertEquals(1, schedules.size());
        assertSame(sportterySchedule, schedules.get(0));
    }

    @Test
    void shouldDeduplicateSameTeamDateAndScoreWithUnmappedOpponentAlias() {
        MatchSchedule sourceSchedule = completedSchedule(
                "SOURCE-001",
                "巴伊亚",
                "Chapecoense Unknown");
        sourceSchedule.setHomeScore(2);
        sourceSchedule.setAwayScore(0);
        MatchSchedule sportterySchedule = completedSchedule(
                "SPORTTERY-001",
                "巴伊亚",
                "沙佩科未知别名");
        sportterySchedule.setHomeScore(2);
        sportterySchedule.setAwayScore(0);
        sportterySchedule.setSportteryMatchId("20260718-001");

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(
                sourceSchedule,
                sportterySchedule));

        assertEquals(1, schedules.size());
        assertSame(sportterySchedule, schedules.get(0));
    }

    @Test
    void shouldKeepClubFriendlyDoubleHeader() {
        MatchSchedule first = completedSchedule("FRIENDLY-001", "测试队", "对手甲");
        first.setCompetition(Competition.CLUB_FRIENDLY);
        MatchSchedule second = completedSchedule("FRIENDLY-002", "测试队", "对手乙");
        second.setCompetition(Competition.CLUB_FRIENDLY);

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(first, second));

        assertEquals(2, schedules.size());
    }

    private MatchSchedule completedSchedule(String matchId, String homeTeam, String awayTeam) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId(matchId);
        schedule.setCompetition(Competition.BRAZIL_SERIE_A);
        schedule.setMatchDate(LocalDate.of(2026, 5, 11));
        schedule.setKickoffTime(LocalTime.of(3, 0));
        schedule.setHomeTeamCn(homeTeam);
        schedule.setAwayTeamCn(awayTeam);
        schedule.setHomeTeamEn(homeTeam);
        schedule.setAwayTeamEn(awayTeam);
        schedule.setStatus("COMPLETED");
        schedule.setHomeScore(1);
        schedule.setAwayScore(1);
        return schedule;
    }

}
