import {kotlinBridge} from "@/kotlin/bridge";

/** 이 속성이 붙은 요소는 필터가 훑지 않는다. */
export const FILTER_SKIP_ATTR = kotlinBridge.filterSkipAttr;

/** 현재·추가 요소에 필터를 적용하고 해제 함수를 돌려준다. */
export const addFilter = kotlinBridge.addDomFilter;
