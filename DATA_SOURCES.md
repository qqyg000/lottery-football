# 数据源说明

当前项目内置的数据文件用于程序可直接运行和演示算法流程。

## 历史比赛数据

`src/main/resources/data/historical_matches.csv` 保存 125 种来源赛事及其全部参赛球队的比赛，当前包含去重后的 115,260 场，日期范围为 2014-10-22 至 2026-07-22；导入流程以 2014-10-22 作为历史数据最早截点。15 类前端可查询赛事保留独立内部代码，其余比赛按国家队正式赛、国家队友谊赛、俱乐部正式赛和俱乐部友谊赛归类，原始赛事名保存在 `source_competition`。字段为：

```text
match_id,match_date,competition,home_team_cn,away_team_cn,home_score,away_score,neutral,match_type,source_competition
```

历史比分统一使用 90 分钟加伤停补时口径。数据源包括：

- [OpenFootball worldcup.json](https://github.com/openfootball/worldcup.json)：1930 年起世界杯决赛圈，直接使用 `score.ft`
- [Mart Jürisoo international_results](https://github.com/martj42/international_results)：成年男子国家队正式比赛和国际友谊赛，结合进球分钟剔除加时赛进球
- [FootballCSV europe-champions-league](https://github.com/footballcsv/europe-champions-league)：1955-56 至 2015-16 赛季欧冠，使用 `FT` 常规时间比分
- [OpenFootball club-world-cup](https://github.com/openfootball/club-world-cup)：2000 年起世俱杯，点球和加时场次读取括号中的 90 分钟比分
- FotMob 各赛事赛季接口、阿塞拜疆超级联赛、芬兰超级联赛，以及上述来源未覆盖且不重复的竞彩场次
- ESPN Scoreboard：国内杯赛、超级杯、洲际俱乐部赛事和俱乐部友谊赛
- Futbol24 按日及赛季比赛接口：补充 ESPN 未收录的俱乐部友谊赛、阿塞杯、芬超、芬兰杯、丹超、丹麦杯、波超杯、波甲、奥甲、苏超、土超、土耳其杯、匈甲、匈牙利杯、克甲、塞浦甲和哈萨超
- 阿塞拜疆职业足球联盟（PFL）官方接口：补齐 Futbol24 未收录的阿塞杯第一资格轮和阿塞超升降级附加赛
- Sofascore Club Friendly Games：Futbol24 不可用时的俱乐部友谊赛降级来源

上述四个公共仓库均采用 CC0 公共领域许可。中文球队名优先复用 `historical_odds_data.csv`；赔率表未出现过的国家队采用已核验兜底名称，无法可靠映射的俱乐部比赛不导入。

公共历史源通过 `scripts/import-public-history.mjs` 导入。脚本默认只检查增量，传入 `--write` 才会写文件；下载缓存位于 `target/public-history-cache`。公共源比赛使用 `OPEN-{source}-{hash}` ID，比分重建脚本会保留这些记录。

参赛球队的扩展历史通过 `scripts/import-supplemental-history.mjs` 导入，下载缓存位于 `target/supplemental-history-cache`。脚本强制裁剪 2014-10-22 之前的记录，并按比赛 ID、同日对阵、相邻日期同比分及“正式比赛中同队同日同得失球”四层去重；最后一种重复会合并记录，并把确认出的对手别名写入 `team_name_mappings.csv`，俱乐部友谊赛不使用该推断规则。ESPN、FotMob、Futbol24、Sofascore、PFL 比赛分别使用 `ESPN-{id}`、`FOTMOB-{id}`、`FUTBOL24-{id}`、`SOFASCORE-{id}`、`PFL-{id}`，国家队公共源比赛使用确定性 `OPEN-{source}-{hash}`。可用 `--only-sources` 逗号分隔指定本次导入源。

历史比赛使用以下内部类型和回测权重：

- `OFFICIAL`：正式比赛，权重 1.0
- `INTERNATIONAL_FRIENDLY`：国家队国际窗口友谊赛，权重 0.5
- `CLUB_FRIENDLY`：俱乐部正常阵容友谊赛，权重 0.3

类型权重会与 Dixon-Coles 时间衰减权重相乘。`source_competition` 保存原始赛事名，用于历史交手弹窗；内部 `competition` 分类不会加入前端赛事下拉框。

程序启动和点击“更新数据”时会动态加载近期赛程，因此赛程、开球时间、场地、状态等易变字段不再存入历史比赛 CSV。

## 欧洲冠军联赛

欧冠赛程和历史结果由 ESPN Scoreboard 接口动态加载，分为两个赛事代码：

- `uefa.champions_qual`：资格赛与附加赛
- `uefa.champions`：联赛阶段与淘汰赛

应用只动态加载当前日期前 30 天至后 30 天，历史样本直接使用本地 `historical_matches.csv` 中 `competition=CHAMPIONS_LEAGUE` 的记录，其中可映射球队的样本已扩展到 1955-56 赛季。目标时区为 `Asia/Shanghai`，相关地址和超时时间配置位于 `src/main/resources/application.yml` 的 `champions-league.espn-update` 节点，日期窗口和计算时区配置位于 `data-refresh` 节点。

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

为补足上述 15 类赛事参赛球队的近期样本，同一刷新流程还会加载：

- `fifa.friendly`：国家队国际友谊赛
- 世界杯各大洲预选赛、欧预赛、欧国联、金杯赛、非洲杯和亚洲杯：国家队其他正式比赛
- 国内杯赛、超级杯、欧协联、解放者杯、南美杯等：俱乐部其他正式比赛
- `club.friendly`、国际冠军杯、酋长杯、英超亚洲杯和甘伯杯：俱乐部正常阵容友谊赛
- FotMob `leagueId=262`：阿塞超正式比赛
- FotMob `leagueId=51`：芬超完整赛季历史
- Futbol24 `league_id=472`：ESPN 未覆盖的俱乐部友谊赛
- Futbol24 `league_id=525`：阿塞杯历史和近期比赛
- Futbol24 `league_id=322`：芬超完整赛季历史和近期比赛
- Futbol24 `league_id=324`：芬兰杯完整赛季历史和近期比赛
- Futbol24 `league_id=28`：丹超完整赛季历史和近期比赛
- Futbol24 `league_id=297`：波超杯完整赛季历史和近期比赛
- Futbol24 `league_id=107`：波甲完整赛季历史和近期比赛
- Futbol24 `league_id=15`：奥甲完整赛季历史和近期比赛
- Futbol24 `league_id=51`：苏超完整赛季历史和近期比赛
- Futbol24 `league_id=133`：土超完整赛季历史和近期比赛
- Futbol24 `league_id=537`：土耳其杯完整赛季历史和近期比赛
- Futbol24 `league_id=33`：丹麦杯完整赛季历史和近期比赛
- Futbol24 `league_id=92`：匈甲完整赛季历史和近期比赛
- Futbol24 `league_id=531`：匈牙利杯完整赛季历史和近期比赛
- Futbol24 `league_id=26`：克甲完整赛季历史和近期比赛
- Futbol24 `league_id=75`：塞浦甲完整赛季历史和近期比赛
- Futbol24 `league_id=269`：哈萨超完整赛季历史和近期比赛
- PFL 官方 `games/show/{id}`：阿塞杯第一资格轮和阿塞超升降级附加赛的核验赛果
- Sofascore `tournamentId=853`：Futbol24 不可用时的俱乐部友谊赛降级来源

阿塞超、阿塞杯、芬超、芬兰杯、丹超、丹麦杯、波超杯、波甲、奥甲、苏超、土超、土耳其杯、匈甲、匈牙利杯、克甲、塞浦甲和哈萨超等可信固定联赛按整项赛事进入运行时缓存，即使新升班马暂时没有映射也不会被丢弃；开放式俱乐部友谊赛仍要求至少一方能映射到系统球队。已映射球队使用统一名称，未被体彩收录的低级别或预备队对手保留数据源官方名称并写入自映射。国家队和俱乐部补充源使用内部赛事代码，不会改变前端 15 类赛事加“全部”的下拉选项。

所有成功结果都会缓存到 `config/club-competition-schedules.json`；点击“更新数据”时，常规赛程和俱乐部杯赛等补充赛事统一按 `data-refresh.days-back` 回溯，默认只补取过去 30 天，窗口外历史缓存继续保留。2014-10-22 起的长期历史由 `historical_matches.csv` 提供。服务启动时读取已有补充缓存，只有点击“更新数据”或调用刷新接口时才主动请求补充远程源。相关配置位于 `application.yml` 的 `club-competitions.schedule-update` 和 `data-refresh` 节点。

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

体彩日期和外部赛程数据转换后的日期可能相差一天，因此常规查询允许一天日期偏差，并同时校验赛事、主客队和比分，避免仅按队名误关联。结果写入 `config/sporttery-market-selections.json`；默认采用体彩结果，用户可逐场手工覆盖。点击“更新数据”时，会以北京时间当天为基准强制读取最近 30 天体彩赛果，再按“赛事、日期、映射后的主客队”与 ESPN、FotMob 等赛程合并；双方球队无法全部匹配时，仅允许同日、同比分且至少一方球队匹配的记录合并。体彩未销售的俱乐部友谊赛继续由 Futbol24 等补充源提供。赔率仍按缺失状态和近期窗口增量刷新，避免对最近 30 天每场比赛重复请求赔率历史。相关配置位于 `application.yml` 的 `sporttery.result-update` 节点，其中 `result-lookback-days` 默认为 30。

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

球队中文名映射入口位于 `src/main/java/com/eason/worldcup/util/ClubTeamNameTranslator.java`。程序启动时读取 `team_name_mappings.csv`，按来源优先级建立英文名、历史简称和体彩中文名之间的映射；近 10 场和历史交锋展示优先使用体彩接口标准名。

点击“更新数据”后，FotMob、ESPN 和 OpenFootball 返回的英文球队名都会经过该映射。赔率表从未出现过的球队只使用代码中已核验的兜底名称，未知球队保留数据源原名，避免自动音译造成误匹配。
