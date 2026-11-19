package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentCoreTest {
    @Test
    fun comparesCommentFieldsWithoutStringifying() {
        val first: dynamic = js("""([{no:"1",is_delete:"0"},{no:"2",is_delete:"0"}])""")
        val equal: dynamic = js("""([{no:"1",is_delete:"0"},{no:"2",is_delete:"0"}])""")
        val extra: dynamic = js("""([{no:"1",is_delete:"0",extra:"x"},{no:"2",is_delete:"0"}])""")
        val reordered: dynamic = js("""([{no:"2",is_delete:"0"},{no:"1",is_delete:"0"}])""")
        assertTrue(sameComments(first, equal))
        assertTrue(sameComments(js("[]"), js("[]")))
        assertFalse(sameComments(first, extra))
        assertFalse(sameComments(first, reordered))
        assertFalse(sameComments(first, js("[]")))
    }

    @Test
    fun splitsMergedDcconsAndNormalizesStatus() {
        val raw = "<img class=\"written_dccon\" src=\"a\"img class=\"written_dccon\" src=\"b\">"
        val expected = "<img class=\"written_dccon\" src=\"a\"><img class=\"written_dccon\" src=\"b\">"
        assertEquals(expected, preprocessCommentMemo(raw))
        assertEquals("data-dcconoverstatus=\"true\"",
            preprocessCommentMemo("data-dcconoverstatus=\"false\""))
        assertEquals("그냥 댓글", preprocessCommentMemo("그냥 댓글"))
    }

    @Test
    fun extractsOnlyDcinsideVoiceUrls() {
        val plain = extractVoice("hello")
        assertEquals(null, plain)

        val voice = extractVoice("voice/a.mp3?x=1&y=2@^dc^@들어 봐")
        assertEquals("들어 봐", voice.memo as String)
        assertEquals("https://vr.dcinside.com/voice/a.mp3?x=1&y=2", voice.voice.src as String)
        assertFalse(voice.voice.iframe as Boolean)

        val iframe = extractVoice("<iframe src=\"https://vr.dcinside.com/player?no=1\"></iframe>@^dc^@글")
        assertEquals("글", iframe.memo as String)
        assertTrue(iframe.voice.iframe as Boolean)

        val unsafe = extractVoice("<iframe src=\"https://evil.example/\"></iframe>@^dc^@글")
        assertEquals("글", unsafe.memo as String)
        assertEquals(null, unsafe.voice)
    }
}
