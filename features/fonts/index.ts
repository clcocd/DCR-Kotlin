import {defineModule} from "@/core/module/define";
import type {ModuleImpl} from "@/core/module/types";
import {kotlinBridge} from "@/kotlin/bridge";

import meta from "./meta";

/** 글꼴 적용·설정 변경·종료 처리는 Kotlin/JS에서 실행한다. */
export default defineModule(meta, kotlinBridge.fontsModuleImpl() as ModuleImpl<typeof meta, void>);
