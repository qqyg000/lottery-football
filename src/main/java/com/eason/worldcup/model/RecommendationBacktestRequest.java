package com.eason.worldcup.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecommendationBacktestRequest {

    private Map<String, UserConfig.ModelFactors> modelFactorsByCompetition = new LinkedHashMap<>();

    public Map<String, UserConfig.ModelFactors> getModelFactorsByCompetition() {
        return modelFactorsByCompetition;
    }

    public void setModelFactorsByCompetition(Map<String, UserConfig.ModelFactors> modelFactorsByCompetition) {
        this.modelFactorsByCompetition = modelFactorsByCompetition;
    }

}
