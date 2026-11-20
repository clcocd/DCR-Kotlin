package dev.dcrk

import dev.dcrk.block.blockMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class BlockMetaTest {
    @Test
    fun metadataKeepsModuleSettingsAndActions() {
        val urls = arrayOf<dynamic>("board")
        val meta = blockMeta(urls)
        assertEquals("block", meta.id as String)
        assertSame(urls, meta.urls as Array<dynamic>)
        assertEquals("reveal", (meta.toggles as Array<String>)[0])
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("""{"blockReveal":{}}""", stringify(meta.commands) as String)
        assertEquals(stringify(staticFeatureSettings("block")) as String, stringify(meta.settings) as String)
    }
}
