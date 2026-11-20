package dev.dcrk.standardui

import dev.dcrk.*

internal fun createUiAlert(s: dynamic): dynamic {
    val ui = ReactUi(s.react)
    val views = props()

    fun Alert(input: dynamic): dynamic {
        val className = input["className"]
        val variant = input["variant"]
        val rest = omitUiProps(input, "className", "variant")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert", "role" to "alert", "className" to s.cn(s.alertVariants(props("variant" to variant)), className)), rest))
    }

    fun AlertTitle(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert-title", "className" to s.cn("font-medium group-has-[>svg]/alert:col-start-2 [&_a]:underline [&_a]:underline-offset-3 [&_a]:hover:text-foreground", className)), rest))
    }

    fun AlertDescription(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert-description", "className" to s.cn("text-sm text-balance text-muted-foreground md:text-pretty [&_a]:underline [&_a]:underline-offset-3 [&_a]:hover:text-foreground [&_p:not(:last-child)]:mb-4", className)), rest))
    }

    fun AlertAction(input: dynamic): dynamic {
        val className = input["className"]
        val rest = omitUiProps(input, "className")
        return ui.h("div", mergeUiProps(props("data-slot" to "alert-action", "className" to s.cn("absolute top-2.5 right-3", className)), rest))
    }

    views.Alert = ::Alert
    views.AlertTitle = ::AlertTitle
    views.AlertDescription = ::AlertDescription
    views.AlertAction = ::AlertAction
    return views
}
