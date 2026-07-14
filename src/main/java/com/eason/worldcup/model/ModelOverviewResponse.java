package com.eason.worldcup.model;

import java.util.ArrayList;
import java.util.List;

public class ModelOverviewResponse {

    private int historicalMatchCount;

    private int scheduleMatchCount;

    private int completedMatchCount;

    private double baselineGoals;

    private List<String> scheduleDates = new ArrayList<>();

    public int getHistoricalMatchCount() {
        return historicalMatchCount;
    }

    public void setHistoricalMatchCount(int historicalMatchCount) {
        this.historicalMatchCount = historicalMatchCount;
    }

    public int getScheduleMatchCount() {
        return scheduleMatchCount;
    }

    public void setScheduleMatchCount(int scheduleMatchCount) {
        this.scheduleMatchCount = scheduleMatchCount;
    }

    public int getCompletedMatchCount() {
        return completedMatchCount;
    }

    public void setCompletedMatchCount(int completedMatchCount) {
        this.completedMatchCount = completedMatchCount;
    }

    public double getBaselineGoals() {
        return baselineGoals;
    }

    public void setBaselineGoals(double baselineGoals) {
        this.baselineGoals = baselineGoals;
    }

    public List<String> getScheduleDates() {
        return scheduleDates;
    }

    public void setScheduleDates(List<String> scheduleDates) {
        this.scheduleDates = scheduleDates;
    }

}
