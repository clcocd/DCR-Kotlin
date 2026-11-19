package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StorageCompareTest {
    @Test
    fun objectShapeIsStrict() {
        assertFalse(isJsRecord(null))
        assertFalse(isJsRecord(js("([])")))
        assertTrue(isJsRecord(js("({})")))
        assertTrue(isJsRecord(js("(new Date(0))")))
    }

    @Test
    fun canonicalSortsNestedObjectsButNotArrays() {
        val value: dynamic = js("([{text:'a',color:'red'}, {nested:{z:1,a:2}}, {list:[{b:2,a:1},{a:4}]}])")
        assertEquals(
            "[{\"color\":\"red\",\"text\":\"a\"},{\"nested\":{\"a\":2,\"z\":1}},{\"list\":[{\"a\":1,\"b\":2},{\"a\":4}]}]",
            canonicalStorageValue(value)
        )
        assertEquals("null", canonicalStorageValue(null))
    }

    @Test
    fun sizesAreOnlyForListsAndRecords() {
        assertEquals(2, storageSizeOf(js("([1,2])")))
        assertEquals(2, storageSizeOf(js("({a:1,b:2})")))
        assertTrue(js("typeof")(storageSizeOf("string")) == "undefined")
    }
}
