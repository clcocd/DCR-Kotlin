import {blockMode, isBlocked} from "@/core/block";
import {isAbortError} from "@/utils/error";
import {runningModuleSettings} from "@/core/module/registry";
import {getEntry, postKey, setEntry} from "@/core/preview/cache";
import {fetchPost} from "@/core/preview/request";
import {sanitizeHtml} from "@/utils/sanitize";

import {kotlinBridge} from "@/kotlin/bridge";

export const {textBlockOf, cachedPost, createPostLoader, supplementYoutubeEmbeds} = kotlinBridge.createPreviewPostService({
    blockMode, isBlocked, runningModuleSettings, getEntry, postKey, setEntry, fetchPost, sanitizeHtml, isAbortError
});
