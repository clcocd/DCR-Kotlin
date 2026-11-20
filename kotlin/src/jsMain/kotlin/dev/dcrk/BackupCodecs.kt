package dev.dcrk

internal fun gzipBackup(text: String): dynamic {
    val blob = js("new Blob([text])")
    val compressor = js("new CompressionStream('gzip')")
    val compressed = blob.stream().pipeThrough(compressor)
    return js("new Response(compressed)").bytes()
}

internal fun gunzipBackup(bytes: dynamic): dynamic {
    val blob = js("new Blob([bytes])")
    val decompressor = js("new DecompressionStream('gzip')")
    val text = blob.stream().pipeThrough(decompressor)
    return js("new Response(text)").text()
}

internal fun createHttpClient(ky: dynamic): dynamic {
    val scope: dynamic = js("globalThis")
    val page = scope.content
    val nativeFetch = scope.fetch
    val fetcher = if (truthy(page)) {
        val fetch: (dynamic, dynamic) -> dynamic = { input, init ->
            kotlin.js.Promise<dynamic> { resolve, reject -> page.fetch(input, init).then(resolve, reject) }
        }
        fetch
    } else nativeFetch.bind(scope)
    val http = ky.create(props("timeout" to false, "totalTimeout" to false, "fetch" to fetcher, "retry" to 0))
    val ajax = http.extend(props("headers" to props("X-Requested-With" to "XMLHttpRequest")))
    return props("ajax" to ajax, "http" to http)
}
