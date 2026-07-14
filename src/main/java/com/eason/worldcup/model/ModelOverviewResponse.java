package com.eason.worldcup.model;

import java.util.ArrayList;
import java.util.List;

public class ModelOverviewResponse {

    private Competition competition = Competition.WORLD_CUP;

    private String competitionName;

    private int historicalMatchCount;

    private int scheduleMatchCount;

    private int completedMatchCount;

    private double baselineGoals;

    private List<String> scheduleDates = new ArrayList<>();

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition == null ? Competition.WORLD_CUP : competition;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

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
