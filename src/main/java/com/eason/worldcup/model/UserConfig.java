package com.eason.worldcup.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserConfig {

    private ModelFactors modelFactors = ModelFactors.defaults();

    private Map<String, RecommendationSelection> selectedRows = new LinkedHashMap<>();

    public ModelFactors getModelFactors() {
        return modelFactors;
    }

    public void setModelFactors(ModelFactors modelFactors) {
        this.modelFactors = modelFactors;
    }

    public Map<String, RecommendationSelection> getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(Map<String, RecommendationSelection> selectedRows) {
        this.selectedRows = selectedRows;
    }

    public static class ModelFactors {

        private Double baseMatchWeight;

        private Double recentHalfYearBonus;

        private Double worldCupBonus;

        private Double hostTeamGoalFactor;

        private Double seedTeamGoalFactor;

        private Double handicapSmoothingFactor;

        public static ModelFactors defaults() {
            ModelFactors factors = new ModelFactors();
            factors.setBaseMatchWeight(1.0D);
            factors.setRecentHalfYearBonus(0.10D);
            factors.setWorldCupBonus(0.25D);
            factors.setHostTeamGoalFactor(1.42D);
            factors.setSeedTeamGoalFactor(1.77D);
            factors.setHandicapSmoothingFactor(0.185D);
            return factors;
        }

        public Double getBaseMatchWeight() {
            return baseMatchWeight;
        }

        public void setBaseMatchWeight(Double baseMatchWeight) {
            this.baseMatchWeight = baseMatchWeight;
        }

        public Double getRecentHalfYearBonus() {
            return recentHalfYearBonus;
        }

        public void setRecentHalfYearBonus(Double recentHalfYearBonus) {
            this.recentHalfYearBonus = recentHalfYearBonus;
        }

        public Double getWorldCupBonus() {
            return worldCupBonus;
        }

        public void setWorldCupBonus(Double worldCupBonus) {
            this.worldCupBonus = worldCupBonus;
        }

        public Double getHostTeamGoalFactor() {
            return hostTeamGoalFactor;
        }

        public void setHostTeamGoalFactor(Double hostTeamGoalFactor) {
            this.hostTeamGoalFactor = hostTeamGoalFactor;
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

        private Boolean normal;

        private String handicap;

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
