# lottery-football

竞彩足球概率预测与推荐回测程序，基于 Spring Boot 和 Vue 2，支持查询赛程、赛果、体彩赔率及模型预测结果。

## 主要功能

- 按赛事和日期查询赛程、比分、比赛状态及体彩赔率，支持多选赛事
- 使用泊松分布和蒙特卡洛模拟计算胜平负、让球胜平负、总进球数及比分概率
- 点击球队名称查看主客队近况和历史交锋，每栏最多 10 场，球队名统一采用体彩标准名
- 每类赛事独立保存“本届/含上届 × 稳健/激进”参数档案，修改参数后自动重算
- 支持推荐回测及进度展示，统计场均投注、场均返奖、采样率、命中率、ROI 和收益波动率

模型统一使用 90 分钟加伤停补时的比分，不计入加时赛和点球大战。

## 支持的赛事

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
| 德甲 | `BUNDESLIGA` |
| 意甲 | `SERIE_A` |
| 法甲 | `LIGUE_1` |
| 葡超 | `PRIMEIRA_LIGA` |
| 荷甲 | `EREDIVISIE` |
| 阿甲 | `ARGENTINE_PRIMERA_DIVISION` |
| 瑞超 | `SWEDISH_ALLSVENSKAN` |
| 芬超 | `FINNISH_VEIKKAUSLIIGA` |
| 韩职 | `K_LEAGUE_1` |

多选赛事时，参数区显示首个赛事的方案，具体数值不可编辑，但可以统一切换稳健/激进方案。预测与回测按各场比赛所属赛事使用对应参数。

## 技术栈

- Java 17、Spring Boot 3.3.5、Maven
- Vue 2.7.16、Vue CLI 5
- Maven 自动安装 Node.js 20.17.0 和 npm 10.8.2，并将前端产物打入 Spring Boot jar

## 快速启动

### 环境与构建

需要 JDK 17 或更高版本、Maven 3.8 或更高版本，无需手动安装 Node.js。

```powershell
mvn clean package
```

构建产物为 `target/lottery-football-1.0.0.jar`。也可双击 `build.cmd` 构建并生成分发目录；该脚本默认跳过测试：

```text
target/dist/lottery-football-1.0.0.jar
target/dist/run.cmd
```

### 启动与测试

开发目录双击 `启动程序.cmd` 或 `run.cmd`；分发目录运行 `target/dist/run.cmd`。启动后访问 <http://127.0.0.1:8080>。

```powershell
# 健康检查
Invoke-RestMethod "http://127.0.0.1:8080/api/football/health"

# 后端测试
mvn test

# 前端与回测逻辑测试，需先完成 Maven 构建以安装 Node.js
.\target\node\npm.cmd --prefix frontend test
```

## 目录结构

```text
lottery-football
├─ frontend/                  Vue 页面与前端测试
├─ scripts/                   辅助脚本与参数优化工具
├─ config/                    运行时缓存与用户参数
├─ reports/                   回测与参数优化报告
├─ src/main/java/             Spring Boot 后端
├─ src/main/resources/data/   历史比赛、赔率和球队名映射
├─ pom.xml
├─ build.cmd
├─ run.cmd
└─ 启动程序.cmd
```

## 主要接口

接口前缀为 `/api/football`，兼容旧版 `/api/worldcup`。

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/health` | 健康检查 |
| GET | `/overview` | 赛事概览和可查询日期 |
| GET | `/predictions` | 指定日期的概率预测 |
| GET | `/head-to-head` | 双方历史交锋 |
| GET | `/head-to-head/overview` | 主客队近况及历史交锋 |
| GET | `/recommendation-backtest` | 同步推荐回测 |
| POST | `/recommendation-backtest/jobs` | 创建异步回测任务 |
| GET | `/recommendation-backtest/jobs/{jobId}` | 查询回测进度 |
| GET、PUT | `/user-config` | 读取或保存用户配置 |

查询示例：

```http
GET /api/football/predictions?competition=CHAMPIONS_LEAGUE&date=2026-07-14&simulations=50000
```

| 参数 | 说明 |
|---|---|
| `competition` | 赛事代码，普通查询默认 `WORLD_CUP`；推荐回测支持 `ALL` 或逗号分隔的多个代码 |
| `date` | 比赛日期，格式 `yyyy-MM-dd`，时区为 `Asia/Shanghai` |
| `simulations` | 模拟次数，范围 1,000 至 500,000，默认 50,000 |
| `includePreviousEdition` | 是否把上届赛事纳入回测范围 |

只有球队、日期和主客方向可靠匹配时，系统才会关联体彩比赛 ID 和赔率。

## 参数与回测

### 参数档案

参数保存在 `config/user-config.json`，键格式为 `{competition}:{range}:{preset}`：

| 后缀 | 范围 | 方案 |
|---|---|---|
| `CURRENT:STABLE` | 仅本届 | 稳健 |
| `CURRENT:AGGRESSIVE` | 仅本届 | 激进 |
| `PREVIOUS:STABLE` | 含上届 | 稳健 |
| `PREVIOUS:AGGRESSIVE` | 含上届 | 激进 |

尚未开赛的赛事自动使用 `PREVIOUS` 档案。页面支持配置进球系数、让球与推荐阈值、赔率阈值和比赛类型权重，信息图标提供口径说明。比赛类型权重范围为 0 至 1，`0` 表示不计入该类样本。

每个赛事、每个时间范围只保存一套进球数策略，统一使用对应的 `STABLE` 模型参数。切换稳健/激进方案只影响胜平负和让球等推荐。

### 回测口径

- “仅本届”从本届开始日计算，“含上届”从上届开始日计算，均截至本届结束日或北京时间当天中的较早日期
- 每场回测只读取比赛日前的历史数据，避免未来数据泄漏
- 仅已完赛且具备完整比分、体彩比赛 ID 和至少一类完整赔率的比赛进入推荐计算
- 每个推荐项投入 1 单位，中奖项按对应赔率计算返奖

| 指标 | 计算方式 |
|---|---|
| ROI | `(总返奖 / 总投注 - 1) × 100%` |
| 场均投注 | `总投注 / 推荐比赛数` |
| 场均返奖 | `总返奖 / 推荐比赛数` |
| 命中率 | `命中比赛数 / 推荐比赛数 × 100%` |
| 采样率 | `推荐比赛数 / 有赔率的已完赛比赛数 × 100%` |
| 波动率 | 推荐比赛逐场收益率的样本标准差 × 100% |

没有推荐项时 ROI 为空，全部未命中时为 `-100%`；少于 2 场推荐比赛时波动率为空。已完赛比赛总数仅用于展示数据覆盖情况，不作为采样率分母。

### 参数优化

- 胜平负优化入口：`scripts/reoptimize-shared-backtest-profiles.mjs`
- 进球数优化入口：`scripts/optimize-total-goals-strategies.mjs`，使用 `--robust-validation` 开启时间留出验证
- 时间留出验证按完整比赛日划分前约 70% 训练集和后约 30% 验证集，训练集用于搜索与稳定性排名，验证集只用于通过或拒绝候选
- 候选按实际保存的完整模型因子重新回测，统一核验训练、验证和全量指标；无法满足门槛时关闭对应推荐，样本不足时复用含上届方案并执行额外验证

胜平负搜索预计算比赛快照的选盘与结算信息；独立复验可设置 `USE_PREPARED_EVALUATOR=false` 使用页面回测算法。原方案通过全部门槛时保留为基准，仅在新候选满足采样率窗口且训练稳定性更高时替换，不按验证集 ROI 排名。后端缓存球队名归一化结果，并在同赛事、同截止日期的模型间共用只读历史列表以减少重复计算；模型重建时清除历史列表缓存。

隔离优化可通过胜平负优化器的 `OPTIMIZER_CONFIG_PATH` 或进球数优化器的 `--config-path` 指定配置副本。并行运行时使用独立检查点和报告路径，进球数搜索添加 `--dry-run`，复验通过后再合并正式配置。

## 模型说明

系统根据比赛日前的正式比赛和降权友谊赛计算球队攻防强度，并向 1.0 收缩以降低小样本波动。历史样本权重由 Dixon-Coles 时间衰减权重与比赛类型权重相乘得到。

```text
λ_home = baselineGoals × homeAttack × awayDefenseWeakness × homeAdvantage × h2hFactor
λ_away = baselineGoals × awayAttack × homeDefenseWeakness ÷ h2hFactor
P(X = k) = e^-λ × λ^k / k!
```

模型通过蒙特卡洛采样统计各类概率。让球按 `homeGoals + handicap` 计算，`-1` 表示主队让 1 球，`+1` 表示主队受让 1 球。

## 配置与数据

| 文件 | 用途 |
|---|---|
| `src/main/resources/application.yml` | 应用、数据源、时区和缓存配置 |
| `config/user-config.json` | 赛事参数档案和页面配置 |
| `src/main/resources/data/historical_matches.csv` | 历史比赛与常规时间比分 |
| `src/main/resources/data/historical_odds_data.csv` | 历史体彩赔率 |
| `src/main/resources/data/team_name_mappings.csv` | 体彩标准球队名与数据源别名 |

外部接口不可用时，服务使用内置数据和本地缓存。数据来源见 [DATA_SOURCES.md](DATA_SOURCES.md)，优化与审计结果见 [reports](reports/)。

## 法律免责声明

本项目仅供足球数据分析、算法学习、技术研究与开发验证，不构成任何形式的投注建议、投资建议、盈利承诺或结果保证。足球比赛结果、赔率及模型预测均具有不确定性，项目作者及贡献者不对数据的准确性、完整性、及时性或适用性作任何明示或暗示的保证。

使用者应自行判断并承担使用本项目所产生的全部风险与责任，并遵守所在国家或地区适用的法律法规及第三方数据源的使用条款。严禁将本项目用于非法赌博、欺诈或其他违法活动；未成年人不得参与任何形式的彩票购买或博彩活动。因使用或无法使用本项目而产生的任何直接或间接损失，项目作者及贡献者在法律允许的范围内不承担责任。

本项目引用的赛事、赔率及其他第三方数据，其权利归相应权利人所有。如相关内容侵犯了您的合法权益，请联系项目维护者处理。对本项目的赞助完全出于自愿，仅用于支持项目开发与维护，不代表购买投注服务，也不构成任何收益或预测结果的承诺。

## 赞助支持

如果这个项目对你有帮助，欢迎赞助。

<table>
  <tr>
    <th>支付宝</th>
    <th>微信</th>
  </tr>
  <tr>
    <td><img src="docs/images/alipay-qr.png" alt="支付宝收款码" width="260"></td>
    <td><img src="docs/images/wechat-pay-qr.png" alt="微信收款码" width="260"></td>
  </tr>
</table>
