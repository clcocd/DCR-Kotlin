package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiTable(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Table(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", props("data-slot" to "table-container", "className" to "relative w-full overflow-x-auto"),
            ui.h("table", mergeUiProps(props("data-slot" to "table", "className" to s.cn("w-full caption-bottom text-sm", className)), rest))
        )
    }

    fun TableHeader(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("thead", mergeUiProps(props("data-slot" to "table-header", "className" to s.cn("[&_tr]:border-b", className)), rest))
    }

    fun TableBody(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("tbody", mergeUiProps(props("data-slot" to "table-body", "className" to s.cn("[&_tr:last-child]:border-0", className)), rest))
    }

    fun TableFooter(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("tfoot", mergeUiProps(props("data-slot" to "table-footer", "className" to s.cn("border-t bg-muted/50 font-medium [&>tr]:last:border-b-0", className)), rest))
    }

    fun TableRow(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("tr", mergeUiProps(props("data-slot" to "table-row", "className" to s.cn("border-b transition-colors hover:bg-muted/50 has-aria-expanded:bg-muted/50 data-[state=selected]:bg-muted", className)), rest))
    }

    fun TableHead(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("th", mergeUiProps(props("data-slot" to "table-head", "className" to s.cn("h-10 px-2 text-left align-middle font-medium whitespace-nowrap text-foreground [&:has([role=checkbox])]:pr-0", className)), rest))
    }

    fun TableCell(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("td", mergeUiProps(props("data-slot" to "table-cell", "className" to s.cn("p-2 align-middle whitespace-nowrap [&:has([role=checkbox])]:pr-0", className)), rest))
    }

    fun TableCaption(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("caption", mergeUiProps(props("data-slot" to "table-caption", "className" to s.cn("mt-4 text-sm text-muted-foreground", className)), rest))
    }

    views.Table = ::Table
    views.TableHeader = ::TableHeader
    views.TableBody = ::TableBody
    views.TableFooter = ::TableFooter
    views.TableRow = ::TableRow
    views.TableHead = ::TableHead
    views.TableCell = ::TableCell
    views.TableCaption = ::TableCaption
    return views
}
