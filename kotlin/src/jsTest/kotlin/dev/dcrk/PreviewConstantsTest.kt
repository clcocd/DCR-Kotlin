package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class PreviewConstantsTest {
    @Test
    fun originalBlockChoiceDataAndOrder() {
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("""{"1":"1시간","6":"6시간","24":"1일","168":"7일","336":"14일","744":"31일"}""",
            stringify(blockDays()) as String)
        assertEquals(
            """[["1","음란성"],["2","광고"],["3","욕설"],["4","도배"],["5","혐오 콘텐츠"],["6","저작권 침해"],["7","명예훼손"],["0","직접 입력"]]""",
            stringify(blockReasons()) as String
        )
    }

    @Test
    fun tokenKeysAndTxtconPaletteMatchOriginal() {
        val stringify: dynamic = js("JSON.stringify")
        assertEquals("6Lc-Fr0UAAAAAOdqLYqPy53MxlRMIXpNXFvBliwI", ICON_SITE_KEY)
        assertEquals(
            """{"new":"6LfrAuMtAAAAABXdEPC0ZSItQjLjlt2lC4jo3VNX","new3":"6LcjA-MtAAAAAIdgo8TKvID6HS34Ac9AkzRjuJfY"}""",
            stringify(commentSiteKeys()) as String
        )
        assertEquals("yL/M=zNa0bcPQdReSfTgUhViWjXkYIZmnpo+qArOBslCt2D3uE4Fv5G6wH178xJ9K", dAlphabet)
        assertEquals("""["3b4890","b4b4e1","f5e1f0","d2f0e6","ffffff","333333"]""", stringify(txtconBackgrounds) as String)
        assertEquals("""["ffffff","333333"]""", stringify(txtconColors) as String)
    }
}
