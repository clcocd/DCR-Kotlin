import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { Separator as SeparatorPrimitive } from "@base-ui/react/separator"
import { cn } from "cn"

function Separator(props: SeparatorPrimitive.Props): react.ReactNode {
  return view.Separator(props)
}

const view = kotlinBridge.createUiSeparator<{
  Separator: (props: Parameters<typeof Separator>[0]) => react.ReactNode
}>({react, SeparatorPrimitive, cn})

export { Separator }
