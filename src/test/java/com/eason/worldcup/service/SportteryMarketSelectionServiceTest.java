package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SportteryMarketSelectionServiceTest {

    private final SportteryMarketSelectionService service =
            new SportteryMarketSelectionService(new ObjectMapper());

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
    void shouldRejectRemovedBrazilianCompetitionNames() throws Exception {
        for (String leagueName : List.of(
                "巴甲",
                "巴乙",
                "巴西乙级联赛",
                "巴西杯",
                "巴东北杯",
                "巴西东北杯",
                "圣保罗锦",
                "Campeonato Paulista",
                "Brazil Serie B",
                "Copa do Nordeste")) {
            var match = new ObjectMapper().readTree("""
                    {
                      "leagueNameAbbr": "%s"
                    }
                    """.formatted(leagueName));

            Competition competition =
                    ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

            assertNull(competition, leagueName);
        }
    }

    @Test
    void shouldRecognizeRequestedEuropeanDomesticCompetitionNames() throws Exception {
        for (String leagueName : List.of(
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
    void shouldRecognizeNewSelectableCompetitionNames() throws Exception {
        Map<String, Competition> competitionsByLeagueName = Map.of(
                "瑞超", Competition.SWEDISH_ALLSVENSKAN,
                "芬超", Competition.FINNISH_VEIKKAUSLIIGA,
                "韩职", Competition.K_LEAGUE_1);

        for (Map.Entry<String, Competition> item : competitionsByLeagueName.entrySet()) {
            var match = new ObjectMapper().readTree("""
                    {
                      "leagueNameAbbr": "%s"
                    }
                    """.formatted(item.getKey()));

            Competition competition =
                    ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

            assertEquals(item.getValue(), competition, item.getKey());
        }
    }

    @Test
    void shouldRecognizeSupplementalClubCompetitionNames() throws Exception {
        Map<String, Competition> competitionsByLeagueName = Map.ofEntries(
                Map.entry("俱乐部赛", Competition.CLUB_FRIENDLY),
                Map.entry("联赛杯", Competition.CLUB_OFFICIAL_OTHER),
                Map.entry("瑞甲", Competition.CLUB_OFFICIAL_OTHER),
                Map.entry("亚冠精英", Competition.CLUB_OFFICIAL_OTHER),
                Map.entry("Play-offs 1/2", Competition.CLUB_OFFICIAL_OTHER),
                Map.entry("韩挑战联", Competition.CLUB_OFFICIAL_OTHER),
                Map.entry("韩国杯", Competition.CLUB_OFFICIAL_OTHER));

        for (Map.Entry<String, Competition> item : competitionsByLeagueName.entrySet()) {
            var match = new ObjectMapper().readTree("""
                    {
                      "leagueNameAbbr": "%s"
                    }
                    """.formatted(item.getKey()));

            Competition competition =
                    ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

            assertEquals(item.getValue(), competition, item.getKey());
        }
    }

    @Test
    void shouldExcludeNorwegianEliteserienFromSportteryUpdates() throws Exception {
        var match = new ObjectMapper().readTree("""
                {
                  "leagueNameAbbr": "挪超"
                }
                """);

        Competition competition =
                ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

        assertNull(competition);
    }

    @Test
    void shouldExcludeNorwegianEliteserienScheduleFromSportteryUpdateScope() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        schedule.setGroupName("挪超");
        schedule.setMatchDate(LocalDate.of(2026, 7, 21));

        List<MatchSchedule> supportedSchedules =
                ReflectionTestUtils.invokeMethod(service, "filterSupportedSchedules", List.of(schedule));

        assertTrue(supportedSchedules.isEmpty());
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
    void shouldKeepClubFriendlySchedulesInSportteryUpdateScope() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CLUB_FRIENDLY);
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
        entry.setCompetition(Competition.CHAMPIONS_LEAGUE);
        entry.setHomeTeam("奥胡斯");
        entry.setAwayTeam("体彩客队名");

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId("ESPN-CHAMPIONS_LEAGUE-401841151");
        schedule.setCompetition(Competition.CHAMPIONS_LEAGUE);
        schedule.setMatchDate(LocalDate.of(2026, 7, 24));
        schedule.setHomeTeamCn("奥胡斯");
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
        entry.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        entry.setLeagueName("芬超");
        entry.setHomeTeam("玛丽港");
        entry.setAwayTeam("拉赫蒂");
        entry.setHomeScore(2);
        entry.setAwayScore(0);

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        List<MatchSchedule> schedules = service.getRecentCompletedSchedules(30);

        assertEquals(1, schedules.size());
        assertEquals("玛丽港", schedules.get(0).getHomeTeamCn());
        assertEquals("拉赫蒂", schedules.get(0).getAwayTeamCn());
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
        entry.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        entry.setLeagueName("芬超");
        entry.setHomeTeam("玛丽港");
        entry.setAwayTeam("拉赫蒂");
        entry.setHomeScore(2);
        entry.setAwayScore(0);
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        MatchSchedule officialSchedule = new MatchSchedule();
        officialSchedule.setMatchId("FOTMOB-CLUB_OFFICIAL_OTHER-401879459");
        officialSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        officialSchedule.setMatchDate(LocalDate.of(2026, 7, 18));
        officialSchedule.setGroupName("芬超");
        officialSchedule.setHomeTeamCn("玛丽港");
        officialSchedule.setAwayTeamCn("拉赫蒂");
        officialSchedule.setHomeTeamEn("IFK Mariehamn");
        officialSchedule.setAwayTeamEn("FC Lahti");
        officialSchedule.setHomeScore(2);
        officialSchedule.setAwayScore(0);
        List<MatchSchedule> schedules = new ArrayList<>(List.of(officialSchedule));

        int addedCount = service.mergeRecentCompletedSchedulesInto(schedules, 30);

        assertEquals(0, addedCount);
        assertEquals(1, schedules.size());
        assertEquals("2040535", schedules.get(0).getSportteryMatchId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldExposeUnmatchedUpcomingMarketAsScheduleCard() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040610");
        entry.setSportteryMatchNumber("周五201");
        entry.setMatchDate(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(1));
        entry.setKickoffTime(LocalTime.MIDNIGHT);
        entry.setCompetition(Competition.FINNISH_VEIKKAUSLIIGA);
        entry.setLeagueName("芬超");
        entry.setHomeTeam("雅罗");
        entry.setAwayTeam("塞伊奈约基");
        entry.setNormalOdds(new SportteryOdds(3.15, 3.65, 1.86, "2026-07-23 13:22:39"));
        entry.setHandicap(1);
        entry.setHandicapOdds(new SportteryOdds(1.74, 3.72, 3.50, "2026-07-23 13:22:39"));

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        List<MatchSchedule> schedules = new ArrayList<>();
        int addedCount = service.mergeRecentAndUpcomingSchedulesInto(schedules, 30, 7);

        assertEquals(1, addedCount);
        assertEquals(1, schedules.size());
        MatchSchedule schedule = schedules.get(0);
        assertEquals("SPORTTERY-2040610", schedule.getMatchId());
        assertEquals(Competition.FINNISH_VEIKKAUSLIIGA, schedule.getCompetition());
        assertEquals(entry.getMatchDate(), schedule.getMatchDate());
        assertEquals(LocalTime.MIDNIGHT, schedule.getKickoffTime());
        assertEquals("雅罗", schedule.getHomeTeamCn());
        assertEquals("塞伊奈约基", schedule.getAwayTeamCn());
        assertEquals("SCHEDULED", schedule.getStatus());
        assertEquals(3.15, schedule.getSportteryNormalOdds().getWin());
        assertEquals(1, schedule.getSportteryHandicap());
        assertNull(schedule.getHomeScore());
        assertNull(schedule.getAwayScore());
    }

}
