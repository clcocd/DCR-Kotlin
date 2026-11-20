import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import { cn } from "cn"

function Skeleton(props: React.ComponentProps<"div">): react.ReactNode {
  return view.Skeleton(props)
}

const view = kotlinBridge.createUiSkeleton<{
  Skeleton: (props: Parameters<typeof Skeleton>[0]) => react.ReactNode
}>({react, cn})

export { Skeleton }
