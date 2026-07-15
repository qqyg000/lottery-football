package com.eason.worldcup.model;

import java.util.ArrayList;
import java.util.List;

public class RecommendationBacktestResponse {

    private int completedMatchCount;

    private int sportteryCompletedMatchCount;

    private int oddsMatchCount;

    private List<MatchPredictionResponse> matches = new ArrayList<>();

    public int getCompletedMatchCount() {
        return completedMatchCount;
    }

    public void setCompletedMatchCount(int completedMatchCount) {
        this.completedMatchCount = completedMatchCount;
    }

    public int getSportteryCompletedMatchCount() {
        return sportteryCompletedMatchCount;
    }

    public void setSportteryCompletedMatchCount(int sportteryCompletedMatchCount) {
        this.sportteryCompletedMatchCount = sportteryCompletedMatchCount;
    }

    public int getOddsMatchCount() {
        return oddsMatchCount;
    }

    public void setOddsMatchCount(int oddsMatchCount) {
        this.oddsMatchCount = oddsMatchCount;
    }

    public List<MatchPredictionResponse> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchPredictionResponse> matches) {
        this.matches = matches;
    }

}
