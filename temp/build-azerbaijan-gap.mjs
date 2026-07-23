import fs from 'node:fs/promises'

const rows = [
  ['PFL-5384', '2024-10-12', 'CLUB_OFFICIAL_OTHER', 'Shahdagh Gusar', 'Kür-Araz', 3, 1, false, 'OFFICIAL', '阿塞杯', '5384', 'https://pfl.az/api/v1/games/show/5384?withActions=true', 'https://pfl.az/storage/games/files/674424748db25_1732519028.pdf', '90分钟'],
  ['PFL-5451', '2024-10-12', 'CLUB_OFFICIAL_OTHER', 'Shafa Baku', 'Araz Saatlı', 2, 0, false, 'OFFICIAL', '阿塞杯', '5451', 'https://pfl.az/api/v1/games/show/5451?withActions=true', 'https://pfl.az/storage/games/files/6744293fa709c_1732520255.pdf', '90分钟'],
  ['PFL-5455', '2024-10-13', 'CLUB_OFFICIAL_OTHER', 'Simal FK', 'Qusar', 4, 1, false, 'OFFICIAL', '阿塞杯', '5455', 'https://pfl.az/api/v1/games/show/5455?withActions=true', 'https://pfl.az/storage/games/files/67443c3a362e0_1732525114.pdf', '90分钟'],
  ['PFL-5461', '2024-10-13', 'CLUB_OFFICIAL_OTHER', 'Gyogol FK', 'Quba', 2, 2, false, 'OFFICIAL', '阿塞杯', '5461', 'https://pfl.az/api/v1/games/show/5461?withActions=true', 'https://pfl.az/storage/games/files/674437a65147a_1732523942.pdf', '90分钟2:2；加时94分钟乌龙后终场2:3'],
  ['PFL-5463', '2024-10-13', 'CLUB_OFFICIAL_OTHER', 'Lerik', 'Agdas FK', 0, 4, false, 'OFFICIAL', '阿塞杯', '5463', 'https://pfl.az/api/v1/games/show/5463?withActions=true', 'https://pfl.az/storage/games/files/67443178800d3_1732522360.pdf', '90分钟'],
  ['PFL-5465', '2024-10-13', 'CLUB_OFFICIAL_OTHER', 'Sheki City', 'Shamkir', 0, 1, false, 'OFFICIAL', '阿塞杯', '5465', 'https://pfl.az/api/v1/games/show/5465?withActions=true', 'https://pfl.az/storage/games/files/674439510ba97_1732524369.pdf', '90分钟'],
  ['PFL-5468', '2024-10-14', 'CLUB_OFFICIAL_OTHER', 'Dinamo Baku', 'Füzuli', 4, 0, false, 'OFFICIAL', '阿塞杯', '5468', 'https://pfl.az/api/v1/games/show/5468?withActions=true', 'https://pfl.az/storage/games/files/67443e89a2393_1732525705.pdf', '90分钟'],
  ['PFL-6705', '2025-10-09', 'CLUB_OFFICIAL_OTHER', 'Sheki City', 'Araz Saatlı', 3, 2, false, 'OFFICIAL', '阿塞杯', '6705', 'https://pfl.az/api/v1/games/show/6705?withActions=true', 'https://pfl.az/storage/games/files/68e7ae3ac2367_1760013882.pdf', '90分钟'],
  ['PFL-6706', '2025-10-09', 'CLUB_OFFICIAL_OTHER', 'Qaradag Lokb', 'Agdas FK', 8, 0, false, 'OFFICIAL', '阿塞杯', '6706', 'https://pfl.az/api/v1/games/show/6706?withActions=true', 'https://pfl.az/storage/games/files/68e7b1c5be5c1_1760014789.pdf', '90分钟'],
  ['PFL-6707', '2025-10-09', 'CLUB_OFFICIAL_OTHER', 'Dinamo Baku', 'Kür-Araz', 2, 2, false, 'OFFICIAL', '阿塞杯', '6707', 'https://pfl.az/api/v1/games/show/6707?withActions=true', 'https://pfl.az/storage/games/files/68e7b965b6341_1760016741.pdf', '90分钟2:2；加时99分钟进球后终场3:2'],
  ['PFL-6708', '2025-10-09', 'CLUB_OFFICIAL_OTHER', 'Agstafa Gencleri', 'Şirvan', 1, 0, false, 'OFFICIAL', '阿塞杯', '6708', 'https://pfl.az/api/v1/games/show/6708?withActions=true', 'https://pfl.az/storage/games/files/68e7ad24f0a8d_1760013604.pdf', '90分钟'],
  ['PFL-6704', '2025-10-10', 'CLUB_OFFICIAL_OTHER', 'Gyogol FK', 'Shamkir', 1, 0, false, 'OFFICIAL', '阿塞杯', '6704', 'https://pfl.az/api/v1/games/show/6704?withActions=true', 'https://pfl.az/storage/games/files/68e8fc94ead16_1760099476.pdf', '90分钟'],
  ['PFL-6709', '2025-10-10', 'CLUB_OFFICIAL_OTHER', 'Quba', 'Xankendi FK', 1, 2, false, 'OFFICIAL', '阿塞杯', '6709', 'https://pfl.az/api/v1/games/show/6709?withActions=true', 'https://pfl.az/storage/games/files/68e8fa3e9bdeb_1760098878.pdf', '90分钟'],
  ['PFL-6777', '2026-05-28', 'CLUB_OFFICIAL_OTHER', '盖贝莱', 'Mingachevir FK', 2, 0, false, 'OFFICIAL', '阿塞超', '6777', 'https://pfl.az/api/v1/games/show/6777?withActions=true', 'https://pfl.az/storage/games/files/6a187831f3e91_1779988529.pdf', '90分钟；阿塞超升降级附加赛']
]

const projectHeaders = ['match_id', 'match_date', 'competition', 'home_team_cn', 'away_team_cn', 'home_score', 'away_score', 'neutral', 'match_type', 'source_competition']
const sourceHeaders = ['match_id', 'official_game_id', 'source_url', 'protocol_url', 'score_note']

function csvValue(value) {
  const text = String(value)
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function csv(headers, values) {
  return `\uFEFF${headers.join(',')}\r\n${values.map(row => row.map(csvValue).join(',')).join('\r\n')}\r\n`
}

await fs.writeFile(new URL('./azerbaijan-14-missing-90min.csv', import.meta.url), csv(projectHeaders, rows.map(row => row.slice(0, 10))), 'utf8')
await fs.writeFile(new URL('./azerbaijan-14-missing-sources.csv', import.meta.url), csv(sourceHeaders, rows.map(row => [row[0], ...row.slice(10)])), 'utf8')
