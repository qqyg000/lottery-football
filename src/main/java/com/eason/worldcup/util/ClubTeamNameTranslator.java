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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ClubTeamNameTranslator {

    private static final String TEAM_NAME_MAPPINGS_RESOURCE = "data/team_name_mappings.csv";

    private static final Map<String, Integer> SOURCE_PRIORITIES = Map.of(
            "HISTORICAL_MATCHES", 1,
            "HISTORICAL_ODDS", 2,
            "ESPN_SCHEDULE", 3,
            "INFERRED_DUPLICATE", 3,
            "VERIFIED_ALIAS", 4,
            "MANUAL", 5,
            "VERIFIED_SPORTTERY", 6);

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
        String currentName = normalized;
        Set<String> visitedNames = new HashSet<>();
        while (visitedNames.add(canonicalName(currentName))) {
            String mappedName = findMappedName(competition, currentName);
            if (mappedName == null || mappedName.equals(currentName)) {
                return currentName;
            }
            currentName = mappedName;
        }
        return currentName;
    }

    public static boolean hasMapping(String teamName) {
        return hasMapping(null, teamName);
    }

    public static boolean hasMapping(Competition competition, String teamName) {
        String normalized = teamName == null ? "" : teamName.trim();
        return !normalized.isBlank() && findMappedName(competition, normalized) != null;
    }

    private static String findMappedName(Competition competition, String teamName) {
        MappingValue competitionMapping = null;
        if (competition != null) {
            NameMappings competitionMappings = MAPPINGS.byCompetition().get(competition);
            competitionMapping = lookup(competitionMappings, teamName);
        }
        MappingValue globalMapping = lookup(MAPPINGS.global(), teamName);
        if (competitionMapping == null) {
            return globalMapping == null ? null : globalMapping.standardName();
        }
        if (globalMapping == null || competitionMapping.priority() >= globalMapping.priority()) {
            return competitionMapping.standardName();
        }
        return globalMapping.standardName();
    }

    private static MappingValue lookup(NameMappings mappings, String teamName) {
        if (mappings == null) {
            return null;
        }
        MappingValue exactMatch = mappings.exact().get(canonicalName(teamName));
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
            int sourceColumn = requiredColumn(headerIndexes, "source");
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
                String source = CsvUtils.get(row, sourceColumn).trim();
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
                register(mappings, aliasName, standardName, source, lineNumber);
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
            String source,
            int lineNumber) {
        MappingValue candidate = new MappingValue(
                standardName,
                SOURCE_PRIORITIES.getOrDefault(source, 0));
        String exactKey = canonicalName(aliasName);
        MappingValue current = mappings.exact.get(exactKey);
        if (current == null || candidate.priority() > current.priority()) {
            mappings.exact.put(exactKey, candidate);
        } else if (candidate.priority() == current.priority()
                && !current.standardName().equals(standardName)) {
            throw new IllegalStateException(
                    "球队名映射冲突，第 " + lineNumber + " 行：" + aliasName + " -> " + standardName
                            + "，已有标准名 " + current.standardName());
        }
        String clubKey = canonicalClubName(aliasName);
        if (!clubKey.isBlank()) {
            MappingValue clubCurrent = mappings.withoutClubTokens.get(clubKey);
            if (clubCurrent == null || candidate.priority() > clubCurrent.priority()) {
                mappings.withoutClubTokens.put(clubKey, candidate);
            }
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
            Map<String, MappingValue> exact,
            Map<String, MappingValue> withoutClubTokens) {

    }

    private record MappingValue(String standardName, int priority) {

    }

    private static class MutableNameMappings {

        private final Map<String, MappingValue> exact = new HashMap<>();

        private final Map<String, MappingValue> withoutClubTokens = new HashMap<>();

        private NameMappings toImmutable() {
            return new NameMappings(
                    Collections.unmodifiableMap(new HashMap<>(exact)),
                    Collections.unmodifiableMap(new HashMap<>(withoutClubTokens)));
        }

    }

}
