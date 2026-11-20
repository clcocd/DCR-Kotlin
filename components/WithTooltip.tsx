import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";
import type {ComponentProps, ReactElement, ReactNode} from "react";
import {Tooltip as TooltipPrimitive} from "@base-ui/react/tooltip";

import {overlay} from "@/components/overlay/shadow";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipTrigger} from "@/components/ui/tooltip";
import {cn} from "cn";

type TooltipSide = TooltipPrimitive.Positioner.Props["side"];

export const {WithTooltip, TooltipButton} = kotlinBridge.createTooltipViews<{
    WithTooltip(props: {tip: ReactNode; side?: TooltipSide; trigger: ReactElement; children: ReactNode}): ReactNode;
    TooltipButton(props: ComponentProps<typeof Button> & {tip: string; side?: TooltipSide}): ReactNode;
}>({react, overlay, cn, TooltipPrimitive, Button, Tooltip, TooltipTrigger});
