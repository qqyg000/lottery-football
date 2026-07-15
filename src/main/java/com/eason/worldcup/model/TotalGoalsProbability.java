package com.eason.worldcup.model;

public class TotalGoalsProbability {

    private int totalGoals;

    private double probability;

    public TotalGoalsProbability() {
    }

    public TotalGoalsProbability(int totalGoals, double probability) {
        this.totalGoals = totalGoals;
        this.probability = probability;
    }

    public int getTotalGoals() {
        return totalGoals;
    }

    public void setTotalGoals(int totalGoals) {
        this.totalGoals = totalGoals;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

}
