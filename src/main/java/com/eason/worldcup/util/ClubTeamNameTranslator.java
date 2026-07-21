package com.eason.worldcup.util;

import com.eason.worldcup.model.Competition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClubTeamNameTranslator {

    private static final String TEAM_NAME_MAPPINGS_RESOURCE = "data/team_name_mappings.csv";

    private static final MappingData MAPPINGS = loadMappings();

    private ClubTeamNameTranslator() {
    }

    public static String translate(String teamName) {
        return translate(null, teamName);
    }

    public static String translate(Competition competition, String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        if (normalized.isBlank()) {
            return normalized;
        }
        String mappedName = findMappedName(competition, normalized);
        return mappedName == null ? normalized : mappedName;
    }

    public static boolean hasMapping(String teamName) {
        return hasMapping(null, teamName);
    }

    public static boolean hasMapping(Competition competition, String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        return !normalized.isBlank() && findMappedName(competition, normalized) != null;
    }

    private static String findMappedName(Competition competition, String teamName) {
        if (competition != null) {
            NameMappings competitionMappings = MAPPINGS.byCompetition().get(competition);
            String mappedName = lookup(competitionMappings, teamName);
            if (mappedName != null) {
                return mappedName;
            }
        }
        return lookup(MAPPINGS.global(), teamName);
    }

    private static String lookup(NameMappings mappings, String teamName) {
        if (mappings == null) {
            return null;
        }
        String exactMatch = mappings.exact().get(canonicalName(teamName));
        if (exactMatch != null) {
            return exactMatch;
        }
        return mappings.withoutClubTokens().get(canonicalClubName(teamName));
    }

    private static MappingData loadMappings() {
        InputStream inputStream = ClubTeamNameTranslator.class.getClassLoader()
                .getResourceAsStream(TEAM_NAME_MAPPINGS_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException("找不到球队名映射：" + TEAM_NAME_MAPPINGS_RESOURCE);
        }

        Map<Competition, MutableNameMappings> byCompetition = new EnumMap<>(Competition.class);
        MutableNameMappings global = new MutableNameMappings();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalStateException("球队名映射为空：" + TEAM_NAME_MAPPINGS_RESOURCE);
            }
            List<String> headers = CsvUtils.parseLine(headerLine);
            Map<String, Integer> headerIndexes = new LinkedHashMap<>();
            for (int index = 0; index < headers.size(); index++) {
                headerIndexes.put(headers.get(index), index);
            }

            int competitionColumn = requiredColumn(headerIndexes, "competition");
            int standardNameColumn = requiredColumn(headerIndexes, "standard_team_name");
            int aliasNameColumn = requiredColumn(headerIndexes, "alias_team_name");
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> row = CsvUtils.parseLine(line);
                String competitionCode = CsvUtils.get(row, competitionColumn).trim();
                String standardName = CsvUtils.get(row, standardNameColumn).trim();
                String aliasName = CsvUtils.get(row, aliasNameColumn).trim();
                if (standardName.isBlank() || aliasName.isBlank()) {
                    throw new IllegalStateException("球队名映射存在空名称，第 " + lineNumber + " 行");
                }
                MutableNameMappings mappings;
                if ("*".equals(competitionCode)) {
                    mappings = global;
                } else {
                    Competition competition = Competition.fromCode(competitionCode);
                    mappings = byCompetition.computeIfAbsent(
                            competition,
                            ignored -> new MutableNameMappings());
                }
                register(mappings, aliasName, standardName, lineNumber);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("读取球队名映射失败：" + TEAM_NAME_MAPPINGS_RESOURCE, ex);
        }

        Map<Competition, NameMappings> immutableByCompetition = new EnumMap<>(Competition.class);
        for (Map.Entry<Competition, MutableNameMappings> entry : byCompetition.entrySet()) {
            immutableByCompetition.put(entry.getKey(), entry.getValue().toImmutable());
        }
        return new MappingData(
                Collections.unmodifiableMap(immutableByCompetition),
                global.toImmutable());
    }

    private static int requiredColumn(Map<String, Integer> headerIndexes, String columnName) {
        Integer index = headerIndexes.get(columnName);
        if (index == null) {
            throw new IllegalStateException("球队名映射缺少列：" + columnName);
        }
        return index;
    }

    private static void register(
            MutableNameMappings mappings,
            String aliasName,
            String standardName,
            int lineNumber) {
        String exactKey = canonicalName(aliasName);
        String current = mappings.exact.putIfAbsent(exactKey, standardName);
        if (current != null && !current.equals(standardName)) {
            throw new IllegalStateException(
                    "球队名映射冲突，第 " + lineNumber + " 行：" + aliasName + " -> " + standardName
                            + "，已有标准名 " + current);
        }
        String clubKey = canonicalClubName(aliasName);
        if (!clubKey.isBlank()) {
            mappings.withoutClubTokens.putIfAbsent(clubKey, standardName);
        }
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

    private record MappingData(
            Map<Competition, NameMappings> byCompetition,
            NameMappings global) {

    }

    private record NameMappings(
            Map<String, String> exact,
            Map<String, String> withoutClubTokens) {

    }

    private static class MutableNameMappings {

        private final Map<String, String> exact = new HashMap<>();

        private final Map<String, String> withoutClubTokens = new HashMap<>();

        private NameMappings toImmutable() {
            return new NameMappings(
                    Collections.unmodifiableMap(new HashMap<>(exact)),
                    Collections.unmodifiableMap(new HashMap<>(withoutClubTokens)));
        }

    }

}
