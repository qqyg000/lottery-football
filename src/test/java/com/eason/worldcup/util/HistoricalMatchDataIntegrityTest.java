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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricalMatchDataIntegrityTest {

    @Test
    void shouldContainCompleteRequestedGermanCompetitionHistory() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(fixtures, LocalDate.of(2024, 2, 18),
                Competition.BUNDESLIGA, "莱红牛", "门兴", 2, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 2, 21),
                Competition.BUNDESLIGA, "科隆", "霍芬海姆", 2, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2020, 2, 29),
                Competition.BUNDESLIGA, "美因茨", "帕德博恩", 2, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 3),
                Competition.CLUB_OFFICIAL_OTHER, "埃沃斯堡", "帕德博恩", 5, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 22),
                Competition.CLUB_OFFICIAL_OTHER, "罗斯托克", "斯图加特", 0, 4);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 23),
                Competition.CLUB_OFFICIAL_OTHER, "多特蒙德", "拜仁", 1, 2);

        Map<String, Long> minimumCoverage = Map.of(
                "德甲", 3_600L,
                "德乙", 3_600L,
                "德国杯", 754L,
                "德国超级杯", 12L);
        minimumCoverage.forEach((sourceCompetition, minimumCount) -> {
            List<HistoricalFixture> competitionFixtures = fixtures.stream()
                    .filter(fixture -> fixture.sourceCompetition().equals(sourceCompetition))
                    .toList();
            assertTrue(competitionFixtures.size() >= minimumCount,
                    sourceCompetition + " 历史数据不完整：" + competitionFixtures.size());
            assertTrue(competitionFixtures.stream()
                            .allMatch(fixture -> fixture.matchId().startsWith("FOTMOB-")),
                    sourceCompetition + " 仍包含旧来源重复记录");
        });
    }

    @Test
    void shouldContainRequestedEnglishAndFrenchHistoricalFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(fixtures, LocalDate.of(2017, 2, 2),
                Competition.PREMIER_LEAGUE, "曼联", "赫尔城", 0, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 12),
                Competition.CLUB_OFFICIAL_OTHER, "米尔沃尔", "赫尔城", 0, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2023, 4, 5),
                Competition.PREMIER_LEAGUE, "利兹联", "诺丁汉", 2, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 2),
                Competition.CLUB_OFFICIAL_OTHER, "伊普斯", "女王巡游", 3, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 17),
                Competition.CLUB_OFFICIAL_OTHER, "朗斯", "巴黎圣曼", 1, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 2, 15),
                Competition.LIGUE_1, "马赛", "斯特拉斯", 2, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2024, 9, 30),
                Competition.LIGUE_1, "斯特拉斯", "马赛", 1, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2023, 11, 26),
                Competition.LIGUE_1, "斯特拉斯", "马赛", 1, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2025, 10, 5),
                Competition.LIGUE_1, "欧塞尔", "朗斯", 1, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 2),
                Competition.CLUB_OFFICIAL_OTHER, "沃特福德", "考文垂", 0, 4);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 4, 22),
                Competition.CLUB_OFFICIAL_OTHER, "考文垂", "朴次茅斯", 5, 1);

        assertFixtureOccursOnce(fixtures, LocalDate.of(2019, 8, 10),
                Competition.CLUB_OFFICIAL_OTHER, "伊普斯", "桑德兰", 1, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2020, 2, 8),
                Competition.CLUB_OFFICIAL_OTHER, "桑德兰", "伊普斯", 1, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2020, 11, 4),
                Competition.CLUB_OFFICIAL_OTHER, "桑德兰", "伊普斯", 2, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2021, 1, 27),
                Competition.CLUB_OFFICIAL_OTHER, "伊普斯", "桑德兰", 0, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2021, 11, 20),
                Competition.CLUB_OFFICIAL_OTHER, "桑德兰", "伊普斯", 2, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2021, 12, 18),
                Competition.CLUB_OFFICIAL_OTHER, "伊普斯", "桑德兰", 1, 1);

        Map<String, Long> minimumCoverage = Map.of(
                "英足总杯", 2_050L,
                "英联赛杯", 1_260L,
                "英社区盾", 12L,
                "英冠", 7_300L,
                "英甲", 8_500L,
                "法超杯", 12L,
                "法甲", 4_100L);
        minimumCoverage.forEach((sourceCompetition, minimumCount) -> {
            long count = fixtures.stream()
                    .filter(fixture -> fixture.sourceCompetition().equals(sourceCompetition))
                    .count();
            assertTrue(count >= minimumCount, sourceCompetition + " 历史数据不完整：" + count);
        });
    }

    @Test
    void shouldContainCompleteItalianCupHistoryAndVerifiedLeedsFriendly() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(fixtures, LocalDate.of(2014, 12, 2),
                Competition.CLUB_OFFICIAL_OTHER, "拉齐奥", "瓦雷泽", 3, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 18),
                Competition.CLUB_OFFICIAL_OTHER, "巴勒莫", "莱切", 2, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 8),
                Competition.CLUB_FRIENDLY, "利兹联", "莱红牛", 1, 0);

        long italianCupCount = fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("意大利杯"))
                .count();
        assertTrue(italianCupCount >= 740L, "意大利杯历史数据不完整：" + italianCupCount);
        assertTrue(fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("意大利杯"))
                .allMatch(fixture -> fixture.matchId().startsWith("FOTMOB-")),
                "意大利杯仍包含旧来源重复记录");
    }

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
                LocalDate.of(2026, 7, 30),
                Competition.CHAMPIONS_LEAGUE,
                "波兹南",
                "奥胡斯",
                0,
                3);
        assertFixtureOccurs(
                fixtures,
                LocalDate.of(2026, 7, 30),
                Competition.CHAMPIONS_LEAGUE,
                "波兹南",
                "奥胡斯",
                1,
                4,
                0L);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2020, 9, 24),
                Competition.EUROPA_LEAGUE,
                "亚拉腊",
                "采列",
                0,
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
        assertSourceCompetitionCoverage(fixtures, "斯洛文甲", 1_500L);
        assertSourceCompetitionCoverage(fixtures, "亚美尼超", 1_300L);

        Set<String> replacedAliases = Set.of(
                "阿拉木图凯拉特",
                "索菲亚列夫斯基",
                "Septemvri Sofia",
                "PFC Lokomotiv Sofia 1929",
                "Dunav Ruse",
                "巴尼亚",
                "FK Radnik Surdulica",
                "U Craiova",
                "利勒斯特罗姆",
                "桑纳菲尤尔",
                "K. Diegem Sport",
                "Celje",
                "Riga FC");
        assertTrue(fixtures.stream().noneMatch(fixture ->
                replacedAliases.contains(fixture.homeTeam())
                        || replacedAliases.contains(fixture.awayTeam())), "指定队名别名未完全归一化");
    }

    @Test
    void shouldNormalizeAugustThirtiethTeamNamesAndRemoveDuplicates() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2014, 10, 22),
                Competition.CLUB_OFFICIAL_OTHER,
                "克劳利",
                "沃尔索尔",
                1,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 6, 3),
                Competition.CLUB_OFFICIAL_OTHER,
                "Hudiksvalls FF",
                "弗里斯卡",
                0,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 25),
                Competition.CLUB_FRIENDLY,
                "莱切",
                "伊斯特拉1961",
                2,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 1),
                Competition.CLUB_FRIENDLY,
                "印尼明星",
                "维拉",
                1,
                3);

        Set<String> replacedAliases = Set.of(
                "NK Istra 1961",
                "Friska Viljor",
                "Indonesia SL All Star",
                "Walsall");
        assertTrue(fixtures.stream().noneMatch(fixture ->
                replacedAliases.contains(fixture.homeTeam())
                        || replacedAliases.contains(fixture.awayTeam())), "指定队名别名未完全归一化");
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
    void shouldContainRequestedFinnishDutchSpanishAndVerifiedFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertSourceCompetitionCount(fixtures, "芬甲", 1_650L);
        assertSourceCompetitionCount(fixtures, "荷兰杯", 680L);
        assertSourceCompetitionCount(fixtures, "西乙", 5_500L);
        long laLiga2022To2023Count = fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("西甲"))
                .filter(fixture -> !fixture.matchDate().isBefore(LocalDate.of(2022, 8, 1)))
                .filter(fixture -> !fixture.matchDate().isAfter(LocalDate.of(2023, 6, 30)))
                .count();
        assertEquals(380L, laLiga2022To2023Count, "2022-2023 西甲应有 380 场");

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 22),
                Competition.CLUB_FRIENDLY,
                "吉马良斯",
                "莱里亚",
                0,
                0);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2023, 7, 19),
                Competition.CLUB_FRIENDLY,
                "特温特",
                "兹沃勒",
                3,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2018, 7, 15),
                Competition.CLUB_FRIENDLY,
                "埃斯托里",
                "葡国民",
                2,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 9),
                Competition.CLUB_FRIENDLY,
                "卡斯特隆",
                "莱万特",
                1,
                3);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2022, 8, 28),
                Competition.EREDIVISIE,
                "SBV精英",
                "埃因霍温",
                1,
                6);
        assertFixtureOccurs(
                fixtures,
                LocalDate.of(2022, 8, 28),
                Competition.EREDIVISIE,
                "SBV精英",
                "埃因霍温",
                3,
                4,
                0L);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2018, 7, 29),
                Competition.CLUB_FRIENDLY,
                "斯图加特",
                "埃瓦尔",
                2,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2019, 7, 27),
                Competition.CLUB_FRIENDLY,
                "贝西克塔斯",
                "埃瓦尔",
                0,
                2);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 26),
                Competition.CLUB_FRIENDLY,
                "埃瓦尔",
                "毕尔巴鄂",
                2,
                2);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2015, 7, 18),
                LocalDate.of(2015, 7, 19),
                "埃瓦尔",
                "凯尔特人",
                1,
                4);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2016, 8, 4),
                LocalDate.of(2016, 8, 5),
                "科隆",
                "埃瓦尔",
                2,
                0);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2016, 8, 6),
                LocalDate.of(2016, 8, 7),
                "莱红牛",
                "埃瓦尔",
                3,
                2);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2016, 8, 10),
                LocalDate.of(2016, 8, 11),
                "皇家社会",
                "埃瓦尔",
                1,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2016, 8, 12),
                LocalDate.of(2016, 8, 13),
                "奥萨苏纳",
                "埃瓦尔",
                0,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 18),
                LocalDate.of(2026, 7, 19),
                "阿拉维斯",
                "埃瓦尔",
                1,
                1);
    }

    @Test
    void shouldStoreFotMobKnockoutMatchesAtRegulationTime() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertMatchScore(fixtures, "FOTMOB-1801921", 1, 1);
        assertMatchScore(fixtures, "FOTMOB-3028221", 1, 1);
        assertMatchScore(fixtures, "FOTMOB-3608943", 1, 0);
        assertMatchScore(fixtures, "FOTMOB-3835647", 2, 1);
        assertMatchScore(fixtures, "FOTMOB-4737736", 4, 3);
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
    void shouldNotContainAdjacentDateDuplicateNormalizedFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();
        Map<String, HistoricalFixture> fixturesByResult = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        for (HistoricalFixture fixture : fixtures) {
            HistoricalFixture previousFixture = fixturesByResult.get(buildFixtureResultKey(
                    fixture,
                    fixture.matchDate().minusDays(1)));
            if (previousFixture != null && duplicates.size() < 20) {
                duplicates.add(previousFixture.matchId() + " / " + fixture.matchId());
            }
            fixturesByResult.put(buildFixtureResultKey(fixture, fixture.matchDate()), fixture);
        }

        assertEquals(List.of(), duplicates, "存在统一队名后的跨日重复比赛");
    }

    @Test
    void shouldKeepAugustTwentyThirdMappedFixturesAndRemoveAliasDuplicates() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 7, 27),
                Competition.CLUB_FRIENDLY, "拉齐奥", "弗拉米纳西维塔", 6, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2014, 11, 8),
                Competition.CLUB_OFFICIAL_OTHER, "卢顿", "纽波特郡", 4, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 7, 22),
                Competition.CLUB_FRIENDLY, "蒙彼利埃", "戛纳", 3, 4);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 7, 23),
                Competition.CLUB_FRIENDLY, "佛罗伦萨", "古比奥", 1, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 7, 25),
                Competition.CLUB_FRIENDLY, "博洛尼亚", "Iraklis 1908", 2, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 7, 25),
                Competition.CLUB_FRIENDLY, "女王巡游", "佛罗伦萨", 3, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 6),
                Competition.CLUB_FRIENDLY, "马拉加", "阿拉比", 4, 2);
        assertShiftedFixtureRemoved(fixtures,
                LocalDate.of(2016, 7, 8), LocalDate.of(2016, 7, 9),
                "埃因霍温", "女王巡游", 0, 1);
        assertShiftedFixtureRemoved(fixtures,
                LocalDate.of(2016, 7, 11), LocalDate.of(2016, 7, 12),
                "格罗宁根", "女王巡游", 3, 1);
    }

    @Test
    void shouldKeepRequestedCanonicalFriendliesAndRemoveAliasDuplicates() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2015, 7, 9),
                Competition.CLUB_FRIENDLY,
                "柔佛",
                "多特蒙德",
                1,
                6);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2022, 11, 28),
                Competition.CLUB_FRIENDLY,
                "柔佛",
                "多特蒙德",
                1,
                4);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 18),
                LocalDate.of(2026, 7, 19),
                "埃尔切",
                "凯萨酋长",
                2,
                1);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 18),
                LocalDate.of(2026, 7, 19),
                "Compostela",
                "拉科",
                0,
                4);
        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 25),
                "埃尔切",
                "柔佛",
                0,
                0);
    }

    @Test
    void shouldKeepBadSchallerbachFixtureAndRemovePreviousDateAliasDuplicate() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertShiftedFixtureRemoved(
                fixtures,
                LocalDate.of(2026, 6, 25),
                LocalDate.of(2026, 6, 24),
                "Bad Schallerbach",
                "LASK林茨",
                0,
                6);
    }

    @Test
    void shouldKeepOnlyVerifiedSlovanPafosFriendly() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 7, 16),
                Competition.CLUB_FRIENDLY,
                "布拉迪斯",
                "帕福斯",
                3,
                2);
        assertTrue(fixtures.stream().noneMatch(fixture ->
                fixture.matchDate().equals(LocalDate.of(2026, 7, 17))
                        && fixture.competition() == Competition.CLUB_FRIENDLY
                        && fixture.homeTeam().equals("布拉迪斯")
                        && fixture.awayTeam().equals("帕福斯")
                        && fixture.homeScore() == 3
                        && fixture.awayScore() == 2));
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

    @Test
    void shouldContainRequestedLigueOneAndGreekSuperCupFixtures() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 5, 11),
                Competition.LIGUE_1,
                "图卢兹",
                "里昂",
                2,
                1);
        assertFixtureOccursOnce(
                fixtures,
                LocalDate.of(2026, 8, 13),
                Competition.CLUB_OFFICIAL_OTHER,
                "雅典AEK",
                "OFI",
                2,
                2);
    }

    @Test
    void shouldContainRequestedSerieAClubFriendlySerieBAndFrenchLeagueCupData() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 7, 10),
                Competition.CLUB_FRIENDLY, "维塞乌", "科维良", 5, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 17),
                Competition.SERIE_A, "国际米兰", "维罗纳", 1, 1);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2020, 9, 28),
                Competition.SERIE_A, "那不勒斯", "热那亚", 6, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 17),
                Competition.SERIE_A, "比萨", "那不勒斯", 0, 3);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 10),
                Competition.CLUB_FRIENDLY, "帕尔马", "桑普多利亚", 0, 2);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 9),
                Competition.CLUB_FRIENDLY, "卡利亚里", "尼斯", 0, 0);
        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 5, 18),
                Competition.SERIE_A, "卡利亚里", "都灵", 2, 1);

        assertSourceCompetitionCount(fixtures, "意乙", 4_000L);
        assertSourceCompetitionCount(fixtures, "法联赛杯", 200L);
        assertTrue(fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("意乙"))
                .allMatch(fixture -> !fixture.matchDate().isBefore(LocalDate.of(2014, 10, 22))),
                "意乙包含超出导入范围的数据");
        assertTrue(fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("法联赛杯"))
                .allMatch(fixture -> !fixture.matchDate().isBefore(LocalDate.of(2014, 10, 22))),
                "法联赛杯包含超出导入范围的数据");
    }

    @Test
    void shouldContainRequestedLecceFriendlyAndCompletePortugueseCupHistory() throws IOException {
        List<HistoricalFixture> fixtures = readHistoricalFixtures();

        assertFixtureOccursOnce(fixtures, LocalDate.of(2026, 8, 9),
                Competition.CLUB_FRIENDLY, "莱切", "Monopoli", 1, 0);
        assertSourceCompetitionCount(fixtures, "葡萄牙杯", 850L);
        assertTrue(fixtures.stream()
                .filter(fixture -> fixture.sourceCompetition().equals("葡萄牙杯"))
                .allMatch(fixture -> !fixture.matchDate().isBefore(LocalDate.of(2014, 10, 22))),
                "葡萄牙杯包含超出导入范围的数据");
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

    private void assertMatchScore(
            List<HistoricalFixture> fixtures,
            String matchId,
            int homeScore,
            int awayScore) {
        HistoricalFixture fixture = fixtures.stream()
                .filter(candidate -> candidate.matchId().equals(matchId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少历史比赛：" + matchId));

        assertEquals(homeScore, fixture.homeScore(), matchId + " 主队 90 分钟比分错误");
        assertEquals(awayScore, fixture.awayScore(), matchId + " 客队 90 分钟比分错误");
    }

    private String buildFixtureResultKey(HistoricalFixture fixture, LocalDate matchDate) {
        String scope = fixture.competition() == Competition.CLUB_OFFICIAL_OTHER
                ? fixture.competition().name() + "|" + fixture.sourceCompetition()
                : fixture.competition().name();
        if (fixture.homeTeam().compareTo(fixture.awayTeam()) <= 0) {
            return scope
                    + "|" + matchDate
                    + "|" + fixture.homeTeam()
                    + "|" + fixture.awayTeam()
                    + "|" + fixture.homeScore()
                    + "|" + fixture.awayScore();
        }
        return scope
                + "|" + matchDate
                + "|" + fixture.awayTeam()
                + "|" + fixture.homeTeam()
                + "|" + fixture.awayScore()
                + "|" + fixture.homeScore();
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
