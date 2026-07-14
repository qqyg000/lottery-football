package com.eason.worldcup.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PredictionQueryResponse {

    private LocalDate date;

    private int simulations;

    private int total;

    private List<MatchPredictionResponse> matches = new ArrayList<>();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getSimulations() {
        return simulations;
    }

    public void setSimulations(int simulations) {
        this.simulations = simulations;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<MatchPredictionResponse> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchPredictionResponse> matches) {
        this.matches = matches;
    }

}
