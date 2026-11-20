import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { Button as ButtonPrimitive } from "@base-ui/react/button"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

const buttonVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  variant: Record<"default" | "outline" | "secondary" | "ghost" | "destructive" | "link", string>;
  size: Record<"default" | "xs" | "sm" | "lg" | "icon" | "icon-xs" | "icon-sm" | "icon-lg", string>
}>>>("buttonVariants", cva)

function Button(props: ButtonPrimitive.Props & VariantProps<typeof buttonVariants>): react.ReactNode {
  return view.Button(props)
}

const view = kotlinBridge.createUiButton<{
  Button: (props: Parameters<typeof Button>[0]) => react.ReactNode
}>({react, ButtonPrimitive, cn, buttonVariants})

export { Button, buttonVariants }
