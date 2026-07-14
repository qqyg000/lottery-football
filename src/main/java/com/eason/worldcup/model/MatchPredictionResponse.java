package com.eason.worldcup.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MatchPredictionResponse {

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

    private String status;

    private String scoreText;

    private String actualHalfFullResult;

    private int simulations;

    private double expectedHomeGoals;

    private double expectedAwayGoals;

    private ThreeWayProbability normalProbability;

    private List<HandicapProbability> handicapProbabilities = new ArrayList<>();

    private List<ScoreProbability> scoreProbabilities = new ArrayList<>();

    private List<HalfFullProbability> halfFullProbabilities = new ArrayList<>();

    private double adjustedExpectedHomeGoals;

    private double adjustedExpectedAwayGoals;

    private ThreeWayProbability adjustedNormalProbability;

    private List<HandicapProbability> adjustedHandicapProbabilities = new ArrayList<>();

    private List<ScoreProbability> adjustedScoreProbabilities = new ArrayList<>();

    private List<HalfFullProbability> adjustedHalfFullProbabilities = new ArrayList<>();

    private int correctionMatchCount;

    private String modelRemark;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScoreText() {
        return scoreText;
    }

    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
    }

    public String getActualHalfFullResult() {
        return actualHalfFullResult;
    }

    public void setActualHalfFullResult(String actualHalfFullResult) {
        this.actualHalfFullResult = actualHalfFullResult;
    }

    public int getSimulations() {
        return simulations;
    }

    public void setSimulations(int simulations) {
        this.simulations = simulations;
    }

    public double getExpectedHomeGoals() {
        return expectedHomeGoals;
    }

    public void setExpectedHomeGoals(double expectedHomeGoals) {
        this.expectedHomeGoals = expectedHomeGoals;
    }

    public double getExpectedAwayGoals() {
        return expectedAwayGoals;
    }

    public void setExpectedAwayGoals(double expectedAwayGoals) {
        this.expectedAwayGoals = expectedAwayGoals;
    }

    public ThreeWayProbability getNormalProbability() {
        return normalProbability;
    }

    public void setNormalProbability(ThreeWayProbability normalProbability) {
        this.normalProbability = normalProbability;
    }

    public List<HandicapProbability> getHandicapProbabilities() {
        return handicapProbabilities;
    }

    public void setHandicapProbabilities(List<HandicapProbability> handicapProbabilities) {
        this.handicapProbabilities = handicapProbabilities;
    }

    public List<ScoreProbability> getScoreProbabilities() {
        return scoreProbabilities;
    }

    public void setScoreProbabilities(List<ScoreProbability> scoreProbabilities) {
        this.scoreProbabilities = scoreProbabilities;
    }

    public List<HalfFullProbability> getHalfFullProbabilities() {
        return halfFullProbabilities;
    }

    public void setHalfFullProbabilities(List<HalfFullProbability> halfFullProbabilities) {
        this.halfFullProbabilities = halfFullProbabilities;
    }

    public double getAdjustedExpectedHomeGoals() {
        return adjustedExpectedHomeGoals;
    }

    public void setAdjustedExpectedHomeGoals(double adjustedExpectedHomeGoals) {
        this.adjustedExpectedHomeGoals = adjustedExpectedHomeGoals;
    }

    public double getAdjustedExpectedAwayGoals() {
        return adjustedExpectedAwayGoals;
    }

    public void setAdjustedExpectedAwayGoals(double adjustedExpectedAwayGoals) {
        this.adjustedExpectedAwayGoals = adjustedExpectedAwayGoals;
    }

    public ThreeWayProbability getAdjustedNormalProbability() {
        return adjustedNormalProbability;
    }

    public void setAdjustedNormalProbability(ThreeWayProbability adjustedNormalProbability) {
        this.adjustedNormalProbability = adjustedNormalProbability;
    }

    public List<HandicapProbability> getAdjustedHandicapProbabilities() {
        return adjustedHandicapProbabilities;
    }

    public void setAdjustedHandicapProbabilities(List<HandicapProbability> adjustedHandicapProbabilities) {
        this.adjustedHandicapProbabilities = adjustedHandicapProbabilities;
    }

    public List<ScoreProbability> getAdjustedScoreProbabilities() {
        return adjustedScoreProbabilities;
    }

    public void setAdjustedScoreProbabilities(List<ScoreProbability> adjustedScoreProbabilities) {
        this.adjustedScoreProbabilities = adjustedScoreProbabilities;
    }

    public List<HalfFullProbability> getAdjustedHalfFullProbabilities() {
        return adjustedHalfFullProbabilities;
    }

    public void setAdjustedHalfFullProbabilities(List<HalfFullProbability> adjustedHalfFullProbabilities) {
        this.adjustedHalfFullProbabilities = adjustedHalfFullProbabilities;
    }

    public int getCorrectionMatchCount() {
        return correctionMatchCount;
    }

    public void setCorrectionMatchCount(int correctionMatchCount) {
        this.correctionMatchCount = correctionMatchCount;
    }

    public String getModelRemark() {
        return modelRemark;
    }

    public void setModelRemark(String modelRemark) {
        this.modelRemark = modelRemark;
    }

}
