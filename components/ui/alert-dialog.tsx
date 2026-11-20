import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { AlertDialog as AlertDialogPrimitive } from "@base-ui/react/alert-dialog"
import { cn } from "cn"

import { Button } from "@/components/ui/button"

function AlertDialog(props: AlertDialogPrimitive.Root.Props): react.ReactNode {
  return view.AlertDialog(props)
}

function AlertDialogTrigger(props: AlertDialogPrimitive.Trigger.Props): react.ReactNode {
  return view.AlertDialogTrigger(props)
}

function AlertDialogPortal(props: AlertDialogPrimitive.Portal.Props): react.ReactNode {
  return view.AlertDialogPortal(props)
}

function AlertDialogOverlay(props: AlertDialogPrimitive.Backdrop.Props): react.ReactNode {
  return view.AlertDialogOverlay(props)
}

function AlertDialogContent(props: AlertDialogPrimitive.Popup.Props & {
  size?: "default" | "sm"
}): react.ReactNode {
  return view.AlertDialogContent(props)
}

function AlertDialogHeader(props: React.ComponentProps<"div">): react.ReactNode {
  return view.AlertDialogHeader(props)
}

function AlertDialogFooter(props: React.ComponentProps<"div">): react.ReactNode {
  return view.AlertDialogFooter(props)
}

function AlertDialogMedia(props: React.ComponentProps<"div">): react.ReactNode {
  return view.AlertDialogMedia(props)
}

function AlertDialogTitle(props: React.ComponentProps<typeof AlertDialogPrimitive.Title>): react.ReactNode {
  return view.AlertDialogTitle(props)
}

function AlertDialogDescription(props: React.ComponentProps<typeof AlertDialogPrimitive.Description>): react.ReactNode {
  return view.AlertDialogDescription(props)
}

function AlertDialogAction(props: React.ComponentProps<typeof Button>): react.ReactNode {
  return view.AlertDialogAction(props)
}

function AlertDialogCancel(props: AlertDialogPrimitive.Close.Props &
  Pick<React.ComponentProps<typeof Button>, "variant" | "size">): react.ReactNode {
  return view.AlertDialogCancel(props)
}

const view = kotlinBridge.createUiAlertDialog<{
  AlertDialog: (props: Parameters<typeof AlertDialog>[0]) => react.ReactNode
  AlertDialogTrigger: (props: Parameters<typeof AlertDialogTrigger>[0]) => react.ReactNode
  AlertDialogPortal: (props: Parameters<typeof AlertDialogPortal>[0]) => react.ReactNode
  AlertDialogOverlay: (props: Parameters<typeof AlertDialogOverlay>[0]) => react.ReactNode
  AlertDialogContent: (props: Parameters<typeof AlertDialogContent>[0]) => react.ReactNode
  AlertDialogHeader: (props: Parameters<typeof AlertDialogHeader>[0]) => react.ReactNode
  AlertDialogFooter: (props: Parameters<typeof AlertDialogFooter>[0]) => react.ReactNode
  AlertDialogMedia: (props: Parameters<typeof AlertDialogMedia>[0]) => react.ReactNode
  AlertDialogTitle: (props: Parameters<typeof AlertDialogTitle>[0]) => react.ReactNode
  AlertDialogDescription: (props: Parameters<typeof AlertDialogDescription>[0]) => react.ReactNode
  AlertDialogAction: (props: Parameters<typeof AlertDialogAction>[0]) => react.ReactNode
  AlertDialogCancel: (props: Parameters<typeof AlertDialogCancel>[0]) => react.ReactNode
}>({react, AlertDialogPrimitive, cn, Button})

export {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogOverlay,
  AlertDialogPortal,
  AlertDialogTitle,
  AlertDialogTrigger,
}
