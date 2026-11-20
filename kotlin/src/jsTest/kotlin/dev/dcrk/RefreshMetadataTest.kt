package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshMetadataTest {
    @Test
    fun metaPreservesOriginalCommandIdsAndKeys() {
        val pattern: dynamic = js("new RegExp('/board/(view|lists)')")
        val settings = staticFeatureSettings("refresh")
        val meta = refreshMeta(arrayOf(pattern), settings)
        assertEquals("refresh", meta.id as String)
        assertEquals(pattern, (meta.urls as Array<dynamic>)[0])
        assertEquals(settings, meta.settings)
        assertEquals("pause", (meta.toggles as Array<String>)[0])
        assertEquals("Alt+R", meta.commands.refreshLists.key as String)
        assertEquals("Alt+S", meta.commands.refreshPause.key as String)
        val keys = js("Object.keys")(meta.commands) as Array<String>
        assertEquals("refreshLists,refreshPause", keys.joinToString(","))
    }
}
