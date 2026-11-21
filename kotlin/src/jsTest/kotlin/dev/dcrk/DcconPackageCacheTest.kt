package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DcconPackageCacheTest {
    @Test
    fun originalPackageCacheOptionsArePreserved() {
        val instance = props("marker" to true)
        var options: dynamic = null
        val result = createDcconPackageCache { value: dynamic ->
            options = value
            instance
        }
        assertSame(instance, result)
        assertEquals(100, options.maxSize as Int)
        assertEquals(600_000, options.maxAge as Int)
    }
}
