package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class TitleCountTest {
    @Test
    fun countOnlyReplacesOurPrefix() {
        assertEquals("(3) 갤러리", titleCountText("갤러리", false, 3))
        assertEquals("(5) 갤러리", titleCountText("(3) 갤러리", true, 5))
        assertEquals("갤러리", titleCountText("(5) 갤러리", true, 0))
        assertEquals("(2) (1) 글 제목", titleCountText("(1) 글 제목", false, 2))
    }
}
