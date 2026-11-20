import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"

function FieldSet(props: React.ComponentProps<"fieldset">): react.ReactNode {
  return view.FieldSet(props)
}

function FieldLegend(props: React.ComponentProps<"legend"> & { variant?: "legend" | "label" }): react.ReactNode {
  return view.FieldLegend(props)
}

function FieldGroup(props: React.ComponentProps<"div">): react.ReactNode {
  return view.FieldGroup(props)
}

const fieldVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  orientation: Record<"vertical" | "horizontal" | "responsive", string>
}>>>("fieldVariants", cva)

function Field(props: React.ComponentProps<"div"> & VariantProps<typeof fieldVariants>): react.ReactNode {
  return view.Field(props)
}

function FieldContent(props: React.ComponentProps<"div">): react.ReactNode {
  return view.FieldContent(props)
}

function FieldLabel(props: React.ComponentProps<typeof Label>): react.ReactNode {
  return view.FieldLabel(props)
}

function FieldTitle(props: React.ComponentProps<"div">): react.ReactNode {
  return view.FieldTitle(props)
}

function FieldDescription(props: React.ComponentProps<"p">): react.ReactNode {
  return view.FieldDescription(props)
}

function FieldSeparator(props: React.ComponentProps<"div"> & {
  children?: React.ReactNode
}): react.ReactNode {
  return view.FieldSeparator(props)
}

const FieldError = kotlinBridge.createFieldError<React.ComponentProps<"div"> & {
  errors?: Array<{ message?: string } | undefined>
}>({react, cn})

const view = kotlinBridge.createUiField<{
  FieldSet: (props: Parameters<typeof FieldSet>[0]) => react.ReactNode
  FieldLegend: (props: Parameters<typeof FieldLegend>[0]) => react.ReactNode
  FieldGroup: (props: Parameters<typeof FieldGroup>[0]) => react.ReactNode
  Field: (props: Parameters<typeof Field>[0]) => react.ReactNode
  FieldContent: (props: Parameters<typeof FieldContent>[0]) => react.ReactNode
  FieldLabel: (props: Parameters<typeof FieldLabel>[0]) => react.ReactNode
  FieldTitle: (props: Parameters<typeof FieldTitle>[0]) => react.ReactNode
  FieldDescription: (props: Parameters<typeof FieldDescription>[0]) => react.ReactNode
  FieldSeparator: (props: Parameters<typeof FieldSeparator>[0]) => react.ReactNode
}>({react, cn, fieldVariants, Label, Separator})

export {
  Field,
  FieldLabel,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLegend,
  FieldSeparator,
  FieldSet,
  FieldContent,
  FieldTitle,
}
