import {cn} from "cn";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {FOCUS_RING} from "@/components/focusRing";

import {parseDate} from "./previewStore";

const clock = kotlinBridge.createPreviewClock({react, cn, focusRing: FOCUS_RING, parseDate});

export const {TimeStamp, useTick} = clock;
