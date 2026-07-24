package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DataRepositoryTest {

    private final DataRepository repository = new DataRepository(null, null, null, null, null, null);

    @Test
    void shouldDeduplicateMappedFixtureAndKeepSportteryOdds() {
        MatchSchedule officialSchedule = completedSchedule(
                "ESPN-CHAMPIONS_LEAGUE-401841108",
                "库奥皮奥",
                "AGF");
        MatchSchedule sportterySchedule = completedSchedule(
                "HIS-401841108",
                "KuPS Kuopio",
                "奥胡斯");
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
                "测试主队",
                "Unknown Opponent");
        sourceSchedule.setHomeScore(2);
        sourceSchedule.setAwayScore(0);
        MatchSchedule sportterySchedule = completedSchedule(
                "SPORTTERY-001",
                "测试主队",
                "未知对手别名");
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

    @Test
    void shouldRemoveNorwegianEliteserienSchedules() {
        MatchSchedule norwegianSchedule = completedSchedule("NORWAY-001", "挪威主队", "挪威客队");
        norwegianSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        norwegianSchedule.setGroupName("挪超");
        MatchSchedule swedishSchedule = completedSchedule("SWEDEN-001", "瑞典主队", "瑞典客队");
        swedishSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        swedishSchedule.setGroupName("瑞超");
        List<MatchSchedule> schedules = new ArrayList<>(List.of(norwegianSchedule, swedishSchedule));

        ReflectionTestUtils.invokeMethod(repository, "removeExcludedCompetitionSchedules", schedules);

        assertEquals(List.of(swedishSchedule), schedules);
    }

    private MatchSchedule completedSchedule(String matchId, String homeTeam, String awayTeam) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId(matchId);
        schedule.setCompetition(Competition.CHAMPIONS_LEAGUE);
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
