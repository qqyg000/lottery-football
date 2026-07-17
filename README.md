# lottery-football

竞彩足球胜平负概率预测程序

## 一、功能说明

本项目是一个可直接落地运行的 Java + Vue2 小程序，支持按赛事和日期查询世界杯、欧冠、挪超、瑞超、芬超、欧罗巴、巴甲、美职和韩职比赛，并展示：

1. 常规胜平负概率：主胜、平局、主负
2. 主队让球胜平负概率：-3、-2、-1、+1、+2、+3
3. 前三总进球数与前三比分预测
4. 双方期望进球
5. 比赛状态、比分、分组、场地
6. 从中国体彩网自动识别全场胜平负是否开售及全场让球胜平负盘口

模型口径：

1. 世界杯从 `history_matches.csv` 读取国家队历史战绩
2. 俱乐部赛事优先读取 `club_history_matches.csv` 中的体彩历史全场比分，再合并外部赛程的近五季结果
3. 欧冠、挪超、瑞超、欧罗巴、巴甲和美职从 ESPN 读取赛程及结果
4. 芬超、韩职优先从 FotMob 读取完整赛程，TheSportsDB 作为兜底，并使用本地缓存抗网络波动
5. 所有比分采用 90 分钟加伤停补时口径，不抓取半场比分、加时赛和点球大战比分
6. 中国体彩网赛果与在售接口提供胜平负开售状态、让球数和最新赔率
7. 各俱乐部赛事独立计算球队进攻强度、失球弱点，不跨赛事混合样本
8. 根据双方强度生成双方期望进球 λ
9. 使用泊松分布生成单场进球
10. 使用蒙特卡洛统计胜平负和让球胜平负概率

该项目仅用于数据分析、算法学习和系统开发验证，不构成投注建议。

## 二、技术栈

后端：

- Java 17
- Spring Boot 3.3.5
- Maven
- CSV、ESPN、TheSportsDB、FotMob 与中国体彩网赛事数据源

前端：

- Vue 2.7.16
- Vue CLI 5
- Maven 打包时自动执行前端构建
- 前端产物复制到 Spring Boot jar 的 `static` 目录

## 三、目录结构

```text
lottery-football
├─ pom.xml
├─ build.cmd
├─ 启动程序.cmd
├─ README.md
├─ frontend
│  ├─ package.json
│  ├─ vue.config.js
│  ├─ public
│  │  └─ index.html
│  └─ src
│     ├─ App.vue
│     └─ main.js
└─ src
   └─ main
      ├─ java
      │  └─ com/eason/worldcup
      │     ├─ LotteryFootballApplication.java
      │     ├─ controller
      │     │  └─ PredictionController.java
      │     ├─ model
      │     ├─ service
      │     └─ util
      └─ resources
         ├─ application.yml
         └─ data
            ├─ history_matches.csv
            ├─ club_history_matches.csv
            └─ schedule_2026.csv
```

## 四、Windows 运行方式

### 1. 前置环境

需要安装：

- JDK 17
- Maven 3.8+

Node 不需要手动安装，Maven 打包时 `frontend-maven-plugin` 会自动下载指定版本 Node 和 npm。

### 2. 打包

双击：

```text
build.cmd
```

打包完成后会生成：

```text
target/dist/lottery-football-1.0.0.jar
target/dist/启动程序.cmd
```

### 3. 启动

双击：

```text
target/dist/启动程序.cmd
```

启动后浏览器会打开：

```text
http://127.0.0.1:8080
```

## 五、后端接口

### 1. 健康检查

```http
GET /api/football/health
```

### 2. 数据概览

```http
GET /api/football/overview?competition=CHAMPIONS_LEAGUE
```

返回字段：

| 字段 | 说明 |
|---|---|
| competition | 赛事代码，见下方赛事代码表 |
| competitionName | 赛事中文名 |
| historicalMatchCount | 历史战绩样本数量 |
| scheduleMatchCount | 赛程数量 |
| completedMatchCount | 已完赛数量 |
| baselineGoals | 基准场均进球 |
| scheduleDates | 可查询的比赛日期 |

### 3. 按日期查询概率

```http
GET /api/football/predictions?competition=CHAMPIONS_LEAGUE&date=2026-07-14&simulations=50000
```

参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| competition | 否 | 赛事代码，默认 WORLD_CUP |
| date | 是 | 比赛日期，格式 yyyy-MM-dd |
| simulations | 否 | 蒙特卡洛模拟次数，默认 50000，允许范围 1000 到 500000 |

旧版 `/api/worldcup` 路径继续保留，未传 `competition` 时默认查询世界杯。

每场比赛额外返回体彩玩法字段：

| 字段 | 说明 |
|---|---|
| sportteryMatchId | 中国体彩网竞彩足球比赛 ID，未匹配时为空 |
| sportteryMatchNumber | 体彩赛事编号，例如周一201 |
| sportteryNormalAvailable | 是否开售全场胜平负 |
| sportteryHandicap | 全场让球胜平负让球数，例如 -1、+1 |
| sportteryNormalOdds | 最新不让球胜平负赔率，包含 win、draw、lose、updatedAt |
| sportteryHandicapOdds | 最新让球胜平负赔率，包含 win、draw、lose、updatedAt |

赛事代码：

| 赛事 | 代码 |
|---|---|
| 世界杯 | WORLD_CUP |
| 欧冠 | CHAMPIONS_LEAGUE |
| 挪超 | NORWEGIAN_ELITESERIEN |
| 瑞超 | SWEDISH_ALLSVENSKAN |
| 芬超 | FINNISH_VEIKKAUSLIIGA |
| 欧罗巴 | EUROPA_LEAGUE |
| 巴甲 | BRAZIL_SERIE_A |
| 美职 | MLS |
| 韩职 | K_LEAGUE_1 |

## 六、数据文件说明

### 1. 2026 世界杯赛程

文件：

```text
src/main/resources/data/schedule_2026.csv
```

字段：

| 字段 | 说明 |
|---|---|
| match_id | 比赛 ID |
| match_date | 比赛日期 |
| kickoff_time | 开球时间 |
| group_name | 小组 |
| home_team_cn | 主队中文名 |
| away_team_cn | 客队中文名 |
| home_team_en | 主队英文名，用于模型匹配 |
| away_team_en | 客队英文名，用于模型匹配 |
| venue | 场地 |
| neutral | 是否中立场 |
| status | 状态：SCHEDULED、LIVE、COMPLETED |
| home_score | 主队实际进球，未开赛留空 |
| away_score | 客队实际进球，未开赛留空 |

内置 CSV 的日期和时间按 `worldcup.schedule-source-zone` 解释，加载时统一转换为 `data-refresh.target-zone`。OpenFootball、ESPN、欧冠、其他俱乐部赛事和体彩刷新也共用该目标时区，当前统一为 `Asia/Shanghai`（UTC+8）；接口查询日期、返回日期和开球时间均使用转换后的北京时间。

### 2. 历史战绩

文件：

```text
src/main/resources/data/history_matches.csv
```

字段：

| 字段 | 说明 |
|---|---|
| match_date | 比赛日期 |
| tournament | 赛事名称 |
| home_team | 主队英文名 |
| away_team | 客队英文名 |
| home_score | 主队进球 |
| away_score | 客队进球 |
| neutral | 是否中立场 |

英文队名必须和 `schedule_2026.csv` 中的 `home_team_en`、`away_team_en` 保持一致，否则模型会使用默认强度。

俱乐部历史战绩使用独立文件：

```text
src/main/resources/data/club_history_matches.csv
```

该文件包含欧冠、挪超、瑞超、芬超、欧罗巴、巴甲、美职和韩职的历史全场比分。球队统一使用项目中文名，避免与国家队英文历史样本混合；同日同队同时出现在动态赛程时，以动态赛程数据覆盖本地历史记录。

### 3. 欧冠赛程与历史结果

欧冠近期赛程和结果在启动及手动更新数据时会分别调用：

- `uefa.champions_qual`：欧冠资格赛与附加赛
- `uefa.champions`：欧冠联赛阶段与淘汰赛

启动及手动更新时只查询当前日期前 30 天至后 30 天，历史样本直接使用 `club_history_matches.csv`，不再重复抓取多个赛季。比赛时间统一转换为 `Asia/Shanghai`，数据源不可用时不会影响世界杯内置赛程启动。

### 4. 其他俱乐部赛事

挪超、瑞超、欧罗巴、巴甲和美职由 ESPN Scoreboard 动态加载当前日期前 30 天至后 30 天。芬超和韩职在同一日期窗口内优先使用 FotMob，TheSportsDB 作为兜底数据源。

所有新增赛事按赛事代码独立建模，时间统一转换为 `Asia/Shanghai`。成功获取的赛程会缓存到 `config/club-competition-schedules.json`；更新时只替换日期窗口内的数据，窗口外历史记录继续保留，外部数据源暂时不可用时继续使用已有缓存。日期窗口由 `application.yml` 的 `data-refresh` 节点统一配置，并按 `Asia/Shanghai` 计算。

### 5. 体彩玩法自动选择

查询某个比赛日时，后端会合并中国体彩网的足球赛果接口和计算器在售接口。赛果窗口从目标日期前一天查到后两天，在售接口补充当前及未来两天的玩法状态和让球数，并按赛事、主客队、全场比分和日期匹配本地赛程：

- `h`、`d`、`a` 三个固定奖字段都有值时，自动勾选全场胜平负
- `goalLine` 有值时，自动勾选对应的全场让球胜平负行
- 概率表在概率与推荐标记之间显示体彩最新赔率，历史场次从官方赔率历史接口取时间最新记录
- 默认使用体彩勾选结果，用户修改任意勾选框后，该场改为手工覆盖并保存
- 没有体彩数据的场次仍可手工勾选，兼容非竞彩场次和接口异常
- 查询结果缓存到 `config/sporttery-market-selections.json`，近期日期默认每 30 分钟更新
- 点击“更新数据”会补查所选日期前 30 天内尚无赔率的比赛，并强制刷新所选日期前 1 天至后 4 天的全部比赛，不受已有赔率和缓存间隔影响
- `historical_odds.csv` 内置从 2022-11-20 起的历史初盘赔率，回测默认使用本届世界杯开赛日（2026-06-11）至北京时间今天的已完赛比赛

历史赔率通过以下命令从原始文件重新生成，俱乐部名称复用 `club_history_matches.csv` 已核验的球队映射，世界杯球队使用固定中英文映射：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/import-historical-odds.ps1 -SourcePath "C:\path\to\his-data.csv"
```

也可以先调用 `/api/football/data/refresh-historical-odds` 按日期范围补取官方赔率，再将缓存中成功匹配的比赛合并到内置文件：

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/football/data/refresh-historical-odds?startDate=2026-04-21&endDate=2026-06-10"
powershell -ExecutionPolicy Bypass -File scripts/merge-sporttery-cache-odds.ps1 -StartDate "2026-04-21" -EndDate "2026-06-10"
```

单次补取范围最多 366 天。相关开关、接口地址、历史赔率路径、回测年数和缓存位置位于 `application.yml` 的 `sporttery.result-update` 节点。

## 七、模型说明

### 1. 球队强度

每支球队计算：

- 平均进球
- 平均失球
- 进攻强度
- 防守弱点
- 样本权重

为了避免样本过少导致结果过激，代码中对强度做了向 1.0 收缩的平滑处理。

### 2. 期望进球

主队期望进球：

```text
λ_home = baselineGoals × homeAttack × awayDefenseWeakness × homeAdvantage × h2hFactor
```

客队期望进球：

```text
λ_away = baselineGoals × awayAttack × homeDefenseWeakness ÷ h2hFactor
```

### 3. 泊松分布

根据 λ 生成进球数：

```text
P(X = k) = e^-λ × λ^k / k!
```

代码中通过随机采样实现泊松分布进球生成。

### 4. 让球口径

主队让球按以下方式计算：

```text
adjustedHomeGoals = homeGoals + handicap
```

示例：

- `-1`：主队让 1 球，即主队进球 - 1 后再比较
- `+1`：主队受让 1 球，即主队进球 + 1 后再比较
