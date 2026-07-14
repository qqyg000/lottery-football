package com.eason.worldcup.model;

public class ScoreProbability {

    private int homeScore;

    private int awayScore;

    private double probability;

    public ScoreProbability() {
    }

    public ScoreProbability(int homeScore, int awayScore, double probability) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.probability = probability;
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

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

}
