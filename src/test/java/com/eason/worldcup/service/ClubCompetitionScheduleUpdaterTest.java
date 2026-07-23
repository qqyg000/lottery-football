package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClubCompetitionScheduleUpdaterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ClubCompetitionScheduleUpdater updater = new ClubCompetitionScheduleUpdater(objectMapper);

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
    void shouldUseThirtyDayRefreshWindow() {
        ReflectionTestUtils.setField(updater, "daysBack", 30);

        assertEquals(
                LocalDate.of(2026, 6, 22),
                updater.resolveRefreshStartDate(LocalDate.of(2026, 7, 22)));
    }

    @Test
    void shouldDeduplicateMappedTeamsAcrossProviders() {
        MatchSchedule espn = completedSchedule(
                "ESPN-BRAZIL_SERIE_A-401841108",
                "米内罗竞技",
                "Botafogo");
        MatchSchedule fotMob = completedSchedule(
                "FOTMOB-BRAZIL_SERIE_A-5103509",
                "米竞技",
                "博塔弗戈");

        List<MatchSchedule> schedules = updater.deduplicateSchedulesByFixture(List.of(espn, fotMob));

        assertEquals(1, schedules.size());
        assertEquals("米内罗竞技", schedules.get(0).getHomeTeamCn());
        assertEquals("博塔弗戈", schedules.get(0).getAwayTeamCn());
    }

    @Test
    void shouldDeduplicateSameOfficialTeamDateAndScoreWithOpponentAlias() {
        MatchSchedule first = completedSchedule(
                "SOURCE-001",
                "巴伊亚",
                "Chapecoense Unknown");
        first.setHomeScore(2);
        first.setAwayScore(0);
        MatchSchedule second = completedSchedule(
                "SOURCE-002",
                "巴伊亚",
                "沙佩科未知别名");
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
        assertEquals("Admira Wacker", schedules.get(0).getHomeTeamCn());
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
        assertEquals("Turan Tovuz", schedule.getHomeTeamCn());
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
        assertEquals("Polissya Zhitomir", schedule.getHomeTeamCn());
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
        assertEquals("Polissya Zhitomir", schedule.getHomeTeamCn());
        assertEquals("萨巴赫", schedule.getAwayTeamCn());
        assertEquals(4, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
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
        assertEquals(LocalDate.of(2026, 7, 18), schedule.getMatchDate());
        assertEquals("芬超", schedule.getGroupName());
        assertEquals("塞伊奈约基", schedule.getHomeTeamCn());
        assertEquals("库奥皮奥", schedule.getAwayTeamCn());
        assertEquals(0, schedule.getHomeScore());
        assertEquals(2, schedule.getAwayScore());
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
                List.of("51", "苏超"),
                List.of("133", "土超"),
                List.of("537", "土耳其杯"),
                List.of("33", "丹麦杯"),
                List.of("92", "匈甲"),
                List.of("531", "匈牙利杯"),
                List.of("26", "克甲"));

        for (List<String> source : sources) {
            JsonNode match = objectMapper.readTree("""
                    {
                      "league_id": %s,
                      "status_id": 5,
                      "date": "2026-07-18T14:00:00+00:00",
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
            assertEquals(source.get(1), schedule.getGroupName());
            assertEquals(2, schedule.getHomeScore());
            assertEquals(1, schedule.getAwayScore());
        }
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
