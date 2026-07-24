import crypto from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historicalMatchesPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const teamNameMappingsPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')
const cacheRoot = path.join(root, 'target/supplemental-history-cache')
const MINIMUM_HISTORY_DATE = '2014-10-22'

const EXCLUDED_COMPETITIONS = new Set(['BRAZIL_SERIE_A'])

const EXCLUDED_SOURCE_COMPETITIONS = new Set([
  '巴甲',
  '巴乙',
  '巴西乙',
  '巴西乙级联赛',
  '巴西杯',
  '巴东北',
  '巴东北杯',
  '巴西东北杯',
  '圣保罗锦',
  '圣保罗州锦标赛',
  'BRAZIL SERIE B',
  'CAMPEONATO BRASILEIRO SERIE B',
  'CAMPEONATO PAULISTA',
  'PAULISTA A1',
  'COPA DO BRASIL',
  'COPA DO NORDESTE'
])

const HISTORY_HEADERS = [
  'match_id',
  'match_date',
  'competition',
  'home_team_cn',
  'away_team_cn',
  'home_score',
  'away_score',
  'neutral',
  'match_type',
  'source_competition'
]

const MAPPING_HEADERS = [
  'competition',
  'standard_team_name',
  'alias_team_name',
  'alias_type',
  'source',
  'last_seen_date'
]

const MAPPING_SOURCE_PRIORITY = new Map([
  ['HISTORICAL_MATCHES', 1],
  ['HISTORICAL_ODDS', 2],
  ['INFERRED_DUPLICATE', 3],
  ['VERIFIED_ALIAS', 4],
  ['MANUAL', 5],
  ['VERIFIED_SPORTTERY', 6]
])

const SOURCE_COMPETITION_ALIASES = new Map([
  ['韩足总杯', '韩国杯'],
  ['韩国足总杯', '韩国杯']
])

const NATIONAL_COMPETITIONS = new Set([
  'WORLD_CUP',
  'EUROPEAN_CHAMPIONSHIP',
  'COPA_AMERICA'
])

const CLUB_COMPETITIONS = new Set([
  'CLUB_WORLD_CUP',
  'EUROPA_LEAGUE',
  'CHAMPIONS_LEAGUE',
  'PREMIER_LEAGUE',
  'LA_LIGA',
  'BUNDESLIGA',
  'SERIE_A',
  'LIGUE_1',
  'PRIMEIRA_LIGA',
  'EREDIVISIE',
  'ARGENTINE_PRIMERA_DIVISION',
  'SWEDISH_ALLSVENSKAN',
  'FINNISH_VEIKKAUSLIIGA',
  'K_LEAGUE_1'
])

const ALL_CLUB_COMPETITIONS = new Set([
  ...CLUB_COMPETITIONS,
  'CLUB_OFFICIAL_OTHER',
  'CLUB_FRIENDLY'
])

const COMPETITION_NAMES = new Map(Object.entries({
  WORLD_CUP: '世界杯',
  EUROPEAN_CHAMPIONSHIP: '欧洲杯',
  COPA_AMERICA: '美洲杯',
  INTERNATIONAL_OFFICIAL: '国家队其他正式比赛',
  INTERNATIONAL_FRIENDLY: '国家队国际窗口友谊赛',
  CLUB_WORLD_CUP: '世俱杯',
  CLUB_OFFICIAL_OTHER: '俱乐部其他正式比赛',
  CLUB_FRIENDLY: '俱乐部正常阵容友谊赛',
  EUROPA_LEAGUE: '欧罗巴',
  CHAMPIONS_LEAGUE: '欧冠',
  PREMIER_LEAGUE: '英超',
  LA_LIGA: '西甲',
  BUNDESLIGA: '德甲',
  SERIE_A: '意甲',
  LIGUE_1: '法甲',
  PRIMEIRA_LIGA: '葡超',
  EREDIVISIE: '荷甲',
  ARGENTINE_PRIMERA_DIVISION: '阿甲',
  SWEDISH_ALLSVENSKAN: '瑞超',
  FINNISH_VEIKKAUSLIIGA: '芬超',
  K_LEAGUE_1: '韩职'
}))

function isExcludedCompetition(competition, sourceCompetition) {
  return EXCLUDED_COMPETITIONS.has(String(competition ?? '').trim().toUpperCase())
    || EXCLUDED_SOURCE_COMPETITIONS.has(
      String(sourceCompetition ?? '').trim().toUpperCase()
    )
}

const NATIONAL_TOURNAMENT_NAMES = new Map(Object.entries({
  Friendly: '国家队国际窗口友谊赛',
  'FIFA World Cup': '世界杯',
  'FIFA World Cup qualification': '世界杯预选赛',
  'UEFA Euro': '欧洲杯',
  'UEFA Euro qualification': '欧洲杯预选赛',
  'UEFA Nations League': '欧国联',
  'Copa América': '美洲杯',
  'CONCACAF Nations League': '中北美洲及加勒比海国家联赛',
  'CONCACAF Championship': '中北美洲及加勒比海锦标赛',
  'Gold Cup': '中北美洲及加勒比海金杯赛',
  'African Cup of Nations': '非洲杯',
  'African Cup of Nations qualification': '非洲杯预选赛',
  'AFC Asian Cup': '亚洲杯',
  'AFC Asian Cup qualification': '亚洲杯预选赛',
  'Oceania Nations Cup': '大洋洲国家杯',
  'Confederations Cup': '联合会杯'
}))

const ESPN_PRIMARY_SOURCES = [
  ['fifa.cwc', 'CLUB_WORLD_CUP'],
  ['uefa.europa', 'EUROPA_LEAGUE'],
  ['uefa.europa_qual', 'EUROPA_LEAGUE'],
  ['uefa.champions', 'CHAMPIONS_LEAGUE'],
  ['uefa.champions_qual', 'CHAMPIONS_LEAGUE'],
  ['eng.1', 'PREMIER_LEAGUE'],
  ['esp.1', 'LA_LIGA'],
  ['ita.1', 'SERIE_A'],
  ['ger.1', 'BUNDESLIGA'],
  ['fra.1', 'LIGUE_1'],
  ['por.1', 'PRIMEIRA_LIGA'],
  ['ned.1', 'EREDIVISIE'],
  ['arg.1', 'ARGENTINE_PRIMERA_DIVISION']
].map(([slug, competition]) => ({
  slug,
  sourceCompetition: COMPETITION_NAMES.get(competition),
  competition,
  matchType: 'OFFICIAL',
  monthly: false
}))

const ESPN_OFFICIAL_SOURCES = [
  ['uefa.europa.conf', '欧协联'],
  ['uefa.europa.conf_qual', '欧协联资格赛'],
  ['uefa.super_cup', '欧洲超级杯'],
  ['eng.fa', '英格兰足总杯'],
  ['eng.league_cup', '英格兰联赛杯'],
  ['eng.charity', '英格兰社区盾'],
  ['esp.copa_del_rey', '西班牙国王杯'],
  ['esp.super_cup', '西班牙超级杯'],
  ['ger.dfb_pokal', '德国杯'],
  ['ger.super_cup', '德国超级杯'],
  ['ita.coppa_italia', '意大利杯'],
  ['ita.super_cup', '意大利超级杯'],
  ['fra.coupe_de_france', '法国杯'],
  ['fra.super_cup', '法国超级杯'],
  ['por.taca.portugal', '葡萄牙杯'],
  ['ned.cup', '荷兰杯'],
  ['ned.supercup', '荷兰超级杯'],
  ['arg.copa', '阿根廷杯'],
  ['conmebol.libertadores', '解放者杯'],
  ['conmebol.sudamericana', '南美杯'],
  ['conmebol.recopa', '南美优胜者杯'],
  ['fifa.intercontinental_cup', '洲际杯']
].map(([slug, sourceCompetition]) => ({
  slug,
  sourceCompetition,
  competition: 'CLUB_OFFICIAL_OTHER',
  matchType: 'OFFICIAL',
  monthly: false
}))

const ESPN_FRIENDLY_SOURCES = [
  ['club.friendly', '俱乐部赛', true],
  ['global.champs_cup', '国际冠军杯', false],
  ['friendly.emirates_cup', '酋长杯', false],
  ['eng.asia_trophy', '英超亚洲杯', false],
  ['esp.joan_gamper', '甘伯杯', false]
].map(([slug, sourceCompetition, monthly]) => ({
  slug,
  sourceCompetition,
  competition: 'CLUB_FRIENDLY',
  matchType: 'CLUB_FRIENDLY',
  monthly
}))

const FOTMOB_LEAGUE_SOURCES = [
  {
    leagueId: '489',
    competition: 'CLUB_FRIENDLY',
    matchType: 'CLUB_FRIENDLY',
    sourceCompetition: '俱乐部赛',
    calendarYearSeason: true,
    firstSeasonStartYear: 2026
  },
  {
    leagueId: '180',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '联赛杯',
    calendarYearSeason: false
  },
  {
    leagueId: '168',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '瑞甲',
    calendarYearSeason: true
  },
  {
    leagueId: '172',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: 'Play-offs 1/2',
    calendarYearSeason: true
  },
  {
    leagueId: '525',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '亚冠精英',
    calendarYearSeason: false,
    crossYearSeasonFrom: 2023
  },
  {
    leagueId: '9116',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '韩挑战联',
    calendarYearSeason: true
  },
  {
    leagueId: '9551',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '韩国杯',
    calendarYearSeason: true,
    firstSeasonStartYear: 2015
  },
  {
    leagueId: '262',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '阿塞超',
    calendarYearSeason: false
  },
  {
    leagueId: '51',
    competition: 'FINNISH_VEIKKAUSLIIGA',
    matchType: 'OFFICIAL',
    sourceCompetition: '芬超',
    calendarYearSeason: true
  },
  {
    leagueId: '9080',
    competition: 'K_LEAGUE_1',
    matchType: 'OFFICIAL',
    sourceCompetition: '韩职',
    calendarYearSeason: true
  },
  {
    leagueId: '67',
    competition: 'SWEDISH_ALLSVENSKAN',
    matchType: 'OFFICIAL',
    sourceCompetition: '瑞超',
    calendarYearSeason: true
  },
  {
    leagueId: '57',
    competition: 'EREDIVISIE',
    matchType: 'OFFICIAL',
    sourceCompetition: '荷甲',
    calendarYearSeason: false
  },
  {
    leagueId: '10216',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '欧协联',
    calendarYearSeason: false,
    firstSeasonStartYear: 2021
  },
  {
    leagueId: '10615',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '欧协联资格赛',
    calendarYearSeason: false,
    firstSeasonStartYear: 2021
  },
  {
    leagueId: '40',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '比甲',
    calendarYearSeason: false
  },
  {
    leagueId: '149',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '比利时杯',
    calendarYearSeason: false
  },
  {
    leagueId: '164',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '瑞士杯',
    calendarYearSeason: false
  },
  {
    leagueId: '69',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '瑞士超',
    calendarYearSeason: false
  },
  {
    leagueId: '271',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '保杯',
    calendarYearSeason: false
  },
  {
    leagueId: '270',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '保超',
    calendarYearSeason: false
  },
  {
    leagueId: '126',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '爱超',
    calendarYearSeason: true
  },
  {
    leagueId: '182',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '塞超',
    calendarYearSeason: false
  },
  {
    leagueId: '183',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '塞杯',
    calendarYearSeason: false
  },
  {
    leagueId: '176',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '斯洛伐超',
    calendarYearSeason: false
  },
  {
    leagueId: '177',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '斯洛伐杯',
    calendarYearSeason: false
  },
  {
    leagueId: '229',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '卢森联',
    calendarYearSeason: false
  },
  {
    leagueId: '9527',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '卢森杯',
    calendarYearSeason: false
  },
  {
    leagueId: '250',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '法罗超',
    calendarYearSeason: true
  },
  {
    leagueId: '9523',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '法罗杯',
    calendarYearSeason: true
  },
  {
    leagueId: '232',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '黑山甲',
    calendarYearSeason: false
  },
  {
    leagueId: '215',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '冰超',
    calendarYearSeason: true
  },
  {
    leagueId: '217',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '冰岛杯',
    calendarYearSeason: true
  },
  {
    leagueId: '116',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '威尔士超',
    calendarYearSeason: false
  }
]

const SOCCERWAY_KOREA_CUP_SOURCE = {
  competition: 'CLUB_OFFICIAL_OTHER',
  matchType: 'OFFICIAL',
  sourceCompetition: '韩国杯',
  firstSeasonStartYear: 2014
}

const SOFASCORE_CLUB_FRIENDLY_SOURCE = {
  tournamentId: '853',
  competition: 'CLUB_FRIENDLY',
  matchType: 'CLUB_FRIENDLY',
  sourceCompetition: '俱乐部友谊赛'
}

const FUTBOL24_SOURCES = [
  {
    leagueId: '472',
    competition: 'CLUB_FRIENDLY',
    matchType: 'CLUB_FRIENDLY',
    sourceCompetition: '俱乐部友谊赛'
  },
  {
    leagueId: '525',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '阿塞杯',
    seasonPath: 'national/Azerbaijan/Kubok',
    crossYearSeason: true
  },
  {
    leagueId: '322',
    competition: 'FINNISH_VEIKKAUSLIIGA',
    matchType: 'OFFICIAL',
    sourceCompetition: '芬超',
    seasonPath: 'national/Finland/Veikkausliiga',
    crossYearSeason: false
  },
  {
    leagueId: '324',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '芬兰杯',
    seasonPath: 'national/Finland/Suomen-Cup',
    crossYearSeason: false
  },
  {
    leagueId: '28',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '丹超',
    seasonPath: 'national/Denmark/Superligaen',
    crossYearSeason: true
  },
  {
    leagueId: '297',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '波超杯',
    seasonPath: 'national/Poland/Super-Cup',
    crossYearSeason: false
  },
  {
    leagueId: '107',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '波甲',
    seasonPath: 'national/Poland/Ekstraklasa',
    crossYearSeason: true
  },
  {
    leagueId: '15',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '奥甲',
    seasonPath: 'national/Austria/Bundesliga',
    crossYearSeason: true
  },
  {
    leagueId: '51',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '苏超',
    seasonPath: 'national/Scotland/Premiership',
    crossYearSeason: true
  },
  {
    leagueId: '133',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '土超',
    seasonPath: 'national/Turkiye/Super-Lig',
    crossYearSeason: true
  },
  {
    leagueId: '537',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '土耳其杯',
    seasonPath: 'national/Turkiye/Turkiye-Kupasi',
    crossYearSeason: true
  },
  {
    leagueId: '33',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '丹麦杯',
    seasonPath: 'national/Denmark/DBUs-Landspokal',
    crossYearSeason: true
  },
  {
    leagueId: '92',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '匈甲',
    seasonPath: 'national/Hungary/NB-I',
    crossYearSeason: true
  },
  {
    leagueId: '531',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '匈牙利杯',
    seasonPath: 'national/Hungary/Magyar-Kupa',
    crossYearSeason: true
  },
  {
    leagueId: '26',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '克甲',
    seasonPath: 'national/Croatia/1-HNL',
    crossYearSeason: true
  },
  {
    leagueId: '75',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '塞浦甲',
    seasonPath: 'national/Cyprus/1-Division',
    crossYearSeason: true
  },
  {
    leagueId: '269',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '哈萨超',
    seasonPath: 'national/Kazakhstan/Super-League',
    crossYearSeason: false
  },
  {
    leagueId: '70',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '威联杯',
    seasonPath: 'national/Wales/League-Cup',
    crossYearSeason: true
  },
  {
    leagueId: '534',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '塞杯',
    seasonPath: 'national/Serbia/Kup',
    crossYearSeason: true
  },
  {
    leagueId: '868',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '卢森杯',
    seasonPath: 'national/Luxemburg/Coupe-de-Luxembourg',
    crossYearSeason: true
  },
  {
    leagueId: '291',
    competition: 'CLUB_OFFICIAL_OTHER',
    matchType: 'OFFICIAL',
    sourceCompetition: '法罗杯',
    seasonPath: 'national/Faroe-Islands/Logmanssteypid',
    crossYearSeason: false
  }
]

const VERIFIED_SUPPLEMENTAL_ROWS = [
  {
    provider: 'SOFASCORE',
    providerId: '16411586',
    source: 'VERIFIED-SOFASCORE',
    competition: 'CLUB_FRIENDLY',
    matchType: 'CLUB_FRIENDLY',
    sourceCompetition: '俱乐部友谊赛',
    matchDate: '2026-07-02',
    homeTeam: 'Polissya Zhytomyr',
    awayTeam: 'Sabah FK',
    homeScore: 4,
    awayScore: 1,
    neutral: true
  },
  {
    provider: 'PFL', providerId: '5384', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-12', homeTeam: 'Şahdağ Qusar', awayTeam: 'Kür-Araz',
    homeScore: 3, awayScore: 1, neutral: false
  },
  {
    provider: 'PFL', providerId: '5451', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-12', homeTeam: 'Şəfa', awayTeam: 'Araz Saatlı',
    homeScore: 2, awayScore: 0, neutral: false
  },
  {
    provider: 'PFL', providerId: '5455', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-13', homeTeam: 'Şimal', awayTeam: 'Qusar',
    homeScore: 4, awayScore: 1, neutral: false
  },
  {
    provider: 'PFL', providerId: '5461', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-13', homeTeam: 'Göygöl', awayTeam: 'Quba',
    homeScore: 2, awayScore: 2, neutral: false
  },
  {
    provider: 'PFL', providerId: '5463', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-13', homeTeam: 'Lerik', awayTeam: 'Ağdaş',
    homeScore: 0, awayScore: 4, neutral: false
  },
  {
    provider: 'PFL', providerId: '5465', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-13', homeTeam: 'Sheki City', awayTeam: 'Şəmkir',
    homeScore: 0, awayScore: 1, neutral: false
  },
  {
    provider: 'PFL', providerId: '5468', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2024-10-14', homeTeam: 'Dinamo', awayTeam: 'Füzuli',
    homeScore: 4, awayScore: 0, neutral: false
  },
  {
    provider: 'PFL', providerId: '6705', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2025-10-09', homeTeam: 'Sheki City', awayTeam: 'Araz Saatlı',
    homeScore: 3, awayScore: 2, neutral: false
  },
  {
    provider: 'PFL', providerId: '6706', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2025-10-09', homeTeam: 'Qaradağ L.', awayTeam: 'Ağdaş',
    homeScore: 8, awayScore: 0, neutral: false
  },
  {
    provider: 'PFL', providerId: '6707', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2025-10-09', homeTeam: 'Dinamo', awayTeam: 'Kür-Araz',
    homeScore: 2, awayScore: 2, neutral: false
  },
  {
    provider: 'PFL', providerId: '6708', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2025-10-09', homeTeam: 'Ağstafa Gəncləri', awayTeam: 'Şirvan',
    homeScore: 1, awayScore: 0, neutral: false
  },
  {
    provider: 'PFL', providerId: '6704', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2025-10-10', homeTeam: 'Göygöl', awayTeam: 'Şəmkir',
    homeScore: 1, awayScore: 0, neutral: false
  },
  {
    provider: 'PFL', providerId: '6709', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞杯',
    matchDate: '2025-10-10', homeTeam: 'Quba', awayTeam: 'Xankəndi',
    homeScore: 1, awayScore: 2, neutral: false
  },
  {
    provider: 'PFL', providerId: '6777', source: 'VERIFIED-PFL',
    competition: 'CLUB_OFFICIAL_OTHER', matchType: 'OFFICIAL', sourceCompetition: '阿塞超',
    matchDate: '2026-05-28', homeTeam: 'Qəbələ', awayTeam: 'Mingəçevir',
    homeScore: 2, awayScore: 0, neutral: false
  }
]


function parseArguments(argv) {
  const options = {
    write: false,
    compact: false,
    refreshCache: false,
    skipEspn: false,
    skipFotMob: false,
    skipSoccerway: false,
    skipFutbol24: false,
    skipSofaScore: false,
    skipNational: false,
    resetInferredMappings: false,
    onlySources: null,
    sourceRoot: null,
    historySourcePath: historicalMatchesPath,
    minDate: MINIMUM_HISTORY_DATE,
    maxDate: localDate(new Date()),
    concurrency: 8
  }
  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index]
    if (argument === '--write') {
      options.write = true
    } else if (argument === '--compact') {
      options.compact = true
    } else if (argument === '--refresh-cache') {
      options.refreshCache = true
    } else if (argument === '--skip-espn') {
      options.skipEspn = true
    } else if (argument === '--skip-fotmob') {
      options.skipFotMob = true
    } else if (argument === '--skip-soccerway') {
      options.skipSoccerway = true
    } else if (argument === '--skip-futbol24') {
      options.skipFutbol24 = true
    } else if (argument === '--skip-sofascore') {
      options.skipSofaScore = true
    } else if (argument === '--skip-national') {
      options.skipNational = true
    } else if (argument === '--reset-inferred-mappings') {
      options.resetInferredMappings = true
    } else if (argument === '--only-sources') {
      options.onlySources = new Set(requiredArgument(argv, ++index, argument)
        .split(',')
        .map(value => value.trim())
        .filter(Boolean))
    } else if (argument === '--source-root') {
      options.sourceRoot = path.resolve(requiredArgument(argv, ++index, argument))
    } else if (argument === '--history-source-path') {
      options.historySourcePath = path.resolve(requiredArgument(argv, ++index, argument))
    } else if (argument === '--min-date') {
      options.minDate = requiredArgument(argv, ++index, argument)
    } else if (argument === '--max-date') {
      options.maxDate = requiredArgument(argv, ++index, argument)
    } else if (argument === '--concurrency') {
      options.concurrency = Number(requiredArgument(argv, ++index, argument))
    } else {
      throw new Error(`未知参数：${argument}`)
    }
  }
  for (const [name, value] of [['--min-date', options.minDate], ['--max-date', options.maxDate]]) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      throw new Error(`${name} 必须使用 yyyy-MM-dd 格式：${value}`)
    }
  }
  if (options.minDate > options.maxDate) {
    throw new Error('--min-date 不能晚于 --max-date')
  }
  options.concurrency = Math.max(1, Math.min(16, Number.isFinite(options.concurrency)
    ? Math.floor(options.concurrency)
    : 8))
  return options
}

function sourceSelected(options, sourceKey) {
  return !options.onlySources || options.onlySources.has(sourceKey)
}

function requiredArgument(argv, index, optionName) {
  const value = argv[index]
  if (!value || value.startsWith('--')) {
    throw new Error(`${optionName} 缺少参数值`)
  }
  return value
}

function localDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let inQuotes = false
  for (let index = 0; index < text.length; index += 1) {
    const character = text[index]
    const nextCharacter = text[index + 1]
    if (inQuotes) {
      if (character === '"' && nextCharacter === '"') {
        field += '"'
        index += 1
      } else if (character === '"') {
        inQuotes = false
      } else {
        field += character
      }
    } else if (character === '"') {
      inQuotes = true
    } else if (character === ',') {
      row.push(field)
      field = ''
    } else if (character === '\n') {
      row.push(field.replace(/\r$/, ''))
      rows.push(row)
      row = []
      field = ''
    } else {
      field += character
    }
  }
  if (field.length > 0 || row.length > 0) {
    row.push(field.replace(/\r$/, ''))
    rows.push(row)
  }
  const headers = rows.shift().map((name, index) => (
    index === 0 ? name.replace(/^\uFEFF/, '') : name
  ))
  return rows
    .filter(values => values.length === headers.length)
    .map(values => Object.fromEntries(headers.map((name, index) => [name, values[index]])))
}

function escapeCsv(value) {
  const text = String(value ?? '')
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function toCsv(rows) {
  return [
    HISTORY_HEADERS.join(','),
    ...rows.map(row => HISTORY_HEADERS.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

function toMappingCsv(rows) {
  return [
    MAPPING_HEADERS.join(','),
    ...rows.map(row => MAPPING_HEADERS.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

function canonicalName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]/gu, '')
}

function canonicalClubName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]+/gu, ' ')
    .replace(/^(AFC|AC|AS|CA|CD|CF|FC|FK|NK|SC|SV|TSG)\s+/u, '')
    .replace(/\s+(AFC|AC|AS|CA|CD|CF|FC|FK|NK|SC|SV|TSG)$/u, '')
    .replaceAll(' ', '')
}

function nameKeys(value) {
  return [...new Set([canonicalName(value), canonicalClubName(value)].filter(Boolean))]
}

function canonicalChineseName(value) {
  return String(value ?? '')
    .normalize('NFKC')
    .toUpperCase()
    .replace(/[\s·•，,.'’`´()（）\[\]【】_\/&-]+/gu, '')
    .replace(/^(FC|SC|CF)(?=\p{Script=Han})/u, '')
    .replace(/(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$/u, '')
}

function normalizeHistoryRow(row) {
  const competition = String(row.competition ?? '').trim()
  const sourceCompetition = String(row.source_competition ?? '').trim()
    || COMPETITION_NAMES.get(competition)
    || competition
  const defaultType = competition === 'INTERNATIONAL_FRIENDLY'
    ? 'INTERNATIONAL_FRIENDLY'
    : competition === 'CLUB_FRIENDLY'
      ? 'CLUB_FRIENDLY'
      : 'OFFICIAL'
  return {
    match_id: String(row.match_id ?? '').trim(),
    match_date: String(row.match_date ?? '').trim(),
    competition,
    home_team_cn: String(row.home_team_cn ?? '').trim(),
    away_team_cn: String(row.away_team_cn ?? '').trim(),
    home_score: String(row.home_score ?? '').trim(),
    away_score: String(row.away_score ?? '').trim(),
    neutral: String(row.neutral ?? '').trim().toLowerCase(),
    match_type: String(row.match_type ?? '').trim() || defaultType,
    source_competition: SOURCE_COMPETITION_ALIASES.get(sourceCompetition)
      ?? sourceCompetition
  }
}

function buildMappings(mappingRows, allowedCompetitions) {
  const global = new Map()
  const globalSources = new Map()
  const byCompetition = new Map()
  const byCompetitionSources = new Map()
  const prioritizedRows = [...mappingRows].sort((left, right) => (
    (MAPPING_SOURCE_PRIORITY.get(left.source) ?? 0)
      - (MAPPING_SOURCE_PRIORITY.get(right.source) ?? 0)
    || String(left.last_seen_date ?? '').localeCompare(String(right.last_seen_date ?? ''))
  ))
  for (const row of prioritizedRows) {
    if (row.competition !== '*' && !allowedCompetitions.has(row.competition)) {
      continue
    }
    const aliasName = String(row.alias_team_name ?? '').trim()
    const standardName = String(row.standard_team_name ?? '').trim()
    if (!aliasName || !standardName) {
      continue
    }
    const mappings = row.competition === '*'
      ? global
      : byCompetition.get(row.competition) ?? new Map()
    const mappingSources = row.competition === '*'
      ? globalSources
      : byCompetitionSources.get(row.competition) ?? new Map()
    for (const key of nameKeys(aliasName)) {
      mappings.set(key, standardName)
      mappingSources.set(key, row.source)
    }
    if (row.competition !== '*') {
      byCompetition.set(row.competition, mappings)
      byCompetitionSources.set(row.competition, mappingSources)
    }
  }
  return { global, globalSources, byCompetition, byCompetitionSources }
}

function mappedNameEntry(sourceName, mappings, competition) {
  const competitionMappings = mappings.byCompetition.get(competition)
  const competitionSources = mappings.byCompetitionSources.get(competition)
  let selected = null
  for (const key of nameKeys(sourceName)) {
    for (const [standardName, source, scopePriority] of [
      [competitionMappings?.get(key), competitionSources?.get(key), 1],
      [mappings.global.get(key), mappings.globalSources.get(key), 0]
    ]) {
      if (!standardName) {
        continue
      }
      const priority = MAPPING_SOURCE_PRIORITY.get(source) ?? 0
      if (!selected
          || priority > selected.priority
          || priority === selected.priority && scopePriority > selected.scopePriority) {
        selected = { standardName, source, priority, scopePriority }
      }
    }
  }
  return selected
}

function mappedChineseName(sourceName, mappings, competition) {
  return mappedNameEntry(sourceName, mappings, competition)?.standardName ?? null
}

function mappedNameSource(sourceName, mappings, competition) {
  return mappedNameEntry(sourceName, mappings, competition)?.source ?? null
}

function extraTimeGoalCounts(goalRows) {
  const counts = new Map()
  for (const row of goalRows) {
    const minute = Number(row.minute)
    if (!Number.isFinite(minute) || minute <= 90) {
      continue
    }
    const key = [row.date, row.home_team, row.away_team, row.team].join('|')
    counts.set(key, (counts.get(key) ?? 0) + 1)
  }
  return counts
}

function regulationNationalScore(result, extraTimeGoals) {
  const homeExtraTimeGoals = extraTimeGoals.get([
    result.date,
    result.home_team,
    result.away_team,
    result.home_team
  ].join('|')) ?? 0
  const awayExtraTimeGoals = extraTimeGoals.get([
    result.date,
    result.home_team,
    result.away_team,
    result.away_team
  ].join('|')) ?? 0
  const homeScore = Number(result.home_score) - homeExtraTimeGoals
  const awayScore = Number(result.away_score) - awayExtraTimeGoals
  if (!Number.isInteger(homeScore) || !Number.isInteger(awayScore) || homeScore < 0 || awayScore < 0) {
    return null
  }
  return {
    homeScore,
    awayScore,
    correctedExtraTimeGoals: homeExtraTimeGoals + awayExtraTimeGoals
  }
}

function nationalCompetition(tournament) {
  if (tournament === 'FIFA World Cup') {
    return 'WORLD_CUP'
  }
  if (tournament === 'UEFA Euro') {
    return 'EUROPEAN_CHAMPIONSHIP'
  }
  if (tournament === 'Copa América') {
    return 'COPA_AMERICA'
  }
  return tournament === 'Friendly' ? 'INTERNATIONAL_FRIENDLY' : 'INTERNATIONAL_OFFICIAL'
}

function parseNationalRows(resultRows, goalRows) {
  const extraTimeGoals = extraTimeGoalCounts(goalRows)
  const rows = []
  for (const result of resultRows) {
    const score = regulationNationalScore(result, extraTimeGoals)
    if (!score) {
      continue
    }
    const competition = nationalCompetition(result.tournament)
    rows.push({
      provider: 'INTL',
      source: result.tournament === 'Friendly' ? 'INTL-FRIENDLY' : 'INTL-OFFICIAL',
      competition,
      matchType: result.tournament === 'Friendly' ? 'INTERNATIONAL_FRIENDLY' : 'OFFICIAL',
      sourceCompetition: NATIONAL_TOURNAMENT_NAMES.get(result.tournament) ?? result.tournament,
      matchDate: result.date,
      homeTeam: result.home_team,
      awayTeam: result.away_team,
      homeScore: score.homeScore,
      awayScore: score.awayScore,
      neutral: String(result.neutral ?? '').toLowerCase() === 'true',
      allowNationalFallback: true,
      correctedExtraTimeGoals: score.correctedExtraTimeGoals
    })
  }
  return rows
}

function monthRanges(minDate, maxDate) {
  const ranges = []
  const cursor = new Date(`${minDate.slice(0, 7)}-01T00:00:00Z`)
  while (cursor.toISOString().slice(0, 10) <= maxDate) {
    const year = cursor.getUTCFullYear()
    const month = cursor.getUTCMonth()
    const nextMonth = new Date(Date.UTC(year, month + 1, 1))
    const monthStart = `${year}-${String(month + 1).padStart(2, '0')}-01`
    const monthEndDate = new Date(nextMonth.getTime() - 86_400_000).toISOString().slice(0, 10)
    ranges.push({
      key: monthStart.slice(0, 7),
      start: monthStart < minDate ? minDate : monthStart,
      end: monthEndDate > maxDate ? maxDate : monthEndDate
    })
    cursor.setUTCMonth(cursor.getUTCMonth() + 1)
  }
  return ranges
}

function yearRanges(minDate, maxDate) {
  const ranges = []
  for (let year = Number(minDate.slice(0, 4)); year <= Number(maxDate.slice(0, 4)); year += 1) {
    const yearStart = `${year}-01-01`
    const yearEnd = `${year}-12-31`
    ranges.push({
      key: String(year),
      start: yearStart < minDate ? minDate : yearStart,
      end: yearEnd > maxDate ? maxDate : yearEnd
    })
  }
  return ranges
}

function basicDate(dateText) {
  return dateText.replaceAll('-', '')
}

async function fetchJsonWithRetry(url) {
  let lastError = null
  for (let attempt = 1; attempt <= 4; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: { 'User-Agent': 'lottery-football-history-importer/1.0' },
        signal: AbortSignal.timeout(45_000)
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      return await response.json()
    } catch (error) {
      lastError = error
      if (attempt < 4) {
        await new Promise(resolve => setTimeout(resolve, attempt * 750))
      }
    }
  }
  throw new Error(`${lastError?.message ?? '未知错误'}`)
}

async function fetchTextWithRetry(url) {
  let lastError = null
  for (let attempt = 1; attempt <= 4; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: { 'User-Agent': 'lottery-football-history-importer/1.0' },
        signal: AbortSignal.timeout(45_000)
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      return await response.text()
    } catch (error) {
      lastError = error
      if (attempt < 4) {
        await new Promise(resolve => setTimeout(resolve, attempt * 750))
      }
    }
  }
  throw new Error(`下载失败：${url}，${lastError?.message ?? '未知错误'}`)
}

async function fetchSofaScoreJsonWithRetry(url) {
  let lastError = null
  for (let attempt = 1; attempt <= 5; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: {
          Accept: 'application/json, text/plain, */*',
          Referer: 'https://www.sofascore.com/',
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36'
        },
        signal: AbortSignal.timeout(45_000)
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      return await response.json()
    } catch (error) {
      lastError = error
      if (attempt < 5) {
        await new Promise(resolve => setTimeout(resolve, attempt * 2_000))
      }
    }
  }
  throw new Error(`下载失败：${url}，${lastError?.message ?? '未知错误'}`)
}

async function readEspnRange(request, options) {
  const cachePath = path.join(cacheRoot, 'espn', request.source.slug, `${request.range.key}.json`)
  const refreshCurrentRange = options.maxDate === localDate(new Date())
    && request.range.end === options.maxDate
  if (!options.refreshCache && !refreshCurrentRange) {
    try {
      return JSON.parse(await fs.readFile(cachePath, 'utf8'))
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/${request.source.slug}`
    + `/scoreboard?limit=1000&dates=${basicDate(request.range.start)}-${basicDate(request.range.end)}`
  const json = await fetchJsonWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, JSON.stringify(json), 'utf8')
  return json
}

async function mapWithConcurrency(items, concurrency, worker) {
  const results = new Array(items.length)
  let nextIndex = 0
  async function runWorker() {
    while (nextIndex < items.length) {
      const index = nextIndex
      nextIndex += 1
      results[index] = await worker(items[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, runWorker))
  return results
}

function shanghaiDate(isoText) {
  const date = new Date(isoText)
  if (Number.isNaN(date.getTime())) {
    return null
  }
  const parts = Object.fromEntries(new Intl.DateTimeFormat('en', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(date).filter(part => part.type !== 'literal').map(part => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day}`
}

function eventCompetitors(event) {
  const competition = event?.competitions?.[0]
  const competitors = Array.isArray(competition?.competitors) ? competition.competitors : []
  const home = competitors.find(competitor => competitor.homeAway === 'home')
  const away = competitors.find(competitor => competitor.homeAway === 'away')
  return { competition, home, away }
}

function competitorTeamName(competitor) {
  return String(competitor?.team?.displayName ?? competitor?.team?.location ?? '').trim()
}

function integerScore(value) {
  const numberValue = Number(value)
  return Number.isInteger(numberValue) && numberValue >= 0 ? numberValue : null
}

function detailMinute(detail) {
  const displayValue = String(detail?.clock?.displayValue ?? '')
  const displayMatch = displayValue.match(/^(\d+)/)
  if (displayMatch) {
    return Number(displayMatch[1])
  }
  const clockValue = Number(detail?.clock?.value)
  return Number.isFinite(clockValue) ? clockValue / 60 : null
}

function regulationEspnScore(event, competition, home, away) {
  const rawHomeScore = integerScore(home?.score)
  const rawAwayScore = integerScore(away?.score)
  if (rawHomeScore === null || rawAwayScore === null) {
    return null
  }
  const statusText = [
    event?.status?.type?.description,
    event?.status?.type?.detail,
    event?.status?.type?.shortDetail
  ].join(' ').toLowerCase()
  const beyondRegulation = Number(event?.status?.period ?? 0) > 2
    || statusText.includes('extra time')
    || statusText.includes('penalt')
  if (!beyondRegulation) {
    return { homeScore: rawHomeScore, awayScore: rawAwayScore }
  }
  const details = Array.isArray(competition?.details) ? competition.details : []
  let homeScore = 0
  let awayScore = 0
  let sawGoal = false
  for (const detail of details) {
    if (!detail?.scoringPlay || detail?.shootout || Number(detail?.scoreValue ?? 0) <= 0) {
      continue
    }
    sawGoal = true
    const minute = detailMinute(detail)
    if (minute !== null && minute > 90) {
      continue
    }
    const scoreValue = Number(detail.scoreValue)
    const teamId = String(detail?.team?.id ?? '')
    if (teamId === String(home?.team?.id ?? home?.id ?? '')) {
      homeScore += scoreValue
    } else if (teamId === String(away?.team?.id ?? away?.id ?? '')) {
      awayScore += scoreValue
    }
  }
  if (sawGoal || rawHomeScore + rawAwayScore === 0) {
    return { homeScore, awayScore }
  }
  return null
}

function parseEspnEvents(json, source) {
  const rows = []
  for (const event of json?.events ?? []) {
    if (!event?.status?.type?.completed) {
      continue
    }
    const { competition, home, away } = eventCompetitors(event)
    const homeTeam = competitorTeamName(home)
    const awayTeam = competitorTeamName(away)
    const matchDate = shanghaiDate(event?.date)
    const score = regulationEspnScore(event, competition, home, away)
    if (!homeTeam || !awayTeam || !matchDate || !score) {
      continue
    }
    rows.push({
      provider: 'ESPN',
      providerId: String(event.id ?? ''),
      source: `ESPN-${source.slug}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore: score.homeScore,
      awayScore: score.awayScore,
      neutral: Boolean(competition?.neutralSite),
      allowNationalFallback: false
    })
  }
  return rows
}

async function loadEspnRows(options) {
  if (options.skipEspn) {
    return { rows: [], errors: [] }
  }
  const sources = [...ESPN_PRIMARY_SOURCES, ...ESPN_OFFICIAL_SOURCES, ...ESPN_FRIENDLY_SOURCES]
    .filter(source => sourceSelected(options, `ESPN-${source.slug}`))
  const requests = sources.flatMap(source => (
    (source.monthly ? monthRanges(options.minDate, options.maxDate) : yearRanges(options.minDate, options.maxDate))
      .map(range => ({ source, range }))
  ))
  let completedRequests = 0
  const errors = []
  const batches = await mapWithConcurrency(requests, options.concurrency, async request => {
    try {
      const json = await readEspnRange(request, options)
      return parseEspnEvents(json, request.source)
    } catch (error) {
      errors.push({
        source: request.source.slug,
        range: request.range.key,
        message: error.message
      })
      return []
    } finally {
      completedRequests += 1
      if (completedRequests % 25 === 0 || completedRequests === requests.length) {
        console.error(`ESPN 历史数据进度 ${completedRequests}/${requests.length}`)
      }
    }
  })
  return { rows: batches.flat(), errors }
}

function fotMobSeasonRequests(options) {
  const requests = []
  const firstYear = Number(options.minDate.slice(0, 4))
  const lastYear = Number(options.maxDate.slice(0, 4))
  for (const source of FOTMOB_LEAGUE_SOURCES
    .filter(item => sourceSelected(options, `FOTMOB-${item.leagueId}`))) {
    const previousYear = firstYear - 1
    const includesPreviousCrossYearSeason = !source.calendarYearSeason
      && (!source.crossYearSeasonFrom || previousYear >= source.crossYearSeasonFrom)
    const sourceFirstYear = Math.max(
      includesPreviousCrossYearSeason ? previousYear : firstYear,
      source.firstSeasonStartYear ?? Number.MIN_SAFE_INTEGER
    )
    for (let seasonStartYear = sourceFirstYear; seasonStartYear <= lastYear; seasonStartYear += 1) {
      requests.push({ source, seasonStartYear })
    }
  }
  return requests
}

async function readFotMobSeason(request, options) {
  const crossYearSeason = !request.source.calendarYearSeason
    && (!request.source.crossYearSeasonFrom
      || request.seasonStartYear >= request.source.crossYearSeasonFrom)
  const season = crossYearSeason
    ? `${request.seasonStartYear}/${request.seasonStartYear + 1}`
    : String(request.seasonStartYear)
  const cacheFileName = `${season.replace('/', '-')}.json`
  const cachePath = path.join(
    cacheRoot,
    'fotmob',
    request.source.leagueId,
    cacheFileName
  )
  if (!options.refreshCache) {
    try {
      return JSON.parse(await fs.readFile(cachePath, 'utf8'))
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://www.fotmob.com/api/data/leagues?id=${request.source.leagueId}`
    + `&ccode3=CHN&season=${encodeURIComponent(season)}`
  const json = await fetchJsonWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, JSON.stringify(json), 'utf8')
  return json
}

function parseFotMobRows(json, source) {
  const rows = []
  for (const match of json?.fixtures?.allMatches ?? []) {
    if (!match?.status?.finished || match?.status?.cancelled) {
      continue
    }
    const scoreMatch = String(match?.status?.scoreStr ?? '').match(/^\s*(\d+)\s*-\s*(\d+)\s*$/)
    const matchDate = shanghaiDate(match?.status?.utcTime)
    const sourceTeamName = team => {
      const rawName = String(team?.longName ?? team?.name ?? team?.shortName ?? '').trim()
      if (source.leagueId === '270'
          && String(team?.id ?? '') === '10127'
          && matchDate < '2016-06-06') {
        return 'Litex Lovech'
      }
      return rawName
    }
    const homeTeam = sourceTeamName(match?.home)
    const awayTeam = sourceTeamName(match?.away)
    if (!scoreMatch || !homeTeam || !awayTeam || !matchDate) {
      continue
    }
    rows.push({
      provider: 'FOTMOB',
      providerId: String(match.id ?? ''),
      source: `FOTMOB-${source.leagueId}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore: Number(scoreMatch[1]),
      awayScore: Number(scoreMatch[2]),
      neutral: false
    })
  }
  return rows
}

async function loadFotMobRows(options) {
  if (options.skipFotMob) {
    return { rows: [], errors: [] }
  }
  const requests = fotMobSeasonRequests(options)
  const errors = []
  const batches = await mapWithConcurrency(requests, Math.min(3, options.concurrency), async request => {
    try {
      return parseFotMobRows(await readFotMobSeason(request, options), request.source)
    } catch (error) {
      errors.push({
        source: request.source.leagueId,
        season: !request.source.calendarYearSeason
            && (!request.source.crossYearSeasonFrom
              || request.seasonStartYear >= request.source.crossYearSeasonFrom)
          ? `${request.seasonStartYear}/${request.seasonStartYear + 1}`
          : String(request.seasonStartYear),
        message: error.message
      })
      return []
    }
  })
  return { rows: batches.flat(), errors }
}

function soccerwayKoreaCupSeasonYears(options) {
  const firstYear = Math.max(
    SOCCERWAY_KOREA_CUP_SOURCE.firstSeasonStartYear,
    Number(options.minDate.slice(0, 4))
  )
  const requestedLastYear = Number(options.maxDate.slice(0, 4))
  const currentYear = new Date().getFullYear()
  const lastArchivedYear = requestedLastYear < currentYear
    ? requestedLastYear
    : currentYear - 1
  const years = []
  for (let year = firstYear; year <= lastArchivedYear; year += 1) {
    years.push(year)
  }
  return years
}

async function readSoccerwayKoreaCupSeason(year, options) {
  const cachePath = path.join(cacheRoot, 'soccerway', 'korea-cup', `${year}.html`)
  if (!options.refreshCache) {
    try {
      return await fs.readFile(cachePath, 'utf8')
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://www.soccerway.com/south-korea/korean-cup-${year}/results/`
  const html = await fetchTextWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, html, 'utf8')
  return html
}

function decodeSoccerwayText(value) {
  return String(value ?? '')
    .replaceAll('&amp;', '&')
    .replaceAll('&quot;', '"')
    .replaceAll('&#39;', "'")
    .replaceAll('&#x27;', "'")
    .replaceAll('&nbsp;', ' ')
    .replace(/<[^>]*>/g, '')
    .trim()
}

function parseSoccerwayKoreaCupRows(html) {
  const source = SOCCERWAY_KOREA_CUP_SOURCE
  const fieldSeparator = String.fromCodePoint(172)
  const valueSeparator = String.fromCodePoint(247)
  const eventSeparator = `${fieldSeparator}~AA${valueSeparator}`
  const rows = []
  const seenProviderIds = new Set()
  for (const segment of String(html ?? '').split(eventSeparator).slice(1)) {
    const record = segment.split(`${fieldSeparator}~`)[0]
    const fields = new Map()
    for (const part of `AA${valueSeparator}${record}`.split(fieldSeparator)) {
      const separatorIndex = part.indexOf(valueSeparator)
      if (separatorIndex > 0) {
        fields.set(part.slice(0, separatorIndex), part.slice(separatorIndex + 1))
      }
    }
    const providerId = fields.get('AA')
    if (fields.get('AB') !== '3' || !providerId || seenProviderIds.has(providerId)) {
      continue
    }
    const timestamp = Number(fields.get('AD'))
    const homeScore = integerScore(fields.get('AT') || fields.get('REA') || fields.get('AG'))
    const awayScore = integerScore(fields.get('AU') || fields.get('REB') || fields.get('AH'))
    const homeTeam = decodeSoccerwayText(fields.get('AE'))
    const awayTeam = decodeSoccerwayText(fields.get('AF'))
    const matchDate = Number.isFinite(timestamp)
      ? shanghaiDate(new Date(timestamp * 1_000).toISOString())
      : null
    if (!homeTeam
        || !awayTeam
        || !matchDate
        || homeScore === null
        || awayScore === null) {
      continue
    }
    seenProviderIds.add(providerId)
    rows.push({
      provider: 'SOCCERWAY',
      providerId,
      source: 'SOCCERWAY-KOREA-CUP',
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore,
      awayScore,
      neutral: false
    })
  }
  return rows
}

async function loadSoccerwayKoreaCupRows(options) {
  if (options.skipSoccerway || !sourceSelected(options, 'SOCCERWAY-KOREA-CUP')) {
    return { rows: [], errors: [] }
  }
  const errors = []
  const years = soccerwayKoreaCupSeasonYears(options)
  const batches = await mapWithConcurrency(
    years,
    Math.min(3, options.concurrency),
    async year => {
      try {
        return parseSoccerwayKoreaCupRows(
          await readSoccerwayKoreaCupSeason(year, options)
        )
      } catch (error) {
        errors.push({
          source: 'KOREA-CUP',
          range: String(year),
          message: error.message
        })
        return []
      }
    }
  )
  return { rows: batches.flat(), errors }
}

function parseFutbol24Rows(json, sources) {
  const rows = []
  const statuses = json?.live?.statuses ?? {}
  const sourcesByLeagueId = new Map(sources.map(source => [source.leagueId, source]))
  for (const [eventId, match] of Object.entries(json?.live?.matches ?? {})) {
    const source = sourcesByLeagueId.get(String(match?.league_id ?? ''))
    if (!source) {
      continue
    }
    const status = statuses[String(match?.status_id ?? '')]
    if (String(status?.name_short ?? '').toUpperCase() !== 'FT') {
      continue
    }
    const scoreMatch = String(match?.score1 ?? '').match(/^\s*(\d+)\s*-\s*(\d+)\s*$/)
    const homeTeam = String(match?.team1?.name ?? '').trim()
    const awayTeam = String(match?.team2?.name ?? '').trim()
    const matchDate = shanghaiDate(match?.date)
    if (!scoreMatch || !homeTeam || !awayTeam || !matchDate) {
      continue
    }
    rows.push({
      provider: 'FUTBOL24',
      providerId: String(eventId),
      source: source.leagueId === '472'
        ? 'FUTBOL24-CLUB-FRIENDLY'
        : `FUTBOL24-${source.leagueId}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore: Number(scoreMatch[1]),
      awayScore: Number(scoreMatch[2]),
      neutral: false
    })
  }
  return rows
}

function decodeHtml(value) {
  return String(value ?? '')
    .replaceAll(/&#(\d+);/g, (_, code) => String.fromCodePoint(Number(code)))
    .replaceAll(/&#x([0-9a-f]+);/gi, (_, code) => String.fromCodePoint(Number.parseInt(code, 16)))
    .replaceAll('&amp;', '&')
    .replaceAll('&quot;', '"')
    .replaceAll('&#39;', "'")
    .replaceAll('&lt;', '<')
    .replaceAll('&gt;', '>')
    .trim()
}

function futbol24ShanghaiDate(year, month, day, timeText) {
  const [hour, minute] = String(timeText ?? '00:00').split(':').map(Number)
  if (![year, month, day, hour, minute].every(Number.isInteger)) {
    return null
  }
  const utcTime = Date.UTC(year, month - 1, day, hour - 2, minute)
  return shanghaiDate(new Date(utcTime).toISOString())
}

function parseFutbol24SeasonPage(html, source) {
  const rows = []
  const matchPattern = /<a href="(\/match\/(\d{4})\/(\d{2})\/(\d{2})\/[^"]*\/([^\/"]+)\/vs\/([^"]+))"[^>]*>([\s\S]*?)<\/a>/g
  for (const match of html.matchAll(matchPattern)) {
    const score = match[7].match(/(\d+)\s*-\s*(\d+)/)
    if (!score) {
      continue
    }
    const prefix = html.slice(Math.max(0, match.index - 2_000), match.index)
    const timeMatches = [...prefix.matchAll(
      /f-single-match__cell--time"[^>]*>[\s\S]*?(\d{1,2}:\d{2})/g
    )]
    const timeText = timeMatches.at(-1)?.[1] ?? '00:00'
    const homeTeam = decodeHtml(decodeURIComponent(match[5]).replaceAll('-', ' '))
    const awayTeam = decodeHtml(decodeURIComponent(match[6]).replaceAll('-', ' '))
    const matchDate = futbol24ShanghaiDate(
      Number(match[2]),
      Number(match[3]),
      Number(match[4]),
      timeText
    )
    if (!homeTeam || !awayTeam || !matchDate) {
      continue
    }
    const providerId = crypto.createHash('sha1')
      .update(match[1])
      .digest('hex')
      .slice(0, 16)
      .toUpperCase()
    rows.push({
      provider: 'FUTBOL24',
      providerId,
      source: `FUTBOL24-${source.leagueId}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore: Number(score[1]),
      awayScore: Number(score[2]),
      neutral: false
    })
  }
  return rows
}

function parseFutbol24SeasonResults(json, source) {
  const rows = []
  for (const match of json?.data ?? []) {
    const score = String(match?.score1 ?? '').match(/^\s*(\d+)\s*-\s*(\d+)\s*$/)
    const homeTeam = String(match?.team1?.name ?? '').trim()
    const awayTeam = String(match?.team2?.name ?? '').trim()
    const matchDate = shanghaiDate(match?.date)
    const slug = String(match?.slug ?? '').trim()
    if (!score || !homeTeam || !awayTeam || !matchDate || !slug) {
      continue
    }
    const providerId = crypto.createHash('sha1')
      .update(slug)
      .digest('hex')
      .slice(0, 16)
      .toUpperCase()
    rows.push({
      provider: 'FUTBOL24',
      providerId,
      source: `FUTBOL24-${source.leagueId}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore: Number(score[1]),
      awayScore: Number(score[2]),
      neutral: false
    })
  }
  return rows
}

function futbol24SeasonRequests(options) {
  const firstYear = Number(options.minDate.slice(0, 4))
  const lastYear = Number(options.maxDate.slice(0, 4))
  const lastMonth = Number(options.maxDate.slice(5, 7))
  const requests = []
  for (const source of FUTBOL24_SOURCES.filter(item => (
    item.seasonPath && sourceSelected(options, `FUTBOL24-${item.leagueId}`)
  ))) {
    const sourceFirstYear = source.crossYearSeason ? firstYear - 1 : firstYear
    const sourceLastYear = source.crossYearSeason && lastMonth < 8 ? lastYear - 1 : lastYear
    for (let seasonStartYear = sourceFirstYear; seasonStartYear <= sourceLastYear; seasonStartYear += 1) {
      const season = source.crossYearSeason
        ? `${seasonStartYear}-${seasonStartYear + 1}`
        : String(seasonStartYear)
      requests.push({ source, season })
    }
  }
  return requests
}

async function readFutbol24SeasonPage(request, options) {
  const cachePath = path.join(
    cacheRoot,
    'futbol24-seasons',
    request.source.leagueId,
    `${request.season}.html`
  )
  if (!options.refreshCache) {
    try {
      return await fs.readFile(cachePath, 'utf8')
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://www.futbol24.com/${request.source.seasonPath}/${request.season}/results`
  const html = await fetchTextWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, html, 'utf8')
  return html
}

async function readFutbol24SeasonMeta(request) {
  const slug = `${request.source.seasonPath}/${request.season}`
  const url = `https://api.futbol24.com/api/stats/league/meta?slug=${encodeURIComponent(slug)}`
  const meta = await fetchJsonWithRetry(url)
  if (!meta?.id || !meta?.expire || !meta?.sign || meta?.results === false) {
    throw new Error(`Futbol24 赛季元数据无效：${slug}`)
  }
  return meta
}

async function readFutbol24SeasonResultsPage(request, meta, page, options) {
  const cachePath = path.join(
    cacheRoot,
    'futbol24-season-results',
    request.source.leagueId,
    request.season,
    `${page}.json`
  )
  if (!options.refreshCache) {
    try {
      return JSON.parse(await fs.readFile(cachePath, 'utf8'))
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const query = new URLSearchParams()
  query.set('expire', String(meta.expire))
  query.set('id', String(meta.id))
  query.set('lang', 'en')
  query.set('page', String(page))
  query.set('perPage', '100')
  query.set('sign', String(meta.sign))
  const url = `https://api.futbol24.com/api/stats/league/results?${query}`
  const json = await fetchJsonWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, JSON.stringify(json), 'utf8')
  return json
}

async function readFutbol24SeasonResults(request, options) {
  const meta = await readFutbol24SeasonMeta(request)
  const rows = []
  for (let page = 0; page < 20; page += 1) {
    const json = await readFutbol24SeasonResultsPage(request, meta, page, options)
    const pageRows = parseFutbol24SeasonResults(json, request.source)
    rows.push(...pageRows)
    const totalItems = Number(json?.total_items)
    if ((Number.isFinite(totalItems) && rows.length >= totalItems)
        || !Array.isArray(json?.data)
        || json.data.length < 100) {
      return rows
    }
  }
  throw new Error(`Futbol24 赛季分页超过安全上限：${request.source.leagueId}/${request.season}`)
}

async function loadFutbol24SeasonRows(options) {
  const requests = futbol24SeasonRequests(options)
  const errors = []
  const batches = await mapWithConcurrency(requests, Math.min(4, options.concurrency), async request => {
    try {
      return await readFutbol24SeasonResults(request, options)
    } catch (error) {
      errors.push({
        source: request.source.leagueId,
        season: request.season,
        message: error.message
      })
      return []
    }
  })
  return { rows: batches.flat(), errors }
}

async function readFutbol24Date(date, options) {
  const cachePath = path.join(cacheRoot, 'futbol24', `${date}.json`)
  const refreshCurrentDate = date === localDate(new Date())
  if (!options.refreshCache && !refreshCurrentDate) {
    try {
      return JSON.parse(await fs.readFile(cachePath, 'utf8'))
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const dateValue = encodeURIComponent(`${date}T00:00:00+08:00`)
  const url = `https://api.futbol24.com/api/live/matches?_=0&date=${dateValue}&lang=en&sort=league`
  const json = await fetchJsonWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, JSON.stringify(json), 'utf8')
  return json
}

async function loadFutbol24Rows(options) {
  if (options.skipFutbol24) {
    return { rows: [], errors: [] }
  }
  const selectedSources = FUTBOL24_SOURCES.filter(source => (
    sourceSelected(options, source.leagueId === '472'
      ? 'FUTBOL24-CLUB-FRIENDLY'
      : `FUTBOL24-${source.leagueId}`)
  ))
  const seasonData = await loadFutbol24SeasonRows(options)
  if (selectedSources.length === 0) {
    return seasonData
  }
  const recentStartDate = dateWithOffset(options.maxDate, -30) < options.minDate
    ? options.minDate
    : dateWithOffset(options.maxDate, -30)
  const dates = []
  for (let date = recentStartDate; date <= options.maxDate; date = dateWithOffset(date, 1)) {
    dates.push(date)
  }
  const errors = []
  const batches = await mapWithConcurrency(dates, Math.min(6, options.concurrency), async date => {
    try {
      return parseFutbol24Rows(
        await readFutbol24Date(date, options),
        selectedSources
      )
    } catch (error) {
      errors.push({ source: 'FUTBOL24', date, message: error.message })
      return []
    }
  })
  return {
    rows: [...seasonData.rows, ...batches.flat()],
    errors: [...seasonData.errors, ...errors]
  }
}

function sofaScoreValue(score) {
  for (const field of ['normaltime', 'current', 'display']) {
    const value = Number(score?.[field])
    if (Number.isInteger(value) && value >= 0) {
      return value
    }
  }
  return null
}

function parseSofaScoreRows(json, source) {
  const rows = []
  for (const event of json?.events ?? []) {
    if (event?.status?.type !== 'finished') {
      continue
    }
    const homeTeam = String(event?.homeTeam?.name ?? '').trim()
    const awayTeam = String(event?.awayTeam?.name ?? '').trim()
    const homeScore = sofaScoreValue(event?.homeScore)
    const awayScore = sofaScoreValue(event?.awayScore)
    const matchDate = shanghaiDate(new Date(Number(event?.startTimestamp) * 1_000).toISOString())
    if (!homeTeam || !awayTeam || homeScore === null || awayScore === null || !matchDate) {
      continue
    }
    rows.push({
      provider: 'SOFASCORE',
      providerId: String(event.id ?? ''),
      source: `SOFASCORE-${source.tournamentId}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore,
      awayScore,
      neutral: Boolean(event?.neutral)
    })
  }
  return rows
}

async function readSofaScoreJson(cachePath, url, options) {
  if (!options.refreshCache) {
    try {
      return JSON.parse(await fs.readFile(cachePath, 'utf8'))
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const json = await fetchSofaScoreJsonWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, JSON.stringify(json), 'utf8')
  return json
}

async function loadSofaScoreRows(options) {
  if (options.skipSofaScore || !sourceSelected(options, `SOFASCORE-${SOFASCORE_CLUB_FRIENDLY_SOURCE.tournamentId}`)) {
    return { rows: [], errors: [] }
  }
  const source = SOFASCORE_CLUB_FRIENDLY_SOURCE
  const errors = []
  const recentStartDate = dateWithOffset(options.maxDate, -30) < options.minDate
    ? options.minDate
    : dateWithOffset(options.maxDate, -30)
  try {
    const seasonsCachePath = path.join(cacheRoot, 'sofascore', source.tournamentId, 'seasons.json')
    const seasonsUrl = `https://www.sofascore.com/api/v1/unique-tournament/${source.tournamentId}/seasons`
    const seasonsJson = await readSofaScoreJson(seasonsCachePath, seasonsUrl, options)
    const firstYear = Number(recentStartDate.slice(0, 4))
    const lastYear = Number(options.maxDate.slice(0, 4))
    const seasons = (seasonsJson?.seasons ?? []).filter(season => {
      const year = Number(String(season.year ?? '').match(/\d{4}/)?.[0])
      return year >= firstYear && year <= lastYear
    })
    const rows = []
    for (const season of seasons) {
      for (let page = 0; page < 250; page += 1) {
        const cachePath = path.join(
          cacheRoot,
          'sofascore',
          source.tournamentId,
          String(season.id),
          `last-${page}.json`
        )
        const url = `https://www.sofascore.com/api/v1/unique-tournament/${source.tournamentId}`
          + `/season/${season.id}/events/last/${page}`
        const json = await readSofaScoreJson(cachePath, url, options)
        rows.push(...parseSofaScoreRows(json, source))
        const dates = (json?.events ?? [])
          .map(event => shanghaiDate(new Date(Number(event?.startTimestamp) * 1_000).toISOString()))
          .filter(Boolean)
        const maximumDate = dates.sort().at(-1) ?? null
        console.error(`Sofascore 俱乐部友谊赛进度 ${season.year} 第 ${page + 1} 页`)
        if (!json?.hasNextPage || (maximumDate && maximumDate < recentStartDate)) {
          break
        }
        await new Promise(resolve => setTimeout(resolve, 800))
      }
    }
    return { rows, errors }
  } catch (error) {
    errors.push({ source: source.tournamentId, message: error.message })
    return { rows: [], errors }
  }
}

async function readInternationalSource(options, fileName) {
  if (options.sourceRoot) {
    const localPath = path.join(
      options.sourceRoot,
      'international-results-source/international_results-master',
      fileName
    )
    return await fs.readFile(localPath, 'utf8')
  }
  const cachePath = path.join(cacheRoot, 'international', fileName)
  if (!options.refreshCache) {
    try {
      return await fs.readFile(cachePath, 'utf8')
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://raw.githubusercontent.com/martj42/international_results/master/${fileName}`
  const text = await fetchTextWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, text, 'utf8')
  return text
}

function dateWithOffset(dateText, days) {
  const date = new Date(`${dateText}T00:00:00Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}

function hasChineseName(value) {
  return /\p{Script=Han}/u.test(String(value ?? ''))
}

function fixtureScope(competition, sourceCompetition) {
  return competition === 'CLUB_OFFICIAL_OTHER'
    ? `${competition}|${canonicalName(sourceCompetition)}`
    : competition
}

function fixturePairKey(competition, sourceCompetition, date, homeTeam, awayTeam) {
  const teams = [canonicalChineseName(homeTeam), canonicalChineseName(awayTeam)].sort()
  return `${fixtureScope(competition, sourceCompetition)}|${date}|${teams[0]}|${teams[1]}`
}

function fixtureResultKey(
  competition,
  sourceCompetition,
  date,
  homeTeam,
  awayTeam,
  homeScore,
  awayScore
) {
  const home = canonicalChineseName(homeTeam)
  const away = canonicalChineseName(awayTeam)
  if (home <= away) {
    return `${fixtureScope(competition, sourceCompetition)}`
      + `|${date}|${home}|${away}|${homeScore}|${awayScore}`
  }
  return `${fixtureScope(competition, sourceCompetition)}`
    + `|${date}|${away}|${home}|${awayScore}|${homeScore}`
}

function teamResultEntries(row) {
  const scope = fixtureScope(row.competition, row.source_competition)
  return [
    {
      key: `${scope}|${row.match_date}|${canonicalChineseName(row.home_team_cn)}`
        + `|${row.home_score}|${row.away_score}`,
      side: 'home'
    },
    {
      key: `${scope}|${row.match_date}|${canonicalChineseName(row.away_team_cn)}`
        + `|${row.away_score}|${row.home_score}`,
      side: 'away'
    }
  ]
}

function sideTeamName(row, side) {
  return side === 'home' ? row.home_team_cn : row.away_team_cn
}

function opponentTeamName(row, side) {
  return side === 'home' ? row.away_team_cn : row.home_team_cn
}

function setOpponentTeamName(row, side, teamName) {
  if (side === 'home') {
    row.away_team_cn = teamName
  } else {
    row.home_team_cn = teamName
  }
}

function preferredAliasStandard(leftName, rightName, mappings, competition) {
  const leftEntry = mappedNameEntry(leftName, mappings, competition)
  const rightEntry = mappedNameEntry(rightName, mappings, competition)
  const mappedLeft = leftEntry?.standardName
  const mappedRight = rightEntry?.standardName
  if (mappedLeft && mappedRight
      && canonicalChineseName(mappedLeft) === canonicalChineseName(mappedRight)) {
    return leftEntry.priority >= rightEntry.priority ? mappedLeft : mappedRight
  }
  if (leftEntry && rightEntry && leftEntry.priority !== rightEntry.priority) {
    const preferredEntry = leftEntry.priority > rightEntry.priority ? leftEntry : rightEntry
    return preferredEntry.standardName
  }
  if (leftEntry?.priority >= (MAPPING_SOURCE_PRIORITY.get('HISTORICAL_ODDS') ?? 2)
      && rightEntry?.priority >= (MAPPING_SOURCE_PRIORITY.get('HISTORICAL_ODDS') ?? 2)) {
    return null
  }
  if (mappedLeft && hasChineseName(mappedLeft)) {
    return mappedLeft
  }
  if (mappedRight && hasChineseName(mappedRight)) {
    return mappedRight
  }
  if (hasChineseName(leftName) && !hasChineseName(rightName)) {
    return leftName
  }
  if (hasChineseName(rightName) && !hasChineseName(leftName)) {
    return rightName
  }
  const leftClubName = canonicalClubName(mappedLeft ?? leftName)
  const rightClubName = canonicalClubName(mappedRight ?? rightName)
  if (leftClubName && rightClubName
      && (leftClubName.includes(rightClubName) || rightClubName.includes(leftClubName))) {
    const leftStandard = mappedLeft ?? leftName
    const rightStandard = mappedRight ?? rightName
    return canonicalClubName(leftStandard).length <= canonicalClubName(rightStandard).length
      ? leftStandard
      : rightStandard
  }
  return null
}

function isSportteryBackedHistoryRow(row) {
  const matchId = String(row?.match_id ?? '').toUpperCase()
  return matchId.startsWith('ODDS-')
    || matchId.startsWith('HIS-SPT-')
    || matchId.includes('SPORTTERY')
}

function areLikelyOpponentAliases(leftName, rightName, mappings, competition) {
  const mappedLeft = mappedChineseName(leftName, mappings, competition)
  const mappedRight = mappedChineseName(rightName, mappings, competition)
  if (mappedLeft && mappedRight
      && canonicalChineseName(mappedLeft) === canonicalChineseName(mappedRight)) {
    return true
  }
  const leftClubName = canonicalClubName(leftName)
  const rightClubName = canonicalClubName(rightName)
  return leftClubName.length >= 3
    && rightClubName.length >= 3
    && (leftClubName.includes(rightClubName) || rightClubName.includes(leftClubName))
}

function registerInferredAlias(inferredAliases, standardName, aliasName, lastSeenDate) {
  const standard = String(standardName ?? '').trim()
  const alias = String(aliasName ?? '').trim()
  if (!standard || !alias
      || canonicalChineseName(standard) === canonicalChineseName(alias)) {
    return
  }
  const key = canonicalName(alias)
  const current = inferredAliases.get(key)
  if (!current || lastSeenDate >= current.last_seen_date) {
    inferredAliases.set(key, {
      competition: '*',
      standard_team_name: standard,
      alias_team_name: alias,
      alias_type: hasChineseName(alias) ? 'ZH' : 'EN',
      source: 'INFERRED_DUPLICATE',
      last_seen_date: lastSeenDate
    })
  }
}

function collectPairAliases(inferredAliases, standardName, names, lastSeenDate) {
  for (const name of names) {
    registerInferredAlias(inferredAliases, standardName, name, lastSeenDate)
  }
}

function normalizeAndDeduplicateHistoryRows(rows, nationalMappings, clubMappings) {
  const rowsByFixture = new Map()
  const rowsByFixtureResult = new Map()
  const rowsByTeamResult = new Map()
  const inferredAliases = new Map()
  let duplicateRows = 0
  let shiftedDateDuplicateRows = 0
  let sameTeamScoreDuplicateRows = 0
  const duplicateSamples = []
  for (const row of rows) {
    const isNational = NATIONAL_COMPETITIONS.has(row.competition)
      || row.competition.startsWith('INTERNATIONAL_')
    const mappings = isNational ? nationalMappings : clubMappings
    const normalizedRow = {
      ...row,
      home_team_cn: mappedChineseName(row.home_team_cn, mappings, row.competition) ?? row.home_team_cn,
      away_team_cn: mappedChineseName(row.away_team_cn, mappings, row.competition) ?? row.away_team_cn,
      _homeMappingSource: mappedNameSource(row.home_team_cn, mappings, row.competition),
      _awayMappingSource: mappedNameSource(row.away_team_cn, mappings, row.competition)
    }
    const key = fixturePairKey(
      normalizedRow.competition,
      normalizedRow.source_competition,
      normalizedRow.match_date,
      normalizedRow.home_team_cn,
      normalizedRow.away_team_cn
    )
    const existingFixture = rowsByFixture.get(key)
    if (existingFixture) {
      duplicateRows += 1
      const directOrientation = canonicalChineseName(existingFixture.home_team_cn)
        === canonicalChineseName(normalizedRow.home_team_cn)
      for (const [existingName, candidateName] of directOrientation
        ? [
            [existingFixture.home_team_cn, row.home_team_cn],
            [existingFixture.away_team_cn, row.away_team_cn]
          ]
        : [
            [existingFixture.home_team_cn, row.away_team_cn],
            [existingFixture.away_team_cn, row.home_team_cn]
          ]) {
        const standardName = preferredAliasStandard(
          existingName,
          candidateName,
          mappings,
          row.competition
        )
        if (standardName) {
          collectPairAliases(
            inferredAliases,
            standardName,
            [existingName, candidateName],
            row.match_date
          )
        }
      }
      continue
    }
    const shiftedDuplicate = normalizedRow.match_type !== 'CLUB_FRIENDLY'
      ? [-1, 1]
        .map(offset => rowsByFixtureResult.get(fixtureResultKey(
          normalizedRow.competition,
          normalizedRow.source_competition,
          dateWithOffset(normalizedRow.match_date, offset),
          normalizedRow.home_team_cn,
          normalizedRow.away_team_cn,
          normalizedRow.home_score,
          normalizedRow.away_score
        )))
        .find(Boolean)
      : null
    if (shiftedDuplicate) {
      duplicateRows += 1
      shiftedDateDuplicateRows += 1
      if (duplicateSamples.length < 30) {
        duplicateSamples.push({
          competition: row.competition,
          matchDate: row.match_date,
          shiftedFromDate: shiftedDuplicate.match_date,
          score: `${normalizedRow.home_score}:${normalizedRow.away_score}`,
          retainedFixture: `${shiftedDuplicate.home_team_cn} vs ${shiftedDuplicate.away_team_cn}`,
          mergedFixture: `${normalizedRow.home_team_cn} vs ${normalizedRow.away_team_cn}`
        })
      }
      continue
    }
    if (normalizedRow.match_type !== 'CLUB_FRIENDLY') {
      const teamEntries = teamResultEntries(normalizedRow)
      const duplicateEntry = teamEntries
        .map(entry => ({ ...entry, existing: rowsByTeamResult.get(entry.key) }))
        .find(entry => entry.existing)
      const existingOpponent = duplicateEntry
        ? opponentTeamName(duplicateEntry.existing.row, duplicateEntry.existing.side)
        : null
      const candidateOpponent = duplicateEntry
        ? opponentTeamName(normalizedRow, duplicateEntry.side)
        : null
      const candidateSharedTeamMappingSource = duplicateEntry?.side === 'home'
        ? normalizedRow._homeMappingSource
        : normalizedRow._awayMappingSource
      const sharedTeamMappingIsTrusted = duplicateEntry
        && duplicateEntry.existing.mappingSource !== 'INFERRED_DUPLICATE'
        && candidateSharedTeamMappingSource !== 'INFERRED_DUPLICATE'
      const likelyDuplicate = duplicateEntry && (
        isSportteryBackedHistoryRow(duplicateEntry.existing.row)
        || isSportteryBackedHistoryRow(normalizedRow)
        || sharedTeamMappingIsTrusted
        || areLikelyOpponentAliases(
          existingOpponent,
          candidateOpponent,
          mappings,
          row.competition
        )
      )
      if (likelyDuplicate) {
        duplicateRows += 1
        sameTeamScoreDuplicateRows += 1
        const existingRow = duplicateEntry.existing.row
        const existingSide = duplicateEntry.existing.side
        const retainedFixture = `${existingRow.home_team_cn} vs ${existingRow.away_team_cn}`
        const mergedFixture = `${normalizedRow.home_team_cn} vs ${normalizedRow.away_team_cn}`
        const standardOpponent = preferredAliasStandard(
          existingOpponent,
          candidateOpponent,
          mappings,
          row.competition
        )
        if (standardOpponent) {
          setOpponentTeamName(existingRow, existingSide, standardOpponent)
          collectPairAliases(
            inferredAliases,
            standardOpponent,
            [existingOpponent, candidateOpponent],
            row.match_date
          )
        }
        if (duplicateSamples.length < 30) {
          duplicateSamples.push({
            competition: row.competition,
            matchDate: row.match_date,
            sharedTeam: sideTeamName(normalizedRow, duplicateEntry.side),
            score: `${normalizedRow.home_score}:${normalizedRow.away_score}`,
            retainedFixture,
            mergedFixture
          })
        }
        continue
      }
    }
    rowsByFixture.set(key, normalizedRow)
    rowsByFixtureResult.set(fixtureResultKey(
      normalizedRow.competition,
      normalizedRow.source_competition,
      normalizedRow.match_date,
      normalizedRow.home_team_cn,
      normalizedRow.away_team_cn,
      normalizedRow.home_score,
      normalizedRow.away_score
    ), normalizedRow)
    for (const entry of teamResultEntries(normalizedRow)) {
      rowsByTeamResult.set(entry.key, {
        row: normalizedRow,
        side: entry.side,
        mappingSource: entry.side === 'home'
          ? normalizedRow._homeMappingSource
          : normalizedRow._awayMappingSource
      })
    }
  }
  return {
    rows: [...rowsByFixture.values()],
    duplicateRows,
    shiftedDateDuplicateRows,
    sameTeamScoreDuplicateRows,
    inferredAliases: [...inferredAliases.values()],
    duplicateSamples
  }
}

function mergeInferredMappings(mappingRows, inferredAliases) {
  const rowsByKey = new Map()
  for (const row of [...mappingRows, ...inferredAliases]) {
    const key = `${row.competition}|${canonicalName(row.alias_team_name)}`
    const current = rowsByKey.get(key)
    const rowPriority = MAPPING_SOURCE_PRIORITY.get(row.source) ?? 0
    const currentPriority = MAPPING_SOURCE_PRIORITY.get(current?.source) ?? 0
    if (!current
        || rowPriority > currentPriority
        || rowPriority === currentPriority && row.last_seen_date >= current.last_seen_date) {
      rowsByKey.set(key, row)
    }
  }
  return [...rowsByKey.values()]
}

function sourceMatchId(row) {
  if (row.provider === 'ESPN' && row.providerId) {
    return `ESPN-${row.providerId}`
  }
  if (row.provider === 'FOTMOB' && row.providerId) {
    return `FOTMOB-${row.providerId}`
  }
  if (row.provider === 'SOFASCORE' && row.providerId) {
    return `SOFASCORE-${row.providerId}`
  }
  if (row.provider === 'SOCCERWAY' && row.providerId) {
    return `SOCCERWAY-${row.providerId}`
  }
  if (row.provider === 'FUTBOL24' && row.providerId) {
    return `FUTBOL24-${row.providerId}`
  }
  if (row.provider === 'PFL' && row.providerId) {
    return `PFL-${row.providerId}`
  }
  const digest = crypto.createHash('sha1')
    .update([
      row.source,
      row.matchDate,
      canonicalName(row.homeTeam),
      canonicalName(row.awayTeam)
    ].join('|'))
    .digest('hex')
    .slice(0, 16)
    .toUpperCase()
  return `OPEN-${row.source.replaceAll(/[^A-Za-z0-9]+/g, '-').toUpperCase()}-${digest}`
}

function emptySourceSummary() {
  return {
    parsedRows: 0,
    addedRows: 0,
    updatedRows: 0,
    duplicateRows: 0,
    outsideDateRows: 0,
    outsideTargetRows: 0,
    unmappedRows: 0,
    correctedExtraTimeGoals: 0,
    unmappedTeams: new Map()
  }
}

function outputSourceSummaries(summaries, compact) {
  return Object.fromEntries([...summaries.entries()].map(([source, summary]) => [source, {
    parsedRows: summary.parsedRows,
    addedRows: summary.addedRows,
    updatedRows: summary.updatedRows,
    duplicateRows: summary.duplicateRows,
    outsideDateRows: summary.outsideDateRows,
    outsideTargetRows: summary.outsideTargetRows,
    unmappedRows: summary.unmappedRows,
    correctedExtraTimeGoals: summary.correctedExtraTimeGoals,
    ...(compact ? {} : {
      topUnmappedTeams: [...summary.unmappedTeams.entries()]
        .sort((left, right) => right[1] - left[1] || left[0].localeCompare(right[0]))
        .slice(0, 30)
        .map(([team, count]) => ({ team, count }))
    })
  }]))
}

const options = parseArguments(process.argv.slice(2))
const [
  historyText,
  mappingText,
  resultsText,
  goalsText,
  espnData,
  fotMobData,
  soccerwayKoreaCupData
] = await Promise.all([
  fs.readFile(options.historySourcePath, 'utf8'),
  fs.readFile(teamNameMappingsPath, 'utf8'),
  options.skipNational ? Promise.resolve('') : readInternationalSource(options, 'results.csv'),
  options.skipNational ? Promise.resolve('') : readInternationalSource(options, 'goalscorers.csv'),
  loadEspnRows(options),
  loadFotMobRows(options),
  loadSoccerwayKoreaCupRows(options)
])
const futbol24Data = await loadFutbol24Rows(options)
const sofaScoreData = futbol24Data.rows.length > 0
  ? { rows: [], errors: [] }
  : await loadSofaScoreRows(options)

const originalRows = parseCsv(historyText).map(normalizeHistoryRow)
const mappingRows = parseCsv(mappingText)
const effectiveMappingRows = options.resetInferredMappings
  ? mappingRows.filter(row => row.source !== 'INFERRED_DUPLICATE')
  : mappingRows
const nationalMappings = buildMappings(effectiveMappingRows, NATIONAL_COMPETITIONS)
const clubMappings = buildMappings(effectiveMappingRows, ALL_CLUB_COMPETITIONS)
const retainedHistory = normalizeAndDeduplicateHistoryRows(
  originalRows.filter(row => (
    row.match_date >= options.minDate
    && row.match_date <= options.maxDate
  )),
  nationalMappings,
  clubMappings
)
const retainedRows = retainedHistory.rows
const targetNationalTeams = new Set(originalRows
  .filter(row => NATIONAL_COMPETITIONS.has(row.competition))
  .flatMap(row => [
    mappedChineseName(row.home_team_cn, nationalMappings, row.competition) ?? row.home_team_cn,
    mappedChineseName(row.away_team_cn, nationalMappings, row.competition) ?? row.away_team_cn
  ])
  .map(canonicalChineseName))
const targetClubTeams = new Set(originalRows
  .filter(row => CLUB_COMPETITIONS.has(row.competition))
  .flatMap(row => [
    mappedChineseName(row.home_team_cn, clubMappings, row.competition) ?? row.home_team_cn,
    mappedChineseName(row.away_team_cn, clubMappings, row.competition) ?? row.away_team_cn
  ])
  .map(canonicalChineseName))

const sourceRows = [
  ...(options.skipNational ? [] : parseNationalRows(parseCsv(resultsText), parseCsv(goalsText))),
  ...espnData.rows,
  ...fotMobData.rows,
  ...soccerwayKoreaCupData.rows,
  ...futbol24Data.rows,
  ...sofaScoreData.rows,
  ...VERIFIED_SUPPLEMENTAL_ROWS.filter(row => sourceSelected(options, row.source))
]
const rowsById = new Map(retainedRows.map(row => [row.match_id, row]))
const fixturePairs = new Set(retainedRows.map(row => fixturePairKey(
  row.competition,
  row.source_competition,
  row.match_date,
  row.home_team_cn,
  row.away_team_cn
)))
const rowsByFixturePair = new Map(retainedRows.map(row => [
  fixturePairKey(
    row.competition,
    row.source_competition,
    row.match_date,
    row.home_team_cn,
    row.away_team_cn
  ),
  row
]))
const fixtureResults = new Set(retainedRows.map(row => fixtureResultKey(
  row.competition,
  row.source_competition,
  row.match_date,
  row.home_team_cn,
  row.away_team_cn,
  row.home_score,
  row.away_score
)))
const sourceSummaries = new Map()

for (const sourceRow of sourceRows) {
  const summary = sourceSummaries.get(sourceRow.source) ?? emptySourceSummary()
  sourceSummaries.set(sourceRow.source, summary)
  summary.parsedRows += 1
  summary.correctedExtraTimeGoals += sourceRow.correctedExtraTimeGoals ?? 0
  if (isExcludedCompetition(sourceRow.competition, sourceRow.sourceCompetition)) {
    summary.outsideTargetRows += 1
    continue
  }
  if (!sourceRow.matchDate
      || sourceRow.matchDate < options.minDate
      || sourceRow.matchDate > options.maxDate) {
    summary.outsideDateRows += 1
    continue
  }

  const national = sourceRow.provider === 'INTL'
  const mappings = national ? nationalMappings : clubMappings
  const sourceHomeTeam = sourceRow.homeTeam
  const sourceAwayTeam = sourceRow.awayTeam
  const mappedHomeTeam = mappedChineseName(sourceHomeTeam, mappings, sourceRow.competition)
  const mappedAwayTeam = mappedChineseName(sourceAwayTeam, mappings, sourceRow.competition)
  const targets = national ? targetNationalTeams : targetClubTeams
  const homeIsTarget = mappedHomeTeam && targets.has(canonicalChineseName(mappedHomeTeam))
  const awayIsTarget = mappedAwayTeam && targets.has(canonicalChineseName(mappedAwayTeam))
  const importsWholeCompetition = sourceRow.provider === 'FUTBOL24'
      && ['15', '26', '28', '33', '51', '75', '92', '107', '133', '269', '297',
        '291', '322', '324', '525', '531', '534', '537', '70', '868']
        .includes(String(sourceRow.source).replace('FUTBOL24-', ''))
    || sourceRow.provider === 'FOTMOB' && FOTMOB_LEAGUE_SOURCES.some(
      source => `FOTMOB-${source.leagueId}` === sourceRow.source
    )
    || sourceRow.provider === 'SOCCERWAY'
    || sourceRow.provider === 'PFL'
  if (!importsWholeCompetition && !homeIsTarget && !awayIsTarget) {
    summary.outsideTargetRows += 1
    continue
  }
  if (national && (!mappedHomeTeam || !mappedAwayTeam)) {
    summary.unmappedRows += 1
    continue
  }
  const homeTeam = mappedHomeTeam ?? String(sourceHomeTeam ?? '').trim()
  const awayTeam = mappedAwayTeam ?? String(sourceAwayTeam ?? '').trim()
  if (!homeTeam || !awayTeam) {
    summary.unmappedRows += 1
    for (const [sourceName, mappedName] of [
      [sourceRow.homeTeam, homeTeam],
      [sourceRow.awayTeam, awayTeam]
    ]) {
      if (!mappedName) {
        summary.unmappedTeams.set(sourceName, (summary.unmappedTeams.get(sourceName) ?? 0) + 1)
      }
    }
    continue
  }
  if (canonicalChineseName(homeTeam) === canonicalChineseName(awayTeam)) {
    summary.unmappedRows += 1
    continue
  }

  const matchId = sourceMatchId(sourceRow)
  const expectedRow = {
    match_id: matchId,
    match_date: sourceRow.matchDate,
    competition: sourceRow.competition,
    home_team_cn: homeTeam,
    away_team_cn: awayTeam,
    home_score: String(sourceRow.homeScore),
    away_score: String(sourceRow.awayScore),
    neutral: String(sourceRow.neutral),
    match_type: sourceRow.matchType,
    source_competition: sourceRow.sourceCompetition
  }
  const existingById = rowsById.get(matchId)
  if (existingById) {
    const changed = HISTORY_HEADERS.some(header => (
      String(existingById[header] ?? '') !== String(expectedRow[header] ?? '')
    ))
    if (changed) {
      Object.assign(existingById, expectedRow)
      summary.updatedRows += 1
    }
    continue
  }

  const exactPair = fixturePairKey(
    sourceRow.competition,
    sourceRow.sourceCompetition,
    sourceRow.matchDate,
    homeTeam,
    awayTeam
  )
  const shiftedDuplicate = [-1, 1].some(offset => fixtureResults.has(fixtureResultKey(
    sourceRow.competition,
    sourceRow.sourceCompetition,
    dateWithOffset(sourceRow.matchDate, offset),
    homeTeam,
    awayTeam,
    sourceRow.homeScore,
    sourceRow.awayScore
  )))
  const existingFixture = rowsByFixturePair.get(exactPair)
  if (existingFixture
      && sourceRow.provider === 'SOCCERWAY'
      && sourceRow.sourceCompetition === '韩国杯') {
    const directOrientation = canonicalChineseName(existingFixture.home_team_cn)
      === canonicalChineseName(homeTeam)
    const expectedHomeScore = String(
      directOrientation ? sourceRow.homeScore : sourceRow.awayScore
    )
    const expectedAwayScore = String(
      directOrientation ? sourceRow.awayScore : sourceRow.homeScore
    )
    if (existingFixture.home_score !== expectedHomeScore
        || existingFixture.away_score !== expectedAwayScore
        || existingFixture.source_competition !== sourceRow.sourceCompetition) {
      existingFixture.home_score = expectedHomeScore
      existingFixture.away_score = expectedAwayScore
      existingFixture.source_competition = sourceRow.sourceCompetition
      summary.updatedRows += 1
    } else {
      summary.duplicateRows += 1
    }
    continue
  }
  if (existingFixture || shiftedDuplicate) {
    summary.duplicateRows += 1
    continue
  }

  retainedRows.push(expectedRow)
  rowsById.set(matchId, expectedRow)
  fixturePairs.add(exactPair)
  rowsByFixturePair.set(exactPair, expectedRow)
  fixtureResults.add(fixtureResultKey(
    sourceRow.competition,
    sourceRow.sourceCompetition,
    sourceRow.matchDate,
    homeTeam,
    awayTeam,
    sourceRow.homeScore,
    sourceRow.awayScore
  ))
  summary.addedRows += 1
}

let finalHistory = normalizeAndDeduplicateHistoryRows(
  retainedRows,
  nationalMappings,
  clubMappings
)
const inferredAliasesByKey = new Map()
function collectInferredAliases(aliases) {
  let changed = false
  for (const alias of aliases) {
    const key = `${alias.competition}|${canonicalName(alias.alias_team_name)}`
    const current = inferredAliasesByKey.get(key)
    if (!current || alias.last_seen_date > current.last_seen_date) {
      inferredAliasesByKey.set(key, alias)
      changed = true
    }
  }
  return changed
}

collectInferredAliases(retainedHistory.inferredAliases)
collectInferredAliases(finalHistory.inferredAliases)

let finalDuplicateRows = finalHistory.duplicateRows
let finalShiftedDateDuplicateRows = finalHistory.shiftedDateDuplicateRows
let finalSameTeamScoreDuplicateRows = finalHistory.sameTeamScoreDuplicateRows
const finalDuplicateSamples = [...finalHistory.duplicateSamples]
let refinementPasses = 0
for (; refinementPasses < 10; refinementPasses += 1) {
  const refinedMappingRows = mergeInferredMappings(
    effectiveMappingRows,
    [...inferredAliasesByKey.values()]
  )
  const refinedHistory = normalizeAndDeduplicateHistoryRows(
    finalHistory.rows,
    buildMappings(refinedMappingRows, NATIONAL_COMPETITIONS),
    buildMappings(refinedMappingRows, ALL_CLUB_COMPETITIONS)
  )
  finalDuplicateRows += refinedHistory.duplicateRows
  finalShiftedDateDuplicateRows += refinedHistory.shiftedDateDuplicateRows
  finalSameTeamScoreDuplicateRows += refinedHistory.sameTeamScoreDuplicateRows
  finalDuplicateSamples.push(...refinedHistory.duplicateSamples)
  const inferredAliasesChanged = collectInferredAliases(refinedHistory.inferredAliases)
  finalHistory = refinedHistory
  if (refinedHistory.duplicateRows === 0 && !inferredAliasesChanged) {
    refinementPasses += 1
    break
  }
}

const rebuiltRows = finalHistory.rows.sort((left, right) => (
  left.match_date.localeCompare(right.match_date)
  || left.competition.localeCompare(right.competition)
  || left.match_id.localeCompare(right.match_id)
))
const inferredAliases = [...inferredAliasesByKey.values()]

if (options.write) {
  await fs.writeFile(historicalMatchesPath, `\uFEFF${toCsv(rebuiltRows)}`, 'utf8')
  const writtenHistoryRows = parseCsv(
    await fs.readFile(historicalMatchesPath, 'utf8')
  ).length
  if (writtenHistoryRows !== rebuiltRows.length) {
    throw new Error(
      `历史比赛写入校验失败：预期 ${rebuiltRows.length} 行，实际 ${writtenHistoryRows} 行`
    )
  }
  if (options.resetInferredMappings || inferredAliases.length > 0) {
    await fs.writeFile(
      teamNameMappingsPath,
      `\uFEFF${toMappingCsv(mergeInferredMappings(effectiveMappingRows, inferredAliases))}`,
      'utf8'
    )
  }
  await import('./generate-team-name-mappings.mjs')
}

const matchTypes = Object.fromEntries([...new Set(rebuiltRows.map(row => row.match_type))]
  .sort()
  .map(matchType => [matchType, rebuiltRows.filter(row => row.match_type === matchType).length]))
const competitions = Object.fromEntries([...new Set(rebuiltRows.map(row => row.competition))]
  .sort()
  .map(competition => [competition, rebuiltRows.filter(row => row.competition === competition).length]))

console.log(JSON.stringify({
  minDate: options.minDate,
  maxDate: options.maxDate,
  originalRows: originalRows.length,
  removedBeforeOrAfterRange: originalRows.length - originalRows.filter(row => (
    row.match_date >= options.minDate && row.match_date <= options.maxDate
  )).length,
  removedDuplicateRows: retainedHistory.duplicateRows + finalDuplicateRows,
  removedShiftedDateDuplicateRows:
    retainedHistory.shiftedDateDuplicateRows + finalShiftedDateDuplicateRows,
  removedSameTeamScoreDuplicateRows:
    retainedHistory.sameTeamScoreDuplicateRows + finalSameTeamScoreDuplicateRows,
  refinementPasses,
  inferredTeamAliases: inferredAliases.length,
  inferredAliasSamples: inferredAliases.slice(0, 50),
  duplicateSamples: [
    ...retainedHistory.duplicateSamples,
    ...finalDuplicateSamples
  ].slice(0, 30),
  rebuiltRows: rebuiltRows.length,
  importedRows: [...sourceSummaries.values()].reduce((sum, summary) => sum + summary.addedRows, 0),
  updatedRows: [...sourceSummaries.values()].reduce((sum, summary) => sum + summary.updatedRows, 0),
  targetNationalTeams: targetNationalTeams.size,
  targetClubTeams: targetClubTeams.size,
  matchTypes,
  competitions,
  sources: outputSourceSummaries(sourceSummaries, options.compact),
  espnRequestErrors: espnData.errors,
  fotMobRequestErrors: fotMobData.errors,
  soccerwayRequestErrors: soccerwayKoreaCupData.errors,
  futbol24RequestErrors: futbol24Data.errors,
  sofaScoreRequestErrors: sofaScoreData.errors,
  wroteFile: options.write
}, null, 2))
