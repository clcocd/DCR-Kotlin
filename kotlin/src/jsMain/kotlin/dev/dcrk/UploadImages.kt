package dev.dcrk

import kotlin.js.jsTypeOf

private data class UploadOptions(val webp: Boolean, val quality: Double, val rename: Boolean, val concurrency: Int)
private data class PendingUpload(val files: Array<dynamic>, val options: UploadOptions)
private val keepImageTypes = setOf("image/gif", "image/webp", "image/avif", "image/apng")

fun hookUploads(limit: (Int) -> dynamic) {
    val window: dynamic = js("window")
    val doc: dynamic = js("document")
    val console: dynamic = js("console")
    val hooked = js("Symbol.for('refresher.uploads')")
    if (truthy(window[hooked])) return
    window[hooked] = true

    fun readOptions(): UploadOptions? = try {
        val raw = doc.documentElement.dataset[UPLOAD_OPTIONS_KEY]
        val data = if (truthy(raw)) js("JSON.parse")(raw) else null
        val number: dynamic = js("Number")
        if (data != null && jsTypeOf(data) == "object" && jsTypeOf(data.webp) == "boolean" && jsTypeOf(data.rename) == "boolean" &&
            number.isFinite(data.quality) == true && number.isInteger(data.concurrency) == true && (data.concurrency as Double) >= 1
        ) UploadOptions(data.webp as Boolean, data.quality as Double, data.rename as Boolean, data.concurrency as Int)
        else null
    } catch (_: Throwable) {
        null
    }

    suspend fun isApng(file: dynamic): Boolean {
        if (file.type != "image/png") return false
        var offset = 8.0
        while (offset + 8 <= (file.size as Double)) {
            val buffer = awaitJs(file.slice(offset, offset + 8).arrayBuffer())
            val header: dynamic = js("new DataView(buffer)")
            val chunk = (4..7).map { (header.getUint8(it) as Int).toChar() }.joinToString("")
            if (chunk == "acTL") return true
            if (chunk == "IDAT" || chunk == "IEND") return false
            offset += 12 + (header.getUint32(0) as Double)
        }
        return false
    }

    val done: dynamic = js("new WeakSet()")
    fun needsWork(file: dynamic, options: UploadOptions): Boolean =
        done.has(file) == false && (file.type as String).startsWith("image/") &&
            (options.rename || (options.webp && (file.type as String) !in keepImageTypes))

    fun named(blob: dynamic, original: dynamic, extension: String, rename: Boolean): dynamic {
        val stem = if (rename) js("crypto.randomUUID()") as String
        else (original.name as String).replace(Regex("""\.[^.]*$"""), "")
        val name = "$stem.$extension"
        return js("new File([blob], name, {type: blob.type})")
    }

    suspend fun toWebp(file: dynamic, quality: Double): dynamic {
        val bitmap = awaitJs(js("createImageBitmap")(file))
        val canvas: dynamic = js("new OffscreenCanvas(bitmap.width, bitmap.height)")
        canvas.getContext("2d")?.drawImage(bitmap, 0, 0)
        bitmap.close()
        val output = awaitJs(canvas.convertToBlob(props("type" to "image/webp", "quality" to quality)))
        return if (output.type == "image/webp") output else null
    }

    suspend fun convert(file: dynamic, options: UploadOptions): dynamic {
        if (!needsWork(file, options)) return file
        if (options.webp && (file.type as String) !in keepImageTypes) {
            try {
                val webp = if (isApng(file)) null else toWebp(file, options.quality)
                if (webp != null && (webp.size as Double) < (file.size as Double)) return named(webp, file, "webp", options.rename)
            } catch (failure: Throwable) {
                console.error(failure)
            }
        }
        val ext = Regex("""\.([^.]+)$""").find(file.name as String)?.groupValues?.get(1) ?: (file.type as String).drop(6)
        return if (options.rename) named(file, file, ext.lowercase(), true) else file
    }

    suspend fun canvasReliable(): Boolean {
        suspend fun encode(): String {
            val canvas: dynamic = js("new OffscreenCanvas(1, 1)")
            val ctx = canvas.getContext("2d")
            if (ctx != null) {
                ctx.fillStyle = "#f00"
                ctx.fillRect(0, 0, 1, 1)
            }
            val probe = awaitJs(canvas.convertToBlob(props("type" to "image/webp")))
            val buffer = awaitJs(probe.arrayBuffer())
            val bytes: dynamic = js("new Uint8Array(buffer)")
            return bytes.join() as String
        }
        return try { encode() == encode() } catch (failure: Throwable) { console.error(failure); false }
    }

    fun makeTransfer(files: Array<dynamic>, markDone: Boolean): dynamic {
        val transfer: dynamic = js("new DataTransfer()")
        for (file in files) {
            if (markDone) done.add(file)
            transfer.items.add(file)
        }
        return transfer
    }

    fun convertAll(upload: PendingUpload) = jsPromise {
        val config = upload.options
        val options = if (config.webp && !canvasReliable()) config.copy(webp = false) else config
        val build: (dynamic) -> dynamic = { file -> jsPromise { convert(file, options) } }
        makeTransfer(awaitJs(limit(options.concurrency).map(upload.files, build)) as Array<dynamic>, true)
    }

    fun pending(files: dynamic): PendingUpload? {
        val config = readOptions() ?: return null
        val list = jsArray(files ?: emptyArray<dynamic>())
        return if (list.any { needsWork(it, config) }) PendingUpload(list, config) else null
    }

    fun sendChange(input: dynamic, transfer: dynamic) {
        input.files = transfer.files
        input.dispatchEvent(js("new Event('change', {bubbles: true})"))
    }

    var forwardingOriginal = false
    fun sendOriginal(upload: PendingUpload, send: (dynamic) -> Unit) {
        forwardingOriginal = true
        try {
            send(makeTransfer(upload.files, false))
        } finally {
            forwardingOriginal = false
        }
    }

    var picking = 0
    val change: (dynamic) -> Unit = change@{ event ->
        if (forwardingOriginal) return@change
        val input = event.target
        if (!(js("input instanceof HTMLInputElement") as Boolean) || input.type != "file") return@change
        val upload = pending(input.files) ?: return@change
        event.stopImmediatePropagation()
        val current = ++picking
        convertAll(upload).then<Unit>(
            { transfer: dynamic -> if (current == picking) sendChange(input, transfer) },
            { _ -> if (current == picking) sendOriginal(upload) { sendChange(input, it) } },
        )
    }

    fun intercept(event: dynamic, transfer: dynamic) {
        if (forwardingOriginal) return
        val target = event.target
        val dropzone = if (js("target instanceof Element") as Boolean) target.closest(".note-editor")?.querySelector(".note-dropzone") else null
        val input = if (dropzone == null) doc.querySelector("#fileupload input[type=file]") else null
        if (dropzone == null && input == null) return
        val upload = pending(transfer?.files) ?: return
        event.preventDefault()
        event.stopImmediatePropagation()
        fun send(transformed: dynamic) {
            if (dropzone != null) dropzone.dispatchEvent(js("new DragEvent('drop', {dataTransfer: transformed, bubbles: true, cancelable: true})"))
            else if (input != null) sendChange(input, transformed)
        }
        convertAll(upload).then<Unit>({ send(it) }, { sendOriginal(upload, ::send) })
    }
    window.addEventListener("change", change, true)
    window.addEventListener("paste", { event: dynamic -> intercept(event, event.clipboardData) }, true)
    window.addEventListener("drop", { event: dynamic -> intercept(event, event.dataTransfer) }, true)
}
