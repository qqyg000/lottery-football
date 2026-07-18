package com.eason.worldcup.model;

public class DataRefreshJobResponse {

    private String jobId;

    private String status;

    private double progress;

    private String message;

    private ModelOverviewResponse result;

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

    public ModelOverviewResponse getResult() {
        return result;
    }

    public void setResult(ModelOverviewResponse result) {
        this.result = result;
    }

}
