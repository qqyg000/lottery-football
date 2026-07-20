package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.RecommendationBacktestJobResponse;
import com.eason.worldcup.model.RecommendationBacktestResponse;
import com.eason.worldcup.model.UserConfig;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RecommendationBacktestJobService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationBacktestJobService.class);

    private static final String STATUS_QUEUED = "QUEUED";

    private static final String STATUS_RUNNING = "RUNNING";

    private static final String STATUS_COMPLETED = "COMPLETED";

    private static final String STATUS_FAILED = "FAILED";

    private static final Duration COMPLETED_JOB_RETENTION = Duration.ofHours(1);

    private final PredictionService predictionService;

    private final Map<String, BacktestJob> jobs = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "recommendation-backtest-worker");
        thread.setDaemon(true);
        return thread;
    });

    public RecommendationBacktestJobService(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public RecommendationBacktestJobResponse start(
            Set<Competition> competitions,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
            Double handicapSmoothingFactor,
            boolean includePreviousEdition,
            Map<Competition, UserConfig.ModelFactors> modelFactorsByCompetition) {
        removeExpiredJobs();
        BacktestJob job = new BacktestJob(UUID.randomUUID().toString());
        jobs.put(job.getJobId(), job);
        Set<Competition> selectedCompetitions = competitions == null ? Set.of() : Set.copyOf(competitions);
        executorService.submit(() -> execute(
                job,
                selectedCompetitions,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor,
                handicapSmoothingFactor,
                includePreviousEdition,
                modelFactorsByCompetition == null ? Map.of() : Map.copyOf(modelFactorsByCompetition)));
        return toResponse(job);
    }

    public RecommendationBacktestJobResponse find(String jobId) {
        BacktestJob job = jobs.get(jobId);
        return job == null ? null : toResponse(job);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    private void execute(
            BacktestJob job,
            Set<Competition> competitions,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
            Double handicapSmoothingFactor,
            boolean includePreviousEdition,
            Map<Competition, UserConfig.ModelFactors> modelFactorsByCompetition) {
        job.start();
        try {
            RecommendationBacktestResponse result = predictionService.queryRecommendationBacktest(
                    competitions,
                    simulations,
                    hostTeamGoalFactor,
                    seedTeamGoalFactor,
                    homeTeamGoalFactor,
                    handicapSmoothingFactor,
                    includePreviousEdition,
                    modelFactorsByCompetition,
                    job::updateProgress);
            job.complete(result);
        } catch (Exception ex) {
            log.error("Recommendation backtest job {} failed", job.getJobId(), ex);
            job.fail(resolveErrorMessage(ex));
        }
    }

    private RecommendationBacktestJobResponse toResponse(BacktestJob job) {
        RecommendationBacktestJobResponse response = new RecommendationBacktestJobResponse();
        response.setJobId(job.getJobId());
        response.setStatus(job.getStatus());
        response.setProcessedMatchCount(job.getProcessedMatchCount());
        response.setTotalMatchCount(job.getTotalMatchCount());
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
                ? "回测计算失败"
                : ex.getMessage();
    }

    private static class BacktestJob {

        private final String jobId;

        private volatile String status = STATUS_QUEUED;

        private volatile int processedMatchCount;

        private volatile int totalMatchCount;

        private volatile String message = "等待开始回测";

        private volatile RecommendationBacktestResponse result;

        private volatile Instant finishedAt;

        private BacktestJob(String jobId) {
            this.jobId = jobId;
        }

        private String getJobId() {
            return jobId;
        }

        private String getStatus() {
            return status;
        }

        private int getProcessedMatchCount() {
            return processedMatchCount;
        }

        private int getTotalMatchCount() {
            return totalMatchCount;
        }

        private String getMessage() {
            return message;
        }

        private RecommendationBacktestResponse getResult() {
            return result;
        }

        private double getProgress() {
            if (STATUS_COMPLETED.equals(status)) {
                return 100.0;
            }
            if (totalMatchCount <= 0) {
                return 0.0;
            }
            return Math.min(100.0, processedMatchCount * 100.0 / totalMatchCount);
        }

        private void start() {
            status = STATUS_RUNNING;
            message = "正在准备回测数据";
        }

        private void updateProgress(int processedMatchCount, int totalMatchCount) {
            this.totalMatchCount = Math.max(0, totalMatchCount);
            this.processedMatchCount = Math.max(0, Math.min(processedMatchCount, this.totalMatchCount));
            message = this.totalMatchCount > 0 ? "正在计算历史赔率样本" : "正在准备回测数据";
        }

        private void complete(RecommendationBacktestResponse result) {
            this.result = result;
            processedMatchCount = totalMatchCount;
            status = STATUS_COMPLETED;
            message = "回测完成";
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
