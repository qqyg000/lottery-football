package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SportteryMarketSelectionServiceTest {

    private final SportteryMarketSelectionService service =
            new SportteryMarketSelectionService(new ObjectMapper());

    @Test
    void shouldMatchBrazilScheduleAliasToSportteryName() {
        assertTrue(service.teamNamesMatch(
                Competition.BRAZIL_SERIE_A,
                "米竞技",
                "Atlético-MG",
                "米内罗竞技"));
    }

    @Test
    void shouldMatchChampionsLeagueScheduleAliasesToSportteryNames() {
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "KuPS Kuopio",
                "KuPS Kuopio",
                "库奥皮奥"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "AGF",
                "AGF",
                "奥胡斯"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "波兹南",
                "Lech Poznan",
                "波兹南莱赫"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "格风暴",
                "SK Sturm Graz",
                "格拉茨风暴"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "Heart of Midlothian",
                "Heart of Midlothian",
                "哈茨"));
    }

    @Test
    void shouldRejectUnrelatedSportteryTeamName() {
        assertFalse(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "AGF",
                "AGF",
                "格拉茨风暴"));
    }

    @Test
    void shouldRecognizeCypriotFirstDivisionSportteryLeagueName() throws Exception {
        var match = new ObjectMapper().readTree("""
                {
                  "leagueNameAbbr": "塞浦甲"
                }
                """);

        Competition competition =
                ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

        assertEquals(Competition.CLUB_OFFICIAL_OTHER, competition);
    }

    @Test
    void shouldRecognizeKazakhstanPremierLeagueSportteryLeagueName() throws Exception {
        var match = new ObjectMapper().readTree("""
                {
                  "leagueNameAbbr": "哈萨超"
                }
                """);

        Competition competition =
                ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

        assertEquals(Competition.CLUB_OFFICIAL_OTHER, competition);
    }

    @Test
    void shouldRecognizeRequestedEuropeanDomesticCompetitionNames() throws Exception {
        for (String leagueName : List.of(
                "芬超",
                "芬兰杯",
                "丹超",
                "波超杯",
                "波甲",
                "奥甲",
                "苏超",
                "土超",
                "土耳其杯",
                "丹麦杯",
                "匈甲",
                "匈牙利杯",
                "克甲",
                "韩职",
                "瑞超",
                "挪超",
                "美职")) {
            var match = new ObjectMapper().readTree("""
                    {
                      "leagueNameAbbr": "%s"
                    }
                    """.formatted(leagueName));

            Competition competition =
                    ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

            assertEquals(Competition.CLUB_OFFICIAL_OTHER, competition, leagueName);
        }
    }

    @Test
    void shouldKeepClubOfficialOtherSchedulesInSportteryUpdateScope() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        schedule.setMatchDate(LocalDate.of(2026, 7, 21));

        List<MatchSchedule> supportedSchedules =
                ReflectionTestUtils.invokeMethod(service, "filterSupportedSchedules", List.of(schedule));

        assertEquals(List.of(schedule), supportedSchedules);
    }

    @Test
    void shouldPreserveSportteryLeagueNameOnCompletedSchedule() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040567");
        entry.setMatchDate(LocalDate.of(2026, 7, 21));
        entry.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        entry.setLeagueName("芬超");
        entry.setHomeTeam("玛丽港");
        entry.setAwayTeam("拉赫蒂");
        entry.setHomeScore(0);
        entry.setAwayScore(2);

        MatchSchedule schedule =
                ReflectionTestUtils.invokeMethod(service, "toCompletedSchedule", entry);

        assertEquals("芬超", schedule.getGroupName());
    }

    @Test
    void shouldMatchKazakhstanEnglishAliasToMappedChineseName() {
        assertTrue(service.teamNamesMatch(
                Competition.CLUB_OFFICIAL_OTHER,
                "FK Aktobe",
                "FK Aktobe",
                "阿克托比"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMatchUpcomingFixtureWhenOneTeamNameIsConfirmed() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040594");
        entry.setSportteryMatchNumber("周四201");
        entry.setMatchDate(LocalDate.of(2026, 7, 24));
        entry.setCompetition(Competition.BRAZIL_SERIE_A);
        entry.setHomeTeam("科林蒂安");
        entry.setAwayTeam("体彩客队名");

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId("ESPN-BRAZIL_SERIE_A-401841151");
        schedule.setCompetition(Competition.BRAZIL_SERIE_A);
        schedule.setMatchDate(LocalDate.of(2026, 7, 24));
        schedule.setHomeTeamCn("科林蒂安");
        schedule.setAwayTeamCn("供应商客队名");
        List<MatchSchedule> schedules = new ArrayList<>(List.of(schedule));

        ReflectionTestUtils.invokeMethod(service, "applyMarketEntries", schedules, false);

        assertEquals("2040594", schedule.getSportteryMatchId());
        assertEquals("体彩客队名", schedule.getSportteryAwayTeamName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldExposeRecentCompletedResultAsMappedSchedule() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040535");
        entry.setSportteryMatchNumber("周五204");
        entry.setMatchDate(LocalDate.of(2026, 7, 18));
        entry.setCompetition(Competition.BRAZIL_SERIE_A);
        entry.setHomeTeam("巴伊亚");
        entry.setAwayTeam("沙佩科恩斯");
        entry.setHomeScore(2);
        entry.setAwayScore(0);

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        List<MatchSchedule> schedules = service.getRecentCompletedSchedules(30);

        assertEquals(1, schedules.size());
        assertEquals("巴伊亚", schedules.get(0).getHomeTeamCn());
        assertEquals("沙佩科恩斯", schedules.get(0).getAwayTeamCn());
        assertEquals(2, schedules.get(0).getHomeScore());
        assertEquals(0, schedules.get(0).getAwayScore());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMergeOfficialResultWithoutAddingDuplicateSchedule() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040535");
        entry.setMatchDate(LocalDate.of(2026, 7, 18));
        entry.setCompetition(Competition.BRAZIL_SERIE_A);
        entry.setHomeTeam("巴伊亚");
        entry.setAwayTeam("沙佩科恩斯");
        entry.setHomeScore(2);
        entry.setAwayScore(0);
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        MatchSchedule officialSchedule = new MatchSchedule();
        officialSchedule.setMatchId("ESPN-BRAZIL_SERIE_A-401879459");
        officialSchedule.setCompetition(Competition.BRAZIL_SERIE_A);
        officialSchedule.setMatchDate(LocalDate.of(2026, 7, 18));
        officialSchedule.setHomeTeamCn("巴伊亚");
        officialSchedule.setAwayTeamCn("Chapecoense");
        officialSchedule.setHomeTeamEn("Bahia");
        officialSchedule.setAwayTeamEn("Chapecoense");
        officialSchedule.setHomeScore(2);
        officialSchedule.setAwayScore(0);
        List<MatchSchedule> schedules = new ArrayList<>(List.of(officialSchedule));

        int addedCount = service.mergeRecentCompletedSchedulesInto(schedules, 30);

        assertEquals(0, addedCount);
        assertEquals(1, schedules.size());
        assertEquals("2040535", schedules.get(0).getSportteryMatchId());
    }

}
