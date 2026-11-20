package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StorageCompareTest {
    @Test
    fun objectShapeIsStrict() {
        assertFalse(record(null))
        assertFalse(record(js("([])")))
        assertTrue(record(js("({})")))
        assertTrue(record(js("(new Date(0))")))
    }

    @Test
    fun canonicalSortsNestedObjectsButNotArrays() {
        val value: dynamic = js("([{text:'a',color:'red'}, {nested:{z:1,a:2}}, {list:[{b:2,a:1},{a:4}]}])")
        assertEquals(
            "[{\"color\":\"red\",\"text\":\"a\"},{\"nested\":{\"a\":2,\"z\":1}},{\"list\":[{\"a\":1,\"b\":2},{\"a\":4}]}]",
            canonical(value)
        )
        assertEquals("null", canonical(null))
    }

    @Test
    fun sizesAreOnlyForListsAndRecords() {
        assertEquals(2, sizeOf(js("([1,2])")))
        assertEquals(2, sizeOf(js("({a:1,b:2})")))
        assertNull(sizeOf("string"))
    }
}
