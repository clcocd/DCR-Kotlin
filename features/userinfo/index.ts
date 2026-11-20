import QuickLRU from "quick-lru";
import {storage} from "wxt/utils/storage";

import {badgeViewOf, showsUid} from "@/core/badges";
import {initDatabase, ipInfoOf, onDatabaseChange, passesIpFilter} from "@/core/database";
import {defineModule} from "@/core/module/define";
import {getGallogActivityUnlessBusy, onGallogActivity, trimRatios} from "@/core/gallog";
import {ROWS_HIDDEN_EVENT} from "@/core/block";
import {FILTER_SKIP_ATTR} from "@/core/filtering";
import {pageGallery} from "@/core/http/urls";
import {WRITER_ROW_SELECTOR} from "@/core/list";
import {moduleDataKey} from "@/core/storage/items";
import {watchStorage} from "@/core/storage/sync";
import {onBfcacheRestore} from "@/utils/dom";
import {findMemo, useMemosStore} from "@/stores/memos";
import {isFresh, isLowActivity, openWriterBubble, useUiStore} from "@/stores/ui";
import {kotlinBridge} from "@/kotlin/bridge";

import meta, {BADGE_COLORS} from "./meta";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupUserInfo(ctx, {
        storage, ratioKey: moduleDataKey("userinfo"), pageGallery, badgeColors: BADGE_COLORS,
        writerRowSelector: WRITER_ROW_SELECTOR, rowsHiddenEvent: ROWS_HIDDEN_EVENT, filterSkipAttr: FILTER_SKIP_ATTR,
        findMemo, memoStore: useMemosStore, uiStore: useUiStore, watchStorage, onBfcacheRestore, openWriterBubble,
        badgeViewOf, showsUid, initDatabase, ipInfoOf, onDatabaseChange, passesIpFilter,
        trimRatios, isFresh, isLowActivity, onGallogActivity, getUnlessBusy: getGallogActivityUnlessBusy,
        createFailedCache: () => new QuickLRU<string, true>({maxSize: 500, maxAge: 5 * 60_000})
    })
});
