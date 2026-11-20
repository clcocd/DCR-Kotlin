package dev.dcrk

import dev.dcrk.block.normalizeDefaults
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockDefaultsWatcherTest {
    @Test
    fun equalStoredDefaultsDoNotTriggerStateUpdate() {
        var defaults: dynamic = normalizeDefaults(null)
        var writes = 0
        val store: dynamic = js("({})")
        store.getState = { props("defaults" to defaults) }
        store.setState = { next: dynamic ->
            defaults = next.defaults
            writes++
        }
        val normalizer: (dynamic) -> dynamic = { normalizeDefaults(it) }
        val equal: (dynamic, dynamic) -> Boolean = { left, right ->
            val stringify: dynamic = js("JSON.stringify")
            stringify(left) == stringify(right)
        }
        val watch = createBlockDefaultsWatcher(store, normalizer, equal)

        watch(props("NICK" to "SAME"))
        assertEquals(0, writes)

        watch(props("NICK" to "CONTAIN"))
        assertEquals(1, writes)
        assertEquals("CONTAIN", defaults.NICK as String)

        watch(props("NICK" to "CONTAIN"))
        assertEquals(1, writes)

        watch(props("NICK" to "invalid"))
        assertEquals(2, writes)
        assertEquals("SAME", defaults.NICK as String)
    }
}
