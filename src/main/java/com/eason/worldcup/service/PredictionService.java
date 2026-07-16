package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HandicapProbability;
import com.eason.worldcup.model.HeadToHeadMatchResponse;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.MatchPredictionResponse;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.ModelOverviewResponse;
import com.eason.worldcup.model.PredictionQueryResponse;
import com.eason.worldcup.model.RecommendationBacktestResponse;
import com.eason.worldcup.model.ScoreProbability;
import com.eason.worldcup.model.ThreeWayProbability;
import com.eason.worldcup.model.TotalGoalsProbability;
import com.eason.worldcup.util.PoissonRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SplittableRandom;

@Service
public class PredictionService {

    private static final int[] HANDICAPS = {-3, -2, -1, 1, 2, 3};

    private static final ZoneId UTC_PLUS_EIGHT_ZONE = ZoneId.of("Asia/Shanghai");

    private static final ZoneId WORLD_CUP_SCHEDULE_ZONE = ZoneId.of("America/New_York");

    private final DataRepository dataRepository;

    private final TeamStrengthService teamStrengthService;

    private final SportteryMarketSelectionService sportteryMarketSelectionService;

    @Value("${worldcup.simulation-count:50000}")
    private int defaultSimulationCount;

    @Value("${worldcup.handicap-smoothing-factor:0.225}")
    private double handicapSmoothingFactor;

    @Value("${worldcup.handicap-max-smoothing:0.225}")
    private double handicapMaxSmoothing;

    public PredictionService(
            DataRepository dataRepository,
            TeamStrengthService teamStrengthService,
            SportteryMarketSelectionService sportteryMarketSelectionService) {
        this.dataRepository = dataRepository;
        this.teamStrengthService = teamStrengthService;
        this.sportteryMarketSelectionService = sportteryMarketSelectionService;
    }

    public PredictionQueryResponse queryByDate(LocalDate date, Integer simulations) {
        return queryByDate(date, simulations, null, null, null);
    }

    public PredictionQueryResponse queryByDate(LocalDate date, Integer simulations, Double hostTeamGoalFactor, Double seedTeamGoalFactor, Double handicapSmoothingFactor) {
        return queryByDate(
                Competition.WORLD_CUP,
                date,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                handicapSmoothingFactor);
    }

    public PredictionQueryResponse queryByDate(
            Competition competition,
            LocalDate date,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double handicapSmoothingFactor) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        int simulationCount = normalizeSimulationCount(simulations);
        double effectiveHandicapSmoothingFactor = normalizeHandicapSmoothingFactor(handicapSmoothingFactor);
        List<MatchSchedule> schedules = dataRepository.findSchedulesByDate(date, effectiveCompetition);
        sportteryMarketSelectionService.updateSelections(schedules);
        List<MatchPredictionResponse> matches = schedules.stream()
                .map(schedule -> predict(
                        schedule,
                        simulationCount,
                        date,
                        hostTeamGoalFactor,
                        seedTeamGoalFactor,
                        effectiveHandicapSmoothingFactor))
                .toList();
        PredictionQueryResponse response = new PredictionQueryResponse();
        response.setCompetition(effectiveCompetition);
        response.setDate(date);
        response.setSimulations(simulationCount);
        response.setTotal(matches.size());
        response.setMatches(matches);
        return response;
    }

    public RecommendationBacktestResponse queryRecommendationBacktest(
            Competition competition,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double handicapSmoothingFactor) {
        int simulationCount = normalizeSimulationCount(simulations);
        double effectiveHandicapSmoothingFactor = normalizeHandicapSmoothingFactor(handicapSmoothingFactor);
        List<MatchSchedule> completedSchedules = dataRepository.getSchedules().stream()
                .filter(schedule -> competition == null || schedule.getCompetition() == competition)
                .filter(dataRepository::isCurrentSeasonSchedule)
                .filter(schedule -> "COMPLETED".equalsIgnoreCase(schedule.getStatus()))
                .filter(schedule -> schedule.getHomeScore() != null && schedule.getAwayScore() != null)
                .toList();
        sportteryMarketSelectionService.applyCachedSelections(completedSchedules);
        List<MatchSchedule> sportterySchedules = completedSchedules.stream()
                .filter(schedule -> schedule.getSportteryMatchId() != null && !schedule.getSportteryMatchId().isBlank())
                .toList();
        List<MatchSchedule> oddsSchedules = sportterySchedules.stream()
                .filter(schedule -> schedule.getSportteryNormalOdds() != null || schedule.getSportteryHandicapOdds() != null)
                .sorted(Comparator
                        .comparing(
                                MatchSchedule::getMatchDate,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(
                                MatchSchedule::getKickoffTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<MatchPredictionResponse> matches = oddsSchedules.stream()
                .map(schedule -> predict(
                        schedule,
                        simulationCount,
                        schedule.getMatchDate(),
                        hostTeamGoalFactor,
                        seedTeamGoalFactor,
                        effectiveHandicapSmoothingFactor))
                .toList();

        RecommendationBacktestResponse response = new RecommendationBacktestResponse();
        response.setCompletedMatchCount(completedSchedules.size());
        response.setSportteryCompletedMatchCount(sportterySchedules.size());
        response.setOddsMatchCount(oddsSchedules.size());
        response.setMatches(matches);
        return response;
    }

    public ModelOverviewResponse overview() {
        return overview(Competition.WORLD_CUP);
    }

    public ModelOverviewResponse overview(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        ModelOverviewResponse response = new ModelOverviewResponse();
        response.setCompetition(effectiveCompetition);
        response.setCompetitionName(effectiveCompetition.getDisplayName());
        response.setHistoricalMatchCount(teamStrengthService.countHistoricalMatches(effectiveCompetition));
        response.setScheduleMatchCount(dataRepository.getCurrentSeasonSchedules(effectiveCompetition).size());
        response.setCompletedMatchCount(teamStrengthService.countCompletedScheduleMatches(effectiveCompetition));
        response.setBaselineGoals(teamStrengthService.getBaselineGoals(effectiveCompetition));
        response.setScheduleDates(dataRepository.findScheduleDates(effectiveCompetition));
        return response;
    }

    public ModelOverviewResponse refreshData() {
        return refreshData(Competition.WORLD_CUP);
    }

    public ModelOverviewResponse refreshData(Competition competition) {
        return refreshData(competition, null);
    }

    public ModelOverviewResponse refreshData(Competition competition, LocalDate date) {
        dataRepository.refreshSchedules();
        teamStrengthService.rebuildModels();
        sportteryMarketSelectionService.forceRefresh(date);
        return overview(competition);
    }

    public List<HeadToHeadMatchResponse> queryHeadToHead(
            Competition competition,
            String matchId,
            Integer limit) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        if (matchId == null || matchId.isBlank()) {
            return List.of();
        }

        MatchSchedule target = dataRepository.getSchedules(effectiveCompetition).stream()
                .filter(schedule -> matchId.equals(schedule.getMatchId()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return List.of();
        }

        int resultLimit = limit == null ? 10 : Math.max(1, Math.min(50, limit));
        Map<String, HeadToHeadMatchResponse> matchesByFixture = new HashMap<>();
        String targetHomeTeam = getScheduleComparisonTeamName(target, true);
        String targetAwayTeam = getScheduleComparisonTeamName(target, false);
        for (MatchSchedule schedule : dataRepository.getSchedules()) {
            if (!isCompletedSchedule(schedule)
                    || !isScheduleBeforeTarget(schedule, target)
                    || !isSameTeamPair(
                            getScheduleComparisonTeamName(schedule, true),
                            getScheduleComparisonTeamName(schedule, false),
                            targetHomeTeam,
                            targetAwayTeam)) {
                continue;
            }
            HeadToHeadMatchResponse response = toHeadToHeadResponse(schedule);
            matchesByFixture.putIfAbsent(buildHeadToHeadFixtureKey(
                    schedule.getMatchDate(),
                    getScheduleComparisonTeamName(schedule, true),
                    getScheduleComparisonTeamName(schedule, false),
                    schedule.getHomeScore(),
                    schedule.getAwayScore()), response);
        }

        List<HistoricalMatch> historicalMatches = effectiveCompetition == Competition.WORLD_CUP
                ? dataRepository.getHistoricalMatches()
                : dataRepository.getClubHistoricalMatches();
        for (HistoricalMatch historicalMatch : historicalMatches) {
            if (historicalMatch.getMatchDate() == null
                    || target.getMatchDate() == null
                    || !historicalMatch.getMatchDate().isBefore(target.getMatchDate())
                    || !isSameTeamPair(
                            historicalMatch.getHomeTeam(),
                            historicalMatch.getAwayTeam(),
                            targetHomeTeam,
                            targetAwayTeam)) {
                continue;
            }
            HeadToHeadMatchResponse response = toHeadToHeadResponse(historicalMatch, target);
            matchesByFixture.putIfAbsent(buildHeadToHeadFixtureKey(
                    historicalMatch.getMatchDate(),
                    historicalMatch.getHomeTeam(),
                    historicalMatch.getAwayTeam(),
                    historicalMatch.getHomeScore(),
                    historicalMatch.getAwayScore()), response);
        }

        return matchesByFixture.values().stream()
                .sorted(Comparator
                        .comparing(
                                HeadToHeadMatchResponse::getMatchDate,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(
                                HeadToHeadMatchResponse::getKickoffTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(resultLimit)
                .toList();
    }

    private boolean isCompletedSchedule(MatchSchedule schedule) {
        return "COMPLETED".equalsIgnoreCase(schedule.getStatus())
                && schedule.getHomeScore() != null
                && schedule.getAwayScore() != null;
    }

    private boolean isScheduleBeforeTarget(MatchSchedule schedule, MatchSchedule target) {
        if (schedule.getMatchDate() == null || target.getMatchDate() == null) {
            return false;
        }
        int dateComparison = schedule.getMatchDate().compareTo(target.getMatchDate());
        if (dateComparison != 0) {
            return dateComparison < 0;
        }
        LocalTime scheduleTime = schedule.getKickoffTime() == null ? LocalTime.MIN : schedule.getKickoffTime();
        LocalTime targetTime = target.getKickoffTime() == null ? LocalTime.MIN : target.getKickoffTime();
        return scheduleTime.isBefore(targetTime);
    }

    private boolean isSameTeamPair(
            String homeTeam,
            String awayTeam,
            String targetHomeTeam,
            String targetAwayTeam) {
        String home = canonicalTeamName(homeTeam);
        String away = canonicalTeamName(awayTeam);
        String targetHome = canonicalTeamName(targetHomeTeam);
        String targetAway = canonicalTeamName(targetAwayTeam);
        return home.equals(targetHome) && away.equals(targetAway)
                || home.equals(targetAway) && away.equals(targetHome);
    }

    private HeadToHeadMatchResponse toHeadToHeadResponse(MatchSchedule schedule) {
        LocalDateTime displayKickoffDateTime = toUtcPlusEight(schedule);
        HeadToHeadMatchResponse response = new HeadToHeadMatchResponse();
        response.setMatchDate(displayKickoffDateTime == null
                ? schedule.getMatchDate()
                : displayKickoffDateTime.toLocalDate());
        response.setKickoffTime(displayKickoffDateTime == null
                ? schedule.getKickoffTime()
                : displayKickoffDateTime.toLocalTime());
        response.setCompetitionName(buildHeadToHeadCompetitionName(schedule));
        response.setHomeTeamCn(readableTeamName(schedule.getHomeTeamCn(), schedule.getHomeTeamEn()));
        response.setAwayTeamCn(readableTeamName(schedule.getAwayTeamCn(), schedule.getAwayTeamEn()));
        response.setHomeScore(schedule.getHomeScore());
        response.setAwayScore(schedule.getAwayScore());
        response.setNeutral(schedule.isNeutral());
        return response;
    }

    private HeadToHeadMatchResponse toHeadToHeadResponse(HistoricalMatch historicalMatch, MatchSchedule target) {
        HeadToHeadMatchResponse response = new HeadToHeadMatchResponse();
        response.setMatchDate(historicalMatch.getMatchDate());
        response.setCompetitionName(historicalMatch.getTournament());
        response.setHomeTeamCn(resolveHistoricalTeamName(historicalMatch.getHomeTeam(), target));
        response.setAwayTeamCn(resolveHistoricalTeamName(historicalMatch.getAwayTeam(), target));
        response.setHomeScore(historicalMatch.getHomeScore());
        response.setAwayScore(historicalMatch.getAwayScore());
        response.setNeutral(historicalMatch.isNeutral());
        return response;
    }

    private String buildHeadToHeadCompetitionName(MatchSchedule schedule) {
        String competitionName = schedule.getCompetition().getDisplayName();
        String groupName = schedule.getGroupName();
        if (groupName == null || groupName.isBlank() || competitionName.equals(groupName)) {
            return competitionName;
        }
        return competitionName + " · " + groupName;
    }

    private String resolveHistoricalTeamName(String teamName, MatchSchedule target) {
        String canonicalName = canonicalTeamName(teamName);
        if (canonicalName.equals(canonicalTeamName(getScheduleComparisonTeamName(target, true)))) {
            return readableTeamName(target.getHomeTeamCn(), target.getHomeTeamEn());
        }
        if (canonicalName.equals(canonicalTeamName(getScheduleComparisonTeamName(target, false)))) {
            return readableTeamName(target.getAwayTeamCn(), target.getAwayTeamEn());
        }
        return teamName;
    }

    private String getScheduleComparisonTeamName(MatchSchedule schedule, boolean homeTeam) {
        if (schedule.getCompetition().isClubCompetition()) {
            String chineseName = homeTeam ? schedule.getHomeTeamCn() : schedule.getAwayTeamCn();
            if (chineseName != null && !chineseName.isBlank()) {
                return chineseName;
            }
        }
        return homeTeam ? schedule.getHomeTeamEn() : schedule.getAwayTeamEn();
    }

    private String readableTeamName(String chineseName, String englishName) {
        if (chineseName != null && !chineseName.isBlank()) {
            return chineseName;
        }
        return englishName == null ? "" : englishName;
    }

    private String buildHeadToHeadFixtureKey(
            LocalDate matchDate,
            String homeTeamName,
            String awayTeamName,
            int homeScore,
            int awayScore) {
        String homeTeam = canonicalTeamName(homeTeamName);
        String awayTeam = canonicalTeamName(awayTeamName);
        if (homeTeam.compareTo(awayTeam) <= 0) {
            return matchDate + "|" + homeTeam + "|" + homeScore
                    + "|" + awayTeam + "|" + awayScore;
        }
        return matchDate + "|" + awayTeam + "|" + awayScore
                + "|" + homeTeam + "|" + homeScore;
    }

    private String canonicalTeamName(String teamName) {
        String source = teamName == null ? "" : teamName.trim();
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
        return switch (normalized) {
            case "usa" -> "unitedstates";
            case "czechrepublic" -> "czechia";
            case "bosniaherzegovina" -> "bosniaandherzegovina";
            case "cotedivoire" -> "ivorycoast";
            case "korearepublic" -> "southkorea";
            case "iriran" -> "iran";
            case "caboverde" -> "capeverde";
            case "congodr" -> "drcongo";
            case "turkiye" -> "turkey";
            default -> normalized;
        };
    }

    private MatchPredictionResponse predict(
            MatchSchedule schedule,
            int simulationCount,
            LocalDate predictionDate,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            double effectiveHandicapSmoothingFactor) {
        TeamStrengthService.AdjustedExpectedGoals preMatchExpectedGoals = teamStrengthService.calculatePreTournamentExpectedGoals(
                schedule,
                hostTeamGoalFactor,
                seedTeamGoalFactor);
        SimulationCounter preMatchCounter = runMonteCarlo(schedule, preMatchExpectedGoals, simulationCount, effectiveHandicapSmoothingFactor);
        TeamStrengthService.AdjustedExpectedGoals postMatchExpectedGoals = teamStrengthService.calculateCurrentExpectedGoals(
                schedule,
                predictionDate,
                hostTeamGoalFactor,
                seedTeamGoalFactor);
        SimulationCounter postMatchCounter = runMonteCarlo(schedule, postMatchExpectedGoals, simulationCount, effectiveHandicapSmoothingFactor);
        LocalDateTime displayKickoffDateTime = toUtcPlusEight(schedule);
        MatchPredictionResponse response = new MatchPredictionResponse();
        response.setCompetition(schedule.getCompetition());
        response.setMatchId(schedule.getMatchId());
        response.setMatchDate(displayKickoffDateTime == null ? schedule.getMatchDate() : displayKickoffDateTime.toLocalDate());
        response.setKickoffTime(displayKickoffDateTime == null ? schedule.getKickoffTime() : displayKickoffDateTime.toLocalTime());
        response.setGroupName(schedule.getGroupName());
        response.setHomeTeamCn(schedule.getHomeTeamCn());
        response.setAwayTeamCn(schedule.getAwayTeamCn());
        response.setHomeTeamEn(schedule.getHomeTeamEn());
        response.setAwayTeamEn(schedule.getAwayTeamEn());
        response.setVenue(schedule.getVenue());
        response.setStatus(toChineseStatus(schedule.getStatus()));
        response.setScoreText(buildScoreText(schedule));
        response.setSportteryMatchId(schedule.getSportteryMatchId());
        response.setSportteryMatchNumber(schedule.getSportteryMatchNumber());
        response.setSportteryNormalAvailable(schedule.getSportteryNormalAvailable());
        response.setSportteryHandicap(schedule.getSportteryHandicap());
        response.setSportteryNormalOdds(schedule.getSportteryNormalOdds());
        response.setSportteryHandicapOdds(schedule.getSportteryHandicapOdds());
        response.setSimulations(simulationCount);
        response.setExpectedHomeGoals(round(preMatchExpectedGoals.getHomeGoals(), 2));
        response.setExpectedAwayGoals(round(preMatchExpectedGoals.getAwayGoals(), 2));
        ThreeWayProbability preMatchProbability = preMatchCounter.toNormalProbability(simulationCount);
        response.setNormalProbability(preMatchProbability);
        response.setHandicapProbabilities(preMatchCounter.toHandicapProbabilities(simulationCount, preMatchProbability));
        response.setTotalGoalsProbabilities(preMatchCounter.toTopTotalGoalsProbabilities(simulationCount));
        response.setScoreProbabilities(preMatchCounter.toTopScoreProbabilities(simulationCount));
        response.setAdjustedExpectedHomeGoals(round(postMatchExpectedGoals.getHomeGoals(), 2));
        response.setAdjustedExpectedAwayGoals(round(postMatchExpectedGoals.getAwayGoals(), 2));
        ThreeWayProbability postMatchProbability = postMatchCounter.toNormalProbability(simulationCount);
        response.setAdjustedNormalProbability(postMatchProbability);
        response.setAdjustedHandicapProbabilities(postMatchCounter.toHandicapProbabilities(simulationCount, postMatchProbability));
        response.setAdjustedTotalGoalsProbabilities(postMatchCounter.toTopTotalGoalsProbabilities(simulationCount));
        response.setAdjustedScoreProbabilities(postMatchCounter.toTopScoreProbabilities(simulationCount));
        response.setCorrectionMatchCount(postMatchExpectedGoals.getCorrectionMatchCount());
        response.setModelRemark(buildModelRemark(schedule));
        return response;
    }

    private LocalDateTime toUtcPlusEight(MatchSchedule schedule) {
        if (schedule.getMatchDate() == null || schedule.getKickoffTime() == null) {
            return null;
        }
        ZoneId sourceZone = schedule.getCompetition() == Competition.WORLD_CUP
                ? WORLD_CUP_SCHEDULE_ZONE
                : UTC_PLUS_EIGHT_ZONE;
        return LocalDateTime.of(schedule.getMatchDate(), schedule.getKickoffTime())
                .atZone(sourceZone)
                .withZoneSameInstant(UTC_PLUS_EIGHT_ZONE)
                .toLocalDateTime();
    }

    private SimulationCounter runMonteCarlo(MatchSchedule schedule, TeamStrengthService.ExpectedGoals expectedGoals, int simulationCount, double effectiveHandicapSmoothingFactor) {
        long seed = buildSeed(schedule, expectedGoals, simulationCount);
        SplittableRandom random = new SplittableRandom(seed);
        SimulationCounter counter = new SimulationCounter(effectiveHandicapSmoothingFactor);
        for (int i = 0; i < simulationCount; i++) {
            int homeGoals = PoissonRandom.next(expectedGoals.getHomeGoals(), random);
            int awayGoals = PoissonRandom.next(expectedGoals.getAwayGoals(), random);
            counter.addNormal(homeGoals, awayGoals);
            for (int handicap : HANDICAPS) {
                counter.addHandicap(handicap, homeGoals, awayGoals);
            }
        }
        return counter;
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

        private final Map<Integer, Integer> totalGoalsCounters = new HashMap<>();

        private final List<HandicapCounter> handicapCounters = new ArrayList<>();

        private SimulationCounter(double effectiveHandicapSmoothingFactor) {
            this.effectiveHandicapSmoothingFactor = effectiveHandicapSmoothingFactor;
            for (int handicap : HANDICAPS) {
                handicapCounters.add(new HandicapCounter(handicap));
            }
        }

        private void addNormal(int homeGoals, int awayGoals) {
            addScore(homeGoals, awayGoals);
            addTotalGoals(homeGoals, awayGoals);
            if (homeGoals > awayGoals) {
                win++;
            } else if (homeGoals == awayGoals) {
                draw++;
            } else {
                lose++;
            }
        }

        private void addScore(int homeGoals, int awayGoals) {
            String key = homeGoals + "-" + awayGoals;
            ScoreCounter counter = scoreCounters.computeIfAbsent(key, ignored -> new ScoreCounter(homeGoals, awayGoals));
            counter.count++;
        }

        private void addTotalGoals(int homeGoals, int awayGoals) {
            totalGoalsCounters.merge(homeGoals + awayGoals, 1, Integer::sum);
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
                    .limit(3)
                    .map(counter -> new ScoreProbability(
                            counter.homeScore,
                            counter.awayScore,
                            roundPercent(counter.count * 100.0D / simulationCount)))
                    .toList();
        }

        private List<TotalGoalsProbability> toTopTotalGoalsProbabilities(int simulationCount) {
            return totalGoalsCounters.entrySet()
                    .stream()
                    .sorted((left, right) -> {
                        int countCompare = Integer.compare(right.getValue(), left.getValue());
                        if (countCompare != 0) {
                            return countCompare;
                        }
                        return Integer.compare(left.getKey(), right.getKey());
                    })
                    .limit(3)
                    .map(entry -> new TotalGoalsProbability(
                            entry.getKey(),
                            roundPercent(entry.getValue() * 100.0D / simulationCount)))
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

}
