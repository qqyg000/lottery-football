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

    private final ResourceLoader resourceLoader;

    @Value("${sporttery.result-update.historical-odds-path:classpath:data/historical_odds.csv}")
    private String historicalOddsPath;

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
        Resource resource = resourceLoader.getResource(historicalOddsPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .skip(1)
                    .map(this::parseSchedule)
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("读取历史赔率数据失败：" + historicalOddsPath, ex);
        }
    }

    private MatchSchedule parseSchedule(String line) {
        List<String> row = CsvUtils.parseLine(line);
        MatchSchedule schedule = new MatchSchedule();
        LocalDate matchDate = LocalDate.parse(CsvUtils.get(row, 1));
        SportteryOdds normalOdds = parseOdds(row, 13, matchDate);
        SportteryOdds handicapOdds = parseOdds(row, 16, matchDate);
        schedule.setMatchId(CsvUtils.get(row, 0));
        schedule.setMatchDate(matchDate);
        schedule.setCompetition(Competition.fromCode(CsvUtils.get(row, 2)));
        schedule.setKickoffTime(UNKNOWN_KICKOFF_TIME);
        schedule.setGroupName(CsvUtils.get(row, 3));
        schedule.setHomeTeamCn(CsvUtils.get(row, 4));
        schedule.setAwayTeamCn(CsvUtils.get(row, 5));
        schedule.setHomeTeamEn(CsvUtils.get(row, 6));
        schedule.setAwayTeamEn(CsvUtils.get(row, 7));
        schedule.setHomeScore(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, 8)));
        schedule.setAwayScore(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, 9)));
        schedule.setNeutral(CsvUtils.parseBoolean(CsvUtils.get(row, 10)));
        schedule.setVenue("");
        schedule.setStatus("COMPLETED");
        schedule.setSportteryMatchId(schedule.getMatchId());
        schedule.setSportteryMatchNumber(CsvUtils.get(row, 11));
        schedule.setSportteryHandicap(CsvUtils.parseIntegerOrNull(CsvUtils.get(row, 12)));
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
                + "|" + canonicalTeamName(resolveTeamName(schedule.getHomeTeamCn(), schedule.getHomeTeamEn()))
                + "|" + canonicalTeamName(resolveTeamName(schedule.getAwayTeamCn(), schedule.getAwayTeamEn()));
    }

    private String resolveTeamName(String chineseName, String englishName) {
        if (chineseName != null && !chineseName.isBlank()) {
            return chineseName;
        }
        return ClubTeamNameTranslator.translate(englishName);
    }

    private String canonicalTeamName(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT)
                .replace("足球俱乐部", "")
                .replace("俱乐部", "")
                .replaceAll("[\\s·•.．,，'’`´()（）\\[\\]【】\\-_/&]+", "")
                .replaceAll("^(FC|SC|CF)(?=\\p{IsHan})", "")
                .replaceAll("(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$", "");
        return switch (normalized) {
            case "尤尔加登", "佐加顿斯" -> "佐加顿斯";
            case "桑德菲杰", "桑纳菲尤尔" -> "桑纳菲尤尔";
            case "萨普斯堡", "萨尔普斯堡" -> "萨尔普斯堡";
            case "蔚山HD", "蔚山现代" -> "蔚山现代";
            case "浦项钢铁", "浦项制铁" -> "浦项制铁";
            case "尚州尚武", "金泉尚武" -> "金泉尚武";
            default -> normalized;
        };
    }

}
