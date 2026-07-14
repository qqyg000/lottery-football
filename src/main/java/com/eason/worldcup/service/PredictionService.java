package com.eason.worldcup.service;

import com.eason.worldcup.model.HandicapProbability;
import com.eason.worldcup.model.HalfFullProbability;
import com.eason.worldcup.model.MatchPredictionResponse;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.ModelOverviewResponse;
import com.eason.worldcup.model.PredictionQueryResponse;
import com.eason.worldcup.model.ScoreProbability;
import com.eason.worldcup.model.ThreeWayProbability;
import com.eason.worldcup.util.PoissonRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

@Service
public class PredictionService {

    private static final int[] HANDICAPS = {-3, -2, -1, 1, 2, 3};

    private static final double FIRST_HALF_GOAL_SHARE = 0.45D;

    private final DataRepository dataRepository;

    private final TeamStrengthService teamStrengthService;

    @Value("${worldcup.simulation-count:50000}")
    private int defaultSimulationCount;

    @Value("${worldcup.handicap-smoothing-factor:0.12}")
    private double handicapSmoothingFactor;

    @Value("${worldcup.handicap-max-smoothing:0.36}")
    private double handicapMaxSmoothing;

    public PredictionService(DataRepository dataRepository, TeamStrengthService teamStrengthService) {
        this.dataRepository = dataRepository;
        this.teamStrengthService = teamStrengthService;
    }

    public PredictionQueryResponse queryByDate(LocalDate date, Integer simulations) {
        return queryByDate(date, simulations, null, null, null);
    }

    public PredictionQueryResponse queryByDate(LocalDate date, Integer simulations, Double hostTeamGoalFactor, Double seedTeamGoalFactor, Double handicapSmoothingFactor) {
        return queryByDate(date, simulations, hostTeamGoalFactor, seedTeamGoalFactor, handicapSmoothingFactor, null, null, null);
    }

    public PredictionQueryResponse queryByDate(
            LocalDate date,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double handicapSmoothingFactor,
            Double baseMatchWeight,
            Double recentHalfYearBonus,
            Double worldCupBonus) {
        int simulationCount = normalizeSimulationCount(simulations);
        double effectiveHandicapSmoothingFactor = normalizeHandicapSmoothingFactor(handicapSmoothingFactor);
        List<MatchPredictionResponse> matches = dataRepository.findSchedulesByDate(date)
                .stream()
                .map(schedule -> predict(
                        schedule,
                        simulationCount,
                        date,
                        hostTeamGoalFactor,
                        seedTeamGoalFactor,
                        effectiveHandicapSmoothingFactor,
                        baseMatchWeight,
                        recentHalfYearBonus,
                        worldCupBonus))
                .toList();
        PredictionQueryResponse response = new PredictionQueryResponse();
        response.setDate(date);
        response.setSimulations(simulationCount);
        response.setTotal(matches.size());
        response.setMatches(matches);
        return response;
    }

    public ModelOverviewResponse overview() {
        ModelOverviewResponse response = new ModelOverviewResponse();
        response.setHistoricalMatchCount(dataRepository.getHistoricalMatches().size());
        response.setScheduleMatchCount(dataRepository.getSchedules().size());
        response.setCompletedMatchCount(teamStrengthService.countCompletedScheduleMatches());
        response.setBaselineGoals(teamStrengthService.getBaselineGoals());
        response.setScheduleDates(dataRepository.findScheduleDates());
        return response;
    }

    public ModelOverviewResponse refreshData() {
        dataRepository.reloadData();
        teamStrengthService.rebuildModels();
        return overview();
    }

    private MatchPredictionResponse predict(
            MatchSchedule schedule,
            int simulationCount,
            LocalDate predictionDate,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            double effectiveHandicapSmoothingFactor,
            Double baseMatchWeight,
            Double recentHalfYearBonus,
            Double worldCupBonus) {
        TeamStrengthService.AdjustedExpectedGoals preMatchExpectedGoals = teamStrengthService.calculatePreTournamentExpectedGoals(
                schedule,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                baseMatchWeight,
                recentHalfYearBonus,
                worldCupBonus);
        SimulationCounter preMatchCounter = runMonteCarlo(schedule, preMatchExpectedGoals, simulationCount, effectiveHandicapSmoothingFactor);
        TeamStrengthService.AdjustedExpectedGoals postMatchExpectedGoals = teamStrengthService.calculateCurrentExpectedGoals(
                schedule,
                predictionDate,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                baseMatchWeight,
                recentHalfYearBonus,
                worldCupBonus);
        SimulationCounter postMatchCounter = runMonteCarlo(schedule, postMatchExpectedGoals, simulationCount, effectiveHandicapSmoothingFactor);
        MatchPredictionResponse response = new MatchPredictionResponse();
        response.setMatchId(schedule.getMatchId());
        response.setMatchDate(schedule.getMatchDate());
        response.setKickoffTime(schedule.getKickoffTime());
        response.setGroupName(schedule.getGroupName());
        response.setHomeTeamCn(schedule.getHomeTeamCn());
        response.setAwayTeamCn(schedule.getAwayTeamCn());
        response.setHomeTeamEn(schedule.getHomeTeamEn());
        response.setAwayTeamEn(schedule.getAwayTeamEn());
        response.setVenue(schedule.getVenue());
        response.setStatus(toChineseStatus(schedule.getStatus()));
        response.setScoreText(buildScoreText(schedule));
        response.setActualHalfFullResult(buildActualHalfFullResult(schedule));
        response.setSimulations(simulationCount);
        response.setExpectedHomeGoals(round(preMatchExpectedGoals.getHomeGoals(), 2));
        response.setExpectedAwayGoals(round(preMatchExpectedGoals.getAwayGoals(), 2));
        ThreeWayProbability preMatchProbability = preMatchCounter.toNormalProbability(simulationCount);
        response.setNormalProbability(preMatchProbability);
        response.setHandicapProbabilities(preMatchCounter.toHandicapProbabilities(simulationCount, preMatchProbability));
        response.setHalfFullProbabilities(preMatchCounter.toTopHalfFullProbabilities(simulationCount));
        response.setScoreProbabilities(preMatchCounter.toTopScoreProbabilities(simulationCount));
        response.setAdjustedExpectedHomeGoals(round(postMatchExpectedGoals.getHomeGoals(), 2));
        response.setAdjustedExpectedAwayGoals(round(postMatchExpectedGoals.getAwayGoals(), 2));
        ThreeWayProbability postMatchProbability = postMatchCounter.toNormalProbability(simulationCount);
        response.setAdjustedNormalProbability(postMatchProbability);
        response.setAdjustedHandicapProbabilities(postMatchCounter.toHandicapProbabilities(simulationCount, postMatchProbability));
        response.setAdjustedHalfFullProbabilities(postMatchCounter.toTopHalfFullProbabilities(simulationCount));
        response.setAdjustedScoreProbabilities(postMatchCounter.toTopScoreProbabilities(simulationCount));
        response.setCorrectionMatchCount(postMatchExpectedGoals.getCorrectionMatchCount());
        response.setModelRemark(buildModelRemark(schedule));
        return response;
    }

    private SimulationCounter runMonteCarlo(MatchSchedule schedule, TeamStrengthService.ExpectedGoals expectedGoals, int simulationCount, double effectiveHandicapSmoothingFactor) {
        long seed = buildSeed(schedule, expectedGoals, simulationCount);
        SplittableRandom random = new SplittableRandom(seed);
        SplittableRandom halfTimeRandom = new SplittableRandom(seed ^ 0x9E3779B97F4A7C15L);
        SimulationCounter counter = new SimulationCounter(effectiveHandicapSmoothingFactor);
        for (int i = 0; i < simulationCount; i++) {
            int homeGoals = PoissonRandom.next(expectedGoals.getHomeGoals(), random);
            int awayGoals = PoissonRandom.next(expectedGoals.getAwayGoals(), random);
            int halfTimeHomeGoals = nextBinomial(homeGoals, FIRST_HALF_GOAL_SHARE, halfTimeRandom);
            int halfTimeAwayGoals = nextBinomial(awayGoals, FIRST_HALF_GOAL_SHARE, halfTimeRandom);
            counter.addNormal(homeGoals, awayGoals, halfTimeHomeGoals, halfTimeAwayGoals);
            for (int handicap : HANDICAPS) {
                counter.addHandicap(handicap, homeGoals, awayGoals);
            }
        }
        return counter;
    }

    private int nextBinomial(int trials, double probability, SplittableRandom random) {
        int successes = 0;
        for (int i = 0; i < trials; i++) {
            if (random.nextDouble() < probability) {
                successes++;
            }
        }
        return successes;
    }

    private int normalizeSimulationCount(Integer simulations) {
        if (simulations == null) {
            return defaultSimulationCount;
        }
        return Math.max(1000, Math.min(500000, simulations));
    }

    private double normalizeHandicapSmoothingFactor(Double value) {
        if (value == null || !Double.isFinite(value)) {
            double factor = Math.max(0.0D, handicapSmoothingFactor);
            double maxWeight = Math.max(0.0D, Math.min(0.8D, handicapMaxSmoothing));
            return Math.min(maxWeight, factor);
        }
        return Math.max(0.0D, Math.min(0.8D, value));
    }

    private long buildSeed(MatchSchedule schedule, TeamStrengthService.ExpectedGoals expectedGoals, int simulationCount) {
        long seed = 1125899906842597L;
        seed = seed * 31 + schedule.getMatchId().hashCode();
        seed = seed * 31 + Double.valueOf(expectedGoals.getHomeGoals()).hashCode();
        seed = seed * 31 + Double.valueOf(expectedGoals.getAwayGoals()).hashCode();
        seed = seed * 31 + simulationCount;
        return seed;
    }

    private String toChineseStatus(String status) {
        if ("COMPLETED".equalsIgnoreCase(status)) {
            return "已完赛";
        }
        if ("LIVE".equalsIgnoreCase(status)) {
            return "进行中";
        }
        return "未开赛";
    }

    private String buildScoreText(MatchSchedule schedule) {
        if (schedule.getHomeScore() == null || schedule.getAwayScore() == null) {
            if ("COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
                return "常规时间比分暂无";
            }
            return "未开赛";
        }
        return schedule.getHomeScore() + " - " + schedule.getAwayScore();
    }

    private String buildActualHalfFullResult(MatchSchedule schedule) {
        if (!"COMPLETED".equalsIgnoreCase(schedule.getStatus())
                || schedule.getHalfTimeHomeScore() == null
                || schedule.getHalfTimeAwayScore() == null
                || schedule.getHomeScore() == null
                || schedule.getAwayScore() == null) {
            return "";
        }
        return toMatchResult(schedule.getHalfTimeHomeScore(), schedule.getHalfTimeAwayScore())
                + toMatchResult(schedule.getHomeScore(), schedule.getAwayScore());
    }

    private String toMatchResult(int homeGoals, int awayGoals) {
        if (homeGoals > awayGoals) {
            return "胜";
        }
        if (homeGoals == awayGoals) {
            return "平";
        }
        return "负";
    }

    private String buildModelRemark(MatchSchedule schedule) {
        if ("COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
            return "该场已完赛，页面展示的是模型概率口径，实际比分以赛程数据为准";
        }
        return "概率由历史战绩强度、泊松进球分布和蒙特卡洛模拟生成，仅作数据分析参考";
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private class SimulationCounter {

        private final double effectiveHandicapSmoothingFactor;

        private int win;

        private int draw;

        private int lose;

        private final Map<String, ScoreCounter> scoreCounters = new HashMap<>();

        private final Map<String, HalfFullCounter> halfFullCounters = new HashMap<>();

        private final List<HandicapCounter> handicapCounters = new ArrayList<>();

        private SimulationCounter(double effectiveHandicapSmoothingFactor) {
            this.effectiveHandicapSmoothingFactor = effectiveHandicapSmoothingFactor;
            for (int handicap : HANDICAPS) {
                handicapCounters.add(new HandicapCounter(handicap));
            }
        }

        private void addNormal(int homeGoals, int awayGoals, int halfTimeHomeGoals, int halfTimeAwayGoals) {
            addScore(homeGoals, awayGoals);
            addHalfFull(halfTimeHomeGoals, halfTimeAwayGoals, homeGoals, awayGoals);
            if (homeGoals > awayGoals) {
                win++;
            } else if (homeGoals == awayGoals) {
                draw++;
            } else {
                lose++;
            }
        }

        private void addHalfFull(int halfTimeHomeGoals, int halfTimeAwayGoals, int homeGoals, int awayGoals) {
            String halfTimeResult = toResult(halfTimeHomeGoals, halfTimeAwayGoals);
            String fullTimeResult = toResult(homeGoals, awayGoals);
            String key = halfTimeResult + fullTimeResult;
            HalfFullCounter counter = halfFullCounters.computeIfAbsent(
                    key,
                    ignored -> new HalfFullCounter(halfTimeResult, fullTimeResult));
            counter.count++;
        }

        private String toResult(int homeGoals, int awayGoals) {
            if (homeGoals > awayGoals) {
                return "胜";
            }
            if (homeGoals == awayGoals) {
                return "平";
            }
            return "负";
        }

        private void addScore(int homeGoals, int awayGoals) {
            String key = homeGoals + "-" + awayGoals;
            ScoreCounter counter = scoreCounters.computeIfAbsent(key, ignored -> new ScoreCounter(homeGoals, awayGoals));
            counter.count++;
        }

        private void addHandicap(int handicap, int homeGoals, int awayGoals) {
            int adjustedHomeGoals = homeGoals + handicap;
            for (HandicapCounter counter : handicapCounters) {
                if (counter.handicap != handicap) {
                    continue;
                }
                if (adjustedHomeGoals > awayGoals) {
                    counter.win++;
                } else if (adjustedHomeGoals == awayGoals) {
                    counter.draw++;
                } else {
                    counter.lose++;
                }
                return;
            }
        }

        private List<ScoreProbability> toTopScoreProbabilities(int simulationCount) {
            return scoreCounters.values()
                    .stream()
                    .sorted((left, right) -> {
                        int countCompare = Integer.compare(right.count, left.count);
                        if (countCompare != 0) {
                            return countCompare;
                        }
                        int totalCompare = Integer.compare(left.homeScore + left.awayScore, right.homeScore + right.awayScore);
                        if (totalCompare != 0) {
                            return totalCompare;
                        }
                        int homeCompare = Integer.compare(left.homeScore, right.homeScore);
                        if (homeCompare != 0) {
                            return homeCompare;
                        }
                        return Integer.compare(left.awayScore, right.awayScore);
                    })
                    .limit(4)
                    .map(counter -> new ScoreProbability(
                            counter.homeScore,
                            counter.awayScore,
                            roundPercent(counter.count * 100.0D / simulationCount)))
                    .toList();
        }

        private List<HalfFullProbability> toTopHalfFullProbabilities(int simulationCount) {
            return halfFullCounters.values()
                    .stream()
                    .sorted((left, right) -> {
                        int countCompare = Integer.compare(right.count, left.count);
                        if (countCompare != 0) {
                            return countCompare;
                        }
                        return (left.halfTimeResult + left.fullTimeResult)
                                .compareTo(right.halfTimeResult + right.fullTimeResult);
                    })
                    .limit(4)
                    .map(counter -> new HalfFullProbability(
                            counter.halfTimeResult,
                            counter.fullTimeResult,
                            roundPercent(counter.count * 100.0D / simulationCount)))
                    .toList();
        }

        private ThreeWayProbability toNormalProbability(int simulationCount) {
            return toProbability(win, draw, lose, simulationCount);
        }

        private List<HandicapProbability> toHandicapProbabilities(int simulationCount, ThreeWayProbability normalProbability) {
            List<HandicapProbability> probabilities = new ArrayList<>();
            for (HandicapCounter counter : handicapCounters) {
                HandicapProbability probability = new HandicapProbability();
                ThreeWayProbability rawProbability = toProbability(counter.win, counter.draw, counter.lose, simulationCount);
                probability.setHandicap(counter.handicap);
                probability.setHandicapName(toHandicapName(counter.handicap));
                probability.setProbability(smoothHandicapProbability(rawProbability, normalProbability, counter.handicap));
                probabilities.add(probability);
            }
            return probabilities;
        }

        private ThreeWayProbability smoothHandicapProbability(ThreeWayProbability rawProbability, ThreeWayProbability normalProbability, int handicap) {
            double smoothingWeight = calculateHandicapSmoothingWeight(handicap);
            if (smoothingWeight <= 0) {
                return rawProbability;
            }
            double rawWeight = 1.0D - smoothingWeight;
            double win = rawProbability.getWin() * rawWeight + normalProbability.getWin() * smoothingWeight;
            double draw = rawProbability.getDraw() * rawWeight + normalProbability.getDraw() * smoothingWeight;
            double lose = rawProbability.getLose() * rawWeight + normalProbability.getLose() * smoothingWeight;
            return toRoundedProbabilityFromPercent(win, draw, lose);
        }

        private double calculateHandicapSmoothingWeight(int handicap) {
            if (handicap == 0) {
                return 0.0D;
            }
            return effectiveHandicapSmoothingFactor;
        }

        private ThreeWayProbability toProbability(int win, int draw, int lose, int simulationCount) {
            return toRoundedProbabilityFromPercent(
                    win * 100.0D / simulationCount,
                    draw * 100.0D / simulationCount,
                    lose * 100.0D / simulationCount
            );
        }

        private ThreeWayProbability toRoundedProbabilityFromPercent(double win, double draw, double lose) {
            double total = win + draw + lose;
            if (total <= 0) {
                return new ThreeWayProbability(0.0D, 0.0D, 0.0D);
            }
            double roundedWin = roundPercent(win * 100.0D / total);
            double roundedDraw = roundPercent(draw * 100.0D / total);
            double roundedLose = roundPercent(Math.max(0.0D, 100.0D - roundedWin - roundedDraw));
            return new ThreeWayProbability(roundedWin, roundedDraw, roundedLose);
        }

        private double roundPercent(double value) {
            return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }

        private String toHandicapName(int handicap) {
            if (handicap < 0) {
                return "主队让" + Math.abs(handicap) + "球";
            }
            return "主队受让" + handicap + "球";
        }

    }

    private static class HandicapCounter {

        private final int handicap;

        private int win;

        private int draw;

        private int lose;

        private HandicapCounter(int handicap) {
            this.handicap = handicap;
        }

    }

    private static class ScoreCounter {

        private final int homeScore;

        private final int awayScore;

        private int count;

        private ScoreCounter(int homeScore, int awayScore) {
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }

    }

    private static class HalfFullCounter {

        private final String halfTimeResult;

        private final String fullTimeResult;

        private int count;

        private HalfFullCounter(String halfTimeResult, String fullTimeResult) {
            this.halfTimeResult = halfTimeResult;
            this.fullTimeResult = fullTimeResult;
        }

    }

}
