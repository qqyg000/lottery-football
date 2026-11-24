package com.eason.worldcup.model;

import java.time.LocalDate;
import java.util.List;

public record ScoreBacktestMatch(
        String matchId,
        LocalDate matchDate,
        int homeScore,
        int awayScore,
        double expectedHomeGoals,
        double expectedAwayGoals,
        List<ScoreProbability> topScores) {

}
