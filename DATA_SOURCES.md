# 数据源说明

当前项目内置的数据文件用于程序可直接运行和演示算法流程。

## 历史比赛数据

`src/main/resources/data/historical_matches.csv` 保存 2014-10-22 起系统 15 类赛事的已完赛历史比赛。字段为：

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_score,away_score,neutral
```

历史比分统一使用 90 分钟加伤停补时口径。主要比赛源为 FotMob 各赛事赛季接口；没有竞彩赔率的比赛也会保留，FotMob 赛季接口未覆盖且不重复的竞彩场次会作为补充。中文球队名优先复用 `historical_odds_data.csv`，赔率表未出现过的球队采用已核验兜底名称。

程序启动和点击“更新数据”时会动态加载近期赛程，因此赛程、开球时间、场地、状态等易变字段不再存入历史比赛 CSV。

## 欧洲冠军联赛

欧冠赛程和历史结果由 ESPN Scoreboard 接口动态加载，分为两个赛事代码：

- `uefa.champions_qual`：资格赛与附加赛
- `uefa.champions`：联赛阶段与淘汰赛

应用只动态加载当前日期前 30 天至后 30 天，历史样本直接使用本地 `historical_matches.csv` 中 `competition=CHAMPIONS_LEAGUE` 的记录。目标时区为 `Asia/Shanghai`，相关地址和超时时间配置位于 `src/main/resources/application.yml` 的 `champions-league.espn-update` 节点，日期窗口和计算时区配置位于 `data-refresh` 节点。

## 其他赛事

以下赛事由 ESPN Scoreboard 动态加载当前日期前 30 天至后 30 天：

- `uefa.euro`：欧洲杯
- `conmebol.america`：美洲杯
- `fifa.cwc`：世俱杯
- `uefa.europa`、`uefa.europa_qual`：欧罗巴正赛、资格赛与附加赛
- `eng.1`：英超
- `esp.1`：西甲
- `ita.1`：意甲
- `ger.1`：德甲
- `fra.1`：法甲
- `bra.1`：巴甲
- `por.1`：葡超
- `ned.1`：荷甲
- `arg.1`：阿甲

所有成功结果都会缓存到 `config/club-competition-schedules.json`；每次只替换日期窗口内的数据，窗口外历史缓存继续保留。相关配置位于 `application.yml` 的 `club-competitions.schedule-update` 和 `data-refresh` 节点。

## 全场比分口径

系统只保存 90 分钟加伤停补时的全场比分，不抓取半场比分。ESPN 赛事会根据进球明细排除加时赛和点球大战；OpenFootball 标记为加时赛的最终比分不会直接写入常规时间赛果。

## 体彩玩法开售状态与让球数

全场胜平负和全场让球胜平负自动选择及最新赔率来自中国体彩网的[足球赛果开奖页面](https://www.lottery.gov.cn/jc/zqsgkj/)和[竞彩足球计算器](https://www.sporttery.cn/jc/jsq/zqbf/)。程序调用页面实际使用的三个官方接口：

`https://webapi.sporttery.cn/gateway/uniform/football/getUniformMatchResultV1.qry`

`https://webapi.sporttery.cn/gateway/uniform/football/getMatchCalculatorV1.qry`

`https://webapi.sporttery.cn/gateway/uniform/football/getOddsHistoryV1.qry`

字段口径：

- 赛果接口的 `h`、`d`、`a` 以及在售接口的 `had` 用于判断全场胜平负
- 赛果接口的 `goalLine` 以及在售接口的 `hhad.goalLine` 表示全场让球胜平负的让球数
- 在售接口的 `had`、`hhad` 直接提供当前赔率；已完赛场次从赔率历史接口的 `hadList`、`hhadList` 选择更新时间最新的一条
- `matchId`、`matchDate`、`leagueId`、`allHomeTeam`、`allAwayTeam` 用于关联本地赛程
- `sectionsNo999` 用于已完赛场次的比分复核

体彩日期和外部赛程数据转换后的日期可能相差一天，因此常规查询从目标日期前一天查询到后两天，并同时校验赛事、主客队和比分，避免仅按队名误关联。结果写入 `config/sporttery-market-selections.json`；默认采用体彩结果，用户可逐场手工覆盖。点击“更新数据”时只处理系统下拉框中的 15 类赛事，会补查目标日期前 30 天内尚无赔率的比赛，并强制刷新目标日期前 1 天至后 4 天的全部比赛。相关配置位于 `application.yml` 的 `sporttery.result-update` 节点。

## 历史初盘赔率

`src/main/resources/data/historical_odds_data.csv` 保存属于系统已支持赛事的胜平负和让球胜平负初盘赔率。字段为：

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_team_en,away_team_en,home_score,away_score,neutral,sporttery_match_number,handicap,normal_win,normal_draw,normal_lose,handicap_win,handicap_draw,handicap_lose
```

导入脚本只保留 15 类赛事、有完场比分且至少包含一组完整初盘赔率的记录。接口英文球队名与赔率表英文名关联后，统一转换为赔率表中的最新中文名。脚本会继续执行 `reconcile-historical-scores.mjs`，用 FotMob 或已核验接口赛果纠正原始文件中合计 7 球的压缩比分、跨日日期和主客顺序，再重建去重后的历史比赛文件。

运行以下命令可以从新的原始赔率文件重新生成：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/import-historical-odds.ps1 -SourcePath "C:\path\to\his-data.csv"
```

指定日期范围的官方赔率可以先通过接口写入缓存，再用脚本合并到内置文件。历史补取接口只保留系统支持的 15 类赛事，并从 `hadList`、`hhadList` 中选择更新时间最早的有效记录作为初盘：

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/football/data/refresh-historical-odds?startDate=2026-04-21&endDate=2026-06-10"
powershell -ExecutionPolicy Bypass -File scripts/merge-sporttery-cache-odds.ps1 -StartDate "2026-04-21" -EndDate "2026-06-10"
```

补取接口单次最多查询 366 天。程序启动时会把历史赔率合并到已有赛程，同一日期、赛事和主客队的记录只保留一场；动态获取到的官方赔率优先于内置初盘赔率。回测按北京时间计算，默认使用本届世界杯开赛日（2026-06-11）至今天的已完赛比赛，起始日期由 `sporttery.result-update.backtest-start-date` 配置。

## 球队中文名

球队中文名映射入口位于 `src/main/java/com/eason/worldcup/util/ClubTeamNameTranslator.java`。程序启动时读取 `historical_odds_data.csv`，按赛事建立英文名到中文名的映射；同一球队存在历史简称和全称时，以日期最近的赔率记录为准。

点击“更新数据”后，FotMob、ESPN 和 OpenFootball 返回的英文球队名都会经过该映射。赔率表从未出现过的球队只使用代码中已核验的兜底名称，未知球队保留数据源原名，避免自动音译造成误匹配。
