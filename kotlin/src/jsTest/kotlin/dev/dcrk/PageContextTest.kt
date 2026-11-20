package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PageContextTest {
    private val viewPage: dynamic get() = js("new RegExp('/board/view')")

    @Test
    fun viewPageGetsItsOriginalPostAndGallery() {
        val url: dynamic = js("new URL('https://gall.dcinside.com/board/view/?id=test&no=123')")
        val result = pageContextOf(url, viewPage)
        assertSame(url, result.documentUrl)
        assertTrue(result.isViewPage as Boolean)
        assertEquals("123", result.pagePostNo as String)
        assertEquals("test", result.pageGallery as String)
    }

    @Test
    fun listPageHasNoPostNumberEvenWhenQueryHasOne() {
        val url: dynamic = js("new URL('https://gall.dcinside.com/board/lists/?id=mini&no=999')")
        val result = pageContextOf(url, viewPage)
        assertFalse(result.isViewPage as Boolean)
        assertNull(result.pagePostNo)
        assertEquals("mini", result.pageGallery as String)
    }

    @Test
    fun missingParametersRemainNull() {
        val url: dynamic = js("new URL('https://gall.dcinside.com/board/view/')")
        val result = pageContextOf(url, viewPage)
        assertTrue(result.isViewPage as Boolean)
        assertNull(result.pagePostNo)
        assertNull(result.pageGallery)
    }
}
