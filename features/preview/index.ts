import {HTTPError} from "ky";

import {isAbortError} from "@/utils/error";
import {defineModule} from "@/core/module/define";
import {getModuleApi} from "@/core/module/registry";
import {useUiStore} from "@/stores/ui";
import {messageOf} from "@/utils/error";
import {kotlinBridge} from "@/kotlin/bridge";
import {notifyManage} from "@/stores/notify";

import {getEntry} from "@/core/preview/cache";
import {historyDoc, ownPreviewDepth, ownPreviewEntry} from "@/core/preview/history";
import {ADULT_ERROR, SECRET_ERROR} from "@/core/preview/parser";
import {blockCommenters, fetchComments} from "@/core/preview/request";
import {bindListKeys} from "./keyboard";
import meta from "./meta";
import {createCommentsPull} from "./commentsPull";
import {createPostLoader, cachedPost} from "./postLoader";
import {createPreviewHistory} from "./history";
import {bindRows} from "./rows-input";
import {adjacentPreData, isTextPost} from "./rows";
import {NO_FRESH, NO_HOOKS, usePreviewStore} from "./ui/previewStore";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupPreview(ctx, {
        store: usePreviewStore, uiStore: useUiStore, isAbortError, messageOf,
        isHttpError: (error: unknown) => error instanceof HTTPError, adultError: ADULT_ERROR, secretError: SECRET_ERROR,
        getEntry, historyDoc, ownPreviewDepth, ownPreviewEntry, refresh: () => getModuleApi("refresh"),
        blockCommenters, fetchComments, notifyManage,
        bindListKeys, createCommentsPull, createPostLoader, cachedPost, createPreviewHistory, bindRows,
        adjacentPreData, isTextPost, noFresh: NO_FRESH, noHooks: NO_HOOKS
    })
});
