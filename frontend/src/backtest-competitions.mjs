export const BACKTEST_COMPETITIONS = Object.freeze([
  { code: 'WORLD_CUP', name: '世界杯' },
  { code: 'EUROPEAN_CHAMPIONSHIP', name: '欧洲杯' },
  { code: 'COPA_AMERICA', name: '美洲杯' },
  { code: 'CLUB_WORLD_CUP', name: '世俱杯' },
  { code: 'EUROPA_LEAGUE', name: '欧罗巴' },
  { code: 'CHAMPIONS_LEAGUE', name: '欧冠' },
  { code: 'PREMIER_LEAGUE', name: '英超' },
  { code: 'LA_LIGA', name: '西甲' },
  { code: 'BUNDESLIGA', name: '德甲' },
  { code: 'SERIE_A', name: '意甲' },
  { code: 'LIGUE_1', name: '法甲' },
  { code: 'PRIMEIRA_LIGA', name: '葡超' },
  { code: 'EREDIVISIE', name: '荷甲' },
  { code: 'ARGENTINE_PRIMERA_DIVISION', name: '阿甲' },
  { code: 'SWEDISH_ALLSVENSKAN', name: '瑞超' },
  { code: 'FINNISH_VEIKKAUSLIIGA', name: '芬超' },
  { code: 'K_LEAGUE_1', name: '韩职' }
].map(competition => Object.freeze(competition)))

const competitionCodes = new Set(BACKTEST_COMPETITIONS.map(competition => competition.code))

export function isBacktestCompetition(competition) {
  return competitionCodes.has(competition)
}

// 清理旧配置和检查点中的非回测赛事，保留其余参数原值
export function filterBacktestProfiles(profiles) {
  return Object.fromEntries(Object.entries(profiles || {})
    .filter(([key]) => isBacktestCompetition(key.split(':')[0])))
}
