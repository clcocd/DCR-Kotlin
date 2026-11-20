package dev.dcrk

import dev.dcrk.block.*

import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BlockNormalizeTest {
    @Test
    fun rejectsInvalidValuesAndKeepsValidEntries() {
        val source: dynamic = js("""([
            {id:"1",content:"ok",isRegex:false},
            {id:"2",content:"   ",isRegex:false},
            {id:"3",content:"x",isRegex:"no"},
            {id:"4",content:"x",isRegex:false,mode:"same"},
            {id:"5",content:"x",isRegex:false,gallery:1},
            "text"
        ])""")
        val list = normalizeBlockList(source, js("[]"))
        assertEquals(1, list.length as Int)
        assertEquals("1", list[0].id as String)
        assertEquals(0, normalizeBlockList(js("({})"), js("[]")).length as Int)
    }

    @Test
    fun preservesIdsAndLegacyFieldOrder() {
        val source: dynamic = js("""([
            {id:"a",content:"1",isRegex:true,extra:"[정규식] [갤러리: g]",gallery:"g"},
            {id:"a",content:"2",isRegex:false},
            {content:"3",isRegex:false}
        ])""")
        val list = normalizeBlockList(source, js("[]"))
        assertEquals("a", list[0].id as String)
        assertNotEquals("a", list[1].id as String)
        assertTrue((list[2].id as String).isNotEmpty())
        assertEquals("undefined", jsTypeOf(list[0].extra))
        val keys: Array<String> = js("Object.keys")(list[0])
        assertEquals("id,content,isRegex,extra,gallery", keys.joinToString(","))
    }

    @Test
    fun matchesKnownKeysAndDefaults() {
        val known: dynamic = js("""([{id:"old",content:"a",isRegex:false,gallery:"g"}])""")
        val raw: dynamic = js("""([{content:"a",isRegex:true,gallery:"g"}])""")
        assertEquals("old", normalizeBlockList(raw, known)[0].id as String)

        val options: dynamic = js("""({NICK:"CONTAIN",TITLE:"same",ID:1})""")
        val defaults = normalizeDefaults(options)
        assertEquals("CONTAIN", defaults.NICK as String)
        assertEquals("CONTAIN", defaults.TITLE as String)
        assertEquals("SAME", defaults.ID as String)
        assertEquals("SAME", normalizeDefaults("not a record").NICK as String)
    }
}
