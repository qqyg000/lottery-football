package com.eason.worldcup.model;

public class SportteryOdds {

    private Double win;

    private Double draw;

    private Double lose;

    private String updatedAt;

    public SportteryOdds() {
    }

    public SportteryOdds(Double win, Double draw, Double lose, String updatedAt) {
        this.win = win;
        this.draw = draw;
        this.lose = lose;
        this.updatedAt = updatedAt;
    }

    public Double getWin() {
        return win;
    }

    public void setWin(Double win) {
        this.win = win;
    }

    public Double getDraw() {
        return draw;
    }

    public void setDraw(Double draw) {
        this.draw = draw;
    }

    public Double getLose() {
        return lose;
    }

    public void setLose(Double lose) {
        this.lose = lose;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
