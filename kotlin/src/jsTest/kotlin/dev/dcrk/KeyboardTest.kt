package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KeyboardTest {
    @Test
    fun modifierKeys() {
        assertFalse(hasModifier(false, false, false, false))
        assertTrue(hasModifier(true, false, false, false))
        assertTrue(hasModifier(false, true, false, false))
        assertTrue(hasModifier(false, false, true, false))
        assertTrue(hasModifier(false, false, false, true))
    }

    @Test
    fun keyCodeWinsWhenItMatchesOriginalPattern() {
        assertEquals("a", pressedKey("KeyA", "ㅇ"))
        assertEquals("3", pressedKey("Digit3", "삼"))
        assertEquals("1", pressedKey("Key1", "일"))
        assertEquals("a", pressedKey("DigitA", "한"))
        assertEquals("pagedown", pressedKey("PageDown", "PageDown"))
        assertEquals("ㅇ", pressedKey("Keya", "ㅇ"))
        assertEquals("b", pressedKey("KeyAB", "B"))
    }
}
