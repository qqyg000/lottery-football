package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HandicapProbability;
import com.eason.worldcup.model.HeadToHeadMatchResponse;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.HistoricalMatchType;
import com.eason.worldcup.model.MatchPredictionResponse;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.ModelOverviewResponse;
import com.eason.worldcup.model.PredictionQueryResponse;
import com.eason.worldcup.model.RecommendationBacktestResponse;
import com.eason.worldcup.model.ScoreProbability;
import com.eason.worldcup.model.SportteryHistoricalOddsRefreshResponse;
import com.eason.worldcup.model.ThreeWayProbability;
import com.eason.worldcup.model.TotalGoalsProbability;
import com.eason.worldcup.model.UserConfig;
import com.eason.worldcup.util.ApplicationTime;
import com.eason.worldcup.util.PoissonRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.function.BiConsumer;

@Service
public class PredictionService {

    private static final int[] HANDICAPS = {-3, -2, -1, 1, 2, 3};

    private static final Map<Competition, LocalDate> CURRENT_EDITION_BACKTEST_START_DATES = Map.ofEntries(
            Map.entry(Competition.WORLD_CUP, LocalDate.of(2026, 6, 11)),
            Map.entry(Competition.EUROPEAN_CHAMPIONSHIP, LocalDate.of(2028, 6, 9)),
            Map.entry(Competition.COPA_AMERICA, LocalDate.of(2028, 6, 9)),
            Map.entry(Competition.CLUB_WORLD_CUP, LocalDate.of(2028, 6, 9)),
            Map.entry(Competition.EUROPA_LEAGUE, LocalDate.of(2026, 9, 16)),
            Map.entry(Competition.CHAMPIONS_LEAGUE, LocalDate.of(2026, 9, 8)),
            Map.entry(Competition.PREMIER_LEAGUE, LocalDate.of(2026, 8, 21)),
            Map.entry(Competition.LA_LIGA, LocalDate.of(2026, 8, 15)),
            Map.entry(Competition.SERIE_A, LocalDate.of(2026, 8, 21)),
            Map.entry(Competition.BUNDESLIGA, LocalDate.of(2026, 8, 28)),
            Map.entry(Competition.LIGUE_1, LocalDate.of(2026, 8, 21)),
            Map.entry(Competition.BRAZIL_SERIE_A, LocalDate.of(2026, 1, 28)),
            Map.entry(Competition.PRIMEIRA_LIGA, LocalDate.of(2026, 8, 8)),
            Map.entry(Competition.EREDIVISIE, LocalDate.of(2026, 8, 7)),
            Map.entry(Competition.ARGENTINE_PRIMERA_DIVISION, LocalDate.of(2026, 1, 22)));

    private static final Map<Competition, LocalDate> PREVIOUS_EDITION_BACKTEST_START_DATES = Map.ofEntries(
            Map.entry(Competition.WORLD_CUP, LocalDate.of(2022, 11, 20)),
            Map.entry(Competition.EUROPEAN_CHAMPIONSHIP, LocalDate.of(2024, 6, 14)),
            Map.entry(Competition.COPA_AMERICA, LocalDate.of(2024, 6, 20)),
            Map.entry(Competition.CLUB_WORLD_CUP, LocalDate.of(2025, 6, 14)),
            Map.entry(Competition.EUROPA_LEAGUE, LocalDate.of(2025, 9, 24)),
            Map.entry(Competition.CHAMPIONS_LEAGUE, LocalDate.of(2025, 9, 16)),
            Map.entry(Competition.PREMIER_LEAGUE, LocalDate.of(2025, 8, 15)),
            Map.entry(Competition.LA_LIGA, LocalDate.of(2025, 8, 15)),
            Map.entry(Competition.SERIE_A, LocalDate.of(2025, 8, 23)),
            Map.entry(Competition.BUNDESLIGA, LocalDate.of(2025, 8, 22)),
            Map.entry(Competition.LIGUE_1, LocalDate.of(2025, 8, 15)),
            Map.entry(Competition.BRAZIL_SERIE_A, LocalDate.of(2025, 3, 29)),
            Map.entry(Competition.PRIMEIRA_LIGA, LocalDate.of(2025, 8, 8)),
            Map.entry(Competition.EREDIVISIE, LocalDate.of(2025, 8, 8)),
            Map.entry(Competition.ARGENTINE_PRIMERA_DIVISION, LocalDate.of(2025, 1, 23)));

    private final DataRepository dataRepository;

    private final TeamStrengthService teamStrengthService;

    private final SportteryMarketSelectionService sportteryMarketSelectionService;

    @Value("${worldcup.simulation-count:50000}")
    private int defaultSimulationCount;

    @Value("${worldcup.handicap-smoothing-factor:0.274}")
    private double handicapSmoothingFactor;

    @Value("${worldcup.handicap-max-smoothing:0.685}")
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
        return queryByDate(
                competition,
                date,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                null,
                handicapSmoothingFactor);
    }

    public PredictionQueryResponse queryByDate(
            Competition competition,
            LocalDate date,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
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
                        homeTeamGoalFactor,
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
            Set<Competition> competitions,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double handicapSmoothingFactor) {
        return queryRecommendationBacktest(
                competitions,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                null,
                handicapSmoothingFactor);
    }

    public RecommendationBacktestResponse queryRecommendationBacktest(
            Set<Competition> competitions,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
            Double handicapSmoothingFactor) {
        return queryRecommendationBacktest(
                competitions,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor,
                handicapSmoothingFactor,
                false);
    }

    public RecommendationBacktestResponse queryRecommendationBacktest(
            Set<Competition> competitions,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
            Double handicapSmoothingFactor,
            boolean includePreviousEdition) {
        return queryRecommendationBacktest(
                competitions,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor,
                handicapSmoothingFactor,
                includePreviousEdition,
                Map.of(),
                null);
    }

    RecommendationBacktestResponse queryRecommendationBacktest(
            Set<Competition> competitions,
            Integer simulations,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
            Double handicapSmoothingFactor,
            boolean includePreviousEdition,
            Map<Competition, UserConfig.ModelFactors> modelFactorsByCompetition,
            BiConsumer<Integer, Integer> progressConsumer) {
        int simulationCount = normalizeSimulationCount(simulations);
        LocalDate backtestEndDate = ApplicationTime.today();
        List<MatchSchedule> completedSchedules = dataRepository.getSchedules().stream()
                .filter(schedule -> competitions == null
                        || competitions.isEmpty()
                        || competitions.contains(schedule.getCompetition()))
                .filter(schedule -> isWithinRecommendationBacktestRange(
                        schedule,
                        backtestEndDate,
                        includePreviousEdition))
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
        int totalMatchCount = oddsSchedules.size();
        notifyBacktestProgress(progressConsumer, 0, totalMatchCount);
        List<MatchPredictionResponse> matches = new ArrayList<>(totalMatchCount);
        for (int index = 0; index < totalMatchCount; index++) {
            MatchSchedule schedule = oddsSchedules.get(index);
            UserConfig.ModelFactors competitionFactors = resolveBacktestModelFactors(
                    schedule.getCompetition(),
                    modelFactorsByCompetition);
            matches.add(predict(
                    schedule,
                    simulationCount,
                    schedule.getMatchDate(),
                    resolveModelFactor(
                            competitionFactors == null ? null : competitionFactors.getHostTeamGoalFactor(),
                            hostTeamGoalFactor),
                    resolveModelFactor(
                            competitionFactors == null ? null : competitionFactors.getSeedTeamGoalFactor(),
                            seedTeamGoalFactor),
                    resolveModelFactor(
                            competitionFactors == null ? null : competitionFactors.getHomeTeamGoalFactor(),
                            homeTeamGoalFactor),
                    normalizeHandicapSmoothingFactor(resolveModelFactor(
                            competitionFactors == null ? null : competitionFactors.getHandicapSmoothingFactor(),
                            handicapSmoothingFactor))));
            notifyBacktestProgress(progressConsumer, index + 1, totalMatchCount);
        }

        RecommendationBacktestResponse response = new RecommendationBacktestResponse();
        response.setCompletedMatchCount(completedSchedules.size());
        response.setSportteryCompletedMatchCount(sportterySchedules.size());
        response.setOddsMatchCount(oddsSchedules.size());
        response.setMatches(matches);
        return response;
    }

    UserConfig.ModelFactors resolveBacktestModelFactors(
            Competition competition,
            Map<Competition, UserConfig.ModelFactors> modelFactorsByCompetition) {
        if (competition == null || modelFactorsByCompetition == null) {
            return null;
        }
        return modelFactorsByCompetition.get(competition);
    }

    private Double resolveModelFactor(Double competitionValue, Double fallbackValue) {
        return competitionValue == null || !Double.isFinite(competitionValue)
                ? fallbackValue
                : competitionValue;
    }

    private void notifyBacktestProgress(
            BiConsumer<Integer, Integer> progressConsumer,
            int processedMatchCount,
            int totalMatchCount) {
        if (progressConsumer != null) {
            progressConsumer.accept(processedMatchCount, totalMatchCount);
        }
    }

    public SportteryHistoricalOddsRefreshResponse refreshHistoricalOdds(
            LocalDate startDate,
            LocalDate endDate) {
        SportteryHistoricalOddsRefreshResponse response =
                sportteryMarketSelectionService.refreshHistoricalRange(startDate, endDate);
        List<MatchSchedule> schedules = dataRepository.getSchedules().stream()
                .filter(schedule -> schedule.getMatchDate() != null)
                .filter(schedule -> !schedule.getMatchDate().isBefore(startDate))
                .filter(schedule -> !schedule.getMatchDate().isAfter(endDate))
                .filter(schedule -> "COMPLETED".equalsIgnoreCase(schedule.getStatus()))
                .toList();
        int matchedScheduleCount = sportteryMarketSelectionService.applyCachedSelections(schedules);
        int matchedOddsScheduleCount = (int) schedules.stream()
                .filter(schedule -> schedule.getSportteryNormalOdds() != null
                        || schedule.getSportteryHandicapOdds() != null)
                .count();
        response.setScheduleCount(schedules.size());
        response.setMatchedScheduleCount(matchedScheduleCount);
        response.setMatchedOddsScheduleCount(matchedOddsScheduleCount);
        return response;
    }

    private boolean isWithinRecommendationBacktestRange(
            MatchSchedule schedule,
            LocalDate backtestEndDate,
            boolean includePreviousEdition) {
        if (schedule.getCompetition() == null || schedule.getMatchDate() == null) {
            return false;
        }
        Map<Competition, LocalDate> backtestStartDates = includePreviousEdition
                ? PREVIOUS_EDITION_BACKTEST_START_DATES
                : CURRENT_EDITION_BACKTEST_START_DATES;
        LocalDate backtestStartDate = backtestStartDates.get(schedule.getCompetition());
        return backtestStartDate != null
                && !backtestStartDate.isAfter(backtestEndDate)
                && !schedule.getMatchDate().isBefore(backtestStartDate)
                && !schedule.getMatchDate().isAfter(backtestEndDate);
    }

    public ModelOverviewResponse overview() {
        return overview(Competition.WORLD_CUP);
    }

    public ModelOverviewResponse overview(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        sportteryMarketSelectionService.applyCachedSelections(dataRepository.getSchedules(effectiveCompetition));
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
        return refreshData(competition, date, null);
    }

    ModelOverviewResponse refreshData(
            Competition competition,
            LocalDate date,
            BiConsumer<Integer, String> progressConsumer) {
        notifyDataRefreshProgress(progressConsumer, 5, "正在刷新15类赛事赛程与补充数据");
        dataRepository.refreshSchedules();
        notifyDataRefreshProgress(progressConsumer, 55, "赛程数据已更新，正在重建球队模型");
        teamStrengthService.rebuildModels();
        notifyDataRefreshProgress(progressConsumer, 70, "球队模型已重建，正在刷新竞彩数据");
        sportteryMarketSelectionService.forceRefresh(date);
        notifyDataRefreshProgress(progressConsumer, 90, "竞彩数据已刷新，正在加载赛事概览");
        ModelOverviewResponse response = overview(competition);
        notifyDataRefreshProgress(progressConsumer, 100, "数据更新完成");
        return response;
    }

    private void notifyDataRefreshProgress(
            BiConsumer<Integer, String> progressConsumer,
            int progress,
            String message) {
        if (progressConsumer != null) {
            progressConsumer.accept(progress, message);
        }
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

        List<HistoricalMatch> historicalMatches = effectiveCompetition.isClubCompetition()
                ? dataRepository.getClubHistoricalMatches()
                : dataRepository.getHistoricalMatches();
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
        HistoricalMatchType matchType = HistoricalMatchType.fromCompetition(schedule.getCompetition());
        HeadToHeadMatchResponse response = new HeadToHeadMatchResponse();
        response.setMatchDate(schedule.getMatchDate());
        response.setKickoffTime(schedule.getKickoffTime());
        response.setCompetitionName(buildHeadToHeadCompetitionName(schedule));
        response.setMatchTypeName(matchType.getDisplayName());
        response.setModelWeight(matchType.getModelWeight());
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
        response.setCompetitionName(historicalMatch.getSourceCompetition());
        response.setMatchTypeName(historicalMatch.getMatchType().getDisplayName());
        response.setModelWeight(historicalMatch.getMatchType().getModelWeight());
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
        String chineseName = homeTeam ? schedule.getHomeTeamCn() : schedule.getAwayTeamCn();
        if (chineseName != null && !chineseName.isBlank()) {
            return chineseName;
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
        return normalized;
    }

    private MatchPredictionResponse predict(
            MatchSchedule schedule,
            int simulationCount,
            LocalDate predictionDate,
            Double hostTeamGoalFactor,
            Double seedTeamGoalFactor,
            Double homeTeamGoalFactor,
            double effectiveHandicapSmoothingFactor) {
        TeamStrengthService.AdjustedExpectedGoals preMatchExpectedGoals = teamStrengthService.calculatePreTournamentExpectedGoals(
                schedule,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor);
        SimulationCounter preMatchCounter = runMonteCarlo(schedule, preMatchExpectedGoals, simulationCount, effectiveHandicapSmoothingFactor);
        TeamStrengthService.AdjustedExpectedGoals postMatchExpectedGoals = teamStrengthService.calculateCurrentExpectedGoals(
                schedule,
                predictionDate,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor);
        SimulationCounter postMatchCounter = runMonteCarlo(schedule, postMatchExpectedGoals, simulationCount, effectiveHandicapSmoothingFactor);
        MatchPredictionResponse response = new MatchPredictionResponse();
        response.setCompetition(schedule.getCompetition());
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
