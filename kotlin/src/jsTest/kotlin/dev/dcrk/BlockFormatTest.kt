package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BlockFormatTest {
    @Test
    fun extraLabelsPreserveOriginalOrder() {
        assertEquals("[정규식] [갤러리: g] [불일치]",
            composeExtra(true, "g", "NOT_SAME"))
        assertEquals("", composeExtra(false, null, null))
        assertEquals("[포함]", composeExtra(false, "", "CONTAIN"))
        assertEquals("[불포함]", composeExtra(false, "", "NOT_CONTAIN"))
    }

    @Test
    fun blockKeyIgnoresRegexAndSeparatesGallery() {
        assertEquals("[\"a\",\"\"]", blockKey("a", null))
        assertEquals(blockKey("a", null), blockKey("a", ""))
        assertNotEquals(blockKey("a", "g"), blockKey("a", ""))
        assertEquals("[\"\\\"test\\\"\",\"g\"]", blockKey("\"test\"", "g"))
    }
}
