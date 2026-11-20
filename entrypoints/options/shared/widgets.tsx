import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";
import {Search} from "lucide-react";
import type {ComponentProps, ReactNode} from "react";

import {Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Empty, EmptyDescription} from "@/components/ui/empty";
import {InputGroup, InputGroupAddon, InputGroupInput} from "@/components/ui/input-group";

const widgets = kotlinBridge.createOptionWidgets({
    react, Search, Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle,
    Empty, EmptyDescription, InputGroup, InputGroupAddon, InputGroupInput
});

export const Section = widgets.Section as (props: {
    title?: ReactNode; desc?: ReactNode; actions?: ReactNode; children?: ReactNode
}) => ReactNode;
export const SearchInput = widgets.SearchInput as (props: ComponentProps<typeof InputGroupInput>) => ReactNode;
export const EmptyList = widgets.EmptyList as (props: {children: ReactNode}) => ReactNode;
