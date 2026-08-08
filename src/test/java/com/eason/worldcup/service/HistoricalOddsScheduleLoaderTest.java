package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.MatchSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HistoricalOddsScheduleLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldKeepScheduledStatusWhenFutureOddsRowHasNoScore() throws Exception {
        HistoricalOddsScheduleLoader loader = createLoader("""
                match_id,match_date,competition,home_team_cn,away_team_cn,home_team_en,away_team_en,home_score,away_score,neutral,sporttery_match_number,handicap,normal_win,normal_draw,normal_lose,handicap_win,handicap_draw,handicap_lose,total_goals_0,total_goals_1,total_goals_2,total_goals_3,total_goals_4,total_goals_5,total_goals_6,total_goals_7_plus,total_goals_updated_at,total_goals_source_match_id
                HIS-SPT-2040751,2026-08-08,K_LEAGUE_1,安养FC,大田市民,,,,,false,周六003,1,2.72,3.15,2.25,1.49,3.75,5.22,11.00,4.60,3.40,3.70,6.00,11.50,22.00,31.00,2026-08-07 09:35:03,2040751
                """);
        MatchSchedule scheduled = schedule("安养FC", "大田市民", "SCHEDULED");
        List<MatchSchedule> schedules = new ArrayList<>(List.of(scheduled));

        loader.mergeInto(schedules);

        assertEquals(1, schedules.size());
        assertEquals("SCHEDULED", scheduled.getStatus());
        assertNull(scheduled.getHomeScore());
        assertNull(scheduled.getAwayScore());
        assertNotNull(scheduled.getSportteryTotalGoalsOdds());
        assertEquals(11.0D, scheduled.getSportteryTotalGoalsOdds().getGoal0());
    }

    @Test
    void shouldMarkHistoricalOddsRowCompletedWhenScoreExists() throws Exception {
        HistoricalOddsScheduleLoader loader = createLoader("""
                match_id,match_date,competition,home_team_cn,away_team_cn,home_team_en,away_team_en,home_score,away_score,neutral,sporttery_match_number,handicap,normal_win,normal_draw,normal_lose,handicap_win,handicap_draw,handicap_lose,total_goals_0,total_goals_1,total_goals_2,total_goals_3,total_goals_4,total_goals_5,total_goals_6,total_goals_7_plus,total_goals_updated_at,total_goals_source_match_id
                HIS-SPT-2040751,2026-08-08,K_LEAGUE_1,安养FC,大田市民,,,1,2,false,周六003,1,2.72,3.15,2.25,1.49,3.75,5.22,11.00,4.60,3.40,3.70,6.00,11.50,22.00,31.00,2026-08-07 09:35:03,2040751
                """);
        List<MatchSchedule> schedules = new ArrayList<>();

        loader.mergeInto(schedules);

        assertEquals(1, schedules.size());
        assertEquals("COMPLETED", schedules.get(0).getStatus());
        assertEquals(1, schedules.get(0).getHomeScore());
        assertEquals(2, schedules.get(0).getAwayScore());
    }

    private HistoricalOddsScheduleLoader createLoader(String csv) throws Exception {
        Path csvPath = tempDir.resolve("historical_odds_data.csv");
        Files.writeString(csvPath, csv, StandardCharsets.UTF_8);
        HistoricalOddsScheduleLoader loader = new HistoricalOddsScheduleLoader(new DefaultResourceLoader());
        ReflectionTestUtils.setField(loader, "historicalOddsDataPath", csvPath.toUri().toString());
        return loader;
    }

    private MatchSchedule schedule(String homeTeam, String awayTeam, String status) {
        MatchSchedule schedule = new MatchSchedule();
        schedule.setCompetition(Competition.K_LEAGUE_1);
        schedule.setMatchDate(LocalDate.of(2026, 8, 8));
        schedule.setHomeTeamCn(homeTeam);
        schedule.setAwayTeamCn(awayTeam);
        schedule.setStatus(status);
        return schedule;
    }

}
