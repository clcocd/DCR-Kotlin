package dev.dcrk

private const val ORIGIN_CLASS = "refresherImageOrigin"
private const val SEARCH_CLASS = "refresherTitleSearch"
private val IMAGE_ID_KEY = intArrayOf(0x4d, 0xdd, 0xb0, 0x46, 0x85, 0xb2, 0x58, 0xc6,
    0x0e, 0xdf, 0xb6, 0xd5, 0x76, 0xb1, 0x44, 0x5d)
private val DOWNLOADED_NAME = Regex("^([a-z0-9_]+)-\\d{8}-\\d{6}-\\d{3}(?:-resize)?\\.\\w+$")

/** dcimg 이미지 ID를 원본과 같은 XOR 규칙으로 업로드 갤러리 ID로 복원한다. */
private fun manageUploadedGallery(src: String): String? {
    val url: dynamic = js("URL.parse")(src)
    if (url == null || !(url.hostname as String).startsWith("dcimg")) return null
    val id: dynamic = url.searchParams.get("id")
    if (id == null || id == "") return null

    val hex = id as String
    val gallery = StringBuilder()
    var offset = 0
    var index = 0
    while (offset + 2 <= hex.length) {
        // parseInt+비트 XOR는 원본처럼 잘못된 16진수를 0으로 취급한다.
        val number: dynamic = js("parseInt")(hex.substring(offset, offset + 2), 16)
        val value = (number as Int) xor IMAGE_ID_KEY[index % IMAGE_ID_KEY.size]
        gallery.append(value.toChar())
        offset += 2
        index++
    }
    return gallery.toString()
}

/**
 * 관리 모듈의 글 보기 도구 UI는 Kotlin/JS에서 구성한다.
 * 기존 TS의 검색 메시지/토스트 및 NUMBERED_SOURCE 판정은 외부 API로 재사용한다.
 */
fun manageViewImplementation(
    numberedSource: (String) -> Boolean,
    searchPosts: (String) -> dynamic,
    postSearchUrl: (String) -> String,
    showToast: (String, String, dynamic, dynamic) -> Unit,
    pageGallery: String?,
    pagePostNo: String?
): dynamic {
    // Node 배경/테스트가 모듈을 import할 때 document가 없어도 실패하지 않는다.
    val doc: dynamic = js("globalThis.document")
    val api: dynamic = js("({})")

    val remove: () -> Unit = {
        val items: dynamic = doc.querySelectorAll(".$ORIGIN_CLASS, .$SEARCH_CLASS")
        for (i in 0 until (items.length as Int)) items[i].remove()
    }

    val showImageOrigin: (dynamic) -> Unit = { head ->
        val gallery = pageGallery ?: ""
        val own = setOf(gallery.take(16), ("mi\$$gallery").take(16))
        val foreign = linkedMapOf<String, MutableList<String>>()
        val record: (String?, String) -> Unit = { source, label ->
            if (source != null && !own.contains(source.take(16))) {
                foreign.getOrPut(source) { mutableListOf() }.add(label)
            }
        }

        val media: dynamic = doc.querySelectorAll(
            ".write_div :is(img:not(.written_dccon, .og-img), video)"
        )
        var number = 0
        for (i in 0 until (media.length as Int)) {
            val element: dynamic = media[i]
            val source: dynamic = when {
                element.dataset.original != null -> element.dataset.original
                element.dataset.src != null -> element.dataset.src
                else -> element.src
            }
            if (source != null && numberedSource(source as String)) {
                number++
                record(manageUploadedGallery(source as String), "${number}번")
            }
        }

        val attachments: dynamic = doc.querySelectorAll(".appending_file li")
        for (i in 0 until (attachments.length as Int)) {
            val text: dynamic = attachments[i].textContent
            val fileName = if (text == null) "" else (text as String).trim()
            val source = DOWNLOADED_NAME.matchEntire(fileName)?.groupValues?.get(1)
            record(source, "첨부 ${i + 1}번")
        }

        if (foreign.isNotEmpty()) {
            val text = foreign.entries.joinToString(", ") { (source, labels) ->
                "$source(${labels.joinToString(", ")})"
            }
            val label: dynamic = doc.createElement("p")
            label.className = ORIGIN_CLASS
            label.textContent = "다른 갤러리의 이미지: $text"
            head.prepend(label)
        }
    }

    val addTitleSearch: (dynamic) -> Unit = { head ->
        val title = textWithoutScripts(head.querySelector(".title_subject"))
        val counts: dynamic = head.querySelector(".gall_writer .fr")
        if (title.isNotEmpty() && counts != null) {
            val button: dynamic = doc.createElement("button")
            button.type = "button"
            button.className = SEARCH_CLASS
            button.textContent = "같은 제목 찾기"

            val click: (dynamic) -> Unit = {
                button.disabled = true
                val openResults: dynamic = js("({})")
                openResults.label = "검색 결과 보기"
                openResults.run = { js("window").open(postSearchUrl(title), "_blank", "noopener") }

                val success: (dynamic) -> Unit = { html ->
                    val resultDoc: dynamic = js("new DOMParser()").parseFromString(html, "text/html")
                    val items: dynamic = resultDoc.querySelectorAll("ul.sch_result_list > li")
                    val galleries = mutableListOf<String>()
                    for (i in 0 until (items.length as Int)) {
                        val item: dynamic = items[i]
                        val link: dynamic = item.querySelector(".tit_txt")
                        val href: dynamic = if (link != null) link.getAttribute("href") else ""
                        val url: dynamic = js("URL.parse")(href ?: "")
                        val params: dynamic = if (url != null) url.searchParams else null
                        val self = params != null && params.get("id") == pageGallery &&
                            params.get("no") == pagePostNo
                        val linkText: dynamic = if (link != null) link.textContent else null
                        if (!self && linkText != null && (linkText as String).trim() == title) {
                            val name: dynamic = item.querySelector(".sub_txt")?.textContent
                            galleries.add(if (name == null) "" else (name as String).trim())
                        }
                    }
                    if (galleries.isEmpty()) {
                        showToast("제목이 같은 글을 찾지 못했습니다.", "info", 5000, openResults)
                    } else {
                        val unique = galleries.distinct().joinToString(", ")
                        showToast("제목이 같은 글 ${galleries.size}개: $unique", "warning", 10000, openResults)
                    }
                }
                val failure: (dynamic) -> Unit = { error ->
                    js("console").error("Title search failed:", error)
                    showToast("통합검색을 불러오지 못했습니다.", "error", js("undefined"), js("undefined"))
                }
                val finished: () -> Unit = { button.disabled = false }
                searchPosts(title).then(success).catch(failure).finally(finished)
            }
            button.addEventListener("click", click)
            counts.prepend(button)
        }
    }

    val render: (dynamic) -> Unit = { show ->
        remove()
        val head: dynamic = doc.querySelector(".gallview_head")
        if (head != null) {
            if ((show.imageOrigin as Boolean) && pageGallery != "dcbest") showImageOrigin(head)
            if (show.titleSearch as Boolean) addTitleSearch(head)
        }
    }

    api.remove = remove
    api.render = render
    return api
}
