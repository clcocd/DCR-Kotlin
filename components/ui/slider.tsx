import * as react from "react"
import { Slider as SliderPrimitive } from "@base-ui/react/slider"
import { cn } from "cn"
import {kotlinBridge} from "@/kotlin/bridge"

export const Slider = kotlinBridge.createUiSlider<SliderPrimitive.Root.Props>({react, cn, SliderPrimitive})
