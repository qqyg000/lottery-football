# 参数方案再次精调报告

> 注意：本报告的采样率使用已经废止的旧分母，且部分推荐数和 ROI 受旧版赔率玩法可用性判断影响，不能继续作为当前绩效基准。当前复核结果见 [采样率口径与历史方案数据审计](sampling-rate-definition-audit-2026-07-24.md)。

生成日期：2026-07-24

## 本轮规则

- 采样率定义：`推荐比赛数 ÷ 方案回测时间范围内已完赛比赛数`
- 普通方案采样率允许在本轮开始前的采样率上下浮动 2 个百分点
- 允许普通方案突破原“稳健 >66.6%、激进 >=50%”约束
- `EUROPA_LEAGUE:CURRENT:STABLE`、`CHAMPIONS_LEAGUE:CURRENT:STABLE`、`CHAMPIONS_LEAGUE:CURRENT:AGGRESSIVE` 不受采样率约束
- 正式比赛权重允许在 `[1.00, 3.00]` 范围内参与优化
- 在满足对应约束后优先最大化 ROI
- 最终指标使用 50,000 次模拟复核

本轮以开始前配置快照 `target/user-config-before-sampling-fine-tune.json` 为基线重新执行，避免多次局部精调造成采样率允许范围累计漂移。

## 汇总

- 配置中共有 68 套参数档案
- 50 套档案存在有效赔率回测样本并完成精调
- 47 套普通档案全部位于各自基线采样率上下 2 个百分点内，最大绝对偏移为 1.59 个百分点
- 3 套指定档案按无采样率约束完成全参数空间搜索
- 25 个有样本的赛事/时间范围组合全部复核通过，无约束失败
- 32 套最终方案的 ROI 高于本轮开始前配置
- 所有 68 套档案的正式比赛权重都位于 `[1.00, 3.00]`
- 8 套档案最终选择了高于 1 的正式比赛权重，其余档案选择 1
- 4 套阿甲档案因只有 3 场已完赛且没有有效赔率，无法优化
- 英超、西甲、德甲、意甲、法甲、葡超、荷甲本届赛事尚未开始，对应 14 套档案没有本届完赛样本，保持当前配置

## 最终复核指标

| 赛事范围 | 稳健推荐 | 稳健采样率 | 稳健 ROI | 激进推荐 | 激进采样率 | 激进 ROI |
|---|---:|---:|---:|---:|---:|---:|
| WORLD_CUP:PREVIOUS | 112/168 | 66.67% | 12.84% | 81/168 | 48.21% | 16.95% |
| WORLD_CUP:CURRENT | 71/104 | 68.27% | 15.26% | 55/104 | 52.88% | 19.95% |
| EUROPEAN_CHAMPIONSHIP:PREVIOUS | 69/102 | 67.65% | 14.48% | 52/102 | 50.98% | 27.71% |
| EUROPEAN_CHAMPIONSHIP:CURRENT | 47/51 | 92.16% | 19.91% | 47/51 | 92.16% | 24.19% |
| COPA_AMERICA:PREVIOUS | 60/86 | 69.77% | 22.20% | 43/86 | 50.00% | 23.57% |
| COPA_AMERICA:CURRENT | 22/32 | 68.75% | 52.82% | 16/32 | 50.00% | 73.63% |
| CLUB_WORLD_CUP:PREVIOUS | 47/69 | 68.12% | 34.07% | 36/69 | 52.17% | 30.69% |
| CLUB_WORLD_CUP:CURRENT | 43/63 | 68.25% | 41.37% | 33/63 | 52.38% | 58.72% |
| EUROPA_LEAGUE:PREVIOUS | 132/190 | 69.47% | 27.65% | 95/190 | 50.00% | 42.79% |
| EUROPA_LEAGUE:CURRENT | 1/21 | 4.76% | 285.00% | 11/21 | 52.38% | 45.25% |
| CHAMPIONS_LEAGUE:PREVIOUS | 167/248 | 67.34% | 26.21% | 120/248 | 48.39% | 41.71% |
| CHAMPIONS_LEAGUE:CURRENT | 1/42 | 2.38% | 163.00% | 1/42 | 2.38% | 163.00% |
| PREMIER_LEAGUE:PREVIOUS | 236/338 | 69.82% | 6.94% | 185/338 | 54.73% | 9.91% |
| LA_LIGA:PREVIOUS | 234/354 | 66.10% | 17.62% | 181/354 | 51.13% | 24.56% |
| BUNDESLIGA:PREVIOUS | 193/282 | 68.44% | 16.35% | 148/282 | 52.48% | 18.81% |
| SERIE_A:PREVIOUS | 236/350 | 67.43% | 17.44% | 178/350 | 50.86% | 20.79% |
| LIGUE_1:PREVIOUS | 147/224 | 65.63% | 13.77% | 128/224 | 57.14% | 25.87% |
| PRIMEIRA_LIGA:PREVIOUS | 129/177 | 72.88% | 12.45% | 93/177 | 52.54% | 19.96% |
| EREDIVISIE:PREVIOUS | 128/189 | 67.72% | 18.90% | 96/189 | 50.79% | 29.28% |
| SWEDISH_ALLSVENSKAN:PREVIOUS | 16/24 | 66.67% | 50.67% | 12/24 | 50.00% | 45.62% |
| SWEDISH_ALLSVENSKAN:CURRENT | 16/24 | 66.67% | 50.67% | 14/24 | 58.33% | 53.50% |
| FINNISH_VEIKKAUSLIIGA:PREVIOUS | 18/26 | 69.23% | 22.35% | 14/26 | 53.85% | 33.06% |
| FINNISH_VEIKKAUSLIIGA:CURRENT | 18/26 | 69.23% | 24.90% | 14/26 | 53.85% | 33.06% |
| K_LEAGUE_1:PREVIOUS | 17/24 | 70.83% | 17.56% | 12/24 | 50.00% | 26.17% |
| K_LEAGUE_1:CURRENT | 16/24 | 66.67% | 24.91% | 12/24 | 50.00% | 26.17% |

## 无采样率约束方案

| 档案 | 基线 ROI | 最终 ROI | 基线采样率 | 最终采样率 | 推荐场次 |
|---|---:|---:|---:|---:|---:|
| EUROPA_LEAGUE:CURRENT:STABLE | -20.56% | 285.00% | 42.86% | 4.76% | 1/21 |
| CHAMPIONS_LEAGUE:CURRENT:STABLE | -2.25% | 163.00% | 4.76% | 2.38% | 1/42 |
| CHAMPIONS_LEAGUE:CURRENT:AGGRESSIVE | -100.00% | 163.00% | 0.00% | 2.38% | 1/42 |

这 3 套方案的最终 ROI 都由 1 场推荐产生，统计稳定性很弱，存在明显的小样本和过拟合风险。它们满足本轮明确给出的无采样率约束，但不应与大样本方案的稳健性直接比较。

## 正式比赛权重高于 1 的档案

| 档案 | 正式比赛权重 |
|---|---:|
| WORLD_CUP:CURRENT:STABLE | 1.02 |
| WORLD_CUP:CURRENT:AGGRESSIVE | 1.02 |
| EUROPEAN_CHAMPIONSHIP:CURRENT:STABLE | 1.01 |
| CLUB_WORLD_CUP:CURRENT:AGGRESSIVE | 1.02 |
| PREMIER_LEAGUE:PREVIOUS:AGGRESSIVE | 1.02 |
| LIGUE_1:PREVIOUS:STABLE | 1.02 |
| SWEDISH_ALLSVENSKAN:PREVIOUS:STABLE | 1.01 |
| SWEDISH_ALLSVENSKAN:CURRENT:STABLE | 1.01 |

## 产物

- `config/user-config.json`：最终 68 套参数配置
- `target/profile-sampling-fine-tune-report.json`：50 套可回测档案的基线与精调结果
- `target/profile-unconstrained-europa-current-stable.json`：欧罗巴本届稳健无约束搜索结果
- `target/profile-unconstrained-champions-current.json`：欧冠本届稳健/激进无约束搜索结果
- `target/profile-verification-report.json`：最终统一复核结果
