package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModuleSettingsTest {
    @Test
    fun enabledTypeIsStrict() {
        val values: dynamic = js("({preview:'true'})")
        assertFalse(moduleEnabled("preview", false, values))
        values.preview = true
        assertTrue(moduleEnabled("preview", false, values))
        values.preview = js("undefined")
        assertTrue(moduleEnabled("preview", null, values))
    }

    @Test
    fun typeAndRangeNormalization() {
        val check: dynamic = js("({type:'check',default:true})")
        assertEquals(false, normalizeSetting(check, false))
        assertEquals(true, normalizeSetting(check, "false"))

        val range: dynamic = js("({type:'range',default:5,min:1,max:10,step:0.5})")
        assertEquals(3.5, normalizeSetting(range, 3.3))
        assertEquals(3.5, normalizeSetting(range, 3.25)) // JS Math.round: tie goes toward +infinity
        assertEquals(10.0, normalizeSetting(range, 9999))
        assertEquals(5, normalizeSetting(range, Double.NaN))
        assertEquals(5, normalizeSetting(range, Double.POSITIVE_INFINITY))
    }

    @Test
    fun orderIsPlainJsArrayAndIndependentOfDefaults() {
        val schema: dynamic = js("({type:'order',default:['a','b','c'],items:{a:'',b:'',c:''}})")
        val raw: dynamic = js("(['c','x','c',1,'a'])")
        val result = normalizeSetting(schema, raw)
        assertTrue(js("Array.isArray")(result) as Boolean)
        assertEquals("[\"c\",\"a\",\"b\"]", js("JSON.stringify")(result))
        val copy = defaultSetting(schema)
        copy.push("x")
        assertEquals(3, schema.default.length as Int)
    }

    @Test
    fun settingsReturnOnlySchemaKeysAndSupportPrototypeNames() {
        val schema: dynamic = js("({settings:{flag:{type:'check',default:true},size:{type:'range',default:5,min:1,max:10,step:1}}})")
        val stored: dynamic = js("({flag:false,gone:1})")
        val settings = settingsOf(schema, stored)
        assertEquals("{\"flag\":false,\"size\":5}", js("JSON.stringify")(settings))
        val bad: dynamic = js("(['false'])")
        assertEquals("{\"flag\":true,\"size\":5}", js("JSON.stringify")(settingsOf(schema, bad)))
    }

    @Test
    fun invalidStoredEnableMapIsDiscarded() {
        assertEquals("{}", js("JSON.stringify")(moduleEnables(null)))
        assertEquals("{}", js("JSON.stringify")(moduleEnables(js("([true])"))))
        assertEquals("{\"a\":true}", js("JSON.stringify")(moduleEnables(js("({a:true})"))))
    }
}
