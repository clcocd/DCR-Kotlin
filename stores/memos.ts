import {create} from "zustand";

import {LIST_LOCKS, MEMO_TYPES, memoMapKey} from "@/core/storage/items";
import {typedListSync} from "@/core/storage/sync";
import type {MemoEntry, MemoType} from "@/core/storage/types";
import {kotlinBridge} from "@/kotlin/bridge";

type MemoMap = Record<string, MemoEntry>;

interface MemosState {
    memos: Record<MemoType, MemoMap>;
    /** 저장소의 현재 값에 change를 붙여 쓴다 (가져오기, 여러 개 지우기). */
    updateMemos: (type: MemoType, change: (memos: MemoMap) => MemoMap) => Promise<void>;
    setMemo: (type: MemoType, user: string, entry: MemoEntry) => Promise<void>;
    removeMemo: (type: MemoType, user: string) => Promise<void>;
}

export const randomColor = kotlinBridge.randomMemoColor;
export const normalizeMemoMap = kotlinBridge.normalizeMemoMap;
export const ownMemo = kotlinBridge.ownMemo;

export const useMemosStore = create<MemosState>(() => kotlinBridge.createMemoState<MemosState>(() => lists));

type MemoUser = {uid?: string; ip?: string; nick?: string};
const lookupMemo = kotlinBridge.lookupMemo;

/** 유저에 달린 메모 (아이디 > IP > 닉네임 순). 다른 갤러리 전용 메모는 건너뛴다. */
export const findMemo = (user: MemoUser, gallery?: string | null): MemoEntry | undefined =>
    lookupMemo(useMemosStore.getState().memos, user, gallery);

/** 컴포넌트용 findMemo. 구독한 memos로 찾아야 메모가 바뀔 때 다시 그린다. */
export const useUserMemo = (user: MemoUser, gallery?: string | null): MemoEntry | undefined =>
    lookupMemo(useMemosStore((state) => state.memos), user, gallery);

// 값이 없으면 null → 빈 목록. 같은 값이 돌아오면 구독자(배지 전체 다시 그리기)를 깨우지 않는다 (typedListSync).
const lists = typedListSync({
    types: MEMO_TYPES,
    keyOf: memoMapKey,
    normalize: normalizeMemoMap,
    get: (): Record<MemoType, MemoMap> => useMemosStore.getState().memos,
    set: (memos) => useMemosStore.setState({memos}),
    failure: "메모를 저장하지 못했습니다.",
    lock: LIST_LOCKS.memos
});

/** 저장소 값을 읽고 변경(다른 탭, 옵션 페이지)을 감시한다. 여러 번 불러도 한 번만 한다. signal은 콘텐츠 스크립트 컨텍스트의 것이다. */
export const initMemosStore = lists.start;
