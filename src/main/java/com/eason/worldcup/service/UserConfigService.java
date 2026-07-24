package com.eason.worldcup.service;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.UserConfig;
import com.eason.worldcup.model.UserConfig.ParameterProfile;
import com.eason.worldcup.model.UserConfig.RecommendationSelection;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserConfigService {

    private final ObjectMapper objectMapper;

    @Value("${worldcup.user-config-path:config/user-config.json}")
    private String userConfigPath;

    private Path resolvedUserConfigPath;

    public UserConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        resolvedUserConfigPath = resolvePath(userConfigPath);
        if (!Files.exists(resolvedUserConfigPath)) {
            save(UserConfigDefaults.newConfig());
        }
    }

    public synchronized UserConfig load() {
        if (!Files.exists(resolvedUserConfigPath)) {
            return save(UserConfigDefaults.newConfig());
        }
        try {
            UserConfig loaded = objectMapper.readValue(resolvedUserConfigPath.toFile(), UserConfig.class);
            boolean requiresMigration = requiresParameterProfileMigration(loaded);
            UserConfig normalized = normalize(loaded);
            if (requiresMigration) {
                writeConfig(normalized);
            }
            return normalized;
        } catch (IOException ex) {
            throw new IllegalStateException("读取用户配置失败：" + resolvedUserConfigPath, ex);
        }
    }

    public synchronized UserConfig save(UserConfig config) {
        UserConfig normalized = normalize(config);
        try {
            writeConfig(normalized);
            return normalized;
        } catch (IOException ex) {
            throw new IllegalStateException("保存用户配置失败：" + resolvedUserConfigPath, ex);
        }
    }

    private void writeConfig(UserConfig config) throws IOException {
        Path directory = resolvedUserConfigPath.getParent();
        if (directory != null) {
            Files.createDirectories(directory);
        }
        Path tempFile = directory == null
                ? Files.createTempFile("user-config-", ".json")
                : Files.createTempFile(directory, "user-config-", ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), config);
        moveConfigFile(tempFile, resolvedUserConfigPath);
    }

    private void moveConfigFile(Path tempFile, Path targetFile) throws IOException {
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path resolvePath(String pathText) {
        Path path = Path.of(pathText);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return Path.of(System.getProperty("user.dir")).resolve(path).normalize();
    }

    private UserConfig normalize(UserConfig config) {
        UserConfig normalized = config == null ? UserConfigDefaults.newConfig() : config;
        normalized.setModelMode("after");
        UserConfig.ModelFactors legacyModelFactors = normalizeModelFactors(normalized.getModelFactors());
        UserConfig.GlobalParameters legacyGlobalParameters = normalizeGlobalParameters(normalized.getGlobalParameters());
        normalized.setParameterProfiles(normalizeParameterProfiles(
                normalized.getParameterProfiles(),
                legacyModelFactors,
                legacyGlobalParameters));
        normalized.setSelectedRows(normalizeSelectedRows(normalized.getSelectedRows()));
        return normalized;
    }

    private Map<String, ParameterProfile> normalizeParameterProfiles(
            Map<String, ParameterProfile> parameterProfiles,
            UserConfig.ModelFactors legacyModelFactors,
            UserConfig.GlobalParameters legacyGlobalParameters) {
        Map<String, ParameterProfile> normalized = new LinkedHashMap<>();
        for (var competition : UserConfig.getParameterCompetitions()) {
            for (String profileRange : UserConfig.getParameterProfileRanges()) {
                for (String parameterPreset : UserConfig.getParameterPresets()) {
                    addNormalizedParameterProfile(
                            normalized,
                            parameterProfiles,
                            competition,
                            profileRange,
                            parameterPreset,
                            legacyModelFactors,
                            legacyGlobalParameters);
                }
            }
        }
        return normalized;
    }

    private void addNormalizedParameterProfile(
            Map<String, ParameterProfile> normalized,
            Map<String, ParameterProfile> parameterProfiles,
            Competition competition,
            String profileRange,
            String parameterPreset,
            UserConfig.ModelFactors legacyModelFactors,
            UserConfig.GlobalParameters legacyGlobalParameters) {
        String profileKey = UserConfig.parameterProfileKey(competition, profileRange, parameterPreset);
        ParameterProfile source = parameterProfiles == null ? null : parameterProfiles.get(profileKey);
        if (source == null && UserConfig.STABLE_PARAMETER_PRESET.equals(parameterPreset) && parameterProfiles != null) {
            source = parameterProfiles.get(UserConfig.legacyParameterProfileKey(competition, profileRange));
        }
        ParameterProfile presetDefaults = UserConfig.AGGRESSIVE_PARAMETER_PRESET.equals(parameterPreset)
                ? ParameterProfile.aggressiveDefaults()
                : ParameterProfile.of(legacyModelFactors, legacyGlobalParameters);
        UserConfig.ModelFactors sourceModelFactors = source == null
                ? presetDefaults.getModelFactors()
                : source.getModelFactors();
        UserConfig.GlobalParameters sourceGlobalParameters = source == null
                ? presetDefaults.getGlobalParameters()
                : source.getGlobalParameters();
        normalized.put(profileKey, ParameterProfile.of(
                normalizeModelFactors(
                        copyModelFactors(sourceModelFactors),
                        presetDefaults.getModelFactors()),
                normalizeGlobalParameters(
                        copyGlobalParameters(sourceGlobalParameters),
                        presetDefaults.getGlobalParameters())));
    }

    private UserConfig.ModelFactors copyModelFactors(UserConfig.ModelFactors source) {
        UserConfig.ModelFactors copy = new UserConfig.ModelFactors();
        if (source != null) {
            copy.setHostTeamGoalFactor(source.getHostTeamGoalFactor());
            copy.setHomeTeamGoalFactor(source.getHomeTeamGoalFactor());
            copy.setSeedTeamGoalFactor(source.getSeedTeamGoalFactor());
            copy.setOfficialMatchWeight(source.getOfficialMatchWeight());
            copy.setInternationalFriendlyWeight(source.getInternationalFriendlyWeight());
            copy.setClubFriendlyWeight(source.getClubFriendlyWeight());
            copy.setHandicapSmoothingFactor(source.getHandicapSmoothingFactor());
        }
        return copy;
    }

    private UserConfig.GlobalParameters copyGlobalParameters(UserConfig.GlobalParameters source) {
        UserConfig.GlobalParameters copy = new UserConfig.GlobalParameters();
        if (source != null) {
            copy.setRecommendationOdds(source.getRecommendationOdds());
            copy.setHandicapRecommendationThreshold(source.getHandicapRecommendationThreshold());
            copy.setHandicapReverseThreshold(source.getHandicapReverseThreshold());
            copy.setSingleRecommendationThreshold(source.getSingleRecommendationThreshold());
        }
        return copy;
    }

    private boolean requiresParameterProfileMigration(UserConfig config) {
        if (config == null || config.getParameterProfiles() == null
                || config.getParameterProfiles().size() != UserConfig.getParameterCompetitions().size()
                * UserConfig.getParameterProfileRanges().size()
                * UserConfig.getParameterPresets().size()) {
            return true;
        }
        for (var competition : UserConfig.getParameterCompetitions()) {
            for (String profileRange : UserConfig.getParameterProfileRanges()) {
                for (String parameterPreset : UserConfig.getParameterPresets()) {
                    if (!hasCompleteParameterProfile(config.getParameterProfiles().get(UserConfig.parameterProfileKey(
                            competition,
                            profileRange,
                            parameterPreset)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasCompleteParameterProfile(ParameterProfile profile) {
        return profile != null
                && profile.getModelFactors() != null
                && profile.getModelFactors().getHostTeamGoalFactor() != null
                && profile.getModelFactors().getHomeTeamGoalFactor() != null
                && profile.getModelFactors().getSeedTeamGoalFactor() != null
                && profile.getModelFactors().getOfficialMatchWeight() != null
                && profile.getModelFactors().getInternationalFriendlyWeight() != null
                && profile.getModelFactors().getClubFriendlyWeight() != null
                && profile.getModelFactors().getHandicapSmoothingFactor() != null
                && profile.getGlobalParameters() != null
                && profile.getGlobalParameters().getRecommendationOdds() != null
                && profile.getGlobalParameters().getHandicapRecommendationThreshold() != null
                && profile.getGlobalParameters().getHandicapReverseThreshold() != null
                && profile.getGlobalParameters().getSingleRecommendationThreshold() != null;
    }

    private UserConfig.GlobalParameters normalizeGlobalParameters(UserConfig.GlobalParameters parameters) {
        return normalizeGlobalParameters(parameters, UserConfig.GlobalParameters.defaults());
    }

    private UserConfig.GlobalParameters normalizeGlobalParameters(
            UserConfig.GlobalParameters parameters,
            UserConfig.GlobalParameters defaults) {
        UserConfig.GlobalParameters normalized = parameters == null
                ? new UserConfig.GlobalParameters()
                : parameters;
        normalized.setRecommendationOdds(normalizeNumber(
                normalized.getRecommendationOdds(),
                defaults.getRecommendationOdds(),
                1.0D,
                100.0D));
        normalized.setHandicapRecommendationThreshold(normalizeNumber(
                normalized.getHandicapRecommendationThreshold(),
                defaults.getHandicapRecommendationThreshold(),
                0.0D,
                100.0D));
        normalized.setHandicapReverseThreshold(normalizeNumber(
                normalized.getHandicapReverseThreshold(),
                defaults.getHandicapReverseThreshold(),
                0.0D,
                100.0D));
        normalized.setSingleRecommendationThreshold(normalizeNumber(
                normalized.getSingleRecommendationThreshold(),
                defaults.getSingleRecommendationThreshold(),
                0.0D,
                100.0D));
        return normalized;
    }

    private UserConfig.ModelFactors normalizeModelFactors(UserConfig.ModelFactors factors) {
        return normalizeModelFactors(factors, UserConfig.ModelFactors.defaults());
    }

    private UserConfig.ModelFactors normalizeModelFactors(
            UserConfig.ModelFactors factors,
            UserConfig.ModelFactors defaults) {
        UserConfig.ModelFactors normalized = factors == null ? new UserConfig.ModelFactors() : factors;
        normalized.setHostTeamGoalFactor(normalizeNumber(normalized.getHostTeamGoalFactor(), defaults.getHostTeamGoalFactor(), 0.1D, 3.0D));
        normalized.setHomeTeamGoalFactor(normalizeNumber(normalized.getHomeTeamGoalFactor(), defaults.getHomeTeamGoalFactor(), 0.1D, 3.0D));
        normalized.setSeedTeamGoalFactor(normalizeNumber(normalized.getSeedTeamGoalFactor(), defaults.getSeedTeamGoalFactor(), 0.1D, 3.0D));
        normalized.setOfficialMatchWeight(normalizeNumber(normalized.getOfficialMatchWeight(), defaults.getOfficialMatchWeight(), 1.0D, 3.0D));
        normalized.setInternationalFriendlyWeight(normalizeNumber(normalized.getInternationalFriendlyWeight(), defaults.getInternationalFriendlyWeight(), 0.0D, 1.0D));
        normalized.setClubFriendlyWeight(normalizeNumber(normalized.getClubFriendlyWeight(), defaults.getClubFriendlyWeight(), 0.0D, 1.0D));
        normalized.setHandicapSmoothingFactor(normalizeNumber(normalized.getHandicapSmoothingFactor(), defaults.getHandicapSmoothingFactor(), 0.0D, 0.8D));
        return normalized;
    }

    private Double normalizeNumber(Double value, Double defaultValue, double min, double max) {
        if (value == null || !Double.isFinite(value)) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, value));
    }

    private Map<String, RecommendationSelection> normalizeSelectedRows(Map<String, RecommendationSelection> selectedRows) {
        Map<String, RecommendationSelection> normalized = new LinkedHashMap<>();
        if (selectedRows == null) {
            return normalized;
        }
        selectedRows.forEach((matchId, selection) -> {
            if (matchId == null || matchId.isBlank() || selection == null) {
                return;
            }
            RecommendationSelection normalizedSelection = new RecommendationSelection();
            boolean manualOverride = Boolean.TRUE.equals(selection.getManualOverride())
                    || Boolean.TRUE.equals(selection.getNormal())
                    || isValidHandicap(selection.getHandicap());
            if (!manualOverride) {
                return;
            }
            normalizedSelection.setManualOverride(true);
            if (Boolean.TRUE.equals(selection.getNormal())) {
                normalizedSelection.setNormal(true);
            }
            if (isValidHandicap(selection.getHandicap())) {
                normalizedSelection.setHandicap(selection.getHandicap());
            }
            normalized.put(matchId, normalizedSelection);
        });
        return normalized;
    }

    private boolean isValidHandicap(String handicap) {
        return handicap != null && handicap.matches("handicap--?\\d+");
    }

    private static class UserConfigDefaults {

        private static UserConfig newConfig() {
            UserConfig config = new UserConfig();
            config.setModelMode("after");
            config.setIncludePreviousEdition(false);
            config.setModelFactors(UserConfig.ModelFactors.defaults());
            config.setGlobalParameters(UserConfig.GlobalParameters.defaults());
            config.setParameterProfiles(new LinkedHashMap<>());
            config.setSelectedRows(new LinkedHashMap<>());
            return config;
        }

    }

}
