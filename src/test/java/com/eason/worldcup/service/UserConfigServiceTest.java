package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.UserConfig;
import com.eason.worldcup.model.UserConfig.ParameterProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserConfigServiceTest {

    @TempDir
    Path tempDirectory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMigrateLegacyParametersToSixtyEightIndependentProfiles() throws Exception {
        Path configPath = tempDirectory.resolve("user-config.json");
        Files.writeString(configPath, """
                {
                  "modelMode": "after",
                  "includePreviousEdition": false,
                  "modelFactors": {
                    "hostTeamGoalFactor": 1.21,
                    "homeTeamGoalFactor": 1.11,
                    "seedTeamGoalFactor": 1.91,
                    "handicapSmoothingFactor": 0.321
                  },
                  "globalParameters": {
                    "recommendationOdds": 1.51,
                    "handicapRecommendationThreshold": 61.2,
                    "handicapReverseThreshold": 42.3,
                    "singleRecommendationThreshold": 73.4
                  },
                  "selectedRows": {}
                }
                """);
        UserConfigService service = createService(configPath);

        UserConfig config = service.load();

        assertEquals(68, config.getParameterProfiles().size());
        ParameterProfile currentProfile = config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.WORLD_CUP,
                UserConfig.CURRENT_EDITION_PROFILE,
                UserConfig.STABLE_PARAMETER_PRESET));
        ParameterProfile previousProfile = config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.WORLD_CUP,
                UserConfig.PREVIOUS_EDITION_PROFILE,
                UserConfig.STABLE_PARAMETER_PRESET));
        ParameterProfile aggressiveProfile = config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.WORLD_CUP,
                UserConfig.CURRENT_EDITION_PROFILE,
                UserConfig.AGGRESSIVE_PARAMETER_PRESET));
        assertEquals(1.21D, currentProfile.getModelFactors().getHostTeamGoalFactor());
        assertEquals(1.00D, currentProfile.getModelFactors().getOfficialMatchWeight());
        assertEquals(0.50D, currentProfile.getModelFactors().getInternationalFriendlyWeight());
        assertEquals(0.30D, currentProfile.getModelFactors().getClubFriendlyWeight());
        assertEquals(1.51D, previousProfile.getGlobalParameters().getRecommendationOdds());
        assertEquals(2.30D, aggressiveProfile.getModelFactors().getHostTeamGoalFactor());
        assertEquals(1.00D, aggressiveProfile.getModelFactors().getOfficialMatchWeight());
        assertEquals(0.50D, aggressiveProfile.getModelFactors().getInternationalFriendlyWeight());
        assertEquals(0.30D, aggressiveProfile.getModelFactors().getClubFriendlyWeight());
        assertEquals(2.46D, aggressiveProfile.getGlobalParameters().getRecommendationOdds());
        assertNotSame(currentProfile, previousProfile);
        assertNotSame(currentProfile, aggressiveProfile);
        assertNotSame(currentProfile.getModelFactors(), previousProfile.getModelFactors());
        assertNotSame(currentProfile.getModelFactors(), aggressiveProfile.getModelFactors());
        assertNotSame(currentProfile.getGlobalParameters(), previousProfile.getGlobalParameters());
        assertNotSame(currentProfile.getGlobalParameters(), aggressiveProfile.getGlobalParameters());

        currentProfile.getModelFactors().setHostTeamGoalFactor(2.22D);
        currentProfile.getGlobalParameters().setRecommendationOdds(2.33D);

        assertEquals(1.21D, previousProfile.getModelFactors().getHostTeamGoalFactor());
        assertEquals(1.51D, previousProfile.getGlobalParameters().getRecommendationOdds());
    }

    @Test
    void shouldPersistOnlySixtyEightProfileStructureAfterMigration() throws Exception {
        Path configPath = tempDirectory.resolve("user-config.json");
        Files.writeString(configPath, """
                {
                  "modelFactors": {
                    "hostTeamGoalFactor": 1.2
                  },
                  "globalParameters": {
                    "recommendationOdds": 1.4
                  }
                }
                """);
        UserConfigService service = createService(configPath);

        service.load();

        JsonNode persisted = objectMapper.readTree(configPath.toFile());
        assertFalse(persisted.has("modelFactors"));
        assertFalse(persisted.has("globalParameters"));
        assertTrue(persisted.has("parameterProfiles"));
        assertEquals(68, persisted.get("parameterProfiles").size());
        JsonNode modelFactors = persisted.get("parameterProfiles")
                .get("WORLD_CUP:CURRENT:STABLE")
                .get("modelFactors");
        assertEquals(1.00D, modelFactors.get("officialMatchWeight").asDouble());
        assertEquals(0.50D, modelFactors.get("internationalFriendlyWeight").asDouble());
        assertEquals(0.30D, modelFactors.get("clubFriendlyWeight").asDouble());
    }

    @Test
    void shouldMigrateTwoPartProfileKeysToStablePresetOnly() throws Exception {
        Path configPath = tempDirectory.resolve("user-config.json");
        Files.writeString(configPath, """
                {
                  "parameterProfiles": {
                    "PREMIER_LEAGUE:PREVIOUS": {
                      "modelFactors": {
                        "hostTeamGoalFactor": 1.27,
                        "homeTeamGoalFactor": 1.42,
                        "seedTeamGoalFactor": 1.64,
                        "handicapSmoothingFactor": 0.456
                      },
                      "globalParameters": {
                        "recommendationOdds": 1.88,
                        "handicapRecommendationThreshold": 72.11,
                        "handicapReverseThreshold": 41.22,
                        "singleRecommendationThreshold": 76.33
                      }
                    }
                  }
                }
                """);
        UserConfigService service = createService(configPath);

        UserConfig config = service.load();

        ParameterProfile stableProfile = config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.PREMIER_LEAGUE,
                UserConfig.PREVIOUS_EDITION_PROFILE,
                UserConfig.STABLE_PARAMETER_PRESET));
        ParameterProfile aggressiveProfile = config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.PREMIER_LEAGUE,
                UserConfig.PREVIOUS_EDITION_PROFILE,
                UserConfig.AGGRESSIVE_PARAMETER_PRESET));
        assertEquals(1.27D, stableProfile.getModelFactors().getHostTeamGoalFactor());
        assertEquals(1.88D, stableProfile.getGlobalParameters().getRecommendationOdds());
        assertEquals(2.30D, aggressiveProfile.getModelFactors().getHostTeamGoalFactor());
        assertEquals(2.46D, aggressiveProfile.getGlobalParameters().getRecommendationOdds());

        JsonNode persistedProfiles = objectMapper.readTree(configPath.toFile()).get("parameterProfiles");
        assertFalse(persistedProfiles.has("PREMIER_LEAGUE:PREVIOUS"));
        assertTrue(persistedProfiles.has("PREMIER_LEAGUE:PREVIOUS:STABLE"));
        assertTrue(persistedProfiles.has("PREMIER_LEAGUE:PREVIOUS:AGGRESSIVE"));
    }

    @Test
    void shouldKeepOfficialMatchWeightAtOrAboveOne() {
        Path configPath = tempDirectory.resolve("user-config.json");
        UserConfigService service = createService(configPath);
        UserConfig config = service.load();
        ParameterProfile profile = config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.WORLD_CUP,
                UserConfig.CURRENT_EDITION_PROFILE,
                UserConfig.STABLE_PARAMETER_PRESET));
        profile.getModelFactors().setOfficialMatchWeight(0.25D);

        UserConfig saved = service.save(config);

        ParameterProfile savedProfile = saved.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.WORLD_CUP,
                UserConfig.CURRENT_EDITION_PROFILE,
                UserConfig.STABLE_PARAMETER_PRESET));
        assertEquals(1.0D, savedProfile.getModelFactors().getOfficialMatchWeight());

        savedProfile.getModelFactors().setOfficialMatchWeight(2.5D);
        UserConfig savedAgain = service.save(saved);

        ParameterProfile savedAgainProfile = savedAgain.getParameterProfiles().get(UserConfig.parameterProfileKey(
                Competition.WORLD_CUP,
                UserConfig.CURRENT_EDITION_PROFILE,
                UserConfig.STABLE_PARAMETER_PRESET));
        assertEquals(2.5D, savedAgainProfile.getModelFactors().getOfficialMatchWeight());
    }

    private UserConfigService createService(Path configPath) {
        UserConfigService service = new UserConfigService(objectMapper);
        ReflectionTestUtils.setField(service, "userConfigPath", configPath.toString());
        service.init();
        return service;
    }

}
