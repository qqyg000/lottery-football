# 队名联网核实与映射更新

核实日期：2026-09-28。本报告记录本次已落地的映射、去重和暂未合并项。

- 原候选 228 组，确认并处理 **119 组名称关系**，涉及 **112 个标准队名**；其中 **98 个中文**、14 个保留原文
- 主映射与测试映射各新增 **171 行**；普通新增使用 VERIFIED_ALIAS，奥地利的友谊赛专用别名使用 MANUAL，以覆盖全局国家队映射的较高优先级
- 映射后合并 **590 场比赛的 598 条重复来源记录**；其中 146 场涉及相邻日期，3 场存在已查官方结果的比分冲突
- 历史主表由 **239,359** 行变为 **238,761** 行；保留记录的日期、主客方向和原始字段均未改写
- 其余 **109 组**未建立合并映射，详见后表；这不代表它们均已联网判定为不同队

## 修改文件

- `src/main/resources/data/team_name_mappings.csv`：实际生产队名映射
- `src/test/resources/data/team_name_mappings.csv`：同步测试资源，避免测试与生产映射不一致
- `src/main/resources/data/historical_matches.csv`：仅删除本次新映射产生且通过规则核对的重复来源行
- `src/test/java/com/eason/worldcup/util/VerifiedClubAliasesTest.java`：隔离测试小型映射表，直接验证生产资源的中文优先、赛事限制和不同队伍边界

- `src/test/java/com/eason/worldcup/util/HistoricalMatchDataIntegrityTest.java`：按有证据的去重结果更新数量基线，新增官方比分及比亚韦单场唯一性检查

业务翻译方法仍为 `ClubTeamNameTranslator.translate`，无需修改 Java 业务逻辑；原生成脚本会保留 VERIFIED_ALIAS / MANUAL 来源。

## 依据与保留规则

确认双方身份后，仅合并同一赛事、同日或相邻日的不同来源记录；按球队视角核对比分，三个已查官方赛果的冲突组单独覆盖；每个相连组最多跨一天且每来源最多一条。普通同分组按 EXCEL > FOTMOB > FUTBOL24 > ESPN > FOOTMERCATO 的保留顺序，不改保留记录的原字段。相邻日期并不等于逐场重新核验过的开球日期

联网证据用于确认球队身份，三个冲突组另查官方比分；未逐场重查全部比赛开球时间；去重仅覆盖本次映射产生的同日或相邻日重复，保留其余不同比分、相隔 2—7 天和赛事分类不同的待核记录；没有足够中文依据的 14 个标准名保留已核实的原文名称；泛称按当前 CSV 中已经核实的俱乐部友谊赛语境限定，未来导入不同国家同名队仍应依据来源球队 ID 核查。

采用中文标准名时优先沿用仓库已有中文名称，新中文名称附中文参考资料；俱乐部身份主要依据官方俱乐部、联赛或足协资料，少量使用原数据源比赛页确认其具体含义。

## 已落实的全部映射

| 原候选 | 核实别名 | 归一名称 | 适用范围 | 身份证据 / 中文依据 |
| --- | --- | --- | --- | --- |
| G001 | Huddersfield / 哈德斯菲尔德 | 哈德斯菲尔德 | 全局 | [身份1](https://www.htafc.com/club/)、沿用现有中文 |
| G002 | Hertha Berlin / 柏林赫塔 | 柏林赫塔 | 全局 | [身份1](https://www.herthabsc.com/en/club/we-are-hertha)、沿用现有中文 |
| G003 | Montpellier HSC / 蒙彼利埃 | 蒙彼利埃 | 全局 | [身份1](https://mhscfoot.com/)、沿用现有中文 |
| G004 | Mainz / 美因茨 | 美因茨 | 全局 | [身份1](https://www.mainz05.de/)、沿用现有中文 |
| G005 | FC Cologne / 科隆 | 科隆 | 全局 | [身份1](https://fc.de/en/fanshop/fanwear)、沿用现有中文 |
| G006 | UD Las Palmas / 拉帕马斯 | 拉帕马斯 | 全局 | [身份1](https://www.udlaspalmas.es/datos-del-club)、沿用现有中文 |
| G007 | Gimnàstic Tarragona / Gimnàstic de Tarragona | 塔拉戈纳 | 全局 | [身份1](https://gimnasticdetarragona.cat/es/)、沿用现有中文 |
| G008 | CSKA 1948 / CSKA 1948 Sofia | 索菲亚1948 | 全局 | [身份1](https://cska1948.bg/en/about-the-club)、[中文](https://www.qiumiwu.com/team/suofeiya1948/game) |
| G009 | West Bromwich / 西布罗姆 | 西布罗姆 | 全局 | [身份1](https://www.wba.co.uk/club)、沿用现有中文 |
| G010 | Rotherham / Rotherham United | 罗瑟汉姆 | 全局 | [身份1](https://www.themillers.co.uk/centenary/town-county-united-the-story-of-how-the-millers-came-to-be)、沿用现有中文 |
| G011 | Austria Vienna / 维也纳 | 维也纳 | 全局 | [身份1](https://www.fk-austria.at/en)、沿用现有中文 |
| G012 | Cork / 科克城 | 科克城 | 全局 | [身份1](https://www.corkcityfc.ie/pages/club-directory)、沿用现有中文 |
| G013 | Forest Green / Forest Green Rovers | 绿色森林 | 全局 | [身份1](https://www.fgr.co.uk/)、沿用现有中文 |
| G014 | Lausanne Sports / Lausanne-Sport | 洛桑 | 全局 | [身份1](https://www.lausanne-sport.ch/)、沿用现有中文 |
| G015 | Al Ahly Cairo / 开罗国民 | 开罗国民 | 全局 | [身份1](https://www.alahlyegypt.com/en/football/first-team)、沿用现有中文 |
| G016 | NK Lokomotiva / 萨格勒布火车头 | 萨格勒布火车头 | 全局 | [身份1](https://nklokomotiva.hr/o-nama/)、沿用现有中文 |
| G017 | Perugia Calcio / 佩鲁贾 | 佩鲁贾 | 全局 | [身份1](https://acperugiacalcio.com/club/)、沿用现有中文 |
| G018 | Rot-Weiss Essen / 埃森 | 埃森 | 全局 | [身份1](https://www.rot-weiss-essen.de/)、沿用现有中文 |
| G019 | Shkendija / 斯肯迪亚 | 斯肯迪亚 | 全局 | [身份1](https://kfshkendija.com/)、沿用现有中文 |
| G020 | Colchester United / 科切斯特 | 科切斯特 | 全局 | [身份1](https://www.cu-fc.com/club/whos-who/)、沿用现有中文 |
| G021 | Hamburg SV / 汉堡 | 汉堡 | 全局 | [身份1](https://www.hsv.de/en)、沿用现有中文 |
| G022 | New England Revolution / New England Revs | 新英格兰革命 | 全局 | [身份1](https://www.revolutionsoccer.net/)、沿用现有中文 |
| G023 | Atlético Mineiro/MG / 米内罗竞技 | 米内罗竞技 | 全局 | [身份1](https://atletico.com.br/institucional/apresentacao/)、沿用现有中文 |
| G024 | Guadalajara / Guadalajara Chivas | 瓜达拉 | 仅 Guadalajara 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.chivasdecorazon.com.mx/es/club/historia)、沿用现有中文 |
| G025 | SC Paderborn 07 / 帕德博恩 | 帕德博恩 | 全局 | [身份1](https://www.scp07.de/)、沿用现有中文 |
| G026 | Debrecen / 德布勒森 | 德布勒森 | 全局 | [身份1](https://dvsc.hu/jegy-es-berlet/)、沿用现有中文 |
| G027 | Glentoran / 格伦托兰 | 格伦托兰 | 全局 | [身份1](https://www.glentoran.com/about/)、沿用现有中文 |
| G028 | SC Rheindorf Altach / 阿尔塔奇 | 阿尔塔奇 | 全局 | [身份1](https://www.scra.at/startseite)、沿用现有中文 |
| G029 | Sheffield Wed / 谢周三 | 谢周三 | 全局 | [身份1](https://www.swfc.co.uk/hillsborough/visiting-for-the-first-time)、沿用现有中文 |
| G030 | Shrewsbury / Shrewsbury Town | 什鲁斯 | 全局 | [身份1](https://www.shrewsburytown.com/)、沿用现有中文 |
| G031 | TSV 1860 / 慕1860 | 慕1860 | 全局 | [身份1](https://www.tsv1860.de/de?id=104)、沿用现有中文 |
| G032 | Öster / 厄斯特什 | 厄斯特什 | 全局 | [身份1](https://ostersif.se/om-osters-if)、沿用现有中文 |
| G033 | Caykur Rizespor / 里泽 | 里泽 | 全局 | [身份1](https://caykurrizespor.org.tr/haberler)、沿用现有中文 |
| G034 | Club Olimpia / Olimpia Asunción | 亚松森奥林匹亚 | 仅 Club Olimpia 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.clubolimpia.com.py/historia)、[中文](https://zh.wikipedia.org/wiki/%E5%A5%A5%E6%9E%97%E5%8C%B9%E4%BA%9A%E4%BF%B1%E4%B9%90%E9%83%A8) |
| G035 | Cúcuta / Cúcuta Deportivo | 库库塔体育 | 全局 | [身份1](https://dimayor.com.co/cucuta-deportivo-3/)、[中文](https://bkso.baidu.com/item/%E7%BA%A6%E7%BF%B0%C2%B7%E7%93%A6%E4%BC%A6%E8%A5%BF%E4%BA%9A/0?fromModule=lemma_inlink) |
| G036 | Estudiantes La Plata / 拉普大学 | 拉普大学 | 全局 | [身份1](https://estudiantesdelaplata.com/)、沿用现有中文 |
| G037 | FC Halifax Town / Halifax | 哈利法克 | 全局 | [身份1](https://fchalifaxtown.com/?lang=en)、沿用现有中文 |
| G038 | Győr / 杰尔 | 杰尔 | 全局 | [身份1](https://www.eto.hu/hu/contact)、沿用现有中文 |
| G039 | Hirnyk / Kryvbas | 克里夫巴斯 | 仅 Hirnyk 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.footmercato.net/club/fk-hirnyk-kryvyi-rih/tableau/)、[身份2](https://en.wikipedia.org/wiki/FC_Hirnyk_Kryvyi_Rih)、[中文](https://zh.wikipedia.org/wiki/%E5%85%8B%E9%87%8C%E7%BB%B4%E9%87%8C%E8%B5%AB%E5%85%8B%E9%87%8C%E5%A4%AB%E5%B7%B4%E6%96%AF%E8%B6%B3%E7%90%83%E4%BF%B1%E4%B9%90%E9%83%A8_%282020%E5%B9%B4%29) |
| G040 | IK Oddevold / Oddevold | 奥迪沃特 | 全局 | [身份1](https://oddevold.se/kontakt/)、[中文](https://www.sofascore.com/zh/football/team/ik-oddevold/1860) |
| G041 | Lausanne-Sport / 洛桑 | 洛桑 | 全局 | [身份1](https://www.lausanne-sport.ch/)、沿用现有中文 |
| G042 | Mansfield / Mansfield Town | 曼斯菲尔德 | 全局 | [身份1](https://www.mansfieldtown.net/news/)、沿用现有中文 |
| G043 | Mezokövesdi SE / 梅索科菲德 | 梅索科菲德 | 全局 | [身份1](https://mezokovesdzsory.hu/kapcsolat/)、沿用现有中文 |
| G044 | Red Star Belgrade / 贝红星 | 贝红星 | 全局 | [身份1](https://www.crvenazvezdafk.com/en)、沿用现有中文 |
| G045 | SC Verl 1924 / Verl | SC维尔 | 全局 | [身份1](https://www.sportclub-verl.de/File/SC-Verl-Satzung_28.11.2021-final.pdf)、[中文](https://team.7m.com.cn/4661/index.shtml) |
| G046 | SV Darmstadt 98 / 达姆施塔特 | 达姆施塔特 | 全局 | [身份1](https://shop.sv98.de/)、沿用现有中文 |
| G047 | Salt Lake / 皇家盐湖城 | 皇家盐湖城 | 全局 | [身份1](https://www.rsl.com/schedule/)、沿用现有中文 |
| G048 | SpVgg Greuther Fürth / 菲尔特 | 菲尔特 | 全局 | [身份1](https://www.sgf1903.de/)、沿用现有中文 |
| G049 | St. Patrick's Athletic / 圣帕特里 | 圣帕特里 | 全局 | [身份1](https://www.stpatsfc.com/news.php?id=9584)、沿用现有中文 |
| G050 | Paksi SE / 保克什 | 保克什 | 全局 | [身份1](https://paksifc.hu/page.php?id=3)、沿用现有中文 |
| G051 | Radomiak / 拉多米亚克 | 拉多米亚克 | 全局 | [身份1](https://www.radom.pl/dla-mieszkancow/sport/wizytowki-sportowe-radomia/radomiak-s-a/)、沿用现有中文 |
| G052 | ADV Montecatini / Montecatini | Montecatini | 全局 | [身份1](https://www.asroma.com/en/news/61623/report-productive-run-out-against-montecatini)、[身份2](https://www.futbol24.com/nl/team/Italy/ADV-Montecatini) |
| G053 | Akhisar Belediyespor / 阿卡希萨尔 | 阿卡希萨尔 | 全局 | [身份1](https://www.uefa.com/nationalassociations/teams/2604479--akhisar/)、沿用现有中文 |
| G054 | Al Hilal Riyadh / 利雅新月 | 利雅新月 | 全局 | [身份1](https://alhilal.com/en)、沿用现有中文 |
| G055 | Al Wakra / Wakrah | 威柯拉 | 全局 | [身份1](https://qsl.qa/index.php/en/al-wakrah)、[中文](https://team.7m.com.cn/3951/index_gb.shtml) |
| G056 | Al-Wakrah / Wakrah | 威柯拉 | 全局 | [身份1](https://qsl.qa/index.php/en/al-wakrah)、[中文](https://team.7m.com.cn/3951/index_gb.shtml) |
| G057 | Antwerp / 安特卫普 | 安特卫普 | 全局 | [身份1](https://www.royalantwerpfc.be/)、沿用现有中文 |
| G058 | Apollon / 利阿波罗 | 利阿波罗 | 仅 Apollon 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.apollon.com.cy/en/)、[身份2](https://www.footmercato.net/live/4367879561717798419-apollon-limassol-vs-aek-larnaca)、沿用现有中文 |
| G059 | Apollon Larisa / Apollon Larissa | Apollon Larissa | 全局 | [身份1](https://www.aa.com.tr/tr/futbol/besiktas-berabere-kaldi/1544510) |
| G060 | Atlético San Luis / Atlético de San Luis | 圣路易斯 | 全局 | [身份1](https://www.atleticodesanluis.mx/)、沿用现有中文 |
| G061 | Bilbao / 毕尔巴鄂 | 毕尔巴鄂 | 全局 | [身份1](https://www.athletic-club.eus/)、沿用现有中文 |
| G062 | Bray / Bray Wanderers | 布雷 | 全局 | [身份1](https://www.braywanderersfc.ie/)、[中文](https://team.7m.com.cn/1560/index.shtml) |
| G063 | Brinje / Brinje Grosuplje | Brinje Grosuplje | 全局 | [身份1](https://nogometniklub-brinje.si/) |
| G064 | Brunswick / Eintr. Braunschweig | 不伦瑞克 | 全局 | [身份1](https://www.eintracht.com/eintracht/gmbh-co-kg/informationen)、沿用现有中文 |
| G065 | Brunswick / 不伦瑞克 | 不伦瑞克 | 全局 | [身份1](https://www.eintracht.com/eintracht/gmbh-co-kg/informationen)、沿用现有中文 |
| G066 | Cadix / 加的斯 | 加的斯 | 全局 | [身份1](https://www.cadizcf.com/datos-generales)、沿用现有中文 |
| G068 | Charlton / 查尔顿 | 查尔顿 | 全局 | [身份1](https://cdn.charltonafc.com/node)、沿用现有中文 |
| G069 | Cibalia / 希巴利亚 | 希巴利亚 | 全局 | [身份1](https://hnk-cibalia.hr/organizacija/)、沿用现有中文 |
| G070 | Club Africain / Club Africain Tunis | 非洲人 | 全局 | [身份1](https://clubafricain.com/en/)、[中文](https://zh.wikipedia.org/wiki/%E9%9D%9E%E6%B4%B2%E4%BA%BA%E7%90%83%E6%9C%83) |
| G071 | Columbus / Columbus Crew | 哥伦布机员 | 全局 | [身份1](https://www.columbuscrew.com/)、沿用现有中文 |
| G072 | Dijon FCO / 第戎 | 第戎 | 全局 | [身份1](https://www.dfco.fr/)、沿用现有中文 |
| G073 | Egnatia / Egnatia Rrogozhinë | 埃格纳蒂亚 | 全局 | [身份1](https://www.uefa.com/uefaeuropaleague/match/2049134--egnatia-vs-shamrock-rovers/matchinfo/)、[中文](https://www.vietnam.vn/zh-cn/egnatia-rrogozhine-va-lillestrom-bat-phan-thang-bai-o-play-offs) |
| G074 | Erminio / Giana Erminio | 吉安纳 | 全局 | [身份1](https://www.asgiana.com/societa/storia/)、[中文](https://zh.100ke.info/wiki/%E6%84%8F%E4%B8%99) |
| G075 | FC Fleury 91 / Fleury | FC Fleury 91 | 全局 | [身份1](https://www.fcfleury91.fr/) |
| G078 | Feronikeli / KF Feronikeli | 费罗尼克利 | 全局 | [身份1](https://www.uefa.com/uefachampionsleague/history/clubs/2608281--feronikeli/)、[中文](https://zh.wikipedia.org/wiki/%E8%B2%BB%E7%BE%85%E5%B0%BC%E5%85%8B%E5%88%A9%E8%B6%B3%E7%90%83%E6%9C%83) |
| G079 | Forfar / Forfar Athletic | 福弗尔竞技 | 全局 | [身份1](https://forfarathletic.co.uk/club-info/history/)、[中文](https://zh.wikipedia.org/wiki/%E7%A6%8F%E5%BC%97%E7%88%BE%E7%AB%B6%E6%8A%80%E8%B6%B3%E7%90%83%E4%BF%B1%E6%A8%82%E9%83%A8) |
| G080 | GC Zurich / Grasshopper | 草蜢 | 全局 | [身份1](https://www.gcz.ch/club/gc-zuerich/)、沿用现有中文 |
| G081 | GC Zurich / 草蜢 | 草蜢 | 全局 | [身份1](https://www.gcz.ch/club/gc-zuerich/)、沿用现有中文 |
| G082 | GrIFK / Grankulla IFK | Grankulla IFK | 全局 | [身份1](https://grifkfotboll.fi/) |
| G083 | H Ramat Gan / Hapoel Ramat Gan | 拉马甘夏普尔 | 全局 | [身份1](https://www.football.org.il/en/team-details/?season_id=28&team_id=2175)、[中文](https://team.7m.com.cn/1884/index_gb.shtml) |
| G084 | H&W Welders / Welders | H&W Welders | 全局 | [身份1](https://www.weldersfc.com/club_profile.html) |
| G085 | HUI / Hørsholm Usserød IK | Hørsholm-Usserød | 全局 | [身份1](https://hui-fodbold.dk/om-hui-1/) |
| G086 | HUI / Hørsholm-Usserød | Hørsholm-Usserød | 全局 | [身份1](https://hui-fodbold.dk/om-hui-1/) |
| G087 | Hajduk / 斯海杜克 | 斯海杜克 | 全局 | [身份1](https://hajduk.hr/eng/)、沿用现有中文 |
| G088 | Honvéd / 布达佩斯捍卫者 | 布达佩斯捍卫者 | 全局 | [身份1](https://www.honvedfc.hu/tartalom/klub-informaciok)、沿用现有中文 |
| G089 | Ind. Santa Fe / 圣菲独立 | 圣菲独立 | 全局 | [身份1](https://independientesantafe.com/noticias-masculino/)、沿用现有中文 |
| G091 | KVC Westerlo / 韦斯特洛 | 韦斯特洛 | 全局 | [身份1](https://kvcwesterlo.be/en/)、沿用现有中文 |
| G092 | Kaiserslautern / 凯泽 | 凯泽 | 全局 | [身份1](https://fck.de/fck/mitgliedschaft/mitglied-werden/)、沿用现有中文 |
| G094 | Latina / Latina Calcio | 拉蒂纳 | 全局 | [身份1](https://www.latinacalcio1932.com/)、[中文](https://zh.wikipedia.org/wiki/%E5%8B%92%E5%A4%A9%E6%8B%BF%E9%AB%94%E8%82%B2%E6%9C%83) |
| G095 | Limavady United / Limavady Utd | Limavady United | 全局 | [身份1](https://limavadyunitedfc.com/about/) |
| G096 | Mondorf / US Mondorf les Bains | US Mondorf les Bains | 全局 | [身份1](https://www.usmondorf.lu/valeurs-268221v4) |
| G097 | Nassr/Hilal All-Stars / Riyadh All-Stars XI | 利雅得全明星 | 全局 | [身份1](https://en.psg.fr/teams/first-team/content/the-gala-match-against-riyadh-season-live-on-thursday-1901-at-6pm-qatar-tour-2023)、[中文](https://m.thepaper.cn/newsDetail_forward_21641833) |
| G098 | Notts / 诺茨郡 | 诺茨郡 | 全局 | [身份1](https://www.nottscountyfc.co.uk/club/)、沿用现有中文 |
| G099 | Nördlingen / TSV 1861 Nördlingen | TSV 1861 Nördlingen | 全局 | [身份1](https://www.bfv.de/vereine/00ES8GNJHS00000FVV0AG08LVUPGND5I) |
| G100 | Orlando Pirates / Pirates | 奥兰多海盗 | 仅 Pirates 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.orlandopiratesfc.com/club/)、[身份2](https://www.cordobacf.com/en/news/cordoba-cf-will-play-their-first-preseason-friendly-against-orlando-pirates)、[中文](https://team.7m.com.cn/3989/index_gb.shtml) |
| G101 | Peñarol / 佩纳罗尔 | 佩纳罗尔 | 全局 | [身份1](https://sudamericanos20.auf.org.uy/club-atletico-penarol/)、沿用现有中文 |
| G102 | Puskas FC Academy / Puskás | 普斯卡什学院 | 全局 | [身份1](https://pfla.hu/downloads/pafc_honved_musorfuzet.pdf)、沿用现有中文 |
| G103 | Puskas FC Academy / 普斯卡什学院 | 普斯卡什学院 | 全局 | [身份1](https://pfla.hu/downloads/pafc_honved_musorfuzet.pdf)、沿用现有中文 |
| G104 | RS Waasland-Beveren / Waasland-Beveren | 贝弗伦 | 全局 | [身份1](https://www.skbeveren.be/club-2/)、沿用现有中文 |
| G105 | SD Ponferradina / 蓬费拉迪 | 蓬费拉迪 | 全局 | [身份1](https://sdponferradina.com/)、沿用现有中文 |
| G106 | SG Sonnenhof Großaspach / Sonnenhof Großaspach | SG Sonnenhof Großaspach | 全局 | [身份1](https://www.sg94.de/impressum/) |
| G107 | SK Traeff / Træff | SK Træff | 全局 | [身份1](https://traeff.no/) |
| G108 | Sainte Genevieve des Bois / Sainte-Geneviève | Sainte-Geneviève | 全局 | [身份1](https://www.sgdb91.com/annuaires_asso/sgs-football/) |
| G109 | Sandnes / Sandnes Ulf | 桑内斯 | 全局 | [身份1](https://www.sandnesulf.no/om-klubben/nokkelinfo-og-fakta)、沿用现有中文 |
| G110 | Shabab Riyadh / 利沙巴布 | 利沙巴布 | 全局 | [身份1](https://www.spl.com.sa/en/teams/al-shabab)、沿用现有中文 |
| G111 | Shamrock / 沙姆洛克 | 沙姆洛克 | 全局 | [身份1](https://www.shamrockrovers.ie/teams/first-team/)、沿用现有中文 |
| G112 | Stade Nyonnais / 尼永 | 尼永 | 全局 | [身份1](https://stadenyonnais.ch/le_club/histoire)、沿用现有中文 |
| G114 | Szegad-Csanád / Szeged-Csanád GA | Szeged-Csanád GA | 全局 | [身份1](https://www.szeged-grosicsakademia.hu/hir/2758/tatabanyan-rajtolunk-vasarnap)、[身份2](https://www.espn.com.co/futbol/resultados/_/fecha/20240710) |
| G115 | Séville / 塞维利亚 | 塞维利亚 | 全局 | [身份1](https://ticketstour.sevillafc.es/en/aviso_legal)、沿用现有中文 |
| G116 | Ternana / Ternana Calcio | 特尔纳纳 | 全局 | [身份1](https://ternanacalcio.com/societa/)、[中文](https://zh.wikipedia.org/wiki/%E7%89%B9%E5%B0%94%E7%BA%B3%E7%BA%B3%E8%B6%B3%E7%90%83%E4%BF%B1%E4%B9%90%E9%83%A8) |
| G117 | UCD / 都柏林 | 都柏林 | 全局 | [身份1](https://www.uefa.com/newsfiles/332930.pdf)、沿用现有中文 |
| G118 | US Avellino / 阿韦利诺 | 阿韦利诺 | 全局 | [身份1](https://www.usavellino1912.com/)、沿用现有中文 |
| G119 | US Lecce / 莱切 | 莱切 | 全局 | [身份1](https://oo.uslecce.it/contatti)、沿用现有中文 |
| G120 | Utsikten / Utsiktens BK | 乌锡坦斯 | 全局 | [身份1](https://ettanfotboll.se/klubb/utsikten/)、[中文](https://team.7m.com.cn/5782/index.shtml) |
| G121 | Varberg / 瓦尔贝里 | 瓦尔贝里 | 全局 | [身份1](https://boisfc.nu/)、沿用现有中文 |
| G122 | Zénith / 泽尼特 | 泽尼特 | 全局 | [身份1](https://app.fc-zenit.ru/en)、沿用现有中文 |
| G184 | Aris / 阿里斯 | 阿里斯 | 仅 Aris 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.psv.nl/en/media/artikel/alles-wat-je-moet-weten-over-psv-aris)、[身份2](https://schalke04.de/aktuelles/matchcenter/2020-2021-testspiel-4-fc-schalke-04-aris-saloniki/)、沿用现有中文 |
| G185 | CSKA / 莫陆军 | 莫陆军 | 仅 CSKA 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.footmercato.net/live/4900069620584456365-cska-moscou-vs-krasnodar)、沿用现有中文 |
| G220 | 奥地利 / 维也纳 | 维也纳 | 仅 奥地利 限 CLUB_FRIENDLY；其余全局 | [身份1](https://www.sofascore.com/football/match/paksi-fc-fk-austria-wien/dUsjwc)、沿用现有中文 |

## 限定范围与身份边界

- G008：官方区分 CSKA 1948 与 CSKA-Sofia，1948 一队、二队及索菲亚中央陆军保持独立
- G024：Guadalajara 在库内还指西班牙国王杯球队，仅在已核实的俱乐部友谊赛中指向墨西哥瓜达拉；Guadalajara Chivas 可全局归一
- G034：巴拉圭亚松森俱乐部；Club Olimpia 限友谊赛，避免与洪都拉斯 Olimpia 混用；不映射到已有奥林匹亚
- G039：Foot Mercato 的 2026 年 Hirnyk 记录沿用旧名称，指向现 Kryvbas；Hirnyk 仅限友谊赛，不扩展至 Hirnyk-Sport 或二队
- G058：源比赛页明确写 Apollon Limassol，Apollon 泛称仅限本次核实的俱乐部友谊赛范围
- G061：Bilbao 指 Athletic Club，一队与 Bilbao Athletic 保持独立
- G097：利雅得胜利与利雅得新月组成的临时全明星联队，保持独立身份
- G100：2026 年西班牙热身赛中的 Pirates 为南非 Orlando Pirates；Pirates 泛称仅限友谊赛
- G104：官方沿革确认 Waasland-Beveren 与现 SK Beveren 的联系，不扩展到其他 KSK Beveren 名称
- G184：PSV 2019 年和沙尔克 2020 年官方比赛资料均指 Aris Saloniki；Aris 仅限友谊赛，不合并塞浦路斯 Aris Limassol
- G185：Foot Mercato 对应源比赛 ID 的完整球队为 CSKA Moscou，CSKA 仅限友谊赛，不合并索菲亚球队
- G220：已核实 2026-07-17/18 维也纳对保克什 4:1；仅 CLUB_FRIENDLY 将奥地利作为维也纳别称，国家队保留奥地利

## 已纠正的比分冲突

| 日期（保留记录） | 官方比分 | 错误来源记录 | 保留 ID | 证据 |
| --- | --- | --- | --- | --- |
| 2017-01-18 | 帕德博恩 1:6 多特蒙德 | EXCEL-24431 帕德博恩 3:4 多特蒙德 | ESPN-490276 | [官方](https://www.bvb.de/de/de/aktuelles/news/news.html/News/Uebersicht/6-1-in-Paderborn-BVB-feiert-gelungene-Generalprobe.html) |
| 2019-01-06 | 曼城 7:0 罗瑟汉姆 | EXCEL-45959 曼城 4:3 罗瑟汉姆 | FOTMOB-2942390 | [官方](https://live.mancity.com/news/mens/man-city-domestic-cup-run-stats-and-highlights-6-march-63719097) |
| 2019-03-30 | 德比郡 6:1 罗瑟汉姆 | EXCEL-47540 德比郡 4:3 罗瑟汉姆 | FOTMOB-2791782 | [官方](https://www.dcfc.co.uk/results?139team=1&season=16) |

## 未合并的全部候选

| 原候选 | 名称 | 本次处理理由 |
| --- | --- | --- |
| G067 | Canelas 2010 / Puskas FC Academy | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G076 | FC Kosice / MFK Kosice | 历史俱乐部身份不同；2024 年对罗马比赛官方称 FC Košice，不能把所有 MFK Košice 全局并入；[依据](https://www.fckosice.sk/fc-kosice-v-generalke-remizoval-doma-s-as-rim-11-a19-2079) |
| G077 | FK AS Pardubice / Pardubice | FK AS Pardubice 与现 FK Pardubice 不是可直接全局合并的同一历史俱乐部，需按源球队 ID 修正具体比赛；[依据](https://www.fkpardubice.cz/Strucna-historie-klubu),[依据](https://de.wikipedia.org/wiki/FK_Pardubice) |
| G090 | KFC Uerdingen 05 / Verl | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G093 | Krefelder FC Uerdingen / SC Verl 1924 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G113 | Stade-Lausanne / 索肖 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G123 | 亚布洛 / 泽尼特 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G124 | 比勒费 / 萨尔茨堡 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G125 | 法伦斯 / 费内巴切 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G126 | Aarhus Fremad / 杜保尔 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G127 | Abdysh-Ata Kant / 阿斯塔纳 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G128 | Akhmat Groznyi / 阿劳 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G129 | Al Ula / 布尔萨体育 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G130 | AlbinoLeffe / Alcione | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G131 | Altona 93 / 基尔 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G132 | Anadia FC / CD Trofense | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G133 | Arsenal Tula / Spittal/Drau | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G134 | CS Mioveni / 贝游击 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G135 | Carrarese / 卡尔皮 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G136 | Chamois Niortais / SM Caen | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G137 | Deinze / KSV Oudenaarde | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G138 | Dinamo Batumi / FC Rustavi | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G139 | Dinamo Batumi / Valmiera FC | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G140 | Dinamo Tbilisi / 鲁达普列 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G141 | Excelsior Virton / 勒芬 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G142 | FC Rapperswil-Jona / 乌法 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G143 | FC Stade Lausanne-Ouchy / 索肖 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G144 | FK Liepaja / Sileks Kratovo | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G145 | FK Rad / OFK Bačka | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G146 | First Vienna FC / 德布勒森 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G147 | Floridsdorfer AC / 维尔茨堡 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G148 | GKS Tychy / 拉多米亚克 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G149 | GS Arconatese / 里昂 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G150 | KFC Uerdingen 05 / SC Verl 1924 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G151 | Krefelder FC Uerdingen / Verl | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G152 | Kvik Halden FK / 埃斯比约 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G153 | Lions Gibraltar / 赫根 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G154 | MFK Karviná / 特马利卡 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G155 | Maccabi Netanya / Stal Stalowa Wola | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G156 | Miami FC / Sarasota Paradise | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G157 | Mladost Lucani / ND Gorica | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G158 | Navbahor / 捷特苏 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G159 | Politehnica Iasi* / 弗罗茨瓦夫 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G160 | Sogndal / 奥德 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G161 | Union Fürstenwalde / 开姆尼茨 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G162 | Xamax / 埃弗顿 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G163 | ŁKS Łomża / 科罗纳 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G164 | 伯恩利 / 里斯本 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G165 | 克拉斯诺 / 采列 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G166 | 加拉塔萨 / 特斯巴达 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G167 | 南锡 / 斯托克城 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G168 | 卢宾扎格勒比 / 特普利斯 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G169 | 哈尔科夫 / 索列夫 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G170 | 圣吉联合 / 比利亚雷 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G171 | 埃库莱斯 / 阿梅里亚 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G172 | 奥斯坦德 / 瓦朗谢纳 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G173 | 富勒姆 / 格罗迪SV | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G174 | 巴列卡诺 / 拉齐奥 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G175 | 巴特 / 贝乌哈图夫 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G176 | 拉纳卡 / 特马利卡 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G177 | 斯拉维亚 / 莫陆军 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G178 | 标准列日 / 罗达JC | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G179 | 沙勒罗瓦 / 瓦尔韦克 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G180 | 波尔图 / 西布罗姆 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G181 | 瓦路尔 / 萨普斯堡 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G182 | 瓦雷赫姆 / 福伦丹 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G183 | 维迪奥顿 / 采列 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G186 | 布斯巴达 / 鹿斯巴达 | 布拉格斯巴达与鹿特丹斯巴达不同，疑似来源泛称或误译，不能建立队名合并映射 |
| G187 | BW林茨 / 里德 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G188 | Burton Albion / Rotherham United | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G189 | CA Osasuna B / 莱万特 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G190 | GD Gafanha / 法马利康 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G191 | Guayaquil City / 西班牙人 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G192 | Guayaquil City / 阿瓦塞特 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G193 | Hannover II / 达姆施塔特 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G194 | Inter Bratislava / 国际米兰 | Inter Bratislava 与国际米兰不同，疑似来源误译，不能建立队名合并映射 |
| G195 | ML Vitebsk / Neman Grodno | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G196 | Mushuc Runa / 赫罗纳 | 没有建立同一球队的身份依据，保留独立名称；相邻日期和相同比分不足以合并 |
| G197 | Racing Ferrol / 奥维耶多 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G198 | SC Braga B / 吉维森特 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G199 | SC Braga B / 雷克斯欧 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G200 | US Quevilly Rouen / 勒阿弗尔 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G201 | 亚拉腊 / 泽尼特 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G202 | 佐加顿斯 / 哈马比 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G203 | 克拉斯诺 / 沃尔夫斯 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G204 | 克拉科维亚 / 卢宾扎格勒比 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G205 | 克里斯蒂 / 桑纳菲 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G206 | 利物浦 / 曼联 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G207 | 博德闪耀 / 马韦利亚 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G208 | 博莱 / 布斯巴达 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G209 | 博阿维斯 / 费雷拉 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G210 | 卡斯鲁厄 / 布鲁日 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G211 | 吉马良斯 / 维多利亚 | 维多利亚已用于巴西球队，另有葡萄牙友谊赛同名记录；赛事范围也不足以安全区分，需源球队 ID 或单场修正；[依据](https://www.abola.pt/noticias/vitoria-de-guimaraes-anuncia-um-reforco-e-seis-amigaveis-2026071017130182615) |
| G212 | 圣克拉拉 / 葡国民 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G213 | 圣克拉拉 / 费雷拉 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G214 | 埃弗顿 / 西布罗姆 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G215 | 埃斯比约 / 锡尔克堡 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G216 | 埃森 / 科隆 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G217 | 埃瓦尔 / 奥维耶多 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G218 | 埃门 / 奥斯纳 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G219 | 基尔 / 腓特烈 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G221 | 奥斯纳 / 波鸿 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G222 | 女王巡游 / 西汉姆联 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G223 | 巴利亚多 / 贝西克塔斯 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G224 | 比萨 / 萨索洛 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G225 | 沃夫斯堡 / 首尔FC | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G226 | 法伦斯 / 里斯本 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G227 | 法马利康 / 费伦斯 | 库内存在双方直接交锋，不依据邻日同比分合并 |
| G228 | 特温特 / 阿尔克马 | 库内存在双方直接交锋，不依据邻日同比分合并 |

## 完整去重清单

下面每行是一场保留的比赛。JSON 附件含每条删除记录的全部原字段、修改前行号、原始 CSV 行、保留行及文件 SHA-256，可逐条复核或恢复。旧审计报告中的行号指修改前的 239,359 行快照，不可直接定位修改后的 CSV。

[完整机器可读证据与原始行](D:/WorkJava/lottery-football/reports/verified-team-name-mappings-2026-09-28.json)

| 编号 | 保留日期 | 归一后比赛 | 保留 ID | 删除 ID（原日期） | 原因 |
| --- | --- | --- | --- | --- | --- |
| 1 | 2014-10-22 | 罗瑟汉姆 3:3 富勒姆 | EXCEL-18 | FOTMOB-1724511 (2014-10-22) | 同日同比分 |
| 2 | 2014-10-25 | 布赖顿 1:1 罗瑟汉姆 | EXCEL-143 | FOTMOB-1724521 (2014-10-25) | 同日同比分 |
| 3 | 2014-10-25 | 斯文登 2:2 科切斯特 | EXCEL-160 | FOTMOB-1725094 (2014-10-25) | 同日同比分 |
| 4 | 2014-10-29 | 什鲁斯 1:2 切尔西 | EXCEL-307 | FOTMOB-1837276 (2014-10-29) | 同日同比分 |
| 5 | 2014-11-01 | 罗瑟汉姆 0:3 米堡 | EXCEL-418 | FOTMOB-1724537 (2014-11-01) | 同日同比分 |
| 6 | 2014-11-05 | 雷丁 3:0 罗瑟汉姆 | EXCEL-560 | FOTMOB-1724550 (2014-11-05) | 同日同比分 |
| 7 | 2014-11-08 | 谢周三 0:0 罗瑟汉姆 | EXCEL-657 | FOTMOB-1724565 (2014-11-08) | 同日同比分 |
| 8 | 2014-11-22 | 罗瑟汉姆 0:1 伯明翰 | EXCEL-992 | FOTMOB-1724574 (2014-11-22) | 同日同比分 |
| 9 | 2014-11-22 | 科切斯特 0:1 考文垂 | EXCEL-999 | FOTMOB-1725120 (2014-11-22) | 同日同比分 |
| 10 | 2014-11-29 | 米尔顿 6:0 科切斯特 | EXCEL-1241 | FOTMOB-1725137 (2014-11-29) | 同日同比分 |
| 11 | 2014-12-06 | 加的夫城 0:0 罗瑟汉姆 | EXCEL-1431 | FOTMOB-1724590 (2014-12-06) | 同日同比分 |
| 12 | 2014-12-06 | 普雷斯顿 1:0 什鲁斯 | EXCEL-1445 | FOTMOB-1863353 (2014-12-06) | 同日同比分 |
| 13 | 2014-12-13 | 罗瑟汉姆 0:0 诺丁汉 | EXCEL-1656 | FOTMOB-1724606 (2014-12-13) | 同日同比分 |
| 14 | 2014-12-26 | 罗瑟汉姆 2:2 哈德斯菲尔德 | EXCEL-1951 | FOTMOB-1724630 (2014-12-26) | 同日同比分 |
| 15 | 2015-01-03 | 加的夫城 3:1 科切斯特 | EXCEL-2038 | FOTMOB-1873311 (2015-01-03) | 同日同比分 |
| 16 | 2015-01-03 | 罗瑟汉姆 1:5 伯恩茅斯 | EXCEL-2057 | FOTMOB-1873297 (2015-01-03) | 同日同比分 |
| 17 | 2015-01-10 | 布伦特 1:0 罗瑟汉姆 | EXCEL-2170 | FOTMOB-1724657 (2015-01-10) | 同日同比分 |
| 18 | 2015-01-17 | 罗瑟汉姆 0:2 伯恩茅斯 | EXCEL-2309 | FOTMOB-1724666 (2015-01-17) | 同日同比分 |
| 19 | 2015-01-28 | 罗瑟汉姆 4:2 博尔顿 | EXCEL-2576 | FOTMOB-1724675 (2015-01-28) | 同日同比分 |
| 20 | 2015-01-31 | 查尔顿 1:1 罗瑟汉姆 | EXCEL-2646 | FOTMOB-1724689 (2015-01-31) | 同日同比分 |
| 21 | 2015-02-07 | 罗瑟汉姆 2:0 伊普斯 | EXCEL-2847 | FOTMOB-1724708 (2015-02-07) | 同日同比分 |
| 22 | 2015-02-11 | 布莱克本 2:1 罗瑟汉姆 | EXCEL-2965 | FOTMOB-1724714 (2015-02-11) | 同日同比分 |
| 23 | 2015-02-11 | 谢菲联 4:1 科切斯特 | EXCEL-2981 | FOTMOB-1725271 (2015-02-11) | 同日同比分 |
| 24 | 2015-02-18 | 罗瑟汉姆 3:3 德比郡 | EXCEL-3182 | FOTMOB-1724729 (2015-02-18) | 同日同比分 |
| 25 | 2015-02-18 | 科切斯特 0:1 米尔顿 | EXCEL-3183 | FOTMOB-1725200 (2015-02-18) | 同日同比分 |
| 26 | 2015-02-28 | 罗瑟汉姆 2:1 米尔沃尔 | EXCEL-3301 | FOTMOB-1724761 (2015-02-28) | 同日同比分 |
| 27 | 2015-03-04 | 罗瑟汉姆 1:3 加的夫城 | EXCEL-3430 | FOTMOB-1724781 (2015-03-04) | 同日同比分 |
| 28 | 2015-03-04 | 科切斯特 0:1 诺茨郡 | EXCEL-3434 | FOTMOB-1725320 (2015-03-04) | 同日同比分 |
| 29 | 2015-03-07 | 哈德斯菲尔德 0:2 罗瑟汉姆 | EXCEL-3543 | FOTMOB-1724789 (2015-03-07) | 同日同比分 |
| 30 | 2015-03-19 | 诺丁汉 2:0 罗瑟汉姆 | EXCEL-3969 | FOTMOB-1724817 (2015-03-19) | 同日同比分 |
| 31 | 2015-03-21 | 罗瑟汉姆 2:3 谢周三 | EXCEL-4063 | FOTMOB-1724824 (2015-03-21) | 同日同比分 |
| 32 | 2015-04-03 | 伯明翰 2:1 罗瑟汉姆 | EXCEL-4343 | FOTMOB-1724834 (2015-04-03) | 同日同比分 |
| 33 | 2015-04-06 | 罗瑟汉姆 1:0 布赖顿 | EXCEL-4556 | FOTMOB-1724852 (2015-04-06) | 同日同比分 |
| 34 | 2015-04-11 | 米堡 2:0 罗瑟汉姆 | EXCEL-4744 | FOTMOB-1724862 (2015-04-11) | 同日同比分 |
| 35 | 2015-04-11 | 考文垂 1:0 科切斯特 | EXCEL-4750 | FOTMOB-1725418 (2015-04-11) | 同日同比分 |
| 36 | 2015-04-16 | 富勒姆 1:1 罗瑟汉姆 | EXCEL-4932 | FOTMOB-1724876 (2015-04-16) | 同日同比分 |
| 37 | 2015-04-25 | 罗瑟汉姆 1:1 诺维奇 | EXCEL-5286 | FOTMOB-1724892 (2015-04-25) | 同日同比分 |
| 38 | 2015-04-29 | 罗瑟汉姆 2:1 雷丁 | EXCEL-5428 | FOTMOB-1724880 (2015-04-29) | 同日同比分 |
| 39 | 2015-04-29 | 科切斯特 1:1 斯文登 | EXCEL-5429 | FOTMOB-1725378 (2015-04-29) | 同日同比分 |
| 40 | 2015-05-02 | 利兹联 0:0 罗瑟汉姆 | EXCEL-5539 | FOTMOB-1724908 (2015-05-02) | 同日同比分 |
| 41 | 2015-05-03 | 科切斯特 1:0 普雷斯顿 | EXCEL-5618 | FOTMOB-1725464 (2015-05-03) | 同日同比分 |
| 42 | 2015-07-15 | 绿色森林 1:1 加的夫城 | FUTBOL24-1298F27F32A9D2FF | ESPN-430317 (2015-07-16) | 相邻日同比分 |
| 43 | 2015-07-18 | 什鲁斯 2:2 加的夫城 | FUTBOL24-B24C0ABC428B97CC | ESPN-430336 (2015-07-18) | 同日同比分 |
| 44 | 2015-07-19 | 帕德博恩 0:2 沃特福德 | FUTBOL24-CA559C1300CF2CFD | ESPN-430351 (2015-07-19) | 同日同比分 |
| 45 | 2015-07-25 | 曼斯菲尔德 1:1 莱切斯特 | FUTBOL24-082F1878D9C35935 | ESPN-430400 (2015-07-25) | 同日同比分 |
| 46 | 2015-07-29 | 罗瑟汉姆 1:2 莱切斯特 | FUTBOL24-ACB86F10FD85BD85 | ESPN-430565 (2015-07-30) | 相邻日同比分 |
| 47 | 2015-08-02 | 佩纳罗尔 1:3 马拉加 | EXCEL-7540 | FUTBOL24-50522DD341166F23 (2015-08-01) | 相邻日同比分 |
| 48 | 2015-08-08 | 罗瑟汉姆 1:4 米尔顿 | EXCEL-7721 | FOTMOB-1987052 (2015-08-08) | 同日同比分 |
| 49 | 2015-08-08 | 什鲁斯 1:2 米尔沃尔 | EXCEL-7732 | FOTMOB-1987097 (2015-08-08) | 同日同比分 |
| 50 | 2015-08-12 | 布莱克本 1:2 什鲁斯 | EXCEL-7834 | FOTMOB-1986325 (2015-08-12) | 同日同比分 |
| 51 | 2015-08-12 | 科切斯特 0:0 雷丁 | EXCEL-7841 | FOTMOB-1986351 (2015-08-12) | 同日同比分 |
| 52 | 2015-08-15 | 诺丁汉 2:1 罗瑟汉姆 | EXCEL-8000 | FOTMOB-1987062 (2015-08-15) | 同日同比分 |
| 53 | 2015-08-19 | 罗瑟汉姆 0:0 普雷斯顿 | EXCEL-8144 | FOTMOB-1987659 (2015-08-19) | 同日同比分 |
| 54 | 2015-08-22 | 女王巡游 4:2 罗瑟汉姆 | EXCEL-8285 | FOTMOB-1987672 (2015-08-22) | 同日同比分 |
| 55 | 2015-08-26 | 水晶宫 1:1 什鲁斯 | EXCEL-8457 | FOTMOB-2077151 (2015-08-26) | 同日同比分 |
| 56 | 2015-08-26 | 罗瑟汉姆 1:2 诺维奇 | EXCEL-8469 | FOTMOB-2077161 (2015-08-26) | 同日同比分 |
| 57 | 2015-08-29 | 罗瑟汉姆 1:3 富勒姆 | EXCEL-8596 | FOTMOB-1987683 (2015-08-29) | 同日同比分 |
| 58 | 2015-09-12 | 查尔顿 1:1 罗瑟汉姆 | EXCEL-8994 | FOTMOB-1987691 (2015-09-12) | 同日同比分 |
| 59 | 2015-09-16 | 布赖顿 2:1 罗瑟汉姆 | EXCEL-9161 | FOTMOB-1987699 (2015-09-16) | 同日同比分 |
| 60 | 2015-09-16 | 谢菲联 2:3 科切斯特 | EXCEL-9169 | FOTMOB-1987157 (2015-09-16) | 同日同比分 |
| 61 | 2015-09-19 | 罗瑟汉姆 2:1 加的夫城 | EXCEL-9302 | FOTMOB-1987719 (2015-09-19) | 同日同比分 |
| 62 | 2015-09-26 | 伯明翰 0:2 罗瑟汉姆 | EXCEL-9647 | FOTMOB-1987722 (2015-09-26) | 同日同比分 |
| 63 | 2015-09-26 | 斯文登 1:2 科切斯特 | EXCEL-9665 | FOTMOB-1987194 (2015-09-26) | 同日同比分 |
| 64 | 2015-10-03 | 罗瑟汉姆 1:2 伯恩利 | EXCEL-9919 | FOTMOB-1987743 (2015-10-03) | 同日同比分 |
| 65 | 2015-10-03 | 考文垂 3:0 什鲁斯 | EXCEL-9963 | FOTMOB-1987211 (2015-10-03) | 同日同比分 |
| 66 | 2015-10-10 | 什鲁斯 4:2 科切斯特 | EXCEL-10194 | FOTMOB-1987228 (2015-10-10) | 同日同比分 |
| 67 | 2015-10-17 | 布伦特 2:1 罗瑟汉姆 | EXCEL-10363 | FOTMOB-1987747 (2015-10-17) | 同日同比分 |
| 68 | 2015-10-21 | 罗瑟汉姆 1:1 雷丁 | EXCEL-10538 | FOTMOB-1987767 (2015-10-21) | 同日同比分 |
| 69 | 2015-10-24 | 罗瑟汉姆 1:2 谢周三 | EXCEL-10631 | FOTMOB-1987780 (2015-10-24) | 同日同比分 |
| 70 | 2015-10-31 | 德比郡 3:0 罗瑟汉姆 | EXCEL-10941 | FOTMOB-1987786 (2015-10-31) | 同日同比分 |
| 71 | 2015-11-04 | 米堡 1:0 罗瑟汉姆 | EXCEL-11104 | FOTMOB-1987801 (2015-11-04) | 同日同比分 |
| 72 | 2015-11-07 | 罗瑟汉姆 2:5 伊普斯 | EXCEL-11221 | FOTMOB-1987816 (2015-11-07) | 同日同比分 |
| 73 | 2015-11-14 | 科切斯特 1:3 考文垂 | EXCEL-11463 | FOTMOB-1987285 (2015-11-14) | 同日同比分 |
| 74 | 2015-11-21 | 利兹联 0:1 罗瑟汉姆 | EXCEL-11602 | FOTMOB-1987824 (2015-11-21) | 同日同比分 |
| 75 | 2015-11-21 | 米尔沃尔 4:1 科切斯特 | EXCEL-11612 | FOTMOB-1987297 (2015-11-21) | 同日同比分 |
| 76 | 2015-11-25 | 谢菲联 2:4 什鲁斯 | EXCEL-11758 | FOTMOB-1987313 (2015-11-25) | 同日同比分 |
| 77 | 2015-11-28 | 罗瑟汉姆 3:0 布城 | EXCEL-11861 | FOTMOB-1987840 (2015-11-28) | 同日同比分 |
| 78 | 2015-12-05 | 罗瑟汉姆 1:2 伍尔弗 | EXCEL-12096 | FOTMOB-1987852 (2015-12-05) | 同日同比分 |
| 79 | 2015-12-06 | 牛津联 1:0 绿色森林 | EXCEL-12154 | FOTMOB-2136418 (2015-12-06) | 同日同比分 |
| 80 | 2015-12-12 | 布莱克本 1:0 罗瑟汉姆 | EXCEL-12272 | FOTMOB-1987854 (2015-12-12) | 同日同比分 |
| 81 | 2015-12-16 | 哈德斯菲尔德 2:0 罗瑟汉姆 | EXCEL-12390 | FOTMOB-1987871 (2015-12-16) | 同日同比分 |
| 82 | 2015-12-19 | 罗瑟汉姆 2:0 赫尔城 | EXCEL-12491 | FOTMOB-1987888 (2015-12-19) | 同日同比分 |
| 83 | 2015-12-19 | 什鲁斯 0:1 斯文登 | EXCEL-12500 | FOTMOB-1987349 (2015-12-19) | 同日同比分 |
| 84 | 2015-12-26 | 罗瑟汉姆 4:0 博尔顿 | EXCEL-12595 | FOTMOB-1987899 (2015-12-26) | 同日同比分 |
| 85 | 2015-12-30 | 富勒姆 4:1 罗瑟汉姆 | EXCEL-12653 | FOTMOB-1987908 (2015-12-30) | 同日同比分 |
| 86 | 2016-01-02 | 普雷斯顿 2:1 罗瑟汉姆 | EXCEL-12692 | FOTMOB-1987923 (2016-01-02) | 同日同比分 |
| 87 | 2016-01-09 | 科切斯特 2:1 查尔顿 | EXCEL-12805 | FOTMOB-2146192 (2016-01-09) | 同日同比分 |
| 88 | 2016-01-09 | 利兹联 2:0 罗瑟汉姆 | EXCEL-12810 | FOTMOB-2146173 (2016-01-09) | 同日同比分 |
| 89 | 2016-01-11 | 勒沃库森 1:0 圣菲独立 | EXCEL-12885 | FUTBOL24-F8839EFA5443DE45 (2016-01-10) | 相邻日同比分 |
| 90 | 2016-01-11 | 加的夫城 0:1 什鲁斯 | EXCEL-12878 | FOTMOB-2146174 (2016-01-11) | 同日同比分 |
| 91 | 2016-01-13 | 罗瑟汉姆 2:0 布赖顿 | EXCEL-12909 | FOTMOB-1987935 (2016-01-13) | 同日同比分 |
| 92 | 2016-01-14 | 沙尔克04 0:3 米内罗竞技 | EXCEL-12937 | FUTBOL24-93A2BF34DD1BCF8D (2016-01-14) | 同日同比分 |
| 93 | 2016-01-16 | 罗瑟汉姆 0:3 女王巡游 | EXCEL-13003 | FOTMOB-1987947 (2016-01-16) | 同日同比分 |
| 94 | 2016-01-16 | 科切斯特 1:2 谢菲联 | EXCEL-13008 | FOTMOB-1987403 (2016-01-16) | 同日同比分 |
| 95 | 2016-01-23 | 加的夫城 2:2 罗瑟汉姆 | EXCEL-13203 | FOTMOB-1987954 (2016-01-23) | 同日同比分 |
| 96 | 2016-01-30 | 科切斯特 1:4 热刺 | EXCEL-13375 | FOTMOB-2168550 (2016-01-30) | 同日同比分 |
| 97 | 2016-01-30 | 罗瑟汉姆 1:4 查尔顿 | EXCEL-13386 | FOTMOB-1987971 (2016-01-30) | 同日同比分 |
| 98 | 2016-01-30 | 什鲁斯 3:2 谢周三 | EXCEL-13397 | FOTMOB-2168558 (2016-01-30) | 同日同比分 |
| 99 | 2016-02-06 | 博尔顿 2:1 罗瑟汉姆 | EXCEL-13641 | FOTMOB-1987975 (2016-02-06) | 同日同比分 |
| 100 | 2016-02-20 | 伯恩利 2:0 罗瑟汉姆 | EXCEL-13926 | FOTMOB-1988001 (2016-02-20) | 同日同比分 |
| 101 | 2016-02-23 | 什鲁斯 0:3 曼联 | EXCEL-14042 | FOTMOB-2180766 (2016-02-23) | 同日同比分 |
| 102 | 2016-02-24 | 雷丁 1:0 罗瑟汉姆 | EXCEL-14074 | FOTMOB-1988021 (2016-02-24) | 同日同比分 |
| 103 | 2016-02-27 | 罗瑟汉姆 2:1 布伦特 | EXCEL-14203 | FOTMOB-1988032 (2016-02-27) | 同日同比分 |
| 104 | 2016-02-27 | 科切斯特 0:0 什鲁斯 | EXCEL-14206 | FOTMOB-1987474 (2016-02-27) | 同日同比分 |
| 105 | 2016-03-05 | 谢周三 0:1 罗瑟汉姆 | EXCEL-14511 | FOTMOB-1988045 (2016-03-05) | 同日同比分 |
| 106 | 2016-03-09 | 罗瑟汉姆 1:0 米堡 | EXCEL-14665 | FOTMOB-1988055 (2016-03-09) | 同日同比分 |
| 107 | 2016-03-09 | 什鲁斯 2:1 考文垂 | EXCEL-14669 | FOTMOB-1987468 (2016-03-09) | 同日同比分 |
| 108 | 2016-03-12 | 罗瑟汉姆 3:3 德比郡 | EXCEL-14775 | FOTMOB-1988068 (2016-03-12) | 同日同比分 |
| 109 | 2016-03-19 | 伊普斯 0:1 罗瑟汉姆 | EXCEL-15054 | FOTMOB-1988075 (2016-03-19) | 同日同比分 |
| 110 | 2016-03-30 | 考文垂 0:1 科切斯特 | EXCEL-15349 | FOTMOB-1987545 (2016-03-30) | 同日同比分 |
| 111 | 2016-04-02 | 罗瑟汉姆 2:1 利兹联 | EXCEL-15453 | FOTMOB-1988092 (2016-04-02) | 同日同比分 |
| 112 | 2016-04-02 | 科切斯特 0:0 米尔沃尔 | EXCEL-15459 | FOTMOB-1987561 (2016-04-02) | 同日同比分 |
| 113 | 2016-04-06 | 布城 1:1 罗瑟汉姆 | EXCEL-15617 | FOTMOB-1988096 (2016-04-06) | 同日同比分 |
| 114 | 2016-04-09 | 米尔顿 0:4 罗瑟汉姆 | EXCEL-15752 | FOTMOB-1988112 (2016-04-09) | 同日同比分 |
| 115 | 2016-04-09 | 米尔沃尔 3:1 什鲁斯 | EXCEL-15760 | FOTMOB-1987572 (2016-04-09) | 同日同比分 |
| 116 | 2016-04-16 | 罗瑟汉姆 0:0 诺丁汉 | EXCEL-16030 | FOTMOB-1988127 (2016-04-16) | 同日同比分 |
| 117 | 2016-04-20 | 罗瑟汉姆 1:1 哈德斯菲尔德 | EXCEL-16187 | FOTMOB-1988137 (2016-04-20) | 同日同比分 |
| 118 | 2016-04-20 | 什鲁斯 1:2 谢菲联 | EXCEL-16197 | FOTMOB-1987611 (2016-04-20) | 同日同比分 |
| 119 | 2016-04-23 | 伍尔弗 0:0 罗瑟汉姆 | EXCEL-16344 | FOTMOB-1988152 (2016-04-23) | 同日同比分 |
| 120 | 2016-04-30 | 罗瑟汉姆 0:1 布莱克本 | EXCEL-16603 | FOTMOB-1988163 (2016-04-30) | 同日同比分 |
| 121 | 2016-05-07 | 赫尔城 5:1 罗瑟汉姆 | EXCEL-16831 | FOTMOB-1987072 (2016-05-07) | 同日同比分 |
| 122 | 2016-05-08 | 斯文登 3:0 什鲁斯 | EXCEL-16920 | FOTMOB-1987648 (2016-05-08) | 同日同比分 |
| 123 | 2016-07-09 | 科克城 0:3 富勒姆 | FUTBOL24-A6705C644DB6D8BA | ESPN-457314 (2016-07-09) | 同日同比分 |
| 124 | 2016-07-09 | 格伦托兰 1:2 圣约翰 | FUTBOL24-0AE679575A7596AF | ESPN-457316 (2016-07-09) | 同日同比分 |
| 125 | 2016-07-13 | 绿色森林 3:1 加的夫城 | FUTBOL24-6C84ADCB69DFFC00 | ESPN-457338 (2016-07-14) | 相邻日同比分 |
| 126 | 2016-07-14 | 巴黎圣曼 2:1 西布罗姆 | EXCEL-18064 | FUTBOL24-E99B51337DD505A4 (2016-07-13) | 相邻日同比分 |
| 127 | 2016-07-16 | 什鲁斯 0:4 加的夫城 | FUTBOL24-A8202FE41071BB49 | ESPN-457553 (2016-07-16) | 同日同比分 |
| 128 | 2016-07-16 | 慕1860 1:0 多特蒙德 | EXCEL-18133 | FUTBOL24-AEC540D140762ECE (2016-07-16) | 同日同比分 |
| 129 | 2016-07-18 | 科克城 1:2 伍尔弗 | FUTBOL24-6059E0329898A9A1 | ESPN-458464 (2016-07-19) | 相邻日同比分 |
| 130 | 2016-07-19 | 曼斯菲尔德 0:1 赫尔城 | FUTBOL24-7A9C9980E6182B73 | ESPN-458468 (2016-07-20) | 相邻日同比分 |
| 131 | 2016-07-19 | 哈利法克 2:0 谢菲联 | FUTBOL24-D6C127194AE0E13B | ESPN-458482 (2016-07-20) | 相邻日同比分 |
| 132 | 2016-07-20 | 哈德斯菲尔德 0:2 利物浦 | FUTBOL24-9846BBFFC90AF2FB | ESPN-458537 (2016-07-21) | 相邻日同比分 |
| 133 | 2016-07-23 | 蒙彼利埃 2:1 克莱蒙 | EXCEL-18257 | FUTBOL24-9F36DFF564FC2DD6 (2016-07-22) | 相邻日同比分 |
| 134 | 2016-07-26 | 尼永 0:2 桑德兰 | EXCEL-18353 | FUTBOL24-861CD80FB7C78938 (2016-07-25) | 相邻日同比分 |
| 135 | 2016-07-26 | 科切斯特 0:1 水晶宫 | EXCEL-18350 | FUTBOL24-BF6E13D536EF5CAC (2016-07-25) | 相邻日同比分 |
| 136 | 2016-07-27 | 不来梅 0:0 哈德斯菲尔德 | EXCEL-18371 | FUTBOL24-10D151495E43AD39 (2016-07-26) | 相邻日同比分 |
| 137 | 2016-07-28 | 蒙彼利埃 0:3 图卢兹 | EXCEL-18393 | FUTBOL24-703AA221C14B7DEE (2016-07-27) | 相邻日同比分 |
| 138 | 2016-07-28 | 第戎 2:3 桑德兰 | EXCEL-18378 | FUTBOL24-AEC9E377F13E2420 (2016-07-27) | 相邻日同比分 |
| 139 | 2016-07-28 | 国际米兰 1:1 拉普大学 | EXCEL-18390 | FUTBOL24-8611393D9A2394DD (2016-07-28) | 同日同比分 |
| 140 | 2016-07-29 | 美因茨 0:1 塞维利亚 | EXCEL-18403 | ESPN-461435 (2016-07-29) | 同日同比分 |
| 141 | 2016-07-30 | 因戈施塔 0:1 哈德斯菲尔德 | EXCEL-18418 | FUTBOL24-8B12821E5165929B (2016-07-29) | 相邻日同比分 |
| 142 | 2016-07-30 | 凯泽 1:1 梅斯 | EXCEL-18419 | FUTBOL24-A0398DE2617091CF (2016-07-29) | 相邻日同比分 |
| 143 | 2016-07-30 | 弗赖堡 0:0 慕1860 | EXCEL-18467 | FUTBOL24-284097FEC43912C6 (2016-07-30) | 同日同比分 |
| 144 | 2016-07-31 | 蒙彼利埃 1:1 桑德兰 | EXCEL-18472 | FUTBOL24-BA8DBBEEAA495D76 (2016-07-30) | 相邻日同比分 |
| 145 | 2016-08-01 | 瓜达拉 1:3 阿森纳 | EXCEL-18558 | ESPN-460617 (2016-08-01)<br>FUTBOL24-A26B1E7821F493E6 (2016-08-01) | 同日同比分 |
| 146 | 2016-08-03 | 里泽 1:3 赫尔城 | FUTBOL24-F00067317935F595 | ESPN-461439 (2016-08-04) | 相邻日同比分 |
| 147 | 2016-08-06 | 罗瑟汉姆 2:2 伍尔弗 | EXCEL-18704 | FOTMOB-2259293 (2016-08-06) | 同日同比分 |
| 148 | 2016-08-06 | 什鲁斯 0:1 米尔顿 | EXCEL-18714 | FOTMOB-2259844 (2016-08-06) | 同日同比分 |
| 149 | 2016-08-07 | 蒙彼利埃 3:0 贝蒂斯 | EXCEL-18733 | FUTBOL24-54BFB3F1D43DB591 (2016-08-06) | 相邻日同比分 |
| 150 | 2016-08-07 | 美因茨 4:0 利物浦 | EXCEL-18801 | ESPN-462145 (2016-08-07) | 同日同比分 |
| 151 | 2016-08-10 | 布赖顿 4:0 科切斯特 | EXCEL-18854 | FOTMOB-2260994 (2016-08-10) | 同日同比分 |
| 152 | 2016-08-10 | 什鲁斯 2:1 哈德斯菲尔德 | EXCEL-18873 | FOTMOB-2261022 (2016-08-10) | 同日同比分 |
| 153 | 2016-08-13 | 维拉 3:0 罗瑟汉姆 | EXCEL-19023 | FOTMOB-2259296 (2016-08-13) | 同日同比分 |
| 154 | 2016-08-13 | 考文垂 0:0 什鲁斯 | EXCEL-19028 | FOTMOB-2259852 (2016-08-13) | 同日同比分 |
| 155 | 2016-08-17 | 布赖顿 3:0 罗瑟汉姆 | EXCEL-19176 | FOTMOB-2259311 (2016-08-17) | 同日同比分 |
| 156 | 2016-08-17 | 查尔顿 3:0 什鲁斯 | EXCEL-19185 | FOTMOB-2259862 (2016-08-17) | 同日同比分 |
| 157 | 2016-08-20 | 罗瑟汉姆 1:0 布伦特 | EXCEL-19324 | FOTMOB-2259330 (2016-08-20) | 同日同比分 |
| 158 | 2016-08-25 | 桑德兰 1:0 什鲁斯 | EXCEL-19533 | FOTMOB-2346055 (2016-08-25) | 同日同比分 |
| 159 | 2016-09-10 | 罗瑟汉姆 2:2 布城 | EXCEL-20058 | FOTMOB-2259353 (2016-09-10) | 同日同比分 |
| 160 | 2016-09-15 | 罗瑟汉姆 2:2 诺丁汉 | EXCEL-20260 | FOTMOB-2259364 (2016-09-15) | 同日同比分 |
| 161 | 2016-09-17 | 布莱克本 4:2 罗瑟汉姆 | EXCEL-20377 | FOTMOB-2259370 (2016-09-17) | 同日同比分 |
| 162 | 2016-09-24 | 罗瑟汉姆 1:2 加的夫城 | EXCEL-20758 | FOTMOB-2259389 (2016-09-24) | 同日同比分 |
| 163 | 2016-09-24 | 温布尔登 1:1 什鲁斯 | EXCEL-20762 | FOTMOB-2259932 (2016-09-24) | 同日同比分 |
| 164 | 2016-09-28 | 哈德斯菲尔德 2:1 罗瑟汉姆 | EXCEL-20955 | FOTMOB-2259399 (2016-09-28) | 同日同比分 |
| 165 | 2016-10-01 | 罗瑟汉姆 0:1 纽卡斯尔 | EXCEL-21101 | FOTMOB-2259413 (2016-10-01) | 同日同比分 |
| 166 | 2016-10-01 | 什鲁斯 1:1 斯文登 | EXCEL-21114 | FOTMOB-2259965 (2016-10-01) | 同日同比分 |
| 167 | 2016-10-15 | 诺维奇 3:1 罗瑟汉姆 | EXCEL-21514 | FOTMOB-2259424 (2016-10-15) | 同日同比分 |
| 168 | 2016-10-19 | 伯明翰 4:2 罗瑟汉姆 | EXCEL-21690 | FOTMOB-2259429 (2016-10-19) | 同日同比分 |
| 169 | 2016-10-19 | 什鲁斯 0:3 谢菲联 | EXCEL-21708 | FOTMOB-2260002 (2016-10-19) | 同日同比分 |
| 170 | 2016-10-22 | 罗瑟汉姆 0:1 雷丁 | EXCEL-21842 | FOTMOB-2259448 (2016-10-22) | 同日同比分 |
| 171 | 2016-10-22 | 什鲁斯 2:4 北安普敦 | EXCEL-21856 | FOTMOB-2260014 (2016-10-22) | 同日同比分 |
| 172 | 2016-10-29 | 伊普斯 2:2 罗瑟汉姆 | EXCEL-22159 | FOTMOB-2259459 (2016-10-29) | 同日同比分 |
| 173 | 2016-11-05 | 罗瑟汉姆 1:3 普雷斯顿 | EXCEL-22460 | FOTMOB-2259472 (2016-11-05) | 同日同比分 |
| 174 | 2016-11-11 | 慕1860 3:0 因戈施塔 | EXCEL-22650 | FUTBOL24-4B3BDD1E2A907D6F (2016-11-10) | 相邻日同比分 |
| 175 | 2016-11-12 | 什鲁斯 2:0 牛津联 | EXCEL-22704 | FOTMOB-2260038 (2016-11-12) | 同日同比分 |
| 176 | 2016-11-19 | 德比郡 3:0 罗瑟汉姆 | EXCEL-22846 | FOTMOB-2259481 (2016-11-19) | 同日同比分 |
| 177 | 2016-11-19 | 谢菲联 2:1 什鲁斯 | EXCEL-22860 | FOTMOB-2260049 (2016-11-19) | 同日同比分 |
| 178 | 2016-11-27 | 罗瑟汉姆 1:2 利兹联 | EXCEL-23142 | FOTMOB-2259498 (2016-11-27) | 同日同比分 |
| 179 | 2016-12-10 | 罗瑟汉姆 1:0 女王巡游 | EXCEL-23616 | FOTMOB-2259522 (2016-12-10) | 同日同比分 |
| 180 | 2016-12-10 | 米尔沃尔 0:1 什鲁斯 | EXCEL-23623 | FOTMOB-2260080 (2016-12-10) | 同日同比分 |
| 181 | 2016-12-14 | 富勒姆 2:1 罗瑟汉姆 | EXCEL-23732 | FOTMOB-2259529 (2016-12-14) | 同日同比分 |
| 182 | 2016-12-17 | 谢周三 1:0 罗瑟汉姆 | EXCEL-23825 | FOTMOB-2259546 (2016-12-17) | 同日同比分 |
| 183 | 2016-12-26 | 博尔顿 2:1 什鲁斯 | EXCEL-24027 | FOTMOB-2260100 (2016-12-26) | 同日同比分 |
| 184 | 2017-01-02 | 利兹联 3:0 罗瑟汉姆 | EXCEL-24121 | FOTMOB-2259578 (2017-01-02) | 同日同比分 |
| 185 | 2017-01-06 | 美因茨 1:1 海牙 | EXCEL-24164 | ESPN-490283 (2017-01-06) | 同日同比分 |
| 186 | 2017-01-07 | 美因茨 0:2 费耶诺德 | EXCEL-24180 | ESPN-490281 (2017-01-07) | 同日同比分 |
| 187 | 2017-01-07 | 斯文登 1:1 什鲁斯 | EXCEL-24186 | FOTMOB-2260147 (2017-01-07) | 同日同比分 |
| 188 | 2017-01-07 | 罗瑟汉姆 2:3 牛津联 | EXCEL-24203 | FOTMOB-2418669 (2017-01-07) | 同日同比分 |
| 189 | 2017-01-09 | 拉普大学 1:1 勒沃库森 | EXCEL-24261 | FUTBOL24-3673C527C5206C47 (2017-01-08) | 相邻日同比分 |
| 190 | 2017-01-12 | 勒沃库森 1:0 米内罗竞技 | EXCEL-24292 | FUTBOL24-EBBED038311168F8 (2017-01-12) | 同日同比分 |
| 191 | 2017-01-14 | 罗瑟汉姆 2:1 诺维奇 | EXCEL-24346 | FOTMOB-2259593 (2017-01-14) | 同日同比分 |
| 192 | 2017-01-18 | 帕德博恩 1:6 多特蒙德 | ESPN-490276 | EXCEL-24431 (2017-01-18)<br>FUTBOL24-B20A9E941F510EE5 (2017-01-17) | 官方比分纠错 |
| 193 | 2017-01-21 | 纽卡斯尔 4:0 罗瑟汉姆 | EXCEL-24521 | FOTMOB-2259603 (2017-01-21) | 同日同比分 |
| 194 | 2017-02-04 | 布城 1:0 罗瑟汉姆 | EXCEL-24762 | FOTMOB-2259636 (2017-02-04) | 同日同比分 |
| 195 | 2017-02-11 | 罗瑟汉姆 1:1 布莱克本 | EXCEL-25003 | FOTMOB-2259653 (2017-02-11) | 同日同比分 |
| 196 | 2017-02-15 | 罗瑟汉姆 2:3 哈德斯菲尔德 | EXCEL-25119 | FOTMOB-2259664 (2017-02-15) | 同日同比分 |
| 197 | 2017-02-18 | 加的夫城 5:0 罗瑟汉姆 | EXCEL-25216 | FOTMOB-2259674 (2017-02-18) | 同日同比分 |
| 198 | 2017-02-18 | 什鲁斯 2:1 温布尔登 | EXCEL-25227 | FOTMOB-2260228 (2017-02-18) | 同日同比分 |
| 199 | 2017-02-25 | 布伦特 4:2 罗瑟汉姆 | EXCEL-25461 | FOTMOB-2259682 (2017-02-25) | 同日同比分 |
| 200 | 2017-02-25 | 米尔顿 2:1 什鲁斯 | EXCEL-25473 | FOTMOB-2260239 (2017-02-25) | 同日同比分 |
| 201 | 2017-03-01 | 什鲁斯 4:3 查尔顿 | EXCEL-25604 | FOTMOB-2260251 (2017-03-01) | 同日同比分 |
| 202 | 2017-03-04 | 罗瑟汉姆 0:2 维拉 | EXCEL-25739 | FOTMOB-2259702 (2017-03-04) | 同日同比分 |
| 203 | 2017-03-04 | 什鲁斯 0:0 考文垂 | EXCEL-25749 | FOTMOB-2260264 (2017-03-04) | 同日同比分 |
| 204 | 2017-03-08 | 罗瑟汉姆 0:2 布赖顿 | EXCEL-25882 | FOTMOB-2259713 (2017-03-08) | 同日同比分 |
| 205 | 2017-03-11 | 伍尔弗 1:0 罗瑟汉姆 | EXCEL-25991 | FOTMOB-2259727 (2017-03-11) | 同日同比分 |
| 206 | 2017-03-18 | 女王巡游 5:1 罗瑟汉姆 | EXCEL-26270 | FOTMOB-2259737 (2017-03-18) | 同日同比分 |
| 207 | 2017-03-25 | 什鲁斯 0:2 博尔顿 | EXCEL-26464 | FOTMOB-2260313 (2017-03-25) | 同日同比分 |
| 208 | 2017-04-01 | 罗瑟汉姆 0:1 富勒姆 | EXCEL-26626 | FOTMOB-2259750 (2017-04-01) | 同日同比分 |
| 209 | 2017-04-05 | 罗瑟汉姆 0:2 谢周三 | EXCEL-26797 | FOTMOB-2259760 (2017-04-05) | 同日同比分 |
| 210 | 2017-04-05 | 什鲁斯 1:2 米尔沃尔 | EXCEL-26800 | FOTMOB-2260289 (2017-04-05) | 同日同比分 |
| 211 | 2017-04-14 | 罗瑟汉姆 1:1 伯明翰 | EXCEL-27180 | FOTMOB-2259784 (2017-04-14) | 同日同比分 |
| 212 | 2017-04-17 | 雷丁 2:1 罗瑟汉姆 | EXCEL-27406 | FOTMOB-2259799 (2017-04-17) | 同日同比分 |
| 213 | 2017-04-17 | 北安普敦 1:1 什鲁斯 | EXCEL-27411 | FOTMOB-2260357 (2017-04-17) | 同日同比分 |
| 214 | 2017-04-22 | 罗瑟汉姆 1:0 伊普斯 | EXCEL-27543 | FOTMOB-2259808 (2017-04-22) | 同日同比分 |
| 215 | 2017-04-29 | 普雷斯顿 1:1 罗瑟汉姆 | EXCEL-27839 | FOTMOB-2259821 (2017-04-29) | 同日同比分 |
| 216 | 2017-04-30 | 牛津联 2:0 什鲁斯 | EXCEL-27911 | FOTMOB-2260382 (2017-04-30) | 同日同比分 |
| 217 | 2017-05-07 | 罗瑟汉姆 1:1 德比郡 | EXCEL-28168 | FOTMOB-2259832 (2017-05-07) | 同日同比分 |
| 218 | 2017-07-11 | 埃森 3:2 多特蒙德 | FUTBOL24-C8C1CAC5932A08FF | ESPN-490202 (2017-07-12) | 相邻日同比分 |
| 219 | 2017-07-12 | 西布罗姆 1:2 斯拉维亚 | EXCEL-29605 | FUTBOL24-8C586425D8B0B0AF (2017-07-12) | 同日同比分 |
| 220 | 2017-07-19 | 莱切斯特 1:1 西布罗姆 | EXCEL-29737 | FUTBOL24-523CA38F22CE3663 (2017-07-19) | 同日同比分 |
| 221 | 2017-07-20 | 瓜达拉 2:2 波尔图 | FUTBOL24-E0E5D1DB23C784BC | ESPN-490118 (2017-07-20) | 同日同比分 |
| 222 | 2017-07-22 | 水晶宫 2:0 西布罗姆 | EXCEL-29813 | FUTBOL24-7467A4FDF969C7BB (2017-07-22) | 同日同比分 |
| 223 | 2017-07-29 | 柏林赫塔 0:3 利物浦 | FUTBOL24-7D95A2391317E565 | ESPN-489553 (2017-07-30) | 相邻日同比分 |
| 224 | 2017-08-04 | 塔拉戈纳 1:1 巴萨 | FUTBOL24-3CE1F75C9B952846 | ESPN-492041 (2017-08-05) | 相邻日同比分 |
| 225 | 2017-08-05 | 什鲁斯 1:0 北安普敦 | EXCEL-30197 | FOTMOB-2529748 (2017-08-05) | 同日同比分 |
| 226 | 2017-08-09 | 诺丁汉 2:1 什鲁斯 | EXCEL-30316 | FOTMOB-2526578 (2017-08-09) | 同日同比分 |
| 227 | 2017-08-09 | 绿色森林 0:0 米尔顿 | EXCEL-30327 | FOTMOB-2526599 (2017-08-09) | 同日同比分 |
| 228 | 2017-08-10 | 科切斯特 1:2 维拉 | EXCEL-30357 | FOTMOB-2526575 (2017-08-10) | 同日同比分 |
| 229 | 2017-08-12 | 温布尔登 0:1 什鲁斯 | EXCEL-30440 | FOTMOB-2529750 (2017-08-12) | 同日同比分 |
| 230 | 2017-08-24 | 哈德斯菲尔德 2:1 罗瑟汉姆 | EXCEL-30886 | FOTMOB-2605247 (2017-08-24) | 同日同比分 |
| 231 | 2017-08-26 | 牛津联 1:1 什鲁斯 | EXCEL-31003 | FOTMOB-2529780 (2017-08-26) | 同日同比分 |
| 232 | 2017-08-26 | 罗瑟汉姆 0:2 查尔顿 | EXCEL-31006 | FOTMOB-2529783 (2017-08-26) | 同日同比分 |
| 233 | 2017-09-03 | 朴次茅斯 0:1 罗瑟汉姆 | EXCEL-31273 | FOTMOB-2529794 (2017-09-03) | 同日同比分 |
| 234 | 2017-09-03 | 瓜达拉 0:2 麦国民 | FUTBOL24-8EB382C5E178B1B1 | ESPN-496359 (2017-09-04) | 相邻日同比分 |
| 235 | 2017-09-23 | 什鲁斯 1:1 布莱克本 | EXCEL-32096 | FOTMOB-2529845 (2017-09-23) | 同日同比分 |
| 236 | 2017-09-27 | 布莱克本 2:0 罗瑟汉姆 | EXCEL-32260 | FOTMOB-2529846 (2017-09-27) | 同日同比分 |
| 237 | 2017-09-30 | 罗瑟汉姆 1:0 北安普敦 | EXCEL-32415 | FOTMOB-2529866 (2017-09-30) | 同日同比分 |
| 238 | 2017-10-14 | 普利茅斯 1:1 什鲁斯 | EXCEL-32827 | FOTMOB-2529889 (2017-10-14) | 同日同比分 |
| 239 | 2017-10-18 | 温布尔登 3:1 罗瑟汉姆 | EXCEL-32982 | FOTMOB-2529894 (2017-10-18) | 同日同比分 |
| 240 | 2017-10-21 | 牛津联 3:3 罗瑟汉姆 | EXCEL-33133 | FOTMOB-2529913 (2017-10-21) | 同日同比分 |
| 241 | 2017-11-18 | 罗瑟汉姆 1:2 什鲁斯 | EXCEL-34117 | FOTMOB-2529951 (2017-11-18) | 同日同比分 |
| 242 | 2017-12-09 | 米尔顿 1:1 什鲁斯 | EXCEL-34916 | FOTMOB-2529983 (2017-12-09) | 同日同比分 |
| 243 | 2017-12-16 | 罗瑟汉姆 1:1 普利茅斯 | EXCEL-35143 | FOTMOB-2529998 (2017-12-16) | 同日同比分 |
| 244 | 2017-12-23 | 罗瑟汉姆 2:1 米尔顿 | EXCEL-35327 | FOTMOB-2530011 (2017-12-23) | 同日同比分 |
| 245 | 2017-12-23 | 什鲁斯 2:0 朴次茅斯 | EXCEL-35329 | FOTMOB-2530013 (2017-12-23) | 同日同比分 |
| 246 | 2017-12-30 | 开罗国民 2:3 马竞 | FUTBOL24-692AE8F77E90CB74 | ESPN-499829 (2017-12-31) | 相邻日同比分 |
| 247 | 2018-01-01 | 罗瑟汉姆 1:1 布莱克本 | EXCEL-35488 | FOTMOB-2530047 (2018-01-01) | 同日同比分 |
| 248 | 2018-01-07 | 什鲁斯 0:0 西汉姆联 | EXCEL-35589 | FOTMOB-2678201 (2018-01-07) | 同日同比分 |
| 249 | 2018-01-09 | 拜仁 5:3 SG Sonnenhof Großaspach | FUTBOL24-EF2A5C7ED059B07D | ESPN-502754 (2018-01-10) | 相邻日同比分 |
| 250 | 2018-01-12 | 米内罗竞技 0:1 流浪者 | FUTBOL24-8528C42D7A8111C7 | ESPN-501639 (2018-01-12) | 同日同比分 |
| 251 | 2018-01-13 | 布莱克本 3:1 什鲁斯 | EXCEL-35702 | FOTMOB-2530062 (2018-01-13) | 同日同比分 |
| 252 | 2018-01-14 | 麦国民 2:0 米内罗竞技 | FUTBOL24-C44C06FE747C2290 | ESPN-501635 (2018-01-15) | 相邻日同比分 |
| 253 | 2018-01-17 | 西汉姆联 0:0 什鲁斯 | EXCEL-35769 | FOTMOB-2694489 (2018-01-17) | 同日同比分 |
| 254 | 2018-01-20 | 罗瑟汉姆 1:0 朴次茅斯 | EXCEL-35867 | FOTMOB-2530083 (2018-01-20) | 同日同比分 |
| 255 | 2018-01-27 | 朴次茅斯 0:1 什鲁斯 | EXCEL-36065 | FOTMOB-2530094 (2018-01-27) | 同日同比分 |
| 256 | 2018-02-03 | 罗瑟汉姆 2:0 温布尔登 | EXCEL-36282 | FOTMOB-2530107 (2018-02-03) | 同日同比分 |
| 257 | 2018-02-10 | 什鲁斯 1:2 普利茅斯 | EXCEL-36500 | FOTMOB-2530120 (2018-02-10) | 同日同比分 |
| 258 | 2018-02-14 | 罗瑟汉姆 3:1 牛津联 | EXCEL-36623 | FOTMOB-2530131 (2018-02-14) | 同日同比分 |
| 259 | 2018-02-24 | 查尔顿 0:2 什鲁斯 | EXCEL-36755 | FOTMOB-2530148 (2018-02-24) | 同日同比分 |
| 260 | 2018-03-14 | 米尔顿 3:2 罗瑟汉姆 | EXCEL-37427 | FOTMOB-2530092 (2018-03-14) | 同日同比分 |
| 261 | 2018-03-17 | 北安普敦 0:3 罗瑟汉姆 | EXCEL-37550 | FOTMOB-2530186 (2018-03-17) | 同日同比分 |
| 262 | 2018-03-21 | 北安普敦 1:1 什鲁斯 | EXCEL-37681 | FOTMOB-2530234 (2018-03-21) | 同日同比分 |
| 263 | 2018-03-24 | 什鲁斯 1:0 温布尔登 | EXCEL-37725 | FOTMOB-2530203 (2018-03-24) | 同日同比分 |
| 264 | 2018-04-02 | 查尔顿 3:1 罗瑟汉姆 | EXCEL-38003 | FOTMOB-2530219 (2018-04-02) | 同日同比分 |
| 265 | 2018-04-02 | 什鲁斯 3:2 牛津联 | EXCEL-38008 | FOTMOB-2530227 (2018-04-02) | 同日同比分 |
| 266 | 2018-04-18 | 什鲁斯 0:2 查尔顿 | EXCEL-38640 | FOTMOB-2529940 (2018-04-18) | 同日同比分 |
| 267 | 2018-04-28 | 普利茅斯 2:1 罗瑟汉姆 | EXCEL-39083 | FOTMOB-2530274 (2018-04-28) | 同日同比分 |
| 268 | 2018-05-06 | 什鲁斯 0:1 米尔顿 | EXCEL-39361 | FOTMOB-2530287 (2018-05-06) | 同日同比分 |
| 269 | 2018-05-11 | 查尔顿 0:1 什鲁斯 | EXCEL-39560 | FOTMOB-2757213 (2018-05-11) | 同日同比分 |
| 270 | 2018-05-11 | 开罗国民 1:0 阿贾克斯 | FUTBOL24-C492A0D63D14C38B | ESPN-510707 (2018-05-12) | 相邻日同比分 |
| 271 | 2018-05-14 | 什鲁斯 1:0 查尔顿 | EXCEL-39718 | FOTMOB-2757214 (2018-05-14) | 同日同比分 |
| 272 | 2018-05-27 | 罗瑟汉姆 1:1 什鲁斯 | EXCEL-39977 | FOTMOB-2762564 (2018-05-27) | 同日同比分 |
| 273 | 2018-07-11 | 巴黎圣曼 1:0 Sainte-Geneviève | FUTBOL24-9EA85332CA58274A | ESPN-519126 (2018-07-12) | 相邻日同比分 |
| 274 | 2018-07-13 | 维也纳 0:1 多特蒙德 | FUTBOL24-1AC35F00208A8DFC | ESPN-513169 (2018-07-14) | 相邻日同比分 |
| 275 | 2018-07-13 | 伯恩利 1:0 科克城 | FUTBOL24-3A9712481C6C1C1D | ESPN-523294 (2018-07-14) | 相邻日同比分 |
| 276 | 2018-07-13 | 洛桑 2:1 费内巴切 | FUTBOL24-E74C339988F68D1B | ESPN-518977 (2018-07-14) | 相邻日同比分 |
| 277 | 2018-07-14 | 拉蒂纳 0:9 罗马 | FUTBOL24-09642605FCC7D432 | ESPN-519130 (2018-07-15) | 相邻日同比分 |
| 278 | 2018-07-17 | 科隆 1:1 沃特福德 | FUTBOL24-4108355594A6783A | ESPN-523320 (2018-07-18) | 相邻日同比分 |
| 279 | 2018-07-17 | 圣帕特里 0:2 纽卡斯尔 | FUTBOL24-CAD08D8C882726AE | ESPN-523311 (2018-07-18) | 相邻日同比分 |
| 280 | 2018-07-17 | 特拉布宗 1:0 萨格勒布火车头 | FUTBOL24-F0AE43B3E44690B5 | ESPN-518973 (2018-07-18) | 相邻日同比分 |
| 281 | 2018-07-20 | 罗马 1:1 阿韦利诺 | FUTBOL24-AFA03C7E02B8E266 | ESPN-519120 (2018-07-21) | 相邻日同比分 |
| 282 | 2018-07-22 | 达姆施塔特 1:1 哈德斯菲尔德 | FUTBOL24-9FC725A4C6503096 | ESPN-523310 (2018-07-22) | 同日同比分 |
| 283 | 2018-07-24 | 巴伦西亚 0:0 洛桑 | FUTBOL24-E378A1D7D2CF344E | ESPN-520837 (2018-07-25) | 相邻日同比分 |
| 284 | 2018-07-25 | 罗瑟汉姆 2:1 加的夫城 | FUTBOL24-3C61B52162B03C8F | ESPN-523296 (2018-07-26) | 相邻日同比分 |
| 285 | 2018-07-25 | 蒙彼利埃 1:1 比利亚雷 | FUTBOL24-3F99E071144C6B7B | ESPN-520343 (2018-07-26) | 相邻日同比分 |
| 286 | 2018-07-25 | 莱切斯特 0:0 阿卡希萨尔 | FUTBOL24-A374223CDD7EE604 | ESPN-521926 (2018-07-26) | 相邻日同比分 |
| 287 | 2018-07-25 | 哈德斯菲尔德 3:1 里昂 | FUTBOL24-F4D5E58709A833C1 | ESPN-521539 (2018-07-26) | 相邻日同比分 |
| 288 | 2018-07-28 | 汉堡 3:1 摩纳哥 | FUTBOL24-4B984EB97A868861 | ESPN-523269 (2018-07-28) | 同日同比分 |
| 289 | 2018-07-28 | 谢周三 1:3 比利亚雷 | FUTBOL24-BDD6FEEB17993CC0 | ESPN-523270 (2018-07-28) | 同日同比分 |
| 290 | 2018-07-28 | 非洲人 0:1 加拉塔萨 | FUTBOL24-E46D038B9AAC4BB3 | ESPN-521925 (2018-07-29) | 相邻日同比分 |
| 291 | 2018-07-29 | 蒙彼利埃 0:0 伯恩利 | FUTBOL24-D18089C48BE2111B | ESPN-523283 (2018-07-29) | 同日同比分 |
| 292 | 2018-07-29 | 利兹联 1:0 拉帕马斯 | FUTBOL24-6FDB6D2F642FC066 | ESPN-523285 (2018-07-29) | 同日同比分 |
| 293 | 2018-07-31 | 博洛尼亚 1:2 哈德斯菲尔德 | FUTBOL24-2068D59A25B77DE9 | ESPN-523342 (2018-08-01) | 相邻日同比分 |
| 294 | 2018-07-31 | 西汉姆联 1:1 美因茨 | FUTBOL24-C0713E49591D819A | ESPN-523410 (2018-08-01) | 相邻日同比分 |
| 295 | 2018-08-03 | 莱红牛 0:3 哈德斯菲尔德 | FUTBOL24-220327399C246FE8 | ESPN-523418 (2018-08-03) | 同日同比分 |
| 296 | 2018-08-04 | 布伦特 5:1 罗瑟汉姆 | EXCEL-40775 | FOTMOB-2791327 (2018-08-04) | 同日同比分 |
| 297 | 2018-08-11 | 罗瑟汉姆 1:0 伊普斯 | EXCEL-40981 | FOTMOB-2791346 (2018-08-11) | 同日同比分 |
| 298 | 2018-08-11 | 查尔顿 2:1 什鲁斯 | EXCEL-40989 | FOTMOB-2792444 (2018-08-11) | 同日同比分 |
| 299 | 2018-08-15 | 汉堡 1:4 拜仁 | FUTBOL24-196E780E54F5716D | ESPN-523767 (2018-08-16) | 相邻日同比分 |
| 300 | 2018-08-18 | 利兹联 2:0 罗瑟汉姆 | EXCEL-41237 | FOTMOB-2791355 (2018-08-18) | 同日同比分 |
| 301 | 2018-08-22 | 罗瑟汉姆 2:3 赫尔城 | EXCEL-41404 | FOTMOB-2791366 (2018-08-22) | 同日同比分 |
| 302 | 2018-08-25 | 卢顿 3:2 什鲁斯 | EXCEL-41563 | FOTMOB-2792483 (2018-08-25) | 同日同比分 |
| 303 | 2018-08-26 | 罗瑟汉姆 1:0 米尔沃尔 | EXCEL-41636 | FOTMOB-2791381 (2018-08-26) | 同日同比分 |
| 304 | 2018-08-30 | 埃弗顿 3:1 罗瑟汉姆 | EXCEL-41744 | FOTMOB-2870044 (2018-08-30) | 同日同比分 |
| 305 | 2018-09-08 | 朴次茅斯 1:1 什鲁斯 | EXCEL-42072 | FOTMOB-2792508 (2018-09-08) | 同日同比分 |
| 306 | 2018-09-15 | 罗瑟汉姆 1:0 德比郡 | EXCEL-42221 | FOTMOB-2791406 (2018-09-15) | 同日同比分 |
| 307 | 2018-09-19 | 维拉 2:0 罗瑟汉姆 | EXCEL-42374 | FOTMOB-2791414 (2018-09-19) | 同日同比分 |
| 308 | 2018-09-22 | 诺丁汉 1:0 罗瑟汉姆 | EXCEL-42512 | FOTMOB-2791426 (2018-09-22) | 同日同比分 |
| 309 | 2018-09-30 | 罗瑟汉姆 2:2 斯托克城 | EXCEL-42872 | FOTMOB-2791442 (2018-09-30) | 同日同比分 |
| 310 | 2018-10-04 | 罗瑟汉姆 0:0 布城 | EXCEL-43037 | FOTMOB-2791455 (2018-10-04) | 同日同比分 |
| 311 | 2018-10-06 | 伯明翰 3:1 罗瑟汉姆 | EXCEL-43147 | FOTMOB-2791457 (2018-10-06) | 同日同比分 |
| 312 | 2018-10-20 | 罗瑟汉姆 1:1 博尔顿 | EXCEL-43516 | FOTMOB-2791477 (2018-10-20) | 同日同比分 |
| 313 | 2018-10-20 | 什鲁斯 0:2 桑德兰 | EXCEL-43528 | FOTMOB-2792593 (2018-10-20) | 同日同比分 |
| 314 | 2018-10-24 | 米堡 0:0 罗瑟汉姆 | EXCEL-43671 | FOTMOB-2791481 (2018-10-24) | 同日同比分 |
| 315 | 2018-10-27 | 普雷斯顿 1:1 罗瑟汉姆 | EXCEL-43814 | FOTMOB-2791500 (2018-10-27) | 同日同比分 |
| 316 | 2018-10-27 | 牛津联 3:0 什鲁斯 | EXCEL-43825 | FOTMOB-2792615 (2018-10-27) | 同日同比分 |
| 317 | 2018-11-03 | 罗瑟汉姆 2:1 斯旺西 | EXCEL-44117 | FOTMOB-2791513 (2018-11-03) | 同日同比分 |
| 318 | 2018-11-03 | 温布尔登 1:2 什鲁斯 | EXCEL-44119 | FOTMOB-2792620 (2018-11-03) | 同日同比分 |
| 319 | 2018-11-10 | 布莱克本 1:1 罗瑟汉姆 | EXCEL-44402 | FOTMOB-2791518 (2018-11-10) | 同日同比分 |
| 320 | 2018-11-10 | 牛津联 0:0 绿色森林 | EXCEL-44414 | FOTMOB-2911882 (2018-11-10) | 同日同比分 |
| 321 | 2018-11-21 | 绿色森林 0:3 牛津联 | EXCEL-44674 | FOTMOB-2927581 (2018-11-21) | 同日同比分 |
| 322 | 2018-11-24 | 罗瑟汉姆 2:2 谢菲联 | EXCEL-44748 | FOTMOB-2791536 (2018-11-24) | 同日同比分 |
| 323 | 2018-11-24 | 威科姆 3:2 什鲁斯 | EXCEL-44782 | FOTMOB-2792655 (2018-11-24) | 同日同比分 |
| 324 | 2018-11-28 | 罗瑟汉姆 2:2 女王巡游 | EXCEL-44905 | FOTMOB-2791545 (2018-11-28) | 同日同比分 |
| 325 | 2018-11-28 | 什鲁斯 2:0 普利茅斯 | EXCEL-44917 | FOTMOB-2792665 (2018-11-28) | 同日同比分 |
| 326 | 2018-12-01 | 诺维奇 3:1 罗瑟汉姆 | EXCEL-45045 | FOTMOB-2791559 (2018-12-01) | 同日同比分 |
| 327 | 2018-12-08 | 谢周三 2:2 罗瑟汉姆 | EXCEL-45284 | FOTMOB-2791573 (2018-12-08) | 同日同比分 |
| 328 | 2018-12-15 | 罗瑟汉姆 1:1 雷丁 | EXCEL-45491 | FOTMOB-2791586 (2018-12-15) | 同日同比分 |
| 329 | 2018-12-22 | 罗瑟汉姆 0:4 西布罗姆 | EXCEL-45660 | FOTMOB-2791597 (2018-12-22) | 同日同比分 |
| 330 | 2018-12-22 | 什鲁斯 1:0 考文垂 | EXCEL-45673 | FOTMOB-2792701 (2018-12-22) | 同日同比分 |
| 331 | 2018-12-26 | 博尔顿 2:1 罗瑟汉姆 | EXCEL-45762 | FOTMOB-2791602 (2018-12-26) | 同日同比分 |
| 332 | 2018-12-29 | 布城 1:0 罗瑟汉姆 | EXCEL-45821 | FOTMOB-2791615 (2018-12-29) | 同日同比分 |
| 333 | 2018-12-29 | 桑德兰 1:1 什鲁斯 | EXCEL-45840 | FOTMOB-2792726 (2018-12-29) | 同日同比分 |
| 334 | 2019-01-01 | 罗瑟汉姆 2:1 普雷斯顿 | EXCEL-45869 | FOTMOB-2791633 (2019-01-01) | 同日同比分 |
| 335 | 2019-01-05 | 什鲁斯 1:1 斯托克城 | EXCEL-45918 | FOTMOB-2942389 (2019-01-05) | 同日同比分 |
| 336 | 2019-01-06 | 曼城 7:0 罗瑟汉姆 | FOTMOB-2942390 | EXCEL-45959 (2019-01-06) | 官方比分纠错 |
| 337 | 2019-01-12 | 伊普斯 1:0 罗瑟汉姆 | EXCEL-46054 | FOTMOB-2791641 (2019-01-12) | 同日同比分 |
| 338 | 2019-01-12 | 什鲁斯 0:3 查尔顿 | EXCEL-46067 | FOTMOB-2792759 (2019-01-12) | 同日同比分 |
| 339 | 2019-01-16 | 斯托克城 2:3 什鲁斯 | EXCEL-46137 | FOTMOB-2960897 (2019-01-16) | 同日同比分 |
| 340 | 2019-01-19 | 罗瑟汉姆 2:4 布伦特 | EXCEL-46214 | FOTMOB-2791657 (2019-01-19) | 同日同比分 |
| 341 | 2019-01-26 | 罗瑟汉姆 1:2 利兹联 | EXCEL-46401 | FOTMOB-2791669 (2019-01-26) | 同日同比分 |
| 342 | 2019-01-26 | 什鲁斯 2:2 伍尔弗 | EXCEL-46417 | FOTMOB-2961418 (2019-01-26) | 同日同比分 |
| 343 | 2019-02-01 | 比尔森 2:2 新英格兰革命 | FUTBOL24-E687162324C52A57 | ESPN-536162 (2019-02-01) | 同日同比分 |
| 344 | 2019-02-02 | 米尔沃尔 0:0 罗瑟汉姆 | EXCEL-46621 | FOTMOB-2791679 (2019-02-02) | 同日同比分 |
| 345 | 2019-02-02 | 什鲁斯 0:3 卢顿 | EXCEL-46632 | FOTMOB-2792795 (2019-02-02) | 同日同比分 |
| 346 | 2019-02-03 | 克拉斯诺 2:0 新英格兰革命 | FUTBOL24-CACC0A2C2BC01169 | ESPN-536161 (2019-02-03) | 同日同比分 |
| 347 | 2019-02-06 | 基迪纳摩 1:3 新英格兰革命 | FUTBOL24-DFB72C7EC2112F8E | ESPN-534508 (2019-02-06) | 同日同比分 |
| 348 | 2019-02-13 | 赫尔城 2:2 罗瑟汉姆 | EXCEL-46681 | FOTMOB-2791698 (2019-02-13) | 同日同比分 |
| 349 | 2019-02-16 | 罗瑟汉姆 2:2 谢周三 | EXCEL-46739 | FOTMOB-2791718 (2019-02-16) | 同日同比分 |
| 350 | 2019-02-23 | 雷丁 1:1 罗瑟汉姆 | EXCEL-46856 | FOTMOB-2791728 (2019-02-23) | 同日同比分 |
| 351 | 2019-02-23 | 新英格兰革命 2:1 雷克雅 | FUTBOL24-D89691E4CA37AC17 | ESPN-534496 (2019-02-24) | 相邻日同比分 |
| 352 | 2019-03-02 | 罗瑟汉姆 3:2 布莱克本 | EXCEL-46990 | FOTMOB-2791740 (2019-03-02) | 同日同比分 |
| 353 | 2019-03-09 | 谢菲联 2:0 罗瑟汉姆 | EXCEL-47146 | FOTMOB-2791755 (2019-03-09) | 同日同比分 |
| 354 | 2019-03-14 | 女王巡游 1:2 罗瑟汉姆 | EXCEL-47273 | FOTMOB-2791767 (2019-03-14) | 同日同比分 |
| 355 | 2019-03-16 | 罗瑟汉姆 1:2 诺维奇 | EXCEL-47342 | FOTMOB-2791776 (2019-03-16) | 同日同比分 |
| 356 | 2019-03-30 | 德比郡 6:1 罗瑟汉姆 | FOTMOB-2791782 | EXCEL-47540 (2019-03-30) | 官方比分纠错 |
| 357 | 2019-04-06 | 罗瑟汉姆 2:1 诺丁汉 | EXCEL-47753 | FOTMOB-2791802 (2019-04-06) | 同日同比分 |
| 358 | 2019-04-11 | 罗瑟汉姆 1:2 维拉 | EXCEL-47889 | FOTMOB-2791816 (2019-04-11) | 同日同比分 |
| 359 | 2019-04-13 | 斯托克城 2:2 罗瑟汉姆 | EXCEL-47962 | FOTMOB-2791826 (2019-04-13) | 同日同比分 |
| 360 | 2019-04-19 | 斯旺西 4:3 罗瑟汉姆 | EXCEL-48079 | FOTMOB-2791839 (2019-04-19) | 同日同比分 |
| 361 | 2019-04-22 | 罗瑟汉姆 1:3 伯明翰 | EXCEL-48221 | FOTMOB-2791849 (2019-04-22) | 同日同比分 |
| 362 | 2019-04-27 | 西布罗姆 2:1 罗瑟汉姆 | EXCEL-48367 | FOTMOB-2791864 (2019-04-27) | 同日同比分 |
| 363 | 2019-05-05 | 罗瑟汉姆 1:2 米堡 | EXCEL-48576 | FOTMOB-2791873 (2019-05-05) | 同日同比分 |
| 364 | 2019-05-16 | 新英格兰革命 0:3 切尔西 | FUTBOL24-D05A80C30BBDF2FB | ESPN-538548 (2019-05-16) | 同日同比分 |
| 365 | 2019-07-10 | 费耶诺德 2:1 达姆施塔特 | FUTBOL24-D1A26142E123CB44 | ESPN-549120 (2019-07-10) | 同日同比分 |
| 366 | 2019-07-11 | 阿尔塔奇 2:3 西汉姆联 | FUTBOL24-AD37EEB80F0818F4 | ESPN-547162 (2019-07-11) | 同日同比分 |
| 367 | 2019-07-12 | 埃因霍温 3:0 阿里斯 | FUTBOL24-7CEF852C2D5B5056 | ESPN-551567 (2019-07-13) | 相邻日同比分 |
| 368 | 2019-07-13 | 圣帕特里 0:4 切尔西 | FUTBOL24-372A659AECBAED0C | ESPN-548056 (2019-07-13) | 同日同比分 |
| 369 | 2019-07-13 | 洛桑 2:1 摩纳哥 | FUTBOL24-909D11BDC28F6BE6 | ESPN-548866 (2019-07-13) | 同日同比分 |
| 370 | 2019-07-13 | 比利亚雷 3:0 西布罗姆 | FUTBOL24-41C898E504DD5EAB | ESPN-547159 (2019-07-14) | 相邻日同比分 |
| 371 | 2019-07-25 | 柏林赫塔 2:1 费内巴切 | FUTBOL24-9E19C6F44BD880C4 | ESPN-548674 (2019-07-26) | 相邻日同比分 |
| 372 | 2019-07-27 | 罗瑟汉姆 2:2 莱切斯特 | FUTBOL24-7C821AC61FA89C30 | ESPN-540314 (2019-07-27) | 同日同比分 |
| 373 | 2019-07-29 | 贝西克塔斯 0:0 Apollon Larissa | FUTBOL24-FEA2039EDFD0153D | ESPN-552061 (2019-07-30) | 相邻日同比分 |
| 374 | 2019-07-31 | 佩鲁贾 1:3 罗马 | FUTBOL24-3EF6E28A10553FEB | ESPN-553995 (2019-08-01) | 相邻日同比分 |
| 375 | 2019-07-31 | 柏林赫塔 3:5 西汉姆联 | FUTBOL24-A8E63036A95D171E | ESPN-547151 (2019-08-01) | 相邻日同比分 |
| 376 | 2019-08-03 | 圣路易斯 1:2 马竞 | FUTBOL24-93809C8F66A0FA87 | ESPN-540532 (2019-08-04) | 相邻日同比分 |
| 377 | 2019-08-10 | 费罗尼克利 0:2 AC米兰 | FUTBOL24-FAC419E11532DD87 | ESPN-556445 (2019-08-11) | 相邻日同比分 |
| 378 | 2019-11-09 | 科切斯特 0:2 考文垂 | EXCEL-52228 | FOTMOB-3206721 (2019-11-09) | 同日同比分 |
| 379 | 2020-01-04 | 罗瑟汉姆 2:3 赫尔城 | EXCEL-53215 | FOTMOB-3231250 (2020-01-04) | 同日同比分 |
| 380 | 2020-01-08 | 贝西克塔斯 2:0 梅索科菲德 | FUTBOL24-8FE2CE42BB7CA9E0 | ESPN-587396 (2020-01-08) | 同日同比分 |
| 381 | 2020-01-09 | 法兰克福 1:2 柏林赫塔 | FUTBOL24-173CA8A16ECEAF78 | ESPN-563214 (2020-01-09) | 同日同比分 |
| 382 | 2020-01-11 | 多特蒙德 0:2 美因茨 | FUTBOL24-90ACB316C08E8A0B | ESPN-563226 (2020-01-12) | 相邻日同比分 |
| 383 | 2020-01-15 | 什鲁斯 1:0 布城 | EXCEL-53364 | FOTMOB-3248456 (2020-01-15) | 同日同比分 |
| 384 | 2020-07-17 | 巴黎圣曼 7:0 贝弗伦 | FUTBOL24-CE7DF442CB811C00 | ESPN-573687 (2020-07-18) | 相邻日同比分 |
| 385 | 2020-08-08 | SC维尔 0:3 埃因霍温 | FUTBOL24-5BB3712BE94EAEE6 | ESPN-575020 (2020-08-08) | 同日同比分 |
| 386 | 2020-08-12 | 阿尔塔奇 0:6 多特蒙德 | FUTBOL24-7BF668AD1B4D4F10 | ESPN-575930 (2020-08-12) | 同日同比分 |
| 387 | 2020-08-16 | 多特蒙德 11:2 维也纳 | FUTBOL24-F5375C0BC3D76B8B | ESPN-577325 (2020-08-16) | 同日同比分 |
| 388 | 2020-08-28 | 多特蒙德 1:1 帕德博恩 | FUTBOL24-ECFFF8DF0319BABC | ESPN-575931 (2020-08-28) | 同日同比分 |
| 389 | 2020-08-28 | 费耶诺德 0:1 汉堡 | FUTBOL24-560F2629B3CC6C96 | ESPN-578011 (2020-08-28) | 同日同比分 |
| 390 | 2020-08-28 | 沙尔克04 1:0 阿里斯 | FUTBOL24-D6F4676002F6CDEB | ESPN-581144 (2020-08-28) | 同日同比分 |
| 391 | 2020-08-29 | 柏林赫塔 0:4 埃因霍温 | FUTBOL24-86DB92834BCD3946 | ESPN-575940 (2020-08-29) | 同日同比分 |
| 392 | 2020-09-04 | 里泽 3:4 特拉布宗 | FUTBOL24-5EC4C2D290B142F8 | ESPN-581768 (2020-09-04) | 同日同比分 |
| 393 | 2020-09-05 | 米堡 4:3 什鲁斯 | EXCEL-55226 | FOTMOB-3410248 (2020-09-05) | 同日同比分 |
| 394 | 2020-09-12 | 威科姆 0:1 罗瑟汉姆 | EXCEL-55404 | FOTMOB-3414545 (2020-09-12) | 同日同比分 |
| 395 | 2020-09-12 | 巴萨 3:1 塔拉戈纳 | FUTBOL24-6802B767EA0B7854 | ESPN-583163 (2020-09-13) | 相邻日同比分 |
| 396 | 2020-09-19 | 罗瑟汉姆 0:1 米尔沃尔 | EXCEL-55658 | FOTMOB-3412975 (2020-09-19) | 同日同比分 |
| 397 | 2020-09-26 | 伯明翰 1:1 罗瑟汉姆 | EXCEL-55884 | FOTMOB-3412971 (2020-09-26) | 同日同比分 |
| 398 | 2020-10-17 | 罗瑟汉姆 1:2 诺维奇 | EXCEL-56276 | FOTMOB-3413012 (2020-10-17) | 同日同比分 |
| 399 | 2020-10-21 | 诺丁汉 1:1 罗瑟汉姆 | EXCEL-56399 | FOTMOB-3413007 (2020-10-21) | 同日同比分 |
| 400 | 2020-10-24 | 雷丁 3:0 罗瑟汉姆 | EXCEL-56491 | FOTMOB-3413028 (2020-10-24) | 同日同比分 |
| 401 | 2020-10-29 | 罗瑟汉姆 3:0 谢周三 | EXCEL-56623 | FOTMOB-3414042 (2020-10-29) | 同日同比分 |
| 402 | 2020-11-05 | 罗瑟汉姆 0:1 卢顿 | EXCEL-56814 | FOTMOB-3414054 (2020-11-05) | 同日同比分 |
| 403 | 2020-11-07 | 罗瑟汉姆 2:1 普雷斯顿 | EXCEL-56891 | FOTMOB-3414078 (2020-11-07) | 同日同比分 |
| 404 | 2020-11-14 | 什鲁斯 3:3 斯文登 | EXCEL-57072 | FOTMOB-3413095 (2020-11-14) | 同日同比分 |
| 405 | 2020-11-21 | 斯旺西 1:0 罗瑟汉姆 | EXCEL-57205 | FOTMOB-3414153 (2020-11-21) | 同日同比分 |
| 406 | 2020-11-25 | 女王巡游 3:2 罗瑟汉姆 | EXCEL-57308 | FOTMOB-3414095 (2020-11-25) | 同日同比分 |
| 407 | 2020-11-28 | 罗瑟汉姆 2:2 伯恩茅斯 | EXCEL-57405 | FOTMOB-3414100 (2020-11-28) | 同日同比分 |
| 408 | 2020-12-02 | 罗瑟汉姆 0:2 布伦特 | EXCEL-57503 | FOTMOB-3414109 (2020-12-02) | 同日同比分 |
| 409 | 2020-12-05 | 考文垂 3:1 罗瑟汉姆 | EXCEL-57594 | FOTMOB-3414116 (2020-12-05) | 同日同比分 |
| 410 | 2020-12-09 | 沃特福德 2:0 罗瑟汉姆 | EXCEL-57723 | FOTMOB-3414124 (2020-12-09) | 同日同比分 |
| 411 | 2020-12-12 | 罗瑟汉姆 2:0 布城 | EXCEL-57815 | FOTMOB-3414143 (2020-12-12) | 同日同比分 |
| 412 | 2020-12-17 | 布莱克本 2:1 罗瑟汉姆 | EXCEL-57972 | FOTMOB-3414172 (2020-12-17) | 同日同比分 |
| 413 | 2021-01-09 | 埃弗顿 1:1 罗瑟汉姆 | EXCEL-58404 | FOTMOB-3491314 (2021-01-09) | 同日同比分 |
| 414 | 2021-01-28 | 米堡 0:3 罗瑟汉姆 | EXCEL-58713 | FOTMOB-3414220 (2021-01-28) | 同日同比分 |
| 415 | 2021-01-30 | 罗瑟汉姆 1:3 斯旺西 | EXCEL-58765 | FOTMOB-3414397 (2021-01-30) | 同日同比分 |
| 416 | 2021-02-04 | 罗瑟汉姆 3:0 德比郡 | EXCEL-58838 | FOTMOB-3414195 (2021-02-04) | 同日同比分 |
| 417 | 2021-02-20 | 诺维奇 1:0 罗瑟汉姆 | EXCEL-58981 | FOTMOB-3414300 (2021-02-20) | 同日同比分 |
| 418 | 2021-02-24 | 罗瑟汉姆 0:1 诺丁汉 | EXCEL-59050 | FOTMOB-3414323 (2021-02-24) | 同日同比分 |
| 419 | 2021-03-17 | 罗瑟汉姆 1:4 沃特福德 | EXCEL-59482 | FOTMOB-3414412 (2021-03-17) | 同日同比分 |
| 420 | 2021-03-27 | 什鲁斯 1:2 朴次茅斯 | EXCEL-59635 | FOTMOB-3413395 (2021-03-27) | 同日同比分 |
| 421 | 2021-04-02 | 米尔沃尔 1:0 罗瑟汉姆 | EXCEL-59683 | FOTMOB-3414425 (2021-04-02) | 同日同比分 |
| 422 | 2021-04-05 | 罗瑟汉姆 0:3 威科姆 | EXCEL-59764 | FOTMOB-3414446 (2021-04-05) | 同日同比分 |
| 423 | 2021-04-10 | 哈德斯菲尔德 0:0 罗瑟汉姆 | EXCEL-59810 | FOTMOB-3414455 (2021-04-10) | 同日同比分 |
| 424 | 2021-04-14 | 罗瑟汉姆 3:1 女王巡游 | EXCEL-59874 | FOTMOB-3414295 (2021-04-14) | 同日同比分 |
| 425 | 2021-04-18 | 罗瑟汉姆 0:1 伯明翰 | EXCEL-59941 | FOTMOB-3414462 (2021-04-18) | 同日同比分 |
| 426 | 2021-04-22 | 罗瑟汉姆 1:2 米堡 | EXCEL-59998 | FOTMOB-3414485 (2021-04-22) | 同日同比分 |
| 427 | 2021-04-28 | 布伦特 1:0 罗瑟汉姆 | EXCEL-60118 | FOTMOB-3414348 (2021-04-28) | 同日同比分 |
| 428 | 2021-05-01 | 罗瑟汉姆 1:1 布莱克本 | EXCEL-60155 | FOTMOB-3414507 (2021-05-01) | 同日同比分 |
| 429 | 2021-05-05 | 卢顿 0:0 罗瑟汉姆 | EXCEL-60212 | FOTMOB-3414245 (2021-05-05) | 同日同比分 |
| 430 | 2021-05-08 | 加的夫城 1:1 罗瑟汉姆 | EXCEL-60246 | FOTMOB-3414538 (2021-05-08) | 同日同比分 |
| 431 | 2021-07-15 | 罗马 10:0 Montecatini | FUTBOL24-D531CAEAE41C4B1F | ESPN-613953 (2021-07-16) | 相邻日同比分 |
| 432 | 2021-07-17 | 科隆 3:2 拜仁 | FUTBOL24-A6C4EA92BB89D197 | ESPN-611766 (2021-07-17) | 同日同比分 |
| 433 | 2021-07-18 | 罗马 2:0 特尔纳纳 | FUTBOL24-ECC8F463613AEAEC | ESPN-613954 (2021-07-19) | 相邻日同比分 |
| 434 | 2021-07-21 | 巴萨 4:0 塔拉戈纳 | FUTBOL24-DD8C2F8D741455BA | ESPN-609921 (2021-07-22) | 相邻日同比分 |
| 435 | 2021-07-22 | 塞维利亚 1:0 拉帕马斯 | FUTBOL24-DD6DBF06061F177E | ESPN-615647 (2021-07-23) | 相邻日同比分 |
| 436 | 2021-07-23 | 利物浦 1:0 美因茨 | FUTBOL24-ABBE71AA5D6179AE | ESPN-613951 (2021-07-24) | 相邻日同比分 |
| 437 | 2021-07-25 | 罗马 5:2 德布勒森 | FUTBOL24-0D8EADC95C6EE5C6 | ESPN-613956 (2021-07-26) | 相邻日同比分 |
| 438 | 2021-07-29 | 柏林赫塔 4:3 利物浦 | FUTBOL24-0665430AA81129E3 | ESPN-613952 (2021-07-30) | 相邻日同比分 |
| 439 | 2021-07-31 | 费内巴切 3:2 菲尔特 | FUTBOL24-4B6165B8F30F02B2 | ESPN-615820 (2021-08-01) | 相邻日同比分 |
| 440 | 2022-07-14 | SC维尔 0:5 多特蒙德 | FUTBOL24-DFC69F505370B39E | ESPN-645233 (2022-07-15) | 相邻日同比分 |
| 441 | 2022-07-15 | 贝西克塔斯 1:0 美因茨 | FUTBOL24-AEB5B036C75831B4 | ESPN-644615 (2022-07-16) | 相邻日同比分 |
| 442 | 2022-07-16 | 科隆 1:2 AC米兰 | FUTBOL24-B5D4E766401C70A8 | ESPN-636006 (2022-07-17) | 相邻日同比分 |
| 443 | 2022-07-17 | 那不勒斯 4:1 佩鲁贾 | FUTBOL24-3533C1D8D31C5634 | ESPN-649987 (2022-07-18) | 相邻日同比分 |
| 444 | 2022-07-18 | 美因茨 1:0 纽卡斯尔 | FUTBOL24-2B44A5B40C08034A | ESPN-650008 (2022-07-18) | 同日同比分 |
| 445 | 2022-07-20 | 诺丁汉 3:1 柏林赫塔 | FUTBOL24-3946388ADEAEFF84 | ESPN-650125 (2022-07-21) | 相邻日同比分 |
| 446 | 2022-07-20 | 谢周三 0:2 巴列卡诺 | FUTBOL24-8717F9AB40BB1F93 | ESPN-650124 (2022-07-21) | 相邻日同比分 |
| 447 | 2022-07-23 | 尤文图斯 2:0 瓜达拉 | FUTBOL24-957EF0242DA007AE | ESPN-637069 (2022-07-23) | 同日同比分 |
| 448 | 2022-07-30 | 水晶宫 4:2 蒙彼利埃 | FUTBOL24-903F2261473A25AB | ESPN-650702 (2022-07-30) | 同日同比分 |
| 449 | 2022-07-30 | 罗瑟汉姆 1:1 斯旺西 | EXCEL-65322 | FOTMOB-3915312 (2022-07-30) | 同日同比分 |
| 450 | 2022-08-13 | 罗瑟汉姆 4:0 雷丁 | EXCEL-65583 | FOTMOB-3915284 (2022-08-13) | 同日同比分 |
| 451 | 2022-08-17 | 普雷斯顿 0:0 罗瑟汉姆 | EXCEL-65673 | FOTMOB-3915299 (2022-08-17) | 同日同比分 |
| 452 | 2022-08-20 | 女王巡游 1:1 罗瑟汉姆 | EXCEL-65747 | FOTMOB-3915277 (2022-08-20) | 同日同比分 |
| 453 | 2022-09-03 | 罗瑟汉姆 1:1 沃特福德 | EXCEL-65990 | FOTMOB-3915340 (2022-09-03) | 同日同比分 |
| 454 | 2022-10-29 | 加的夫城 1:0 罗瑟汉姆 | EXCEL-66517 | FOTMOB-3915501 (2022-10-29) | 同日同比分 |
| 455 | 2022-11-03 | 伯恩利 3:2 罗瑟汉姆 | EXCEL-66576 | FOTMOB-3915448 (2022-11-03) | 同日同比分 |
| 456 | 2022-11-05 | 罗瑟汉姆 1:2 诺维奇 | EXCEL-66642 | FOTMOB-3915458 (2022-11-05) | 同日同比分 |
| 457 | 2022-11-09 | 谢菲联 0:1 罗瑟汉姆 | EXCEL-66757 | FOTMOB-3915364 (2022-11-09) | 同日同比分 |
| 458 | 2022-11-12 | 卢顿 1:1 罗瑟汉姆 | EXCEL-66845 | FOTMOB-3915542 (2022-11-12) | 同日同比分 |
| 459 | 2022-11-19 | 牛津联 1:1 绿色森林 | EXCEL-66945 | FOTMOB-3916068 (2022-11-19) | 同日同比分 |
| 460 | 2022-11-19 | 谢周三 1:0 什鲁斯 | EXCEL-66950 | FOTMOB-3916071 (2022-11-19) | 同日同比分 |
| 461 | 2022-11-19 | 科隆 2:4 斯图加特 | FUTBOL24-634CA6826BA843DB | ESPN-656999 (2022-11-20) | 相邻日同比分 |
| 462 | 2022-12-11 | 贝西克塔斯 1:0 韦斯特洛 | FUTBOL24-F03AFB3CB6671F6B | ESPN-658037 (2022-12-12) | 相邻日同比分 |
| 463 | 2022-12-14 | 蓬费拉迪 2:4 马竞 | FUTBOL24-B7A9DF1690AADC2B | ESPN-659106 (2022-12-15) | 相邻日同比分 |
| 464 | 2022-12-26 | 罗瑟汉姆 2:2 斯托克城 | EXCEL-67085 | FOTMOB-3915553 (2022-12-26) | 同日同比分 |
| 465 | 2022-12-30 | 哈德斯菲尔德 2:0 罗瑟汉姆 | EXCEL-67126 | FOTMOB-3915584 (2022-12-30) | 同日同比分 |
| 466 | 2023-01-01 | 米尔沃尔 3:0 罗瑟汉姆 | EXCEL-67176 | FOTMOB-3915595 (2023-01-01) | 同日同比分 |
| 467 | 2023-01-07 | 什鲁斯 1:2 桑德兰 | EXCEL-67252 | FOTMOB-4072406 (2023-01-07) | 同日同比分 |
| 468 | 2023-01-07 | 伊普斯 4:1 罗瑟汉姆 | EXCEL-67253 | FOTMOB-4072410 (2023-01-07) | 同日同比分 |
| 469 | 2023-01-19 | 利雅得全明星 4:5 巴黎圣曼 | FUTBOL24-20147EE92CAC30AB | ESPN-662861 (2023-01-20) | 相邻日同比分 |
| 470 | 2023-02-15 | 雷丁 2:1 罗瑟汉姆 | EXCEL-67624 | FOTMOB-3915617 (2023-02-15) | 同日同比分 |
| 471 | 2023-02-22 | 罗瑟汉姆 2:1 桑德兰 | EXCEL-67739 | FOTMOB-3915599 (2023-02-22) | 同日同比分 |
| 472 | 2023-02-28 | 斯旺西 1:1 罗瑟汉姆 | EXCEL-67857 | FOTMOB-3915687 (2023-02-28) | 同日同比分 |
| 473 | 2023-03-04 | 罗瑟汉姆 3:1 女王巡游 | EXCEL-67913 | FOTMOB-3915672 (2023-03-04) | 同日同比分 |
| 474 | 2023-03-15 | 罗瑟汉姆 1:2 普雷斯顿 | EXCEL-68063 | FOTMOB-3915699 (2023-03-15) | 同日同比分 |
| 475 | 2023-04-07 | 罗瑟汉姆 3:1 西布罗姆 | EXCEL-68329 | FOTMOB-3915737 (2023-04-07) | 同日同比分 |
| 476 | 2023-04-19 | 罗瑟汉姆 2:2 伯恩利 | EXCEL-68476 | FOTMOB-3915782 (2023-04-19) | 同日同比分 |
| 477 | 2023-04-22 | 布城 2:1 罗瑟汉姆 | EXCEL-68516 | FOTMOB-3915775 (2023-04-22) | 同日同比分 |
| 478 | 2023-04-28 | 罗瑟汉姆 1:2 加的夫城 | EXCEL-68588 | FOTMOB-4150353 (2023-04-28) | 同日同比分 |
| 479 | 2023-07-12 | 费内巴切 1:3 贝红星 | FUTBOL24-07DDF1BCD2EBEA5F | ESPN-671682 (2023-07-13) | 相邻日同比分 |
| 480 | 2023-07-14 | 维也纳 1:1 加拉塔萨 | FUTBOL24-556C0D9481C8476E | ESPN-675058 (2023-07-15) | 相邻日同比分 |
| 481 | 2023-07-15 | 萨尔茨堡 4:1 汉堡 | FUTBOL24-4DCD44E60EDB2798 | ESPN-680954 (2023-07-15) | 同日同比分 |
| 482 | 2023-07-18 | 贝西克塔斯 4:0 梅索科菲德 | FUTBOL24-01DBAF187ABFA5EB | ESPN-680955 (2023-07-19) | 相邻日同比分 |
| 483 | 2023-07-18 | 阿尔塔奇 0:3 比利亚雷 | FUTBOL24-FDF180373EC1E923 | ESPN-681263 (2023-07-19) | 相邻日同比分 |
| 484 | 2023-07-21 | 勒沃库森 1:2 帕德博恩 | FUTBOL24-7D48178A839EDECB | ESPN-680377 (2023-07-21) | 同日同比分 |
| 485 | 2023-07-24 | 菲尔特 4:4 利物浦 | FUTBOL24-2580A479A95F4C60 | ESPN-670149 (2023-07-24) | 同日同比分 |
| 486 | 2023-08-05 | 美因茨 1:0 伯恩利 | FUTBOL24-1D898DC4B67FEA86 | ESPN-685322 (2023-08-05) | 同日同比分 |
| 487 | 2023-08-07 | 利物浦 3:1 达姆施塔特 | FUTBOL24-D2CBF50A2EE814A8 | ESPN-682689 (2023-08-08) | 相邻日同比分 |
| 488 | 2023-08-13 | 国际米兰 4:2 埃格纳蒂亚 | FUTBOL24-1BF874888B5DBA24 | ESPN-685329 (2023-08-14) | 相邻日同比分 |
| 489 | 2023-08-26 | 罗瑟汉姆 1:2 莱切斯特 | EXCEL-69682 | FOTMOB-4202262 (2023-08-26) | 同日同比分 |
| 490 | 2023-10-29 | 谢周三 2:0 罗瑟汉姆 | EXCEL-70253 | FOTMOB-4202355 (2023-10-29) | 同日同比分 |
| 491 | 2023-12-23 | 莱切斯特 3:0 罗瑟汉姆 | EXCEL-70784 | FOTMOB-4202505 (2023-12-23) | 同日同比分 |
| 492 | 2023-12-26 | 罗瑟汉姆 1:0 米堡 | EXCEL-70805 | FOTMOB-4202491 (2023-12-26) | 同日同比分 |
| 493 | 2023-12-30 | 罗瑟汉姆 1:1 桑德兰 | EXCEL-70823 | FOTMOB-4202525 (2023-12-30) | 同日同比分 |
| 494 | 2024-01-06 | 费耶诺德 1:2 美因茨 | FUTBOL24-0911347A9BE060CF | ESPN-694559 (2024-01-06) | 同日同比分 |
| 495 | 2024-01-06 | 富勒姆 1:0 罗瑟汉姆 | EXCEL-70880 | FOTMOB-4374878 (2024-01-06) | 同日同比分 |
| 496 | 2024-01-07 | 汉堡 2:2 埃因霍温 | FUTBOL24-9F03F9287ADE2183 | ESPN-693920 (2024-01-07) | 同日同比分 |
| 497 | 2024-01-20 | 米堡 1:1 罗瑟汉姆 | EXCEL-71047 | FOTMOB-4202552 (2024-01-20) | 同日同比分 |
| 498 | 2024-01-24 | 利沙巴布 1:2 罗马 | FUTBOL24-ED4CEEA1FB56FEC5 | ESPN-695555 (2024-01-25) | 相邻日同比分 |
| 499 | 2024-01-29 | 利雅新月 4:3 迈国际 | FUTBOL24-598A211F4095FAF8 | ESPN-691204 (2024-01-30) | 相邻日同比分 |
| 500 | 2024-02-21 | 伊普斯 4:3 罗瑟汉姆 | EXCEL-71340 | FOTMOB-4202538 (2024-02-21) | 同日同比分 |
| 501 | 2024-07-10 | Szeged-Csanád GA 1:2 特拉布宗 | FUTBOL24-C8180FDA9C88DFC1 | ESPN-711284 (2024-07-11) | 相邻日同比分 |
| 502 | 2024-07-13 | 河床 3:1 亚松森奥林匹亚 | FUTBOL24-1494D2BCED2AF550 | ESPN-707341 (2024-07-14) | 相邻日同比分 |
| 503 | 2024-07-20 | 萨尔茨堡 4:0 谢周三 | FUTBOL24-42B1DFF0D8D79D27 | ESPN-712950 (2024-07-20) | 同日同比分 |
| 504 | 2024-07-20 | 安特卫普 1:2 帕尔马 | FUTBOL24-C5CFC4EC269737AB | ESPN-715539 (2024-07-21) | 相邻日同比分 |
| 505 | 2024-07-23 | 什鲁斯 1:2 莱切斯特 | FUTBOL24-E10FFDBD860663FB | ESPN-707621 (2024-07-24) | 相邻日同比分 |
| 506 | 2024-07-24 | 加拉塔萨 2:1 莱切 | FUTBOL24-DBD1FF39D16DA888 | ESPN-711234 (2024-07-25) | 相邻日同比分 |
| 507 | 2024-07-26 | 埃森 1:2 勒沃库森 | FUTBOL24-A744B0606D2CE46D | ESPN-715058 (2024-07-27) | 相邻日同比分 |
| 508 | 2024-07-27 | 国际米兰 3:0 拉帕马斯 | FUTBOL24-C677FA3475DFFFA8 | ESPN-712568 (2024-07-28) | 相邻日同比分 |
| 509 | 2024-08-11 | 利物浦 0:0 拉帕马斯 | FUTBOL24-2C1859B95DEB4FE9 | ESPN-718515 (2024-08-12) | 相邻日同比分 |
| 510 | 2025-03-19 | 罗瑟汉姆 2:3 威科姆 | EXCEL-75871 | FOTMOB-4520099 (2025-03-19) | 同日同比分 |
| 511 | 2025-10-11 | 北安普敦 1:2 罗瑟汉姆 | EXCEL-78370 | FOTMOB-4825716 (2025-10-11) | 同日同比分 |
| 512 | 2025-11-15 | 卢顿 0:0 罗瑟汉姆 | EXCEL-79078 | FOTMOB-4825762 (2025-11-15) | 同日同比分 |
| 513 | 2025-12-26 | 博尔顿 2:1 罗瑟汉姆 | EXCEL-79885 | FOTMOB-4825831 (2025-12-26) | 同日同比分 |
| 514 | 2025-12-30 | 博尔顿 0:1 曼斯菲尔德 | EXCEL-79951 | FOTMOB-4825843 (2025-12-30) | 同日同比分 |
| 515 | 2026-01-05 | 科隆 2:1 卢加诺 | FOTMOB-5115519 | FOOTMERCATO-4091178744424312551 (2026-01-05) | 同日同比分 |
| 516 | 2026-01-10 | 亨克 1:0 普斯卡什学院 | FOTMOB-5123296 | FUTBOL24-75B728CE49CE8F00 (2026-01-09)<br>FOOTMERCATO-3637440408854178248 (2026-01-09) | 相邻日同比分 |
| 517 | 2026-01-10 | 杰尔 2:2 贝游击 | FUTBOL24-0DBD2F114DFF29A0 | FOOTMERCATO-2641264252777556065 (2026-01-10) | 同日同比分 |
| 518 | 2026-01-10 | 草蜢 1:7 纽伦堡 | FOTMOB-5107384 | FUTBOL24-419F9AB1347B4531 (2026-01-10) | 同日同比分 |
| 519 | 2026-01-15 | 萨尔茨堡 2:0 贝红星 | FOTMOB-5115621 | ESPN-401845336 (2026-01-15) | 同日同比分 |
| 520 | 2026-01-15 | 圣洛伦索 0:1 库库塔体育 | FUTBOL24-03014EBB71232E40 | FOOTMERCATO-5582214894575324327 (2026-01-15) | 同日同比分 |
| 521 | 2026-01-16 | 中日德兰 3:0 克里夫巴斯 | FOTMOB-5119555 | FOOTMERCATO-8279402134613529588 (2026-01-15) | 相邻日同比分 |
| 522 | 2026-01-15 | 索菲亚1948 1:2 LASK林茨 | FOTMOB-5107386 | FUTBOL24-36AE73709C65C13A (2026-01-15) | 同日同比分 |
| 523 | 2026-01-17 | 库库塔体育 1:1 飓风 | FUTBOL24-356987B1C7FA3C5D | FOOTMERCATO-111179782490233302 (2026-01-16) | 相邻日同比分 |
| 524 | 2026-01-17 | 都柏林 1:5 邓多克 | FUTBOL24-3D7C01CEA23AA3E1 | FOOTMERCATO-1533949345301838225 (2026-01-16) | 相邻日同比分 |
| 525 | 2026-01-19 | 博卡 2:1 亚松森奥林匹亚 | FUTBOL24-73D182F45EE294E1 | ESPN-401850358 (2026-01-19) | 同日同比分 |
| 526 | 2026-01-19 | 奥胡斯 4:2 克里夫巴斯 | FOTMOB-5119551 | FOOTMERCATO-2071193140237616594 (2026-01-19) | 同日同比分 |
| 527 | 2026-01-19 | 比亚韦 1:1 索菲亚1948 | FUTBOL24-FD967F9A515A99CF | FOOTMERCATO-2142303485592475329 (2026-01-19) | 同日同比分 |
| 528 | 2026-01-20 | 波尔蒂芒 1:1 拉多米亚克 | FUTBOL24-9F687CB07929D052 | FOOTMERCATO-7125724063402551471 (2026-01-19) | 相邻日同比分 |
| 529 | 2026-01-19 | 马里博尔 2:3 德布勒森 | FUTBOL24-53B33C289019BC21 | FOOTMERCATO-7299978534705558350 (2026-01-19) | 同日同比分 |
| 530 | 2026-01-19 | 法伦斯 0:5 拉多米亚克 | FUTBOL24-CBDD03948FD43A22 | FOOTMERCATO-7909353830382216492 (2026-01-19) | 同日同比分 |
| 531 | 2026-01-23 | 兰纳斯 1:2 皇家盐湖城 | FOTMOB-5120700 | FOOTMERCATO-8547827050003216054 (2026-01-22) | 相邻日同比分 |
| 532 | 2026-01-24 | 哥德堡 3:1 厄斯特什 | FOTMOB-5138359 | FOOTMERCATO-8835662279427129085 (2026-01-23) | 相邻日同比分 |
| 533 | 2026-01-31 | 桑内斯 0:2 维京 | FOTMOB-5160575 | FOOTMERCATO-1160561582846467682 (2026-01-30) | 相邻日同比分 |
| 534 | 2026-01-31 | 布雷 0:3 谢尔本 | FUTBOL24-9E8A458CA6D1A3F7 | FOOTMERCATO-1906271035182011722 (2026-01-30) | 相邻日同比分 |
| 535 | 2026-02-01 | 索菲亚1948 3:0 江原FC | FUTBOL24-45ED339CA3CD4CE4 | FOOTMERCATO-7069173355828562243 (2026-02-01) | 同日同比分 |
| 536 | 2026-02-03 | 莫尔德 4:0 SK Træff | FUTBOL24-17566F9FAA06FDD4 | FOOTMERCATO-3113933193933670226 (2026-02-02) | 相邻日同比分 |
| 537 | 2026-02-04 | 莫陆军 2:2 克拉斯诺 | FOTMOB-5165813 | FOOTMERCATO-4900069620584456365 (2026-02-03) | 相邻日同比分 |
| 538 | 2026-02-07 | 克拉斯诺 3:0 泽尼特 | FOTMOB-5165810 | FOOTMERCATO-2741659809419284201 (2026-02-06) | 相邻日同比分 |
| 539 | 2026-02-14 | 米亚尔比 3:2 厄斯特什 | FOTMOB-5182345 | FOOTMERCATO-1826976075174945187 (2026-02-14) | 同日同比分 |
| 540 | 2026-02-18 | 莫陆军 3:1 罗斯托夫 | FOTMOB-5143678 | FOOTMERCATO-7351191070765803299 (2026-02-18) | 同日同比分 |
| 541 | 2026-02-26 | 拉赫蒂 2:2 Grankulla IFK | FUTBOL24-06212140007E245E | FOOTMERCATO-4689201915116724797 (2026-02-25) | 相邻日同比分 |
| 542 | 2026-03-05 | 乌锡坦斯 0:6 哥德堡 | FOTMOB-5214650 | FOOTMERCATO-6319648642237548419 (2026-03-04) | 相邻日同比分 |
| 543 | 2026-03-11 | 盖斯 1:2 瓦尔贝里 | FOTMOB-5137635 | FOOTMERCATO-3962589721037483651 (2026-03-10) | 相邻日同比分 |
| 544 | 2026-03-19 | 盖斯 2:1 奥迪沃特 | FOTMOB-5137636 | FOOTMERCATO-2018616026945404342 (2026-03-18) | 相邻日同比分 |
| 545 | 2026-03-23 | 哥德堡 2:1 奥迪沃特 | FOTMOB-5266717 | FOOTMERCATO-7565152443817452523 (2026-03-23) | 同日同比分 |
| 546 | 2026-03-26 | 埃夫斯堡 3:1 厄斯特什 | FUTBOL24-7F60680D74084BC4 | FOOTMERCATO-6459748358589251901 (2026-03-26) | 同日同比分 |
| 547 | 2026-06-25 | 杰尔 2:1 伏伊伏丁 | FUTBOL24-2CE242D62E4D7ECD | FOOTMERCATO-283038513849681629 (2026-06-24) | 相邻日同比分 |
| 548 | 2026-06-25 | 格伦托兰 1:1 新圣徒 | FUTBOL24-79CF6BD244997490 | FOOTMERCATO-7369615683962746770 (2026-06-24) | 相邻日同比分 |
| 549 | 2026-06-28 | 采列 1:0 斯肯迪亚 | FUTBOL24-6F190131CB1AD4A2 | FOOTMERCATO-4683372954973941251 (2026-06-27) | 相邻日同比分 |
| 550 | 2026-06-30 | 索菲亚1948 3:0 贝游击 | FUTBOL24-72D04EF16541D461 | FOOTMERCATO-4726207553043751904 (2026-06-29) | 相邻日同比分 |
| 551 | 2026-06-30 | H&W Welders 1:2 拉恩 | FUTBOL24-7501E4B4BD5D97D0 | FOOTMERCATO-7126514711216342135 (2026-06-29) | 相邻日同比分 |
| 552 | 2026-07-01 | Brinje Grosuplje 0:3 里耶卡 | FUTBOL24-1D2E8CC703C8C40A | FOOTMERCATO-2580131103675369648 (2026-06-30) | 相邻日同比分 |
| 553 | 2026-07-01 | 沙姆洛克 1:0 希伯尼安 | FOTMOB-5823109 | FOOTMERCATO-6067784325631270268 (2026-06-30) | 相邻日同比分 |
| 554 | 2026-07-01 | 福弗尔竞技 0:4 圣约翰 | FOTMOB-5860645 | FOOTMERCATO-7426634738830685276 (2026-06-30) | 相邻日同比分 |
| 555 | 2026-07-01 | 斯肯迪亚 2:2 卢甘斯克 | FUTBOL24-D1CD90EF0AB693D8 | FOOTMERCATO-8915799358562829431 (2026-06-30) | 相邻日同比分 |
| 556 | 2026-07-02 | 马里博尔 3:3 索菲亚1948 | FUTBOL24-6FBF061F638BA404 | FOOTMERCATO-1339038829544678404 (2026-07-01) | 相邻日同比分 |
| 557 | 2026-07-02 | 采列 0:1 斯海杜克 | FUTBOL24-B5493ABF2D0846B3 | FOOTMERCATO-6514063108953382617 (2026-07-01) | 相邻日同比分 |
| 558 | 2026-07-01 | 格伦托兰 0:4 基马诺克 | FOTMOB-5869710 | FUTBOL24-570C6E34320B5CB3 (2026-07-01) | 同日同比分 |
| 559 | 2026-07-02 | 邓迪FC 2:1 保克什 | FOTMOB-5872437 | FOOTMERCATO-121361534197843609 (2026-07-02) | 同日同比分 |
| 560 | 2026-07-04 | 梅斯 5:2 US Mondorf les Bains | FUTBOL24-C3C9C5061A8CA513 | FOOTMERCATO-7309454389057499403 (2026-07-03) | 相邻日同比分 |
| 561 | 2026-07-04 | 维也纳 1:1 德布勒森 | FOTMOB-5766933 | FUTBOL24-873299DAE412C969 (2026-07-04) | 同日同比分 |
| 562 | 2026-07-04 | 哈利法克 1:3 谢菲联 | FOTMOB-5880240 | FUTBOL24-D180FFEBD984A58D (2026-07-04) | 同日同比分 |
| 563 | 2026-07-09 | 弗拉门戈 2:0 洛桑 | FOTMOB-5901301 | FUTBOL24-BD921AC10DE8B699 (2026-07-09) | 同日同比分 |
| 564 | 2026-07-11 | 萨格勒布火车头 2:2 贝游击 | FUTBOL24-A02E8EC884196F32 | FOOTMERCATO-3193243135490954012 (2026-07-10) | 相邻日同比分 |
| 565 | 2026-07-11 | 马里博尔 1:1 查尔顿 | FOTMOB-5904573 | FOOTMERCATO-6223899820681377637 (2026-07-10) | 相邻日同比分 |
| 566 | 2026-07-12 | FC Fleury 91 0:5 南特 | FUTBOL24-1D7A93F5BBE60941 | FOOTMERCATO-2153208722003040492 (2026-07-11) | 相邻日同比分 |
| 567 | 2026-07-12 | 帕纳辛纳 3:0 草蜢 | FOTMOB-5847092 | FUTBOL24-212CDFE1D9E2D10F (2026-07-12)<br>FOOTMERCATO-585632813680751454 (2026-07-11) | 相邻日同比分 |
| 568 | 2026-07-13 | 哥伦布机员 1:1 伯恩利 | FOTMOB-5915947 | FOOTMERCATO-7921680934037218621 (2026-07-12) | 相邻日同比分 |
| 569 | 2026-07-15 | 兰纳斯 3:1 诺茨郡 | FOTMOB-5748089 | FOOTMERCATO-6702520266517056365 (2026-07-14) | 相邻日同比分 |
| 570 | 2026-07-15 | 拉马甘夏普尔 0:4 保克什 | FUTBOL24-167BB90D18A23688 | FOOTMERCATO-8037854443371724927 (2026-07-14) | 相邻日同比分 |
| 571 | 2026-07-16 | 科尔多瓦 1:1 奥兰多海盗 | FUTBOL24-F236DD5FD1E0280E | FOOTMERCATO-5555474696815246099 (2026-07-15) | 相邻日同比分 |
| 572 | 2026-07-16 | TSV 1861 Nördlingen 0:9 海登海姆 | FUTBOL24-F427FEC88A01CAE4 | FOOTMERCATO-626170263727542366 (2026-07-15) | 相邻日同比分 |
| 573 | 2026-07-17 | 利阿波罗 0:0 拉纳卡 | FUTBOL24-AFA94B3F23CC6CE3 | FOOTMERCATO-4367879561717798419 (2026-07-16) | 相邻日同比分 |
| 574 | 2026-07-16 | 皇家盐湖城 4:1 伯恩利 | FOTMOB-5915948 | FOOTMERCATO-7077312114259676966 (2026-07-16) | 同日同比分 |
| 575 | 2026-07-18 | 维也纳 4:1 保克什 | FOTMOB-5943036 | FUTBOL24-38E9C32EC2F421A4 (2026-07-18)<br>FOOTMERCATO-9198592711473818860 (2026-07-17) | 相邻日同比分 |
| 576 | 2026-07-19 | 奥西耶克 2:1 布达佩斯捍卫者 | FUTBOL24-8258216C12AABDD0 | FOOTMERCATO-6921210628147518222 (2026-07-18) | 相邻日同比分 |
| 577 | 2026-07-22 | Hørsholm-Usserød 2:2 北西兰 | FOTMOB-5904512 | FUTBOL24-F21BEB9E61EEF17D (2026-07-22)<br>FOOTMERCATO-8131434302939075219 (2026-07-21) | 相邻日同比分 |
| 578 | 2026-07-21 | 谢菲联 3:1 哈德斯菲尔德 | FOTMOB-5956424 | FUTBOL24-652B0619AC73EA29 (2026-07-21) | 同日同比分 |
| 579 | 2026-07-23 | 奥西耶克 3:0 希巴利亚 | FUTBOL24-03F8EFFA248EBB96 | FOOTMERCATO-2151624015079664293 (2026-07-22) | 相邻日同比分 |
| 580 | 2026-07-23 | 布雷斯特 0:2 威柯拉 | FOTMOB-5900183 | FUTBOL24-6C8A1939C71775DD (2026-07-23)<br>FOOTMERCATO-4219054278128280387 (2026-07-22) | 相邻日同比分 |
| 581 | 2026-07-24 | 科尔多瓦 0:0 塞维利亚 | FOTMOB-5793435 | FOOTMERCATO-6251022688481608940 (2026-07-23) | 相邻日同比分 |
| 582 | 2026-07-25 | 克雷莫纳 4:0 吉安纳 | FUTBOL24-6EC6DE0CEB3F4FF6 | FOOTMERCATO-243914298615016193 (2026-07-24) | 相邻日同比分 |
| 583 | 2026-07-25 | 不伦瑞克 0:1 南安普敦 | FOTMOB-5950687 | FUTBOL24-508C1461C9105681 (2026-07-25)<br>FOOTMERCATO-6070662401871731742 (2026-07-24) | 相邻日同比分 |
| 584 | 2026-07-25 | Limavady United 0:1 林菲尔德 | FUTBOL24-55056B629494E4ED | FOOTMERCATO-7888488579050705906 (2026-07-24) | 相邻日同比分 |
| 585 | 2026-07-26 | 埃瓦尔 2:2 毕尔巴鄂 | FOTMOB-5900301 | FOOTMERCATO-5581192688261619355 (2026-07-25) | 相邻日同比分 |
| 586 | 2026-07-26 | 加的斯 0:1 科尔多瓦 | FUTBOL24-F8A18175D334A3B1 | FOOTMERCATO-5791053078504230610 (2026-07-25) | 相邻日同比分 |
| 587 | 2026-07-25 | 罗瑟汉姆 0:2 谢菲联 | FOTMOB-5960994 | FUTBOL24-100AE378F11693BE (2026-07-25) | 同日同比分 |
| 588 | 2026-07-25 | 米堡 1:3 哈德斯菲尔德 | FOTMOB-5963804 | FUTBOL24-2C81E41B9C87F9B1 (2026-07-25) | 同日同比分 |
| 589 | 2026-07-25 | 绿色森林 1:5 加的夫城 | FOTMOB-5967544 | FUTBOL24-C26C4C6232EC0848 (2026-07-25) | 同日同比分 |
| 590 | 2026-07-29 | 圣埃蒂安 4:0 洛桑 | FOTMOB-5978751 | FUTBOL24-7C1C7ACC52B503A4 (2026-07-30) | 相邻日同比分 |

## 验证

- 全量 226 项测试通过，0 失败、0 错误、0 跳过；前端构建通过
- 使用实际 Java 翻译器检查 171 行新增映射、3,757 次赛事范围组合，全部符合预期
- 按生产映射复查 238,761 条历史记录，同日重复 0，相邻日同比分重复 0，本次新增自身对阵 0
- 已核对每条保留记录原字段均未改写，删除记录均能从本报告 JSON 恢复
- 默认 JAR 被运行中的 Java 进程占用，首次打包在重命名步骤失败；临时仅调整产物名称后打包成功，再将可执行验证包同步到默认 JAR 路径，两个文件 SHA-256 一致
- 产物 `target/lottery-football-1.0.0.jar` 与 `target/lottery-football-1.0.0-verified-aliases.jar` 已验证 Spring Boot 入口及内置 CSV 与源文件一致
- 没有重启应用进程；运行中的应用需要重启后才能加载新映射
- JAR SHA-256：`a60ed53dd06b6725d9d9fdb017d1f855c45cb6708a89a2760752b9b9871f84c1`
