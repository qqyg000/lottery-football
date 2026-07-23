# lottery-football

竞彩足球胜平负概率预测与推荐回测程序。后端使用 Spring Boot，前端使用 Vue 2，支持按赛事和日期查询赛程、赛果、体彩赔率及模型预测结果。

> 当前内置比赛数据快照及球队名映射更新至 2026-07-23。项目仅用于数据分析、算法学习和开发验证，不构成投注建议。

## 主要功能

- 支持 15 类赛事，按赛事和日期查询近期赛程、完场比分与比赛状态
- 使用泊松分布和蒙特卡洛模拟计算常规及让球胜平负概率
- 展示双方期望进球、总进球数和比分预测
- 读取中国体彩网开售状态、让球数及胜平负赔率
- 使用统一球队名映射关联历史数据、赛程、体彩赔率和页面展示
- 点击球队名称查看主队近况、双方历史交锋和客队近况，每栏最多 10 场；近况球队名统一采用体彩标准名
- 每类赛事维护“本届/含上届 × 稳健/激进”四套独立参数档案
- 前端动态配置进球系数、让球阈值、赔率阈值和比赛类型权重，修改后自动重算
- 提供参数说明提示、异步数据更新、推荐回测及进度展示
- 回测展示场均投注、场均返奖、采样率、命中率和 ROI

模型统一使用 90 分钟加伤停补时的全场比分，不把加时赛和点球大战计入常规赛果。

## 支持的赛事

| 赛事 | 代码 |
|---|---|
| 世界杯 | `WORLD_CUP` |
| 欧洲杯 | `EUROPEAN_CHAMPIONSHIP` |
| 美洲杯 | `COPA_AMERICA` |
| 世俱杯 | `CLUB_WORLD_CUP` |
| 欧罗巴 | `EUROPA_LEAGUE` |
| 欧冠 | `CHAMPIONS_LEAGUE` |
| 英超 | `PREMIER_LEAGUE` |
| 西甲 | `LA_LIGA` |
| 意甲 | `SERIE_A` |
| 德甲 | `BUNDESLIGA` |
| 法甲 | `LIGUE_1` |
| 巴甲 | `BRAZIL_SERIE_A` |
| 葡超 | `PRIMEIRA_LIGA` |
| 荷甲 | `EREDIVISIE` |
| 阿甲 | `ARGENTINE_PRIMERA_DIVISION` |

前端仅支持单选具体赛事。推荐回测接口仍兼容 `ALL` 或逗号分隔的多个赛事代码，供脚本批量调用。

## 技术栈与数据

- Java 17、Spring Boot 3.3.5、Maven
- Vue 2.7.16、Vue CLI 5
- Maven 自动安装 Node.js 20.17.0 和 npm 10.8.2，并把前端产物打入 Spring Boot jar

当前数据文件：

| 文件 | 行数 | 日期范围 |
|---|---:|---|
| `historical_matches.csv` | 115,260 | 2014-10-22 至 2026-07-22 |
| `historical_odds_data.csv` | 30,953 | 2014-10-22 至 2026-07-22 |
| `team_name_mappings.csv` | 10,481 | 2014-06-24 至 2026-08-23 |

主要数据来自 FotMob、Futbol24、阿塞拜疆 PFL、Sofascore、OpenFootball、ESPN、FootballCSV、`international_results` 和中国体彩网。外部接口不可用时，服务继续使用内置数据和本地缓存。完整来源说明见 [DATA_SOURCES.md](DATA_SOURCES.md)。

## 快速启动

### 环境要求

- JDK 17 或更高版本
- Maven 3.8 或更高版本

Node.js 不需要手动安装。

### 构建

双击 `build.cmd`，或执行：

```powershell
mvn clean package
```

Maven 会构建前后端并生成可执行 jar。`build.cmd` 还会生成分发目录：

```text
target/dist/lottery-football-1.0.0.jar
target/dist/run.cmd
```

### 启动

开发目录可双击 `启动程序.cmd` 或 `run.cmd`；分发目录可运行 `target/dist/run.cmd`。

启动地址：

```text
http://127.0.0.1:8080
```

健康检查：

```powershell
Invoke-RestMethod "http://127.0.0.1:8080/api/football/health"
```

### 测试

```powershell
mvn test
.\target\node\npm.cmd --prefix frontend test
```

## 目录结构

```text
lottery-football
├─ frontend/                  Vue 页面与前端回测测试
├─ scripts/                   数据导入、校正、更新和参数优化脚本
├─ config/                    运行时缓存与用户参数
├─ src/main/java/             Spring Boot 后端
├─ src/main/resources/data/   历史比赛、赔率和球队名映射
├─ pom.xml
├─ build.cmd
├─ run.cmd
└─ 启动程序.cmd
```

## 后端接口

主要接口同时兼容 `/api/football` 和旧版 `/api/worldcup` 前缀。

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/football/health` | 健康检查 |
| GET | `/api/football/overview` | 获取赛事概览和可查询日期 |
| GET | `/api/football/predictions` | 查询指定日期的概率预测 |
| GET | `/api/football/head-to-head` | 查询双方历史交锋 |
| GET | `/api/football/head-to-head/overview` | 查询主客队近况及双方历史交锋 |
| POST | `/api/football/data/refresh` | 同步刷新运行时数据 |
| POST | `/api/football/data/refresh/jobs` | 创建异步数据更新任务 |
| GET | `/api/football/data/refresh/jobs/{jobId}` | 查询数据更新进度 |
| POST | `/api/football/data/refresh-historical-odds` | 补取官方历史赔率 |
| GET | `/api/football/recommendation-backtest` | 同步执行推荐回测 |
| POST | `/api/football/recommendation-backtest/jobs` | 创建异步回测任务 |
| GET | `/api/football/recommendation-backtest/jobs/{jobId}` | 查询回测进度 |
| GET、PUT | `/api/football/user-config` | 读取或保存用户配置 |

查询示例：

```http
GET /api/football/predictions?competition=CHAMPIONS_LEAGUE&date=2026-07-14&simulations=50000
```

主要参数：

| 参数 | 说明 |
|---|---|
| `competition` | 赛事代码，普通查询默认 `WORLD_CUP` |
| `date` | 比赛日期，格式 `yyyy-MM-dd` |
| `simulations` | 模拟次数，范围 1,000 至 500,000，默认 50,000 |
| `includePreviousEdition` | 是否把上届赛事纳入回测范围 |

所有日期统一转换为 `Asia/Shanghai`。只有球队、日期和主客方向均可靠匹配时，系统才会挂载体彩比赛 ID 和赔率。

## 参数档案

每类赛事在 `config/user-config.json` 中保存四套参数档案，共 15 × 4 = 60 套：

| 后缀 | 范围 | 方案 |
|---|---|---|
| `CURRENT:STABLE` | 仅本届 | 稳健 |
| `CURRENT:AGGRESSIVE` | 仅本届 | 激进 |
| `PREVIOUS:STABLE` | 含上届 | 稳健 |
| `PREVIOUS:AGGRESSIVE` | 含上届 | 激进 |

完整键格式为 `{competition}:{range}:{preset}`。尚未开赛的赛事自动使用 `PREVIOUS` 档案，参数修改后会保存并自动重新计算。

默认参数：

| 参数 | 默认值 | 说明 |
|---|---:|---|
| `seedTeamGoalFactor` | 1.85 | 世界杯种子队进球修正 |
| `hostTeamGoalFactor` | 1.10 | 世界杯东道主进球修正 |
| `homeTeamGoalFactor` | 1.06 | 主场进球修正 |
| `handicapSmoothingFactor` | 0.274 | 让球概率平滑强度 |
| `officialMatchWeight` | 1.00 | 正式比赛样本权重 |
| `internationalFriendlyWeight` | 0.50 | 国家队友谊赛权重 |
| `clubFriendlyWeight` | 0.30 | 俱乐部友谊赛权重 |
| `recommendationOdds` | 1.03 | 推荐项最低竞彩赔率 |
| `handicapRecommendationThreshold` | 68.16% | 让球推荐阈值 |
| `handicapReverseThreshold` | 46.78% | 让球反向阈值 |
| `singleRecommendationThreshold` | 71.72% | 单项推荐阈值 |

页面圆形信息图标提供各参数和指标的口径说明。比赛类型权重范围为 0 至 1，`0` 表示不计入该类历史样本。

## 回测口径

页面范围按赛事届次过滤：

- “仅本届赛事”：本届开始日至本届结束日
- “含上届赛事”：上届开始日至本届结束日
- 本届尚未结束时，结束日期截取到北京时间当天
- 每场回测只读取比赛日前的历史数据，避免未来数据泄漏

只有已完赛且拥有完整比分、体彩比赛 ID 和至少一类完整赔率的场次进入推荐计算。

回测采用平投注入口径，每个推荐项投入 1 单位：

```text
totalStake = recommendedSelectionCount
totalReturn = sum(winningSelectionOdds)
netProfit = totalReturn - totalStake
ROI = (totalReturn / totalStake - 1) × 100%
场均投注 = totalStake / recommendedMatchCount
场均返奖 = totalReturn / recommendedMatchCount
命中率 = hitMatchCount / recommendedMatchCount × 100%
采样率 = recommendedMatchCount / completedMatchCount × 100%
```

没有推荐项时 ROI 为空；存在推荐项但全部未命中时 ROI 为 `-100%`。

## 模型说明

系统根据预测日期之前的正式比赛和降权友谊赛计算球队进攻强度、防守弱点及样本权重，并向 1.0 收缩以降低小样本波动。

```text
λ_home = baselineGoals × homeAttack × awayDefenseWeakness × homeAdvantage × h2hFactor
λ_away = baselineGoals × awayAttack × homeDefenseWeakness ÷ h2hFactor
P(X = k) = e^-λ × λ^k / k!
```

最终历史样本权重等于 Dixon-Coles 时间衰减权重乘以页面配置的比赛类型权重。模型通过蒙特卡洛采样统计胜平负、让球胜平负、总进球数和比分概率。

主队让球口径：

```text
adjustedHomeGoals = homeGoals + handicap
```

`-1` 表示主队让 1 球，`+1` 表示主队受让 1 球。

## 数据文件

### 历史比赛

`src/main/resources/data/historical_matches.csv`

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_score,away_score,neutral,match_type,source_competition
```

`match_type` 支持：

- `OFFICIAL`
- `INTERNATIONAL_FRIENDLY`
- `CLUB_FRIENDLY`

### 历史赔率

`src/main/resources/data/historical_odds_data.csv`

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_team_en,away_team_en,home_score,away_score,neutral,sporttery_match_number,handicap,normal_win,normal_draw,normal_lose,handicap_win,handicap_draw,handicap_lose
```

### 球队名映射

`src/main/resources/data/team_name_mappings.csv`

```text
competition,standard_team_name,alias_team_name,alias_type,source,last_seen_date
```

系统按来源优先级读取具体赛事和 `competition=*` 全局映射，体彩核验名称可以覆盖历史数据中的来源自名称。人工别名使用 `source=MANUAL`，修改后需要重启服务。

重新生成自动映射：

```powershell
node scripts/generate-team-name-mappings.mjs
```

## 数据更新与维护

页面“更新数据”会异步执行以下阶段：

1. 读取以当天为基准的体彩最近 30 天赛果
2. 刷新近期赛程，按统一球队名合并体彩、ESPN、FotMob 和 Futbol24 补充来源，其中包括阿塞超、阿塞杯、芬超、芬兰杯、丹超、丹麦杯、波超杯、波甲、奥甲、苏超、土超、土耳其杯、匈甲、匈牙利杯、克甲、塞浦甲和哈萨超
3. 重建球队模型
4. 更新赛事概览

运行时缓存写入 `config`，不会直接改写内置历史 CSV。

常用维护命令：

```powershell
# 运行时数据刷新
powershell -ExecutionPolicy Bypass -File scripts/update-history-data.ps1 `
  -Competition "CHAMPIONS_LEAGUE" -Date "2026-07-18"

# 导入原始历史赔率
powershell -ExecutionPolicy Bypass -File scripts/import-historical-odds.ps1 `
  -SourcePath "C:\path\to\his-data.csv"

# 导入全部赛事的历史比赛，并输出赛事类型统计
node scripts/import-all-historical-matches.mjs `
  --source="C:\path\to\his-data.csv" --start-date=2014-10-22 --write `
  --stats-output=reports/his-data-competition-stats.csv

# 导入公共历史比赛
node scripts/import-public-history.mjs --write

# 导入参赛球队补充比赛
node scripts/import-supplemental-history.mjs --write --compact

# 仅补充俱乐部历史，跳过国家队公共源
node scripts/import-supplemental-history.mjs --write --compact --skip-national

# 只补取阿塞超、阿塞杯和巴甲
node scripts/import-supplemental-history.mjs --write --compact --skip-national `
  --only-sources ESPN-bra.1,FOTMOB-262,FUTBOL24-525,VERIFIED-PFL

# 只补取芬超、芬兰杯、丹超、波超杯、波甲、奥甲和苏超
node scripts/import-supplemental-history.mjs --write --compact --skip-national `
  --only-sources FUTBOL24-322,FUTBOL24-324,FUTBOL24-28,FUTBOL24-297,FUTBOL24-107,FUTBOL24-15,FUTBOL24-51

# 只补取土超、土耳其杯、丹麦杯、匈甲、匈牙利杯和克甲
node scripts/import-supplemental-history.mjs --write --compact --skip-national `
  --only-sources FUTBOL24-133,FUTBOL24-537,FUTBOL24-33,FUTBOL24-92,FUTBOL24-531,FUTBOL24-26

# 校正比分并压缩数据
node scripts/reconcile-historical-scores.mjs --write --compact

# 核验参数档案
node scripts/optimize-profile-parameters.mjs --verify-only=true
```

执行会写入正式 CSV 的脚本前，建议先省略 `--write` 检查增量和汇总结果。

## 关键配置

| 文件或节点 | 用途 |
|---|---|
| `src/main/resources/application.yml` | 数据源、时区、刷新窗口和缓存路径 |
| `config/user-config.json` | 60 套参数档案和页面配置 |
| `config/club-competition-schedules.json` | 俱乐部赛事运行时赛程缓存 |
| `config/sporttery-market-selections.json` | 体彩玩法及赔率缓存 |
| `team_name_mappings.csv` | 体彩标准球队名与数据源别名 |

修改 CSV 字段或赛事代码时，需要同步检查 Java 加载器、数据脚本和前端赛事列表。

## 法律免责声明

本项目仅供足球数据分析、算法学习、技术研究与开发验证，不构成任何形式的投注建议、投资建议、盈利承诺或结果保证。足球比赛结果、赔率及模型预测均具有不确定性，项目作者及贡献者不对数据的准确性、完整性、及时性或适用性作任何明示或暗示的保证。

使用者应自行判断并承担使用本项目所产生的全部风险与责任，并遵守所在国家或地区适用的法律法规及第三方数据源的使用条款。严禁将本项目用于非法赌博、欺诈或其他违法活动；未成年人不得参与任何形式的彩票购买或博彩活动。因使用或无法使用本项目而产生的任何直接或间接损失，项目作者及贡献者在法律允许的范围内不承担责任。

本项目引用的赛事、赔率及其他第三方数据，其权利归相应权利人所有。如相关内容侵犯了您的合法权益，请联系项目维护者处理。对本项目的赞助完全出于自愿，仅用于支持项目开发与维护，不代表购买投注服务，也不构成任何收益或预测结果的承诺。

## 赞助支持

如果这个项目对你有帮助，欢迎赞助。

<table>
  <tr>
    <th>支付宝</th>
    <th>微信</th>
  </tr>
  <tr>
    <td><img src="docs/images/alipay-qr.png" alt="支付宝收款码" width="260"></td>
    <td><img src="docs/images/wechat-pay-qr.png" alt="微信收款码" width="260"></td>
  </tr>
</table>
