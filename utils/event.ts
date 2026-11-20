import {overlay} from "@/components/overlay/shadow";
import {kotlinBridge} from "@/kotlin/bridge";

/** 실제 이벤트 대상. document/window 리스너에서는 shadow DOM 안 요소가 shadow host로 재지정되므로 composedPath로 되찾는다. */
export const eventTarget = kotlinBridge.originalEventTarget;

/** 조합키를 같이 눌렀는지. Kotlin 구현을 호출한다. */
export const hasModifier = (ev: Pick<KeyboardEvent, "ctrlKey" | "altKey" | "metaKey" | "shiftKey">): boolean =>
    kotlinBridge.hasModifier(ev.ctrlKey, ev.altKey, ev.metaKey, ev.shiftKey);

/** 한글 입력 중에도 물리적인 영문·숫자 키를 우선 처리한다. Kotlin 구현을 호출한다. */
export const pressedKey = (ev: Pick<KeyboardEvent, "code" | "key">): string =>
    kotlinBridge.pressedKey(ev.code, ev.key);

/** 모달 다이얼로그(components/ui/dialog.tsx, alert-dialog.tsx)의 배경. 떠 있으면 그 다이얼로그가 키를 받는다. */
export const MODAL_OVERLAY_SELECTOR = "[data-slot=dialog-overlay], [data-slot=alert-dialog-overlay]";

/** 단축키를 무시할지. 입력칸에 타이핑 중이거나 모달 다이얼로그가 떠 있으면 true. */
export const isTyping = kotlinBridge.createTyping(overlay, MODAL_OVERLAY_SELECTOR);
