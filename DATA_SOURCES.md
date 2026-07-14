# 数据源说明

当前项目内置的数据文件用于程序可直接运行和演示算法流程。

## 2026 世界杯赛程

`src/main/resources/data/schedule_2026.csv` 使用 2026 世界杯小组赛赛程作为种子数据。

建议生产使用时以 FIFA 官方赛程页面为准，并定期更新本地 CSV：

- FIFA World Cup 2026 fixtures and results
- ESPN FIFA World Cup schedule

## 历史战绩

`src/main/resources/data/history_matches.csv` 内置部分世界杯历史比赛和本届已完赛比分，用于演示建模流程。

建议生产使用时替换为完整国际比赛数据集，例如：

- Kaggle: International football results from 1872
- GitHub: martj42/international_results
- Fjelstul World Cup Database

替换时保持字段：

```text
match_date,tournament,home_team,away_team,home_score,away_score,neutral
```

其中 `home_team` 和 `away_team` 必须与赛程文件中的英文队名一致。
