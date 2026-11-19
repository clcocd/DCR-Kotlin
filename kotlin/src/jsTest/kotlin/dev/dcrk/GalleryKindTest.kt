package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class GalleryKindTest {
    @Test
    fun kindsMatchOriginalPaths() {
        assertEquals("normal", galleryKindFromPath("/board/lists"))
        assertEquals("minor", galleryKindFromPath("/mgallery/board/lists"))
        assertEquals("mini", galleryKindFromPath("/mini/board/lists"))
        assertEquals("person", galleryKindFromPath("/person/board/lists"))
        assertEquals("normal", galleryKindFromPath("/mgallery"))
        assertEquals("normal", galleryKindFromPath("/other/mgallery/board/lists"))
    }
}
