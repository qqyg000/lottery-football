package com.eason.worldcup.service;

import com.eason.worldcup.model.HistoricalMatch;
import com.eason.worldcup.model.MatchSchedule;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataRepository {

    private static final Logger log = LoggerFactory.getLogger(DataRepository.class);

    private final ResourceLoader resourceLoader;

    private final OpenFootballScheduleUpdater scheduleUpdater;

    private final EspnScheduleUpdater espnScheduleUpdater;

    @Value("${worldcup.history-path:classpath:data/history_matches.csv}")
    private String historyPath;

    @Value("${worldcup.schedule-path:classpath:data/schedule_2026.csv}")
    private String schedulePath;

    private volatile List<HistoricalMatch> historicalMatches = Collections.emptyList();

    private volatile List<MatchSchedule> schedules = Collections.emptyList();

    public DataRepository(ResourceLoader resourceLoader, OpenFootballScheduleUpdater scheduleUpdater, EspnScheduleUpdater espnScheduleUpdater) {
        this.resourceLoader = resourceLoader;
        this.scheduleUpdater = scheduleUpdater;
        this.espnScheduleUpdater = espnScheduleUpdater;
    }

    @PostConstruct
    public void init() {
        reloadData();
    }

    public synchronized void reloadData() {
        List<HistoricalMatch> reloadedHistoricalMatches = Collections.unmodifiableList(loadHistoricalMatches());
        List<MatchSchedule> reloadedSchedules = Collections.unmodifiableList(loadSchedules(reloadedHistoricalMatches));
        this.historicalMatches = reloadedHistoricalMatches;
        this.schedules = reloadedSchedules;
    }

    public List<HistoricalMatch> getHistoricalMatches() {
        return historicalMatches;
    }

    public List<MatchSchedule> getSchedules() {
        return schedules;
    }

    public List<MatchSchedule> findSchedulesByDate(LocalDate date) {
        return schedules.stream()
                .filter(item -> getScheduleQueryDate(item).equals(date))
                .sorted(Comparator.comparingInt(this::getScheduleSortSeconds)
                        .thenComparing(MatchSchedule::getMatchDate)
                        .thenComparing(MatchSchedule::getMatchId))
                .collect(Collectors.toList());
    }

    public List<String> findScheduleDates() {
        return schedules.stream()
                .map(item -> getScheduleQueryDate(item).toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private LocalDate getScheduleQueryDate(MatchSchedule schedule) {
        if (LocalTime.MIDNIGHT.equals(schedule.getKickoffTime())) {
            return schedule.getMatchDate().minusDays(1);
        }
        return schedule.getMatchDate();
    }

    private int getScheduleSortSeconds(MatchSchedule schedule) {
        if (LocalTime.MIDNIGHT.equals(schedule.getKickoffTime())) {
            return 24 * 60 * 60;
        }
        return schedule.getKickoffTime().toSecondOfDay();
    }

    private List<HistoricalMatch> loadHistoricalMatches() {
        Resource resource = resourceLoader.getResource(historyPath);
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
                HistoricalMatch match = new HistoricalMatch();
                match.setMatchDate(LocalDate.parse(CsvUtils.get(row, 0)));
                match.setTournament(CsvUtils.get(row, 1));
                match.setHomeTeam(CsvUtils.get(row, 2));
                match.setAwayTeam(CsvUtils.get(row, 3));
                match.setHomeScore(Integer.parseInt(CsvUtils.get(row, 4)));
                match.setAwayScore(Integer.parseInt(CsvUtils.get(row, 5)));
                match.setNeutral(CsvUtils.parseBoolean(CsvUtils.get(row, 6)));
                result.add(match);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("读取历史战绩数据失败：" + historyPath, ex);
        }
        return result;
    }

    private List<MatchSchedule> loadSchedules(List<HistoricalMatch> sourceHistoricalMatches) {
        Resource resource = resourceLoader.getResource(schedulePath);
        List<MatchSchedule> result = new ArrayList<>();
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
                MatchSchedule schedule = new MatchSchedule();
                schedule.setMatchId(CsvUtils.get(row, 0));
                schedule.setMatchDate(LocalDate.parse(CsvUtils.get(row, 1)));
                schedule.setKickoffTime(LocalTime.parse(CsvUtils.get(row, 2)));
                schedule.setGroupName(CsvUtils.get(row, 3));
                schedule.setHomeTeamCn(CsvUtils.get(row, 4));
                schedule.setAwayTeamCn(CsvUtils.get(row, 5));
                schedule.setHomeTeamEn(CsvUtils.get(row, 6));
                schedule.setAwayTeamEn(CsvUtils.get(row, 7));
                schedule.setVenue(CsvUtils.get(row, 8));
                schedule.setNeutral(CsvUtils.parseBoolean(CsvUtils.get(row, 9)));
                schedule.setStatus(CsvUtils.get(row, 10));
                schedule.setHomeScore(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, 11)));
                schedule.setAwayScore(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, 12)));
                result.add(schedule);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("读取2026世界杯赛程数据失败：" + schedulePath, ex);
        }
        scheduleUpdater.updateSchedules(result);
        int backfilledCount = backfillScheduleResultsFromHistory(result, sourceHistoricalMatches);
        if (backfilledCount > 0) {
            log.info("Backfilled {} World Cup schedule rows from historical results.", backfilledCount);
        }
        int espnUpdatedCount = espnScheduleUpdater.updateSchedules(result);
        if (espnUpdatedCount > 0) {
            log.info("Backfilled {} World Cup schedule rows from ESPN scoreboard.", espnUpdatedCount);
        }
        result.sort(Comparator.comparing(MatchSchedule::getMatchDate).thenComparing(MatchSchedule::getKickoffTime));
        return result;
    }

    private int backfillScheduleResultsFromHistory(List<MatchSchedule> schedules, List<HistoricalMatch> sourceHistoricalMatches) {
        Map<String, HistoricalMatch> resultsByFixture = new HashMap<>();
        for (HistoricalMatch match : sourceHistoricalMatches) {
            if (!isWorldCup2026Result(match)) {
                continue;
            }
            resultsByFixture.put(
                    buildFixtureKey(match.getMatchDate(), match.getHomeTeam(), match.getAwayTeam()),
                    match);
        }

        int updatedCount = 0;
        for (MatchSchedule schedule : schedules) {
            LocalDate queryDate = getScheduleQueryDate(schedule);
            HistoricalMatch match = resultsByFixture.get(
                    buildFixtureKey(queryDate, schedule.getHomeTeamEn(), schedule.getAwayTeamEn()));
            boolean reversed = false;
            if (match == null) {
                match = resultsByFixture.get(
                        buildFixtureKey(queryDate, schedule.getAwayTeamEn(), schedule.getHomeTeamEn()));
                reversed = match != null;
            }
            if (match == null) {
                continue;
            }

            schedule.setStatus("COMPLETED");
            if (reversed) {
                schedule.setHomeScore(match.getAwayScore());
                schedule.setAwayScore(match.getHomeScore());
            } else {
                schedule.setHomeScore(match.getHomeScore());
                schedule.setAwayScore(match.getAwayScore());
            }
            updatedCount++;
        }
        return updatedCount;
    }

    private boolean isWorldCup2026Result(HistoricalMatch match) {
        String tournament = match.getTournament();
        if (tournament == null) {
            return false;
        }
        String normalized = tournament.toLowerCase(Locale.ROOT);
        return normalized.contains("world cup") && normalized.contains("2026");
    }

    private String buildFixtureKey(LocalDate matchDate, String homeTeam, String awayTeam) {
        return matchDate + "|" + normalizeTeamName(homeTeam).toLowerCase(Locale.ROOT)
                + "|" + normalizeTeamName(awayTeam).toLowerCase(Locale.ROOT);
    }

    private String normalizeTeamName(String teamName) {
        String cleaned = teamName == null ? "" : teamName.trim().replaceAll("\\s+", " ");
        String ascii = Normalizer.normalize(cleaned, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        Map<String, String> aliases = teamAliases();
        return aliases.getOrDefault(ascii, ascii);
    }

    private Map<String, String> teamAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("USA", "United States");
        aliases.put("Czech Republic", "Czechia");
        aliases.put("Bosnia & Herzegovina", "Bosnia and Herzegovina");
        aliases.put("Bosnia-Herzegovina", "Bosnia and Herzegovina");
        aliases.put("Curacao", "Curacao");
        aliases.put("Cote d'Ivoire", "Ivory Coast");
        aliases.put("Korea Republic", "South Korea");
        aliases.put("IR Iran", "Iran");
        aliases.put("Cabo Verde", "Cape Verde");
        aliases.put("Congo DR", "DR Congo");
        aliases.put("T眉rkiye", "Turkey");
        aliases.put("Turkiye", "Turkey");
        return aliases;
    }

}
