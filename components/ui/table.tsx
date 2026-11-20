import * as react from "react"
import {kotlinBridge} from "@/kotlin/bridge"
import * as React from "react"
import { cn } from "cn"

function Table(props: React.ComponentProps<"table">): react.ReactNode {
  return view.Table(props)
}

function TableHeader(props: React.ComponentProps<"thead">): react.ReactNode {
  return view.TableHeader(props)
}

function TableBody(props: React.ComponentProps<"tbody">): react.ReactNode {
  return view.TableBody(props)
}

function TableFooter(props: React.ComponentProps<"tfoot">): react.ReactNode {
  return view.TableFooter(props)
}

function TableRow(props: React.ComponentProps<"tr">): react.ReactNode {
  return view.TableRow(props)
}

function TableHead(props: React.ComponentProps<"th">): react.ReactNode {
  return view.TableHead(props)
}

function TableCell(props: React.ComponentProps<"td">): react.ReactNode {
  return view.TableCell(props)
}

function TableCaption(props: React.ComponentProps<"caption">): react.ReactNode {
  return view.TableCaption(props)
}

const view = kotlinBridge.createUiTable<{
  Table: (props: Parameters<typeof Table>[0]) => react.ReactNode
  TableHeader: (props: Parameters<typeof TableHeader>[0]) => react.ReactNode
  TableBody: (props: Parameters<typeof TableBody>[0]) => react.ReactNode
  TableFooter: (props: Parameters<typeof TableFooter>[0]) => react.ReactNode
  TableRow: (props: Parameters<typeof TableRow>[0]) => react.ReactNode
  TableHead: (props: Parameters<typeof TableHead>[0]) => react.ReactNode
  TableCell: (props: Parameters<typeof TableCell>[0]) => react.ReactNode
  TableCaption: (props: Parameters<typeof TableCaption>[0]) => react.ReactNode
}>({react, cn})

export {
  Table,
  TableHeader,
  TableBody,
  TableFooter,
  TableHead,
  TableRow,
  TableCell,
  TableCaption,
}
