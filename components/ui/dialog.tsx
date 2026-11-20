"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import * as React from "react"
import { Dialog as DialogPrimitive } from "@base-ui/react/dialog"
import { cn } from "cn"

import { Button } from "@/components/ui/button"
import { XIcon } from "lucide-react"

function Dialog(props: DialogPrimitive.Root.Props): react.ReactNode {
  return view.Dialog(props)
}

function DialogTrigger(props: DialogPrimitive.Trigger.Props): react.ReactNode {
  return view.DialogTrigger(props)
}

function DialogPortal(props: DialogPrimitive.Portal.Props): react.ReactNode {
  return view.DialogPortal(props)
}

function DialogClose(props: DialogPrimitive.Close.Props): react.ReactNode {
  return view.DialogClose(props)
}

function DialogOverlay(props: DialogPrimitive.Backdrop.Props): react.ReactNode {
  return view.DialogOverlay(props)
}

function DialogContent(props: DialogPrimitive.Popup.Props & {
  showCloseButton?: boolean
}): react.ReactNode {
  return view.DialogContent(props)
}

function DialogHeader(props: React.ComponentProps<"div">): react.ReactNode {
  return view.DialogHeader(props)
}

function DialogFooter(props: React.ComponentProps<"div"> & {
  showCloseButton?: boolean
}): react.ReactNode {
  return view.DialogFooter(props)
}

function DialogTitle(props: DialogPrimitive.Title.Props): react.ReactNode {
  return view.DialogTitle(props)
}

function DialogDescription(props: DialogPrimitive.Description.Props): react.ReactNode {
  return view.DialogDescription(props)
}

const view = kotlinBridge.createUiDialog<{
  Dialog: (props: Parameters<typeof Dialog>[0]) => react.ReactNode
  DialogTrigger: (props: Parameters<typeof DialogTrigger>[0]) => react.ReactNode
  DialogPortal: (props: Parameters<typeof DialogPortal>[0]) => react.ReactNode
  DialogClose: (props: Parameters<typeof DialogClose>[0]) => react.ReactNode
  DialogOverlay: (props: Parameters<typeof DialogOverlay>[0]) => react.ReactNode
  DialogContent: (props: Parameters<typeof DialogContent>[0]) => react.ReactNode
  DialogHeader: (props: Parameters<typeof DialogHeader>[0]) => react.ReactNode
  DialogFooter: (props: Parameters<typeof DialogFooter>[0]) => react.ReactNode
  DialogTitle: (props: Parameters<typeof DialogTitle>[0]) => react.ReactNode
  DialogDescription: (props: Parameters<typeof DialogDescription>[0]) => react.ReactNode
}>({react, DialogPrimitive, cn, Button, XIcon})

export {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogOverlay,
  DialogPortal,
  DialogTitle,
  DialogTrigger,
}
