package dev.dcrk

import dev.dcrk.block.createBlockControls
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureControlsTest {
    @Test
    fun blockRevealKeepsShortcutAndPopupInSync() {
        var revealed = false
        var hidden = 12
        val api: dynamic = js("({})")
        api.toggleReveal = { revealed = !revealed }
        api.isRevealed = { revealed }
        api.hiddenCount = { hidden }

        val controls = createBlockControls()
        val toggle = controls.pageToggles.reveal
        assertEquals("가린 12개를 흐리게 보입니다", toggle.desc(api) as String)
        assertFalse(toggle.isOn(api) as Boolean)
        controls.shortcuts.blockReveal(api)
        assertTrue(toggle.isOn(api) as Boolean)
        hidden = 0
        assertEquals("가린 0개를 흐리게 보입니다", toggle.desc(api) as String)
        toggle.toggle(api)
        assertFalse(toggle.isOn(api) as Boolean)
    }

    @Test
    fun refreshShortcutsAndPageToggleUseSamePauseState() {
        var paused = false
        var loads = 0
        val api: dynamic = js("({})")
        api.refreshLists = { loads++; js("Promise.resolve()") }
        api.togglePause = { paused = !paused }
        api.isPaused = { paused }

        val controls = createRefreshControls()
        val toggle = controls.pageToggles.pause
        assertEquals("이 페이지의 자동 새로고침을 멈춥니다", toggle.desc as String)
        assertFalse(toggle.isOn(api) as Boolean)
        controls.shortcuts.refreshLists(api)
        assertEquals(1, loads)
        controls.shortcuts.refreshPause(api)
        assertTrue(toggle.isOn(api) as Boolean)
        toggle.toggle(api)
        assertFalse(toggle.isOn(api) as Boolean)
    }
}
