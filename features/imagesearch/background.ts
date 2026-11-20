import {defineBackgroundModule} from "@/core/module/background";
import {IMAGE_URL_PATTERNS} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

import meta, {ENGINE_IDS, ENGINES} from "./meta";

/** 이미지 검색 URL·우클릭 메뉴·설정 적용은 Kotlin/JS, 브라우저 API는 기존 인터페이스를 유지한다. */
const impl = kotlinBridge.imageSearchBackgroundImpl(
    ENGINE_IDS,
    ENGINES,
    IMAGE_URL_PATTERNS,
    (listener) => browser.contextMenus.onClicked.addListener(listener),
    (id) => browser.contextMenus.remove(id),
    (menu) => browser.contextMenus.create(menu),
    (details) => browser.tabs.create(details)
) as {
    listen(): void;
    apply(state: {enabled: boolean; settings: Record<keyof typeof ENGINES, boolean>}): Promise<void>;
};

export default defineBackgroundModule(meta, impl);
