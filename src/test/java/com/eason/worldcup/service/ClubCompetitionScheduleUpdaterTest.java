package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubCompetitionScheduleUpdaterTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final ClubCompetitionScheduleUpdater updater = new ClubCompetitionScheduleUpdater(objectMapper);

    @Test
    void shouldScaleTaskProgressWithinConfiguredStage() {
        assertEquals(10, ClubCompetitionScheduleUpdater.scaleTaskProgress(0, 40, 10, 35));
        assertEquals(23, ClubCompetitionScheduleUpdater.scaleTaskProgress(20, 40, 10, 35));
        assertEquals(35, ClubCompetitionScheduleUpdater.scaleTaskProgress(40, 40, 10, 35));
        assertEquals(10, ClubCompetitionScheduleUpdater.scaleTaskProgress(-1, 40, 10, 35));
        assertEquals(35, ClubCompetitionScheduleUpdater.scaleTaskProgress(41, 40, 10, 35));
    }

    @Test
    void shouldContinueWhenOneParallelScheduleSourceStalls() {
        MatchSchedule completedSchedule = new MatchSchedule();
        completedSchedule.setMatchId("FOTMOB-SWEDISH_ALLSVENSKAN-5107535");
        List<Supplier<List<MatchSchedule>>> tasks = List.of(
                () -> List.of(completedSchedule),
                () -> {
                    try {
                        Thread.sleep(5_000L);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    return List.of();
                });

        List<MatchSchedule> schedules = assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> updater.executeTasks(tasks, 2, Duration.ofMillis(50), null));

        assertEquals(List.of(completedSchedule), schedules);
    }

    @Test
    void shouldStartTaskTimeoutAfterQueuedTaskEntersAnExecutionBatch() {
        List<Supplier<List<MatchSchedule>>> tasks = new ArrayList<>();
        for (int index = 0; index < 4; index++) {
            int matchIndex = index;
            tasks.add(() -> {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                MatchSchedule schedule = new MatchSchedule();
                schedule.setMatchId("QUEUED-" + matchIndex);
                return List.of(schedule);
            });
        }

        List<MatchSchedule> schedules = assertTimeoutPreemptively(
                Duration.ofSeconds(2),
                () -> updater.executeTasks(tasks, 1, Duration.ofMillis(250), null));

        assertEquals(4, schedules.size());
    }

    @Test
    void shouldAllowFotMobRetryWindowBeforeParallelTaskTimeout() {
        assertEquals(
                Duration.ofSeconds(35),
                ClubCompetitionScheduleUpdater.calculateParallelTaskTimeout(Duration.ofSeconds(15), 2));
    }

    @Test
    void shouldParseCompletedSudamericanaMatchInShanghaiTime() throws Exception {
        JsonNode event = objectMapper.readTree("""
                {
                  "id": "401865439",
                  "date": "2026-05-21T22:00Z",
                  "season": { "slug": "group-stage" },
                  "status": {
                    "period": 2,
                    "type": { "completed": true, "state": "post" }
                  },
                  "competitions": [
                    {
                      "neutralSite": false,
                      "venue": { "fullName": "Arena MRV" },
                      "competitors": [
                        {
                          "homeAway": "home",
                          "score": "2",
                          "team": { "id": "7632", "displayName": "Atlético-MG" }
                        },
                        {
                          "homeAway": "away",
                          "score": "0",
                          "team": { "id": "3372", "displayName": "Cienciano del Cusco" }
                        }
                      ]
                    }
                  ]
                }
                """);

        MatchSchedule schedule = updater.parseEspnEvent(
                event,
                Competition.CLUB_OFFICIAL_OTHER,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("ESPN-CLUB_OFFICIAL_OTHER-401865439", schedule.getMatchId());
        assertEquals(LocalDate.of(2026, 5, 22), schedule.getMatchDate());
        assertEquals(LocalTime.of(6, 0), schedule.getKickoffTime());
        assertEquals("米内罗竞技", schedule.getHomeTeamCn());
        assertEquals("Cienciano", schedule.getAwayTeamCn());
        assertEquals(2, schedule.getHomeScore());
        assertEquals(0, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
    }

    @Test
    void shouldUseThirtyDayLookbackAndSevenDayForwardWindow() {
        ReflectionTestUtils.setField(updater, "daysBack", 30);
        ReflectionTestUtils.setField(updater, "daysForward", 7);

        assertEquals(
                LocalDate.of(2026, 6, 22),
                updater.resolveRefreshStartDate(LocalDate.of(2026, 7, 22)));
        assertEquals(
                LocalDate.of(2026, 7, 29),
                updater.resolveRefreshEndDate(LocalDate.of(2026, 7, 22)));
    }

    @Test
    void shouldResolveHybridAfcChampionsLeagueSeasonNames() {
        ClubCompetitionScheduleUpdater.FotMobLeagueSource source =
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "525",
                        "亚冠精英",
                        false,
                        2023);

        assertEquals("2022", source.seasonValue(2022));
        assertEquals("2023%2F2024", source.seasonValue(2023));
    }

    @Test
    void shouldResolveHistoricalScottishLeagueCupSeasonNames() {
        ClubCompetitionScheduleUpdater.FotMobLeagueSource source =
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "180",
                        "苏联赛杯",
                        false);

        assertEquals("2015%2F2016", source.seasonValue(2015));
        assertEquals("2016", source.seasonValue(2016));
        assertEquals("2021", source.seasonValue(2021));
        assertEquals("2022%2F2023", source.seasonValue(2022));
    }

    @Test
    void shouldFilterReplacedSeasonsFromImmutableSourceList() {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        schedule.setMatchDate(LocalDate.of(2026, 7, 23));

        List<MatchSchedule> filtered = updater.removeReplacedCachedSchedules(
                List.of(schedule),
                Set.of(new ClubCompetitionScheduleUpdater.CompetitionSeason(
                        Competition.CLUB_OFFICIAL_OTHER,
                        2026)),
                LocalDate.of(2026, 6, 23),
                LocalDate.of(2026, 8, 22));

        assertTrue(filtered.isEmpty());
    }

    @Test
    void shouldOnlyReplaceCacheRowsFromTheLoadedFotMobSource() {
        MatchSchedule slovakSchedule = new MatchSchedule();
        slovakSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        slovakSchedule.setMatchDate(LocalDate.of(2026, 7, 26));
        slovakSchedule.setGroupName("斯洛伐超 第1轮");
        MatchSchedule greekSchedule = new MatchSchedule();
        greekSchedule.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        greekSchedule.setMatchDate(LocalDate.of(2026, 8, 22));
        greekSchedule.setGroupName("希超 第1轮");

        List<MatchSchedule> filtered = updater.removeReplacedCachedSchedules(
                List.of(slovakSchedule, greekSchedule),
                Set.of(new ClubCompetitionScheduleUpdater.CompetitionSeason(
                        Competition.CLUB_OFFICIAL_OTHER,
                        2026,
                        "斯洛伐超")),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 31));

        assertEquals(List.of(greekSchedule), filtered);
    }

    @Test
    void shouldDeduplicateMappedTeamsAcrossProviders() {
        MatchSchedule espn = completedSchedule(
                "ESPN-CHAMPIONS_LEAGUE-401841108",
                "库奥皮奥",
                "AGF");
        MatchSchedule fotMob = completedSchedule(
                "FOTMOB-CHAMPIONS_LEAGUE-5103509",
                "KuPS Kuopio",
                "奥胡斯");

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(espn, fotMob));

        assertEquals(1, schedules.size());
        assertEquals("库奥皮奥", schedules.get(0).getHomeTeamCn());
        assertEquals("奥胡斯", schedules.get(0).getAwayTeamCn());
    }

    @Test
    void shouldDeduplicateLevadiaAndHamKamAliasesAcrossProviders() {
        MatchSchedule espnLevadia = completedSchedule(
                "ESPN-CLUB_OFFICIAL_OTHER-401877798",
                "Caernarfon",
                "FC Levadia Tallinn");
        espnLevadia.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        MatchSchedule fotMobLevadia = completedSchedule(
                "FOTMOB-CLUB_OFFICIAL_OTHER-5786616",
                "Caernarfon",
                "FCI Levadia");
        fotMobLevadia.setCompetition(Competition.CLUB_OFFICIAL_OTHER);

        List<MatchSchedule> levadiaSchedules = updater.deduplicateSchedulesByFixture(
                List.of(espnLevadia, fotMobLevadia));

        assertEquals(1, levadiaSchedules.size());
        assertEquals("利瓦迪亚", levadiaSchedules.get(0).getAwayTeamCn());

        MatchSchedule futbol24HamKam = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3337036",
                "IFK哥德堡",
                "HamKam",
                1,
                2);
        MatchSchedule fotMobHamKam = completedClubFriendlySchedule(
                "FOTMOB-CLUB_FRIENDLY-5838510",
                "IFK哥德堡",
                "Hamarkameratene",
                1,
                2);

        List<MatchSchedule> hamKamSchedules = updater.deduplicateSchedulesByFixture(
                List.of(futbol24HamKam, fotMobHamKam));

        assertEquals(1, hamKamSchedules.size());
        assertEquals("汉坎", hamKamSchedules.get(0).getAwayTeamCn());
    }

    @Test
    void shouldDeduplicateSameOfficialTeamDateAndScoreWithOpponentAlias() {
        MatchSchedule first = completedSchedule(
                "SOURCE-001",
                "测试主队",
                "Unknown Opponent");
        first.setHomeScore(2);
        first.setAwayScore(0);
        MatchSchedule second = completedSchedule(
                "SOURCE-002",
                "测试主队",
                "未知对手别名");
        second.setHomeScore(2);
        second.setAwayScore(0);
        second.setSportteryMatchId("20260718-001");

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(first, second));

        assertEquals(1, schedules.size());
    }

    @Test
    void shouldDeduplicateReversedClubFriendlyAcrossProviders() {
        MatchSchedule espn = completedClubFriendlySchedule(
                "ESPN-CLUB_FRIENDLY-401888135",
                "FC Kharkiv",
                "萨尔茨堡",
                0,
                4);
        MatchSchedule futbol24 = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3355945",
                "萨尔茨堡",
                "FC Kharkiv",
                4,
                0);

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(espn, futbol24));

        assertEquals(1, schedules.size());
    }

    @Test
    void shouldDeduplicateMappedClubFriendlyAliases() {
        MatchSchedule espn = completedClubFriendlySchedule(
                "ESPN-CLUB_FRIENDLY-401889643",
                "莱切斯特",
                "Northampton Town",
                3,
                0);
        MatchSchedule futbol24 = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3360592",
                "莱切斯特",
                "Northampton",
                3,
                0);

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(espn, futbol24));

        assertEquals(1, schedules.size());
        assertEquals("北安普敦", schedules.get(0).getAwayTeamCn());
    }

    @Test
    void shouldDeduplicateAikClubFriendlyAcrossProviders() {
        MatchSchedule futbol24 = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3337082",
                "AIK Fotboll",
                "韦斯特罗斯",
                3,
                2);
        futbol24.setMatchDate(LocalDate.of(2026, 6, 28));
        futbol24.setKickoffTime(LocalTime.of(20, 0));
        MatchSchedule fotMob = completedClubFriendlySchedule(
                "FOTMOB-CLUB_FRIENDLY-5838835",
                "AIK索尔纳",
                "韦斯特罗斯",
                3,
                2);
        fotMob.setMatchDate(LocalDate.of(2026, 6, 28));
        fotMob.setKickoffTime(LocalTime.of(20, 0));

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(futbol24, fotMob));

        assertEquals(1, schedules.size());
        assertEquals("索尔纳", schedules.get(0).getHomeTeamCn());
    }

    @Test
    void shouldDeduplicateAdjacentDateClubFriendlyAcrossProviders() {
        MatchSchedule footMercato = completedClubFriendlySchedule(
                "FOOTMERCATO-3996254935012421155",
                "阿尔克马尔",
                "安德莱",
                0,
                1);
        footMercato.setMatchDate(LocalDate.of(2026, 7, 15));
        footMercato.setKickoffTime(LocalTime.of(16, 30));
        MatchSchedule futbol24 = completedClubFriendlySchedule(
                "FUTBOL24-CLUB-FRIENDLY-3371034",
                "AZ Alkmaar",
                "Anderlecht",
                0,
                1);
        futbol24.setMatchDate(LocalDate.of(2026, 7, 16));
        futbol24.setKickoffTime(LocalTime.of(0, 30));

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(footMercato, futbol24));

        assertEquals(1, schedules.size());
        assertEquals("阿尔克马", schedules.get(0).getHomeTeamCn());
        assertEquals("安德莱", schedules.get(0).getAwayTeamCn());
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

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(
                futbol24,
                fotMob));

        assertEquals(1, schedules.size());
    }

    @Test
    void shouldDeduplicateAdjacentDateScheduledMatchesFromSameProvider() {
        MatchSchedule first = completedSchedule(
                "FOTMOB-SCOTTISH_FA_CUP-6000001",
                "Sporting",
                "Dynamo Kiev");
        first.setCompetition(Competition.SCOTTISH_FA_CUP);
        first.setGroupName("苏足总杯");
        first.setMatchDate(LocalDate.of(2026, 8, 1));
        first.setStatus("SCHEDULED");
        first.setHomeScore(null);
        first.setAwayScore(null);
        MatchSchedule second = completedSchedule(
                "FOTMOB-SCOTTISH_FA_CUP-6000002",
                "里斯本",
                "基迪纳摩");
        second.setCompetition(Competition.SCOTTISH_FA_CUP);
        second.setGroupName("苏格兰足总杯");
        second.setMatchDate(LocalDate.of(2026, 8, 2));
        second.setStatus("SCHEDULED");
        second.setHomeScore(null);
        second.setAwayScore(null);

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(
                first,
                second));

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

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(league, cup));

        assertEquals(2, schedules.size());
    }

    @Test
    void shouldDeduplicatePortugueseSuperCupFinalAcrossProviders() {
        MatchSchedule futbol24 = completedSchedule(
                "FUTBOL24-CLUB_OFFICIAL_OTHER-3364604",
                "波尔图",
                "托林斯");
        futbol24.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        futbol24.setGroupName("葡超杯");
        futbol24.setMatchDate(LocalDate.of(2026, 8, 2));
        futbol24.setKickoffTime(LocalTime.of(3, 15));
        MatchSchedule fotMob = completedSchedule(
                "FOTMOB-CLUB_OFFICIAL_OTHER-5886251",
                "波尔图",
                "托林斯");
        fotMob.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        fotMob.setGroupName("葡超杯 第final轮");
        fotMob.setMatchDate(LocalDate.of(2026, 8, 2));
        fotMob.setKickoffTime(LocalTime.of(3, 15));

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(futbol24, fotMob));

        assertEquals(1, schedules.size());
    }

    @Test
    void shouldDeduplicateSameKickoffAndResultAcrossClubFriendlyProviders() {
        MatchSchedule futbol24 = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3337055",
                "FC Hertha Wels",
                "LASK林茨",
                0,
                5);
        futbol24.setMatchDate(LocalDate.of(2026, 6, 28));
        futbol24.setKickoffTime(LocalTime.of(0, 30));
        MatchSchedule fotMob = completedClubFriendlySchedule(
                "FOTMOB-CLUB_FRIENDLY-5766943",
                "SPG HOGO Wels",
                "LASK林茨",
                0,
                5);
        fotMob.setMatchDate(LocalDate.of(2026, 6, 28));
        fotMob.setKickoffTime(LocalTime.of(0, 30));

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(futbol24, fotMob));

        assertEquals(1, schedules.size());
    }

    @Test
    void shouldPreferMappedEnglishNameForStaleCachedChineseName() {
        MatchSchedule cached = completedClubFriendlySchedule(
                "ESPN-CLUB_FRIENDLY-401886523",
                "默德林",
                "费内巴切",
                0,
                5);
        cached.setHomeTeamEn("FC Admira Wacker Modling");
        cached.setAwayTeamEn("Fenerbahce");

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(cached));

        assertEquals(1, schedules.size());
        assertEquals("默德林", schedules.get(0).getHomeTeamCn());
    }

    @Test
    void shouldKeepLegitimateSameDayClubFriendliesWithSameScore() {
        MatchSchedule first = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3348282",
                "Ludogorets",
                "Septemvri Sofia",
                1,
                0);
        MatchSchedule second = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3348283",
                "Ludogorets",
                "KF Teuta Durres",
                1,
                0);

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(
                List.of(first, second));

        assertEquals(2, schedules.size());
    }

    @Test
    void shouldParseAzerbaijanPremierLeagueMatch() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "4946822",
                  "round": "33",
                  "home": { "name": "Turan Tovuz" },
                  "away": { "name": "Sabah FK" },
                  "status": {
                    "utcTime": "2026-05-22T13:00:00Z",
                    "finished": true,
                    "started": true,
                    "cancelled": false,
                    "scoreStr": "1 - 2"
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "262",
                        "阿塞超"),
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("FOTMOB-CLUB_OFFICIAL_OTHER-4946822", schedule.getMatchId());
        assertEquals(LocalDate.of(2026, 5, 22), schedule.getMatchDate());
        assertEquals("Turan", schedule.getHomeTeamCn());
        assertEquals("萨巴赫", schedule.getAwayTeamCn());
        assertEquals(1, schedule.getHomeScore());
        assertEquals(2, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
    }

    @Test
    void shouldKeepUnmappedTeamsFromCompleteAzerbaijanLeagueSource() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "9990001",
                  "home": { "name": "Newly Promoted Home" },
                  "away": { "name": "Newly Promoted Away" },
                  "status": {
                    "utcTime": "2026-07-20T13:00:00Z",
                    "finished": true,
                    "started": true,
                    "cancelled": false,
                    "scoreStr": "2 - 0"
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "262",
                        "阿塞超"),
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("Newly Promoted Home", schedule.getHomeTeamCn());
        assertEquals("Newly Promoted Away", schedule.getAwayTeamCn());
    }

    @Test
    void shouldUseFotMobNinetyMinuteScoreForMatchCompletedAfterExtraTime() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "5231353",
                  "round": "1/4",
                  "home": { "name": "Sirius" },
                  "away": { "name": "IFK Göteborg" },
                  "status": {
                    "utcTime": "2026-03-15T16:15:00Z",
                    "finished": true,
                    "started": true,
                    "cancelled": false,
                    "scoreStr": "1 - 0",
                    "reason": {
                      "short": "AET",
                      "long": "After extra time"
                    }
                  }
                }
                """);
        JsonNode matchDetails = objectMapper.readTree("""
                {
                  "content": {
                    "matchFacts": {
                      "events": {
                        "events": [
                          {
                            "type": "Half",
                            "time": 90,
                            "homeScore": 0,
                            "awayScore": 0,
                            "halfStrShort": "FT"
                          },
                          {
                            "type": "Goal",
                            "time": 104,
                            "isHome": true,
                            "isPenaltyShootoutEvent": false
                          }
                        ]
                      }
                    }
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "171",
                        "瑞典杯"),
                ZoneId.of("Asia/Shanghai"),
                matchDetails);

        assertNotNull(schedule);
        assertEquals(0, schedule.getHomeScore());
        assertEquals(0, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
    }

    @Test
    void shouldIncludeStoppageTimeGoalsAndExcludeExtraTimeGoals() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "9000001",
                  "home": { "name": "Home" },
                  "away": { "name": "Away" },
                  "status": {
                    "utcTime": "2026-08-08T16:00:00Z",
                    "finished": true,
                    "started": true,
                    "cancelled": false,
                    "scoreStr": "2 - 1",
                    "reason": { "short": "AET", "long": "After extra time" }
                  }
                }
                """);
        JsonNode matchDetails = objectMapper.readTree("""
                {
                  "content": {
                    "matchFacts": {
                      "events": {
                        "events": [
                          { "type": "Goal", "time": 45, "isHome": true },
                          { "type": "Goal", "time": 90, "overloadTime": 4, "isHome": false },
                          { "type": "Goal", "time": 105, "isHome": true }
                        ]
                      }
                    }
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "235",
                        "荷兰杯"),
                ZoneId.of("Asia/Shanghai"),
                matchDetails);

        assertNotNull(schedule);
        assertEquals(1, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
    }

    @Test
    void shouldNotUseFotMobExtraTimeScoreWithoutRegulationDetails() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "5231353",
                  "home": { "name": "Sirius" },
                  "away": { "name": "IFK Göteborg" },
                  "status": {
                    "utcTime": "2026-03-15T16:15:00Z",
                    "finished": true,
                    "scoreStr": "1 - 0",
                    "reason": { "short": "AET" }
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "171",
                        "瑞典杯"),
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertNull(schedule.getHomeScore());
        assertNull(schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
    }

    @Test
    void shouldParseVerifiedClubFriendlyScore() throws Exception {
        JsonNode event = objectMapper.readTree("""
                {
                  "id": "14573151",
                  "startTimestamp": 1783004400,
                  "status": { "type": "finished" },
                  "tournament": { "uniqueTournament": { "id": 853 } },
                  "homeTeam": { "name": "Polissya Zhytomyr" },
                  "awayTeam": { "name": "Sabah FK" },
                  "homeScore": { "current": 4, "normaltime": 4 },
                  "awayScore": { "current": 1, "normaltime": 1 },
                  "neutral": true
                }
                """);

        MatchSchedule schedule = updater.parseSofaScoreEvent(
                event,
                new ClubCompetitionScheduleUpdater.SofaScoreTournamentSource(
                        Competition.CLUB_FRIENDLY,
                        "853",
                        "俱乐部友谊赛"),
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("SOFASCORE-CLUB_FRIENDLY-14573151", schedule.getMatchId());
        assertEquals(LocalDate.of(2026, 7, 2), schedule.getMatchDate());
        assertEquals(LocalTime.of(23, 0), schedule.getKickoffTime());
        assertEquals("Polissya", schedule.getHomeTeamCn());
        assertEquals("萨巴赫", schedule.getAwayTeamCn());
        assertEquals(4, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
    }

    @Test
    void shouldParseFutbol24ClubFriendlyMatch() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 472,
                  "status_id": 5,
                  "date": "2026-07-02T15:00:00+00:00",
                  "score1": "4-1",
                  "team1": { "name": "Polissya Zhytomyr" },
                  "team2": { "name": "Sabah FK" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3345454",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("FUTBOL24-CLUB_FRIENDLY-3345454", schedule.getMatchId());
        assertEquals(LocalDate.of(2026, 7, 2), schedule.getMatchDate());
        assertEquals(LocalTime.of(23, 0), schedule.getKickoffTime());
        assertEquals("Polissya", schedule.getHomeTeamCn());
        assertEquals("萨巴赫", schedule.getAwayTeamCn());
        assertEquals(4, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
    }

    @Test
    void shouldUseVerifiedRegulationTimeScoreForUnionAndPatroEisden() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 472,
                  "status_id": 5,
                  "date": "2026-07-24T11:30:00+00:00",
                  "score1": "4-1",
                  "score2": "2-0",
                  "team1": { "name": "Union Saint-Gilloise" },
                  "team2": { "name": "Patro Eisden" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3383418",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("圣吉联合", schedule.getHomeTeamCn());
        assertEquals(4, schedule.getHomeScore());
        assertEquals(0, schedule.getAwayScore());
    }

    @Test
    void shouldUseRegulationTimeScoreForLechPoznanAndAarhus() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "9": { "name": "AP", "name_short": "AP", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 8,
                  "status_id": 9,
                  "date": "2026-07-29T17:00:00+00:00",
                  "score1": "1-4",
                  "score2": "p.3-4",
                  "team1": { "name": "Lech Poznań" },
                  "team2": { "name": "AGF Aarhus" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3332579",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(LocalDate.of(2026, 7, 30), schedule.getMatchDate());
        assertEquals(LocalTime.of(1, 0), schedule.getKickoffTime());
        assertEquals("波兹南", schedule.getHomeTeamCn());
        assertEquals("奥胡斯", schedule.getAwayTeamCn());
        assertEquals(0, schedule.getHomeScore());
        assertEquals(3, schedule.getAwayScore());
    }

    @Test
    void shouldParseAzerbaijanCupMatchInShanghaiTime() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 525,
                  "status_id": 5,
                  "date": "2026-05-13T16:00:00+00:00",
                  "score1": "2-1",
                  "team1": { "name": "Sabah FK" },
                  "team2": { "name": "Zira FK" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3320356",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("FUTBOL24-CLUB_OFFICIAL_OTHER-3320356", schedule.getMatchId());
        assertEquals(LocalDate.of(2026, 5, 14), schedule.getMatchDate());
        assertEquals(LocalTime.MIDNIGHT, schedule.getKickoffTime());
        assertEquals("阿塞杯", schedule.getGroupName());
        assertEquals("萨巴赫", schedule.getHomeTeamCn());
        assertEquals("齐拉", schedule.getAwayTeamCn());
        assertEquals(2, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
    }

    @Test
    void shouldParseScottishFaCupMatchInShanghaiTime() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "1": { "name": "Not started", "name_short": "NS", "is_ended": false }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 520,
                  "status_id": 1,
                  "date": "2026-08-01T18:30:00+00:00",
                  "team1": { "name": "Dundee United" },
                  "team2": { "name": "Dunfermline Athletic" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3400001",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("FUTBOL24-SCOTTISH_FA_CUP-3400001", schedule.getMatchId());
        assertEquals(Competition.SCOTTISH_FA_CUP, schedule.getCompetition());
        assertEquals(LocalDate.of(2026, 8, 2), schedule.getMatchDate());
        assertEquals(LocalTime.of(2, 30), schedule.getKickoffTime());
        assertEquals("苏足总杯", schedule.getGroupName());
        assertEquals("SCHEDULED", schedule.getStatus());
    }

    @Test
    void shouldUseFutbol24NinetyMinuteScoreAfterExtraTime() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "8": { "name": "AET", "name_short": "AET", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 525,
                  "status_id": 8,
                  "date": "2025-12-02T15:00:00+00:00",
                  "score1": "1-0",
                  "score2": "0-0, 0-0",
                  "team1": { "name": "Zira FK" },
                  "team2": { "name": "Neftchi Baku" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3249056",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(0, schedule.getHomeScore());
        assertEquals(0, schedule.getAwayScore());
    }

    @Test
    void shouldUseVerifiedRomanianSuperCupRegulationScoreBeforePenalties() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "15": { "name": "AP w/ET", "name_short": "AP", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 286,
                  "status_id": 15,
                  "date": "2026-07-12T17:30:00+00:00",
                  "slug": "2026/07/12/national/Romania/Super-Cup/2026/U-Craiova/vs/Univ-Cluj",
                  "score1": "1-1",
                  "score2": "p.5-3",
                  "team1": { "name": "Universitatea Craiova" },
                  "team2": { "name": "Universitatea Cluj" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3328575",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(LocalDate.of(2026, 7, 13), schedule.getMatchDate());
        assertEquals("罗超杯", schedule.getGroupName());
        assertEquals("克拉约瓦", schedule.getHomeTeamCn());
        assertEquals(1, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
    }

    @Test
    void shouldRejectPenaltyOnlyScoreWithoutRegulationDetails() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "15": { "name": "AP w/ET", "name_short": "AP", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 472,
                  "status_id": 15,
                  "date": "2026-07-31T18:00:00+00:00",
                  "slug": "2026/07/31/international/International/Club-Friendly/2026/Test/vs/Test",
                  "score1": "1-1",
                  "score2": "p.4-2",
                  "team1": { "name": "SBV Excelsior" },
                  "team2": { "name": "NFC Volos" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3391547",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNull(schedule);
    }

    @Test
    void shouldParseRegulationScoreFromFutbol24MatchPage() {
        Object score = ReflectionTestUtils.invokeMethod(
                updater,
                "parseFutbol24FullTimeScore",
                "<div>HT 0-1, FT 0-3, AET 1-4</div>");

        assertNotNull(score);
        assertEquals(0, ReflectionTestUtils.getField(score, "homeScore"));
        assertEquals(3, ReflectionTestUtils.getField(score, "awayScore"));
        assertNull(ReflectionTestUtils.invokeMethod(
                updater,
                "parseFutbol24FullTimeScore",
                "<div>AET 1-4, p.3-4</div>"));
    }

    @Test
    void shouldParseChampionsLeagueNinetyMinuteScoreAfterExtraTime() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "8": { "name": "AET", "name_short": "AET", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 8,
                  "status_id": 8,
                  "date": "2026-07-14T15:00:00+00:00",
                  "slug": "2026/07/14/international/UEFA/Champions-League/2026-2027/Qualifying-Round-1/KuPS/vs/Vardar-Skopje",
                  "score1": "2-3",
                  "score2": "0-0, 0-2",
                  "team1": { "name": "KuPS Kuopio" },
                  "team2": { "name": "Vardar Skopje" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3332464",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(Competition.CHAMPIONS_LEAGUE, schedule.getCompetition());
        assertEquals(LocalDate.of(2026, 7, 14), schedule.getMatchDate());
        assertEquals("欧冠", schedule.getGroupName());
        assertEquals("库奥皮奥", schedule.getHomeTeamCn());
        assertEquals("瓦尔达尔", schedule.getAwayTeamCn());
        assertEquals(0, schedule.getHomeScore());
        assertEquals(2, schedule.getAwayScore());
    }

    @Test
    void shouldUseUtcPlusEightDateForEuropeanLateMatch() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 8,
                  "status_id": 5,
                  "date": "2026-07-15T17:30:00+00:00",
                  "slug": "2026/07/15/international/UEFA/Champions-League/2026-2027/Qualifying-Round-1/U-Craiova/vs/FK-Maxline",
                  "score1": "1-0",
                  "team1": { "name": "Universitatea Craiova" },
                  "team2": { "name": "FK Maxline Vitebsk" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3332468",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(LocalDate.of(2026, 7, 16), schedule.getMatchDate());
        assertEquals(LocalTime.of(1, 30), schedule.getKickoffTime());
        assertEquals("克拉约瓦", schedule.getHomeTeamCn());
        assertEquals("ML Vitebsk", schedule.getAwayTeamCn());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureFutbol24ChampionsLeagueAndRomanianSources() {
        List<ClubCompetitionScheduleUpdater.Futbol24LeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.Futbol24LeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FUTBOL24_SOURCES");

        assertNotNull(sources);
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CHAMPIONS_LEAGUE
                        && "8".equals(source.leagueId())
                        && "欧冠".equals(source.sourceCompetition())));
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.EUROPA_LEAGUE
                        && "9".equals(source.leagueId())
                        && "欧罗巴".equals(source.sourceCompetition())));
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "48".equals(source.leagueId())
                        && "罗甲".equals(source.sourceCompetition())));
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "286".equals(source.leagueId())
                        && "罗超杯".equals(source.sourceCompetition())));
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "338".equals(source.leagueId())
                        && "葡超杯".equals(source.sourceCompetition())));
    }

    @Test
    void shouldParseCurrentEuropaLeagueResultFromFutbol24Fallback() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 9,
                  "status_id": 5,
                  "date": "2026-08-06T15:00:00+00:00",
                  "slug": "2026/08/06/international/UEFA/Europa-League/2026-2027/Qualifying-Round-3-Champ-Path/KuPS/vs/U-Craiova",
                  "score1": "1-1",
                  "team1": { "name": "KuPS Kuopio" },
                  "team2": { "name": "Universitatea Craiova" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3380025",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(Competition.EUROPA_LEAGUE, schedule.getCompetition());
        assertEquals(LocalDate.of(2026, 8, 6), schedule.getMatchDate());
        assertEquals("欧罗巴", schedule.getGroupName());
        assertEquals("库奥皮奥", schedule.getHomeTeamCn());
        assertEquals("克拉约瓦", schedule.getAwayTeamCn());
        assertEquals(1, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
    }

    @Test
    void shouldUseShanghaiDateForPortugueseSuperCup() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 338,
                  "status_id": 5,
                  "date": "2026-08-01T19:15:00+00:00",
                  "slug": "2026/08/01/national/Portugal/Super-Cup/2026/Final/FC-Porto/vs/Torreense",
                  "score1": "1-0",
                  "team1": { "name": "FC Porto" },
                  "team2": { "name": "União Torreense" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3364604",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(LocalDate.of(2026, 8, 2), schedule.getMatchDate());
        assertEquals(LocalTime.of(3, 15), schedule.getKickoffTime());
        assertEquals("葡超杯", schedule.getGroupName());
        assertEquals(1, schedule.getHomeScore());
        assertEquals(0, schedule.getAwayScore());
    }

    @Test
    void shouldUseShanghaiDateAndRegulationScoreForGreekSuperCup() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "5803515",
                  "round": "final",
                  "home": { "name": "AEK Athens" },
                  "away": { "name": "OFI Crete" },
                  "status": {
                    "utcTime": "2026-08-12T17:00:00Z",
                    "finished": true,
                    "started": true,
                    "cancelled": false,
                    "scoreStr": "2 - 2",
                    "reason": { "short": "Pen" }
                  }
                }
                """);
        JsonNode matchDetails = objectMapper.readTree("""
                {
                  "content": {
                    "matchFacts": {
                      "events": {
                        "events": [
                          {
                            "type": "Half",
                            "time": 90,
                            "halfStrShort": "FT",
                            "homeScore": 2,
                            "awayScore": 2
                          }
                        ]
                      }
                    }
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "8816",
                        "希腊超杯",
                        false),
                ZoneId.of("Asia/Shanghai"),
                matchDetails);

        assertNotNull(schedule);
        assertEquals("FOTMOB-CLUB_OFFICIAL_OTHER-5803515", schedule.getMatchId());
        assertEquals(LocalDate.of(2026, 8, 13), schedule.getMatchDate());
        assertEquals(LocalTime.of(1, 0), schedule.getKickoffTime());
        assertEquals("希腊超杯 第final轮", schedule.getGroupName());
        assertEquals("雅典AEK", schedule.getHomeTeamCn());
        assertEquals("OFI", schedule.getAwayTeamCn());
        assertEquals(2, schedule.getHomeScore());
        assertEquals(2, schedule.getAwayScore());
    }

    @Test
    void shouldRejectRequestedDuplicateFutbol24ClubFriendly() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 472,
                  "status_id": 5,
                  "date": "2026-07-26T10:00:00+00:00",
                  "score1": "2-1",
                  "team1": { "name": "FC Porto" },
                  "team2": { "name": "São João de Ver" }
                }
                """);

        assertNull(updater.parseFutbol24Match(
                "3384993",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void shouldExposeVerifiedPrivateFriendliesToRuntimeCards() {
        List<MatchSchedule> schedules = updater.verifiedSupplementalSchedules();

        assertEquals(10, schedules.size());
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2025, 1, 30))
                        && schedule.getCompetition() == Competition.CLUB_FRIENDLY
                        && "天狼星".equals(schedule.getHomeTeamCn())
                        && "布鲁马波".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(2).equals(schedule.getHomeScore())
                        && Integer.valueOf(1).equals(schedule.getAwayScore())));
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2024, 1, 12))
                        && schedule.getCompetition() == Competition.CLUB_OFFICIAL_OTHER
                        && "葡萄牙杯".equals(schedule.getGroupName())
                        && "圣克拉拉".equals(schedule.getHomeTeamCn())
                        && "葡国民".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(0).equals(schedule.getHomeScore())
                        && Integer.valueOf(0).equals(schedule.getAwayScore())));
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2026, 7, 11))
                        && "乌德勒支".equals(schedule.getHomeTeamCn())
                        && "比肖特VA".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(2).equals(schedule.getHomeScore())
                        && Integer.valueOf(0).equals(schedule.getAwayScore())
                        && "COMPLETED".equals(schedule.getStatus())));
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2026, 8, 1))
                        && "格罗宁根".equals(schedule.getHomeTeamCn())
                        && "阿尔梅勒".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(1).equals(schedule.getHomeScore())
                        && Integer.valueOf(3).equals(schedule.getAwayScore())
                        && "COMPLETED".equals(schedule.getStatus())));
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2026, 7, 22))
                        && "吉马良斯".equals(schedule.getHomeTeamCn())
                        && "莱里亚".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(0).equals(schedule.getHomeScore())
                        && Integer.valueOf(0).equals(schedule.getAwayScore())));
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2018, 7, 15))
                        && LocalTime.MIDNIGHT.equals(schedule.getKickoffTime())
                        && "埃斯托里".equals(schedule.getHomeTeamCn())
                        && "葡国民".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(2).equals(schedule.getHomeScore())
                        && Integer.valueOf(1).equals(schedule.getAwayScore())));
        assertTrue(schedules.stream().anyMatch(schedule ->
                schedule.getMatchDate().equals(LocalDate.of(2026, 8, 9))
                        && "卡斯特隆".equals(schedule.getHomeTeamCn())
                        && "莱万特".equals(schedule.getAwayTeamCn())
                        && Integer.valueOf(1).equals(schedule.getHomeScore())
                        && Integer.valueOf(3).equals(schedule.getAwayScore())));
    }

    @Test
    void shouldKeepUnmappedTeamsFromCompleteAzerbaijanCupSource() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 525,
                  "status_id": 5,
                  "date": "2026-07-20T16:00:00+00:00",
                  "score1": "1-0",
                  "team1": { "name": "New Cup Home" },
                  "team2": { "name": "New Cup Away" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "9990002",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals("阿塞杯", schedule.getGroupName());
        assertEquals("New Cup Home", schedule.getHomeTeamCn());
        assertEquals("New Cup Away", schedule.getAwayTeamCn());
    }

    @Test
    void shouldParseFinnishVeikkausliigaMatch() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 322,
                  "status_id": 5,
                  "date": "2026-07-18T14:00:00+00:00",
                  "score1": "0-2",
                  "team1": { "name": "Seinajoen JK" },
                  "team2": { "name": "KuPS Kuopio" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3312016",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(Competition.FINNISH_VEIKKAUSLIIGA, schedule.getCompetition());
        assertEquals(LocalDate.of(2026, 7, 18), schedule.getMatchDate());
        assertEquals("芬超", schedule.getGroupName());
        assertEquals("塞伊奈", schedule.getHomeTeamCn());
        assertEquals("库奥皮奥", schedule.getAwayTeamCn());
        assertEquals(0, schedule.getHomeScore());
        assertEquals(2, schedule.getAwayScore());
    }

    @Test
    void shouldParseUpcomingFinnishVeikkausliigaMatch() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "1": { "name": "Not started", "name_short": "NS", "is_ended": false }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 322,
                  "status_id": 1,
                  "date": "2026-07-24T16:00:00+00:00",
                  "team1": { "name": "FF Jaro" },
                  "team2": { "name": "Seinajoen JK" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3311966",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(Competition.FINNISH_VEIKKAUSLIIGA, schedule.getCompetition());
        assertEquals(LocalDate.of(2026, 7, 25), schedule.getMatchDate());
        assertEquals(LocalTime.MIDNIGHT, schedule.getKickoffTime());
        assertEquals("FF Jaro", schedule.getHomeTeamCn());
        assertEquals("塞伊奈", schedule.getAwayTeamCn());
        assertEquals("SCHEDULED", schedule.getStatus());
        assertNull(schedule.getHomeScore());
        assertNull(schedule.getAwayScore());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureFotMobFinnishVeikkausliigaSource() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FOTMOB_SOURCES");

        assertNotNull(sources);
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.FINNISH_VEIKKAUSLIIGA
                        && "51".equals(source.leagueId())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureRequestedHistoricalAndClickableUpdateSources() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FOTMOB_SOURCES");

        assertNotNull(sources);
        assertTrue(sources.stream().anyMatch(source ->
                "251".equals(source.leagueId())
                        && "芬甲".equals(source.sourceCompetition())
                        && source.calendarYearSeason()));
        assertTrue(sources.stream().anyMatch(source ->
                "235".equals(source.leagueId())
                        && "荷兰杯".equals(source.sourceCompetition())
                        && !source.calendarYearSeason()));
        assertTrue(sources.stream().anyMatch(source ->
                "140".equals(source.leagueId())
                        && "西乙".equals(source.sourceCompetition())
                        && !source.calendarYearSeason()));
        assertTrue(sources.stream().anyMatch(source ->
                "87".equals(source.leagueId())
                        && source.competition() == Competition.LA_LIGA));
        assertTrue(sources.stream().anyMatch(source ->
                "53".equals(source.leagueId())
                        && source.competition() == Competition.LIGUE_1
                        && "法甲".equals(source.sourceCompetition())));
        assertTrue(sources.stream().anyMatch(source ->
                "8816".equals(source.leagueId())
                        && source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "希腊超杯".equals(source.sourceCompetition())));
        assertTrue(sources.stream().anyMatch(source ->
                "137".equals(source.leagueId())
                        && source.competition() == Competition.SCOTTISH_FA_CUP
                        && "苏足总杯".equals(source.sourceCompetition())
                        && !source.calendarYearSeason()));
        assertTrue(sources.stream().anyMatch(source ->
                "9551".equals(source.leagueId())
                        && source.competition() == Competition.K_LEAGUE_1
                        && "韩国杯".equals(source.sourceCompetition())
                        && source.calendarYearSeason()));
        assertEquals("2014", sources.stream()
                .filter(source -> "251".equals(source.leagueId()))
                .findFirst()
                .orElseThrow()
                .seasonValue(2014));
        assertEquals("2022%2F2023", sources.stream()
                .filter(source -> "140".equals(source.leagueId()))
                .findFirst()
                .orElseThrow()
                .seasonValue(2022));
        assertEquals("2025%2F2026", sources.stream()
                .filter(source -> "8816".equals(source.leagueId()))
                .findFirst()
                .orElseThrow()
                .seasonValue(2025));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureFotMobPrimeiraLigaFallbackSource() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FOTMOB_SOURCES");

        assertNotNull(sources);
        ClubCompetitionScheduleUpdater.FotMobLeagueSource source = sources.stream()
                .filter(item -> item.competition() == Competition.PRIMEIRA_LIGA)
                .findFirst()
                .orElseThrow();
        assertEquals("61", source.leagueId());
        assertEquals("葡超", source.sourceCompetition());
        assertEquals("2026%2F2027", source.seasonValue(2026));
    }

    @Test
    void shouldReplaceStaleEspnPrimeiraLigaScheduleWithCompletedFotMobResult() throws Exception {
        MatchSchedule staleEspnSchedule = new MatchSchedule();
        staleEspnSchedule.setCompetition(Competition.PRIMEIRA_LIGA);
        staleEspnSchedule.setMatchId("ESPN-PRIMEIRA_LIGA-401885491");
        staleEspnSchedule.setMatchDate(LocalDate.of(2026, 8, 8));
        staleEspnSchedule.setKickoffTime(LocalTime.of(3, 15));
        staleEspnSchedule.setGroupName("葡超");
        staleEspnSchedule.setHomeTeamCn("埃斯托里");
        staleEspnSchedule.setAwayTeamCn("法马利康");
        staleEspnSchedule.setHomeTeamEn("Estoril");
        staleEspnSchedule.setAwayTeamEn("FC Famalicao");
        staleEspnSchedule.setStatus("SCHEDULED");

        JsonNode match = objectMapper.readTree("""
                {
                  "id": "5887559",
                  "round": "1",
                  "home": { "name": "Estoril", "id": "7842" },
                  "away": { "name": "Famalicao", "id": "1634" },
                  "status": {
                    "utcTime": "2026-08-07T19:15:00Z",
                    "finished": true,
                    "started": true,
                    "cancelled": false,
                    "scoreStr": "1 - 1"
                  }
                }
                """);
        MatchSchedule fotMobSchedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.PRIMEIRA_LIGA,
                        "61",
                        "葡超",
                        false),
                ZoneId.of("Asia/Shanghai"));

        List<MatchSchedule> schedules = updater.removeReplacedCachedSchedules(
                List.of(staleEspnSchedule),
                Set.of(new ClubCompetitionScheduleUpdater.CompetitionSeason(
                        Competition.PRIMEIRA_LIGA,
                        2026,
                        "葡超")),
                LocalDate.of(2026, 7, 9),
                LocalDate.of(2026, 8, 15));
        schedules.add(fotMobSchedule);
        schedules = updater.deduplicateSchedulesByFixture(schedules);

        assertEquals(1, schedules.size());
        MatchSchedule refreshedSchedule = schedules.get(0);
        assertEquals("FOTMOB-PRIMEIRA_LIGA-5887559", refreshedSchedule.getMatchId());
        assertEquals(LocalDate.of(2026, 8, 8), refreshedSchedule.getMatchDate());
        assertEquals(LocalTime.of(3, 15), refreshedSchedule.getKickoffTime());
        assertEquals("COMPLETED", refreshedSchedule.getStatus());
        assertEquals(1, refreshedSchedule.getHomeScore());
        assertEquals(1, refreshedSchedule.getAwayScore());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureFotMobFinnishLeagueCupAndSwedishCupSources() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FOTMOB_SOURCES");

        assertNotNull(sources);
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "342".equals(source.leagueId())
                        && "联赛杯".equals(source.sourceCompetition())
                        && source.calendarYearSeason()));
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "171".equals(source.leagueId())
                        && "瑞典杯".equals(source.sourceCompetition())
                        && !source.calendarYearSeason()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureKLeagueQualificationAsPlayOffsSource() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FOTMOB_SOURCES");

        assertNotNull(sources);
        assertTrue(sources.stream().anyMatch(source ->
                source.competition() == Competition.CLUB_OFFICIAL_OTHER
                        && "9422".equals(source.leagueId())
                        && "Play-offs 1/2".equals(source.sourceCompetition())
                        && source.calendarYearSeason()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotConfigureRemovedBrazilianCompetitionSources() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>)
                        ReflectionTestUtils.getField(
                                ClubCompetitionScheduleUpdater.class,
                                "FOTMOB_SOURCES");

        assertNotNull(sources);
        assertFalse(sources.stream().anyMatch(source ->
                "8814".equals(source.leagueId())
                        || "巴乙".equals(source.sourceCompetition())
                        || "圣保罗锦".equals(source.sourceCompetition())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRemoveExcludedBrazilianCompetitionsFromScheduleCache(@TempDir Path tempDir)
            throws Exception {
        Path cachePath = tempDir.resolve("club-competition-schedules.json");
        MatchSchedule brazilSerieB = completedSchedule(
                "FOTMOB-CLUB_OFFICIAL_OTHER-5190620",
                "Novorizontino",
                "克里西");
        brazilSerieB.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        brazilSerieB.setGroupName("巴乙 第19轮");
        MatchSchedule swissSuperLeague = completedSchedule(
                "FOTMOB-CLUB_OFFICIAL_OTHER-5000000",
                "巴塞尔",
                "苏黎世");
        swissSuperLeague.setCompetition(Competition.CLUB_OFFICIAL_OTHER);
        swissSuperLeague.setGroupName("瑞士超 第1轮");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                cachePath.toFile(),
                List.of(brazilSerieB, swissSuperLeague));
        ReflectionTestUtils.setField(updater, "cachePath", cachePath.toString());

        List<MatchSchedule> loadedSchedules =
                (List<MatchSchedule>) ReflectionTestUtils.invokeMethod(
                        updater,
                        "loadCachedSchedules");

        assertNotNull(loadedSchedules);
        assertEquals(1, loadedSchedules.size());
        assertEquals("瑞士超 第1轮", loadedSchedules.get(0).getGroupName());

        ReflectionTestUtils.invokeMethod(
                updater,
                "saveCachedSchedules",
                List.of(brazilSerieB, swissSuperLeague));
        MatchSchedule[] savedSchedules =
                objectMapper.readValue(cachePath.toFile(), MatchSchedule[].class);

        assertEquals(1, savedSchedules.length);
        assertEquals("瑞士超 第1轮", savedSchedules[0].getGroupName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCorrectVerifiedRegulationScoreWhenLoadingScheduleCache(@TempDir Path tempDir)
            throws Exception {
        Path cachePath = tempDir.resolve("club-competition-schedules.json");
        MatchSchedule cachedSchedule = completedClubFriendlySchedule(
                "FUTBOL24-CLUB_FRIENDLY-3383418",
                "圣吉联合",
                "Patro Eisden",
                4,
                1);
        cachedSchedule.setMatchDate(LocalDate.of(2026, 7, 24));
        cachedSchedule.setGroupName("俱乐部友谊赛");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                cachePath.toFile(),
                List.of(cachedSchedule));
        ReflectionTestUtils.setField(updater, "cachePath", cachePath.toString());

        List<MatchSchedule> loadedSchedules =
                (List<MatchSchedule>) ReflectionTestUtils.invokeMethod(
                        updater,
                        "loadCachedSchedules");

        assertNotNull(loadedSchedules);
        assertEquals(1, loadedSchedules.size());
        assertEquals(4, loadedSchedules.get(0).getHomeScore());
        assertEquals(0, loadedSchedules.get(0).getAwayScore());
    }

    @Test
    void shouldParseRequestedEuropeanDomesticCompetitions() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        List<List<String>> sources = List.of(
                List.of("322", "芬超"),
                List.of("324", "芬兰杯"),
                List.of("28", "丹超"),
                List.of("297", "波超杯"),
                List.of("107", "波甲"),
                List.of("15", "奥甲"),
                List.of("17", "奥地利杯"),
                List.of("51", "苏超"),
                List.of("133", "土超"),
                List.of("537", "土耳其杯"),
                List.of("33", "丹麦杯"),
                List.of("92", "匈甲"),
                List.of("531", "匈牙利杯"),
                List.of("26", "克甲"),
                List.of("70", "威联杯"),
                List.of("534", "塞杯"),
                List.of("868", "卢森杯"),
                List.of("291", "法罗杯"),
                List.of("60", "斯洛文甲"),
                List.of("310", "亚美尼超"));

        for (List<String> source : sources) {
            JsonNode match = objectMapper.readTree("""
                    {
                      "league_id": %s,
                      "status_id": 5,
                      "date": "2026-07-18T18:30:00+00:00",
                      "score1": "2-1",
                      "team1": { "name": "Home Team" },
                      "team2": { "name": "Away Team" }
                    }
                    """.formatted(source.get(0)));

            MatchSchedule schedule = updater.parseFutbol24Match(
                    "TEST-" + source.get(0),
                    match,
                    statuses,
                    ZoneId.of("Asia/Shanghai"));

            assertNotNull(schedule, source.get(1));
            assertEquals(LocalDate.of(2026, 7, 19), schedule.getMatchDate());
            assertEquals(LocalTime.of(2, 30), schedule.getKickoffTime());
            assertEquals(source.get(1), schedule.getGroupName());
            assertEquals(2, schedule.getHomeScore());
            assertEquals(1, schedule.getAwayScore());
        }
    }

    @Test
    void shouldParseRequestedFotMobCompetitionsWithoutReversingHomeAndAway() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": 900001,
                  "round": "3",
                  "home": { "longName": "Home Team" },
                  "away": { "longName": "Away Team" },
                  "status": {
                    "utcTime": "2026-07-18T14:00:00Z",
                    "finished": true,
                    "cancelled": false,
                    "scoreStr": "2 - 1"
                  }
                }
                """);
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources = List.of(
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "342", "联赛杯", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "171", "瑞典杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.K_LEAGUE_1, "9080", "韩职", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.SWEDISH_ALLSVENSKAN, "67", "瑞超", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.EREDIVISIE, "57", "荷甲", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "111", "荷乙", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.PRIMEIRA_LIGA, "61", "葡超", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "185", "葡甲", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "187", "葡联赛杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "58", "荷乙附加赛", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "188", "葡超杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "10216", "欧协联", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "10615", "欧协联资格赛", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "135", "希超", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "122", "捷甲", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "40", "比甲", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "149", "比利时杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "266", "比超杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "164", "瑞士杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "69", "瑞士超", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "271", "保杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "270", "保超", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "278", "奥地利杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "126", "爱超", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "182", "塞超", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "183", "塞杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "176", "斯洛伐超", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "177", "斯洛伐杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "229", "卢森联", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "9527", "卢森杯", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "250", "法罗超", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "9523", "法罗杯", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "232", "黑山甲", false),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "59", "挪超", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "215", "冰超", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "217", "冰岛杯", true),
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER, "116", "威尔士超", false));

        for (ClubCompetitionScheduleUpdater.FotMobLeagueSource source : sources) {
            MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                    match,
                    source,
                    ZoneId.of("Asia/Shanghai"));

            assertNotNull(schedule, source.sourceCompetition());
            assertTrue(schedule.getGroupName().startsWith(source.sourceCompetition()));
            assertEquals("Home Team", schedule.getHomeTeamCn());
            assertEquals("Away Team", schedule.getAwayTeamCn());
            assertEquals(2, schedule.getHomeScore());
            assertEquals(1, schedule.getAwayScore());
        }
    }

    @Test
    void shouldRejectRequestedAbnormalAndDuplicateFotMobClubFriendlies() throws Exception {
        for (String matchId : List.of("5838416", "5838413", "5900532", "5900828", "5961030")) {
            JsonNode match = objectMapper.readTree("""
                    {
                      "id": "%s",
                      "home": { "longName": "Abnormal Home" },
                      "away": { "longName": "Abnormal Away" },
                      "status": {
                        "utcTime": "2026-06-27T14:00:00Z",
                        "finished": true,
                        "cancelled": false,
                        "scoreStr": "0 - 23"
                      }
                    }
                    """.formatted(matchId));

            MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                    match,
                    new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                            Competition.CLUB_FRIENDLY,
                            "489",
                            "俱乐部赛",
                            true),
                    ZoneId.of("Asia/Shanghai"));

            assertNull(schedule, matchId);
        }
    }

    @Test
    void shouldKeepPreRenameLitexMatchesSeparateFromCskaSofia() throws Exception {
        JsonNode match = objectMapper.readTree("""
                {
                  "id": "1940759",
                  "home": {"id": "10127", "longName": "CSKA Sofia"},
                  "away": {"id": "10128", "longName": "PFC Lokomotiv Sofia 1929"},
                  "status": {
                    "utcTime": "2015-04-20T17:00:00.000Z",
                    "finished": true,
                    "scoreStr": "0 - 1"
                  }
                }
                """);

        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                match,
                new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                        Competition.CLUB_OFFICIAL_OTHER,
                        "270",
                        "保超",
                        false),
                ZoneId.of("Asia/Shanghai"));

        assertEquals("利特克斯", schedule.getHomeTeamCn());
        assertEquals("Litex Lovech", schedule.getHomeTeamEn());
        assertEquals("Lokomotiv Sf", schedule.getAwayTeamCn());
    }

    @Test
    void shouldParseCypriotFirstDivisionMatchWithMappedTeamNames() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 75,
                  "status_id": 5,
                  "date": "2026-05-22T14:30:00+00:00",
                  "score1": "2-0",
                  "team1": { "name": "Pafos FC" },
                  "team2": { "name": "APOEL FC" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3311348",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(LocalDate.of(2026, 5, 22), schedule.getMatchDate());
        assertEquals(LocalTime.of(22, 30), schedule.getKickoffTime());
        assertEquals("塞浦甲", schedule.getGroupName());
        assertEquals("帕福斯", schedule.getHomeTeamCn());
        assertEquals("希腊人", schedule.getAwayTeamCn());
        assertEquals(2, schedule.getHomeScore());
        assertEquals(0, schedule.getAwayScore());
    }

    @Test
    void shouldParseKazakhstanPremierLeagueMatchWithMappedTeamNames() throws Exception {
        JsonNode statuses = objectMapper.readTree("""
                {
                  "5": { "name": "FT", "name_short": "FT", "is_ended": true }
                }
                """);
        JsonNode match = objectMapper.readTree("""
                {
                  "league_id": 269,
                  "status_id": 5,
                  "date": "2026-07-20T13:00:00+00:00",
                  "score1": "3-1",
                  "team1": { "name": "FK Ordabasy" },
                  "team2": { "name": "FK Yelimay Semey" }
                }
                """);

        MatchSchedule schedule = updater.parseFutbol24Match(
                "3361470",
                match,
                statuses,
                ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(LocalDate.of(2026, 7, 20), schedule.getMatchDate());
        assertEquals(LocalTime.of(21, 0), schedule.getKickoffTime());
        assertEquals("哈萨超", schedule.getGroupName());
        assertEquals("奥达巴斯", schedule.getHomeTeamCn());
        assertEquals("叶利迈塞米", schedule.getAwayTeamCn());
        assertEquals(3, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
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

    private MatchSchedule completedClubFriendlySchedule(
            String matchId,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore) {
        MatchSchedule schedule = completedSchedule(matchId, homeTeam, awayTeam);
        schedule.setCompetition(Competition.CLUB_FRIENDLY);
        schedule.setMatchDate(LocalDate.of(2026, 7, 9));
        schedule.setHomeScore(homeScore);
        schedule.setAwayScore(awayScore);
        return schedule;
    }

}
