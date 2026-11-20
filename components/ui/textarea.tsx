import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { cn } from "cn"

function Textarea(props: React.ComponentProps<"textarea">): react.ReactNode {
  return view.Textarea(props)
}

const view = kotlinBridge.createUiTextarea<{
  Textarea: (props: Parameters<typeof Textarea>[0]) => react.ReactNode
}>({react, cn})

export { Textarea }
