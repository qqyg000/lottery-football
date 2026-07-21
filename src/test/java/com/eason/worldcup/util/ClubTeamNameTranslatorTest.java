package com.eason.worldcup.util;

import com.eason.worldcup.model.Competition;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubTeamNameTranslatorTest {

    @Test
    void shouldUseSportteryStandardNamesForCurrentAliases() {
        assertEquals("米内罗竞技", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "米竞技"));
        assertEquals("米内罗竞技", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "Atlético-MG"));
        assertEquals("库奥皮奥", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "KuPS Kuopio"));
        assertEquals("奥胡斯", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "AGF"));
        assertEquals("波兹南莱赫", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "Lech Poznan"));
        assertEquals("格拉茨风暴", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "SK Sturm Graz"));
        assertEquals("哈茨", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "Heart of Midlothian"));
    }

    @Test
    void shouldResolveEveryMappingRowToItsStandardName() throws IOException {
        InputStream inputStream = ClubTeamNameTranslatorTest.class.getClassLoader()
                .getResourceAsStream("data/team_name_mappings.csv");
        assertNotNull(inputStream);

        int checkedRows = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            List<String> headers = CsvUtils.parseLine(headerLine);
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
                String competitionCode = CsvUtils.get(row, indexes.get("competition"));
                Competition competition = "*".equals(competitionCode)
                        ? null
                        : Competition.fromCode(competitionCode);
                String standardName = CsvUtils.get(row, indexes.get("standard_team_name"));
                String aliasName = CsvUtils.get(row, indexes.get("alias_team_name"));
                assertEquals(
                        standardName,
                        ClubTeamNameTranslator.translate(competition, aliasName),
                        competitionCode + ":" + aliasName);
                checkedRows++;
            }
        }
        assertTrue(checkedRows > 4000);
    }

}
