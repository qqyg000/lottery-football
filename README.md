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
- 点击球队名称查看双方最多 50 场正式比赛和降权友谊赛
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

| 文件 | 行数 | 日期范围 | 赛事分类数 | 列数 |
|---|---:|---|---:|---:|
| `historical_matches.csv` | 56,809 | 2014-06-12 至 2026-07-18 | 19 | 10 |
| `historical_odds_data.csv` | 30,942 | 2014-10-22 至 2026-07-17 | 15 | 18 |

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
│  └─ src
│     ├─ App.vue
│     └─ main.js
├─ scripts
│  ├─ import-historical-odds.ps1
│  ├─ import-public-history.mjs
│  ├─ import-supplemental-history.mjs
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

### 3. 推荐回测

推荐回测按赛事使用独立的起始日期，结束日期统一为 `Asia/Shanghai` 时区的当天，起始日和结束日都包含在回测范围内。

| 赛事 | 赛事代码 | 回测起始日期 |
|---|---|---|
| 世界杯 | `WORLD_CUP` | `2026-06-11` |
| 欧洲杯 | `EUROPEAN_CHAMPIONSHIP` | `2028-06-09` |
| 美洲杯 | `COPA_AMERICA` | `2028-06-09` |
| 世俱杯 | `CLUB_WORLD_CUP` | `2028-06-09` |
| 欧罗巴 | `EUROPA_LEAGUE` | `2026-09-16` |
| 欧冠 | `CHAMPIONS_LEAGUE` | `2026-09-08` |
| 英超 | `PREMIER_LEAGUE` | `2026-08-21` |
| 西甲 | `LA_LIGA` | `2026-08-15` |
| 意甲 | `SERIE_A` | `2026-08-21` |
| 德甲 | `BUNDESLIGA` | `2026-08-28` |
| 法甲 | `LIGUE_1` | `2026-08-21` |
| 巴甲 | `BRAZIL_SERIE_A` | `2026-01-28` |
| 葡超 | `PRIMEIRA_LIGA` | `2026-08-08` |
| 荷甲 | `EREDIVISIE` | `2026-08-07` |
| 阿甲 | `ARGENTINE_PRIMERA_DIVISION` | `2026-01-22` |

选择“全部”时，每场比赛按自身赛事的起始日期过滤，不会使用全局统一起始日期。起始日期晚于当天的未开始赛事不参与回测。只有已完赛且有完整比分、竞彩比赛 ID 和至少一类胜平负赔率的场次才进入最终回测。每场回测仅使用该场比赛日期之前的历史比赛建模。

### 4. 刷新运行时数据

```http
POST /api/football/data/refresh?competition=CHAMPIONS_LEAGUE&date=2026-07-18
```

刷新会重新加载系统配置的 15 类赛事近期赛程，并为这些赛事的参赛球队补充近期国家队正式赛、国际窗口友谊赛、俱乐部杯赛、洲际赛和一线队友谊赛，然后重建模型并强制刷新目标日期附近的体彩数据。`competition` 只决定接口最终返回哪一类赛事的概览；前端下拉仍只显示 15 类赛事和“全部”。

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

### 3. 球队中文名映射

- `historical_odds_data.csv` 中的中文球队名是映射基准
- 同一英文球队存在多个历史中文名时，优先使用日期最新的赔率记录
- 映射同时按赛事和全局别名匹配，避免同名球队跨赛事误映射
- 近期赛程接口返回英文名后，先映射为赔率数据中的中文名再参与合并和去重
- 无法可靠映射的球队使用已核验兜底名称或数据源名称，不进行猜测式覆盖

## 九、数据更新与维护

### 1. 页面“更新数据”与运行时刷新

点击页面“更新数据”时，前端查询和体彩更新范围仍严格限制为 15 类赛事，同时为其参赛球队加载补充比赛：

- 近期赛程窗口为当前日期前 30 天至后 30 天
- 同时补取这些参赛球队的国家队正式赛、国际窗口友谊赛、俱乐部杯赛、洲际赛和一线队友谊赛
- 俱乐部补充比赛双方都必须能映射到 `historical_odds_data.csv`，避免青年队、预备队和未知球队混入模型
- 服务启动时直接使用本地历史和已有补充缓存，补充远程源只在手动更新时主动刷新，避免拖慢启动
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
