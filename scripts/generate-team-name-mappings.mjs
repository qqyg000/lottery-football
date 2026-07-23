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
  ['MANUAL', 5],
  ['VERIFIED_SPORTTERY', 6]
])

const REJECTED_MAPPING_KEYS = new Set([
  ['EREDIVISIE', 'Odds BK'],
  ['LIGUE_1', 'Crusaders'],
  ['BUNDESLIGA', 'KR Reykjavik']
].map(([competition, aliasName]) => `${competition}|${canonicalName(aliasName)}`))

const VERIFIED_SPORTTERY_ENGLISH_ALIASES = [
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

const REQUESTED_DOMESTIC_COMPETITION_ALIASES = [
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
const historyRows = parseCsv(historyText)
const oddsRows = parseCsv(oddsText)
const supplementalOddsRows = supplementalOddsTexts.flatMap(parseCsv)
const supplementalMappingRows = supplementalMappingTexts.flatMap(parseCsv)
const existingRows = existingMappingText ? parseCsv(existingMappingText) : []
const clubSchedules = JSON.parse(clubSchedulesText)
const sportteryEntries = JSON.parse(sportteryCacheText).entries ?? []
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
