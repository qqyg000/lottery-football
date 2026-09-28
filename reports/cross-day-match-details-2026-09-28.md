# 跨日比赛逐条核查清单

> 本文件保留修改前的原始证据与行号。新增映射、去重保留 ID 和未合并结论见 [最新核实结果](D:/WorkJava/lottery-football/reports/verified-team-name-mappings-2026-09-28.md)。

队名汇总与方法见 [核查报告](D:/WorkJava/lottery-football/reports/cross-day-team-name-candidates-2026-09-28.md)。所有比分保持原始主客队顺序，判断相等时才转换成共同队伍视角。

D：相隔一天同分的核心候选；B：双方别名补充；S：比分不一致重点复核；E：2—7天扩展参考；T：同日跨赛事分类；W：双边弱关联，不作为映射建议。

## D 相邻日期同比分：全部260对

<a id="d001"></a>
### D001

名称关系：**女王巡游 / 西汉姆联**（G222）；共同队伍：**阿森纳**。
复核层级：身份/范围风险；库内已有双方直接交锋2场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2014-12-27 | 阿森纳 | 2:1 | 女王巡游 | 英超 / PREMIER_LEAGUE | FOTMOB-1724152 | [第3742行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:3742) |
| 2014-12-28 | 西汉姆联 | 1:2 | 阿森纳 | 英超 / PREMIER_LEAGUE | FOTMOB-1724171 | [第3769行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:3769) |

交锋反证示例：2015-04-25 女王巡游 0:0 西汉姆联，FOTMOB-1724318，[第10800行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:10800)。

<a id="d002"></a>
### D002

名称关系：**基尔 / 腓特烈**（G219）；共同队伍：**桑德捷**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-01-22 | 腓特烈 | 0:1 | 桑德捷 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5E23FD83665BA6A6 | [第4688行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4688) |
| 2015-01-23 | 基尔 | 0:1 | 桑德捷 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E197A9312FACBDF8 | [第4729行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4729) |

交锋反证示例：2026-07-04 基尔 1:3 腓特烈，FUTBOL24-B85C4CF8B1EA2A2F，[第237180行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237180)。

<a id="d003"></a>
### D003

名称关系：**巴特 / 贝乌哈图夫**（G175）；共同队伍：**马里博尔**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-02-03 | 马里博尔 | 2:2 | 贝乌哈图夫 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E46BD5F8D6A9F956 | [第5426行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5426) |
| 2015-02-04 | 马里博尔 | 2:2 | 巴特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2AF9AF40CF12BA46 | [第5462行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5462) |

<a id="d004"></a>
### D004

名称关系：**克里斯蒂 / 桑纳菲**（G205）；共同队伍：**莫尔德**。
复核层级：身份/范围风险；库内已有双方直接交锋15场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-02-28 | 莫尔德 | 2:1 | 克里斯蒂 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3DCFFA20ED1348E6 | [第6941行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:6941) |
| 2015-03-01 | 莫尔德 | 2:1 | 桑纳菲 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1B0D8C62B7451DA6 | [第7076行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:7076) |

交锋反证示例：2017-04-18 桑纳菲 2:0 克里斯蒂，FOTMOB-2427038，[第55398行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:55398)。

<a id="d005"></a>
### D005

名称关系：**Deinze / KSV Oudenaarde**（G137）；共同队伍：**根特**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-06-26 | KSV Oudenaarde | 2:2 | 根特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1240D937E5D641C3 | [第14147行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14147) |
| 2015-06-27 | Deinze | 2:2 | 根特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BAAC534876C1A14B | [第14224行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14224) |

<a id="d006"></a>
### D006

名称关系：**MFK Karviná / 特马利卡**（G154）；共同队伍：**扎布热矿工**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-10 | 扎布热矿工 | 0:0 | MFK Karviná | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FE00D92259F182B6 | [第14838行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14838) |
| 2015-07-11 | 扎布热矿工 | 0:0 | 特马利卡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E748FEB5FF32E119 | [第14948行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14948) |

<a id="d007"></a>
### D007

名称关系：**Forest Green / Forest Green Rovers**（G013）；共同队伍：**加的夫城**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-15 | Forest Green | 1:1 | 加的夫城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1298F27F32A9D2FF | [第15132行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15132) |
| 2015-07-16 | Forest Green Rovers | 1:1 | 加的夫城 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-430317 | [第15174行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15174) |

<a id="d008"></a>
### D008

名称关系：**富勒姆 / 格罗迪SV**（G173）；共同队伍：**柏林赫塔**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-21 | 柏林赫塔 | 2:2 | 格罗迪SV | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CFC00D416E568712 | [第15588行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15588) |
| 2015-07-22 | 柏林赫塔 | 2:2 | 富勒姆 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2CF1553095A0E594 | [第15632行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15632) |

<a id="d009"></a>
### D009

名称关系：**Burton Albion / Rotherham United**（G188）；共同队伍：**莱切斯特**。
复核层级：身份/范围风险；库内已有双方直接交锋10场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-29 | Burton Albion | 1:2 | 莱切斯特 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-430500 | [第16092行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16092) |
| 2015-07-30 | Rotherham United | 1:2 | 莱切斯特 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-430565 | [第16167行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16167) |

交锋反证示例：2016-12-03 Burton Albion 2:1 Rotherham United，FOTMOB-2259503，[第47472行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:47472)。

<a id="d010"></a>
### D010

名称关系：**Rotherham / Rotherham United**（G010）；共同队伍：**莱切斯特**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-29 | Rotherham | 1:2 | 莱切斯特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-ACB86F10FD85BD85 | [第16153行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16153) |
| 2015-07-30 | Rotherham United | 1:2 | 莱切斯特 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-430565 | [第16167行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16167) |

<a id="d011"></a>
### D011

名称关系：**Peñarol / 佩纳罗尔**（G101）；共同队伍：**马拉加**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-08-01 | Peñarol | 1:3 | 马拉加 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-50522DD341166F23 | [第16347行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16347) |
| 2015-08-02 | 佩纳罗尔 | 1:3 | 马拉加 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-7540 | [第16390行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16390) |

<a id="d012"></a>
### D012

名称关系：**埃弗顿 / 西布罗姆**（G214）；共同队伍：**纽卡斯尔**。
复核层级：身份/范围风险；库内已有双方直接交锋9场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-12-27 | 纽卡斯尔 | 0:1 | 埃弗顿 | 英超 / PREMIER_LEAGUE | FOTMOB-1988885 | [第26278行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26278) |
| 2015-12-28 | 西布罗姆 | 1:0 | 纽卡斯尔 | 英超 / PREMIER_LEAGUE | FOTMOB-1988898 | [第26331行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26331) |

交锋反证示例：2015-01-20 埃弗顿 0:0 西布罗姆，FOTMOB-1724194，[第4593行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4593)。

<a id="d013"></a>
### D013

名称关系：**Ind. Santa Fe / 圣菲独立**（G089）；共同队伍：**勒沃库森**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-01-10 | 勒沃库森 | 1:0 | Ind. Santa Fe | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F8839EFA5443DE45 | [第26724行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26724) |
| 2016-01-11 | 勒沃库森 | 1:0 | 圣菲独立 | 杯赛 / CLUB_FRIENDLY | EXCEL-12885 | [第26734行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26734) |

<a id="d014"></a>
### D014

名称关系：**亚布洛 / 泽尼特**（G123）；共同队伍：**北雪平**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-02-05 | 北雪平 | 2:3 | 泽尼特 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-13571 | [第28138行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:28138) |
| 2016-02-06 | 亚布洛 | 3:2 | 北雪平 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E850B3C8033F5211 | [第28294行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:28294) |

<a id="d015"></a>
### D015

名称关系：**Floridsdorfer AC / 维尔茨堡**（G147）；共同队伍：**维也纳**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-02 | Floridsdorfer AC | 1:2 | 维也纳 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BAE203B831D33B1F | [第36744行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36744) |
| 2016-07-03 | 维也纳 | 2:1 | 维尔茨堡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D74C7C2D64E8FC0A | [第36822行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36822) |

<a id="d016"></a>
### D016

名称关系：**Forest Green / Forest Green Rovers**（G013）；共同队伍：**加的夫城**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-13 | Forest Green | 3:1 | 加的夫城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6C84ADCB69DFFC00 | [第37258行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37258) |
| 2016-07-14 | Forest Green Rovers | 3:1 | 加的夫城 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-457338 | [第37288行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37288) |

<a id="d017"></a>
### D017

名称关系：**West Bromwich / 西布罗姆**（G009）；共同队伍：**巴黎圣曼**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-13 | 巴黎圣曼 | 2:1 | West Bromwich | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E99B51337DD505A4 | [第37283行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37283) |
| 2016-07-14 | 巴黎圣曼 | 2:1 | 西布罗姆 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18064 | [第37291行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37291) |

<a id="d018"></a>
### D018

名称关系：**伯恩利 / 里斯本**（G164）；共同队伍：**Stade Nyonnais**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-14 | Stade Nyonnais | 1:3 | 里斯本 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8251C80F1B3BC3DD | [第37318行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37318) |
| 2016-07-15 | Stade Nyonnais | 1:3 | 伯恩利 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-13402A18051F91F3 | [第37341行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37341) |

<a id="d019"></a>
### D019

名称关系：**利物浦 / 曼联**（G206）；共同队伍：**Wigan Athletic**。
复核层级：身份/范围风险；库内已有双方直接交锋30场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-16 | Wigan Athletic | 0:2 | 曼联 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-458463 | [第37395行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37395) |
| 2016-07-17 | Wigan Athletic | 0:2 | 利物浦 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-458115 | [第37551行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37551) |

交锋反证示例：2014-12-14 曼联 3:0 利物浦，FOTMOB-1724138，[第3217行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:3217)。

<a id="d020"></a>
### D020

名称关系：**Cork / 科克城**（G012）；共同队伍：**伍尔弗**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-18 | 科克城 | 1:2 | 伍尔弗 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6059E0329898A9A1 | [第37667行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37667) |
| 2016-07-19 | Cork | 1:2 | 伍尔弗 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-458464 | [第37680行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37680) |

<a id="d021"></a>
### D021

名称关系：**Mansfield / Mansfield Town**（G042）；共同队伍：**赫尔城**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-19 | Mansfield | 0:1 | 赫尔城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7A9C9980E6182B73 | [第37698行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37698) |
| 2016-07-20 | Mansfield Town | 0:1 | 赫尔城 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-458468 | [第37716行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37716) |

<a id="d022"></a>
### D022

名称关系：**FC Halifax Town / Halifax**（G037）；共同队伍：**谢菲联**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-19 | Halifax | 2:0 | 谢菲联 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D6C127194AE0E13B | [第37707行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37707) |
| 2016-07-20 | FC Halifax Town | 2:0 | 谢菲联 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-458482 | [第37719行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37719) |

<a id="d023"></a>
### D023

名称关系：**Huddersfield / 哈德斯菲尔德**（G001）；共同队伍：**利物浦**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-20 | Huddersfield | 0:2 | 利物浦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9846BBFFC90AF2FB | [第37791行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37791) |
| 2016-07-21 | 哈德斯菲尔德 | 0:2 | 利物浦 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-458537 | [第37809行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37809) |

<a id="d024"></a>
### D024

名称关系：**波尔图 / 西布罗姆**（G180）；共同队伍：**维迪斯**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-22 | 维迪斯 | 1:2 | 西布罗姆 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18242 | [第37845行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37845) |
| 2016-07-23 | 维迪斯 | 1:2 | 波尔图 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18276 | [第37937行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37937) |

<a id="d025"></a>
### D025

名称关系：**GD Gafanha / 法马利康**（G190）；共同队伍：**通德拉**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-22 | GD Gafanha | 1:0 | 通德拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2E68290756BB2EBE | [第37865行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37865) |
| 2016-07-23 | 法马利康 | 1:0 | 通德拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FF3608F8E504A07D | [第38043行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38043) |

交锋反证示例：2016-07-23 GD Gafanha 0:3 法马利康，FUTBOL24-01DAE6B02860B18A，[第37971行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37971)。

<a id="d026"></a>
### D026

名称关系：**博阿维斯 / 费雷拉**（G209）；共同队伍：**吉马良斯**。
复核层级：身份/范围风险；库内已有双方直接交锋18场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-22 | 费雷拉 | 1:1 | 吉马良斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-629677C330A8A46F | [第37874行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37874) |
| 2016-07-23 | 博阿维斯 | 1:1 | 吉马良斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-04A42088BE3D9384 | [第37973行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37973) |

交锋反证示例：2014-10-25 博阿维斯 1:2 费雷拉，EXCEL-171，[第157行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157)。

<a id="d027"></a>
### D027

名称关系：**Montpellier HSC / 蒙彼利埃**（G003）；共同队伍：**克莱蒙**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-22 | Montpellier HSC | 2:1 | 克莱蒙 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9F36DFF564FC2DD6 | [第37891行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37891) |
| 2016-07-23 | 蒙彼利埃 | 2:1 | 克莱蒙 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18257 | [第37920行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37920) |

<a id="d028"></a>
### D028

名称关系：**Stade Nyonnais / 尼永**（G112）；共同队伍：**桑德兰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-25 | 桑德兰 | 2:0 | Stade Nyonnais | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-861CD80FB7C78938 | [第38187行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38187) |
| 2016-07-26 | 尼永 | 0:2 | 桑德兰 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18353 | [第38196行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38196) |

<a id="d029"></a>
### D029

名称关系：**Colchester United / 科切斯特**（G020）；共同队伍：**水晶宫**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-25 | Colchester United | 0:1 | 水晶宫 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BF6E13D536EF5CAC | [第38188行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38188) |
| 2016-07-26 | 科切斯特 | 0:1 | 水晶宫 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18350 | [第38195行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38195) |

<a id="d030"></a>
### D030

名称关系：**Huddersfield / 哈德斯菲尔德**（G001）；共同队伍：**不来梅**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-26 | 不来梅 | 0:0 | Huddersfield | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-10D151495E43AD39 | [第38202行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38202) |
| 2016-07-27 | 不来梅 | 0:0 | 哈德斯菲尔德 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18371 | [第38240行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38240) |

<a id="d031"></a>
### D031

名称关系：**Montpellier HSC / 蒙彼利埃**（G003）；共同队伍：**图卢兹**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-27 | Montpellier HSC | 0:3 | 图卢兹 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-703AA221C14B7DEE | [第38277行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38277) |
| 2016-07-28 | 蒙彼利埃 | 0:3 | 图卢兹 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18393 | [第38306行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38306) |

<a id="d032"></a>
### D032

名称关系：**Dijon FCO / 第戎**（G072）；共同队伍：**桑德兰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-27 | 桑德兰 | 3:2 | Dijon FCO | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-AEC9E377F13E2420 | [第38284行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38284) |
| 2016-07-28 | 第戎 | 2:3 | 桑德兰 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18378 | [第38297行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38297) |

<a id="d033"></a>
### D033

名称关系：**Racing Ferrol / 奥维耶多**（G197）；共同队伍：**拉科**。
复核层级：身份/范围风险；库内已有双方直接交锋2场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-29 | Racing Ferrol | 0:2 | 拉科 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3A05AA95CC6CD8BF | [第38360行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38360) |
| 2016-07-30 | 奥维耶多 | 0:2 | 拉科 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2047D2D3338F041D | [第38480行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38480) |

交锋反证示例：2022-07-20 Racing Ferrol 0:0 奥维耶多，FUTBOL24-A64D1EA3CA207E26，[第159581行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159581)。

<a id="d034"></a>
### D034

名称关系：**Huddersfield / 哈德斯菲尔德**（G001）；共同队伍：**因戈施塔**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-29 | 因戈施塔 | 0:1 | Huddersfield | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8B12821E5165929B | [第38365行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38365) |
| 2016-07-30 | 因戈施塔 | 0:1 | 哈德斯菲尔德 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18418 | [第38390行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38390) |

<a id="d035"></a>
### D035

名称关系：**Kaiserslautern / 凯泽**（G092）；共同队伍：**梅斯**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-29 | Kaiserslautern | 1:1 | 梅斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A0398DE2617091CF | [第38369行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38369) |
| 2016-07-30 | 凯泽 | 1:1 | 梅斯 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18419 | [第38391行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38391) |

<a id="d036"></a>
### D036

名称关系：**Montpellier HSC / 蒙彼利埃**（G003）；共同队伍：**桑德兰**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-30 | 桑德兰 | 1:1 | Montpellier HSC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BA8DBBEEAA495D76 | [第38506行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38506) |
| 2016-07-31 | 蒙彼利埃 | 1:1 | 桑德兰 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18472 | [第38523行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38523) |

<a id="d037"></a>
### D037

名称关系：**Caykur Rizespor / 里泽**（G033）；共同队伍：**赫尔城**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-03 | 里泽 | 1:3 | 赫尔城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F00067317935F595 | [第38758行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38758) |
| 2016-08-04 | 赫尔城 | 3:1 | Caykur Rizespor | 俱乐部赛 / CLUB_FRIENDLY | ESPN-461439 | [第38761行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38761) |

<a id="d038"></a>
### D038

名称关系：**Montpellier HSC / 蒙彼利埃**（G003）；共同队伍：**贝蒂斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-06 | Montpellier HSC | 3:0 | 贝蒂斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-54BFB3F1D43DB591 | [第38996行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38996) |
| 2016-08-07 | 蒙彼利埃 | 3:0 | 贝蒂斯 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18733 | [第39036行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39036) |

<a id="d039"></a>
### D039

名称关系：**TSV 1860 / 慕1860**（G031）；共同队伍：**因戈施塔**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-11-10 | TSV 1860 | 3:0 | 因戈施塔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4B3BDD1E2A907D6F | [第46230行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:46230) |
| 2016-11-11 | 慕1860 | 3:0 | 因戈施塔 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-22650 | [第46240行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:46240) |

<a id="d040"></a>
### D040

名称关系：**Estudiantes La Plata / 拉普大学**（G036）；共同队伍：**勒沃库森**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-08 | Estudiantes La Plata | 1:1 | 勒沃库森 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3673C527C5206C47 | [第49028行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49028) |
| 2017-01-09 | 拉普大学 | 1:1 | 勒沃库森 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-24261 | [第49051行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49051) |

<a id="d041"></a>
### D041

名称关系：**卡斯鲁厄 / 布鲁日**（G210）；共同队伍：**不来梅**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-09 | 不来梅 | 1:1 | 卡斯鲁厄 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-146E20CD447A36E2 | [第49065行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49065) |
| 2017-01-10 | 不来梅 | 1:1 | 布鲁日 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-24267 | [第49073行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49073) |

交锋反证示例：2022-01-05 布鲁日 0:0 卡斯鲁厄，FUTBOL24-7DCFE0DBDC4010F0，[第149490行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:149490)。

<a id="d042"></a>
### D042

名称关系：**SC Paderborn 07 / 帕德博恩**（G025）；共同队伍：**多特蒙德**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-17 | 帕德博恩 | 1:6 | 多特蒙德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B20A9E941F510EE5 | [第49391行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49391) |
| 2017-01-18 | SC Paderborn 07 | 1:6 | 多特蒙德 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-490276 | [第49398行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49398) |

<a id="d043"></a>
### D043

名称关系：**Mladost Lucani / ND Gorica**（G157）；共同队伍：**采列**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-02-04 | 采列 | 1:3 | ND Gorica | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CC99D3427A583B4D | [第50441行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:50441) |
| 2017-02-05 | 采列 | 1:3 | Mladost Lucani | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BBDFE490B9009DE3 | [第50549行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:50549) |

<a id="d044"></a>
### D044

名称关系：**加拉塔萨 / 特斯巴达**（G166）；共同队伍：**吉尔莫特**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-01 | 特斯巴达 | 1:0 | 吉尔莫特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B90F272DB61145EA | [第59233行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59233) |
| 2017-07-02 | 吉尔莫特 | 0:1 | 加拉塔萨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-966238F14FFA0A68 | [第59281行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59281) |

<a id="d045"></a>
### D045

名称关系：**斯拉维亚 / 莫陆军**（G177）；共同队伍：**克拉斯诺**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-08 | 克拉斯诺 | 1:1 | 斯拉维亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F182AF1372C1EFCF | [第59613行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59613) |
| 2017-07-09 | 莫陆军 | 1:1 | 克拉斯诺 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5682076FAAAB873E | [第59649行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59649) |

<a id="d046"></a>
### D046

名称关系：**Rot-Weiss Essen / 埃森**（G018）；共同队伍：**多特蒙德**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-11 | 埃森 | 3:2 | 多特蒙德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C8C1CAC5932A08FF | [第59702行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59702) |
| 2017-07-12 | Rot-Weiss Essen | 3:2 | 多特蒙德 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-490202 | [第59711行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59711) |

<a id="d047"></a>
### D047

名称关系：**南锡 / 斯托克城**（G167）；共同队伍：**亚眠**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-21 | 亚眠 | 0:1 | 南锡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B689306E9524C090 | [第60368行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60368) |
| 2017-07-22 | 亚眠 | 0:1 | 斯托克城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9C4F7CD75C3DB1DE | [第60493行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60493) |

<a id="d048"></a>
### D048

名称关系：**Hertha Berlin / 柏林赫塔**（G002）；共同队伍：**利物浦**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-29 | 柏林赫塔 | 0:3 | 利物浦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7D95A2391317E565 | [第60987行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60987) |
| 2017-07-30 | Hertha Berlin | 0:3 | 利物浦 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-489553 | [第61023行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61023) |

<a id="d049"></a>
### D049

名称关系：**巴利亚多 / 贝西克塔斯**（G223）；共同队伍：**贝蒂斯**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-29 | 贝蒂斯 | 1:1 | 巴利亚多 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CEC6892CD8222B91 | [第61010行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61010) |
| 2017-07-30 | 贝蒂斯 | 1:1 | 贝西克塔斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-584F4455E38C5B18 | [第61113行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61113) |

交锋反证示例：2017-07-25 巴利亚多 2:2 贝西克塔斯，FUTBOL24-46203C72DCD1687D，[第60699行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60699)。

<a id="d050"></a>
### D050

名称关系：**Gimnàstic Tarragona / Gimnàstic de Tarragona**（G007）；共同队伍：**巴萨**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-08-04 | Gimnàstic Tarragona | 1:1 | 巴萨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3CE1F75C9B952846 | [第61338行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61338) |
| 2017-08-05 | Gimnàstic de Tarragona | 1:1 | 巴萨 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-492041 | [第61367行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61367) |

<a id="d051"></a>
### D051

名称关系：**Guadalajara / Guadalajara Chivas**（G024）；共同队伍：**麦国民**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-09-03 | Guadalajara Chivas | 0:2 | 麦国民 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8EB382C5E178B1B1 | [第63788行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63788) |
| 2017-09-04 | Guadalajara | 0:2 | 麦国民 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-496359 | [第63789行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63789) |

<a id="d052"></a>
### D052

名称关系：**Al Ahly Cairo / 开罗国民**（G015）；共同队伍：**马竞**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-12-30 | Al Ahly Cairo | 2:3 | 马竞 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-692AE8F77E90CB74 | [第71217行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71217) |
| 2017-12-31 | 开罗国民 | 2:3 | 马竞 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-499829 | [第71218行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71218) |

<a id="d053"></a>
### D053

名称关系：**SG Sonnenhof Großaspach / Sonnenhof Großaspach**（G106）；共同队伍：**拜仁**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-01-09 | 拜仁 | 5:3 | Sonnenhof Großaspach | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-EF2A5C7ED059B07D | [第71511行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71511) |
| 2018-01-10 | 拜仁 | 5:3 | SG Sonnenhof Großaspach | 俱乐部赛 / CLUB_FRIENDLY | ESPN-502754 | [第71512行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71512) |

<a id="d054"></a>
### D054

名称关系：**Atlético Mineiro/MG / 米内罗竞技**（G023）；共同队伍：**麦国民**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-01-14 | 麦国民 | 2:0 | Atlético Mineiro/MG | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C44C06FE747C2290 | [第71799行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71799) |
| 2018-01-15 | 麦国民 | 2:0 | 米内罗竞技 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-501635 | [第71802行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71802) |

<a id="d055"></a>
### D055

名称关系：**Guayaquil City / 西班牙人**（G191）；共同队伍：**巴萨**。
复核层级：身份/范围风险；共同队名巴萨可能已被误归一，先查共同队伍的来源身份；存在跨赛事分类配对。

日期差1天；主客顺序相反；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-02-03 | 巴萨 | 1:1 | Guayaquil City | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7BA40F8CE1322B52 | [第72944行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:72944) |
| 2018-02-04 | 西班牙人 | 1:1 | 巴萨 | 西甲 / LA_LIGA | FOTMOB-2580754 | [第73034行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:73034) |

<a id="d056"></a>
### D056

名称关系：**Al Ahly Cairo / 开罗国民**（G015）；共同队伍：**阿贾克斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-11 | Al Ahly Cairo | 1:0 | 阿贾克斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C492A0D63D14C38B | [第79468行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79468) |
| 2018-05-12 | 开罗国民 | 1:0 | 阿贾克斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-510707 | [第79469行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79469) |

<a id="d057"></a>
### D057

名称关系：**Union Fürstenwalde / 开姆尼茨**（G161）；共同队伍：**柏林联合**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-04 | 开姆尼茨 | 1:3 | 柏林联合 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-56DA5C1743DDDF98 | [第81288行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81288) |
| 2018-07-05 | Union Fürstenwalde | 1:3 | 柏林联合 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B77B4795762B9DEA | [第81339行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81339) |

<a id="d058"></a>
### D058

名称关系：**Sainte Genevieve des Bois / Sainte-Geneviève**（G108）；共同队伍：**巴黎圣曼**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-11 | 巴黎圣曼 | 1:0 | Sainte-Geneviève | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9EA85332CA58274A | [第81650行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81650) |
| 2018-07-12 | 巴黎圣曼 | 1:0 | Sainte Genevieve des Bois | 俱乐部赛 / CLUB_FRIENDLY | ESPN-519126 | [第81668行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81668) |

<a id="d059"></a>
### D059

名称关系：**Austria Vienna / 维也纳**（G011）；共同队伍：**多特蒙德**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-13 | 维也纳 | 0:1 | 多特蒙德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1AC35F00208A8DFC | [第81718行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81718) |
| 2018-07-14 | Austria Vienna | 0:1 | 多特蒙德 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-513169 | [第81786行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81786) |

<a id="d060"></a>
### D060

名称关系：**Cork / 科克城**（G012）；共同队伍：**伯恩利**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-13 | 伯恩利 | 1:0 | 科克城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3A9712481C6C1C1D | [第81726行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81726) |
| 2018-07-14 | Cork | 0:1 | 伯恩利 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523294 | [第81791行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81791) |

<a id="d061"></a>
### D061

名称关系：**Lausanne Sports / Lausanne-Sport**（G014）；共同队伍：**费内巴切**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-13 | Lausanne-Sport | 2:1 | 费内巴切 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E74C339988F68D1B | [第81776行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81776) |
| 2018-07-14 | Lausanne Sports | 2:1 | 费内巴切 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-518977 | [第81788行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81788) |

<a id="d062"></a>
### D062

名称关系：**Latina / Latina Calcio**（G094）；共同队伍：**罗马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-14 | Latina Calcio | 0:9 | 罗马 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-09642605FCC7D432 | [第81827行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81827) |
| 2018-07-15 | Latina | 0:9 | 罗马 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-519130 | [第81909行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81909) |

<a id="d063"></a>
### D063

名称关系：**FC Cologne / 科隆**（G005）；共同队伍：**沃特福德**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-17 | 科隆 | 1:1 | 沃特福德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4108355594A6783A | [第82001行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82001) |
| 2018-07-18 | FC Cologne | 1:1 | 沃特福德 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523320 | [第82033行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82033) |

<a id="d064"></a>
### D064

名称关系：**St. Patrick's Athletic / 圣帕特里**（G049）；共同队伍：**纽卡斯尔**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-17 | 圣帕特里 | 0:2 | 纽卡斯尔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CAD08D8C882726AE | [第82021行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82021) |
| 2018-07-18 | St. Patrick's Athletic | 0:2 | 纽卡斯尔 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523311 | [第82032行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82032) |

<a id="d065"></a>
### D065

名称关系：**NK Lokomotiva / 萨格勒布火车头**（G016）；共同队伍：**特拉布宗**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-17 | 特拉布宗 | 1:0 | 萨格勒布火车头 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F0AE43B3E44690B5 | [第82026行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82026) |
| 2018-07-18 | 特拉布宗 | 1:0 | NK Lokomotiva | 俱乐部赛 / CLUB_FRIENDLY | ESPN-518973 | [第82030行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82030) |

<a id="d066"></a>
### D066

名称关系：**埃森 / 科隆**（G216）；共同队伍：**不来梅**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-20 | 不来梅 | 0:1 | 科隆 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7F90BDC0EA3BB35D | [第82202行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82202) |
| 2018-07-21 | 不来梅 | 0:1 | 埃森 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-50634DA09B504587 | [第82356行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82356) |

交锋反证示例：2024-01-06 埃森 4:4 科隆，FUTBOL24-9DB5FDB35F33B6DD，[第188751行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188751)。

<a id="d067"></a>
### D067

名称关系：**US Avellino / 阿韦利诺**（G118）；共同队伍：**罗马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-20 | 罗马 | 1:1 | 阿韦利诺 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-AFA03C7E02B8E266 | [第82218行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82218) |
| 2018-07-21 | 罗马 | 1:1 | US Avellino | 俱乐部赛 / CLUB_FRIENDLY | ESPN-519120 | [第82246行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82246) |

<a id="d068"></a>
### D068

名称关系：**Lausanne Sports / Lausanne-Sport**（G014）；共同队伍：**巴伦西亚**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-24 | 巴伦西亚 | 0:0 | Lausanne-Sport | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E378A1D7D2CF344E | [第82589行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82589) |
| 2018-07-25 | 巴伦西亚 | 0:0 | Lausanne Sports | 俱乐部赛 / CLUB_FRIENDLY | ESPN-520837 | [第82594行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82594) |

<a id="d069"></a>
### D069

名称关系：**Rotherham / Rotherham United**（G010）；共同队伍：**加的夫城**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-25 | Rotherham | 2:1 | 加的夫城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3C61B52162B03C8F | [第82651行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82651) |
| 2018-07-26 | Rotherham United | 2:1 | 加的夫城 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523296 | [第82693行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82693) |

<a id="d070"></a>
### D070

名称关系：**Montpellier HSC / 蒙彼利埃**（G003）；共同队伍：**比利亚雷**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-25 | Montpellier HSC | 1:1 | 比利亚雷 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3F99E071144C6B7B | [第82652行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82652) |
| 2018-07-26 | 蒙彼利埃 | 1:1 | 比利亚雷 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-520343 | [第82690行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82690) |

<a id="d071"></a>
### D071

名称关系：**Akhisar Belediyespor / 阿卡希萨尔**（G053）；共同队伍：**莱切斯特**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-25 | 莱切斯特 | 0:0 | 阿卡希萨尔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A374223CDD7EE604 | [第82672行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82672) |
| 2018-07-26 | 莱切斯特 | 0:0 | Akhisar Belediyespor | 俱乐部赛 / CLUB_FRIENDLY | ESPN-521926 | [第82692行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82692) |

<a id="d072"></a>
### D072

名称关系：**Huddersfield / 哈德斯菲尔德**（G001）；共同队伍：**里昂**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-25 | Huddersfield | 3:1 | 里昂 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F4D5E58709A833C1 | [第82687行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82687) |
| 2018-07-26 | 哈德斯菲尔德 | 3:1 | 里昂 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-521539 | [第82691行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82691) |

<a id="d073"></a>
### D073

名称关系：**Club Africain / Club Africain Tunis**（G070）；共同队伍：**加拉塔萨**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-28 | Club Africain Tunis | 0:1 | 加拉塔萨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E46D038B9AAC4BB3 | [第82930行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82930) |
| 2018-07-29 | Club Africain | 0:1 | 加拉塔萨 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-521925 | [第82940行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82940) |

<a id="d074"></a>
### D074

名称关系：**Huddersfield / 哈德斯菲尔德**（G001）；共同队伍：**博洛尼亚**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-31 | 博洛尼亚 | 1:2 | Huddersfield | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2068D59A25B77DE9 | [第83124行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83124) |
| 2018-08-01 | 博洛尼亚 | 1:2 | 哈德斯菲尔德 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523342 | [第83137行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83137) |

<a id="d075"></a>
### D075

名称关系：**Mainz / 美因茨**（G004）；共同队伍：**西汉姆联**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-31 | 西汉姆联 | 1:1 | 美因茨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C0713E49591D819A | [第83131行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83131) |
| 2018-08-01 | Mainz | 1:1 | 西汉姆联 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523410 | [第83138行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83138) |

<a id="d076"></a>
### D076

名称关系：**Hamburg SV / 汉堡**（G021）；共同队伍：**拜仁**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-08-15 | 汉堡 | 1:4 | 拜仁 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-196E780E54F5716D | [第84266行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84266) |
| 2018-08-16 | Hamburg SV | 1:4 | 拜仁 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523767 | [第84273行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84273) |

<a id="d077"></a>
### D077

名称关系：**Excelsior Virton / 勒芬**（G141）；共同队伍：**安特卫普**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-05 | 安特卫普 | 1:0 | Excelsior Virton | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-264340D8C43ED2D1 | [第93152行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93152) |
| 2019-01-06 | 安特卫普 | 1:0 | 勒芬 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BD2E00681E7A81DD | [第93209行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93209) |

<a id="d078"></a>
### D078

名称关系：**Politehnica Iasi* / 弗罗茨瓦夫**（G159）；共同队伍：**萨迪纳摩**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-22 | 萨迪纳摩 | 2:0 | Politehnica Iasi* | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-33F475DB07326D37 | [第93956行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93956) |
| 2019-01-23 | 萨迪纳摩 | 2:0 | 弗罗茨瓦夫 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4E11E08184D10268 | [第94004行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94004) |

<a id="d079"></a>
### D079

名称关系：**Akhmat Groznyi / 阿劳**（G128）；共同队伍：**布达佩斯捍卫者**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-22 | Akhmat Groznyi | 1:1 | 布达佩斯捍卫者 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DCFF3FA917542640 | [第93976行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93976) |
| 2019-01-23 | 阿劳 | 1:1 | 布达佩斯捍卫者 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F5F3B320D7F4BA71 | [第94019行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94019) |

<a id="d080"></a>
### D080

名称关系：**克拉科维亚 / 卢宾扎格勒比**（G204）；共同队伍：**伏伊伏丁**。
复核层级：身份/范围风险；库内已有双方直接交锋25场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-31 | 卢宾扎格勒比 | 0:1 | 伏伊伏丁 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-EDB74F0FAB3E7B68 | [第94509行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94509) |
| 2019-02-01 | 克拉科维亚 | 0:1 | 伏伊伏丁 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3303BEC518659DE2 | [第94522行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94522) |

交锋反证示例：2015-09-18 卢宾扎格勒比 4:2 克拉科维亚，FUTBOL24-6F8ABF523097A63B，[第19806行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:19806)。

<a id="d081"></a>
### D081

名称关系：**Sogndal / 奥德**（G160）；共同队伍：**莫陆军**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-02-06 | 莫陆军 | 3:0 | 奥德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7A8CE2EA2E314546 | [第94883行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94883) |
| 2019-02-07 | Sogndal | 0:3 | 莫陆军 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-48185DA4D08517E0 | [第94909行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94909) |

<a id="d082"></a>
### D082

名称关系：**New England Revolution / New England Revs**（G022）；共同队伍：**雷克雅**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-02-23 | New England Revs | 2:1 | 雷克雅 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D89691E4CA37AC17 | [第95863行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:95863) |
| 2019-02-24 | New England Revolution | 2:1 | 雷克雅 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-534496 | [第95869行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:95869) |

<a id="d083"></a>
### D083

名称关系：**博莱 / 布斯巴达**（G208）；共同队伍：**克卢日**。
复核层级：身份/范围风险；库内已有双方直接交锋26场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-06-28 | 克卢日 | 1:0 | 博莱 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A68BD24D4EDFEE48 | [第102395行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102395) |
| 2019-06-29 | 克卢日 | 1:0 | 布斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-41A38AFFD09D16E1 | [第102461行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102461) |

交锋反证示例：2014-10-26 布斯巴达 1:0 博莱，FOTMOB-1734548，[第392行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:392)。

<a id="d084"></a>
### D084

名称关系：**拉纳卡 / 特马利卡**（G176）；共同队伍：**扎布热矿工**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-01 | 扎布热矿工 | 1:2 | 拉纳卡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-17646CBCA3A0E7E2 | [第102592行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102592) |
| 2019-07-02 | 特马利卡 | 2:1 | 扎布热矿工 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-EC2BE766E1133C11 | [第102645行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102645) |

<a id="d085"></a>
### D085

名称关系：**克拉斯诺 / 采列**（G165）；共同队伍：**Krylia Sovetov Samara**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-06 | 采列 | 1:2 | Krylia Sovetov Samara | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D678A46510547CE8 | [第102886行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102886) |
| 2019-07-07 | 克拉斯诺 | 1:2 | Krylia Sovetov Samara | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-16EB4DE5FDFC1242 | [第102938行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102938) |

<a id="d086"></a>
### D086

名称关系：**BW林茨 / 里德**（G187）；共同队伍：**柏林联合**。
复核层级：身份/范围风险；库内已有双方直接交锋4场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 里德 | 0:3 | 柏林联合 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-50326951AC162BFA | [第103126行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103126) |
| 2019-07-13 | BW林茨 | 0:3 | 柏林联合 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4D2D261A180EA5FA | [第103248行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103248) |

交锋反证示例：2025-08-23 BW林茨 0:2 里德，FUTBOL24-EA77C2F9BDDC9D9F，[第220596行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:220596)。

<a id="d087"></a>
### D087

名称关系：**Aris / 阿里斯**（G184）；共同队伍：**埃因霍温**。
复核层级：身份/范围风险；Aris 为短名，需核对国家/城市和来源球队ID。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 埃因霍温 | 3:0 | 阿里斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7CEF852C2D5B5056 | [第103133行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103133) |
| 2019-07-13 | 埃因霍温 | 3:0 | Aris | 俱乐部赛 / CLUB_FRIENDLY | ESPN-551567 | [第103171行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103171) |

<a id="d088"></a>
### D088

名称关系：**FK Rad / OFK Bačka**（G145）；共同队伍：**托波拉**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-13 | 托波拉 | 1:1 | FK Rad | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3AB54ECD94AEE069 | [第103242行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103242) |
| 2019-07-14 | OFK Bačka | 1:1 | 托波拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C534E5633D669E35 | [第103380行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103380) |

<a id="d089"></a>
### D089

名称关系：**West Bromwich / 西布罗姆**（G009）；共同队伍：**比利亚雷**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-13 | 比利亚雷 | 3:0 | West Bromwich | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-41C898E504DD5EAB | [第103245行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103245) |
| 2019-07-14 | 比利亚雷 | 3:0 | 西布罗姆 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-547159 | [第103310行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103310) |

<a id="d090"></a>
### D090

名称关系：**Xamax / 埃弗顿**（G162）；共同队伍：**锡永**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-13 | Xamax | 0:0 | 锡永 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8A48E20A70E5E7DF | [第103260行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103260) |
| 2019-07-14 | 锡永 | 0:0 | 埃弗顿 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7B2FBEB8060611B7 | [第103367行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103367) |

<a id="d091"></a>
### D091

名称关系：**Maccabi Netanya / Stal Stalowa Wola**（G155）；共同队伍：**雅典AEK**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-15 | 雅典AEK | 1:1 | Maccabi Netanya | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4CFB8ECF531783C6 | [第103405行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103405) |
| 2019-07-16 | 雅典AEK | 1:1 | Stal Stalowa Wola | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-10B745713F412D7E | [第103422行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103422) |

<a id="d092"></a>
### D092

名称关系：**圣克拉拉 / 费雷拉**（G213）；共同队伍：**佩纳菲耶**。
复核层级：身份/范围风险；库内已有双方直接交锋12场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-16 | 佩纳菲耶 | 2:2 | 圣克拉拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2C07C627619C63AA | [第103425行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103425) |
| 2019-07-17 | 费雷拉 | 2:2 | 佩纳菲耶 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E498EF1EF6056B40 | [第103495行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103495) |

交锋反证示例：2017-07-04 费雷拉 2:0 圣克拉拉，FUTBOL24-3CE292EC74F5988C，[第59315行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59315)。

<a id="d093"></a>
### D093

名称关系：**法马利康 / 费伦斯**（G227）；共同队伍：**阿维斯**。
复核层级：身份/范围风险；库内已有双方直接交锋4场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-24 | 阿维斯 | 0:1 | 法马利康 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-113BE5EC6BD1FF17 | [第103988行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103988) |
| 2019-07-25 | 费伦斯 | 1:0 | 阿维斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-EAD19073C1330419 | [第104061行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104061) |

交锋反证示例：2015-10-01 费伦斯 2:1 法马利康，FOTMOB-2016254，[第21089行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:21089)。

<a id="d094"></a>
### D094

名称关系：**Hertha Berlin / 柏林赫塔**（G002）；共同队伍：**费内巴切**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-25 | 柏林赫塔 | 2:1 | 费内巴切 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9E19C6F44BD880C4 | [第104051行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104051) |
| 2019-07-26 | Hertha Berlin | 2:1 | 费内巴切 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-548674 | [第104064行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104064) |

<a id="d095"></a>
### D095

名称关系：**Apollon Larisa / Apollon Larissa**（G059）；共同队伍：**贝西克塔斯**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-29 | 贝西克塔斯 | 0:0 | Apollon Larisa | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FEA2039EDFD0153D | [第104417行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104417) |
| 2019-07-30 | 贝西克塔斯 | 0:0 | Apollon Larissa | 俱乐部赛 / CLUB_FRIENDLY | ESPN-552061 | [第104419行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104419) |

<a id="d096"></a>
### D096

名称关系：**Perugia Calcio / 佩鲁贾**（G017）；共同队伍：**罗马**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-31 | Perugia Calcio | 1:3 | 罗马 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3EF6E28A10553FEB | [第104481行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104481) |
| 2019-08-01 | 佩鲁贾 | 1:3 | 罗马 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-553995 | [第104510行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104510) |

<a id="d097"></a>
### D097

名称关系：**Hertha Berlin / 柏林赫塔**（G002）；共同队伍：**西汉姆联**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-31 | 柏林赫塔 | 3:5 | 西汉姆联 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A8E63036A95D171E | [第104490行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104490) |
| 2019-08-01 | Hertha Berlin | 3:5 | 西汉姆联 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-547151 | [第104509行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104509) |

<a id="d098"></a>
### D098

名称关系：**Atlético San Luis / Atlético de San Luis**（G060）；共同队伍：**马竞**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-03 | Atlético San Luis | 1:2 | 马竞 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-93809C8F66A0FA87 | [第104716行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104716) |
| 2019-08-04 | Atlético de San Luis | 1:2 | 马竞 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-540532 | [第104743行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104743) |

<a id="d099"></a>
### D099

名称关系：**Feronikeli / KF Feronikeli**（G078）；共同队伍：**AC米兰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-10 | KF Feronikeli | 0:2 | AC米兰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FAC419E11532DD87 | [第105246行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105246) |
| 2019-08-11 | Feronikeli | 0:2 | AC米兰 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-556445 | [第105252行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105252) |

<a id="d100"></a>
### D100

名称关系：**沃夫斯堡 / 首尔FC**（G225）；共同队伍：**塞尔维特**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-01-10 | 塞尔维特 | 2:1 | 沃夫斯堡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-646899DE56B9AF32 | [第113487行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113487) |
| 2020-01-11 | 塞尔维特 | 2:1 | 首尔FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-AE35194751A649CD | [第113591行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113591) |

交锋反证示例：2020-01-10 首尔FC 1:1 沃夫斯堡，FUTBOL24-4324A6ED36AC6106，[第113483行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113483)。

<a id="d101"></a>
### D101

名称关系：**Mainz / 美因茨**（G004）；共同队伍：**多特蒙德**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-01-11 | 多特蒙德 | 0:2 | 美因茨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-90ACB316C08E8A0B | [第113589行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113589) |
| 2020-01-12 | 多特蒙德 | 0:2 | Mainz | 俱乐部赛 / CLUB_FRIENDLY | ESPN-563226 | [第113605行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113605) |

<a id="d102"></a>
### D102

名称关系：**博德闪耀 / 马韦利亚**（G207）；共同队伍：**布斯巴达**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-01-30 | Marbella FC | 2:1 | 布斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-69667D79072423DF | [第114553行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:114553) |
| 2020-01-31 | 布斯巴达 | 1:2 | 博德闪耀 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-978447E62F5CAF24 | [第114578行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:114578) |

当前映射后：马韦利亚 2:1 布斯巴达；布斯巴达 1:2 博德闪耀

交锋反证示例：2021-02-27 Marbella FC 2:2 博德闪耀，FUTBOL24-8A2F003DCE19479F，[第132676行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:132676)。

<a id="d103"></a>
### D103

名称关系：**FC Rapperswil-Jona / 乌法**（G142）；共同队伍：**莫陆军**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-02-21 | 莫陆军 | 3:0 | FC Rapperswil-Jona | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0A4E5CEA6BF9804F | [第115750行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:115750) |
| 2020-02-22 | 莫陆军 | 3:0 | 乌法 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8FF00CE3F83B47EF | [第115923行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:115923) |

<a id="d104"></a>
### D104

名称关系：**RS Waasland-Beveren / Waasland-Beveren**（G104）；共同队伍：**巴黎圣曼**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-07-17 | 巴黎圣曼 | 7:0 | RS Waasland-Beveren | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CE7DF442CB811C00 | [第119360行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:119360) |
| 2020-07-18 | 巴黎圣曼 | 7:0 | Waasland-Beveren | 俱乐部赛 / CLUB_FRIENDLY | ESPN-573687 | [第119363行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:119363) |

<a id="d105"></a>
### D105

名称关系：**KFC Uerdingen 05 / SC Verl 1924**（G150）；共同队伍：**埃因霍温**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-08-08 | SC Verl 1924 | 0:3 | 埃因霍温 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-575020 | [第120345行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120345) |
| 2020-08-09 | KFC Uerdingen 05 | 0:3 | 埃因霍温 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-575021 | [第120431行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120431) |

<a id="d106"></a>
### D106

名称关系：**Krefelder FC Uerdingen / SC Verl 1924**（G093）；共同队伍：**埃因霍温**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-08-08 | SC Verl 1924 | 0:3 | 埃因霍温 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-575020 | [第120345行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120345) |
| 2020-08-09 | Krefelder FC Uerdingen | 0:3 | 埃因霍温 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B850E384F8E511A4 | [第120476行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120476) |

<a id="d107"></a>
### D107

名称关系：**KFC Uerdingen 05 / Verl**（G090）；共同队伍：**埃因霍温**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-08-08 | Verl | 0:3 | 埃因霍温 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5BB3712BE94EAEE6 | [第120400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120400) |
| 2020-08-09 | KFC Uerdingen 05 | 0:3 | 埃因霍温 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-575021 | [第120431行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120431) |

<a id="d108"></a>
### D108

名称关系：**Krefelder FC Uerdingen / Verl**（G151）；共同队伍：**埃因霍温**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-08-08 | Verl | 0:3 | 埃因霍温 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5BB3712BE94EAEE6 | [第120400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120400) |
| 2020-08-09 | Krefelder FC Uerdingen | 0:3 | 埃因霍温 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B850E384F8E511A4 | [第120476行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120476) |

<a id="d109"></a>
### D109

名称关系：**埃瓦尔 / 奥维耶多**（G217）；共同队伍：**毕尔巴鄂**。
复核层级：身份/范围风险；库内已有双方直接交锋12场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-09-04 | 奥维耶多 | 2:2 | 毕尔巴鄂 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3597A4F864CFEA5B | [第121808行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121808) |
| 2020-09-05 | 毕尔巴鄂 | 2:2 | 埃瓦尔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DAB74A123A74A653 | [第121970行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121970) |

交锋反证示例：2017-08-05 埃瓦尔 1:0 奥维耶多，FUTBOL24-EFABEDE055F98B4D，[第61515行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61515)。

<a id="d110"></a>
### D110

名称关系：**Carrarese / 卡尔皮**（G135）；共同队伍：**热那亚**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-09-09 | 热那亚 | 2:1 | 卡尔皮 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-65AC1C106DAB90CA | [第122103行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122103) |
| 2020-09-10 | 热那亚 | 2:1 | Carrarese | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7969E55663568BFC | [第122135行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122135) |

<a id="d111"></a>
### D111

名称关系：**Gimnàstic Tarragona / Gimnàstic de Tarragona**（G007）；共同队伍：**巴萨**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-09-12 | 巴萨 | 3:1 | Gimnàstic Tarragona | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6802B767EA0B7854 | [第122288行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122288) |
| 2020-09-13 | 巴萨 | 3:1 | Gimnàstic de Tarragona | 俱乐部赛 / CLUB_FRIENDLY | ESPN-583163 | [第122315行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122315) |

<a id="d112"></a>
### D112

名称关系：**埃斯比约 / 锡尔克堡**（G215）；共同队伍：**中日德兰**。
复核层级：身份/范围风险；库内已有双方直接交锋10场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-01-28 | 中日德兰 | 2:0 | 锡尔克堡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5F5D6362FC6A66B0 | [第130760行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:130760) |
| 2021-01-29 | 中日德兰 | 2:0 | 埃斯比约 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0E886E1E761CEA52 | [第130790行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:130790) |

交锋反证示例：2015-02-22 锡尔克堡 1:3 埃斯比约，FUTBOL24-65382E4ADA0615EE，[第6614行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:6614)。

<a id="d113"></a>
### D113

名称关系：**FK Liepaja / Sileks Kratovo**（G144）；共同队伍：**Arsenal Tula**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-01-30 | Arsenal Tula | 4:1 | Sileks Kratovo | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C896BEE2B39E4357 | [第130927行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:130927) |
| 2021-01-31 | Arsenal Tula | 4:1 | FK Liepaja | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-715935E07CBA77AF | [第131023行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:131023) |

<a id="d114"></a>
### D114

名称关系：**Dinamo Batumi / Valmiera FC**（G139）；共同队伍：**FC Lviv**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-02-05 | FC Lviv | 1:1 | Valmiera FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6490A58172578F2F | [第131264行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:131264) |
| 2021-02-06 | FC Lviv | 1:1 | Dinamo Batumi | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CF7D5B358DA63D93 | [第131415行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:131415) |

<a id="d115"></a>
### D115

名称关系：**克拉斯诺 / 沃尔夫斯**（G203）；共同队伍：**奥林匹亚**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-06-26 | 沃尔夫斯 | 0:1 | 奥林匹亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5AE98194EB00A8D8 | [第138828行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138828) |
| 2021-06-27 | 克拉斯诺 | 0:1 | 奥林匹亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C76F90DEA9FDCFB6 | [第138895行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138895) |

交锋反证示例：2015-07-04 克拉斯诺 1:2 沃尔夫斯，FUTBOL24-F0736EF1D04A4280，[第14561行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14561)。

<a id="d116"></a>
### D116

名称关系：**Altona 93 / 基尔**（G131）；共同队伍：**兰纳斯**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-02 | 基尔 | 1:2 | 兰纳斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-59775C72001347B7 | [第139057行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139057) |
| 2021-07-03 | Altona 93 | 1:2 | 兰纳斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A1CD6C19445B1E68 | [第139147行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139147) |

<a id="d117"></a>
### D117

名称关系：**Arsenal Tula / Spittal/Drau**（G133）；共同队伍：**布斯巴达**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-06 | Arsenal Tula | 1:4 | 布斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4D14BD0AB4E8E27C | [第139261行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139261) |
| 2021-07-07 | Spittal/Drau | 1:4 | 布斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B010E81CFFC5876C | [第139319行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139319) |

<a id="d118"></a>
### D118

名称关系：**标准列日 / 罗达JC**（G178）；共同队伍：**梅赫伦**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-09 | 梅赫伦 | 2:2 | 罗达JC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-81971B436997ABCB | [第139420行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139420) |
| 2021-07-10 | 标准列日 | 2:2 | 梅赫伦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-551F0F9DA64EBBFE | [第139495行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139495) |

<a id="d119"></a>
### D119

名称关系：**ADV Montecatini / Montecatini**（G052）；共同队伍：**罗马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-15 | 罗马 | 10:0 | ADV Montecatini | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D531CAEAE41C4B1F | [第139737行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139737) |
| 2021-07-16 | 罗马 | 10:0 | Montecatini | 俱乐部赛 / CLUB_FRIENDLY | ESPN-613953 | [第139742行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139742) |

<a id="d120"></a>
### D120

名称关系：**Chamois Niortais / SM Caen**（G136）；共同队伍：**南特**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-17 | SM Caen | 1:0 | 南特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9CAD765E1FD55271 | [第139943行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139943) |
| 2021-07-18 | 南特 | 0:1 | Chamois Niortais | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4690F40470EA960D | [第140035行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140035) |

<a id="d121"></a>
### D121

名称关系：**Ternana / Ternana Calcio**（G116）；共同队伍：**罗马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-18 | 罗马 | 2:0 | Ternana Calcio | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-ECC8F463613AEAEC | [第140049行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140049) |
| 2021-07-19 | 罗马 | 2:0 | Ternana | 俱乐部赛 / CLUB_FRIENDLY | ESPN-613954 | [第140051行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140051) |

<a id="d122"></a>
### D122

名称关系：**Gimnàstic Tarragona / Gimnàstic de Tarragona**（G007）；共同队伍：**巴萨**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-21 | 巴萨 | 4:0 | Gimnàstic Tarragona | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DD8C2F8D741455BA | [第140176行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140176) |
| 2021-07-22 | 巴萨 | 4:0 | Gimnàstic de Tarragona | 俱乐部赛 / CLUB_FRIENDLY | ESPN-609921 | [第140179行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140179) |

<a id="d123"></a>
### D123

名称关系：**UD Las Palmas / 拉帕马斯**（G006）；共同队伍：**塞维利亚**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-22 | 塞维利亚 | 1:0 | UD Las Palmas | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DD6DBF06061F177E | [第140226行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140226) |
| 2021-07-23 | 塞维利亚 | 1:0 | 拉帕马斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-615647 | [第140230行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140230) |

<a id="d124"></a>
### D124

名称关系：**Mainz / 美因茨**（G004）；共同队伍：**利物浦**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | 利物浦 | 1:0 | 美因茨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-ABBE71AA5D6179AE | [第140303行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140303) |
| 2021-07-24 | 利物浦 | 1:0 | Mainz | 俱乐部赛 / CLUB_FRIENDLY | ESPN-613951 | [第140315行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140315) |

<a id="d125"></a>
### D125

名称关系：**Debrecen / 德布勒森**（G026）；共同队伍：**罗马**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-25 | 罗马 | 5:2 | 德布勒森 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0D8EADC95C6EE5C6 | [第140525行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140525) |
| 2021-07-26 | 罗马 | 5:2 | Debrecen | 俱乐部赛 / CLUB_FRIENDLY | ESPN-613956 | [第140555行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140555) |

<a id="d126"></a>
### D126

名称关系：**Hertha Berlin / 柏林赫塔**（G002）；共同队伍：**利物浦**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-29 | 柏林赫塔 | 4:3 | 利物浦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0665430AA81129E3 | [第140715行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140715) |
| 2021-07-30 | Hertha Berlin | 4:3 | 利物浦 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-613952 | [第140732行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140732) |

<a id="d127"></a>
### D127

名称关系：**SpVgg Greuther Fürth / 菲尔特**（G048）；共同队伍：**费内巴切**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-31 | 费内巴切 | 3:2 | 菲尔特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4B6165B8F30F02B2 | [第140868行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140868) |
| 2021-08-01 | 费内巴切 | 3:2 | SpVgg Greuther Fürth | 俱乐部赛 / CLUB_FRIENDLY | ESPN-615820 | [第140934行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140934) |

<a id="d128"></a>
### D128

名称关系：**GKS Tychy / 拉多米亚克**（G148）；共同队伍：**华沙军团**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-01-28 | 华沙军团 | 4:0 | GKS Tychy | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-EE48277CB568D52D | [第150436行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:150436) |
| 2022-01-29 | 华沙军团 | 4:0 | 拉多米亚克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0851378433FE971C | [第150486行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:150486) |

<a id="d129"></a>
### D129

名称关系：**Dinamo Tbilisi / 鲁达普列**（G140）；共同队伍：**基迪纳摩**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-02-07 | 基迪纳摩 | 1:0 | 鲁达普列 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-617949CFE8CB9D93 | [第151007行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:151007) |
| 2022-02-08 | 基迪纳摩 | 1:0 | Dinamo Tbilisi | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-839001E09A5C3DBB | [第151042行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:151042) |

<a id="d130"></a>
### D130

名称关系：**哈尔科夫 / 索列夫**（G169）；共同队伍：**Veres Rivne**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-02-07 | 索列夫 | 2:1 | Veres Rivne | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A9A9A93FAC618B6E | [第151015行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:151015) |
| 2022-02-08 | 哈尔科夫 | 2:1 | Veres Rivne | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D1D91D2237147446 | [第151047行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:151047) |

<a id="d131"></a>
### D131

名称关系：**比勒费 / 萨尔茨堡**（G124）；共同队伍：**奥林匹亚**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-01 | 比勒费 | 3:1 | 奥林匹亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CDB71BC5458E0650 | [第158322行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158322) |
| 2022-07-02 | 萨尔茨堡 | 3:1 | 奥林匹亚 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-651177 | [第158331行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158331) |

<a id="d132"></a>
### D132

名称关系：**奥斯纳 / 波鸿**（G221）；共同队伍：**帕德博恩**。
复核层级：身份/范围风险；库内已有双方直接交锋5场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-08 | 帕德博恩 | 2:0 | 波鸿 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6C32A481FB8D5919 | [第158710行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158710) |
| 2022-07-09 | 帕德博恩 | 2:0 | 奥斯纳 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4CC2BE7FFE64DD70 | [第158805行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158805) |

交锋反证示例：2019-11-23 波鸿 1:1 奥斯纳，FOTMOB-3081801，[第111389行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:111389)。

<a id="d133"></a>
### D133

名称关系：**圣吉联合 / 比利亚雷**（G170）；共同队伍：**里斯本**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-13 | 里斯本 | 1:1 | 圣吉联合 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-927E1D10771BC7B2 | [第159045行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159045) |
| 2022-07-14 | 里斯本 | 1:1 | 比利亚雷 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C9173C261062EEB7 | [第159101行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159101) |

<a id="d134"></a>
### D134

名称关系：**SC Verl 1924 / Verl**（G045）；共同队伍：**多特蒙德**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-14 | Verl | 0:5 | 多特蒙德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DFC69F505370B39E | [第159103行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159103) |
| 2022-07-15 | SC Verl 1924 | 0:5 | 多特蒙德 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-645233 | [第159109行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159109) |

<a id="d135"></a>
### D135

名称关系：**Mainz / 美因茨**（G004）；共同队伍：**贝西克塔斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-15 | 贝西克塔斯 | 1:0 | 美因茨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-AEB5B036C75831B4 | [第159161行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159161) |
| 2022-07-16 | Mainz | 0:1 | 贝西克塔斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-644615 | [第159178行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159178) |

<a id="d136"></a>
### D136

名称关系：**埃门 / 奥斯纳**（G218）；共同队伍：**格罗宁根**。
复核层级：身份/范围风险；库内已有双方直接交锋4场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-16 | 奥斯纳 | 1:2 | 格罗宁根 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6E8B5EAA3E1B0305 | [第159298行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159298) |
| 2022-07-17 | 格罗宁根 | 2:1 | 埃门 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-340F8D4E4305498B | [第159424行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159424) |

交锋反证示例：2019-11-14 埃门 0:2 奥斯纳，FUTBOL24-A16138D96E142F29，[第111135行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:111135)。

<a id="d137"></a>
### D137

名称关系：**FC Cologne / 科隆**（G005）；共同队伍：**AC米兰**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-16 | 科隆 | 1:2 | AC米兰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B5D4E766401C70A8 | [第159323行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159323) |
| 2022-07-17 | FC Cologne | 1:2 | AC米兰 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-636006 | [第159352行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159352) |

<a id="d138"></a>
### D138

名称关系：**Perugia Calcio / 佩鲁贾**（G017）；共同队伍：**那不勒斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-17 | 那不勒斯 | 4:1 | Perugia Calcio | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3533C1D8D31C5634 | [第159425行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159425) |
| 2022-07-18 | 佩鲁贾 | 1:4 | 那不勒斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-649987 | [第159443行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159443) |

<a id="d139"></a>
### D139

名称关系：**FK AS Pardubice / Pardubice**（G077）；共同队伍：**加拉塔萨**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-19 | 加拉塔萨 | 0:0 | Pardubice | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-26917C3306712BD9 | [第159499行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159499) |
| 2022-07-20 | 加拉塔萨 | 0:0 | FK AS Pardubice | 俱乐部赛 / CLUB_FRIENDLY | ESPN-645743 | [第159523行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159523) |

<a id="d140"></a>
### D140

名称关系：**Hertha Berlin / 柏林赫塔**（G002）；共同队伍：**诺丁汉**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-20 | 诺丁汉 | 3:1 | 柏林赫塔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3946388ADEAEFF84 | [第159563行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159563) |
| 2022-07-21 | 诺丁汉 | 3:1 | Hertha Berlin | 俱乐部赛 / CLUB_FRIENDLY | ESPN-650125 | [第159605行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159605) |

<a id="d141"></a>
### D141

名称关系：**Sheffield Wed / 谢周三**（G029）；共同队伍：**巴列卡诺**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-20 | Sheffield Wed | 0:2 | 巴列卡诺 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8717F9AB40BB1F93 | [第159579行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159579) |
| 2022-07-21 | 谢周三 | 0:2 | 巴列卡诺 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-650124 | [第159604行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159604) |

<a id="d142"></a>
### D142

名称关系：**Canelas 2010 / Puskas FC Academy**（G067）；共同队伍：**吉马良斯**。
复核层级：单次或名称线索；存在跨赛事分类配对。

日期差1天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-22 | 吉马良斯 | 3:0 | Puskas FC Academy | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3900093 | [第159655行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159655) |
| 2022-07-23 | 吉马良斯 | 3:0 | Canelas 2010 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F8589E0FBC11FD32 | [第159871行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159871) |

<a id="d143"></a>
### D143

名称关系：**圣克拉拉 / 葡国民**（G212）；共同队伍：**马里迪莫**。
复核层级：身份/范围风险；库内已有双方直接交锋15场，不宜直接合并。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-29 | 马里迪莫 | 1:0 | 圣克拉拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-247A7F824BEAA8BB | [第160162行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160162) |
| 2022-07-30 | 葡国民 | 0:1 | 马里迪莫 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F310002007858EB8 | [第160335行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160335) |

交锋反证示例：2017-12-04 圣克拉拉 1:1 葡国民，FOTMOB-2559534，[第70077行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:70077)。

<a id="d144"></a>
### D144

名称关系：**法伦斯 / 里斯本**（G226）；共同队伍：**伍尔弗**。
复核层级：身份/范围风险；库内已有双方直接交锋10场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-30 | 里斯本 | 1:1 | 伍尔弗 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1A8BBB67833A83BB | [第160284行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160284) |
| 2022-07-31 | 法伦斯 | 1:1 | 伍尔弗 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B0BD33BDC4D99970 | [第160452行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160452) |

交锋反证示例：2020-12-20 里斯本 1:0 法伦斯，EXCEL-58074，[第129037行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:129037)。

<a id="d145"></a>
### D145

名称关系：**巴列卡诺 / 拉齐奥**（G174）；共同队伍：**巴利亚多**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-05 | 巴列卡诺 | 0:0 | 巴利亚多 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-EAA720C1A2ED745C | [第160682行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160682) |
| 2022-08-06 | 巴利亚多 | 0:0 | 拉齐奥 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-47D58527F08AA7F7 | [第160793行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160793) |

<a id="d146"></a>
### D146

名称关系：**FC Cologne / 科隆**（G005）；共同队伍：**斯图加特**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-11-19 | 科隆 | 2:4 | 斯图加特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-634CA6826BA843DB | [第167536行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:167536) |
| 2022-11-20 | FC Cologne | 2:4 | 斯图加特 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-656999 | [第167542行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:167542) |

<a id="d147"></a>
### D147

名称关系：**KVC Westerlo / 韦斯特洛**（G091）；共同队伍：**贝西克塔斯**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-12-11 | 贝西克塔斯 | 1:0 | 韦斯特洛 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F03AFB3CB6671F6B | [第168183行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:168183) |
| 2022-12-12 | 贝西克塔斯 | 1:0 | KVC Westerlo | 俱乐部赛 / CLUB_FRIENDLY | ESPN-658037 | [第168184行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:168184) |

<a id="d148"></a>
### D148

名称关系：**SD Ponferradina / 蓬费拉迪**（G105）；共同队伍：**马竞**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-12-14 | SD Ponferradina | 2:4 | 马竞 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B7A9DF1690AADC2B | [第168235行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:168235) |
| 2022-12-15 | Ponferradina | 2:4 | 马竞 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-659106 | [第168241行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:168241) |

当前映射后：SD Ponferradina 2:4 马竞；蓬费拉迪 2:4 马竞

<a id="d149"></a>
### D149

名称关系：**GS Arconatese / 里昂**（G149）；共同队伍：**蒙扎**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-12-22 | 里昂 | 1:2 | 蒙扎 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-638FAF8D7E76260F | [第168486行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:168486) |
| 2022-12-23 | 蒙扎 | 2:1 | GS Arconatese | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D181F7DF5507D29C | [第168524行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:168524) |

<a id="d150"></a>
### D150

名称关系：**CS Mioveni / 贝游击**（G134）；共同队伍：**克拉约瓦**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-01-14 | CS Mioveni | 1:1 | 克拉约瓦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-90AFF52B5FCAAAD9 | [第169308行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:169308) |
| 2023-01-15 | 克拉约瓦 | 1:1 | 贝游击 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-706F07F8CDF48019 | [第169403行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:169403) |

<a id="d151"></a>
### D151

名称关系：**Nassr/Hilal All-Stars / Riyadh All-Stars XI**（G097）；共同队伍：**巴黎圣曼**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-01-19 | Nassr/Hilal All-Stars | 4:5 | 巴黎圣曼 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-20147EE92CAC30AB | [第169532行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:169532) |
| 2023-01-20 | Riyadh All-Stars XI | 4:5 | 巴黎圣曼 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-662861 | [第169555行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:169555) |

<a id="d152"></a>
### D152

名称关系：**Mushuc Runa / 赫罗纳**（G196）；共同队伍：**巴萨**。
复核层级：身份/范围风险；共同队名巴萨可能已被误归一，先查共同队伍的来源身份；存在跨赛事分类配对。

日期差1天；主客顺序相反；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-01-28 | 赫罗纳 | 0:1 | 巴萨 | 西甲 / LA_LIGA | FOTMOB-3918123 | [第169985行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:169985) |
| 2023-01-29 | 巴萨 | 1:0 | Mushuc Runa | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6691E984DAE49396 | [第170164行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:170164) |

<a id="d153"></a>
### D153

名称关系：**亚拉腊 / 泽尼特**（G201）；共同队伍：**Ural**。
复核层级：身份/范围风险；库内已有双方直接交锋2场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-02-05 | Ural | 1:1 | 亚拉腊 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8C601FE65184A2C3 | [第170621行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:170621) |
| 2023-02-06 | Ural | 1:1 | 泽尼特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C86397E167EB2E3A | [第170674行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:170674) |

交锋反证示例：2021-02-04 泽尼特 4:0 亚拉腊，FUTBOL24-A15AB3B602C88F87，[第131235行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:131235)。

<a id="d154"></a>
### D154

名称关系：**Kvik Halden FK / 埃斯比约**（G152）；共同队伍：**腓特烈**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-02-11 | 埃斯比约 | 1:0 | 腓特烈 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-74E0065CA3C276F2 | [第170934行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:170934) |
| 2023-02-12 | 腓特烈 | 0:1 | Kvik Halden FK | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A2F93B363AFAD179 | [第171061行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:171061) |

<a id="d155"></a>
### D155

名称关系：**Red Star Belgrade / 贝红星**（G044）；共同队伍：**费内巴切**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-12 | 费内巴切 | 1:3 | 贝红星 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-07DDF1BCD2EBEA5F | [第178488行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178488) |
| 2023-07-13 | 费内巴切 | 1:3 | Red Star Belgrade | 俱乐部赛 / CLUB_FRIENDLY | ESPN-671682 | [第178534行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178534) |

<a id="d156"></a>
### D156

名称关系：**Austria Vienna / 维也纳**（G011）；共同队伍：**加拉塔萨**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-14 | 维也纳 | 1:1 | 加拉塔萨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-556C0D9481C8476E | [第178621行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178621) |
| 2023-07-15 | Austria Vienna | 1:1 | 加拉塔萨 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-675058 | [第178649行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178649) |

<a id="d157"></a>
### D157

名称关系：**First Vienna FC / 德布勒森**（G146）；共同队伍：**LASK林茨**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-14 | LASK林茨 | 4:2 | 德布勒森 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A12A971BD9AD7923 | [第178630行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178630) |
| 2023-07-15 | LASK林茨 | 4:2 | First Vienna FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5051700FBDE1C2E0 | [第178735行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178735) |

<a id="d158"></a>
### D158

名称关系：**维迪奥顿 / 采列**（G183）；共同队伍：**特拉布宗**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-15 | 维迪奥顿 | 2:2 | 特拉布宗 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-680956 | [第178654行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178654) |
| 2023-07-16 | 采列 | 2:2 | 特拉布宗 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-681656 | [第178808行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178808) |

<a id="d159"></a>
### D159

名称关系：**Mezokövesdi SE / 梅索科菲德**（G043）；共同队伍：**贝西克塔斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-18 | 贝西克塔斯 | 4:0 | 梅索科菲德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-01DBAF187ABFA5EB | [第178900行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178900) |
| 2023-07-19 | 贝西克塔斯 | 4:0 | Mezokövesdi SE | 俱乐部赛 / CLUB_FRIENDLY | ESPN-680955 | [第178928行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178928) |

<a id="d160"></a>
### D160

名称关系：**SC Rheindorf Altach / 阿尔塔奇**（G028）；共同队伍：**比利亚雷**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-18 | 阿尔塔奇 | 0:3 | 比利亚雷 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FDF180373EC1E923 | [第178925行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178925) |
| 2023-07-19 | SC Rheindorf Altach | 0:3 | 比利亚雷 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-681263 | [第178929行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178929) |

<a id="d161"></a>
### D161

名称关系：**Anadia FC / CD Trofense**（G132）；共同队伍：**阿罗卡**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-21 | 阿罗卡 | 2:1 | CD Trofense | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0911DFC52230736A | [第179085行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179085) |
| 2023-07-22 | 阿罗卡 | 2:1 | Anadia FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3A0C633FF2FC7E87 | [第179211行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179211) |

<a id="d162"></a>
### D162

名称关系：**奥斯坦德 / 瓦朗谢纳**（G172）；共同队伍：**圣吉联合**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-21 | 奥斯坦德 | 0:1 | 圣吉联合 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9D88EF283B83B182 | [第179097行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179097) |
| 2023-07-22 | 圣吉联合 | 1:0 | Valenciennes FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-38F64EC494FF5952 | [第179209行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179209) |

当前映射后：奥斯坦德 0:1 圣吉联合；圣吉联合 1:0 瓦朗谢纳

<a id="d163"></a>
### D163

名称关系：**CA Osasuna B / 莱万特**（G189）；共同队伍：**莱加内斯**。
复核层级：身份/范围风险；包含青年/预备队标识，需核对队伍级别。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-04 | 莱加内斯 | 2:0 | CA Osasuna B | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8BF84597F466EA5F | [第180103行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180103) |
| 2023-08-05 | 莱加内斯 | 2:0 | 莱万特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0C6291E7BA027CD9 | [第180191行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180191) |

<a id="d164"></a>
### D164

名称关系：**SV Darmstadt 98 / 达姆施塔特**（G046）；共同队伍：**利物浦**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-07 | 利物浦 | 3:1 | 达姆施塔特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D2CBF50A2EE814A8 | [第180411行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180411) |
| 2023-08-08 | 利物浦 | 3:1 | SV Darmstadt 98 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-682689 | [第180413行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180413) |

<a id="d165"></a>
### D165

名称关系：**Egnatia / Egnatia Rrogozhinë**（G073）；共同队伍：**国际米兰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-13 | 国际米兰 | 4:2 | Egnatia Rrogozhinë | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1BF874888B5DBA24 | [第180839行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180839) |
| 2023-08-14 | 国际米兰 | 4:2 | Egnatia | 俱乐部赛 / CLUB_FRIENDLY | ESPN-685329 | [第180865行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180865) |

<a id="d166"></a>
### D166

名称关系：**Shabab Riyadh / 利沙巴布**（G110）；共同队伍：**罗马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-01-24 | Shabab Riyadh | 1:2 | 罗马 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-ED4CEEA1FB56FEC5 | [第189619行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:189619) |
| 2024-01-25 | 利沙巴布 | 1:2 | 罗马 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-695555 | [第189621行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:189621) |

<a id="d167"></a>
### D167

名称关系：**Al Hilal Riyadh / 利雅新月**（G054）；共同队伍：**迈国际**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-01-29 | Al Hilal Riyadh | 4:3 | 迈国际 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-598A211F4095FAF8 | [第189966行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:189966) |
| 2024-01-30 | 利雅新月 | 4:3 | 迈国际 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-691204 | [第189983行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:189983) |

<a id="d168"></a>
### D168

名称关系：**ML Vitebsk / Neman Grodno**（G195）；共同队伍：**Dinamo Brest**。
复核层级：身份/范围风险；库内已有双方直接交锋1场，不宜直接合并。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-02-10 | Dinamo Brest | 0:1 | ML Vitebsk | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5F5FA6512BC9E218 | [第190690行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:190690) |
| 2024-02-11 | Dinamo Brest | 0:1 | Neman Grodno | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-081FD0443B80C6C8 | [第190802行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:190802) |

交锋反证示例：2024-02-09 Neman Grodno 0:0 ML Vitebsk，FUTBOL24-BB9E9B7EBFEB6A71，[第190567行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:190567)。

<a id="d169"></a>
### D169

名称关系：**Abdysh-Ata Kant / 阿斯塔纳**（G127）；共同队伍：**莫斯巴达**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-02-20 | 莫斯巴达 | 4:0 | Abdysh-Ata Kant | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-87B83316857F128B | [第191346行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191346) |
| 2024-02-21 | 阿斯塔纳 | 0:4 | 莫斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A7605BB3842F6F35 | [第191370行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191370) |

<a id="d170"></a>
### D170

名称关系：**Szegad-Csanád / Szeged-Csanád GA**（G114）；共同队伍：**特拉布宗**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-10 | Szeged-Csanád GA | 1:2 | 特拉布宗 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C8180FDA9C88DFC1 | [第198144行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198144) |
| 2024-07-11 | 特拉布宗 | 2:1 | Szegad-Csanád | 俱乐部赛 / CLUB_FRIENDLY | ESPN-711284 | [第198157行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198157) |

<a id="d171"></a>
### D171

名称关系：**Club Olimpia / Olimpia Asunción**（G034）；共同队伍：**河床**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-13 | 河床 | 3:1 | Olimpia Asunción | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1494D2BCED2AF550 | [第198301行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198301) |
| 2024-07-14 | 河床 | 3:1 | Club Olimpia | 俱乐部赛 / CLUB_FRIENDLY | ESPN-707341 | [第198399行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198399) |

<a id="d172"></a>
### D172

名称关系：**Antwerp / 安特卫普**（G057）；共同队伍：**帕尔马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-20 | 安特卫普 | 1:2 | 帕尔马 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C5CFC4EC269737AB | [第198807行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198807) |
| 2024-07-21 | Antwerp | 1:2 | 帕尔马 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-715539 | [第198835行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198835) |

<a id="d173"></a>
### D173

名称关系：**FC Kosice / MFK Kosice**（G076）；共同队伍：**罗马**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-22 | FC Kosice | 1:1 | 罗马 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A161F1442B82B299 | [第198921行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198921) |
| 2024-07-23 | MFK Kosice | 1:1 | 罗马 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-707618 | [第198926行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198926) |

<a id="d174"></a>
### D174

名称关系：**Shrewsbury / Shrewsbury Town**（G030）；共同队伍：**莱切斯特**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-23 | Shrewsbury | 1:2 | 莱切斯特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E10FFDBD860663FB | [第198956行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198956) |
| 2024-07-24 | Shrewsbury Town | 1:2 | 莱切斯特 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-707621 | [第198960行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198960) |

<a id="d175"></a>
### D175

名称关系：**US Lecce / 莱切**（G119）；共同队伍：**加拉塔萨**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-24 | 加拉塔萨 | 2:1 | US Lecce | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DBD1FF39D16DA888 | [第199030行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199030) |
| 2024-07-25 | 加拉塔萨 | 2:1 | 莱切 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-711234 | [第199042行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199042) |

<a id="d176"></a>
### D176

名称关系：**Rot-Weiss Essen / 埃森**（G018）；共同队伍：**勒沃库森**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-26 | 埃森 | 1:2 | 勒沃库森 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A744B0606D2CE46D | [第199147行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199147) |
| 2024-07-27 | Rot-Weiss Essen | 1:2 | 勒沃库森 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-715058 | [第199166行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199166) |

<a id="d177"></a>
### D177

名称关系：**UD Las Palmas / 拉帕马斯**（G006）；共同队伍：**国际米兰**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-27 | 国际米兰 | 3:0 | UD Las Palmas | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C677FA3475DFFFA8 | [第199310行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199310) |
| 2024-07-28 | 国际米兰 | 3:0 | 拉帕马斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-712568 | [第199342行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199342) |

<a id="d178"></a>
### D178

名称关系：**UD Las Palmas / 拉帕马斯**（G006）；共同队伍：**利物浦**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-11 | 利物浦 | 0:0 | UD Las Palmas | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2C1859B95DEB4FE9 | [第200353行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200353) |
| 2024-08-12 | 利物浦 | 0:0 | 拉帕马斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-718515 | [第200383行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200383) |

<a id="d179"></a>
### D179

名称关系：**Lions Gibraltar / 赫根**（G153）；共同队伍：**北西兰**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-02-01 | 北西兰 | 5:1 | Lions Gibraltar | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-099E12FF9C7FF29A | [第209709行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:209709) |
| 2025-02-02 | 北西兰 | 5:1 | 赫根 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-33492B6F7D1E00EF | [第209839行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:209839) |

<a id="d180"></a>
### D180

名称关系：**Aarhus Fremad / 杜保尔**（G126）；共同队伍：**里加FC**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-02-20 | 杜保尔 | 1:2 | 里加FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8AD2486E4D6DEF95 | [第210871行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:210871) |
| 2025-02-21 | Aarhus Fremad | 1:2 | 里加FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B18A5A169444D579 | [第210900行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:210900) |

<a id="d181"></a>
### D181

名称关系：**瓦路尔 / 萨普斯堡**（G181）；共同队伍：**汉坎**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-03-07 | 萨普斯堡 | 2:1 | 汉坎 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1B840A71BA0D14CB | [第211750行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:211750) |
| 2025-03-08 | 汉坎 | 1:2 | 瓦路尔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-74A5F9BA515202CC | [第211898行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:211898) |

<a id="d182"></a>
### D182

名称关系：**Miami FC / Sarasota Paradise**（G156）；共同队伍：**马姆洛迪**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-06-12 | Sarasota Paradise | 0:6 | 马姆洛迪 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D69D8CD220F64C84 | [第216634行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:216634) |
| 2025-06-13 | Miami FC | 0:6 | 马姆洛迪 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5B5031BDE37411F6 | [第216644行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:216644) |

<a id="d183"></a>
### D183

名称关系：**卢宾扎格勒比 / 特普利斯**（G168）；共同队伍：**比亚韦**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-11 | 比亚韦 | 0:1 | 卢宾扎格勒比 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-731A9AABD4C0C911 | [第217669行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217669) |
| 2025-07-12 | 比亚韦 | 0:1 | 特普利斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FA5E5C875875AB34 | [第217828行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217828) |

<a id="d184"></a>
### D184

名称关系：**沙勒罗瓦 / 瓦尔韦克**（G179）；共同队伍：**乌德勒支**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-18 | 乌德勒支 | 1:2 | 沙勒罗瓦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F869B6080D948CC3 | [第218101行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218101) |
| 2025-07-19 | 瓦尔韦克 | 2:1 | 乌德勒支 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-47B297917C7EF066 | [第218182行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218182) |

<a id="d185"></a>
### D185

名称关系：**瓦雷赫姆 / 福伦丹**（G182）；共同队伍：**诺维奇**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-19 | 福伦丹 | 1:1 | 诺维奇 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8A91AEEC99CFC86F | [第218221行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218221) |
| 2025-07-20 | 瓦雷赫姆 | 1:1 | 诺维奇 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6970E6A197198928 | [第218336行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218336) |

<a id="d186"></a>
### D186

名称关系：**法伦斯 / 费内巴切**（G125）；共同队伍：**莱里亚**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-20 | 法伦斯 | 2:0 | 莱里亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-745BDBFF1FCBB252 | [第218338行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218338) |
| 2025-07-21 | 莱里亚 | 0:2 | 费内巴切 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-750784 | [第218371行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218371) |

<a id="d187"></a>
### D187

名称关系：**SC Braga B / 雷克斯欧**（G199）；共同队伍：**阿尔维卡**。
复核层级：身份/范围风险；库内已有双方直接交锋9场，不宜直接合并；包含青年/预备队标识，需核对队伍级别。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-26 | 阿尔维卡 | 1:0 | 雷克斯欧 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DADEE2680A8BB265 | [第218755行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218755) |
| 2025-07-27 | 阿尔维卡 | 1:0 | SC Braga B | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-19377EED84C31FB7 | [第218844行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218844) |

交锋反证示例：2015-03-07 雷克斯欧 0:0 SC Braga B，FOTMOB-1752237，[第7417行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:7417)。

<a id="d188"></a>
### D188

名称关系：**AlbinoLeffe / Alcione**（G130）；共同队伍：**蒙扎**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-29 | 蒙扎 | 2:0 | Alcione | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-51F6153B8C8C7FEA | [第218930行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218930) |
| 2025-07-30 | 蒙扎 | 2:0 | AlbinoLeffe | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2A3FF9DA6DAEC0F7 | [第218948行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218948) |

<a id="d189"></a>
### D189

名称关系：**埃库莱斯 / 阿梅里亚**（G171）；共同队伍：**埃尔切**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-09 | 阿梅里亚 | 0:1 | 埃尔切 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2FCF4C2F4DB1D48F | [第219637行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219637) |
| 2025-08-10 | 埃尔切 | 1:0 | Hércules CF | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4E9A0C4C0CA48449 | [第219774行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219774) |

当前映射后：阿梅里亚 0:1 埃尔切；埃尔切 1:0 埃库莱斯

<a id="d190"></a>
### D190

名称关系：**Puskas FC Academy / Puskás**（G102）；共同队伍：**亨克**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-09 | 亨克 | 1:0 | Puskás | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-3637440408854178248 | [第228130行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228130) |
| 2026-01-10 | 亨克 | 1:0 | Puskas FC Academy | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5123296 | [第228231行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228231) |

<a id="d191"></a>
### D191

名称关系：**Puskas FC Academy / 普斯卡什学院**（G103）；共同队伍：**亨克**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-09 | 普斯卡什学院 | 0:1 | 亨克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-75B728CE49CE8F00 | [第228150行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228150) |
| 2026-01-10 | 亨克 | 1:0 | Puskas FC Academy | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5123296 | [第228231行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228231) |

<a id="d192"></a>
### D192

名称关系：**Hirnyk / Kryvbas**（G039）；共同队伍：**中日德兰**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-15 | 中日德兰 | 3:0 | Hirnyk | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8279402134613529588 | [第228448行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228448) |
| 2026-01-16 | 中日德兰 | 3:0 | Kryvbas | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5119555 | [第228514行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228514) |

<a id="d193"></a>
### D193

名称关系：**Cúcuta / Cúcuta Deportivo**（G035）；共同队伍：**飓风**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-16 | Cúcuta | 1:1 | 飓风 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-111179782490233302 | [第228497行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228497) |
| 2026-01-17 | Cúcuta Deportivo | 1:1 | 飓风 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-356987B1C7FA3C5D | [第228641行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228641) |

<a id="d194"></a>
### D194

名称关系：**UCD / 都柏林**（G117）；共同队伍：**邓多克**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-16 | UCD | 1:5 | 邓多克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-1533949345301838225 | [第228501行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228501) |
| 2026-01-17 | 都柏林 | 1:5 | 邓多克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-3D7C01CEA23AA3E1 | [第228644行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228644) |

<a id="d195"></a>
### D195

名称关系：**Hannover II / 达姆施塔特**（G193）；共同队伍：**波鸿**。
复核层级：身份/范围风险；包含青年/预备队标识，需核对队伍级别；存在跨赛事分类配对。

日期差1天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-17 | 波鸿 | 3:3 | Hannover II | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6426644785187786869 | [第228544行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228544) |
| 2026-01-18 | 波鸿 | 3:3 | 达姆施塔特 | 德乙 / CLUB_OFFICIAL_OTHER | FOTMOB-4829806 | [第228711行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228711) |

<a id="d196"></a>
### D196

名称关系：**Radomiak / 拉多米亚克**（G051）；共同队伍：**波尔蒂芒**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-19 | 波尔蒂芒 | 1:1 | Radomiak | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7125724063402551471 | [第228769行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228769) |
| 2026-01-20 | 波尔蒂芒 | 1:1 | 拉多米亚克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9F687CB07929D052 | [第228843行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228843) |

<a id="d197"></a>
### D197

名称关系：**Salt Lake / 皇家盐湖城**（G047）；共同队伍：**兰纳斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-22 | 兰纳斯 | 1:2 | Salt Lake | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8547827050003216054 | [第228899行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228899) |
| 2026-01-23 | 兰纳斯 | 1:2 | 皇家盐湖城 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5120700 | [第228949行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228949) |

<a id="d198"></a>
### D198

名称关系：**Öster / 厄斯特什**（G032）；共同队伍：**哥德堡**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-23 | Öster | 1:3 | 哥德堡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8835662279427129085 | [第228939行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228939) |
| 2026-01-24 | 哥德堡 | 3:1 | 厄斯特什 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5138359 | [第229081行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229081) |

<a id="d199"></a>
### D199

名称关系：**Sandnes / Sandnes Ulf**（G109）；共同队伍：**维京**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-30 | Sandnes | 0:2 | 维京 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-1160561582846467682 | [第229396行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229396) |
| 2026-01-31 | Sandnes Ulf | 0:2 | 维京 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5160575 | [第229547行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229547) |

<a id="d200"></a>
### D200

名称关系：**Bray / Bray Wanderers**（G062）；共同队伍：**谢尔本**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-30 | Bray | 0:3 | 谢尔本 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-1906271035182011722 | [第229397行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229397) |
| 2026-01-31 | Bray Wanderers | 0:3 | 谢尔本 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9E8A458CA6D1A3F7 | [第229580行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229580) |

<a id="d201"></a>
### D201

名称关系：**SK Traeff / Træff**（G107）；共同队伍：**莫尔德**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-02 | 莫尔德 | 4:0 | Træff | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-3113933193933670226 | [第229715行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229715) |
| 2026-02-03 | 莫尔德 | 4:0 | SK Traeff | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-17566F9FAA06FDD4 | [第229773行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229773) |

<a id="d202"></a>
### D202

名称关系：**CSKA / 莫陆军**（G185）；共同队伍：**克拉斯诺**。
复核层级：身份/范围风险；CSKA 为短名，需核对国家/城市，不能只凭缩写新增全局映射。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-03 | CSKA | 2:2 | 克拉斯诺 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4900069620584456365 | [第229757行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229757) |
| 2026-02-04 | 莫陆军 | 2:2 | 克拉斯诺 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5165813 | [第229810行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229810) |

<a id="d203"></a>
### D203

名称关系：**Guayaquil City / 阿瓦塞特**（G192）；共同队伍：**巴萨**。
复核层级：身份/范围风险；共同队名巴萨可能已被误归一，先查共同队伍的来源身份；存在跨赛事分类配对。

日期差1天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-04 | 阿瓦塞特 | 1:2 | 巴萨 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-5144838 | [第229809行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229809) |
| 2026-02-05 | Guayaquil City | 1:2 | 巴萨 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-267900D7C2A5DD37 | [第229861行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229861) |

<a id="d204"></a>
### D204

名称关系：**Zénith / 泽尼特**（G122）；共同队伍：**克拉斯诺**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-06 | 克拉斯诺 | 3:0 | Zénith | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-2741659809419284201 | [第229886行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229886) |
| 2026-02-07 | 克拉斯诺 | 3:0 | 泽尼特 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5165810 | [第230041行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230041) |

<a id="d205"></a>
### D205

名称关系：**Dinamo Batumi / FC Rustavi**（G138）；共同队伍：**奥勒克**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-12 | 奥勒克 | 1:0 | FC Rustavi | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9D4E991088C85DDB | [第230392行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230392) |
| 2026-02-13 | 奥勒克 | 1:0 | Dinamo Batumi | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2D196A73DEB15142 | [第230425行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230425) |

<a id="d206"></a>
### D206

名称关系：**Navbahor / 捷特苏**（G158）；共同队伍：**Dinamo Tbilisi**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-16 | Dinamo Tbilisi | 1:1 | Navbahor | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DFD99CA4DF32A02D | [第230758行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230758) |
| 2026-02-17 | Dinamo Tbilisi | 1:1 | 捷特苏 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A11190C857CED9A3 | [第230789行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230789) |

<a id="d207"></a>
### D207

名称关系：**GrIFK / Grankulla IFK**（G082）；共同队伍：**拉赫蒂**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-25 | 拉赫蒂 | 2:2 | GrIFK | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4689201915116724797 | [第231230行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231230) |
| 2026-02-26 | 拉赫蒂 | 2:2 | Grankulla IFK | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-06212140007E245E | [第231267行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231267) |

<a id="d208"></a>
### D208

名称关系：**特温特 / 阿尔克马**（G228）；共同队伍：**乌德勒支**。
复核层级：身份/范围风险；库内已有双方直接交锋26场，不宜直接合并。

日期差1天；主客顺序相反；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-01 | 乌德勒支 | 2:0 | 阿尔克马 | 荷甲 / EREDIVISIE | FOTMOB-4815459 | [第231478行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231478) |
| 2026-03-02 | 特温特 | 0:2 | 乌德勒支 | 荷甲 / EREDIVISIE | EXCEL-80820 | [第231587行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231587) |

交锋反证示例：2015-01-28 特温特 3:0 阿尔克马，FOTMOB-1880493，[第5029行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5029)。

<a id="d209"></a>
### D209

名称关系：**比萨 / 萨索洛**（G224）；共同队伍：**博洛尼亚**。
复核层级：身份/范围风险；库内已有双方直接交锋4场，不宜直接合并。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-02 | 萨索洛 | 0:1 | 博洛尼亚 | 意甲 / SERIE_A | EXCEL-80830 | [第231595行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231595) |
| 2026-03-03 | 比萨 | 0:1 | 博洛尼亚 | 意甲 / SERIE_A | FOTMOB-4803301 | [第231662行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231662) |

交锋反证示例：2024-12-26 比萨 3:1 萨索洛，FOTMOB-4548601，[第208101行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:208101)。

<a id="d210"></a>
### D210

名称关系：**Utsikten / Utsiktens BK**（G120）；共同队伍：**哥德堡**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-04 | Utsikten | 0:6 | 哥德堡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6319648642237548419 | [第231690行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231690) |
| 2026-03-05 | Utsiktens BK | 0:6 | 哥德堡 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5214650 | [第231756行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231756) |

<a id="d211"></a>
### D211

名称关系：**佐加顿斯 / 哈马比**（G202）；共同队伍：**布鲁马波**。
复核层级：身份/范围风险；库内已有双方直接交锋27场，不宜直接合并；存在跨赛事分类配对。

日期差1天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-08 | 佐加顿斯 | 2:0 | 布鲁马波 | 瑞典杯 / CLUB_OFFICIAL_OTHER | FOTMOB-5071567 | [第232004行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232004) |
| 2026-03-09 | 哈马比 | 2:0 | 布鲁马波 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-3524164735491565319 | [第232065行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232065) |

交锋反证示例：2015-04-14 哈马比 2:1 佐加顿斯，EXCEL-4886，[第10084行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:10084)。

<a id="d212"></a>
### D212

名称关系：**Varberg / 瓦尔贝里**（G121）；共同队伍：**盖斯**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-10 | 盖斯 | 1:2 | Varberg | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-3962589721037483651 | [第232120行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232120) |
| 2026-03-11 | 盖斯 | 1:2 | 瓦尔贝里 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5137635 | [第232168行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232168) |

<a id="d213"></a>
### D213

名称关系：**IK Oddevold / Oddevold**（G040）；共同队伍：**盖斯**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-18 | 盖斯 | 2:1 | Oddevold | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-2018616026945404342 | [第232589行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232589) |
| 2026-03-19 | 盖斯 | 2:1 | IK Oddevold | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5137636 | [第232635行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232635) |

<a id="d214"></a>
### D214

名称关系：**Győr / 杰尔**（G038）；共同队伍：**伏伊伏丁**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-24 | Győr | 2:1 | 伏伊伏丁 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-283038513849681629 | [第236677行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236677) |
| 2026-06-25 | 杰尔 | 2:1 | 伏伊伏丁 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2CE242D62E4D7ECD | [第236708行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236708) |

<a id="d215"></a>
### D215

名称关系：**Glentoran / 格伦托兰**（G027）；共同队伍：**新圣徒**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-24 | Glentoran | 1:1 | 新圣徒 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7369615683962746770 | [第236680行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236680) |
| 2026-06-25 | 格伦托兰 | 1:1 | 新圣徒 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-79CF6BD244997490 | [第236710行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236710) |

<a id="d216"></a>
### D216

名称关系：**Shkendija / 斯肯迪亚**（G019）；共同队伍：**采列**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-27 | 采列 | 1:0 | Shkendija | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4683372954973941251 | [第236753行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236753) |
| 2026-06-28 | 采列 | 1:0 | 斯肯迪亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6F190131CB1AD4A2 | [第236873行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236873) |

<a id="d217"></a>
### D217

名称关系：**CSKA 1948 / CSKA 1948 Sofia**（G008）；共同队伍：**贝游击**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-29 | CSKA 1948 | 3:0 | 贝游击 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4726207553043751904 | [第236891行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236891) |
| 2026-06-30 | CSKA 1948 Sofia | 3:0 | 贝游击 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-72D04EF16541D461 | [第236916行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236916) |

<a id="d218"></a>
### D218

名称关系：**H&W Welders / Welders**（G084）；共同队伍：**拉恩**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-29 | Welders | 1:2 | 拉恩 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7126514711216342135 | [第236892行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236892) |
| 2026-06-30 | H&W Welders | 1:2 | 拉恩 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7501E4B4BD5D97D0 | [第236917行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236917) |

<a id="d219"></a>
### D219

名称关系：**Brinje / Brinje Grosuplje**（G063）；共同队伍：**里耶卡**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-30 | Brinje | 0:3 | 里耶卡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-2580131103675369648 | [第236903行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236903) |
| 2026-07-01 | Brinje Grosuplje | 0:3 | 里耶卡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1D2E8CC703C8C40A | [第236942行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236942) |

<a id="d220"></a>
### D220

名称关系：**Shamrock / 沙姆洛克**（G111）；共同队伍：**希伯尼安**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-30 | Shamrock | 1:0 | 希伯尼安 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6067784325631270268 | [第236904行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236904) |
| 2026-07-01 | 沙姆洛克 | 1:0 | 希伯尼安 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5823109 | [第236933行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236933) |

<a id="d221"></a>
### D221

名称关系：**Forfar / Forfar Athletic**（G079）；共同队伍：**圣约翰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-30 | Forfar | 0:4 | 圣约翰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7426634738830685276 | [第236905行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236905) |
| 2026-07-01 | Forfar Athletic | 0:4 | 圣约翰 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5860645 | [第236934行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236934) |

<a id="d222"></a>
### D222

名称关系：**Shkendija / 斯肯迪亚**（G019）；共同队伍：**卢甘斯克**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-30 | Shkendija | 2:2 | 卢甘斯克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8915799358562829431 | [第236906行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236906) |
| 2026-07-01 | 斯肯迪亚 | 2:2 | 卢甘斯克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D1CD90EF0AB693D8 | [第236958行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236958) |

<a id="d223"></a>
### D223

名称关系：**CSKA 1948 / CSKA 1948 Sofia**（G008）；共同队伍：**马里博尔**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-01 | CSKA 1948 | 3:3 | 马里博尔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-1339038829544678404 | [第236924行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236924) |
| 2026-07-02 | 马里博尔 | 3:3 | CSKA 1948 Sofia | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6FBF061F638BA404 | [第236982行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236982) |

<a id="d224"></a>
### D224

名称关系：**Hajduk / 斯海杜克**（G087）；共同队伍：**采列**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-01 | 采列 | 0:1 | Hajduk | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6514063108953382617 | [第236928行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236928) |
| 2026-07-02 | 采列 | 0:1 | 斯海杜克 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B5493ABF2D0846B3 | [第236988行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236988) |

<a id="d225"></a>
### D225

名称关系：**Inter Bratislava / 国际米兰**（G194）；共同队伍：**特伦钦**。
复核层级：身份/范围风险；英文含 Bratislava，现有中文为国际米兰；优先排查来源名称误译，不能据此建立全局映射。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-03 | 特伦钦 | 0:1 | 国际米兰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6532666115089121856 | [第236999行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236999) |
| 2026-07-04 | 特伦钦 | 0:1 | Inter Bratislava | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7DDEA5C1B0BC3C10 | [第237164行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237164) |

<a id="d226"></a>
### D226

名称关系：**Mondorf / US Mondorf les Bains**（G096）；共同队伍：**梅斯**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-03 | 梅斯 | 5:2 | Mondorf | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7309454389057499403 | [第237002行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237002) |
| 2026-07-04 | 梅斯 | 5:2 | US Mondorf les Bains | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C3C9C5061A8CA513 | [第237183行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237183) |

<a id="d227"></a>
### D227

名称关系：**NK Lokomotiva / 萨格勒布火车头**（G016）；共同队伍：**贝游击**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | NK Lokomotiva | 2:2 | 贝游击 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-3193243135490954012 | [第237375行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237375) |
| 2026-07-11 | 萨格勒布火车头 | 2:2 | 贝游击 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A02E8EC884196F32 | [第237599行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237599) |

<a id="d228"></a>
### D228

名称关系：**Charlton / 查尔顿**（G068）；共同队伍：**马里博尔**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | 马里博尔 | 1:1 | Charlton | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6223899820681377637 | [第237379行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237379) |
| 2026-07-11 | 马里博尔 | 1:1 | 查尔顿 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5904573 | [第237541行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237541) |

<a id="d229"></a>
### D229

名称关系：**Stade-Lausanne / 索肖**（G113）；共同队伍：**苏黎世**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | 苏黎世 | 1:2 | 索肖 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5904569 | [第237418行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237418) |
| 2026-07-11 | 苏黎世 | 1:2 | Stade-Lausanne | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-2705976135406309109 | [第237461行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237461) |

<a id="d230"></a>
### D230

名称关系：**FC Stade Lausanne-Ouchy / 索肖**（G143）；共同队伍：**苏黎世**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | 苏黎世 | 1:2 | 索肖 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5904569 | [第237418行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237418) |
| 2026-07-11 | 苏黎世 | 1:2 | FC Stade Lausanne-Ouchy | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5905707 | [第237561行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237561) |

<a id="d231"></a>
### D231

名称关系：**FC Fleury 91 / Fleury**（G075）；共同队伍：**南特**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | Fleury | 0:5 | 南特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-2153208722003040492 | [第237457行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237457) |
| 2026-07-12 | FC Fleury 91 | 0:5 | 南特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1D7A93F5BBE60941 | [第237653行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237653) |

<a id="d232"></a>
### D232

名称关系：**GC Zurich / Grasshopper**（G080）；共同队伍：**帕纳辛纳**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | 帕纳辛纳 | 3:0 | GC Zurich | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-585632813680751454 | [第237470行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237470) |
| 2026-07-12 | 帕纳辛纳 | 3:0 | Grasshopper | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5847092 | [第237645行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237645) |

<a id="d233"></a>
### D233

名称关系：**GC Zurich / 草蜢**（G081）；共同队伍：**帕纳辛纳**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | 帕纳辛纳 | 3:0 | GC Zurich | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-585632813680751454 | [第237470行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237470) |
| 2026-07-12 | 帕纳辛纳 | 3:0 | 草蜢 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-212CDFE1D9E2D10F | [第237654行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237654) |

<a id="d234"></a>
### D234

名称关系：**Columbus / Columbus Crew**（G071）；共同队伍：**伯恩利**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | Columbus | 1:1 | 伯恩利 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7921680934037218621 | [第237622行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237622) |
| 2026-07-13 | Columbus Crew | 1:1 | 伯恩利 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5915947 | [第237681行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237681) |

<a id="d235"></a>
### D235

名称关系：**Notts / 诺茨郡**（G098）；共同队伍：**兰纳斯**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-14 | 兰纳斯 | 3:1 | Notts | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6702520266517056365 | [第237696行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237696) |
| 2026-07-15 | 兰纳斯 | 3:1 | 诺茨郡 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5748089 | [第237750行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237750) |

<a id="d236"></a>
### D236

名称关系：**H Ramat Gan / Hapoel Ramat Gan**（G083）；共同队伍：**保克什**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-14 | H Ramat Gan | 0:4 | 保克什 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8037854443371724927 | [第237698行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237698) |
| 2026-07-15 | Hapoel Ramat Gan | 0:4 | 保克什 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-167BB90D18A23688 | [第237786行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237786) |

<a id="d237"></a>
### D237

名称关系：**Orlando Pirates / Pirates**（G100）；共同队伍：**科尔多瓦**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-15 | Pirates | 1:1 | 科尔多瓦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-5555474696815246099 | [第237727行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237727) |
| 2026-07-16 | 科尔多瓦 | 1:1 | Orlando Pirates | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F236DD5FD1E0280E | [第237852行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237852) |

<a id="d238"></a>
### D238

名称关系：**Nördlingen / TSV 1861 Nördlingen**（G099）；共同队伍：**海登海姆**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-15 | Nördlingen | 0:9 | 海登海姆 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-626170263727542366 | [第237728行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237728) |
| 2026-07-16 | TSV 1861 Nördlingen | 0:9 | 海登海姆 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F427FEC88A01CAE4 | [第237853行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237853) |

<a id="d239"></a>
### D239

名称关系：**Apollon / 利阿波罗**（G058）；共同队伍：**拉纳卡**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | Apollon | 0:0 | 拉纳卡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4367879561717798419 | [第237817行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237817) |
| 2026-07-17 | 利阿波罗 | 0:0 | 拉纳卡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-AFA94B3F23CC6CE3 | [第237925行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237925) |

<a id="d240"></a>
### D240

名称关系：**布斯巴达 / 鹿斯巴达**（G186）；共同队伍：**比亚韦**。
复核层级：身份/范围风险；名称中的布/鹿不同，疑似把不同地区的 Sparta 误译到同一场；先核对来源球队ID。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | 比亚韦 | 1:0 | 鹿斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7491322527933731030 | [第237822行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237822) |
| 2026-07-17 | 比亚韦 | 1:0 | 布斯巴达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1BB1282C66C67DAD | [第237910行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237910) |

<a id="d241"></a>
### D241

名称关系：**奥地利 / 维也纳**（G220）；共同队伍：**保克什**。
复核层级：身份/范围风险；奥地利同时是现有国家队名；若为俱乐部别名，必须限制赛事/来源范围。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-17 | 奥地利 | 4:1 | 保克什 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-9198592711473818860 | [第237869行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237869) |
| 2026-07-18 | 维也纳 | 4:1 | 保克什 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-38E9C32EC2F421A4 | [第238112行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238112) |

<a id="d242"></a>
### D242

名称关系：**US Quevilly Rouen / 勒阿弗尔**（G200）；共同队伍：**亚眠**。
复核层级：身份/范围风险；库内已有双方直接交锋4场，不宜直接合并。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-17 | 勒阿弗尔 | 1:2 | 亚眠 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5900186 | [第237895行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237895) |
| 2026-07-18 | 亚眠 | 2:1 | US Quevilly Rouen | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-39B56683C5621451 | [第238113行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238113) |

交锋反证示例：2018-07-13 US Quevilly Rouen 0:0 勒阿弗尔，FUTBOL24-8055A1BBF7D3F85F，[第81751行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81751)。

<a id="d243"></a>
### D243

名称关系：**吉马良斯 / 维多利亚**（G211）；共同队伍：**科英布拉**。
复核层级：身份/范围风险；维多利亚名称含糊，需核对国家、城市和来源球队ID后确定适用范围。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | 维多利亚 | 2:0 | 科英布拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4718990763196114659 | [第237959行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237959) |
| 2026-07-19 | 吉马良斯 | 2:0 | 科英布拉 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-651291E47C40C3D6 | [第238200行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238200) |

<a id="d244"></a>
### D244

名称关系：**Honvéd / 布达佩斯捍卫者**（G088）；共同队伍：**奥西耶克**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | 奥西耶克 | 2:1 | Honvéd | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6921210628147518222 | [第237971行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237971) |
| 2026-07-19 | 奥西耶克 | 2:1 | 布达佩斯捍卫者 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-8258216C12AABDD0 | [第238203行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238203) |

<a id="d245"></a>
### D245

名称关系：**HUI / Hørsholm Usserød IK**（G085）；共同队伍：**北西兰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-21 | HUI | 2:2 | 北西兰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8131434302939075219 | [第238233行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238233) |
| 2026-07-22 | Hørsholm Usserød IK | 2:2 | 北西兰 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5904512 | [第238311行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238311) |

<a id="d246"></a>
### D246

名称关系：**HUI / Hørsholm-Usserød**（G086）；共同队伍：**北西兰**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-21 | HUI | 2:2 | 北西兰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-8131434302939075219 | [第238233行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238233) |
| 2026-07-22 | Hørsholm-Usserød | 2:2 | 北西兰 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F21BEB9E61EEF17D | [第238338行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238338) |

<a id="d247"></a>
### D247

名称关系：**Cibalia / 希巴利亚**（G069）；共同队伍：**奥西耶克**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-22 | 奥西耶克 | 3:0 | Cibalia | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-2151624015079664293 | [第238257行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238257) |
| 2026-07-23 | 奥西耶克 | 3:0 | 希巴利亚 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-03F8EFFA248EBB96 | [第238378行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238378) |

<a id="d248"></a>
### D248

名称关系：**Al-Wakrah / Wakrah**（G056）；共同队伍：**布雷斯特**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-22 | 布雷斯特 | 0:2 | Wakrah | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4219054278128280387 | [第238265行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238265) |
| 2026-07-23 | 布雷斯特 | 0:2 | Al-Wakrah | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5900183 | [第238360行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238360) |

<a id="d249"></a>
### D249

名称关系：**Al Wakra / Wakrah**（G055）；共同队伍：**布雷斯特**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-22 | 布雷斯特 | 0:2 | Wakrah | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-4219054278128280387 | [第238265行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238265) |
| 2026-07-23 | 布雷斯特 | 0:2 | Al Wakra | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6C8A1939C71775DD | [第238382行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238382) |

<a id="d250"></a>
### D250

名称关系：**Séville / 塞维利亚**（G115）；共同队伍：**科尔多瓦**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-23 | 科尔多瓦 | 0:0 | Séville | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6251022688481608940 | [第238347行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238347) |
| 2026-07-24 | 科尔多瓦 | 0:0 | 塞维利亚 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5793435 | [第238405行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238405) |

<a id="d251"></a>
### D251

名称关系：**Erminio / Giana Erminio**（G074）；共同队伍：**克雷莫纳**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-24 | 克雷莫纳 | 4:0 | Erminio | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-243914298615016193 | [第238392行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238392) |
| 2026-07-25 | 克雷莫纳 | 4:0 | Giana Erminio | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6EC6DE0CEB3F4FF6 | [第238563行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238563) |

<a id="d252"></a>
### D252

名称关系：**Brunswick / 不伦瑞克**（G065）；共同队伍：**南安普敦**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-24 | Brunswick | 0:1 | 南安普敦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6070662401871731742 | [第238398行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238398) |
| 2026-07-25 | 不伦瑞克 | 0:1 | 南安普敦 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5950687 | [第238522行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238522) |

<a id="d253"></a>
### D253

名称关系：**Brunswick / Eintr. Braunschweig**（G064）；共同队伍：**南安普敦**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-24 | Brunswick | 0:1 | 南安普敦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-6070662401871731742 | [第238398行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238398) |
| 2026-07-25 | Eintr. Braunschweig | 0:1 | 南安普敦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-508C1461C9105681 | [第238554行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238554) |

<a id="d254"></a>
### D254

名称关系：**Limavady United / Limavady Utd**（G095）；共同队伍：**林菲尔德**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-24 | Limavady Utd | 0:1 | 林菲尔德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-7888488579050705906 | [第238400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238400) |
| 2026-07-25 | Limavady United | 0:1 | 林菲尔德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-55056B629494E4ED | [第238555行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238555) |

<a id="d255"></a>
### D255

名称关系：**Bilbao / 毕尔巴鄂**（G061）；共同队伍：**埃瓦尔**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-25 | Bilbao | 2:2 | 埃瓦尔 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-5581192688261619355 | [第238464行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238464) |
| 2026-07-26 | 埃瓦尔 | 2:2 | 毕尔巴鄂 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5900301 | [第238611行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238611) |

<a id="d256"></a>
### D256

名称关系：**Cadix / 加的斯**（G066）；共同队伍：**科尔多瓦**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-25 | Cadix | 0:1 | 科尔多瓦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-5791053078504230610 | [第238466行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238466) |
| 2026-07-26 | 加的斯 | 0:1 | 科尔多瓦 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-F8A18175D334A3B1 | [第238658行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238658) |

<a id="d257"></a>
### D257

名称关系：**ŁKS Łomża / 科罗纳**（G163）；共同队伍：**比亚韦**。
复核层级：弱线索；存在跨赛事分类配对。

日期差1天；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-25 | 比亚韦 | 1:0 | 科罗纳 | 波甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-58EB1F20AFE25687 | [第238556行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238556) |
| 2026-07-26 | 比亚韦 | 1:0 | ŁKS Łomża | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-DED32649086C926E | [第238652行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238652) |

<a id="d258"></a>
### D258

名称关系：**Al Ula / 布尔萨体育**（G129）；共同队伍：**顿矿工**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差1天；主客顺序相反

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-26 | 顿矿工 | 0:0 | Al Ula | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-50E65F2D31E44AD2 | [第238629行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238629) |
| 2026-07-27 | 布尔萨体育 | 0:0 | 顿矿工 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E77667A52747FC92 | [第238680行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238680) |

<a id="d259"></a>
### D259

名称关系：**Lausanne-Sport / 洛桑**（G041）；共同队伍：**圣埃蒂安**。
复核层级：多次证据优先核对；尚无库内交锋反证；仍需人工确认身份。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-29 | 圣埃蒂安 | 4:0 | 洛桑 | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5978751 | [第238705行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238705) |
| 2026-07-30 | 圣埃蒂安 | 4:0 | Lausanne-Sport | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-7C1C7ACC52B503A4 | [第238766行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238766) |

<a id="d260"></a>
### D260

名称关系：**SC Braga B / 吉维森特**（G198）；共同队伍：**佩纳菲耶**。
复核层级：身份/范围风险；库内已有双方直接交锋9场，不宜直接合并；包含青年/预备队标识，需核对队伍级别。

日期差1天

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-08-01 | 佩纳菲耶 | 0:2 | SC Braga B | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2ED7D17E0416B609 | [第238873行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238873) |
| 2026-08-02 | 佩纳菲耶 | 0:2 | 吉维森特 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E0DF0987DE21E08A | [第239003行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:239003) |

交锋反证示例：2015-08-16 SC Braga B 1:0 吉维森特，FOTMOB-2016163，[第17641行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:17641)。

## B 双方均有别名：1对补充

<a id="b001"></a>
### B001

名称关系：奥地利 / 维也纳；Paksi SE / 保克什。

日期差1天；不同来源；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-17 | 奥地利 | 4:1 | 保克什 | 俱乐部友谊赛 / CLUB_FRIENDLY | FOOTMERCATO-9198592711473818860 | [第237869行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237869) |
| 2026-07-18 | 维也纳 | 4:1 | Paksi SE | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5943036 | [第238088行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238088) |

## S 比分不同的重点复核：21对

<a id="s001"></a>
### S001

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-02-12 | Dinamo Brest | 2:1 | Slavia Mozyr | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-38380E202D1F5989 | [第5953行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5953) |
| 2015-02-13 | Dinamo Brest | 1:2 | Slavia Mozyr | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A774A38257F99FD8 | [第5983行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5983) |

<a id="s002"></a>
### S002

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-05 | 维京 | 0:8 | 阿森纳 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A2F750968147D8CE | [第38859行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38859) |
| 2016-08-06 | 维京 | 3:4 | 阿森纳 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-18669 | [第38870行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38870) |

<a id="s003"></a>
### S003

名称关系：**Club Africain / Club Africain Tunis**（G070）；共同队伍：**巴黎圣曼**。
复核层级：单次或名称线索；尚无库内交锋反证；仍需人工确认身份。

日期差1天；主客顺序相反；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-04 | Club Africain Tunis | 0:3 | 巴黎圣曼 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-FFAFEA109A964C9A | [第48868行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48868) |
| 2017-01-05 | 巴黎圣曼 | 0:0 | Club Africain | 俱乐部赛 / CLUB_FRIENDLY | ESPN-494072 | [第48869行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48869) |

<a id="s004"></a>
### S004

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-17 | 帕德博恩 | 1:6 | 多特蒙德 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-B20A9E941F510EE5 | [第49391行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49391) |
| 2017-01-18 | 帕德博恩 | 3:4 | 多特蒙德 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-24431 | [第49400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49400) |

<a id="s005"></a>
### S005

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-28 | 不来梅 | 1:0 | 西汉姆联 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6CDC9531C902F2F4 | [第60853行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60853) |
| 2017-07-29 | 不来梅 | 2:2 | 西汉姆联 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-81CB1B103BDE6476 | [第60989行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60989) |

<a id="s006"></a>
### S006

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-20 | 莱万特 | 3:4 | 伯恩茅斯 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-16DFB33845A99D52 | [第82176行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82176) |
| 2018-07-21 | 莱万特 | 4:3 | 伯恩茅斯 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523278 | [第82248行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82248) |

<a id="s007"></a>
### S007

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-20 | Torquay United FC | 1:1 | 加的夫城 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-BA417ADDCD4CF077 | [第82220行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82220) |
| 2018-07-21 | Torquay United FC | 0:0 | 加的夫城 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-523343 | [第82254行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82254) |

<a id="s008"></a>
### S008

双方队名已一致，无需为此新增队名映射。

日期差1天；主客顺序相反；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-06-27 | 哈马比 | 3:1 | 天狼星 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5E35C99DE2320C69 | [第138889行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138889) |
| 2021-06-28 | 天狼星 | 0:2 | 哈马比 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5D15EBF7A88BC3BB | [第138919行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138919) |

<a id="s009"></a>
### S009

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-04 | 阿贾克斯 | 3:1 | 利兹联 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-617261 | [第141097行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141097) |
| 2021-08-05 | 阿贾克斯 | 4:0 | 利兹联 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-617262 | [第141164行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141164) |

<a id="s010"></a>
### S010

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-16 | 安德莱 | 2:3 | 里昂 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E10E0EA3E32FCC14 | [第159341行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159341) |
| 2022-07-17 | 安德莱 | 3:0 | 里昂 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-649602 | [第159359行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159359) |

<a id="s011"></a>
### S011

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-29 | 埃弗顿 | 3:0 | 基迪纳摩 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C3056F3C5982BCB1 | [第160174行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160174) |
| 2022-07-30 | 埃弗顿 | 4:0 | 基迪纳摩 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-650212 | [第160184行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160184) |

<a id="s012"></a>
### S012

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-29 | 乌迪内斯 | 1:3 | 切尔西 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C8812EACC985FC81 | [第160175行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160175) |
| 2022-07-30 | 乌迪内斯 | 0:2 | 切尔西 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-518A05870D4B3117 | [第160301行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160301) |

<a id="s013"></a>
### S013

名称关系：**SC Olhanense / Sanjoanense**（扩展名称线索）；共同队伍：**CF Os Belenenses**。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-10-14 | SC Olhanense | 0:2 | CF Os Belenenses | 葡萄牙杯 / CLUB_OFFICIAL_OTHER | FOTMOB-4041869 | [第165083行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:165083) |
| 2022-10-15 | Sanjoanense | 1:1 | CF Os Belenenses | 葡萄牙杯 / CLUB_OFFICIAL_OTHER | FOTMOB-4041867 | [第165221行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:165221) |

<a id="s014"></a>
### S014

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-01-27 | 竞技 | 1:3 | CS Emelec | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A9E1F53D4A4E0473 | [第169937行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:169937) |
| 2023-01-28 | 竞技 | 2:1 | CS Emelec | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2379C169615B4C1E | [第170022行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:170022) |

<a id="s015"></a>
### S015

双方队名已一致，无需为此新增队名映射。

日期差1天；主客顺序相反；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-08 | 莫陆军 | 0:3 | 莫迪纳摩 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-9021AFEB24D272C8 | [第178336行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178336) |
| 2023-07-09 | 莫迪纳摩 | 6:3 | 莫陆军 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-28CCE12B659A3698 | [第178406行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178406) |

<a id="s016"></a>
### S016

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-01-06 | 阿贾克斯 | 0:3 | 汉诺威96 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-694585 | [第188692行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188692) |
| 2024-01-07 | 阿贾克斯 | 2:1 | 汉诺威96 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-694549 | [第188764行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188764) |

<a id="s017"></a>
### S017

名称关系：**Petrocub Hîncești / 里加足校**（扩展名称线索）；共同队伍：**Pakhtakor**。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-02-19 | 里加足校 | 2:0 | Pakhtakor | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-1C5955AAEC6C8107 | [第191311行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191311) |
| 2024-02-20 | Petrocub Hîncești | 3:2 | Pakhtakor | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-E69DC00B18019C54 | [第191352行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191352) |

<a id="s018"></a>
### S018

双方队名已一致，无需为此新增队名映射。

日期差1天；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-26 | 萨迪纳摩 | 1:0 | 顿矿工 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5E2F20BEEAEF90E5 | [第199137行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199137) |
| 2024-07-27 | 萨迪纳摩 | 2:2 | 顿矿工 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-D722C2B6923A2AC3 | [第199318行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199318) |

<a id="s019"></a>
### S019

双方队名已一致，无需为此新增队名映射。

日期差1天；主客顺序相反；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-05 | 基斯华达 | 2:0 | 基迪纳摩 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-179B49B3622FF933 | [第217382行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217382) |
| 2025-07-06 | 基迪纳摩 | 3:0 | 基斯华达 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-6B95B97243DABF77 | [第217495行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217495) |

<a id="s020"></a>
### S020

双方队名已一致，无需为此新增队名映射。

日期差1天；主客顺序相反；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-01 | 奥格斯堡 | 1:3 | 水晶宫 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4F25490610E8E86B | [第219084行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219084) |
| 2025-08-02 | 水晶宫 | 0:1 | 奥格斯堡 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-751498 | [第219100行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219100) |

<a id="s021"></a>
### S021

双方队名已一致，无需为此新增队名映射。

日期差1天；不同来源；原始赛事名不同；比分冲突或短期再赛，需查原始赛果

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-09 | 布赖顿 | 2:0 | 沃夫斯堡 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-115B22326228CF07 | [第219630行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219630) |
| 2025-08-10 | 布赖顿 | 2:1 | 沃夫斯堡 | 俱乐部赛 / CLUB_FRIENDLY | ESPN-754682 | [第219671行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219671) |

## E 相隔2—7天的扩展参考：375对

<a id="e001"></a>
### E001

双方队名已一致，无需为此新增队名映射。

日期差2天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-05-23 | 格拉夫 | 1:0 | 前进之鹰 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-1969461 | [第12881行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12881) |
| 2015-05-25 | 前进之鹰 | 0:1 | 格拉夫 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-1969462 | [第13108行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:13108) |

<a id="e002"></a>
### E002

双方队名已一致，无需为此新增队名映射。

日期差2天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-11-06 | Akropolis IF | 0:1 | Hammarby TFF | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2133920 | [第23482行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:23482) |
| 2015-11-08 | Hammarby TFF | 1:0 | Akropolis IF | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2133921 | [第23809行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:23809) |

<a id="e003"></a>
### E003

双方队名已一致，无需为此新增队名映射。

日期差2天；主客顺序相反；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-10-27 | 拜仁 | 3:1 | 奥格斯堡 | 德国杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2365883 | [第45205行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:45205) |
| 2016-10-29 | 奥格斯堡 | 1:3 | 拜仁 | 德甲 / BUNDESLIGA | FOTMOB-2272375 | [第45387行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:45387) |

<a id="e004"></a>
### E004

双方队名已一致，无需为此新增队名映射。

日期差2天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-11-24 | Nordic United FC | 0:1 | 厄格里特 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4362877 | [第186738行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:186738) |
| 2023-11-26 | 厄格里特 | 1:0 | Nordic United FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4362878 | [第186968行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:186968) |

<a id="e005"></a>
### E005

双方队名已一致，无需为此新增队名映射。

日期差2天；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-12-10 | 格拉纳达 | 1:1 | 毕尔巴鄂 | 西甲 / LA_LIGA | EXCEL-70654 | [第187774行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:187774) |
| 2023-12-12 | 格拉纳达 | 1:1 | 毕尔巴鄂 | 西甲 / LA_LIGA | FOTMOB-4205500 | [第187908行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:187908) |

<a id="e006"></a>
### E006

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-04-06 | 新佩斯 | 0:1 | 维迪奥顿 | 匈甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-746EDA751F430353 | [第9516行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:9516) |
| 2015-04-09 | 维迪奥顿 | 1:0 | 新佩斯 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-CB18D74DA2331DD2 | [第9664行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:9664) |

<a id="e007"></a>
### E007

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-04-29 | Gandzasar Kapan | 0:0 | Ulisses Yerevan | 亚美尼超 / CLUB_OFFICIAL_OTHER | FUTBOL24-BBDA7B6B9BC05FCA | [第11232行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:11232) |
| 2015-05-02 | Ulisses Yerevan | 0:0 | Gandzasar Kapan | 亚美尼超 / CLUB_OFFICIAL_OTHER | FUTBOL24-5619037466676504 | [第11459行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:11459) |

<a id="e008"></a>
### E008

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-01-10 | 加拉塔萨 | 3:1 | Karşıyaka SK | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-994E11459650F121 | [第26717行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26717) |
| 2016-01-13 | Karşıyaka SK | 1:3 | 加拉塔萨 | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-AD2F60200B1D5CBA | [第26822行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26822) |

<a id="e009"></a>
### E009

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-01-20 | 开塞利体育 | 0:1 | 科尼亚 | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-4FF40083B061A116 | [第27235行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:27235) |
| 2016-01-23 | 科尼亚 | 1:0 | 开塞利体育 | 土超 / CLUB_OFFICIAL_OTHER | FUTBOL24-089A56C6A0E35185 | [第27431行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:27431) |

<a id="e010"></a>
### E010

双方队名已一致，无需为此新增队名映射。

日期差3天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-02-04 | 布鲁日 | 1:0 | 根特 | 比利时杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2150256 | [第28120行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:28120) |
| 2016-02-07 | 布鲁日 | 1:0 | 根特 | 比甲 / CLUB_OFFICIAL_OTHER | FOTMOB-1980016 | [第28320行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:28320) |

<a id="e011"></a>
### E011

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-23 | Panionios | 0:2 | 塞萨洛 | 希超 / CLUB_OFFICIAL_OTHER | FOTMOB-2223858 | [第35565行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35565) |
| 2016-05-26 | 塞萨洛 | 2:0 | Panionios | 希超 / CLUB_OFFICIAL_OTHER | FOTMOB-2223860 | [第35640行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35640) |

<a id="e012"></a>
### E012

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-30 | 希贝尼克 | 1:1 | 伊斯特拉1961 | 克甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-4BC200FEAD3F8C9F | [第35839行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35839) |
| 2016-06-02 | 伊斯特拉1961 | 1:1 | 希贝尼克 | 克甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-80EC471A79D6D1D5 | [第35879行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35879) |

<a id="e013"></a>
### E013

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-11-27 | CF Os Belenenses | 0:0 | 波尔图 | 葡超 / PRIMEIRA_LIGA | EXCEL-23162 | [第47070行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:47070) |
| 2016-11-30 | 波尔图 | 0:0 | CF Os Belenenses | 葡联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2406815 | [第47307行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:47307) |

<a id="e014"></a>
### E014

双方队名已一致，无需为此新增队名映射。

日期差3天；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-08 | 吉马良斯 | 0:2 | 本菲卡 | 葡超 / PRIMEIRA_LIGA | EXCEL-24219 | [第48980行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48980) |
| 2017-01-11 | 吉马良斯 | 0:2 | 本菲卡 | 葡联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2406832 | [第49099行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49099) |

<a id="e015"></a>
### E015

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-18 | 格罗宁根 | 1:4 | 阿尔克马 | 荷甲 / EREDIVISIE | FOTMOB-2499203 | [第57476行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57476) |
| 2017-05-21 | 阿尔克马 | 4:1 | 格罗宁根 | 荷甲 / EREDIVISIE | FOTMOB-2499204 | [第57797行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57797) |

<a id="e016"></a>
### E016

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-19 | 托卢卡 | 1:1 | 瓜达拉 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-28572 | [第57498行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57498) |
| 2017-05-22 | 瓜达拉 | 1:1 | 托卢卡 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-28756 | [第57839行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57839) |

<a id="e017"></a>
### E017

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-19 | 老虎大学 | 2:0 | 蒂华纳 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-28573 | [第57499行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57499) |
| 2017-05-22 | 蒂华纳 | 0:2 | 老虎大学 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-28758 | [第57841行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57841) |

<a id="e018"></a>
### E018

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-21 | 塞萨洛 | 1:0 | Panionios | 希超 / CLUB_OFFICIAL_OTHER | FOTMOB-2497082 | [第57793行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57793) |
| 2017-05-24 | Panionios | 0:1 | 塞萨洛 | 希超 / CLUB_OFFICIAL_OTHER | FOTMOB-2497084 | [第57932行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57932) |

<a id="e019"></a>
### E019

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-08-30 | 库里科联 | 2:1 | 巴勒斯人 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-31165 | [第63533行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63533) |
| 2017-09-02 | 巴勒斯人 | 1:2 | 库里科联 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-31247 | [第63704行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63704) |

<a id="e020"></a>
### E020

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-09-01 | 奥伊金斯 | 0:2 | 圣漫步者 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-31189 | [第63634行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63634) |
| 2017-09-04 | 圣漫步者 | 2:0 | 奥伊金斯 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-31285 | [第63800行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63800) |

<a id="e021"></a>
### E021

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-11-23 | 莱昂 | 1:1 | 老虎大学 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-34291 | [第69215行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69215) |
| 2017-11-26 | 老虎大学 | 1:1 | 莱昂 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-34434 | [第69425行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69425) |

<a id="e022"></a>
### E022

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-11-24 | 蓝十字 | 0:0 | 墨美洲 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-34318 | [第69243行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69243) |
| 2017-11-27 | 墨美洲 | 0:0 | 蓝十字 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-34506 | [第69560行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69560) |

<a id="e023"></a>
### E023

双方队名已一致，无需为此新增队名映射。

日期差3天；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-02-26 | 巴黎圣曼 | 3:0 | 马赛 | 法甲 / LIGUE_1 | FOTMOB-2525355 | [第74385行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:74385) |
| 2018-03-01 | 巴黎圣曼 | 3:0 | 马赛 | 法国杯 / CLUB_OFFICIAL_OTHER | EXCEL-36901 | [第74486行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:74486) |

<a id="e024"></a>
### E024

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-04 | 马萨特兰 | 2:2 | 托卢卡 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-39284 | [第78921行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:78921) |
| 2018-05-07 | 托卢卡 | 2:2 | 马萨特兰 | 墨超 / CLUB_OFFICIAL_OTHER | EXCEL-39476 | [第79279行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79279) |

<a id="e025"></a>
### E025

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-11 | 查尔顿 | 0:1 | 什鲁斯 | 英甲 / CLUB_OFFICIAL_OTHER | EXCEL-39560 | [第79450行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79450) |
| 2018-05-14 | 什鲁斯 | 1:0 | 查尔顿 | 英甲 / CLUB_OFFICIAL_OTHER | EXCEL-39718 | [第79758行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79758) |

<a id="e026"></a>
### E026

名称关系：**Shrewsbury Town / 什鲁斯**（扩展名称线索）；共同队伍：**查尔顿**。

日期差3天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-11 | 查尔顿 | 0:1 | 什鲁斯 | 英甲 / CLUB_OFFICIAL_OTHER | EXCEL-39560 | [第79450行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79450) |
| 2018-05-14 | Shrewsbury Town | 1:0 | 查尔顿 | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-2757214 | [第79811行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79811) |

<a id="e027"></a>
### E027

名称关系：**Shrewsbury Town / 什鲁斯**（扩展名称线索）；共同队伍：**查尔顿**。

日期差3天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-11 | 查尔顿 | 0:1 | Shrewsbury Town | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-2757213 | [第79461行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79461) |
| 2018-05-14 | 什鲁斯 | 1:0 | 查尔顿 | 英甲 / CLUB_OFFICIAL_OTHER | EXCEL-39718 | [第79758行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79758) |

<a id="e028"></a>
### E028

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-11 | 查尔顿 | 0:1 | Shrewsbury Town | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-2757213 | [第79461行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79461) |
| 2018-05-14 | Shrewsbury Town | 1:0 | 查尔顿 | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-2757214 | [第79811行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79811) |

<a id="e029"></a>
### E029

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-18 | 灵比 | 1:2 | 兰纳斯 | 丹超 / CLUB_OFFICIAL_OTHER | FUTBOL24-483FE673C2E7B49C | [第79935行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79935) |
| 2018-05-21 | 兰纳斯 | 2:1 | 灵比 | 丹超 / CLUB_OFFICIAL_OTHER | FUTBOL24-BC7A6DD592900894 | [第80190行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:80190) |

<a id="e030"></a>
### E030

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；不同来源；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-09-24 | 博德闪耀 | 1:2 | 斯达 | 挪超 / CLUB_OFFICIAL_OTHER | FOTMOB-2684362 | [第87126行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:87126) |
| 2018-09-27 | 斯达 | 2:1 | 博德闪耀 | 挪威杯 / CLUB_OFFICIAL_OTHER | EXCEL-42711 | [第87332行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:87332) |

<a id="e031"></a>
### E031

双方队名已一致，无需为此新增队名映射。

日期差3天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-10-29 | 纽约城 | 3:1 | 费城 | 美职足 / CLUB_OFFICIAL_OTHER | EXCEL-43957 | [第89545行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:89545) |
| 2018-11-01 | 纽约城 | 3:1 | 费城 | 美职足 / CLUB_OFFICIAL_OTHER | EXCEL-44032 | [第89744行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:89744) |

<a id="e032"></a>
### E032

双方队名已一致，无需为此新增队名映射。

日期差3天；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-16 | 吉马良斯 | 0:1 | 本菲卡 | 葡萄牙杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2951411 | [第93584行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93584) |
| 2019-01-19 | 吉马良斯 | 0:1 | 本菲卡 | 葡超 / PRIMEIRA_LIGA | EXCEL-46188 | [第93670行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93670) |

<a id="e033"></a>
### E033

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-31 | 凯尔特人 | 2:0 | 圣约翰 | 苏超 / CLUB_OFFICIAL_OTHER | EXCEL-46546 | [第94467行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94467) |
| 2019-02-03 | 圣约翰 | 0:2 | 凯尔特人 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-BE06250D80981758 | [第94789行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94789) |

<a id="e034"></a>
### E034

名称关系：**FK Rad / OFK Bačka**（G145）；共同队伍：**采列**。
复核层级：弱线索；仅日期/比分相同，身份依据不足。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-02-03 | OFK Bačka | 0:1 | 采列 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-5E8DB98D45F0ECCB | [第94786行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94786) |
| 2019-02-06 | 采列 | 1:0 | FK Rad | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-0F53EF0FE8A25375 | [第94875行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94875) |

<a id="e035"></a>
### E035

双方队名已一致，无需为此新增队名映射。

日期差3天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-03-10 | 德布勒森 | 1:0 | 梅索科菲德 | 匈甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-96087F71E7234991 | [第96867行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:96867) |
| 2019-03-13 | 德布勒森 | 1:0 | 梅索科菲德 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-28D9FE0612E05E2A | [第97018行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:97018) |

<a id="e036"></a>
### E036

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-05-19 | Vereya | 0:3 | Vitosha Bistritsa | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-3042133 | [第101212行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:101212) |
| 2019-05-22 | Vitosha Bistritsa | 3:0 | Vereya | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-3042134 | [第101360行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:101360) |

<a id="e037"></a>
### E037

双方队名已一致，无需为此新增队名映射。

日期差3天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-03-05 | 保克什 | 0:0 | 布达佩斯捍卫者 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-24AAAC571D9F0119 | [第116531行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:116531) |
| 2020-03-08 | 保克什 | 0:0 | 布达佩斯捍卫者 | 匈甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-17A0BB08B42E836C | [第116790行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:116790) |

<a id="e038"></a>
### E038

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-05-24 | 布达佩斯MTK | 0:0 | 布达佩斯捍卫者 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-CFC69A3ABD12F94B | [第117104行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117104) |
| 2020-05-27 | 布达佩斯捍卫者 | 0:0 | 布达佩斯MTK | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-2CD5CF3E081010CA | [第117136行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117136) |

<a id="e039"></a>
### E039

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-06-17 | 默德林 | 1:1 | 阿尔塔奇 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-313893020F066D83 | [第117746行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117746) |
| 2020-06-20 | 阿尔塔奇 | 1:1 | 默德林 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-08A914AAD464D585 | [第117902行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117902) |

<a id="e040"></a>
### E040

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-07-04 | 朴次茅斯 | 1:1 | 牛津联 | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3346176 | [第118661行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:118661) |
| 2020-07-07 | 牛津联 | 1:1 | 朴次茅斯 | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3346177 | [第118824行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:118824) |

<a id="e041"></a>
### E041

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-09-14 | 迪拜阿沙 | 1:0 | 沙赫尔 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-55517 | [第122487行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122487) |
| 2020-09-17 | 沙赫尔 | 0:1 | 迪拜阿沙 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-55587 | [第122635行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122635) |

<a id="e042"></a>
### E042

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-09-16 | 波斯波利 | 1:0 | 塔亚文 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-55557 | [第122576行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122576) |
| 2020-09-19 | 塔亚文 | 0:1 | 波斯波利 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-55625 | [第122730行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122730) |

<a id="e043"></a>
### E043

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-09-16 | 塞帕汉 | 0:2 | 利雅胜利 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-55558 | [第122577行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122577) |
| 2020-09-19 | 利雅胜利 | 2:0 | 塞帕汉 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-55626 | [第122731行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:122731) |

<a id="e044"></a>
### E044

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-12-10 | 布鲁马波 | 1:1 | 特雷勒堡 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-3493895 | [第128307行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:128307) |
| 2020-12-13 | 特雷勒堡 | 1:1 | 布鲁马波 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-3493897 | [第128575行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:128575) |

<a id="e045"></a>
### E045

双方队名已一致，无需为此新增队名映射。

日期差3天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-04-18 | 贝红星 | 2:1 | Radnik | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-3367289 | [第135563行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:135563) |
| 2021-04-21 | 贝红星 | 2:1 | Radnik | 塞杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3571029 | [第135818行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:135818) |

<a id="e046"></a>
### E046

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-06-30 | 广州恒大 | 0:1 | 香港杰志 | 亚冠精英 / CLUB_OFFICIAL_OTHER | FOTMOB-3539531 | [第138960行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138960) |
| 2021-07-03 | 香港杰志 | 1:0 | 广州恒大 | 亚冠精英 / CLUB_OFFICIAL_OTHER | FOTMOB-3539532 | [第139106行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139106) |

<a id="e047"></a>
### E047

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-02 | 清莱联 | 1:1 | 大阪钢巴 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-60973 | [第139043行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139043) |
| 2021-07-05 | 大阪钢巴 | 1:1 | 清莱联 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-61038 | [第139221行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139221) |

<a id="e048"></a>
### E048

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-05-11 | 布雷达 | 1:2 | 海牙 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3872780 | [第156594行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:156594) |
| 2022-05-14 | 海牙 | 2:1 | 布雷达 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3872781 | [第156739行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:156739) |

<a id="e049"></a>
### E049

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-05-11 | 因弗内斯 | 0:0 | 阿布罗斯 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-49863CDBBEAF3D18 | [第156597行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:156597) |
| 2022-05-14 | 阿布罗斯 | 0:0 | 因弗内斯 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-F4A51FA577F70D1E | [第156770行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:156770) |

<a id="e050"></a>
### E050

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-05-18 | 海牙 | 2:1 | 埃因FC | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3876540 | [第157017行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157017) |
| 2022-05-21 | 埃因FC | 1:2 | 海牙 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3876541 | [第157114行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157114) |

<a id="e051"></a>
### E051

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-05-27 | 欧塞尔 | 1:1 | 圣埃蒂安 | 法甲 / LIGUE_1 | EXCEL-64512 | [第157391行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157391) |
| 2022-05-30 | 圣埃蒂安 | 1:1 | 欧塞尔 | 法甲 / LIGUE_1 | EXCEL-64558 | [第157488行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157488) |

<a id="e052"></a>
### E052

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-05-31 | 芬洛 | 1:1 | 阿尔梅勒 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4179993 | [第177233行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:177233) |
| 2023-06-03 | 阿尔梅勒 | 1:1 | 芬洛 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4179994 | [第177325行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:177325) |

<a id="e053"></a>
### E053

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-11-02 | 洪卡 | 0:1 | 瓦萨 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-CA2B5A812544664D | [第185761行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:185761) |
| 2023-11-05 | 瓦萨 | 1:0 | 洪卡 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-9B24BC223E743BC0 | [第186065行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:186065) |

<a id="e054"></a>
### E054

双方队名已一致，无需为此新增队名映射。

日期差3天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-11-06 | 泽姆匹林米哈洛夫采 | 1:1 | Zilina | 斯洛伐杯 / CLUB_OFFICIAL_OTHER | FOTMOB-4663687 | [第205691行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:205691) |
| 2024-11-09 | 泽姆匹林米哈洛夫采 | 1:1 | Zilina | 斯洛伐超 / CLUB_OFFICIAL_OTHER | FOTMOB-4514794 | [第205821行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:205821) |

<a id="e055"></a>
### E055

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-03-21 | 保加利亚 | 1:2 | 爱尔兰 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679411 | [第212598行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212598) |
| 2025-03-24 | 爱尔兰 | 2:1 | 保加利亚 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679412 | [第212711行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212711) |

<a id="e056"></a>
### E056

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-03-21 | 斯洛伐克 | 0:0 | 斯洛文尼 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679415 | [第212600行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212600) |
| 2025-03-24 | 斯洛文尼 | 0:0 | 斯洛伐克 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679416 | [第212712行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212712) |

<a id="e057"></a>
### E057

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-03-21 | 荷兰 | 2:2 | 西班牙 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679441 | [第212605行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212605) |
| 2025-03-24 | 西班牙 | 2:2 | 荷兰 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679442 | [第212717行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212717) |

<a id="e058"></a>
### E058

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-05-14 | 帕尔蒂克 | 0:2 | 利文斯顿 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-9F440AF80A6740A1 | [第215665行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:215665) |
| 2025-05-17 | 利文斯顿 | 2:0 | 帕尔蒂克 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-54754D312253ED95 | [第215822行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:215822) |

<a id="e059"></a>
### E059

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-12-04 | 卡辛巴西卡 | 1:3 | 杰尔 | 匈甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-842211C3E0EB319F | [第226620行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226620) |
| 2025-12-07 | 杰尔 | 3:1 | 卡辛巴西卡 | 匈甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-3D01EB5D7D7C1398 | [第226884行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226884) |

<a id="e060"></a>
### E060

双方队名已一致，无需为此新增队名映射。

日期差3天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-25 | Botev Vratsa | 1:1 | Vejle Boldklub | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5123367 | [第229208行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229208) |
| 2026-01-28 | Botev Vratsa | 1:1 | Vejle Boldklub | 俱乐部赛 / CLUB_FRIENDLY | FOTMOB-5159299 | [第229332行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229332) |

<a id="e061"></a>
### E061

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-11 | 索菲亚中央陆军 | 2:0 | CSKA 1948 | 保杯 / CLUB_OFFICIAL_OTHER | FOTMOB-5102913 | [第230334行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230334) |
| 2026-02-14 | CSKA 1948 | 0:2 | 索菲亚中央陆军 | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-4867606 | [第230512行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230512) |

<a id="e062"></a>
### E062

双方队名已一致，无需为此新增队名映射。

日期差3天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-04-29 | 罗达JC | 1:1 | 瓦尔韦克 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-5406264 | [第234926行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234926) |
| 2026-05-02 | 瓦尔韦克 | 1:1 | 罗达JC | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-5406265 | [第235068行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:235068) |

<a id="e063"></a>
### E063

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2014-11-29 | 伏伊伏丁 | 1:2 | Cukaricki | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-1727432 | [第2308行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:2308) |
| 2014-12-03 | Cukaricki | 2:1 | 伏伊伏丁 | 塞杯 / CLUB_OFFICIAL_OTHER | FOTMOB-1867259 | [第2614行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:2614) |

<a id="e064"></a>
### E064

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-01-18 | 沙勒罗瓦 | 0:2 | 色格拉 | 比甲 / CLUB_OFFICIAL_OTHER | FOTMOB-1717988 | [第4484行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4484) |
| 2015-01-22 | 色格拉 | 2:0 | 沙勒罗瓦 | 比利时杯 / CLUB_OFFICIAL_OTHER | FOTMOB-1870878 | [第4672行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4672) |

<a id="e065"></a>
### E065

双方队名已一致，无需为此新增队名映射。

日期差4天；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-02-12 | 本菲卡 | 3:0 | 塞图巴尔 | 葡联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-1910978 | [第5952行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5952) |
| 2015-02-16 | 本菲卡 | 3:0 | 塞图巴尔 | 葡超 / PRIMEIRA_LIGA | EXCEL-3146 | [第6234行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:6234) |

<a id="e066"></a>
### E066

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-05-11 | 斯蒂文 | 1:1 | 南安联 | 英乙 / CLUB_OFFICIAL_OTHER | EXCEL-5913 | [第12153行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12153) |
| 2015-05-15 | 南安联 | 1:1 | 斯蒂文 | 英乙 / CLUB_OFFICIAL_OTHER | EXCEL-5970 | [第12296行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12296) |

<a id="e067"></a>
### E067

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-05-12 | 阿尔梅勒 | 1:1 | 格拉夫 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-1965907 | [第12230行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12230) |
| 2015-05-16 | 格拉夫 | 1:1 | 阿尔梅勒 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-1965908 | [第12413行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12413) |

<a id="e068"></a>
### E068

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-17 | 兰纳斯 | 0:0 | 埃夫斯堡 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-7176 | [第15198行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15198) |
| 2015-07-21 | 埃夫斯堡 | 0:0 | 兰纳斯 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-7278 | [第15552行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15552) |

<a id="e069"></a>
### E069

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-09-02 | 大阪钢巴 | 1:1 | 名古屋鲸 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-8756 | [第18919行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:18919) |
| 2015-09-06 | 名古屋鲸 | 1:1 | 大阪钢巴 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-8845 | [第19075行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:19075) |

<a id="e070"></a>
### E070

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-09-20 | 温哥华 | 0:3 | 西雅图 | 美职足 / CLUB_OFFICIAL_OTHER | EXCEL-9351 | [第19993行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:19993) |
| 2015-09-24 | 西雅图 | 3:0 | 温哥华 | 中北美冠 / CLUB_OFFICIAL_OTHER | EXCEL-9575 | [第20481行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:20481) |

<a id="e071"></a>
### E071

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-11-14 | 毛里塔尼 | 1:2 | 突尼斯 | 世预赛 / INTERNATIONAL_OFFICIAL | EXCEL-11416 | [第23983行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:23983) |
| 2015-11-18 | 突尼斯 | 2:1 | 毛里塔尼 | 世预赛 / INTERNATIONAL_OFFICIAL | EXCEL-11503 | [第24132行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:24132) |

<a id="e072"></a>
### E072

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-12-04 | 莱万特 | 1:1 | 西班牙人 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2127209 | [第25156行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:25156) |
| 2015-12-08 | 西班牙人 | 1:1 | 莱万特 | 西甲 / LA_LIGA | FOTMOB-2030220 | [第25474行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:25474) |

<a id="e073"></a>
### E073

双方队名已一致，无需为此新增队名映射。

日期差4天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-04-27 | 大邱FC | 2:1 | Hummel | 韩国杯 / K_LEAGUE_1 | FOTMOB-2216058 | [第33808行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:33808) |
| 2016-05-01 | 大邱FC | 2:1 | Hummel | 韩挑战联 / CLUB_OFFICIAL_OTHER | FOTMOB-2177764 | [第34181行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34181) |

<a id="e074"></a>
### E074

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-03 | MVV Maastricht | 2:1 | 福伦丹 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-2224862 | [第34295行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34295) |
| 2016-05-07 | 福伦丹 | 1:2 | MVV Maastricht | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-2224864 | [第34487行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34487) |

<a id="e075"></a>
### E075

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-03 | 阿尔梅勒 | 4:1 | 埃门 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-2224863 | [第34296行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34296) |
| 2016-05-07 | 埃门 | 1:4 | 阿尔梅勒 | 荷乙附加赛 / CLUB_OFFICIAL_OTHER | FOTMOB-2224865 | [第34488行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34488) |

<a id="e076"></a>
### E076

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-05 | 安托法 | 0:0 | 圣漫步者 | 智利甲 / CLUB_OFFICIAL_OTHER | EXCEL-16776 | [第34339行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34339) |
| 2016-05-09 | 圣漫步者 | 0:0 | 安托法 | 智利甲 / CLUB_OFFICIAL_OTHER | EXCEL-16977 | [第34721行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34721) |

<a id="e077"></a>
### E077

双方队名已一致，无需为此新增队名映射。

日期差4天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-06 | 南安联 | 1:3 | 吉灵汉姆 | 英甲 / CLUB_OFFICIAL_OTHER | EXCEL-18715 | [第38904行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38904) |
| 2016-08-10 | 南安联 | 1:3 | 吉灵汉姆 | 英联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-18874 | [第39297行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39297) |

<a id="e078"></a>
### E078

双方队名已一致，无需为此新增队名映射。

日期差4天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-06 | Southend United | 1:3 | Gillingham | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-2259845 | [第38964行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38964) |
| 2016-08-10 | Southend United | 1:3 | Gillingham | 英联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2260997 | [第39336行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39336) |

<a id="e079"></a>
### E079

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-01-22 | 拉茨 | 1:1 | 哈茨 | 苏足总杯 / SCOTTISH_FA_CUP | EXCEL-24581 | [第49676行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49676) |
| 2017-01-26 | 哈茨 | 1:1 | 拉茨 | 苏足总杯 / SCOTTISH_FA_CUP | EXCEL-24662 | [第49876行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49876) |

<a id="e080"></a>
### E080

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-02-11 | 邓弗姆林 | 1:1 | 汉密尔顿 | 苏足总杯 / SCOTTISH_FA_CUP | EXCEL-25017 | [第50772行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:50772) |
| 2017-02-15 | 汉密尔顿 | 1:1 | 邓弗姆林 | 苏足总杯 / SCOTTISH_FA_CUP | EXCEL-25109 | [第51077行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:51077) |

<a id="e081"></a>
### E081

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-09-02 | 佛得角 | 2:1 | 南非 | 世预赛 / INTERNATIONAL_OFFICIAL | EXCEL-31210 | [第63668行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63668) |
| 2017-09-06 | 南非 | 1:2 | 佛得角 | 世预赛 / INTERNATIONAL_OFFICIAL | EXCEL-31308 | [第63845行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:63845) |

<a id="e082"></a>
### E082

双方队名已一致，无需为此新增队名映射。

日期差4天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-09-17 | SBV精英 | 1:2 | 海伦芬 | 荷甲 / EREDIVISIE | FOTMOB-2520332 | [第64708行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:64708) |
| 2017-09-21 | SBV精英 | 1:2 | 海伦芬 | 荷兰杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2623883 | [第65117行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:65117) |

<a id="e083"></a>
### E083

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-10-15 | 安托法 | 0:2 | 智利大学 | 智利甲 / CLUB_OFFICIAL_OTHER | EXCEL-32868 | [第66673行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:66673) |
| 2017-10-19 | 智利大学 | 2:0 | 安托法 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-33015 | [第66962行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:66962) |

<a id="e084"></a>
### E084

双方队名已一致，无需为此新增队名映射。

日期差4天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-02-26 | 阿森纳 | 0:3 | 曼城 | 英联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2702572 | [第74403行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:74403) |
| 2018-03-02 | 阿森纳 | 0:3 | 曼城 | 英超 / PREMIER_LEAGUE | FOTMOB-2523013 | [第74528行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:74528) |

<a id="e085"></a>
### E085

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-04-26 | CSM Studentesc Iasi | 0:3 | 久尔久 | 罗甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-2CC6180BC944E5F5 | [第78368行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:78368) |
| 2018-04-30 | 久尔久 | 3:0 | CSM Studentesc Iasi | 罗甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-9D62D63635F87BD9 | [第78796行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:78796) |

<a id="e086"></a>
### E086

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-06-07 | 奇塔代拉 | 1:1 | 弗洛西诺 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-2763446 | [第80597行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:80597) |
| 2018-06-11 | 弗洛西诺 | 1:1 | 奇塔代拉 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-2763448 | [第80672行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:80672) |

<a id="e087"></a>
### E087

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-10-10 | 柏太阳神 | 1:1 | 湘南海洋 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-43321 | [第88413行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:88413) |
| 2018-10-14 | 湘南海洋 | 1:1 | 柏太阳神 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-43387 | [第88542行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:88542) |

<a id="e088"></a>
### E088

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-11-17 | 希腊21 | 0:1 | 奥地利21 | 欧青预赛 / INTERNATIONAL_OFFICIAL | EXCEL-44583 | [第90781行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:90781) |
| 2018-11-21 | 奥地利21 | 1:0 | 希腊21 | 欧青预赛 / INTERNATIONAL_OFFICIAL | EXCEL-44665 | [第90915行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:90915) |

<a id="e089"></a>
### E089

双方队名已一致，无需为此新增队名映射。

日期差4天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-11-17 | 阿根廷 | 2:0 | 墨西哥 | 国际赛 / INTERNATIONAL_FRIENDLY | EXCEL-44592 | [第90785行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:90785) |
| 2018-11-21 | 阿根廷 | 2:0 | 墨西哥 | 国际赛 / INTERNATIONAL_FRIENDLY | EXCEL-44676 | [第90921行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:90921) |

<a id="e090"></a>
### E090

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-12-02 | 菲律宾 | 1:2 | 越南 | 东南亚锦 / INTERNATIONAL_OFFICIAL | EXCEL-45098 | [第91597行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:91597) |
| 2018-12-06 | 越南 | 2:1 | 菲律宾 | 东南亚锦 / INTERNATIONAL_OFFICIAL | EXCEL-45228 | [第91871行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:91871) |

<a id="e091"></a>
### E091

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-12-16 | 盖贝莱 | 1:0 | 萨巴赫 | 阿塞杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-CF189838CEFE3F03 | [第92515行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:92515) |
| 2018-12-20 | 萨巴赫 | 0:1 | 盖贝莱 | 阿塞杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-13A958A4A4376311 | [第92632行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:92632) |

<a id="e092"></a>
### E092

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-05-07 | Botev Vratsa | 0:0 | Slavia Sofia | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-3034788 | [第100470行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100470) |
| 2019-05-11 | Slavia Sofia | 0:0 | Botev Vratsa | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-3034789 | [第100636行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100636) |

<a id="e093"></a>
### E093

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-05-19 | 巴塞尔 | 2:1 | 图恩 | 瑞士杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3019052 | [第101201行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:101201) |
| 2019-05-23 | 图恩 | 1:2 | 巴塞尔 | 瑞士超 / CLUB_OFFICIAL_OTHER | FOTMOB-2783784 | [第101377行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:101377) |

<a id="e094"></a>
### E094

双方队名已一致，无需为此新增队名映射。

日期差4天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-10 | 牛津联 | 1:0 | Peterborough United | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3066414 | [第105180行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105180) |
| 2019-08-14 | 牛津联 | 1:0 | Peterborough United | 英联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3068539 | [第105506行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105506) |

<a id="e095"></a>
### E095

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-05-21 | 邓迪FC | 2:1 | 基马诺克 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-5B3EDE1E74ED6C68 | [第137853行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:137853) |
| 2021-05-25 | 基马诺克 | 1:2 | 邓迪FC | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-38ABCDDC1DB92234 | [第138112行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138112) |

<a id="e096"></a>
### E096

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-06 | FC Kolos Kovalivka | 0:0 | Shakhter Karagandy | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3664662 | [第141221行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141221) |
| 2021-08-10 | Shakhter Karagandy | 0:0 | FC Kolos Kovalivka | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3664694 | [第141600行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141600) |

<a id="e097"></a>
### E097

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-22 | 贝雷达 | 2:0 | KA | 冰超 / CLUB_OFFICIAL_OTHER | FOTMOB-3536280 | [第142382行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:142382) |
| 2021-08-26 | KA | 0:2 | 贝雷达 | 冰超 / CLUB_OFFICIAL_OTHER | FOTMOB-3536204 | [第142619行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:142619) |

<a id="e098"></a>
### E098

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-04-15 | 胡齐斯坦 | 1:0 | Ahal | 亚冠精英 / CLUB_OFFICIAL_OTHER | FOTMOB-3818343 | [第154853行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:154853) |
| 2022-04-19 | Ahal | 0:1 | 胡齐斯坦 | 亚冠精英 / CLUB_OFFICIAL_OTHER | FOTMOB-3818341 | [第155227行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:155227) |

<a id="e099"></a>
### E099

双方队名已一致，无需为此新增队名映射。

日期差4天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-05-07 | 贝游击 | 2:1 | 伏伊伏丁 | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-3860960 | [第156320行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:156320) |
| 2022-05-11 | 贝游击 | 2:1 | 伏伊伏丁 | 塞杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3865314 | [第156589行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:156589) |

<a id="e100"></a>
### E100

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-05-19 | 布雷西亚 | 1:2 | 蒙扎 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-3876571 | [第157037行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157037) |
| 2022-05-23 | 蒙扎 | 2:1 | 布雷西亚 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-3876572 | [第157290行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157290) |

<a id="e101"></a>
### E101

双方队名已一致，无需为此新增队名映射。

日期差4天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-04-20 | Cukaricki | 1:0 | 贝游击 | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-3902540 | [第174784行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:174784) |
| 2023-04-24 | Cukaricki | 1:0 | 贝游击 | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4159744 | [第175167行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:175167) |

<a id="e102"></a>
### E102

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-26 | 国际图尔 | 2:1 | 拉赫蒂 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-989102A663B69103 | [第181626行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181626) |
| 2023-08-30 | 拉赫蒂 | 1:2 | 国际图尔 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-DBEE3F7C0F6020C3 | [第181904行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181904) |

<a id="e103"></a>
### E103

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-09-06 | 名古屋鲸 | 1:1 | 鹿岛鹿角 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-69800 | [第182331行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:182331) |
| 2023-09-10 | 鹿岛鹿角 | 1:1 | 名古屋鲸 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-69836 | [第182440行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:182440) |

<a id="e104"></a>
### E104

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-10-11 | 福冈黄蜂 | 1:0 | 名古屋鲸 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-70090 | [第184519行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:184519) |
| 2023-10-15 | 名古屋鲸 | 0:1 | 福冈黄蜂 | 日联赛杯 / CLUB_OFFICIAL_OTHER | EXCEL-70126 | [第184633行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:184633) |

<a id="e105"></a>
### E105

双方队名已一致，无需为此新增队名映射。

日期差4天；不同来源；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-21 | 光州FC | 0:1 | 蔚山现代 | 韩国杯 / K_LEAGUE_1 | EXCEL-73410 | [第200967行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200967) |
| 2024-08-25 | 光州FC | 0:1 | 蔚山现代 | 韩职 / K_LEAGUE_1 | FOTMOB-4404801 | [第201199行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201199) |

<a id="e106"></a>
### E106

双方队名已一致，无需为此新增队名映射。

日期差4天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-03-29 | 贝游击 | 0:0 | 托波拉 | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4500346 | [第212772行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212772) |
| 2025-04-02 | 贝游击 | 0:0 | 托波拉 | 塞杯 / CLUB_OFFICIAL_OTHER | FOTMOB-4761759 | [第213149行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:213149) |

<a id="e107"></a>
### E107

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-05-09 | 布城 | 0:3 | 谢菲联 | 英冠 / CLUB_OFFICIAL_OTHER | EXCEL-76540 | [第215271行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:215271) |
| 2025-05-13 | 谢菲联 | 3:0 | 布城 | 英冠 / CLUB_OFFICIAL_OTHER | EXCEL-76613 | [第215603行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:215603) |

<a id="e108"></a>
### E108

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-04-19 | 维也纳 | 1:3 | 萨尔茨堡 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-40A8FCF1773F996A | [第234364行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234364) |
| 2026-04-23 | 萨尔茨堡 | 3:1 | 维也纳 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-0B4E9B194A7A98E4 | [第234516行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234516) |

<a id="e109"></a>
### E109

双方队名已一致，无需为此新增队名映射。

日期差4天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-04-19 | LASK林茨 | 1:1 | 格风暴 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-701FE3B630CA0FAE | [第234372行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234372) |
| 2026-04-23 | 格风暴 | 1:1 | LASK林茨 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-CD8B5424E96A0BE6 | [第234530行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234530) |

<a id="e110"></a>
### E110

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-02-07 | 河床 | 1:0 | 圣洛伦索 | 优胜者杯 / CLUB_OFFICIAL_OTHER | EXCEL-2818 | [第5564行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5564) |
| 2015-02-12 | 圣洛伦索 | 0:1 | 河床 | 优胜者杯 / CLUB_OFFICIAL_OTHER | EXCEL-3007 | [第5939行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5939) |

<a id="e111"></a>
### E111

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-05-10 | 阿里卡 | 1:1 | 拉卡联合 | 智利甲 / CLUB_OFFICIAL_OTHER | EXCEL-5853 | [第12005行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12005) |
| 2015-05-15 | 拉卡联合 | 1:1 | 阿里卡 | 智利甲 / CLUB_OFFICIAL_OTHER | EXCEL-5975 | [第12301行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:12301) |

<a id="e112"></a>
### E112

双方队名已一致，无需为此新增队名映射。

日期差5天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-08 | 格罗宁根 | 2:1 | 赫拉克勒 | 荷甲 / EREDIVISIE | FOTMOB-1983575 | [第34562行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34562) |
| 2016-05-13 | 格罗宁根 | 2:1 | 赫拉克勒 | 荷甲 / EREDIVISIE | FOTMOB-2227784 | [第34918行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34918) |

<a id="e113"></a>
### E113

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-06-03 | 马尔代夫 | 0:2 | 也门 | 亚预赛 / INTERNATIONAL_OFFICIAL | EXCEL-17549 | [第35889行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35889) |
| 2016-06-08 | 也门 | 2:0 | 马尔代夫 | 亚预赛 / INTERNATIONAL_OFFICIAL | EXCEL-17613 | [第35985行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35985) |

<a id="e114"></a>
### E114

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-04-01 | 乌法 | 0:1 | 莫火车头 | 俄超 / CLUB_OFFICIAL_OTHER | EXCEL-26606 | [第53895行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:53895) |
| 2017-04-06 | 莫火车头 | 1:0 | 乌法 | 俄罗斯杯 / CLUB_OFFICIAL_OTHER | EXCEL-26825 | [第54362行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:54362) |

<a id="e115"></a>
### E115

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-24 | 巴黎FC | 0:1 | 奥尔良 | 法乙 / CLUB_OFFICIAL_OTHER | EXCEL-28781 | [第57907行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57907) |
| 2017-05-29 | 奥尔良 | 1:0 | 巴黎FC | 法乙 / CLUB_OFFICIAL_OTHER | EXCEL-28906 | [第58170行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:58170) |

<a id="e116"></a>
### E116

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-10-05 | 叙利亚 | 1:1 | 澳大利亚 | 世预赛 / INTERNATIONAL_OFFICIAL | EXCEL-32580 | [第66187行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:66187) |
| 2017-10-10 | 澳大利亚 | 1:1 | 叙利亚 | 世预赛 / INTERNATIONAL_OFFICIAL | EXCEL-32700 | [第66397行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:66397) |

<a id="e117"></a>
### E117

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-05-10 | Vereya | 0:3 | Septemvri | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-3034320 | [第100523行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100523) |
| 2019-05-15 | Septemvri | 3:0 | Vereya | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-3034321 | [第100913行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100913) |

<a id="e118"></a>
### E118

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-02-27 | 塞维利亚 | 0:2 | 巴萨 | 西甲 / LA_LIGA | FOTMOB-3424286 | [第132637行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:132637) |
| 2021-03-04 | 巴萨 | 2:0 | 塞维利亚 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3529061 | [第133010行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133010) |

<a id="e119"></a>
### E119

双方队名已一致，无需为此新增队名映射。

日期差5天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-06-13 | 塞伊奈 | 1:1 | 国际图尔 | 芬兰杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-A02058B149B57F07 | [第157711行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157711) |
| 2022-06-18 | 塞伊奈 | 1:1 | 国际图尔 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-FC25BB10C3A066A9 | [第157848行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:157848) |

<a id="e120"></a>
### E120

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-18 | 玛丽港 | 0:1 | 洪卡 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-3DD547D03C3D5D56 | [第181033行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181033) |
| 2023-08-23 | 洪卡 | 1:0 | 玛丽港 | 芬兰杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-2DE1E3D8D76CC4FB | [第181441行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181441) |

<a id="e121"></a>
### E121

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-03-22 | 直布罗陀 | 0:1 | 立陶宛 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4151923 | [第193110行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:193110) |
| 2024-03-27 | 立陶宛 | 1:0 | 直布罗陀 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4151924 | [第193252行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:193252) |

<a id="e122"></a>
### E122

双方队名已一致，无需为此新增队名映射。

日期差5天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-04-16 | FK Spartak Subotica | 1:0 | Javor | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4271823 | [第194504行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:194504) |
| 2024-04-21 | FK Spartak Subotica | 1:0 | Javor | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4464920 | [第194854行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:194854) |

<a id="e123"></a>
### E123

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-04-22 | 哥本哈根 | 3:1 | 奥胡斯 | 丹超 / CLUB_OFFICIAL_OTHER | FUTBOL24-5639D1086D3B2C1B | [第214359行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:214359) |
| 2025-04-27 | 奥胡斯 | 1:3 | 哥本哈根 | 丹超 / CLUB_OFFICIAL_OTHER | FUTBOL24-223BF45864EB3DD0 | [第214709行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:214709) |

<a id="e124"></a>
### E124

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-07 | 流浪者 | 2:0 | 阿伯丁 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-99929D870F59CD42 | [第228095行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228095) |
| 2026-01-12 | 阿伯丁 | 0:2 | 流浪者 | 苏超 / CLUB_OFFICIAL_OTHER | FUTBOL24-29C9C9BC71545ABE | [第228355行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228355) |

<a id="e125"></a>
### E125

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-27 | 直布罗陀 | 0:1 | 拉脱维亚 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679405 | [第233076行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:233076) |
| 2026-04-01 | 拉脱维亚 | 1:0 | 直布罗陀 | 欧国联 / UEFA_NATIONS_LEAGUE | FOTMOB-4679406 | [第233203行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:233203) |

<a id="e126"></a>
### E126

双方队名已一致，无需为此新增队名映射。

日期差5天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-05-10 | 博尔顿 | 1:0 | Bradford City | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-5428147 | [第235585行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:235585) |
| 2026-05-15 | Bradford City | 0:1 | 博尔顿 | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-5428148 | [第235784行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:235784) |

<a id="e127"></a>
### E127

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-17 | 索尔纳 | 2:0 | 希拉克 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-7178 | [第15199行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15199) |
| 2015-07-23 | 希拉克 | 0:2 | 索尔纳 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-7316 | [第15686行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15686) |

<a id="e128"></a>
### E128

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-01-08 | 埃瓦尔 | 2:3 | 拉帕马斯 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2150370 | [第26551行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26551) |
| 2016-01-14 | 拉帕马斯 | 3:2 | 埃瓦尔 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2150378 | [第26852行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26852) |

<a id="e129"></a>
### E129

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-03-10 | 格雷米奥 | 1:1 | 圣洛伦索 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-14684 | [第30459行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:30459) |
| 2016-03-16 | 圣洛伦索 | 1:1 | 格雷米奥 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-14951 | [第30955行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:30955) |

<a id="e130"></a>
### E130

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-04-13 | 鲁容贝罗 | 0:1 | 特伦钦 | 斯洛伐杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2211181 | [第32734行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:32734) |
| 2016-04-19 | 特伦钦 | 1:0 | 鲁容贝罗 | 斯洛伐超 / CLUB_OFFICIAL_OTHER | FOTMOB-2017609 | [第33195行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:33195) |

<a id="e131"></a>
### E131

双方队名已一致，无需为此新增队名映射。

日期差6天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-01 | SC Freamunde | 0:1 | 马德拉 | 葡联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2304151 | [第38666行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38666) |
| 2016-08-07 | SC Freamunde | 0:1 | 马德拉 | 葡甲 / CLUB_OFFICIAL_OTHER | FOTMOB-2302515 | [第39143行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39143) |

<a id="e132"></a>
### E132

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-11 | 蒙漫步者 | 0:0 | 奥伊金斯 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-18923 | [第39423行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39423) |
| 2016-08-17 | 奥伊金斯 | 0:0 | 蒙漫步者 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-19196 | [第39959行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39959) |

<a id="e133"></a>
### E133

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-16 | 防御正义 | 2:0 | 基尔梅斯 | 阿甲 / ARGENTINE_PRIMERA_DIVISION | EXCEL-28489 | [第57340行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57340) |
| 2017-05-22 | 基尔梅斯 | 0:2 | 防御正义 | 阿甲 / ARGENTINE_PRIMERA_DIVISION | EXCEL-28754 | [第57837行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57837) |

<a id="e134"></a>
### E134

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-08-17 | 那不勒斯 | 2:0 | 尼斯 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-30605 | [第62462行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:62462) |
| 2017-08-23 | 尼斯 | 0:2 | 那不勒斯 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-30855 | [第62935行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:62935) |

<a id="e135"></a>
### E135

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-05 | Cherno More Varna | 2:1 | Septemvri | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-2754221 | [第79038行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79038) |
| 2018-05-11 | Septemvri | 1:2 | Cherno More Varna | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-2754222 | [第79459行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:79459) |

<a id="e136"></a>
### E136

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-09 | 意大利人 | 2:1 | 瓦奇巴托 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-40360 | [第81537行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81537) |
| 2018-07-15 | 瓦奇巴托 | 1:2 | 意大利人 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-40407 | [第81914行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81914) |

<a id="e137"></a>
### E137

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-25 | 根特 | 2:2 | 奥斯坦德 | 比利时杯 / CLUB_OFFICIAL_OTHER | EXCEL-46356 | [第94061行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94061) |
| 2019-01-31 | 奥斯坦德 | 2:2 | 根特 | 比利时杯 / CLUB_OFFICIAL_OTHER | EXCEL-46548 | [第94469行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94469) |

<a id="e138"></a>
### E138

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-25 | 西班牙人 | 1:1 | 贝蒂斯 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2972059 | [第94068行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94068) |
| 2019-01-31 | 贝蒂斯 | 1:1 | 西班牙人 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2972063 | [第94491行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94491) |

<a id="e139"></a>
### E139

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 希罗基 | 1:2 | 阿拉木图 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-89C7953450ADA761 | [第103135行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103135) |
| 2019-07-18 | 阿拉木图 | 2:1 | 希罗基 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-147311FF9C124144 | [第103524行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103524) |

<a id="e140"></a>
### E140

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 埃施福拉 | 1:2 | Chikhura Sachkhere | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C2F0145C3638C9CC | [第103145行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103145) |
| 2019-07-18 | Chikhura Sachkhere | 2:1 | 埃施福拉 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-1BD03FE9974DCBDB | [第103525行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103525) |

<a id="e141"></a>
### E141

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-24 | SP Tre Penne | 0:5 | Suduva | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A83E32078B5A350D | [第104007行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104007) |
| 2019-07-30 | Suduva | 5:0 | SP Tre Penne | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-8B82A8C7E7952D3C | [第104447行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104447) |

<a id="e142"></a>
### E142

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-08 | 马里博尔 | 1:3 | 罗森博格 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-50053 | [第105007行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105007) |
| 2019-08-14 | 罗森博格 | 3:1 | 马里博尔 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-298E559EA84052E3 | [第105533行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105533) |

<a id="e143"></a>
### E143

双方队名已一致，无需为此新增队名映射。

日期差6天；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-12-01 | 考文垂 | 1:1 | 伊普斯 | 英足总杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3218989 | [第111917行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:111917) |
| 2019-12-07 | 考文垂 | 1:1 | 伊普斯 | 英甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3066628 | [第112203行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:112203) |

<a id="e144"></a>
### E144

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-06-21 | Slovacko | 1:2 | Bohemians 1905 | 捷甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3346609 | [第117979行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117979) |
| 2020-06-27 | Bohemians 1905 | 2:1 | Slovacko | 捷甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3346596 | [第118274行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:118274) |

<a id="e145"></a>
### E145

双方队名已一致，无需为此新增队名映射。

日期差6天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-02-27 | 莱万特 | 1:1 | 毕尔巴鄂 | 西甲 / LA_LIGA | FOTMOB-3424284 | [第132636行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:132636) |
| 2021-03-05 | 莱万特 | 1:1 | 毕尔巴鄂 | 西国王杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3529062 | [第133041行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133041) |

<a id="e146"></a>
### E146

双方队名已一致，无需为此新增队名映射。

日期差6天；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-03-14 | 佐加顿斯 | 3:0 | 厄斯特松 | 瑞典杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3550791 | [第133746行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133746) |
| 2021-03-20 | 佐加顿斯 | 3:0 | 厄斯特松 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-2F1B8C759FA3D2DA | [第134042行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:134042) |

<a id="e147"></a>
### E147

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-05-02 | 库奥皮奥 | 0:0 | 赫尔辛基 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-4833A33CC60F30F4 | [第136671行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:136671) |
| 2021-05-08 | 赫尔辛基 | 0:0 | 库奥皮奥 | 芬兰杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-78D97299C4D6F9DE | [第136979行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:136979) |

<a id="e148"></a>
### E148

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-06-24 | AC奥卢 | 0:2 | 库奥皮奥 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-14B50D28089F0B09 | [第138756行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138756) |
| 2021-06-30 | 库奥皮奥 | 2:0 | AC奥卢 | 芬超 / FINNISH_VEIKKAUSLIIGA | FUTBOL24-29A135388E7A7E7A | [第138967行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:138967) |

<a id="e149"></a>
### E149

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-08 | 卢多戈雷 | 1:0 | 索矿工 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-0C4B0E05B23790BB | [第139359行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139359) |
| 2021-07-14 | 索矿工 | 0:1 | 卢多戈雷 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-C31E5AACB21A1C59 | [第139707行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139707) |

<a id="e150"></a>
### E150

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | 索菲亚中央陆军 | 0:0 | FK Liepaja | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610550 | [第140264行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140264) |
| 2021-07-29 | FK Liepaja | 0:0 | 索菲亚中央陆军 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610551 | [第140713行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140713) |

<a id="e151"></a>
### E151

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-05 | 莫斯巴达 | 0:2 | 本菲卡 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-61445 | [第141172行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141172) |
| 2021-08-11 | 本菲卡 | 2:0 | 莫斯巴达 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-61514 | [第141607行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141607) |

<a id="e152"></a>
### E152

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-09 | 罗斯托克 | 1:1 | 海登海姆 | 德国杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3638921 | [第141567行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141567) |
| 2021-08-15 | 海登海姆 | 1:1 | 罗斯托克 | 德乙 / CLUB_OFFICIAL_OTHER | FOTMOB-3624785 | [第141941行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141941) |

<a id="e153"></a>
### E153

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-19 | 年轻人 | 3:2 | 费伦茨 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-D8B245F92273EAAA | [第142188行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:142188) |
| 2021-08-25 | 费伦茨 | 2:3 | 年轻人 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-61681 | [第142578行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:142578) |

<a id="e154"></a>
### E154

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-20 | 中日德兰 | 1:1 | 拉纳卡 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-65211 | [第159529行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159529) |
| 2022-07-26 | 拉纳卡 | 1:1 | 中日德兰 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-65279 | [第160015行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160015) |

<a id="e155"></a>
### E155

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-10-06 | 本菲卡 | 1:1 | 巴黎圣曼 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-66272 | [第164589行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:164589) |
| 2022-10-12 | 巴黎圣曼 | 1:1 | 本菲卡 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-66340 | [第165015行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:165015) |

<a id="e156"></a>
### E156

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-04-13 | 皇马 | 2:0 | 切尔西 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-68406 | [第174359行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:174359) |
| 2023-04-19 | 切尔西 | 0:2 | 皇马 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-68481 | [第174745行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:174745) |

<a id="e157"></a>
### E157

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-06-12 | 阿拉维斯 | 0:0 | 莱万特 | 西乙 / CLUB_OFFICIAL_OTHER | FOTMOB-4187703 | [第177627行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:177627) |
| 2023-06-18 | 莱万特 | 0:0 | 阿拉维斯 | 西乙 / CLUB_OFFICIAL_OTHER | FOTMOB-4187704 | [第177674行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:177674) |

<a id="e158"></a>
### E158

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-28 | 卡尔马 | 1:2 | 埃凤凰 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200981 | [第179608行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179608) |
| 2023-08-03 | 埃凤凰 | 2:1 | 卡尔马 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200982 | [第180030行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180030) |

<a id="e159"></a>
### E159

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-05-02 | 多特蒙德 | 1:0 | 巴黎圣曼 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-72225 | [第195457行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:195457) |
| 2024-05-08 | 巴黎圣曼 | 0:1 | 多特蒙德 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-72314 | [第195821行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:195821) |

<a id="e160"></a>
### E160

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-21 | Sandvikens IF | 1:2 | Utsiktens BK | 瑞甲 / CLUB_OFFICIAL_OTHER | FOTMOB-4387161 | [第198855行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198855) |
| 2024-07-27 | Utsiktens BK | 2:1 | Sandvikens IF | 瑞甲 / CLUB_OFFICIAL_OTHER | FOTMOB-4387168 | [第199183行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199183) |

<a id="e161"></a>
### E161

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-22 | 马尔默 | 0:2 | 布斯巴达 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-73415 | [第200987行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200987) |
| 2024-08-28 | 布斯巴达 | 2:0 | 马尔默 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-73503 | [第201400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201400) |

<a id="e162"></a>
### E162

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-23 | 贝游击 | 0:1 | 根特 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4591282 | [第201044行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201044) |
| 2024-08-29 | 根特 | 1:0 | 贝游击 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4591283 | [第201463行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201463) |

<a id="e163"></a>
### E163

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-04-08 | Mladost Lucani | 1:3 | 伏伊伏丁 | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4500354 | [第213534行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:213534) |
| 2025-04-14 | 伏伊伏丁 | 3:1 | Mladost Lucani | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4770313 | [第213925行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:213925) |

<a id="e164"></a>
### E164

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-05-01 | 巴萨 | 3:3 | 国际米兰 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-76425 | [第214818行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:214818) |
| 2025-05-07 | 国际米兰 | 3:3 | 巴萨 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-76533 | [第215231行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:215231) |

<a id="e165"></a>
### E165

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-24 | 新圣徒 | 0:1 | 迪弗当日 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4814637 | [第218490行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218490) |
| 2025-07-30 | 迪弗当日 | 1:0 | 新圣徒 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4814638 | [第218943行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218943) |

<a id="e166"></a>
### E166

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-07 | 尼斯 | 0:2 | 本菲卡 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-77454 | [第219471行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219471) |
| 2025-08-13 | 本菲卡 | 2:0 | 尼斯 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-77537 | [第219895行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219895) |

<a id="e167"></a>
### E167

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-21 | 凯尔特人 | 0:0 | 阿拉木图 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-3B435D27A04D7448 | [第220422行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:220422) |
| 2025-08-27 | 阿拉木图 | 0:0 | 凯尔特人 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-77722 | [第220811行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:220811) |

<a id="e168"></a>
### E168

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-09 | 维快速 | 1:0 | 萨尔茨堡 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-1B44BC4E1382C6C5 | [第232102行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232102) |
| 2026-03-15 | 萨尔茨堡 | 0:1 | 维快速 | 奥甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-2D37C633E62FEDA9 | [第232472行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232472) |

<a id="e169"></a>
### E169

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-15 | 埃凤凰 | 1:0 | 叶阿拉什 | 亚美尼超 / CLUB_OFFICIAL_OTHER | FUTBOL24-1361823619E5B98F | [第232468行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232468) |
| 2026-03-21 | 叶阿拉什 | 0:1 | 埃凤凰 | 亚美尼超 / CLUB_OFFICIAL_OTHER | FUTBOL24-D84156979C53E0C3 | [第232817行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232817) |

<a id="e170"></a>
### E170

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-04-09 | 巴黎圣曼 | 2:0 | 利物浦 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-81306 | [第233659行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:233659) |
| 2026-04-15 | 利物浦 | 0:2 | 巴黎圣曼 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-81403 | [第234034行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234034) |

<a id="e171"></a>
### E171

双方队名已一致，无需为此新增队名映射。

日期差6天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | Alashkert FC | 1:1 | FC Yelimay | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-5786617 | [第237405行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237405) |
| 2026-07-16 | FC Yelimay | 1:1 | Alashkert FC | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-5786642 | [第237827行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237827) |

<a id="e172"></a>
### E172

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2014-10-30 | 麦国民 | 1:0 | 塞巴大学 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-346 | [第627行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:627) |
| 2014-11-06 | 塞巴大学 | 0:1 | 麦国民 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-574 | [第1142行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:1142) |

<a id="e173"></a>
### E173

双方队名已一致，无需为此新增队名映射。

日期差7天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2014-11-22 | 圣约翰 | 2:1 | 罗斯郡 | 苏超 / CLUB_OFFICIAL_OTHER | EXCEL-1011 | [第1821行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:1821) |
| 2014-11-29 | 圣约翰 | 2:1 | 罗斯郡 | 苏足总杯 / SCOTTISH_FA_CUP | EXCEL-1246 | [第2248行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:2248) |

<a id="e174"></a>
### E174

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-01-07 | 埃弗顿 | 1:1 | 西汉姆联 | 英足总杯 / CLUB_OFFICIAL_OTHER | FOTMOB-1873309 | [第4022行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4022) |
| 2015-01-14 | 西汉姆联 | 1:1 | 埃弗顿 | 英足总杯 / CLUB_OFFICIAL_OTHER | FOTMOB-1893027 | [第4263行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:4263) |

<a id="e175"></a>
### E175

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-03-13 | 胡安奥里 | 1:1 | 河床 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-3709 | [第7790行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:7790) |
| 2015-03-20 | 河床 | 1:1 | 胡安奥里 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-3986 | [第8359行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:8359) |

<a id="e176"></a>
### E176

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-01 | 埃凤凰 | 2:1 | Folgore Calcio | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-57EADF3E834C6D3B | [第14370行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14370) |
| 2015-07-08 | Folgore Calcio | 1:2 | 埃凤凰 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-C979E02B73166FEF | [第14726行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14726) |

<a id="e177"></a>
### E177

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-03 | 科克城 | 1:1 | 雷克雅 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-6959 | [第14421行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14421) |
| 2015-07-10 | 雷克雅 | 1:1 | 科克城 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-7057 | [第14760行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14760) |

<a id="e178"></a>
### E178

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-03 | FC Saxan* | 0:2 | 利阿波罗 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-22487805876548E2 | [第14432行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14432) |
| 2015-07-10 | 利阿波罗 | 2:0 | FC Saxan* | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-E8DF4023CF740438 | [第14829行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14829) |

<a id="e179"></a>
### E179

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-03 | 塞伊奈 | 0:1 | 哈夫纳夫 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-4B2F725B22895F2A | [第14440行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14440) |
| 2015-07-10 | 哈夫纳夫 | 1:0 | 塞伊奈 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-D59CE83391109FFE | [第14821行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14821) |

<a id="e180"></a>
### E180

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-03 | 纽敦 | 2:1 | Valletta FC | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-F5C6DB8B4C8D6D16 | [第14482行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14482) |
| 2015-07-10 | Valletta FC | 1:2 | 纽敦 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-83A3CF144294EEE6 | [第14796行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:14796) |

<a id="e181"></a>
### E181

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-07-31 | 斯海杜克 | 2:0 | 斯特罗姆 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-16B614B69DC8EFA9 | [第16202行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16202) |
| 2015-08-07 | 斯特罗姆 | 0:2 | 斯海杜克 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A1AD31D1C635FA01 | [第16703行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:16703) |

<a id="e182"></a>
### E182

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-08-13 | 亚奥林 | 2:0 | 瓦奇巴托 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-7905 | [第17265行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:17265) |
| 2015-08-20 | 瓦奇巴托 | 0:2 | 亚奥林 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-8171 | [第17834行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:17834) |

<a id="e183"></a>
### E183

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-08-21 | 利贝雷茨 | 1:0 | 斯海杜克 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-8190 | [第17878行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:17878) |
| 2015-08-28 | 斯海杜克 | 0:1 | 利贝雷茨 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-8519 | [第18467行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:18467) |

<a id="e184"></a>
### E184

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-09-25 | 拉努斯 | 0:0 | 捍卫者 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-9589 | [第20571行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:20571) |
| 2015-10-02 | 捍卫者 | 0:0 | 拉努斯 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-9890 | [第21116行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:21116) |

<a id="e185"></a>
### E185

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-09-25 | 亚自由 | 1:1 | 沙佩科恩斯 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-9590 | [第20572行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:20572) |
| 2015-10-02 | 沙佩科恩斯 | 1:1 | 亚自由 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-9889 | [第21115行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:21115) |

<a id="e186"></a>
### E186

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-11-02 | 华盛顿 | 0:1 | 纽约红牛 | 美职足 / CLUB_OFFICIAL_OTHER | EXCEL-11072 | [第23337行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:23337) |
| 2015-11-09 | 纽约红牛 | 1:0 | 华盛顿 | 美职足 / CLUB_OFFICIAL_OTHER | EXCEL-11346 | [第23850行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:23850) |

<a id="e187"></a>
### E187

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2015-12-03 | 飓风 | 0:0 | 圣菲独立 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-12025 | [第25089行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:25089) |
| 2015-12-10 | 圣菲独立 | 0:0 | 飓风 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-12218 | [第25523行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:25523) |

<a id="e188"></a>
### E188

双方队名已一致，无需为此新增队名映射。

日期差7天；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-01-24 | 博卡 | 0:1 | 河床 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-13254 | [第27479行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:27479) |
| 2016-01-31 | 博卡 | 0:1 | 河床 | 俱乐部赛 / CLUB_FRIENDLY | EXCEL-13441 | [第27866行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:27866) |

<a id="e189"></a>
### E189

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-02-24 | 艾因 | 1:2 | 武装体育 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-14088 | [第29361行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:29361) |
| 2016-03-02 | 武装体育 | 2:1 | 艾因 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-14392 | [第29850行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:29850) |

<a id="e190"></a>
### E190

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-04-20 | 麦国民 | 0:0 | 飓风 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-16209 | [第33230行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:33230) |
| 2016-04-27 | 飓风 | 0:0 | 麦国民 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-16486 | [第33799行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:33799) |

<a id="e191"></a>
### E191

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-04-27 | 纳夫兹 | 1:1 | 盖贝莱 | 阿塞杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-7A067A171346C2B0 | [第33818行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:33818) |
| 2016-05-04 | 盖贝莱 | 1:1 | 纳夫兹 | 阿塞杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-7EE9CD899AFADED1 | [第34329行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34329) |

<a id="e192"></a>
### E192

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-05-13 | 蒙国民 | 1:1 | 博卡 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-17036 | [第34893行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:34893) |
| 2016-05-20 | 博卡 | 1:1 | 蒙国民 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-17273 | [第35337行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:35337) |

<a id="e193"></a>
### E193

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-01 | 特斯巴达 | 3:0 | 爱尔兰人 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A589CC72F44BDDFC | [第36671行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36671) |
| 2016-07-08 | 爱尔兰人 | 0:3 | 特斯巴达 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-0FD028F19D74B33D | [第36963行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36963) |

<a id="e194"></a>
### E194

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-01 | 索尔纳 | 2:0 | 巴拉 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A8402E99EA48A478 | [第36672行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36672) |
| 2016-07-08 | 巴拉 | 0:2 | 索尔纳 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-8C64A8E73340F941 | [第36986行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36986) |

<a id="e195"></a>
### E195

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-01 | 中日德兰 | 1:0 | Suduva | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C1DE5E26EC881B3D | [第36679行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:36679) |
| 2016-07-08 | Suduva | 0:1 | 中日德兰 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C0B0E8CF7011B018 | [第37000行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37000) |

<a id="e196"></a>
### E196

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-13 | Valletta FC | 1:2 | 贝红星 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-3C82C88BAEEF3476 | [第37250行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37250) |
| 2016-07-20 | 贝红星 | 2:1 | Valletta FC | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-A1455E97ED6BA218 | [第37792行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37792) |

<a id="e197"></a>
### E197

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-14 | Partizani Tirana | 1:1 | 费伦茨 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-235EC8413CB5CCB4 | [第37313行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37313) |
| 2016-07-21 | 费伦茨 | 1:1 | Partizani Tirana | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-9AAFFFBBE24539D0 | [第37837行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37837) |

<a id="e198"></a>
### E198

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-15 | 索尔纳 | 1:0 | 欧罗巴FC | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-28544515CAE62857 | [第37342行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37342) |
| 2016-07-22 | 欧罗巴FC | 0:1 | 索尔纳 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-7A7DAB04F381C195 | [第37879行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37879) |

<a id="e199"></a>
### E199

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-15 | 贝游击 | 0:0 | 卢宾扎格勒比 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-806C091413850E4E | [第37358行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37358) |
| 2016-07-22 | 卢宾扎格勒比 | 0:0 | 贝游击 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-987A8968B9929C1F | [第37888行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37888) |

<a id="e200"></a>
### E200

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-15 | 海马卡比 | 1:1 | Nomme Kalju | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-D08C0431DBA3C737 | [第37372行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37372) |
| 2016-07-22 | Nomme Kalju | 1:1 | 海马卡比 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-14308EA2C139203A | [第37858行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37858) |

<a id="e201"></a>
### E201

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-24 | 老男孩 | 0:0 | 罗萨里奥 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-A9405C98A0980811 | [第38134行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38134) |
| 2016-07-31 | 罗萨里奥 | 0:0 | 老男孩 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-C4A629912C7B493C | [第38623行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38623) |

<a id="e202"></a>
### E202

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-07-27 | 卢多戈雷 | 2:2 | 贝红星 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-18356 | [第38233行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38233) |
| 2016-08-03 | 贝红星 | 2:2 | 卢多戈雷 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-18576 | [第38718行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38718) |

<a id="e203"></a>
### E203

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-19 | 斯拉维亚 | 0:3 | 安德莱 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-19233 | [第40032行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:40032) |
| 2016-08-26 | 安德莱 | 3:0 | 斯拉维亚 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-19562 | [第40624行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:40624) |

<a id="e204"></a>
### E204

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-08-19 | 维也纳 | 2:1 | 罗森博格 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-19235 | [第40034行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:40034) |
| 2016-08-26 | 罗森博格 | 1:2 | 维也纳 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-19558 | [第40621行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:40621) |

<a id="e205"></a>
### E205

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-09-22 | 阿独立 | 0:0 | 沙佩科恩斯 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-20655 | [第42606行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:42606) |
| 2016-09-29 | 沙佩科恩斯 | 0:0 | 阿独立 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-20987 | [第43273行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:43273) |

<a id="e206"></a>
### E206

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-09-22 | 蒙漫步者 | 0:0 | 巴兰基亚 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-20656 | [第42607行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:42607) |
| 2016-09-29 | 巴兰基亚 | 0:0 | 蒙漫步者 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-20988 | [第43274行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:43274) |

<a id="e207"></a>
### E207

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-09-22 | 天主大学 | 2:1 | 特木科 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-20661 | [第42609行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:42609) |
| 2016-09-29 | 特木科 | 1:2 | 天主大学 | 智利杯 / CLUB_OFFICIAL_OTHER | EXCEL-20986 | [第43272行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:43272) |

<a id="e208"></a>
### E208

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-03-10 | 希腊人 | 0:1 | 安德莱 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-25900 | [第52613行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:52613) |
| 2017-03-17 | 安德莱 | 1:0 | 希腊人 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-26190 | [第53150行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:53150) |

<a id="e209"></a>
### E209

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-04-14 | 安德莱 | 1:1 | 曼联 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-27164 | [第54959行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:54959) |
| 2017-04-21 | 曼联 | 1:1 | 安德莱 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-27462 | [第55467行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:55467) |

<a id="e210"></a>
### E210

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-05-24 | 胡独立 | 1:2 | 希拉尔 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-28780 | [第57906行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:57906) |
| 2017-05-31 | 希拉尔 | 2:1 | 胡独立 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-28950 | [第58236行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:58236) |

<a id="e211"></a>
### E211

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-06-30 | 瓦萨 | 1:0 | 奥林 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-2AD58872378010EC | [第59108行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59108) |
| 2017-07-07 | 奥林 | 0:1 | 瓦萨 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-29511 | [第59428行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59428) |

<a id="e212"></a>
### E212

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-06-30 | 加扎拜尔 | 0:1 | 沙姆洛克 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-3191274D3BA851F3 | [第59112行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59112) |
| 2017-07-07 | 沙姆洛克 | 1:0 | 加扎拜尔 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-29513 | [第59430行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59430) |

<a id="e213"></a>
### E213

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-06-30 | Nomme Kalju | 2:1 | 托尔B36 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-75A329648660F5AF | [第59121行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59121) |
| 2017-07-07 | 托尔B36 | 1:2 | Nomme Kalju | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-0C53B100A6632729 | [第59441行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59441) |

<a id="e214"></a>
### E214

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-13 | 希腊人 | 1:0 | 迪德朗日 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-29608 | [第59786行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59786) |
| 2017-07-20 | 迪德朗日 | 0:1 | 希腊人 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-29745 | [第60276行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60276) |

<a id="e215"></a>
### E215

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-13 | 邓多克 | 1:1 | 罗森博格 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-29611 | [第59789行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59789) |
| 2017-07-20 | 罗森博格 | 1:1 | 邓多克 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-29746 | [第60277行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60277) |

<a id="e216"></a>
### E216

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-14 | 科克城 | 0:1 | 拉纳卡 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-29638 | [第59825行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59825) |
| 2017-07-21 | 拉纳卡 | 1:0 | 科克城 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-29774 | [第60320行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60320) |

<a id="e217"></a>
### E217

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-14 | 瓦杜兹 | 0:1 | 奥德 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-182AF214F44F0722 | [第59840行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59840) |
| 2017-07-21 | 奥德 | 1:0 | 瓦杜兹 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-3F98785D28798272 | [第60347行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60347) |

<a id="e218"></a>
### E218

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-27 | 马里博尔 | 1:0 | 哈夫纳夫 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-29903 | [第60785行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60785) |
| 2017-08-03 | 哈夫纳夫 | 0:1 | 马里博尔 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-30082 | [第61273行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61273) |

<a id="e219"></a>
### E219

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-28 | 索尔纳 | 1:1 | 布拉加 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-29921 | [第60816行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60816) |
| 2017-08-04 | 布拉加 | 1:1 | 索尔纳 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-30125 | [第61324行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61324) |

<a id="e220"></a>
### E220

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-28 | 埃因霍温 | 0:1 | 奥西耶克 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-29923 | [第60818行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60818) |
| 2017-08-04 | 奥西耶克 | 1:0 | 埃因霍温 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-30121 | [第61323行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61323) |

<a id="e221"></a>
### E221

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-28 | 埃弗顿 | 1:0 | 鲁容贝罗 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-1DC830D216E21ADD | [第60840行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60840) |
| 2017-08-04 | 鲁容贝罗 | 0:1 | 埃弗顿 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-0A3CA873585920D7 | [第61331行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61331) |

<a id="e222"></a>
### E222

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-07-28 | 特马卡比 | 1:0 | 帕尼奥尼 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-4FAA428102A16491 | [第60846行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60846) |
| 2017-08-04 | 帕尼奥尼 | 0:1 | 特马卡比 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-7F2BFB5C2D144DB3 | [第61350行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61350) |

<a id="e223"></a>
### E223

双方队名已一致，无需为此新增队名映射。

日期差7天；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-12-14 | 里尔 | 1:1 | 尼斯 | 法联赛杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2666517 | [第70582行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:70582) |
| 2017-12-21 | 里尔 | 1:1 | 尼斯 | 法甲 / LIGUE_1 | FOTMOB-2525272 | [第70883行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:70883) |

<a id="e224"></a>
### E224

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-02-01 | 沙佩科恩斯 | 0:1 | 蒙国民 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-36207 | [第72759行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:72759) |
| 2018-02-08 | 蒙国民 | 1:0 | 沙佩科恩斯 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-36417 | [第73197行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:73197) |

<a id="e225"></a>
### E225

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-02-01 | 吉雷松体育 | 1:2 | 费内巴切 | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-983D7465F0A39004 | [第72782行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:72782) |
| 2018-02-08 | 费内巴切 | 2:1 | 吉雷松体育 | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-A13FB06CF71E694C | [第73216行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:73216) |

<a id="e226"></a>
### E226

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-02-28 | 新佩斯 | 2:0 | 保克什 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-10CE987B90EE56A8 | [第74472行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:74472) |
| 2018-03-07 | 保克什 | 0:2 | 新佩斯 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-732A4A8DFDD11FDB | [第74945行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:74945) |

<a id="e227"></a>
### E227

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-04-18 | 索菲亚中央陆军 | 2:2 | 索列夫 | 保超 / CLUB_OFFICIAL_OTHER | FOTMOB-2738096 | [第77783行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:77783) |
| 2018-04-25 | 索列夫 | 2:2 | 索菲亚中央陆军 | 保杯 / CLUB_OFFICIAL_OTHER | FOTMOB-2684121 | [第78342行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:78342) |

<a id="e228"></a>
### E228

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-05-25 | 恩特拉 | 0:0 | 阿斯科利 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-2763450 | [第80278行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:80278) |
| 2018-06-01 | 阿斯科利 | 0:0 | 恩特拉 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-2763451 | [第80473行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:80473) |

<a id="e229"></a>
### E229

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-06-29 | 托尔B36 | 1:1 | St Joseph's (GIB) | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-F522EDEA03FA4D60 | [第81083行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81083) |
| 2018-07-06 | St Joseph's (GIB) | 1:1 | 托尔B36 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A7A80733D5A418E2 | [第81372行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81372) |

<a id="e230"></a>
### E230

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-13 | 鲁达普列 | 0:3 | 贝游击 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-D183ECBEC76BD2BB | [第81767行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:81767) |
| 2018-07-20 | 贝游击 | 3:0 | 鲁达普列 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-42D3ED53848163EA | [第82187行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82187) |

<a id="e231"></a>
### E231

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-27 | 北西兰 | 1:0 | 索尔纳 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-40602 | [第82731行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82731) |
| 2018-08-03 | 索尔纳 | 0:1 | 北西兰 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-40732 | [第83204行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83204) |

<a id="e232"></a>
### E232

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-27 | 阿伯丁 | 1:1 | 伯恩利 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-40603 | [第82732行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82732) |
| 2018-08-03 | 伯恩利 | 1:1 | 阿伯丁 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-40736 | [第83208行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83208) |

<a id="e233"></a>
### E233

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-27 | 索矿工 | 1:1 | 波兹南 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C37D175BAD2E9C7C | [第82776行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82776) |
| 2018-08-03 | 波兹南 | 1:1 | 索矿工 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-B16B8D5290B53B79 | [第83254行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83254) |

<a id="e234"></a>
### E234

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-07-27 | 佐加顿斯 | 1:1 | 马里乌波 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-F11AE92150569327 | [第82791行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82791) |
| 2018-08-03 | 马里乌波 | 1:1 | 佐加顿斯 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-40733 | [第83205行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83205) |

<a id="e235"></a>
### E235

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-08-08 | 贝红星 | 1:1 | 特斯巴达 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-86C445A4208AE329 | [第83669行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83669) |
| 2018-08-15 | 特斯巴达 | 1:1 | 贝红星 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-03A9B46E09226CE1 | [第84262行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84262) |

<a id="e236"></a>
### E236

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-08-10 | 伊斯坦布 | 0:0 | 伯恩利 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-40910 | [第83720行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83720) |
| 2018-08-17 | 伯恩利 | 0:0 | 伊斯坦布 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-41174 | [第84292行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84292) |

<a id="e237"></a>
### E237

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-08-10 | 索菲亚中央陆军 | 1:2 | 哥本哈根 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-82B550118B157040 | [第83754行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83754) |
| 2018-08-17 | 哥本哈根 | 2:1 | 索菲亚中央陆军 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-3ABA5A104AFCF436 | [第84312行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84312) |

<a id="e238"></a>
### E238

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-08-10 | 维迪斯 | 0:1 | 巴塞尔 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-F1C1687C23CB8AE4 | [第83771行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83771) |
| 2018-08-17 | 巴塞尔 | 1:0 | 维迪斯 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-93FC63EDCE064359 | [第84318行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84318) |

<a id="e239"></a>
### E239

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-08-24 | 亚特兰大 | 0:0 | 哥本哈根 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-41469 | [第84890行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:84890) |
| 2018-08-31 | 哥本哈根 | 0:0 | 亚特兰大 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-41761 | [第85459行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:85459) |

<a id="e240"></a>
### E240

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-12-06 | 巴兰基亚 | 1:1 | 巴拉纳竞技 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-45221 | [第91865行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:91865) |
| 2018-12-13 | 巴拉纳竞技 | 1:1 | 巴兰基亚 | 俱乐部杯 / CLUB_OFFICIAL_OTHER | EXCEL-45407 | [第92263行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:92263) |

<a id="e241"></a>
### E241

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-01-18 | 乌姆拉尼耶体育 | 1:0 | 费内巴切 | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-662DA6D0BB804261 | [第93657行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:93657) |
| 2019-01-25 | 费内巴切 | 0:1 | 乌姆拉尼耶体育 | 土耳其杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-077AE87251B9426A | [第94069行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94069) |

<a id="e242"></a>
### E242

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-03-08 | 塞维利亚 | 2:2 | 斯拉维亚 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-47095 | [第96554行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:96554) |
| 2019-03-15 | 斯拉维亚 | 2:2 | 塞维利亚 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-47288 | [第97050行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:97050) |

<a id="e243"></a>
### E243

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-05-03 | 法兰克福 | 1:1 | 切尔西 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-48482 | [第100086行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100086) |
| 2019-05-10 | 切尔西 | 1:1 | 法兰克福 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-48663 | [第100514行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100514) |

<a id="e244"></a>
### E244

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-06-19 | 上海上港 | 1:1 | 全北现代 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-49359 | [第102059行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102059) |
| 2019-06-26 | 全北现代 | 1:1 | 上海上港 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-49449 | [第102289行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:102289) |

<a id="e245"></a>
### E245

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-11 | 布拉迪斯 | 1:1 | 苏捷斯卡 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-769F94ECC96259E5 | [第103085行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103085) |
| 2019-07-18 | 苏捷斯卡 | 1:1 | 布拉迪斯 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-6DD6C1C70E8ABC31 | [第103530行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103530) |

<a id="e246"></a>
### E246

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-11 | 邓多克 | 0:0 | 里加FC | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-B256C59347F333D3 | [第103092行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103092) |
| 2019-07-18 | 里加FC | 0:0 | 邓多克 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-D0A5F50A10FBDACF | [第103539行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103539) |

<a id="e247"></a>
### E247

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 鲁容贝罗 | 0:2 | 索列夫 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-24FC46BB6CD610E5 | [第103114行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103114) |
| 2019-07-19 | 索列夫 | 2:0 | 鲁容贝罗 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-5103C31D60D1A8B0 | [第103574行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103574) |

<a id="e248"></a>
### E248

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | Akademija Pandev | 0:3 | 兹林尼 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-56008AFB6A435590 | [第103127行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103127) |
| 2019-07-19 | 兹林尼 | 3:0 | Akademija Pandev | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-6B8C27493D16DDD2 | [第103582行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103582) |

<a id="e249"></a>
### E249

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 阿伯丁 | 2:1 | 罗瓦涅米 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-6DB342BB828CD792 | [第103131行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103131) |
| 2019-07-19 | 罗瓦涅米 | 1:2 | 阿伯丁 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-49740 | [第103545行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103545) |

<a id="e250"></a>
### E250

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | Dunajska Streda | 1:1 | 克拉科维亚 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-CEB3B1F9072E3FC5 | [第103151行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103151) |
| 2019-07-19 | 克拉科维亚 | 1:1 | Dunajska Streda | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-BB8EAB7AED427B3D | [第103605行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103605) |

<a id="e251"></a>
### E251

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | Sabail FK | 2:3 | 克拉约瓦 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-CF5AF81F7734F2BD | [第103152行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103152) |
| 2019-07-19 | 克拉约瓦 | 3:2 | Sabail FK | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-20B3FAAA0EA68C15 | [第103557行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103557) |

<a id="e252"></a>
### E252

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-12 | 索矿工 | 1:0 | 爱尔兰人 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-EF8CA2C51308BB64 | [第103161行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103161) |
| 2019-07-19 | 爱尔兰人 | 0:1 | 索矿工 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-7EACAD39546C53C3 | [第103588行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103588) |

<a id="e253"></a>
### E253

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-24 | 河床 | 0:0 | 克鲁塞罗 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-49811 | [第103968行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103968) |
| 2019-07-31 | 克鲁塞罗 | 0:0 | 河床 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-49904 | [第104460行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104460) |

<a id="e254"></a>
### E254

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-26 | 卢塞恩 | 1:0 | 克拉克斯 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-337ADFEFDE80B6CF | [第104079行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104079) |
| 2019-08-02 | 克拉克斯 | 0:1 | 卢塞恩 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-40C6A32B93B10FDF | [第104561行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104561) |

<a id="e255"></a>
### E255

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-26 | 弗罗拉 | 1:2 | 法兰克福 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-6F753FBBFDD38411 | [第104091行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104091) |
| 2019-08-02 | 法兰克福 | 2:1 | 弗罗拉 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-424D4BBB3856B6BA | [第104562行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104562) |

<a id="e256"></a>
### E256

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-26 | 布达佩斯捍卫者 | 0:0 | 克拉约瓦 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-779A27820444A11F | [第104094行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104094) |
| 2019-08-02 | 克拉约瓦 | 0:0 | 布达佩斯捍卫者 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-26F5E4C7F8CBD6DF | [第104550行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104550) |

<a id="e257"></a>
### E257

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-07-26 | 乌德勒支 | 1:1 | 兹林尼 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-FD1BEBDA532C9B92 | [第104127行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104127) |
| 2019-08-02 | 兹林尼 | 1:1 | 乌德勒支 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-49930 | [第104532行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104532) |

<a id="e258"></a>
### E258

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-07 | 贝红星 | 1:1 | 哥本哈根 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-50047 | [第104964行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104964) |
| 2019-08-14 | 哥本哈根 | 1:1 | 贝红星 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-50203 | [第105471行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105471) |

<a id="e259"></a>
### E259

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-09 | 卢塞恩 | 0:3 | 西班牙人 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-50065 | [第105040行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105040) |
| 2019-08-16 | 西班牙人 | 3:0 | 卢塞恩 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-072950913F1E3E4B | [第105579行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105579) |

<a id="e260"></a>
### E260

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-09 | 埃凤凰 | 0:4 | 伍尔弗 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-54518665950B9ABE | [第105055行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105055) |
| 2019-08-16 | 伍尔弗 | 4:0 | 埃凤凰 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C66B429F15833795 | [第105602行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105602) |

<a id="e261"></a>
### E261

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-09 | 里耶卡 | 2:0 | 阿伯丁 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-9A01EFE13546EE33 | [第105066行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105066) |
| 2019-08-16 | 阿伯丁 | 0:2 | 里耶卡 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-50236 | [第105571行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105571) |

<a id="e262"></a>
### E262

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-09 | 普火车头 | 0:1 | 斯特拉斯 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-AA38010A5583F4E4 | [第105070行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105070) |
| 2019-08-16 | 斯特拉斯 | 1:0 | 普火车头 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-EE021EBAFA9367AE | [第105606行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105606) |

<a id="e263"></a>
### E263

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-09 | 特马卡比 | 1:2 | Suduva | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-E68660AC6F38C0BE | [第105079行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105079) |
| 2019-08-16 | Suduva | 2:1 | 特马卡比 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-BE3BC6D3B2ADB7CE | [第105600行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:105600) |

<a id="e264"></a>
### E264

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-23 | 阿尔克马 | 1:1 | 安特卫普 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-50421 | [第106083行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:106083) |
| 2019-08-30 | 安特卫普 | 1:1 | 阿尔克马 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-50620 | [第106571行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:106571) |

<a id="e265"></a>
### E265

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-08-23 | 费耶诺德 | 3:0 | 贝夏普尔 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-3E73BA9F6B5E65F7 | [第106105行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:106105) |
| 2019-08-30 | 贝夏普尔 | 0:3 | 费耶诺德 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-0B81D08CE3EE912C | [第106582行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:106582) |

<a id="e266"></a>
### E266

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-01-25 | 雷丁 | 1:1 | 加的夫城 | 英足总杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3249680 | [第114263行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:114263) |
| 2020-02-01 | 加的夫城 | 1:1 | 雷丁 | 英冠 / CLUB_OFFICIAL_OTHER | FOTMOB-3066192 | [第114613行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:114613) |

<a id="e267"></a>
### E267

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-02-13 | 贝凯什乔包 | 0:3 | 普斯卡什学院 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-D34EEC9B92E56FDC | [第115364行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:115364) |
| 2020-02-20 | 普斯卡什学院 | 3:0 | 贝凯什乔包 | 匈牙利杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-CC71F572FEF914A0 | [第115747行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:115747) |

<a id="e268"></a>
### E268

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-05-15 | 保克什 | 1:1 | 卡波斯瓦里 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-49A7FACAC56DCDD9 | [第116985行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:116985) |
| 2020-05-22 | 卡波斯瓦里 | 1:1 | 保克什 | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4BE01F6A0C03BB2E | [第117046行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117046) |

<a id="e269"></a>
### E269

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2020-06-17 | FC Yerevan | 0:3 | 埃凤凰 | 亚美尼超 / CLUB_OFFICIAL_OTHER | FUTBOL24-0924C0B7A638F3EB | [第117744行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:117744) |
| 2020-06-24 | 埃凤凰 | 3:0 | FC Yerevan | 亚美尼超 / CLUB_OFFICIAL_OTHER | FUTBOL24-4D2D62D7553DF070 | [第118113行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:118113) |

<a id="e270"></a>
### E270

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-02-19 | 里尔 | 1:2 | 阿贾克斯 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C607D97306F96F9B | [第132108行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:132108) |
| 2021-02-26 | 阿贾克斯 | 2:1 | 里尔 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-59066 | [第132518行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:132518) |

<a id="e271"></a>
### E271

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-03-10 | Barry Town | 0:1 | Penybont | 威尔士超 / CLUB_OFFICIAL_OTHER | FOTMOB-3425708 | [第133445行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133445) |
| 2021-03-17 | Penybont | 1:0 | Barry Town | 威尔士超 / CLUB_OFFICIAL_OTHER | FOTMOB-3425710 | [第133871行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133871) |

<a id="e272"></a>
### E272

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-03-12 | 基迪纳摩 | 0:2 | 比利亚雷 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-59376 | [第133488行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133488) |
| 2021-03-19 | 比利亚雷 | 2:0 | 基迪纳摩 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-59514 | [第133917行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:133917) |

<a id="e273"></a>
### E273

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-04-09 | 格拉纳达 | 0:2 | 曼联 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-59786 | [第134907行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:134907) |
| 2021-04-16 | 曼联 | 2:0 | 格拉纳达 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-59882 | [第135352行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:135352) |

<a id="e274"></a>
### E274

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-04-22 | Sumqayit | 0:0 | 卡拉巴赫 | 阿塞杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-E4C0410DB33EAA69 | [第135924行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:135924) |
| 2021-04-29 | 卡拉巴赫 | 0:0 | Sumqayit | 阿塞杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-9E4C83C68987F25B | [第136397行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:136397) |

<a id="e275"></a>
### E275

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-08 | Dinamo Tbilisi | 1:2 | 纳夫兹 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-EEFFA7DC658D9E9F | [第139375行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139375) |
| 2021-07-15 | 纳夫兹 | 2:1 | Dinamo Tbilisi | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-959CBDAF4617CE90 | [第139733行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139733) |

<a id="e276"></a>
### E276

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-09 | FK Velez Mostar | 2:1 | 高利宁 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3608964 | [第139393行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139393) |
| 2021-07-16 | 高利宁 | 1:2 | FK Velez Mostar | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3608965 | [第139760行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139760) |

<a id="e277"></a>
### E277

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-09 | 马里博尔 | 1:0 | Urartu FC | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3608980 | [第139399行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139399) |
| 2021-07-16 | Urartu FC | 0:1 | 马里博尔 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3608981 | [第139768行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139768) |

<a id="e278"></a>
### E278

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-09 | 巴拉 | 0:1 | 拉恩 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3608984 | [第139401行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139401) |
| 2021-07-16 | 拉恩 | 1:0 | 巴拉 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3608985 | [第139770行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139770) |

<a id="e279"></a>
### E279

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-14 | 博卡 | 0:0 | 米内罗竞技 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-61145 | [第139647行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139647) |
| 2021-07-21 | 米内罗竞技 | 0:0 | 博卡 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-61219 | [第140114行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140114) |

<a id="e280"></a>
### E280

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-15 | 天主大学 | 0:1 | 帕尔梅拉斯 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-61152 | [第139716行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139716) |
| 2021-07-22 | 帕尔梅拉斯 | 1:0 | 天主大学 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-61244 | [第140184行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140184) |

<a id="e281"></a>
### E281

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-16 | 亚奥林 | 0:0 | 巴西国际 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-61158 | [第139744行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139744) |
| 2021-07-23 | 巴西国际 | 0:0 | 亚奥林 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-61270 | [第140232行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140232) |

<a id="e282"></a>
### E282

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-22 | 奥林匹亚 | 1:0 | 纳夫兹 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-B960E64FA9967D6C | [第140224行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140224) |
| 2021-07-29 | 纳夫兹 | 0:1 | 奥林匹亚 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-CF70FB9854F6A991 | [第140728行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140728) |

<a id="e283"></a>
### E283

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | 库奥皮奥 | 2:2 | 波尔塔瓦 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610482 | [第140236行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140236) |
| 2021-07-30 | 波尔塔瓦 | 2:2 | 库奥皮奥 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610483 | [第140740行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140740) |

<a id="e284"></a>
### E284

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | 佩特罗库 | 0:1 | 锡瓦斯 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610496 | [第140241行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140241) |
| 2021-07-30 | 锡瓦斯 | 1:0 | 佩特罗库 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610497 | [第140746行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140746) |

<a id="e285"></a>
### E285

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | AEL利马索尔 | 1:0 | Vllaznia | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610498 | [第140242行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140242) |
| 2021-07-30 | Vllaznia | 0:1 | AEL利马索尔 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610499 | [第140747行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140747) |

<a id="e286"></a>
### E286

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | 比尔森 | 2:1 | Dynamo Brest | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610544 | [第140263行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140263) |
| 2021-07-30 | Dynamo Brest | 1:2 | 比尔森 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610545 | [第140768行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140768) |

<a id="e287"></a>
### E287

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | Valur | 0:3 | 博德闪耀 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610688 | [第140280行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140280) |
| 2021-07-30 | 博德闪耀 | 3:0 | Valur | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3610689 | [第140786行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140786) |

<a id="e288"></a>
### E288

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-07-23 | Suduva | 0:0 | 琴斯托霍 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3611228 | [第140281行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140281) |
| 2021-07-30 | 琴斯托霍 | 0:0 | Suduva | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3611229 | [第140787行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:140787) |

<a id="e289"></a>
### E289

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-04 | 亨克 | 1:2 | 顿矿工 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-61434 | [第141099行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141099) |
| 2021-08-11 | 顿矿工 | 2:1 | 亨克 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-61511 | [第141604行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141604) |

<a id="e290"></a>
### E290

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-04 | 马尔默 | 2:1 | 流浪者 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-BE824F3AE73C3D14 | [第141149行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141149) |
| 2021-08-11 | 流浪者 | 1:2 | 马尔默 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-61515 | [第141608行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141608) |

<a id="e291"></a>
### E291

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-06 | 琴斯托霍 | 0:0 | 喀山 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3664647 | [第141207行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141207) |
| 2021-08-13 | 喀山 | 0:0 | 琴斯托霍 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3664679 | [第141695行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141695) |

<a id="e292"></a>
### E292

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-06 | 卢塞恩 | 0:3 | 费耶诺德 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3664651 | [第141211行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141211) |
| 2021-08-13 | 费耶诺德 | 3:0 | 卢塞恩 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3664683 | [第141698行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:141698) |

<a id="e293"></a>
### E293

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-08-20 | 里加FC | 1:1 | 红色小鬼 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3687210 | [第142199行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:142199) |
| 2021-08-27 | 红色小鬼 | 1:1 | 里加FC | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3687232 | [第142643行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:142643) |

<a id="e294"></a>
### E294

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-02-18 | 费内巴切 | 2:3 | 斯拉维亚 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-3780900 | [第151583行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:151583) |
| 2022-02-25 | 斯拉维亚 | 3:2 | 费内巴切 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-3780908 | [第152046行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:152046) |

<a id="e295"></a>
### E295

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-03-11 | 马赛 | 2:1 | 巴塞尔 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-3835636 | [第152965行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:152965) |
| 2022-03-18 | 巴塞尔 | 1:2 | 马赛 | 欧协联 / CLUB_OFFICIAL_OTHER | EXCEL-63667 | [第153405行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:153405) |

<a id="e296"></a>
### E296

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-06-29 | 科林蒂安 | 0:0 | 博卡 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-64930 | [第158204行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158204) |
| 2022-07-06 | 博卡 | 0:0 | 科林蒂安 | 解放者杯 / CLUB_OFFICIAL_OTHER | EXCEL-65021 | [第158571行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158571) |

<a id="e297"></a>
### E297

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-07 | 布拉迪斯 | 0:0 | Dinamo Batumi | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-C8A075CBC4E573DA | [第158666行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158666) |
| 2022-07-14 | Dinamo Batumi | 0:0 | 布拉迪斯 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-40E813B1893774B7 | [第159090行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159090) |

<a id="e298"></a>
### E298

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-08 | FC Sfintul Gheorghe | 1:2 | 穆拉 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3899194 | [第158686行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158686) |
| 2022-07-15 | 穆拉 | 2:1 | FC Sfintul Gheorghe | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3899195 | [第159128行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159128) |

<a id="e299"></a>
### E299

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-08 | 奥林 | 1:1 | 迪弗当日 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3899208 | [第158692行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158692) |
| 2022-07-15 | 迪弗当日 | 1:1 | 奥林 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3899209 | [第159134行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159134) |

<a id="e300"></a>
### E300

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-08 | 德里城 | 0:2 | 里加FC | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3899226 | [第158700行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:158700) |
| 2022-07-15 | 里加FC | 2:0 | 德里城 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3899227 | [第159142行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159142) |

<a id="e301"></a>
### E301

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-22 | Gzira United | 2:2 | Radnicki Nis | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3900061 | [第159642行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159642) |
| 2022-07-29 | Radnicki Nis | 2:2 | Gzira United | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3900062 | [第160117行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160117) |

<a id="e302"></a>
### E302

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-22 | FK Velez Mostar | 0:1 | Hamrun Spartans | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3900196 | [第159673行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159673) |
| 2022-07-29 | Hamrun Spartans | 1:0 | FK Velez Mostar | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3900197 | [第160152行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160152) |

<a id="e303"></a>
### E303

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-22 | Suduva | 0:1 | 维堡 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3967454 | [第159678行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159678) |
| 2022-07-29 | 维堡 | 1:0 | Suduva | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3900106 | [第160138行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160138) |

<a id="e304"></a>
### E304

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-07-26 | 北雪平 | 0:2 | 哥德堡 | 瑞超 / SWEDISH_ALLSVENSKAN | EXCEL-65276 | [第160012行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160012) |
| 2022-08-02 | 哥德堡 | 2:0 | 北雪平 | 瑞超 / SWEDISH_ALLSVENSKAN | EXCEL-65374 | [第160509行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160509) |

<a id="e305"></a>
### E305

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-03 | 谢里夫 | 1:2 | 比尔森 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-65381 | [第160526行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160526) |
| 2022-08-10 | 比尔森 | 2:1 | 谢里夫 | 欧冠 / CHAMPIONS_LEAGUE | EXCEL-65527 | [第161050行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161050) |

<a id="e306"></a>
### E306

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-05 | 索尔纳 | 1:1 | KF Shkendija | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3960346 | [第160636行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160636) |
| 2022-08-12 | KF Shkendija | 1:1 | 索尔纳 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3960347 | [第161137行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161137) |

<a id="e307"></a>
### E307

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-05 | Sepsi OSK | 1:3 | 佐加顿斯 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3960380 | [第160650行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160650) |
| 2022-08-12 | 佐加顿斯 | 3:1 | Sepsi OSK | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-3960381 | [第161151行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161151) |

<a id="e308"></a>
### E308

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-05 | 奥林匹亚 | 1:1 | 布拉迪斯 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-E5590D3E27B270A3 | [第160681行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160681) |
| 2022-08-12 | 布拉迪斯 | 1:1 | 奥林匹亚 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-26967870D6732145 | [第161159行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161159) |

<a id="e309"></a>
### E309

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-19 | 根特 | 0:2 | 奥莫尼亚 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-04A471591F1BC994 | [第161609行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161609) |
| 2022-08-26 | 奥莫尼亚 | 2:0 | 根特 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-65841 | [第162062行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:162062) |

<a id="e310"></a>
### E310

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-19 | 埃凤凰 | 0:0 | 谢里夫 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-145AF024FDD027DA | [第161610行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161610) |
| 2022-08-26 | 谢里夫 | 0:0 | 埃凤凰 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-9345426F6E94E316 | [第162100行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:162100) |

<a id="e311"></a>
### E311

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-08-19 | 利阿波罗 | 1:1 | 奥林匹亚 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-FD86BACF71035255 | [第161623行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:161623) |
| 2022-08-26 | 奥林匹亚 | 1:1 | 利阿波罗 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-F725125936D0FCB6 | [第162104行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:162104) |

<a id="e312"></a>
### E312

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-10-07 | 马尔默 | 0:1 | 柏林联合 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-66276 | [第164609行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:164609) |
| 2022-10-14 | 柏林联合 | 1:0 | 马尔默 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-66351 | [第165058行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:165058) |

<a id="e313"></a>
### E313

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-10-07 | 锡尔克堡 | 5:0 | 布星 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4010222 | [第164620行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:164620) |
| 2022-10-14 | 布星 | 0:5 | 锡尔克堡 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4010223 | [第165070行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:165070) |

<a id="e314"></a>
### E314

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2022-10-07 | 中日德兰 | 2:2 | 费耶诺德 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A1F4D272730FBFE5 | [第164644行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:164644) |
| 2022-10-14 | 费耶诺德 | 2:2 | 中日德兰 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-8A51CCB70ADC7DA6 | [第165099行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:165099) |

<a id="e315"></a>
### E315

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-03-04 | FC Baranovichi | 0:0 | Dinamo Brest | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-4E8CCCD67CDC692A | [第172248行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:172248) |
| 2023-03-11 | Dinamo Brest | 0:0 | FC Baranovichi | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-ED1F9AD37CBD9AB7 | [第172704行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:172704) |

<a id="e316"></a>
### E316

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-03-10 | 勒沃库森 | 2:0 | 费伦茨 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-67981 | [第172541行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:172541) |
| 2023-03-17 | 费伦茨 | 0:2 | 勒沃库森 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-68083 | [第172968行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:172968) |

<a id="e317"></a>
### E317

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-03-10 | 巴塞尔 | 2:2 | 布拉迪斯 | 欧协联 / CLUB_OFFICIAL_OTHER | EXCEL-67991 | [第172551行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:172551) |
| 2023-03-17 | 布拉迪斯 | 2:2 | 巴塞尔 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4128791 | [第172981行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:172981) |

<a id="e318"></a>
### E318

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-05-12 | 尤文图斯 | 1:1 | 塞维利亚 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-68757 | [第176104行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:176104) |
| 2023-05-19 | 塞维利亚 | 1:1 | 尤文图斯 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-68836 | [第176504行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:176504) |

<a id="e319"></a>
### E319

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-12 | 奥林 | 2:1 | Valmiera FC | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-C23452556EDF9656 | [第178524行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178524) |
| 2023-07-19 | Valmiera FC | 1:2 | 奥林 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-4702341B93C5C33A | [第178968行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178968) |

<a id="e320"></a>
### E320

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-13 | 托尔B36 | 0:0 | Paide Linnameeskond | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200115 | [第178556行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178556) |
| 2023-07-20 | Paide Linnameeskond | 0:0 | 托尔B36 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200125 | [第179028行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179028) |

<a id="e321"></a>
### E321

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-14 | KA | 2:0 | Connah's Quay Nomads | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200123 | [第178601行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178601) |
| 2023-07-21 | Connah's Quay Nomads | 0:2 | KA | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200122 | [第179079行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179079) |

<a id="e322"></a>
### E322

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-28 | FCV Farul Constanta | 3:2 | Urartu FC | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200938 | [第179596行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179596) |
| 2023-08-04 | Urartu FC | 2:3 | FCV Farul Constanta | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200939 | [第180052行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180052) |

<a id="e323"></a>
### E323

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-07-28 | 希腊人 | 2:1 | 伏伊伏丁 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200973 | [第179606行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179606) |
| 2023-08-04 | 伏伊伏丁 | 1:2 | 希腊人 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4200974 | [第180063行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180063) |

<a id="e324"></a>
### E324

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-11 | 卡拉巴赫 | 2:1 | 赫尔辛基 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-69529 | [第180538行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180538) |
| 2023-08-18 | 赫尔辛基 | 1:2 | 卡拉巴赫 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-69594 | [第180990行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180990) |

<a id="e325"></a>
### E325

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-11 | 布鲁日 | 5:1 | KA | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4268965 | [第180572行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180572) |
| 2023-08-18 | KA | 1:5 | 布鲁日 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4268997 | [第181030行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181030) |

<a id="e326"></a>
### E326

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-11 | 博德闪耀 | 3:0 | 埃凤凰 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4268966 | [第180573行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180573) |
| 2023-08-18 | 埃凤凰 | 0:3 | 博德闪耀 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4268998 | [第181031行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181031) |

<a id="e327"></a>
### E327

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2023-08-25 | Sepsi OSK | 2:2 | 博德闪耀 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4287942 | [第181500行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181500) |
| 2023-09-01 | 博德闪耀 | 2:2 | Sepsi OSK | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4287964 | [第181948行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:181948) |

<a id="e328"></a>
### E328

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-02-16 | 奥林匹亚 | 1:0 | 费伦茨 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4384283 | [第190971行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:190971) |
| 2024-02-23 | 费伦茨 | 0:1 | 奥林匹亚 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4384291 | [第191410行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191410) |

<a id="e329"></a>
### E329

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-02-16 | 塞帕汉 | 1:3 | 利雅新月 | 亚冠精英 / CLUB_OFFICIAL_OTHER | FOTMOB-4391112 | [第190976行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:190976) |
| 2024-02-23 | 利雅新月 | 3:1 | 塞帕汉 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-71356 | [第191394行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191394) |

<a id="e330"></a>
### E330

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-02-16 | 费耶诺德 | 1:1 | 罗马 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-EEC1BC24E279DFC9 | [第191003行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191003) |
| 2024-02-23 | 罗马 | 1:1 | 费耶诺德 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-71358 | [第191395行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:191395) |

<a id="e331"></a>
### E331

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-03-06 | 利雅新月 | 2:0 | 吉达联合 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-71523 | [第192223行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:192223) |
| 2024-03-13 | 吉达联合 | 0:2 | 利雅新月 | 亚冠 / CLUB_OFFICIAL_OTHER | EXCEL-71612 | [第192647行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:192647) |

<a id="e332"></a>
### E332

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-03-08 | 塞尔维特 | 0:0 | 比尔森 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4430919 | [第192273行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:192273) |
| 2024-03-15 | 比尔森 | 0:0 | 塞尔维特 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4430927 | [第192702行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:192702) |

<a id="e333"></a>
### E333

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-04-03 | 伏伊伏丁 | 1:1 | Cukaricki | 塞超 / CLUB_OFFICIAL_OTHER | FOTMOB-4271832 | [第193661行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:193661) |
| 2024-04-10 | Cukaricki | 1:1 | 伏伊伏丁 | 塞杯 / CLUB_OFFICIAL_OTHER | FOTMOB-4390655 | [第194104行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:194104) |

<a id="e334"></a>
### E334

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-04-12 | 比尔森 | 0:0 | 佛罗伦萨 | 欧协联 / CLUB_OFFICIAL_OTHER | EXCEL-71951 | [第194137行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:194137) |
| 2024-04-19 | 佛罗伦萨 | 0:0 | 比尔森 | 欧协联 / CLUB_OFFICIAL_OTHER | EXCEL-72043 | [第194567行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:194567) |

<a id="e335"></a>
### E335

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-05-05 | Kralove | 3:1 | 奥洛穆茨 | 捷甲 / CLUB_OFFICIAL_OTHER | FOTMOB-4470836 | [第195727行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:195727) |
| 2024-05-12 | 奥洛穆茨 | 1:3 | Kralove | 捷甲 / CLUB_OFFICIAL_OTHER | FOTMOB-4470837 | [第196114行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:196114) |

<a id="e336"></a>
### E336

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-05-17 | FC Botosani | 1:0 | CS Mioveni | 罗甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-E97A32D00FA04EA5 | [第196327行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:196327) |
| 2024-05-24 | CS Mioveni | 0:1 | FC Botosani | 罗甲 / CLUB_OFFICIAL_OTHER | FUTBOL24-3068D048D3504E10 | [第196698行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:196698) |

<a id="e337"></a>
### E337

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-07-26 | SC Dnipro-1 | 0:3 | Puskas FC Academy | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4509846 | [第199103行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199103) |
| 2024-08-02 | Puskas FC Academy | 3:0 | SC Dnipro-1 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4509847 | [第199617行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:199617) |

<a id="e338"></a>
### E338

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-09 | St. Patrick's Athletic | 1:0 | 萨巴赫 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4570440 | [第200083行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200083) |
| 2024-08-16 | 萨巴赫 | 0:1 | St. Patrick's Athletic | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4570444 | [第200546行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200546) |

<a id="e339"></a>
### E339

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-09 | Maccabi Petach Tikva | 0:1 | 克卢日 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4570459 | [第200091行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200091) |
| 2024-08-16 | 克卢日 | 1:0 | Maccabi Petach Tikva | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4570463 | [第200555行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200555) |

<a id="e340"></a>
### E340

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-09 | 锡尔克堡 | 2:2 | 根特 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4570460 | [第200092行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200092) |
| 2024-08-16 | 根特 | 2:2 | 锡尔克堡 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4570464 | [第200556行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:200556) |

<a id="e341"></a>
### E341

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-23 | 比尔森 | 1:0 | 哈茨 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-73420 | [第201010行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201010) |
| 2024-08-30 | 哈茨 | 0:1 | 比尔森 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-73522 | [第201475行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201475) |

<a id="e342"></a>
### E342

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-23 | 佐加顿斯 | 1:0 | 马里博尔 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4591254 | [第201030行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201030) |
| 2024-08-30 | 马里博尔 | 0:1 | 佐加顿斯 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4591255 | [第201492行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201492) |

<a id="e343"></a>
### E343

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-23 | 费伦茨 | 0:0 | 巴战士 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-1D270E0DB107CEFE | [第201048行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201048) |
| 2024-08-30 | 巴战士 | 0:0 | 费伦茨 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-C51AA33BA78BA753 | [第201515行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201515) |

<a id="e344"></a>
### E344

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-08-23 | 明迪纳摩 | 0:1 | 安德莱 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-52138284578FE362 | [第201050行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201050) |
| 2024-08-30 | 安德莱 | 1:0 | 明迪纳摩 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-73520 | [第201473行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:201473) |

<a id="e345"></a>
### E345

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-01 | 首尔衣恋 | 1:2 | 全北现代 | 韩职 / K_LEAGUE_1 | EXCEL-74548 | [第206805行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206805) |
| 2024-12-08 | 全北现代 | 2:1 | 首尔衣恋 | 韩职 / K_LEAGUE_1 | EXCEL-74676 | [第207270行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:207270) |

<a id="e346"></a>
### E346

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-01 | 首尔衣恋 | 1:2 | 全北现代 | 韩职 / K_LEAGUE_1 | EXCEL-74548 | [第206805行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206805) |
| 2024-12-08 | 全北现代 | 2:1 | 首尔衣恋 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679983 | [第207353行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:207353) |

<a id="e347"></a>
### E347

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；映射赛事分类不同；原始赛事名不同；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-01 | 首尔衣恋 | 1:2 | 全北现代 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679982 | [第206898行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206898) |
| 2024-12-08 | 全北现代 | 2:1 | 首尔衣恋 | 韩职 / K_LEAGUE_1 | EXCEL-74676 | [第207270行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:207270) |

<a id="e348"></a>
### E348

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-01 | 首尔衣恋 | 1:2 | 全北现代 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679982 | [第206898行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206898) |
| 2024-12-08 | 全北现代 | 2:1 | 首尔衣恋 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679983 | [第207353行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:207353) |

<a id="e349"></a>
### E349

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-02-14 | 托波拉 | 1:3 | 比亚韦 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4695831 | [第210464行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:210464) |
| 2025-02-21 | 比亚韦 | 3:1 | 托波拉 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4695839 | [第210886行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:210886) |

<a id="e350"></a>
### E350

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-03-07 | 巴战士 | 1:1 | 维快速 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4737745 | [第211749行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:211749) |
| 2025-03-14 | 维快速 | 1:1 | 巴战士 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-4737746 | [第212206行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:212206) |

<a id="e351"></a>
### E351

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-04-11 | 里昂 | 2:2 | 曼联 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-BF29764A6B8E293F | [第213618行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:213618) |
| 2025-04-18 | 曼联 | 2:2 | 里昂 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-29AA11FB79EF2BB2 | [第214049行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:214049) |

<a id="e352"></a>
### E352

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-05-02 | 维堡 | 0:1 | 哥本哈根 | 丹麦杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-299162E14C4743B0 | [第214858行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:214858) |
| 2025-05-09 | 哥本哈根 | 1:0 | 维堡 | 丹麦杯 / CLUB_OFFICIAL_OTHER | FUTBOL24-6785CF27A9CC936B | [第215278行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:215278) |

<a id="e353"></a>
### E353

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-09 | Saburtalo | 1:3 | 马尔默 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-1F5A3764801892DC | [第217565行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217565) |
| 2025-07-16 | 马尔默 | 3:1 | Saburtalo | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-D3026C62B4799001 | [第217992行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217992) |

<a id="e354"></a>
### E354

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-09 | 利瓦迪亚 | 0:1 | 里加足校 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-CBBB512FA949936C | [第217592行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217592) |
| 2025-07-16 | 里加足校 | 1:0 | 利瓦迪亚 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-5C8C2C428C6F5C10 | [第217974行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217974) |

<a id="e355"></a>
### E355

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-11 | 索列夫 | 0:0 | 贝夏普尔 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-475AC4E5C39ED563 | [第217663行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217663) |
| 2025-07-18 | 贝夏普尔 | 0:0 | 索列夫 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-4ECA917A2E40C8AF | [第218085行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218085) |

<a id="e356"></a>
### E356

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-11 | 华沙军团 | 1:0 | 阿克托比 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-475E624D54D81D00 | [第217664行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:217664) |
| 2025-07-18 | 阿克托比 | 0:1 | 华沙军团 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-5E9DD7C1B14DF79D | [第218087行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218087) |

<a id="e357"></a>
### E357

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-23 | Hamrun Spartans | 0:3 | 基迪纳摩 | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-53145AC6E6E5212D | [第218456行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218456) |
| 2025-07-30 | 基迪纳摩 | 3:0 | Hamrun Spartans | 欧冠 / CHAMPIONS_LEAGUE | FUTBOL24-377D787F15EFE375 | [第218949行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218949) |

<a id="e358"></a>
### E358

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-25 | 索列夫 | 0:0 | 布拉加 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-77365 | [第218529行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218529) |
| 2025-08-01 | 布拉加 | 0:0 | 索列夫 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-77413 | [第219029行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219029) |

<a id="e359"></a>
### E359

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-25 | 邓迪联 | 1:0 | UNA Strassen | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4814693 | [第218544行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218544) |
| 2025-08-01 | UNA Strassen | 0:1 | 邓迪联 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4814694 | [第219042行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219042) |

<a id="e360"></a>
### E360

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-07-25 | 卢加诺 | 0:0 | 克卢日 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-161E7C5EBCA3C8C9 | [第218575行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:218575) |
| 2025-08-01 | 克卢日 | 0:0 | 卢加诺 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-1F47DC5627D1E005 | [第219079行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219079) |

<a id="e361"></a>
### E361

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-08 | 塞萨洛 | 0:0 | 沃尔夫斯 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-77458 | [第219497行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219497) |
| 2025-08-15 | 沃尔夫斯 | 0:0 | 塞萨洛 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-77541 | [第219946行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219946) |

<a id="e362"></a>
### E362

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-08 | 维快速 | 2:2 | 邓迪联 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4885206 | [第219507行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219507) |
| 2025-08-15 | 邓迪联 | 2:2 | 维快速 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4885236 | [第219962行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219962) |

<a id="e363"></a>
### E363

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-08 | 帕纳辛纳 | 0:0 | 顿矿工 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-334E11889F371DFF | [第219532行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219532) |
| 2025-08-15 | 顿矿工 | 0:0 | 帕纳辛纳 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-F378608C6DD310AA | [第219997行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:219997) |

<a id="e364"></a>
### E364

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-08-22 | 顿矿工 | 1:1 | 塞尔维特 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4912640 | [第220446行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:220446) |
| 2025-08-29 | 塞尔维特 | 1:1 | 顿矿工 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-4912664 | [第220882行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:220882) |

<a id="e365"></a>
### E365

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-20 | Drita | 2:3 | 采列 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-5141721 | [第230859行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230859) |
| 2026-02-27 | 采列 | 3:2 | Drita | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-5141729 | [第231291行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231291) |

<a id="e366"></a>
### E366

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；不同来源；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-02-20 | 布兰 | 0:1 | 博洛尼亚 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-EF70A0DE9D9D602B | [第230880行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230880) |
| 2026-02-27 | 博洛尼亚 | 1:0 | 布兰 | 欧罗巴 / EUROPA_LEAGUE | EXCEL-80744 | [第231275行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231275) |

<a id="e367"></a>
### E367

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-04 | 大阪钢巴 | 1:1 | 叻武里 | 亚冠乙 / CLUB_OFFICIAL_OTHER | EXCEL-80864 | [第231687行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:231687) |
| 2026-03-11 | 叻武里 | 1:1 | 大阪钢巴 | 亚冠乙 / CLUB_OFFICIAL_OTHER | EXCEL-80970 | [第232149行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232149) |

<a id="e368"></a>
### E368

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-03-13 | 佛罗伦萨 | 2:1 | 琴斯托霍 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-5206221 | [第232204行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232204) |
| 2026-03-20 | 琴斯托霍 | 1:2 | 佛罗伦萨 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-5206229 | [第232659行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232659) |

<a id="e369"></a>
### E369

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-05-01 | 巴列卡诺 | 1:0 | 斯特拉斯 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-5206268 | [第234973行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:234973) |
| 2026-05-08 | 斯特拉斯 | 0:1 | 巴列卡诺 | 欧协联 / CLUB_OFFICIAL_OTHER | FOTMOB-5206270 | [第235353行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:235353) |

<a id="e370"></a>
### E370

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-05-16 | 巴里 | 0:0 | Südtirol | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-5496954 | [第235843行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:235843) |
| 2026-05-23 | Südtirol | 0:0 | 巴里 | 意乙 / CLUB_OFFICIAL_OTHER | FOTMOB-5496955 | [第236189行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236189) |

<a id="e371"></a>
### E371

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-06-28 | 乌利套 | 0:1 | 奇姆肯特 | 哈萨超 / CLUB_OFFICIAL_OTHER | FUTBOL24-413C0E320E8BD284 | [第236867行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236867) |
| 2026-07-05 | 奇姆肯特 | 1:0 | 乌利套 | 哈萨超 / CLUB_OFFICIAL_OTHER | FUTBOL24-B04C15D02364874B | [第237242行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237242) |

<a id="e372"></a>
### E372

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | Caernarfon | 0:5 | 利瓦迪亚 | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-5786616 | [第237404行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237404) |
| 2026-07-17 | 利瓦迪亚 | 5:0 | Caernarfon | 欧协联资格赛 / CLUB_OFFICIAL_OTHER | FOTMOB-5786641 | [第237889行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237889) |

<a id="e373"></a>
### E373

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | 卡拉巴赫 | 3:0 | IF Vestri | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-8B05DDB67FC53B37 | [第237435行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237435) |
| 2026-07-17 | IF Vestri | 0:3 | 卡拉巴赫 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-2461FBE9912D6F47 | [第237911行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237911) |

<a id="e374"></a>
### E374

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-10 | 基迪纳摩 | 0:0 | U Cluj | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-9B1FFAD923BE9ABD | [第237437行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237437) |
| 2026-07-17 | U Cluj | 0:0 | 基迪纳摩 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-65E263D268A6F717 | [第237917行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237917) |

<a id="e375"></a>
### E375

双方队名已一致，无需为此新增队名映射。

日期差7天；主客顺序相反；可能为主客两回合或短期再赛，不计核心疑似数

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-24 | 卡拉巴赫 | 0:0 | 索菲亚中央陆军 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-CBEB41B4585E9B66 | [第238428行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238428) |
| 2026-07-31 | 索菲亚中央陆军 | 0:0 | 卡拉巴赫 | 欧罗巴 / EUROPA_LEAGUE | FUTBOL24-A0400EB6DCADE256 | [第238818行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238818) |

## T 同日同队同分但赛事分类不同：17对

<a id="t001"></a>
### T001

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-11-17 | 江原FC | 0:0 | 城南FC | 韩职 / K_LEAGUE_1 | EXCEL-22778 | [第46449行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:46449) |
| 2016-11-17 | 江原FC | 0:0 | 城南FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2410766 | [第46452行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:46452) |

<a id="t002"></a>
### T002

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2016-11-20 | 城南FC | 1:1 | 江原FC | 韩职 / K_LEAGUE_1 | EXCEL-22920 | [第46653行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:46653) |
| 2016-11-20 | 城南FC | 1:1 | 江原FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2410767 | [第46729行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:46729) |

<a id="t003"></a>
### T003

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-11-22 | 釜山偶像 | 0:1 | 金泉尚武 | 韩职 / K_LEAGUE_1 | EXCEL-34276 | [第69185行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69185) |
| 2017-11-22 | 釜山偶像 | 0:1 | 金泉尚武 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2670961 | [第69202行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69202) |

<a id="t004"></a>
### T004

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2017-11-26 | 金泉尚武 | 0:1 | 釜山偶像 | 韩职 / K_LEAGUE_1 | EXCEL-34445 | [第69436行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69436) |
| 2017-11-26 | 金泉尚武 | 0:1 | 釜山偶像 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2671078 | [第69522行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:69522) |

<a id="t005"></a>
### T005

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-12-06 | 釜山偶像 | 1:3 | 首尔FC | 韩职 / K_LEAGUE_1 | EXCEL-45226 | [第91870行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:91870) |
| 2018-12-06 | 釜山偶像 | 1:3 | 首尔FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2941681 | [第91905行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:91905) |

<a id="t006"></a>
### T006

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2018-12-09 | 首尔FC | 1:1 | 釜山偶像 | 韩职 / K_LEAGUE_1 | EXCEL-45333 | [第92079行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:92079) |
| 2018-12-09 | 首尔FC | 1:1 | 釜山偶像 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-2941682 | [第92149行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:92149) |

<a id="t007"></a>
### T007

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-12-05 | 釜山偶像 | 0:0 | 庆南FC | 韩职 / K_LEAGUE_1 | EXCEL-52755 | [第112073行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:112073) |
| 2019-12-05 | 釜山偶像 | 0:0 | 庆南FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-3230772 | [第112106行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:112106) |

<a id="t008"></a>
### T008

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2019-12-08 | 庆南FC | 0:2 | 釜山偶像 | 韩职 / K_LEAGUE_1 | EXCEL-52846 | [第112262行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:112262) |
| 2019-12-08 | 庆南FC | 0:2 | 釜山偶像 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-3230773 | [第112348行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:112348) |

<a id="t009"></a>
### T009

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-12-12 | 江原FC | 4:1 | 大田市民 | 韩职 / K_LEAGUE_1 | EXCEL-62708 | [第148763行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:148763) |
| 2021-12-12 | 江原FC | 4:1 | 大田市民 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-3776103 | [第148843行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:148843) |

<a id="t010"></a>
### T010

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-11-28 | 忠南牙山 | 4:3 | 大邱FC | 韩职 / K_LEAGUE_1 | EXCEL-74469 | [第206619行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206619) |
| 2024-11-28 | 忠南牙山 | 4:3 | 大邱FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679971 | [第206626行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206626) |

<a id="t011"></a>
### T011

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-01 | 大邱FC | 2:1 | 忠南牙山 | 韩职 / K_LEAGUE_1 | EXCEL-74547 | [第206804行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206804) |
| 2024-12-01 | 大邱FC | 2:1 | 忠南牙山 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679972 | [第206897行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206897) |

<a id="t012"></a>
### T012

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-01 | 首尔衣恋 | 1:2 | 全北现代 | 韩职 / K_LEAGUE_1 | EXCEL-74548 | [第206805行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206805) |
| 2024-12-01 | 首尔衣恋 | 1:2 | 全北现代 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679982 | [第206898行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:206898) |

<a id="t013"></a>
### T013

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2024-12-08 | 全北现代 | 2:1 | 首尔衣恋 | 韩职 / K_LEAGUE_1 | EXCEL-74676 | [第207270行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:207270) |
| 2024-12-08 | 全北现代 | 2:1 | 首尔衣恋 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-4679983 | [第207353行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:207353) |

<a id="t014"></a>
### T014

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-12-03 | 水原三星 | 0:1 | 济州SK | 韩职 / K_LEAGUE_1 | EXCEL-79456 | [第226523行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226523) |
| 2025-12-03 | 水原三星 | 0:1 | 济州SK | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-5078164 | [第226550行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226550) |

<a id="t015"></a>
### T015

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-12-05 | 富川FC | 1:0 | 水原FC | 韩职 / K_LEAGUE_1 | EXCEL-79473 | [第226634行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226634) |
| 2025-12-05 | 富川FC | 1:0 | 水原FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-5078165 | [第226659行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226659) |

<a id="t016"></a>
### T016

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-12-07 | 济州SK | 2:0 | 水原三星 | 韩职 / K_LEAGUE_1 | EXCEL-79539 | [第226798行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226798) |
| 2025-12-07 | 济州SK | 2:0 | 水原三星 | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-5078166 | [第226877行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226877) |

<a id="t017"></a>
### T017

双方队名已一致，无需为此新增队名映射。

日期差0天；不同来源；映射赛事分类不同；原始赛事名不同

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2025-12-08 | 水原FC | 2:3 | 富川FC | 韩职 / K_LEAGUE_1 | EXCEL-79570 | [第226911行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226911) |
| 2025-12-08 | 水原FC | 2:3 | 富川FC | Play-offs 1/2 / CLUB_OFFICIAL_OTHER | FOTMOB-5078167 | [第226946行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:226946) |

## W 双方名称弱关联：不作为新增映射依据

<a id="w001"></a>
### W001

名称关系：FC Lviv / Qyzylqum；Dinamo Batumi / Valmiera FC。

日期差1天；两端身份缺少可靠共同队伍锚点，未纳入核心候选

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-02-05 | Qyzylqum | 1:1 | Valmiera FC | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-40D6CB9F94176792 | [第131262行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:131262) |
| 2021-02-06 | FC Lviv | 1:1 | Dinamo Batumi | 俱乐部友谊赛 / CLUB_FRIENDLY | FUTBOL24-CF7D5B358DA63D93 | [第131415行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:131415) |

<a id="w002"></a>
### W002

名称关系：沙勒罗瓦 / 瓦尔韦克；威廉二世 / 欧本。

日期差1天；原始赛事名不同；两端身份缺少可靠共同队伍锚点，未纳入核心候选

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2021-10-29 | 瓦尔韦克 | 3:0 | 威廉二世 | 荷兰杯 / CLUB_OFFICIAL_OTHER | FOTMOB-3734065 | [第146361行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:146361) |
| 2021-10-30 | 沙勒罗瓦 | 3:0 | 欧本 | 比甲 / CLUB_OFFICIAL_OTHER | FOTMOB-3604251 | [第146396行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:146396) |

<a id="w003"></a>
### W003

名称关系：伊拉克U23 / 伊朗U23；中国U23 / 韩国U23。

日期差1天；主客顺序相反；两端身份缺少可靠共同队伍锚点，未纳入核心候选

| 日期 | 原始主队 | 比分 | 原始客队 | 原始赛事 / 有效分类 | match_id | 原表位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-01-07 | 韩国U23 | 0:0 | 伊朗U23 | 亚洲杯23 / INTERNATIONAL_OFFICIAL | EXCEL-80097 | [第228074行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228074) |
| 2026-01-08 | 伊拉克U23 | 0:0 | 中国U23 | 亚洲杯23 / INTERNATIONAL_OFFICIAL | EXCEL-80114 | [第228099行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228099) |

## 同日旁证索引

仅列出上述队名候选的同日同比分支持，供检查身份时交叉参考；不计入跨日260对。

| 候选编号 | 日期 | 记录A | 记录B | match_id A / B | 原表A / B |
| --- | --- | --- | --- | --- | --- |
| G001 | 2018-08-03 | 莱红牛 0:3 哈德斯菲尔德 | 莱红牛 0:3 Huddersfield | ESPN-523418 / FUTBOL24-220327399C246FE8 | [第83203行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83203) / [第83218行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83218) |
| G001 | 2026-07-21 | 谢菲联 3:1 哈德斯菲尔德 | 谢菲联 3:1 Huddersfield | FOTMOB-5956424 / FUTBOL24-652B0619AC73EA29 | [第238249行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238249) / [第238254行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238254) |
| G001 | 2026-07-25 | 米堡 1:3 哈德斯菲尔德 | 米堡 1:3 Huddersfield | FOTMOB-5963804 / FUTBOL24-2C81E41B9C87F9B1 | [第238532行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238532) / [第238544行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238544) |
| G002 | 2020-01-09 | 法兰克福 1:2 Hertha Berlin | 法兰克福 1:2 柏林赫塔 | ESPN-563214 / FUTBOL24-173CA8A16ECEAF78 | [第113445行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113445) / [第113461行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113461) |
| G002 | 2020-08-29 | Hertha Berlin 0:4 埃因霍温 | 柏林赫塔 0:4 埃因霍温 | ESPN-575940 / FUTBOL24-86DB92834BCD3946 | [第121388行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121388) / [第121495行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121495) |
| G003 | 2018-07-29 | 伯恩利 0:0 蒙彼利埃 | Montpellier HSC 0:0 伯恩利 | ESPN-523283 / FUTBOL24-D18089C48BE2111B | [第82947行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82947) / [第83041行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83041) |
| G003 | 2022-07-30 | 水晶宫 4:2 蒙彼利埃 | 水晶宫 4:2 Montpellier HSC | ESPN-650702 / FUTBOL24-903F2261473A25AB | [第160189行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160189) / [第160315行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:160315) |
| G004 | 2016-07-29 | Mainz 0:1 塞维利亚 | 美因茨 0:1 塞维利亚 | ESPN-461435 / EXCEL-18403 | [第38325行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38325) / [第38332行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38332) |
| G004 | 2016-08-07 | Mainz 4:0 利物浦 | 美因茨 4:0 利物浦 | ESPN-462145 / EXCEL-18801 | [第39029行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39029) / [第39087行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39087) |
| G004 | 2017-01-06 | Mainz 1:1 海牙 | 美因茨 1:1 海牙 | ESPN-490283 / EXCEL-24164 | [第48880行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48880) / [第48883行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48883) |
| G004 | 2017-01-07 | Mainz 0:2 费耶诺德 | 美因茨 0:2 费耶诺德 | ESPN-490281 / EXCEL-24180 | [第48892行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48892) / [第48903行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:48903) |
| G004 | 2022-07-18 | Mainz 1:0 纽卡斯尔 | 美因茨 1:0 纽卡斯尔 | ESPN-650008 / FUTBOL24-2B44A5B40C08034A | [第159444行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159444) / [第159475行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159475) |
| G004 | 2023-08-05 | Mainz 1:0 伯恩利 | 美因茨 1:0 伯恩利 | ESPN-685322 / FUTBOL24-1D898DC4B67FEA86 | [第180122行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180122) / [第180198行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:180198) |
| G004 | 2024-01-06 | 费耶诺德 1:2 Mainz | 费耶诺德 1:2 美因茨 | ESPN-694559 / FUTBOL24-0911347A9BE060CF | [第188691行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188691) / [第188736行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188736) |
| G005 | 2021-07-17 | FC Cologne 3:2 拜仁 | 科隆 3:2 拜仁 | ESPN-611766 / FUTBOL24-A6C4EA92BB89D197 | [第139811行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139811) / [第139947行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:139947) |
| G005 | 2026-01-05 | 卢加诺 1:2 FC Cologne | 科隆 2:1 卢加诺 | FOOTMERCATO-4091178744424312551 / FOTMOB-5115519 | [第228027行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228027) / [第228044行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228044) |
| G006 | 2018-07-29 | 利兹联 1:0 拉帕马斯 | 利兹联 1:0 UD Las Palmas | ESPN-523285 / FUTBOL24-6FDB6D2F642FC066 | [第82949行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82949) / [第83029行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:83029) |
| G008 | 2026-01-15 | CSKA 1948 1:2 LASK林茨 | CSKA 1948 Sofia 1:2 LASK林茨 | FOTMOB-5107386 / FUTBOL24-36AE73709C65C13A | [第228464行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228464) / [第228481行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228481) |
| G008 | 2026-01-19 | 比亚韦 1:1 CSKA 1948 | 比亚韦 1:1 CSKA 1948 Sofia | FOOTMERCATO-2142303485592475329 / FUTBOL24-FD967F9A515A99CF | [第228767行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228767) / [第228807行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228807) |
| G008 | 2026-02-01 | CSKA 1948 3:0 江原FC | CSKA 1948 Sofia 3:0 江原FC | FOOTMERCATO-7069173355828562243 / FUTBOL24-45ED339CA3CD4CE4 | [第229604行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229604) / [第229689行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:229689) |
| G009 | 2017-07-12 | 西布罗姆 1:2 斯拉维亚 | West Bromwich 1:2 斯拉维亚 | EXCEL-29605 / FUTBOL24-8C586425D8B0B0AF | [第59738行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59738) / [第59763行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:59763) |
| G009 | 2017-07-19 | 莱切斯特 1:1 西布罗姆 | 莱切斯特 1:1 West Bromwich | EXCEL-29737 / FUTBOL24-523CA38F22CE3663 | [第60202行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60202) / [第60251行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60251) |
| G009 | 2017-07-22 | 水晶宫 2:0 西布罗姆 | West Bromwich 0:2 水晶宫 | EXCEL-29813 / FUTBOL24-7467A4FDF969C7BB | [第60395行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60395) / [第60474行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60474) |
| G010 | 2019-07-27 | Rotherham United 2:2 莱切斯特 | Rotherham 2:2 莱切斯特 | ESPN-540314 / FUTBOL24-7C821AC61FA89C30 | [第104131行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104131) / [第104242行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:104242) |
| G010 | 2026-07-25 | Rotherham United 0:2 谢菲联 | Rotherham 0:2 谢菲联 | FOTMOB-5960994 / FUTBOL24-100AE378F11693BE | [第238528行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238528) / [第238539行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238539) |
| G011 | 2020-08-16 | 多特蒙德 11:2 Austria Vienna | 多特蒙德 11:2 维也纳 | ESPN-577325 / FUTBOL24-F5375C0BC3D76B8B | [第120738行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120738) / [第120800行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120800) |
| G012 | 2016-07-09 | Cork 0:3 富勒姆 | 科克城 0:3 富勒姆 | ESPN-457314 / FUTBOL24-A6705C644DB6D8BA | [第37021行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37021) / [第37104行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37104) |
| G013 | 2026-07-25 | Forest Green Rovers 1:5 加的夫城 | Forest Green 1:5 加的夫城 | FOTMOB-5967544 / FUTBOL24-C26C4C6232EC0848 | [第238535行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238535) / [第238578行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238578) |
| G014 | 2019-07-13 | Lausanne Sports 2:1 摩纳哥 | Lausanne-Sport 2:1 摩纳哥 | ESPN-548866 / FUTBOL24-909D11BDC28F6BE6 | [第103169行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103169) / [第103264行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103264) |
| G020 | 2014-10-25 | 斯文登 2:2 科切斯特 | 斯文登 2:2 Colchester United | EXCEL-160 / FOTMOB-1725094 | [第149行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:149) / [第225行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:225) |
| G020 | 2014-11-22 | 科切斯特 0:1 考文垂 | Colchester United 0:1 考文垂 | EXCEL-999 / FOTMOB-1725120 | [第1847行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:1847) / [第1885行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:1885) |
| G020 | 2014-11-29 | 米尔顿 6:0 科切斯特 | 米尔顿 6:0 Colchester United | EXCEL-1241 / FOTMOB-1725137 | [第2244行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:2244) / [第2301行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:2301) |
| G020 | 2015-01-03 | 加的夫城 3:1 科切斯特 | 加的夫城 3:1 Colchester United | EXCEL-2038 / FOTMOB-1873311 | [第3859行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:3859) / [第3903行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:3903) |
| G020 | 2015-02-11 | 谢菲联 4:1 科切斯特 | 谢菲联 4:1 Colchester United | EXCEL-2981 / FOTMOB-1725271 | [第5887行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5887) / [第5911行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:5911) |
| G020 | 2015-02-18 | 科切斯特 0:1 米尔顿 | Colchester United 0:1 米尔顿 | EXCEL-3183 / FOTMOB-1725200 | [第6301行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:6301) / [第6317行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:6317) |
| G020 | 2015-03-04 | 科切斯特 0:1 诺茨郡 | Colchester United 0:1 诺茨郡 | EXCEL-3434 / FOTMOB-1725320 | [第7177行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:7177) / [第7215行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:7215) |
| G020 | 2015-04-11 | 考文垂 1:0 科切斯特 | 考文垂 1:0 Colchester United | EXCEL-4750 / FOTMOB-1725418 | [第9713行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:9713) / [第9771行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:9771) |
| G020 | 2015-04-29 | 科切斯特 1:1 斯文登 | Colchester United 1:1 斯文登 | EXCEL-5429 / FOTMOB-1725378 | [第11155行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:11155) / [第11189行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:11189) |
| G020 | 2015-05-03 | 科切斯特 1:0 普雷斯顿 | Colchester United 1:0 普雷斯顿 | EXCEL-5618 / FOTMOB-1725464 | [第11507行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:11507) / [第11545行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:11545) |
| G020 | 2015-08-12 | 科切斯特 0:0 雷丁 | Colchester United 0:0 雷丁 | EXCEL-7841 / FOTMOB-1986351 | [第17108行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:17108) / [第17195行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:17195) |
| G020 | 2015-09-16 | 谢菲联 2:3 科切斯特 | 谢菲联 2:3 Colchester United | EXCEL-9169 / FOTMOB-1987157 | [第19715行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:19715) / [第19728行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:19728) |
| G020 | 2015-09-26 | 斯文登 1:2 科切斯特 | 斯文登 1:2 Colchester United | EXCEL-9665 / FOTMOB-1987194 | [第20627行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:20627) / [第20665行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:20665) |
| G020 | 2015-11-14 | 科切斯特 1:3 考文垂 | Colchester United 1:3 考文垂 | EXCEL-11463 / FOTMOB-1987285 | [第24030行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:24030) / [第24042行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:24042) |
| G020 | 2015-11-21 | 米尔沃尔 4:1 科切斯特 | 米尔沃尔 4:1 Colchester United | EXCEL-11612 / FOTMOB-1987297 | [第24224行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:24224) / [第24257行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:24257) |
| G020 | 2016-01-09 | 科切斯特 2:1 查尔顿 | Colchester United 2:1 查尔顿 | EXCEL-12805 / FOTMOB-2146192 | [第26575行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26575) / [第26629行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26629) |
| G020 | 2016-01-16 | 科切斯特 1:2 谢菲联 | Colchester United 1:2 谢菲联 | EXCEL-13008 / FOTMOB-1987403 | [第26922行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26922) / [第26946行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26946) |
| G020 | 2016-01-30 | 科切斯特 1:4 热刺 | Colchester United 1:4 热刺 | EXCEL-13375 / FOTMOB-2168550 | [第27738行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:27738) / [第27810行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:27810) |
| G020 | 2016-03-30 | 考文垂 0:1 科切斯特 | 考文垂 0:1 Colchester United | EXCEL-15349 / FOTMOB-1987545 | [第31680行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:31680) / [第31695行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:31695) |
| G020 | 2016-04-02 | 科切斯特 0:0 米尔沃尔 | Colchester United 0:0 米尔沃尔 | EXCEL-15459 / FOTMOB-1987561 | [第31753行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:31753) / [第31785行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:31785) |
| G020 | 2016-08-10 | 布赖顿 4:0 科切斯特 | 布赖顿 4:0 Colchester United | EXCEL-18854 / FOTMOB-2260994 | [第39278行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39278) / [第39333行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:39333) |
| G020 | 2017-08-10 | 科切斯特 1:2 维拉 | Colchester United 1:2 维拉 | EXCEL-30357 / FOTMOB-2526575 | [第61884行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61884) / [第61906行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:61906) |
| G020 | 2019-11-09 | 科切斯特 0:2 考文垂 | Colchester United 0:2 考文垂 | EXCEL-52228 / FOTMOB-3206721 | [第110755行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:110755) / [第110850行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:110850) |
| G021 | 2018-07-28 | Hamburg SV 3:1 摩纳哥 | 汉堡 3:1 摩纳哥 | ESPN-523269 / FUTBOL24-4B984EB97A868861 | [第82800行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82800) / [第82889行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82889) |
| G021 | 2020-08-28 | Hamburg SV 1:0 费耶诺德 | 费耶诺德 0:1 汉堡 | ESPN-578011 / FUTBOL24-560F2629B3CC6C96 | [第121328行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121328) / [第121353行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121353) |
| G021 | 2023-07-15 | 萨尔茨堡 4:1 Hamburg SV | 萨尔茨堡 4:1 汉堡 | ESPN-680954 / FUTBOL24-4DCD44E60EDB2798 | [第178653行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178653) / [第178732行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:178732) |
| G021 | 2024-01-07 | 埃因霍温 2:2 Hamburg SV | 汉堡 2:2 埃因霍温 | ESPN-693920 / FUTBOL24-9F03F9287ADE2183 | [第188763行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188763) / [第188816行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:188816) |
| G022 | 2019-02-01 | New England Revolution 2:2 比尔森 | 比尔森 2:2 New England Revs | ESPN-536162 / FUTBOL24-E687162324C52A57 | [第94512行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94512) / [第94545行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94545) |
| G022 | 2019-02-03 | New England Revolution 0:2 克拉斯诺 | 克拉斯诺 2:0 New England Revs | ESPN-536161 / FUTBOL24-CACC0A2C2BC01169 | [第94701行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94701) / [第94790行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94790) |
| G022 | 2019-02-06 | New England Revolution 3:1 基迪纳摩 | 基迪纳摩 1:3 New England Revs | ESPN-534508 / FUTBOL24-DFB72C7EC2112F8E | [第94862行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94862) / [第94888行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:94888) |
| G022 | 2019-05-16 | New England Revolution 0:3 切尔西 | New England Revs 0:3 切尔西 | ESPN-538548 / FUTBOL24-D05A80C30BBDF2FB | [第100932行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100932) / [第100976行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:100976) |
| G023 | 2016-01-14 | 沙尔克04 0:3 米内罗竞技 | 沙尔克04 0:3 Atlético Mineiro/MG | EXCEL-12937 / FUTBOL24-93A2BF34DD1BCF8D | [第26832行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26832) / [第26864行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:26864) |
| G023 | 2017-01-12 | 勒沃库森 1:0 米内罗竞技 | 勒沃库森 1:0 Atlético Mineiro/MG | EXCEL-24292 / FUTBOL24-EBBED038311168F8 | [第49114行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49114) / [第49133行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:49133) |
| G023 | 2018-01-12 | 米内罗竞技 0:1 流浪者 | Atlético Mineiro/MG 0:1 流浪者 | ESPN-501639 / FUTBOL24-8528C42D7A8111C7 | [第71573行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71573) / [第71592行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:71592) |
| G024 | 2016-08-01 | 阿森纳 3:1 Guadalajara | Guadalajara Chivas 1:3 阿森纳 | ESPN-460617 / FUTBOL24-A26B1E7821F493E6 | [第38628行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38628) / [第38685行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38685) |
| G024 | 2017-07-20 | Guadalajara 2:2 波尔图 | Guadalajara Chivas 2:2 波尔图 | ESPN-490118 / FUTBOL24-E0E5D1DB23C784BC | [第60275行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60275) / [第60314行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:60314) |
| G024 | 2022-07-23 | 尤文图斯 2:0 Guadalajara | 尤文图斯 2:0 Guadalajara Chivas | ESPN-637069 / FUTBOL24-957EF0242DA007AE | [第159706行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159706) / [第159832行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:159832) |
| G025 | 2015-07-19 | SC Paderborn 07 0:2 沃特福德 | 帕德博恩 0:2 沃特福德 | ESPN-430351 / FUTBOL24-CA559C1300CF2CFD | [第15412行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15412) / [第15502行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15502) |
| G025 | 2020-08-28 | 多特蒙德 1:1 SC Paderborn 07 | 多特蒙德 1:1 帕德博恩 | ESPN-575931 / FUTBOL24-ECFFF8DF0319BABC | [第121325行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121325) / [第121383行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121383) |
| G025 | 2023-07-21 | SC Paderborn 07 2:1 勒沃库森 | 勒沃库森 1:2 帕德博恩 | ESPN-680377 / FUTBOL24-7D48178A839EDECB | [第179049行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179049) / [第179095行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179095) |
| G026 | 2026-01-19 | Debrecen 3:2 马里博尔 | 马里博尔 2:3 德布勒森 | FOOTMERCATO-7299978534705558350 / FUTBOL24-53B33C289019BC21 | [第228770行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228770) / [第228799行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228799) |
| G026 | 2026-07-04 | 维也纳 1:1 Debrecen | 维也纳 1:1 德布勒森 | FOTMOB-5766933 / FUTBOL24-873299DAE412C969 | [第237103行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237103) / [第237167行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237167) |
| G027 | 2016-07-09 | Glentoran 1:2 圣约翰 | 格伦托兰 1:2 圣约翰 | ESPN-457316 / FUTBOL24-0AE679575A7596AF | [第37023行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37023) / [第37057行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37057) |
| G027 | 2026-07-01 | Glentoran 0:4 基马诺克 | 格伦托兰 0:4 基马诺克 | FOTMOB-5869710 / FUTBOL24-570C6E34320B5CB3 | [第236938行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236938) / [第236946行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236946) |
| G028 | 2019-07-11 | 西汉姆联 3:2 SC Rheindorf Altach | 阿尔塔奇 2:3 西汉姆联 | ESPN-547162 / FUTBOL24-AD37EEB80F0818F4 | [第103066行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103066) / [第103090行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103090) |
| G028 | 2020-08-12 | SC Rheindorf Altach 0:6 多特蒙德 | 阿尔塔奇 0:6 多特蒙德 | ESPN-575930 / FUTBOL24-7BF668AD1B4D4F10 | [第120536行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120536) / [第120579行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120579) |
| G029 | 2018-07-28 | 谢周三 1:3 比利亚雷 | Sheffield Wed 1:3 比利亚雷 | ESPN-523270 / FUTBOL24-BDD6FEEB17993CC0 | [第82801行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82801) / [第82920行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:82920) |
| G029 | 2024-07-20 | 萨尔茨堡 4:0 谢周三 | 萨尔茨堡 4:0 Sheffield Wed | ESPN-712950 / FUTBOL24-42B1DFF0D8D79D27 | [第198668行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198668) / [第198751行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:198751) |
| G030 | 2015-07-18 | Shrewsbury Town 2:2 加的夫城 | Shrewsbury 2:2 加的夫城 | ESPN-430336 / FUTBOL24-B24C0ABC428B97CC | [第15264行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15264) / [第15384行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15384) |
| G030 | 2016-07-16 | Shrewsbury Town 0:4 加的夫城 | Shrewsbury 0:4 加的夫城 | ESPN-457553 / FUTBOL24-A8202FE41071BB49 | [第37394行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37394) / [第37516行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37516) |
| G031 | 2016-07-16 | 慕1860 1:0 多特蒙德 | TSV 1860 1:0 多特蒙德 | EXCEL-18133 / FUTBOL24-AEC540D140762ECE | [第37420行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37420) / [第37520行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:37520) |
| G031 | 2016-07-30 | 弗赖堡 0:0 慕1860 | 弗赖堡 0:0 TSV 1860 | EXCEL-18467 / FUTBOL24-284097FEC43912C6 | [第38419行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38419) / [第38484行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38484) |
| G032 | 2026-02-14 | 米亚尔比 3:2 Öster | 米亚尔比 3:2 厄斯特什 | FOOTMERCATO-1826976075174945187 / FOTMOB-5182345 | [第230442行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230442) / [第230544行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230544) |
| G032 | 2026-03-26 | 埃夫斯堡 3:1 Öster | 埃夫斯堡 3:1 厄斯特什 | FOOTMERCATO-6459748358589251901 / FUTBOL24-7F60680D74084BC4 | [第233019行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:233019) / [第233028行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:233028) |
| G033 | 2020-09-04 | Caykur Rizespor 3:4 特拉布宗 | 里泽 3:4 特拉布宗 | ESPN-581768 / FUTBOL24-5EC4C2D290B142F8 | [第121799行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121799) / [第121817行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121817) |
| G034 | 2026-01-19 | 博卡 2:1 Club Olimpia | 博卡 2:1 Olimpia Asunción | ESPN-401850358 / FUTBOL24-73D182F45EE294E1 | [第228761行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228761) / [第228800行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228800) |
| G035 | 2026-01-15 | 圣洛伦索 0:1 Cúcuta | 圣洛伦索 0:1 Cúcuta Deportivo | FOOTMERCATO-5582214894575324327 / FUTBOL24-03014EBB71232E40 | [第228446行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228446) / [第228476行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228476) |
| G036 | 2016-07-28 | 国际米兰 1:1 拉普大学 | 国际米兰 1:1 Estudiantes La Plata | EXCEL-18390 / FUTBOL24-8611393D9A2394DD | [第38304行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38304) / [第38319行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:38319) |
| G037 | 2026-07-04 | FC Halifax Town 1:3 谢菲联 | Halifax 1:3 谢菲联 | FOTMOB-5880240 / FUTBOL24-D180FFEBD984A58D | [第237133行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237133) / [第237188行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237188) |
| G038 | 2026-01-10 | Győr 2:2 贝游击 | 杰尔 2:2 贝游击 | FOOTMERCATO-2641264252777556065 / FUTBOL24-0DBD2F114DFF29A0 | [第228175行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228175) / [第228238行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228238) |
| G039 | 2026-01-19 | 奥胡斯 4:2 Hirnyk | 奥胡斯 4:2 Kryvbas | FOOTMERCATO-2071193140237616594 / FOTMOB-5119551 | [第228766行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228766) / [第228791行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228791) |
| G040 | 2026-03-23 | 哥德堡 2:1 Oddevold | 哥德堡 2:1 IK Oddevold | FOOTMERCATO-7565152443817452523 / FOTMOB-5266717 | [第232955行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232955) / [第232991行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:232991) |
| G041 | 2026-07-09 | 弗拉门戈 2:0 洛桑 | 弗拉门戈 2:0 Lausanne-Sport | FOTMOB-5901301 / FUTBOL24-BD921AC10DE8B699 | [第237355行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237355) / [第237370行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237370) |
| G042 | 2015-07-25 | Mansfield Town 1:1 莱切斯特 | Mansfield 1:1 莱切斯特 | ESPN-430400 / FUTBOL24-082F1878D9C35935 | [第15770行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15770) / [第15831行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:15831) |
| G043 | 2020-01-08 | 贝西克塔斯 2:0 Mezokövesdi SE | 贝西克塔斯 2:0 梅索科菲德 | ESPN-587396 / FUTBOL24-8FE2CE42BB7CA9E0 | [第113427行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113427) / [第113441行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:113441) |
| G044 | 2026-01-15 | 萨尔茨堡 2:0 Red Star Belgrade | 萨尔茨堡 2:0 贝红星 | ESPN-401845336 / FOTMOB-5115621 | [第228438行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228438) / [第228465行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228465) |
| G045 | 2020-08-08 | SC Verl 1924 0:3 埃因霍温 | Verl 0:3 埃因霍温 | ESPN-575020 / FUTBOL24-5BB3712BE94EAEE6 | [第120345行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120345) / [第120400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:120400) |
| G046 | 2019-07-10 | 费耶诺德 2:1 SV Darmstadt 98 | 费耶诺德 2:1 达姆施塔特 | ESPN-549120 / FUTBOL24-D1A26142E123CB44 | [第103021行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103021) / [第103058行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103058) |
| G047 | 2026-07-16 | Salt Lake 4:1 伯恩利 | 皇家盐湖城 4:1 伯恩利 | FOOTMERCATO-7077312114259676966 / FOTMOB-5915948 | [第237821行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237821) / [第237833行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237833) |
| G048 | 2023-07-24 | SpVgg Greuther Fürth 4:4 利物浦 | 菲尔特 4:4 利物浦 | ESPN-670149 / FUTBOL24-2580A479A95F4C60 | [第179400行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179400) / [第179423行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:179423) |
| G049 | 2019-07-13 | St. Patrick's Athletic 0:4 切尔西 | 圣帕特里 0:4 切尔西 | ESPN-548056 / FUTBOL24-372A659AECBAED0C | [第103168行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103168) / [第103240行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:103240) |
| G050 | 2026-07-02 | 保克什 1:2 邓迪FC | 邓迪FC 2:1 Paksi SE | FOOTMERCATO-121361534197843609 / FOTMOB-5872437 | [第236964行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236964) / [第236974行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:236974) |
| G050 | 2026-07-18 | 维也纳 4:1 Paksi SE | 维也纳 4:1 保克什 | FOTMOB-5943036 / FUTBOL24-38E9C32EC2F421A4 | [第238088行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238088) / [第238112行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:238112) |
| G051 | 2026-01-19 | 法伦斯 0:5 Radomiak | 法伦斯 0:5 拉多米亚克 | FOOTMERCATO-7909353830382216492 / FUTBOL24-CBDD03948FD43A22 | [第228771行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228771) / [第228803行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:228803) |
| G184 | 2020-08-28 | 沙尔克04 1:0 Aris | 沙尔克04 1:0 阿里斯 | ESPN-581144 / FUTBOL24-D6F4676002F6CDEB | [第121330行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121330) / [第121377行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:121377) |
| G185 | 2026-02-18 | CSKA 3:1 罗斯托夫 | 莫陆军 3:1 罗斯托夫 | FOOTMERCATO-7351191070765803299 / FOTMOB-5143678 | [第230794行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230794) / [第230817行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:230817) |
| G186 | 2026-07-17 | 北西兰 5:1 鹿斯巴达 | 北西兰 5:1 布斯巴达 | FOOTMERCATO-1327571690804637982 / FOTMOB-5904510 | [第237858行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237858) / [第237899行](D:/WorkJava/lottery-football/src/main/resources/data/historical_matches.csv:237899) |
