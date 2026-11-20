package dev.dcrk

import kotlin.js.Date
import kotlin.js.Promise

private const val GALLOG_API = "https://gall.dcinside.com/api/gallog_user_layer/gallog_content_reple"

private fun trimGallogRatios(all: dynamic): dynamic = fromEntries(entriesOf(all).sortedByDescending {
    it[1].date as? Double ?: Double.NEGATIVE_INFINITY
}.take(500).toTypedArray())

internal fun createGallog(s: dynamic): dynamic {
    val cache = s.cache
    val listeners = mutableSetOf<(String, dynamic) -> Unit>()
    val console: dynamic = js("console")

    fun deliver(uid: String, activity: dynamic) {
        for (listener in listeners.toList()) {
            try {
                listener(uid, activity)
            } catch (error: Throwable) {
                console.error(error)
            }
        }
    }
    suspend fun fetch(uid: String): dynamic {
        val body = awaitJs(s.csrfBody(props("user_id" to uid)))
        val response = awaitJs(s.ajax.post(GALLOG_API, props("body" to body)).text()) as String
        val values = response.split(',')
        val number: dynamic = js("Number")
        if (values.size < 2) return undefinedValue()
        val article = number(values[0]) as Double
        val comment = number(values[1]) as Double
        if (!article.isFinite() || !comment.isFinite()) return undefinedValue()
        return props("article" to article, "comment" to comment)
    }
    fun observe(listener: (String, dynamic) -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }
    fun remember(uid: String, request: dynamic): dynamic {
        cache.set(uid, request)
        val resolve: (dynamic) -> Unit = { if (!truthy(it) && cache.get(uid) === request) cache.delete(uid) }
        request.then(resolve)
        return request
    }
    suspend fun claim(uid: String): dynamic = try {
        awaitJs(s.sendMessage("refresher:gallogClaim", uid))
    } catch (_: Throwable) {
        props("mine" to true)
    }
    fun fetchAndShare(uid: String): Promise<dynamic> = jsPromise {
        val activity = try { fetch(uid) } catch (_: Throwable) { undefinedValue() }
        val ignore: () -> Unit = {}
        s.sendMessage("refresher:gallogResult", props("uid" to uid, "activity" to activity)).catch(ignore)
        if (truthy(activity)) deliver(uid, activity)
        activity
    }
    fun get(uid: String): dynamic = cache.get(uid) ?: remember(uid, jsPromise {
        val shared = claim(uid)
        if (truthy(shared.cached)) {
            deliver(uid, shared.cached)
            shared.cached
        } else awaitJs(fetchAndShare(uid))
    })
    fun getUnlessBusy(uid: String): dynamic = cache.get(uid) ?: jsPromise {
        val shared = claim(uid)
        if (truthy(shared.cached)) {
            deliver(uid, shared.cached)
            awaitJs(remember(uid, Promise.resolve<dynamic>(shared.cached)))
        } else if (shared.mine != true) null
        else awaitJs(remember(uid, fetchAndShare(uid)))
    }
    return props("trimRatios" to ::trimGallogRatios, "onGallogActivity" to ::observe,
        "getGallogActivity" to ::get, "getGallogActivityUnlessBusy" to ::getUnlessBusy)
}

internal fun createGallogBackground(s: dynamic): () -> Unit {
    val shared = s.cache
    val inflight = mutableMapOf<String, Double>()
    var pending = mutableMapOf<String, dynamic>()
    var flushTimer: dynamic = undefinedValue()
    val assign: dynamic = js("Object.assign")
    val schedule: dynamic = js("setTimeout")

    fun flush() {
        if (pending.isEmpty()) return
        val batch = pending
        pending = mutableMapOf()
        val date = Date.now()
        jsPromise {
            val current = awaitJs(s.ratioStorage.getValue())
            val entries = batch.entries.map { (uid, activity) ->
                arrayOf<dynamic>(uid, assign(props(), activity, props("date" to date)))
            }.toTypedArray()
            val all = assign(props(), current.ratio, fromEntries(entries))
            awaitJs(s.ratioStorage.setValue(props("ratio" to trimGallogRatios(all))))
        }
    }
    fun claim(msg: dynamic): dynamic {
        val uid = msg.data as String
        val cached = shared.get(uid)
        if (truthy(cached)) return props("mine" to false, "cached" to cached)
        val mine = Date.now() - (inflight[uid] ?: 0.0) >= 5000
        if (mine) inflight[uid] = Date.now()
        return props("mine" to mine)
    }
    fun result(msg: dynamic) {
        val uid = msg.data.uid as String
        val activity = msg.data.activity
        if (truthy(activity)) {
            shared.set(uid, activity)
            pending[uid] = activity
            if (flushTimer === undefinedValue()) {
                val task: () -> Unit = {
                    flushTimer = undefinedValue()
                    flush()
                }
                flushTimer = schedule(task, 2000)
            }
        }
        inflight.remove(uid)
    }
    return {
        s.onMessage("refresher:gallogClaim", ::claim)
        s.onMessage("refresher:gallogResult", ::result)
    }
}
