"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import * as React from "react"
import { Select as SelectPrimitive } from "@base-ui/react/select"
import { cn } from "cn"
import { ChevronDownIcon, CheckIcon, ChevronUpIcon } from "lucide-react"

const Select = SelectPrimitive.Root

function SelectGroup(props: SelectPrimitive.Group.Props): react.ReactNode {
  return view.SelectGroup(props)
}

function SelectValue(props: SelectPrimitive.Value.Props): react.ReactNode {
  return view.SelectValue(props)
}

function SelectTrigger(props: SelectPrimitive.Trigger.Props & {
  size?: "sm" | "default"
}): react.ReactNode {
  return view.SelectTrigger(props)
}

function SelectContent(props: SelectPrimitive.Popup.Props &
  Pick<
    SelectPrimitive.Positioner.Props,
    "align" | "alignOffset" | "side" | "sideOffset" | "alignItemWithTrigger"
  >): react.ReactNode {
  return view.SelectContent(props)
}

function SelectLabel(props: SelectPrimitive.GroupLabel.Props): react.ReactNode {
  return view.SelectLabel(props)
}

function SelectItem(props: SelectPrimitive.Item.Props): react.ReactNode {
  return view.SelectItem(props)
}

function SelectSeparator(props: SelectPrimitive.Separator.Props): react.ReactNode {
  return view.SelectSeparator(props)
}

function SelectScrollUpButton(props: React.ComponentProps<typeof SelectPrimitive.ScrollUpArrow>): react.ReactNode {
  return view.SelectScrollUpButton(props)
}

function SelectScrollDownButton(props: React.ComponentProps<typeof SelectPrimitive.ScrollDownArrow>): react.ReactNode {
  return view.SelectScrollDownButton(props)
}

const view = kotlinBridge.createUiSelect<{
  SelectGroup: (props: Parameters<typeof SelectGroup>[0]) => react.ReactNode
  SelectValue: (props: Parameters<typeof SelectValue>[0]) => react.ReactNode
  SelectTrigger: (props: Parameters<typeof SelectTrigger>[0]) => react.ReactNode
  SelectContent: (props: Parameters<typeof SelectContent>[0]) => react.ReactNode
  SelectLabel: (props: Parameters<typeof SelectLabel>[0]) => react.ReactNode
  SelectItem: (props: Parameters<typeof SelectItem>[0]) => react.ReactNode
  SelectSeparator: (props: Parameters<typeof SelectSeparator>[0]) => react.ReactNode
  SelectScrollUpButton: (props: Parameters<typeof SelectScrollUpButton>[0]) => react.ReactNode
  SelectScrollDownButton: (props: Parameters<typeof SelectScrollDownButton>[0]) => react.ReactNode
}>({react, SelectPrimitive, cn, ChevronDownIcon, CheckIcon, ChevronUpIcon})

export {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectScrollDownButton,
  SelectScrollUpButton,
  SelectSeparator,
  SelectTrigger,
  SelectValue,
}
