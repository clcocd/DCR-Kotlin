import {kotlinBridge} from "@/kotlin/bridge";

/** 실제 실행 값은 Kotlin에서 관리하며 TypeScript는 타입/연결만 담당한다. */
export const ICON_SITE_KEY = kotlinBridge.iconSiteKey;
export const COMMENT_SITE_KEYS = kotlinBridge.commentSiteKeys;
export const D_ALPHABET = kotlinBridge.dAlphabet;
export const decodeD = kotlinBridge.decodeD;
