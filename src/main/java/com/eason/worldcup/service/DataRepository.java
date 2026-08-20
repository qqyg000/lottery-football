package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.HistoricalMatchType;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import com.eason.worldcup.util.ApplicationTime;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import com.eason.worldcup.util.CompetitionDataPolicy;
import com.eason.worldcup.util.CsvUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class DataRepository {

    private static final Logger log = LoggerFactory.getLogger(DataRepository.class);

    private static final int MATCH_DATE_COLUMN = 1;

    private static final int COMPETITION_COLUMN = 2;

    private static final int HOME_TEAM_CN_COLUMN = 3;

    private static final int AWAY_TEAM_CN_COLUMN = 4;

    private static final int HOME_SCORE_COLUMN = 5;

    private static final int AWAY_SCORE_COLUMN = 6;

    private static final int NEUTRAL_COLUMN = 7;

    private static final int MATCH_TYPE_COLUMN = 8;

    private static final int SOURCE_COMPETITION_COLUMN = 9;

    private final ResourceLoader resourceLoader;

    private final OpenFootballScheduleUpdater scheduleUpdater;

    private final EspnScheduleUpdater espnScheduleUpdater;

    private final ClubCompetitionScheduleUpdater clubCompetitionScheduleUpdater;

    private final HistoricalOddsScheduleLoader historicalOddsScheduleLoader;

    private final SportteryMarketSelectionService sportteryMarketSelectionService;

    @Value("${football-data.historical-matches-path:classpath:data/historical_matches.csv}")
    private String historicalMatchesPath;

    @Value("${data-refresh.days-back:30}")
    private int refreshDaysBack;

    @Value("${data-refresh.days-forward:7}")
    private int refreshDaysForward;

    @Value("${data-refresh.target-zone:Asia/Shanghai}")
    private String refreshTargetZone;

    private volatile List<HistoricalMatch> historicalMatches = Collections.emptyList();

    private volatile List<HistoricalMatch> clubHistoricalMatches = Collections.emptyList();

    private volatile List<MatchSchedule> schedules = Collections.emptyList();

    public DataRepository(
            ResourceLoader resourceLoader,
            OpenFootballScheduleUpdater scheduleUpdater,
            EspnScheduleUpdater espnScheduleUpdater,
            ClubCompetitionScheduleUpdater clubCompetitionScheduleUpdater,
            HistoricalOddsScheduleLoader historicalOddsScheduleLoader,
            SportteryMarketSelectionService sportteryMarketSelectionService) {
        this.resourceLoader = resourceLoader;
        this.scheduleUpdater = scheduleUpdater;
        this.espnScheduleUpdater = espnScheduleUpdater;
        this.clubCompetitionScheduleUpdater = clubCompetitionScheduleUpdater;
        this.historicalOddsScheduleLoader = historicalOddsScheduleLoader;
        this.sportteryMarketSelectionService = sportteryMarketSelectionService;
    }

    @PostConstruct
    public void init() {
        reloadData();
    }

    public synchronized void reloadData() {
        reloadData(false);
    }

    private void reloadData(boolean includeSupplementalSources) {
        reloadData(includeSupplementalSources, null);
    }

    private void reloadData(
            boolean includeSupplementalSources,
            BiConsumer<Integer, String> progressConsumer) {
        List<HistoricalMatch> reloadedHistoricalMatches = Collections.unmodifiableList(loadHistoricalMatches());
        List<HistoricalMatch> reloadedClubHistoricalMatches = Collections.unmodifiableList(loadClubHistoricalMatches());
        List<MatchSchedule> reloadedSchedules = Collections.unmodifiableList(loadSchedules(
                includeSupplementalSources,
                progressConsumer));
        this.historicalMatches = reloadedHistoricalMatches;
        this.clubHistoricalMatches = reloadedClubHistoricalMatches;
        this.schedules = reloadedSchedules;
    }

    public void refreshSchedules() {
        refreshSchedules(null);
    }

    public synchronized void refreshSchedules(BiConsumer<Integer, String> progressConsumer) {
        if (historicalMatches.isEmpty() || clubHistoricalMatches.isEmpty()) {
            notifyRefreshProgress(progressConsumer, 26, "正在重新加载历史比赛数据");
            reloadData(true, progressConsumer);
            return;
        }

        List<MatchSchedule> refreshedSchedules = loadSchedules(true, progressConsumer);
        notifyRefreshProgress(progressConsumer, 64, "赛程源已刷新，正在整理刷新窗口数据");
        preserveSchedulesOutsideRefreshWindow(refreshedSchedules, schedules);
        removeExcludedCompetitionSchedules(refreshedSchedules);
        refreshedSchedules.sort(Comparator
                .comparing(MatchSchedule::getMatchDate)
                .thenComparing(MatchSchedule::getKickoffTime));
        this.schedules = Collections.unmodifiableList(refreshedSchedules);
    }

    public List<HistoricalMatch> getHistoricalMatches() {
        return historicalMatches;
    }

    public List<HistoricalMatch> getClubHistoricalMatches() {
        return clubHistoricalMatches;
    }

    public List<HistoricalMatch> getClubHistoricalMatches(Competition competition) {
        if (competition == null || !competition.isClubCompetition()) {
            return Collections.emptyList();
        }
        String tournament = competition.getDisplayName();
        Set<String> competitionTeams = new HashSet<>();
        for (HistoricalMatch match : clubHistoricalMatches) {
            if (!tournament.equals(match.getTournament())) {
                continue;
            }
            competitionTeams.add(normalizeTeamName(match.getHomeTeam()));
            competitionTeams.add(normalizeTeamName(match.getAwayTeam()));
        }
        if (competitionTeams.isEmpty()) {
            return Collections.emptyList();
        }
        return clubHistoricalMatches.stream()
                .filter(match -> tournament.equals(match.getTournament())
                        || competitionTeams.contains(normalizeTeamName(match.getHomeTeam()))
                        || competitionTeams.contains(normalizeTeamName(match.getAwayTeam())))
                .collect(Collectors.toList());
    }

    public List<MatchSchedule> getSchedules() {
        return schedules;
    }

    public List<MatchSchedule> getSchedules(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        return schedules.stream()
                .filter(item -> item.getCompetition() == effectiveCompetition)
                .collect(Collectors.toList());
    }

    public List<MatchSchedule> getCurrentSeasonSchedules(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        LocalDate today = ApplicationTime.today();
        return schedules.stream()
                .filter(item -> item.getCompetition() == effectiveCompetition)
                .filter(item -> effectiveCompetition.isDateInSeason(item.getMatchDate(), today))
                .collect(Collectors.toList());
    }

    public boolean isCurrentSeasonSchedule(MatchSchedule schedule) {
        return schedule != null
                && schedule.getCompetition() != null
                && schedule.getCompetition().isDateInSeason(schedule.getMatchDate(), ApplicationTime.today());
    }

    public List<MatchSchedule> findSchedulesByDate(LocalDate date) {
        return findSchedulesByDate(date, Competition.WORLD_CUP);
    }

    public List<MatchSchedule> findSchedulesByDate(LocalDate date, Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        return schedules.stream()
                .filter(item -> item.getCompetition() == effectiveCompetition)
                .filter(item -> date.equals(item.getMatchDate()))
                .sorted(Comparator.comparingInt(this::getScheduleSortSeconds)
                        .thenComparing(MatchSchedule::getMatchDate)
                        .thenComparing(MatchSchedule::getMatchId))
                .collect(Collectors.toList());
    }

    public List<String> findScheduleDates() {
        return findScheduleDates(Competition.WORLD_CUP);
    }

    public List<String> findScheduleDates(Competition competition) {
        Competition effectiveCompetition = competition == null ? Competition.WORLD_CUP : competition;
        return schedules.stream()
                .filter(item -> item.getCompetition() == effectiveCompetition)
                .filter(item -> item.getMatchDate() != null)
                .filter(this::hasSportteryOdds)
                .map(item -> item.getMatchDate().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean hasSportteryOdds(MatchSchedule schedule) {
        return hasPositiveOdds(schedule.getSportteryNormalOdds())
                || hasPositiveOdds(schedule.getSportteryHandicapOdds())
                || schedule.getSportteryTotalGoalsOdds() != null;
    }

    private boolean hasPositiveOdds(SportteryOdds odds) {
        return odds != null
                && (isPositive(odds.getWin()) || isPositive(odds.getDraw()) || isPositive(odds.getLose()));
    }

    private boolean isPositive(Double value) {
        return value != null && Double.isFinite(value) && value > 0;
    }

    private int getScheduleSortSeconds(MatchSchedule schedule) {
        return schedule.getKickoffTime().toSecondOfDay();
    }

    private List<HistoricalMatch> loadHistoricalMatches() {
        return loadHistoricalMatches(false);
    }

    private List<HistoricalMatch> loadClubHistoricalMatches() {
        return loadHistoricalMatches(true);
    }

    private List<HistoricalMatch> loadHistoricalMatches(boolean clubCompetition) {
        Resource resource = resourceLoader.getResource(historicalMatchesPath);
        List<HistoricalMatch> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                if (first) {
                    first = false;
                    continue;
                }
                List<String> row = CsvUtils.parseLine(line);
                String sourceCompetition = CsvUtils.get(row, SOURCE_COMPETITION_COLUMN);
                if (CompetitionDataPolicy.isExcludedSourceCompetition(sourceCompetition)) {
                    continue;
                }
                Competition competition = Competition.fromSourceCompetition(
                        sourceCompetition,
                        Competition.fromCode(CsvUtils.get(row, COMPETITION_COLUMN)));
                if (competition.isClubCompetition() != clubCompetition) {
                    continue;
                }
                HistoricalMatch match = new HistoricalMatch();
                match.setMatchDate(LocalDate.parse(CsvUtils.get(row, MATCH_DATE_COLUMN)));
                match.setTournament(competition.getDisplayName());
                match.setHomeTeam(ClubTeamNameTranslator.translate(
                        competition,
                        CsvUtils.get(row, HOME_TEAM_CN_COLUMN)));
                match.setAwayTeam(ClubTeamNameTranslator.translate(
                        competition,
                        CsvUtils.get(row, AWAY_TEAM_CN_COLUMN)));
                match.setHomeScore(Integer.parseInt(CsvUtils.get(row, HOME_SCORE_COLUMN)));
                match.setAwayScore(Integer.parseInt(CsvUtils.get(row, AWAY_SCORE_COLUMN)));
                match.setNeutral(CsvUtils.parseBoolean(CsvUtils.get(row, NEUTRAL_COLUMN)));
                match.setMatchType(HistoricalMatchType.fromCode(CsvUtils.get(row, MATCH_TYPE_COLUMN)));
                match.setSourceCompetition(sourceCompetition.isBlank()
                        ? competition.getDisplayName()
                        : sourceCompetition);
                result.add(match);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("读取历史比赛数据失败：" + historicalMatchesPath, ex);
        }
        log.info(
                "Loaded {} {} historical match rows from {}",
                result.size(),
                clubCompetition ? "club" : "national team",
                historicalMatchesPath);
        return result;
    }

    private List<MatchSchedule> loadSchedules(boolean includeSupplementalSources) {
        return loadSchedules(includeSupplementalSources, null);
    }

    private List<MatchSchedule> loadSchedules(
            boolean includeSupplementalSources,
            BiConsumer<Integer, String> progressConsumer) {
        List<MatchSchedule> result = new ArrayList<>();
        notifyRefreshProgress(progressConsumer, 26, "正在刷新世界杯 OpenFootball 赛程");
        scheduleUpdater.updateSchedules(result);
        notifyRefreshProgress(progressConsumer, 29, "OpenFootball 已处理，正在刷新世界杯 ESPN 赛程");
        int espnUpdatedCount = espnScheduleUpdater.updateSchedules(result);
        if (espnUpdatedCount > 0) {
            log.info("Backfilled {} World Cup schedule rows from ESPN scoreboard.", espnUpdatedCount);
        }
        notifyRefreshProgress(progressConsumer, 32, "世界杯 ESPN 已处理，正在刷新欧冠赛程");
        int championsLeagueUpdatedCount = espnScheduleUpdater.updateChampionsLeagueSchedules(result);
        if (championsLeagueUpdatedCount > 0) {
            log.info("Loaded {} Champions League schedule rows from ESPN scoreboard.", championsLeagueUpdatedCount);
        }
        notifyRefreshProgress(progressConsumer, 36, "欧冠赛程已处理，正在刷新俱乐部赛事数据");
        BiConsumer<Integer, String> clubProgressConsumer = progressConsumer == null
                ? null
                : (progress, message) -> notifyRefreshProgress(
                        progressConsumer,
                        mapClubCompetitionProgress(progress),
                        message);
        int clubCompetitionUpdatedCount = clubCompetitionScheduleUpdater.updateSchedules(
                result,
                includeSupplementalSources,
                clubProgressConsumer);
        if (clubCompetitionUpdatedCount > 0) {
            log.info("Loaded {} additional club competition schedule rows.", clubCompetitionUpdatedCount);
        }
        if (sportteryMarketSelectionService != null) {
            notifyRefreshProgress(progressConsumer, 59, "俱乐部赛事已处理，正在合并体彩近期赛程");
            int addedSportteryScheduleCount = sportteryMarketSelectionService
                    .mergeRecentAndUpcomingSchedulesInto(
                            result,
                            refreshDaysBack,
                            refreshDaysForward);
            if (addedSportteryScheduleCount > 0) {
                log.info(
                        "Loaded {} unmatched recent or upcoming Sporttery schedule rows.",
                        addedSportteryScheduleCount);
            }
        }
        notifyRefreshProgress(progressConsumer, 61, "体彩赛程已合并，正在加载历史赔率");
        historicalOddsScheduleLoader.mergeInto(result);
        notifyRefreshProgress(progressConsumer, 62, "历史赔率已加载，正在统一球队名称");
        normalizeScheduleTeamNames(result);
        notifyRefreshProgress(progressConsumer, 63, "球队名称已统一，正在合并重复赛程");
        result = deduplicateSchedulesByFixture(result);
        removeExcludedCompetitionSchedules(result);
        result.sort(Comparator.comparing(MatchSchedule::getMatchDate).thenComparing(MatchSchedule::getKickoffTime));
        return result;
    }

    static int mapClubCompetitionProgress(int progress) {
        int normalizedProgress = Math.max(0, Math.min(100, progress));
        return 36 + (int) Math.round(normalizedProgress * 22.0D / 100.0D);
    }

    private void notifyRefreshProgress(
            BiConsumer<Integer, String> progressConsumer,
            int progress,
            String message) {
        if (progressConsumer != null) {
            progressConsumer.accept(progress, message);
        }
    }

    private void removeExcludedCompetitionSchedules(List<MatchSchedule> schedules) {
        schedules.removeIf(schedule -> schedule != null
                && CompetitionDataPolicy.isExcludedSourceCompetition(schedule.getGroupName()));
    }

    private void normalizeScheduleTeamNames(List<MatchSchedule> schedules) {
        for (MatchSchedule schedule : schedules) {
            Competition competition = Competition.fromSourceCompetition(
                    schedule.getGroupName(),
                    schedule.getCompetition());
            schedule.setCompetition(competition);
            schedule.setHomeTeamCn(resolveStandardTeamName(
                    competition,
                    schedule.getHomeTeamCn(),
                    schedule.getHomeTeamEn()));
            schedule.setAwayTeamCn(resolveStandardTeamName(
                    competition,
                    schedule.getAwayTeamCn(),
                    schedule.getAwayTeamEn()));
            schedule.setSportteryHomeTeamName(ClubTeamNameTranslator.translate(
                    competition,
                    schedule.getSportteryHomeTeamName()));
            schedule.setSportteryAwayTeamName(ClubTeamNameTranslator.translate(
                    competition,
                    schedule.getSportteryAwayTeamName()));
        }
    }

    private String resolveStandardTeamName(
            Competition competition,
            String chineseName,
            String englishName) {
        if (chineseName != null && !chineseName.isBlank()) {
            return ClubTeamNameTranslator.translate(competition, chineseName);
        }
        return ClubTeamNameTranslator.translate(competition, englishName);
    }

    private void preserveSchedulesOutsideRefreshWindow(
            List<MatchSchedule> refreshedSchedules,
            List<MatchSchedule> previousSchedules) {
        if (previousSchedules == null || previousSchedules.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of(refreshTargetZone));
        LocalDate startDate = today.minusDays(normalizeRefreshDays(refreshDaysBack));
        LocalDate endDate = today.plusDays(normalizeRefreshDays(refreshDaysForward));
        Map<String, MatchSchedule> refreshedById = new HashMap<>();
        for (MatchSchedule schedule : refreshedSchedules) {
            refreshedById.put(buildScheduleIdentity(schedule), schedule);
        }

        int preservedCount = 0;
        for (MatchSchedule schedule : previousSchedules) {
            if (schedule.getMatchDate() == null
                    || (!schedule.getMatchDate().isBefore(startDate)
                    && !schedule.getMatchDate().isAfter(endDate))) {
                continue;
            }
            String identity = buildScheduleIdentity(schedule);
            if (refreshedById.containsKey(identity)) {
                continue;
            }
            refreshedSchedules.add(schedule);
            refreshedById.put(identity, schedule);
            preservedCount++;
        }
        if (preservedCount > 0) {
            log.info(
                    "Preserved {} existing schedule rows outside refresh window {} to {}.",
                    preservedCount,
                    startDate,
                    endDate);
        }
    }

    private String buildScheduleIdentity(MatchSchedule schedule) {
        return schedule.getCompetition()
                + "|" + schedule.getMatchDate()
                + "|" + canonicalScheduleTeamName(schedule, true)
                + "|" + canonicalScheduleTeamName(schedule, false)
                + "|" + fixtureCompetitionContext(schedule);
    }

    private String fixtureCompetitionContext(MatchSchedule schedule) {
        return schedule.getCompetition() == Competition.CLUB_OFFICIAL_OTHER
                ? canonicalCompetitionContext(schedule.getGroupName())
                : "";
    }

    List<MatchSchedule> deduplicateSchedulesByFixture(List<MatchSchedule> schedules) {
        normalizeScheduleTeamNames(schedules);
        Map<String, MatchSchedule> schedulesByFixture = new LinkedHashMap<>();
        for (MatchSchedule schedule : schedules) {
            schedulesByFixture.merge(
                    buildScheduleIdentity(schedule),
                    schedule,
                    this::preferSchedule);
        }
        List<MatchSchedule> schedulesByTeam = deduplicateSchedulesByTeamResult(
                new ArrayList<>(schedulesByFixture.values()));
        return deduplicateAdjacentSchedules(schedulesByTeam);
    }

    private List<MatchSchedule> deduplicateAdjacentSchedules(List<MatchSchedule> schedules) {
        Map<String, List<MatchSchedule>> schedulesByTeamDate = new HashMap<>();
        Set<MatchSchedule> duplicateSchedules = new HashSet<>();
        for (MatchSchedule schedule : schedules) {
            if (schedule.getCompetition() == null || schedule.getMatchDate() == null) {
                continue;
            }
            MatchSchedule duplicateCandidate = null;
            for (int dayOffset : List.of(-1, 0, 1)) {
                LocalDate candidateDate = schedule.getMatchDate().plusDays(dayOffset);
                duplicateCandidate = buildAdjacentTeamBuckets(schedule, candidateDate).stream()
                        .flatMap(bucket -> schedulesByTeamDate
                                .getOrDefault(bucket, List.of())
                                .stream())
                        .distinct()
                        .filter(candidate -> isAdjacentFixtureDuplicate(candidate, schedule))
                        .findFirst()
                        .orElse(null);
                if (duplicateCandidate != null) {
                    break;
                }
            }
            if (duplicateCandidate == null) {
                indexAdjacentSchedule(schedulesByTeamDate, schedule);
                continue;
            }

            MatchSchedule preferred = preferSchedule(duplicateCandidate, schedule);
            MatchSchedule duplicate = preferred == duplicateCandidate ? schedule : duplicateCandidate;
            duplicateSchedules.add(duplicate);
            if (preferred == schedule) {
                removeAdjacentSchedule(schedulesByTeamDate, duplicateCandidate);
                indexAdjacentSchedule(schedulesByTeamDate, schedule);
            }
        }
        return schedules.stream()
                .filter(schedule -> !duplicateSchedules.contains(schedule))
                .collect(Collectors.toList());
    }

    private void indexAdjacentSchedule(
            Map<String, List<MatchSchedule>> schedulesByTeamDate,
            MatchSchedule schedule) {
        for (String bucket : buildAdjacentTeamBuckets(schedule, schedule.getMatchDate())) {
            schedulesByTeamDate.computeIfAbsent(bucket, ignored -> new ArrayList<>()).add(schedule);
        }
    }

    private void removeAdjacentSchedule(
            Map<String, List<MatchSchedule>> schedulesByTeamDate,
            MatchSchedule schedule) {
        for (String bucket : buildAdjacentTeamBuckets(schedule, schedule.getMatchDate())) {
            List<MatchSchedule> bucketSchedules = schedulesByTeamDate.get(bucket);
            if (bucketSchedules != null) {
                bucketSchedules.remove(schedule);
            }
        }
    }

    private List<String> buildAdjacentTeamBuckets(MatchSchedule schedule, LocalDate matchDate) {
        return List.of(
                        canonicalScheduleTeamName(schedule, true),
                        canonicalScheduleTeamName(schedule, false))
                .stream()
                .filter(teamName -> !teamName.isBlank())
                .map(teamName -> schedule.getCompetition() + "|" + matchDate + "|" + teamName)
                .distinct()
                .toList();
    }

    private boolean isAdjacentFixtureDuplicate(MatchSchedule left, MatchSchedule right) {
        if (!hasCompatibleCompetitionContext(left, right)) {
            return false;
        }
        long dateDifference = Math.abs(ChronoUnit.DAYS.between(
                left.getMatchDate(),
                right.getMatchDate()));
        if (dateDifference > 1) {
            return false;
        }

        boolean directResult = Objects.equals(left.getHomeScore(), right.getHomeScore())
                && Objects.equals(left.getAwayScore(), right.getAwayScore());
        boolean reversedResult = Objects.equals(left.getHomeScore(), right.getAwayScore())
                && Objects.equals(left.getAwayScore(), right.getHomeScore());
        boolean directTeams = areSimilarScheduleTeams(left, true, right, true)
                && areSimilarScheduleTeams(left, false, right, false);
        boolean reversedTeams = areSimilarScheduleTeams(left, true, right, false)
                && areSimilarScheduleTeams(left, false, right, true);
        boolean bothCompleted = left.getHomeScore() != null
                && left.getAwayScore() != null
                && right.getHomeScore() != null
                && right.getAwayScore() != null;
        if (directTeams && (!bothCompleted || directResult)
                || reversedTeams && (!bothCompleted || reversedResult)) {
            return true;
        }

        return bothCompleted
                && (hasSportteryIdentity(left)
                        || hasSportteryIdentity(right)
                        || hasDifferentScheduleProvider(left, right))
                && dateDifference == 0
                && left.getKickoffTime() != null
                && Objects.equals(left.getKickoffTime(), right.getKickoffTime())
                && (directResult && (areSimilarScheduleTeams(left, true, right, true)
                        || areSimilarScheduleTeams(left, false, right, false))
                        || reversedResult && (areSimilarScheduleTeams(left, true, right, false)
                        || areSimilarScheduleTeams(left, false, right, true)));
    }

    private boolean hasDifferentScheduleProvider(MatchSchedule left, MatchSchedule right) {
        String leftProvider = scheduleProvider(left);
        String rightProvider = scheduleProvider(right);
        return !leftProvider.isBlank()
                && !rightProvider.isBlank()
                && !leftProvider.equals(rightProvider);
    }

    private String scheduleProvider(MatchSchedule schedule) {
        String matchId = schedule.getMatchId();
        int separatorIndex = matchId == null ? -1 : matchId.indexOf('-');
        return separatorIndex <= 0 ? "" : matchId.substring(0, separatorIndex);
    }

    private boolean hasCompatibleCompetitionContext(MatchSchedule left, MatchSchedule right) {
        if (left.getCompetition() != Competition.CLUB_OFFICIAL_OTHER) {
            return true;
        }
        String leftContext = canonicalCompetitionContext(left.getGroupName());
        String rightContext = canonicalCompetitionContext(right.getGroupName());
        return leftContext.isBlank()
                || rightContext.isBlank()
                || leftContext.equals(rightContext);
    }

    private String canonicalCompetitionContext(String groupName) {
        String withoutRound = groupName == null
                ? ""
                : groupName.replaceFirst("\\s*第?\\s*\\d+\\s*轮\\s*$", "");
        return normalizeTeamName(withoutRound).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private boolean areSimilarScheduleTeams(
            MatchSchedule left,
            boolean leftHomeTeam,
            MatchSchedule right,
            boolean rightHomeTeam) {
        for (String leftName : resolveScheduleTeamNames(left, leftHomeTeam)) {
            for (String rightName : resolveScheduleTeamNames(right, rightHomeTeam)) {
                if (areSimilarTeamNames(leftName, rightName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> resolveScheduleTeamNames(MatchSchedule schedule, boolean homeTeam) {
        String chineseName = homeTeam ? schedule.getHomeTeamCn() : schedule.getAwayTeamCn();
        String englishName = homeTeam ? schedule.getHomeTeamEn() : schedule.getAwayTeamEn();
        return List.of(
                        chineseName == null ? "" : chineseName,
                        englishName == null ? "" : englishName,
                        ClubTeamNameTranslator.translate(schedule.getCompetition(), chineseName),
                        ClubTeamNameTranslator.translate(schedule.getCompetition(), englishName))
                .stream()
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }

    private List<MatchSchedule> deduplicateSchedulesByTeamResult(List<MatchSchedule> schedules) {
        Map<String, MatchSchedule> schedulesByTeamResult = new HashMap<>();
        Set<MatchSchedule> duplicateSchedules = new HashSet<>();
        for (MatchSchedule schedule : schedules) {
            if (schedule.getCompetition() == Competition.CLUB_FRIENDLY
                    || schedule.getHomeScore() == null
                    || schedule.getAwayScore() == null) {
                continue;
            }
            String homeKey = buildTeamResultIdentity(schedule, true);
            String awayKey = buildTeamResultIdentity(schedule, false);
            MatchSchedule existing = schedulesByTeamResult.get(homeKey);
            if (existing == null) {
                existing = schedulesByTeamResult.get(awayKey);
            }
            if (existing == null) {
                schedulesByTeamResult.put(homeKey, schedule);
                schedulesByTeamResult.put(awayKey, schedule);
                continue;
            }
            if (!isLikelyDuplicateSchedule(existing, schedule)) {
                schedulesByTeamResult.put(homeKey, schedule);
                schedulesByTeamResult.put(awayKey, schedule);
                continue;
            }

            MatchSchedule preferred = preferSchedule(existing, schedule);
            MatchSchedule duplicate = preferred == existing ? schedule : existing;
            duplicateSchedules.add(duplicate);
            for (String key : List.of(
                    buildTeamResultIdentity(existing, true),
                    buildTeamResultIdentity(existing, false),
                    homeKey,
                    awayKey)) {
                schedulesByTeamResult.put(key, preferred);
            }
        }
        return schedules.stream()
                .filter(schedule -> !duplicateSchedules.contains(schedule))
                .collect(Collectors.toList());
    }

    private String buildTeamResultIdentity(MatchSchedule schedule, boolean homeTeam) {
        int goalsFor = homeTeam ? schedule.getHomeScore() : schedule.getAwayScore();
        int goalsAgainst = homeTeam ? schedule.getAwayScore() : schedule.getHomeScore();
        return schedule.getCompetition()
                + "|" + schedule.getMatchDate()
                + "|" + canonicalScheduleTeamName(schedule, homeTeam)
                + "|" + goalsFor
                + "|" + goalsAgainst;
    }

    private boolean isLikelyDuplicateSchedule(MatchSchedule left, MatchSchedule right) {
        if (!hasCompatibleCompetitionContext(left, right)) {
            return false;
        }
        if (hasSportteryIdentity(left) || hasSportteryIdentity(right)) {
            return true;
        }
        String leftOpponent = resolveDifferentOpponentName(left, right);
        String rightOpponent = resolveDifferentOpponentName(right, left);
        return areSimilarTeamNames(leftOpponent, rightOpponent);
    }

    private boolean hasSportteryIdentity(MatchSchedule schedule) {
        return schedule.getSportteryMatchId() != null && !schedule.getSportteryMatchId().isBlank();
    }

    private String resolveDifferentOpponentName(MatchSchedule source, MatchSchedule other) {
        String sourceHome = canonicalScheduleTeamName(source, true);
        String otherHome = canonicalScheduleTeamName(other, true);
        String otherAway = canonicalScheduleTeamName(other, false);
        if (sourceHome.equals(otherHome) || sourceHome.equals(otherAway)) {
            return source.getAwayTeamCn();
        }
        return source.getHomeTeamCn();
    }

    private boolean areSimilarTeamNames(String left, String right) {
        String leftName = normalizeTeamName(left).replaceAll("[^\\p{L}\\p{N}]", "");
        String rightName = normalizeTeamName(right).replaceAll("[^\\p{L}\\p{N}]", "");
        return (!leftName.isBlank() && leftName.equals(rightName))
                || (leftName.length() >= 3
                && rightName.length() >= 3
                && (leftName.contains(rightName) || rightName.contains(leftName)));
    }

    private String canonicalScheduleTeamName(MatchSchedule schedule, boolean homeTeam) {
        String chineseName = homeTeam ? schedule.getHomeTeamCn() : schedule.getAwayTeamCn();
        String englishName = homeTeam ? schedule.getHomeTeamEn() : schedule.getAwayTeamEn();
        String sourceName = chineseName == null || chineseName.isBlank() ? englishName : chineseName;
        return normalizeTeamName(ClubTeamNameTranslator.translate(schedule.getCompetition(), sourceName))
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private MatchSchedule preferSchedule(MatchSchedule current, MatchSchedule candidate) {
        return scheduleQuality(candidate) > scheduleQuality(current) ? candidate : current;
    }

    private int scheduleQuality(MatchSchedule schedule) {
        int quality = 0;
        if (schedule.getSportteryNormalOdds() != null
                || schedule.getSportteryHandicapOdds() != null
                || schedule.getSportteryTotalGoalsOdds() != null) {
            quality += 4;
        }
        if (schedule.getSportteryMatchId() != null && !schedule.getSportteryMatchId().isBlank()) {
            quality += 2;
        }
        if ("COMPLETED".equalsIgnoreCase(schedule.getStatus())
                && schedule.getHomeScore() != null
                && schedule.getAwayScore() != null) {
            quality++;
        }
        return quality;
    }

    private int normalizeRefreshDays(int value) {
        return Math.max(0, Math.min(365, value));
    }

    private String normalizeTeamName(String teamName) {
        String cleaned = teamName == null ? "" : teamName.trim().replaceAll("\\s+", " ");
        return Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

}
