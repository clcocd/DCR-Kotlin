import {kotlinBridge} from "@/kotlin/bridge";

/** WXT 콘텐츠 매치 설정과 독립적으로 기능 실행 경로의 정규식을 Kotlin에서 관리한다. */
export const {board: BOARD_PAGE, view: VIEW_PAGE, write: WRITE_PAGE, list: LIST_PAGE} =
    kotlinBridge.runtimeConstants.pagePatterns;
