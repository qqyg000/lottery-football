package com.eason.worldcup.service;

import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.TeamStrength;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TeamStrengthService {

    private static final double SHRINK_WEIGHT = 8.0D;
    private static final int RECENT_HALF_YEAR_DAYS = 183;
    private static final double DEFAULT_BASE_MATCH_WEIGHT = 1.0D;
    private static final double DEFAULT_RECENT_HALF_YEAR_BONUS = 0.10D;
    private static final double DEFAULT_WORLD_CUP_BONUS = 0.25D;
    private static final LocalDate TOURNAMENT_START_DATE = LocalDate.of(2026, 6, 11);
    private static final String WORLD_CUP_2026 = "FIFA World Cup 2026";
    private static final List<String> HOST_TEAMS = List.of("Canada", "Mexico", "United States");
    private static final List<String> SEEDED_TEAMS = List.of(
            "Mexico",
            "Canada",
            "Brazil",
            "United States",
            "Germany",
            "Netherlands",
            "Belgium",
            "Spain",
            "France",
            "Argentina",
            "Portugal",
            "England"
    );

    private final DataRepository dataRepository;

    @Value("${worldcup.max-goals-lambda:4.5}")
    private double maxGoalsLambda;

    @Value("${worldcup.min-goals-lambda:0.2}")
    private double minGoalsLambda;

    @Value("${worldcup.host-team-goal-factor:1.42}")
    private double hostTeamGoalFactor;

    @Value("${worldcup.seed-team-goal-factor:1.77}")
    private double seedTeamGoalFactor;

    @Value("${worldcup.base-match-weight:1.0}")
    private double baseMatchWeight;

    @Value("${worldcup.recent-half-year-bonus:0.10}")
    private double recentHalfYearBonus;

    @Value("${worldcup.world-cup-bonus:0.25}")
    private double worldCupBonus;

    @Value("${worldcup.current-model-excluded-dates:}")
    private String currentModelExcludedDateText;

    private volatile List<LocalDate> currentModelExcludedDates = Collections.emptyList();

    private volatile StrengthModel preTournamentModel = StrengthModel.empty();

    private volatile StrengthModel currentModel = StrengthModel.empty();

    private final Map<LocalDate, StrengthModel> currentModelByPredictionDate = new ConcurrentHashMap<>();

    public TeamStrengthService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @PostConstruct
    public void init() {
        rebuildModels();
    }

    public synchronized void rebuildModels() {
        List<HistoricalMatch> historicalMatches = dataRepository.getHistoricalMatches();
        currentModelExcludedDates = parseExcludedDates(currentModelExcludedDateText);
        currentModelByPredictionDate.clear();
        preTournamentModel = buildStrengthModel(filterPreTournamentMatches(historicalMatches));
        currentModel = buildStrengthModel(buildCurrentMatches(historicalMatches, LocalDate.now()));
    }

    public AdjustedExpectedGoals calculatePreTournamentExpectedGoals(MatchSchedule schedule) {
        return calculatePreTournamentExpectedGoals(schedule, null, null);
    }

    public AdjustedExpectedGoals calculatePreTournamentExpectedGoals(MatchSchedule schedule, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        return calculatePreTournamentExpectedGoals(schedule, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride, null, null, null);
    }

    public AdjustedExpectedGoals calculatePreTournamentExpectedGoals(
            MatchSchedule schedule,
            Double hostTeamGoalFactorOverride,
            Double seedTeamGoalFactorOverride,
            Double baseMatchWeightOverride,
            Double recentHalfYearBonusOverride,
            Double worldCupBonusOverride) {
        MatchWeightConfig weightConfig = resolveMatchWeightConfig(baseMatchWeightOverride, recentHalfYearBonusOverride, worldCupBonusOverride);
        StrengthModel model = weightConfig.isDefaultOf(defaultMatchWeightConfig())
                ? preTournamentModel
                : buildStrengthModel(filterPreTournamentMatches(dataRepository.getHistoricalMatches()), weightConfig);
        ExpectedGoals expectedGoals = calculateExpectedGoals(schedule, model, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride);
        return new AdjustedExpectedGoals(expectedGoals.getHomeGoals(), expectedGoals.getAwayGoals(), model.getSampleCount());
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule) {
        return calculateCurrentExpectedGoals(schedule, null, null);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        ExpectedGoals expectedGoals = calculateExpectedGoals(schedule, currentModel, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride);
        return new AdjustedExpectedGoals(expectedGoals.getHomeGoals(), expectedGoals.getAwayGoals(), currentModel.getSampleCount());
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule, LocalDate predictionDate) {
        return calculateCurrentExpectedGoals(schedule, predictionDate, null, null);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule, LocalDate predictionDate, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        return calculateCurrentExpectedGoals(schedule, predictionDate, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride, null, null, null);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(
            MatchSchedule schedule,
            LocalDate predictionDate,
            Double hostTeamGoalFactorOverride,
            Double seedTeamGoalFactorOverride,
            Double baseMatchWeightOverride,
            Double recentHalfYearBonusOverride,
            Double worldCupBonusOverride) {
        MatchWeightConfig weightConfig = resolveMatchWeightConfig(baseMatchWeightOverride, recentHalfYearBonusOverride, worldCupBonusOverride);
        StrengthModel model = getCurrentModelForPredictionDate(predictionDate, weightConfig);
        ExpectedGoals expectedGoals = calculateExpectedGoals(schedule, model, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride);
        return new AdjustedExpectedGoals(expectedGoals.getHomeGoals(), expectedGoals.getAwayGoals(), model.getSampleCount());
    }

    public double getBaselineGoals() {
        return round(currentModel.getBaselineGoals(), 4);
    }

    public int countCompletedScheduleMatches() {
        return (int) dataRepository.getSchedules()
                .stream()
                .filter(this::isCompletedWithScore)
                .count();
    }

    private ExpectedGoals calculateExpectedGoals(MatchSchedule schedule, StrengthModel model, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        TeamStrength homeStrength = getStrength(model, schedule.getHomeTeamEn());
        TeamStrength awayStrength = getStrength(model, schedule.getAwayTeamEn());
        double homeAdvantage = schedule.isNeutral() ? 1.0D : 1.08D;
        double effectiveHostTeamGoalFactor = normalizeTournamentFactor(hostTeamGoalFactorOverride, hostTeamGoalFactor);
        double effectiveSeedTeamGoalFactor = normalizeTournamentFactor(seedTeamGoalFactorOverride, seedTeamGoalFactor);
        double homeTournamentFactor = getTournamentTeamFactor(schedule.getHomeTeamEn(), effectiveHostTeamGoalFactor, effectiveSeedTeamGoalFactor);
        double awayTournamentFactor = getTournamentTeamFactor(schedule.getAwayTeamEn(), effectiveHostTeamGoalFactor, effectiveSeedTeamGoalFactor);
        double h2hFactor = calculateHeadToHeadFactor(schedule.getHomeTeamEn(), schedule.getAwayTeamEn(), model);
        double homeLambda = model.getBaselineGoals() * homeStrength.getAttackStrength() * awayStrength.getDefenseWeakness() * homeAdvantage * homeTournamentFactor * h2hFactor;
        double awayLambda = model.getBaselineGoals() * awayStrength.getAttackStrength() * homeStrength.getDefenseWeakness() * awayTournamentFactor / h2hFactor;
        return new ExpectedGoals(round(clamp(homeLambda), 4), round(clamp(awayLambda), 4));
    }

    private TeamStrength getStrength(StrengthModel model, String teamName) {
        return model.getStrengthMap().getOrDefault(teamName, TeamStrength.defaultOf(teamName, model.getBaselineGoals()));
    }

    private double getTournamentTeamFactor(String teamName, double effectiveHostTeamGoalFactor, double effectiveSeedTeamGoalFactor) {
        if (HOST_TEAMS.contains(teamName)) {
            return effectiveHostTeamGoalFactor;
        }
        if (SEEDED_TEAMS.contains(teamName)) {
            return effectiveSeedTeamGoalFactor;
        }
        return 1.0D;
    }

    private double normalizeTournamentFactor(Double value, double defaultValue) {
        if (value == null || !Double.isFinite(value)) {
            return defaultValue;
        }
        return Math.max(0.1D, Math.min(3.0D, value));
    }

    private List<HistoricalMatch> filterPreTournamentMatches(List<HistoricalMatch> matches) {
        if (matches == null || matches.isEmpty()) {
            return Collections.emptyList();
        }
        return matches.stream()
                .filter(match -> match.getMatchDate().isBefore(TOURNAMENT_START_DATE))
                .toList();
    }

    private List<HistoricalMatch> buildCurrentMatches(List<HistoricalMatch> matches, LocalDate today) {
        List<HistoricalMatch> result = new ArrayList<>();
        if (matches != null) {
            for (HistoricalMatch match : matches) {
                if (match.getMatchDate().isAfter(today) || isWorldCup2026Match(match) || isCurrentModelExcludedDate(match.getMatchDate())) {
                    continue;
                }
                result.add(match);
            }
        }
        for (MatchSchedule schedule : dataRepository.getSchedules()) {
            LocalDate trainingDate = getScheduleTrainingDate(schedule);
            if (!isCompletedWithScore(schedule) || trainingDate.isAfter(today) || isCurrentModelExcludedDate(trainingDate)) {
                continue;
            }
            result.add(toHistoricalMatch(schedule));
        }
        return result;
    }

    private StrengthModel getCurrentModelForPredictionDate(LocalDate predictionDate) {
        return getCurrentModelForPredictionDate(predictionDate, defaultMatchWeightConfig());
    }

    private StrengthModel getCurrentModelForPredictionDate(LocalDate predictionDate, MatchWeightConfig weightConfig) {
        if (!weightConfig.isDefaultOf(defaultMatchWeightConfig())) {
            LocalDate cutoffDate = predictionDate == null ? LocalDate.now() : predictionDate;
            return buildStrengthModel(buildCurrentMatchesBefore(dataRepository.getHistoricalMatches(), cutoffDate), weightConfig);
        }
        if (predictionDate == null) {
            return currentModel;
        }
        return currentModelByPredictionDate.computeIfAbsent(predictionDate, this::buildCurrentModelBefore);
    }

    private StrengthModel buildCurrentModelBefore(LocalDate predictionDate) {
        return buildStrengthModel(buildCurrentMatchesBefore(dataRepository.getHistoricalMatches(), predictionDate));
    }

    private List<HistoricalMatch> buildCurrentMatchesBefore(List<HistoricalMatch> matches, LocalDate cutoffDate) {
        List<HistoricalMatch> result = new ArrayList<>();
        if (matches != null) {
            for (HistoricalMatch match : matches) {
                if (!match.getMatchDate().isBefore(cutoffDate) || isWorldCup2026Match(match) || isCurrentModelExcludedDate(match.getMatchDate())) {
                    continue;
                }
                result.add(match);
            }
        }
        for (MatchSchedule schedule : dataRepository.getSchedules()) {
            LocalDate trainingDate = getScheduleTrainingDate(schedule);
            if (!isCompletedWithScore(schedule) || !trainingDate.isBefore(cutoffDate) || isCurrentModelExcludedDate(trainingDate)) {
                continue;
            }
            result.add(toHistoricalMatch(schedule));
        }
        return result;
    }

    private List<LocalDate> parseExcludedDates(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        List<LocalDate> dates = new ArrayList<>();
        for (String item : value.split(",")) {
            String dateText = item.trim();
            if (!dateText.isBlank()) {
                dates.add(LocalDate.parse(dateText));
            }
        }
        return Collections.unmodifiableList(dates);
    }

    private boolean isCurrentModelExcludedDate(LocalDate date) {
        return currentModelExcludedDates.contains(date);
    }

    private boolean isWorldCup2026Match(HistoricalMatch match) {
        String tournament = match.getTournament() == null ? "" : match.getTournament().toLowerCase(Locale.ROOT);
        return !match.getMatchDate().isBefore(TOURNAMENT_START_DATE)
                && tournament.contains("world cup")
                && tournament.contains("2026");
    }

    private HistoricalMatch toHistoricalMatch(MatchSchedule schedule) {
        HistoricalMatch match = new HistoricalMatch();
        match.setMatchDate(getScheduleTrainingDate(schedule));
        match.setTournament(WORLD_CUP_2026);
        match.setHomeTeam(schedule.getHomeTeamEn());
        match.setAwayTeam(schedule.getAwayTeamEn());
        match.setHomeScore(schedule.getHomeScore());
        match.setAwayScore(schedule.getAwayScore());
        match.setNeutral(schedule.isNeutral());
        return match;
    }

    private LocalDate getScheduleTrainingDate(MatchSchedule schedule) {
        if (LocalTime.MIDNIGHT.equals(schedule.getKickoffTime())) {
            return schedule.getMatchDate().minusDays(1);
        }
        return schedule.getMatchDate();
    }

    private boolean isCompletedWithScore(MatchSchedule schedule) {
        return "COMPLETED".equalsIgnoreCase(schedule.getStatus())
                && schedule.getHomeScore() != null
                && schedule.getAwayScore() != null;
    }

    private StrengthModel buildStrengthModel(List<HistoricalMatch> matches) {
        return buildStrengthModel(matches, defaultMatchWeightConfig());
    }

    private StrengthModel buildStrengthModel(List<HistoricalMatch> matches, MatchWeightConfig weightConfig) {
        if (matches == null || matches.isEmpty()) {
            return StrengthModel.empty();
        }
        LocalDate latestDate = matches.stream()
                .map(HistoricalMatch::getMatchDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        Map<String, TeamStatsAccumulator> statsMap = new HashMap<>();
        double totalGoals = 0.0D;
        double totalAppearances = 0.0D;
        for (HistoricalMatch match : matches) {
            double weight = calculateMatchWeight(match, latestDate, weightConfig);
            if (weight <= 0.0D) {
                continue;
            }
            addTeamStats(statsMap, match.getHomeTeam(), match.getHomeScore(), match.getAwayScore(), weight);
            addTeamStats(statsMap, match.getAwayTeam(), match.getAwayScore(), match.getHomeScore(), weight);
            totalGoals += (match.getHomeScore() + match.getAwayScore()) * weight;
            totalAppearances += 2 * weight;
        }
        if (totalAppearances <= 0.0D) {
            return StrengthModel.empty();
        }
        double baselineGoals = totalAppearances <= 0 ? 1.25D : totalGoals / totalAppearances;
        Map<String, TeamStrength> newStrengthMap = new HashMap<>();
        for (Map.Entry<String, TeamStatsAccumulator> entry : statsMap.entrySet()) {
            TeamStatsAccumulator accumulator = entry.getValue();
            double averageGoalsFor = accumulator.goalsFor / accumulator.weight;
            double averageGoalsAgainst = accumulator.goalsAgainst / accumulator.weight;
            double confidence = accumulator.weight / (accumulator.weight + SHRINK_WEIGHT);
            double rawAttack = safeDivide(averageGoalsFor, baselineGoals, 1.0D);
            double rawDefenseWeakness = safeDivide(averageGoalsAgainst, baselineGoals, 1.0D);
            TeamStrength strength = new TeamStrength();
            strength.setTeamName(entry.getKey());
            strength.setAverageGoalsFor(round(averageGoalsFor, 4));
            strength.setAverageGoalsAgainst(round(averageGoalsAgainst, 4));
            strength.setAttackStrength(round(1 + (rawAttack - 1) * confidence, 4));
            strength.setDefenseWeakness(round(1 + (rawDefenseWeakness - 1) * confidence, 4));
            strength.setSampleWeight(round(accumulator.weight, 4));
            newStrengthMap.put(entry.getKey(), strength);
        }
        return new StrengthModel(newStrengthMap, baselineGoals, matches, latestDate, weightConfig);
    }

    private void addTeamStats(Map<String, TeamStatsAccumulator> statsMap, String teamName, int goalsFor, int goalsAgainst, double weight) {
        TeamStatsAccumulator accumulator = statsMap.computeIfAbsent(teamName, ignored -> new TeamStatsAccumulator());
        accumulator.goalsFor += goalsFor * weight;
        accumulator.goalsAgainst += goalsAgainst * weight;
        accumulator.weight += weight;
    }

    private double calculateMatchWeight(HistoricalMatch match, LocalDate latestDate) {
        return calculateMatchWeight(match, latestDate, defaultMatchWeightConfig());
    }

    private double calculateMatchWeight(HistoricalMatch match, LocalDate latestDate, MatchWeightConfig weightConfig) {
        long days = Math.max(0, ChronoUnit.DAYS.between(match.getMatchDate(), latestDate));
        double recencyWeight = Math.exp(-days / 1825.0D);
        double tournamentWeight = weightConfig.baseMatchWeight();
        String tournament = match.getTournament() == null ? "" : match.getTournament().toLowerCase(Locale.ROOT);
        if (days <= RECENT_HALF_YEAR_DAYS) {
            tournamentWeight += weightConfig.recentHalfYearBonus();
        }
        if (tournament.contains("world cup")) {
            tournamentWeight += weightConfig.worldCupBonus();
        }
        return recencyWeight * tournamentWeight;
    }

    private double calculateHeadToHeadFactor(String homeTeam, String awayTeam, StrengthModel model) {
        double goalDiff = 0.0D;
        double weightSum = 0.0D;
        for (HistoricalMatch match : model.getMatches()) {
            boolean sameDirection = homeTeam.equals(match.getHomeTeam()) && awayTeam.equals(match.getAwayTeam());
            boolean reverseDirection = homeTeam.equals(match.getAwayTeam()) && awayTeam.equals(match.getHomeTeam());
            if (!sameDirection && !reverseDirection) {
                continue;
            }
            double weight = calculateMatchWeight(match, model.getLatestDate(), model.getWeightConfig());
            if (sameDirection) {
                goalDiff += (match.getHomeScore() - match.getAwayScore()) * weight;
            } else {
                goalDiff += (match.getAwayScore() - match.getHomeScore()) * weight;
            }
            weightSum += weight;
        }
        if (weightSum <= 0) {
            return 1.0D;
        }
        double averageGoalDiff = goalDiff / weightSum;
        return Math.max(0.90D, Math.min(1.10D, 1.0D + averageGoalDiff * 0.04D));
    }

    private double safeDivide(double numerator, double denominator, double defaultValue) {
        if (Math.abs(denominator) < 0.000001D) {
            return defaultValue;
        }
        return numerator / denominator;
    }

    private double clamp(double value) {
        return Math.max(minGoalsLambda, Math.min(maxGoalsLambda, value));
    }

    private MatchWeightConfig defaultMatchWeightConfig() {
        return new MatchWeightConfig(
                normalizeWeightValue(null, baseMatchWeight, 0.0D, 5.0D),
                normalizeWeightValue(null, recentHalfYearBonus, 0.0D, 3.0D),
                normalizeWeightValue(null, worldCupBonus, 0.0D, 3.0D)
        );
    }

    private MatchWeightConfig resolveMatchWeightConfig(Double baseMatchWeightOverride, Double recentHalfYearBonusOverride, Double worldCupBonusOverride) {
        MatchWeightConfig defaults = defaultMatchWeightConfig();
        return new MatchWeightConfig(
                normalizeWeightValue(baseMatchWeightOverride, defaults.baseMatchWeight(), 0.0D, 5.0D),
                normalizeWeightValue(recentHalfYearBonusOverride, defaults.recentHalfYearBonus(), 0.0D, 3.0D),
                normalizeWeightValue(worldCupBonusOverride, defaults.worldCupBonus(), 0.0D, 3.0D)
        );
    }

    private double normalizeWeightValue(Double value, double defaultValue, double min, double max) {
        if (value == null || !Double.isFinite(value)) {
            return Math.max(min, Math.min(max, defaultValue));
        }
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private static class StrengthModel {

        private final Map<String, TeamStrength> strengthMap;

        private final double baselineGoals;

        private final List<HistoricalMatch> matches;

        private final LocalDate latestDate;

        private final MatchWeightConfig weightConfig;

        private StrengthModel(Map<String, TeamStrength> strengthMap, double baselineGoals, List<HistoricalMatch> matches, LocalDate latestDate, MatchWeightConfig weightConfig) {
            this.strengthMap = Collections.unmodifiableMap(new HashMap<>(strengthMap));
            this.baselineGoals = baselineGoals;
            this.matches = Collections.unmodifiableList(new ArrayList<>(matches));
            this.latestDate = latestDate;
            this.weightConfig = weightConfig;
        }

        private static StrengthModel empty() {
            return new StrengthModel(Collections.emptyMap(), 1.25D, Collections.emptyList(), LocalDate.now(), MatchWeightConfig.defaults());
        }

        private Map<String, TeamStrength> getStrengthMap() {
            return strengthMap;
        }

        private double getBaselineGoals() {
            return baselineGoals;
        }

        private List<HistoricalMatch> getMatches() {
            return matches;
        }

        private LocalDate getLatestDate() {
            return latestDate;
        }

        private MatchWeightConfig getWeightConfig() {
            return weightConfig;
        }

        private int getSampleCount() {
            return matches.size();
        }

    }

    private record MatchWeightConfig(double baseMatchWeight, double recentHalfYearBonus, double worldCupBonus) {

        private static MatchWeightConfig defaults() {
            return new MatchWeightConfig(DEFAULT_BASE_MATCH_WEIGHT, DEFAULT_RECENT_HALF_YEAR_BONUS, DEFAULT_WORLD_CUP_BONUS);
        }

        private boolean isDefaultOf(MatchWeightConfig other) {
            return Math.abs(baseMatchWeight - other.baseMatchWeight) < 0.000001D
                    && Math.abs(recentHalfYearBonus - other.recentHalfYearBonus) < 0.000001D
                    && Math.abs(worldCupBonus - other.worldCupBonus) < 0.000001D;
        }

    }

    private static class TeamStatsAccumulator {

        private double goalsFor;

        private double goalsAgainst;

        private double weight;

    }

    public static class ExpectedGoals {

        private final double homeGoals;

        private final double awayGoals;

        public ExpectedGoals(double homeGoals, double awayGoals) {
            this.homeGoals = homeGoals;
            this.awayGoals = awayGoals;
        }

        public double getHomeGoals() {
            return homeGoals;
        }

        public double getAwayGoals() {
            return awayGoals;
        }

    }

    public static class AdjustedExpectedGoals extends ExpectedGoals {

        private final int correctionMatchCount;

        public AdjustedExpectedGoals(double homeGoals, double awayGoals, int correctionMatchCount) {
            super(homeGoals, awayGoals);
            this.correctionMatchCount = correctionMatchCount;
        }

        public int getCorrectionMatchCount() {
            return correctionMatchCount;
        }

    }

}
