import {isBlockedHidden} from "@/core/block";
import {dcinsideHref} from "@/core/http/urls";
import {kotlinBridge} from "@/kotlin/bridge";

import {openDcconInfo} from "./DcconInfoPopup";
import {usePreviewStore} from "./previewStore";

export const {clickContents, pressContents} = kotlinBridge.createContentsControls({
    store: usePreviewStore, openDcconInfo, dcinsideHref, isBlockedHidden
});

export const markMedia = kotlinBridge.markPreviewMedia;
export const markPressable = kotlinBridge.markPreviewPressable;
