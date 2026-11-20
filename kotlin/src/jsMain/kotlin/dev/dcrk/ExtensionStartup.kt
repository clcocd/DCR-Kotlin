package dev.dcrk

import kotlin.js.Promise

internal fun startContentScript(ctx: dynamic, s: dynamic): Promise<dynamic> = jsPromise {
    val command: (dynamic) -> Unit = { s.runShortcut(it.data) }
    val pageAction: (dynamic) -> dynamic = {
        s.runPageToggle(it.data)
        s.pageState()
    }
    ctx.onInvalidated(s.onMessage("refresher:executeShortcut", command))
    ctx.onInvalidated(s.onMessage("refresher:pageState", s.settledPageState))
    ctx.onInvalidated(s.onMessage("refresher:pageAction", pageAction))
    val cleanup: () -> Unit = {
        s.stopAll()
        s.releaseDatabase()
    }
    ctx.onInvalidated(cleanup)
    val check: () -> Unit = {}
    ctx.setInterval(check, 5000)
    s.mountOverlayWhenNeeded(ctx)
    var ready: Promise<dynamic>? = null
    val storeUsers = setOf("block", "preview", "userinfo")
    fun ensureStores(): Promise<dynamic> = ready ?: jsPromise {
        val value = awaitJs(s.storage.getItem(s.modulesKey))
        val enables = s.enablesOf(value)
        val needed = (s.metas as Array<dynamic>).any { it.id in storeUsers && s.isModuleEnabled(it, enables) == true }
        if (needed) {
            val promise: dynamic = js("Promise")
            awaitJs(promise.all(arrayOf<dynamic>(s.initBlocksStore(ctx.signal), s.initMemosStore(ctx.signal))))
        } else {
            ready = null
        }
        undefinedValue()
    }.also { ready = it }
    val prepare: dynamic = if (s.boardPage.test(s.documentUrl.pathname) == true) ::ensureStores else undefinedValue()
    awaitJs(s.loadAll(s.metas, s.loaders, ctx.signal, prepare))
    undefinedValue()
}

internal fun createBackgroundStartup(s: dynamic): () -> Unit {
    val browser = s.browser
    val runtime = browser.runtime
    val console: dynamic = js("console")

    fun toPattern(pattern: String): dynamic {
        val path = pattern.replaceFirst("//*.", "//{*.}?")
        return js("new URLPattern(path)")
    }
    val content = (s.contentMatches as Array<String>).map(::toPattern)
    val exclude = (s.contentExcludeMatches as Array<String>).map(::toPattern)
    fun contentPage(url: String): Boolean = content.any { it.test(url) == true } && exclude.none { it.test(url) == true }

    return {
        val applyModules = s.startBackgroundModules(s.modules)
        if (s.firefox == true) applyModules() else runtime.onStartup.addListener(applyModules)
        val onCommand: (String, dynamic) -> dynamic = { command, tab ->
            jsPromise {
                if (truthy(tab?.id) && truthy(tab?.url) && contentPage(tab.url as String)) {
                    val ignore: () -> Unit = {}
                    s.sendMessage("refresher:executeShortcut", command, props("tabId" to tab.id)).catch(ignore)
                }
                undefinedValue()
            }
        }
        browser.commands.onCommand.addListener(onCommand)
        s.listenPageMessages()
        val update = s.startDatabaseUpdates()
        val manual: () -> dynamic = {
            val format: (dynamic) -> dynamic = { failure ->
                console.error(failure)
                val message = s.friendlyMessage(failure) as String
                throw Error(message)
            }
            update(true).catch(format)
        }
        s.onMessage("refresher:updateDatabase", manual)
        val installed: () -> dynamic = {
            jsPromise {
                if (s.firefox != true) awaitJs(applyModules())
                awaitJs(s.fillMissingBlockIds())
                val production = s.production == true
                val download = if (production) true else {
                    val meta = awaitJs(s.dbMetaStorage().getValue())
                    !truthy(meta.version)
                }
                if (download) awaitJs(update())
                undefinedValue()
            }
        }
        runtime.onInstalled.addListener(installed)
        s.startAutoBackup()
    }
}

internal fun startBackgroundModules(modules: Array<dynamic>, s: dynamic): () -> Promise<dynamic> {
    val console: dynamic = js("console")
    val appliers = mutableListOf<() -> dynamic>()
    for (module in modules) {
        try {
            module.listen?.call(module)
        } catch (error: Throwable) {
            console.error("Background module listen failed: ${module.id}", error)
            continue
        }
        val applyState: dynamic = module.apply?.bind(module) ?: continue
        val limit = s.limit(1)
        val log: (dynamic) -> Unit = { error -> console.error(error) }
        val operation: () -> dynamic = {
            jsPromise {
                val data = awaitJs(s.readModuleStorage(arrayOf<dynamic>(module.id)))
                awaitJs(applyState(props("enabled" to s.isModuleEnabled(module, data.enables),
                    "settings" to s.settingsOf(module, data.settings.get(module.id)))))
                undefinedValue()
            }
        }
        val apply: () -> dynamic = { limit(operation).catch(log) }
        val changed: (dynamic, dynamic) -> Unit = { current, prev ->
            val id = module.id
            val now = if (current == null) undefinedValue() else current[id]
            val old = if (prev == null) undefinedValue() else prev[id]
            if (now !== old) apply()
        }
        s.storage.watch(s.modulesKey, changed)
        if (truthy(module.settings)) s.storage.watch(s.moduleSettingsKey(module.id), apply)
        appliers.add(apply)
    }
    return {
        jsPromise {
            val promise: dynamic = js("Promise")
            awaitJs(promise.all(appliers.map { it() }.toTypedArray()))
            undefinedValue()
        }
    }
}
