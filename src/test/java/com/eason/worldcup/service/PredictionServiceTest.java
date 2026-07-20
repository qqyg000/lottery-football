package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.UserConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PredictionServiceTest {

    private final PredictionService predictionService = new PredictionService(null, null, null);

    @Test
    void shouldResolveBacktestFactorsByCompetition() {
        UserConfig.ModelFactors worldCupFactors = UserConfig.ModelFactors.defaults();
        UserConfig.ModelFactors premierLeagueFactors = UserConfig.ModelFactors.defaults();
        Map<Competition, UserConfig.ModelFactors> factorsByCompetition = Map.of(
                Competition.WORLD_CUP, worldCupFactors,
                Competition.PREMIER_LEAGUE, premierLeagueFactors);

        assertSame(worldCupFactors, predictionService.resolveBacktestModelFactors(
                Competition.WORLD_CUP,
                factorsByCompetition));
        assertSame(premierLeagueFactors, predictionService.resolveBacktestModelFactors(
                Competition.PREMIER_LEAGUE,
                factorsByCompetition));
        assertNull(predictionService.resolveBacktestModelFactors(
                Competition.LA_LIGA,
                factorsByCompetition));
    }

}
