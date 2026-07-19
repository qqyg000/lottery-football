package com.eason.worldcup.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserConfig {

    private String modelMode = "after";

    private boolean includePreviousEdition;

    private ModelFactors modelFactors = ModelFactors.defaults();

    private GlobalParameters globalParameters = GlobalParameters.defaults();

    private Map<String, RecommendationSelection> selectedRows = new LinkedHashMap<>();

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

    public Map<String, RecommendationSelection> getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(Map<String, RecommendationSelection> selectedRows) {
        this.selectedRows = selectedRows;
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

        private Double handicapSmoothingFactor;

        public static ModelFactors defaults() {
            ModelFactors factors = new ModelFactors();
            factors.setHostTeamGoalFactor(1.10D);
            factors.setHomeTeamGoalFactor(1.06D);
            factors.setSeedTeamGoalFactor(1.85D);
            factors.setHandicapSmoothingFactor(0.274D);
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
