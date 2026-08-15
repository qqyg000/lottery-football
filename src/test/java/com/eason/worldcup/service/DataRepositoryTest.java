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
    void shouldMapClubCompetitionProgressIntoScheduleRefreshRange() {
        assertEquals(36, DataRepository.mapClubCompetitionProgress(0));
        assertEquals(47, DataRepository.mapClubCompetitionProgress(50));
        assertEquals(58, DataRepository.mapClubCompetitionProgress(100));
        assertEquals(36, DataRepository.mapClubCompetitionProgress(-1));
        assertEquals(58, DataRepository.mapClubCompetitionProgress(101));
    }

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
    void shouldDeduplicateSameClubFriendlyAcrossProviders() {
        MatchSchedule futbol24 = completedSchedule(
                "FUTBOL24-CLUB_FRIENDLY-3337082",
                "AIK Fotboll",
                "韦斯特罗斯");
        futbol24.setCompetition(Competition.CLUB_FRIENDLY);
        futbol24.setMatchDate(LocalDate.of(2026, 6, 28));
        futbol24.setKickoffTime(LocalTime.of(20, 0));
        futbol24.setHomeScore(3);
        futbol24.setAwayScore(2);
        MatchSchedule fotMob = completedSchedule(
                "FOTMOB-CLUB_FRIENDLY-5838835",
                "AIK索尔纳",
                "韦斯特罗斯");
        fotMob.setCompetition(Competition.CLUB_FRIENDLY);
        fotMob.setMatchDate(LocalDate.of(2026, 6, 28));
        fotMob.setKickoffTime(LocalTime.of(20, 0));
        fotMob.setHomeScore(3);
        fotMob.setAwayScore(2);

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(
                futbol24,
                fotMob));

        assertEquals(1, schedules.size());
        assertEquals("索尔纳", schedules.get(0).getHomeTeamCn());
    }

    @Test
    void shouldDeduplicateAdjacentDateClubFriendlyAcrossProviders() {
        MatchSchedule footMercato = completedSchedule(
                "FOOTMERCATO-1784255144786875322",
                "拉茨流浪",
                "哈茨");
        footMercato.setCompetition(Competition.CLUB_FRIENDLY);
        footMercato.setMatchDate(LocalDate.of(2026, 7, 24));
        footMercato.setKickoffTime(LocalTime.of(18, 30));
        footMercato.setHomeScore(0);
        footMercato.setAwayScore(1);
        MatchSchedule futbol24 = completedSchedule(
                "FUTBOL24-CLUB-FRIENDLY-3378927",
                "Raith Rovers",
                "Hearts FC");
        futbol24.setCompetition(Competition.CLUB_FRIENDLY);
        futbol24.setMatchDate(LocalDate.of(2026, 7, 25));
        futbol24.setKickoffTime(LocalTime.of(2, 30));
        futbol24.setHomeScore(0);
        futbol24.setAwayScore(1);

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(
                footMercato,
                futbol24));

        assertEquals(1, schedules.size());
        assertEquals("拉茨", schedules.get(0).getHomeTeamCn());
        assertEquals("哈茨", schedules.get(0).getAwayTeamCn());
    }

    @Test
    void shouldDeduplicateAdjacentDateFinnishLeagueAcrossProviders() {
        MatchSchedule futbol24 = completedSchedule(
                "FUTBOL24-FINNISH_VEIKKAUSLIIGA-3311994",
                "Gnistan",
                "KuPS Kuopio");
        futbol24.setCompetition(Competition.FINNISH_VEIKKAUSLIIGA);
        futbol24.setGroupName("芬超");
        futbol24.setMatchDate(LocalDate.of(2026, 8, 1));
        futbol24.setKickoffTime(LocalTime.of(19, 0));
        futbol24.setHomeScore(0);
        futbol24.setAwayScore(1);
        MatchSchedule fotMob = completedSchedule(
                "FOTMOB-FINNISH_VEIKKAUSLIIGA-5147613",
                "IF Gnistan",
                "KuPS");
        fotMob.setCompetition(Competition.FINNISH_VEIKKAUSLIIGA);
        fotMob.setGroupName("芬超");
        fotMob.setMatchDate(LocalDate.of(2026, 8, 2));
        fotMob.setKickoffTime(LocalTime.MIDNIGHT);
        fotMob.setHomeScore(0);
        fotMob.setAwayScore(1);

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(
                futbol24,
                fotMob));

        assertEquals(1, schedules.size());
    }

    @Test
    void shouldKeepOtherOfficialMatchesFromDifferentCompetitionContexts() {
        MatchSchedule league = completedSchedule("FUTBOL24-107-001", "测试队甲", "测试队乙");
        league.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        league.setGroupName("波甲 第1轮");
        MatchSchedule cup = completedSchedule("FOTMOB-9551-001", "测试队甲", "测试队乙");
        cup.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        cup.setGroupName("波兰杯");

        List<MatchSchedule> schedules = repository.deduplicateSchedulesByFixture(List.of(league, cup));

        assertEquals(2, schedules.size());
    }

    @Test
    void shouldKeepNorwegianEliteserienSchedules() {
        MatchSchedule norwegianSchedule = completedSchedule("NORWAY-001", "挪威主队", "挪威客队");
        norwegianSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        norwegianSchedule.setGroupName("挪超");
        MatchSchedule swedishSchedule = completedSchedule("SWEDEN-001", "瑞典主队", "瑞典客队");
        swedishSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        swedishSchedule.setGroupName("瑞超");
        List<MatchSchedule> schedules = new ArrayList<>(List.of(norwegianSchedule, swedishSchedule));

        ReflectionTestUtils.invokeMethod(repository, "removeExcludedCompetitionSchedules", schedules);

        assertEquals(List.of(norwegianSchedule, swedishSchedule), schedules);
    }

    @Test
    void shouldDisplayMidnightKickoffCardOnPreviousDateAfterLateMatches() {
        MatchSchedule lateMatch = completedSchedule("LATE-001", "晚场主队", "晚场客队");
        lateMatch.setMatchDate(LocalDate.of(2026, 8, 8));
        lateMatch.setKickoffTime(LocalTime.of(23, 30));
        MatchSchedule midnightMatch = completedSchedule("MIDNIGHT-001", "零点主队", "零点客队");
        midnightMatch.setMatchDate(LocalDate.of(2026, 8, 9));
        midnightMatch.setKickoffTime(LocalTime.MIDNIGHT);
        ReflectionTestUtils.setField(repository, "schedules", List.of(midnightMatch, lateMatch));

        List<MatchSchedule> schedules = repository.findSchedulesByDate(
                LocalDate.of(2026, 8, 8),
                Competition.CHAMPIONS_LEAGUE);

        assertEquals(List.of("LATE-001", "MIDNIGHT-001"), schedules.stream()
                .map(MatchSchedule::getMatchId)
                .toList());
        assertEquals(0, repository.findSchedulesByDate(
                LocalDate.of(2026, 8, 9),
                Competition.CHAMPIONS_LEAGUE).size());
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
