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
                        CsvUtils.get(row, indexes.get("source_competition"))));
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

    private record HistoricalFixture(
            LocalDate matchDate,
            Competition competition,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            String sourceCompetition) {

    }

}
