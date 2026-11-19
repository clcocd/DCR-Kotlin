import {isViewPage, queryString} from "@/core/http/urls";
import {defineModule} from "@/core/module/define";
import type {ModuleImpl} from "@/core/module/types";
import {kotlinBridge} from "@/kotlin/bridge";

import meta, {HIDE_OPTIONS} from "./meta";

// 기존 페이지 판정과 메타 선택자는 그대로 사용하고, 실행·정리는 Kotlin이 담당한다.
const isDcconShop = location.hostname === "dccon.dcinside.com";

export default defineModule(meta, kotlinBridge.layoutModuleImpl(
    HIDE_OPTIONS,
    isViewPage,
    isDcconShop,
    () => queryString("exception_mode") === "notice"
) as ModuleImpl<typeof meta, void>);
