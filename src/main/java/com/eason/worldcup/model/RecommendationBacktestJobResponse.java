package com.eason.worldcup.model;

public class RecommendationBacktestJobResponse {

    private String jobId;

    private String status;

    private int processedMatchCount;

    private int totalMatchCount;

    private double progress;

    private String message;

    private RecommendationBacktestResponse result;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProcessedMatchCount() {
        return processedMatchCount;
    }

    public void setProcessedMatchCount(int processedMatchCount) {
        this.processedMatchCount = processedMatchCount;
    }

    public int getTotalMatchCount() {
        return totalMatchCount;
    }

    public void setTotalMatchCount(int totalMatchCount) {
        this.totalMatchCount = totalMatchCount;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RecommendationBacktestResponse getResult() {
        return result;
    }

    public void setResult(RecommendationBacktestResponse result) {
        this.result = result;
    }

}
