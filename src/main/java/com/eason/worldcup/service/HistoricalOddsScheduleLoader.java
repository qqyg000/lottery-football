package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import com.eason.worldcup.model.SportteryOdds;
import com.eason.worldcup.util.ClubTeamNameTranslator;
import com.eason.worldcup.util.CsvUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class HistoricalOddsScheduleLoader {

    private static final Logger log = LoggerFactory.getLogger(HistoricalOddsScheduleLoader.class);

    private static final LocalTime UNKNOWN_KICKOFF_TIME = LocalTime.NOON;

    private static final int MATCH_ID_COLUMN = 0;

    private static final int MATCH_DATE_COLUMN = 1;

    private static final int COMPETITION_COLUMN = 2;

    private static final int HOME_TEAM_CN_COLUMN = 3;

    private static final int AWAY_TEAM_CN_COLUMN = 4;

    private static final int HOME_TEAM_EN_COLUMN = 5;

    private static final int AWAY_TEAM_EN_COLUMN = 6;

    private static final int HOME_SCORE_COLUMN = 7;

    private static final int AWAY_SCORE_COLUMN = 8;

    private static final int NEUTRAL_COLUMN = 9;

    private static final int SPORTTERY_MATCH_NUMBER_COLUMN = 10;

    private static final int HANDICAP_COLUMN = 11;

    private static final int NORMAL_ODDS_COLUMN = 12;

    private static final int HANDICAP_ODDS_COLUMN = 15;

    private final ResourceLoader resourceLoader;

    @Value("${sporttery.result-update.historical-odds-data-path:classpath:data/historical_odds_data.csv}")
    private String historicalOddsDataPath;

    public HistoricalOddsScheduleLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public int mergeInto(List<MatchSchedule> schedules) {
        List<MatchSchedule> historicalSchedules = loadSchedules();
        Map<String, MatchSchedule> schedulesByFixture = new HashMap<>();
        for (MatchSchedule schedule : schedules) {
            schedulesByFixture.putIfAbsent(buildFixtureKey(schedule), schedule);
        }

        int mergedCount = 0;
        int addedCount = 0;
        for (MatchSchedule historicalSchedule : historicalSchedules) {
            String fixtureKey = buildFixtureKey(historicalSchedule);
            MatchSchedule existingSchedule = schedulesByFixture.get(fixtureKey);
            if (existingSchedule == null) {
                schedules.add(historicalSchedule);
                schedulesByFixture.put(fixtureKey, historicalSchedule);
                addedCount++;
                continue;
            }
            applyHistoricalData(existingSchedule, historicalSchedule);
            mergedCount++;
        }
        log.info(
                "Loaded {} bundled historical odds rows: merged {}, added {}.",
                historicalSchedules.size(),
                mergedCount,
                addedCount);
        return historicalSchedules.size();
    }

    private List<MatchSchedule> loadSchedules() {
        Resource resource = resourceLoader.getResource(historicalOddsDataPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .skip(1)
                    .map(this::parseSchedule)
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("读取历史赔率数据失败：" + historicalOddsDataPath, ex);
        }
    }

    private MatchSchedule parseSchedule(String line) {
        List<String> row = CsvUtils.parseLine(line);
        MatchSchedule schedule = new MatchSchedule();
        LocalDate matchDate = LocalDate.parse(CsvUtils.get(row, MATCH_DATE_COLUMN));
        Competition competition = Competition.fromCode(CsvUtils.get(row, COMPETITION_COLUMN));
        String homeTeamCn = CsvUtils.get(row, HOME_TEAM_CN_COLUMN);
        String awayTeamCn = CsvUtils.get(row, AWAY_TEAM_CN_COLUMN);
        String homeTeamEn = CsvUtils.get(row, HOME_TEAM_EN_COLUMN);
        String awayTeamEn = CsvUtils.get(row, AWAY_TEAM_EN_COLUMN);
        SportteryOdds normalOdds = parseOdds(row, NORMAL_ODDS_COLUMN, matchDate);
        SportteryOdds handicapOdds = parseOdds(row, HANDICAP_ODDS_COLUMN, matchDate);
        schedule.setMatchId(CsvUtils.get(row, MATCH_ID_COLUMN));
        schedule.setMatchDate(matchDate);
        schedule.setCompetition(competition);
        schedule.setKickoffTime(UNKNOWN_KICKOFF_TIME);
        schedule.setGroupName(competition.getDisplayName());
        schedule.setHomeTeamCn(resolveChineseTeamName(competition, homeTeamCn, homeTeamEn));
        schedule.setAwayTeamCn(resolveChineseTeamName(competition, awayTeamCn, awayTeamEn));
        schedule.setHomeTeamEn(homeTeamEn);
        schedule.setAwayTeamEn(awayTeamEn);
        schedule.setHomeScore(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, HOME_SCORE_COLUMN)));
        schedule.setAwayScore(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, AWAY_SCORE_COLUMN)));
        schedule.setNeutral(CsvUtils.parseBoolean(CsvUtils.get(row, NEUTRAL_COLUMN)));
        schedule.setVenue("");
        schedule.setStatus("COMPLETED");
        schedule.setSportteryMatchId(schedule.getMatchId());
        schedule.setSportteryMatchNumber(CsvUtils.get(row, SPORTTERY_MATCH_NUMBER_COLUMN));
        schedule.setSportteryHandicap(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, HANDICAP_COLUMN)));
        schedule.setSportteryNormalAvailable(normalOdds != null);
        schedule.setSportteryNormalOdds(normalOdds);
        schedule.setSportteryHandicapOdds(handicapOdds);
        return schedule;
    }

    private SportteryOdds parseOdds(List<String> row, int startIndex, LocalDate matchDate) {
        Double win = parseDoubleOrNull(CsvUtils.get(row, startIndex));
        Double draw = parseDoubleOrNull(CsvUtils.get(row, startIndex + 1));
        Double lose = parseDoubleOrNull(CsvUtils.get(row, startIndex + 2));
        if (win == null || draw == null || lose == null) {
            return null;
        }
        return new SportteryOdds(win, draw, lose, matchDate + " 初盘");
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.valueOf(value.trim());
    }

    private void applyHistoricalData(MatchSchedule target, MatchSchedule source) {
        target.setStatus("COMPLETED");
        target.setHomeTeamCn(source.getHomeTeamCn());
        target.setAwayTeamCn(source.getAwayTeamCn());
        if (target.getHomeTeamEn() == null || target.getHomeTeamEn().isBlank()) {
            target.setHomeTeamEn(source.getHomeTeamEn());
        }
        if (target.getAwayTeamEn() == null || target.getAwayTeamEn().isBlank()) {
            target.setAwayTeamEn(source.getAwayTeamEn());
        }
        target.setHomeScore(source.getHomeScore());
        target.setAwayScore(source.getAwayScore());
        target.setSportteryMatchId(source.getSportteryMatchId());
        target.setSportteryMatchNumber(source.getSportteryMatchNumber());
        target.setSportteryNormalAvailable(source.getSportteryNormalAvailable());
        target.setSportteryHandicap(source.getSportteryHandicap());
        target.setSportteryNormalOdds(source.getSportteryNormalOdds());
        target.setSportteryHandicapOdds(source.getSportteryHandicapOdds());
    }

    private String buildFixtureKey(MatchSchedule schedule) {
        return schedule.getCompetition()
                + "|" + schedule.getMatchDate()
                + "|" + canonicalTeamName(resolveTeamName(
                        schedule.getCompetition(),
                        schedule.getHomeTeamCn(),
                        schedule.getHomeTeamEn()))
                + "|" + canonicalTeamName(resolveTeamName(
                        schedule.getCompetition(),
                        schedule.getAwayTeamCn(),
                        schedule.getAwayTeamEn()));
    }

    private String resolveTeamName(Competition competition, String chineseName, String englishName) {
        if (chineseName != null && !chineseName.isBlank()) {
            return ClubTeamNameTranslator.translate(competition, chineseName);
        }
        return ClubTeamNameTranslator.translate(competition, englishName);
    }

    private String resolveChineseTeamName(Competition competition, String chineseName, String englishName) {
        return resolveTeamName(competition, chineseName, englishName);
    }

    private String canonicalTeamName(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT)
                .replace("足球俱乐部", "")
                .replace("俱乐部", "")
                .replaceAll("[\\s·•.．,，'’`´()（）\\[\\]【】\\-_/&]+", "")
                .replaceAll("^(FC|SC|CF)(?=\\p{IsHan})", "")
                .replaceAll("(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$", "");
        return normalized;
    }

}
