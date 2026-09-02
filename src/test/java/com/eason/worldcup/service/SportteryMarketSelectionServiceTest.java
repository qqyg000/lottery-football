package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import com.eason.worldcup.model.SportteryTotalGoalsOdds;
import com.eason.worldcup.util.ApplicationTime;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        Map<String, Competition> competitionsByLeagueName = Map.ofEntries(
                Map.entry("瑞超", Competition.SWEDISH_ALLSVENSKAN),
                Map.entry("芬超", Competition.FINNISH_VEIKKAUSLIIGA),
                Map.entry("意大利杯", Competition.SERIE_A),
                Map.entry("意杯", Competition.SERIE_A),
                Map.entry("韩职", Competition.K_LEAGUE_1),
                Map.entry("苏足总杯", Competition.SCOTTISH_FA_CUP));

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
                Map.entry("韩国杯", Competition.K_LEAGUE_1));

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
    void shouldParseNorwegianEliteserienForSportteryUpdates() throws Exception {
        var match = new ObjectMapper().readTree("""
                {
                  "leagueNameAbbr": "挪超"
                }
                """);

        Competition competition =
                ReflectionTestUtils.invokeMethod(service, "parseCompetition", match);

        assertEquals(Competition.CLUB_OFFICIAL_OTHER, competition);
    }

    @Test
    void shouldKeepNorwegianEliteserienScheduleInSportteryUpdateScope() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        schedule.setGroupName("挪超");
        schedule.setMatchDate(LocalDate.of(2026, 7, 21));

        List<MatchSchedule> supportedSchedules =
                ReflectionTestUtils.invokeMethod(service, "filterSupportedSchedules", List.of(schedule));

        assertEquals(List.of(schedule), supportedSchedules);
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
    void shouldRefreshCalculatorForKnownScheduleBeyondNormalTwoDayWindow() {
        ReflectionTestUtils.setField(service, "targetZone", "Asia/Shanghai");
        ReflectionTestUtils.setField(service, "futureDays", 2);
        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchDate(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(3));

        boolean containsUpcomingSchedule = Boolean.TRUE.equals(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "containsUpcomingSchedule",
                        List.of(schedule)));

        assertTrue(containsUpcomingSchedule);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCacheAllCalculatorRowsWithinSevenDayWindow() {
        LocalDate today = LocalDate.of(2026, 7, 25);
        SportteryMarketSelectionService.SportteryMarketEntry targetEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        targetEntry.setSportteryMatchId("2040641");
        targetEntry.setMatchDate(today.plusDays(3));
        targetEntry.setCurrentSale(true);
        SportteryMarketSelectionService.SportteryMarketEntry distantEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        distantEntry.setSportteryMatchId("2040999");
        distantEntry.setMatchDate(today.plusDays(8));
        distantEntry.setCurrentSale(true);

        int storedCount = ReflectionTestUtils.invokeMethod(
                service,
                "replaceUpcomingEntries",
                List.of(targetEntry, distantEntry),
                today);
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");

        assertEquals(1, storedCount);
        assertTrue(entries.containsKey("2040641"));
        assertFalse(entries.containsKey("2040999"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPreserveRecentlyClosedMarketWhileAwaitingSettlement() {
        LocalDate today = LocalDate.of(2026, 7, 29);
        SportteryMarketSelectionService.SportteryMarketEntry settlingEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        settlingEntry.setSportteryMatchId("2040643");
        settlingEntry.setMatchDate(today.minusDays(1));
        settlingEntry.setCurrentSale(true);
        settlingEntry.setNormalOdds(new SportteryOdds(2.80, 3.38, 2.10, "2026-07-28 21:48:38"));
        SportteryMarketSelectionService.SportteryMarketEntry staleEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        staleEntry.setSportteryMatchId("2040600");
        staleEntry.setMatchDate(today.minusDays(2));
        staleEntry.setCurrentSale(true);
        staleEntry.setNormalOdds(new SportteryOdds(2.10, 3.20, 3.10, "2026-07-27 21:00:00"));
        SportteryMarketSelectionService.SportteryMarketEntry currentEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        currentEntry.setSportteryMatchId("2040645");
        currentEntry.setMatchDate(today);
        currentEntry.setCurrentSale(true);

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(settlingEntry.getSportteryMatchId(), settlingEntry);
        entries.put(staleEntry.getSportteryMatchId(), staleEntry);

        int storedCount = ReflectionTestUtils.invokeMethod(
                service,
                "replaceUpcomingEntries",
                List.of(currentEntry),
                today);

        assertEquals(1, storedCount);
        assertTrue(entries.containsKey("2040643"));
        assertFalse(Boolean.TRUE.equals(entries.get("2040643").getCurrentSale()));
        assertFalse(entries.containsKey("2040600"));
        assertTrue(entries.containsKey("2040645"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDiscoverMissingMatchIdsBetweenRecentOfficialRows() {
        ReflectionTestUtils.setField(service, "targetZone", "Asia/Shanghai");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        SportteryMarketSelectionService.SportteryMarketEntry previousEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        previousEntry.setSportteryMatchId("2040641");
        previousEntry.setMatchDate(today.minusDays(1));
        SportteryMarketSelectionService.SportteryMarketEntry currentEntry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        currentEntry.setSportteryMatchId("2040645");
        currentEntry.setMatchDate(today);
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(previousEntry.getSportteryMatchId(), previousEntry);
        entries.put(currentEntry.getSportteryMatchId(), currentEntry);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchDate(today.minusDays(1));

        List<String> candidates = ReflectionTestUtils.invokeMethod(
                service,
                "buildSettlingMatchIdCandidates",
                List.of(schedule));

        assertEquals(List.of("2040642", "2040643", "2040644"), candidates);
    }

    @Test
    void shouldRecoverSettlingEntryFromOddsHistoryMetadata() throws Exception {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId("ESPN-UCL-401891534");
        schedule.setCompetition(Competition.CHAMPIONS_LEAGUE);
        schedule.setMatchDate(LocalDate.of(2026, 7, 28));
        schedule.setKickoffTime(LocalTime.of(23, 0));
        schedule.setHomeTeamCn("库奥皮奥");
        schedule.setHomeTeamEn("KuPS Kuopio");
        schedule.setAwayTeamCn("萨巴赫");
        schedule.setAwayTeamEn("Sabah FK");
        schedule.setHomeScore(1);
        schedule.setAwayScore(2);
        var value = new ObjectMapper().readTree("""
                {
                  "matchId": 2040643,
                  "leagueId": "69",
                  "leagueAbbName": "欧冠",
                  "homeTeamAllName": "库奥皮奥",
                  "awayTeamAllName": "萨巴赫",
                  "hadList": [
                    {
                      "h": "2.90",
                      "d": "3.30",
                      "a": "2.15",
                      "updateDate": "2026-07-28",
                      "updateTime": "18:00:00"
                    },
                    {
                      "h": "2.80",
                      "d": "3.38",
                      "a": "2.10",
                      "updateDate": "2026-07-28",
                      "updateTime": "21:48:38"
                    }
                  ],
                  "hhadList": [
                    {
                      "h": "1.57",
                      "d": "3.70",
                      "a": "4.50",
                      "goalLine": "+1",
                      "updateDate": "2026-07-28",
                      "updateTime": "21:48:48"
                    }
                  ]
                }
                """);

        SportteryMarketSelectionService.SportteryMarketEntry entry =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseSettlingEntry",
                        "2040643",
                        value,
                        List.of(schedule));

        assertNotNull(entry);
        assertEquals("2040643", entry.getSportteryMatchId());
        assertEquals(LocalDate.of(2026, 7, 28), entry.getMatchDate());
        assertEquals(LocalTime.of(23, 0), entry.getKickoffTime());
        assertEquals(Competition.CHAMPIONS_LEAGUE, entry.getCompetition());
        assertEquals("库奥皮奥", entry.getHomeTeam());
        assertEquals("萨巴赫", entry.getAwayTeam());
        assertEquals(2.80, entry.getNormalOdds().getWin());
        assertEquals(1, entry.getHandicap());
        assertEquals(1.57, entry.getHandicapOdds().getWin());
        assertEquals(1, entry.getHomeScore());
        assertEquals(2, entry.getAwayScore());
        assertFalse(Boolean.TRUE.equals(entry.getCurrentSale()));
    }

    @Test
    void shouldParseEarliestCompleteTotalGoalsOddsForHistoricalRefresh() throws Exception {
        var totalGoalsHistory = new ObjectMapper().readTree("""
                [
                  {
                    "s0": "12.00",
                    "s1": "5.50",
                    "s2": "3.80",
                    "s3": "3.60",
                    "s4": "5.40",
                    "s5": "10.00",
                    "s6": "18.00",
                    "s7": "25.00",
                    "updateDate": "2026-07-17",
                    "updateTime": "10:00:00"
                  },
                  {
                    "s0": "11.00",
                    "s1": "5.20",
                    "s2": "3.70",
                    "s3": "3.50",
                    "s4": "5.20",
                    "s5": "9.50",
                    "s6": "17.00",
                    "s7": "24.00",
                    "updateDate": "2026-07-17",
                    "updateTime": "18:00:00"
                  }
                ]
                """);

        SportteryTotalGoalsOdds odds = ReflectionTestUtils.invokeMethod(
                service,
                "parseInitialTotalGoalsOdds",
                totalGoalsHistory);

        assertNotNull(odds);
        assertEquals(12.00, odds.getGoal0());
        assertEquals(25.00, odds.getGoal7Plus());
        assertEquals("2026-07-17 10:00:00", odds.getUpdatedAt());
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
        LocalDate recentMatchDate = ApplicationTime.today().minusDays(1);
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040535");
        entry.setSportteryMatchNumber("周五204");
        entry.setMatchDate(recentMatchDate);
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
    void shouldApplyCompletedSportteryResultsToMatchedScheduledFixtures() {
        LocalDate matchDate = LocalDate.of(2026, 8, 26);
        SportteryMarketSelectionService.SportteryMarketEntry laskEntry = completedEntry(
                "2041053",
                "周二005",
                matchDate,
                "LASK林茨",
                "凯尔特人",
                4,
                1);
        SportteryMarketSelectionService.SportteryMarketEntry bodoEntry = completedEntry(
                "2041054",
                "周二006",
                matchDate,
                "博德闪耀",
                "奈梅亨",
                3,
                0);
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(laskEntry.getSportteryMatchId(), laskEntry);
        entries.put(bodoEntry.getSportteryMatchId(), bodoEntry);

        MatchSchedule laskSchedule = scheduledFixture(
                "FUTBOL24-CHAMPIONS_LEAGUE-3396885",
                matchDate,
                "LASK林茨",
                "凯尔特人",
                "LASK Linz",
                "Celtic FC");
        MatchSchedule bodoSchedule = scheduledFixture(
                "FUTBOL24-CHAMPIONS_LEAGUE-3396897",
                matchDate,
                "博德闪耀",
                "奈梅亨",
                "FK Bodo/Glimt",
                "NEC Nijmegen");

        int matchedCount = service.applyCachedSelections(List.of(laskSchedule, bodoSchedule));

        assertEquals(2, matchedCount);
        assertCompletedResult(laskSchedule, "2041053", "周二005", 4, 1);
        assertCompletedResult(bodoSchedule, "2041054", "周二006", 3, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMatchRequestedSwedishCompletedFixtureAfterRefresh() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040532");
        entry.setSportteryMatchNumber("周五201");
        entry.setMatchDate(LocalDate.of(2026, 7, 18));
        entry.setCompetition(Competition.SWEDISH_ALLSVENSKAN);
        entry.setLeagueName("瑞超");
        entry.setHomeTeam("IFK哥德堡");
        entry.setAwayTeam("布鲁马波卡纳");
        entry.setHomeScore(2);
        entry.setAwayScore(1);
        entry.setNormalOdds(new SportteryOdds(1.93, 3.40, 3.15, "2026-07-17 21:17:42"));
        entry.setHandicap(-1);
        entry.setHandicapOdds(new SportteryOdds(3.70, 3.90, 1.66, "2026-07-17 21:18:24"));
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId("FOTMOB-SWEDISH_ALLSVENSKAN-5107535");
        schedule.setCompetition(Competition.SWEDISH_ALLSVENSKAN);
        schedule.setMatchDate(LocalDate.of(2026, 7, 18));
        schedule.setGroupName("瑞超 第13轮");
        schedule.setHomeTeamCn("IFK哥德堡");
        schedule.setAwayTeamCn("布鲁马波卡纳");
        schedule.setHomeTeamEn("IFK Göteborg");
        schedule.setAwayTeamEn("Brommapojkarna");
        schedule.setHomeScore(2);
        schedule.setAwayScore(1);
        List<MatchSchedule> schedules = new ArrayList<>(List.of(schedule));

        int matchedCount = service.applyCachedSelections(schedules);

        assertEquals(1, matchedCount);
        assertEquals("2040532", schedule.getSportteryMatchId());
        assertEquals(1.93, schedule.getSportteryNormalOdds().getWin());
        assertEquals(-1, schedule.getSportteryHandicap());
        assertEquals(3.70, schedule.getSportteryHandicapOdds().getWin());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPreserveBundledTotalGoalsOddsWhenCachedEntryDoesNotContainThem() {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040532");
        entry.setMatchDate(LocalDate.of(2026, 7, 18));
        entry.setCompetition(Competition.SWEDISH_ALLSVENSKAN);
        entry.setHomeTeam("IFK哥德堡");
        entry.setAwayTeam("布鲁马波卡纳");
        entry.setNormalOdds(new SportteryOdds(1.93, 3.40, 3.15, "2026-07-17 21:17:42"));
        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId("HIS-2040532");
        schedule.setSportteryMatchId("HIS-2040532");
        schedule.setCompetition(Competition.SWEDISH_ALLSVENSKAN);
        schedule.setMatchDate(LocalDate.of(2026, 7, 18));
        schedule.setHomeTeamCn("IFK哥德堡");
        schedule.setAwayTeamCn("布鲁马波卡纳");
        SportteryTotalGoalsOdds totalGoalsOdds = new SportteryTotalGoalsOdds(
                12.0,
                5.5,
                3.8,
                3.6,
                5.4,
                10.0,
                18.0,
                25.0,
                "2026-07-17 10:00:00");
        schedule.setSportteryTotalGoalsOdds(totalGoalsOdds);

        int matchedCount = service.applyCachedSelections(List.of(schedule));

        assertEquals(1, matchedCount);
        assertEquals("2040532", schedule.getSportteryMatchId());
        assertEquals(totalGoalsOdds, schedule.getSportteryTotalGoalsOdds());
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
        assertEquals("塞伊奈", schedule.getAwayTeamCn());
        assertEquals("SCHEDULED", schedule.getStatus());
        assertEquals(3.15, schedule.getSportteryNormalOdds().getWin());
        assertEquals(1, schedule.getSportteryHandicap());
        assertNull(schedule.getHomeScore());
        assertNull(schedule.getAwayScore());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldExposeKoreanCupOddsAsKLeagueScheduleCard() throws Exception {
        var match = new ObjectMapper().readTree("""
                {
                  "leagueNameAbbr": "韩国杯"
                }
                """);
        Competition competition = ReflectionTestUtils.invokeMethod(
                service,
                "parseCompetition",
                match);
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId("2040801");
        entry.setSportteryMatchNumber("周三001");
        entry.setMatchDate(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(1));
        entry.setKickoffTime(LocalTime.of(18, 30));
        entry.setCompetition(competition);
        entry.setLeagueName("韩国杯");
        entry.setHomeTeam("蔚山现代");
        entry.setAwayTeam("浦项制铁");
        entry.setNormalOdds(new SportteryOdds(1.80, 3.40, 3.60, "2026-08-19 10:00:00"));

        Map<String, SportteryMarketSelectionService.SportteryMarketEntry> entries =
                (Map<String, SportteryMarketSelectionService.SportteryMarketEntry>)
                        ReflectionTestUtils.getField(service, "entriesByMatchId");
        entries.put(entry.getSportteryMatchId(), entry);

        List<MatchSchedule> schedules = new ArrayList<>();
        int addedCount = service.mergeRecentAndUpcomingSchedulesInto(schedules, 30, 7);

        assertEquals(1, addedCount);
        MatchSchedule schedule = schedules.get(0);
        assertEquals(Competition.K_LEAGUE_1, schedule.getCompetition());
        assertEquals("韩国杯", schedule.getGroupName());
        assertEquals(1.80, schedule.getSportteryNormalOdds().getWin());
        assertEquals("SCHEDULED", schedule.getStatus());
    }

    private SportteryMarketSelectionService.SportteryMarketEntry completedEntry(
            String matchId,
            String matchNumber,
            LocalDate matchDate,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore) {
        SportteryMarketSelectionService.SportteryMarketEntry entry =
                new SportteryMarketSelectionService.SportteryMarketEntry();
        entry.setSportteryMatchId(matchId);
        entry.setSportteryMatchNumber(matchNumber);
        entry.setMatchDate(matchDate);
        entry.setCompetition(Competition.CHAMPIONS_LEAGUE);
        entry.setLeagueName("欧冠");
        entry.setHomeTeam(homeTeam);
        entry.setAwayTeam(awayTeam);
        entry.setHomeScore(homeScore);
        entry.setAwayScore(awayScore);
        return entry;
    }

    private MatchSchedule scheduledFixture(
            String matchId,
            LocalDate matchDate,
            String homeTeamCn,
            String awayTeamCn,
            String homeTeamEn,
            String awayTeamEn) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setMatchId(matchId);
        schedule.setCompetition(Competition.CHAMPIONS_LEAGUE);
        schedule.setMatchDate(matchDate);
        schedule.setKickoffTime(LocalTime.of(3, 0));
        schedule.setGroupName("欧冠");
        schedule.setHomeTeamCn(homeTeamCn);
        schedule.setAwayTeamCn(awayTeamCn);
        schedule.setHomeTeamEn(homeTeamEn);
        schedule.setAwayTeamEn(awayTeamEn);
        schedule.setStatus("SCHEDULED");
        return schedule;
    }

    private void assertCompletedResult(
            MatchSchedule schedule,
            String matchId,
            String matchNumber,
            int homeScore,
            int awayScore) {
        assertEquals("COMPLETED", schedule.getStatus());
        assertEquals(homeScore, schedule.getHomeScore());
        assertEquals(awayScore, schedule.getAwayScore());
        assertEquals(matchId, schedule.getSportteryMatchId());
        assertEquals(matchNumber, schedule.getSportteryMatchNumber());
    }

}
