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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ClubTeamNameTranslatorTest {

    @Test
    void shouldTranslateSupplementalClubCupAliases() throws IOException {
        assumeMappingsImported();

        assertEquals("沙佩科恩斯", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "Chapecoense"));
        assertEquals("沙佩科恩斯", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "沙佩科恩斯"));
        assertEquals("巴伊亚", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Bahia/BA"));
        assertEquals("Cienciano", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Cienciano del Cusco"));
        assertEquals("Polissya Zhitomir", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Polissya Zhytomyr"));
        assertEquals("Turan Tovuz", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Turan"));
    }

    @Test
    void shouldUseSportteryStandardNamesForCurrentAliases() throws IOException {
        assumeMappingsImported();

        assertEquals("博塔弗戈", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "Botafogo"));
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
        assertEquals("库奥皮奥", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "KuPS"));
        assertEquals("齐拉", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Zira FK"));
        assertEquals("塞伊奈约基", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "SJK"));
        assertEquals("坦佩雷山猫", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Ilves Tampere"));
        assertEquals("赫尔辛基火花", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "IF Gnistan"));
        assertEquals("贝西克塔", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Besiktas JK"));
        assertEquals("北安普敦", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Northampton"));
        assertEquals("费内巴切", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Fenerbahçe SK"));
        assertEquals("沙勒罗瓦", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Sporting Charleroi"));
        assertEquals("加拉塔萨雷", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Galatasaray SK"));
        assertEquals("奥林匹亚科斯", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Olympiakos Pireus"));
        assertEquals("Admira Wacker", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "FC Admira Wacker Modling"));
        assertEquals("巴拉纳竞技", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "Athletico-PR"));
        assertEquals("里莫", ClubTeamNameTranslator.translate(
                Competition.BRAZIL_SERIE_A,
                "瑞模贝雷"));
        assertEquals("伏伊伏丁那", ClubTeamNameTranslator.translate(
                Competition.EUROPA_LEAGUE,
                "Vojvodina"));
        assertEquals("斯普利特海杜克", ClubTeamNameTranslator.translate(
                Competition.EUROPA_LEAGUE,
                "斯海杜克"));
        assertEquals("索菲亚中央陆军", ClubTeamNameTranslator.translate(
                Competition.EUROPA_LEAGUE,
                "CSKA Sofia"));
        assertEquals("克拉克斯维克", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "KI Klaksvik"));
        assertEquals("雷克雅未克维京人", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "Vikingur Reykjavik"));
        assertEquals("沙特阿拉伯", ClubTeamNameTranslator.translate(
                Competition.WORLD_CUP,
                "Saudi Arabia"));
        assertEquals("科罗纳", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Korona Kielce"));
        assertEquals("保克什", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Paksi FC"));
        assertEquals("北西兰", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "FC Nordsjælland"));
        assertEquals("莫迪纳摩", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Dynamo Moscow"));
        assertEquals("布迪纳摩", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Dinamo Bucuresti"));
        assertEquals("拉赫蒂", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "FC Lahti"));
        assertEquals("奥胡斯", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "AGF Aarhus"));
        assertEquals("琴斯托霍", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Raków"));
        assertEquals("比亚韦", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Jagiellonia"));
        assertEquals("LASK林茨", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "LASK Linz"));
        assertEquals("南部女王", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Queen of the South"));
        assertEquals("伊斯坦布", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Istanbul Basaksehir"));
        assertEquals("费伦茨瓦罗斯", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Ferencvaros TC"));
        assertEquals("里耶卡", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "HNK Rijeka"));
        assertEquals("兹林", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "FK Zlin"));
    }

    @Test
    void shouldResolveEveryMappingAliasToStableStandardName() throws IOException {
        assumeMappingsImported();

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
                String aliasName = CsvUtils.get(row, indexes.get("alias_team_name"));
                String translatedName = ClubTeamNameTranslator.translate(competition, aliasName);
                assertNotNull(translatedName, competitionCode + ":" + aliasName);
                assertEquals(
                        translatedName,
                        ClubTeamNameTranslator.translate(competition, translatedName),
                        competitionCode + ":" + aliasName);
                checkedRows++;
            }
        }
        assertTrue(checkedRows > 0);
    }

    private void assumeMappingsImported() throws IOException {
        InputStream inputStream = ClubTeamNameTranslatorTest.class.getClassLoader()
                .getResourceAsStream("data/team_name_mappings.csv");
        assertNotNull(inputStream);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8))) {
            reader.readLine();
            assumeTrue(
                    reader.lines().anyMatch(line -> !line.isBlank() && !line.startsWith("#")),
                    "球队名映射等待重新导入");
        }
    }

}
