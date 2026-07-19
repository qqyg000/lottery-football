package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.HistoricalMatchType;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.TeamStrength;
import com.eason.worldcup.util.ApplicationTime;
import com.eason.worldcup.util.DixonColesWeightModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TeamStrengthService {

    private static final double SHRINK_WEIGHT = 8.0D;
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

    @Value("${worldcup.host-team-goal-factor:1.55}")
    private double hostTeamGoalFactor;

    @Value("${worldcup.home-team-goal-factor:1.05}")
    private double homeTeamGoalFactor;

    @Value("${worldcup.seed-team-goal-factor:1.85}")
    private double seedTeamGoalFactor;

    @Value("${worldcup.current-model-excluded-dates:}")
    private String currentModelExcludedDateText;

    private volatile List<LocalDate> currentModelExcludedDates = Collections.emptyList();

    private volatile StrengthModel preTournamentModel = StrengthModel.empty();

    private volatile StrengthModel currentModel = StrengthModel.empty();

    private final Map<LocalDate, StrengthModel> currentModelByPredictionDate = new ConcurrentHashMap<>();

    private final Map<CompetitionDateKey, StrengthModel> clubPreSeasonModelByStartDate = new ConcurrentHashMap<>();

    private final Map<CompetitionDateKey, StrengthModel> clubModelByPredictionDate = new ConcurrentHashMap<>();

    public TeamStrengthService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @PostConstruct
    public void init() {
        rebuildModels();
    }

    public synchronized void rebuildModels() {
        List<HistoricalMatch> historicalMatches = dataRepository.getHistoricalMatches();
        LocalDate today = ApplicationTime.today();
        currentModelExcludedDates = parseExcludedDates(currentModelExcludedDateText);
        currentModelByPredictionDate.clear();
        clubPreSeasonModelByStartDate.clear();
        clubModelByPredictionDate.clear();
        preTournamentModel = buildStrengthModel(
                filterPreTournamentMatches(historicalMatches),
                TOURNAMENT_START_DATE);
        currentModel = buildStrengthModel(
                buildCurrentMatches(historicalMatches, today),
                today);
    }

    public AdjustedExpectedGoals calculatePreTournamentExpectedGoals(MatchSchedule schedule) {
        return calculatePreTournamentExpectedGoals(schedule, null, null);
    }

    public AdjustedExpectedGoals calculatePreTournamentExpectedGoals(MatchSchedule schedule, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        return calculatePreTournamentExpectedGoals(schedule, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride, null);
    }

    public AdjustedExpectedGoals calculatePreTournamentExpectedGoals(
            MatchSchedule schedule,
            Double hostTeamGoalFactorOverride,
            Double seedTeamGoalFactorOverride,
            Double homeTeamGoalFactorOverride) {
        StrengthModel model = schedule.getCompetition().isClubCompetition()
                ? getClubPreSeasonModel(schedule.getCompetition(), schedule.getMatchDate())
                : preTournamentModel;
        ExpectedGoals expectedGoals = calculateExpectedGoals(
                schedule,
                model,
                hostTeamGoalFactorOverride,
                seedTeamGoalFactorOverride,
                homeTeamGoalFactorOverride);
        return new AdjustedExpectedGoals(expectedGoals.getHomeGoals(), expectedGoals.getAwayGoals(), model.getSampleCount());
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule) {
        return calculateCurrentExpectedGoals(schedule, null, null);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        return calculateCurrentExpectedGoals(schedule, null, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule, LocalDate predictionDate) {
        return calculateCurrentExpectedGoals(schedule, predictionDate, null, null);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(MatchSchedule schedule, LocalDate predictionDate, Double hostTeamGoalFactorOverride, Double seedTeamGoalFactorOverride) {
        return calculateCurrentExpectedGoals(schedule, predictionDate, hostTeamGoalFactorOverride, seedTeamGoalFactorOverride, null);
    }

    public AdjustedExpectedGoals calculateCurrentExpectedGoals(
            MatchSchedule schedule,
            LocalDate predictionDate,
            Double hostTeamGoalFactorOverride,
            Double seedTeamGoalFactorOverride,
            Double homeTeamGoalFactorOverride) {
        StrengthModel model = schedule.getCompetition().isClubCompetition()
                ? getClubModelForPredictionDate(schedule.getCompetition(), predictionDate)
                : getCurrentModelForPredictionDate(predictionDate);
        ExpectedGoals expectedGoals = calculateExpectedGoals(
                schedule,
                model,
                hostTeamGoalFactorOverride,
                seedTeamGoalFactorOverride,
                homeTeamGoalFactorOverride);
        return new AdjustedExpectedGoals(expectedGoals.getHomeGoals(), expectedGoals.getAwayGoals(), model.getSampleCount());
    }

    public double getBaselineGoals() {
        return getBaselineGoals(Competition.WORLD_CUP);
    }

    public double getBaselineGoals(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        StrengthModel model = effectiveCompetition.isClubCompetition()
                ? getClubModelForPredictionDate(effectiveCompetition, ApplicationTime.today().plusDays(1))
                : currentModel;
        return round(model.getBaselineGoals(), 4);
    }

    public int countHistoricalMatches(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        if (!effectiveCompetition.isClubCompetition()) {
            return dataRepository.getHistoricalMatches().size();
        }
        return buildClubMatchesBefore(effectiveCompetition, LocalDate.MAX).size();
    }

    public int countCompletedScheduleMatches() {
        return countCompletedScheduleMatches(Competition.WORLD_CUP);
    }

    public int countCompletedScheduleMatches(Competition competition) {
        return (int) dataRepository.getCurrentSeasonSchedules(competition)
                .stream()
                .filter(this::isCompletedWithScore)
                .count();
    }

    private ExpectedGoals calculateExpectedGoals(
            MatchSchedule schedule,
            StrengthModel model,
            Double hostTeamGoalFactorOverride,
            Double seedTeamGoalFactorOverride,
            Double homeTeamGoalFactorOverride) {
        String homeModelTeam = getModelTeamName(schedule, true);
        String awayModelTeam = getModelTeamName(schedule, false);
        TeamStrength homeStrength = getStrength(model, homeModelTeam);
        TeamStrength awayStrength = getStrength(model, awayModelTeam);
        double homeAdvantage = getHomeAdvantage(schedule, homeTeamGoalFactorOverride);
        double effectiveHostTeamGoalFactor = normalizeGoalFactor(hostTeamGoalFactorOverride, hostTeamGoalFactor);
        double effectiveSeedTeamGoalFactor = normalizeGoalFactor(seedTeamGoalFactorOverride, seedTeamGoalFactor);
        double homeTournamentFactor = getTournamentTeamFactor(schedule, schedule.getHomeTeamEn(), effectiveHostTeamGoalFactor, effectiveSeedTeamGoalFactor);
        double awayTournamentFactor = getTournamentTeamFactor(schedule, schedule.getAwayTeamEn(), effectiveHostTeamGoalFactor, effectiveSeedTeamGoalFactor);
        double h2hFactor = calculateHeadToHeadFactor(homeModelTeam, awayModelTeam, model);
        double homeLambda = model.getBaselineGoals() * homeStrength.getAttackStrength() * awayStrength.getDefenseWeakness() * homeAdvantage * homeTournamentFactor * h2hFactor;
        double awayLambda = model.getBaselineGoals() * awayStrength.getAttackStrength() * homeStrength.getDefenseWeakness() * awayTournamentFactor / h2hFactor;
        return new ExpectedGoals(round(clamp(homeLambda), 4), round(clamp(awayLambda), 4));
    }

    private TeamStrength getStrength(StrengthModel model, String teamName) {
        return model.getStrengthMap().getOrDefault(teamName, TeamStrength.defaultOf(teamName, model.getBaselineGoals()));
    }

    private double getTournamentTeamFactor(MatchSchedule schedule, String teamName, double effectiveHostTeamGoalFactor, double effectiveSeedTeamGoalFactor) {
        if (schedule.getCompetition() != Competition.WORLD_CUP) {
            return 1.0D;
        }
        if (HOST_TEAMS.contains(teamName)) {
            return effectiveHostTeamGoalFactor;
        }
        if (SEEDED_TEAMS.contains(teamName)) {
            return effectiveSeedTeamGoalFactor;
        }
        return 1.0D;
    }

    private double getHomeAdvantage(MatchSchedule schedule, Double homeTeamGoalFactorOverride) {
        if (schedule.isNeutral()) {
            return 1.0D;
        }
        if (schedule.getCompetition() == Competition.WORLD_CUP) {
            return normalizeGoalFactor(null, homeTeamGoalFactor);
        }
        return normalizeGoalFactor(homeTeamGoalFactorOverride, homeTeamGoalFactor);
    }

    private double normalizeGoalFactor(Double value, double defaultValue) {
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
        Map<String, HistoricalMatch> matchesByFixture = new LinkedHashMap<>();
        if (matches != null) {
            for (HistoricalMatch match : matches) {
                if (match.getMatchDate().isAfter(today) || isWorldCup2026Match(match) || isCurrentModelExcludedDate(match.getMatchDate())) {
                    continue;
                }
                matchesByFixture.put(buildFixtureKey(match), match);
            }
        }
        for (MatchSchedule schedule : dataRepository.getSchedules()) {
            if (schedule.getCompetition().isClubCompetition()) {
                continue;
            }
            LocalDate trainingDate = getScheduleTrainingDate(schedule);
            if (!isCompletedWithScore(schedule) || trainingDate.isAfter(today) || isCurrentModelExcludedDate(trainingDate)) {
                continue;
            }
            HistoricalMatch historicalMatch = toHistoricalMatch(schedule);
            matchesByFixture.put(buildFixtureKey(historicalMatch), historicalMatch);
        }
        return new ArrayList<>(matchesByFixture.values());
    }

    private StrengthModel getCurrentModelForPredictionDate(LocalDate predictionDate) {
        if (predictionDate == null) {
            return currentModel;
        }
        return currentModelByPredictionDate.computeIfAbsent(predictionDate, this::buildCurrentModelBefore);
    }

    private StrengthModel buildCurrentModelBefore(LocalDate predictionDate) {
        return buildStrengthModel(
                buildCurrentMatchesBefore(dataRepository.getHistoricalMatches(), predictionDate),
                predictionDate);
    }

    private List<HistoricalMatch> buildCurrentMatchesBefore(List<HistoricalMatch> matches, LocalDate cutoffDate) {
        Map<String, HistoricalMatch> matchesByFixture = new LinkedHashMap<>();
        if (matches != null) {
            for (HistoricalMatch match : matches) {
                if (!match.getMatchDate().isBefore(cutoffDate) || isWorldCup2026Match(match) || isCurrentModelExcludedDate(match.getMatchDate())) {
                    continue;
                }
                matchesByFixture.put(buildFixtureKey(match), match);
            }
        }
        for (MatchSchedule schedule : dataRepository.getSchedules()) {
            if (schedule.getCompetition().isClubCompetition()) {
                continue;
            }
            LocalDate trainingDate = getScheduleTrainingDate(schedule);
            if (!isCompletedWithScore(schedule) || !trainingDate.isBefore(cutoffDate) || isCurrentModelExcludedDate(trainingDate)) {
                continue;
            }
            HistoricalMatch historicalMatch = toHistoricalMatch(schedule);
            matchesByFixture.put(buildFixtureKey(historicalMatch), historicalMatch);
        }
        return new ArrayList<>(matchesByFixture.values());
    }

    private StrengthModel getClubPreSeasonModel(
            Competition competition,
            LocalDate matchDate) {
        LocalDate effectiveMatchDate = matchDate == null ? ApplicationTime.today() : matchDate;
        LocalDate seasonStartDate = competition.getSeasonStartDate(effectiveMatchDate);
        CompetitionDateKey key = new CompetitionDateKey(competition, effectiveMatchDate);
        return clubPreSeasonModelByStartDate.computeIfAbsent(
                key,
                ignored -> buildStrengthModel(
                        buildClubMatchesBefore(competition, seasonStartDate),
                        effectiveMatchDate));
    }

    private StrengthModel getClubModelForPredictionDate(
            Competition competition,
            LocalDate predictionDate) {
        LocalDate cutoffDate = predictionDate == null ? ApplicationTime.today().plusDays(1) : predictionDate;
        CompetitionDateKey key = new CompetitionDateKey(competition, cutoffDate);
        return clubModelByPredictionDate.computeIfAbsent(
                key,
                ignored -> buildStrengthModel(
                        buildClubMatchesBefore(competition, cutoffDate),
                        cutoffDate));
    }

    private List<HistoricalMatch> buildClubMatchesBefore(Competition competition, LocalDate cutoffDate) {
        Map<String, HistoricalMatch> matchesByFixture = new LinkedHashMap<>();
        Set<String> competitionTeams = getClubCompetitionTeams(competition);
        for (HistoricalMatch historicalMatch : dataRepository.getClubHistoricalMatches(competition)) {
            LocalDate matchDate = historicalMatch.getMatchDate();
            if (matchDate == null
                    || !matchDate.isBefore(cutoffDate)
                    || isCurrentModelExcludedDate(matchDate)) {
                continue;
            }
            matchesByFixture.put(buildClubFixtureKey(historicalMatch), historicalMatch);
        }
        for (MatchSchedule schedule : dataRepository.getSchedules()) {
            LocalDate trainingDate = getScheduleTrainingDate(schedule);
            if (!schedule.getCompetition().isClubCompetition()
                    || !isClubCompetitionTeamMatch(schedule, competition, competitionTeams)
                    || !isCompletedWithScore(schedule)
                    || !trainingDate.isBefore(cutoffDate)
                    || isCurrentModelExcludedDate(trainingDate)) {
                continue;
            }
            HistoricalMatch historicalMatch = toHistoricalMatch(schedule);
            matchesByFixture.put(buildClubFixtureKey(historicalMatch), historicalMatch);
        }
        return new ArrayList<>(matchesByFixture.values());
    }

    private Set<String> getClubCompetitionTeams(Competition competition) {
        Set<String> competitionTeams = new HashSet<>();
        String tournament = competition.getDisplayName();
        for (HistoricalMatch match : dataRepository.getClubHistoricalMatches()) {
            if (!tournament.equals(match.getTournament())) {
                continue;
            }
            competitionTeams.add(normalizeClubTeamName(match.getHomeTeam()));
            competitionTeams.add(normalizeClubTeamName(match.getAwayTeam()));
        }
        for (MatchSchedule schedule : dataRepository.getSchedules(competition)) {
            competitionTeams.add(normalizeClubTeamName(getModelTeamName(schedule, true)));
            competitionTeams.add(normalizeClubTeamName(getModelTeamName(schedule, false)));
        }
        return competitionTeams;
    }

    private boolean isClubCompetitionTeamMatch(
            MatchSchedule schedule,
            Competition competition,
            Set<String> competitionTeams) {
        return schedule.getCompetition() == competition
                || competitionTeams.contains(normalizeClubTeamName(getModelTeamName(schedule, true)))
                || competitionTeams.contains(normalizeClubTeamName(getModelTeamName(schedule, false)));
    }

    private String buildClubFixtureKey(HistoricalMatch match) {
        return buildFixtureKey(match);
    }

    private String buildFixtureKey(HistoricalMatch match) {
        String homeTeam = normalizeClubTeamName(match.getHomeTeam());
        String awayTeam = normalizeClubTeamName(match.getAwayTeam());
        if (homeTeam.compareTo(awayTeam) <= 0) {
            return match.getMatchDate() + "|" + homeTeam + "|" + awayTeam;
        }
        return match.getMatchDate() + "|" + awayTeam + "|" + homeTeam;
    }

    private String normalizeClubTeamName(String teamName) {
        return Normalizer.normalize(teamName == null ? "" : teamName, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
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
                && (tournament.contains("world cup") || tournament.contains("世界杯"));
    }

    private HistoricalMatch toHistoricalMatch(MatchSchedule schedule) {
        HistoricalMatch match = new HistoricalMatch();
        match.setMatchDate(getScheduleTrainingDate(schedule));
        match.setTournament(schedule.getCompetition() == Competition.WORLD_CUP
                ? WORLD_CUP_2026
                : schedule.getCompetition().getDisplayName());
        match.setSourceCompetition(schedule.getCompetition().getDisplayName());
        match.setMatchType(HistoricalMatchType.fromCompetition(schedule.getCompetition()));
        match.setHomeTeam(getModelTeamName(schedule, true));
        match.setAwayTeam(getModelTeamName(schedule, false));
        match.setHomeScore(schedule.getHomeScore());
        match.setAwayScore(schedule.getAwayScore());
        match.setNeutral(schedule.isNeutral());
        return match;
    }

    private String getModelTeamName(MatchSchedule schedule, boolean homeTeam) {
        String chineseName = homeTeam ? schedule.getHomeTeamCn() : schedule.getAwayTeamCn();
        if (chineseName != null && !chineseName.isBlank()) {
            return chineseName;
        }
        return homeTeam ? schedule.getHomeTeamEn() : schedule.getAwayTeamEn();
    }

    private LocalDate getScheduleTrainingDate(MatchSchedule schedule) {
        return schedule.getMatchDate();
    }

    private boolean isCompletedWithScore(MatchSchedule schedule) {
        return "COMPLETED".equalsIgnoreCase(schedule.getStatus())
                && schedule.getHomeScore() != null
                && schedule.getAwayScore() != null;
    }

    private StrengthModel buildStrengthModel(List<HistoricalMatch> matches, LocalDate referenceDate) {
        if (matches == null || matches.isEmpty()) {
            return StrengthModel.empty();
        }
        LocalDate latestMatchDate = matches.stream()
                .map(HistoricalMatch::getMatchDate)
                .max(LocalDate::compareTo)
                .orElse(ApplicationTime.today());
        LocalDate effectiveReferenceDate = referenceDate != null && referenceDate.isAfter(latestMatchDate)
                ? referenceDate
                : latestMatchDate;
        Map<String, TeamStatsAccumulator> statsMap = new HashMap<>();
        double totalGoals = 0.0D;
        double totalAppearances = 0.0D;
        for (HistoricalMatch match : matches) {
            double weight = calculateMatchWeight(match, effectiveReferenceDate);
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
        return new StrengthModel(newStrengthMap, baselineGoals, matches, effectiveReferenceDate);
    }

    private void addTeamStats(Map<String, TeamStatsAccumulator> statsMap, String teamName, int goalsFor, int goalsAgainst, double weight) {
        TeamStatsAccumulator accumulator = statsMap.computeIfAbsent(teamName, ignored -> new TeamStatsAccumulator());
        accumulator.goalsFor += goalsFor * weight;
        accumulator.goalsAgainst += goalsAgainst * weight;
        accumulator.weight += weight;
    }

    private double calculateMatchWeight(HistoricalMatch match, LocalDate latestDate) {
        long days = Math.max(0, ChronoUnit.DAYS.between(match.getMatchDate(), latestDate));
        return DixonColesWeightModel.weightForDays(days) * match.getMatchType().getModelWeight();
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
            double weight = calculateMatchWeight(match, model.getLatestDate());
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

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private static class StrengthModel {

        private final Map<String, TeamStrength> strengthMap;

        private final double baselineGoals;

        private final List<HistoricalMatch> matches;

        private final LocalDate latestDate;

        private StrengthModel(Map<String, TeamStrength> strengthMap, double baselineGoals, List<HistoricalMatch> matches, LocalDate latestDate) {
            this.strengthMap = Collections.unmodifiableMap(new HashMap<>(strengthMap));
            this.baselineGoals = baselineGoals;
            this.matches = Collections.unmodifiableList(new ArrayList<>(matches));
            this.latestDate = latestDate;
        }

        private static StrengthModel empty() {
            return new StrengthModel(Collections.emptyMap(), 1.25D, Collections.emptyList(), ApplicationTime.today());
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

        private int getSampleCount() {
            return matches.size();
        }

    }

    private record CompetitionDateKey(Competition competition, LocalDate date) {

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
