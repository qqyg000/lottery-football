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
            <div class="competition-option-list" role="listbox" aria-label="联赛单选">
              <button
                v-for="competition in getConcreteCompetitions()"
                :key="competition.code"
                type="button"
                class="competition-option"
                role="option"
                :aria-selected="activeCompetition === competition.code ? 'true' : 'false'"
                @click="selectCompetition(competition.code)"
              >
                <span>{{ competition.name }}</span>
                <span v-if="activeCompetition === competition.code" class="competition-option-check" aria-hidden="true">✓</span>
              </button>
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
            <div class="backtest-range-toggle" aria-label="回测范围">
              <button
                type="button"
                :class="{ 'is-active': includePreviousEdition }"
                :disabled="loading || updatingData || backtesting"
                @click="setIncludePreviousEdition(true)"
              >含上届赛事</button>
              <button
                type="button"
                :class="{ 'is-active': !includePreviousEdition }"
                :disabled="loading || updatingData || backtesting"
                @click="setIncludePreviousEdition(false)"
              >仅本届赛事</button>
            </div>
            <div class="hero-actions">
              <button
                type="button"
                class="factor-recalculate factor-reset refresh-data-button"
                :disabled="loading || updatingData || backtesting"
                @click="refreshData"
              >
                {{ updatingData ? '更新中' : '更新数据' }}
              </button>
            </div>
          </div>
          <div class="factor-controls" aria-label="模型参数">
            <div class="factor-control-column">
              <label
                class="factor-control"
                :class="{ 'is-competition-disabled': !activeParameterProfileEditable || activeCompetition !== 'WORLD_CUP' }"
                :title="!activeParameterProfileEditable ? '请只选择一个赛事后编辑参数' : (activeCompetition !== 'WORLD_CUP' ? '仅世界杯赛事可用' : '')"
              >
                <span><i class="help-icon" tabindex="0" role="img" aria-label="世界杯种子队的预期进球修正倍数，仅世界杯赛事生效" data-tooltip="世界杯种子队的预期进球修正倍数，仅世界杯赛事生效">i</i>种子队进球系数</span>
                <input
                  type="number"
                  min="0.1"
                  max="3"
                  step="0.01"
                  v-model.number="modelFactors.seedTeamGoalFactor"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable || activeCompetition !== 'WORLD_CUP'"
                  @blur="saveModelFactorInput('seedTeamGoalFactor')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
              <label
                class="factor-control"
                :class="{ 'is-competition-disabled': !activeParameterProfileEditable || activeCompetition !== 'WORLD_CUP' }"
                :title="!activeParameterProfileEditable ? '请只选择一个赛事后编辑参数' : (activeCompetition !== 'WORLD_CUP' ? '仅世界杯赛事可用' : '')"
              >
                <span><i class="help-icon" tabindex="0" role="img" aria-label="世界杯东道主的预期进球修正倍数，仅世界杯赛事生效" data-tooltip="世界杯东道主的预期进球修正倍数，仅世界杯赛事生效">i</i>东道主进球系数</span>
                <input
                  type="number"
                  min="0.1"
                  max="3"
                  step="0.01"
                  v-model.number="modelFactors.hostTeamGoalFactor"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable || activeCompetition !== 'WORLD_CUP'"
                  @blur="saveModelFactorInput('hostTeamGoalFactor')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
              <label class="factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="主队预期进球的修正倍数，1.00 表示不额外修正" data-tooltip="主队预期进球的修正倍数，1.00 表示不额外修正">i</i>主场进球系数</span>
                <input
                  type="number"
                  min="0.1"
                  max="3"
                  step="0.01"
                  v-model.number="modelFactors.homeTeamGoalFactor"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                  @blur="saveModelFactorInput('homeTeamGoalFactor')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
              <label class="factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="控制让球概率向普通胜平负概率平滑修正的强度，数值越大修正越明显" data-tooltip="控制让球概率向普通胜平负概率平滑修正的强度，数值越大修正越明显">i</i>让球平滑系数</span>
                <input
                  type="number"
                  min="0"
                  max="0.8"
                  step="0.001"
                  v-model.number="modelFactors.handicapSmoothingFactor"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                  @blur="saveModelFactorInput('handicapSmoothingFactor')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
            </div>
            <div class="factor-control-column">
              <label class="factor-control percentage-factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="普通盘和让球盘同时选择时，让球同向概率达到该值才切换为让球推荐" data-tooltip="普通盘和让球盘同时选择时，让球同向概率达到该值才切换为让球推荐">i</i>让球推荐阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="handicapRecommendationThreshold"
                    :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                    @change="saveRecommendationThresholdInputs"
                    @keyup.enter="saveRecommendationThresholdInputs"
                  >
                  <span class="percentage-suffix">%</span>
                </span>
              </label>
              <label class="factor-control percentage-factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="让球盘的最高非平局概率低于该值时，改为推荐另外两个结果" data-tooltip="让球盘的最高非平局概率低于该值时，改为推荐另外两个结果">i</i>让球反向阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="handicapReverseThreshold"
                    :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                    @change="saveRecommendationThresholdInputs"
                    @keyup.enter="saveRecommendationThresholdInputs"
                  >
                  <span class="percentage-suffix">%</span>
                </span>
              </label>
              <label class="factor-control percentage-factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="双项推荐中最高概率超过该值时，只保留概率最高的一项" data-tooltip="双项推荐中最高概率超过该值时，只保留概率最高的一项">i</i>单项推荐阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="singleRecommendationThreshold"
                    :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                    @change="saveRecommendationThresholdInputs"
                    @keyup.enter="saveRecommendationThresholdInputs"
                  >
                  <span class="percentage-suffix">%</span>
                </span>
              </label>
              <label class="factor-control percentage-factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="所有推荐项的竞彩赔率都不低于该值时，才保留本场推荐" data-tooltip="所有推荐项的竞彩赔率都不低于该值时，才保留本场推荐">i</i>推荐赔率阈值</span>
                <span class="percentage-input">
                  <input
                    type="number"
                    min="1"
                    max="100"
                    step="0.01"
                    inputmode="decimal"
                    v-model.number="recommendationOdds"
                    :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                    @change="normalizeRecommendationOddsInput"
                    @keyup.enter="runRecommendationOddsBacktest"
                  >
                  <span class="percentage-suffix odds-suffix" aria-hidden="true">倍</span>
                </span>
              </label>
            </div>
            <div class="factor-control-column">
              <label class="factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="正式比赛在球队强度计算中的样本权重，1.00 表示完整计入" data-tooltip="正式比赛在球队强度计算中的样本权重，1.00 表示完整计入">i</i>正式比赛权重</span>
                <input
                  type="number"
                  min="0"
                  max="1"
                  step="0.01"
                  v-model.number="modelFactors.officialMatchWeight"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                  @blur="saveModelFactorInput('officialMatchWeight')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
              <label class="factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="国家队友谊赛在球队强度计算中的样本权重，0 表示不计入" data-tooltip="国家队友谊赛在球队强度计算中的样本权重，0 表示不计入">i</i>国家队友谊赛权重</span>
                <input
                  type="number"
                  min="0"
                  max="1"
                  step="0.01"
                  v-model.number="modelFactors.internationalFriendlyWeight"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                  @blur="saveModelFactorInput('internationalFriendlyWeight')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
              <label class="factor-control">
                <span><i class="help-icon" tabindex="0" role="img" aria-label="俱乐部友谊赛在球队强度计算中的样本权重，0 表示不计入" data-tooltip="俱乐部友谊赛在球队强度计算中的样本权重，0 表示不计入">i</i>俱乐部友谊赛权重</span>
                <input
                  type="number"
                  min="0"
                  max="1"
                  step="0.01"
                  v-model.number="modelFactors.clubFriendlyWeight"
                  :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                  @blur="saveModelFactorInput('clubFriendlyWeight')"
                  @keyup.enter="$event.target.blur()"
                >
              </label>
              <button
                type="button"
                class="factor-recalculate factor-column-action"
                :disabled="loading || updatingData || backtesting || !activeParameterProfileEditable"
                @click="toggleParameterPreset"
              >
                {{ parameterPresetToggleText }}
              </button>
            </div>
          </div>
        </div>
        <div class="global-parameter-card" aria-label="全局参数">
          <div class="backtest-result" :class="{ 'is-empty': !backtestActive }">
            <div class="backtest-average-grid">
              <div title="场均投注 = 总投入 ÷ 推荐比赛数，每个推荐项按 1 单位投入">
                <strong>{{ backtestAverageStakeText }}</strong>
                <span><i class="help-icon" tabindex="0" role="img" aria-label="总投入除以推荐比赛数，每个推荐项按 1 单位投入" data-tooltip="总投入除以推荐比赛数，每个推荐项按 1 单位投入">i</i>场均投注</span>
              </div>
              <div>
                <strong>{{ backtestAverageOddsText }}</strong>
                <span><i class="help-icon" tabindex="0" role="img" aria-label="总返奖除以推荐比赛数，未命中的比赛按 0 返奖计入" data-tooltip="总返奖除以推荐比赛数，未命中的比赛按 0 返奖计入">i</i>场均返奖</span>
              </div>
              <div>
                <strong>{{ backtestSamplingRateText }}</strong>
                <span><i class="help-icon" tabindex="0" role="img" aria-label="产生推荐的比赛数除以回测已完赛比赛总数" data-tooltip="产生推荐的比赛数除以回测已完赛比赛总数">i</i>采样率</span>
              </div>
              <div>
                <strong>{{ backtestHitRateText }}</strong>
                <span><i class="help-icon" tabindex="0" role="img" aria-label="至少命中一个推荐项的比赛数除以推荐比赛数" data-tooltip="至少命中一个推荐项的比赛数除以推荐比赛数">i</i>命中率</span>
              </div>
              <div class="backtest-roi-card" title="ROI = [(总返奖 ÷ 总投入) - 1] × 100%，每个推荐项按 1 单位投入">
                <strong>{{ backtestRoiText }}</strong>
                <span><i class="help-icon" tabindex="0" role="img" aria-label="投资回报率，计算公式为总返奖除以总投入再减 1" data-tooltip="投资回报率，计算公式为总返奖除以总投入再减 1">i</i>ROI</span>
              </div>
            </div>
          </div>
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
          :class="{
            'is-empty': cell.empty,
            'is-selected': !cell.empty && !!queryDate && cell.date === queryDate,
            'is-loading': !cell.empty && !!queryDate && cell.date === queryDate && loading,
            'has-schedule': cell.hasSchedule
          }"
          :disabled="cell.empty || loading || updatingData || backtesting"
          @click="selectDate(cell.date)"
        >
          <span class="calendar-day-number">{{ cell.day }}</span>
        </button>
      </div>
    </section>

    <section v-if="errorMessage" class="error-box">{{ errorMessage }}</section>

    <section v-if="!loading && !backtesting && matches.length === 0" class="empty-box">
      {{ backtestActive ? '当前回测参数下没有推荐场次' : '当前日期暂无带赔率的赛程，请切换日期后查询' }}
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
      v-if="queryDate"
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
        <p class="head-to-head-description">展示本场开赛前的正式比赛及降权友谊赛，最多显示最近 50 场</p>

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

    <div v-if="backtesting || updatingData" class="backtest-mask" role="status" aria-live="polite" aria-busy="true">
      <div class="backtest-mask-card">
        <div class="backtest-mask-heading">
          <strong>{{ updatingData ? '正在更新数据' : '正在回测' }}</strong>
          <span>{{ operationProgressText }}</span>
        </div>
        <span>{{ operationProgressDetail }}</span>
        <div
          class="backtest-progress"
          role="progressbar"
          :aria-label="updatingData ? '数据更新进度' : '回测进度'"
          aria-valuemin="0"
          aria-valuemax="100"
          :aria-valuenow="operationProgress"
          :aria-valuetext="operationProgressDetail"
        >
          <span class="backtest-progress-bar" :style="{ width: operationProgress + '%' }"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { calculateFlatStakeBacktest, calculateSamplingRate } from './backtest-roi.mjs'

const FIXED_SIMULATIONS = 50000
const BACKTEST_PROGRESS_POLL_INTERVAL = 300
const DATA_REFRESH_PROGRESS_POLL_INTERVAL = 300
const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日']
const UTC_PLUS_EIGHT_TIME_ZONE = 'Asia/Shanghai'
const COMPETITIONS = [
  { code: 'ALL', name: '全部' },
  { code: 'WORLD_CUP', name: '世界杯' },
  { code: 'EUROPEAN_CHAMPIONSHIP', name: '欧洲杯' },
  { code: 'COPA_AMERICA', name: '美洲杯' },
  { code: 'CLUB_WORLD_CUP', name: '世俱杯' },
  { code: 'EUROPA_LEAGUE', name: '欧罗巴' },
  { code: 'CHAMPIONS_LEAGUE', name: '欧冠' },
  { code: 'PREMIER_LEAGUE', name: '英超' },
  { code: 'LA_LIGA', name: '西甲' },
  { code: 'SERIE_A', name: '意甲' },
  { code: 'BUNDESLIGA', name: '德甲' },
  { code: 'LIGUE_1', name: '法甲' },
  { code: 'BRAZIL_SERIE_A', name: '巴甲' },
  { code: 'PRIMEIRA_LIGA', name: '葡超' },
  { code: 'EREDIVISIE', name: '荷甲' },
  { code: 'ARGENTINE_PRIMERA_DIVISION', name: '阿甲' }
]
const CURRENT_EDITION_START_DATES = {
  WORLD_CUP: '2026-06-11',
  EUROPEAN_CHAMPIONSHIP: '2024-06-14',
  COPA_AMERICA: '2024-06-20',
  CLUB_WORLD_CUP: '2025-06-14',
  EUROPA_LEAGUE: '2026-07-09',
  CHAMPIONS_LEAGUE: '2026-07-07',
  PREMIER_LEAGUE: '2026-08-21',
  LA_LIGA: '2026-08-15',
  SERIE_A: '2026-08-22',
  BUNDESLIGA: '2026-08-28',
  LIGUE_1: '2026-08-20',
  BRAZIL_SERIE_A: '2026-01-28',
  PRIMEIRA_LIGA: '2026-08-07',
  EREDIVISIE: '2026-08-07',
  ARGENTINE_PRIMERA_DIVISION: '2026-01-25'
}
const SELECTION_COOKIE = 'worldcup_recommendation_rows'
const SELECTION_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const MODEL_FACTOR_COOKIE = 'worldcup_model_factors'
const MODEL_FACTOR_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const GLOBAL_PARAMETER_COOKIE = 'worldcup_global_parameters'
const GLOBAL_PARAMETER_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const ACTIVE_COMPETITION_COOKIE = 'football_active_competition'
const ACTIVE_COMPETITION_COOKIE_MAX_AGE = 60 * 60 * 24 * 180
const DEFAULT_HOST_TEAM_GOAL_FACTOR = 1.10
const DEFAULT_HOME_TEAM_GOAL_FACTOR = 1.06
const DEFAULT_SEED_TEAM_GOAL_FACTOR = 1.85
const DEFAULT_OFFICIAL_MATCH_WEIGHT = 1.00
const DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT = 0.50
const DEFAULT_CLUB_FRIENDLY_WEIGHT = 0.30
const DEFAULT_HANDICAP_SMOOTHING_FACTOR = 0.274
const DEFAULT_RECOMMENDATION_ODDS = 1.03
const DEFAULT_HANDICAP_RECOMMENDATION_THRESHOLD = 68.16
const DEFAULT_HANDICAP_REVERSE_THRESHOLD = 46.78
const DEFAULT_SINGLE_RECOMMENDATION_THRESHOLD = 71.72
const RECOMMENDATION_ODDS_MIN = 1
const RECOMMENDATION_ODDS_MAX = 100
const RECOMMENDATION_THRESHOLD_MIN = 0
const RECOMMENDATION_THRESHOLD_MAX = 100
const MODEL_FACTOR_MIN = 0.1
const MODEL_FACTOR_MAX = 3
const HANDICAP_SMOOTHING_MIN = 0
const HANDICAP_SMOOTHING_MAX = 0.8
const MATCH_TYPE_WEIGHT_MIN = 0
const MATCH_TYPE_WEIGHT_MAX = 1
const MATCH_TYPE_WEIGHT_KEYS = [
  'officialMatchWeight',
  'internationalFriendlyWeight',
  'clubFriendlyWeight'
]
const STABLE_PARAMETER_PRESET = {
  modelFactors: {
    hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR,
    homeTeamGoalFactor: DEFAULT_HOME_TEAM_GOAL_FACTOR,
    seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR,
    officialMatchWeight: DEFAULT_OFFICIAL_MATCH_WEIGHT,
    internationalFriendlyWeight: DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT,
    clubFriendlyWeight: DEFAULT_CLUB_FRIENDLY_WEIGHT,
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
    hostTeamGoalFactor: 2.30,
    homeTeamGoalFactor: 1.75,
    seedTeamGoalFactor: 1.55,
    officialMatchWeight: DEFAULT_OFFICIAL_MATCH_WEIGHT,
    internationalFriendlyWeight: DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT,
    clubFriendlyWeight: DEFAULT_CLUB_FRIENDLY_WEIGHT,
    handicapSmoothingFactor: 0.650
  },
  globalParameters: {
    recommendationOdds: 2.46,
    handicapRecommendationThreshold: 89.09,
    handicapReverseThreshold: 43.41,
    singleRecommendationThreshold: 78.71
  }
}
const PARAMETER_PROFILE_RANGES = ['CURRENT', 'PREVIOUS']
const PARAMETER_PRESETS = ['STABLE', 'AGGRESSIVE']
const MODEL_FACTOR_KEYS = [
  'hostTeamGoalFactor',
  'homeTeamGoalFactor',
  'seedTeamGoalFactor',
  'officialMatchWeight',
  'internationalFriendlyWeight',
  'clubFriendlyWeight',
  'handicapSmoothingFactor'
]
const PROBABILITY_KEYS = ['win', 'draw', 'lose']
const PROBABILITY_LABELS = {
  win: '胜',
  draw: '平',
  lose: '负'
}

function createDefaultParameterProfile(parameterPreset = 'STABLE') {
  const preset = parameterPreset === 'AGGRESSIVE'
    ? AGGRESSIVE_PARAMETER_PRESET
    : STABLE_PARAMETER_PRESET
  return {
    modelFactors: { ...preset.modelFactors },
    globalParameters: { ...preset.globalParameters }
  }
}

function createDefaultParameterProfiles() {
  return COMPETITIONS
    .filter(competition => competition.code !== 'ALL')
    .reduce((profiles, competition) => {
      PARAMETER_PROFILE_RANGES.forEach(range => {
        PARAMETER_PRESETS.forEach(parameterPreset => {
          profiles[competition.code + ':' + range + ':' + parameterPreset] = createDefaultParameterProfile(parameterPreset)
        })
      })
      return profiles
    }, {})
}

function createEmptyBacktestSummary() {
  return {
    completedMatchCount: 0,
    sportteryCompletedMatchCount: 0,
    oddsMatchCount: 0,
    samplingRate: null,
    recommendedMatchCount: 0,
    recommendedSelectionCount: 0,
    hitMatchCount: 0,
    missMatchCount: 0,
    winningSelectionCount: 0,
    averageWinningOdds: null,
    averageOddsIncludingMisses: null,
    totalStake: 0,
    totalReturn: 0,
    netProfit: 0,
    roi: null
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
      competitionDropdownOpen: false,
      scheduleDates: [],
      weekdays: WEEKDAYS,
      calendarMonth: '',
      queryDate: '',
      modelMode: 'after',
      includePreviousEdition: false,
      parameterProfiles: createDefaultParameterProfiles(),
      modelFactors: {
        hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR.toFixed(2),
        homeTeamGoalFactor: DEFAULT_HOME_TEAM_GOAL_FACTOR.toFixed(2),
        seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR.toFixed(2),
        officialMatchWeight: DEFAULT_OFFICIAL_MATCH_WEIGHT.toFixed(2),
        internationalFriendlyWeight: DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT.toFixed(2),
        clubFriendlyWeight: DEFAULT_CLUB_FRIENDLY_WEIGHT.toFixed(2),
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
      dataRefreshProgress: 0,
      dataRefreshMessage: '正在准备更新数据...',
      errorMessage: ''
    }
  },
  computed: {
    activeCompetition() {
      return this.getSelectedCompetitionCodes()[0] || 'WORLD_CUP'
    },
    activeCompetitionLabel() {
      const selectedCompetition = this.getSelectedCompetitions()[0]
      return selectedCompetition ? selectedCompetition.name : '世界杯'
    },
    activeParameterProfileEditable() {
      return this.getSelectedCompetitionCodes().length === 1
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
    backtestAverageStakeText() {
      if (!this.backtestActive) {
        return '--'
      }
      const recommendedMatchCount = Number(this.backtestSummary.recommendedMatchCount) || 0
      if (recommendedMatchCount <= 0) {
        return '0.00'
      }
      const totalStake = Number(this.backtestSummary.totalStake) || 0
      return (totalStake / recommendedMatchCount).toFixed(2)
    },
    backtestSamplingRateText() {
      if (!this.backtestActive) {
        return '--'
      }
      const samplingRate = this.backtestSummary.samplingRate
      const numberValue = Number(samplingRate)
      if (samplingRate === null || !Number.isFinite(numberValue)) {
        return '--'
      }
      return (numberValue * 100).toFixed(1) + '%'
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
      const netRoi = this.backtestSummary.roi
      if (typeof netRoi !== 'number' || !Number.isFinite(netRoi)) {
        return '--'
      }
      return (netRoi * 100).toFixed(1) + '%'
    },
    operationProgress() {
      return this.updatingData ? this.dataRefreshProgress : this.backtestProgress
    },
    operationProgressText() {
      return Number(this.operationProgress).toFixed(1) + '%'
    },
    operationProgressDetail() {
      if (this.updatingData) {
        return this.dataRefreshMessage || '正在准备更新数据...'
      }
      return this.backtestProgressDetail
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
      return this.activeParameterPreset === 'aggressive'
        ? '切换稳健方案'
        : '切换激进方案'
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
      this.competitionDropdownOpen = true
    },
    closeCompetitionDropdown() {
      this.competitionDropdownOpen = false
    },
    handleCompetitionOutsideClick(event) {
      if (!this.competitionDropdownOpen || !this.$refs.competitionSelect) {
        return
      }
      if (!this.$refs.competitionSelect.contains(event.target)) {
        this.closeCompetitionDropdown()
      }
    },
    async selectCompetition(code) {
      if (this.loading || this.updatingData || this.backtesting) {
        return
      }
      const selectedCompetition = this.getConcreteCompetitions().find(item => item.code === code)
      if (!selectedCompetition) {
        return
      }
      const selectionChanged = selectedCompetition.code !== this.activeCompetition
      this.competitionDropdownOpen = false
      if (!selectionChanged) {
        return
      }
      const currentDate = this.queryDate
      this.storeActiveParameterProfile()
      this.clearBacktestResults()
      this.activeCompetitions = [selectedCompetition.code]
      this.loadActiveParameterProfile()
      this.saveActiveCompetition()
      this.saveUserConfig()
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
        params.append('limit', '50')
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
      this.parameterProfiles = this.normalizeParameterProfiles(null, this.buildParameterProfilePayload())
      this.loadActiveParameterProfile()
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
      this.modelMode = 'after'
      this.includePreviousEdition = config.includePreviousEdition === true
      const displayedProfile = this.buildParameterProfilePayload()
      const legacyProfile = {
        modelFactors: config.modelFactors && typeof config.modelFactors === 'object' && !Array.isArray(config.modelFactors)
          ? config.modelFactors
          : displayedProfile.modelFactors,
        globalParameters: config.globalParameters && typeof config.globalParameters === 'object' && !Array.isArray(config.globalParameters)
          ? config.globalParameters
          : displayedProfile.globalParameters
      }
      this.parameterProfiles = this.normalizeParameterProfiles(config.parameterProfiles, legacyProfile)
      this.loadActiveParameterProfile()
      if (config.selectedRows && typeof config.selectedRows === 'object' && !Array.isArray(config.selectedRows)) {
        this.selectedRows = this.normalizeSelectedRows(config.selectedRows)
        this.saveRecommendationSelectionsToCookie()
      }
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
      const concreteCompetitions = this.getConcreteCompetitions()
      const validCodes = new Set(concreteCompetitions.map(competition => competition.code))
      const selectedCode = this.activeCompetitions.find(code => validCodes.has(code))
      return selectedCode ? [selectedCode] : concreteCompetitions.slice(0, 1).map(competition => competition.code)
    },
    getSelectedCompetitions() {
      const selectedCodes = new Set(this.getSelectedCompetitionCodes())
      return this.getConcreteCompetitions().filter(competition => selectedCodes.has(competition.code))
    },
    getParameterProfileKey(
      competition,
      includePreviousEdition = this.includePreviousEdition,
      parameterPreset = this.activeParameterPreset
    ) {
      const usePreviousProfile = includePreviousEdition || !this.isCurrentEditionStarted(competition)
      const presetName = String(parameterPreset || 'stable').toUpperCase() === 'AGGRESSIVE'
        ? 'AGGRESSIVE'
        : 'STABLE'
      return competition + ':' + (usePreviousProfile ? 'PREVIOUS' : 'CURRENT') + ':' + presetName
    },
    isCurrentEditionStarted(competition) {
      const startDate = CURRENT_EDITION_START_DATES[competition]
      return !startDate || startDate <= getUtcPlusEightDate()
    },
    normalizeParameterProfiles(parameterProfiles, legacyProfile) {
      const sourceProfiles = parameterProfiles && typeof parameterProfiles === 'object' && !Array.isArray(parameterProfiles)
        ? parameterProfiles
        : {}
      const stableFallbackProfile = this.normalizeParameterProfile(
        legacyProfile || createDefaultParameterProfile('STABLE'),
        'STABLE'
      )
      return this.getConcreteCompetitions().reduce((profiles, competition) => {
        PARAMETER_PROFILE_RANGES.forEach(range => {
          PARAMETER_PRESETS.forEach(parameterPreset => {
            const key = competition.code + ':' + range + ':' + parameterPreset
            const legacyKey = competition.code + ':' + range
            const fallbackProfile = parameterPreset === 'AGGRESSIVE'
              ? createDefaultParameterProfile('AGGRESSIVE')
              : (sourceProfiles[legacyKey] || stableFallbackProfile)
            profiles[key] = this.normalizeParameterProfile(
              sourceProfiles[key] || fallbackProfile,
              parameterPreset
            )
          })
        })
        return profiles
      }, {})
    },
    normalizeParameterProfile(profile, parameterPreset = 'STABLE') {
      const defaults = createDefaultParameterProfile(parameterPreset)
      const source = profile && typeof profile === 'object' && !Array.isArray(profile)
        ? profile
        : defaults
      const modelFactors = source.modelFactors && typeof source.modelFactors === 'object' && !Array.isArray(source.modelFactors)
        ? source.modelFactors
        : {}
      const globalParameters = source.globalParameters && typeof source.globalParameters === 'object' && !Array.isArray(source.globalParameters)
        ? source.globalParameters
        : {}
      return {
        modelFactors: {
          hostTeamGoalFactor: this.normalizeModelFactor(modelFactors.hostTeamGoalFactor, defaults.modelFactors.hostTeamGoalFactor, 'hostTeamGoalFactor'),
          homeTeamGoalFactor: this.normalizeModelFactor(modelFactors.homeTeamGoalFactor, defaults.modelFactors.homeTeamGoalFactor, 'homeTeamGoalFactor'),
          seedTeamGoalFactor: this.normalizeModelFactor(modelFactors.seedTeamGoalFactor, defaults.modelFactors.seedTeamGoalFactor, 'seedTeamGoalFactor'),
          officialMatchWeight: this.normalizeModelFactor(modelFactors.officialMatchWeight, defaults.modelFactors.officialMatchWeight, 'officialMatchWeight'),
          internationalFriendlyWeight: this.normalizeModelFactor(modelFactors.internationalFriendlyWeight, defaults.modelFactors.internationalFriendlyWeight, 'internationalFriendlyWeight'),
          clubFriendlyWeight: this.normalizeModelFactor(modelFactors.clubFriendlyWeight, defaults.modelFactors.clubFriendlyWeight, 'clubFriendlyWeight'),
          handicapSmoothingFactor: this.normalizeModelFactor(modelFactors.handicapSmoothingFactor, defaults.modelFactors.handicapSmoothingFactor, 'handicapSmoothingFactor')
        },
        globalParameters: {
          recommendationOdds: this.normalizeRecommendationOdds(
            globalParameters.recommendationOdds,
            defaults.globalParameters.recommendationOdds
          ),
          handicapRecommendationThreshold: this.normalizeRecommendationThreshold(
            globalParameters.handicapRecommendationThreshold,
            defaults.globalParameters.handicapRecommendationThreshold
          ),
          handicapReverseThreshold: this.normalizeRecommendationThreshold(
            globalParameters.handicapReverseThreshold,
            defaults.globalParameters.handicapReverseThreshold
          ),
          singleRecommendationThreshold: this.normalizeRecommendationThreshold(
            globalParameters.singleRecommendationThreshold,
            defaults.globalParameters.singleRecommendationThreshold
          )
        }
      }
    },
    buildParameterProfilePayload() {
      return {
        modelFactors: this.buildModelFactorPayload(),
        globalParameters: this.buildGlobalParameterPayload()
      }
    },
    storeActiveParameterProfile() {
      const competition = this.getSelectedCompetitionCodes()[0]
      if (!this.activeParameterProfileEditable || !competition) {
        return
      }
      const presetName = this.activeParameterPreset === 'aggressive' ? 'AGGRESSIVE' : 'STABLE'
      const profile = this.normalizeParameterProfile(this.buildParameterProfilePayload(), presetName)
      this.$set(this.parameterProfiles, this.getParameterProfileKey(competition), profile)
    },
    loadActiveParameterProfile() {
      const competition = this.getSelectedCompetitionCodes()[0]
      if (!this.activeParameterProfileEditable || !competition) {
        return
      }
      this.applyParameterProfile(this.getParameterProfile(competition))
      this.saveModelFactorsToCookie()
      this.saveGlobalParametersToCookie()
    },
    applyParameterProfile(profile) {
      const presetName = this.activeParameterPreset === 'aggressive' ? 'AGGRESSIVE' : 'STABLE'
      const defaults = createDefaultParameterProfile(presetName)
      const normalized = this.normalizeParameterProfile(profile, presetName)
      MODEL_FACTOR_KEYS.forEach(key => {
        this.$set(
          this.modelFactors,
          key,
          this.formatModelFactorValue(normalized.modelFactors[key], defaults.modelFactors[key], key)
        )
      })
      this.recommendationOdds = this.formatRecommendationOddsValue(
        normalized.globalParameters.recommendationOdds,
        defaults.globalParameters.recommendationOdds
      )
      this.handicapRecommendationThreshold = this.formatRecommendationThresholdValue(
        normalized.globalParameters.handicapRecommendationThreshold,
        defaults.globalParameters.handicapRecommendationThreshold
      )
      this.handicapReverseThreshold = this.formatRecommendationThresholdValue(
        normalized.globalParameters.handicapReverseThreshold,
        defaults.globalParameters.handicapReverseThreshold
      )
      this.singleRecommendationThreshold = this.formatRecommendationThresholdValue(
        normalized.globalParameters.singleRecommendationThreshold,
        defaults.globalParameters.singleRecommendationThreshold
      )
    },
    getParameterProfile(competition, includePreviousEdition = this.includePreviousEdition) {
      const profile = this.parameterProfiles[this.getParameterProfileKey(competition, includePreviousEdition)]
      const presetName = this.activeParameterPreset === 'aggressive' ? 'AGGRESSIVE' : 'STABLE'
      return profile || createDefaultParameterProfile(presetName)
    },
    getMatchGlobalParameters(match) {
      const competition = match && match.competition
        ? match.competition
        : this.getSelectedCompetitionCodes()[0]
      return this.getParameterProfile(competition).globalParameters
    },
    async fetchCompetitionOverview(competition) {
      const params = new URLSearchParams()
      params.append('competition', competition)
      params.append('includePreviousEdition', String(this.includePreviousEdition))
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
      this.resetDataRefreshProgress()
      this.updatingData = true
      this.errorMessage = ''
      try {
        const currentDate = this.queryDate
        const params = new URLSearchParams()
        params.append('competition', this.getSelectedCompetitionCodes()[0] || 'WORLD_CUP')
        if (currentDate) {
          params.append('date', currentDate)
        }
        const res = await fetch('/api/football/data/refresh/jobs?' + params.toString(), {
          method: 'POST'
        })
        if (!res.ok) {
          throw new Error('创建数据更新任务失败')
        }
        const job = await res.json()
        await this.waitForDataRefreshJob(job)
        this.dataRefreshProgress = 100
        this.dataRefreshMessage = '数据更新完成，正在刷新页面数据'
        await this.loadOverview(currentDate)
      } catch (error) {
        this.errorMessage = '更新数据失败：' + error.message
      } finally {
        this.updatingData = false
      }
    },
    async waitForDataRefreshJob(initialJob) {
      let job = initialJob
      while (job && (job.status === 'QUEUED' || job.status === 'RUNNING')) {
        this.applyDataRefreshProgress(job)
        await this.wait(DATA_REFRESH_PROGRESS_POLL_INTERVAL)
        const res = await fetch(
          '/api/football/data/refresh/jobs/' + encodeURIComponent(job.jobId),
          { cache: 'no-store' }
        )
        if (!res.ok) {
          throw new Error('读取数据更新进度失败')
        }
        job = await res.json()
      }
      this.applyDataRefreshProgress(job)
      if (job && job.status === 'COMPLETED' && job.result) {
        return job.result
      }
      throw new Error(job && job.message ? job.message : '数据更新任务失败')
    },
    applyDataRefreshProgress(job) {
      if (!job) {
        return
      }
      const progress = Number(job.progress)
      this.dataRefreshProgress = Number.isFinite(progress)
        ? Math.max(0, Math.min(100, progress))
        : 0
      this.dataRefreshMessage = job.message || '正在准备更新数据...'
    },
    resetDataRefreshProgress() {
      this.dataRefreshProgress = 0
      this.dataRefreshMessage = '正在准备更新数据...'
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
      this.appendModelFactorParams(params, this.getParameterProfile(competition).modelFactors)
      const res = await fetch('/api/football/predictions?' + params.toString())
      if (!res.ok) {
        throw new Error('服务响应异常')
      }
      return res.json()
    },
    appendModelFactorParams(params, modelFactors) {
      const defaults = this.getActiveParameterPresetDefaults().modelFactors
      const factors = modelFactors || defaults
      params.append('hostTeamGoalFactor', this.formatModelFactorValue(factors.hostTeamGoalFactor, defaults.hostTeamGoalFactor, 'hostTeamGoalFactor'))
      params.append('homeTeamGoalFactor', this.formatModelFactorValue(factors.homeTeamGoalFactor, defaults.homeTeamGoalFactor, 'homeTeamGoalFactor'))
      params.append('seedTeamGoalFactor', this.formatModelFactorValue(factors.seedTeamGoalFactor, defaults.seedTeamGoalFactor, 'seedTeamGoalFactor'))
      params.append('officialMatchWeight', this.formatModelFactorValue(factors.officialMatchWeight, defaults.officialMatchWeight, 'officialMatchWeight'))
      params.append('internationalFriendlyWeight', this.formatModelFactorValue(factors.internationalFriendlyWeight, defaults.internationalFriendlyWeight, 'internationalFriendlyWeight'))
      params.append('clubFriendlyWeight', this.formatModelFactorValue(factors.clubFriendlyWeight, defaults.clubFriendlyWeight, 'clubFriendlyWeight'))
      params.append('handicapSmoothingFactor', this.formatModelFactorValue(factors.handicapSmoothingFactor, defaults.handicapSmoothingFactor, 'handicapSmoothingFactor'))
    },
    buildBacktestModelFactorsPayload(competitions) {
      return competitions.reduce((result, competition) => {
        result[competition] = { ...this.getParameterProfile(competition).modelFactors }
        return result
      }, {})
    },
    async runRecommendationOddsBacktest() {
      if (this.backtesting) {
        return
      }
      this.recommendationOdds = this.formatRecommendationOddsValue(this.recommendationOdds)
      this.normalizeRecommendationThresholdInputs()
      this.saveGlobalParametersToCookie()
      this.resetBacktestProgress()
      this.queryDate = ''
      this.response = {}
      this.closeRecommendationDialog()
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
        params.append('includePreviousEdition', String(this.includePreviousEdition))
        const res = await fetch('/api/football/recommendation-backtest/jobs?' + params.toString(), {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            modelFactorsByCompetition: this.buildBacktestModelFactorsPayload(selectedCompetitions)
          })
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
          return
        }
        hitMatches.push(match)
        const matchWinningOdds = this.getRecommendationOddsDetails(match)
          .filter(item => item.winning)
          .map(item => item.odds)
        winningSelectionCount += matchWinningOdds.length
        const winningOdds = matchWinningOdds.reduce((sum, odds) => sum + odds, 0)
        winningMatchOdds.push(winningOdds)
      })
      const missMatchCount = recommendedMatches.length - hitMatches.length
      const financials = calculateFlatStakeBacktest(
        winningMatchOdds,
        recommendedSelectionCount,
        recommendedMatches.length
      )
      const totalMatchCount = Number(this.backtestSummary.completedMatchCount) || this.backtestSourceMatches.length
      this.backtestMatches = recommendedMatches
      this.backtestSummary = {
        ...this.backtestSummary,
        samplingRate: calculateSamplingRate(recommendedMatches.length, totalMatchCount),
        recommendedMatchCount: recommendedMatches.length,
        recommendedSelectionCount,
        hitMatchCount: hitMatches.length,
        missMatchCount,
        winningSelectionCount,
        averageWinningOdds: this.calculateAverageOdds(winningMatchOdds),
        averageOddsIncludingMisses: financials.averageReturnIncludingMisses,
        totalStake: financials.totalStake,
        totalReturn: financials.totalReturn,
        netProfit: financials.netProfit,
        roi: financials.roi
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
    async setIncludePreviousEdition(includePreviousEdition) {
      const nextValue = includePreviousEdition === true
      if (this.includePreviousEdition === nextValue) {
        return
      }
      this.storeActiveParameterProfile()
      this.includePreviousEdition = nextValue
      this.loadActiveParameterProfile()
      if (this.backtestActive) {
        this.clearBacktestResults()
        this.response = {}
      }
      this.saveUserConfig()
      await this.loadOverview(this.queryDate)
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
      const fallbackCode = concreteCompetitions[0] ? concreteCompetitions[0].code : 'WORLD_CUP'
      const selectedCode = value === 'ALL'
        ? fallbackCode
        : value.split(',').map(code => code.trim()).find(code => validCodes.has(code))
      this.activeCompetitions = [selectedCode || fallbackCode]
    },
    saveActiveCompetition() {
      const value = this.getSelectedCompetitionCodes()[0] || 'WORLD_CUP'
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
      const defaults = this.getActiveParameterPresetDefaults().globalParameters
      return {
        recommendationOdds: this.normalizeRecommendationOdds(
          this.recommendationOdds,
          defaults.recommendationOdds
        ),
        handicapRecommendationThreshold: this.normalizeRecommendationThreshold(
          this.handicapRecommendationThreshold,
          defaults.handicapRecommendationThreshold
        ),
        handicapReverseThreshold: this.normalizeRecommendationThreshold(
          this.handicapReverseThreshold,
          defaults.handicapReverseThreshold
        ),
        singleRecommendationThreshold: this.normalizeRecommendationThreshold(
          this.singleRecommendationThreshold,
          defaults.singleRecommendationThreshold
        )
      }
    },
    normalizeRecommendationOdds(value, fallback = DEFAULT_RECOMMENDATION_ODDS) {
      const numberValue = Number(value)
      if (!Number.isFinite(numberValue)) {
        return fallback
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
    formatRecommendationOddsValue(value, fallback = this.getActiveParameterPresetDefaults().globalParameters.recommendationOdds) {
      return this.normalizeRecommendationOdds(value, fallback).toFixed(2)
    },
    formatRecommendationThresholdValue(value, fallback) {
      return this.normalizeRecommendationThreshold(value, fallback).toFixed(2)
    },
    normalizeRecommendationThresholdInputs() {
      const defaults = this.getActiveParameterPresetDefaults().globalParameters
      this.handicapRecommendationThreshold = this.formatRecommendationThresholdValue(
        this.handicapRecommendationThreshold,
        defaults.handicapRecommendationThreshold
      )
      this.handicapReverseThreshold = this.formatRecommendationThresholdValue(
        this.handicapReverseThreshold,
        defaults.handicapReverseThreshold
      )
      this.singleRecommendationThreshold = this.formatRecommendationThresholdValue(
        this.singleRecommendationThreshold,
        defaults.singleRecommendationThreshold
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
          officialMatchWeight: this.formatModelFactorValue(parsedValue.officialMatchWeight, DEFAULT_OFFICIAL_MATCH_WEIGHT, 'officialMatchWeight'),
          internationalFriendlyWeight: this.formatModelFactorValue(parsedValue.internationalFriendlyWeight, DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT, 'internationalFriendlyWeight'),
          clubFriendlyWeight: this.formatModelFactorValue(parsedValue.clubFriendlyWeight, DEFAULT_CLUB_FRIENDLY_WEIGHT, 'clubFriendlyWeight'),
          handicapSmoothingFactor: this.formatModelFactorValue(parsedValue.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor')
        }
      } catch (error) {
        this.modelFactors = {
          hostTeamGoalFactor: DEFAULT_HOST_TEAM_GOAL_FACTOR.toFixed(2),
          homeTeamGoalFactor: DEFAULT_HOME_TEAM_GOAL_FACTOR.toFixed(2),
          seedTeamGoalFactor: DEFAULT_SEED_TEAM_GOAL_FACTOR.toFixed(2),
          officialMatchWeight: DEFAULT_OFFICIAL_MATCH_WEIGHT.toFixed(2),
          internationalFriendlyWeight: DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT.toFixed(2),
          clubFriendlyWeight: DEFAULT_CLUB_FRIENDLY_WEIGHT.toFixed(2),
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
        officialMatchWeight: Number(this.formatModelFactorValue(this.modelFactors.officialMatchWeight, DEFAULT_OFFICIAL_MATCH_WEIGHT, 'officialMatchWeight')),
        internationalFriendlyWeight: Number(this.formatModelFactorValue(this.modelFactors.internationalFriendlyWeight, DEFAULT_INTERNATIONAL_FRIENDLY_WEIGHT, 'internationalFriendlyWeight')),
        clubFriendlyWeight: Number(this.formatModelFactorValue(this.modelFactors.clubFriendlyWeight, DEFAULT_CLUB_FRIENDLY_WEIGHT, 'clubFriendlyWeight')),
        handicapSmoothingFactor: Number(this.formatModelFactorValue(this.modelFactors.handicapSmoothingFactor, DEFAULT_HANDICAP_SMOOTHING_FACTOR, 'handicapSmoothingFactor'))
      }
    },
    buildUserConfigPayload() {
      this.storeActiveParameterProfile()
      const normalizedProfiles = this.normalizeParameterProfiles(this.parameterProfiles)
      this.parameterProfiles = normalizedProfiles
      return {
        modelMode: 'after',
        includePreviousEdition: this.includePreviousEdition,
        parameterProfiles: normalizedProfiles,
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
    async saveModelFactorInput(key) {
      const fallback = this.getDefaultModelFactor(key)
      this.$set(this.modelFactors, key, this.formatModelFactorValue(this.modelFactors[key], fallback, key))
      this.saveModelFactors()
      if (this.queryDate) {
        await this.loadPredictions()
      }
    },
    async toggleParameterPreset() {
      this.storeActiveParameterProfile()
      const targetPresetName = this.activeParameterPreset === 'aggressive' ? 'stable' : 'aggressive'
      this.activeParameterPreset = targetPresetName
      this.loadActiveParameterProfile()
      await this.persistUserConfig()
      if (this.backtestActive) {
        this.clearBacktestResults()
        this.response = {}
      }
      if (this.queryDate) {
        await this.loadPredictions()
      }
    },
    getActiveParameterPresetDefaults() {
      const presetName = this.activeParameterPreset === 'aggressive' ? 'AGGRESSIVE' : 'STABLE'
      return createDefaultParameterProfile(presetName)
    },
    getDefaultModelFactor(key) {
      const defaults = this.getActiveParameterPresetDefaults().modelFactors
      return defaults[key] ?? defaults.hostTeamGoalFactor
    },
    getModelFactorMin(key) {
      if (key === 'handicapSmoothingFactor') {
        return HANDICAP_SMOOTHING_MIN
      }
      return MATCH_TYPE_WEIGHT_KEYS.includes(key) ? MATCH_TYPE_WEIGHT_MIN : MODEL_FACTOR_MIN
    },
    getModelFactorMax(key) {
      if (key === 'handicapSmoothingFactor') {
        return HANDICAP_SMOOTHING_MAX
      }
      return MATCH_TYPE_WEIGHT_KEYS.includes(key) ? MATCH_TYPE_WEIGHT_MAX : MODEL_FACTOR_MAX
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
      const globalParameters = this.getMatchGlobalParameters(match)
      const threshold = this.normalizeRecommendationThreshold(
        globalParameters.singleRecommendationThreshold,
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
        const pairSwitchKeys = this.buildHandicapPairSwitchKeys(match, normalRows[0], handicapRows[0], selectedRows)
        if (pairSwitchKeys) {
          return pairSwitchKeys
        }
        return this.buildRecommendationKeys(match, selectedRows, true)
      }
      if (handicapRows.length > 0) {
        return this.buildRecommendationKeys(match, handicapRows, false)
      }
      return this.buildRecommendationKeys(match, normalRows, false)
    },
    hasQualifiedRecommendationOdds(match, recommendationKeys) {
      if (!recommendationKeys || recommendationKeys.size === 0) {
        return false
      }
      const threshold = this.normalizeRecommendationOdds(
        this.getMatchGlobalParameters(match).recommendationOdds
      )
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
    buildRecommendationKeys(match, rows, applyHandicapThreshold) {
      const maxCell = this.findMaxProbabilityCell(rows)
      if (!maxCell) {
        return new Set()
      }
      const globalParameters = this.getMatchGlobalParameters(match)
      const reverseThreshold = this.normalizeRecommendationThreshold(
        globalParameters.handicapReverseThreshold,
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
    buildHandicapPairSwitchKeys(match, normalRow, handicapRow, rows) {
      const maxCell = this.findMaxProbabilityCell(rows)
      if (!maxCell || maxCell.row !== normalRow) {
        return null
      }
      if (maxCell.probabilityKey !== 'win' && maxCell.probabilityKey !== 'lose') {
        return null
      }

      const handicapValue = Number(handicapRow.probability[maxCell.probabilityKey]) || 0
      const globalParameters = this.getMatchGlobalParameters(match)
      const recommendationThreshold = this.normalizeRecommendationThreshold(
        globalParameters.handicapRecommendationThreshold,
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

.competition-option-list {
  max-height: 270px;
  overflow-y: auto;
  padding: 6px;
}

.competition-option {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
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

.competition-option-check {
  color: #2563eb;
  font-size: 13px;
  font-weight: 900;
}

.hero-card {
  align-self: stretch;
  display: grid;
  grid-template-columns: 230px 774px;
  align-items: stretch;
  gap: 14px;
  justify-content: center;
  width: 1044px;
  min-width: 0;
  max-width: 100%;
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  text-align: center;
}

.hero-summary-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.hero-number {
  font-size: 24px;
  line-height: 1;
  font-weight: 800;
}

.hero-label {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4px;
  font-size: 14px;
}

.help-icon {
  position: relative;
  z-index: 20;
  display: inline-block;
  width: 10px;
  height: 10px;
  flex: 0 0 10px;
  align-self: center;
  margin-right: 3px;
  border: 0;
  color: transparent;
  background-color: transparent;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='%23bfdbfe'%3E%3Cpath d='M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16m.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2'/%3E%3C/svg%3E");
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
  cursor: default;
  filter: drop-shadow(0 1px 2px rgba(15, 23, 42, 0.34));
  font-size: 0;
  font-style: normal;
  line-height: 1;
  vertical-align: middle;
}

.help-icon::after {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  width: 220px;
  padding: 7px 9px;
  border: 1px solid rgba(148, 163, 184, 0.52);
  border-radius: 7px;
  color: #e2e8f0;
  background: rgba(15, 23, 42, 0.97);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.28);
  content: attr(data-tooltip);
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
  line-height: 1.45;
  opacity: 0;
  overflow-wrap: anywhere;
  pointer-events: none;
  text-align: left;
  transform: translate(-50%, 4px);
  transition: opacity 120ms ease, transform 120ms ease, visibility 120ms ease;
  visibility: hidden;
  white-space: normal;
}

.help-icon:hover {
  z-index: 1000;
}

.help-icon:focus-visible {
  z-index: 1000;
  outline: none;
  box-shadow: 0 0 0 2px rgba(191, 219, 254, 0.52);
}

.help-icon:hover::after,
.help-icon:focus-visible::after {
  opacity: 1;
  transform: translate(-50%, 0);
  visibility: visible;
}

.hero-small {
  margin: auto 0;
  font-size: 12px;
  opacity: 0.78;
  transform: translateY(-2px);
}

.backtest-range-toggle {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  margin-top: 0;
  padding: 0 3px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.22);
  transform: translateY(-1px);
}

.backtest-range-toggle button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.78);
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
}

.backtest-range-toggle button.is-active {
  color: #1d4ed8;
  background: #ffffff;
}

.backtest-range-toggle button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.hero-actions {
  display: grid;
  margin-top: 5px;
  margin-bottom: 3px;
}

.refresh-data-button {
  white-space: nowrap;
}

.factor-controls {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 20px;
  min-width: 0;
  padding-left: 14px;
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  text-align: left;
}

.factor-control-column {
  display: grid;
  grid-template-rows: repeat(4, 24px);
  align-content: end;
  gap: 8px;
  min-width: 0;
  padding: 0 12px;
}

.factor-control-column + .factor-control-column {
  border-left: 1px solid rgba(255, 255, 255, 0.2);
}

.factor-control-column > * {
  transform: translateY(-3px);
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

.factor-control > span:first-child {
  display: flex;
  align-items: center;
}

.factor-control input[type="number"] {
  width: 78px;
  height: 24px;
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
  grid-template-columns: minmax(0, 1fr) 12px;
  gap: 4px;
  align-items: center;
  min-width: 0;
}

.percentage-input input[type="number"] {
  width: 100%;
  min-width: 0;
}

.factor-control .percentage-suffix {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
}

.factor-control .percentage-suffix.odds-suffix {
  font-size: 10px;
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

.factor-column-action {
  align-self: end;
  margin-bottom: 0;
}

.global-parameter-card {
  flex: 0 0 230px;
  align-self: stretch;
  display: grid;
  grid-template-rows: 1fr auto;
  gap: 8px;
  width: 230px;
  min-width: 230px;
  padding: 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  text-align: left;
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
  transform: translateY(2px);
}

.backtest-average-grid > div {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 4px;
  align-items: center;
  min-width: 0;
  height: 28px;
  padding: 2px 8px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.08);
}

.backtest-average-grid > .backtest-roi-card {
  grid-column: 1 / -1;
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

.backtest-average-grid > div > span {
  display: flex;
  align-items: center;
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

.factor-control.is-competition-disabled input:disabled {
  cursor: not-allowed;
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

@media (max-width: 1650px) {
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
    grid-template-columns: 230px minmax(0, 1fr);
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

  .factor-control-column {
    padding: 0;
  }

  .help-icon::after {
    left: -8px;
    transform: translate(0, 4px);
  }

  .help-icon:hover::after,
  .help-icon:focus-visible::after {
    transform: translate(0, 0);
  }

  .factor-control-column + .factor-control-column {
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

  .percentage-input {
    grid-template-columns: minmax(0, 1fr) auto;
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
