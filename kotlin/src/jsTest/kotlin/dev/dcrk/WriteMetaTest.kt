package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WriteMetaTest {
    @Test
    fun metadataKeepsOriginalUrlAndSettings() {
        val urls = arrayOf<dynamic>("write", "upload")
        val meta = writeMeta(urls)
        assertEquals("write", meta.id as String)
        assertFalse(meta.defaultEnable as Boolean)
        assertSame(urls, meta.urls as Array<dynamic>)
        val settings = meta.settings
        assertTrue(settings.preventExit.default as Boolean)
        assertFalse(settings.webpConvert.default as Boolean)
        assertFalse(settings.obfuscateName.default as Boolean)
        assertEquals(80, settings.webpQuality.default as Int)
        assertEquals(10, settings.webpQuality.min as Int)
        assertEquals(100, settings.webpQuality.max as Int)
        assertEquals(5, settings.webpQuality.step as Int)
        assertEquals("%", settings.webpQuality.unit as String)
        assertEquals(2, settings.webpConcurrency.default as Int)
        assertEquals(1, settings.webpConcurrency.min as Int)
        assertEquals(8, settings.webpConcurrency.max as Int)
        assertEquals("장", settings.webpConcurrency.unit as String)
    }
}
