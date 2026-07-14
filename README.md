# lottery-football

2026世界杯胜平负概率预测程序

## 一、功能说明

本项目是一个可直接落地运行的 Java + Vue2 小程序，用于根据 2026 世界杯赛程按日期查询比赛，并展示：

1. 常规胜平负概率：主胜、平局、主负
2. 主队让球胜平负概率：-3、-2、-1、+1、+2、+3
3. 双方期望进球
4. 比赛状态、比分、分组、场地

模型口径：

1. 从 `history_matches.csv` 读取历史战绩
2. 按比赛时间衰减权重和世界杯赛事权重计算球队进攻强度、失球弱点
3. 根据双方强度生成双方期望进球 λ
4. 使用泊松分布生成单场进球
5. 使用蒙特卡洛重复模拟 N 次
6. 统计胜平负与让球胜平负概率

该项目仅用于数据分析、算法学习和系统开发验证，不构成投注建议。

## 二、技术栈

后端：

- Java 17
- Spring Boot 3.3.5
- Maven
- CSV 文件数据源

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
GET /api/worldcup/health
```

### 2. 数据概览

```http
GET /api/worldcup/overview
```

返回字段：

| 字段 | 说明 |
|---|---|
| historicalMatchCount | 历史战绩样本数量 |
| scheduleMatchCount | 赛程数量 |
| baselineGoals | 基准场均进球 |
| scheduleDates | 可查询的比赛日期 |

### 3. 按日期查询概率

```http
GET /api/worldcup/predictions?date=2026-06-18&simulations=50000
```

参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| date | 是 | 比赛日期，格式 yyyy-MM-dd |
| simulations | 否 | 蒙特卡洛模拟次数，默认 50000，允许范围 1000 到 500000 |

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
