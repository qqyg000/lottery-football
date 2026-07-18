package com.eason.worldcup.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class HeadToHeadMatchResponse {

    private LocalDate matchDate;

    private LocalTime kickoffTime;

    private String competitionName;

    private String matchTypeName;

    private double modelWeight;

    private String homeTeamCn;

    private String awayTeamCn;

    private int homeScore;

    private int awayScore;

    private boolean neutral;

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public LocalTime getKickoffTime() {
        return kickoffTime;
    }

    public void setKickoffTime(LocalTime kickoffTime) {
        this.kickoffTime = kickoffTime;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getMatchTypeName() {
        return matchTypeName;
    }

    public void setMatchTypeName(String matchTypeName) {
        this.matchTypeName = matchTypeName;
    }

    public double getModelWeight() {
        return modelWeight;
    }

    public void setModelWeight(double modelWeight) {
        this.modelWeight = modelWeight;
    }

    public String getHomeTeamCn() {
        return homeTeamCn;
    }

    public void setHomeTeamCn(String homeTeamCn) {
        this.homeTeamCn = homeTeamCn;
    }

    public String getAwayTeamCn() {
        return awayTeamCn;
    }

    public void setAwayTeamCn(String awayTeamCn) {
        this.awayTeamCn = awayTeamCn;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public void setNeutral(boolean neutral) {
        this.neutral = neutral;
    }

}
