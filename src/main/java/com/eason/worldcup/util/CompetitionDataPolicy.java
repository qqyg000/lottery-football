package com.eason.worldcup.util;

import java.util.Locale;
import java.util.Set;

public final class CompetitionDataPolicy {

    private static final Set<String> EXCLUDED_SOURCE_COMPETITIONS = Set.of(
            "巴甲",
            "巴乙",
            "巴西乙",
            "巴西乙级联赛",
            "巴西杯",
            "巴东北",
            "巴东北杯",
            "巴西东北杯",
            "圣保罗锦",
            "圣保罗州锦标赛",
            "BRAZIL SERIE A",
            "BRAZIL SERIE B",
            "CAMPEONATO BRASILEIRO SERIE A",
            "CAMPEONATO BRASILEIRO SERIE B",
            "CAMPEONATO PAULISTA",
            "PAULISTA A1",
            "COPA DO BRASIL",
            "COPA DO NORDESTE",
            "挪超",
            "挪威超",
            "ELITESERIEN",
            "NORWEGIAN ELITESERIEN");

    private CompetitionDataPolicy() {
    }

    public static boolean isExcludedSourceCompetition(String sourceCompetition) {
        if (sourceCompetition == null || sourceCompetition.isBlank()) {
            return false;
        }
        String normalized = sourceCompetition.trim().toUpperCase(Locale.ROOT);
        return EXCLUDED_SOURCE_COMPETITIONS.stream().anyMatch(excludedCompetition ->
                normalized.equals(excludedCompetition)
                        || normalized.startsWith(excludedCompetition + " ")
                        || normalized.startsWith(excludedCompetition + "第")
                        || normalized.startsWith(excludedCompetition + "-"));
    }

}
