package com.eason.worldcup.model;

import java.util.Locale;

public enum Competition {

    WORLD_CUP("世界杯", false, false),
    CHAMPIONS_LEAGUE("欧冠", true, true),
    NORWEGIAN_ELITESERIEN("挪超", true, false),
    SWEDISH_ALLSVENSKAN("瑞超", true, false),
    FINNISH_VEIKKAUSLIIGA("芬超", true, false),
    EUROPA_LEAGUE("欧罗巴", true, true),
    BRAZIL_SERIE_A("巴甲", true, false),
    MLS("美职", true, false),
    K_LEAGUE_1("韩职", true, false);

    private final String displayName;

    private final boolean clubCompetition;

    private final boolean crossYearSeason;

    Competition(String displayName, boolean clubCompetition, boolean crossYearSeason) {
        this.displayName = displayName;
        this.clubCompetition = clubCompetition;
        this.crossYearSeason = crossYearSeason;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isClubCompetition() {
        return clubCompetition;
    }

    public boolean isCrossYearSeason() {
        return crossYearSeason;
    }

    public static Competition fromCode(String value) {
        if (value == null || value.isBlank()) {
            return WORLD_CUP;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if ("UCL".equals(normalized) || "CHAMPIONSLEAGUE".equals(normalized)) {
            return CHAMPIONS_LEAGUE;
        }
        return Competition.valueOf(normalized);
    }

}
