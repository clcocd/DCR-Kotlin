package dev.dcrk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserResponseTest {
    @Test
    fun nickIconsHaveExactNames() {
        assertEquals("FIXED", nickType("https://nstatic.dcinside.com/fix_managernik.gif"))
        assertEquals("FIXED", nickType("nftmdcon_fix.png"))
        assertEquals("HALF_FIXED", nickType("/nik.gif"))
        assertEquals("HALF_FIXED", nickType("bestcon.png"))
        assertEquals("UNFIXED", nickType("https://nstatic.dcinside.com/unknown.gif"))
        assertEquals("UNFIXED", nickType("/FIX_MANAGERNIK.GIF"))
        assertEquals("UNFIXED", nickType(""))
    }

    @Test
    fun responseShapeAndMessages() {
        val success = submitResult(" 12345 ")
        assertEquals("12345", success.result as String)
        assertEquals(null, success.message)

        val failure = submitResult("false||nomember||회원만 쓸 수 있습니다.")
        assertEquals("false", failure.result as String)
        assertEquals("nomember", failure.message as String)
        assertEquals("회원만 쓸 수 있습니다.", failure.detail as String)
        assertEquals("회원만 쓸 수 있습니다.", resultMessage(failure.message, failure.detail))
        assertEquals("도배", resultMessage("도배", null))
        assertNull(resultMessage("", null))
        assertNull(resultMessage("nomember", null))

        val html = submitResult("  <html>||script</html> ")
        assertEquals("<html>||script</html>", html.result as String)
        assertEquals(null, html.message)

        val emptyDetail = submitResult("false||captcha||")
        assertEquals("", emptyDetail.detail as String)
        val extra = submitResult("a||b||c||d")
        assertEquals("c", extra.detail as String)
    }
}
