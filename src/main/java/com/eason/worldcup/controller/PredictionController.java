package com.eason.worldcup.controller;

import com.eason.worldcup.model.ModelOverviewResponse;
import com.eason.worldcup.model.PredictionQueryResponse;
import com.eason.worldcup.model.UserConfig;
import com.eason.worldcup.service.PredictionService;
import com.eason.worldcup.service.UserConfigService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/worldcup")
public class PredictionController {

    private final PredictionService predictionService;

    private final UserConfigService userConfigService;

    public PredictionController(PredictionService predictionService, UserConfigService userConfigService) {
        this.predictionService = predictionService;
        this.userConfigService = userConfigService;
    }

    @GetMapping("/predictions")
    public PredictionQueryResponse queryPredictions(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "simulations", required = false) Integer simulations,
            @RequestParam(value = "hostTeamGoalFactor", required = false) Double hostTeamGoalFactor,
            @RequestParam(value = "seedTeamGoalFactor", required = false) Double seedTeamGoalFactor,
            @RequestParam(value = "handicapSmoothingFactor", required = false) Double handicapSmoothingFactor,
            @RequestParam(value = "baseMatchWeight", required = false) Double baseMatchWeight,
            @RequestParam(value = "recentHalfYearBonus", required = false) Double recentHalfYearBonus,
            @RequestParam(value = "worldCupBonus", required = false) Double worldCupBonus) {
        return predictionService.queryByDate(
                date,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                handicapSmoothingFactor,
                baseMatchWeight,
                recentHalfYearBonus,
                worldCupBonus);
    }

    @GetMapping("/overview")
    public ModelOverviewResponse overview() {
        return predictionService.overview();
    }

    @PostMapping("/data/refresh")
    public ModelOverviewResponse refreshData() {
        return predictionService.refreshData();
    }

    @GetMapping("/user-config")
    public UserConfig userConfig() {
        return userConfigService.load();
    }

    @PutMapping("/user-config")
    public UserConfig saveUserConfig(@RequestBody UserConfig userConfig) {
        return userConfigService.save(userConfig);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "message", "世界杯预测服务运行中");
    }

}
