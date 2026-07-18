package com.eason.worldcup.util;

import com.eason.worldcup.model.Competition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClubTeamNameTranslator {

    private static final String HISTORICAL_ODDS_RESOURCE = "data/historical_odds_data.csv";

    private static final Map<String, String> SOURCE_NAME_ALIASES = Map.ofEntries(
            Map.entry(canonicalName("Bayern Munich"), "Bayern München"),
            Map.entry(canonicalName("FC Bayern Munich"), "Bayern München"),
            Map.entry(canonicalName("Internazionale"), "Inter"),
            Map.entry(canonicalName("Internazionale Milano"), "Inter"),
            Map.entry(canonicalName("Inter Milan"), "Inter"),
            Map.entry(canonicalName("Manchester Utd"), "Manchester United"),
            Map.entry(canonicalName("Man United"), "Manchester United"),
            Map.entry(canonicalName("Man City"), "Manchester City"),
            Map.entry(canonicalName("Athletic Bilbao"), "Athletic Club"),
            Map.entry(canonicalName("Atletico de Madrid"), "Atletico Madrid"),
            Map.entry(canonicalName("Sporting Lisbon"), "Sporting CP"),
            Map.entry(canonicalName("FC Copenhagen"), "FC København"),
            Map.entry(canonicalName("Bayer 04 Leverkusen"), "Bayer Leverkusen"),
            Map.entry(canonicalName("Olympique Lyonnais"), "Lyon"),
            Map.entry(canonicalName("Olympique Marseille"), "Marseille"),
            Map.entry(canonicalName("Paris SG"), "Paris Saint-Germain"),
            Map.entry(canonicalName("PSV"), "PSV Eindhoven"),
            Map.entry(canonicalName("AZ"), "AZ Alkmaar"),
            Map.entry(canonicalName("AC Sparta Praha"), "Sparta Prague"),
            Map.entry(canonicalName("ACF Fiorentina"), "Fiorentina"),
            Map.entry(canonicalName("Aalborg BK"), "AaB"),
            Map.entry(canonicalName("AJ Auxerre"), "Auxerre"),
            Map.entry(canonicalName("Al Jazira Club"), "Al-Jazira"),
            Map.entry(canonicalName("APOEL Nikosia"), "APOEL Nicosia"),
            Map.entry(canonicalName("Atlético Mineiro"), "Atletico MG"),
            Map.entry(canonicalName("Beşiktaş İstanbul JK"), "Beşiktaş"),
            Map.entry(canonicalName("Brondby IF"), "Brøndby IF"),
            Map.entry(canonicalName("BSC Young Boys"), "Young Boys"),
            Map.entry(canonicalName("Corinthians SP"), "Corinthians"),
            Map.entry(canonicalName("CSKA Moskva"), "CSKA Moscow"),
            Map.entry(canonicalName("Deportivo La Coruna"), "Deportivo A Coruña"),
            Map.entry(canonicalName("Dinamo Kiev"), "Dynamo Kyiv"),
            Map.entry(canonicalName("Espérance de Tunis"), "Espérance"),
            Map.entry(canonicalName("FC RB Salzburg"), "Salzburg"),
            Map.entry(canonicalName("Fenerbahçe İstanbul SK"), "Fenerbahçe"),
            Map.entry(canonicalName("Feyenoord Rotterdam"), "Feyenoord"),
            Map.entry(canonicalName("Flamengo RJ"), "Flamengo"),
            Map.entry(canonicalName("Fluminense RJ"), "Fluminense"),
            Map.entry(canonicalName("Galatasaray İstanbul AŞ"), "Galatasaray"),
            Map.entry(canonicalName("Girondins Bordeaux"), "Bordeaux"),
            Map.entry(canonicalName("Grêmio Porto Alegre"), "Gremio"),
            Map.entry(canonicalName("Guangzhou Evergrande"), "Guangzhou FC"),
            Map.entry(canonicalName("HJK Helsinki"), "HJK"),
            Map.entry(canonicalName("Kobenhavn"), "FC København"),
            Map.entry(canonicalName("Koln"), "1. FC Köln"),
            Map.entry(canonicalName("KRC Genk"), "Genk"),
            Map.entry(canonicalName("Legia Warsaw"), "Legia Warszawa"),
            Map.entry(canonicalName("Lille OSC"), "Lille"),
            Map.entry(canonicalName("Lokomotiv Moskva"), "Lokomotiv Moscow"),
            Map.entry(canonicalName("Olympique Lyon"), "Lyon"),
            Map.entry(canonicalName("PAOK FC"), "PAOK Thessaloniki"),
            Map.entry(canonicalName("Partizan Belgrade"), "Partizan Beograd"),
            Map.entry(canonicalName("RB Salzburg"), "Salzburg"),
            Map.entry(canonicalName("Rosenborg BK"), "Rosenborg"),
            Map.entry(canonicalName("RSC Anderlecht"), "Anderlecht"),
            Map.entry(canonicalName("Servette Geneve"), "Servette"),
            Map.entry(canonicalName("Sheriff Tiraspol"), "FC Sheriff"),
            Map.entry(canonicalName("SK Slovan Bratislava"), "Slovan Bratislava"),
            Map.entry(canonicalName("SK Sturm Graz"), "Sturm Graz"),
            Map.entry(canonicalName("SL Benfica"), "Benfica"),
            Map.entry(canonicalName("Slavia Praha"), "Slavia Prague"),
            Map.entry(canonicalName("Sparta Praha"), "Sparta Prague"),
            Map.entry(canonicalName("Spartak Moskva"), "Spartak Moscow"),
            Map.entry(canonicalName("Spartak TAZ Trnava"), "Spartak Trnava"),
            Map.entry(canonicalName("Sporting Braga"), "Braga"),
            Map.entry(canonicalName("SS Lazio"), "Lazio"),
            Map.entry(canonicalName("SSC Napoli"), "Napoli"),
            Map.entry(canonicalName("Stade Reims"), "Reims"),
            Map.entry(canonicalName("Steaua Bucuresti"), "FCSB"),
            Map.entry(canonicalName("UANL Tigres"), "Tigres"),
            Map.entry(canonicalName("Vasco da Gama RJ"), "Vasco da Gama"),
            Map.entry(canonicalName("Wydad AC"), "Wydad Casablanca"),
            Map.entry(canonicalName("United States"), "USA"),
            Map.entry(canonicalName("Turkey"), "Turkiye"),
            Map.entry(canonicalName("Republic of Ireland"), "Ireland"),
            Map.entry(canonicalName("Czech Republic"), "Czechia"),
            Map.entry(canonicalName("West Germany"), "Germany"),
            Map.entry(canonicalName("Soviet Union"), "Russia"),
            Map.entry(canonicalName("Côte d'Ivoire"), "Ivory Coast"),
            Map.entry(canonicalName("Bosnia & Herzegovina"), "Bosnia and Herzegovina"),
            Map.entry(canonicalName("Serbia and Montenegro"), "Serbia"),
            Map.entry(canonicalName("Zaire"), "DR Congo"));

    private static final Map<String, String> FALLBACK_TEAM_NAMES = Map.ofEntries(
            Map.entry(canonicalName("Central Cordoba de Santiago"), "科尔多瓦中央"),
            Map.entry(canonicalName("Central Cordoba de Santiago del Estero"), "科尔多瓦中央"),
            Map.entry(canonicalName("Club Atletico Platense"), "普拉滕斯"),
            Map.entry(canonicalName("Barracas Central"), "巴拉卡斯中央"),
            Map.entry(canonicalName("Instituto"), "科尔多瓦学院"),
            Map.entry(canonicalName("Instituto Atletico Central Cordoba"), "科尔多瓦学院"),
            Map.entry(canonicalName("Deportivo Riestra"), "利斯特雷"),
            Map.entry(canonicalName("Independiente Rivadavia"), "里瓦达维亚独立"),
            Map.entry(canonicalName("Remo"), "瑞模贝雷"),
            Map.entry(canonicalName("Estudiantes de Rio Cuarto"), "里奥夸尔托学生队"),
            Map.entry(canonicalName("Gimnasia Mendoza"), "门多萨体操"),
            Map.entry(canonicalName("Skenderbeu"), "科尔察"),
            Map.entry(canonicalName("RFS"), "里加足校"),
            Map.entry(canonicalName("Rigas Futbola Skola"), "里加足校"),
            Map.entry(canonicalName("Aris Limassol"), "艾里斯利马素尔"),
            Map.entry(canonicalName("Raków Częstochowa"), "琴斯托霍"),
            Map.entry(canonicalName("TSC Backa Topola"), "托波拉"),
            Map.entry(canonicalName("FK TSC"), "托波拉"),
            Map.entry(canonicalName("Pirae"), "皮莱"),
            Map.entry(canonicalName("AS Pirae"), "皮莱"),
            Map.entry(canonicalName("Cuba"), "古巴"),
            Map.entry(canonicalName("Trinidad and Tobago"), "特立尼达和多巴哥"),
            Map.entry(canonicalName("Angola"), "安哥拉"),
            Map.entry(canonicalName("Bulgaria"), "保加利亚"),
            Map.entry(canonicalName("China"), "中国"),
            Map.entry(canonicalName("Czechoslovakia"), "捷克斯洛伐克"),
            Map.entry(canonicalName("Dutch East Indies"), "印度尼西亚"),
            Map.entry(canonicalName("East Germany"), "东德"),
            Map.entry(canonicalName("El Salvador"), "萨尔瓦多"),
            Map.entry(canonicalName("German DR"), "东德"),
            Map.entry(canonicalName("Greece"), "希腊"),
            Map.entry(canonicalName("Honduras"), "洪都拉斯"),
            Map.entry(canonicalName("Indonesia"), "印度尼西亚"),
            Map.entry(canonicalName("Israel"), "以色列"),
            Map.entry(canonicalName("Kuwait"), "科威特"),
            Map.entry(canonicalName("Latvia"), "拉脱维亚"),
            Map.entry(canonicalName("North Korea"), "朝鲜"),
            Map.entry(canonicalName("Togo"), "多哥"),
            Map.entry(canonicalName("United Arab Emirates"), "阿联酋"),
            Map.entry(canonicalName("Yugoslavia"), "南斯拉夫"));

    private static final MappingData MAPPINGS = loadMappings();

    private ClubTeamNameTranslator() {
    }

    public static String translate(String teamName) {
        return translate(null, teamName);
    }

    public static String translate(Competition competition, String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        if (normalized.isBlank() || containsChinese(normalized)) {
            return normalized;
        }

        String mappedName = findMappedName(competition, normalized);
        if (mappedName != null) {
            return mappedName;
        }

        String sourceAlias = SOURCE_NAME_ALIASES.get(canonicalName(normalized));
        if (sourceAlias != null) {
            mappedName = findMappedName(competition, sourceAlias);
            if (mappedName != null) {
                return mappedName;
            }
        }

        return FALLBACK_TEAM_NAMES.getOrDefault(canonicalName(normalized), normalized);
    }

    public static boolean hasHistoricalOddsMapping(String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        if (normalized.isBlank()) {
            return false;
        }
        if (findMappedName(null, normalized) != null) {
            return true;
        }
        String sourceAlias = SOURCE_NAME_ALIASES.get(canonicalName(normalized));
        return sourceAlias != null && findMappedName(null, sourceAlias) != null;
    }

    private static String findMappedName(Competition competition, String teamName) {
        if (competition != null) {
            Map<String, String> competitionMappings = MAPPINGS.byCompetition().get(competition);
            String mappedName = lookup(competitionMappings, teamName);
            if (mappedName != null) {
                return mappedName;
            }
        }
        return lookup(MAPPINGS.global(), teamName);
    }

    private static String lookup(Map<String, String> mappings, String teamName) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        for (String key : nameKeys(teamName)) {
            String mappedName = mappings.get(key);
            if (mappedName != null) {
                return mappedName;
            }
        }
        return null;
    }

    private static MappingData loadMappings() {
        InputStream inputStream = ClubTeamNameTranslator.class.getClassLoader()
                .getResourceAsStream(HISTORICAL_ODDS_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException("找不到历史赔率数据：" + HISTORICAL_ODDS_RESOURCE);
        }

        Map<Competition, Map<String, DatedName>> byCompetition = new EnumMap<>(Competition.class);
        Map<String, DatedName> global = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalStateException("历史赔率数据为空：" + HISTORICAL_ODDS_RESOURCE);
            }
            List<String> headers = CsvUtils.parseLine(headerLine);
            Map<String, Integer> headerIndexes = new LinkedHashMap<>();
            for (int index = 0; index < headers.size(); index++) {
                headerIndexes.put(headers.get(index), index);
            }

            int matchDateColumn = requiredColumn(headerIndexes, "match_date");
            int competitionColumn = requiredColumn(headerIndexes, "competition");
            int homeTeamChineseColumn = requiredColumn(headerIndexes, "home_team_cn");
            int awayTeamChineseColumn = requiredColumn(headerIndexes, "away_team_cn");
            int homeTeamEnglishColumn = requiredColumn(headerIndexes, "home_team_en");
            int awayTeamEnglishColumn = requiredColumn(headerIndexes, "away_team_en");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> row = CsvUtils.parseLine(line);
                Competition competition = Competition.fromCode(CsvUtils.get(row, competitionColumn));
                String matchDate = CsvUtils.get(row, matchDateColumn);
                register(
                        byCompetition,
                        global,
                        competition,
                        CsvUtils.get(row, homeTeamEnglishColumn),
                        CsvUtils.get(row, homeTeamChineseColumn),
                        matchDate);
                register(
                        byCompetition,
                        global,
                        competition,
                        CsvUtils.get(row, awayTeamEnglishColumn),
                        CsvUtils.get(row, awayTeamChineseColumn),
                        matchDate);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("读取历史赔率球队名映射失败：" + HISTORICAL_ODDS_RESOURCE, ex);
        }

        Map<Competition, Map<String, String>> immutableByCompetition = new EnumMap<>(Competition.class);
        for (Map.Entry<Competition, Map<String, DatedName>> entry : byCompetition.entrySet()) {
            immutableByCompetition.put(entry.getKey(), toImmutableNameMap(entry.getValue()));
        }
        return new MappingData(
                Collections.unmodifiableMap(immutableByCompetition),
                toImmutableNameMap(global));
    }

    private static int requiredColumn(Map<String, Integer> headerIndexes, String columnName) {
        Integer index = headerIndexes.get(columnName);
        if (index == null) {
            throw new IllegalStateException("历史赔率数据缺少列：" + columnName);
        }
        return index;
    }

    private static void register(
            Map<Competition, Map<String, DatedName>> byCompetition,
            Map<String, DatedName> global,
            Competition competition,
            String englishName,
            String chineseName,
            String matchDate) {
        if (englishName == null || englishName.isBlank() || chineseName == null || chineseName.isBlank()) {
            return;
        }
        Map<String, DatedName> competitionMappings = byCompetition.computeIfAbsent(
                competition,
                ignored -> new HashMap<>());
        for (String key : nameKeys(englishName)) {
            registerName(competitionMappings, key, chineseName, matchDate);
            registerName(global, key, chineseName, matchDate);
        }
    }

    private static void registerName(
            Map<String, DatedName> mappings,
            String key,
            String chineseName,
            String matchDate) {
        DatedName current = mappings.get(key);
        if (current == null || matchDate.compareTo(current.matchDate()) >= 0) {
            mappings.put(key, new DatedName(chineseName, matchDate));
        }
    }

    private static Map<String, String> toImmutableNameMap(Map<String, DatedName> source) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, DatedName> entry : source.entrySet()) {
            result.put(entry.getKey(), entry.getValue().name());
        }
        return Collections.unmodifiableMap(result);
    }

    private static List<String> nameKeys(String teamName) {
        String canonical = canonicalName(teamName);
        String withoutClubTokens = canonicalClubName(teamName);
        List<String> keys = new ArrayList<>();
        if (!canonical.isBlank()) {
            keys.add(canonical);
        }
        if (!withoutClubTokens.isBlank() && !withoutClubTokens.equals(canonical)) {
            keys.add(withoutClubTokens);
        }
        return keys;
    }

    private static String canonicalName(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private static String canonicalClubName(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("^(AFC|AC|AS|CA|CD|CF|FC|FK|SC|SV|TSG)\\s+", "")
                .replaceAll("\\s+(AFC|AC|AS|CA|CD|CF|FC|FK|SC|SV|TSG)$", "")
                .replace(" ", "");
    }

    private static boolean containsChinese(String value) {
        return value.codePoints().anyMatch(codePoint -> Character.UnicodeScript.of(codePoint)
                == Character.UnicodeScript.HAN);
    }

    private record DatedName(String name, String matchDate) {

    }

    private record MappingData(
            Map<Competition, Map<String, String>> byCompetition,
            Map<String, String> global) {

    }

}
