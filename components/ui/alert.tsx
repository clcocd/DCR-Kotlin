import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

const alertVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  variant: Record<"default" | "destructive", string>
}>>>("alertVariants", cva)

function Alert(props: React.ComponentProps<"div"> & VariantProps<typeof alertVariants>): react.ReactNode {
  return view.Alert(props)
}

function AlertTitle(props: React.ComponentProps<"div">): react.ReactNode {
  return view.AlertTitle(props)
}

function AlertDescription(props: React.ComponentProps<"div">): react.ReactNode {
  return view.AlertDescription(props)
}

function AlertAction(props: React.ComponentProps<"div">): react.ReactNode {
  return view.AlertAction(props)
}

const view = kotlinBridge.createUiAlert<{
  Alert: (props: Parameters<typeof Alert>[0]) => react.ReactNode
  AlertTitle: (props: Parameters<typeof AlertTitle>[0]) => react.ReactNode
  AlertDescription: (props: Parameters<typeof AlertDescription>[0]) => react.ReactNode
  AlertAction: (props: Parameters<typeof AlertAction>[0]) => react.ReactNode
}>({react, cn, alertVariants})

export { Alert, AlertTitle, AlertDescription, AlertAction }
