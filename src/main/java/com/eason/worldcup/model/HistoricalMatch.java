package com.eason.worldcup.model;

import java.time.LocalDate;

public class HistoricalMatch {

    private LocalDate matchDate;

    private String tournament;

    private String sourceCompetition;

    private HistoricalMatchType matchType = HistoricalMatchType.OFFICIAL;

    private String homeTeam;

    private String awayTeam;

    private int homeScore;

    private int awayScore;

    private boolean neutral;

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public String getTournament() {
        return tournament;
    }

    public void setTournament(String tournament) {
        this.tournament = tournament;
    }

    public String getSourceCompetition() {
        return sourceCompetition;
    }

    public void setSourceCompetition(String sourceCompetition) {
        this.sourceCompetition = sourceCompetition;
    }

    public HistoricalMatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(HistoricalMatchType matchType) {
        this.matchType = matchType == null ? HistoricalMatchType.OFFICIAL : matchType;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
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
