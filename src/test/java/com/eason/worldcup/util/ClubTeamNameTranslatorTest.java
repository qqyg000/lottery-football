package com.eason.worldcup.util;

import com.eason.worldcup.model.Competition;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ClubTeamNameTranslatorTest {

    @Test
    void shouldTranslateUserRequestedClubAliases() throws IOException {
        assumeMappingsImported();

        Map<String, String> aliases = Map.ofEntries(
                Map.entry("雷克雅未克维京人", "雷克维京"),
                Map.entry("斯普利特海杜克", "斯海杜克"),
                Map.entry("Slovan", "布拉迪斯"),
                Map.entry("FC Arges Pitesti", "Arges"),
                Map.entry("IBV Vestmannaeyjar", "韦斯特曼"),
                Map.entry("IA Akranes", "IA"),
                Map.entry("Thor Akureyri", "Thor"),
                Map.entry("KA Akureyri", "KA"),
                Map.entry("SV Elversberg", "埃沃斯堡"),
                Map.entry("Waldhof Mannheim", "曼海姆"),
                Map.entry("Neuchatel Xamax", "Xamax"),
                Map.entry("RAAL La Louviere", "La Louviere"),
                Map.entry("沃尔夫斯堡", "沃夫斯堡"),
                Map.entry("斯特拉斯堡", "斯特拉斯"));

        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            assertEquals(alias.getValue(), ClubTeamNameTranslator.translate(alias.getKey()));
        }
    }

    @Test
    void shouldTranslateSupplementalClubCupAliases() throws IOException {
        assumeMappingsImported();

        assertEquals("巴伊亚", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Bahia/BA"));
        assertEquals("Cienciano", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Cienciano del Cusco"));
        assertEquals("Polissya", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Polissya Zhytomyr"));
        assertEquals("Turan", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Turan"));
    }

    @Test
    void shouldUseSportteryStandardNamesForCurrentAliases() throws IOException {
        assumeMappingsImported();

        assertEquals("库奥皮奥", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "KuPS Kuopio"));
        assertEquals("奥胡斯", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "AGF"));
        assertEquals("波兹南", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "Lech Poznan"));
        assertEquals("格风暴", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "SK Sturm Graz"));
        assertEquals("哈茨", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "Heart of Midlothian"));
        assertEquals("库奥皮奥", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "KuPS"));
        assertEquals("齐拉", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Zira FK"));
        assertEquals("塞伊奈", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "SJK"));
        assertEquals("坦山猫", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Ilves Tampere"));
        assertEquals("赫尔火花", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "IF Gnistan"));
        assertEquals("贝西克塔", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Besiktas JK"));
        assertEquals("北安普敦", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Northampton"));
        assertEquals("费内巴切", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Fenerbahçe SK"));
        assertEquals("沙勒罗瓦", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Sporting Charleroi"));
        assertEquals("加拉塔萨", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Galatasaray SK"));
        assertEquals("奥林匹亚", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Olympiakos Pireus"));
        assertEquals("默德林", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "FC Admira Wacker Modling"));
        assertEquals("伏伊伏丁", ClubTeamNameTranslator.translate(
                Competition.EUROPA_LEAGUE,
                "Vojvodina"));
        assertEquals("斯海杜克", ClubTeamNameTranslator.translate(
                Competition.EUROPA_LEAGUE,
                "斯海杜克"));
        assertEquals("索菲亚中央陆军", ClubTeamNameTranslator.translate(
                Competition.EUROPA_LEAGUE,
                "CSKA Sofia"));
        assertEquals("克拉克斯维克", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "KI Klaksvik"));
        assertEquals("雷克维京", ClubTeamNameTranslator.translate(
                Competition.CHAMPIONS_LEAGUE,
                "Vikingur Reykjavik"));
        assertEquals("沙特阿拉伯", ClubTeamNameTranslator.translate(
                Competition.WORLD_CUP,
                "Saudi Arabia"));
        assertEquals("科罗纳", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Korona Kielce"));
        assertEquals("保克什", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Paksi FC"));
        assertEquals("北西兰", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "FC Nordsjælland"));
        assertEquals("莫迪纳摩", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Dynamo Moscow"));
        assertEquals("布迪纳摩", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Dinamo Bucuresti"));
        assertEquals("拉赫蒂", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "FC Lahti"));
        assertEquals("大邱FC", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Daegu FC"));
        assertEquals("济州SK", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "济州联"));
        assertEquals("水原三星", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Suwon Samsung Bluewings"));
        assertEquals("金泉尚武", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Gimcheon Sangmu"));
        assertEquals("大田市民", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Daejeon Hana Citizen"));
        assertEquals("金浦FC", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Gimpo FC"));
        assertEquals("首尔衣恋", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Seoul E-Land FC"));
        assertEquals("忠南牙山", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Chungnam Asan FC"));
        assertEquals("奥胡斯", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "AGF Aarhus"));
        assertEquals("琴斯托霍", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Raków"));
        assertEquals("比亚韦", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Jagiellonia"));
        assertEquals("LASK林茨", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "LASK Linz"));
        assertEquals("南部女王", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Queen of the South"));
        assertEquals("伊斯坦布", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Istanbul Basaksehir"));
        assertEquals("费伦茨", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Ferencvaros TC"));
        assertEquals("里耶卡", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "HNK Rijeka"));
        assertEquals("兹林", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "FK Zlin"));
        assertEquals("瓦尔达尔", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Vardar"));
        assertEquals("高利宁", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Coleraine"));
        assertEquals("利瓦迪亚", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "FC Levadia Tallinn"));
        assertEquals("利瓦迪亚", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "FCI Levadia"));
        assertEquals("汉坎", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Hamarkameratene"));
        assertEquals("汉坎", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "HamKam"));
        assertEquals("越南", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Vietnam"));
        assertEquals("始兴市民", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Siheung FC"));
        assertEquals("始兴市民", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Siheung Citizen"));
        assertEquals("龙仁FC", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Yongin FC"));
        assertEquals("特罗姆瑟", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Tromsø"));
        assertEquals("江原FC", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Gangwon FC"));
        assertEquals("萨拉热窝", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "FK Sarajevo"));
        assertEquals("索尔纳", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "AIK Fotboll"));
        assertEquals("纳夫兹", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "FK Neftchi"));
        assertEquals("纳夫兹", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Neftchi Baku"));
        assertEquals("纳夫兹", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Neftçi"));
        assertEquals("Neftchi Fergana", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Neftchi Fergana"));
        assertEquals("卡尔斯多夫", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "Kalsdorf"));
        assertEquals("卡尔斯多夫", ClubTeamNameTranslator.translate(
                Competition.CLUB_FRIENDLY,
                "SC Kalsdorf"));
        assertEquals("西基臣", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Seekirchen"));
        assertEquals("阿尔塔奇", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "Altach"));
        assertEquals("奥地利克拉根福", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "SK Austria Klagenfurt"));
        assertEquals("BW林茨", ClubTeamNameTranslator.translate(
                Competition.CLUB_OFFICIAL_OTHER,
                "BW Linz"));
    }

    @Test
    void shouldUseRequestedManualCardTeamNames() throws IOException {
        assumeMappingsImported();

        Map<String, String> expectedMappings = Map.ofEntries(
                Map.entry("赫尔辛基火花", "赫尔火花"),
                Map.entry("TPS图尔库", "TPS图尔"),
                Map.entry("格拉斯哥流浪者", "流浪者"),
                Map.entry("West Ham", "西汉姆联"),
                Map.entry("塞萨洛尼基", "塞萨洛"),
                Map.entry("AEK拉纳卡", "拉纳卡"),
                Map.entry("阿尔克马尔", "阿尔克马"),
                Map.entry("SC N.E.C.", "奈梅亨"),
                Map.entry("Boulogne", "布洛涅"),
                Map.entry("US Boulogne", "布洛涅"),
                Map.entry("比利亚雷亚尔", "比利亚雷"),
                Map.entry("埃斯托里尔", "埃斯托里"),
                Map.entry("拉茨流浪", "拉茨"),
                Map.entry("Vallecano", "巴列卡诺"));

        expectedMappings.forEach((source, expected) ->
                assertEquals(expected, ClubTeamNameTranslator.translate(
                        Competition.CLUB_FRIENDLY,
                        source), source));
    }

    @Test
    void shouldApplyManualCanonicalNameOverrides() throws IOException {
        assumeMappingsImported();

        assertEquals("加扎拜尔", ClubTeamNameTranslator.translate("Stjarnan"));
        assertEquals("迪弗当日", ClubTeamNameTranslator.translate("Differdange"));
        assertEquals("哥德堡", ClubTeamNameTranslator.translate("IFK哥德堡"));
        assertEquals("布鲁马波", ClubTeamNameTranslator.translate("布鲁马波卡纳"));
        assertEquals("国际图尔", ClubTeamNameTranslator.translate("国际图尔库"));
        assertEquals("坦山猫", ClubTeamNameTranslator.translate("坦佩雷山猫"));
        assertEquals("索尔纳", ClubTeamNameTranslator.translate("AIK索尔纳"));
        assertEquals("厄格里特", ClubTeamNameTranslator.translate("厄尔格里特"));
        assertEquals("塞伊奈", ClubTeamNameTranslator.translate("塞伊奈约基"));
        assertEquals("韦斯特罗", ClubTeamNameTranslator.translate("韦斯特罗斯"));
        assertEquals("哈尔姆斯", ClubTeamNameTranslator.translate("哈尔姆斯塔德"));
        assertEquals("佐加顿斯", ClubTeamNameTranslator.translate("Djurgårdens IF"));
        assertEquals("韦斯特罗", ClubTeamNameTranslator.translate("Västerås"));
    }

    @Test
    void shouldApplyRequestedDutchPortugueseAndItalianTeamAliases() throws IOException {
        assumeMappingsImported();

        Map<String, String> expectedMappings = Map.ofEntries(
                Map.entry("NFC Volos", "Volos NFC"),
                Map.entry("Volos", "Volos NFC"),
                Map.entry("Amarante FC", "阿马兰蒂"),
                Map.entry("Sporting CP B", "Sporting CP II"),
                Map.entry("SBV Excelsior", "SBV精英"),
                Map.entry("NAC", "布雷达"),
                Map.entry("鹿特丹斯巴达", "鹿斯巴达"),
                Map.entry("Atlético CP", "葡竞技"),
                Map.entry("Como 1907", "科莫"),
                Map.entry("Lusitânia Lourosa", "鲁斯塔尼亚"),
                Map.entry("GD Estoril", "埃斯托里"),
                Map.entry("CF Belenenses", "CF Os Belenenses"));

        expectedMappings.forEach((source, expected) ->
                assertEquals(expected, ClubTeamNameTranslator.translate(
                        Competition.CLUB_FRIENDLY,
                        source), source));
    }

    @Test
    void shouldApplyRequestedCardAndSupplementalTeamAliases() throws IOException {
        assumeMappingsImported();

        Map<String, String> expectedMappings = Map.ofEntries(
                Map.entry("FC Den Bosch", "登博思"),
                Map.entry("Heracles Almelo", "赫拉克勒"),
                Map.entry("União Torreense", "托林斯"),
                Map.entry("Lourosa", "鲁斯塔尼亚"),
                Map.entry("Lusitânia", "鲁斯塔尼亚"),
                Map.entry("Kalmar", "卡尔马"),
                Map.entry("Al Fayha Club", "迈季宽广"),
                Map.entry("Sparta", "鹿斯巴达"),
                Map.entry("Almere", "阿尔梅勒"),
                Map.entry("Nacional Madeira", "葡国民"),
                Map.entry("AD Camacha", "Camacha"),
                Map.entry("Leixoes", "雷克斯欧"),
                Map.entry("AVS", "阿维SAD"),
                Map.entry("Académica Coimbra", "科英布拉"),
                Map.entry("Benfica B", "Benfica II"),
                Map.entry("Estrela", "阿马多拉"),
                Map.entry("埃尔夫斯堡", "埃夫斯堡"),
                Map.entry("OFI Creta", "OFI"),
                Map.entry("OFI Crete", "OFI"),
                Map.entry("TOP Oss", "奥斯"),
                Map.entry("PSV埃因霍温", "埃因霍温"),
                Map.entry("Malines", "梅赫伦"),
                Map.entry("FC Eindhoven", "埃因FC"),
                Map.entry("Quick Boys", "迅速男孩"),
                Map.entry("GD Chaves", "沙维什"),
                Map.entry("Al Ahli Jeddah", "吉达国民"),
                Map.entry("吉阿赫利", "吉达国民"),
                Map.entry("葡萄牙国民", "葡国民"),
                Map.entry("K. Lierse SK", "Lierse K"),
                Map.entry("Olympiakos", "奥林匹亚"),
                Map.entry("ZW", "瓦雷赫姆"),
                Map.entry("La Gantoise", "根特"),
                Map.entry("Asteras Tripoli", "特里波利"),
                Map.entry("里斯本竞技", "里斯本"),
                Map.entry("Al Nassr Riyadh", "利雅胜利"),
                Map.entry("利亚胜利", "利雅胜利"),
                Map.entry("托特纳姆热刺", "热刺"),
                Map.entry("马德里竞技", "马竞"),
                Map.entry("Valladolid", "巴利亚多"),
                Map.entry("Tenerife", "特内里费"),
                Map.entry("Reading", "雷丁"),
                Map.entry("AD Ceuta", "休达"),
                Map.entry("皇家马德里", "皇马"),
                Map.entry("Paju Frontier", "Paju"),
                Map.entry("Racing Santander", "桑坦德"),
                Map.entry("桑坦德竞技", "桑坦德"),
                Map.entry("狼队", "伍尔弗"),
                Map.entry("毕尔巴鄂竞技", "毕尔巴鄂"),
                Map.entry("加拉塔萨雷", "加拉塔萨"),
                Map.entry("Levante UD", "莱万特"),
                Map.entry("Coventry City", "考文垂"),
                Map.entry("CE Sabadell", "萨瓦德尔"),
                Map.entry("Sabadell", "萨瓦德尔"),
                Map.entry("Pau", "波城FC"),
                Map.entry("UE Olot", "Olot"),
                Map.entry("Albacete", "阿瓦塞特"),
                Map.entry("Al Qadsiah", "胡巴卡德"),
                Map.entry("Sheff Utd", "谢菲联"),
                Map.entry("CD Castellón", "卡斯特隆"),
                Map.entry("SD Eibar", "埃瓦尔"),
                Map.entry("Alavés", "阿拉维斯"),
                Map.entry("Deportivo Alavés", "阿拉维斯"),
                Map.entry("Naval", "Naval 1893"),
                Map.entry("诺丁汉森林", "诺丁汉"),
                Map.entry("AD Machico", "马奇科"),
                Map.entry("SV Meerssen", "SV梅尔森"),
                Map.entry("VOC Rotterdam", "VOC"),
                Map.entry("Jazira Abu Dhabi", "贾兹拉"),
                Map.entry("伏伊伏丁那", "伏伊伏丁"),
                Map.entry("AEK", "雅典AEK"),
                Map.entry("FC Vaduz", "瓦杜兹"),
                Map.entry("哥德堡盖斯", "盖斯"),
                Map.entry("Louvain", "勒芬"),
                Map.entry("DAC 1904 Dunajska Streda", "DAC 1904"),
                Map.entry("费伦茨瓦罗斯", "费伦茨"),
                Map.entry("Sportfreunde Lotte", "洛特"),
                Map.entry("SF Lotte", "洛特"),
                Map.entry("São João de Ver", "维拉"),
                Map.entry("阿斯顿维拉", "维拉"),
                Map.entry("Birmingham City", "伯明翰"),
                Map.entry("Birmingham", "伯明翰"),
                Map.entry("Sporting Lisboa B", "Sporting CP II"),
                Map.entry("CD Mafra", "Mafra"),
                Map.entry("莱里雅", "莱里亚"),
                Map.entry("Uniao Leiria", "莱里亚"),
                Map.entry("Uniao de Leiria", "莱里亚"),
                Map.entry("Varzim SC", "瓦兹姆"),
                Map.entry("USC Paredes", "Paredes"),
                Map.entry("Ahli", "吉达国民"),
                Map.entry("FC Felgueiras", "Felgueiras 1932"),
                Map.entry("维戈塞尔塔", "维尔塔"),
                Map.entry("Stoke", "斯托克城"),
                Map.entry("Leca", "勒卡"),
                Map.entry("Machico", "马奇科"));

        expectedMappings.forEach((source, expected) ->
                assertEquals(expected, ClubTeamNameTranslator.translate(
                        Competition.CLUB_FRIENDLY,
                        source), source));
    }

    @Test
    void shouldApplyRequestedTeamNameMappings() throws IOException {
        assumeMappingsImported();

        Map<String, String> expectedMappings = Map.ofEntries(
                Map.entry("林肯红色小鬼", "红色小鬼"),
                Map.entry("Copenhague", "哥本哈根"),
                Map.entry("Halmstad", "哈尔姆斯"),
                Map.entry("Iberia 1999", "Saburtalo"),
                Map.entry("布拉格斯拉维亚", "斯拉维亚"),
                Map.entry("贝尔格莱德红星", "贝红星"),
                Map.entry("格拉茨AK", "GAK"),
                Map.entry("Zemplin Michalovce", "泽姆匹林米哈洛夫采"),
                Map.entry("奥林匹亚科斯", "奥林匹亚"),
                Map.entry("Ajax Amsterdam", "阿贾克斯"),
                Map.entry("福图纳锡塔德", "福图纳"),
                Map.entry("V-Varen Nagasaki", "长崎航海"),
                Map.entry("MSV Duisburg", "杜伊斯堡"),
                Map.entry("De Treffers", "赫鲁斯"),
                Map.entry("Opava", "奥帕瓦"),
                Map.entry("皇家贝蒂斯", "贝蒂斯"),
                Map.entry("VfL Wolfsburg", "沃夫斯堡"),
                Map.entry("Saint-Gall", "圣加仑"),
                Map.entry("Macon 71", "Mâcon"),
                Map.entry("圣吉尔联合", "圣吉联合"),
                Map.entry("布加勒斯特星", "布星"),
                Map.entry("Diegem", "迪耶根体育"),
                Map.entry("安德莱赫特", "安德莱"),
                Map.entry("Start", "斯达"),
                Map.entry("林比", "灵比"),
                Map.entry("波兹南莱赫", "波兹南"),
                Map.entry("Union Saint-Gilloise", "圣吉联合"),
                Map.entry("Polissya Zhitomir", "Polissya"),
                Map.entry("克拉约瓦大学", "克拉约瓦"),
                Map.entry("Turan Tovuz", "Turan"),
                Map.entry("格拉茨风暴", "格风暴"),
                Map.entry("阿德米拉", "默德林"),
                Map.entry("Sanfrecce", "广岛三箭"),
                Map.entry("布拉格斯巴达", "布斯巴达"),
                Map.entry("Zeleziarne Podbrezova", "Podbrezova"),
                Map.entry("Dukla Banska Bystrica", "Banska Bystrica"),
                Map.entry("FC Zbrojovka Brno", "布尔诺"),
                Map.entry("Lillestrøm", "利勒斯特"),
                Map.entry("腓特烈斯塔", "腓特烈"),
                Map.entry("霍森斯", "霍尔森斯"),
                Map.entry("布拉迪斯拉发", "布拉迪斯"),
                Map.entry("Hradec Kralove", "Kralove"),
                Map.entry("Mura", "穆拉"),
                Map.entry("Petrolul Ploiesti", "Petrolul 52"),
                Map.entry("Levski Sofia", "索列夫"),
                Map.entry("UTA Arad", "UTA"),
                Map.entry("FK Maxline Vitebsk", "ML Vitebsk"),
                Map.entry("Universitatea Cluj", "U Cluj"),
                Map.entry("比亚韦斯托克", "比亚韦"),
                Map.entry("ASSE", "圣埃蒂安"),
                Map.entry("帕纳辛纳科斯", "帕纳辛纳"),
                Map.entry("阿拉木图凯拉特", "阿拉木图"),
                Map.entry("索菲亚列夫斯基", "索列夫"),
                Map.entry("Septemvri Sofia", "Septemvri"),
                Map.entry("PFC Lokomotiv Sofia 1929", "Lokomotiv Sf"),
                Map.entry("Dunav Ruse", "Dunav"),
                Map.entry("巴尼亚", "巴战士"),
                Map.entry("FK Radnik Surdulica", "Radnik"),
                Map.entry("U Craiova", "克拉约瓦"),
                Map.entry("利勒斯特罗姆", "利勒斯特"),
                Map.entry("桑纳菲尤尔", "桑纳菲"),
                Map.entry("K. Diegem Sport", "迪耶根体育"),
                Map.entry("Celje", "采列"),
                Map.entry("Riga FC", "里加FC"),
                Map.entry("Gandzasar FC", "Gandzasar"),
                Map.entry("巴黎圣日尔曼", "巴黎圣曼"));

        for (Map.Entry<String, String> mapping : expectedMappings.entrySet()) {
            assertEquals(mapping.getValue(), ClubTeamNameTranslator.translate(mapping.getKey()));
        }
    }

    @Test
    void shouldResolveEveryMappingAliasToStableStandardName() throws IOException {
        assumeMappingsImported();

        InputStream inputStream = ClubTeamNameTranslatorTest.class.getClassLoader()
                .getResourceAsStream("data/team_name_mappings.csv");
        assertNotNull(inputStream);

        int checkedRows = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            List<String> headers = CsvUtils.parseLine(headerLine);
            Map<String, Integer> indexes = new LinkedHashMap<>();
            for (int index = 0; index < headers.size(); index++) {
                indexes.put(headers.get(index), index);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                List<String> row = CsvUtils.parseLine(line);
                String competitionCode = CsvUtils.get(row, indexes.get("competition"));
                Competition competition = "*".equals(competitionCode)
                        ? null
                        : Competition.fromCode(competitionCode);
                String aliasName = CsvUtils.get(row, indexes.get("alias_team_name"));
                String translatedName = ClubTeamNameTranslator.translate(competition, aliasName);
                assertNotNull(translatedName, competitionCode + ":" + aliasName);
                assertEquals(
                        translatedName,
                        ClubTeamNameTranslator.translate(competition, translatedName),
                        competitionCode + ":" + aliasName);
                checkedRows++;
            }
        }
        assertTrue(checkedRows > 0);
    }

    private void assumeMappingsImported() throws IOException {
        InputStream inputStream = ClubTeamNameTranslatorTest.class.getClassLoader()
                .getResourceAsStream("data/team_name_mappings.csv");
        assertNotNull(inputStream);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream,
                StandardCharsets.UTF_8))) {
            reader.readLine();
            assumeTrue(
                    reader.lines().anyMatch(line -> !line.isBlank() && !line.startsWith("#")),
                    "球队名映射等待重新导入");
        }
    }

}
