package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModuleDefinitionsTest {
    @Test
    fun contentDefinitionKeepsIdentityAndMeta() {
        val meta: dynamic = js("({id:'preview',defaultEnable:false,toggles:['reveal','unused'],settings:{x:{type:'check',default:true}}})")
        val setup: dynamic = js("(function setup() { return 42 })")
        val shortcut: dynamic = js("(function shortcut() {})")
        val action: dynamic = js("({desc:'Revealed',isOn:function(){return false},toggle:function(){}})")
        val impl: dynamic = js("({})")
        impl.setup = setup
        impl.shortcuts = js("({})")
        impl.shortcuts.example = shortcut
        impl.pageToggles = js("({})")
        impl.pageToggles.reveal = action

        val result = defineContentModule(meta, impl)
        assertEquals("preview", result.id)
        assertEquals(false, result.defaultEnable)
        assertTrue(result.settings === meta.settings)
        assertTrue(result.setup === setup)
        assertTrue(result.shortcuts.example === shortcut)
        assertEquals(1, result.pageToggles.length as Int)
        assertEquals("reveal", result.pageToggles[0].id)
        assertEquals("Revealed", result.pageToggles[0].desc)
        assertTrue(result.pageToggles[0].toggle === action.toggle)
        assertFalse(js("Object.hasOwn")(meta, "pageToggles") as Boolean)
    }

    @Test
    fun emptyAndAbsentTogglesMatchJsOptionalChaining() {
        val impl: dynamic = js("({setup:function() {}})")
        val empty = defineContentModule(js("({id:'x',toggles:[]})"), impl)
        assertEquals(0, empty.pageToggles.length as Int)
        val noToggles = defineContentModule(js("({id:'x'})"), impl)
        assertTrue(js("Object.hasOwn")(noToggles, "pageToggles") as Boolean)
        assertEquals("undefined", kotlin.js.jsTypeOf(noToggles.pageToggles))
    }

    @Test
    fun backgroundCopiesOnlySelectedMetaAndImpl() {
        val meta: dynamic = js("({id:'refresh',defaultEnable:true,settings:{flag:true},urls:['ignored'],toggles:['ignored']})")
        val impl: dynamic = js("({listen:function(){},apply:function(){}})")
        val result = defineBackgroundModuleKotlin(meta, impl)
        assertEquals("refresh", result.id)
        assertTrue(result.settings === meta.settings)
        assertFalse(js("Object.hasOwn")(result, "urls") as Boolean)
        assertFalse(js("Object.hasOwn")(result, "toggles") as Boolean)
        assertTrue(result.listen === impl.listen)
        assertTrue(result.apply === impl.apply)
    }
}
