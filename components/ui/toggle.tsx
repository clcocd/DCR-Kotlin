import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { Toggle as TogglePrimitive } from "@base-ui/react/toggle"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

const toggleVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  variant: Record<"default" | "outline", string>;
  size: Record<"default" | "sm" | "lg", string>
}>>>("toggleVariants", cva)

function Toggle(props: TogglePrimitive.Props & VariantProps<typeof toggleVariants>): react.ReactNode {
  return view.Toggle(props)
}

const view = kotlinBridge.createUiToggle<{
  Toggle: (props: Parameters<typeof Toggle>[0]) => react.ReactNode
}>({react, TogglePrimitive, cn, toggleVariants})

export { Toggle, toggleVariants }
