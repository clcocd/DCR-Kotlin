package dev.dcrk

private const val MENU_PREFIX = "imagesearch:"

fun createImageSearch(s: dynamic): dynamic {
    val browser = s.browser
    val ids = s.engineIds as Array<String>
    val menus = browser.contextMenus
    val listen: () -> Unit = {
        val search: (dynamic, dynamic) -> dynamic = { info, tab -> jsPromise handle@{
            val id = js("String")(info.menuItemId) as String
            val source = info.srcUrl as String?
            if (!id.startsWith(MENU_PREFIX) || source.isNullOrEmpty() || !source.contains("viewimage.php")) return@handle undefinedValue()
            val engine = id.removePrefix(MENU_PREFIX)
            if (engine !in ids) return@handle undefinedValue()
            val url = BrowserUrl(source)
            val address = url.asDynamic()
            address.host = "image.dcinside.com"
            address.pathname = "/dccon.php"
            val finalUrl = (s.engines[engine].url as String) + (js("encodeURIComponent")(url.href) as String)
            val tabId = tab?.id as Int?
            val args = if (tabId != null && tabId >= 0) props(
                "url" to finalUrl, "index" to (tab.index as Int) + 1, "openerTabId" to tabId, "windowId" to tab.windowId,
            ) else props("url" to finalUrl)
            awaitJs(browser.tabs.create(args))
            undefinedValue()
        } }
        menus.onClicked.addListener(search)
    }
    val apply: (dynamic) -> dynamic = { state -> jsPromise {
        val promise: dynamic = js("Promise")
        awaitJs(promise.all(ids.map { menus.remove(MENU_PREFIX + it).catch { _: dynamic -> undefinedValue() } }.toTypedArray()))
        if (state.enabled == true) {
            for (engine in ids) {
                if (!truthy(state.settings[engine])) continue
                menus.create(props("id" to MENU_PREFIX + engine, "title" to "${s.engines[engine].name} 검색",
                    "contexts" to arrayOf("image"), "targetUrlPatterns" to s.patterns))
            }
        }
        undefinedValue()
    } }
    return props("listen" to listen, "apply" to apply)
}
