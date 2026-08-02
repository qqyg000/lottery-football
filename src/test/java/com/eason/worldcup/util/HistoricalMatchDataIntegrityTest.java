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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoricalMatchDataIntegrityTest {

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
                        Integer.parseInt(CsvUtils.get(row, indexes.get("away_score")))));
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
            int awayScore) {

    }

}
