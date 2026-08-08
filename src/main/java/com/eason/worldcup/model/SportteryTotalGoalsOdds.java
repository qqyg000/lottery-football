package com.eason.worldcup.model;

public class SportteryTotalGoalsOdds {

    private Double goal0;

    private Double goal1;

    private Double goal2;

    private Double goal3;

    private Double goal4;

    private Double goal5;

    private Double goal6;

    private Double goal7Plus;

    private String updatedAt;

    public SportteryTotalGoalsOdds() {
    }

    public SportteryTotalGoalsOdds(
            Double goal0,
            Double goal1,
            Double goal2,
            Double goal3,
            Double goal4,
            Double goal5,
            Double goal6,
            Double goal7Plus,
            String updatedAt) {
        this.goal0 = goal0;
        this.goal1 = goal1;
        this.goal2 = goal2;
        this.goal3 = goal3;
        this.goal4 = goal4;
        this.goal5 = goal5;
        this.goal6 = goal6;
        this.goal7Plus = goal7Plus;
        this.updatedAt = updatedAt;
    }

    public Double getGoal0() {
        return goal0;
    }

    public void setGoal0(Double goal0) {
        this.goal0 = goal0;
    }

    public Double getGoal1() {
        return goal1;
    }

    public void setGoal1(Double goal1) {
        this.goal1 = goal1;
    }

    public Double getGoal2() {
        return goal2;
    }

    public void setGoal2(Double goal2) {
        this.goal2 = goal2;
    }

    public Double getGoal3() {
        return goal3;
    }

    public void setGoal3(Double goal3) {
        this.goal3 = goal3;
    }

    public Double getGoal4() {
        return goal4;
    }

    public void setGoal4(Double goal4) {
        this.goal4 = goal4;
    }

    public Double getGoal5() {
        return goal5;
    }

    public void setGoal5(Double goal5) {
        this.goal5 = goal5;
    }

    public Double getGoal6() {
        return goal6;
    }

    public void setGoal6(Double goal6) {
        this.goal6 = goal6;
    }

    public Double getGoal7Plus() {
        return goal7Plus;
    }

    public void setGoal7Plus(Double goal7Plus) {
        this.goal7Plus = goal7Plus;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
