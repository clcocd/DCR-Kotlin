import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { Toggle as TogglePrimitive } from "@base-ui/react/toggle"
import { ToggleGroup as ToggleGroupPrimitive } from "@base-ui/react/toggle-group"
import { type VariantProps } from "class-variance-authority"
import { cn } from "cn"

import { toggleVariants } from "@/components/ui/toggle"

const ToggleGroupContext = React.createContext<
  VariantProps<typeof toggleVariants> & {
    spacing?: number
    orientation?: "horizontal" | "vertical"
  }
>({
  size: "default",
  variant: "default",
  spacing: 2,
  orientation: "horizontal",
})

function ToggleGroup(props: ToggleGroupPrimitive.Props &
  VariantProps<typeof toggleVariants> & {
    spacing?: number
    orientation?: "horizontal" | "vertical"
  }): react.ReactNode {
  return view.ToggleGroup(props)
}

function ToggleGroupItem(props: TogglePrimitive.Props & VariantProps<typeof toggleVariants>): react.ReactNode {
  return view.ToggleGroupItem(props)
}

const view = kotlinBridge.createUiToggleGroup<{
  ToggleGroup: (props: Parameters<typeof ToggleGroup>[0]) => react.ReactNode
  ToggleGroupItem: (props: Parameters<typeof ToggleGroupItem>[0]) => react.ReactNode
}>({react, ToggleGroupPrimitive, cn, ToggleGroupContext, React, TogglePrimitive, toggleVariants})

export { ToggleGroup, ToggleGroupItem }
