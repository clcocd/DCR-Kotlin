package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class GalleryKindEdgeTest {
    @Test
    fun requiresExactLowercasePrefixAndTrailingSlash() {
        assertEquals("normal", galleryKindFromPath("/MGALLERY/board/lists"))
        assertEquals("normal", galleryKindFromPath("/mgallery"))
        assertEquals("normal", galleryKindFromPath("/board/lists?text=/mini/"))
        assertEquals("mini", galleryKindFromPath("/mini/"))
        assertEquals("person", galleryKindFromPath("/person/board/view"))
    }
}
