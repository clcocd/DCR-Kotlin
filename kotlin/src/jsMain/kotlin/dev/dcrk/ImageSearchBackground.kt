package dev.dcrk

private const val IMAGE_SEARCH_MENU_PREFIX = "imagesearch:"

/** 디시 본문 이미지 경로를 디시콘 호스트로 바꿔 각 엔진의 URL 검색 주소를 만든다. */
fun imageSearchUrlKotlin(engine: String, src: String, ids: dynamic, engines: dynamic): String? {
    if (!(ids.includes(engine) as Boolean) || !src.contains("viewimage.php")) return null
    val image = BrowserUrl(src)
    image.host = "image.dcinside.com"
    image.pathname = "/dccon.php"
    return (engines[engine].url as String) + (js("encodeURIComponent")(image.href) as String)
}

/**
 * 이미지 역검색의 브라우저 컨텍스트 메뉴를 Kotlin/JS에서 관리한다.
 * browser API와 엔진 메타는 원본 TS 경계를 통해 전달받아 그대로 사용한다.
 */
fun imageSearchBackgroundImplementation(
    ids: dynamic,
    engines: dynamic,
    urlPatterns: dynamic,
    registerClicked: ((dynamic, dynamic) -> dynamic) -> Unit,
    removeMenu: (String) -> dynamic,
    createMenu: (dynamic) -> dynamic,
    createTab: (dynamic) -> dynamic
): dynamic {
    val impl: dynamic = js("({})")

    val listen: () -> Unit = {
        val clicked: (dynamic, dynamic) -> dynamic = { info, tab ->
            val id = js("String")(info.menuItemId) as String
            val src: dynamic = info.srcUrl
            val url = if (id.startsWith(IMAGE_SEARCH_MENU_PREFIX) && src != null)
                imageSearchUrlKotlin(id.removePrefix(IMAGE_SEARCH_MENU_PREFIX), src as String, ids, engines)
            else null

            if (url == null) {
                js("Promise.resolve")()
            } else {
                val props: dynamic = js("({})")
                props.url = url
                if (tab != null && tab.id != null && (tab.id as Int) >= 0) {
                    props.index = (tab.index as Int) + 1
                    props.openerTabId = tab.id
                    props.windowId = tab.windowId
                }
                createTab(props)
            }
        }
        registerClicked(clicked)
    }
    impl.listen = listen

    val apply: (dynamic) -> dynamic = { state ->
        // 다른 모듈의 메뉴는 삭제하지 않는다. 없는 메뉴를 지우려는 거절도 원본처럼 무시한다.
        val removes: dynamic = js("[]")
        for (index in 0 until (ids.length as Int)) {
            val id = ids[index] as String
            val ignore: (dynamic) -> Unit = { _ -> }
            removes.push(removeMenu(IMAGE_SEARCH_MENU_PREFIX + id).catch(ignore))
        }

        val createEnabled: (dynamic) -> Unit = { _ ->
            if (state.enabled as Boolean) {
                for (index in 0 until (ids.length as Int)) {
                    val id = ids[index] as String
                    if (state.settings[id] == true) {
                        val menu: dynamic = js("({})")
                        menu.id = IMAGE_SEARCH_MENU_PREFIX + id
                        menu.title = (engines[id].name as String) + " 검색"
                        menu.contexts = js("(['image'])")
                        menu.targetUrlPatterns = urlPatterns
                        createMenu(menu)
                    }
                }
            }
        }
        js("Promise.all")(removes).then(createEnabled)
    }
    impl.apply = apply
    return impl
}
