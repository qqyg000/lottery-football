package com.eason.worldcup.util;

import com.eason.worldcup.model.Competition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class VerifiedClubAliasesTest {

    private static URLClassLoader productionLoader;

    private static Method translate;

    private static Map<String, Object> competitions;

    @BeforeAll
    static void loadProductionMappings() throws Exception {
        // 隔离测试资源中的小型映射表，验证实际打包使用的完整生产映射及优先级
        productionLoader = new URLClassLoader(new URL[]{
                Path.of("src/main/resources").toUri().toURL(),
                ClubTeamNameTranslator.class.getProtectionDomain().getCodeSource().getLocation()
        }, ClassLoader.getPlatformClassLoader());
        Class<?> competitionClass = productionLoader.loadClass(Competition.class.getName());
        Class<?> translatorClass = productionLoader.loadClass(ClubTeamNameTranslator.class.getName());
        translate = translatorClass.getMethod("translate", competitionClass, String.class);
        competitions = new java.util.HashMap<>();
        for (Object competition : competitionClass.getEnumConstants()) {
            competitions.put(((Enum<?>) competition).name(), competition);
        }
    }

    @AfterAll
    static void closeProductionMappings() throws Exception {
        if (productionLoader != null) {
            productionLoader.close();
        }
    }

    @Test
    void shouldPreferChineseNamesForVerifiedAliasesAcrossCompetitions() throws Exception {
        Map<String, String> examples = Map.ofEntries(
                Map.entry("Huddersfield", "哈德斯菲尔德"),
                Map.entry("Hertha Berlin", "柏林赫塔"),
                Map.entry("Montpellier HSC", "蒙彼利埃"),
                Map.entry("FC Cologne", "科隆"),
                Map.entry("Rotherham United", "罗瑟汉姆"),
                Map.entry("Gimnàstic de Tarragona", "塔拉戈纳"),
                Map.entry("New England Revs", "新英格兰革命"),
                Map.entry("Columbus Crew", "哥伦布机员"),
                Map.entry("SC Verl 1924", "SC维尔"),
                Map.entry("CSKA 1948 Sofia", "索菲亚1948"),
                Map.entry("Al-Wakrah", "威柯拉"),
                Map.entry("Paksi SE", "保克什"));

        for (Map.Entry<String, String> example : examples.entrySet()) {
            assertEquals(example.getValue(), mapped(null, example.getKey()));
            for (Competition competition : Competition.values()) {
                assertEquals(example.getValue(), mapped(competition, example.getKey()),
                        competition + ":" + example.getKey());
            }
        }
    }

    @Test
    void shouldRestrictAmbiguousNamesToVerifiedFriendlyContext() throws Exception {
        assertEquals("维也纳", mapped(Competition.CLUB_FRIENDLY, "奥地利"));
        assertEquals("奥地利", mapped(Competition.INTERNATIONAL_FRIENDLY, "奥地利"));
        assertEquals("奥地利", mapped(Competition.UEFA_NATIONS_LEAGUE, "Austria"));
        assertEquals("瓜达拉", mapped(Competition.CLUB_FRIENDLY, "Guadalajara"));
        assertEquals("Guadalajara", mapped(Competition.CLUB_OFFICIAL_OTHER, "Guadalajara"));
        assertEquals("阿里斯", mapped(Competition.CLUB_FRIENDLY, "Aris"));
        assertEquals("Aris", mapped(Competition.CLUB_OFFICIAL_OTHER, "Aris"));
        assertEquals("莫陆军", mapped(Competition.CLUB_FRIENDLY, "CSKA"));
        assertEquals("CSKA", mapped(Competition.CLUB_OFFICIAL_OTHER, "CSKA"));
    }

    @Test
    void shouldKeepDifferentClubsAndReserveTeamsSeparate() throws Exception {
        for (String[] pair : new String[][]{
                {"CSKA 1948", "索菲亚中央陆军"},
                {"CSKA 1948", "CSKA 1948 II"},
                {"SC Verl 1924", "SC Verl II"},
                {"Bilbao", "Bilbao Athletic"},
                {"FC Kosice", "MFK Kosice"},
                {"FK AS Pardubice", "Pardubice"},
                {"吉马良斯", "维多利亚"},
                {"Club Olimpia", "Olympiakos"},
                {"KFC Uerdingen 05", "Verl"}
        }) {
            assertNotEquals(mapped(Competition.CLUB_FRIENDLY, pair[0]),
                    mapped(Competition.CLUB_FRIENDLY, pair[1]), String.join(" / ", pair));
        }
    }

    private String mapped(Competition competition, String name) throws Exception {
        Object productionCompetition = competition == null ? null : competitions.get(competition.name());
        return (String) translate.invoke(null, productionCompetition, name);
    }

}
