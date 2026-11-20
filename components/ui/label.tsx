import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { cn } from "cn"

function Label(props: React.ComponentProps<"label">): react.ReactNode {
  return view.Label(props)
}

const view = kotlinBridge.createUiLabel<{
  Label: (props: Parameters<typeof Label>[0]) => react.ReactNode
}>({react, cn})

export { Label }
