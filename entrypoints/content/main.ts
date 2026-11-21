import {kotlinBridge} from "@/kotlin/bridge";
import type {ContentScriptContext} from "wxt/utils/content-script-context";

import {releaseDatabase} from "@/core/database";
import {documentUrl} from "@/core/http/urls";
import {onMessage} from "@/core/messaging/protocol";
import {loadAll, pageState, runPageToggle, runShortcut, settledPageState, stopAll} from "@/core/module/registry";
import {enablesOf, isModuleEnabled} from "@/core/module/settings";
import {BOARD_PAGE} from "@/core/pagePatterns";
import {MODULES_KEY} from "@/core/storage/items";

import loaders from "@/features";
import metas from "@/features/metas";
import {initBlocksStore} from "@/stores/blocks";
import {initMemosStore} from "@/stores/memos";

import {storage} from "wxt/utils/storage";

import {mountOverlayWhenNeeded} from "./overlay";

const start = (ctx: ContentScriptContext): Promise<void> => kotlinBridge.startContentScript(ctx, {
    releaseDatabase, documentUrl, onMessage, loadAll, pageState, runPageToggle, runShortcut, settledPageState, stopAll,
    enablesOf, isModuleEnabled, boardPage: BOARD_PAGE, modulesKey: MODULES_KEY, loaders, metas, initBlocksStore,
    initMemosStore, storage, mountOverlayWhenNeeded
});
export {start};

(globalThis as Record<string, unknown>)["__refresherContentStart"] = start;
