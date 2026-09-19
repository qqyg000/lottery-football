package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopaAndLigueTwoScheduleTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final ClubCompetitionScheduleUpdater updater = new ClubCompetitionScheduleUpdater(objectMapper);

    @Test
    void shouldNormalizeHistoricalAndProviderAliasesAcrossClubCompetitions() {
        Map<String, String> aliases = Map.of(
                "昂熱", "昂热",
                "Grenoble Foot 38", "格勒诺布",
                "Valenciennes FC", "瓦朗谢纳",
                "Niort", "尼奥尔",
                "Bourg en Bresse Peronnas", "布尔格",
                "Mirandés", "米兰德斯",
                "布尔戈斯", "博格斯");
        aliases.forEach((alias, standardName) -> {
            for (Competition competition : List.of(Competition.CLUB_OFFICIAL_OTHER,
                    Competition.CLUB_FRIENDLY, Competition.LIGUE_1, Competition.LA_LIGA)) {
                assertEquals(standardName, ClubTeamNameTranslator.translate(competition, alias),
                        competition + ":" + alias);
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldLoadRequestedSourcesThroughRefreshHttpPathWithShanghaiDates() throws Exception {
        List<String> requests = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/leagues", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            requests.add(query);
            String match = query.contains("id=138")
                    ? fixture("4397771", "2024-01-16T20:00:00Z", "Athletic Club", "Deportivo Alavés", "2 - 0")
                    : query.contains("2023%2F2024")
                            ? fixture("4219807", "2024-04-20T17:00:00Z", "Angers", "Troyes", "2 - 1")
                            : fixture("4832167", "2026-05-09T18:00:00Z", "Grenoble", "Troyes", "1 - 0");
            byte[] body = ("{\"fixtures\":{\"allMatches\":[" + match + "]}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        ReflectionTestUtils.setField(updater, "fotMobLeagueUrlTemplate",
                "http://127.0.0.1:" + server.getAddress().getPort() + "/leagues?id={leagueId}&season={season}");
        try {
            List<ClubCompetitionScheduleUpdater.FotMobLeagueSource> sources =
                    (List<ClubCompetitionScheduleUpdater.FotMobLeagueSource>) ReflectionTestUtils.getField(
                            ClubCompetitionScheduleUpdater.class, "FOTMOB_SOURCES");
            Set<String> historicalLeagueIds = (Set<String>) ReflectionTestUtils.getField(
                    ClubCompetitionScheduleUpdater.class, "HISTORICAL_FOTMOB_LEAGUE_IDS");
            assertNotNull(sources);
            assertNotNull(historicalLeagueIds);
            assertTrue(historicalLeagueIds.containsAll(Set.of("110", "138")));
            var cup = sources.stream().filter(source -> "138".equals(source.leagueId())).findFirst().orElseThrow();
            var league = sources.stream().filter(source -> "110".equals(source.leagueId())).findFirst().orElseThrow();

            assertFixture(loadSeason(cup, 2023), "2024-01-17", "04:00", "西国王杯", "毕尔巴鄂", "阿拉维斯", 2, 0);
            assertFixture(loadSeason(league, 2023), "2024-04-21", "01:00", "法乙", "昂热", "特鲁瓦", 2, 1);
            assertFixture(loadSeason(league, 2025), "2026-05-10", "02:00", "法乙", "格勒诺布", "特鲁瓦", 1, 0);
            assertEquals(List.of("id=138&season=2023%2F2024", "id=110&season=2023%2F2024",
                    "id=110&season=2025%2F2026"), requests);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRetainVerifiedAngersTroyesFriendlyDuringRefresh() {
        List<MatchSchedule> matches = updater.verifiedSupplementalSchedules().stream()
                .filter(schedule -> schedule.getMatchDate().equals(LocalDate.of(2021, 7, 24)))
                .filter(schedule -> schedule.getHomeTeamCn().equals("昂热"))
                .toList();

        assertEquals(1, matches.size());
        MatchSchedule match = matches.get(0);
        assertEquals(Competition.CLUB_FRIENDLY, match.getCompetition());
        assertEquals("特鲁瓦", match.getAwayTeamCn());
        assertEquals(LocalTime.of(23, 0), match.getKickoffTime());
        assertEquals(3, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
        assertTrue(match.isNeutral());
    }

    @Test
    void shouldMergeCopaDelReyAliasesWithoutDuplicateFixtures() throws Exception {
        JsonNode json = objectMapper.readTree(
                fixture("4397771", "2024-01-16T20:00:00Z", "Athletic Club", "Deportivo Alavés", "2 - 0"));
        var source = new ClubCompetitionScheduleUpdater.FotMobLeagueSource(
                Competition.CLUB_OFFICIAL_OTHER, "138", "西国王杯", false);
        MatchSchedule fotMob = updater.parseFotMobLeagueMatch(json, source, ZoneId.of("Asia/Shanghai"));
        for (String alias : List.of("国王杯", "西班牙国王杯", "Copa del Rey")) {
            MatchSchedule legacy = updater.parseFotMobLeagueMatch(json, source, ZoneId.of("Asia/Shanghai"));
            legacy.setMatchId("LEGACY-CUP");
            legacy.setGroupName(alias);
            assertEquals(1, updater.deduplicateSchedulesByFixture(List.of(legacy, fotMob)).size());
        }
    }

    private MatchSchedule loadSeason(ClubCompetitionScheduleUpdater.FotMobLeagueSource source, int year) {
        List<MatchSchedule> matches = ReflectionTestUtils.invokeMethod(updater, "loadFotMobSeason",
                HttpClient.newHttpClient(), source, year, ZoneId.of("Asia/Shanghai"), Duration.ofSeconds(3),
                LocalDate.of(2014, 10, 22));
        assertNotNull(matches);
        assertEquals(1, matches.size());
        return matches.get(0);
    }

    private void assertFixture(MatchSchedule match, String date, String time, String competition,
            String home, String away, int homeScore, int awayScore) {
        assertEquals(LocalDate.parse(date), match.getMatchDate());
        assertEquals(LocalTime.parse(time), match.getKickoffTime());
        assertTrue(match.getGroupName().startsWith(competition));
        assertEquals(Competition.CLUB_OFFICIAL_OTHER, match.getCompetition());
        assertEquals(home, match.getHomeTeamCn());
        assertEquals(away, match.getAwayTeamCn());
        assertEquals(homeScore, match.getHomeScore());
        assertEquals(awayScore, match.getAwayScore());
    }

    private String fixture(String id, String utc, String home, String away, String score) {
        return """
                {"id":"%s","round":"1/8","home":{"name":"%s"},"away":{"name":"%s"},
                 "status":{"utcTime":"%s","finished":true,"scoreStr":"%s","reason":{"short":"FT"}}}
                """.formatted(id, home, away, utc, score);
    }

}
