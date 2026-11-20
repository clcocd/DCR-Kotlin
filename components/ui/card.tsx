import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { cn } from "cn"

function Card(props: React.ComponentProps<"div"> & { size?: "default" | "sm" }): react.ReactNode {
  return view.Card(props)
}

function CardHeader(props: React.ComponentProps<"div">): react.ReactNode {
  return view.CardHeader(props)
}

function CardTitle(props: React.ComponentProps<"div">): react.ReactNode {
  return view.CardTitle(props)
}

function CardDescription(props: React.ComponentProps<"div">): react.ReactNode {
  return view.CardDescription(props)
}

function CardAction(props: React.ComponentProps<"div">): react.ReactNode {
  return view.CardAction(props)
}

function CardContent(props: React.ComponentProps<"div">): react.ReactNode {
  return view.CardContent(props)
}

function CardFooter(props: React.ComponentProps<"div">): react.ReactNode {
  return view.CardFooter(props)
}

const view = kotlinBridge.createUiCard<{
  Card: (props: Parameters<typeof Card>[0]) => react.ReactNode
  CardHeader: (props: Parameters<typeof CardHeader>[0]) => react.ReactNode
  CardTitle: (props: Parameters<typeof CardTitle>[0]) => react.ReactNode
  CardDescription: (props: Parameters<typeof CardDescription>[0]) => react.ReactNode
  CardAction: (props: Parameters<typeof CardAction>[0]) => react.ReactNode
  CardContent: (props: Parameters<typeof CardContent>[0]) => react.ReactNode
  CardFooter: (props: Parameters<typeof CardFooter>[0]) => react.ReactNode
}>({react, cn})

export {
  Card,
  CardHeader,
  CardFooter,
  CardTitle,
  CardAction,
  CardDescription,
  CardContent,
}
