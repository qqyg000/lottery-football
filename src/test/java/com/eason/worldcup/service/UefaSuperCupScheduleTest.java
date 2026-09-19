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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UefaSuperCupScheduleTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final ClubCompetitionScheduleUpdater updater = new ClubCompetitionScheduleUpdater(objectMapper);

    @Test
    void shouldImportParisVillaWithShanghaiKickoffAndCanonicalTeamNames() throws Exception {
        MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                fixture("2 - 1", "FT"), source(), ZoneId.of("Asia/Shanghai"));

        assertNotNull(schedule);
        assertEquals(Competition.CLUB_OFFICIAL_OTHER, schedule.getCompetition());
        assertEquals(LocalDate.of(2026, 8, 13), schedule.getMatchDate());
        assertEquals(LocalTime.of(3, 0), schedule.getKickoffTime());
        assertEquals("巴黎圣曼", schedule.getHomeTeamCn());
        assertEquals("维拉", schedule.getAwayTeamCn());
        assertEquals(2, schedule.getHomeScore());
        assertEquals(1, schedule.getAwayScore());
        assertEquals("COMPLETED", schedule.getStatus());
        assertTrue(schedule.getGroupName().startsWith("欧超杯"));
        assertTrue(schedule.isNeutral());
    }

    @Test
    void shouldUseRegulationScoreForExtraTimeAndPenaltyFinals() throws Exception {
        JsonNode details = objectMapper.readTree("""
                {"content":{"matchFacts":{"events":{"events":[
                  {"type":"Half","time":90,"halfStrShort":"FT","homeScore":2,"awayScore":2}
                ]}}}}
                """);

        for (String reason : List.of("AET", "Penalties")) {
            MatchSchedule schedule = updater.parseFotMobLeagueMatch(
                    fixture("3 - 2", reason), source(), ZoneId.of("Asia/Shanghai"), details);
            assertNotNull(schedule);
            assertEquals(2, schedule.getHomeScore());
            assertEquals(2, schedule.getAwayScore());

            MatchSchedule withoutDetails = updater.parseFotMobLeagueMatch(
                    fixture("3 - 2", reason), source(), ZoneId.of("Asia/Shanghai"));
            assertNull(withoutDetails.getHomeScore());
            assertNull(withoutDetails.getAwayScore());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldIncludeSuperCupInClickableHistoricalRefresh() {
        List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>) ReflectionTestUtils.getField(
                        ClubCompetitionScheduleUpdater.class, "FOTMOB_SOURCES");
        Set<String> historicalLeagueIds = (Set<String>) ReflectionTestUtils.getField(
                ClubCompetitionScheduleUpdater.class, "HISTORICAL_FOTMOB_LEAGUE_IDS");

        assertNotNull(sources);
        assertTrue(sources.contains(source()));
        assertNotNull(historicalLeagueIds);
        assertTrue(historicalLeagueIds.contains("74"));
        assertEquals("2025%2F2026", source().seasonValue(2025));
    }

    @Test
    void shouldDeduplicateSuperCupAliasesAcrossProviders() throws Exception {
        MatchSchedule fotMob = updater.parseFotMobLeagueMatch(
                fixture("2 - 1", "FT"), source(), ZoneId.of("Asia/Shanghai"));
        for (String alias : List.of("欧洲超级杯", "欧足联超级杯", "UEFA Super Cup")) {
            MatchSchedule espn = updater.parseFotMobLeagueMatch(
                    fixture("2 - 1", "FT"), source(), ZoneId.of("Asia/Shanghai"));
            espn.setMatchId("ESPN-SUPER-CUP");
            espn.setGroupName(alias + " Final");

            assertEquals(1, updater.deduplicateSchedulesByFixture(List.of(espn, fotMob)).size());
        }
    }

    private ClubCompetitionScheduleUpdater.FotMobLeagueSource source() {
        return new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                Competition.CLUB_OFFICIAL_OTHER, "74", "欧超杯", false);
    }

    private JsonNode fixture(String score, String reason) throws Exception {
        return objectMapper.readTree("""
                {
                  "id":"5729447",
                  "round":"final",
                  "home":{"name":"Paris Saint-Germain"},
                  "away":{"name":"Aston Villa"},
                  "status":{
                    "utcTime":"2026-08-12T19:00:00Z",
                    "finished":true,
                    "scoreStr":"%s",
                    "reason":{"short":"%s"}
                  }
                }
                """.formatted(score, reason));
    }

}
