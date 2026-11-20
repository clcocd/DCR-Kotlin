import {kotlinBridge} from "@/kotlin/bridge";

/** 받아온 목록 응답에서 찾는 디시 마크업. 바뀌면 조용히 빗나가니 e2e/live가 확인한다. */

/** 목록 표의 여는 태그 앞부분. 응답에서 이 뒤의 첫 tbody를 목록으로 자른다. */
export const LIST_TABLE = kotlinBridge.runtimeConstants.refresh.table;

/** 공지 행의 아이콘. 공지는 새 글, 맨 위 글로 세지 않는다. */
export const NOTICE_ICON = kotlinBridge.runtimeConstants.refresh.notice;

/** 수 칸과 내용 제거 패턴은 Kotlin 목록 갱신 코드와 공유한다. */
export const COUNT_CELLS = kotlinBridge.refreshCountCells;
export const COUNT_TEXT = kotlinBridge.refreshCountText;
