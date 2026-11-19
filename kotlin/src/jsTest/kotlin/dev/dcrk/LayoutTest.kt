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
}
