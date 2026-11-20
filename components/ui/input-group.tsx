"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"

function InputGroup(props: React.ComponentProps<"div">): react.ReactNode {
  return view.InputGroup(props)
}

const inputGroupAddonVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  align: Record<"inline-start" | "inline-end" | "block-start" | "block-end", string>
}>>>("inputGroupAddonVariants", cva)

const InputGroupAddon = kotlinBridge.createInputGroupAddon<
  React.ComponentProps<"div"> & VariantProps<typeof inputGroupAddonVariants>
>({react, cn, variants: inputGroupAddonVariants})

const inputGroupButtonVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  size: Record<"xs" | "sm" | "icon-xs" | "icon-sm", string>
}>>>("inputGroupButtonVariants", cva)

function InputGroupButton(props: Omit<React.ComponentProps<typeof Button>, "size" | "type"> &
  VariantProps<typeof inputGroupButtonVariants> & {
    type?: "button" | "submit" | "reset"
  }): react.ReactNode {
  return view.InputGroupButton(props)
}

function InputGroupText(props: React.ComponentProps<"span">): react.ReactNode {
  return view.InputGroupText(props)
}

function InputGroupInput(props: React.ComponentProps<"input">): react.ReactNode {
  return view.InputGroupInput(props)
}

function InputGroupTextarea(props: React.ComponentProps<"textarea">): react.ReactNode {
  return view.InputGroupTextarea(props)
}

const view = kotlinBridge.createUiInputGroup<{
  InputGroup: (props: Parameters<typeof InputGroup>[0]) => react.ReactNode
  InputGroupButton: (props: Parameters<typeof InputGroupButton>[0]) => react.ReactNode
  InputGroupText: (props: Parameters<typeof InputGroupText>[0]) => react.ReactNode
  InputGroupInput: (props: Parameters<typeof InputGroupInput>[0]) => react.ReactNode
  InputGroupTextarea: (props: Parameters<typeof InputGroupTextarea>[0]) => react.ReactNode
}>({react, cn, Button, inputGroupButtonVariants, Input, Textarea})

export {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupText,
  InputGroupInput,
  InputGroupTextarea,
}
