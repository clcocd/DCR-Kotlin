import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { cn } from "cn"

function Kbd(props: React.ComponentProps<"kbd">): react.ReactNode {
  return view.Kbd(props)
}

function KbdGroup(props: React.ComponentProps<"div">): react.ReactNode {
  return view.KbdGroup(props)
}

const view = kotlinBridge.createUiKbd<{
  Kbd: (props: Parameters<typeof Kbd>[0]) => react.ReactNode
  KbdGroup: (props: Parameters<typeof KbdGroup>[0]) => react.ReactNode
}>({react, cn})

export { Kbd, KbdGroup }
