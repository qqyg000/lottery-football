package com.eason.worldcup.model;

import com.eason.worldcup.util.ApplicationTime;

import java.time.LocalDate;
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

    public LocalDate getSeasonStartDate(LocalDate referenceDate) {
        LocalDate effectiveDate = referenceDate == null ? ApplicationTime.today() : referenceDate;
        if (!crossYearSeason) {
            return LocalDate.of(effectiveDate.getYear(), 1, 1);
        }
        int seasonStartYear = effectiveDate.getMonthValue() >= 7
                ? effectiveDate.getYear()
                : effectiveDate.getYear() - 1;
        return LocalDate.of(seasonStartYear, 7, 1);
    }

    public boolean isDateInSeason(LocalDate matchDate, LocalDate referenceDate) {
        if (matchDate == null) {
            return false;
        }
        if (!clubCompetition) {
            return true;
        }
        LocalDate seasonStartDate = getSeasonStartDate(referenceDate);
        return !matchDate.isBefore(seasonStartDate)
                && matchDate.isBefore(seasonStartDate.plusYears(1));
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
