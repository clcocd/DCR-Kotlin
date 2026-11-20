import {HTTPError} from "ky";

import {isAbortError} from "@/utils/error";
import {defineModule} from "@/core/module/define";
import {getModuleApi} from "@/core/module/registry";
import {useUiStore} from "@/stores/ui";
import {messageOf} from "@/utils/error";
import {isTyping, pressedKey} from "@/utils/event";
import {kotlinBridge} from "@/kotlin/bridge";
import {isGalleryManager} from "@/utils/user";
import {notifyManage} from "@/stores/notify";

import {getEntry} from "@/core/preview/cache";
import {historyDoc, ownPreviewDepth, ownPreviewEntry} from "@/core/preview/history";
import {ADULT_ERROR, SECRET_ERROR} from "@/core/preview/parser";
import {blockCommenters, blockUser, bump, deletePost, fetchComments, setNotice, setRecommend} from "@/core/preview/request";
import {bindListKeys} from "./keyboard";
import meta from "./meta";
import {createCommentsPull} from "./commentsPull";
import {createPostLoader, cachedPost} from "./postLoader";
import {createPreviewHistory} from "./history";
import {createMini} from "./mini";
import {bindRows} from "./rows-input";
import {adjacentPreData, isTextPost} from "./rows";
import {MANAGE_LABELS, NO_FRESH, NO_HOOKS, usePreviewStore} from "./ui/previewStore";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupPreview(ctx, {
        store: usePreviewStore, uiStore: useUiStore, isAbortError, messageOf, pressedKey, isTyping, isGalleryManager,
        isHttpError: (error: unknown) => error instanceof HTTPError, adultError: ADULT_ERROR, secretError: SECRET_ERROR,
        getEntry, historyDoc, ownPreviewDepth, ownPreviewEntry, refresh: () => getModuleApi("refresh"),
        blockCommenters, blockUser, bump, deletePost, fetchComments, setNotice, setRecommend, notifyManage,
        bindListKeys, createCommentsPull, createPostLoader, cachedPost, createPreviewHistory, createMini, bindRows,
        adjacentPreData, isTextPost, labels: MANAGE_LABELS, noFresh: NO_FRESH, noHooks: NO_HOOKS
    })
});
