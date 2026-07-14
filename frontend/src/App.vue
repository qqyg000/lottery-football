<template>
  <div class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Monte Carlo + Poisson</p>
        <h1>彩票预测-竞彩足球</h1>
        <nav class="competition-tabs" aria-label="赛事切换">
          <button
            v-for="competition in competitions"
            :key="competition.code"
            type="button"
            :class="{ 'is-active': activeCompetition === competition.code }"
            :disabled="loading || updatingData"
            @click="switchCompetition(competition.code)"
          >
            {{ competition.name }}
          </button>
        </nav>
      </div>
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
            <button type="button" class="factor-recalculate factor-reset refresh-data-button" :disabled="loading || updatingData" @click="refreshData">
              {{ updatingData ? '更新中' : '更新数据' }}
            </button>
          </div>
        </div>
        <div class="weight-controls" aria-label="权重参数">
          <label class="factor-control">
            <span>基础权重</span>
            <input
              type="number"
              min="0"
              max="5"
              step="0.01"
              v-model.number="modelFactors.baseMatchWeight"
              :disabled="loading || updatingData"
              @change="saveModelFactorInput('baseMatchWeight')"
              @keyup.enter="commitModelFactors"
            >
          </label>
          <label class="factor-control">
            <span>近半年加成</span>
            <input
              type="number"
              min="0"
              max="3"
              step="0.01"
              v-model.number="modelFactors.recentHalfYearBonus"
              :disabled="loading || updatingData"
              @change="saveModelFactorInput('recentHalfYearBonus')"
              @keyup.enter="commitModelFactors"
            >
          </label>
          <label
            class="factor-control"
            :class="{ 'is-competition-disabled': activeCompetition !== 'WORLD_CUP' }"
            :title="activeCompetition !== 'WORLD_CUP' ? '仅世界杯赛事可用' : ''"
          >
            <span>世界杯加成</span>
            <input
              type="number"
              min="0"
              max="3"
              step="0.01"
              v-model.number="modelFactors.worldCupBonus"
              :disabled="loading || updatingData || activeCompetition !== 'WORLD_CUP'"
              @change="saveModelFactorInput('worldCupBonus')"
              @keyup.enter="commitModelFactors"
            >
          </label>
          <div class="factor-actions">
            <button type="button" class="factor-recalculate factor-reset" :disabled="loading || updatingData" @click="resetModelFactors">
              重置
            </button>
          </div>
        </div>
        <div class="factor-controls" aria-label="模型参数">
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
              :disabled="loading || updatingData || activeCompetition !== 'WORLD_CUP'"
              @change="saveModelFactorInput('seedTeamGoalFactor')"
              @keyup.enter="commitModelFactors"
            >
          </label>
          <label
            class="factor-control"
            :class="{ 'is-competition-disabled': activeCompetition !== 'WORLD_CUP' }"
            :title="activeCompetition !== 'WORLD_CUP' ? '仅世界杯赛事可用' : ''"
          >
            <span>东道主进球系数</span>
            <input
              type="number"
              min="0.1"
              max="3"
              step="0.01"
              v-model.number="modelFactors.hostTeamGoalFactor"
              :disabled="loading || updatingData || activeCompetition !== 'WORLD_CUP'"
              @change="saveModelFactorInput('hostTeamGoalFactor')"
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
              :disabled="loading || updatingData"
              @change="saveModelFactorInput('handicapSmoothingFactor')"
              @keyup.enter="commitModelFactors"
            >
          </label>
          <div class="factor-actions">
            <button type="button" class="factor-recalculate" :disabled="loading || updatingData || !queryDate" @click="commitModelFactors">
              {{ loading ? '计算中' : '重新计算' }}
            </button>
          </div>
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
          :disabled="cell.empty || loading || updatingData"
          @click="selectDate(cell.date)"
        >
          <span class="calendar-day-number">{{ cell.day }}</span>
        </button>
      </div>
    </section>

    <section v-if="errorMessage" class="error-box">{{ errorMessage }}</section>

    <section v-if="!loading && matches.length === 0" class="empty-box">
      当前日期暂无赛程，请切换日期后查询
    </section>

    <section class="match-list" :style="{ '--match-columns': matchColumns }">
      <article v-for="match in matches" :key="match.matchId" class="match-card">
        <div class="match-head">
          <div class="match-info">
            <div class="match-time">{{ match.matchDate }} {{ match.kickoffTime }} · {{ match.groupName }}</div>
            <h2>{{ match.homeTeamCn }} <span>vs</span> {{ match.awayTeamCn }}</h2>
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
            <div v-if="activeHalfFullProbabilities(match).length" class="score-prob-row score-prob-summary" :title="halfFullProbabilityTitle(match)">
              <span class="score-prob-label">半全场</span>
              <span class="score-prob-items">
                <span
                  v-for="item in activeHalfFullProbabilities(match)"
                  :key="match.matchId + '-half-full-' + item.halfTimeResult + '-' + item.fullTimeResult"
                  class="score-prob-pill half-full-prob-pill"
                  :class="{ 'is-winning-prediction': isWinningHalfFullPrediction(match, item) }"
                >
                  {{ item.label }}
                  <span>{{ formatProbability(item.probability) }}</span>
                </span>
              </span>
            </div>
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
          </div>
        </div>
        <div class="handicap-table-wrap">
          <table class="handicap-table">
            <thead>
            <tr>
              <th class="select-col">选</th>
              <th>盘口</th>
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
                  :aria-label="'选择' + item.label"
                  @change="toggleRecommendationRow(match, item, $event)"
                >
              </td>
              <td class="handicap-cell" :title="item.label">
                {{ item.label }}
              </td>
              <td>
                {{ formatProbability(item.probability.win) }}
                <span v-if="isRecommended(match, item, 'win')" class="recommend-badge">荐</span>
                <span v-if="isWinningRecommendation(match, item, 'win')" class="hit-cell-badge">中</span>
              </td>
              <td>
                {{ formatProbability(item.probability.draw) }}
                <span v-if="isRecommended(match, item, 'draw')" class="recommend-badge">荐</span>
                <span v-if="isWinningRecommendation(match, item, 'draw')" class="hit-cell-badge">中</span>
              </td>
              <td>
                {{ formatProbability(item.probability.lose) }}
                <span v-if="isRecommended(match, item, 'lose')" class="recommend-badge">荐</span>
                <span v-if="isWinningRecommendation(match, item, 'lose')" class="hit-cell-badge">中</span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>
  </div>
</template>

<script>
const FIXED_SIMULATIONS = 50000
const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日']
const COMPETITIONS = [
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
const DEFAULT_BASE_MATCH_WEIGHT = 1
const DEFAULT_RECENT_HALF_YEAR_BONUS = 0.1
const DEFAULT_WORLD_CUP_BONUS = 0.25
const DEFAULT_HOST_TEAM_GOAL_FACTOR = 1.42
const DEFAULT_SEED_TEAM_GOAL_FACTOR = 1.77
const DEFAULT_HANDICAP_SMOOTHING_FACTOR = 0.185
const MODEL_FACTOR_MIN = 0.1
const MODEL_FACTOR_MAX = 3
const MATCH_WEIGHT_MIN = 0
const MATCH_WEIGHT_MAX = 5
const BONUS_WEIGHT_MIN = 0
const BONUS_WEIGHT_MAX = 3
const HANDICAP_SMOOTHING_MIN = 0
const HANDICAP_SMOOTHING_MAX = 0.8
const MODEL_FACTOR_KEYS = [
  'baseMatchWeight',
  'recentHalfYearBonus',
  'worldCupBonus',
  'hostTeamGoalFactor',
  'seedTeamGoalFactor',
  'handicapSmoothingFactor'
]
const PROBABILITY_KEYS = ['win', 'draw', 'lose']
const HANDICAP_PAIR_SWITCH_THRESHOLD = 52

export default {
  name: 'App',
  data() {
    return {
      overview: {},
      response: {},
      competitions: COMPETITIONS,
      activeCompetition: 'WORLD_CUP',
      scheduleDates: [],
      weekdays: WEEKDAYS,
      calendarMonth: '',
      queryDate: '',
      modelMode: 'after',
      modelFactors: {
        baseMatchWeight: DEFAULT_BASE_MATCH_WEIGHT,
        recentHalfYearBonus: DEFAULT_RECENT_HALF_YEAR_BONUS,
        worldCupBonus: DEFAULT_WORLD_CUP_BONUS,
        hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR,
        seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR,
        handicapSmoothingFactor: DEFAULT_HANDICAP_SMOOTHING_FACTOR
      },
      selectedRows: {},
      loading: false,
      updatingData: false,
      errorMessage: ''
    }
  },
  computed: {
    matches() {
      return this.response.matches || []
    },
    matchColumns() {
      const matchCount = this.matches.length || 1
      if (matchCount > 4) {
        return '3'
      }
      return String(matchCount)
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
  methods: {
    async initializeUserConfig() {
      this.loadModelFactors()
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
          this.$set(this.modelFactors, key, this.normalizeModelFactor(config.modelFactors[key], this.getDefaultModelFactor(key), key))
        })
        this.saveModelFactorsToCookie()
      }
      if (config.selectedRows && typeof config.selectedRows === 'object' && !Array.isArray(config.selectedRows)) {
        this.selectedRows = this.normalizeSelectedRows(config.selectedRows)
        this.saveRecommendationSelectionsToCookie()
      }
    },
    async loadOverview() {
      this.errorMessage = ''
      try {
        const params = new URLSearchParams()
        params.append('competition', this.activeCompetition)
        const res = await fetch('/api/football/overview?' + params.toString())
        if (!res.ok) {
          throw new Error('服务响应异常')
        }
        const data = await res.json()
        this.applyOverview(data)
        if (this.queryDate) {
          await this.loadPredictions()
        }
      } catch (error) {
        this.errorMessage = '读取赛程概览失败：' + error.message
      }
    },
    async switchCompetition(competition) {
      if (competition === this.activeCompetition || this.loading || this.updatingData) {
        return
      }
      this.activeCompetition = competition
      this.overview = {}
      this.response = {}
      this.scheduleDates = []
      this.queryDate = ''
      await this.loadOverview()
    },
    applyOverview(data, preferredDate) {
      const nextScheduleDates = data.scheduleDates || []
      const nextDate = preferredDate && nextScheduleDates.includes(preferredDate)
        ? preferredDate
        : this.findDefaultDate(nextScheduleDates)
      this.overview = data
      this.scheduleDates = nextScheduleDates
      this.queryDate = nextDate
      this.calendarMonth = this.queryDate ? this.getMonthKey(this.parseDate(this.queryDate)) : this.getMonthKey(new Date())
    },
    async refreshData() {
      if (this.updatingData) {
        return
      }
      this.updatingData = true
      this.errorMessage = ''
      try {
        const currentDate = this.queryDate
        const params = new URLSearchParams()
        params.append('competition', this.activeCompetition)
        const res = await fetch('/api/football/data/refresh?' + params.toString(), {
          method: 'POST'
        })
        if (!res.ok) {
          throw new Error('服务响应异常')
        }
        const data = await res.json()
        this.applyOverview(data, currentDate)
        if (this.queryDate) {
          await this.loadPredictions()
        } else {
          this.response = {}
        }
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
      this.loading = true
      this.errorMessage = ''
      try {
        const params = new URLSearchParams()
        params.append('date', this.queryDate)
        params.append('competition', this.activeCompetition)
        params.append('simulations', FIXED_SIMULATIONS)
        params.append('baseMatchWeight', this.formatModelFactorValue(this.modelFactors.baseMatchWeight, DEFAULT_BASE_MATCH_WEIGHT, 'baseMatchWeight'))
        params.append('recentHalfYearBonus', this.formatModelFactorValue(this.modelFactors.recentHalfYearBonus, DEFAULT_RECENT_HALF_YEAR_BONUS, 'recentHalfYearBonus'))
        params.append('worldCupBonus', this.formatModelFactorValue(this.modelFactors.worldCupBonus, DEFAULT_WORLD_CUP_BONUS, 'worldCupBonus'))
        params.append('hostTeamGoalFactor', this.formatModelFactorValue(this.modelFactors.hostTeamGoalFactor, DEFAULT_HOST_TEAM_GOAL_FACTOR, 'hostTeamGoalFactor'))
        params.append('seedTeamGoalFactor', this.formatModelFactorValue(this.modelFactors.seedTeamGoalFactor, DEFAULT_SEED_TEAM_GOAL_FACTOR, 'seedTeamGoalFactor'))
        params.append('handicapSmoothingFactor', this.formatModelFactorValue(this.modelFactors.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor'))
        const res = await fetch('/api/football/predictions?' + params.toString())
        if (!res.ok) {
          throw new Error('服务响应异常')
        }
        this.response = await res.json()
      } catch (error) {
        this.errorMessage = '查询概率失败：' + error.message
      } finally {
        this.loading = false
      }
    },
    findDefaultDate(dates) {
      if (!dates || dates.length === 0) {
        return ''
      }
      const today = new Date().toISOString().slice(0, 10)
      if (dates.includes(today)) {
        return today
      }
      const upcomingDate = dates.find(date => date >= today)
      return upcomingDate || dates[dates.length - 1]
    },
    selectDate(date) {
      if (!date) {
        return
      }
      this.queryDate = date
      this.calendarMonth = this.getMonthKey(this.parseDate(date))
      this.loadPredictions()
    },
    shiftCalendarMonth(offset) {
      const baseDate = this.calendarMonth
        ? this.parseDate(this.calendarMonth + '-01')
        : new Date()
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
        parts[1].split(',').forEach(flag => {
          if (flag === 'n') {
            selection.normal = true
          } else if (flag.startsWith('h')) {
            selection.handicap = 'handicap-' + flag.slice(1)
          }
        })
        if (selection.normal || selection.handicap) {
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
        const normalized = {}
        if (selection.normal === true) {
          normalized.normal = true
        }
        if (typeof selection.handicap === 'string' && /^handicap--?\d+$/.test(selection.handicap)) {
          normalized.handicap = selection.handicap
        }
        if (normalized.normal || normalized.handicap) {
          result[matchId] = normalized
        }
      })
      return result
    },
    stringifyCompactSelections() {
      return Object.keys(this.selectedRows).map(matchId => {
        const selection = this.selectedRows[matchId] || {}
        const flags = []
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
          baseMatchWeight: this.normalizeModelFactor(parsedValue.baseMatchWeight, DEFAULT_BASE_MATCH_WEIGHT, 'baseMatchWeight'),
          recentHalfYearBonus: this.normalizeModelFactor(parsedValue.recentHalfYearBonus, DEFAULT_RECENT_HALF_YEAR_BONUS, 'recentHalfYearBonus'),
          worldCupBonus: this.normalizeModelFactor(parsedValue.worldCupBonus, DEFAULT_WORLD_CUP_BONUS, 'worldCupBonus'),
          hostTeamGoalFactor: this.normalizeModelFactor(parsedValue.hostTeamGoalFactor, DEFAULT_HOST_TEAM_GOAL_FACTOR, 'hostTeamGoalFactor'),
          seedTeamGoalFactor: this.normalizeModelFactor(parsedValue.seedTeamGoalFactor, DEFAULT_SEED_TEAM_GOAL_FACTOR, 'seedTeamGoalFactor'),
          handicapSmoothingFactor: this.normalizeModelFactor(parsedValue.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor')
        }
      } catch (error) {
        this.modelFactors = {
          baseMatchWeight: DEFAULT_BASE_MATCH_WEIGHT,
          recentHalfYearBonus: DEFAULT_RECENT_HALF_YEAR_BONUS,
          worldCupBonus: DEFAULT_WORLD_CUP_BONUS,
          hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR,
          seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR,
          handicapSmoothingFactor: DEFAULT_HANDICAP_SMOOTHING_FACTOR
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
        baseMatchWeight: Number(this.formatModelFactorValue(this.modelFactors.baseMatchWeight, DEFAULT_BASE_MATCH_WEIGHT, 'baseMatchWeight')),
        recentHalfYearBonus: Number(this.formatModelFactorValue(this.modelFactors.recentHalfYearBonus, DEFAULT_RECENT_HALF_YEAR_BONUS, 'recentHalfYearBonus')),
        worldCupBonus: Number(this.formatModelFactorValue(this.modelFactors.worldCupBonus, DEFAULT_WORLD_CUP_BONUS, 'worldCupBonus')),
        hostTeamGoalFactor: Number(this.formatModelFactorValue(this.modelFactors.hostTeamGoalFactor, DEFAULT_HOST_TEAM_GOAL_FACTOR, 'hostTeamGoalFactor')),
        seedTeamGoalFactor: Number(this.formatModelFactorValue(this.modelFactors.seedTeamGoalFactor, DEFAULT_SEED_TEAM_GOAL_FACTOR, 'seedTeamGoalFactor')),
        handicapSmoothingFactor: Number(this.formatModelFactorValue(this.modelFactors.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor'))
      }
    },
    saveUserConfig() {
      const payload = {
        modelFactors: this.buildModelFactorPayload(),
        selectedRows: this.normalizeSelectedRows(this.selectedRows)
      }
      fetch('/api/football/user-config', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      }).catch(() => {
        // Local cookies still keep the UI usable if the config file write fails.
      })
    },
    saveModelFactorInput(key) {
      const fallback = this.getDefaultModelFactor(key)
      this.$set(this.modelFactors, key, this.normalizeModelFactor(this.modelFactors[key], fallback, key))
      this.saveModelFactors()
    },
    commitModelFactors() {
      MODEL_FACTOR_KEYS.forEach(key => {
        this.$set(this.modelFactors, key, this.normalizeModelFactor(this.modelFactors[key], this.getDefaultModelFactor(key), key))
      })
      this.saveModelFactors()
      if (this.queryDate) {
        this.loadPredictions()
      }
    },
    resetModelFactors() {
      MODEL_FACTOR_KEYS.forEach(key => {
        this.$set(this.modelFactors, key, this.getDefaultModelFactor(key))
      })
      this.saveModelFactors()
      if (this.queryDate) {
        this.loadPredictions()
      }
    },
    getDefaultModelFactor(key) {
      if (key === 'baseMatchWeight') {
        return DEFAULT_BASE_MATCH_WEIGHT
      }
      if (key === 'recentHalfYearBonus') {
        return DEFAULT_RECENT_HALF_YEAR_BONUS
      }
      if (key === 'worldCupBonus') {
        return DEFAULT_WORLD_CUP_BONUS
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
      if (key === 'baseMatchWeight') {
        return MATCH_WEIGHT_MIN
      }
      if (key === 'recentHalfYearBonus' || key === 'worldCupBonus') {
        return BONUS_WEIGHT_MIN
      }
      return key === 'handicapSmoothingFactor' ? HANDICAP_SMOOTHING_MIN : MODEL_FACTOR_MIN
    },
    getModelFactorMax(key) {
      if (key === 'baseMatchWeight') {
        return MATCH_WEIGHT_MAX
      }
      if (key === 'recentHalfYearBonus' || key === 'worldCupBonus') {
        return BONUS_WEIGHT_MAX
      }
      return key === 'handicapSmoothingFactor' ? HANDICAP_SMOOTHING_MAX : MODEL_FACTOR_MAX
    },
    getModelFactorScale(key) {
      if (key === 'baseMatchWeight' || key === 'recentHalfYearBonus' || key === 'worldCupBonus') {
        return 2
      }
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
    getSelectionForMatch(match) {
      return this.selectedRows[match.matchId] || {}
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
      const selection = Object.assign({}, this.getSelectionForMatch(match))
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

      if (selection.normal || selection.handicap) {
        this.$set(this.selectedRows, match.matchId, selection)
      } else {
        this.$delete(this.selectedRows, match.matchId)
      }
      this.saveRecommendationSelections()
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
    buildRecommendationKeys(rows, applyHandicapThreshold) {
      const maxCell = this.findMaxProbabilityCell(rows)
      if (!maxCell) {
        return new Set()
      }
      if (applyHandicapThreshold && maxCell.row.handicap !== 0 && maxCell.value < 50) {
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
      if (handicapValue >= HANDICAP_PAIR_SWITCH_THRESHOLD && handicapValue < maxCell.value) {
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
      return scores || []
    },
    activeHalfFullProbabilities(match) {
      const probabilities = this.modelMode === 'after' && match.adjustedHalfFullProbabilities
        ? match.adjustedHalfFullProbabilities
        : match.halfFullProbabilities
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
    isWinningHalfFullPrediction(match, item) {
      return Boolean(match &&
        match.status === '已完赛' &&
        match.actualHalfFullResult &&
        match.actualHalfFullResult === item.label)
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
    halfFullProbabilityTitle(match) {
      return this.activeHalfFullProbabilities(match)
        .map(item => item.label + ' ' + this.formatProbability(item.probability))
        .join('，')
    },
    formatHandicap(value) {
      return value > 0 ? '+' + value : String(value)
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
  justify-content: space-between;
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

.competition-tabs {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 7px;
  width: 430px;
  max-width: 100%;
  margin-top: 16px;
}

.competition-tabs button {
  min-width: 0;
  height: 28px;
  padding: 0 8px;
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-radius: 999px;
  color: rgba(255, 255, 255, 0.84);
  background: rgba(15, 23, 42, 0.2);
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
}

.competition-tabs button.is-active {
  border-color: #ffffff;
  color: #1d4ed8;
  background: #ffffff;
}

.competition-tabs button:disabled {
  cursor: wait;
  opacity: 0.7;
}

.hero-card {
  align-self: stretch;
  display: grid;
  grid-template-columns: minmax(160px, 0.8fr) minmax(210px, 1fr) minmax(270px, 1.1fr);
  align-items: end;
  gap: 14px;
  justify-content: center;
  width: 780px;
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

.factor-controls,
.weight-controls {
  display: grid;
  gap: 8px;
  min-width: 0;
  padding-left: 14px;
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  text-align: left;
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
  grid-template-columns: minmax(0, 1fr);
  margin-bottom: 3px;
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
  min-width: 0;
}

.match-time {
  color: #64748b;
  font-size: 12px;
}

.match-card h2 {
  margin: 4px 0;
  font-size: 16px;
  line-height: 1.25;
}

.match-card h2 span {
  color: #94a3b8;
  font-size: 12px;
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
  flex: 0 0 auto;
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
  vertical-align: 1px;
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
  flex: 0 0 auto;
  min-width: 86px;
  padding: 8px 9px;
  border-radius: 10px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 11px;
  text-align: center;
}

.goal-box strong {
  display: block;
  margin-top: 4px;
  font-size: 15px;
  line-height: 1.1;
  white-space: nowrap;
}

.match-summary-row {
  display: grid;
  grid-template-columns: minmax(0, auto) minmax(0, 1fr);
  align-items: center;
  gap: 8px 12px;
  margin-top: 8px;
  margin-bottom: 4px;
  min-height: 36px;
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
  gap: 2px;
  min-width: 0;
  max-height: 36px;
  overflow: hidden;
  transform: translateY(-8px);
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

.handicap-cell {
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

.recommend-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  color: #ffffff;
  background: #ef4444;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
  vertical-align: middle;
}

.hit-cell-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  color: #ffffff;
  background: #16a34a;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
  vertical-align: middle;
}

.normal-row td {
  background: #eff6ff;
}

.normal-row .handicap-cell {
  color: #1d4ed8;
  font-weight: 800;
}

@media (max-width: 980px) {
  .page {
    overflow-y: auto;
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

  .factor-controls,
  .weight-controls {
    margin-top: 10px;
    padding-left: 0;
    padding-top: 10px;
    border-left: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
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
    grid-template-columns: minmax(0, 1fr);
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
}
</style>
