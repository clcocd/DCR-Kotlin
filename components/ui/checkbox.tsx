import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { Checkbox as CheckboxPrimitive } from "@base-ui/react/checkbox"
import { cn } from "cn"
import { CheckIcon } from "lucide-react"

function Checkbox(props: CheckboxPrimitive.Root.Props): react.ReactNode {
  return view.Checkbox(props)
}

const view = kotlinBridge.createUiCheckbox<{
  Checkbox: (props: Parameters<typeof Checkbox>[0]) => react.ReactNode
}>({react, CheckboxPrimitive, cn, CheckIcon})

export { Checkbox }
