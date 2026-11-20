package dev.dcrk

import kotlin.js.Promise
import kotlin.js.jsTypeOf

internal fun savedNonmember(): dynamic {
    val storage: dynamic = js("localStorage")
    return props("nick" to (storage.getItem("nonmember_nick") ?: ""), "pw" to (storage.getItem("nonmember_pw") ?: ""))
}

internal fun saveNonmember(nick: String, password: String) {
    val storage: dynamic = js("localStorage")
    storage.setItem("nonmember_nick", nick)
    if (password.isNotEmpty()) storage.setItem("nonmember_pw", password)
}

internal fun fcmToken(): Promise<dynamic> = jsPromise {
    fun result(request: dynamic): Promise<dynamic> = Promise { resolve, reject ->
        val success: () -> Unit = { resolve(request.result) }
        val failure: () -> Unit = { reject(request.error) }
        request.onsuccess = success
        request.onerror = failure
    }
    try {
        if (js("globalThis.Notification")?.permission != "granted") return@jsPromise undefinedValue()
        val open = js("indexedDB").open("firebase-messaging-database")
        val abort: () -> Unit = { open.transaction?.abort() }
        open.onupgradeneeded = abort
        val db = awaitJs(result(open))
        try {
            val store = db.transaction("firebase-messaging-store").objectStore("firebase-messaging-store")
            val data = awaitJs(result(store.get("1:847263058171:web:f8932168a3b61afbeac85e")))
            if (record(data) && jsTypeOf(data.token) == "string" && truthy(data.token)) data.token else undefinedValue()
        } finally {
            db.close()
        }
    } catch (_: Throwable) {
        undefinedValue()
    }
}
