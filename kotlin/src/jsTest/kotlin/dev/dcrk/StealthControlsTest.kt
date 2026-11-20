package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StealthControlsTest {
    @Test
    fun shortcutAndPageToggleUseSameState() {
        val messages = mutableListOf<String>()
        val controls = createStealthControls { messages.add(it) }
        var revealed = false
        val api: dynamic = js("({})")
        api.toggle = { revealed = !revealed }
        api.isRevealed = { revealed }

        val popup = controls.pageToggles.reveal
        assertEquals("스텔스로 숨긴 이미지를 보입니다", popup.desc as String)
        assertFalse(popup.isOn(api) as Boolean)

        controls.shortcuts.stealthPause(api)
        assertTrue(popup.isOn(api) as Boolean)
        assertEquals(listOf("이미지를 보이게 했습니다."), messages)

        popup.toggle(api)
        assertFalse(popup.isOn(api) as Boolean)
        controls.shortcuts.stealthPause(api)
        assertFalse(popup.isOn(api) as Boolean)
        assertEquals("이미지를 숨겼습니다.", messages.last())
    }
}
