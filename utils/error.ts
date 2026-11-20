import {isHTTPError, isNetworkError} from "ky";
import {kotlinBridge} from "@/kotlin/bridge";

/** 스토어가 저장소 쓰기에 실패해 되돌렸을 때의 안내. 확장을 업데이트한 뒤 남은 옛 탭에서 주로 난다. */
export const SAVE_FAILED = "저장하지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.";

export const {isTimeoutError, isAbortError, messageOf, saveOrReload, friendlyMessage} =
    kotlinBridge.createErrorUtilities({isHTTPError, isNetworkError});
