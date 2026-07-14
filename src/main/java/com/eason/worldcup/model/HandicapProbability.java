package com.eason.worldcup.model;

public class HandicapProbability {

    private int handicap;

    private String handicapName;

    private ThreeWayProbability probability;

    public int getHandicap() {
        return handicap;
    }

    public void setHandicap(int handicap) {
        this.handicap = handicap;
    }

    public String getHandicapName() {
        return handicapName;
    }

    public void setHandicapName(String handicapName) {
        this.handicapName = handicapName;
    }

    public ThreeWayProbability getProbability() {
        return probability;
    }

    public void setProbability(ThreeWayProbability probability) {
        this.probability = probability;
    }

}
