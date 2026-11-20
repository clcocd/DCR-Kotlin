package dev.dcrk

internal const val FILTER_SKIP_ATTR = "data-refresher-no-filter"

private class DomFilter(val scope: String, val callback: (dynamic) -> Unit)

private val domFilters: dynamic = js("new Set()")
private var observer: dynamic = null
private var union: String? = null

private fun htmlElement(node: dynamic): Boolean = js("node instanceof HTMLElement") as Boolean

private fun runFilter(filter: DomFilter, element: dynamic) {
    try {
        filter.callback(element)
    } catch (error: Throwable) {
        val console: dynamic = js("console")
        console.error("Filter \"${filter.scope}\" failed:", error)
    }
}

private fun rebuildUnion() {
    val filters = js("Array.from")(domFilters) as Array<DomFilter>
    union = if (filters.isEmpty()) null else filters.joinToString(", ") { it.scope }
}

/** 같은 mutation 묶음 안에서 추가된 조상의 자손은 다시 훑지 않는다. */
private fun filterMutations(mutations: Array<dynamic>) {
    val added: dynamic = js("new Set()")
    val covered: dynamic = js("new Map()")
    val array: dynamic = js("Array")

    fun isCovered(node: dynamic): Boolean {
        if (covered.has(node) as Boolean) return covered.get(node) as Boolean
        val parent = node.parentElement
        val result = (htmlElement(node) && (added.has(node) as Boolean)) ||
            (parent != null && isCovered(parent))
        covered.set(node, result)
        return result
    }

    for (mutation in mutations) {
        if (isCovered(mutation.target)) continue
        val nodes = mutation.addedNodes
        for (index in 0 until (nodes.length as Int)) {
            val node = nodes[index]
            if (htmlElement(node)) added.add(node)
        }
    }
    if (added.size == 0) return

    val roots = (array.from(added) as Array<dynamic>).filter { node ->
        var parent = node.parentElement
        var nested = false
        while (parent != null) {
            if (added.has(parent) as Boolean) {
                nested = true
                break
            }
            parent = parent.parentElement
        }
        !nested && node.isConnected == true && node.hasAttribute(FILTER_SKIP_ATTR) == false
    }
    val parents: dynamic = js("new Set()")
    for (root in roots) {
        if (root.parentElement != null) parents.add(root.parentElement)
    }
    val selector = union
    val candidates = if (selector == null) roots else roots.filter { root ->
        root.matches(selector) == true || root.querySelector(selector) != null
    }
    val parentNodes = array.from(parents) as Array<dynamic>

    val iterator = domFilters.values()
    while (true) {
        val item = iterator.next()
        if (item.done as Boolean) break
        val filter = item.value as DomFilter
        val matches: dynamic = js("new Set()")
        for (root in candidates) {
            if (root.matches(filter.scope) == true) matches.add(root)
            val found = root.querySelectorAll(filter.scope)
            for (index in 0 until (found.length as Int)) matches.add(found[index])
        }
        for (parent in parentNodes) {
            val nearest = parent.closest(filter.scope)
            if (nearest != null) matches.add(nearest)
        }
        for (element in array.from(matches) as Array<dynamic>) runFilter(filter, element)
    }
}

fun addDomFilter(scope: String, callback: (dynamic) -> Unit): () -> Unit {
    val doc: dynamic = js("document")
    val existing = doc.querySelectorAll(scope)
    val filter = DomFilter(scope, callback)
    domFilters.add(filter)
    rebuildUnion()

    for (index in 0 until (existing.length as Int)) runFilter(filter, existing[index])

    if (observer == null) {
        val flush: (Array<dynamic>) -> Unit = ::filterMutations
        observer = js("new MutationObserver(flush)")
    }
    observer.observe(doc.documentElement, js("({childList: true, subtree: true})"))
    return {
        domFilters.delete(filter)
        rebuildUnion()
        if (domFilters.size == 0 && observer != null) {
            observer.disconnect()
            observer = null
        }
    }
}
