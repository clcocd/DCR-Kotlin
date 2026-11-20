import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { Radio as RadioPrimitive } from "@base-ui/react/radio"
import { RadioGroup as RadioGroupPrimitive } from "@base-ui/react/radio-group"
import { cn } from "cn"

function RadioGroup(props: RadioGroupPrimitive.Props): react.ReactNode {
  return view.RadioGroup(props)
}

function RadioGroupItem(props: RadioPrimitive.Root.Props): react.ReactNode {
  return view.RadioGroupItem(props)
}

const view = kotlinBridge.createUiRadioGroup<{
  RadioGroup: (props: Parameters<typeof RadioGroup>[0]) => react.ReactNode
  RadioGroupItem: (props: Parameters<typeof RadioGroupItem>[0]) => react.ReactNode
}>({react, RadioGroupPrimitive, cn, RadioPrimitive})

export { RadioGroup, RadioGroupItem }
