package com.eason.worldcup.model;

import java.util.Locale;

public enum HistoricalMatchType {

    OFFICIAL("正式比赛", 1.0D),
    INTERNATIONAL_FRIENDLY("国家队国际窗口友谊赛", 0.5D),
    CLUB_FRIENDLY("俱乐部正常阵容友谊赛", 0.3D);

    private final String displayName;

    private final double modelWeight;

    HistoricalMatchType(String displayName, double modelWeight) {
        this.displayName = displayName;
        this.modelWeight = modelWeight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getModelWeight() {
        return modelWeight;
    }

    public static HistoricalMatchType fromCode(String value) {
        if (value == null || value.isBlank()) {
            return OFFICIAL;
        }
        return HistoricalMatchType.valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }

    public static HistoricalMatchType fromCompetition(Competition competition) {
        if (competition == Competition.INTERNATIONAL_FRIENDLY) {
            return INTERNATIONAL_FRIENDLY;
        }
        if (competition == Competition.CLUB_FRIENDLY) {
            return CLUB_FRIENDLY;
        }
        return OFFICIAL;
    }

}
