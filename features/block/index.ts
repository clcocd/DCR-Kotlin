import {kotlinBridge} from "@/kotlin/bridge";
import {BLOCKED_TEXT, dcconCode, duplicateOf, groupDuplicates, HIDDEN_ROW_SELECTOR, isAnyBlocked, isBlocked, ROWS_HIDDEN_EVENT} from "@/core/block";
import {FILTER_SKIP_ATTR} from "@/core/filtering";
import {defineModule} from "@/core/module/define";
import {isViewPage, pageGallery} from "@/core/http/urls";
import {BLOCK_TYPES} from "@/core/storage/items";
import {WRITER_ROW_SELECTOR} from "@/core/list";
import {useBlocksStore} from "@/stores/blocks";
import {openWriterBubble, useUiStore} from "@/stores/ui";
import {textWithoutScripts, whenDomReady} from "@/utils/dom";
import {eventTarget} from "@/utils/event";

import meta, {type Ctx} from "./meta";

export default defineModule(meta, {
    setup(ctx: Ctx) {
        return kotlinBridge.setupBlock(ctx, {
            pageGallery, isViewPage, useBlocksStore, useUiStore, openWriterBubble, textWithoutScripts, whenDomReady,
            eventTarget, isAnyBlocked, isBlocked, dcconCode, duplicateOf, groupDuplicates, blockTypes: BLOCK_TYPES,
            hiddenRowSelector: HIDDEN_ROW_SELECTOR, writerRowSelector: WRITER_ROW_SELECTOR, blockedText: BLOCKED_TEXT,
            rowsHiddenEvent: ROWS_HIDDEN_EVENT, filterSkip: FILTER_SKIP_ATTR
        });
    },
    shortcuts: {
        blockReveal: (api) => api.toggleReveal()
    },
    pageToggles: {
        reveal: {
            desc: (api) => `가린 ${api.hiddenCount()}개를 흐리게 보입니다`,
            isOn: (api) => api.isRevealed(),
            toggle: (api) => api.toggleReveal()
        }
    }
});
