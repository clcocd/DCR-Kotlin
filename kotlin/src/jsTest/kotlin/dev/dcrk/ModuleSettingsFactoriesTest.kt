package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleSettingsFactoriesTest {
    @Test
    fun generatedSettingsKeepOriginalDefaultsAndOrder() {
        val stringify: dynamic = js("JSON.stringify")
        val imageIds = arrayOf("saucenao", "googleLens", "yandex", "ascii2d", "tineye", "iqdb", "tracemoe")
        assertEquals(
            """{"saucenao":{"type":"check","default":true},"googleLens":{"type":"check","default":false},"yandex":{"type":"check","default":false},"ascii2d":{"type":"check","default":false},"tineye":{"type":"check","default":false},"iqdb":{"type":"check","default":false},"tracemoe":{"type":"check","default":false}}""",
            stringify(imageSearchSettings(imageIds)) as String
        )
        val layoutIds = arrayOf("hideGalleryView", "hideUselessView", "hideGalleryImage", "removeNotice", "removeDCNotice", "removeGamemeca", "removeAi")
        assertEquals(
            """{"hideGalleryView":{"type":"check","default":false},"hideUselessView":{"type":"check","default":false},"hideGalleryImage":{"type":"check","default":false},"removeNotice":{"type":"check","default":false},"removeDCNotice":{"type":"check","default":false},"removeGamemeca":{"type":"check","default":false},"removeAi":{"type":"check","default":false}}""",
            stringify(layoutHideSettings(layoutIds)) as String
        )
        val colors = userInfoRuntimeOptions().badgeColors
        assertEquals(
            """{"uidColor":{"type":"color","default":"#999999"},"ratioColor":{"type":"color","default":"#999999"},"ratioAlarmColor":{"type":"color","default":"#ff0000"},"koreaColor":{"type":"color","default":"#6495ed"},"japanColor":{"type":"color","default":"#e5484d"},"chinaColor":{"type":"color","default":"#f76b15"},"foreignColor":{"type":"color","default":"#12a594"},"vpnColor":{"type":"color","default":"#8e4ec6"}}""",
            stringify(userInfoColorSettings(colors)) as String
        )
    }

    @Test
    fun extensionPageFontVariableUsesOriginalKotlinFontFormatter() {
        val fonts = DEFAULT_FONTS
        assertEquals("Noto Sans CJK KR, NanumGothic", fonts)
        val vars = fontExtensionPageVars(props("customFonts" to fonts))
        assertEquals(fontFamilyOf(fonts), vars["--refresher-font"] as String)
    }
}
