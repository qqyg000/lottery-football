package com.eason.worldcup.util;

import com.eason.worldcup.model.Competition;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricalMatchDataIntegrityTest {

    @Test
    void shouldContainRequestedVerifiedAndDomesticCompetitionFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2024, 10, 4),
                Competition.EUROPA_LEAGUE,
                "圣吉联合",
                "博德闪耀",
                0,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 24),
                Competition.CLUB_FRIENDLY,
                "圣吉联合",
                "Patro Eisden",
                4,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 5, 16),
                Competition.CLUB_OFFICIAL_OTHER,
                "布拉迪斯",
                "泽姆匹林米哈洛夫采",
                0,
                2);
        long czech2026Count = fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("捷甲"))
                .filter(fixture -> fixture.matchDate().getYear() == 2026)
                .count();
        assertEquals(140L, czech2026Count, "2026 捷甲已完赛数据不完整");
        for (String sourceCompetition : List.of("希超", "捷甲", "比超杯", "挪超")) {
            long count = fixtures.stream()
                    .filter(fixture -> fixture.sourceCompetition().equals(sourceCompetition))
                    .count();
            assertTrue(count > 0, sourceCompetition);
        }
    }

    @Test
    void shouldContainRequestedChampionsLeagueFixturesAndRemoveShiftedDuplicates() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 14),
                Competition.CHAMPIONS_LEAGUE,
                "库奥皮奥",
                "瓦尔达尔",
                0,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 9),
                Competition.CHAMPIONS_LEAGUE,
                "ML Vitebsk",
                "克拉约瓦",
                1,
                4);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 16),
                Competition.CHAMPIONS_LEAGUE,
                "克拉约瓦",
                "ML Vitebsk",
                1,
                0);

        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 16),
                "阿尔克马",
                "安德莱",
                0,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 25),
                "本菲卡",
                "CF Os Belenenses",
                5,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 17),
                LocalDate.of(2026, 7, 18),
                "哈茨",
                "巴列卡诺",
                2,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 25),
                "拉茨",
                "哈茨",
                0,
                1);
    }

    @Test
    void shouldCoverRequestedDomesticCompetitionsAndClubFriendlies() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertSourceCompetitionCoverage(fixtures, "罗甲", 3_000L);
        assertSourceCompetitionCoverage(fixtures, "罗超杯", 10L);
        assertSourceCompetitionCoverage(fixtures, "波甲", 3_000L);
        for (String club : List.of("比亚韦", "安德莱", "塞萨洛", "本菲卡", "布拉加")) {
            List<HistoricalFixture> clubFriendlies = fixtures.stream()
                    .filter(fixture -> fixture.matchType().equals("CLUB_FRIENDLY"))
                    .filter(fixture -> fixture.homeTeam().equals(club)
                            || fixture.awayTeam().equals(club))
                    .toList();
            assertTrue(!clubFriendlies.isEmpty(), club + " 缺少俱乐部赛历史");
            assertTrue(clubFriendlies.stream().allMatch(fixture ->
                    !fixture.matchDate().isBefore(LocalDate.of(2014, 10, 22))), club);
            assertTrue(clubFriendlies.stream().anyMatch(fixture ->
                    !fixture.matchDate().isBefore(LocalDate.of(2026, 7, 1))), club);
        }
    }

    @Test
    void shouldContainCompleteEersteDivisiePrimeiraLigaAndRequestedClubFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2024, 10, 26),
                Competition.CLUB_OFFICIAL_OTHER,
                "SBV精英",
                "坎布尔",
                0,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 4, 25),
                Competition.CLUB_OFFICIAL_OTHER,
                "坎布尔",
                "维迪斯",
                2,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 4, 26),
                Competition.PRIMEIRA_LIGA,
                "埃斯托里",
                "法马利康",
                0,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 1),
                Competition.CLUB_FRIENDLY,
                "CF Os Belenenses",
                "埃斯托里",
                1,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 7, 28),
                "坎布尔",
                "Volos NFC",
                1,
                2);

        assertSourceCompetitionYearCoverage(fixtures, "荷乙", 2022, 350L);
        assertSourceCompetitionYearCoverage(fixtures, "荷乙", 2023, 380L);
        assertSourceCompetitionYearCoverage(fixtures, "荷乙", 2024, 370L);
        assertSourceCompetitionYearCoverage(fixtures, "葡超", 2022, 290L);
        assertSourceCompetitionYearCoverage(fixtures, "葡超", 2023, 300L);
        assertSourceCompetitionYearCoverage(fixtures, "葡超", 2024, 300L);
    }

    @Test
    void shouldContainCompleteLigaPortugalTwoLeagueCupPlayoffsAndRequestedFriendlies() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 5, 16),
                Competition.CLUB_OFFICIAL_OTHER,
                "马里迪莫",
                "沙维什",
                1,
                3);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 5, 21),
                Competition.CLUB_OFFICIAL_OTHER,
                "威廉二世",
                "福伦丹",
                1,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 26),
                Competition.CLUB_FRIENDLY,
                "马里迪莫",
                "马奇科",
                0,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 31),
                Competition.CLUB_FRIENDLY,
                "SV梅尔森",
                "福图纳",
                0,
                2);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 25),
                LocalDate.of(2026, 7, 24),
                "赫拉克勒",
                "SBV精英",
                3,
                5);

        assertSourceCompetitionCount(fixtures, "葡甲", 4_000L);
        assertSourceCompetitionCount(fixtures, "葡联赛杯", 400L);
        assertSourceCompetitionCount(fixtures, "荷乙附加赛", 140L);
        assertSourceCompetitionYearCoverage(fixtures, "葡甲", 2022, 290L);
        assertSourceCompetitionYearCoverage(fixtures, "葡甲", 2023, 310L);
        assertSourceCompetitionYearCoverage(fixtures, "葡甲", 2024, 300L);
        assertSourceCompetitionYearCoverage(fixtures, "葡联赛杯", 2022, 60L);
        assertSourceCompetitionYearCoverage(fixtures, "葡联赛杯", 2023, 35L);
        assertSourceCompetitionYearCoverage(fixtures, "荷乙附加赛", 2026, 12L);

        for (String excludedMatchId : List.of(
                "FOTMOB-5838416",
                "FOTMOB-5838413",
                "FOTMOB-5900828",
                "FOTMOB-5961030",
                "FUTBOL24-5E023A613754E8B4")) {
            assertTrue(
                    fixtures.stream().noneMatch(fixture -> fixture.matchId().equals(excludedMatchId)),
                    excludedMatchId + " 异常比赛不应进入历史数据");
        }
    }

    @Test
    void shouldContainCurrentEuropaLeaguePortugueseSuperCupAndRequestedFriendlies() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 6),
                Competition.EUROPA_LEAGUE,
                "库奥皮奥",
                "克拉约瓦",
                1,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 7),
                Competition.EUROPA_LEAGUE,
                "本菲卡",
                "哈茨",
                6,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 1),
                Competition.CLUB_FRIENDLY,
                "格罗宁根",
                "阿尔梅勒",
                1,
                3);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 11),
                Competition.CLUB_FRIENDLY,
                "乌德勒支",
                "比肖特VA",
                2,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 2),
                Competition.CLUB_OFFICIAL_OTHER,
                "波尔图",
                "托林斯",
                1,
                0);

        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 7),
                "VOC",
                "鹿斯巴达",
                0,
                4);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 26),
                LocalDate.of(2026, 7, 25),
                "波尔图",
                "维拉",
                2,
                1);

        assertSourceCompetitionCount(fixtures, "葡超杯", 12L);
        assertSourceCompetitionCount(fixtures, "欧罗巴", 4_000L);
    }

    @Test
    void shouldContainComplete2026JagielloniaLeagueAndClubMatches() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();
        List<HistoricalFixture> jagielloniaFixtures = fixtures.stream()
                .filter(fixture -> fixture.matchDate().getYear() == 2026)
                .filter(fixture -> fixture.homeTeam().equals("比亚韦")
                        || fixture.awayTeam().equals("比亚韦"))
                .toList();

        long polishLeagueCount = jagielloniaFixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("波甲"))
                .count();
        long clubMatchCount = jagielloniaFixtures.stream()
                .filter(fixture -> fixture.matchType().equals("CLUB_FRIENDLY"))
                .count();

        assertEquals(19L, polishLeagueCount, "2026 比亚韦波甲数据不完整");
        assertEquals(14L, clubMatchCount, "2026 比亚韦俱乐部赛数据不完整");
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 1),
                Competition.CLUB_OFFICIAL_OTHER,
                "莫托路宾",
                "比亚韦",
                1,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 1),
                Competition.CLUB_FRIENDLY,
                "比亚韦",
                "Suduva",
                1,
                1);
    }

    @Test
    void shouldContainRequestedClubFriendlyAndPortugueseCupMatches() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2025, 1, 30),
                Competition.CLUB_FRIENDLY,
                "天狼星",
                "布鲁马波",
                2,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2024, 1, 12),
                Competition.CLUB_OFFICIAL_OTHER,
                "圣克拉拉",
                "葡国民",
                0,
                0);
        assertTrue(fixtures.stream().noneMatch(fixture ->
                fixture.homeTeam().equals("莱里雅") || fixture.awayTeam().equals("莱里雅")));
    }

    @Test
    void shouldNotContainDuplicateNormalizedFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();
        Map<String, Integer> fixtureCounts = new HashMap<>();
        for (HistoricalFixture fixture : fixtures) {
            String firstTeam = fixture.homeTeam().compareTo(fixture.awayTeam()) <= 0
                    ? fixture.homeTeam()
                    : fixture.awayTeam();
            String secondTeam = fixture.homeTeam().compareTo(fixture.awayTeam()) <= 0
                    ? fixture.awayTeam()
                    : fixture.homeTeam();
            String scope = fixture.competition().name();
            String key = scope + "|" + fixture.matchDate() + "|" + firstTeam + "|" + secondTeam;
            fixtureCounts.merge(key, 1, Integer::sum);
        }
        List<String> duplicates = fixtureCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> entry.getKey() + " x" + entry.getValue())
                .limit(20)
                .toList();

        assertEquals(List.of(), duplicates, "存在统一队名后的重复比赛");
    }

    @Test
    void shouldContainVerifiedDjurgardenAndVasterasFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2022, 1, 22),
                Competition.CLUB_FRIENDLY,
                "佐加顿斯",
                "韦斯特罗",
                3,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2024, 5, 5),
                Competition.SWEDISH_ALLSVENSKAN,
                "韦斯特罗",
                "佐加顿斯",
                0,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2024, 10, 28),
                Competition.SWEDISH_ALLSVENSKAN,
                "佐加顿斯",
                "韦斯特罗",
                2,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2025, 1, 25),
                Competition.CLUB_FRIENDLY,
                "韦斯特罗",
                "佐加顿斯",
                3,
                3);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 2, 7),
                Competition.CLUB_FRIENDLY,
                "韦斯特罗",
                "佐加顿斯",
                0,
                1);
        assertFixtureOccurs(
                fixtures,
                LocalDate.of(2024, 10, 29),
                Competition.SWEDISH_ALLSVENSKAN,
                "佐加顿斯",
                "韦斯特罗",
                2,
                1,
                0L);
    }

    private List<HistoricalFixture> readHistoricalFixtures() throws IOException {
        InputStream inputStream = HistoricalMatchDataIntegrityTest.class.getClassLoader()
                .getResourceAsStream("data/historical_matches.csv");
        assertNotNull(inputStream);

        List<HistoricalFixture> fixtures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8))) {
            List<String> headers = CsvUtils.parseLine(reader.readLine());
            Map<String, Integer> indexes = new LinkedHashMap<>();
            for (int index = 0; index < headers.size(); index++) {
                indexes.put(headers.get(index), index);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> row = CsvUtils.parseLine(line);
                Competition competition = Competition.fromSourceCompetition(
                        CsvUtils.get(row, indexes.get("source_competition")),
                        Competition.fromCode(CsvUtils.get(row, indexes.get("competition"))));
                fixtures.add(new HistoricalFixture(
                        CsvUtils.get(row, indexes.get("match_id")),
                        LocalDate.parse(CsvUtils.get(row, indexes.get("match_date"))),
                        competition,
                        ClubTeamNameTranslator.translate(
                                competition,
                                CsvUtils.get(row, indexes.get("home_team_cn"))),
                        ClubTeamNameTranslator.translate(
                                competition,
                                CsvUtils.get(row, indexes.get("away_team_cn"))),
                        Integer.parseInt(CsvUtils.get(row, indexes.get("home_score"))),
                        Integer.parseInt(CsvUtils.get(row, indexes.get("away_score"))),
                        CsvUtils.get(row, indexes.get("source_competition")),
                        CsvUtils.get(row, indexes.get("match_type"))));
            }
        }
        return fixtures;
    }

    private void assertFixtureOccursOnce(
            List<HistoricalFixture> fixtures,
            LocalDate matchDate,
            Competition competition,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore) {
        assertFixtureOccurs(
                fixtures,
                matchDate,
                competition,
                homeTeam,
                awayTeam,
                homeScore,
                awayScore,
                1L);
    }

    private void assertFixtureOccurs(
            List<HistoricalFixture> fixtures,
            LocalDate matchDate,
            Competition competition,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            long expectedCount) {
        long count = fixtures.stream()
                .filter(fixture -> fixture.matchDate().equals(matchDate))
                .filter(fixture -> fixture.competition() == competition)
                .filter(fixture -> fixture.homeTeam().equals(homeTeam))
                .filter(fixture -> fixture.awayTeam().equals(awayTeam))
                .filter(fixture -> fixture.homeScore() == homeScore)
                .filter(fixture -> fixture.awayScore() == awayScore)
                .count();

        assertEquals(
                expectedCount,
                count,
                matchDate + " " + homeTeam + " " + homeScore + ":" + awayScore + " " + awayTeam);
    }

    private void assertShiftedFixtureRemoved(
            List<HistoricalFixture> fixtures,
            LocalDate retainedDate,
            LocalDate removedDate,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore) {
        assertFixtureOccursOnce(
                fixtures,
                retainedDate,
                Competition.CLUB_FRIENDLY,
                homeTeam,
                awayTeam,
                homeScore,
                awayScore);
        assertFixtureOccurs(
                fixtures,
                removedDate,
                Competition.CLUB_FRIENDLY,
                homeTeam,
                awayTeam,
                homeScore,
                awayScore,
                0L);
    }

    private void assertSourceCompetitionCoverage(
            List<HistoricalFixture> fixtures,
            String sourceCompetition,
            long minimumCount) {
        List<HistoricalFixture> sourceFixtures = fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals(sourceCompetition))
                .toList();
        assertTrue(sourceFixtures.size() >= minimumCount, sourceCompetition + " 历史数据不完整");
        assertTrue(sourceFixtures.stream().allMatch(fixture ->
                !fixture.matchDate().isBefore(LocalDate.of(2014, 10, 22))), sourceCompetition);
        assertTrue(sourceFixtures.stream().anyMatch(fixture ->
                !fixture.matchDate().isBefore(LocalDate.of(2026, 7, 1))), sourceCompetition);
    }

    private void assertSourceCompetitionYearCoverage(
            List<HistoricalFixture> fixtures,
            String sourceCompetition,
            int year,
            long minimumCount) {
        long count = fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals(sourceCompetition))
                .filter(fixture -> fixture.matchDate().getYear() == year)
                .count();

        assertTrue(
                count >= minimumCount,
                sourceCompetition + " " + year + " 数据不足：" + count);
    }

    private void assertSourceCompetitionCount(
            List<HistoricalFixture> fixtures,
            String sourceCompetition,
            long minimumCount) {
        long count = fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals(sourceCompetition))
                .count();

        assertTrue(count >= minimumCount, sourceCompetition + " 历史数据不足：" + count);
    }

    private record HistoricalFixture(
            String matchId,
            LocalDate matchDate,
            Competition competition,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            String sourceCompetition,
            String matchType) {

    }

}
