package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals

class ContentTextTest {
    @Test
    fun stripsMarkupWithoutDoubleUnescaping() {
        assertEquals(" R&B <3 > ", htmlToText("<p>R&amp;B &lt;3&nbsp;&gt;</p>"))
        assertEquals("&lt;", htmlToText("&amp;lt;"))
        assertEquals(" 링크  ", htmlToText("<a title=\"a>b\">링크</a><!-- <b>숨김</b> -->"))
        assertEquals("그냥 글", htmlToText("그냥 글"))
        assertEquals(" A  B ", htmlToText("<div>A</div><div>B</div>"))
    }

    @Test
    fun decodesOriginalDcinsideBase64() {
        assertEquals("207,266,253,256,217,181,304,255.5,119,91",
            decodeD("Q+y6PM0Gdou4dgQtQ+UGPM0Ed4uERM=tQFyvPM05dTw5PM=ERTuHQSKK"))
        assertEquals("aajaa{", decodeD("WVzlWVz7"))
        assertEquals(decodeD("Q+y6PM0Gdo"), decodeD("Q+y6\nPM0G!do"))
    }
}
