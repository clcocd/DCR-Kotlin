import {kotlinBridge} from "@/kotlin/bridge";
import {LIST_REPLACED_EVENT, onMessage} from "@/core/messaging/protocol";
import {hasTab, preparePageRuntime, runInPage} from "@/core/module/background";

declare global {
    interface Window {
        __DCR_PAGE_RUNTIME__?: {
            installListBridge(eventName: string): void;
            executeGrecaptcha(siteKey: string, action: string): Promise<string>;
            executeTurnstile(config: TurnstileConfig, host: string | null, cancelEvent: string): Promise<TurnstileResult>;
        };
    }
}

import type {TurnstileResult} from "@/core/messaging/protocol";
import type {TurnstileConfig} from "@/core/preview/types";

// executeScript는 함수를 복사해 페이지에서 실행하므로 import를 참조하지 않는다.
const installListBridge = (eventName: string): void => window.__DCR_PAGE_RUNTIME__?.installListBridge(eventName);

export const listenPageMessages = (): void =>
    kotlinBridge.listenListBridge({hasTab, onMessage, preparePageRuntime, runInPage, installListBridge, eventName: () => LIST_REPLACED_EVENT});
