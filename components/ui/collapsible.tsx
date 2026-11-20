"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import { Collapsible as CollapsiblePrimitive } from "@base-ui/react/collapsible"

function Collapsible(props: CollapsiblePrimitive.Root.Props): react.ReactNode {
  return view.Collapsible(props)
}

function CollapsibleTrigger(props: CollapsiblePrimitive.Trigger.Props): react.ReactNode {
  return view.CollapsibleTrigger(props)
}

function CollapsibleContent(props: CollapsiblePrimitive.Panel.Props): react.ReactNode {
  return view.CollapsibleContent(props)
}

const view = kotlinBridge.createUiCollapsible<{
  Collapsible: (props: Parameters<typeof Collapsible>[0]) => react.ReactNode
  CollapsibleTrigger: (props: Parameters<typeof CollapsibleTrigger>[0]) => react.ReactNode
  CollapsibleContent: (props: Parameters<typeof CollapsibleContent>[0]) => react.ReactNode
}>({react, CollapsiblePrimitive})

export { Collapsible, CollapsibleTrigger, CollapsibleContent }
