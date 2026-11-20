import {kotlinBridge} from "@/kotlin/bridge";

/** 페이지 컨텍스트에서 사용할 Kotlin 런타임을 설치한다. */
export default defineUnlistedScript(kotlinBridge.installPageRuntime);
