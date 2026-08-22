import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historicalMatchesPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const historicalOddsPath = path.join(root, 'src/main/resources/data/historical_odds_data.csv')
const outputPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')
const clubSchedulesPath = path.join(root, 'config/club-competition-schedules.json')
const sportteryCachePath = path.join(root, 'config/sporttery-market-selections.json')

const HEADERS = [
  'competition',
  'standard_team_name',
  'alias_team_name',
  'alias_type',
  'source',
  'last_seen_date'
]

async function writeFileWithRetry(filePath, content, attempts = 8) {
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    try {
      await fs.writeFile(filePath, content, 'utf8')
      return
    } catch (error) {
      const retryable = ['EBUSY', 'EPERM', 'UNKNOWN'].includes(error?.code)
      if (!retryable || attempt === attempts) {
        throw error
      }
      await new Promise(resolve => setTimeout(resolve, attempt * 125))
    }
  }
}


const SOURCE_PRIORITY = new Map([
  ['HISTORICAL_MATCHES', 1],
  ['HISTORICAL_ODDS', 2],
  ['ESPN_SCHEDULE', 3],
  ['INFERRED_DUPLICATE', 3],
  ['VERIFIED_ALIAS', 4],
  ['VERIFIED_SPORTTERY', 6],
  ['MANUAL', 7]
])

const REJECTED_MAPPING_KEYS = new Set([
  ['EREDIVISIE', 'Odds BK'],
  ['LIGUE_1', 'Crusaders'],
  ['BUNDESLIGA', 'KR Reykjavik']
].map(([competition, aliasName]) => `${competition}|${canonicalName(aliasName)}`))

const VERIFIED_SPORTTERY_ENGLISH_ALIASES = [
  {
    competition: '*',
    standardName: 'AIK索尔纳',
    aliasName: 'AIK Fotboll',
    lastSeenDate: '2026-06-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '纳夫兹',
    aliasName: 'FK Neftchi',
    lastSeenDate: '2026-07-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '纳夫兹',
    aliasName: 'Neftchi Baku',
    lastSeenDate: '2026-07-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '纳夫兹',
    aliasName: 'Neftçi',
    lastSeenDate: '2026-07-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '卡尔斯多夫',
    aliasName: 'Kalsdorf',
    lastSeenDate: '2026-07-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '卡尔斯多夫',
    aliasName: 'SC Kalsdorf',
    lastSeenDate: '2026-07-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '西基臣',
    aliasName: 'Seekirchen',
    lastSeenDate: '2026-07-28',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '阿尔塔奇',
    aliasName: 'Altach',
    lastSeenDate: '2026-07-27',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '奥地利克拉根福',
    aliasName: 'SK Austria Klagenfurt',
    lastSeenDate: '2026-07-25',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: 'BW林茨',
    aliasName: 'BW Linz',
    lastSeenDate: '2026-07-25',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '特罗姆瑟',
    aliasName: 'Tromsø',
    lastSeenDate: '2026-07-26',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: '*',
    standardName: '利瓦迪亚',
    aliasName: 'FC Levadia Tallinn',
    lastSeenDate: '2026-07-22',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '利瓦迪亚',
    aliasName: 'FCI Levadia',
    lastSeenDate: '2026-07-31',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '汉坎',
    aliasName: 'Hamarkameratene',
    lastSeenDate: '2026-06-27',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '汉坎',
    aliasName: 'HamKam',
    lastSeenDate: '2026-06-27',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '萨拉热窝',
    aliasName: 'FK Sarajevo',
    lastSeenDate: '2026-07-16',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '瓦尔达尔',
    aliasName: 'Vardar',
    lastSeenDate: '2026-07-23',
    source: 'MANUAL'
  },
  {
    competition: '*',
    standardName: '高利宁',
    aliasName: 'Coleraine',
    lastSeenDate: '2021-07-16',
    source: 'MANUAL'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '越南',
    aliasName: 'Vietnam',
    lastSeenDate: '2026-07-13',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '始兴市民',
    aliasName: 'Siheung FC',
    lastSeenDate: '2026-07-05',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '始兴市民',
    aliasName: 'Siheung Citizen',
    lastSeenDate: '2026-07-15',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '龙仁FC',
    aliasName: 'Yongin FC',
    lastSeenDate: '2026-07-19',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '竞技俱乐部MG',
    aliasName: 'Athletic Club (MG)',
    lastSeenDate: '2026-07-23',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: 'FK IMT Beograd',
    aliasName: 'IMT Novi Beograd',
    lastSeenDate: '2026-07-20',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '利特克斯',
    aliasName: 'Litex Lovech',
    lastSeenDate: '2015-12-12',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '皇家盐湖城',
    aliasName: 'Real Salt Lake',
    lastSeenDate: '2026-07-23',
    source: 'VERIFIED_SPORTTERY'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '温哥华白帽',
    aliasName: 'Vancouver Whitecaps FC',
    lastSeenDate: '2026-07-17',
    source: 'VERIFIED_SPORTTERY'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '塞伊奈约基',
    aliasName: 'SJK',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_SPORTTERY'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '亨克',
    aliasName: 'Racing Genk',
    lastSeenDate: '2026-07-23',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '波鸿',
    aliasName: 'VfL Bochum',
    lastSeenDate: '2026-07-22',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '根特',
    aliasName: 'KAA Gent',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '泽尼特',
    aliasName: 'Zenit',
    lastSeenDate: '2026-07-13',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: 'AEK拉纳卡',
    aliasName: 'AEK Larnaca',
    lastSeenDate: '2026-05-22',
    source: 'VERIFIED_SPORTTERY'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '希腊人',
    aliasName: 'APOEL FC',
    lastSeenDate: '2026-05-22',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: 'AEL利马索尔',
    aliasName: 'AEL Limassol',
    lastSeenDate: '2026-05-16',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '扎卡基乌',
    aliasName: 'AEZ Zakakiou',
    lastSeenDate: '2024-05-12',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿克里塔斯',
    aliasName: 'Akritas Chlorakas',
    lastSeenDate: '2026-05-16',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿尔基奥罗克林尼',
    aliasName: 'Alki Oroklini',
    lastSeenDate: '2019-05-19',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿纳格尼斯德里尼亚',
    aliasName: 'Anagennisi Derynia',
    lastSeenDate: '2017-03-06',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿诺索西斯',
    aliasName: 'Anorthosis FC',
    lastSeenDate: '2026-05-16',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿依纳帕',
    aliasName: 'Ayia Napa',
    lastSeenDate: '2016-03-02',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '卡托克匹亚斯',
    aliasName: 'Doxa Katokopia',
    lastSeenDate: '2024-05-10',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '帕拉利米尼',
    aliasName: 'EN Paralimni',
    lastSeenDate: '2026-05-17',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '艾米斯',
    aliasName: 'Ermis Aradippou',
    lastSeenDate: '2021-05-29',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿奇纳',
    aliasName: 'Ethnikos Achnas',
    lastSeenDate: '2026-05-16',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '卡米奥提萨',
    aliasName: 'Karmiotissa Polemidion',
    lastSeenDate: '2025-05-12',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '伊普索纳斯',
    aliasName: 'Krasava ENY Ypsonas',
    lastSeenDate: '2026-05-17',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '萨拉米斯',
    aliasName: 'Nea Salamina',
    lastSeenDate: '2025-05-10',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '尼科西亚奥林匹亚',
    aliasName: 'Olympiakos Nicosia',
    lastSeenDate: '2026-05-16',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '奥莫尼亚29M',
    aliasName: 'Omonia 29is Maiou',
    lastSeenDate: '2025-05-12',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '奥莫尼亚阿拉迪普',
    aliasName: 'Omonia Aradippou',
    lastSeenDate: '2026-05-16',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '奥赛罗斯',
    aliasName: 'Othellos Athienou',
    lastSeenDate: '2024-05-13',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '佩伊克',
    aliasName: 'PAEEK Kyrenia',
    lastSeenDate: '2022-05-21',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿克扎伊克',
    aliasName: 'Akzhayik Oral',
    lastSeenDate: '2022-11-06',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿勒泰瑟美',
    aliasName: 'Altay FK (KAZ)',
    lastSeenDate: '2016-11-05',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿勒泰瑟美',
    aliasName: 'FC Altai Öskemen',
    lastSeenDate: '2026-07-19',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '卡斯比阿克套',
    aliasName: 'Caspiy Aktau',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '图兰突厥斯坦',
    aliasName: 'FC Turan (KAZ)',
    lastSeenDate: '2025-10-26',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '热尼斯',
    aliasName: 'FC Zhenis',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿克苏',
    aliasName: 'FK Aksu',
    lastSeenDate: '2023-10-29',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿克托比',
    aliasName: 'FK Aktobe',
    lastSeenDate: '2026-07-19',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿特劳',
    aliasName: 'FK Atyrau',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '克孜勒扎尔',
    aliasName: 'FK Kyzylzhar',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '马克塔拉尔',
    aliasName: 'FK Maktaaral',
    lastSeenDate: '2023-10-29',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '奥肯咸特普斯',
    aliasName: 'FK Okzhetpes',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '奥达巴斯',
    aliasName: 'FK Ordabasy',
    lastSeenDate: '2026-07-20',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '塔拉兹',
    aliasName: 'FK Taraz',
    lastSeenDate: '2022-11-06',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '叶利迈塞米',
    aliasName: 'FK Yelimay Semey',
    lastSeenDate: '2026-07-20',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '伊特什',
    aliasName: 'Irtysh Pavlodar',
    lastSeenDate: '2026-07-11',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '卡萨尔',
    aliasName: 'Kaisar Kyzylorda',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '基兰',
    aliasName: 'Kyran Shymkent',
    lastSeenDate: '2018-11-20',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '沙克特',
    aliasName: 'Shakhtyor Karagandy',
    lastSeenDate: '2024-11-03',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '塞梅伊斯巴达',
    aliasName: 'Spartak Semey',
    lastSeenDate: '2014-11-09',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '杜保尔',
    aliasName: 'Tobol Kostanay',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '乌利套',
    aliasName: 'Ulytau Zhezkazgan',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '厄斯克门沃斯托克',
    aliasName: 'Vostok Oskemen',
    lastSeenDate: '2015-11-14',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '捷特苏',
    aliasName: 'Zhetysu Taldykorgan',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿特劳',
    aliasName: '阿特雷约',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿克扎伊克',
    aliasName: '贾伊克',
    lastSeenDate: '2022-11-06',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿克托比',
    aliasName: '阿克图比',
    lastSeenDate: '2026-07-19',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '阿特劳',
    aliasName: '阿迪拿奥',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '克孜勒扎尔',
    aliasName: '彼得罗巴甫洛夫斯克',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '克孜勒扎尔',
    aliasName: '波格特约',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '塔拉兹',
    aliasName: '拖雷斯',
    lastSeenDate: '2022-11-06',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '伊特什',
    aliasName: '巴甫洛达尔额尔齐斯',
    lastSeenDate: '2026-07-11',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '伊特什',
    aliasName: '艾迪殊',
    lastSeenDate: '2026-07-11',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '卡萨尔',
    aliasName: '卡萨尔克孜勒奥尔达',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '乌利套',
    aliasName: '乌利塔哲兹卡兹甘',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '捷特苏',
    aliasName: '斯咸迪苏',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '沙克特',
    aliasName: '卡拉干达矿工',
    lastSeenDate: '2024-11-03',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_OFFICIAL_OTHER',
    standardName: '杜保尔',
    aliasName: '杜堡尔',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '科罗纳',
    aliasName: 'Korona Kielce',
    lastSeenDate: '2026-07-17',
    source: 'VERIFIED_SPORTTERY'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '保克什',
    aliasName: 'Paksi FC',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '北西兰',
    aliasName: 'FC Nordsjælland',
    lastSeenDate: '2026-07-22',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '莫迪纳摩',
    aliasName: 'Dynamo Moscow',
    lastSeenDate: '2026-07-19',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '布迪纳摩',
    aliasName: 'Dinamo Bucuresti',
    lastSeenDate: '2026-07-12',
    source: 'VERIFIED_SPORTTERY'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '奥勒克',
    aliasName: 'FK Aleksandriya',
    lastSeenDate: '2026-07-18',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CLUB_FRIENDLY',
    standardName: '里斯本竞技',
    aliasName: 'Sporting Lisboa',
    lastSeenDate: '2026-07-21',
    source: 'VERIFIED_ALIAS'
  },
  {
    competition: 'CHAMPIONS_LEAGUE',
    standardName: '克拉克斯维克',
    aliasName: 'KI Klaksvik',
    lastSeenDate: '2026-07-16'
  },
  {
    competition: 'CHAMPIONS_LEAGUE',
    standardName: '雷克雅未克维京人',
    aliasName: 'Vikingur Reykjavik',
    lastSeenDate: '2026-07-15'
  },
  {
    competition: 'WORLD_CUP',
    standardName: '沙特阿拉伯',
    aliasName: 'Saudi Arabia',
    lastSeenDate: '2026-06-27'
  }
]

const USER_REQUESTED_TEAM_ALIASES = [
  ['Iraklis 1908', 'Iraklis Salonica', '2026-08-23'],
  ['Iraklis 1908', 'Iraklis', '2026-08-23'],
  ['纽波特郡', 'Newport County', '2026-08-23'],
  ['戛纳', 'AS Cannes', '2026-08-23'],
  ['女王巡游', 'QPR', '2026-08-23'],
  ['古比奥', 'Gubbio', '2026-08-23'],
  ['阿拉比', 'Al-Arabi', '2026-08-23'],
  ['阿拉比', 'Arabi Doha', '2026-08-23'],
  ['Shahaniya', 'Shahaniya', '2026-08-21'],
  ['Shahaniya', 'Al-Shahaniya', '2026-08-21'],
  ['Shahaniya', 'Shahaniya SC', '2026-08-21'],
  ['谢周三', '谢周三', '2026-08-21'],
  ['谢周三', 'Sheffield Wednesday', '2026-08-21'],
  ['查尔顿', '查尔顿', '2026-08-21'],
  ['查尔顿', 'Charlton Athletic', '2026-08-21'],
  ['布伦特', '布伦特', '2026-08-21'],
  ['布伦特', '布伦特福德', '2026-08-21'],
  ['布伦特', 'Brentford', '2026-08-21'],
  ['布伦特', 'Brentford FC', '2026-08-21'],
  ['威科姆', 'Wycombe Wanderers', '2026-08-21'],
  ['奥克兰FC', '奥克兰FC', '2026-08-21'],
  ['奥克兰FC', 'Auckland FC', '2026-08-21'],
  ['米尔顿', '米尔顿', '2026-08-21'],
  ['米尔顿', 'Milton Keynes Dons', '2026-08-21'],
  ['斯旺西', '斯旺西', '2026-08-21'],
  ['斯旺西', 'Swansea', '2026-08-21'],
  ['斯旺西', 'Swansea City', '2026-08-21'],
  ['伊斯特拉1961', '伊斯特拉1961', '2026-08-21'],
  ['伊斯特拉1961', 'Istra', '2026-08-21'],
  ['卡斯鲁厄', '卡斯鲁厄', '2026-08-21'],
  ['卡斯鲁厄', 'Karlsruher SC', '2026-08-21'],
  ['Milan II', 'Milan II', '2026-08-21'],
  ['Milan II', 'Milan Futuro', '2026-08-21'],
  ['阿里斯', '阿里斯', '2026-08-21'],
  ['阿里斯', 'Aris Salonica', '2026-08-21'],
  ['阿里斯', 'Aris Thessaloniki', '2026-08-21'],
  ['瓦雷泽', 'Varese', '2026-08-21'],
  ['佩鲁贾', 'Perugia', '2026-08-21'],
  ['阿韦利诺', 'Avellino', '2026-08-21'],
  ['摩德纳', 'Modena', '2026-08-21'],
  ['亚历山德', 'Alessandria', '2026-08-21'],
  ['帕维亚', 'AC Pavia', '2026-08-21'],
  ['维琴察', 'Vicenza', '2026-08-21'],
  ['巴里', 'Bari', '2026-08-21'],
  ['特拉帕尼', 'Trapani', '2026-08-21'],
  ['诺瓦拉', 'Novara', '2026-08-21'],
  ['奇塔代拉', 'Cittadella', '2026-08-21'],
  ['波代诺内', 'Pordenone Calcio', '2026-08-21'],
  ['雷吉纳', 'LFA Reggio Calabria', '2026-08-21'],
  ['恩特拉', 'Virtus Entella', '2026-08-21'],
  ['尼姆', 'Nîmes Olympique', '2026-08-21'],
  ['Yamoussoukro', 'Yamoussoukro FC', '2026-08-21'],
  ['纽卡斯尔', '纽卡斯尔', '2026-08-21'],
  ['纽卡斯尔', '纽卡斯尔联', '2026-08-21'],
  ['布莱克本', 'Blackburn Rovers', '2026-08-21'],
  ['布莱克本', 'Blackburn', '2026-08-21'],
  ['阿梅里亚', 'UD Almería', '2026-08-21'],
  ['韦尔瓦', 'Recreativo Huelva', '2026-08-21'],
  ['洛特', 'VfL Sportfreunde Lotte', '2026-08-21'],
  ['桑坦德', 'Santander', '2026-08-21'],
  ['Dangjin', 'Dangjin Citizen', '2026-08-21'],
  ['曼联', '曼联', '2026-08-21'],
  ['曼联', '曼彻斯特联', '2026-08-21'],
  ['曼联', 'Manchester United', '2026-08-21'],
  ['卡斯帕萨', 'Kasimpasa', '2026-08-21'],
  ['里泽', '里泽体育', '2026-08-21'],
  ['雷克斯', 'Wrexham', '2026-08-21'],
  ['诺茨郡', 'Notts County', '2026-08-21'],
  ['莱红牛', '莱比锡红牛', '2026-08-21'],
  ['伊普斯', '伊普斯', '2026-08-21'],
  ['伊普斯', '伊普斯维奇', '2026-08-21'],
  ['米尔沃尔', 'Millwall', '2026-08-21'],
  ['朴次茅斯', 'Portsmouth', '2026-08-21'],
  ['威科姆', 'Wycombe', '2026-08-21'],
  ['牛津联', 'Oxford United', '2026-08-21'],
  ['约克城', 'York City', '2026-08-21'],
  ['约克城', 'York', '2026-08-21'],
  ['博尔顿', 'Bolton Wanderers', '2026-08-21'],
  ['博尔顿', 'Bolton', '2026-08-21'],
  ['Al Ula', 'Al Ula SC', '2026-08-21'],
  ['斯文登', 'Swindon', '2026-08-21'],
  ['斯文登', 'Swindon Town', '2026-08-21'],
  ['博格斯', 'Burgos CF', '2026-08-21'],
  ['Sestao River', 'Sestao', '2026-08-21'],
  ['Leioa', 'SD Leioa', '2026-08-21'],
  ['Derio', 'CD Derio', '2026-08-21'],
  ['沙勒罗瓦', 'Charleroi', '2026-08-21'],
  ['不来梅', '云达不来梅', '2026-08-21'],
  ['特鲁瓦', 'ESTAC Troyes', '2026-08-21'],
  ['索肖', 'Sochaux', '2026-08-21'],
  ['奥尔良', 'US Orléans', '2026-08-21'],
  ['奥尔良', 'Orléans', '2026-08-21'],
  ['St Maur Lusita', 'St Maur Lusitanos', '2026-08-21'],
  ['St Maur Lusita', 'St Maur Lusitanos US', '2026-08-21'],
  ['特拉布宗', '特拉布宗体育', '2026-08-20'],
  ['奇姆肯特', '奥达巴斯', '2026-08-20'],
  ['WSG Tirol', 'WSG蒂罗尔', '2026-08-20'],
  ['Oberwart', 'SV Oberwart', '2026-08-20'],
  ['Metalist 1925', 'FC Kharkiv', '2026-08-20'],
  ['Metalist 1925', 'Metalist 1925 Kharkiv', '2026-08-20'],
  ['乌迪内斯', 'Udinese Calcio', '2026-08-20'],
  ['多哈萨德', '萨德', '2026-08-20'],
  ['沃绍什', '华萨斯', '2026-08-20'],
  ['克拉克斯', '克拉克斯维克', '2026-08-20'],
  ['格里维治', '皮亚斯特', '2026-08-20'],
  ['Wieczysta', 'Wieczysta Kraków', '2026-08-20'],
  ['萨克森体育', 'FC Saxon Sports', '2026-08-20'],
  ['马克瓦', 'Macva Sabac', '2026-08-20'],
  ['特普利斯', 'FK Teplice', '2026-08-20'],
  ['红色小鬼', '林肯红色小鬼'],
  ['哥本哈根', 'Copenhague'],
  ['哈尔姆斯', 'Halmstad'],
  ['Saburtalo', 'Iberia 1999'],
  ['斯拉维亚', '布拉格斯拉维亚'],
  ['贝红星', '贝尔格莱德红星'],
  ['GAK', '格拉茨AK'],
  ['泽姆匹林米哈洛夫采', 'Zemplin Michalovce'],
  ['奥林匹亚', '奥林匹亚科斯'],
  ['阿贾克斯', 'Ajax Amsterdam'],
  ['福图纳', '福图纳锡塔德'],
  ['长崎航海', 'V-Varen Nagasaki'],
  ['杜伊斯堡', 'MSV Duisburg'],
  ['赫鲁斯', 'De Treffers'],
  ['奥帕瓦', 'Opava'],
  ['贝蒂斯', '皇家贝蒂斯'],
  ['沃夫斯堡', 'VfL Wolfsburg'],
  ['圣加仑', 'Saint-Gall'],
  ['Mâcon', 'Macon 71'],
  ['圣吉联合', '圣吉尔联合'],
  ['布星', '布加勒斯特星'],
  ['迪耶根体育', 'Diegem'],
  ['迪耶根体育', 'Diegem Sport'],
  ['安德莱', '安德莱赫特'],
  ['斯达', 'Start'],
  ['灵比', '林比'],
  ['波兹南', '波兹南莱赫'],
  ['圣吉联合', 'Union Saint-Gilloise'],
  ['圣吉联合', 'Royale Union Saint-Gilloise'],
  ['圣吉联合', 'R. Union SG'],
  ['圣吉联合', 'Union SG'],
  ['Polissya', 'Polissya Zhitomir'],
  ['克拉约瓦', '克拉约瓦大学'],
  ['Turan', 'Turan Tovuz'],
  ['格风暴', '格拉茨风暴'],
  ['默德林', '阿德米拉'],
  ['广岛三箭', 'Sanfrecce'],
  ['布斯巴达', '布拉格斯巴达'],
  ['Podbrezova', 'Zeleziarne Podbrezova'],
  ['Banska Bystrica', 'Dukla Banska Bystrica'],
  ['布尔诺', 'FC Zbrojovka Brno'],
  ['利勒斯特罗姆', 'Lillestrøm'],
  ['腓特烈', '腓特烈斯塔'],
  ['霍尔森斯', '霍森斯'],
  ['布拉迪斯', '布拉迪斯拉发'],
  ['Kralove', 'Hradec Kralove'],
  ['穆拉', 'Mura'],
  ['Petrolul 52', 'Petrolul Ploiesti', '2026-08-06'],
  ['索列夫', 'Levski Sofia', '2026-08-06'],
  ['UTA', 'UTA Arad', '2026-08-06'],
  ['ML Vitebsk', 'FK Maxline Vitebsk', '2026-08-06'],
  ['U Cluj', 'Universitatea Cluj', '2026-08-06'],
  ['比亚韦', '比亚韦斯托克', '2026-08-06'],
  ['圣埃蒂安', 'ASSE', '2026-08-06'],
  ['帕纳辛纳', '帕纳辛纳科斯', '2026-08-06'],
  ['阿拉木图', '阿拉木图凯拉特', '2026-08-11'],
  ['索列夫', '索菲亚列夫斯基', '2026-08-11'],
  ['Septemvri', 'Septemvri Sofia', '2026-08-11'],
  ['Lokomotiv Sf', 'PFC Lokomotiv Sofia 1929', '2026-08-11'],
  ['Dunav', 'Dunav Ruse', '2026-08-11'],
  ['巴战士', '巴尼亚', '2026-08-11'],
  ['Radnik', 'FK Radnik Surdulica', '2026-08-11'],
  ['克拉约瓦', 'U Craiova', '2026-08-11'],
  ['利勒斯特', '利勒斯特罗姆', '2026-08-11'],
  ['桑纳菲', '桑纳菲尤尔', '2026-08-11'],
  ['迪耶根体育', 'K. Diegem Sport', '2026-08-11'],
  ['采列', 'Celje', '2026-08-11'],
  ['里加FC', 'Riga FC', '2026-08-11'],
  ['Gandzasar', 'Gandzasar FC', '2026-08-11'],
  ['巴黎圣曼', '巴黎圣日尔曼', '2026-08-11'],
  ['沃夫斯堡', '沃尔夫斯堡', '2026-08-13'],
  ['斯特拉斯', '斯特拉斯堡', '2026-08-13'],
  ['雷克维京', '雷克雅未克维京人', '2026-08-13'],
  ['斯海杜克', '斯普利特海杜克', '2026-08-13'],
  ['布拉迪斯', 'Slovan', '2026-08-13'],
  ['Arges', 'FC Arges Pitesti', '2026-08-13'],
  ['韦斯特曼', 'IBV Vestmannaeyjar', '2026-08-13'],
  ['IA', 'IA Akranes', '2026-08-13'],
  ['Thor', 'Thor Akureyri', '2026-08-13'],
  ['KA', 'KA Akureyri', '2026-08-13'],
  ['埃沃斯堡', 'SV Elversberg', '2026-08-13'],
  ['曼海姆', 'Waldhof Mannheim', '2026-08-13'],
  ['Xamax', 'Neuchatel Xamax', '2026-08-13'],
  ['La Louviere', 'RAAL La Louviere', '2026-08-13'],
  ['Volos NFC', 'NFC Volos', '2026-08-09'],
  ['Volos NFC', 'Volos', '2026-08-09'],
  ['SBV精英', 'SBV Excelsior', '2026-08-08'],
  ['布雷达', 'NAC', '2026-08-08'],
  ['鹿斯巴达', '鹿特丹斯巴达', '2026-08-08'],
  ['葡竞技', 'Atlético CP', '2026-08-08'],
  ['科莫', 'Como 1907', '2026-08-08'],
  ['鲁斯塔尼亚', 'Lusitânia Lourosa', '2026-08-08'],
  ['埃斯托里', 'GD Estoril', '2026-08-08'],
  ['CF Os Belenenses', 'CF Belenenses', '2026-08-08'],
  ['登博思', 'FC Den Bosch', '2026-08-08'],
  ['登博思', '登博斯', '2026-08-08'],
  ['赫拉克勒斯', 'Heracles Almelo', '2026-08-08'],
  ['托林斯', 'União Torreense', '2026-08-08'],
  ['鲁斯塔尼亚', 'Lourosa', '2026-08-08'],
  ['鲁斯塔尼亚', 'Lusitânia', '2026-08-08'],
  ['卡尔马', 'Kalmar', '2026-08-08'],
  ['迈季宽广', 'Al Fayha Club', '2026-08-08'],
  ['阿尔梅勒', 'Almere', '2026-08-08'],
  ['葡国民', 'Nacional Madeira', '2026-08-08'],
  ['葡国民', 'C.D. Nacional', '2026-08-09'],
  ['葡国民', 'CD Nacional', '2026-08-09'],
  ['Camacha', 'AD Camacha', '2026-08-08'],
  ['雷克斯欧', 'Leixoes', '2026-08-08'],
  ['阿维斯SAD', 'AVS', '2026-08-08'],
  ['科英布拉', 'Académica Coimbra', '2026-08-08'],
  ['Benfica II', 'Benfica B', '2026-08-08'],
  ['阿马多拉', 'Estrela', '2026-08-08'],
  ['埃夫斯堡', '埃尔夫斯堡', '2026-08-08'],
  ['OFI', 'OFI Creta', '2026-08-08'],
  ['OFI', 'OFI Crete', '2026-08-08'],
  ['奥斯', 'TOP Oss', '2026-08-08'],
  ['埃因霍温', 'PSV埃因霍温', '2026-08-08'],
  ['梅赫伦', 'Malines', '2026-08-08'],
  ['埃因FC', 'FC Eindhoven', '2026-08-08'],
  ['迅速男孩', 'Quick Boys', '2026-08-08'],
  ['沙维什', 'GD Chaves', '2026-08-08'],
  ['吉达国民', 'Al Ahli Jeddah', '2026-08-08'],
  ['吉达国民', '吉阿赫利', '2026-08-08'],
  ['葡国民', '葡萄牙国民', '2026-08-08'],
  ['Lierse K', 'K. Lierse SK', '2026-08-08'],
  ['奥林匹亚', 'Olympiakos', '2026-08-08'],
  ['根特', 'La Gantoise', '2026-08-08'],
  ['特里波利', 'Asteras Tripoli', '2026-08-08'],
  ['里斯本', '里斯本竞技', '2026-08-08'],
  ['利雅胜利', 'Al Nassr Riyadh', '2026-08-14'],
  ['Naval 1893', 'Naval', '2026-08-08'],
  ['诺丁汉', '诺丁汉森林', '2026-08-08'],
  ['马奇科', 'AD Machico', '2026-08-08'],
  ['SV梅尔森', 'SV Meerssen', '2026-08-08'],
  ['VOC', 'VOC Rotterdam', '2026-08-08'],
  ['贾兹拉', 'Jazira Abu Dhabi', '2026-08-08'],
  ['赫拉克勒', '赫拉克勒斯', '2026-08-08'],
  ['伏伊伏丁', '伏伊伏丁那', '2026-08-08'],
  ['雅典AEK', 'AEK', '2026-08-08'],
  ['瓦杜兹', 'FC Vaduz', '2026-08-08'],
  ['盖斯', '哥德堡盖斯', '2026-08-08'],
  ['勒芬', 'Louvain', '2026-08-08'],
  ['DAC 1904', 'DAC 1904 Dunajska Streda', '2026-08-08'],
  ['费伦茨', '费伦茨瓦罗斯', '2026-08-08'],
  ['洛特', 'Sportfreunde Lotte', '2026-08-08'],
  ['洛特', 'SF Lotte', '2026-08-08'],
  ['维拉', 'São João de Ver', '2026-08-08'],
  ['维拉', '阿斯顿维拉', '2026-08-08'],
  ['伯明翰', 'Birmingham City', '2026-08-08'],
  ['伯明翰', 'Birmingham', '2026-08-08'],
  ['阿维SAD', '阿维斯SAD', '2026-08-08'],
  ['Sporting CP II', 'Sporting Lisboa B', '2026-08-09'],
  ['Sporting CP II', 'Sporting CP B', '2026-08-09'],
  ['Mafra', 'CD Mafra', '2026-08-08'],
  ['莱里亚', '莱里雅', '2026-08-09'],
  ['莱里亚', 'Uniao Leiria', '2026-08-09'],
  ['莱里亚', 'Uniao de Leiria', '2026-08-09'],
  ['阿马兰蒂', 'Amarante FC', '2026-08-09'],
  ['天狼星', 'Sirius IK', '2026-08-09'],
  ['布鲁马波', 'Brommapojkarna', '2026-08-09'],
  ['布鲁马波', 'IF Brommapojkarna', '2026-08-09'],
  ['瓦兹姆', 'Varzim SC', '2026-08-08'],
  ['Paredes', 'USC Paredes', '2026-08-08'],
  ['吉达国民', 'Ahli', '2026-08-08'],
  ['Felgueiras 1932', 'FC Felgueiras', '2026-08-08'],
  ['维尔塔', '维戈塞尔塔', '2026-08-08'],
  ['斯托克城', 'Stoke', '2026-08-08'],
  ['勒卡', 'Leca', '2026-08-08'],
  ['马奇科', 'Machico', '2026-08-08'],
  ['比肖特VA', 'K Beerschot VA', '2026-08-08'],
  ['克拉约瓦', 'Universitatea Craiova', '2026-08-08'],
  ['利雅胜利', '利亚胜利', '2026-08-14'],
  ['利雅胜利', '利雅胜利', '2026-08-14'],
  ['热刺', '托特纳姆热刺', '2026-08-14'],
  ['热刺', '热刺', '2026-08-14'],
  ['马竞', '马德里竞技', '2026-08-14'],
  ['马竞', '马竞', '2026-08-14'],
  ['巴利亚多', 'Valladolid', '2026-08-14'],
  ['特内里费', 'Tenerife', '2026-08-14'],
  ['雷丁', 'Reading', '2026-08-14'],
  ['休达', 'AD Ceuta', '2026-08-14'],
  ['皇马', '皇家马德里', '2026-08-14'],
  ['皇马', '皇马', '2026-08-14'],
  ['Paju', 'Paju Frontier', '2026-08-14'],
  ['Paju', 'Paju', '2026-08-14'],
  ['桑坦德', 'Racing Santander', '2026-08-14'],
  ['桑坦德', '桑坦德竞技', '2026-08-14'],
  ['桑坦德', '桑坦德', '2026-08-14'],
  ['伍尔弗', '狼队', '2026-08-14'],
  ['伍尔弗', '伍尔弗', '2026-08-14'],
  ['毕尔巴鄂', '毕尔巴鄂竞技', '2026-08-14'],
  ['毕尔巴鄂', '毕尔巴鄂', '2026-08-14'],
  ['加拉塔萨', '加拉塔萨雷', '2026-08-14'],
  ['加拉塔萨', '加拉塔萨', '2026-08-14'],
  ['莱万特', 'Levante UD', '2026-08-14'],
  ['莱万特', '莱万特', '2026-08-14'],
  ['考文垂', 'Coventry City', '2026-08-14'],
  ['萨瓦德尔', 'CE Sabadell', '2026-08-14'],
  ['萨瓦德尔', 'Sabadell', '2026-08-14'],
  ['萨瓦德尔', '萨瓦德尔', '2026-08-14'],
  ['波城FC', 'Pau', '2026-08-14'],
  ['Olot', 'UE Olot', '2026-08-14'],
  ['Olot', 'Olot', '2026-08-14'],
  ['阿瓦塞特', 'Albacete', '2026-08-14'],
  ['胡巴卡德', 'Al Qadsiah', '2026-08-14'],
  ['谢菲联', 'Sheff Utd', '2026-08-14'],
  ['卡斯特隆', 'CD Castellón', '2026-08-14'],
  ['卡斯特隆', '卡斯特隆', '2026-08-14'],
  ['埃瓦尔', 'SD Eibar', '2026-08-15'],
  ['埃瓦尔', '埃瓦尔', '2026-08-15'],
  ['阿拉维斯', 'Alavés', '2026-08-14'],
  ['阿拉维斯', 'Deportivo Alavés', '2026-08-14'],
  ['阿拉维斯', 'Deportivo Alaves', '2026-08-14'],
  ['阿拉维斯', '阿拉维斯', '2026-08-14'],
  ['拉科', '拉科鲁尼亚', '2026-08-18'],
  ['卢戈', 'CD Lugo', '2026-07-30'],
  ['奥维耶多', '奥维耶多', '2026-08-18'],
  ['奥维耶多', '皇家奥维耶多', '2026-08-18'],
  ['奥维耶多', 'Real Oviedo', '2026-08-18'],
  ['Compostela', 'SD Compostela', '2026-07-18'],
  ['柔佛', 'Johor Darul Takzim', '2026-07-24'],
  ['柔佛', "Darul Ta'zim", '2026-07-24'],
  ['凯萨酋长', 'Kaizer Chiefs', '2026-07-18'],
  ['凯萨酋长', 'Chiefs', '2026-07-18'],
  ['圣图尔登', 'Sint-Truiden', '2026-08-18'],
  ['圣图尔登', 'Sint-Truidense', '2026-08-18'],
  ['萨姆松', '萨姆松体育', '2026-08-18'],
  ['萨姆松', 'Samsunspor', '2026-08-18'],
  ['萨迪纳摩', '萨格勒布迪纳摩', '2026-08-18'],
  ['FK Kauno Zalgiris', 'FK Kauno Zalgiris', '2026-08-18'],
  ['斯拉文', '斯拉文贝鲁波', '2026-08-18'],
  ['斯拉文', 'NK Slaven Belupo', '2026-08-18'],
  ['科佩尔', 'Koper', '2026-08-18'],
  ['科佩尔', 'FC Koper', '2026-08-18'],
  ['萨普斯堡', '萨尔普斯堡', '2026-08-18'],
  ['萨普斯堡', 'Sarpsborg 08', '2026-08-18'],
  ['克里斯蒂', '克里斯蒂安松', '2026-08-18'],
  ['克里斯蒂', 'Kristiansund', '2026-08-18'],
  ['克里斯蒂', 'Kristiansund BK', '2026-08-18'],
  ['里斯本', 'Sporting', '2026-08-19'],
  ['杜塞多夫', 'Düsseldorf', '2026-08-19'],
  ['基迪纳摩', 'Dynamo Kiev', '2026-08-19'],
  ['鲁容贝罗', 'Ruzomberok', '2026-08-19'],
  ['Nafta', 'Nafta 1903', '2026-08-19'],
  ['埃沃斯堡', 'Elversberg', '2026-08-19'],
  ['曼城', '曼彻斯特城', '2026-08-19'],
  ['吉达联合', 'Ittihad Jeddah', '2026-08-19'],
  ['莱切斯特', 'Leicester', '2026-08-19']
].map(([standardName, aliasName, lastSeenDate = '2026-08-04']) => ({
  competition: '*',
  standardName,
  aliasName,
  lastSeenDate,
  source: 'MANUAL'
}))

const USER_REQUESTED_SCOPED_TEAM_ALIASES = [
  ['EREDIVISIE', '鹿斯巴达', 'Sparta'],
  ['CLUB_OFFICIAL_OTHER', '鹿斯巴达', 'Sparta'],
  ['CLUB_FRIENDLY', '鹿斯巴达', 'Sparta'],
  ['CLUB_OFFICIAL_OTHER', '瓦雷赫姆', 'ZW'],
  ['CLUB_FRIENDLY', '瓦雷赫姆', 'ZW']
].map(([competition, standardName, aliasName]) => ({
  competition,
  standardName,
  aliasName,
  lastSeenDate: '2026-08-08',
  source: 'MANUAL'
}))

const REQUESTED_DOMESTIC_COMPETITION_ALIASES = [
  ['安养FC', 'FC Anyang', 'VERIFIED_SPORTTERY'],
  ['城南FC', 'Seongnam FC'],
  ['大邱FC', 'Daegu FC'],
  ['大田市民', 'Daejeon Hana Citizen', 'VERIFIED_SPORTTERY'],
  ['釜山偶像', "Busan I'Park"],
  ['富川FC', 'Bucheon FC 1995', 'VERIFIED_SPORTTERY'],
  ['光州FC', 'Gwangju FC', 'VERIFIED_SPORTTERY'],
  ['金浦FC', 'Gimpo FC', 'VERIFIED_ALIAS'],
  ['济州SK', 'Jeju SK', 'VERIFIED_SPORTTERY'],
  ['济州SK', 'Jeju United'],
  ['济州SK', '济州联'],
  ['江原FC', 'Gangwon FC', 'VERIFIED_SPORTTERY'],
  ['金泉尚武', 'Gimcheon Sangmu', 'VERIFIED_SPORTTERY'],
  ['金泉尚武', 'Sangju Sangmu'],
  ['浦项制铁', 'Pohang Steelers', 'VERIFIED_SPORTTERY'],
  ['庆南FC', 'Gyeongnam FC'],
  ['全北现代', 'Jeonbuk Hyundai Motors FC', 'VERIFIED_SPORTTERY'],
  ['全南天龙', 'Jeonnam Dragons'],
  ['仁川联', 'Incheon United', 'VERIFIED_SPORTTERY'],
  ['仁川联', 'Incheon', 'VERIFIED_ALIAS'],
  ['首尔FC', 'FC Seoul', 'VERIFIED_SPORTTERY'],
  ['首尔FC', 'Seoul', 'VERIFIED_ALIAS'],
  ['首尔衣恋', 'Seoul E-Land FC', 'VERIFIED_ALIAS'],
  ['水原FC', 'Suwon FC'],
  ['水原三星', 'Suwon Samsung Bluewings'],
  ['忠南牙山', 'Chungnam Asan FC', 'VERIFIED_ALIAS'],
  ['蔚山现代', 'Ulsan HD FC', 'VERIFIED_SPORTTERY'],
  ['蔚山现代', 'Ulsan HD', 'VERIFIED_ALIAS'],
  ['蔚山现代', 'Ulsan Hyundai'],
  ['埃克纳斯', 'EIF Ekenas'],
  ['洪卡', 'FC Honka Espoo'],
  ['拉赫蒂', 'FC Lahti', 'VERIFIED_SPORTTERY'],
  ['赫尔辛基IFK', 'HIFK Helsinki'],
  ['韦斯屈莱', 'JJK Jyväskylä'],
  ['KPV科科拉', 'KPV Kokkola'],
  ['迈帕', 'MyPa'],
  ['PK35万塔', 'PK-35 Vantaa'],
  ['PS凯米', 'PS Kemi'],
  ['罗瓦涅米', 'RoPS Rovaniemi', 'VERIFIED_SPORTTERY'],
  ['TPS图尔库', 'TPS Turku'],
  ['奥尔堡', 'AaB Aalborg', 'VERIFIED_SPORTTERY'],
  ['霍森斯', 'AC Horsens'],
  ['奥胡斯', 'AGF Aarhus', 'VERIFIED_SPORTTERY'],
  ['埃斯比约', 'Esbjerg fB'],
  ['腓特烈', 'FC Fredericia', 'VERIFIED_SPORTTERY'],
  ['赫尔辛格', 'FC Helsingor'],
  ['西希兰', 'FC Vestsjælland'],
  ['霍布罗', 'Hobro IK'],
  ['哈维德夫', 'Hvidovre IF'],
  ['林比', 'Lyngby BK'],
  ['欧登塞', 'Odense BK'],
  ['兰纳斯', 'Randers FC', 'VERIFIED_SPORTTERY'],
  ['锡尔克堡', 'Silkeborg IF', 'VERIFIED_SPORTTERY'],
  ['桑德捷', 'SønderjyskE', 'VERIFIED_SPORTTERY'],
  ['瓦埃勒', 'Vejle BK'],
  ['文德斯尔', 'Vendsyssel FF'],
  ['维堡', 'Viborg FF'],
  ['阿尔卡', 'Arka Gdynia'],
  ['克拉科维亚', 'Cracovia'],
  ['贝乌哈图夫', 'GKS Bełchatów'],
  ['卡托维兹', 'GKS Katowice'],
  ['莱茨纳', 'Górnik Łęczna'],
  ['扎布热矿工', 'Górnik Zabrze'],
  ['比亚韦', 'Jagiellonia', 'VERIFIED_SPORTTERY'],
  ['格但斯克', 'Lechia Gdańsk', 'VERIFIED_SPORTTERY'],
  ['LKS罗兹', 'ŁKS Łódź'],
  ['莱格尼察', 'Miedź Legnica'],
  ['莫托路宾', 'Motor Lublin'],
  ['特马利卡', 'Nieciecza KS'],
  ['皮亚斯特', 'Piast Gliwice'],
  ['保德比斯基', 'Podbeskidzie'],
  ['什切青波贡', 'Pogoń Szczecin'],
  ['涅波沃米采', 'Puszcza Niepołomice'],
  ['拉多米亚克', 'Radomiak Radom'],
  ['琴斯托霍', 'Raków', 'VERIFIED_SPORTTERY'],
  ['罗切霍茹夫', 'Ruch Chorzow'],
  ['桑德克亚', 'Sandecja'],
  ['弗罗茨瓦夫', 'Śląsk Wrocław'],
  ['梅莱茨钢铁', 'Stal Mielec'],
  ['瓦塔波兹南', 'Warta Poznań'],
  ['维德祖罗兹', 'Widzew Łódź'],
  ['克拉科夫', 'Wisła Kraków'],
  ['普沃茨克', 'Wisła Płock'],
  ['卢宾扎格勒比', 'Zaglebie Lubin'],
  ['索斯诺维茨', 'Zaglebie Sosnowiec'],
  ['萨维斯沙', 'Zawisza Bydgoszcz'],
  ['阿德米拉', 'Admira Wacker'],
  ['奥地利克拉根福', 'Austria Klagenfurt'],
  ['奥地利卢斯特瑙', 'Austria Lustenau'],
  ['BW林茨', 'Blau Weiss Linz'],
  ['格拉茨AK', 'Grazer AK'],
  ['哈特贝格', 'Hartberg'],
  ['LASK林茨', 'LASK Linz', 'VERIFIED_SPORTTERY'],
  ['马特斯堡', 'Mattersburg'],
  ['阿尔塔奇', 'SCR Altach'],
  ['圣珀尔滕', 'SKN St. Pölten'],
  ['格罗迪SV', 'SV Grödig'],
  ['里德', 'SV Ried'],
  ['瓦克蒂罗尔', 'Wacker Innsbruck'],
  ['维也纳新城', 'Wiener Neustadt'],
  ['WSG蒂罗尔', 'WSG Swarovski Tirol'],
  ['WSG蒂罗尔', 'WSG Tirol'],
  ['WSG蒂罗尔', 'WSG Wattens'],
  ['艾尔德里联', 'Airdrieonians FC'],
  ['阿布罗斯', 'Arbroath FC'],
  ['艾尔联', 'Ayr United'],
  ['邓弗姆林', 'Dunfermline'],
  ['摩顿', 'Greenock Morton'],
  ['南部女王', 'Queen of the South'],
  ['女王公园', "Queen's Park FC"],
  ['拉茨流浪', 'Raith Rovers'],
  ['阿达纳德米尔体育', 'Adana Demirspor'],
  ['阿达纳体育', 'Adanaspor'],
  ['阿卡希萨尔', 'Akhisar Bld.'],
  ['阿兰亚体育', 'Alanyaspor'],
  ['阿尔泰', 'Altay SK Izmir'],
  ['安卡拉古库', 'Ankaragücü'],
  ['安塔利亚体育', 'Antalyaspor'],
  ['巴里科斯士邦', 'Balikesirspor'],
  ['埃尔祖鲁姆体育', 'BB Erzurumspor'],
  ['博德鲁姆', 'Bodrum FK'],
  ['布尔萨体育', 'Bursaspor'],
  ['代尼兹利体育', 'Denizlispor'],
  ['埃斯基谢希尔体育', 'Eskişehirspor'],
  ['埃于普体育', 'Eyüpspor'],
  ['卡拉古鲁克', 'Fatih Karagümrük'],
  ['加济安泰普', 'Gaziantep FK'],
  ['加济安泰普体育', 'Gaziantepspor*'],
  ['根克勒比利吉', 'Genclerbirligi'],
  ['吉雷松体育', 'Giresunspor'],
  ['哥兹塔比', 'Göztepe Izmir'],
  ['哈塔伊体育', 'Hatayspor'],
  ['伊斯坦布', 'Istanbul Basaksehir', 'VERIFIED_SPORTTERY'],
  ['伊斯坦布尔体育', 'İstanbulspor AŞ'],
  ['卡拉比克体育', 'Kardemir Karabükspor'],
  ['卡斯帕萨', 'Kasımpaşa SK'],
  ['埃尔吉耶斯体育', 'Kayseri Erciyesspor'],
  ['开塞利体育', 'Kayserispor'],
  ['科贾埃利体育', 'Kocaelispor'],
  ['梅尔辛', 'Mersin İdmanyurdu'],
  ['奥斯曼', 'Osmanlispor FK'],
  ['佩迪卡斯堡', 'Pendikspor'],
  ['里泽体育', 'Rizespor'],
  ['萨姆松体育', 'Samsunspor'],
  ['乌姆拉尼耶体育', 'Ümraniyespor'],
  ['马拉蒂亚体育', 'Yeni Malatyaspor'],
  ['巴尔马祖瓦罗斯', 'Balmazujvaros'],
  ['贝凯什乔包', 'Bekescsaba Elore'],
  ['布达弗基', 'Budafoki MTE'],
  ['布达佩斯捍卫者', 'Budapest Honved'],
  ['德布勒森', 'Debrecen VSC', 'VERIFIED_SPORTTERY'],
  ['迪欧斯捷尔', 'Diosgyori VTK'],
  ['多瑙新城', 'Dunaújváros PASE'],
  ['维迪奥顿', 'Fehervar FC'],
  ['费伦茨瓦罗斯', 'Ferencvaros TC', 'VERIFIED_SPORTTERY'],
  ['吉尔莫特', 'Gyirmót FC Győr'],
  ['哈拉达斯', 'Haladas'],
  ['卡波斯瓦里', 'Kaposvari Rakoczi'],
  ['卡辛巴西卡', 'Kazincbarcikai SC'],
  ['凯奇凯梅特', 'Kecskemeti TE'],
  ['基斯华达', 'Kisvárda FC'],
  ['隆巴德', 'Lombard Papa'],
  ['梅索科菲德', 'Mezőkövesd FC'],
  ['布达佩斯MTK', 'MTK Budapest'],
  ['尼赖吉哈佐', 'Nyiregyhaza'],
  ['佩奇', 'Pécsi MFC'],
  ['普斯卡什学院', 'Puskás Akadémia'],
  ['新佩斯', 'Ujpest FC'],
  ['华萨斯', 'Vasas FC'],
  ['维迪奥顿', 'Videoton FC'],
  ['维迪奥顿', 'Vidi FC'],
  ['萨拉格斯基', 'Zalaegerszegi TE'],
  ['希巴利亚', 'Cibalia Vinkovci'],
  ['戈里察', 'HNK Gorica'],
  ['里耶卡', 'HNK Rijeka', 'VERIFIED_SPORTTERY'],
  ['扎达尔', 'HNK Zadar'],
  ['克罗地亚志愿队', 'Hrvatski Dragovoljac'],
  ['萨格勒布国际', 'Inter Zaprešić'],
  ['伊斯特拉1961', 'Istra 1961'],
  ['萨格勒布火车头', 'Lokomotiva Zagreb'],
  ['奥西耶克', 'NK Osijek', 'VERIFIED_SPORTTERY'],
  ['斯拉文贝鲁波', 'NK Slaven Belupo'],
  ['瓦拉日丁', 'NK Varaždin'],
  ['萨格勒布', 'NK Zagreb'],
  ['奥里耶特', 'Orijent Rijeka'],
  ['RNK斯普利特', 'RNK Split'],
  ['鲁德什', 'Rudeš Zagreb'],
  ['希贝尼克', 'Šibenik'],
  ['武科瓦尔', 'Vukovar 1991']
].map(([standardName, aliasName, source = 'VERIFIED_ALIAS']) => ({
  competition: 'CLUB_OFFICIAL_OTHER',
  standardName,
  aliasName,
  lastSeenDate: '2026-07-23',
  source
}))

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let inQuotes = false
  for (let index = 0; index < text.length; index += 1) {
    const character = text[index]
    const next = text[index + 1]
    if (inQuotes) {
      if (character === '"' && next === '"') {
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
  if (field || row.length) {
    row.push(field.replace(/\r$/, ''))
    rows.push(row)
  }
  const headers = rows.shift().map((header, index) => index === 0 ? header.replace(/^\uFEFF/, '') : header)
  return rows
    .filter(values => values.length === headers.length)
    .map(values => Object.fromEntries(headers.map((header, index) => [header, values[index]])))
}

function escapeCsv(value) {
  const text = String(value ?? '')
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function canonicalName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]/gu, '')
}

function canonicalClubIdentity(value) {
  const ignoredTokens = new Set([
    '1', 'AC', 'AFC', 'AS', 'CA', 'CD', 'CF', 'FC', 'FK', 'IF', 'JC', 'SC', 'SK', 'SV', 'TC', 'TSG', 'DE'
  ])
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]+/gu, ' ')
    .trim()
    .split(/\s+/)
    .filter(token => token && !ignoredTokens.has(token))
    .join('')
}

function isReserveOrYouthTeam(value) {
  return /(?:^|[^A-Z0-9])(?:B|II|III|2|U\d{2}|RESERVES?)(?:$|[^A-Z0-9])/i.test(
    String(value ?? '').normalize('NFKD').replace(/\p{Mark}/gu, ''))
}

function aliasType(value, standardName) {
  if (value === standardName) {
    return 'STANDARD'
  }
  if (/\p{Script=Han}/u.test(value)) {
    return 'ZH'
  }
  return /^[A-Z0-9.-]{2,8}$/.test(value) ? 'ABBREVIATION' : 'EN'
}

function rowIsNewer(candidate, current) {
  const candidatePriority = SOURCE_PRIORITY.get(candidate.source) ?? 0
  const currentPriority = SOURCE_PRIORITY.get(current.source) ?? 0
  return candidatePriority > currentPriority
    || candidatePriority === currentPriority && candidate.last_seen_date >= current.last_seen_date
}

function register(rowsByKey, row) {
  const key = `${row.competition}|${canonicalName(row.alias_team_name)}`
  if (!canonicalName(row.alias_team_name) || !row.standard_team_name) {
    return
  }
  const current = rowsByKey.get(key)
  if (!current || rowIsNewer(row, current)) {
    rowsByKey.set(key, row)
  }
}

function registerAlias(rowsByKey, competition, standardName, aliasName, source, lastSeenDate) {
  const row = {
    competition,
    standard_team_name: standardName,
    alias_team_name: aliasName,
    alias_type: aliasType(aliasName, standardName),
    source,
    last_seen_date: lastSeenDate
  }
  register(rowsByKey, row)
  if (competition !== '*') {
    register(rowsByKey, { ...row, competition: '*' })
  }
}

function resolve(rowsByKey, competition, aliasName) {
  const key = canonicalName(aliasName)
  const competitionMapping = rowsByKey.get(`${competition}|${key}`)
  const globalMapping = rowsByKey.get(`*|${key}`)
  if (!competitionMapping) {
    return globalMapping?.standard_team_name ?? null
  }
  if (!globalMapping) {
    return competitionMapping.standard_team_name
  }
  const competitionPriority = SOURCE_PRIORITY.get(competitionMapping.source) ?? 0
  const globalPriority = SOURCE_PRIORITY.get(globalMapping.source) ?? 0
  return globalPriority > competitionPriority
    ? globalMapping.standard_team_name
    : competitionMapping.standard_team_name
}

function buildOddsMappings(oddsRows, rowsByKey) {
  const identities = new Map()
  for (const row of oddsRows) {
    for (const side of ['home', 'away']) {
      const chineseName = String(row[`${side}_team_cn`] ?? '').trim()
      const englishName = String(row[`${side}_team_en`] ?? '').trim()
      if (!chineseName) {
        continue
      }
      const source = String(row.match_id ?? '').startsWith('HIS-SPT-')
        || String(row.sporttery_match_number ?? '').trim()
        ? 'VERIFIED_SPORTTERY'
        : 'HISTORICAL_ODDS'
      const identity = canonicalName(englishName) || canonicalName(chineseName)
      const key = `${row.competition}|${identity}`
      const current = identities.get(key) ?? {
        competition: row.competition,
        names: new Map(),
        latest: null,
        source: 'HISTORICAL_ODDS'
      }
      for (const name of [chineseName, englishName].filter(Boolean)) {
        const date = current.names.get(name) ?? ''
        if (row.match_date >= date) {
          current.names.set(name, row.match_date)
        }
      }
      const currentPriority = SOURCE_PRIORITY.get(current.source) ?? 0
      const candidatePriority = SOURCE_PRIORITY.get(source) ?? 0
      if (!current.latest
          || candidatePriority > currentPriority
          || candidatePriority === currentPriority && row.match_date >= current.latest.date) {
        current.latest = { name: chineseName, date: row.match_date }
        current.source = source
      }
      identities.set(key, current)
    }
  }
  for (const identity of identities.values()) {
    for (const [name, date] of identity.names.entries()) {
      registerAlias(rowsByKey, identity.competition, identity.latest.name, name, identity.source, date)
    }
    registerAlias(
      rowsByKey,
      identity.competition,
      identity.latest.name,
      identity.latest.name,
      identity.source,
      identity.latest.date
    )
  }
}

function addHistoricalMatchNames(historyRows, rowsByKey) {
  for (const row of historyRows) {
    for (const name of [row.home_team_cn, row.away_team_cn]) {
      const standardName = resolve(rowsByKey, row.competition, name) ?? name
      registerAlias(rowsByKey, row.competition, standardName, name, 'HISTORICAL_MATCHES', row.match_date)
    }
  }
}

function selectableCompetition(sourceCompetition, fallback) {
  const sourceName = String(sourceCompetition ?? '').trim()
  if (sourceName.startsWith('瑞超') || sourceName.startsWith('瑞典超')) {
    return 'SWEDISH_ALLSVENSKAN'
  }
  if (sourceName.startsWith('芬超')) {
    return 'FINNISH_VEIKKAUSLIIGA'
  }
  if (sourceName.startsWith('韩职')
      || sourceName.startsWith('韩国职业联赛')
      || sourceName.startsWith('韩国杯')
      || sourceName.startsWith('韩足总杯')) {
    return 'K_LEAGUE_1'
  }
  if (sourceName.startsWith('苏足总杯')
      || sourceName.startsWith('苏格兰足总杯')
      || sourceName.startsWith('苏格兰杯')) {
    return 'SCOTTISH_FA_CUP'
  }
  return fallback
}

function addPreservedMappings(rowsByKey, existingRows) {
  for (const row of existingRows) {
    const key = `${row.competition}|${canonicalName(row.alias_team_name)}`
    if (!SOURCE_PRIORITY.has(row.source) || REJECTED_MAPPING_KEYS.has(key)) {
      continue
    }
    register(rowsByKey, row)
  }
}

function namesMatch(rowsByKey, competition, chineseName, englishName, sportteryName) {
  const target = canonicalName(resolve(rowsByKey, competition, sportteryName) ?? sportteryName)
  return [chineseName, englishName]
    .map(name => canonicalName(resolve(rowsByKey, competition, name) ?? name))
    .some(source => source && target && (source === target
      || Math.min(source.length, target.length) >= 4
        && (source.includes(target) || target.includes(source))))
}

function dayDistance(leftDate, rightDate) {
  return Math.abs((Date.parse(leftDate) - Date.parse(rightDate)) / 86400000)
}

function hasScores(match) {
  return match.homeScore !== null
    && match.homeScore !== undefined
    && match.awayScore !== null
    && match.awayScore !== undefined
}

function scoresMatch(schedule, sportteryEntry) {
  return hasScores(schedule)
    && hasScores(sportteryEntry)
    && schedule.homeScore === sportteryEntry.homeScore
    && schedule.awayScore === sportteryEntry.awayScore
}

function calculateScheduleMatchScore(rowsByKey, schedule, sportteryEntry) {
  const homeMatches = namesMatch(
    rowsByKey,
    schedule.competition,
    schedule.homeTeamCn,
    schedule.homeTeamEn,
    sportteryEntry.homeTeam)
  const awayMatches = namesMatch(
    rowsByKey,
    schedule.competition,
    schedule.awayTeamCn,
    schedule.awayTeamEn,
    sportteryEntry.awayTeam)
  const distance = dayDistance(schedule.matchDate, sportteryEntry.matchDate)
  const sameScore = scoresMatch(schedule, sportteryEntry)
  if (!homeMatches || !awayMatches) {
    const scoreUnavailable = sportteryEntry.homeScore === null
      || sportteryEntry.homeScore === undefined
      || sportteryEntry.awayScore === null
      || sportteryEntry.awayScore === undefined
    return distance === 0 && (homeMatches || awayMatches) && (sameScore || scoreUnavailable)
      ? 80
      : -1
  }
  return 100 + (sameScore ? 20 : 0) + (distance === 0 ? 6 : 2)
}

function findUniqueSchedule(rowsByKey, schedules, sportteryEntry) {
  const nearbySchedules = schedules
    .filter(schedule => schedule.competition === sportteryEntry.competition
      && dayDistance(schedule.matchDate, sportteryEntry.matchDate) <= 1)
  const leagueScopedSchedules = nearbySchedules.filter(schedule => (
    canonicalName(schedule.groupName) === canonicalName(sportteryEntry.leagueName)))
  const candidates = (leagueScopedSchedules.length ? leagueScopedSchedules : nearbySchedules)
    .map(schedule => ({
      schedule,
      score: calculateScheduleMatchScore(rowsByKey, schedule, sportteryEntry)
    }))
    .filter(candidate => candidate.score >= 70)
    .sort((left, right) => right.score - left.score)
  if (!candidates.length || candidates.length > 1 && candidates[0].score === candidates[1].score) {
    return null
  }
  return candidates[0].schedule
}

function findUniqueScheduleByResult(schedules, sportteryEntries, sportteryEntry) {
  if (!hasScores(sportteryEntry) || !canonicalName(sportteryEntry.leagueName)) {
    return null
  }
  const scheduleCandidates = schedules.filter(schedule => (
    schedule.competition === sportteryEntry.competition
      && schedule.matchDate === sportteryEntry.matchDate
      && canonicalName(schedule.groupName) === canonicalName(sportteryEntry.leagueName)
      && scoresMatch(schedule, sportteryEntry)))
  const sportteryCandidates = sportteryEntries.filter(entry => (
    entry.competition === sportteryEntry.competition
      && entry.matchDate === sportteryEntry.matchDate
      && canonicalName(entry.leagueName) === canonicalName(sportteryEntry.leagueName)
      && scoresMatch(entry, sportteryEntry)))
  return scheduleCandidates.length === 1 && sportteryCandidates.length === 1
    ? scheduleCandidates[0]
    : null
}

function addSportteryCacheMappings(rowsByKey, schedules, sportteryEntries) {
  for (const entry of sportteryEntries) {
    for (const standardName of [entry.homeTeam, entry.awayTeam].filter(Boolean)) {
      registerAlias(
        rowsByKey,
        entry.competition,
        standardName,
        standardName,
        'VERIFIED_SPORTTERY',
        entry.matchDate)
    }
  }

  let matchedFixtures = 0
  let resultOnlyMatchedFixtures = 0
  for (const entry of sportteryEntries) {
    let schedule = findUniqueSchedule(rowsByKey, schedules, entry)
    if (!schedule) {
      schedule = findUniqueScheduleByResult(schedules, sportteryEntries, entry)
      if (schedule) {
        resultOnlyMatchedFixtures += 1
      }
    }
    if (!schedule) {
      continue
    }
    for (const side of ['home', 'away']) {
      const standardName = entry[`${side}Team`]
      for (const aliasName of [
        schedule[`${side}TeamCn`],
        schedule[`${side}TeamEn`]
      ].filter(Boolean)) {
        registerAlias(
          rowsByKey,
          entry.competition,
          standardName,
          aliasName,
          'VERIFIED_SPORTTERY',
          entry.matchDate)
      }
    }
    matchedFixtures += 1
  }
  return { matchedFixtures, resultOnlyMatchedFixtures }
}

function addVerifiedSportteryEnglishAliases(rowsByKey) {
  for (const alias of [
    ...VERIFIED_SPORTTERY_ENGLISH_ALIASES,
    ...USER_REQUESTED_TEAM_ALIASES,
    ...USER_REQUESTED_SCOPED_TEAM_ALIASES,
    ...REQUESTED_DOMESTIC_COMPETITION_ALIASES
  ]) {
    registerAlias(
      rowsByKey,
      alias.competition,
      alias.standardName,
      alias.standardName,
      alias.source ?? 'VERIFIED_SPORTTERY',
      alias.lastSeenDate)
    registerAlias(
      rowsByKey,
      alias.competition,
      alias.standardName,
      alias.aliasName,
      alias.source ?? 'VERIFIED_SPORTTERY',
      alias.lastSeenDate)
  }
}

function addUniqueEnglishAliasVariants(rowsByKey, schedules) {
  const candidateRows = [...rowsByKey.values()]
    .filter(row => row.alias_type === 'EN'
      && /\p{Script=Han}/u.test(row.standard_team_name)
      && (SOURCE_PRIORITY.get(row.source) ?? 0) >= SOURCE_PRIORITY.get('HISTORICAL_ODDS'))
    .map(row => ({
      standardName: row.standard_team_name,
      aliasName: row.alias_team_name,
      identity: canonicalClubIdentity(row.alias_team_name)
    }))
    .filter(row => row.identity && !isReserveOrYouthTeam(row.aliasName))
  const candidatesByIdentity = new Map()
  for (const candidate of candidateRows) {
    const candidates = candidatesByIdentity.get(candidate.identity) ?? []
    candidates.push(candidate)
    candidatesByIdentity.set(candidate.identity, candidates)
  }

  const latestSchedulesByAlias = new Map()
  for (const schedule of schedules) {
    for (const side of ['home', 'away']) {
      const aliasName = String(schedule[`${side}TeamEn`] ?? '').trim()
      if (!/[A-Za-z]/.test(aliasName)
          || isReserveOrYouthTeam(aliasName)
          || resolve(rowsByKey, schedule.competition, aliasName) !== null) {
        continue
      }
      const key = `${schedule.competition}|${canonicalName(aliasName)}`
      const current = latestSchedulesByAlias.get(key)
      if (!current || schedule.matchDate >= current.lastSeenDate) {
        latestSchedulesByAlias.set(key, {
          competition: schedule.competition,
          aliasName,
          identity: canonicalClubIdentity(aliasName),
          lastSeenDate: schedule.matchDate
        })
      }
    }
  }

  let inferredAliases = 0
  for (const alias of latestSchedulesByAlias.values()) {
    if (!alias.identity) {
      continue
    }
    const candidates = candidatesByIdentity.get(alias.identity) ?? []
    const standards = new Set(candidates.map(candidate => candidate.standardName))
    if (standards.size !== 1) {
      continue
    }
    registerAlias(
      rowsByKey,
      alias.competition,
      [...standards][0],
      alias.aliasName,
      'VERIFIED_ALIAS',
      alias.lastSeenDate)
    inferredAliases += 1
  }
  return inferredAliases
}

function toCsv(rows) {
  return [
    HEADERS.join(','),
    ...rows.map(row => HEADERS.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

const existingMappingsArgument = process.argv.slice(2)
  .find(argument => argument.startsWith('--existing-mappings='))
const existingMappingsPath = existingMappingsArgument
  ? path.resolve(existingMappingsArgument.slice('--existing-mappings='.length))
  : outputPath
const supplementalOddsPaths = process.argv.slice(2)
  .filter(argument => argument.startsWith('--supplemental-odds='))
  .map(argument => path.resolve(argument.slice('--supplemental-odds='.length)))
const supplementalMappingPaths = process.argv.slice(2)
  .filter(argument => argument.startsWith('--supplemental-mappings='))
  .map(argument => path.resolve(argument.slice('--supplemental-mappings='.length)))
const [
  historyText,
  oddsText,
  existingMappingText,
  clubSchedulesText,
  sportteryCacheText,
  supplementalOddsTexts,
  supplementalMappingTexts
] = await Promise.all([
  fs.readFile(historicalMatchesPath, 'utf8'),
  fs.readFile(historicalOddsPath, 'utf8'),
  fs.readFile(existingMappingsPath, 'utf8').catch(() => ''),
  fs.readFile(clubSchedulesPath, 'utf8').catch(() => '[]'),
  fs.readFile(sportteryCachePath, 'utf8').catch(() => '{"entries":[]}'),
  Promise.all(supplementalOddsPaths.map(supplementalPath => fs.readFile(supplementalPath, 'utf8'))),
  Promise.all(supplementalMappingPaths.map(supplementalPath => fs.readFile(supplementalPath, 'utf8')))
])
const historyRows = parseCsv(historyText).map(row => ({
  ...row,
  competition: selectableCompetition(row.source_competition, row.competition)
}))
const oddsRows = parseCsv(oddsText)
const supplementalOddsRows = supplementalOddsTexts.flatMap(parseCsv)
const supplementalMappingRows = supplementalMappingTexts.flatMap(parseCsv)
const existingRows = existingMappingText ? parseCsv(existingMappingText) : []
const clubSchedules = JSON.parse(clubSchedulesText).map(schedule => ({
  ...schedule,
  competition: selectableCompetition(schedule.groupName, schedule.competition)
}))
const sportteryEntries = (JSON.parse(sportteryCacheText).entries ?? []).map(entry => ({
  ...entry,
  competition: selectableCompetition(entry.leagueName, entry.competition)
}))
const rowsByKey = new Map()
buildOddsMappings([...supplementalOddsRows, ...oddsRows], rowsByKey)
addPreservedMappings(rowsByKey, supplementalMappingRows)
addPreservedMappings(rowsByKey, existingRows)
const sportteryCacheMappingResult = addSportteryCacheMappings(
  rowsByKey,
  clubSchedules,
  sportteryEntries)
addVerifiedSportteryEnglishAliases(rowsByKey)
const inferredEnglishAliasVariants = addUniqueEnglishAliasVariants(rowsByKey, clubSchedules)
addHistoricalMatchNames(historyRows, rowsByKey)

const rows = [...rowsByKey.values()].sort((left, right) => (
  left.competition.localeCompare(right.competition)
  || left.standard_team_name.localeCompare(right.standard_team_name, 'zh-CN')
  || left.alias_team_name.localeCompare(right.alias_team_name, 'zh-CN')
))
await writeFileWithRetry(outputPath, `\uFEFF${toCsv(rows)}`)

const standards = new Set(rows.map(row => `${row.competition}|${row.standard_team_name}`))
const sources = Object.fromEntries([...new Set(rows.map(row => row.source))]
  .sort()
  .map(source => [source, rows.filter(row => row.source === source).length]))
console.log(JSON.stringify({
  outputPath,
  historicalMatchRows: historyRows.length,
  historicalOddsRows: oddsRows.length,
  supplementalHistoricalOddsRows: supplementalOddsRows.length,
  supplementalMappingRows: supplementalMappingRows.length,
  sportteryCacheRows: sportteryEntries.length,
  matchedSportteryCacheFixtures: sportteryCacheMappingResult.matchedFixtures,
  resultOnlyMatchedSportteryCacheFixtures: sportteryCacheMappingResult.resultOnlyMatchedFixtures,
  inferredEnglishAliasVariants,
  mappingRows: rows.length,
  standards: standards.size,
  sources
}, null, 2))
