package com.eason.worldcup.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserConfig {

    public static final String CURRENT_EDITION_PROFILE = "CURRENT";

    public static final String PREVIOUS_EDITION_PROFILE = "PREVIOUS";

    public static final String STABLE_PARAMETER_PRESET = "STABLE";

    public static final String AGGRESSIVE_PARAMETER_PRESET = "AGGRESSIVE";

    private static final List<String> PARAMETER_PROFILE_RANGES = List.of(
            CURRENT_EDITION_PROFILE,
            PREVIOUS_EDITION_PROFILE);

    private static final List<String> PARAMETER_PRESETS = List.of(
            STABLE_PARAMETER_PRESET,
            AGGRESSIVE_PARAMETER_PRESET);

    private static final List<Competition> PARAMETER_COMPETITIONS = List.of(
            Competition.WORLD_CUP,
            Competition.EUROPEAN_CHAMPIONSHIP,
            Competition.COPA_AMERICA,
            Competition.CLUB_WORLD_CUP,
            Competition.EUROPA_LEAGUE,
            Competition.CHAMPIONS_LEAGUE,
            Competition.PREMIER_LEAGUE,
            Competition.LA_LIGA,
            Competition.SERIE_A,
            Competition.BUNDESLIGA,
            Competition.LIGUE_1,
            Competition.BRAZIL_SERIE_A,
            Competition.PRIMEIRA_LIGA,
            Competition.EREDIVISIE,
            Competition.ARGENTINE_PRIMERA_DIVISION);

    private String modelMode = "after";

    private boolean includePreviousEdition;

    private ModelFactors modelFactors = ModelFactors.defaults();

    private GlobalParameters globalParameters = GlobalParameters.defaults();

    private Map<String, ParameterProfile> parameterProfiles = new LinkedHashMap<>();

    private Map<String, RecommendationSelection> selectedRows = new LinkedHashMap<>();

    public static List<Competition> getParameterCompetitions() {
        return PARAMETER_COMPETITIONS;
    }

    public static List<String> getParameterProfileRanges() {
        return PARAMETER_PROFILE_RANGES;
    }

    public static List<String> getParameterPresets() {
        return PARAMETER_PRESETS;
    }

    public static String parameterProfileKey(Competition competition, boolean includePreviousEdition) {
        return parameterProfileKey(
                competition,
                includePreviousEdition ? PREVIOUS_EDITION_PROFILE : CURRENT_EDITION_PROFILE,
                STABLE_PARAMETER_PRESET);
    }

    public static String parameterProfileKey(Competition competition, String profileRange) {
        return parameterProfileKey(competition, profileRange, STABLE_PARAMETER_PRESET);
    }

    public static String parameterProfileKey(
            Competition competition,
            String profileRange,
            String parameterPreset) {
        return competition.name() + ":" + profileRange + ":" + parameterPreset;
    }

    public static String legacyParameterProfileKey(Competition competition, String profileRange) {
        return competition.name() + ":" + profileRange;
    }

    public String getModelMode() {
        return modelMode;
    }

    public void setModelMode(String modelMode) {
        this.modelMode = modelMode;
    }

    public boolean isIncludePreviousEdition() {
        return includePreviousEdition;
    }

    public void setIncludePreviousEdition(boolean includePreviousEdition) {
        this.includePreviousEdition = includePreviousEdition;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public ModelFactors getModelFactors() {
        return modelFactors;
    }

    public void setModelFactors(ModelFactors modelFactors) {
        this.modelFactors = modelFactors;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public GlobalParameters getGlobalParameters() {
        return globalParameters;
    }

    public void setGlobalParameters(GlobalParameters globalParameters) {
        this.globalParameters = globalParameters;
    }

    public Map<String, ParameterProfile> getParameterProfiles() {
        return parameterProfiles;
    }

    public void setParameterProfiles(Map<String, ParameterProfile> parameterProfiles) {
        this.parameterProfiles = parameterProfiles;
    }

    public Map<String, RecommendationSelection> getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(Map<String, RecommendationSelection> selectedRows) {
        this.selectedRows = selectedRows;
    }

    public static class ParameterProfile {

        private ModelFactors modelFactors = ModelFactors.defaults();

        private GlobalParameters globalParameters = GlobalParameters.defaults();

        public static ParameterProfile defaults() {
            return of(ModelFactors.defaults(), GlobalParameters.defaults());
        }

        public static ParameterProfile aggressiveDefaults() {
            return of(ModelFactors.aggressiveDefaults(), GlobalParameters.aggressiveDefaults());
        }

        public static ParameterProfile of(ModelFactors modelFactors, GlobalParameters globalParameters) {
            ParameterProfile profile = new ParameterProfile();
            profile.setModelFactors(modelFactors);
            profile.setGlobalParameters(globalParameters);
            return profile;
        }

        public ModelFactors getModelFactors() {
            return modelFactors;
        }

        public void setModelFactors(ModelFactors modelFactors) {
            this.modelFactors = modelFactors;
        }

        public GlobalParameters getGlobalParameters() {
            return globalParameters;
        }

        public void setGlobalParameters(GlobalParameters globalParameters) {
            this.globalParameters = globalParameters;
        }

    }

    public static class GlobalParameters {

        private Double recommendationOdds;

        private Double handicapRecommendationThreshold;

        private Double handicapReverseThreshold;

        private Double singleRecommendationThreshold;

        public static GlobalParameters defaults() {
            GlobalParameters parameters = new GlobalParameters();
            parameters.setRecommendationOdds(1.03D);
            parameters.setHandicapRecommendationThreshold(68.16D);
            parameters.setHandicapReverseThreshold(46.78D);
            parameters.setSingleRecommendationThreshold(71.72D);
            return parameters;
        }

        public static GlobalParameters aggressiveDefaults() {
            GlobalParameters parameters = new GlobalParameters();
            parameters.setRecommendationOdds(2.46D);
            parameters.setHandicapRecommendationThreshold(89.09D);
            parameters.setHandicapReverseThreshold(43.41D);
            parameters.setSingleRecommendationThreshold(78.71D);
            return parameters;
        }

        public Double getRecommendationOdds() {
            return recommendationOdds;
        }

        public void setRecommendationOdds(Double recommendationOdds) {
            this.recommendationOdds = recommendationOdds;
        }

        public Double getHandicapRecommendationThreshold() {
            return handicapRecommendationThreshold;
        }

        public void setHandicapRecommendationThreshold(Double handicapRecommendationThreshold) {
            this.handicapRecommendationThreshold = handicapRecommendationThreshold;
        }

        public Double getHandicapReverseThreshold() {
            return handicapReverseThreshold;
        }

        public void setHandicapReverseThreshold(Double handicapReverseThreshold) {
            this.handicapReverseThreshold = handicapReverseThreshold;
        }

        public Double getSingleRecommendationThreshold() {
            return singleRecommendationThreshold;
        }

        public void setSingleRecommendationThreshold(Double singleRecommendationThreshold) {
            this.singleRecommendationThreshold = singleRecommendationThreshold;
        }

    }

    public static class ModelFactors {

        private Double hostTeamGoalFactor;

        private Double homeTeamGoalFactor;

        private Double seedTeamGoalFactor;

        private Double officialMatchWeight;

        private Double internationalFriendlyWeight;

        private Double clubFriendlyWeight;

        private Double handicapSmoothingFactor;

        public static ModelFactors defaults() {
            ModelFactors factors = new ModelFactors();
            factors.setHostTeamGoalFactor(1.10D);
            factors.setHomeTeamGoalFactor(1.06D);
            factors.setSeedTeamGoalFactor(1.85D);
            factors.setOfficialMatchWeight(1.00D);
            factors.setInternationalFriendlyWeight(0.50D);
            factors.setClubFriendlyWeight(0.30D);
            factors.setHandicapSmoothingFactor(0.274D);
            return factors;
        }

        public static ModelFactors aggressiveDefaults() {
            ModelFactors factors = new ModelFactors();
            factors.setHostTeamGoalFactor(2.30D);
            factors.setHomeTeamGoalFactor(1.75D);
            factors.setSeedTeamGoalFactor(1.55D);
            factors.setOfficialMatchWeight(1.00D);
            factors.setInternationalFriendlyWeight(0.50D);
            factors.setClubFriendlyWeight(0.30D);
            factors.setHandicapSmoothingFactor(0.650D);
            return factors;
        }

        public Double getHostTeamGoalFactor() {
            return hostTeamGoalFactor;
        }

        public void setHostTeamGoalFactor(Double hostTeamGoalFactor) {
            this.hostTeamGoalFactor = hostTeamGoalFactor;
        }

        public Double getHomeTeamGoalFactor() {
            return homeTeamGoalFactor;
        }

        public void setHomeTeamGoalFactor(Double homeTeamGoalFactor) {
            this.homeTeamGoalFactor = homeTeamGoalFactor;
        }

        public Double getSeedTeamGoalFactor() {
            return seedTeamGoalFactor;
        }

        public void setSeedTeamGoalFactor(Double seedTeamGoalFactor) {
            this.seedTeamGoalFactor = seedTeamGoalFactor;
        }

        public Double getOfficialMatchWeight() {
            return officialMatchWeight;
        }

        public void setOfficialMatchWeight(Double officialMatchWeight) {
            this.officialMatchWeight = officialMatchWeight;
        }

        public Double getInternationalFriendlyWeight() {
            return internationalFriendlyWeight;
        }

        public void setInternationalFriendlyWeight(Double internationalFriendlyWeight) {
            this.internationalFriendlyWeight = internationalFriendlyWeight;
        }

        public Double getClubFriendlyWeight() {
            return clubFriendlyWeight;
        }

        public void setClubFriendlyWeight(Double clubFriendlyWeight) {
            this.clubFriendlyWeight = clubFriendlyWeight;
        }

        public Double getHandicapSmoothingFactor() {
            return handicapSmoothingFactor;
        }

        public void setHandicapSmoothingFactor(Double handicapSmoothingFactor) {
            this.handicapSmoothingFactor = handicapSmoothingFactor;
        }

    }

    public static class RecommendationSelection {

        private Boolean manualOverride;

        private Boolean normal;

        private String handicap;

        public Boolean getManualOverride() {
            return manualOverride;
        }

        public void setManualOverride(Boolean manualOverride) {
            this.manualOverride = manualOverride;
        }

        public Boolean getNormal() {
            return normal;
        }

        public void setNormal(Boolean normal) {
            this.normal = normal;
        }

        public String getHandicap() {
            return handicap;
        }

        public void setHandicap(String handicap) {
            this.handicap = handicap;
        }

    }

}
