import {kotlinBridge} from "@/kotlin/bridge";
import {storage} from "wxt/utils/storage";

import {onMessage, TURNSTILE_CANCEL_EVENT, TURNSTILE_HOST, type TurnstileResult} from "@/core/messaging/protocol";
import {defineBackgroundModule, hasTab, preparePageRuntime, runInPage} from "@/core/module/background";
import {COMMENT_SITE_KEYS, ICON_SITE_KEY} from "@/core/preview/dcinside-js";
import type {TurnstileConfig} from "@/core/preview/types";
import {moduleDataKey} from "@/core/storage/items";

import meta from "./meta";

// executeScript로 복사되는 연결 함수는 페이지에 준비한 Kotlin 런타임만 참조한다.
const executeGrecaptcha = (siteKey: string, action: string): Promise<string> | undefined =>
    window.__DCR_PAGE_RUNTIME__?.executeGrecaptcha(siteKey, action);
const executeTurnstile = (config: TurnstileConfig, host: string | null, cancelEvent: string): Promise<TurnstileResult> | undefined =>
    window.__DCR_PAGE_RUNTIME__?.executeTurnstile(config, host, cancelEvent);

export default defineBackgroundModule(meta, {
    listen: () => kotlinBridge.listenPreviewBackground({
        browser, storage, previewDataKey: moduleDataKey("preview"), onMessage, preparePageRuntime, runInPage, hasTab, executeGrecaptcha,
        executeTurnstile, iconSiteKey: ICON_SITE_KEY, commentSiteKeys: COMMENT_SITE_KEYS,
        turnstileHost: () => TURNSTILE_HOST, turnstileCancel: () => TURNSTILE_CANCEL_EVENT
    })
});
