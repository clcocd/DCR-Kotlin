package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LayoutTest {
    @Test
    fun compactState() {
        assertFalse(layoutCompact(false, false, false, false, true))
        assertTrue(layoutCompact(true, false, false, false, true))
        assertTrue(layoutCompact(false, true, false, false, true))
        assertFalse(layoutCompact(true, true, true, false, true))
        assertFalse(layoutCompact(false, true, false, true, false))
        assertTrue(layoutCompact(false, true, false, true, true))
    }

    @Test
    fun hideCssOmitsOnlyNoticesOnNoticePage() {
        val options: dynamic = js("({removeNotice:{selector:'tr.notice'},removeDCNotice:{selector:'tr.dc'},removeAi:{selector:'tr.ai'}})")
        val settings: dynamic = js("({removeNotice:true,removeDCNotice:true,removeAi:true})")
        assertEquals(
            "tr.ai { display: none !important; }",
            layoutHideCss(options, settings, true)
        )
        assertEquals(
            "tr.notice { display: none !important; }\ntr.dc { display: none !important; }\ntr.ai { display: none !important; }",
            layoutHideCss(options, settings, false)
        )
    }
    @Test
    fun originalSettingsSchemaAndOrder() {
        val settings = layoutSettings(arrayOf("hideGalleryView", "removeNotice"))
        val keys = js("Object.keys")(settings) as Array<String>
        assertEquals("activePixel,forceCompact,useCompactModeOnView,hideGalleryView,removeNotice,pushToRight", keys.joinToString(","))
        val width = settings.activePixel
        assertEquals("range", width.type as String)
        assertEquals(900, width.default as Int)
        assertEquals(100, width.min as Int)
        assertEquals(3840, width.max as Int)
        assertEquals(1, width.step as Int)
        assertEquals("px", width.unit as String)
        assertEquals(false, settings.forceCompact.default as Boolean)
        assertEquals(true, settings.useCompactModeOnView.default as Boolean)
        assertEquals(false, settings.hideGalleryView.default as Boolean)
        assertEquals(false, settings.removeNotice.default as Boolean)
        assertEquals(false, settings.pushToRight.default as Boolean)
    }

    @Test
    fun layoutMetaPreservesModuleIdAndSettings() {
        val ids = arrayOf("hideGalleryView", "removeNotice")
        val meta = layoutMeta(ids)
        assertEquals("layout", meta.id as String)
        val stringify: dynamic = js("JSON.stringify")
        assertEquals(stringify(layoutSettings(ids)) as String, stringify(meta.settings) as String)
    }

}
