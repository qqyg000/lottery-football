# lottery-football

竞彩足球胜平负概率预测程序。后端使用 Spring Boot，前端使用 Vue 2，支持按赛事和日期查询赛程、赛果、体彩赔率及模型预测结果。

> 当前内置数据快照更新于 2026-07-18。项目仅用于数据分析、算法学习和系统开发验证，不构成投注建议。

## 一、主要功能

- 查询 15 类赛事的历史比赛、近期赛程和完场结果
- 同时选择一个、多个或“全部”赛事进行查询和回测
- 计算常规胜平负概率：主胜、平局、主负
- 计算主队让球胜平负概率：`-3`、`-2`、`-1`、`+1`、`+2`、`+3`
- 展示双方期望进球、前三总进球数和前三比分预测
- 展示比赛状态、比分、分组、开球时间和场地
- 从中国体彩网读取胜平负开售状态、让球数和最新赔率
- 使用历史赔率数据统一映射接口返回的英文球队名
- 提供推荐结果回测、模型参数调整和用户配置持久化

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

前端下拉框保留“全部”选项。“全部”表示选中并汇总上述 15 类赛事，不是独立的第 16 类赛事。除回测接口外，直接调用后端接口时应传具体赛事代码。

## 三、当前数据快照

以下行数不含 CSV 表头：

| 文件 | 行数 | 日期范围 | 赛事数 | 列数 |
|---|---:|---|---:|---:|
| `historical_matches.csv` | 42,914 | 2014-10-22 至 2026-07-18 | 15 | 8 |
| `historical_odds_data.csv` | 30,942 | 2014-10-22 至 2026-07-17 | 15 | 18 |

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

- 历史完场比分：FotMob 已核验赛事赛季数据，以及未被其覆盖的唯一竞彩场次
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
│  └─ src
│     ├─ App.vue
│     └─ main.js
├─ scripts
│  ├─ import-historical-odds.ps1
│  ├─ merge-sporttery-cache-odds.ps1
│  ├─ reconcile-historical-scores.mjs
│  ├─ update-history-data.ps1
│  ├─ rebuild-data.ps1
│  ├─ backtest-recommendations.mjs
│  └─ optimize-backtest-parameters.mjs
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
            └─ historical_odds_data.csv
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
mvn -DskipTests clean package
```

`build.cmd` 会先停止当前项目目录下正在运行的旧服务，再构建前后端并生成：

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

## 七、后端接口

所有主要接口同时兼容 `/api/football` 和旧版 `/api/worldcup` 前缀。查询、概览和刷新接口未传 `competition` 时默认使用 `WORLD_CUP`，回测接口未传时默认使用全部 15 类赛事。

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/football/health` | 健康检查 |
| GET | `/api/football/overview` | 获取指定赛事的数据概览和可查询日期 |
| GET | `/api/football/predictions` | 查询指定赛事、日期的概率预测 |
| GET | `/api/football/head-to-head` | 查询某场比赛双方的历史交锋 |
| POST | `/api/football/data/refresh` | 刷新近期赛程、模型和体彩数据 |
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

### 3. 刷新运行时数据

```http
POST /api/football/data/refresh?competition=CHAMPIONS_LEAGUE&date=2026-07-18
```

刷新会重新加载系统配置的 15 类赛事近期赛程、重建模型，并强制刷新目标日期附近的体彩数据。`competition` 决定接口最终返回哪一类赛事的概览，但不会刷新这 15 类之外的赛事。

## 八、CSV 数据说明

### 1. 历史比赛数据

文件：

```text
src/main/resources/data/historical_matches.csv
```

该文件合并国家队历史比赛、2026 世界杯赛程中的已完赛结果和俱乐部赛事历史比分，只保存模型需要的 8 列：

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

固定字段顺序：

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_score,away_score,neutral
```

`match_id` 的来源规则：

- `FM-{id}`：FotMob 的原始比赛 ID
- `ODDS-{id}`：未在 FotMob 历史中匹配到、但赔率数据中唯一且比分完整的补充场次

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

### 3. 球队中文名映射

- `historical_odds_data.csv` 中的中文球队名是映射基准
- 同一英文球队存在多个历史中文名时，优先使用日期最新的赔率记录
- 映射同时按赛事和全局别名匹配，避免同名球队跨赛事误映射
- 近期赛程接口返回英文名后，先映射为赔率数据中的中文名再参与合并和去重
- 无法可靠映射的球队使用已核验兜底名称或数据源名称，不进行猜测式覆盖

## 九、数据更新与维护

### 1. 页面“更新数据”与运行时刷新

点击页面“更新数据”只会使用本项目配置的 15 类赛事数据源：

- 近期赛程窗口为当前日期前 30 天至后 30 天
- 目标日期前 30 天内缺失的体彩赔率会补查
- 目标日期前 1 天至后 4 天的体彩比赛会强制刷新
- 新接口球队名会按 `historical_odds_data.csv` 的最新中文名映射
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

### 4. 校正比分并重建历史比赛文件

只检查、不写文件：

```powershell
node scripts\reconcile-historical-scores.mjs --compact
```

确认后写入两个正式 CSV：

```powershell
node scripts\reconcile-historical-scores.mjs --write --compact
```

校正规则：

- 以已核验的 FotMob 完场记录为比分基准
- 自动修正原始赔率文件对高比分比赛的压缩或错误记录
- 对无法自动关联的高比分场次使用已核验人工覆盖
- `historical_matches.csv` 保留 FotMob 历史，并补入赔率文件中未重复的唯一完场场次
- 重复执行不会继续产生新变更

`import-historical-odds.ps1` 和 `merge-sporttery-cache-odds.ps1` 在写入默认正式路径时已经自动调用该校正脚本，通常不需要再手工执行 `--write`。

## 十、模型说明

每类赛事独立计算球队强度，不跨赛事混合样本。每支球队根据历史比赛计算：

- 平均进球
- 平均失球
- 进攻强度
- 防守弱点
- 样本权重

为避免小样本导致结果过激，强度会向 1.0 收缩平滑。

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

主要配置位于 `src/main/resources/application.yml`：

| 配置节点 | 用途 |
|---|---|
| `data-refresh` | 近期赛程刷新窗口和目标时区 |
| `football-data.historical-matches-path` | 历史比赛 CSV 路径 |
| `worldcup` | 世界杯数据源和模型参数 |
| `champions-league` | 欧冠 ESPN 数据源 |
| `club-competitions.schedule-update` | 其他赛事数据源、并发数和缓存路径 |
| `sporttery.result-update` | 体彩接口、刷新窗口、历史赔率路径和缓存 |

修改 CSV 字段或赛事代码时，需要同步检查 Java 加载器、数据维护脚本和前端赛事列表，避免运行时列错位或出现未支持赛事。
