package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StorageMetadataTest {
    @Test
    fun originalNamesAndOrderStayUnchanged() {
        val tables = storageMetadata()
        val stringify: dynamic = js("JSON.stringify")
        assertEquals(
            """{"NICK":"닉네임","ID":"아이디","IP":"IP","TITLE":"제목","TEXT":"내용","COMMENT":"댓글","DCCON":"디시콘","TAB":"말머리"}""",
            stringify(tables.typeNames) as String
        )
        assertEquals(
            """{"SAME":"일치","CONTAIN":"포함","NOT_SAME":"불일치","NOT_CONTAIN":"불포함"}""",
            stringify(tables.detectModeNames) as String
        )
        assertEquals("""{"UID":"아이디","NICK":"닉네임","IP":"IP"}""", stringify(tables.memoTypeNames) as String)
        assertEquals(
            """{"NICK":"SAME","ID":"SAME","IP":"SAME","TITLE":"CONTAIN","TEXT":"CONTAIN","COMMENT":"CONTAIN","DCCON":"SAME","TAB":"SAME"}""",
            stringify(tables.defaultDetectMode) as String
        )
    }

    @Test
    fun typedObjectUtilitiesRetainNativeJsSemantics() {
        val value: dynamic = js("({first: undefined, second: 2})")
        assertEquals("""["first","second"]""", js("JSON.stringify")(typedObjectKeys(value)) as String)
        assertEquals("""[["first",null],["second",2]]""", js("JSON.stringify")(typedObjectEntries(value)) as String)
        assertEquals("""{"one":1,"two":2}""",
            js("JSON.stringify")(typedObjectFromEntries(js("([['one',1],['two',2]])"))) as String)
        assertTrue(typedArrayIncludes(arrayOf(1, 2, 3), 2))
        assertFalse(typedArrayIncludes(arrayOf(1, 2, 3), "2"))
    }
}
