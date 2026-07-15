# 数据源说明

当前项目内置的数据文件用于程序可直接运行和演示算法流程。

## 2026 世界杯赛程

`src/main/resources/data/schedule_2026.csv` 使用 2026 世界杯小组赛赛程作为种子数据。

建议生产使用时以 FIFA 官方赛程页面为准，并定期更新本地 CSV：

- FIFA World Cup 2026 fixtures and results
- ESPN FIFA World Cup schedule

## 历史战绩

`src/main/resources/data/history_matches.csv` 内置部分世界杯历史比赛和本届已完赛比分，用于演示建模流程。

建议生产使用时替换为完整国际比赛数据集，例如：

- Kaggle: International football results from 1872
- GitHub: martj42/international_results
- Fjelstul World Cup Database

替换时保持字段：

```text
match_date,tournament,home_team,away_team,home_score,away_score,neutral
```

其中 `home_team` 和 `away_team` 必须与赛程文件中的英文队名一致。

## 欧洲冠军联赛

欧冠赛程和历史结果由 ESPN Scoreboard 接口动态加载，分为两个赛事代码：

- `uefa.champions_qual`：资格赛与附加赛
- `uefa.champions`：联赛阶段与淘汰赛

应用默认加载当前赛季和此前两个赛季，目标时区为 `Asia/Shanghai`。相关地址、超时时间和赛季数量配置位于 `src/main/resources/application.yml` 的 `champions-league.espn-update` 节点。

## 其他俱乐部赛事

以下赛事由 ESPN Scoreboard 动态加载当前赛季和此前两个赛季：

- `nor.1`：挪超
- `swe.1`：瑞超
- `uefa.europa`、`uefa.europa_qual`：欧罗巴正赛、资格赛与附加赛
- `bra.1`：巴甲
- `usa.1`：美职

芬超使用 TheSportsDB：

- 联赛 `4636`：芬超

韩职使用 FotMob：

- 联赛 `9080`：韩职
- 读取当前赛季及此前两个赛季的完整赛程和全场比分
- TheSportsDB 联赛 `4689` 仅在 FotMob 不可用时作为兜底

TheSportsDB 免费赛季接口存在返回条数和每分钟请求次数限制，不能作为韩职完整赛季的主数据源。所有成功结果都会缓存到 `config/club-competition-schedules.json`。相关配置位于 `application.yml` 的 `club-competitions.schedule-update` 节点。

## 半场比分补充

ESPN 赛事从 Scoreboard 的进球明细直接计算半场比分。TheSportsDB 和 FotMob 联赛赛程接口只返回全场比分，不包含历史半场比分，因此程序会对所有已完赛但缺少半场比分的赛事使用 FotMob 比赛详情进行补充：

- 按比赛日期读取 `https://www.fotmob.com/api/data/matches?date={date}`
- 按主客队、全场比分、开球时间和赛事名称匹配比赛
- 从 `https://www.fotmob.com/api/data/matchDetails?matchId={matchId}` 的 `HT` 事件读取半场比分
- 韩职赛程本身来自 FotMob 时直接复用比赛 ID，避免再次按日期匹配
- 校验半场比分不大于全场比分后写入 `config/half-time-scores.json`

缓存会优先回填，只有新增完赛且缺少半场比分的比赛才访问 FotMob。相关配置位于 `application.yml` 的 `half-time-score.fotmob-update` 节点。

## 体彩玩法开售状态与让球数

全场胜平负和全场让球胜平负自动选择来自中国体彩网的[足球赛果开奖页面](https://www.lottery.gov.cn/jc/zqsgkj/)和[竞彩足球计算器](https://www.sporttery.cn/jc/jsq/zqbf/)。程序调用页面实际使用的两个官方接口：

`https://webapi.sporttery.cn/gateway/uniform/football/getUniformMatchResultV1.qry`

`https://webapi.sporttery.cn/gateway/uniform/football/getMatchCalculatorV1.qry`

字段口径：

- 赛果接口的 `h`、`d`、`a` 以及在售接口的 `had` 用于判断全场胜平负
- 赛果接口的 `goalLine` 以及在售接口的 `hhad.goalLine` 表示全场让球胜平负的让球数
- `matchId`、`matchDate`、`leagueId`、`allHomeTeam`、`allAwayTeam` 用于关联本地赛程
- `sectionsNo999` 用于已完赛场次的比分复核

体彩日期和外部赛程数据转换后的日期可能相差一天，因此程序从目标日期前一天查询到后两天，并同时校验赛事、主客队和比分，避免仅按队名误关联。结果写入 `config/sporttery-market-selections.json`；默认采用体彩结果，用户可逐场手工覆盖。点击“更新数据”时会跳过缓存刷新间隔并重新查询。相关配置位于 `application.yml` 的 `sporttery.result-update` 节点。

## 俱乐部中文名

俱乐部中文名映射位于 `src/main/java/com/eason/worldcup/util/ClubTeamNameTranslator.java`，英文源名称只用于关联赛程和历史数据。

中文名称优先采用国家体育总局体育彩票管理中心在竞彩网公布的竞猜球队全称及受注赛程：

- [2026 年瑞超、芬超部分竞猜球队全简称](https://www.sporttery.cn/ctzc/czgg/20260421/10053253.html)
- [2025 年挪超部分竞猜球队全简称](https://m.sporttery.cn/mctzc/zcgg/20250429/10048134.html)
- [2025 年美职足部分竞猜球队全简称](https://www.sporttery.cn/ctzc/zcgg/20250522/10048467.html)
- [2026 年 5 月 29 日至 6 月 4 日受注赛程](https://m.sporttery.cn/mctzc/czgg/20260526/10053843.html)
- [竞彩网韩职联赛资料](https://info.sporttery.cn/football/history/history_data.php?mid=51)

新出现的俱乐部如果尚未在国内来源核验，程序保留数据源原名，不根据英文自行音译，核验后再补充静态映射。
