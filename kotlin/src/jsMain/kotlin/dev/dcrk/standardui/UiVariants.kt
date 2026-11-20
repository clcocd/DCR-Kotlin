package dev.dcrk

internal fun createUiVariants(name: String, cva: dynamic): dynamic =
    when (name) {
        "alertVariants" -> cva(
            "group/alert relative grid w-full gap-0.5 rounded-lg border px-4 py-3 text-left text-sm has-data-[slot=alert-action]:relative has-data-[slot=alert-action]:pr-18 has-[>svg]:grid-cols-[auto_1fr] has-[>svg]:gap-x-2.5 *:[svg]:row-span-2 *:[svg]:translate-y-0.5 *:[svg]:text-current *:[svg:not([class*='size-'])]:size-4",
            props(
                "variants" to props(
                    "variant" to props(
                        "default" to "bg-card text-card-foreground",
                        "destructive" to "bg-card text-destructive *:data-[slot=alert-description]:text-destructive/90 *:[svg]:text-current"
                    )
                ),
                "defaultVariants" to props(
                    "variant" to "default"
                )
            )
        )
        "badgeVariants" -> cva(
            "group/badge inline-flex h-5 w-fit shrink-0 items-center justify-center gap-1 overflow-hidden rounded-4xl border border-transparent px-2 py-0.5 text-xs font-medium whitespace-nowrap transition-all focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&>svg]:pointer-events-none [&>svg]:size-3!",
            props(
                "variants" to props(
                    "variant" to props(
                        "default" to "bg-primary text-primary-foreground [a]:hover:bg-primary/80",
                        "secondary" to "bg-secondary text-secondary-foreground [a]:hover:bg-secondary/80",
                        "destructive" to "bg-destructive/10 text-destructive focus-visible:ring-destructive/20 dark:bg-destructive/20 dark:focus-visible:ring-destructive/40 [a]:hover:bg-destructive/20",
                        "outline" to "border-border text-foreground [a]:hover:bg-muted [a]:hover:text-muted-foreground",
                        "ghost" to "hover:bg-muted hover:text-muted-foreground dark:hover:bg-muted/50",
                        "link" to "text-primary underline-offset-4 hover:underline"
                    )
                ),
                "defaultVariants" to props(
                    "variant" to "default"
                )
            )
        )
        "buttonVariants" -> cva(
            "group/button inline-flex shrink-0 items-center justify-center rounded-md border border-transparent bg-clip-padding text-sm font-medium whitespace-nowrap transition-all outline-none select-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 active:not-aria-[haspopup]:translate-y-px disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
            props(
                "variants" to props(
                    "variant" to props(
                        "default" to "bg-primary text-primary-foreground hover:bg-primary/80",
                        "outline" to "border-border bg-background shadow-xs hover:bg-muted hover:text-foreground aria-expanded:bg-muted aria-expanded:text-foreground dark:border-input dark:bg-input/30 dark:hover:bg-input/50",
                        "secondary" to "bg-secondary text-secondary-foreground hover:bg-[color-mix(in_oklch,var(--secondary),var(--foreground)_5%)] aria-expanded:bg-secondary aria-expanded:text-secondary-foreground",
                        "ghost" to "hover:bg-muted hover:text-foreground aria-expanded:bg-muted aria-expanded:text-foreground dark:hover:bg-muted/50",
                        "destructive" to "bg-destructive/10 text-destructive hover:bg-destructive/20 focus-visible:border-destructive/40 focus-visible:ring-destructive/20 dark:bg-destructive/20 dark:hover:bg-destructive/30 dark:focus-visible:ring-destructive/40",
                        "link" to "text-primary underline-offset-4 hover:underline"
                    ),
                    "size" to props(
                        "default" to "h-9 gap-1.5 px-2.5 in-data-[slot=button-group]:rounded-md has-data-[icon=inline-end]:pr-2 has-data-[icon=inline-start]:pl-2",
                        "xs" to "h-6 gap-1 rounded-[min(var(--radius-md),8px)] px-2 text-xs in-data-[slot=button-group]:rounded-md has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 [&_svg:not([class*='size-'])]:size-3",
                        "sm" to "h-8 gap-1 rounded-[min(var(--radius-md),10px)] px-2.5 in-data-[slot=button-group]:rounded-md has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5",
                        "lg" to "h-10 gap-1.5 px-2.5 has-data-[icon=inline-end]:pr-2 has-data-[icon=inline-start]:pl-2",
                        "icon" to "size-9",
                        "icon-xs" to "size-6 rounded-[min(var(--radius-md),8px)] in-data-[slot=button-group]:rounded-md [&_svg:not([class*='size-'])]:size-3",
                        "icon-sm" to "size-8 rounded-[min(var(--radius-md),10px)] in-data-[slot=button-group]:rounded-md",
                        "icon-lg" to "size-10"
                    )
                ),
                "defaultVariants" to props(
                    "variant" to "default",
                    "size" to "default"
                )
            )
        )
        "emptyMediaVariants" -> cva(
            "mb-2 flex shrink-0 items-center justify-center [&_svg]:pointer-events-none [&_svg]:shrink-0",
            props(
                "variants" to props(
                    "variant" to props(
                        "default" to "bg-transparent",
                        "icon" to "flex size-10 shrink-0 items-center justify-center rounded-lg bg-muted text-foreground [&_svg:not([class*='size-'])]:size-6"
                    )
                ),
                "defaultVariants" to props(
                    "variant" to "default"
                )
            )
        )
        "fieldVariants" -> cva(
            "group/field flex w-full gap-3 data-[invalid=true]:text-destructive",
            props(
                "variants" to props(
                    "orientation" to props(
                        "vertical" to "flex-col *:w-full [&>.sr-only]:w-auto",
                        "horizontal" to "flex-row items-center has-[>[data-slot=field-content]]:items-start *:data-[slot=field-label]:flex-auto has-[>[data-slot=field-content]]:[&>[role=checkbox],[role=radio]]:mt-px",
                        "responsive" to "flex-col *:w-full @md/field-group:flex-row @md/field-group:items-center @md/field-group:*:w-auto @md/field-group:has-[>[data-slot=field-content]]:items-start @md/field-group:*:data-[slot=field-label]:flex-auto [&>.sr-only]:w-auto @md/field-group:has-[>[data-slot=field-content]]:[&>[role=checkbox],[role=radio]]:mt-px"
                    )
                ),
                "defaultVariants" to props(
                    "orientation" to "vertical"
                )
            )
        )
        "inputGroupAddonVariants" -> cva(
            "flex h-auto cursor-text items-center justify-center gap-2 py-1.5 text-sm font-medium text-muted-foreground select-none group-data-[disabled=true]/input-group:opacity-50 [&>kbd]:rounded-[calc(var(--radius)-5px)] [&>svg:not([class*='size-'])]:size-4",
            props(
                "variants" to props(
                    "align" to props(
                        "inline-start" to "order-first pl-2 has-[>button]:-ml-1 has-[>kbd]:ml-[-0.15rem]",
                        "inline-end" to "order-last pr-2 has-[>button]:-mr-1 has-[>kbd]:mr-[-0.15rem]",
                        "block-start" to "order-first w-full justify-start px-2.5 pt-2 group-has-[>input]/input-group:pt-2 [.border-b]:pb-2",
                        "block-end" to "order-last w-full justify-start px-2.5 pb-2 group-has-[>input]/input-group:pb-2 [.border-t]:pt-2"
                    )
                ),
                "defaultVariants" to props(
                    "align" to "inline-start"
                )
            )
        )
        "inputGroupButtonVariants" -> cva(
            "flex items-center gap-2 text-sm shadow-none",
            props(
                "variants" to props(
                    "size" to props(
                        "xs" to "h-6 gap-1 rounded-[calc(var(--radius)-5px)] px-1.5 [&>svg:not([class*='size-'])]:size-3.5",
                        "sm" to "",
                        "icon-xs" to "size-6 rounded-[calc(var(--radius)-5px)] p-0 has-[>svg]:p-0",
                        "icon-sm" to "size-8 p-0 has-[>svg]:p-0"
                    )
                ),
                "defaultVariants" to props(
                    "size" to "xs"
                )
            )
        )
        "tabsListVariants" -> cva(
            "group/tabs-list inline-flex w-fit items-center justify-center rounded-lg p-[3px] text-muted-foreground group-data-horizontal/tabs:h-9 group-data-vertical/tabs:h-fit group-data-vertical/tabs:flex-col data-[variant=line]:rounded-none",
            props(
                "variants" to props(
                    "variant" to props(
                        "default" to "bg-muted",
                        "line" to "gap-1 bg-transparent"
                    )
                ),
                "defaultVariants" to props(
                    "variant" to "default"
                )
            )
        )
        "toggleVariants" -> cva(
            "group/toggle inline-flex items-center justify-center gap-1 rounded-md text-sm font-medium whitespace-nowrap transition-[color,box-shadow] outline-none hover:bg-muted hover:text-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 aria-pressed:bg-muted dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
            props(
                "variants" to props(
                    "variant" to props(
                        "default" to "bg-transparent",
                        "outline" to "border border-input bg-transparent shadow-xs hover:bg-muted"
                    ),
                    "size" to props(
                        "default" to "h-9 min-w-9 px-2.5 has-data-[icon=inline-end]:pr-2 has-data-[icon=inline-start]:pl-2",
                        "sm" to "h-8 min-w-8 px-2.5 has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5",
                        "lg" to "h-10 min-w-10 px-2.5 has-data-[icon=inline-end]:pr-2 has-data-[icon=inline-start]:pl-2"
                    )
                ),
                "defaultVariants" to props(
                    "variant" to "default",
                    "size" to "default"
                )
            )
        )
        else -> error("Unknown component variants: $name")
    }
