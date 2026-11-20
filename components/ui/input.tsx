import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { Input as InputPrimitive } from "@base-ui/react/input"
import { cn } from "cn"

function Input(props: React.ComponentProps<"input">): react.ReactNode {
  return view.Input(props)
}

const view = kotlinBridge.createUiInput<{
  Input: (props: Parameters<typeof Input>[0]) => react.ReactNode
}>({react, InputPrimitive, cn})

export { Input }
