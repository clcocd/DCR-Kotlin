"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import { Tooltip as TooltipPrimitive } from "@base-ui/react/tooltip"
import { cn } from "cn"

function TooltipProvider(props: TooltipPrimitive.Provider.Props): react.ReactNode {
  return view.TooltipProvider(props)
}

function Tooltip(props: TooltipPrimitive.Root.Props): react.ReactNode {
  return view.Tooltip(props)
}

function TooltipTrigger(props: TooltipPrimitive.Trigger.Props): react.ReactNode {
  return view.TooltipTrigger(props)
}

function TooltipContent(props: TooltipPrimitive.Popup.Props &
  Pick<
    TooltipPrimitive.Positioner.Props,
    "align" | "alignOffset" | "side" | "sideOffset"
  >): react.ReactNode {
  return view.TooltipContent(props)
}

const view = kotlinBridge.createUiTooltip<{
  TooltipProvider: (props: Parameters<typeof TooltipProvider>[0]) => react.ReactNode
  Tooltip: (props: Parameters<typeof Tooltip>[0]) => react.ReactNode
  TooltipTrigger: (props: Parameters<typeof TooltipTrigger>[0]) => react.ReactNode
  TooltipContent: (props: Parameters<typeof TooltipContent>[0]) => react.ReactNode
}>({react, TooltipPrimitive, cn})

export { Tooltip, TooltipTrigger, TooltipContent, TooltipProvider }
