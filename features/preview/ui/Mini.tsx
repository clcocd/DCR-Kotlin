import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {cn} from "cn";

import {hoverMini, MINI_HEIGHT, MINI_WIDTH, usePreviewStore} from "./previewStore";
import {usePreviewMedia} from "./usePreviewMedia";

export const Mini = kotlinBridge.createMiniPreview({
    react, cn, hoverMini, width: MINI_WIDTH, height: MINI_HEIGHT, usePreviewStore, usePreviewMedia
});
