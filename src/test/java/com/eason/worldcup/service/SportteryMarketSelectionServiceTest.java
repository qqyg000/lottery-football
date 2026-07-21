package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SportteryMarketSelectionServiceTest {

    private final SportteryMarketSelectionService service =
            new SportteryMarketSelectionService(new ObjectMapper());

    @Test
    void shouldMatchBrazilScheduleAliasToSportteryName() {
        assertTrue(service.teamNamesMatch(
                Competition.BRAZIL_SERIE_A,
                "米竞技",
                "Atlético-MG",
                "米内罗竞技"));
    }

    @Test
    void shouldMatchChampionsLeagueScheduleAliasesToSportteryNames() {
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "KuPS Kuopio",
                "KuPS Kuopio",
                "库奥皮奥"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "AGF",
                "AGF",
                "奥胡斯"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "波兹南",
                "Lech Poznan",
                "波兹南莱赫"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "格风暴",
                "SK Sturm Graz",
                "格拉茨风暴"));
        assertTrue(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "Heart of Midlothian",
                "Heart of Midlothian",
                "哈茨"));
    }

    @Test
    void shouldRejectUnrelatedSportteryTeamName() {
        assertFalse(service.teamNamesMatch(
                Competition.CHAMPIONS_LEAGUE,
                "AGF",
                "AGF",
                "格拉茨风暴"));
    }

}
