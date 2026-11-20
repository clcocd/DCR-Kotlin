import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";
import {Clock} from "lucide-react";

import {Badge} from "@/components/ui/badge";
import {WithTooltip} from "@/components/WithTooltip";

import {useTick} from "./TimeStamp";
import {parseDate, usePreviewStore} from "./previewStore";

export const CountDown = kotlinBridge.createCountDown({react, Badge, WithTooltip, useTick, parseDate, store: usePreviewStore, Clock});
