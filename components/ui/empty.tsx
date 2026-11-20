import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

function Empty(props: React.ComponentProps<"div">): react.ReactNode {
  return view.Empty(props)
}

function EmptyHeader(props: React.ComponentProps<"div">): react.ReactNode {
  return view.EmptyHeader(props)
}

const emptyMediaVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  variant: Record<"default" | "icon", string>
}>>>("emptyMediaVariants", cva)

function EmptyMedia(props: React.ComponentProps<"div"> & VariantProps<typeof emptyMediaVariants>): react.ReactNode {
  return view.EmptyMedia(props)
}

function EmptyTitle(props: React.ComponentProps<"div">): react.ReactNode {
  return view.EmptyTitle(props)
}

function EmptyDescription(props: React.ComponentProps<"p">): react.ReactNode {
  return view.EmptyDescription(props)
}

function EmptyContent(props: React.ComponentProps<"div">): react.ReactNode {
  return view.EmptyContent(props)
}

const view = kotlinBridge.createUiEmpty<{
  Empty: (props: Parameters<typeof Empty>[0]) => react.ReactNode
  EmptyHeader: (props: Parameters<typeof EmptyHeader>[0]) => react.ReactNode
  EmptyMedia: (props: Parameters<typeof EmptyMedia>[0]) => react.ReactNode
  EmptyTitle: (props: Parameters<typeof EmptyTitle>[0]) => react.ReactNode
  EmptyDescription: (props: Parameters<typeof EmptyDescription>[0]) => react.ReactNode
  EmptyContent: (props: Parameters<typeof EmptyContent>[0]) => react.ReactNode
}>({react, cn, emptyMediaVariants})

export {
  Empty,
  EmptyHeader,
  EmptyTitle,
  EmptyDescription,
  EmptyContent,
  EmptyMedia,
}
