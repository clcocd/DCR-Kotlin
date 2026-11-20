package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeBoundaryConstantsTest {
    @Test
    fun messagingAndUiContractsMatchOriginal() {
        val values = runtimeConstants()
        val ui = values.ui
        val messages = values.messaging
        assertEquals("cursor-pointer rounded-sm outline-none focus-visible:ring-3 focus-visible:ring-ring/50", ui.focusRing as String)
        assertEquals("[data-slot=dialog-overlay], [data-slot=alert-dialog-overlay]", ui.modalOverlaySelector as String)
        assertEquals("저장하지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.", ui.saveFailed as String)
        assertEquals("refresher:list-replaced", messages.listReplacedEvent as String)
        assertEquals("data-refresher-turnstile", messages.turnstileHost as String)
        assertEquals("refresher-turnstile-cancel", messages.turnstileCancelEvent as String)
    }

    @Test
    fun storageAndWebLocksKeepExistingKeys() {
        val stringify: dynamic = js("JSON.stringify")
        assertEquals(
            """{"blockDefaultsKey":"local:refresher:block:defaults","modulesKey":"local:refresher:modules","listLocks":{"blocks":"refresher:blocks","memos":"refresher:memos","moduleSettings":"refresher:module-settings"},"dbMetaKey":"local:refresher:db:meta","dbKeys":{"ip":"local:refresher:db:ip"}}""",
            stringify(runtimeConstants().storage) as String
        )
    }
}
