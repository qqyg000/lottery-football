# lottery-football

竞彩足球胜平负概率预测程序

## 一、功能说明

本项目是一个可直接落地运行的 Java + Vue2 小程序，支持按赛事和日期查询世界杯、欧冠、挪超、瑞超、芬超、欧罗巴、巴甲、美职和韩职比赛，并展示：

1. 常规胜平负概率：主胜、平局、主负
2. 主队让球胜平负概率：-3、-2、-1、+1、+2、+3
3. 前三半全场、前三总进球数与前三比分预测
4. 双方期望进球
5. 比赛状态、比分、分组、场地
6. 从中国体彩网自动识别全场胜平负是否开售及全场让球胜平负盘口

模型口径：

1. 世界杯从 `history_matches.csv` 读取国家队历史战绩
2. 欧冠、挪超、瑞超、欧罗巴、巴甲和美职从 ESPN 读取赛程及近三季结果
3. 芬超从 TheSportsDB 读取近三季样本和轮次，韩职从 FotMob 读取近三季完整赛程，并使用本地缓存抗网络波动
4. ESPN 赛程直接解析进球事件生成半场比分，缺少半场数据时使用 FotMob 比赛详情补齐并写入本地缓存
5. 中国体彩网赛果与在售接口提供胜平负开售状态和让球数，前端据此自动勾选对应概率行
6. 各俱乐部赛事独立计算球队进攻强度、失球弱点，不跨赛事混合样本
7. 根据双方强度生成双方期望进球 λ
8. 使用泊松分布生成单场进球
9. 使用蒙特卡洛重复模拟 N 次
10. 统计胜平负、让球胜平负及半全场概率

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

### 3. 欧冠赛程与历史结果

欧冠不依赖本地 CSV，启动及手动更新数据时会分别调用：

- `uefa.champions_qual`：欧冠资格赛与附加赛
- `uefa.champions`：欧冠联赛阶段与淘汰赛

默认读取当前赛季及此前两个赛季，比赛时间统一转换为 `Asia/Shanghai`。数据源不可用时不会影响世界杯内置赛程启动。

### 4. 其他俱乐部赛事

挪超、瑞超、欧罗巴、巴甲和美职由 ESPN Scoreboard 动态加载。芬超使用 TheSportsDB 免费接口；韩职使用 FotMob 联赛 `9080` 的完整赛季接口加载当前赛季及此前两个赛季，TheSportsDB 作为韩职兜底数据源。

所有新增赛事按赛事代码独立建模，时间统一转换为 `Asia/Shanghai`。成功获取的赛程会缓存到 `config/club-competition-schedules.json`，外部数据源暂时不可用时继续使用已有缓存。

### 5. 体彩玩法自动选择

查询某个比赛日时，后端会合并中国体彩网的足球赛果接口和计算器在售接口。赛果窗口从目标日期前一天查到后两天，在售接口补充当前及未来两天的玩法状态和让球数，并按赛事、主客队、全场比分和日期匹配本地赛程：

- `h`、`d`、`a` 三个固定奖字段都有值时，自动勾选全场胜平负
- `goalLine` 有值时，自动勾选对应的全场让球胜平负行
- 概率表在概率与推荐标记之间显示体彩最新赔率，历史场次从官方赔率历史接口取时间最新记录
- 默认使用体彩勾选结果，用户修改任意勾选框后，该场改为手工覆盖并保存
- 没有体彩数据的场次仍可手工勾选，兼容非竞彩场次和接口异常
- 查询结果缓存到 `config/sporttery-market-selections.json`，近期日期默认每 30 分钟更新
- 点击“更新数据”会忽略刷新间隔，重新获取所选日期至后两天的体彩数据

相关开关、接口地址、超时、刷新间隔和缓存位置位于 `application.yml` 的 `sporttery.result-update` 节点。

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

## 八、主要代码文件说明

### 1. `PredictionController`

位置：

```text
src/main/java/com/eason/worldcup/controller/PredictionController.java
```

作用：提供前端调用接口。

### 2. `DataRepository`

位置：

```text
src/main/java/com/eason/worldcup/service/DataRepository.java
```

作用：读取历史战绩 CSV 与 2026 世界杯赛程 CSV。

### 3. `TeamStrengthService`

位置：

```text
src/main/java/com/eason/worldcup/service/TeamStrengthService.java
```

作用：根据历史战绩计算球队强度与双方期望进球。

### 4. `PredictionService`

位置：

```text
src/main/java/com/eason/worldcup/service/PredictionService.java
```

作用：执行泊松分布进球采样、蒙特卡洛模拟、胜平负概率统计。

### 5. `PoissonRandom`

位置：

```text
src/main/java/com/eason/worldcup/util/PoissonRandom.java
```

作用：根据 λ 生成泊松分布随机进球数。

## 九、后续可扩展方向

1. 接入完整国际比赛历史数据集
2. 引入 FIFA 排名、Elo 积分、球员伤停、赔率盘口等特征
3. 增加缓存，避免相同日期重复模拟
4. 增加 H2 数据库存储赛程和历史数据
5. 增加一键刷新官方赛程的定时任务
6. 增加小组出线概率、淘汰赛晋级概率模拟


## Windows CMD 乱码问题说明

如果运行旧版 `build.cmd` 出现类似 `'ode銆乶pm' 不是内部或外部命令` 的错误，原因是 `.cmd` 文件中存在中文提示文本，Windows CMD 按本机代码页解析 UTF-8 文件后会把中文拆成乱码命令。当前版本已经把 `build.cmd`、`run.cmd` 改成纯 ASCII 内容，避免 CMD 编码问题。

构建方式：

```bat
build.cmd
```

启动方式：

```bat
target\dist\run.cmd
```
