package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.jsTypeOf
import kotlin.math.ceil
import kotlin.math.max

/** 디시콘 패키지 캐시 용량(100개)과 유효시간(10분)은 Kotlin에서 결정한다. */
internal fun createDcconPackageCache(factory: (dynamic) -> dynamic): dynamic =
    factory(props("maxSize" to 100, "maxAge" to 600_000))

internal fun createPreviewRequests(s: dynamic): dynamic {
    fun viewUrl(link: String, gallery: String, id: String): String = "${s.urls.base}${s.galleryPath(link)}board/view/?id=$gallery&no=$id"
    fun fetchPost(pre: dynamic, signal: dynamic): Promise<dynamic> = jsPromise {
        val url = viewUrl(pre.link as String, pre.gallery as String, pre.id as String)
        val response = awaitJs(s.http.get(url, props("signal" to signal)).text())
        s.parsePostInfo(response) ?: error("게시글 페이지가 아닙니다.")
    }
    fun fetchComments(pre: dynamic, post: dynamic, signal: dynamic, skip: dynamic): Promise<dynamic> = jsPromise {
        val body = awaitJs(s.dcBody(pre.link, props("id" to pre.gallery, "no" to pre.id,
            "cmt_id" to (post.commentId ?: pre.gallery), "cmt_no" to (post.commentNo ?: pre.id), "e_s_n_o" to (post.esno ?: ""))))
        val abort = js("new AbortController()")
        val any = js("AbortSignal")
        val pageSignal = any.any(arrayOf<dynamic>(signal, abort.signal))
        val promise: dynamic = js("Promise")
        val number: dynamic = js("Number")
        val stringify: dynamic = js("String")
        val pager = Regex("""viewComments\((\d+)""")
        var failure: dynamic = null
        fun page(pageNumber: Int): dynamic {
            val copy = js("new URLSearchParams(body)")
            copy.set("comment_page", pageNumber.toString())
            val parse: (dynamic) -> dynamic = { response ->
                if (!record(response) || (response.comments !== null && js("Array.isArray")(response.comments) != true)) error("댓글 목록이 아닙니다.")
                val filtered = (response.comments as Array<dynamic>?)?.filter { record(it) && jsTypeOf(it.no) == "string" }
                props("comments" to (filtered?.toTypedArray() ?: emptyArray<dynamic>()),
                    "pagination" to if (jsTypeOf(response.pagination) == "string") response.pagination else null,
                    "allowReply" to (stringify(response.allow_reply) != "0"))
            }
            val onFailure: (dynamic) -> dynamic = { reason ->
                if (failure == null) failure = reason
                abort.abort()
                throw failure
            }
            return s.ajax.post(s.urls.comments, props("body" to copy, "signal" to pageSignal)).json().then(parse).catch(onFailure)
        }
        fun lastPage(result: dynamic): Int =
            pager.findAll((result.pagination ?: "") as String).mapNotNull { it.groupValues[1].toIntOrNull() }.maxOrNull()?.coerceAtLeast(1) ?: 1
        fun pageList(start: Int, last: Int): dynamic =
            promise.all((start..last).map(::page).toTypedArray())
        fun merge(first: dynamic, more: Array<dynamic>): dynamic {
            val byNumber = mutableMapOf<String, dynamic>()
            for (item in arrayOf<dynamic>(first) + more) {
                for (comment in item.comments as Array<dynamic>) byNumber[comment.no as String] = comment
            }
            val sorted = byNumber.values.sortedWith { a, b ->
                val delta = (number(a.no) as Double) - (number(b.no) as Double)
                if (delta < 0) -1 else if (delta > 0) 1 else 0
            }.toTypedArray()
            return props("list" to sorted, "allowReply" to first.allowReply, "truncated" to (lastPage(first) > 10))
        }
        if (skip !== undefinedValue()) {
            val first = awaitJs(page(1))
            val newest = (first.comments as Array<dynamic>).fold(0.0) { current, item ->
                val value = number(item.no) as Double
                max(current, if (value.isNaN()) 0.0 else value)
            }
            val previous = number(skip) as Double
            if (newest <= (if (previous.isNaN()) 0.0 else previous)) return@jsPromise undefinedValue()
            val rest = awaitJs(pageList(2, lastPage(first).coerceAtMost(10))) as Array<dynamic>
            return@jsPromise merge(first, rest)
        }
        val guessed = ceil((pre.commentCount as Double) / 100).toInt().coerceIn(1, 10)
        val firstPage = page(1)
        val early = pageList(2, guessed)
        val ignore: () -> Unit = {}
        early.catch(ignore)
        val first = awaitJs(firstPage)
        val rest = pageList(guessed + 1, lastPage(first).coerceAtMost(10))
        val pages = awaitJs(promise.all(arrayOf<dynamic>(early, rest)))
        merge(first, (pages[0] as Array<dynamic>) + (pages[1] as Array<dynamic>))
    }

    fun vote(pre: dynamic, post: dynamic, mode: String, code: dynamic): Promise<dynamic> = jsPromise {
        val cookies: dynamic = js("cookieStore")
        val suffix = if (mode == "U") "" else "_down"
        awaitJs(cookies.set(props("name" to "${pre.gallery}${pre.id}_Firstcheck$suffix", "value" to "Y",
            "expires" to Date.now() + 3 * 3_600_000, "path" to "/", "domain" to "dcinside.com")))
        val fields = props("id" to pre.gallery, "no" to pre.id, "mode" to mode, "code_recommend" to (code ?: post.recommendCode ?: ""),
            "link_id" to pre.gallery, "v_cur_t" to if (truthy(post.v_cur_t)) post.v_cur_t else undefinedValue())
        if (truthy(post.randomParam)) fields[post.randomParam.name] = post.randomParam.value
        val body = awaitJs(s.dcBody(pre.link, fields))
        val text = awaitJs(s.ajax.post(s.urls.vote, props("body" to body)).text())
        val response = s.submitResult(text)
        if (response.result == "true") props("success" to true, "counts" to response.message, "fixedCounts" to response.detail)
        else props("success" to false, "message" to s.resultMessage(response))
    }
    return props("viewUrl" to ::viewUrl, "fetchPost" to ::fetchPost, "fetchComments" to ::fetchComments, "vote" to ::vote)
}
