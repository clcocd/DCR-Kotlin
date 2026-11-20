package dev.dcrk

import dev.dcrk.block.blockMeta
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockMetadataTest {
    @Test
    fun originalBlockCommandAndPageToggle() {
        val url: dynamic = js("new RegExp('/board/(view|lists)')")
        val settings = staticFeatureSettings("block")
        val meta = blockMeta(arrayOf(url), settings)
        assertEquals("block", meta.id as String)
        assertEquals(url, (meta.urls as Array<dynamic>)[0])
        assertEquals(settings, meta.settings)
        assertEquals("reveal", (meta.toggles as Array<String>)[0])
        val commands = js("Object.keys")(meta.commands) as Array<String>
        assertEquals("blockReveal", commands.joinToString(","))
        val props = js("Object.keys")(meta.commands.blockReveal) as Array<String>
        assertEquals(0, props.size)
    }
}
