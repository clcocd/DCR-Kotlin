package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageSearchBackgroundTest {
    @Test
    fun imageSearchUrlPreservesQueryWhenChangingHostAndPath() {
        val engines: dynamic = js("({saucenao:{name:'SauceNAO',url:'https://saucenao.com/search.php?url='}})")
        val ids: dynamic = js("(['saucenao'])")
        val original = "https://dcimg1.dcinside.com/viewimage.php?id=test&no=1"
        val transformed = "https://image.dcinside.com/dccon.php?id=test&no=1"
        val expected = "https://saucenao.com/search.php?url=" +
            (js("encodeURIComponent")(transformed) as String)
        assertEquals(expected, imageSearchUrlKotlin("saucenao", original, ids, engines))
    }

    @Test
    fun unknownEngineAndNonImageAreNotOpened() {
        val engines: dynamic = js("({saucenao:{url:'https://saucenao.com/search.php?url='}})")
        val ids: dynamic = js("(['saucenao'])")
        assertNull(imageSearchUrlKotlin("unknown", "https://a.com/viewimage.php?id=1", ids, engines))
        assertNull(imageSearchUrlKotlin("saucenao", "https://a.com/image.png", ids, engines))
    }
}
