package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.DataRefreshJobResponse;
import com.eason.worldcup.model.ModelOverviewResponse;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DataRefreshJobService {

    private static final Logger log = LoggerFactory.getLogger(DataRefreshJobService.class);

    private static final String STATUS_QUEUED = "QUEUED";

    private static final String STATUS_RUNNING = "RUNNING";

    private static final String STATUS_COMPLETED = "COMPLETED";

    private static final String STATUS_FAILED = "FAILED";

    private static final Duration COMPLETED_JOB_RETENTION = Duration.ofHours(1);

    private final PredictionService predictionService;

    private final Map<String, DataRefreshJob> jobs = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "data-refresh-worker");
        thread.setDaemon(true);
        return thread;
    });

    public DataRefreshJobService(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public DataRefreshJobResponse start(Competition competition, LocalDate date) {
        removeExpiredJobs();
        DataRefreshJob job = new DataRefreshJob(UUID.randomUUID().toString());
        jobs.put(job.getJobId(), job);
        executorService.submit(() -> execute(job, competition, date));
        return toResponse(job);
    }

    public DataRefreshJobResponse find(String jobId) {
        DataRefreshJob job = jobs.get(jobId);
        return job == null ? null : toResponse(job);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    private void execute(DataRefreshJob job, Competition competition, LocalDate date) {
        job.start();
        try {
            ModelOverviewResponse result = predictionService.refreshData(
                    competition,
                    date,
                    job::updateProgress);
            job.complete(result);
        } catch (Exception ex) {
            log.error("Data refresh job {} failed", job.getJobId(), ex);
            job.fail(resolveErrorMessage(ex));
        }
    }

    private DataRefreshJobResponse toResponse(DataRefreshJob job) {
        DataRefreshJobResponse response = new DataRefreshJobResponse();
        response.setJobId(job.getJobId());
        response.setStatus(job.getStatus());
        response.setProgress(job.getProgress());
        response.setMessage(job.getMessage());
        response.setResult(job.getResult());
        return response;
    }

    private void removeExpiredJobs() {
        Instant expirationThreshold = Instant.now().minus(COMPLETED_JOB_RETENTION);
        jobs.entrySet().removeIf(entry -> entry.getValue().finishedBefore(expirationThreshold));
    }

    private String resolveErrorMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "数据更新失败"
                : ex.getMessage();
    }

    private static class DataRefreshJob {

        private final String jobId;

        private volatile String status = STATUS_QUEUED;

        private volatile double progress;

        private volatile String message = "等待开始更新数据";

        private volatile ModelOverviewResponse result;

        private volatile Instant finishedAt;

        private DataRefreshJob(String jobId) {
            this.jobId = jobId;
        }

        private String getJobId() {
            return jobId;
        }

        private String getStatus() {
            return status;
        }

        private double getProgress() {
            return progress;
        }

        private String getMessage() {
            return message;
        }

        private ModelOverviewResponse getResult() {
            return result;
        }

        private void start() {
            status = STATUS_RUNNING;
            message = "正在准备更新数据";
        }

        private void updateProgress(int progress, String message) {
            this.progress = Math.max(this.progress, Math.max(0.0D, Math.min(99.0D, progress)));
            if (message != null && !message.isBlank()) {
                this.message = message;
            }
        }

        private void complete(ModelOverviewResponse result) {
            this.result = result;
            progress = 100.0D;
            status = STATUS_COMPLETED;
            message = "数据更新完成";
            finishedAt = Instant.now();
        }

        private void fail(String errorMessage) {
            status = STATUS_FAILED;
            message = errorMessage;
            finishedAt = Instant.now();
        }

        private boolean finishedBefore(Instant threshold) {
            return finishedAt != null && finishedAt.isBefore(threshold);
        }

    }

}
