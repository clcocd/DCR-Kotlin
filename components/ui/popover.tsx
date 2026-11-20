"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import * as React from "react"
import { Popover as PopoverPrimitive } from "@base-ui/react/popover"
import { cn } from "cn"

function Popover(props: PopoverPrimitive.Root.Props): react.ReactNode {
  return view.Popover(props)
}

function PopoverTrigger(props: PopoverPrimitive.Trigger.Props): react.ReactNode {
  return view.PopoverTrigger(props)
}

function PopoverContent(props: PopoverPrimitive.Popup.Props &
  Pick<
    PopoverPrimitive.Positioner.Props,
    "align" | "alignOffset" | "side" | "sideOffset"
  >): react.ReactNode {
  return view.PopoverContent(props)
}

function PopoverHeader(props: React.ComponentProps<"div">): react.ReactNode {
  return view.PopoverHeader(props)
}

function PopoverTitle(props: PopoverPrimitive.Title.Props): react.ReactNode {
  return view.PopoverTitle(props)
}

function PopoverDescription(props: PopoverPrimitive.Description.Props): react.ReactNode {
  return view.PopoverDescription(props)
}

const view = kotlinBridge.createUiPopover<{
  Popover: (props: Parameters<typeof Popover>[0]) => react.ReactNode
  PopoverTrigger: (props: Parameters<typeof PopoverTrigger>[0]) => react.ReactNode
  PopoverContent: (props: Parameters<typeof PopoverContent>[0]) => react.ReactNode
  PopoverHeader: (props: Parameters<typeof PopoverHeader>[0]) => react.ReactNode
  PopoverTitle: (props: Parameters<typeof PopoverTitle>[0]) => react.ReactNode
  PopoverDescription: (props: Parameters<typeof PopoverDescription>[0]) => react.ReactNode
}>({react, PopoverPrimitive, cn})

export {
  Popover,
  PopoverContent,
  PopoverDescription,
  PopoverHeader,
  PopoverTitle,
  PopoverTrigger,
}
