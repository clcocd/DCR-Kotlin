import type {ContentScriptContext} from "wxt/utils/content-script-context";

import {http} from "@/core/http/client";
import {kotlinBridge} from "@/kotlin/bridge";
import {ContentRoot} from "@/components/overlay/ContentRoot";
import {overlay} from "@/components/overlay/shadow";
import {followDcAppearance} from "@/utils/appearance";
import {whenDomReady} from "@/utils/dom";
import {createRoot} from "react-dom/client";
import * as react from "react";

export const mountOverlayWhenNeeded = (ctx: ContentScriptContext): void => kotlinBridge.mountOverlayWhenNeeded(ctx, {
    browser, react, createRoot, createShadowRootUi, ContentRoot, overlay, followDcAppearance, whenDomReady,
    fetchText: (url: string) => http.get(url).text()
});
