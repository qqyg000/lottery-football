package com.eason.worldcup.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class MatchSchedule {

    private Competition competition = Competition.WORLD_CUP;

    private String matchId;

    private LocalDate matchDate;

    private LocalTime kickoffTime;

    private String groupName;

    private String homeTeamCn;

    private String awayTeamCn;

    private String homeTeamEn;

    private String awayTeamEn;

    private String venue;

    private boolean neutral;

    private String status;

    private Integer homeScore;

    private Integer awayScore;

    private String sportteryMatchId;

    private String sportteryMatchNumber;

    private Boolean sportteryNormalAvailable;

    private Integer sportteryHandicap;

    private SportteryOdds sportteryNormalOdds;

    private SportteryOdds sportteryHandicapOdds;

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition == null ? Competition.WORLD_CUP : competition;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public LocalTime getKickoffTime() {
        return kickoffTime;
    }

    public void setKickoffTime(LocalTime kickoffTime) {
        this.kickoffTime = kickoffTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getHomeTeamCn() {
        return homeTeamCn;
    }

    public void setHomeTeamCn(String homeTeamCn) {
        this.homeTeamCn = homeTeamCn;
    }

    public String getAwayTeamCn() {
        return awayTeamCn;
    }

    public void setAwayTeamCn(String awayTeamCn) {
        this.awayTeamCn = awayTeamCn;
    }

    public String getHomeTeamEn() {
        return homeTeamEn;
    }

    public void setHomeTeamEn(String homeTeamEn) {
        this.homeTeamEn = homeTeamEn;
    }

    public String getAwayTeamEn() {
        return awayTeamEn;
    }

    public void setAwayTeamEn(String awayTeamEn) {
        this.awayTeamEn = awayTeamEn;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public void setNeutral(boolean neutral) {
        this.neutral = neutral;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public String getSportteryMatchId() {
        return sportteryMatchId;
    }

    public void setSportteryMatchId(String sportteryMatchId) {
        this.sportteryMatchId = sportteryMatchId;
    }

    public String getSportteryMatchNumber() {
        return sportteryMatchNumber;
    }

    public void setSportteryMatchNumber(String sportteryMatchNumber) {
        this.sportteryMatchNumber = sportteryMatchNumber;
    }

    public Boolean getSportteryNormalAvailable() {
        return sportteryNormalAvailable;
    }

    public void setSportteryNormalAvailable(Boolean sportteryNormalAvailable) {
        this.sportteryNormalAvailable = sportteryNormalAvailable;
    }

    public Integer getSportteryHandicap() {
        return sportteryHandicap;
    }

    public void setSportteryHandicap(Integer sportteryHandicap) {
        this.sportteryHandicap = sportteryHandicap;
    }

    public SportteryOdds getSportteryNormalOdds() {
        return sportteryNormalOdds;
    }

    public void setSportteryNormalOdds(SportteryOdds sportteryNormalOdds) {
        this.sportteryNormalOdds = sportteryNormalOdds;
    }

    public SportteryOdds getSportteryHandicapOdds() {
        return sportteryHandicapOdds;
    }

    public void setSportteryHandicapOdds(SportteryOdds sportteryHandicapOdds) {
        this.sportteryHandicapOdds = sportteryHandicapOdds;
    }

}
