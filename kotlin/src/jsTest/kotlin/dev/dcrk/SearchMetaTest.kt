package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class SearchMetaTest {
    @Test
    fun metadataKeepsOriginalUrlsAndDefaultSetting() {
        val urls = arrayOf<dynamic>("lists", "view")
        val meta = searchMeta(urls)
        assertEquals("search", meta.id as String)
        assertFalse(meta.defaultEnable as Boolean)
        assertSame(urls, meta.urls as Array<dynamic>)
        val setting = meta.settings.maxSearches
        assertEquals("range", setting.type as String)
        assertEquals(10, setting.default as Int)
        assertEquals(1, setting.min as Int)
        assertEquals(30, setting.max as Int)
        assertEquals(1, setting.step as Int)
        assertEquals("회", setting.unit as String)
    }
}
