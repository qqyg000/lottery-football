# lottery-football

竞彩足球胜平负概率预测程序。后端使用 Spring Boot，前端使用 Vue 2，支持按赛事和日期查询赛程、赛果、体彩赔率及模型预测结果。

> 当前内置数据快照更新于 2026-07-18。项目仅用于数据分析、算法学习和系统开发验证，不构成投注建议。

## 一、主要功能

- 查询 15 类赛事的历史比赛、近期赛程和完场结果
- 联赛下拉框仅支持单选，点击联赛后立即切换，不提供多选、“全部”或搜索功能
- 计算常规胜平负概率：主胜、平局、主负
- 计算主队让球胜平负概率：`-3`、`-2`、`-1`、`+1`、`+2`、`+3`
- 展示双方期望进球、前三总进球数和前三比分预测
- 展示比赛状态、比分、分组、开球时间和场地
- 从中国体彩网读取胜平负开售状态、让球数和最新赔率
- 体彩赔率匹配成功后，接口和页面统一以体彩主客队名为准
- 使用 `team_name_mappings.csv` 统一历史数据、赛程、回测和页面球队名
- 点击球队名称查看双方最多 50 场正式比赛和降权友谊赛
- 每类赛事维护“本届/含上届 × 稳定/激进”四套独立参数档案
- 提供推荐结果回测、平投注入 ROI、模型参数调整和用户配置持久化

模型统一使用 90 分钟加伤停补时的全场比分，不把加时赛和点球大战计入常规赛果。

## 二、支持的赛事

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

前端下拉框只展示上述 15 类赛事且仅支持单选，不提供“全部”和搜索入口。预测、概览、历史交锋和刷新接口应传入一个具体赛事代码；推荐回测接口仍兼容 `ALL` 或逗号分隔的多个赛事代码，供脚本和批量接口调用使用。

## 三、当前数据快照

以下行数不含 CSV 表头：

| 文件 | 行数 | 日期范围 | 赛事分类数 | 列数 |
|---|---:|---|---:|---:|
| `historical_matches.csv` | 56,809 | 2014-06-12 至 2026-07-18 | 19 | 10 |
| `historical_odds_data.csv` | 30,942 | 2014-10-22 至 2026-07-17 | 15 | 18 |
| `team_name_mappings.csv` | 4,435 | 截至 2026-07-22 | 19 类赛事及全局映射 | 6 |

历史比赛的 19 个分类由 15 个前端可选赛事和 4 个内部补充分类组成：国家队其他正式比赛、国家队国际窗口友谊赛、俱乐部其他正式比赛、俱乐部正常阵容友谊赛。内部分类只用于补充参赛球队样本，不会出现在赛事下拉框中。

当前两个文件均保证 `match_id` 在各自文件内唯一，中文主客队名和完场比分不为空。赔率记录中的英文队名只有在能够确定映射时才填写，无法可靠映射时允许留空。

## 四、技术栈与数据来源

后端：

- Java 17
- Spring Boot 3.3.5
- Maven

前端：

- Vue 2.7.16
- Vue CLI 5
- Maven 打包时自动安装 Node.js 20.17.0、npm 10.8.2 并执行前端构建
- 前端产物自动复制到 Spring Boot jar 的 `static` 目录

数据来源：

- 现代赛事历史比分：FotMob 已核验赛事赛季数据，以及未被其覆盖的唯一竞彩场次
- 世界杯历史比分：OpenFootball `worldcup.json`
- 国家队正式比赛和国际友谊赛：Mart Jürisoo `international_results`
- 1955-56 至 2015-16 赛季欧冠历史比分：FootballCSV `europe-champions-league`
- 世俱杯历史比分：OpenFootball `club-world-cup`
- 俱乐部杯赛、洲际赛和友谊赛：ESPN Scoreboard
- 世界杯近期赛程：OpenFootball，ESPN 作为补充和回退来源
- 欧冠近期赛程：ESPN `uefa.champions_qual` 和 `uefa.champions`
- 其余 13 类赛事近期赛程：ESPN Scoreboard
- 竞彩赛果、在售玩法和赔率：中国体彩网官方接口
- 外部接口暂时不可用时：继续使用本地历史数据和已有缓存

所有接口日期统一转换为 `Asia/Shanghai`。更完整的数据源说明见 [DATA_SOURCES.md](DATA_SOURCES.md)。

## 五、目录结构

```text
lottery-football
├─ pom.xml
├─ build.cmd
├─ run.cmd
├─ 启动程序.cmd
├─ README.md
├─ DATA_SOURCES.md
├─ frontend
│  ├─ src
│  │  ├─ App.vue
│  │  ├─ backtest-roi.mjs
│  │  └─ main.js
│  └─ tests
│     └─ backtest-roi.test.mjs
├─ scripts
│  ├─ generate-team-name-mappings.mjs
│  ├─ import-historical-odds.ps1
│  ├─ import-public-history.mjs
│  ├─ import-supplemental-history.mjs
│  ├─ merge-sporttery-cache-odds.ps1
│  ├─ reconcile-historical-scores.mjs
│  ├─ update-history-data.ps1
│  ├─ rebuild-data.ps1
│  ├─ backtest-recommendations.mjs
│  ├─ optimize-backtest-parameters.mjs
│  └─ optimize-profile-parameters.mjs
└─ src
   └─ main
      ├─ java
      │  └─ com/eason/worldcup
      │     ├─ LotteryFootballApplication.java
      │     ├─ controller
      │     ├─ model
      │     ├─ service
      │     └─ util
      └─ resources
         ├─ application.yml
         └─ data
            ├─ historical_matches.csv
            ├─ historical_odds_data.csv
            └─ team_name_mappings.csv
```

运行时缓存和用户配置保存在 `config` 目录，不属于内置历史 CSV。

## 六、Windows 构建与启动

### 1. 前置环境

需要安装：

- JDK 17 或更高版本
- Maven 3.8 或更高版本

Node.js 不需要手动安装。Maven 会通过 `frontend-maven-plugin` 下载项目指定版本；数据维护脚本也会优先使用系统 `node`，找不到时使用 Maven 下载到 `target/node` 的运行时。

### 2. 打包

双击：

```text
build.cmd
```

也可以在项目根目录执行：

```powershell
mvn clean package
```

该命令会执行 Java 测试并构建前后端。`build.cmd` 会先停止当前项目目录下正在运行的旧服务，再使用 `-DskipTests` 构建分发包并生成：

```text
target/dist/lottery-football-1.0.0.jar
target/dist/run.cmd
```

### 3. 启动

开发目录可双击根目录的 `启动程序.cmd` 或 `run.cmd`；分发目录可双击：

```text
target/dist/run.cmd
```

启动后浏览器会打开：

```text
http://127.0.0.1:8080
```

健康检查：

```powershell
Invoke-RestMethod "http://127.0.0.1:8080/api/football/health"
```

### 4. 测试

Java 测试：

```powershell
mvn test
```

完成一次 Maven 构建后，使用项目下载的 npm 执行前端 ROI 测试：

```powershell
.\target\node\npm.cmd --prefix frontend test
```

## 七、后端接口

所有主要接口同时兼容 `/api/football` 和旧版 `/api/worldcup` 前缀。查询、概览和刷新接口未传 `competition` 时默认使用 `WORLD_CUP`，回测接口未传时默认使用全部 15 类赛事。

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/football/health` | 健康检查 |
| GET | `/api/football/overview` | 获取指定赛事的数据概览和可查询日期 |
| GET | `/api/football/predictions` | 查询指定赛事、日期的概率预测 |
| GET | `/api/football/head-to-head` | 查询某场比赛双方的历史交锋 |
| POST | `/api/football/data/refresh` | 刷新近期赛程、模型和体彩数据 |
| POST | `/api/football/data/refresh/jobs` | 创建带进度的异步数据更新任务 |
| GET | `/api/football/data/refresh/jobs/{jobId}` | 查询数据更新进度 |
| POST | `/api/football/data/refresh-historical-odds` | 按日期范围补取官方历史赔率 |
| GET | `/api/football/recommendation-backtest` | 同步执行推荐回测 |
| POST | `/api/football/recommendation-backtest/jobs` | 创建异步推荐回测任务 |
| GET | `/api/football/recommendation-backtest/jobs/{jobId}` | 查询异步回测进度 |
| GET、PUT | `/api/football/user-config` | 读取或保存用户配置 |

### 1. 数据概览

```http
GET /api/football/overview?competition=CHAMPIONS_LEAGUE
```

主要返回字段：

| 字段 | 说明 |
|---|---|
| `competition` | 赛事代码 |
| `competitionName` | 赛事中文名 |
| `historicalMatchCount` | 历史比赛样本数 |
| `scheduleMatchCount` | 已加载赛程数 |
| `completedMatchCount` | 已完赛赛程数 |
| `baselineGoals` | 基准场均进球 |
| `scheduleDates` | 可查询的比赛日期 |

### 2. 按日期查询概率

```http
GET /api/football/predictions?competition=CHAMPIONS_LEAGUE&date=2026-07-14&simulations=50000
```

| 参数 | 必填 | 说明 |
|---|---|---|
| `competition` | 否 | 具体赛事代码，默认 `WORLD_CUP` |
| `date` | 是 | 比赛日期，格式 `yyyy-MM-dd` |
| `simulations` | 否 | 蒙特卡洛模拟次数，默认 50,000，范围 1,000 至 500,000 |

每场比赛还会返回以下体彩字段：

| 字段 | 说明 |
|---|---|
| `sportteryMatchId` | 中国体彩网竞彩足球比赛 ID，未匹配时为空 |
| `sportteryMatchNumber` | 体彩赛事编号，例如“周一201” |
| `sportteryNormalAvailable` | 是否开售全场胜平负 |
| `sportteryHandicap` | 全场让球胜平负让球数，例如 `-1`、`+1` |
| `sportteryNormalOdds` | 最新不让球胜平负赔率，包含 `win`、`draw`、`lose`、`updatedAt` |
| `sportteryHandicapOdds` | 最新让球胜平负赔率，包含 `win`、`draw`、`lose`、`updatedAt` |

#### 体彩队名适配

近期赛程与体彩赔率来自不同数据源，球队名称可能分别使用简称、英文名或不同中文译名。系统按赛事、比赛日期和主客队方向匹配比赛，所有球队名称先通过 `team_name_mappings.csv` 转换为体彩标准名称。只有主客队都能可靠对应时才挂载体彩比赛 ID 和赔率，避免将同日其他比赛错误关联。

数据更新、历史比赛加载、模型计算、回测去重、体彩赔率匹配和接口展示共用同一份映射。`homeTeamCn` 和 `awayTeamCn` 返回映射后的体彩标准名称；未收录的名称保持数据源原值，不进行猜测式替换。

当前明确适配的名称包括：

| 赛程源名称 | 体彩标准名称 |
|---|---|
| 米竞技、Atlético-MG | 米内罗竞技 |
| KuPS Kuopio | 库奥皮奥 |
| AGF | 奥胡斯 |
| 波兹南、Lech Poznan | 波兹南莱赫 |
| 格风暴、SK Sturm Graz | 格拉茨风暴 |
| Heart of Midlothian | 哈茨 |

新增球队别名时，直接在 `team_name_mappings.csv` 增加 `source=MANUAL` 的映射行，并重启服务。运行生成脚本时会保留 `MANUAL`、`VERIFIED_ALIAS` 和 `VERIFIED_SPORTTERY` 行，不需要再修改 Java 常量。

### 3. 推荐回测

推荐回测按赛事使用独立的届次日期，起始日和结束日都包含在范围内。页面选择“含上届赛事”时使用“上届开始至本届结束”，选择“仅本届赛事”时使用“本届开始至本届结束”。赛事日期表保留举办地公布的官方日期；由于竞彩比赛日期统一按 `Asia/Shanghai` 保存，回测结束边界额外包含官方结束日后的北京时间次日，避免遗漏决赛或末轮晚场。本届尚未结束时，实际回测结束日仍截取到 `Asia/Shanghai` 时区的当天。

| 赛事 | 赛事代码 | 上届开始 | 上届结束 | 本届开始 | 本届结束 |
|---|---|---|---|---|---|
| 世界杯 | `WORLD_CUP` | `2022-11-20` | `2022-12-18` | `2026-06-11` | `2026-07-19` |
| 欧洲杯 | `EUROPEAN_CHAMPIONSHIP` | `2021-06-11` | `2021-07-11` | `2024-06-14` | `2024-07-14` |
| 美洲杯 | `COPA_AMERICA` | `2019-06-14` | `2019-07-07` | `2024-06-20` | `2024-07-14` |
| 世俱杯 | `CLUB_WORLD_CUP` | `2023-12-12` | `2023-12-22` | `2025-06-14` | `2025-07-13` |
| 欧罗巴 | `EUROPA_LEAGUE` | `2025-07-10` | `2026-05-20` | `2026-07-09` | `2027-05-26` |
| 欧冠 | `CHAMPIONS_LEAGUE` | `2025-07-08` | `2026-05-30` | `2026-07-07` | `2027-06-05` |
| 英超 | `PREMIER_LEAGUE` | `2025-08-15` | `2026-05-24` | `2026-08-21` | `2027-05-30` |
| 西甲 | `LA_LIGA` | `2025-08-15` | `2026-05-24` | `2026-08-15` | `2027-05-30` |
| 意甲 | `SERIE_A` | `2025-08-23` | `2026-05-24` | `2026-08-22` | `2027-05-30` |
| 德甲 | `BUNDESLIGA` | `2025-08-22` | `2026-05-16` | `2026-08-28` | `2027-05-22` |
| 法甲 | `LIGUE_1` | `2025-08-15` | `2026-05-16` | `2026-08-20` | `2027-05-29` |
| 巴甲 | `BRAZIL_SERIE_A` | `2025-03-29` | `2025-12-07` | `2026-01-28` | `2026-12-02` |
| 葡超 | `PRIMEIRA_LIGA` | `2025-08-08` | `2026-05-17` | `2026-08-07` | `2027-05-16` |
| 荷甲 | `EREDIVISIE` | `2025-08-08` | `2026-05-17` | `2026-08-07` | `2027-05-23` |
| 阿甲 | `ARGENTINE_PRIMERA_DIVISION` | `2025-01-24` | `2025-12-13` | `2026-01-25` | `2026-12-13` |

批量接口选择 `ALL` 或多个赛事时，每场比赛仍按自身赛事和页面范围过滤，不会使用全局统一日期。起始日期晚于当天的未开始赛事没有本届回测样本。只有已完赛且有完整比分、竞彩比赛 ID 和至少一类胜平负赔率的场次才进入最终回测；每场回测只使用比赛日期之前的历史数据建模，避免未来数据泄漏。

#### 参数档案

每类赛事在 `config/user-config.json` 中保存四套参数档案，共 15 × 4 = 60 套：

| 档案键后缀 | 页面范围 | 方案 |
|---|---|---|
| `CURRENT:STABLE` | 仅本届赛事 | 稳定方案 |
| `CURRENT:AGGRESSIVE` | 仅本届赛事 | 激进方案 |
| `PREVIOUS:STABLE` | 含上届赛事 | 稳定方案 |
| `PREVIOUS:AGGRESSIVE` | 含上届赛事 | 激进方案 |

完整键格式为 `{competition}:{range}:{preset}`，例如 `PREMIER_LEAGUE:PREVIOUS:AGGRESSIVE`。当前届尚未开始时，页面预测和参数编辑自动使用该赛事的 `PREVIOUS` 档案，`CURRENT` 档案保留到本届开赛后使用。处于稳定方案时按钮显示“切换激进方案”，处于激进方案时显示“切换稳定方案”。

每套档案包含：

| 分组 | 参数 |
|---|---|
| `modelFactors` | `hostTeamGoalFactor`、`homeTeamGoalFactor`、`seedTeamGoalFactor`、`handicapSmoothingFactor` |
| `globalParameters` | `recommendationOdds`、`handicapRecommendationThreshold`、`handicapReverseThreshold`、`singleRecommendationThreshold` |

#### ROI 计算

回测采用平投注入口径，每个推荐项投入 1 个单位；同一场推荐两个赛果时总投入为 2 个单位。命中项按该项初盘赔率返奖，未命中项返奖为 0：

```text
totalStake = recommendedSelectionCount
totalReturn = sum(winningSelectionOdds)
netProfit = totalReturn - totalStake
ROI = (totalReturn / totalStake - 1) × 100%
场均返奖 = totalReturn / recommendedMatchCount
命中率 = hitMatchCount / recommendedMatchCount × 100%
采样率 = recommendedMatchCount / completedMatchCount × 100%
```

没有推荐项时不计算 ROI；存在推荐项但全部未命中时 ROI 为 `-100%`。ROI 不使用命中率、场均返奖或场均推荐数的平方推导。

采样率在页面结果卡片保留 1 位小数展示，并写入前端回测汇总对象及参数优化、核验报告。`recommendedMatchCount` 是运用当前方案参数后实际覆盖的比赛数，`completedMatchCount` 是回测范围内按比赛日期、主队和客队去重后的已完赛赛程总数，不要求存在竞彩比赛 ID 或赔率；总比赛数为 0 时采样率为 `null`。

### 4. 刷新运行时数据

```http
POST /api/football/data/refresh?competition=CHAMPIONS_LEAGUE&date=2026-07-18
```

刷新会重新加载系统配置的 15 类赛事近期赛程，并为这些赛事的参赛球队补充近期国家队正式赛、国际窗口友谊赛、俱乐部杯赛、洲际赛和一线队友谊赛，然后重建模型并强制刷新目标日期附近的体彩数据。`competition` 只决定接口最终返回哪一类赛事的概览；前端下拉只显示 15 类赛事且仅支持单选。

页面“更新数据”使用异步任务接口，按“赛程与补充数据→球队模型→竞彩数据→赛事概览”四个实际阶段更新进度，并复用回测的全屏蒙版和进度条样式。原同步接口保留用于兼容现有调用。

## 八、CSV 数据说明

### 1. 历史比赛数据

文件：

```text
src/main/resources/data/historical_matches.csv
```

该文件合并公共历史赛事数据、FotMob 已核验赛果、ESPN 补充比赛和未重复的竞彩完场场次，只保留 2014-06-12 至当前数据快照日的比赛，并保存模型需要的 10 列：

| 字段 | 说明 |
|---|---|
| `match_id` | 文件内唯一比赛 ID |
| `match_date` | 比赛日期，格式 `yyyy-MM-dd` |
| `competition` | 赛事代码 |
| `home_team_cn` | 主队中文名 |
| `away_team_cn` | 客队中文名 |
| `home_score` | 主队 90 分钟全场进球 |
| `away_score` | 客队 90 分钟全场进球 |
| `neutral` | 是否为中立场，值为 `true` 或 `false` |
| `match_type` | 模型比赛类型：`OFFICIAL`、`INTERNATIONAL_FRIENDLY` 或 `CLUB_FRIENDLY` |
| `source_competition` | 原始赛事中文名，用于历史交手弹窗展示 |

固定字段顺序：

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_score,away_score,neutral,match_type,source_competition
```

`match_id` 的来源规则：

- `FM-{id}`：FotMob 的原始比赛 ID
- `OPEN-{source}-{hash}`：OpenFootball、FootballCSV 或 `international_results` 的确定性比赛 ID
- `ESPN-{id}`：ESPN Scoreboard 的原始比赛 ID
- `ODDS-{id}`：未在 FotMob 历史中匹配到、但赔率数据中唯一且比分完整的补充场次

`competition` 除前端可选的 15 类赛事外，还允许以下内部分类：

- `INTERNATIONAL_OFFICIAL`：15 类赛事参赛国家队参加的其他正式比赛
- `INTERNATIONAL_FRIENDLY`：国家队国际窗口友谊赛
- `CLUB_OFFICIAL_OTHER`：15 类赛事参赛俱乐部参加的杯赛和其他洲际正式比赛
- `CLUB_FRIENDLY`：双方均能映射到赔率球队名的一线队友谊赛

### 2. 历史赔率数据

文件：

```text
src/main/resources/data/historical_odds_data.csv
```

该文件保存历史竞彩胜平负、让球胜平负初盘赔率及对应赛果，同时作为接口英文队名到项目中文队名的映射来源。

| 字段 | 说明 |
|---|---|
| `match_id` | 文件内唯一赔率比赛 ID |
| `match_date` | 比赛日期，格式 `yyyy-MM-dd` |
| `competition` | 赛事代码 |
| `home_team_cn` | 主队中文名，也是球队名映射结果 |
| `away_team_cn` | 客队中文名，也是球队名映射结果 |
| `home_team_en` | 主队英文源名称，可为空 |
| `away_team_en` | 客队英文源名称，可为空 |
| `home_score` | 主队 90 分钟全场进球 |
| `away_score` | 客队 90 分钟全场进球 |
| `neutral` | 是否为中立场 |
| `sporttery_match_number` | 体彩赛事编号，旧导入记录可能为空 |
| `handicap` | 主队让球数，负数表示主队让球，正数表示主队受让 |
| `normal_win` | 不让球主胜初盘赔率 |
| `normal_draw` | 不让球平局初盘赔率 |
| `normal_lose` | 不让球主负初盘赔率 |
| `handicap_win` | 让球主胜初盘赔率 |
| `handicap_draw` | 让球平局初盘赔率 |
| `handicap_lose` | 让球主负初盘赔率 |

固定字段顺序：

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_team_en,away_team_en,home_score,away_score,neutral,sporttery_match_number,handicap,normal_win,normal_draw,normal_lose,handicap_win,handicap_draw,handicap_lose
```

`match_id` 的来源规则：

- `HIS-{sourceRow}`：从原始历史赔率 CSV 导入，编号来自原文件行号
- `HIS-SPT-{sportteryMatchId}`：从中国体彩网官方接口补入

两个 CSV 的 `match_id` 不是跨文件外键。比分校正脚本按赛事、日期和球队匹配比赛，允许日期相差 1 天，并处理主客队方向相反的情况。

### 3. 球队名映射

文件：

```text
src/main/resources/data/team_name_mappings.csv
```

该文件是运行时球队名称的唯一映射基准，以体彩中文名称作为 `standard_team_name`。首版从 `historical_matches.csv` 和 `historical_odds_data.csv` 聚合生成，同一英文球队存在多个历史中文名时使用日期最新的体彩赔率中文名；历史比赛中没有体彩对应关系的球队暂时使用原中文名作为标准名。

| 字段 | 说明 |
|---|---|
| `competition` | 赛事代码，`*` 表示所有赛事可用的全局映射 |
| `standard_team_name` | 体彩标准球队名，更新、模型、回测和页面统一使用该名称 |
| `alias_team_name` | 数据源可能返回的中文简称、旧译名或英文名 |
| `alias_type` | `STANDARD`、`ZH` 或 `EN` |
| `source` | `HISTORICAL_ODDS`、`HISTORICAL_MATCHES`、`VERIFIED_ALIAS`、`VERIFIED_SPORTTERY` 或 `MANUAL` |
| `last_seen_date` | 该名称最后出现日期，人工通用别名允许为空 |

查找时先使用具体赛事映射，再使用 `competition=*` 的全局映射。名称会统一大小写、变音符号、空格和常见俱乐部前后缀后再查找；CSV 中同一作用域的同一规范化别名不允许指向两个不同标准名，服务启动时检测到冲突会直接报错。

重新根据两份历史数据生成自动映射：

```powershell
node scripts\generate-team-name-mappings.mjs
```

生成器会重建 `HISTORICAL_ODDS` 和 `HISTORICAL_MATCHES` 条目，同时保留已有 `VERIFIED_ALIAS`、`VERIFIED_SPORTTERY` 和 `MANUAL` 条目。Java 测试会逐行验证每个别名都能解析为该行的标准名称。

人工增加别名示例：

```csv
CHAMPIONS_LEAGUE,格拉茨风暴,格风暴,ZH,MANUAL,2026-07-22
```

同一别名同时适用于多个赛事时，可把 `competition` 设置为 `*`；只影响一个赛事时应填写具体赛事代码，避免同名球队跨赛事误映射。修改映射文件后需要重启服务，历史数据文件本身无需改名或重写。

## 九、数据更新与维护

### 1. 页面“更新数据”与运行时刷新

点击页面“更新数据”时，前端查询和体彩更新范围仍严格限制为 15 类赛事，同时为其参赛球队加载补充比赛：

- 近期赛程窗口为当前日期前 30 天至后 30 天
- 同时补取这些参赛球队的国家队正式赛、国际窗口友谊赛、俱乐部杯赛、洲际赛和一线队友谊赛
- 俱乐部补充比赛双方都必须能映射到 `team_name_mappings.csv`，避免青年队、预备队和未知球队混入模型
- 服务启动时直接使用本地历史和已有补充缓存，补充远程源只在手动更新时主动刷新，避免拖慢启动
- 目标日期前 30 天内缺失的体彩赔率会补查
- 目标日期前 1 天至后 4 天的体彩比赛会强制刷新
- 新接口、历史数据和体彩队名统一通过 `team_name_mappings.csv` 转换
- 实时赛程和体彩队名不一致时，会使用映射文件中的已核验别名完成关联
- 结果写入 `config/club-competition-schedules.json` 和 `config/sporttery-market-selections.json`
- 不会直接改写两个内置历史 CSV

命令行可执行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\update-history-data.ps1 `
  -Competition "CHAMPIONS_LEAGUE" `
  -Date "2026-07-18"
```

`scripts/rebuild-data.ps1` 现在也是安全的运行时刷新入口，会保留两个静态历史 CSV。

### 2. 从原始文件重建历史赔率

```powershell
powershell -ExecutionPolicy Bypass -File scripts\import-historical-odds.ps1 `
  -SourcePath "C:\path\to\his-data.csv"
```

导入规则：

- 默认从 2014-10-22 开始
- 只保留系统支持的 15 类赛事
- 丢弃无完整完场比分，或常规与让球玩法均没有一套完整初盘赔率的数据
- 保留现有文件中通过接口补充或信息更完整的记录
- 写入默认正式文件后自动执行比分校正

### 3. 补取并合并官方历史赔率

服务运行后，可先按日期范围调用官方接口：

```powershell
$startDate = "2026-04-20"
$endDate = Get-Date -Format "yyyy-MM-dd"
Invoke-RestMethod -Method Post -Uri `
  "http://127.0.0.1:8080/api/football/data/refresh-historical-odds?startDate=$startDate&endDate=$endDate"

powershell -ExecutionPolicy Bypass -File scripts\merge-sporttery-cache-odds.ps1 `
  -StartDate $startDate `
  -EndDate $endDate
```

单次接口补取范围最多 366 天。脚本只合并 15 类赛事，并使用赔率历史中更新时间最早的完整记录作为初盘；写入默认正式文件后自动执行比分校正。

### 4. 导入公共历史比赛

只检查可导入增量，不写文件：

```powershell
node scripts\import-public-history.mjs
```

确认后写入正式历史比赛文件：

```powershell
node scripts\import-public-history.mjs --write
node scripts\reconcile-historical-scores.mjs --write --compact
```

脚本会下载并缓存公共数据到 `target/public-history-cache`，处理世界杯、欧洲杯、美洲杯、欧冠和世俱杯。国家队历史名称会先映射到赔率文件中的现用中文名；赔率文件没有对应英文名的老国家队使用已核验兜底名称。俱乐部队名无法可靠映射时会跳过该场并在汇总中列出，不使用模糊匹配。重复执行不会重复新增比赛。

如需固定截止日期，可增加 `--max-date yyyy-MM-dd`。开发时也可用 `--source-root` 指向已解压的四个源仓库目录。

### 5. 导入参赛球队补充比赛

只检查增量，不写文件：

```powershell
node scripts\import-supplemental-history.mjs --compact
```

确认后写入正式历史比赛文件：

```powershell
node scripts\import-supplemental-history.mjs --write --compact
node scripts\reconcile-historical-scores.mjs --write --compact
```

脚本默认保留 2014-06-12 至今天的数据。国家队比赛来自 `international_results`，其中 `Friendly` 归为国际窗口友谊赛；俱乐部比赛来自 ESPN Scoreboard，包含系统 15 类主赛事、国内杯赛、超级杯、欧协联、南美洲俱乐部赛事及友谊赛。只有至少一方属于 15 类赛事参赛球队且双方球队名均可可靠映射的俱乐部比赛才会导入。重复执行不会重复新增比赛。

### 6. 校正比分并重建历史比赛文件

只检查、不写文件：

```powershell
node scripts\reconcile-historical-scores.mjs --compact
```

确认后写入两个正式 CSV：

```powershell
node scripts\reconcile-historical-scores.mjs --write --compact
```

校正规则：

- 以 FotMob 和公共历史源的已核验完场记录为比分基准
- 自动修正原始赔率文件对高比分比赛的压缩或错误记录
- 对无法自动关联的高比分场次使用已核验人工覆盖
- `historical_matches.csv` 保留 FotMob 与公共历史源数据，并补入赔率文件中未重复的唯一完场场次
- 重复执行不会继续产生新变更

`import-historical-odds.ps1` 和 `merge-sporttery-cache-odds.ps1` 在写入默认正式路径时已经自动调用该校正脚本，通常不需要再手工执行 `--write`。

上述历史数据脚本写入正式 CSV 后会自动重新生成 `team_name_mappings.csv`。仅修改球队别名时无需改写历史数据，直接编辑映射文件并将人工行的 `source` 设置为 `MANUAL` 即可。

### 7. 参数档案优化与核验

参数优化脚本通过正在运行的本地服务读取配置、执行回测并生成报告。先启动服务，再在项目根目录执行只生成结果、不写回配置的优化：

```powershell
node scripts\optimize-profile-parameters.mjs --apply=false
```

确认报告后写回 `config/user-config.json`：

```powershell
node scripts\optimize-profile-parameters.mjs --apply=true
```

仅核验当前已保存档案：

```powershell
node scripts\optimize-profile-parameters.mjs --verify-only=true
```

脚本仅优化稳定方案，以采样率严格大于 `80%` 为硬约束；满足约束后优先提高 ROI，ROI 相同时优先选择采样率更高的参数。激进方案不参与搜索并保持现有参数不变。尚未开赛赛事会跳过 `CURRENT`，只优化 `PREVIOUS`。优化前配置备份、优化报告和核验报告分别写入：

需要单独调优某套档案时，可通过 `--profile`、`--minimum-sampling-rate`、`--minimum-roi` 和 `--priority=roi|sampling-rate` 覆盖默认目标；例如以采样率严格大于 `66.6%` 为约束提高世界杯上届激进方案 ROI：

```powershell
node scripts\optimize-profile-parameters.mjs --profile=WORLD_CUP:PREVIOUS:AGGRESSIVE --minimum-sampling-rate=0.666 --priority=roi --apply=true
```

若需要在 ROI 不为负的前提下优先提高采样率，可增加 `--minimum-roi=0 --priority=sampling-rate`。

```text
target/user-config-before-profile-optimization.json
target/profile-optimization-report.json
target/profile-verification-report.json
```

## 十、模型说明

模型按前端选择的赛事建立参赛球队集合，并把这些球队参加的正式比赛和降权友谊赛纳入同一个时间截面。每支球队根据历史比赛计算：

- 平均进球
- 平均失球
- 进攻强度
- 防守弱点
- 样本权重

为避免小样本导致结果过激，强度会向 1.0 收缩平滑。

比赛类型权重为：

| 比赛类型 | `match_type` | 权重 |
|---|---|---:|
| 正式比赛 | `OFFICIAL` | 1.0 |
| 国家队国际窗口友谊赛 | `INTERNATIONAL_FRIENDLY` | 0.5 |
| 俱乐部正常阵容友谊赛 | `CLUB_FRIENDLY` | 0.3 |

最终样本权重等于 Dixon-Coles 时间衰减权重乘以比赛类型权重。正常预测、历史交手修正和回测都经过同一条权重计算链路，并且回测只读取预测日期之前的比赛，避免未来数据泄漏。

主队期望进球：

```text
λ_home = baselineGoals × homeAttack × awayDefenseWeakness × homeAdvantage × h2hFactor
```

客队期望进球：

```text
λ_away = baselineGoals × awayAttack × homeDefenseWeakness ÷ h2hFactor
```

泊松分布：

```text
P(X = k) = e^-λ × λ^k / k!
```

模型使用随机采样生成双方进球数，再通过蒙特卡洛统计常规胜平负、让球胜平负、总进球数和比分概率。

主队让球口径：

```text
adjustedHomeGoals = homeGoals + handicap
```

- `-1`：主队让 1 球，主队进球减 1 后比较
- `+1`：主队受让 1 球，主队进球加 1 后比较

## 十一、关键配置

主要配置位于 `src/main/resources/application.yml` 和 `config/user-config.json`：

| 配置文件或节点 | 用途 |
|---|---|
| `data-refresh` | 近期赛程刷新窗口和目标时区 |
| `football-data.historical-matches-path` | 历史比赛 CSV 路径 |
| `data/team_name_mappings.csv` | 体彩标准球队名及所有数据源别名映射 |
| `worldcup` | 世界杯数据源和模型参数 |
| `champions-league` | 欧冠 ESPN 数据源 |
| `club-competitions.schedule-update` | 其他赛事数据源、并发数和缓存路径 |
| `sporttery.result-update` | 体彩接口、刷新窗口、历史赔率路径和缓存 |
| `config/user-config.json` | 60 套赛事参数档案、回测范围和页面推荐选择 |

修改 CSV 字段或赛事代码时，需要同步检查 Java 加载器、数据维护脚本和前端赛事列表，避免运行时列错位或出现未支持赛事。
