import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { cn } from "cn"
import { Loader2Icon } from "lucide-react"

function Spinner(props: React.ComponentProps<"svg">): react.ReactNode {
  return view.Spinner(props)
}

const view = kotlinBridge.createUiSpinner<{
  Spinner: (props: Parameters<typeof Spinner>[0]) => react.ReactNode
}>({react, Loader2Icon, cn})

export { Spinner }
