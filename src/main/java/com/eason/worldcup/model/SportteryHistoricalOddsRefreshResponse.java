package com.eason.worldcup.model;

import java.time.LocalDate;

public class SportteryHistoricalOddsRefreshResponse {

    private LocalDate startDate;

    private LocalDate endDate;

    private int officialMatchCount;

    private int normalOddsMatchCount;

    private int handicapOddsMatchCount;

    private int completeOddsMatchCount;

    private int failedOddsQueryCount;

    private int scheduleCount;

    private int matchedScheduleCount;

    private int matchedOddsScheduleCount;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getOfficialMatchCount() {
        return officialMatchCount;
    }

    public void setOfficialMatchCount(int officialMatchCount) {
        this.officialMatchCount = officialMatchCount;
    }

    public int getNormalOddsMatchCount() {
        return normalOddsMatchCount;
    }

    public void setNormalOddsMatchCount(int normalOddsMatchCount) {
        this.normalOddsMatchCount = normalOddsMatchCount;
    }

    public int getHandicapOddsMatchCount() {
        return handicapOddsMatchCount;
    }

    public void setHandicapOddsMatchCount(int handicapOddsMatchCount) {
        this.handicapOddsMatchCount = handicapOddsMatchCount;
    }

    public int getCompleteOddsMatchCount() {
        return completeOddsMatchCount;
    }

    public void setCompleteOddsMatchCount(int completeOddsMatchCount) {
        this.completeOddsMatchCount = completeOddsMatchCount;
    }

    public int getFailedOddsQueryCount() {
        return failedOddsQueryCount;
    }

    public void setFailedOddsQueryCount(int failedOddsQueryCount) {
        this.failedOddsQueryCount = failedOddsQueryCount;
    }

    public int getScheduleCount() {
        return scheduleCount;
    }

    public void setScheduleCount(int scheduleCount) {
        this.scheduleCount = scheduleCount;
    }

    public int getMatchedScheduleCount() {
        return matchedScheduleCount;
    }

    public void setMatchedScheduleCount(int matchedScheduleCount) {
        this.matchedScheduleCount = matchedScheduleCount;
    }

    public int getMatchedOddsScheduleCount() {
        return matchedOddsScheduleCount;
    }

    public void setMatchedOddsScheduleCount(int matchedOddsScheduleCount) {
        this.matchedOddsScheduleCount = matchedOddsScheduleCount;
    }

}
