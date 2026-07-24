package com.eason.worldcup.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetitionDataPolicyTest {

    @Test
    void shouldExcludeNorwegianEliteserienAliases() {
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("挪超"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("挪威超"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("Eliteserien"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("Norwegian Eliteserien"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("挪超 第12轮"));
    }

    @Test
    void shouldExcludeRemovedBrazilianCompetitions() {
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("巴甲"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("巴乙"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("巴乙 第20轮"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("巴西乙级联赛"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("巴西杯"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("巴东北杯"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("圣保罗锦"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("Campeonato Paulista"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("Brazil Serie B"));
        assertTrue(CompetitionDataPolicy.isExcludedSourceCompetition("Copa do Nordeste"));
    }

    @Test
    void shouldKeepOtherCompetitionNames() {
        assertFalse(CompetitionDataPolicy.isExcludedSourceCompetition("瑞超"));
        assertFalse(CompetitionDataPolicy.isExcludedSourceCompetition("英超"));
        assertFalse(CompetitionDataPolicy.isExcludedSourceCompetition(null));
    }

}
