package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DynamicModuleLabelsTest {
    @Test
    fun imageSearchEngineLabelsPreserveNamesAndSharedGroup() {
        val ids = arrayOf("saucenao", "googleLens", "yandex", "ascii2d", "tineye", "iqdb", "tracemoe")
        val engines = props(
            "saucenao" to props("name" to "SauceNAO"),
            "googleLens" to props("name" to "Google Lens"),
            "yandex" to props("name" to "Yandex"),
            "ascii2d" to props("name" to "ascii2d"),
            "tineye" to props("name" to "TinEye"),
            "iqdb" to props("name" to "IQDB"),
            "tracemoe" to props("name" to "trace.moe")
        )
        val labels = imageSearchLabels(ids, engines)
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("이미지 검색", labels.name as String)
        assertEquals(
            """["saucenao","googleLens","yandex","ascii2d","tineye","iqdb","tracemoe"]""",
            stringify(js("Object.keys")(labels.settings)) as String
        )
        assertEquals("SauceNAO", labels.settings.saucenao.name as String)
        assertEquals("Google Lens에서 검색합니다.", labels.settings.googleLens.desc as String)
        assertEquals("trace.moe에서 검색합니다.", labels.settings.tracemoe.desc as String)
        assertSame(labels.settings.saucenao.group, labels.settings.tracemoe.group)
        assertEquals("검색 엔진", labels.settings.saucenao.group.name as String)
    }

    @Test
    fun userInfoLabelsPreserveDynamicBadgeOrderAndGroupIdentity() {
        val labels = userInfoLabels()
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("유저 정보", labels.name as String)
        assertEquals(
            """["showFixedNickUID","showHalfFixedNickUID","ipInfoFilter","checkRatio","alarmRatio","lowActivityAction","uidColor","ratioColor","ratioAlarmColor","koreaColor","japanColor","chinaColor","foreignColor","vpnColor","badgeOrder"]""",
            stringify(js("Object.keys")(labels.settings)) as String
        )
        assertEquals("아이디/IP", labels.settings.uidColor.name as String)
        assertEquals("IP 그 외 해외", labels.settings.foreignColor.name as String)
        assertEquals("IP VPN 배지의 글자 색입니다.", labels.settings.vpnColor.desc as String)
        assertSame(labels.settings.uidColor.group, labels.settings.vpnColor.group)
        assertSame(labels.settings.alarmRatio.group, labels.settings.lowActivityAction.group)
        assertEquals(
            "IP의 통신사, 조직과 국가를 표시할 대상입니다. VPN은 국가와 상관없이 해외, VPN에 들어갑니다. 표시되는 정보는 공개 IP 데이터로 추정한 값이라 실제와 다를 수 있습니다.",
            labels.settings.ipInfoFilter.desc as String
        )
    }
}
