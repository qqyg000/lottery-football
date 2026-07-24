package com.eason.worldcup.model;

import com.eason.worldcup.util.ApplicationTime;

import java.time.LocalDate;
import java.util.Locale;

public enum Competition {

    WORLD_CUP("世界杯", false, false),
    EUROPEAN_CHAMPIONSHIP("欧洲杯", false, false),
    COPA_AMERICA("美洲杯", false, false),
    INTERNATIONAL_OFFICIAL("国家队其他正式比赛", false, false),
    INTERNATIONAL_FRIENDLY("国家队国际窗口友谊赛", false, false),
    CLUB_WORLD_CUP("世俱杯", true, false),
    CLUB_OFFICIAL_OTHER("俱乐部其他正式比赛", true, false),
    CLUB_FRIENDLY("俱乐部正常阵容友谊赛", true, false),
    EUROPA_LEAGUE("欧罗巴", true, true),
    CHAMPIONS_LEAGUE("欧冠", true, true),
    PREMIER_LEAGUE("英超", true, true),
    LA_LIGA("西甲", true, true),
    SERIE_A("意甲", true, true),
    BUNDESLIGA("德甲", true, true),
    LIGUE_1("法甲", true, true),
    PRIMEIRA_LIGA("葡超", true, true),
    EREDIVISIE("荷甲", true, true),
    ARGENTINE_PRIMERA_DIVISION("阿甲", true, false),
    SWEDISH_ALLSVENSKAN("瑞超", true, false),
    FINNISH_VEIKKAUSLIIGA("芬超", true, false),
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
        return switch (normalized) {
            case "UCL", "CHAMPIONSLEAGUE" -> CHAMPIONS_LEAGUE;
            case "EURO", "UEFA_EURO" -> EUROPEAN_CHAMPIONSHIP;
            case "COPAAMERICA" -> COPA_AMERICA;
            case "FIFA_CWC" -> CLUB_WORLD_CUP;
            case "EPL", "PREMIERLEAGUE" -> PREMIER_LEAGUE;
            case "LALIGA" -> LA_LIGA;
            case "SERIEA" -> SERIE_A;
            case "LIGUE1" -> LIGUE_1;
            case "PRIMEIRALIGA" -> PRIMEIRA_LIGA;
            case "ARGENTINEPRIMERADIVISION", "ARGENTINA_PRIMERA_DIVISION" ->
                    ARGENTINE_PRIMERA_DIVISION;
            case "ALLSVENSKAN", "SWEDISHALLSVENSKAN" -> SWEDISH_ALLSVENSKAN;
            case "VEIKKAUSLIIGA", "FINNISHVEIKKAUSLIIGA" -> FINNISH_VEIKKAUSLIIGA;
            case "KLEAGUE", "KLEAGUE1" -> K_LEAGUE_1;
            default -> Competition.valueOf(normalized);
        };
    }

    public static Competition fromSourceCompetition(String sourceCompetition, Competition fallback) {
        String normalized = sourceCompetition == null ? "" : sourceCompetition.trim();
        if (normalized.startsWith("瑞超") || normalized.startsWith("瑞典超")) {
            return SWEDISH_ALLSVENSKAN;
        }
        if (normalized.startsWith("芬超")) {
            return FINNISH_VEIKKAUSLIIGA;
        }
        if (normalized.startsWith("韩职") || normalized.startsWith("韩国职业联赛")) {
            return K_LEAGUE_1;
        }
        return fallback;
    }

}
