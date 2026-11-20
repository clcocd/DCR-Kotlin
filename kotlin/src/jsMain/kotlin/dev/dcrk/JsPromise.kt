package dev.dcrk

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

internal fun <T> jsPromise(block: suspend () -> T): Promise<T> = Promise { resolve, reject ->
    block.startCoroutine(Continuation(EmptyCoroutineContext) { result ->
        result.fold({ resolve(it) }, { reject(it) })
    })
}

internal suspend fun <T> Promise<T>.awaitResult(): T = suspendCoroutine { continuation ->
    then({ continuation.resume(it) }, { continuation.resumeWithException(it) })
}


internal suspend fun awaitJs(promise: dynamic): dynamic = Promise.resolve<dynamic>(promise).awaitResult()
