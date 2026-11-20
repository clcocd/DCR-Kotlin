package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PreviewMetaTest {
    @Test
    fun originalMetadataKeepsPageAndV5CompatibleSettings() {
        val urls = arrayOf<dynamic>("board")
        val meta = previewMeta(urls)
        assertEquals("preview", meta.id as String)
        assertSame(urls, meta.urls as Array<dynamic>)
        val stringify: dynamic = js("JSON.stringify")
        assertEquals(stringify(staticFeatureSettings("preview")) as String, stringify(meta.settings) as String)
        assertEquals(1200, meta.settings.previewWidth.default as Int)
        assertEquals(300, meta.settings.longPressDelay.default as Int)
        assertEquals(false, meta.settings.autoRefreshComment.default as Boolean)
        assertEquals(true, meta.settings.highlightNewComments.default as Boolean)
        assertEquals(false, meta.settings.archiveArticle.default as Boolean)
    }
}
