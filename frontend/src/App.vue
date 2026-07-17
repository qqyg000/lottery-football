<template>
  <div class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Monte Carlo + Poisson</p>
        <h1>彩票预测-竞彩足球</h1>
        <div ref="competitionSelect" class="competition-select" @keydown.esc.stop="closeCompetitionDropdown">
          <button
            type="button"
            class="competition-select-trigger"
            aria-haspopup="listbox"
            :aria-expanded="competitionDropdownOpen ? 'true' : 'false'"
            :disabled="loading || updatingData || backtesting"
            @click="toggleCompetitionDropdown"
          >
            <span>联赛</span>
            <strong>{{ activeCompetitionLabel }}</strong>
            <span class="competition-select-arrow" :class="{ 'is-open': competitionDropdownOpen }" aria-hidden="true"></span>
          </button>
          <div v-if="competitionDropdownOpen" class="competition-select-dropdown">
            <label class="competition-search">
              <span aria-hidden="true">⌕</span>
              <input
                ref="competitionSearchInput"
                v-model.trim="competitionSearch"
                type="search"
                placeholder="输入联赛名称"
                aria-label="模糊查询联赛"
              >
            </label>
            <div class="competition-option-list" role="listbox" aria-label="联赛多选" aria-multiselectable="true">
              <button
                v-for="competition in filteredCompetitionOptions"
                :key="competition.code"
                type="button"
                class="competition-option"
                role="option"
                :aria-selected="isDraftCompetitionSelected(competition.code) ? 'true' : 'false'"
                @click="toggleDraftCompetition(competition.code)"
              >
                <span class="competition-checkbox" :class="{ 'is-checked': isDraftCompetitionSelected(competition.code) }">
                  {{ isDraftCompetitionSelected(competition.code) ? '✓' : '' }}
                </span>
                <span>{{ competition.name }}</span>
              </button>
              <div v-if="filteredCompetitionOptions.length === 0" class="competition-option-empty">未找到匹配联赛</div>
            </div>
            <div class="competition-select-footer">
              <span>已选 {{ draftCompetitions.length }} 项</span>
              <button type="button" :disabled="draftCompetitions.length === 0" @click="applyCompetitionSelection">确定</button>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-card-group">
        <div class="hero-card">
          <div class="hero-summary-column">
          <div class="hero-number">{{ overview.historicalMatchCount || 0 }}</div>
          <div class="hero-label">历史战绩样本</div>
          <div class="hero-small">赛程：{{ overview.scheduleMatchCount || 0 }} 场 · 已完赛：{{ overview.completedMatchCount || 0 }} 场</div>
          <div class="model-toggle" aria-label="模型口径">
            <button type="button" :class="{ 'is-active': modelMode === 'before' }" @click="setModelMode('before')">开赛前</button>
            <button type="button" :class="{ 'is-active': modelMode === 'after' }" @click="setModelMode('after')">开赛后</button>
          </div>
          <div class="hero-actions">
            <button type="button" class="factor-recalculate factor-reset refresh-data-button" :disabled="loading || updatingData || backtesting" @click="refreshData">
              {{ updatingData ? '更新中' : '更新数据' }}
            </button>
          </div>
          </div>
          <div class="factor-controls" aria-label="模型参数">
            <div class="factor-control-column">
              <label
                class="factor-control"
                :class="{ 'is-competition-disabled': activeCompetition !== 'WORLD_CUP' }"
                :title="activeCompetition !== 'WORLD_CUP' ? '仅世界杯赛事可用' : ''"
              >
                <span>种子队进球系数</span>
                <input
                  type="number"
                  min="0.1"
                  max="3"
                  step="0.01"
                  v-model.number="modelFactors.seedTeamGoalFactor"
                  :disabled="loading || updatingData || backtesting || activeCompetition !== 'WORLD_CUP'"
                  @change="saveModelFactorInput('seedTeamGoalFactor')"
                  @keyup.enter="commitModelFactors"
                >
              </label>
              <label v-if="activeCompetition === 'WORLD_CUP'" class="factor-control">
                <span>东道主进球系数</span>
                <input
                  type="number"
                  min="0.1"
                  max="3"
                  step="0.01"
                  v-model.number="modelFactors.hostTeamGoalFactor"
                  :disabled="loading || updatingData || backtesting"
                  @change="saveModelFactorInput('hostTeamGoalFactor')"
                  @keyup.enter="commitModelFactors"
                >
              </label>
              <label v-else class="factor-control">
                <span>主场进球系数</span>
                <input
                  type="number"
                  min="0.1"
                  max="3"
                  step="0.01"
                  v-model.number="modelFactors.homeTeamGoalFactor"
                  :disabled="loading || updatingData || backtesting"
                  @change="saveModelFactorInput('homeTeamGoalFactor')"
                  @keyup.enter="commitModelFactors"
                >
              </label>
              <label class="factor-control">
                <span>让球平滑系数</span>
                <input
                  type="number"
                  min="0"
                  max="0.8"
                  step="0.001"
                  v-model.number="modelFactors.handicapSmoothingFactor"
                  :disabled="loading || updatingData || backtesting"
                  @change="saveModelFactorInput('handicapSmoothingFactor')"
                  @keyup.enter="commitModelFactors"
                >
              </label>
            </div>
            <div class="factor-control-column">
              <label class="factor-control percentage-factor-control">
                <span>让球推荐阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="handicapRecommendationThreshold"
                    :disabled="loading || updatingData || backtesting"
                    @change="saveRecommendationThresholdInputs"
                    @keyup.enter="saveRecommendationThresholdInputs"
                  >
                  <span class="percentage-suffix">%</span>
                </span>
              </label>
              <label class="factor-control percentage-factor-control">
                <span>让球反向阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="handicapReverseThreshold"
                    :disabled="loading || updatingData || backtesting"
                    @change="saveRecommendationThresholdInputs"
                    @keyup.enter="saveRecommendationThresholdInputs"
                  >
                  <span class="percentage-suffix">%</span>
                </span>
              </label>
              <label class="factor-control percentage-factor-control">
                <span>单项推荐阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="singleRecommendationThreshold"
                    :disabled="loading || updatingData || backtesting"
                    @change="saveRecommendationThresholdInputs"
                    @keyup.enter="saveRecommendationThresholdInputs"
                  >
                  <span class="percentage-suffix">%</span>
                </span>
              </label>
            </div>
            <div class="factor-actions">
              <button type="button" class="factor-recalculate" :disabled="loading || updatingData || backtesting" @click="toggleParameterPreset">
                {{ parameterPresetToggleText }}
              </button>
              <button type="button" class="factor-recalculate" :disabled="loading || updatingData || backtesting || !queryDate" @click="commitModelFactors">
                {{ loading ? '计算中' : '重新计算' }}
              </button>
            </div>
          </div>
        </div>
        <div class="global-parameter-card" aria-label="全局参数">
          <div class="backtest-result" :class="{ 'is-empty': !backtestActive }">
            <div class="backtest-average-grid">
              <div>
                <strong>{{ backtestAverageOddsText }}</strong>
                <span>场均赔率</span>
              </div>
              <div>
                <strong>{{ backtestAverageRecommendationText }}</strong>
                <span>场均推荐数</span>
              </div>
              <div>
                <strong>{{ backtestHitRateText }}</strong>
                <span>命中率</span>
              </div>
              <div title="ROI = [((命中率 × 含未中奖场次的场均赔率) ÷ 场均推荐数)² - 1] × 100%">
                <strong>{{ backtestRoiText }}</strong>
                <span>ROI</span>
              </div>
            </div>
          </div>
          <label class="factor-control global-parameter-control">
            <span>推荐赔率阈值</span>
            <input
              type="number"
              min="1"
              max="100"
              step="0.01"
              inputmode="decimal"
              v-model.number="recommendationOdds"
              :disabled="loading || updatingData || backtesting"
              @change="normalizeRecommendationOddsInput"
              @keyup.enter="runRecommendationOddsBacktest"
            >
          </label>
          <button
            type="button"
            class="factor-recalculate backtest-odds-button"
            :disabled="loading || updatingData || backtesting"
            @click="runRecommendationOddsBacktest"
          >
            {{ backtesting ? '回测中' : '开始回测' }}
          </button>
        </div>
      </div>
    </header>

    <section class="calendar-panel" aria-label="比赛日历">
      <div class="calendar-header">
        <button type="button" class="calendar-nav" aria-label="上一月" @click="shiftCalendarMonth(-1)">
          <span aria-hidden="true">&lt;</span>
        </button>
        <div class="calendar-title">{{ calendarTitle }}</div>
        <button type="button" class="calendar-nav" aria-label="下一月" @click="shiftCalendarMonth(1)">
          <span aria-hidden="true">&gt;</span>
        </button>
      </div>
      <div class="calendar-weekdays">
        <span v-for="weekday in weekdays" :key="weekday">{{ weekday }}</span>
      </div>
      <div class="calendar-grid">
        <button
          v-for="cell in calendarCells"
          :key="cell.key"
          type="button"
          class="calendar-day"
          :class="{ 'is-empty': cell.empty, 'is-selected': cell.date === queryDate, 'is-loading': cell.date === queryDate && loading, 'has-schedule': cell.hasSchedule }"
          :disabled="cell.empty || loading || updatingData || backtesting"
          @click="selectDate(cell.date)"
        >
          <span class="calendar-day-number">{{ cell.day }}</span>
        </button>
      </div>
    </section>

    <section v-if="errorMessage" class="error-box">{{ errorMessage }}</section>

    <section v-if="!loading && !backtesting && matches.length === 0" class="empty-box">
      {{ backtestActive ? '当前推荐赔率下没有中奖场次' : '当前日期暂无带赔率的赛程，请切换日期后查询' }}
    </section>

    <section class="match-list" :class="{ 'is-backtest': backtestActive }" :style="{ '--match-columns': matchColumns }">
      <article v-for="match in matches" :key="match.competition + '-' + match.matchId" class="match-card">
        <div class="match-head">
          <div class="match-info">
            <div class="match-time">{{ match.matchDate }} {{ match.kickoffTime }} · {{ match.groupName }}</div>
            <h2>
              <button
                type="button"
                class="match-title-button"
                :aria-label="'查看' + match.homeTeamCn + '与' + match.awayTeamCn + '的历史交战数据'"
                title="点击查看历史交战数据"
                @click="openHeadToHeadDialog(match)"
              >
                {{ match.homeTeamCn }} <span>vs</span> {{ match.awayTeamCn }}
              </button>
            </h2>
            <p class="match-score-line">
              <span class="match-score-text">
                比分：{{ match.scoreText }}
                <span
                  v-if="recommendationResult(match)"
                  class="result-badge"
                  :class="'is-' + recommendationResult(match)"
                >
                  {{ recommendationResultText(match) }}
                </span>
              </span>
            </p>
          </div>
          <div class="goal-box">
            <div>期望进球</div>
            <strong>{{ activeExpectedHomeGoals(match) }} : {{ activeExpectedAwayGoals(match) }}</strong>
          </div>
        </div>

        <div class="match-summary-row">
          <div class="summary-title-row">
            <div class="handicap-title">胜平负概率</div>
            <div class="recommend-total" :class="{ 'is-empty': recommendationTotalProbability(match) === null }">
              <strong>{{ recommendationTotalProbabilityText(match) }}</strong>
            </div>
          </div>
          <div class="prediction-prob-stack">
            <div v-if="activeScoreProbabilities(match).length" class="score-prob-row score-prob-summary" :title="scoreProbabilityTitle(match)">
              <span class="score-prob-label">比分</span>
              <span class="score-prob-items">
                <span
                  v-for="score in activeScoreProbabilities(match)"
                  :key="match.matchId + '-' + score.homeScore + '-' + score.awayScore"
                  class="score-prob-pill"
                  :class="{ 'is-winning-prediction': isWinningScorePrediction(match, score) }"
                >
                  {{ score.homeScore }}-{{ score.awayScore }}
                  <span>{{ formatProbability(score.probability) }}</span>
                </span>
              </span>
            </div>
            <div v-if="activeTotalGoalsProbabilities(match).length" class="score-prob-row score-prob-summary" :title="totalGoalsProbabilityTitle(match)">
              <span class="score-prob-label">进球数</span>
              <span class="score-prob-items">
                <span
                  v-for="item in activeTotalGoalsProbabilities(match)"
                  :key="match.matchId + '-total-goals-' + item.totalGoals"
                  class="score-prob-pill"
                  :class="{ 'is-winning-prediction': isWinningTotalGoalsPrediction(match, item) }"
                >
                  {{ item.totalGoals }}球
                  <span>{{ formatProbability(item.probability) }}</span>
                </span>
              </span>
            </div>
          </div>
        </div>
        <div class="handicap-table-wrap">
          <table class="handicap-table">
            <thead>
            <tr>
              <th class="select-col" aria-label="选择"></th>
              <th class="handicap-col">盘口</th>
              <th>胜</th>
              <th>平</th>
              <th>负</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in probabilityRows(match)" :key="match.matchId + '-' + item.key" :class="item.rowClass">
              <td class="select-cell">
                <input
                  type="checkbox"
                  :checked="isRowSelected(match, item)"
                  :title="selectionInputTitle(match, item)"
                  :aria-label="selectionInputTitle(match, item)"
                  @change="toggleRecommendationRow(match, item, $event)"
                >
              </td>
              <td class="handicap-cell" :title="item.label">
                {{ item.label }}
              </td>
              <td class="probability-cell">
                <span class="probability-cell-content">
                  <span class="probability-value">{{ formatProbability(item.probability.win) }}</span>
                  <span
                    v-if="sportteryOddsValue(match, item, 'win') !== null"
                    class="sporttery-odds"
                    :title="sportteryOddsTitle(match, item)"
                  >{{ formatSportteryOdds(sportteryOddsValue(match, item, 'win')) }}</span>
                  <span v-if="isRecommended(match, item, 'win')" class="recommend-badge">荐</span>
                  <span v-if="isWinningRecommendation(match, item, 'win')" class="hit-cell-badge">中</span>
                </span>
              </td>
              <td class="probability-cell">
                <span class="probability-cell-content">
                  <span class="probability-value">{{ formatProbability(item.probability.draw) }}</span>
                  <span
                    v-if="sportteryOddsValue(match, item, 'draw') !== null"
                    class="sporttery-odds"
                    :title="sportteryOddsTitle(match, item)"
                  >{{ formatSportteryOdds(sportteryOddsValue(match, item, 'draw')) }}</span>
                  <span v-if="isRecommended(match, item, 'draw')" class="recommend-badge">荐</span>
                  <span v-if="isWinningRecommendation(match, item, 'draw')" class="hit-cell-badge">中</span>
                </span>
              </td>
              <td class="probability-cell">
                <span class="probability-cell-content">
                  <span class="probability-value">{{ formatProbability(item.probability.lose) }}</span>
                  <span
                    v-if="sportteryOddsValue(match, item, 'lose') !== null"
                    class="sporttery-odds"
                    :title="sportteryOddsTitle(match, item)"
                  >{{ formatSportteryOdds(sportteryOddsValue(match, item, 'lose')) }}</span>
                  <span v-if="isRecommended(match, item, 'lose')" class="recommend-badge">荐</span>
                  <span v-if="isWinningRecommendation(match, item, 'lose')" class="hit-cell-badge">中</span>
                </span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>

    <button
      type="button"
      class="recommendation-fab"
      :disabled="loading || updatingData || backtesting"
      aria-label="查看选中日期的推荐比赛"
      title="查看选中日期的推荐比赛"
      @click="openRecommendationDialog"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M9 6h10M9 12h10M9 18h10M4.5 6h.01M4.5 12h.01M4.5 18h.01" />
      </svg>
      <span v-if="recommendationDialogMatchCount > 0" class="recommendation-fab-badge">
        {{ recommendationDialogMatchCount > 99 ? '99+' : recommendationDialogMatchCount }}
      </span>
    </button>

    <div
      v-if="recommendationDialogVisible"
      class="dialog-backdrop"
      @click.self="closeRecommendationDialog"
      @keydown.esc="closeRecommendationDialog"
    >
      <section
        ref="recommendationDialog"
        class="recommendation-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="recommendation-dialog-title"
        tabindex="-1"
      >
        <header class="head-to-head-dialog-header">
          <div>
            <h3 id="recommendation-dialog-title">{{ recommendationDialogTitle }}</h3>
          </div>
          <button type="button" class="dialog-close" aria-label="关闭推荐汇总弹窗" @click="closeRecommendationDialog">×</button>
        </header>
        <p class="head-to-head-description">
          推荐 {{ recommendationDialogMatchCount }} 场 · 共 {{ recommendationDialogItemCount }} 项
        </p>

        <div v-if="recommendationDialogRows.length === 0" class="dialog-state">
          当前日期暂无符合条件的推荐比赛
        </div>
        <div v-else class="recommendation-table-wrap">
          <table class="recommendation-table" aria-label="选中日期推荐比赛汇总">
            <thead>
            <tr>
              <th>时间</th>
              <th>赛事</th>
              <th>对阵</th>
              <th>推荐项</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="row in recommendationDialogRows" :key="row.key">
              <td class="recommendation-time-cell">
                <strong>{{ row.kickoffTime }}</strong>
                <span v-if="row.matchNumber">{{ row.matchNumber }}</span>
              </td>
              <td>{{ row.competitionName }}</td>
              <td class="recommendation-fixture-cell">
                <strong>{{ row.homeTeamCn }}</strong>
                <span>vs</span>
                <strong>{{ row.awayTeamCn }}</strong>
              </td>
              <td class="recommendation-items-cell">
                <div class="recommendation-item-list">
                  <div v-for="item in row.recommendations" :key="item.key" class="recommendation-item">
                    <span class="recommendation-market-name">{{ item.marketName }}</span>
                    <span class="recommendation-result-pill" :class="'is-' + item.probabilityKey">
                      {{ item.resultName }}
                    </span>
                    <span class="recommendation-item-probability">{{ item.probabilityText }}</span>
                    <span class="recommendation-item-odds">{{ item.oddsText }}</span>
                  </div>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div
      v-if="headToHeadDialogVisible"
      class="dialog-backdrop"
      @click.self="closeHeadToHeadDialog"
      @keydown.esc="closeHeadToHeadDialog"
    >
      <section
        ref="headToHeadDialog"
        class="head-to-head-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="head-to-head-dialog-title"
        tabindex="-1"
      >
        <header class="head-to-head-dialog-header">
          <div>
            <h3 id="head-to-head-dialog-title">{{ headToHeadTitle }}</h3>
          </div>
          <button type="button" class="dialog-close" aria-label="关闭历史交战弹窗" @click="closeHeadToHeadDialog">×</button>
        </header>
        <p class="head-to-head-description">仅展示本场开赛前已结束的交锋，最多显示最近 10 场</p>

        <div v-if="headToHeadLoading" class="dialog-state" role="status">正在读取历史交战数据...</div>
        <div v-else-if="headToHeadError" class="dialog-state is-error">{{ headToHeadError }}</div>
        <div v-else-if="headToHeadMatches.length === 0" class="dialog-state">暂无双方历史交战数据</div>
        <div v-else class="head-to-head-list">
          <article
            v-for="(item, index) in headToHeadMatches"
            :key="item.matchDate + '-' + item.homeTeamCn + '-' + item.awayTeamCn + '-' + index"
            class="head-to-head-item"
          >
            <div class="head-to-head-meta">
              <span>{{ item.matchDate }}{{ formatHeadToHeadKickoffTime(item.kickoffTime) }}</span>
              <span>{{ item.competitionName }}</span>
              <span v-if="item.neutral" class="neutral-badge">中立场</span>
            </div>
            <div class="head-to-head-score">
              <span class="head-to-head-team is-home">{{ item.homeTeamCn }}</span>
              <strong>{{ item.homeScore }} : {{ item.awayScore }}</strong>
              <span class="head-to-head-team is-away">{{ item.awayTeamCn }}</span>
            </div>
          </article>
        </div>
      </section>
    </div>

    <div v-if="backtesting" class="backtest-mask" role="status" aria-live="polite" aria-busy="true">
      <div class="backtest-mask-card">
        <div class="backtest-mask-heading">
          <strong>正在回测</strong>
          <span>{{ backtestProgressText }}</span>
        </div>
        <span>{{ backtestProgressDetail }}</span>
        <div
          class="backtest-progress"
          role="progressbar"
          aria-label="回测进度"
          aria-valuemin="0"
          aria-valuemax="100"
          :aria-valuenow="backtestProgress"
          :aria-valuetext="backtestProgressDetail"
        >
          <span class="backtest-progress-bar" :style="{ width: backtestProgress + '%' }"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const FIXED_SIMULATIONS = 50000
const BACKTEST_PROGRESS_POLL_INTERVAL = 300
const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日']
const UTC_PLUS_EIGHT_TIME_ZONE = 'Asia/Shanghai'
const COMPETITIONS = [
  { code: 'ALL', name: '全部' },
  { code: 'WORLD_CUP', name: '世界杯' },
  { code: 'CHAMPIONS_LEAGUE', name: '欧冠' },
  { code: 'EUROPA_LEAGUE', name: '欧罗巴' },
  { code: 'BRAZIL_SERIE_A', name: '巴甲' },
  { code: 'MLS', name: '美职' },
  { code: 'NORWEGIAN_ELITESERIEN', name: '挪超' },
  { code: 'SWEDISH_ALLSVENSKAN', name: '瑞超' },
  { code: 'K_LEAGUE_1', name: '韩职' },
  { code: 'FINNISH_VEIKKAUSLIIGA', name: '芬超' }
]
const SELECTION_COOKIE = 'worldcup_recommendation_rows'
const SELECTION_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const MODEL_FACTOR_COOKIE = 'worldcup_model_factors'
const MODEL_FACTOR_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const GLOBAL_PARAMETER_COOKIE = 'worldcup_global_parameters'
const GLOBAL_PARAMETER_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const ACTIVE_COMPETITION_COOKIE = 'football_active_competition'
const ACTIVE_COMPETITION_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const DEFAULT_HOST_TEAM_GOAL_FACTOR = 1.21
const DEFAULT_HOME_TEAM_GOAL_FACTOR = 1.05
const DEFAULT_SEED_TEAM_GOAL_FACTOR = 1.80
const DEFAULT_HANDICAP_SMOOTHING_FACTOR = 0.200
const DEFAULT_RECOMMENDATION_ODDS = 1.62
const DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD = 58.00
const DEFAULT_HANDICAP_REVERSE_THRESHOLD = 52.00
const DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD = 73.00
const RECOMMENDATION_ODDS_MIN = 1
const RECOMMENDATION_ODDS_MAX = 100
const RECOMMENDATION_THRESHOLD_MIN = 0
const RECOMMENDATION_THRESHOLD_MAX = 100
const MODEL_FACTOR_MIN = 0.1
const MODEL_FACTOR_MAX = 3
const HANDICAP_SMOOTHING_MIN = 0
const HANDICAP_SMOOTHING_MAX = 0.8
const STABLE_PARAMETER_PRESET = {
  modelFactors: {
    hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR,
    homeTeamGoalFactor: DEFAULT_HOME_TEAM_GOAL_FACTOR,
    seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR,
    handicapSmoothingFactor: DEFAULT_HANDICAP_SMOOTHING_FACTOR
  },
  globalParameters: {
    recommendationOdds: DEFAULT_RECOMMENDATION_ODDS,
    handicapRecommendationThreshold: DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD,
    handicapReverseThreshold: DEFAULT_HANDICAP_REVERSE_THRESHOLD,
    singleRecommendationThreshold: DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
  }
}
const AGGRESSIVE_PARAMETER_PRESET = {
  modelFactors: {
    hostTeamGoalFactor: 1.21,
    homeTeamGoalFactor: 1.05,
    seedTeamGoalFactor: 1.80,
    handicapSmoothingFactor: 0.200
  },
  globalParameters: {
    recommendationOdds: 2.42,
    handicapRecommendationThreshold: 58.00,
    handicapReverseThreshold: 52.00,
    singleRecommendationThreshold: 71.00
  }
}
const MODEL_FACTOR_KEYS = [
  'hostTeamGoalFactor',
  'homeTeamGoalFactor',
  'seedTeamGoalFactor',
  'handicapSmoothingFactor'
]
const PROBABILITY_KEYS = ['win', 'draw', 'lose']
const PROBABILITY_LABELS = {
  win: '胜',
  draw: '平',
  lose: '负'
}

function createEmptyBacktestSummary() {
  return {
    completedMatchCount: 0,
    sportteryCompletedMatchCount: 0,
    oddsMatchCount: 0,
    recommendedMatchCount: 0,
    recommendedSelectionCount: 0,
    hitMatchCount: 0,
    missMatchCount: 0,
    winningSelectionCount: 0,
    averageWinningOdds: null,
    averageOddsIncludingMisses: null
  }
}

function getUtcPlusEightDate() {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: UTC_PLUS_EIGHT_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(new Date())
  const values = Object.fromEntries(parts.map(part => [part.type, part.value]))
  return values.year + '-' + values.month + '-' + values.day
}

export default {
  name: 'App',
  data() {
    return {
      overview: {},
      competitionOverviews: {},
      response: {},
      competitions: COMPETITIONS,
      activeCompetitions: ['WORLD_CUP'],
      draftCompetitions: ['WORLD_CUP'],
      competitionDropdownOpen: false,
      competitionSearch: '',
      scheduleDates: [],
      weekdays: WEEKDAYS,
      calendarMonth: '',
      queryDate: '',
      modelMode: 'after',
      modelFactors: {
        hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR.toFixed(2),
        homeTeamGoalFactor: DEFAULT_HOME_TEAM_GOAL_FACTOR.toFixed(2),
        seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR.toFixed(2),
        handicapSmoothingFactor: DEFAULT_HANDICAP_SMOOTHING_FACTOR.toFixed(3)
      },
      recommendationOdds: DEFAULT_RECOMMENDATION_ODDS.toFixed(2),
      handicapRecommendationThreshold: DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD.toFixed(2),
      handicapReverseThreshold: DEFAULT_HANDICAP_REVERSE_THRESHOLD.toFixed(2),
      singleRecommendationThreshold: DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD.toFixed(2),
      activeParameterPreset: 'stable',
      selectedRows: {},
      backtesting: false,
      backtestProgress: 0,
      backtestProcessedMatchCount: 0,
      backtestTotalMatchCount: 0,
      backtestActive: false,
      backtestSourceMatches: [],
      backtestMatches: [],
      backtestSummary: createEmptyBacktestSummary(),
      recommendationDialogVisible: false,
      headToHeadDialogVisible: false,
      headToHeadLoading: false,
      headToHeadError: '',
      headToHeadMatch: null,
      headToHeadMatches: [],
      headToHeadRequestId: 0,
      loading: false,
      updatingData: false,
      errorMessage: ''
    }
  },
  computed: {
    activeCompetition() {
      const selectedCodes = this.getSelectedCompetitionCodes()
      if (selectedCodes.length === this.getConcreteCompetitions().length) {
        return 'ALL'
      }
      return selectedCodes.length === 1 ? selectedCodes[0] : 'MULTIPLE'
    },
    activeCompetitionLabel() {
      const selectedCompetitions = this.getSelectedCompetitions()
      if (selectedCompetitions.length <= 3) {
        return selectedCompetitions.map(item => item.name).join('、')
      }
      return '已选 ' + selectedCompetitions.length + ' 项'
    },
    filteredCompetitionOptions() {
      const keyword = this.competitionSearch.trim().toLocaleLowerCase('zh-CN')
      if (!keyword) {
        return this.competitions
      }
      return this.competitions.filter(competition => {
        return competition.name.toLocaleLowerCase('zh-CN').includes(keyword)
      })
    },
    matches() {
      const sourceMatches = this.backtestActive ? this.backtestMatches : (this.response.matches || [])
      return sourceMatches.filter(match => this.hasSportteryOdds(match))
    },
    recommendationDialogTitle() {
      return (this.queryDate || '当前日期') + ' 推荐汇总'
    },
    recommendationDialogRows() {
      const matches = Array.isArray(this.response.matches) ? this.response.matches : []
      return matches
        .filter(match => !this.queryDate || match.matchDate === this.queryDate)
        .map(match => this.buildRecommendationDialogRow(match))
        .filter(Boolean)
    },
    recommendationDialogMatchCount() {
      return this.recommendationDialogRows.length
    },
    recommendationDialogItemCount() {
      return this.recommendationDialogRows.reduce((count, row) => count + row.recommendations.length, 0)
    },
    headToHeadTitle() {
      if (!this.headToHeadMatch) {
        return ''
      }
      return this.headToHeadMatch.homeTeamCn + ' vs ' + this.headToHeadMatch.awayTeamCn
    },
    backtestAverageOddsText() {
      return this.formatBacktestOdds(this.backtestSummary.averageOddsIncludingMisses)
    },
    backtestAverageRecommendationText() {
      if (!this.backtestActive) {
        return '--'
      }
      const recommendedMatchCount = Number(this.backtestSummary.recommendedMatchCount) || 0
      if (recommendedMatchCount <= 0) {
        return '0.00'
      }
      const recommendedSelectionCount = Number(this.backtestSummary.recommendedSelectionCount) || 0
      return (recommendedSelectionCount / recommendedMatchCount).toFixed(2)
    },
    backtestHitRateText() {
      if (!this.backtestActive) {
        return '--'
      }
      const recommendedMatchCount = Number(this.backtestSummary.recommendedMatchCount) || 0
      if (recommendedMatchCount <= 0) {
        return '0.0%'
      }
      const hitMatchCount = Number(this.backtestSummary.hitMatchCount) || 0
      return ((hitMatchCount / recommendedMatchCount) * 100).toFixed(1) + '%'
    },
    backtestRoiText() {
      if (!this.backtestActive) {
        return '--'
      }
      const recommendedMatchCount = Number(this.backtestSummary.recommendedMatchCount) || 0
      if (recommendedMatchCount <= 0) {
        return '0.0%'
      }
      const hitMatchCount = Number(this.backtestSummary.hitMatchCount) || 0
      if (hitMatchCount <= 0) {
        return '-100.0%'
      }
      const recommendedSelectionCount = Number(this.backtestSummary.recommendedSelectionCount) || 0
      const averageRecommendations = recommendedSelectionCount / recommendedMatchCount
      const averageOddsIncludingMisses = Number(this.backtestSummary.averageOddsIncludingMisses)
      if (!Number.isFinite(averageRecommendations) || averageRecommendations <= 0 ||
        !Number.isFinite(averageOddsIncludingMisses) || averageOddsIncludingMisses <= 0) {
        return '--'
      }
      const hitRate = hitMatchCount / recommendedMatchCount
      const netRoi = Math.pow((hitRate * averageOddsIncludingMisses) / averageRecommendations, 2) - 1
      return (netRoi * 100).toFixed(1) + '%'
    },
    backtestProgressText() {
      return Number(this.backtestProgress).toFixed(1) + '%'
    },
    backtestProgressDetail() {
      const processedMatchCount = Number(this.backtestProcessedMatchCount) || 0
      const totalMatchCount = Number(this.backtestTotalMatchCount) || 0
      if (totalMatchCount <= 0) {
        return '正在准备历史赔率样本...'
      }
      return '已完成 ' + processedMatchCount.toLocaleString('zh-CN') +
        ' / ' + totalMatchCount.toLocaleString('zh-CN') + ' 场'
    },
    parameterPresetToggleText() {
      return this.activeParameterPreset === 'aggressive' ? '切换稳健方案' : '切换激进方案'
    },
    matchColumns() {
      const matchCount = this.matches.length || 1
      if (matchCount === 4) {
        return '2'
      }
      return String(Math.min(matchCount, 3))
    },
    calendarCells() {
      return this.buildCalendarCells()
    },
    calendarTitle() {
      if (!this.calendarMonth) {
        return ''
      }
      const parts = this.calendarMonth.split('-').map(Number)
      return parts[0] + ' 年 ' + parts[1] + ' 月'
    }
  },
  created() {
    this.initializeUserConfig()
  },
  mounted() {
    document.addEventListener('click', this.handleCompetitionOutsideClick)
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleCompetitionOutsideClick)
    document.body.classList.remove('dialog-open')
  },
  methods: {
    toggleCompetitionDropdown() {
      if (this.loading || this.updatingData || this.backtesting) {
        return
      }
      if (this.competitionDropdownOpen) {
        this.closeCompetitionDropdown()
        return
      }
      this.draftCompetitions = this.getSelectedCompetitionCodes()
      this.competitionSearch = ''
      this.competitionDropdownOpen = true
      this.$nextTick(() => {
        if (this.$refs.competitionSearchInput) {
          this.$refs.competitionSearchInput.focus()
        }
      })
    },
    closeCompetitionDropdown() {
      this.competitionDropdownOpen = false
      this.competitionSearch = ''
      this.draftCompetitions = this.getSelectedCompetitionCodes()
    },
    handleCompetitionOutsideClick(event) {
      if (!this.competitionDropdownOpen || !this.$refs.competitionSelect) {
        return
      }
      if (!this.$refs.competitionSelect.contains(event.target)) {
        this.closeCompetitionDropdown()
      }
    },
    isDraftCompetitionSelected(code) {
      if (code === 'ALL') {
        return this.draftCompetitions.length === this.getConcreteCompetitions().length
      }
      return this.draftCompetitions.includes(code)
    },
    toggleDraftCompetition(code) {
      if (code === 'ALL') {
        this.draftCompetitions = this.isDraftCompetitionSelected('ALL')
          ? []
          : this.getConcreteCompetitions().map(item => item.code)
        return
      }
      if (this.draftCompetitions.includes(code)) {
        this.draftCompetitions = this.draftCompetitions.filter(item => item !== code)
      } else {
        const selectedCodes = new Set([...this.draftCompetitions, code])
        this.draftCompetitions = this.getConcreteCompetitions()
          .map(item => item.code)
          .filter(item => selectedCodes.has(item))
      }
    },
    async applyCompetitionSelection() {
      if (this.draftCompetitions.length === 0 || this.loading || this.updatingData || this.backtesting) {
        return
      }
      const selectedCodes = new Set(this.draftCompetitions)
      const nextCompetitions = this.getConcreteCompetitions()
        .map(item => item.code)
        .filter(code => selectedCodes.has(code))
      const selectionChanged = nextCompetitions.join(',') !== this.getSelectedCompetitionCodes().join(',')
      this.competitionDropdownOpen = false
      this.competitionSearch = ''
      if (!selectionChanged) {
        return
      }
      const currentDate = this.queryDate
      this.clearBacktestResults()
      this.activeCompetitions = nextCompetitions
      this.draftCompetitions = [...nextCompetitions]
      this.saveActiveCompetition()
      this.overview = {}
      this.response = {}
      this.scheduleDates = []
      await this.loadOverview(currentDate)
    },
    openRecommendationDialog() {
      this.recommendationDialogVisible = true
      document.body.classList.add('dialog-open')
      this.$nextTick(() => {
        if (this.$refs.recommendationDialog) {
          this.$refs.recommendationDialog.focus()
        }
      })
    },
    closeRecommendationDialog() {
      this.recommendationDialogVisible = false
      document.body.classList.remove('dialog-open')
    },
    buildRecommendationDialogRow(match) {
      if (!match) {
        return null
      }
      const recommendationKeys = this.getRecommendationKeys(match)
      if (recommendationKeys.size === 0) {
        return null
      }
      const competition = this.competitions.find(item => item.code === match.competition)
      const matchKey = (match.competition || '') + '-' + match.matchId
      const recommendations = []
      this.probabilityRows(match).forEach(item => {
        PROBABILITY_KEYS.forEach(probabilityKey => {
          const recommendationKey = this.getRecommendationCellKey(item, probabilityKey)
          if (!recommendationKeys.has(recommendationKey)) {
            return
          }
          recommendations.push({
            key: matchKey + '-' + recommendationKey,
            marketName: item.handicap === 0 ? '不让球' : '让球 ' + this.formatHandicap(item.handicap),
            probabilityKey,
            resultName: PROBABILITY_LABELS[probabilityKey],
            probabilityText: this.formatProbability(item.probability[probabilityKey]),
            oddsText: this.formatSportteryOdds(this.sportteryOddsValue(match, item, probabilityKey))
          })
        })
      })
      if (recommendations.length === 0) {
        return null
      }
      return {
        key: matchKey,
        kickoffTime: match.kickoffTime ? String(match.kickoffTime).slice(0, 5) : '--',
        matchNumber: match.sportteryMatchNumber || '',
        competitionName: competition ? competition.name : (match.competition || '--'),
        homeTeamCn: match.homeTeamCn,
        awayTeamCn: match.awayTeamCn,
        recommendations
      }
    },
    async openHeadToHeadDialog(match) {
      if (!match || !match.matchId) {
        return
      }
      const requestId = ++this.headToHeadRequestId
      this.headToHeadMatch = match
      this.headToHeadMatches = []
      this.headToHeadError = ''
      this.headToHeadLoading = true
      this.headToHeadDialogVisible = true
      document.body.classList.add('dialog-open')
      this.$nextTick(() => {
        if (this.$refs.headToHeadDialog) {
          this.$refs.headToHeadDialog.focus()
        }
      })

      try {
        const params = new URLSearchParams()
        params.append('competition', match.competition || this.activeCompetition)
        params.append('matchId', match.matchId)
        params.append('limit', '10')
        const res = await fetch('/api/football/head-to-head?' + params.toString())
        if (!res.ok) {
          throw new Error('服务响应异常')
        }
        const data = await res.json()
        if (requestId !== this.headToHeadRequestId) {
          return
        }
        this.headToHeadMatches = Array.isArray(data) ? data : []
      } catch (error) {
        if (requestId === this.headToHeadRequestId) {
          this.headToHeadError = '读取历史交战数据失败：' + error.message
        }
      } finally {
        if (requestId === this.headToHeadRequestId) {
          this.headToHeadLoading = false
        }
      }
    },
    closeHeadToHeadDialog() {
      this.headToHeadRequestId += 1
      this.headToHeadDialogVisible = false
      this.headToHeadLoading = false
      this.headToHeadError = ''
      this.headToHeadMatch = null
      this.headToHeadMatches = []
      document.body.classList.remove('dialog-open')
    },
    formatHeadToHeadKickoffTime(value) {
      if (!value) {
        return ''
      }
      return ' ' + String(value).slice(0, 5)
    },
    async initializeUserConfig() {
      this.loadActiveCompetition()
      this.loadModelFactors()
      this.loadGlobalParameters()
      this.syncActiveParameterPreset()
      this.loadRecommendationSelections()
      await this.loadUserConfig()
      this.loadOverview()
    },
    async loadUserConfig() {
      try {
        const res = await fetch('/api/football/user-config')
        if (!res.ok) {
          return
        }
        const data = await res.json()
        this.applyUserConfig(data)
      } catch (error) {
        // Cookies remain as an offline fallback if the config file cannot be read.
      }
    },
    applyUserConfig(config) {
      if (!config || typeof config !== 'object' || Array.isArray(config)) {
        return
      }
      if (config.modelFactors && typeof config.modelFactors === 'object' && !Array.isArray(config.modelFactors)) {
        MODEL_FACTOR_KEYS.forEach(key => {
          this.$set(this.modelFactors, key, this.formatModelFactorValue(config.modelFactors[key], this.getDefaultModelFactor(key), key))
        })
        this.saveModelFactorsToCookie()
      }
      if (config.globalParameters && typeof config.globalParameters === 'object' && !Array.isArray(config.globalParameters)) {
        this.recommendationOdds = this.formatRecommendationOddsValue(config.globalParameters.recommendationOdds)
        this.handicapRecommendationThreshold = this.formatRecommendationThresholdValue(
          config.globalParameters.handicapRecommendationThreshold,
          DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD
        )
        this.handicapReverseThreshold = this.formatRecommendationThresholdValue(
          config.globalParameters.handicapReverseThreshold,
          DEFAULT_HANDICAP_REVERSE_THRESHOLD
        )
        this.singleRecommendationThreshold = this.formatRecommendationThresholdValue(
          config.globalParameters.singleRecommendationThreshold,
          DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
        )
        this.saveGlobalParametersToCookie()
      }
      if (config.selectedRows && typeof config.selectedRows === 'object' && !Array.isArray(config.selectedRows)) {
        this.selectedRows = this.normalizeSelectedRows(config.selectedRows)
        this.saveRecommendationSelectionsToCookie()
      }
      this.syncActiveParameterPreset()
    },
    async loadOverview(preferredDate) {
      this.errorMessage = ''
      try {
        const selectedCompetitions = this.getSelectedCompetitions()
        const overviewEntries = await Promise.all(selectedCompetitions.map(async competition => {
          return [competition.code, await this.fetchCompetitionOverview(competition.code)]
        }))
        this.competitionOverviews = Object.fromEntries(overviewEntries)
        const data = overviewEntries.length === 1
          ? overviewEntries[0][1]
          : this.mergeCompetitionOverviews(overviewEntries.map(entry => entry[1]))
        this.applyOverview(data, preferredDate)
        if (this.queryDate) {
          await this.loadPredictions()
        }
      } catch (error) {
        this.errorMessage = '读取赛程概览失败：' + error.message
      }
    },
    getConcreteCompetitions() {
      return this.competitions.filter(competition => competition.code !== 'ALL')
    },
    getSelectedCompetitionCodes() {
      const selectedCodes = new Set(this.activeCompetitions)
      return this.getConcreteCompetitions()
        .map(competition => competition.code)
        .filter(code => selectedCodes.has(code))
    },
    getSelectedCompetitions() {
      const selectedCodes = new Set(this.getSelectedCompetitionCodes())
      return this.getConcreteCompetitions().filter(competition => selectedCodes.has(competition.code))
    },
    async fetchCompetitionOverview(competition) {
      const params = new URLSearchParams()
      params.append('competition', competition)
      const res = await fetch('/api/football/overview?' + params.toString())
      if (!res.ok) {
        throw new Error('服务响应异常')
      }
      return res.json()
    },
    mergeCompetitionOverviews(overviews) {
      const historicalMatchCount = overviews.reduce((sum, item) => sum + (Number(item.historicalMatchCount) || 0), 0)
      const weightedBaselineGoals = overviews.reduce((sum, item) => {
        return sum + (Number(item.baselineGoals) || 0) * (Number(item.historicalMatchCount) || 0)
      }, 0)
      return {
        competition: this.activeCompetition,
        competitionName: this.activeCompetitionLabel,
        historicalMatchCount,
        scheduleMatchCount: overviews.reduce((sum, item) => sum + (Number(item.scheduleMatchCount) || 0), 0),
        completedMatchCount: overviews.reduce((sum, item) => sum + (Number(item.completedMatchCount) || 0), 0),
        baselineGoals: historicalMatchCount > 0 ? weightedBaselineGoals / historicalMatchCount : 0,
        scheduleDates: Array.from(new Set(overviews.flatMap(item => item.scheduleDates || []))).sort()
      }
    },
    applyOverview(data, preferredDate) {
      const nextScheduleDates = data.scheduleDates || []
      const nextDate = preferredDate || this.findDefaultDate(nextScheduleDates)
      this.overview = data
      this.scheduleDates = nextScheduleDates
      this.queryDate = nextDate
      this.calendarMonth = this.queryDate
        ? this.getMonthKey(this.parseDate(this.queryDate))
        : this.getMonthKey(this.parseDate(getUtcPlusEightDate()))
    },
    async refreshData() {
      if (this.updatingData || this.backtesting) {
        return
      }
      this.updatingData = true
      this.errorMessage = ''
      try {
        const currentDate = this.queryDate
        const params = new URLSearchParams()
        params.append('competition', this.getSelectedCompetitionCodes()[0] || 'WORLD_CUP')
        if (currentDate) {
          params.append('date', currentDate)
        }
        const res = await fetch('/api/football/data/refresh?' + params.toString(), {
          method: 'POST'
        })
        if (!res.ok) {
          throw new Error('服务响应异常')
        }
        await res.json()
        await this.loadOverview(currentDate)
      } catch (error) {
        this.errorMessage = '更新数据失败：' + error.message
      } finally {
        this.updatingData = false
      }
    },
    async loadPredictions() {
      if (!this.queryDate) {
        this.errorMessage = '请选择比赛日期'
        return
      }
      this.clearBacktestResults()
      this.loading = true
      this.errorMessage = ''
      try {
        const competitions = this.getPredictionCompetitions()
        const responses = await Promise.all(competitions.map(competition => this.fetchCompetitionPredictions(competition)))
        const competitionOrder = new Map(this.getConcreteCompetitions().map((competition, index) => [competition.code, index]))
        const matches = responses.flatMap(response => response.matches || [])
        matches.sort((left, right) => {
          const leftTime = (left.matchDate || '') + ' ' + (left.kickoffTime || '')
          const rightTime = (right.matchDate || '') + ' ' + (right.kickoffTime || '')
          return leftTime.localeCompare(rightTime) ||
            (competitionOrder.get(left.competition) ?? Number.MAX_SAFE_INTEGER) -
            (competitionOrder.get(right.competition) ?? Number.MAX_SAFE_INTEGER) ||
            String(left.matchId || '').localeCompare(String(right.matchId || ''))
        })
        this.response = {
          competition: this.activeCompetition,
          date: this.queryDate,
          simulations: FIXED_SIMULATIONS,
          total: matches.length,
          matches
        }
      } catch (error) {
        this.errorMessage = '查询概率失败：' + error.message
      } finally {
        this.loading = false
      }
    },
    getPredictionCompetitions() {
      return this.getSelectedCompetitions()
        .filter(competition => {
          const overview = this.competitionOverviews[competition.code]
          return !overview || !Array.isArray(overview.scheduleDates) || overview.scheduleDates.includes(this.queryDate)
        })
        .map(competition => competition.code)
    },
    async fetchCompetitionPredictions(competition) {
      const params = new URLSearchParams()
      params.append('date', this.queryDate)
      params.append('competition', competition)
      params.append('simulations', FIXED_SIMULATIONS)
      this.appendModelFactorParams(params)
      const res = await fetch('/api/football/predictions?' + params.toString())
      if (!res.ok) {
        throw new Error('服务响应异常')
      }
      return res.json()
    },
    appendModelFactorParams(params) {
      params.append('hostTeamGoalFactor', this.formatModelFactorValue(this.modelFactors.hostTeamGoalFactor, DEFAULT_HOST_TEAM_GOAL_FACTOR, 'hostTeamGoalFactor'))
      params.append('homeTeamGoalFactor', this.formatModelFactorValue(this.modelFactors.homeTeamGoalFactor, DEFAULT_HOME_TEAM_GOAL_FACTOR, 'homeTeamGoalFactor'))
      params.append('seedTeamGoalFactor', this.formatModelFactorValue(this.modelFactors.seedTeamGoalFactor, DEFAULT_SEED_TEAM_GOAL_FACTOR, 'seedTeamGoalFactor'))
      params.append('handicapSmoothingFactor', this.formatModelFactorValue(this.modelFactors.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor'))
    },
    async runRecommendationOddsBacktest() {
      if (this.backtesting) {
        return
      }
      this.recommendationOdds = this.formatRecommendationOddsValue(this.recommendationOdds)
      this.normalizeRecommendationThresholdInputs()
      this.saveGlobalParametersToCookie()
      this.resetBacktestProgress()
      this.backtesting = true
      this.errorMessage = ''
      try {
        await this.persistUserConfig()
        const params = new URLSearchParams()
        params.append('simulations', FIXED_SIMULATIONS)
        const selectedCompetitions = this.getSelectedCompetitionCodes()
        params.append(
          'competition',
          selectedCompetitions.length === this.getConcreteCompetitions().length
            ? 'ALL'
            : selectedCompetitions.join(',')
        )
        this.appendModelFactorParams(params)
        const res = await fetch('/api/football/recommendation-backtest/jobs?' + params.toString(), {
          method: 'POST'
        })
        if (!res.ok) {
          throw new Error('创建回测任务失败')
        }
        const job = await res.json()
        const data = await this.waitForRecommendationBacktestJob(job)
        this.backtestSourceMatches = Array.isArray(data.matches) ? data.matches : []
        this.backtestSummary = {
          ...createEmptyBacktestSummary(),
          completedMatchCount: Number(data.completedMatchCount) || 0,
          sportteryCompletedMatchCount: Number(data.sportteryCompletedMatchCount) || 0,
          oddsMatchCount: Number(data.oddsMatchCount) || this.backtestSourceMatches.length
        }
        this.refreshBacktestResults()
        this.backtestActive = true
      } catch (error) {
        this.errorMessage = '回测赔率失败：' + error.message
      } finally {
        this.backtesting = false
      }
    },
    async waitForRecommendationBacktestJob(initialJob) {
      let job = initialJob
      while (job && (job.status === 'QUEUED' || job.status === 'RUNNING')) {
        this.applyBacktestProgress(job)
        await this.wait(BACKTEST_PROGRESS_POLL_INTERVAL)
        const res = await fetch(
          '/api/football/recommendation-backtest/jobs/' + encodeURIComponent(job.jobId),
          { cache: 'no-store' }
        )
        if (!res.ok) {
          throw new Error('读取回测进度失败')
        }
        job = await res.json()
      }
      this.applyBacktestProgress(job)
      if (job && job.status === 'COMPLETED' && job.result) {
        return job.result
      }
      throw new Error(job && job.message ? job.message : '回测任务执行失败')
    },
    applyBacktestProgress(job) {
      if (!job) {
        return
      }
      const totalMatchCount = Math.max(0, Number(job.totalMatchCount) || 0)
      const processedMatchCount = Math.max(
        0,
        Math.min(Number(job.processedMatchCount) || 0, totalMatchCount)
      )
      const progress = Number(job.progress)
      this.backtestTotalMatchCount = totalMatchCount
      this.backtestProcessedMatchCount = processedMatchCount
      this.backtestProgress = Number.isFinite(progress)
        ? Math.max(0, Math.min(100, progress))
        : (totalMatchCount > 0 ? processedMatchCount * 100 / totalMatchCount : 0)
    },
    resetBacktestProgress() {
      this.backtestProgress = 0
      this.backtestProcessedMatchCount = 0
      this.backtestTotalMatchCount = 0
    },
    wait(milliseconds) {
      return new Promise(resolve => window.setTimeout(resolve, milliseconds))
    },
    refreshBacktestResults() {
      const recommendedMatches = []
      const hitMatches = []
      const winningMatchOdds = []
      const settledMatchOdds = []
      let recommendedSelectionCount = 0
      let winningSelectionCount = 0
      this.backtestSourceMatches.forEach(match => {
        const recommendationKeys = this.getRecommendationKeys(match)
        if (recommendationKeys.size === 0) {
          return
        }
        recommendedMatches.push(match)
        recommendedSelectionCount += recommendationKeys.size
        if (this.recommendationResult(match) !== 'hit') {
          settledMatchOdds.push(0)
          return
        }
        hitMatches.push(match)
        const matchWinningOdds = this.getRecommendationOddsDetails(match)
          .filter(item => item.winning)
          .map(item => item.odds)
        winningSelectionCount += matchWinningOdds.length
        const winningOdds = matchWinningOdds.reduce((sum, odds) => sum + odds, 0)
        winningMatchOdds.push(winningOdds)
        if (recommendationKeys.size === 1) {
          return
        }
        settledMatchOdds.push(winningOdds)
      })
      const missMatchCount = recommendedMatches.length - hitMatches.length
      this.backtestMatches = hitMatches
      this.backtestSummary = {
        ...this.backtestSummary,
        recommendedMatchCount: recommendedMatches.length,
        recommendedSelectionCount,
        hitMatchCount: hitMatches.length,
        missMatchCount,
        winningSelectionCount,
        averageWinningOdds: this.calculateAverageOdds(winningMatchOdds),
        averageOddsIncludingMisses: this.calculateAverageOdds(settledMatchOdds)
      }
    },
    calculateAverageOdds(values) {
      return values.length > 0
        ? values.reduce((sum, odds) => sum + odds, 0) / values.length
        : null
    },
    formatBacktestOdds(value) {
      const numberValue = Number(value)
      return this.backtestActive && value !== null && Number.isFinite(numberValue)
        ? numberValue.toFixed(2)
        : '--'
    },
    clearBacktestResults() {
      this.backtestActive = false
      this.backtestSourceMatches = []
      this.backtestMatches = []
      this.backtestSummary = createEmptyBacktestSummary()
    },
    findDefaultDate(dates) {
      if (!dates || dates.length === 0) {
        return ''
      }
      const today = getUtcPlusEightDate()
      if (dates.includes(today)) {
        return today
      }
      const upcomingDate = dates.find(date => date >= today)
      return upcomingDate || dates[dates.length - 1]
    },
    selectDate(date) {
      if (!date || this.backtesting) {
        return
      }
      this.queryDate = date
      this.calendarMonth = this.getMonthKey(this.parseDate(date))
      this.loadPredictions()
    },
    shiftCalendarMonth(offset) {
      const baseDate = this.calendarMonth
        ? this.parseDate(this.calendarMonth + '-01')
        : this.parseDate(getUtcPlusEightDate())
      baseDate.setMonth(baseDate.getMonth() + offset)
      this.calendarMonth = this.getMonthKey(baseDate)
    },
    buildCalendarCells() {
      if (!this.calendarMonth) {
        return []
      }
      const dateSet = new Set(this.scheduleDates)
      const monthStart = this.parseDate(this.calendarMonth + '-01')
      const monthEnd = new Date(monthStart.getFullYear(), monthStart.getMonth() + 1, 0)
      const startDate = new Date(monthStart)
      startDate.setDate(startDate.getDate() - this.getCalendarDayIndex(startDate))
      const endDate = new Date(monthEnd)
      endDate.setDate(endDate.getDate() + 6 - this.getCalendarDayIndex(endDate))

      const cells = []
      const current = new Date(startDate)
      while (current <= endDate) {
        const date = this.formatDate(current)
        const inMonth = current.getMonth() === monthStart.getMonth()
        cells.push({
          key: date,
          date: inMonth ? date : '',
          day: inMonth ? current.getDate() : '',
          empty: !inMonth,
          hasSchedule: dateSet.has(date)
        })
        current.setDate(current.getDate() + 1)
      }
      return cells
    },
    parseDate(value) {
      const parts = value.split('-').map(Number)
      return new Date(parts[0], parts[1] - 1, parts[2])
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return year + '-' + month + '-' + day
    },
    getCalendarDayIndex(date) {
      const day = date.getDay()
      return day === 0 ? 6 : day - 1
    },
    getMonthKey(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      return year + '-' + month
    },
    setModelMode(mode) {
      this.modelMode = mode
      if (this.backtestActive) {
        this.refreshBacktestResults()
      }
    },
    loadRecommendationSelections() {
      const cookieValue = this.getCookie(SELECTION_COOKIE)
      if (!cookieValue) {
        return
      }
      if (!cookieValue.startsWith('{')) {
        this.selectedRows = this.parseCompactSelections(cookieValue)
        return
      }
      try {
        const parsedValue = JSON.parse(cookieValue)
        if (parsedValue && typeof parsedValue === 'object' && !Array.isArray(parsedValue)) {
          this.selectedRows = this.normalizeSelectedRows(parsedValue)
        }
      } catch (error) {
        this.selectedRows = {}
      }
    },
    saveRecommendationSelections() {
      this.saveRecommendationSelectionsToCookie()
      this.saveUserConfig()
    },
    saveRecommendationSelectionsToCookie() {
      this.setCookie(SELECTION_COOKIE, this.stringifyCompactSelections(), SELECTION_COOKIE_MAX_AGE)
    },
    parseCompactSelections(value) {
      const result = {}
      value.split('|').forEach(item => {
        const parts = item.split(':')
        if (parts.length !== 2 || !parts[0]) {
          return
        }
        const selection = {}
        let manualOverride = false
        parts[1].split(',').forEach(flag => {
          if (flag === 'o') {
            manualOverride = true
          } else if (flag === 'n') {
            selection.normal = true
            manualOverride = true
          } else if (flag.startsWith('h')) {
            selection.handicap = 'handicap-' + flag.slice(1)
            manualOverride = true
          }
        })
        if (manualOverride) {
          selection.manualOverride = true
          result[parts[0]] = selection
        }
      })
      return result
    },
    normalizeSelectedRows(value) {
      const result = {}
      Object.keys(value || {}).forEach(matchId => {
        const selection = value[matchId]
        if (!matchId || !selection || typeof selection !== 'object' || Array.isArray(selection)) {
          return
        }
        const validHandicap = typeof selection.handicap === 'string' && /^handicap--?\d+$/.test(selection.handicap)
        const manualOverride = selection.manualOverride === true || selection.normal === true || validHandicap
        if (!manualOverride) {
          return
        }
        const normalized = { manualOverride: true }
        if (selection.normal === true) {
          normalized.normal = true
        }
        if (validHandicap) {
          normalized.handicap = selection.handicap
        }
        result[matchId] = normalized
      })
      return result
    },
    stringifyCompactSelections() {
      return Object.keys(this.selectedRows).map(matchId => {
        const selection = this.selectedRows[matchId] || {}
        const flags = []
        if (selection.manualOverride === true) {
          flags.push('o')
        }
        if (selection.normal) {
          flags.push('n')
        }
        if (selection.handicap) {
          flags.push('h' + selection.handicap.replace('handicap-', ''))
        }
        return flags.length ? matchId + ':' + flags.join(',') : ''
      }).filter(Boolean).join('|')
    },
    getCookie(name) {
      const prefix = name + '='
      const cookies = document.cookie ? document.cookie.split(';') : []
      for (const cookie of cookies) {
        const text = cookie.trim()
        if (text.startsWith(prefix)) {
          return decodeURIComponent(text.slice(prefix.length))
        }
      }
      return ''
    },
    setCookie(name, value, maxAge) {
      document.cookie = name + '=' + encodeURIComponent(value) + '; max-age=' + maxAge + '; path=/; SameSite=Lax'
    },
    loadActiveCompetition() {
      const value = this.getCookie(ACTIVE_COMPETITION_COOKIE)
      if (!value) {
        return
      }
      const concreteCompetitions = this.getConcreteCompetitions()
      const validCodes = new Set(concreteCompetitions.map(item => item.code))
      const selectedCodes = value.split(',').map(code => code.trim()).filter(code => validCodes.has(code))
      this.activeCompetitions = value === 'ALL'
        ? concreteCompetitions.map(item => item.code)
        : concreteCompetitions.map(item => item.code).filter(code => selectedCodes.includes(code))
      if (this.activeCompetitions.length === 0) {
        this.activeCompetitions = ['WORLD_CUP']
      }
      this.draftCompetitions = [...this.activeCompetitions]
    },
    saveActiveCompetition() {
      const selectedCodes = this.getSelectedCompetitionCodes()
      const value = selectedCodes.length === this.getConcreteCompetitions().length
        ? 'ALL'
        : selectedCodes.join(',')
      this.setCookie(ACTIVE_COMPETITION_COOKIE, value, ACTIVE_COMPETITION_COOKIE_MAX_AGE)
    },
    loadGlobalParameters() {
      const cookieValue = this.getCookie(GLOBAL_PARAMETER_COOKIE)
      if (!cookieValue) {
        return
      }
      try {
        const parsedValue = JSON.parse(cookieValue)
        if (parsedValue && typeof parsedValue === 'object' && !Array.isArray(parsedValue)) {
          this.recommendationOdds = this.formatRecommendationOddsValue(parsedValue.recommendationOdds)
          this.handicapRecommendationThreshold = this.formatRecommendationThresholdValue(
            parsedValue.handicapRecommendationThreshold,
            DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD
          )
          this.handicapReverseThreshold = this.formatRecommendationThresholdValue(
            parsedValue.handicapReverseThreshold,
            DEFAULT_HANDICAP_REVERSE_THRESHOLD
          )
          this.singleRecommendationThreshold = this.formatRecommendationThresholdValue(
            parsedValue.singleRecommendationThreshold,
            DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
          )
        }
      } catch (error) {
        this.recommendationOdds = DEFAULT_RECOMMENDATION_ODDS.toFixed(2)
        this.handicapRecommendationThreshold = DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD.toFixed(2)
        this.handicapReverseThreshold = DEFAULT_HANDICAP_REVERSE_THRESHOLD.toFixed(2)
        this.singleRecommendationThreshold = DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD.toFixed(2)
      }
    },
    saveGlobalParametersToCookie() {
      this.setCookie(
        GLOBAL_PARAMETER_COOKIE,
        JSON.stringify(this.buildGlobalParameterPayload()),
        GLOBAL_PARAMETER_COOKIE_MAX_AGE
      )
    },
    buildGlobalParameterPayload() {
      return {
        recommendationOdds: this.normalizeRecommendationOdds(this.recommendationOdds),
        handicapRecommendationThreshold: this.normalizeRecommendationThreshold(
          this.handicapRecommendationThreshold,
          DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD
        ),
        handicapReverseThreshold: this.normalizeRecommendationThreshold(
          this.handicapReverseThreshold,
          DEFAULT_HANDICAP_REVERSE_THRESHOLD
        ),
        singleRecommendationThreshold: this.normalizeRecommendationThreshold(
          this.singleRecommendationThreshold,
          DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
        )
      }
    },
    normalizeRecommendationOdds(value) {
      const numberValue = Number(value)
      if (!Number.isFinite(numberValue)) {
        return DEFAULT_RECOMMENDATION_ODDS
      }
      return Number(Math.max(RECOMMENDATION_ODDS_MIN, Math.min(RECOMMENDATION_ODDS_MAX, numberValue)).toFixed(2))
    },
    normalizeRecommendationOddsInput() {
      this.recommendationOdds = this.formatRecommendationOddsValue(this.recommendationOdds)
      this.saveGlobalParametersToCookie()
      this.saveUserConfig()
      if (this.backtestActive) {
        this.refreshBacktestResults()
      }
    },
    normalizeRecommendationThreshold(value, fallback) {
      const numberValue = Number(value)
      if (!Number.isFinite(numberValue)) {
        return fallback
      }
      const clampedValue = Math.max(RECOMMENDATION_THRESHOLD_MIN, Math.min(RECOMMENDATION_THRESHOLD_MAX, numberValue))
      return Number(clampedValue.toFixed(2))
    },
    formatRecommendationOddsValue(value) {
      return this.normalizeRecommendationOdds(value).toFixed(2)
    },
    formatRecommendationThresholdValue(value, fallback) {
      return this.normalizeRecommendationThreshold(value, fallback).toFixed(2)
    },
    normalizeRecommendationThresholdInputs() {
      this.handicapRecommendationThreshold = this.formatRecommendationThresholdValue(
        this.handicapRecommendationThreshold,
        DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD
      )
      this.handicapReverseThreshold = this.formatRecommendationThresholdValue(
        this.handicapReverseThreshold,
        DEFAULT_HANDICAP_REVERSE_THRESHOLD
      )
      this.singleRecommendationThreshold = this.formatRecommendationThresholdValue(
        this.singleRecommendationThreshold,
        DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
      )
    },
    saveRecommendationThresholdInputs() {
      this.normalizeRecommendationThresholdInputs()
      this.saveGlobalParametersToCookie()
      this.saveUserConfig()
      if (this.backtestActive) {
        this.refreshBacktestResults()
      }
    },
    loadModelFactors() {
      const cookieValue = this.getCookie(MODEL_FACTOR_COOKIE)
      if (!cookieValue) {
        return
      }
      try {
        const parsedValue = JSON.parse(cookieValue)
        if (!parsedValue || typeof parsedValue !== 'object' || Array.isArray(parsedValue)) {
          return
        }
        this.modelFactors = {
          hostTeamGoalFactor: this.formatModelFactorValue(parsedValue.hostTeamGoalFactor, DEFAULT_HOST_TEAM_GOAL_FACTOR, 'hostTeamGoalFactor'),
          homeTeamGoalFactor: this.formatModelFactorValue(parsedValue.homeTeamGoalFactor, DEFAULT_HOME_TEAM_GOAL_FACTOR, 'homeTeamGoalFactor'),
          seedTeamGoalFactor: this.formatModelFactorValue(parsedValue.seedTeamGoalFactor, DEFAULT_SEED_TEAM_GOAL_FACTOR, 'seedTeamGoalFactor'),
          handicapSmoothingFactor: this.formatModelFactorValue(parsedValue.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor')
        }
      } catch (error) {
        this.modelFactors = {
          hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR.toFixed(2),
          homeTeamGoalFactor: DEFAULT_HOME_TEAM_GOAL_FACTOR.toFixed(2),
          seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR.toFixed(2),
          handicapSmoothingFactor: DEFAULT_HANDICAP_SMOOTHING_FACTOR.toFixed(3)
        }
      }
    },
    saveModelFactors() {
      this.saveModelFactorsToCookie()
      this.saveUserConfig()
    },
    saveModelFactorsToCookie() {
      this.setCookie(MODEL_FACTOR_COOKIE, JSON.stringify(this.buildModelFactorPayload()), MODEL_FACTOR_COOKIE_MAX_AGE)
    },
    buildModelFactorPayload() {
      return {
        hostTeamGoalFactor: Number(this.formatModelFactorValue(this.modelFactors.hostTeamGoalFactor, DEFAULT_HOST_TEAM_GOAL_FACTOR, 'hostTeamGoalFactor')),
        homeTeamGoalFactor: Number(this.formatModelFactorValue(this.modelFactors.homeTeamGoalFactor, DEFAULT_HOME_TEAM_GOAL_FACTOR, 'homeTeamGoalFactor')),
        seedTeamGoalFactor: Number(this.formatModelFactorValue(this.modelFactors.seedTeamGoalFactor, DEFAULT_SEED_TEAM_GOAL_FACTOR, 'seedTeamGoalFactor')),
        handicapSmoothingFactor: Number(this.formatModelFactorValue(this.modelFactors.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor'))
      }
    },
    buildUserConfigPayload() {
      return {
        modelFactors: this.buildModelFactorPayload(),
        globalParameters: this.buildGlobalParameterPayload(),
        selectedRows: this.normalizeSelectedRows(this.selectedRows)
      }
    },
    saveUserConfig() {
      this.persistUserConfig().catch(() => {
        // Local cookies still keep the UI usable if the config file write fails.
      })
    },
    async persistUserConfig() {
      const res = await fetch('/api/football/user-config', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.buildUserConfigPayload())
      })
      if (!res.ok) {
        throw new Error('保存全局参数失败')
      }
      return res.json()
    },
    saveModelFactorInput(key) {
      const fallback = this.getDefaultModelFactor(key)
      this.$set(this.modelFactors, key, this.formatModelFactorValue(this.modelFactors[key], fallback, key))
      this.saveModelFactors()
    },
    commitModelFactors() {
      MODEL_FACTOR_KEYS.forEach(key => {
        this.$set(this.modelFactors, key, this.formatModelFactorValue(this.modelFactors[key], this.getDefaultModelFactor(key), key))
      })
      this.saveModelFactors()
      if (this.queryDate) {
        this.loadPredictions()
      }
    },
    async toggleParameterPreset() {
      const targetPresetName = this.activeParameterPreset === 'aggressive' ? 'stable' : 'aggressive'
      const targetPreset = targetPresetName === 'aggressive' ? AGGRESSIVE_PARAMETER_PRESET : STABLE_PARAMETER_PRESET
      this.applyParameterPreset(targetPreset)
      this.activeParameterPreset = targetPresetName
      this.saveModelFactorsToCookie()
      this.saveGlobalParametersToCookie()
      this.saveUserConfig()
      if (this.queryDate) {
        await this.loadPredictions()
      }
    },
    applyParameterPreset(preset) {
      MODEL_FACTOR_KEYS.forEach(key => {
        this.$set(
          this.modelFactors,
          key,
          this.formatModelFactorValue(preset.modelFactors[key], this.getDefaultModelFactor(key), key)
        )
      })
      this.recommendationOdds = this.formatRecommendationOddsValue(preset.globalParameters.recommendationOdds)
      this.handicapRecommendationThreshold = this.formatRecommendationThresholdValue(
        preset.globalParameters.handicapRecommendationThreshold,
        DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD
      )
      this.handicapReverseThreshold = this.formatRecommendationThresholdValue(
        preset.globalParameters.handicapReverseThreshold,
        DEFAULT_HANDICAP_REVERSE_THRESHOLD
      )
      this.singleRecommendationThreshold = this.formatRecommendationThresholdValue(
        preset.globalParameters.singleRecommendationThreshold,
        DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
      )
    },
    syncActiveParameterPreset() {
      this.activeParameterPreset = this.matchesParameterPreset(AGGRESSIVE_PARAMETER_PRESET) ? 'aggressive' : 'stable'
    },
    matchesParameterPreset(preset) {
      const currentModelFactors = this.buildModelFactorPayload()
      const currentGlobalParameters = this.buildGlobalParameterPayload()
      return MODEL_FACTOR_KEYS.every(key => currentModelFactors[key] === preset.modelFactors[key]) &&
        Object.keys(preset.globalParameters).every(key => currentGlobalParameters[key] === preset.globalParameters[key])
    },
    getDefaultModelFactor(key) {
      if (key === 'homeTeamGoalFactor') {
        return DEFAULT_HOME_TEAM_GOAL_FACTOR
      }
      if (key === 'seedTeamGoalFactor') {
        return DEFAULT_SEED_TEAM_GOAL_FACTOR
      }
      if (key === 'handicapSmoothingFactor') {
        return DEFAULT_HANDICAP_SMOOTHING_FACTOR
      }
      return DEFAULT_HOST_TEAM_GOAL_FACTOR
    },
    getModelFactorMin(key) {
      return key === 'handicapSmoothingFactor' ? HANDICAP_SMOOTHING_MIN : MODEL_FACTOR_MIN
    },
    getModelFactorMax(key) {
      return key === 'handicapSmoothingFactor' ? HANDICAP_SMOOTHING_MAX : MODEL_FACTOR_MAX
    },
    getModelFactorScale(key) {
      return key === 'handicapSmoothingFactor' ? 3 : 2
    },
    normalizeModelFactor(value, fallback, key) {
      const numberValue = Number(value)
      if (!Number.isFinite(numberValue)) {
        return fallback
      }
      const clampedValue = Math.max(this.getModelFactorMin(key), Math.min(this.getModelFactorMax(key), numberValue))
      return Number(clampedValue.toFixed(this.getModelFactorScale(key)))
    },
    formatModelFactorValue(value, fallback, key) {
      return this.normalizeModelFactor(value, fallback, key).toFixed(this.getModelFactorScale(key))
    },
    hasSportterySelection(match) {
      return Boolean(match && match.sportteryMatchId)
    },
    getSportterySelection(match) {
      if (!this.hasSportterySelection(match)) {
        return null
      }
      const selection = {}
      if (match.sportteryNormalAvailable === true) {
        selection.normal = true
      }
      const handicap = Number(match.sportteryHandicap)
      if (Number.isInteger(handicap) && handicap !== 0) {
        selection.handicap = 'handicap-' + handicap
      }
      return selection
    },
    getSelectionForMatch(match) {
      const manualSelection = this.selectedRows[match.matchId]
      if (manualSelection && manualSelection.manualOverride === true) {
        return manualSelection
      }
      return this.getSportterySelection(match) || {}
    },
    hasManualOverride(match) {
      const selection = match ? this.selectedRows[match.matchId] : null
      return Boolean(selection && selection.manualOverride === true)
    },
    selectionInputTitle(match, item) {
      if (this.hasManualOverride(match)) {
        return '手工覆盖：' + item.label
      }
      if (!this.hasSportterySelection(match)) {
        return '手工选择：' + item.label
      }
      const matchNumber = match.sportteryMatchNumber ? '（' + match.sportteryMatchNumber + '）' : ''
      return '中国体彩网自动获取' + matchNumber + '，可手工覆盖：' + item.label
    },
    isRowSelected(match, item) {
      const selection = this.getSelectionForMatch(match)
      if (item.handicap === 0) {
        return selection.normal === true
      }
      return selection.handicap === item.key
    },
    toggleRecommendationRow(match, item, event) {
      const checked = event.target.checked
      const effectiveSelection = this.getSelectionForMatch(match)
      const selection = { manualOverride: true }
      if (effectiveSelection.normal === true) {
        selection.normal = true
      }
      if (effectiveSelection.handicap) {
        selection.handicap = effectiveSelection.handicap
      }
      if (item.handicap === 0) {
        if (checked) {
          selection.normal = true
        } else {
          delete selection.normal
        }
      } else if (checked) {
        selection.handicap = item.key
      } else if (selection.handicap === item.key) {
        delete selection.handicap
      }

      this.$set(this.selectedRows, match.matchId, selection)
      this.saveRecommendationSelections()
      if (this.backtestActive) {
        this.refreshBacktestResults()
      }
    },
    isRecommended(match, item, probabilityKey) {
      return this.getRecommendationKeys(match).has(this.getRecommendationCellKey(item, probabilityKey))
    },
    isWinningRecommendation(match, item, probabilityKey) {
      const score = this.parseScore(match)
      if (!score || !this.isRecommended(match, item, probabilityKey)) {
        return false
      }
      return this.getActualProbabilityKey(score, item.handicap) === probabilityKey
    },
    recommendationTotalProbability(match) {
      const recommendationKeys = this.getRecommendationKeys(match)
      if (recommendationKeys.size === 0) {
        return null
      }
      const total = this.probabilityRows(match).reduce((sum, item) => {
        return sum + PROBABILITY_KEYS.reduce((rowSum, probabilityKey) => {
          if (!recommendationKeys.has(this.getRecommendationCellKey(item, probabilityKey))) {
            return rowSum
          }
          return rowSum + (Number(item.probability[probabilityKey]) || 0)
        }, 0)
      }, 0)
      return Number(total.toFixed(1))
    },
    recommendationTotalProbabilityText(match) {
      const total = this.recommendationTotalProbability(match)
      if (total === null) {
        return '暂无推荐'
      }
      return total.toFixed(1) + '%'
    },
    recommendationResult(match) {
      const score = this.parseScore(match)
      if (!score) {
        return ''
      }
      const recommendationKeys = this.getRecommendationKeys(match)
      if (recommendationKeys.size === 0) {
        return ''
      }
      const hit = this.probabilityRows(match).some(item => {
        const actualKey = this.getActualProbabilityKey(score, item.handicap)
        return recommendationKeys.has(this.getRecommendationCellKey(item, actualKey))
      })
      return hit ? 'hit' : 'miss'
    },
    recommendationResultText(match) {
      return this.recommendationResult(match) === 'hit' ? '中奖' : '未中奖'
    },
    parseScore(match) {
      const scoreText = match && match.scoreText ? String(match.scoreText) : ''
      const scoreMatch = scoreText.match(/(\d+)\s*-\s*(\d+)/)
      if (!scoreMatch) {
        return null
      }
      return {
        home: Number(scoreMatch[1]),
        away: Number(scoreMatch[2])
      }
    },
    getActualProbabilityKey(score, handicap) {
      const adjustedHomeScore = score.home + handicap
      if (adjustedHomeScore > score.away) {
        return 'win'
      }
      if (adjustedHomeScore === score.away) {
        return 'draw'
      }
      return 'lose'
    },
    getRecommendationKeys(match) {
      const recommendationKeys = this.applySingleRecommendationThreshold(
        match,
        this.getBaseRecommendationKeys(match)
      )
      return this.hasQualifiedRecommendationOdds(match, recommendationKeys)
        ? recommendationKeys
        : new Set()
    },
    applySingleRecommendationThreshold(match, recommendationKeys) {
      if (!recommendationKeys || recommendationKeys.size !== 2) {
        return recommendationKeys
      }
      const threshold = this.normalizeRecommendationThreshold(
        this.singleRecommendationThreshold,
        DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD
      )
      let strongestRecommendation = null
      this.probabilityRows(match).forEach(item => {
        PROBABILITY_KEYS.forEach(probabilityKey => {
          const key = this.getRecommendationCellKey(item, probabilityKey)
          if (!recommendationKeys.has(key)) {
            return
          }
          const value = Number(item.probability[probabilityKey]) || 0
          if (!strongestRecommendation || value > strongestRecommendation.value) {
            strongestRecommendation = { key, value }
          }
        })
      })
      return strongestRecommendation && strongestRecommendation.value > threshold
        ? new Set([strongestRecommendation.key])
        : recommendationKeys
    },
    getBaseRecommendationKeys(match) {
      const selectedRows = this.probabilityRows(match).filter(item => this.isRowSelected(match, item))
      if (selectedRows.length === 0) {
        return new Set()
      }
      const normalRows = selectedRows.filter(item => item.handicap === 0)
      const handicapRows = selectedRows.filter(item => item.handicap !== 0)
      if (normalRows.length > 0 && handicapRows.length > 0) {
        const pairSwitchKeys = this.buildHandicapPairSwitchKeys(normalRows[0], handicapRows[0], selectedRows)
        if (pairSwitchKeys) {
          return pairSwitchKeys
        }
        return this.buildRecommendationKeys(selectedRows, true)
      }
      if (handicapRows.length > 0) {
        return this.buildRecommendationKeys(handicapRows, false)
      }
      return this.buildRecommendationKeys(normalRows, false)
    },
    hasQualifiedRecommendationOdds(match, recommendationKeys) {
      if (!recommendationKeys || recommendationKeys.size === 0) {
        return false
      }
      const threshold = this.normalizeRecommendationOdds(this.recommendationOdds)
      const recommendationOdds = []
      this.probabilityRows(match).forEach(item => {
        PROBABILITY_KEYS.forEach(probabilityKey => {
          if (!recommendationKeys.has(this.getRecommendationCellKey(item, probabilityKey))) {
            return
          }
          recommendationOdds.push(this.sportteryOddsValue(match, item, probabilityKey))
        })
      })
      return recommendationOdds.length === recommendationKeys.size &&
        recommendationOdds.every(odds => odds !== null && odds >= threshold)
    },
    getRecommendationOddsDetails(match) {
      const score = this.parseScore(match)
      const recommendationKeys = this.getRecommendationKeys(match)
      if (!score || recommendationKeys.size === 0) {
        return []
      }
      return this.probabilityRows(match).reduce((result, item) => {
        const actualProbabilityKey = this.getActualProbabilityKey(score, item.handicap)
        PROBABILITY_KEYS.forEach(probabilityKey => {
          if (!recommendationKeys.has(this.getRecommendationCellKey(item, probabilityKey))) {
            return
          }
          const odds = this.sportteryOddsValue(match, item, probabilityKey)
          if (odds !== null) {
            result.push({
              odds,
              winning: probabilityKey === actualProbabilityKey
            })
          }
        })
        return result
      }, [])
    },
    buildRecommendationKeys(rows, applyHandicapThreshold) {
      const maxCell = this.findMaxProbabilityCell(rows)
      if (!maxCell) {
        return new Set()
      }
      const reverseThreshold = this.normalizeRecommendationThreshold(
        this.handicapReverseThreshold,
        DEFAULT_HANDICAP_REVERSE_THRESHOLD
      )
      if (applyHandicapThreshold && maxCell.row.handicap !== 0 && maxCell.probabilityKey !== 'draw' && maxCell.value < reverseThreshold) {
        return new Set(
          PROBABILITY_KEYS
            .filter(key => key !== maxCell.probabilityKey)
            .map(key => this.getRecommendationCellKey(maxCell.row, key))
        )
      }
      return new Set(
        this.getAdjacentProbabilityKeys(maxCell.probabilityKey)
          .map(key => this.getRecommendationCellKey(maxCell.row, key))
      )
    },
    buildHandicapPairSwitchKeys(normalRow, handicapRow, rows) {
      const maxCell = this.findMaxProbabilityCell(rows)
      if (!maxCell || maxCell.row !== normalRow) {
        return null
      }
      if (maxCell.probabilityKey !== 'win' && maxCell.probabilityKey !== 'lose') {
        return null
      }

      const handicapValue = Number(handicapRow.probability[maxCell.probabilityKey]) || 0
      const recommendationThreshold = this.normalizeRecommendationThreshold(
        this.handicapRecommendationThreshold,
        DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD
      )
      if (handicapValue >= recommendationThreshold && handicapValue < maxCell.value) {
        return new Set(
          [maxCell.probabilityKey, 'draw']
            .map(key => this.getRecommendationCellKey(handicapRow, key))
        )
      }
      return null
    },
    findMaxProbabilityCell(rows) {
      let maxCell = null
      rows.forEach(row => {
        PROBABILITY_KEYS.forEach(probabilityKey => {
          const value = Number(row.probability[probabilityKey]) || 0
          if (!maxCell || value > maxCell.value) {
            maxCell = { row, probabilityKey, value }
          }
        })
      })
      return maxCell
    },
    getAdjacentProbabilityKeys(probabilityKey) {
      if (probabilityKey === 'draw') {
        return ['draw']
      }
      const index = PROBABILITY_KEYS.indexOf(probabilityKey)
      if (index < 0) {
        return []
      }
      return PROBABILITY_KEYS.filter((key, keyIndex) => Math.abs(keyIndex - index) <= 1)
    },
    getRecommendationCellKey(item, probabilityKey) {
      return item.key + '-' + probabilityKey
    },
    activeExpectedHomeGoals(match) {
      return this.modelMode === 'after' && match.adjustedExpectedHomeGoals != null
        ? match.adjustedExpectedHomeGoals
        : match.expectedHomeGoals
    },
    activeExpectedAwayGoals(match) {
      return this.modelMode === 'after' && match.adjustedExpectedAwayGoals != null
        ? match.adjustedExpectedAwayGoals
        : match.expectedAwayGoals
    },
    activeNormalProbability(match) {
      return this.modelMode === 'after' && match.adjustedNormalProbability
        ? match.adjustedNormalProbability
        : match.normalProbability
    },
    activeHandicapProbabilities(match) {
      return this.modelMode === 'after' && match.adjustedHandicapProbabilities
        ? match.adjustedHandicapProbabilities
        : match.handicapProbabilities
    },
    activeScoreProbabilities(match) {
      const scores = this.modelMode === 'after' && match.adjustedScoreProbabilities
        ? match.adjustedScoreProbabilities
        : match.scoreProbabilities
      return (scores || []).slice(0, 3)
    },
    activeTotalGoalsProbabilities(match) {
      const probabilities = this.modelMode === 'after' && match.adjustedTotalGoalsProbabilities
        ? match.adjustedTotalGoalsProbabilities
        : match.totalGoalsProbabilities
      return (probabilities || []).slice(0, 3)
    },
    isWinningScorePrediction(match, score) {
      if (!match || match.status !== '已完赛') {
        return false
      }
      const actualScore = this.parseScore(match)
      return actualScore !== null &&
        actualScore.home === Number(score.homeScore) &&
        actualScore.away === Number(score.awayScore)
    },
    isWinningTotalGoalsPrediction(match, item) {
      if (!match || match.status !== '已完赛') {
        return false
      }
      const actualScore = this.parseScore(match)
      return actualScore !== null &&
        actualScore.home + actualScore.away === Number(item.totalGoals)
    },
    formatProbability(value) {
      const numberValue = Number(value)
      if (!Number.isFinite(numberValue)) {
        return '0.0%'
      }
      return numberValue.toFixed(1) + '%'
    },
    scoreProbabilityTitle(match) {
      return this.activeScoreProbabilities(match)
        .map(score => score.homeScore + '-' + score.awayScore + ' ' + this.formatProbability(score.probability))
        .join('，')
    },
    totalGoalsProbabilityTitle(match) {
      return this.activeTotalGoalsProbabilities(match)
        .map(item => item.totalGoals + '球 ' + this.formatProbability(item.probability))
        .join('，')
    },
    formatHandicap(value) {
      return value > 0 ? '+' + value : String(value)
    },
    hasSportteryOdds(match) {
      if (!match) {
        return false
      }
      return [match.sportteryNormalOdds, match.sportteryHandicapOdds].some(odds => {
        return odds && PROBABILITY_KEYS.some(key => {
          const value = Number(odds[key])
          return Number.isFinite(value) && value > 0
        })
      })
    },
    sportteryOddsForRow(match, item) {
      if (!match || !item) {
        return null
      }
      if (item.handicap === 0) {
        return match.sportteryNormalOdds || null
      }
      return Number(match.sportteryHandicap) === Number(item.handicap)
        ? match.sportteryHandicapOdds || null
        : null
    },
    sportteryOddsValue(match, item, probabilityKey) {
      const odds = this.sportteryOddsForRow(match, item)
      const value = odds ? Number(odds[probabilityKey]) : NaN
      return Number.isFinite(value) && value > 0 ? value : null
    },
    sportteryOddsTitle(match, item) {
      const odds = this.sportteryOddsForRow(match, item)
      const marketName = item.handicap === 0 ? '不让球' : '让球' + this.formatHandicap(item.handicap)
      const updatedAt = odds && odds.updatedAt ? '，更新时间：' + odds.updatedAt : ''
      return '中国体彩网' + marketName + '最新赔率' + updatedAt
    },
    formatSportteryOdds(value) {
      const odds = Number(value)
      return Number.isFinite(odds) && odds > 0 ? odds.toFixed(2) : '--'
    },
    probabilityRows(match) {
      const handicapRows = (this.activeHandicapProbabilities(match) || []).map(item => ({
        key: 'handicap-' + item.handicap,
        handicap: item.handicap,
        label: item.handicapName + '（' + this.formatHandicap(item.handicap) + '）',
        probability: item.probability,
        rowClass: 'handicap-row'
      }))
      const rows = handicapRows.concat({
        key: 'normal',
        handicap: 0,
        label: '不让球（0）',
        probability: this.activeNormalProbability(match) || { win: 0, draw: 0, lose: 0 },
        rowClass: 'normal-row'
      })
      return rows.sort((left, right) => right.handicap - left.handicap)
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", Arial, sans-serif;
  color: #1f2937;
  background: #f3f6fb;
  overflow-x: hidden;
}

.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 16px 18px;
}

.hero {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 16px;
  padding: 12px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a, #1d4ed8);
  color: #ffffff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.2);
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.78;
}

h1 {
  margin: 0;
  font-size: 25px;
  line-height: 1.15;
}

.hero-card-group {
  display: flex;
  align-self: stretch;
  align-items: stretch;
  gap: 14px;
  min-width: 0;
  margin-left: auto;
}

.competition-select {
  position: relative;
  z-index: 30;
  width: 210px;
  max-width: 100%;
  margin-top: 16px;
}

.competition-select-trigger {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 34px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-radius: 9px;
  color: rgba(255, 255, 255, 0.78);
  background: rgba(15, 23, 42, 0.2);
  cursor: pointer;
  font-size: 12px;
  text-align: left;
}

.competition-select-trigger strong {
  overflow: hidden;
  color: #ffffff;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.competition-select-trigger:disabled {
  cursor: wait;
  opacity: 0.7;
}

.competition-select-arrow {
  display: block;
  width: 7px;
  height: 7px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg);
  transition: transform 0.16s ease;
}

.competition-select-arrow.is-open {
  transform: rotate(225deg);
}

.competition-select-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 100;
  width: 100%;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  border-radius: 11px;
  color: #0f172a;
  background: #ffffff;
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.24);
}

.competition-search {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 7px;
  margin: 9px;
  padding: 0 9px;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  color: #64748b;
  background: #f8fafc;
}

.competition-search input {
  min-width: 0;
  height: 30px;
  padding: 0;
  border: 0;
  outline: 0;
  color: #0f172a;
  background: transparent;
  font-size: 12px;
}

.competition-option-list {
  max-height: 270px;
  overflow-y: auto;
  padding: 0 6px 6px;
}

.competition-option {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 32px;
  padding: 4px 7px;
  border: 0;
  border-radius: 7px;
  color: #0f172a;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  text-align: left;
}

.competition-option:hover,
.competition-option[aria-selected="true"] {
  background: #eff6ff;
}

.competition-checkbox {
  display: grid;
  place-items: center;
  width: 16px;
  height: 16px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  color: #ffffff;
  font-size: 11px;
  font-weight: 900;
}

.competition-checkbox.is-checked {
  border-color: #2563eb;
  background: #2563eb;
}

.competition-option-empty {
  padding: 18px 8px;
  color: #94a3b8;
  font-size: 12px;
  text-align: center;
}

.competition-select-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-top: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 11px;
  background: #f8fafc;
}

.competition-select-footer button {
  height: 26px;
  padding: 0 14px;
  border: 0;
  border-radius: 6px;
  color: #ffffff;
  background: #2563eb;
  cursor: pointer;
  font-size: 11px;
  font-weight: 800;
}

.competition-select-footer button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.hero-card {
  align-self: stretch;
  display: grid;
  grid-template-columns: 230px 504px;
  align-items: end;
  gap: 14px;
  justify-content: center;
  width: 774px;
  min-width: 0;
  max-width: 100%;
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  text-align: center;
}

.hero-summary-column {
  min-width: 0;
}

.hero-number {
  font-size: 24px;
  line-height: 1;
  font-weight: 800;
}

.hero-label {
  margin-top: 4px;
  font-size: 14px;
}

.hero-small {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.78;
}

.model-toggle {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  margin-top: 7px;
  padding: 3px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.22);
}

.model-toggle button {
  height: 24px;
  border: 0;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.78);
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.model-toggle button.is-active {
  color: #1d4ed8;
  background: #ffffff;
}

.hero-actions {
  display: grid;
  margin-top: 7px;
  margin-bottom: 3px;
}

.refresh-data-button {
  white-space: nowrap;
}

.factor-controls {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 29px;
  min-width: 0;
  padding-left: 14px;
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  text-align: left;
}

.factor-controls::after {
  position: absolute;
  top: 0;
  bottom: 0;
  left: calc(50% + 7px);
  width: 1px;
  background: rgba(255, 255, 255, 0.2);
  content: '';
  pointer-events: none;
}

.factor-control-column {
  display: grid;
  align-content: end;
  gap: 8px;
  min-width: 0;
  padding: 0 12px;
}

.factor-control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 78px;
  gap: 5px 8px;
  align-items: center;
}

.factor-control span {
  min-width: 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
}

.factor-control input[type="number"] {
  width: 78px;
  height: 26px;
  padding: 0 6px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 6px;
  color: #0f172a;
  background: #ffffff;
  font-size: 12px;
  font-weight: 800;
  text-align: right;
  outline: none;
}

.percentage-factor-control {
  grid-template-columns: minmax(0, 1fr) 96px;
}

.factor-control .percentage-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 4px;
  align-items: center;
  min-width: 0;
}

.percentage-input input[type="number"] {
  width: 100%;
  min-width: 0;
}

.factor-control .percentage-suffix {
  font-size: 12px;
  font-weight: 700;
}

.factor-control input[type="number"]:focus {
  border-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.24);
}

.factor-control.is-competition-disabled span {
  color: rgba(255, 255, 255, 0.48);
}

.factor-control.is-competition-disabled input[type="number"] {
  cursor: not-allowed;
  border-color: rgba(203, 213, 225, 0.58);
  color: #94a3b8;
  background: #e2e8f0;
  opacity: 1;
}

.factor-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 29px;
  grid-column: 1 / -1;
  margin-bottom: 3px;
}

.global-parameter-card {
  flex: 0 0 230px;
  align-self: stretch;
  display: grid;
  grid-template-rows: 1fr auto auto;
  gap: 8px;
  width: 230px;
  min-width: 230px;
  padding: 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  text-align: left;
}

.global-parameter-control {
  grid-template-columns: minmax(0, 1fr) 70px;
}

.global-parameter-control input[type="number"] {
  width: 70px;
}

.backtest-odds-button {
  align-self: end;
  margin-bottom: 3px;
}

.backtest-result {
  align-self: end;
  display: grid;
  gap: 6px;
  text-align: center;
}

.backtest-average-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
}

.backtest-average-grid > div {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 4px;
  align-items: center;
  min-width: 0;
  height: 30px;
  padding: 2px 8px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.08);
}

.backtest-result strong {
  grid-column: 2;
  grid-row: 1;
  color: #ffffff;
  font-size: 14px;
  line-height: 1;
  text-align: right;
}

.backtest-result span {
  grid-column: 1;
  grid-row: 1;
  color: rgba(255, 255, 255, 0.88);
  font-size: 10px;
  font-weight: 800;
  line-height: 1.1;
  text-align: left;
}

.backtest-result.is-empty strong {
  color: rgba(255, 255, 255, 0.58);
}

.factor-recalculate {
  width: 100%;
  height: 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
  border: 0;
  color: #1d4ed8;
  background: #ffffff;
}

.factor-recalculate:hover:not(:disabled) {
  background: #eff6ff;
}

.factor-control input:disabled,
.factor-recalculate:disabled,
.factor-reset:disabled,
.refresh-data-button:disabled {
  cursor: wait;
  opacity: 0.68;
}

.calendar-panel {
  margin-top: 14px;
  padding: 12px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.07);
}

.calendar-header {
  display: grid;
  grid-template-columns: 36px 1fr 36px;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.calendar-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.2;
  text-align: center;
}

.calendar-nav {
  width: 36px;
  height: 32px;
  border: 1px solid #d7deea;
  border-radius: 8px;
  color: #1d4ed8;
  background: #f8fafc;
  cursor: pointer;
  font-size: 18px;
  font-weight: 900;
  line-height: 1;
}

.calendar-nav:hover {
  border-color: #2563eb;
  background: #eff6ff;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  text-align: center;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
}

.calendar-day {
  height: 36px;
  border: 1px solid #d7deea;
  border-radius: 8px;
  color: #1f2937;
  background: #f8fafc;
  cursor: pointer;
  font-size: 14px;
  font-weight: 800;
  outline: none;
  transition: background 0.16s ease, border-color 0.16s ease, color 0.16s ease, transform 0.16s ease;
}

.calendar-day:not(:disabled):hover {
  border-color: #2563eb;
  color: #1d4ed8;
  background: #eff6ff;
  transform: translateY(-1px);
}

.calendar-day.is-selected {
  border-color: #2563eb;
  color: #ffffff;
  background: #2563eb;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.22);
}

.calendar-day.has-schedule:not(.is-selected) {
  border-color: #93c5fd;
  background: #eff6ff;
}

.calendar-day.is-empty {
  visibility: hidden;
  pointer-events: none;
}

.calendar-day:disabled:not(.is-empty) {
  cursor: wait;
  opacity: 0.72;
}

.error-box,
.empty-box {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff7ed;
  color: #c2410c;
}

.match-list {
  display: grid;
  flex: 1 1 auto;
  grid-template-columns: repeat(var(--match-columns), minmax(0, 1fr));
  grid-auto-rows: minmax(0, 1fr);
  align-items: stretch;
  gap: 12px;
  min-height: 0;
  margin-top: 14px;
}

.match-list.is-backtest {
  flex: 0 0 auto;
  grid-auto-rows: auto;
}

.match-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 14px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.07);
}

.match-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.match-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.match-time {
  color: #64748b;
  font-size: 12px;
}

.match-card h2 {
  margin: 0;
  font-size: 16px;
  line-height: 1.25;
}

.match-card h2 span {
  color: #94a3b8;
  font-size: 12px;
}

.match-title-button {
  appearance: none;
  margin: 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
  font-weight: inherit;
  line-height: inherit;
  text-align: left;
}

.match-title-button:hover {
  color: #1d4ed8;
}

.match-title-button:focus-visible {
  border-radius: 3px;
  outline: 2px solid #60a5fa;
  outline-offset: 2px;
}

.match-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.35;
}

.match-score-line {
  display: flex;
  align-items: center;
  gap: 6px;
  max-height: 18px;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
}

.match-score-text {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
}

.score-prob-row {
  display: flex;
  align-items: center;
  gap: 5px;
  flex: 1 1 auto;
  min-width: 0;
  max-width: 100%;
  max-height: 18px;
  overflow: hidden;
  color: #64748b;
  font-size: 11px;
  line-height: 16px;
  white-space: nowrap;
}

.score-prob-label {
  flex: 0 0 auto;
  color: #475569;
  font-weight: 800;
}

.score-prob-items {
  display: flex;
  gap: 4px;
  min-width: 0;
  overflow: hidden;
}

.score-prob-pill {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  color: #334155;
  background: #f1f5f9;
  font-weight: 800;
}

.score-prob-pill span {
  color: #64748b;
  font-weight: 700;
}

.score-prob-pill.is-winning-prediction {
  color: #1e40af;
  background: #dbeafe;
}

.result-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 6px;
  min-width: 42px;
  height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}

.result-badge.is-hit {
  color: #166534;
  background: #dcfce7;
}

.result-badge.is-miss {
  color: #991b1b;
  background: #fee2e2;
}

.goal-box {
  display: flex;
  flex: 0 0 auto;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 78px;
  min-height: 44px;
  padding: 2px 6px;
  border: 0;
  border-radius: 6px;
  background: #eff6ff;
  color: #1d4ed8;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 10px;
  line-height: 1;
  text-align: center;
}

.goal-box strong {
  display: block;
  margin: 0;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}

.match-summary-row {
  display: grid;
  grid-template-columns: minmax(0, auto) minmax(0, 1fr);
  align-items: end;
  gap: 8px 12px;
  margin-top: -22px;
  margin-bottom: 4px;
  min-height: 50px;
}

.summary-title-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
  white-space: nowrap;
}

.handicap-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
}

.recommend-total {
  flex: 0 0 auto;
  display: flex;
  align-items: baseline;
  gap: 4px;
  min-width: 0;
  color: #0f172a;
  font-size: 10px;
  line-height: 1;
  white-space: nowrap;
}

.recommend-total span {
  color: #64748b;
  font-weight: 700;
}

.recommend-total strong {
  color: #64748b;
  font-size: 11px;
  line-height: 1;
}

.recommend-total.is-empty strong {
  color: #64748b;
  font-size: 10px;
}

.score-prob-summary {
  justify-content: flex-end;
  margin-left: auto;
  max-width: 100%;
}

.prediction-prob-stack {
  display: grid;
  gap: 1px;
  min-width: 0;
  max-height: 50px;
  overflow: hidden;
}

.handicap-table-wrap {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  container-type: inline-size;
  overflow-x: auto;
}

.handicap-table {
  width: 100%;
  height: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.handicap-table th,
.handicap-table td {
  padding: 6px 8px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}

.handicap-col,
.handicap-cell {
  width: auto;
  min-width: 150px;
  white-space: nowrap;
}

.select-col,
.select-cell {
  width: 34px;
  padding-left: 5px;
  padding-right: 5px;
  text-align: center;
}

.select-cell input {
  width: 16px;
  height: 16px;
  margin: 0;
  accent-color: #2563eb;
  cursor: pointer;
}

.handicap-table th {
  color: #64748b;
  background: #f8fafc;
  text-align: left;
}

.handicap-table td:nth-child(3) {
  color: #16a34a;
  font-weight: 700;
}

.handicap-table td:nth-child(4) {
  color: #d97706;
  font-weight: 700;
}

.handicap-table td:nth-child(5) {
  color: #dc2626;
  font-weight: 700;
}

.probability-value,
.sporttery-odds {
  white-space: nowrap;
}

.probability-cell {
  text-align: left;
}

.probability-cell-content {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 4px;
  width: 100%;
  min-height: 18px;
}

.probability-value {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}

.sporttery-odds {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 18px;
  padding: 0 4px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  color: #475569;
  background: #e2e8f0;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
}

.recommend-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  color: #ffffff;
  background: #ef4444;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
}

.hit-cell-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  color: #ffffff;
  background: #16a34a;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
}

.normal-row td {
  background: #eff6ff;
}

.normal-row .handicap-cell {
  color: #1d4ed8;
  font-weight: 800;
}

body.dialog-open {
  overflow: hidden;
}

.recommendation-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 900;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.38);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;
}

.recommendation-fab:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(37, 99, 235, 0.46);
}

.recommendation-fab:focus-visible {
  outline: 3px solid rgba(147, 197, 253, 0.9);
  outline-offset: 3px;
}

.recommendation-fab:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.recommendation-fab svg {
  width: 21px;
  height: 21px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 2;
}

.recommendation-fab-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 19px;
  height: 19px;
  padding: 0 5px;
  border: 2px solid #ffffff;
  border-radius: 999px;
  color: #ffffff;
  background: #ef4444;
  font-size: 9px;
  font-weight: 800;
  line-height: 1;
}

.dialog-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(3px);
}

.backtest-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.32);
  backdrop-filter: blur(3px);
}

.backtest-mask-card {
  display: grid;
  gap: 12px;
  width: min(360px, calc(100vw - 48px));
  padding: 24px;
  border: 1px solid #bfdbfe;
  border-radius: 14px;
  color: #0f172a;
  background: linear-gradient(145deg, #ffffff, #eff6ff);
  box-shadow: 0 22px 52px rgba(30, 64, 175, 0.2);
  text-align: left;
}

.backtest-mask-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.backtest-mask-heading strong {
  font-size: 20px;
  line-height: 1.2;
}

.backtest-mask-heading span {
  color: #2563eb;
  font-size: 18px;
  font-weight: 800;
  line-height: 1;
}

.backtest-mask-card > span {
  color: #64748b;
  font-size: 13px;
}

.backtest-progress {
  position: relative;
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: #dbeafe;
}

.backtest-progress-bar {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  transition: width 0.28s ease;
}

.head-to-head-dialog {
  display: flex;
  flex-direction: column;
  width: min(680px, calc(100vw - 48px));
  max-height: min(720px, calc(100vh - 48px));
  overflow: hidden;
  border: 1px solid #dbe4f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);
}

.recommendation-dialog {
  display: flex;
  flex-direction: column;
  width: min(880px, calc(100vw - 48px));
  max-height: min(760px, calc(100vh - 48px));
  overflow: hidden;
  border: 1px solid #dbe4f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);
}

.head-to-head-dialog:focus,
.recommendation-dialog:focus {
  outline: none;
}

.head-to-head-dialog-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 22px 10px;
}

.head-to-head-dialog h3,
.recommendation-dialog h3 {
  margin: 0;
  color: #0f172a;
  font-size: 21px;
  line-height: 1.25;
}

.dialog-close {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  border: 1px solid #d7deea;
  border-radius: 9px;
  color: #475569;
  background: #f8fafc;
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
}

.dialog-close:hover {
  border-color: #93c5fd;
  color: #1d4ed8;
  background: #eff6ff;
}

.head-to-head-description {
  margin: 0;
  padding: 0 22px 15px;
  border-bottom: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 12px;
}

.dialog-state {
  min-height: 180px;
  padding: 72px 24px;
  color: #64748b;
  text-align: center;
}

.dialog-state.is-error {
  color: #b91c1c;
}

.recommendation-table-wrap {
  padding: 16px 22px 22px;
  overflow: auto;
}

.recommendation-table {
  width: 100%;
  min-width: 760px;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 13px;
}

.recommendation-table th,
.recommendation-table td {
  padding: 11px 12px;
  border-bottom: 1px solid #e2e8f0;
  color: #334155;
  text-align: left;
  vertical-align: middle;
  white-space: nowrap;
}

.recommendation-table th {
  position: sticky;
  top: 0;
  z-index: 1;
  color: #64748b;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 800;
}

.recommendation-table tbody tr:hover td {
  background: #f8fbff;
}

.recommendation-time-cell strong,
.recommendation-time-cell span {
  display: block;
}

.recommendation-time-cell strong {
  color: #0f172a;
}

.recommendation-time-cell span {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 10px;
}

.recommendation-fixture-cell {
  min-width: 230px;
}

.recommendation-fixture-cell strong {
  color: #0f172a;
}

.recommendation-fixture-cell span {
  margin: 0 6px;
  color: #94a3b8;
  font-size: 11px;
}

.recommendation-table td.recommendation-items-cell {
  min-width: 370px;
  white-space: normal;
}

.recommendation-item-list {
  display: flex;
  flex-wrap: nowrap;
  gap: 7px;
}

.recommendation-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 7px;
  border: 1px solid #dbe4f0;
  border-radius: 8px;
  background: #f8fafc;
  white-space: nowrap;
}

.recommendation-market-name {
  color: #475569;
  font-size: 11px;
  font-weight: 700;
}

.recommendation-item-probability {
  color: #334155;
  font-weight: 800;
}

.recommendation-item-odds {
  padding: 2px 4px;
  border-radius: 4px;
  color: #475569;
  background: #e2e8f0;
  font-size: 10px;
  font-weight: 800;
}

.recommendation-result-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 22px;
  border-radius: 6px;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
}

.recommendation-result-pill.is-win {
  background: #16a34a;
}

.recommendation-result-pill.is-draw {
  background: #d97706;
}

.recommendation-result-pill.is-lose {
  background: #dc2626;
}

.head-to-head-list {
  display: grid;
  gap: 10px;
  padding: 16px 22px 22px;
  overflow-y: auto;
}

.head-to-head-item {
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
}

.head-to-head-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  margin-bottom: 9px;
  color: #64748b;
  font-size: 11px;
}

.head-to-head-meta span:nth-child(2) {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.neutral-badge {
  flex: 0 0 auto;
  padding: 2px 5px;
  border-radius: 4px;
  color: #475569;
  background: #e2e8f0;
  font-size: 10px;
  font-weight: 700;
}

.head-to-head-score {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 14px;
}

.head-to-head-score strong {
  min-width: 66px;
  color: #1d4ed8;
  font-size: 19px;
  text-align: center;
}

.head-to-head-team {
  min-width: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.head-to-head-team.is-home {
  text-align: right;
}

.head-to-head-team.is-away {
  text-align: left;
}

@media (max-width: 1450px) {
  .hero {
    align-items: stretch;
    flex-direction: column;
  }

  .hero-card-group {
    width: 100%;
    margin-left: 0;
  }

  .hero-card {
    flex: 1 1 auto;
    width: auto;
  }
}

@media (max-width: 980px) {
  .page {
    overflow-y: auto;
  }

  .hero-card {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: stretch;
  }

  .hero-card-group {
    flex-direction: column;
  }

  .global-parameter-card {
    flex-basis: auto;
    width: 100%;
    min-width: 0;
  }

  .match-list {
    flex: 0 0 auto;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    grid-auto-rows: auto;
  }
}

@media (max-width: 760px) {
  .page {
    padding: 16px;
  }

  .hero,
  .match-head {
    flex-direction: column;
  }

  .hero {
    width: 100%;
    min-width: 0;
    overflow: hidden;
  }

  .hero-card {
    display: block;
    width: 100%;
    min-width: 0;
    overflow: hidden;
  }

  .factor-controls {
    grid-template-columns: minmax(0, 1fr);
    margin-top: 10px;
    padding-left: 0;
    padding-top: 10px;
    border-left: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
  }

  .factor-controls::after {
    display: none;
  }

  .factor-control-column {
    padding: 0;
  }

  .factor-control {
    grid-template-columns: minmax(0, 1fr);
    gap: 4px;
  }

  .factor-control span {
    white-space: normal;
  }

  .factor-control input[type="number"] {
    width: 100%;
  }

  .factor-actions {
    grid-column: auto;
    grid-template-columns: minmax(0, 1fr);
    gap: 8px;
  }

  .percentage-input {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .global-parameter-control {
    grid-template-columns: minmax(0, 1fr);
  }

  .global-parameter-control input[type="number"] {
    width: 100%;
  }

  .calendar-panel {
    padding: 10px;
  }

  .calendar-header {
    grid-template-columns: 34px 1fr 34px;
  }

  .calendar-nav {
    width: 34px;
    height: 30px;
  }

  .calendar-weekdays,
  .calendar-grid {
    gap: 6px;
  }

  .calendar-day {
    height: 32px;
    font-size: 13px;
  }

  .match-list {
    grid-template-columns: 1fr;
  }

  .dialog-backdrop {
    align-items: flex-end;
    padding: 12px;
  }

  .head-to-head-dialog {
    width: 100%;
    max-height: calc(100vh - 24px);
    border-radius: 14px;
  }

  .recommendation-dialog {
    width: 100%;
    max-height: calc(100vh - 24px);
    border-radius: 14px;
  }

  .recommendation-fab {
    right: 18px;
    bottom: 18px;
    width: 42px;
    height: 42px;
  }

  .head-to-head-dialog-header {
    padding: 17px 17px 9px;
  }

  .head-to-head-description {
    padding: 0 17px 13px;
  }

  .head-to-head-list {
    padding: 13px 15px 17px;
  }

  .recommendation-table-wrap {
    padding: 13px 15px 17px;
  }

  .head-to-head-score {
    gap: 8px;
  }

  .head-to-head-score strong {
    min-width: 56px;
    font-size: 17px;
  }
}
</style>
