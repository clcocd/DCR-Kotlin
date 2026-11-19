package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PreviewMediaTest {
    @Test
    fun labelsPreserveOriginalPriority() {
        assertEquals("디시콘 정보", previewMediaLabel(true, true, false, true, false))
        assertNull(previewMediaLabel(false, true, false, true, true))
        assertEquals("이미지 크게 보기", previewMediaLabel(false, false, false, true, true))
        assertEquals("원본 보기", previewMediaLabel(false, false, true, true, true))
        assertEquals("원본 보기", previewMediaLabel(false, false, false, false, true))
        assertNull(previewMediaLabel(false, false, true, true, false))
        assertNull(previewMediaLabel(false, false, false, false, false))
    }
}
