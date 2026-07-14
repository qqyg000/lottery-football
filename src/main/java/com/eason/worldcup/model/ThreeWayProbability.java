package com.eason.worldcup.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ThreeWayProbability {

    private double win;

    private double draw;

    private double lose;

    public ThreeWayProbability() {
    }

    public ThreeWayProbability(double win, double draw, double lose) {
        this.win = win;
        this.draw = draw;
        this.lose = lose;
    }

    public double getWin() {
        return win;
    }

    public void setWin(double win) {
        this.win = win;
    }

    public double getDraw() {
        return draw;
    }

    public void setDraw(double draw) {
        this.draw = draw;
    }

    public double getLose() {
        return lose;
    }

    public void setLose(double lose) {
        this.lose = lose;
    }

    public ThreeWayProbability percent(int scale) {
        return new ThreeWayProbability(round(win * 100, scale), round(draw * 100, scale), round(lose * 100, scale));
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

}
