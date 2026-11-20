"use client"
import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"

import { Switch as SwitchPrimitive } from "@base-ui/react/switch"
import { cn } from "cn"

function Switch(props: SwitchPrimitive.Root.Props & {
  size?: "sm" | "default"
}): react.ReactNode {
  return view.Switch(props)
}

const view = kotlinBridge.createUiSwitch<{
  Switch: (props: Parameters<typeof Switch>[0]) => react.ReactNode
}>({react, SwitchPrimitive, cn})

export { Switch }
