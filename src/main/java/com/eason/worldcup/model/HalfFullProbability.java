package com.eason.worldcup.model;

public class HalfFullProbability {

    private String halfTimeResult;

    private String fullTimeResult;

    private String label;

    private double probability;

    public HalfFullProbability() {
    }

    public HalfFullProbability(String halfTimeResult, String fullTimeResult, double probability) {
        this.halfTimeResult = halfTimeResult;
        this.fullTimeResult = fullTimeResult;
        this.label = halfTimeResult + fullTimeResult;
        this.probability = probability;
    }

    public String getHalfTimeResult() {
        return halfTimeResult;
    }

    public void setHalfTimeResult(String halfTimeResult) {
        this.halfTimeResult = halfTimeResult;
    }

    public String getFullTimeResult() {
        return fullTimeResult;
    }

    public void setFullTimeResult(String fullTimeResult) {
        this.fullTimeResult = fullTimeResult;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

}
