package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FontsTest {
    @Test
    fun familyNamesAndDefaults() {
        assertEquals("\"Pretendard\", \"맑은 고딕\", sans-serif", fontFamilyOf("Pretendard, 맑은 고딕"))
        assertEquals("\"Noto Sans CJK KR\", \"NanumGothic\", sans-serif", fontFamilyOf(" "))
        assertEquals("\"AB\", \"C\", sans-serif", fontFamilyOf("A\nB,\tC\u0000"))
        assertEquals("\"serif\", sans-serif", fontFamilyOf("\"serif\""))
        assertEquals("\"Noto Sans\", sans-serif", fontFamilyOf("'Noto Sans'"))
        assertEquals("\"Arial\", MonoSpace", fontFamilyOf("Arial, MonoSpace"))
        assertEquals("\"a\\\\\\\"; } b{\", sans-serif", fontFamilyOf("a\\\"; } b{"))
    }

    @Test
    fun stylesheetKeepsOriginalSelectivity() {
        val noDc = buildFontCss("Pretendard", 13.0, false)
        assertEquals(":root { --refresher-font: \"Pretendard\", sans-serif; --refresher-preview-font-size: 15px; }", noDc)
        assertFalse(noDc.contains(".write_div"))
        val dc = buildFontCss("Pretendard", 13.0, true)
        assertTrue(dc.contains(":root body"))
        assertTrue(dc.contains(":root .btn_cmt_close"))
        assertTrue(dc.contains(":root .write_div { font-size: 13px; }"))
    }
    @Test
    fun metadataKeepsOriginalSettingsAndCssVariable() {
        val meta = fontsMeta()
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("fonts", meta.id as String)
        assertEquals(stringify(staticFeatureSettings("fonts")) as String, stringify(meta.settings) as String)
        val settings = props("customFonts" to "Pretendard")
        assertEquals(
            fontFamilyOf("Pretendard"),
            meta.extensionPageVars(settings)["--refresher-font"] as String
        )
    }

}
