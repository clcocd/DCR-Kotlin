// 수천 번 그리는 목록 줄, 댓글에는 Base UI Button(리스너 여럿) 대신 이 포커스 링만 붙인다.
import {kotlinBridge} from "@/kotlin/bridge";
export const FOCUS_RING = kotlinBridge.runtimeConstants.ui.focusRing;
