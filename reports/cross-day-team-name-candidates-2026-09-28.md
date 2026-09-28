# 跨日比赛与队名候选核查

> 本文件是新增映射前的审计快照，CSV 行号指当时的 239,359 条记录。后续联网核实及实际修改见 [最新核实结果](D:/WorkJava/lottery-football/reports/verified-team-name-mappings-2026-09-28.md)。

核查日期：2026-09-28。仅生成核查报告，未修改比赛数据、映射表或业务代码。

覆盖历史主表 **239,359 条记录**，日期 **2014-10-22—2026-09-27**，原始队名 **8,141** 个，按现有程序映射后的队名 **8,102** 个。这里的队名数量不等于已确认的真实球队数量。

全量查得同一映射队名的相邻日期记录对 **2,019 对**。其中比分按共同队伍视角一致的 **260 对**，涉及 **509 条原始记录、250 个相连候选组、227 组不同队名关系**。记录对不是已确认重复场数，三个来源记录可能组成多对。

另列 **1 对双方均有别名的补充线索**、**21 对比分不同的重点复核记录**、**375 对相隔2—7天的扩展参考**和 **17 对同日同队同分但赛事分类不同的记录**。比分不同重点中，18对双方队名已经一致，另3对为名称线索；扩展参考包含正常主客场两回合或不同赛事，不计入260对核心候选。

最可能的漏检机制：未收录的英文名仍被当作独立标准队名；不同来源的比赛日期相差一天。当前相邻日测试只匹配现有映射后的双方名称，因此别名缺失的记录不会被发现。日期差是否来自时区，仍需原始开球时间核实。

完整比赛证据见 [逐条比赛清单](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md)，每对均有原始日期、主客队、比分、赛事、match_id 和CSV行号。

## 判定口径

- 直接编译调用当前 `Competition.fromSourceCompetition` 与 `ClubTeamNameTranslator.translate`，使用主资源目录里的现有映射，不自行替代程序的优先级与映射链规则
- 核心扫描日期差为1天，覆盖所有赛事和所有队名，不要求同一赛事或同一来源；从共同队伍视角比较进球/失球，因此可以识别主客颠倒
- 日期差2—3天另查双方同名或已有名称线索；4—7天补查双方同名同分。没有把任意相隔数月、数年的相同比分视为重复
- 同日同对手同分记录仅作为名称关系旁证，不并入跨日数量；多次证据按独立日期计算，避免三条来源记录被误算为多场支持
- 身份核实以国家、城市、队伍级别和来源球队ID为准；直接交锋、青年/预备队标识或泛称均保留风险提示
- 数据源范围为历史主表；不包含正在运行的应用内存、临时赛程缓存或尚未写入主表的更新。未联网逐场核验，所有结果均为本地数据证据

## 全部队名候选

下表 **228 组**＝核心227组＋双边补充新增的 `Paksi SE / 保克什` 1组。优先级只表示复核顺序，不代表已经确认应建立映射。

| 复核层级 | 组数 | 含义 |
| --- | ---: | --- |
| 多次证据优先核对 | 50 | 至少两个独立日期有不同来源的同比分支持，且未发现已标注的身份风险 |
| 单次或名称线索 | 75 | 不同来源的一次线索，或拼写相近，仍需确认 |
| 弱线索 | 58 | 主要只有日期和比分相同，不足以判断为同队 |
| 身份/范围风险 | 45 | 库内交锋反证、国家队/俱乐部泛称、预备队标识或疑似误译 |

### 多次证据优先核对

| 编号 | 名称A | 名称B | 相邻日同分对数 | 同日旁证对数 | 比赛证据 | 提示 |
| --- | --- | --- | ---: | ---: | --- | --- |
| G001 | Huddersfield | 哈德斯菲尔德 | 5 | 3 | [D023](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d023)、[D030](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d030)、[D034](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d034)、[D072](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d072)、[D074](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d074) | 尚无库内交锋反证；仍需人工确认身份 |
| G002 | Hertha Berlin | 柏林赫塔 | 5 | 2 | [D048](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d048)、[D094](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d094)、[D097](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d097)、[D126](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d126)、[D140](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d140) | 尚无库内交锋反证；仍需人工确认身份 |
| G003 | Montpellier HSC | 蒙彼利埃 | 5 | 2 | [D027](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d027)、[D031](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d031)、[D036](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d036)、[D038](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d038)、[D070](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d070) | 尚无库内交锋反证；仍需人工确认身份 |
| G004 | Mainz | 美因茨 | 4 | 7 | [D075](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d075)、[D101](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d101)、[D124](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d124)、[D135](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d135) | 尚无库内交锋反证；仍需人工确认身份 |
| G005 | FC Cologne | 科隆 | 3 | 2 | [D063](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d063)、[D137](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d137)、[D146](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d146) | 尚无库内交锋反证；仍需人工确认身份 |
| G006 | UD Las Palmas | 拉帕马斯 | 3 | 1 | [D123](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d123)、[D177](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d177)、[D178](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d178) | 尚无库内交锋反证；仍需人工确认身份 |
| G007 | Gimnàstic Tarragona | Gimnàstic de Tarragona | 3 | 0 | [D050](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d050)、[D111](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d111)、[D122](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d122) | 尚无库内交锋反证；仍需人工确认身份 |
| G008 | CSKA 1948 | CSKA 1948 Sofia | 2 | 3 | [D217](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d217)、[D223](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d223) | 尚无库内交锋反证；仍需人工确认身份 |
| G009 | West Bromwich | 西布罗姆 | 2 | 3 | [D017](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d017)、[D089](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d089) | 尚无库内交锋反证；仍需人工确认身份 |
| G010 | Rotherham | Rotherham United | 2 | 2 | [D010](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d010)、[D069](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d069) | 尚无库内交锋反证；仍需人工确认身份 |
| G011 | Austria Vienna | 维也纳 | 2 | 1 | [D059](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d059)、[D156](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d156) | 尚无库内交锋反证；仍需人工确认身份 |
| G012 | Cork | 科克城 | 2 | 1 | [D020](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d020)、[D060](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d060) | 尚无库内交锋反证；仍需人工确认身份 |
| G013 | Forest Green | Forest Green Rovers | 2 | 1 | [D007](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d007)、[D016](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d016) | 尚无库内交锋反证；仍需人工确认身份 |
| G014 | Lausanne Sports | Lausanne-Sport | 2 | 1 | [D061](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d061)、[D068](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d068) | 尚无库内交锋反证；仍需人工确认身份 |
| G015 | Al Ahly Cairo | 开罗国民 | 2 | 0 | [D052](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d052)、[D056](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d056) | 尚无库内交锋反证；仍需人工确认身份 |
| G016 | NK Lokomotiva | 萨格勒布火车头 | 2 | 0 | [D065](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d065)、[D227](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d227) | 尚无库内交锋反证；仍需人工确认身份 |
| G017 | Perugia Calcio | 佩鲁贾 | 2 | 0 | [D096](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d096)、[D138](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d138) | 尚无库内交锋反证；仍需人工确认身份 |
| G018 | Rot-Weiss Essen | 埃森 | 2 | 0 | [D046](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d046)、[D176](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d176) | 尚无库内交锋反证；仍需人工确认身份 |
| G019 | Shkendija | 斯肯迪亚 | 2 | 0 | [D216](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d216)、[D222](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d222) | 尚无库内交锋反证；仍需人工确认身份 |
| G020 | Colchester United | 科切斯特 | 1 | 23 | [D029](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d029) | 尚无库内交锋反证；仍需人工确认身份 |
| G021 | Hamburg SV | 汉堡 | 1 | 4 | [D076](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d076) | 尚无库内交锋反证；仍需人工确认身份 |
| G022 | New England Revolution | New England Revs | 1 | 4 | [D082](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d082) | 尚无库内交锋反证；仍需人工确认身份 |
| G023 | Atlético Mineiro/MG | 米内罗竞技 | 1 | 3 | [D054](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d054) | 尚无库内交锋反证；仍需人工确认身份 |
| G024 | Guadalajara | Guadalajara Chivas | 1 | 3 | [D051](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d051) | 尚无库内交锋反证；仍需人工确认身份 |
| G025 | SC Paderborn 07 | 帕德博恩 | 1 | 3 | [D042](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d042) | 尚无库内交锋反证；仍需人工确认身份 |
| G026 | Debrecen | 德布勒森 | 1 | 2 | [D125](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d125) | 尚无库内交锋反证；仍需人工确认身份 |
| G027 | Glentoran | 格伦托兰 | 1 | 2 | [D215](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d215) | 尚无库内交锋反证；仍需人工确认身份 |
| G028 | SC Rheindorf Altach | 阿尔塔奇 | 1 | 2 | [D160](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d160) | 尚无库内交锋反证；仍需人工确认身份 |
| G029 | Sheffield Wed | 谢周三 | 1 | 2 | [D141](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d141) | 尚无库内交锋反证；仍需人工确认身份 |
| G030 | Shrewsbury | Shrewsbury Town | 1 | 2 | [D174](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d174) | 尚无库内交锋反证；仍需人工确认身份 |
| G031 | TSV 1860 | 慕1860 | 1 | 2 | [D039](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d039) | 尚无库内交锋反证；仍需人工确认身份 |
| G032 | Öster | 厄斯特什 | 1 | 2 | [D198](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d198) | 尚无库内交锋反证；仍需人工确认身份 |
| G033 | Caykur Rizespor | 里泽 | 1 | 1 | [D037](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d037) | 尚无库内交锋反证；仍需人工确认身份 |
| G034 | Club Olimpia | Olimpia Asunción | 1 | 1 | [D171](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d171) | 尚无库内交锋反证；仍需人工确认身份 |
| G035 | Cúcuta | Cúcuta Deportivo | 1 | 1 | [D193](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d193) | 尚无库内交锋反证；仍需人工确认身份 |
| G036 | Estudiantes La Plata | 拉普大学 | 1 | 1 | [D040](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d040) | 尚无库内交锋反证；仍需人工确认身份 |
| G037 | FC Halifax Town | Halifax | 1 | 1 | [D022](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d022) | 尚无库内交锋反证；仍需人工确认身份 |
| G038 | Győr | 杰尔 | 1 | 1 | [D214](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d214) | 尚无库内交锋反证；仍需人工确认身份 |
| G039 | Hirnyk | Kryvbas | 1 | 1 | [D192](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d192) | 尚无库内交锋反证；仍需人工确认身份 |
| G040 | IK Oddevold | Oddevold | 1 | 1 | [D213](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d213) | 尚无库内交锋反证；仍需人工确认身份 |
| G041 | Lausanne-Sport | 洛桑 | 1 | 1 | [D259](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d259) | 尚无库内交锋反证；仍需人工确认身份 |
| G042 | Mansfield | Mansfield Town | 1 | 1 | [D021](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d021) | 尚无库内交锋反证；仍需人工确认身份 |
| G043 | Mezokövesdi SE | 梅索科菲德 | 1 | 1 | [D159](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d159) | 尚无库内交锋反证；仍需人工确认身份 |
| G044 | Red Star Belgrade | 贝红星 | 1 | 1 | [D155](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d155) | 尚无库内交锋反证；仍需人工确认身份 |
| G045 | SC Verl 1924 | Verl | 1 | 1 | [D134](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d134) | 尚无库内交锋反证；仍需人工确认身份 |
| G046 | SV Darmstadt 98 | 达姆施塔特 | 1 | 1 | [D164](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d164) | 尚无库内交锋反证；仍需人工确认身份 |
| G047 | Salt Lake | 皇家盐湖城 | 1 | 1 | [D197](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d197) | 尚无库内交锋反证；仍需人工确认身份 |
| G048 | SpVgg Greuther Fürth | 菲尔特 | 1 | 1 | [D127](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d127) | 尚无库内交锋反证；仍需人工确认身份 |
| G049 | St. Patrick's Athletic | 圣帕特里 | 1 | 1 | [D064](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d064) | 尚无库内交锋反证；仍需人工确认身份 |
| G050 | Paksi SE | 保克什 | 0 | 2 | [B001](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#b001) | 尚无库内交锋反证；仍需人工确认身份 |

### 单次或名称线索

| 编号 | 名称A | 名称B | 相邻日同分对数 | 同日旁证对数 | 比赛证据 | 提示 |
| --- | --- | --- | ---: | ---: | --- | --- |
| G051 | Radomiak | 拉多米亚克 | 1 | 1 | [D196](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d196) | 尚无库内交锋反证；仍需人工确认身份 |
| G052 | ADV Montecatini | Montecatini | 1 | 0 | [D119](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d119) | 尚无库内交锋反证；仍需人工确认身份 |
| G053 | Akhisar Belediyespor | 阿卡希萨尔 | 1 | 0 | [D071](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d071) | 尚无库内交锋反证；仍需人工确认身份 |
| G054 | Al Hilal Riyadh | 利雅新月 | 1 | 0 | [D167](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d167) | 尚无库内交锋反证；仍需人工确认身份 |
| G055 | Al Wakra | Wakrah | 1 | 0 | [D249](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d249) | 尚无库内交锋反证；仍需人工确认身份 |
| G056 | Al-Wakrah | Wakrah | 1 | 0 | [D248](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d248) | 尚无库内交锋反证；仍需人工确认身份 |
| G057 | Antwerp | 安特卫普 | 1 | 0 | [D172](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d172) | 尚无库内交锋反证；仍需人工确认身份 |
| G058 | Apollon | 利阿波罗 | 1 | 0 | [D239](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d239) | 尚无库内交锋反证；仍需人工确认身份 |
| G059 | Apollon Larisa | Apollon Larissa | 1 | 0 | [D095](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d095) | 尚无库内交锋反证；仍需人工确认身份 |
| G060 | Atlético San Luis | Atlético de San Luis | 1 | 0 | [D098](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d098) | 尚无库内交锋反证；仍需人工确认身份 |
| G061 | Bilbao | 毕尔巴鄂 | 1 | 0 | [D255](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d255) | 尚无库内交锋反证；仍需人工确认身份 |
| G062 | Bray | Bray Wanderers | 1 | 0 | [D200](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d200) | 尚无库内交锋反证；仍需人工确认身份 |
| G063 | Brinje | Brinje Grosuplje | 1 | 0 | [D219](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d219) | 尚无库内交锋反证；仍需人工确认身份 |
| G064 | Brunswick | Eintr. Braunschweig | 1 | 0 | [D253](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d253) | 尚无库内交锋反证；仍需人工确认身份 |
| G065 | Brunswick | 不伦瑞克 | 1 | 0 | [D252](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d252) | 尚无库内交锋反证；仍需人工确认身份 |
| G066 | Cadix | 加的斯 | 1 | 0 | [D256](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d256) | 尚无库内交锋反证；仍需人工确认身份 |
| G067 | Canelas 2010 | Puskas FC Academy | 1 | 0 | [D142](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d142) | 存在跨赛事分类配对 |
| G068 | Charlton | 查尔顿 | 1 | 0 | [D228](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d228) | 尚无库内交锋反证；仍需人工确认身份 |
| G069 | Cibalia | 希巴利亚 | 1 | 0 | [D247](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d247) | 尚无库内交锋反证；仍需人工确认身份 |
| G070 | Club Africain | Club Africain Tunis | 1 | 0 | [D073](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d073) | 尚无库内交锋反证；仍需人工确认身份 |
| G071 | Columbus | Columbus Crew | 1 | 0 | [D234](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d234) | 尚无库内交锋反证；仍需人工确认身份 |
| G072 | Dijon FCO | 第戎 | 1 | 0 | [D032](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d032) | 尚无库内交锋反证；仍需人工确认身份 |
| G073 | Egnatia | Egnatia Rrogozhinë | 1 | 0 | [D165](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d165) | 尚无库内交锋反证；仍需人工确认身份 |
| G074 | Erminio | Giana Erminio | 1 | 0 | [D251](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d251) | 尚无库内交锋反证；仍需人工确认身份 |
| G075 | FC Fleury 91 | Fleury | 1 | 0 | [D231](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d231) | 尚无库内交锋反证；仍需人工确认身份 |
| G076 | FC Kosice | MFK Kosice | 1 | 0 | [D173](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d173) | 尚无库内交锋反证；仍需人工确认身份 |
| G077 | FK AS Pardubice | Pardubice | 1 | 0 | [D139](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d139) | 尚无库内交锋反证；仍需人工确认身份 |
| G078 | Feronikeli | KF Feronikeli | 1 | 0 | [D099](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d099) | 尚无库内交锋反证；仍需人工确认身份 |
| G079 | Forfar | Forfar Athletic | 1 | 0 | [D221](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d221) | 尚无库内交锋反证；仍需人工确认身份 |
| G080 | GC Zurich | Grasshopper | 1 | 0 | [D232](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d232) | 尚无库内交锋反证；仍需人工确认身份 |
| G081 | GC Zurich | 草蜢 | 1 | 0 | [D233](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d233) | 尚无库内交锋反证；仍需人工确认身份 |
| G082 | GrIFK | Grankulla IFK | 1 | 0 | [D207](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d207) | 尚无库内交锋反证；仍需人工确认身份 |
| G083 | H Ramat Gan | Hapoel Ramat Gan | 1 | 0 | [D236](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d236) | 尚无库内交锋反证；仍需人工确认身份 |
| G084 | H&W Welders | Welders | 1 | 0 | [D218](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d218) | 尚无库内交锋反证；仍需人工确认身份 |
| G085 | HUI | Hørsholm Usserød IK | 1 | 0 | [D245](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d245) | 尚无库内交锋反证；仍需人工确认身份 |
| G086 | HUI | Hørsholm-Usserød | 1 | 0 | [D246](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d246) | 尚无库内交锋反证；仍需人工确认身份 |
| G087 | Hajduk | 斯海杜克 | 1 | 0 | [D224](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d224) | 尚无库内交锋反证；仍需人工确认身份 |
| G088 | Honvéd | 布达佩斯捍卫者 | 1 | 0 | [D244](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d244) | 尚无库内交锋反证；仍需人工确认身份 |
| G089 | Ind. Santa Fe | 圣菲独立 | 1 | 0 | [D013](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d013) | 尚无库内交锋反证；仍需人工确认身份 |
| G090 | KFC Uerdingen 05 | Verl | 1 | 0 | [D107](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d107) | 尚无库内交锋反证；仍需人工确认身份 |
| G091 | KVC Westerlo | 韦斯特洛 | 1 | 0 | [D147](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d147) | 尚无库内交锋反证；仍需人工确认身份 |
| G092 | Kaiserslautern | 凯泽 | 1 | 0 | [D035](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d035) | 尚无库内交锋反证；仍需人工确认身份 |
| G093 | Krefelder FC Uerdingen | SC Verl 1924 | 1 | 0 | [D106](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d106) | 尚无库内交锋反证；仍需人工确认身份 |
| G094 | Latina | Latina Calcio | 1 | 0 | [D062](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d062) | 尚无库内交锋反证；仍需人工确认身份 |
| G095 | Limavady United | Limavady Utd | 1 | 0 | [D254](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d254) | 尚无库内交锋反证；仍需人工确认身份 |
| G096 | Mondorf | US Mondorf les Bains | 1 | 0 | [D226](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d226) | 尚无库内交锋反证；仍需人工确认身份 |
| G097 | Nassr/Hilal All-Stars | Riyadh All-Stars XI | 1 | 0 | [D151](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d151) | 尚无库内交锋反证；仍需人工确认身份 |
| G098 | Notts | 诺茨郡 | 1 | 0 | [D235](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d235) | 尚无库内交锋反证；仍需人工确认身份 |
| G099 | Nördlingen | TSV 1861 Nördlingen | 1 | 0 | [D238](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d238) | 尚无库内交锋反证；仍需人工确认身份 |
| G100 | Orlando Pirates | Pirates | 1 | 0 | [D237](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d237) | 尚无库内交锋反证；仍需人工确认身份 |
| G101 | Peñarol | 佩纳罗尔 | 1 | 0 | [D011](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d011) | 尚无库内交锋反证；仍需人工确认身份 |
| G102 | Puskas FC Academy | Puskás | 1 | 0 | [D190](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d190) | 尚无库内交锋反证；仍需人工确认身份 |
| G103 | Puskas FC Academy | 普斯卡什学院 | 1 | 0 | [D191](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d191) | 尚无库内交锋反证；仍需人工确认身份 |
| G104 | RS Waasland-Beveren | Waasland-Beveren | 1 | 0 | [D104](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d104) | 尚无库内交锋反证；仍需人工确认身份 |
| G105 | SD Ponferradina | 蓬费拉迪 | 1 | 0 | [D148](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d148) | 尚无库内交锋反证；仍需人工确认身份 |
| G106 | SG Sonnenhof Großaspach | Sonnenhof Großaspach | 1 | 0 | [D053](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d053) | 尚无库内交锋反证；仍需人工确认身份 |
| G107 | SK Traeff | Træff | 1 | 0 | [D201](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d201) | 尚无库内交锋反证；仍需人工确认身份 |
| G108 | Sainte Genevieve des Bois | Sainte-Geneviève | 1 | 0 | [D058](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d058) | 尚无库内交锋反证；仍需人工确认身份 |
| G109 | Sandnes | Sandnes Ulf | 1 | 0 | [D199](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d199) | 尚无库内交锋反证；仍需人工确认身份 |
| G110 | Shabab Riyadh | 利沙巴布 | 1 | 0 | [D166](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d166) | 尚无库内交锋反证；仍需人工确认身份 |
| G111 | Shamrock | 沙姆洛克 | 1 | 0 | [D220](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d220) | 尚无库内交锋反证；仍需人工确认身份 |
| G112 | Stade Nyonnais | 尼永 | 1 | 0 | [D028](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d028) | 尚无库内交锋反证；仍需人工确认身份 |
| G113 | Stade-Lausanne | 索肖 | 1 | 0 | [D229](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d229) | 尚无库内交锋反证；仍需人工确认身份 |
| G114 | Szegad-Csanád | Szeged-Csanád GA | 1 | 0 | [D170](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d170) | 尚无库内交锋反证；仍需人工确认身份 |
| G115 | Séville | 塞维利亚 | 1 | 0 | [D250](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d250) | 尚无库内交锋反证；仍需人工确认身份 |
| G116 | Ternana | Ternana Calcio | 1 | 0 | [D121](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d121) | 尚无库内交锋反证；仍需人工确认身份 |
| G117 | UCD | 都柏林 | 1 | 0 | [D194](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d194) | 尚无库内交锋反证；仍需人工确认身份 |
| G118 | US Avellino | 阿韦利诺 | 1 | 0 | [D067](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d067) | 尚无库内交锋反证；仍需人工确认身份 |
| G119 | US Lecce | 莱切 | 1 | 0 | [D175](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d175) | 尚无库内交锋反证；仍需人工确认身份 |
| G120 | Utsikten | Utsiktens BK | 1 | 0 | [D210](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d210) | 尚无库内交锋反证；仍需人工确认身份 |
| G121 | Varberg | 瓦尔贝里 | 1 | 0 | [D212](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d212) | 尚无库内交锋反证；仍需人工确认身份 |
| G122 | Zénith | 泽尼特 | 1 | 0 | [D204](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d204) | 尚无库内交锋反证；仍需人工确认身份 |
| G123 | 亚布洛 | 泽尼特 | 1 | 0 | [D014](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d014) | 尚无库内交锋反证；仍需人工确认身份 |
| G124 | 比勒费 | 萨尔茨堡 | 1 | 0 | [D131](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d131) | 尚无库内交锋反证；仍需人工确认身份 |
| G125 | 法伦斯 | 费内巴切 | 1 | 0 | [D186](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d186) | 尚无库内交锋反证；仍需人工确认身份 |

### 弱线索

| 编号 | 名称A | 名称B | 相邻日同分对数 | 同日旁证对数 | 比赛证据 | 提示 |
| --- | --- | --- | ---: | ---: | --- | --- |
| G126 | Aarhus Fremad | 杜保尔 | 1 | 0 | [D180](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d180) | 仅日期/比分相同，身份依据不足 |
| G127 | Abdysh-Ata Kant | 阿斯塔纳 | 1 | 0 | [D169](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d169) | 仅日期/比分相同，身份依据不足 |
| G128 | Akhmat Groznyi | 阿劳 | 1 | 0 | [D079](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d079) | 仅日期/比分相同，身份依据不足 |
| G129 | Al Ula | 布尔萨体育 | 1 | 0 | [D258](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d258) | 仅日期/比分相同，身份依据不足 |
| G130 | AlbinoLeffe | Alcione | 1 | 0 | [D188](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d188) | 仅日期/比分相同，身份依据不足 |
| G131 | Altona 93 | 基尔 | 1 | 0 | [D116](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d116) | 仅日期/比分相同，身份依据不足 |
| G132 | Anadia FC | CD Trofense | 1 | 0 | [D161](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d161) | 仅日期/比分相同，身份依据不足 |
| G133 | Arsenal Tula | Spittal/Drau | 1 | 0 | [D117](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d117) | 仅日期/比分相同，身份依据不足 |
| G134 | CS Mioveni | 贝游击 | 1 | 0 | [D150](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d150) | 仅日期/比分相同，身份依据不足 |
| G135 | Carrarese | 卡尔皮 | 1 | 0 | [D110](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d110) | 仅日期/比分相同，身份依据不足 |
| G136 | Chamois Niortais | SM Caen | 1 | 0 | [D120](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d120) | 仅日期/比分相同，身份依据不足 |
| G137 | Deinze | KSV Oudenaarde | 1 | 0 | [D005](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d005) | 仅日期/比分相同，身份依据不足 |
| G138 | Dinamo Batumi | FC Rustavi | 1 | 0 | [D205](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d205) | 仅日期/比分相同，身份依据不足 |
| G139 | Dinamo Batumi | Valmiera FC | 1 | 0 | [D114](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d114) | 仅日期/比分相同，身份依据不足 |
| G140 | Dinamo Tbilisi | 鲁达普列 | 1 | 0 | [D129](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d129) | 仅日期/比分相同，身份依据不足 |
| G141 | Excelsior Virton | 勒芬 | 1 | 0 | [D077](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d077) | 仅日期/比分相同，身份依据不足 |
| G142 | FC Rapperswil-Jona | 乌法 | 1 | 0 | [D103](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d103) | 仅日期/比分相同，身份依据不足 |
| G143 | FC Stade Lausanne-Ouchy | 索肖 | 1 | 0 | [D230](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d230) | 仅日期/比分相同，身份依据不足 |
| G144 | FK Liepaja | Sileks Kratovo | 1 | 0 | [D113](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d113) | 仅日期/比分相同，身份依据不足 |
| G145 | FK Rad | OFK Bačka | 1 | 0 | [D088](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d088) | 仅日期/比分相同，身份依据不足 |
| G146 | First Vienna FC | 德布勒森 | 1 | 0 | [D157](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d157) | 仅日期/比分相同，身份依据不足 |
| G147 | Floridsdorfer AC | 维尔茨堡 | 1 | 0 | [D015](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d015) | 仅日期/比分相同，身份依据不足 |
| G148 | GKS Tychy | 拉多米亚克 | 1 | 0 | [D128](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d128) | 仅日期/比分相同，身份依据不足 |
| G149 | GS Arconatese | 里昂 | 1 | 0 | [D149](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d149) | 仅日期/比分相同，身份依据不足 |
| G150 | KFC Uerdingen 05 | SC Verl 1924 | 1 | 0 | [D105](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d105) | 仅日期/比分相同，身份依据不足 |
| G151 | Krefelder FC Uerdingen | Verl | 1 | 0 | [D108](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d108) | 仅日期/比分相同，身份依据不足 |
| G152 | Kvik Halden FK | 埃斯比约 | 1 | 0 | [D154](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d154) | 仅日期/比分相同，身份依据不足 |
| G153 | Lions Gibraltar | 赫根 | 1 | 0 | [D179](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d179) | 仅日期/比分相同，身份依据不足 |
| G154 | MFK Karviná | 特马利卡 | 1 | 0 | [D006](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d006) | 仅日期/比分相同，身份依据不足 |
| G155 | Maccabi Netanya | Stal Stalowa Wola | 1 | 0 | [D091](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d091) | 仅日期/比分相同，身份依据不足 |
| G156 | Miami FC | Sarasota Paradise | 1 | 0 | [D182](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d182) | 仅日期/比分相同，身份依据不足 |
| G157 | Mladost Lucani | ND Gorica | 1 | 0 | [D043](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d043) | 仅日期/比分相同，身份依据不足 |
| G158 | Navbahor | 捷特苏 | 1 | 0 | [D206](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d206) | 仅日期/比分相同，身份依据不足 |
| G159 | Politehnica Iasi* | 弗罗茨瓦夫 | 1 | 0 | [D078](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d078) | 仅日期/比分相同，身份依据不足 |
| G160 | Sogndal | 奥德 | 1 | 0 | [D081](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d081) | 仅日期/比分相同，身份依据不足 |
| G161 | Union Fürstenwalde | 开姆尼茨 | 1 | 0 | [D057](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d057) | 仅日期/比分相同，身份依据不足 |
| G162 | Xamax | 埃弗顿 | 1 | 0 | [D090](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d090) | 仅日期/比分相同，身份依据不足 |
| G163 | ŁKS Łomża | 科罗纳 | 1 | 0 | [D257](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d257) | 存在跨赛事分类配对 |
| G164 | 伯恩利 | 里斯本 | 1 | 0 | [D018](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d018) | 仅日期/比分相同，身份依据不足 |
| G165 | 克拉斯诺 | 采列 | 1 | 0 | [D085](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d085) | 仅日期/比分相同，身份依据不足 |
| G166 | 加拉塔萨 | 特斯巴达 | 1 | 0 | [D044](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d044) | 仅日期/比分相同，身份依据不足 |
| G167 | 南锡 | 斯托克城 | 1 | 0 | [D047](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d047) | 仅日期/比分相同，身份依据不足 |
| G168 | 卢宾扎格勒比 | 特普利斯 | 1 | 0 | [D183](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d183) | 仅日期/比分相同，身份依据不足 |
| G169 | 哈尔科夫 | 索列夫 | 1 | 0 | [D130](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d130) | 仅日期/比分相同，身份依据不足 |
| G170 | 圣吉联合 | 比利亚雷 | 1 | 0 | [D133](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d133) | 仅日期/比分相同，身份依据不足 |
| G171 | 埃库莱斯 | 阿梅里亚 | 1 | 0 | [D189](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d189) | 仅日期/比分相同，身份依据不足 |
| G172 | 奥斯坦德 | 瓦朗谢纳 | 1 | 0 | [D162](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d162) | 仅日期/比分相同，身份依据不足 |
| G173 | 富勒姆 | 格罗迪SV | 1 | 0 | [D008](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d008) | 仅日期/比分相同，身份依据不足 |
| G174 | 巴列卡诺 | 拉齐奥 | 1 | 0 | [D145](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d145) | 仅日期/比分相同，身份依据不足 |
| G175 | 巴特 | 贝乌哈图夫 | 1 | 0 | [D003](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d003) | 仅日期/比分相同，身份依据不足 |
| G176 | 拉纳卡 | 特马利卡 | 1 | 0 | [D084](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d084) | 仅日期/比分相同，身份依据不足 |
| G177 | 斯拉维亚 | 莫陆军 | 1 | 0 | [D045](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d045) | 仅日期/比分相同，身份依据不足 |
| G178 | 标准列日 | 罗达JC | 1 | 0 | [D118](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d118) | 仅日期/比分相同，身份依据不足 |
| G179 | 沙勒罗瓦 | 瓦尔韦克 | 1 | 0 | [D184](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d184) | 仅日期/比分相同，身份依据不足 |
| G180 | 波尔图 | 西布罗姆 | 1 | 0 | [D024](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d024) | 仅日期/比分相同，身份依据不足 |
| G181 | 瓦路尔 | 萨普斯堡 | 1 | 0 | [D181](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d181) | 仅日期/比分相同，身份依据不足 |
| G182 | 瓦雷赫姆 | 福伦丹 | 1 | 0 | [D185](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d185) | 仅日期/比分相同，身份依据不足 |
| G183 | 维迪奥顿 | 采列 | 1 | 0 | [D158](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d158) | 仅日期/比分相同，身份依据不足 |

### 身份/范围风险

| 编号 | 名称A | 名称B | 相邻日同分对数 | 同日旁证对数 | 比赛证据 | 提示 |
| --- | --- | --- | ---: | ---: | --- | --- |
| G184 | Aris | 阿里斯 | 1 | 1 | [D087](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d087) | Aris 为短名，需核对国家/城市和来源球队ID |
| G185 | CSKA | 莫陆军 | 1 | 1 | [D202](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d202) | CSKA 为短名，需核对国家/城市，不能只凭缩写新增全局映射 |
| G186 | 布斯巴达 | 鹿斯巴达 | 1 | 1 | [D240](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d240) | 名称中的布/鹿不同，疑似把不同地区的 Sparta 误译到同一场；先核对来源球队ID |
| G187 | BW林茨 | 里德 | 1 | 0 | [D086](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d086) | 库内已有双方直接交锋4场，不宜直接合并 |
| G188 | Burton Albion | Rotherham United | 1 | 0 | [D009](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d009) | 库内已有双方直接交锋10场，不宜直接合并 |
| G189 | CA Osasuna B | 莱万特 | 1 | 0 | [D163](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d163) | 包含青年/预备队标识，需核对队伍级别 |
| G190 | GD Gafanha | 法马利康 | 1 | 0 | [D025](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d025) | 库内已有双方直接交锋1场，不宜直接合并 |
| G191 | Guayaquil City | 西班牙人 | 1 | 0 | [D055](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d055) | 共同队名巴萨可能已被误归一，先查共同队伍的来源身份；存在跨赛事分类配对 |
| G192 | Guayaquil City | 阿瓦塞特 | 1 | 0 | [D203](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d203) | 共同队名巴萨可能已被误归一，先查共同队伍的来源身份；存在跨赛事分类配对 |
| G193 | Hannover II | 达姆施塔特 | 1 | 0 | [D195](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d195) | 包含青年/预备队标识，需核对队伍级别；存在跨赛事分类配对 |
| G194 | Inter Bratislava | 国际米兰 | 1 | 0 | [D225](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d225) | 英文含 Bratislava，现有中文为国际米兰；优先排查来源名称误译，不能据此建立全局映射 |
| G195 | ML Vitebsk | Neman Grodno | 1 | 0 | [D168](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d168) | 库内已有双方直接交锋1场，不宜直接合并 |
| G196 | Mushuc Runa | 赫罗纳 | 1 | 0 | [D152](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d152) | 共同队名巴萨可能已被误归一，先查共同队伍的来源身份；存在跨赛事分类配对 |
| G197 | Racing Ferrol | 奥维耶多 | 1 | 0 | [D033](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d033) | 库内已有双方直接交锋2场，不宜直接合并 |
| G198 | SC Braga B | 吉维森特 | 1 | 0 | [D260](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d260) | 库内已有双方直接交锋9场，不宜直接合并；包含青年/预备队标识，需核对队伍级别 |
| G199 | SC Braga B | 雷克斯欧 | 1 | 0 | [D187](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d187) | 库内已有双方直接交锋9场，不宜直接合并；包含青年/预备队标识，需核对队伍级别 |
| G200 | US Quevilly Rouen | 勒阿弗尔 | 1 | 0 | [D242](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d242) | 库内已有双方直接交锋4场，不宜直接合并 |
| G201 | 亚拉腊 | 泽尼特 | 1 | 0 | [D153](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d153) | 库内已有双方直接交锋2场，不宜直接合并 |
| G202 | 佐加顿斯 | 哈马比 | 1 | 0 | [D211](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d211) | 库内已有双方直接交锋27场，不宜直接合并；存在跨赛事分类配对 |
| G203 | 克拉斯诺 | 沃尔夫斯 | 1 | 0 | [D115](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d115) | 库内已有双方直接交锋1场，不宜直接合并 |
| G204 | 克拉科维亚 | 卢宾扎格勒比 | 1 | 0 | [D080](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d080) | 库内已有双方直接交锋25场，不宜直接合并 |
| G205 | 克里斯蒂 | 桑纳菲 | 1 | 0 | [D004](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d004) | 库内已有双方直接交锋15场，不宜直接合并 |
| G206 | 利物浦 | 曼联 | 1 | 0 | [D019](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d019) | 库内已有双方直接交锋30场，不宜直接合并 |
| G207 | 博德闪耀 | 马韦利亚 | 1 | 0 | [D102](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d102) | 库内已有双方直接交锋1场，不宜直接合并 |
| G208 | 博莱 | 布斯巴达 | 1 | 0 | [D083](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d083) | 库内已有双方直接交锋26场，不宜直接合并 |
| G209 | 博阿维斯 | 费雷拉 | 1 | 0 | [D026](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d026) | 库内已有双方直接交锋18场，不宜直接合并 |
| G210 | 卡斯鲁厄 | 布鲁日 | 1 | 0 | [D041](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d041) | 库内已有双方直接交锋1场，不宜直接合并 |
| G211 | 吉马良斯 | 维多利亚 | 1 | 0 | [D243](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d243) | 维多利亚名称含糊，需核对国家、城市和来源球队ID后确定适用范围 |
| G212 | 圣克拉拉 | 葡国民 | 1 | 0 | [D143](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d143) | 库内已有双方直接交锋15场，不宜直接合并 |
| G213 | 圣克拉拉 | 费雷拉 | 1 | 0 | [D092](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d092) | 库内已有双方直接交锋12场，不宜直接合并 |
| G214 | 埃弗顿 | 西布罗姆 | 1 | 0 | [D012](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d012) | 库内已有双方直接交锋9场，不宜直接合并 |
| G215 | 埃斯比约 | 锡尔克堡 | 1 | 0 | [D112](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d112) | 库内已有双方直接交锋10场，不宜直接合并 |
| G216 | 埃森 | 科隆 | 1 | 0 | [D066](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d066) | 库内已有双方直接交锋1场，不宜直接合并 |
| G217 | 埃瓦尔 | 奥维耶多 | 1 | 0 | [D109](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d109) | 库内已有双方直接交锋12场，不宜直接合并 |
| G218 | 埃门 | 奥斯纳 | 1 | 0 | [D136](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d136) | 库内已有双方直接交锋4场，不宜直接合并 |
| G219 | 基尔 | 腓特烈 | 1 | 0 | [D002](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d002) | 库内已有双方直接交锋1场，不宜直接合并 |
| G220 | 奥地利 | 维也纳 | 1 | 0 | [D241](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d241)、[B001](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#b001) | 奥地利同时是现有国家队名；若为俱乐部别名，必须限制赛事/来源范围 |
| G221 | 奥斯纳 | 波鸿 | 1 | 0 | [D132](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d132) | 库内已有双方直接交锋5场，不宜直接合并 |
| G222 | 女王巡游 | 西汉姆联 | 1 | 0 | [D001](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d001) | 库内已有双方直接交锋2场，不宜直接合并 |
| G223 | 巴利亚多 | 贝西克塔斯 | 1 | 0 | [D049](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d049) | 库内已有双方直接交锋1场，不宜直接合并 |
| G224 | 比萨 | 萨索洛 | 1 | 0 | [D209](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d209) | 库内已有双方直接交锋4场，不宜直接合并 |
| G225 | 沃夫斯堡 | 首尔FC | 1 | 0 | [D100](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d100) | 库内已有双方直接交锋1场，不宜直接合并 |
| G226 | 法伦斯 | 里斯本 | 1 | 0 | [D144](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d144) | 库内已有双方直接交锋10场，不宜直接合并 |
| G227 | 法马利康 | 费伦斯 | 1 | 0 | [D093](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d093) | 库内已有双方直接交锋4场，不宜直接合并 |
| G228 | 特温特 | 阿尔克马 | 1 | 0 | [D208](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d208) | 库内已有双方直接交锋26场，不宜直接合并 |

## 重点身份风险

以下记录尤其应回查来源球队ID和原文，不能仅通过增加全局别名消除重复提示。

| 名称/共同队伍 | 证据 | 应核对的问题 |
| --- | --- | --- |
| Inter Bratislava / 国际米兰 | [D225](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d225) | 英文含 Bratislava，现有中文为国际米兰；优先排查来源名称误译，不能据此建立全局映射 |
| 奥地利 / 维也纳 | [D241](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d241)、[B001](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#b001) | 奥地利同时是现有国家队名；若为俱乐部别名，必须限制赛事/来源范围 |
| 布斯巴达 / 鹿斯巴达 | [D240](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d240) | 名称中的布/鹿不同，疑似把不同地区的 Sparta 误译到同一场；先核对来源球队ID |
| 共同队伍巴萨；对手 Guayaquil City / 西班牙人 | [D055](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d055) | 应先核实共同队伍的来源身份 |
| 共同队伍巴萨；对手 Guayaquil City / 阿瓦塞特 | [D203](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d203) | 应先核实共同队伍的来源身份 |
| 共同队伍巴萨；对手 Mushuc Runa / 赫罗纳 | [D152](D:/WorkJava/lottery-football/reports/cross-day-match-details-2026-09-28.md#d152) | 应先核实共同队伍的来源身份 |

## 现有映射位置

这些候选在当前实际比赛上下文中尚未统一为同名。下表定位当前全局别名行；自映射只保留原名，不会把两端合并。现有代码优先级为 MANUAL 7、VERIFIED_SPORTTERY 6、VERIFIED_ALIAS 4、INFERRED_DUPLICATE/ESPN_SCHEDULE 3、HISTORICAL_ODDS 2、HISTORICAL_MATCHES 1；同优先级时优先赛事级映射。

| 编号 | 两端现有映射 |
| --- | --- |
| G001 | Huddersfield：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6716)<br>哈德斯菲尔德：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1420) |
| G002 | Hertha Berlin：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6637)<br>柏林赫塔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:592) |
| G003 | Montpellier HSC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7734)<br>蒙彼利埃：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2528) |
| G004 | Mainz：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7534)<br>美因茨：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2516) |
| G005 | FC Cologne：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5757)<br>科隆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1878) |
| G006 | UD Las Palmas：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9652)<br>拉帕马斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2029) |
| G007 | Gimnàstic Tarragona：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6408)<br>Gimnàstic de Tarragona：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6407) |
| G008 | CSKA 1948：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5294)<br>CSKA 1948 Sofia：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5295) |
| G009 | West Bromwich：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10057)<br>西布罗姆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3785) |
| G010 | Rotherham：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8551)<br>Rotherham United：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8552) |
| G011 | Austria Vienna：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4586)<br>维也纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3678) |
| G012 | Cork：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5237)<br>科克城：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1870) |
| G013 | Forest Green：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6259)<br>Forest Green Rovers：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6260) |
| G014 | Lausanne Sports：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7321)<br>Lausanne-Sport：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7322) |
| G015 | Al Ahly Cairo：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4289)<br>开罗国民：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1818) |
| G016 | NK Lokomotiva：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7908)<br>萨格勒布火车头：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3001) |
| G017 | Perugia Calcio：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8189)<br>佩鲁贾：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2829) |
| G018 | Rot-Weiss Essen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8549)<br>埃森：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:291) |
| G019 | Shkendija：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8879)<br>斯肯迪亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3287) |
| G020 | Colchester United：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5212)<br>科切斯特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1902) |
| G021 | Hamburg SV：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6548)<br>汉堡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1483) |
| G022 | New England Revolution：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7861)<br>New England Revs：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7862) |
| G023 | Atlético Mineiro/MG：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4558)<br>米内罗竞技：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2574) |
| G024 | Guadalajara：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6488)<br>Guadalajara Chivas：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6490) |
| G025 | SC Paderborn 07：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8726)<br>帕德博恩：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2795) |
| G026 | Debrecen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5358)<br>德布勒森：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:994) |
| G027 | Glentoran：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6417)<br>格伦托兰：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1363) |
| G028 | SC Rheindorf Altach：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8732)<br>阿尔塔奇：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:96) |
| G029 | Sheffield Wed：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8871)<br>谢周三：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3865) |
| G030 | Shrewsbury：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8882)<br>Shrewsbury Town：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8883) |
| G031 | TSV 1860：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9581)<br>慕1860：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2660) |
| G032 | Öster：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8098)<br>厄斯特什：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1126) |
| G033 | Caykur Rizespor：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5040)<br>里泽：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2197) |
| G034 | Club Olimpia：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5202)<br>Olimpia Asunción：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8032) |
| G035 | Cúcuta：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5309)<br>Cúcuta Deportivo：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5310) |
| G036 | Estudiantes La Plata：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5635)<br>拉普大学：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2031) |
| G037 | FC Halifax Town：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5816)<br>Halifax：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6540) |
| G038 | Győr：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6523)<br>杰尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1697) |
| G039 | Hirnyk：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6650)<br>Kryvbas：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7236) |
| G040 | IK Oddevold：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6810)<br>Oddevold：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7998) |
| G041 | Lausanne-Sport：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7322)<br>洛桑：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2375) |
| G042 | Mansfield：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7566)<br>Mansfield Town：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7567) |
| G043 | Mezokövesdi SE：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7659)<br>梅索科菲德：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2506) |
| G044 | Red Star Belgrade：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8450)<br>贝红星：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:645) |
| G045 | SC Verl 1924：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8743)<br>Verl：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9867) |
| G046 | SV Darmstadt 98：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9279)<br>达姆施塔特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:956) |
| G047 | Salt Lake：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8624)<br>皇家盐湖城：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1577) |
| G048 | SpVgg Greuther Fürth：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9148)<br>菲尔特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1164) |
| G049 | St. Patrick's Athletic：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9182)<br>圣帕特里：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3234) |
| G050 | Paksi SE：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8133)<br>保克什：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:613) |
| G051 | Radomiak：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8388)<br>拉多米亚克：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2008) |
| G052 | ADV Montecatini：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4221)<br>Montecatini：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7729) |
| G053 | Akhisar Belediyespor：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4281)<br>阿卡希萨尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:122) |
| G054 | Al Hilal Riyadh：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4299)<br>利雅新月：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2240) |
| G055 | Al Wakra：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4316)<br>Wakrah：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10032) |
| G056 | Al-Wakrah：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4318)<br>Wakrah：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10032) |
| G057 | Antwerp：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4421)<br>安特卫普：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:363) |
| G058 | Apollon：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4429)<br>利阿波罗：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2201) |
| G059 | Apollon Larisa：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4430)<br>Apollon Larissa：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4431) |
| G060 | Atlético San Luis：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4563)<br>Atlético de San Luis：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4553) |
| G061 | Bilbao：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4779)<br>毕尔巴鄂：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:711) |
| G062 | Bray：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4890)<br>Bray Wanderers：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4891) |
| G063 | Brinje：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4908)<br>Brinje Grosuplje：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4909) |
| G064 | Brunswick：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4924)<br>Eintr. Braunschweig：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5565) |
| G065 | Brunswick：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4924)<br>不伦瑞克：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:811) |
| G066 | Cadix：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4988)<br>加的斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1657) |
| G067 | Canelas 2010：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5003)<br>Puskas FC Academy：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8346) |
| G068 | Charlton：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5122)<br>查尔顿：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:923) |
| G069 | Cibalia：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5176)<br>希巴利亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3805) |
| G070 | Club Africain：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5190)<br>Club Africain Tunis：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5191) |
| G071 | Columbus：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5219)<br>Columbus Crew：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5220) |
| G072 | Dijon FCO：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5409)<br>第戎：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1052) |
| G073 | Egnatia：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5558)<br>Egnatia Rrogozhinë：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5559) |
| G074 | Erminio：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5610)<br>Giana Erminio：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6389) |
| G075 | FC Fleury 91：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5789)<br>Fleury：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6241) |
| G076 | FC Kosice：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5858)<br>MFK Kosice：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7667) |
| G077 | FK AS Pardubice：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6104)<br>Pardubice：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8147) |
| G078 | Feronikeli：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6077)<br>KF Feronikeli：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7093) |
| G079 | Forfar：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6261)<br>Forfar Athletic：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6262) |
| G080 | GC Zurich：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6356)<br>Grasshopper：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6462) |
| G081 | GC Zurich：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6356)<br>草蜢：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:921) |
| G082 | GrIFK：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6473)<br>Grankulla IFK：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6460) |
| G083 | H Ramat Gan：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6526)<br>Hapoel Ramat Gan：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6570) |
| G084 | H&W Welders：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6528)<br>Welders：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10049) |
| G085 | HUI：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6720)<br>Hørsholm Usserød IK：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6701) |
| G086 | HUI：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6720)<br>Hørsholm-Usserød：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6702) |
| G087 | Hajduk：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6535)<br>斯海杜克：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3284) |
| G088 | Honvéd：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6693)<br>布达佩斯捍卫者：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:818) |
| G089 | Ind. Santa Fe：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6836)<br>圣菲独立：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3186) |
| G090 | KFC Uerdingen 05：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7123)<br>Verl：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9867) |
| G091 | KVC Westerlo：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7269)<br>韦斯特洛：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3631) |
| G092 | Kaiserslautern：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7001)<br>凯泽：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1833) |
| G093 | Krefelder FC Uerdingen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7219)<br>SC Verl 1924：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8743) |
| G094 | Latina：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7317)<br>Latina Calcio：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7318) |
| G095 | Limavady United：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7384)<br>Limavady Utd：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7385) |
| G096 | Mondorf：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7723)<br>US Mondorf les Bains：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9771) |
| G097 | Nassr/Hilal All-Stars：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7826)<br>Riyadh All-Stars XI：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8506) |
| G098 | Notts：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7958)<br>诺茨郡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2773) |
| G099 | Nördlingen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7944)<br>TSV 1861 Nördlingen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9584) |
| G100 | Orlando Pirates：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8085)<br>Pirates：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8224) |
| G101 | Peñarol：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8176)<br>佩纳罗尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2835) |
| G102 | Puskas FC Academy：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8346)<br>Puskás：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8344) |
| G103 | Puskas FC Academy：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8346)<br>普斯卡什学院：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2893) |
| G104 | RS Waasland-Beveren：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8561)<br>Waasland-Beveren：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10027) |
| G105 | SD Ponferradina：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8783)<br>蓬费拉迪：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2844) |
| G106 | SG Sonnenhof Großaspach：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8841)<br>Sonnenhof Großaspach：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9060) |
| G107 | SK Traeff：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8950)<br>Træff：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9547) |
| G108 | Sainte Genevieve des Bois：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8611)<br>Sainte-Geneviève：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8612) |
| G109 | Sandnes：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8642)<br>Sandnes Ulf：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8643) |
| G110 | Shabab Riyadh：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8848)<br>利沙巴布：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2218) |
| G111 | Shamrock：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8864)<br>沙姆洛克：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3128) |
| G112 | Stade Nyonnais：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9191)<br>尼永：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2733) |
| G113 | Stade-Lausanne：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9196)<br>索肖：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3382) |
| G114 | Szegad-Csanád：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9365)<br>Szeged-Csanád GA：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9367) |
| G115 | Séville：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8832)<br>塞维利亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3079) |
| G116 | Ternana：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9431)<br>Ternana Calcio：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9432) |
| G117 | UCD：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9647)<br>都柏林：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1065) |
| G118 | US Avellino：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9757)<br>阿韦利诺：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:233) |
| G119 | US Lecce：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9768)<br>莱切：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2068) |
| G120 | Utsikten：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9799)<br>Utsiktens BK：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9800) |
| G121 | Varberg：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9828)<br>瓦尔贝里：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3546) |
| G122 | Zénith：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10195)<br>泽尼特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4073) |
| G123 | 亚布洛：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3919)<br>泽尼特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4073) |
| G124 | 比勒费：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:683)<br>萨尔茨堡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2990) |
| G125 | 法伦斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1151)<br>费内巴切：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1194) |
| G126 | Aarhus Fremad：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4146)<br>杜保尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1070) |
| G127 | Abdysh-Ata Kant：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4154)<br>阿斯塔纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:214) |
| G128 | Akhmat Groznyi：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4282)<br>阿劳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:157) |
| G129 | Al Ula：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4313)<br>布尔萨体育：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:829) |
| G130 | AlbinoLeffe：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4327)<br>Alcione：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4332) |
| G131 | Altona 93：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4373)<br>基尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1604) |
| G132 | Anadia FC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4392)<br>CD Trofense：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5074) |
| G133 | Arsenal Tula：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4471)<br>Spittal/Drau：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9115) |
| G134 | CS Mioveni：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5281)<br>贝游击：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:668) |
| G135 | Carrarese：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5024)<br>卡尔皮：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1751) |
| G136 | Chamois Niortais：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5114)<br>SM Caen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9024) |
| G137 | Deinze：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5364)<br>KSV Oudenaarde：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7246) |
| G138 | Dinamo Batumi：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5413)<br>FC Rustavi：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5959) |
| G139 | Dinamo Batumi：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5413)<br>Valmiera FC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9817) |
| G140 | Dinamo Tbilisi：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5424)<br>鲁达普列：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2296) |
| G141 | Excelsior Virton：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5654)<br>勒芬：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2116) |
| G142 | FC Rapperswil-Jona：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5944)<br>乌法：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3733) |
| G143 | FC Stade Lausanne-Ouchy：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5989)<br>索肖：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3382) |
| G144 | FK Liepaja：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6155)<br>Sileks Kratovo：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8892) |
| G145 | FK Rad：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6182)<br>OFK Bačka：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8009) |
| G146 | First Vienna FC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6093)<br>德布勒森：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:994) |
| G147 | Floridsdorfer AC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6249)<br>维尔茨堡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3651) |
| G148 | GKS Tychy：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6414)<br>拉多米亚克：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2008) |
| G149 | GS Arconatese：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6486)<br>里昂：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2159) |
| G150 | KFC Uerdingen 05：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7123)<br>SC Verl 1924：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8743) |
| G151 | Krefelder FC Uerdingen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7219)<br>Verl：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9867) |
| G152 | Kvik Halden FK：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7271)<br>埃斯比约：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:297) |
| G153 | Lions Gibraltar：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7400)<br>赫根：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1518) |
| G154 | MFK Karviná：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7666)<br>特马利卡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3456) |
| G155 | Maccabi Netanya：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7514)<br>Stal Stalowa Wola：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9202) |
| G156 | Miami FC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7676)<br>Sarasota Paradise：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8670) |
| G157 | Mladost Lucani：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7706)<br>ND Gorica：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7838) |
| G158 | Navbahor：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7832)<br>捷特苏：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1707) |
| G159 | Politehnica Iasi*：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8264)<br>弗罗茨瓦夫：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1226) |
| G160 | Sogndal：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9036)<br>奥德：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:377) |
| G161 | Union Fürstenwalde：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9711)<br>开姆尼茨：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1820) |
| G162 | Xamax：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:10111)<br>埃弗顿：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:267) |
| G163 | ŁKS Łomża：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7417)<br>科罗纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1885) |
| G164 | 伯恩利：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:768)<br>里斯本：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2186) |
| G165 | 克拉斯诺：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1936)<br>采列：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:918) |
| G166 | 加拉塔萨：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1667)<br>特斯巴达：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3465) |
| G167 | 南锡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2711)<br>斯托克城：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3326) |
| G168 | 卢宾扎格勒比：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2271)<br>特普利斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3462) |
| G169 | 哈尔科夫：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1422)<br>索列夫：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3374) |
| G170 | 圣吉联合：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3200)<br>比利亚雷：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:690) |
| G171 | 埃库莱斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:280)<br>阿梅里亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:193) |
| G172 | 奥斯坦德：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:469)<br>瓦朗谢纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3562) |
| G173 | 富勒姆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1265)<br>格罗迪SV：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1365) |
| G174 | 巴列卡诺：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:542)<br>拉齐奥：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2036) |
| G175 | 巴特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:569)<br>贝乌哈图夫：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:659) |
| G176 | 拉纳卡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2025)<br>特马利卡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3456) |
| G177 | 斯拉维亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3291)<br>莫陆军：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2628) |
| G178 | 标准列日：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:715)<br>罗达JC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2326) |
| G179 | 沙勒罗瓦：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3124)<br>瓦尔韦克：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3554) |
| G180 | 波尔图：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:739)<br>西布罗姆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3785) |
| G181 | 瓦路尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3571)<br>萨普斯堡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3030) |
| G182 | 瓦雷赫姆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3565)<br>福伦丹：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1251) |
| G183 | 维迪奥顿：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3642)<br>采列：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:918) |
| G184 | Aris：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4461)<br>阿里斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:167) |
| G185 | CSKA：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:5293)<br>莫陆军：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2628) |
| G186 | 布斯巴达：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:913)<br>鹿斯巴达：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2316) |
| G187 | BW林茨：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4976)<br>里德：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2169) |
| G188 | Burton Albion：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4960)<br>Rotherham United：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8552) |
| G189 | CA Osasuna B：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4983)<br>莱万特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2077) |
| G190 | GD Gafanha：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6358)<br>法马利康：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1155) |
| G191 | Guayaquil City：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6496)<br>西班牙人：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3780) |
| G192 | Guayaquil City：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6496)<br>阿瓦塞特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:228) |
| G193 | Hannover II：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6559)<br>达姆施塔特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:956) |
| G194 | Inter Bratislava：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:6849)<br>国际米兰：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1405) |
| G195 | ML Vitebsk：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7701)<br>Neman Grodno：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7851) |
| G196 | Mushuc Runa：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:7788)<br>赫罗纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1528) |
| G197 | Racing Ferrol：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8375)<br>奥维耶多：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:479) |
| G198 | SC Braga B：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8695)<br>吉维森特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1642) |
| G199 | SC Braga B：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:8695)<br>雷克斯欧：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2143) |
| G200 | US Quevilly Rouen：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:9774)<br>勒阿弗尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2113) |
| G201 | 亚拉腊：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3925)<br>泽尼特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4073) |
| G202 | 佐加顿斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:4135)<br>哈马比：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1438) |
| G203 | 克拉斯诺：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1936)<br>沃尔夫斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3707) |
| G204 | 克拉科维亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1930)<br>卢宾扎格勒比：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2271) |
| G205 | 克里斯蒂：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1955)<br>桑纳菲：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3096) |
| G206 | 利物浦：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2230)<br>曼联：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2484) |
| G207 | 博德闪耀：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:784)<br>马韦利亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2449) |
| G208 | 博莱：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:797)<br>布斯巴达：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:913) |
| G209 | 博阿维斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:777)<br>费雷拉：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1184) |
| G210 | 卡斯鲁厄：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1793)<br>布鲁日：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:898) |
| G211 | 吉马良斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1639)<br>维多利亚：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3649) |
| G212 | 圣克拉拉：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3209)<br>葡国民：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2857) |
| G213 | 圣克拉拉：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3209)<br>费雷拉：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1184) |
| G214 | 埃弗顿：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:267)<br>西布罗姆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3785) |
| G215 | 埃斯比约：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:297)<br>锡尔克堡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3830) |
| G216 | 埃森：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:291)<br>科隆：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1878) |
| G217 | 埃瓦尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:311)<br>奥维耶多：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:479) |
| G218 | 埃门：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:285)<br>奥斯纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:467) |
| G219 | 基尔：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1604)<br>腓特烈：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1170) |
| G220 | 奥地利：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:382)<br>维也纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3678) |
| G221 | 奥斯纳：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:467)<br>波鸿：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:746) |
| G222 | 女王巡游：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2764)<br>西汉姆联：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3790) |
| G223 | 巴利亚多：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:538)<br>贝西克塔斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:664) |
| G224 | 比萨：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:694)<br>萨索洛：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3034) |
| G225 | 沃夫斯堡：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3715)<br>首尔FC：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3266) |
| G226 | 法伦斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1151)<br>里斯本：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:2186) |
| G227 | 法马利康：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1155)<br>费伦斯：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:1190) |
| G228 | 特温特：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:3470)<br>阿尔克马：[全局自映射](D:/WorkJava/lottery-football/src/main/resources/data/team_name_mappings.csv:81) |

## 复核结果的使用

可按 G 编号确认哪些名称是同一队，再选定现有标准名和适用赛事。没有完成身份确认前，报告中的名称关系只保留为候选。对于比分不同、同名误译或赛事分类不同的记录，增加队名映射本身不足以修复。

核查可重复性：原始CSV行号包含表头。下列SHA-256对应本次实际读取的文件。

| 文件 | SHA-256 |
| --- | --- |
| historical_matches.csv | `2a6954c4c403d1588a8213fdabbae487460d99a5ce8061ff424f0d4e37ef3bbf` |
| team_name_mappings.csv | `bffdd473ce150efe28461519aaf34c14b503b40de40958f3bc90a666f04704f6` |
