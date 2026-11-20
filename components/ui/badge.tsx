import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { mergeProps } from "@base-ui/react/merge-props"
import { useRender } from "@base-ui/react/use-render"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

const badgeVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  variant: Record<"default" | "secondary" | "destructive" | "outline" | "ghost" | "link", string>
}>>>("badgeVariants", cva)

function Badge(props: useRender.ComponentProps<"span"> & VariantProps<typeof badgeVariants>): react.ReactNode {
  return view.Badge(props)
}

const view = kotlinBridge.createUiBadge<{
  Badge: (props: Parameters<typeof Badge>[0]) => react.ReactNode
}>({react, useRender, mergeProps, cn, badgeVariants})

export { Badge, badgeVariants }
