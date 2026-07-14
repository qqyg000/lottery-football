package com.eason.worldcup.model;

public class TeamStrength {

    private String teamName;

    private double attackStrength;

    private double defenseWeakness;

    private double averageGoalsFor;

    private double averageGoalsAgainst;

    private double sampleWeight;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public double getAttackStrength() {
        return attackStrength;
    }

    public void setAttackStrength(double attackStrength) {
        this.attackStrength = attackStrength;
    }

    public double getDefenseWeakness() {
        return defenseWeakness;
    }

    public void setDefenseWeakness(double defenseWeakness) {
        this.defenseWeakness = defenseWeakness;
    }

    public double getAverageGoalsFor() {
        return averageGoalsFor;
    }

    public void setAverageGoalsFor(double averageGoalsFor) {
        this.averageGoalsFor = averageGoalsFor;
    }

    public double getAverageGoalsAgainst() {
        return averageGoalsAgainst;
    }

    public void setAverageGoalsAgainst(double averageGoalsAgainst) {
        this.averageGoalsAgainst = averageGoalsAgainst;
    }

    public double getSampleWeight() {
        return sampleWeight;
    }

    public void setSampleWeight(double sampleWeight) {
        this.sampleWeight = sampleWeight;
    }

    public static TeamStrength defaultOf(String teamName, double baselineGoals) {
        TeamStrength strength = new TeamStrength();
        strength.setTeamName(teamName);
        strength.setAttackStrength(1.0D);
        strength.setDefenseWeakness(1.0D);
        strength.setAverageGoalsFor(baselineGoals);
        strength.setAverageGoalsAgainst(baselineGoals);
        strength.setSampleWeight(0.0D);
        return strength;
    }

}
