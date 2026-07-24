package com.eason.worldcup.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompetitionTest {

    @Test
    void shouldResolveSelectableCompetitionFromStoredSourceName() {
        assertEquals(
                Competition.SWEDISH_ALLSVENSKAN,
                Competition.fromSourceCompetition("瑞超 第15轮", Competition.CLUB_OFFICIAL_OTHER));
        assertEquals(
                Competition.FINNISH_VEIKKAUSLIIGA,
                Competition.fromSourceCompetition("芬超", Competition.CLUB_OFFICIAL_OTHER));
        assertEquals(
                Competition.K_LEAGUE_1,
                Competition.fromSourceCompetition("韩国职业联赛", Competition.CLUB_OFFICIAL_OTHER));
    }

    @Test
    void shouldKeepFallbackForOtherSourceCompetition() {
        assertEquals(
                Competition.CLUB_OFFICIAL_OTHER,
                Competition.fromSourceCompetition("芬兰杯", Competition.CLUB_OFFICIAL_OTHER));
    }

}
