package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.RecommendationBacktestJobResponse;
import com.eason.worldcup.model.RecommendationBacktestResponse;
import com.eason.worldcup.model.UserConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationBacktestJobServiceTest {

    @Test
    void shouldRunIndependentBacktestJobsInParallel() throws Exception {
        BlockingPredictionService predictionService = new BlockingPredictionService();
        RecommendationBacktestJobService service = new RecommendationBacktestJobService(predictionService, 2);
        try {
            RecommendationBacktestJobResponse first = startJob(service);
            RecommendationBacktestJobResponse second = startJob(service);

            assertTrue(predictionService.awaitBothJobsStarted());
            assertEquals(2, predictionService.getMaximumConcurrentJobs());

            predictionService.releaseJobs();
            awaitCompleted(service, first.getJobId());
            awaitCompleted(service, second.getJobId());
            assertEquals(2, predictionService.getCacheClearCount());
        } finally {
            predictionService.releaseJobs();
            service.shutdown();
        }
    }

    private RecommendationBacktestJobResponse startJob(RecommendationBacktestJobService service) {
        return service.start(
                Set.of(Competition.WORLD_CUP),
                1000,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Map.of());
    }

    private void awaitCompleted(RecommendationBacktestJobService service, String jobId) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            RecommendationBacktestJobResponse response = service.find(jobId);
            if (response != null && "COMPLETED".equals(response.getStatus())) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("回测任务未在超时时间内完成");
    }

    private static class BlockingPredictionService extends PredictionService {

        private final CountDownLatch startedJobs = new CountDownLatch(2);

        private final CountDownLatch releaseJobs = new CountDownLatch(1);

        private final AtomicInteger activeJobs = new AtomicInteger();

        private final AtomicInteger maximumConcurrentJobs = new AtomicInteger();

        private final AtomicInteger cacheClearCount = new AtomicInteger();

        private BlockingPredictionService() {
            super(null, null, null);
        }

        @Override
        public void clearDynamicModelCaches() {
            cacheClearCount.incrementAndGet();
        }

        @Override
        RecommendationBacktestResponse queryRecommendationBacktest(
                Set<Competition> competitions,
                Integer simulations,
                Double hostTeamGoalFactor,
                Double seedTeamGoalFactor,
                Double homeTeamGoalFactor,
                Double handicapSmoothingFactor,
                Double officialMatchWeight,
                Double internationalFriendlyWeight,
                Double clubFriendlyWeight,
                boolean includePreviousEdition,
                Map<Competition, UserConfig.ModelFactors> modelFactorsByCompetition,
                BiConsumer<Integer, Integer> progressConsumer) {
            int currentActiveJobs = activeJobs.incrementAndGet();
            maximumConcurrentJobs.accumulateAndGet(currentActiveJobs, Math::max);
            startedJobs.countDown();
            try {
                if (!releaseJobs.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("等待并发任务释放超时");
                }
                if (progressConsumer != null) {
                    progressConsumer.accept(1, 1);
                }
                return new RecommendationBacktestResponse();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("并发回测任务被中断", ex);
            } finally {
                activeJobs.decrementAndGet();
            }
        }

        private boolean awaitBothJobsStarted() throws InterruptedException {
            return startedJobs.await(2, TimeUnit.SECONDS);
        }

        private void releaseJobs() {
            releaseJobs.countDown();
        }

        private int getMaximumConcurrentJobs() {
            return maximumConcurrentJobs.get();
        }

        private int getCacheClearCount() {
            return cacheClearCount.get();
        }

    }

}
