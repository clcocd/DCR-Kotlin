package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class BlockOptionModesTest {
    @Test
    fun descriptionTextAndModeOrderRemainIdentical() {
        val descriptions = optionDetectModeDescriptions()
        assertEquals("SAME,CONTAIN,NOT_SAME,NOT_CONTAIN",
            (js("Object.keys")(descriptions) as Array<String>).joinToString(","))
        assertEquals("값과 똑같으면 가립니다.", descriptions.SAME as String)
        assertEquals("값이 들어 있으면 가립니다.", descriptions.CONTAIN as String)
        assertEquals("값과 똑같은 것만 남기고 나머지를 모두 가립니다.\n불일치, 불포함 항목이 여럿이면 하나에만 맞아도 남깁니다.", descriptions.NOT_SAME as String)
        assertEquals("값이 들어 있는 것만 남기고 나머지를 모두 가립니다.\n불일치, 불포함 항목이 여럿이면 하나에만 맞아도 남깁니다.", descriptions.NOT_CONTAIN as String)
    }
}
