import {defineModule} from "@/core/module/define";
import type {ModuleImpl} from "@/core/module/types";
import {kotlinBridge} from "@/kotlin/bridge";
import {useUiStore} from "@/stores/ui";

import meta from "./meta";

interface StealthApi {
    isRevealed(): boolean;
    toggle(): void;
}

/** 기능 동작과 수명주기는 Kotlin/JS에 있고 TS는 모듈 타입·UI 알림 연결만 담당한다. */
export default defineModule(
    meta,
    kotlinBridge.stealthModuleImpl((message) => useUiStore.getState().showToast(message)) as ModuleImpl<typeof meta, StealthApi>
);
