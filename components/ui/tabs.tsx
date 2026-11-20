"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import { Tabs as TabsPrimitive } from "@base-ui/react/tabs"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"

function Tabs(props: TabsPrimitive.Root.Props): react.ReactNode {
  return view.Tabs(props)
}

const tabsListVariants = kotlinBridge.createUiVariants<ReturnType<typeof cva<{
  variant: Record<"default" | "line", string>
}>>>("tabsListVariants", cva)

function TabsList(props: TabsPrimitive.List.Props & VariantProps<typeof tabsListVariants>): react.ReactNode {
  return view.TabsList(props)
}

function TabsTrigger(props: TabsPrimitive.Tab.Props): react.ReactNode {
  return view.TabsTrigger(props)
}

function TabsContent(props: TabsPrimitive.Panel.Props): react.ReactNode {
  return view.TabsContent(props)
}

const view = kotlinBridge.createUiTabs<{
  Tabs: (props: Parameters<typeof Tabs>[0]) => react.ReactNode
  TabsList: (props: Parameters<typeof TabsList>[0]) => react.ReactNode
  TabsTrigger: (props: Parameters<typeof TabsTrigger>[0]) => react.ReactNode
  TabsContent: (props: Parameters<typeof TabsContent>[0]) => react.ReactNode
}>({react, TabsPrimitive, cn, tabsListVariants})

export { Tabs, TabsList, TabsTrigger, TabsContent, tabsListVariants }
