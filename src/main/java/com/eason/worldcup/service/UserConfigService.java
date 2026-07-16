package com.eason.worldcup.service;

import com.eason.worldcup.model.UserConfig;
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
            return normalize(objectMapper.readValue(resolvedUserConfigPath.toFile(), UserConfig.class));
        } catch (IOException ex) {
            throw new IllegalStateException("读取用户配置失败：" + resolvedUserConfigPath, ex);
        }
    }

    public synchronized UserConfig save(UserConfig config) {
        UserConfig normalized = normalize(config);
        try {
            Path directory = resolvedUserConfigPath.getParent();
            if (directory != null) {
                Files.createDirectories(directory);
            }
            Path tempFile = directory == null
                    ? Files.createTempFile("user-config-", ".json")
                    : Files.createTempFile(directory, "user-config-", ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), normalized);
            moveConfigFile(tempFile, resolvedUserConfigPath);
            return normalized;
        } catch (IOException ex) {
            throw new IllegalStateException("保存用户配置失败：" + resolvedUserConfigPath, ex);
        }
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
        normalized.setModelFactors(normalizeModelFactors(normalized.getModelFactors()));
        normalized.setGlobalParameters(normalizeGlobalParameters(normalized.getGlobalParameters()));
        normalized.setSelectedRows(normalizeSelectedRows(normalized.getSelectedRows()));
        return normalized;
    }

    private UserConfig.GlobalParameters normalizeGlobalParameters(UserConfig.GlobalParameters parameters) {
        UserConfig.GlobalParameters defaults = UserConfig.GlobalParameters.defaults();
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
        UserConfig.ModelFactors defaults = UserConfig.ModelFactors.defaults();
        UserConfig.ModelFactors normalized = factors == null ? new UserConfig.ModelFactors() : factors;
        normalized.setHostTeamGoalFactor(normalizeNumber(normalized.getHostTeamGoalFactor(), defaults.getHostTeamGoalFactor(), 0.1D, 3.0D));
        normalized.setSeedTeamGoalFactor(normalizeNumber(normalized.getSeedTeamGoalFactor(), defaults.getSeedTeamGoalFactor(), 0.1D, 3.0D));
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
            config.setModelFactors(UserConfig.ModelFactors.defaults());
            config.setGlobalParameters(UserConfig.GlobalParameters.defaults());
            config.setSelectedRows(new LinkedHashMap<>());
            return config;
        }

    }

}
