package com.eason.worldcup.controller;

import com.eason.worldcup.model.Competition;
import com.eason.worldcup.model.HeadToHeadMatchResponse;
import com.eason.worldcup.model.ModelOverviewResponse;
import com.eason.worldcup.model.PredictionQueryResponse;
import com.eason.worldcup.model.RecommendationBacktestJobResponse;
import com.eason.worldcup.model.RecommendationBacktestResponse;
import com.eason.worldcup.model.SportteryHistoricalOddsRefreshResponse;
import com.eason.worldcup.model.UserConfig;
import com.eason.worldcup.service.PredictionService;
import com.eason.worldcup.service.RecommendationBacktestJobService;
import com.eason.worldcup.service.UserConfigService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping({"/api/football", "/api/worldcup"})
public class PredictionController {

    private final PredictionService predictionService;

    private final RecommendationBacktestJobService recommendationBacktestJobService;

    private final UserConfigService userConfigService;

    public PredictionController(
            PredictionService predictionService,
            RecommendationBacktestJobService recommendationBacktestJobService,
            UserConfigService userConfigService) {
        this.predictionService = predictionService;
        this.recommendationBacktestJobService = recommendationBacktestJobService;
        this.userConfigService = userConfigService;
    }

    @GetMapping("/predictions")
    public PredictionQueryResponse queryPredictions(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "simulations", required = false) Integer simulations,
            @RequestParam(value = "hostTeamGoalFactor", required = false) Double hostTeamGoalFactor,
            @RequestParam(value = "homeTeamGoalFactor", required = false) Double homeTeamGoalFactor,
            @RequestParam(value = "seedTeamGoalFactor", required = false) Double seedTeamGoalFactor,
            @RequestParam(value = "handicapSmoothingFactor", required = false) Double handicapSmoothingFactor,
            @RequestParam(value = "competition", defaultValue = "WORLD_CUP") String competition) {
        return predictionService.queryByDate(
                parseCompetition(competition),
                date,
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor,
                handicapSmoothingFactor);
    }

    @PostMapping("/recommendation-backtest/jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RecommendationBacktestJobResponse startRecommendationBacktest(
            @RequestParam(value = "simulations", required = false) Integer simulations,
            @RequestParam(value = "hostTeamGoalFactor", required = false) Double hostTeamGoalFactor,
            @RequestParam(value = "homeTeamGoalFactor", required = false) Double homeTeamGoalFactor,
            @RequestParam(value = "seedTeamGoalFactor", required = false) Double seedTeamGoalFactor,
            @RequestParam(value = "handicapSmoothingFactor", required = false) Double handicapSmoothingFactor,
            @RequestParam(value = "competition", defaultValue = "ALL") String competition) {
        return recommendationBacktestJobService.start(
                parseBacktestCompetitions(competition),
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor,
                handicapSmoothingFactor);
    }

    @GetMapping("/recommendation-backtest/jobs/{jobId}")
    public RecommendationBacktestJobResponse recommendationBacktestProgress(@PathVariable("jobId") String jobId) {
        RecommendationBacktestJobResponse response = recommendationBacktestJobService.find(jobId);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "回测任务不存在或已过期");
        }
        return response;
    }

    @GetMapping("/recommendation-backtest")
    public RecommendationBacktestResponse recommendationBacktest(
            @RequestParam(value = "simulations", required = false) Integer simulations,
            @RequestParam(value = "hostTeamGoalFactor", required = false) Double hostTeamGoalFactor,
            @RequestParam(value = "homeTeamGoalFactor", required = false) Double homeTeamGoalFactor,
            @RequestParam(value = "seedTeamGoalFactor", required = false) Double seedTeamGoalFactor,
            @RequestParam(value = "handicapSmoothingFactor", required = false) Double handicapSmoothingFactor,
            @RequestParam(value = "competition", defaultValue = "ALL") String competition) {
        return predictionService.queryRecommendationBacktest(
                parseBacktestCompetitions(competition),
                simulations,
                hostTeamGoalFactor,
                seedTeamGoalFactor,
                homeTeamGoalFactor,
                handicapSmoothingFactor);
    }

    @GetMapping("/head-to-head")
    public List<HeadToHeadMatchResponse> headToHead(
            @RequestParam(value = "competition", defaultValue = "WORLD_CUP") String competition,
            @RequestParam("matchId") String matchId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return predictionService.queryHeadToHead(parseCompetition(competition), matchId, limit);
    }

    @GetMapping("/overview")
    public ModelOverviewResponse overview(
            @RequestParam(value = "competition", defaultValue = "WORLD_CUP") String competition) {
        return predictionService.overview(parseCompetition(competition));
    }

    @PostMapping("/data/refresh")
    public ModelOverviewResponse refreshData(
            @RequestParam(value = "competition", defaultValue = "WORLD_CUP") String competition,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return predictionService.refreshData(parseCompetition(competition), date);
    }

    @PostMapping("/data/refresh-historical-odds")
    public SportteryHistoricalOddsRefreshResponse refreshHistoricalOdds(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            return predictionService.refreshHistoricalOdds(startDate, endDate);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
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
        return Map.of("status", "OK", "message", "竞彩足球预测服务运行中");
    }

    private Competition parseCompetition(String value) {
        try {
            return Competition.fromCode(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的赛事类型：" + value, ex);
        }
    }

    private Set<Competition> parseBacktestCompetitions(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value.trim())) {
            return Set.of();
        }
        Set<Competition> competitions = new LinkedHashSet<>();
        for (String code : value.split(",")) {
            if (code == null || code.isBlank()) {
                continue;
            }
            if ("ALL".equalsIgnoreCase(code.trim())) {
                return Set.of();
            }
            competitions.add(parseCompetition(code));
        }
        return Set.copyOf(competitions);
    }

}
