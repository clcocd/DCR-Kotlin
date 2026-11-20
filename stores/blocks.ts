import {create} from "zustand";
import {shallow} from "zustand/vanilla/shallow";

import {BLOCK_DEFAULTS_KEY, BLOCK_TYPES, blockDefaultsStorage, blockListKey, LIST_LOCKS} from "@/core/storage/items";
import type {BlockEntry, BlockType, DetectMode} from "@/core/storage/types";
import {typedListSync} from "@/core/storage/sync";
import {saveOrReload} from "@/utils/error";

// 검사, 정규화는 core/block-list에 있다.
import {kotlinBridge} from "@/kotlin/bridge";
import {normalizeBlockList, normalizeDefaults, type BlockInputFields} from "@/core/block-list";

interface BlocksState {
    entries: Record<BlockType, BlockEntry[]>;
    defaults: Record<BlockType, DetectMode>;
    /** 저장소의 현재 값에 change를 붙여 쓴다 (가져오기, 여러 개 지우기). */
    updateEntries: (type: BlockType, change: (entries: BlockEntry[]) => BlockEntry[]) => Promise<void>;
    addEntry: (type: BlockType, fields: BlockInputFields) => Promise<void>;
    /** 여러 항목을 한 번의 쓰기로. */
    addEntries: (type: BlockType, list: BlockInputFields[]) => Promise<void>;
    updateEntry: (type: BlockType, id: string, fields: BlockInputFields) => Promise<void>;
    removeEntry: (type: BlockType, id: string) => Promise<void>;
    setDefault: (type: BlockType, mode: DetectMode) => Promise<void>;
}

export const blockLabel = kotlinBridge.blockLabel;
export const emptyEntries = kotlinBridge.emptyBlockEntries;

export const useBlocksStore = create<BlocksState>((set, get) => kotlinBridge.createBlockState<BlocksState>(set, get, {
    lists: () => lists, defaultsStorage: blockDefaultsStorage, saveOrReload
}));

// 값이 없으면 null이고, normalize가 빈 목록, 기본 모드로 맞춘다.
const lists = typedListSync({
    types: BLOCK_TYPES,
    keyOf: blockListKey,
    normalize: normalizeBlockList,
    get: (): Record<BlockType, BlockEntry[]> => useBlocksStore.getState().entries,
    set: (entries) => useBlocksStore.setState({entries}),
    failure: "차단 목록을 저장하지 못했습니다.",
    // 목록처럼 같은 값이면 스토어를 건드리지 않는다. 새 객체를 넣으면 차단 모듈이 문서 전체를 다시 판정한다 (setDefault 뒤 돌아온 같은 값, bfcache 복원 등).
    extra: {
        [BLOCK_DEFAULTS_KEY]: (value) => {
            const defaults = normalizeDefaults(value);
            if (!shallow(defaults, useBlocksStore.getState().defaults)) useBlocksStore.setState({defaults});
        }
    },
    lock: LIST_LOCKS.blocks
});

/** 저장소 값을 읽고 변경(다른 탭, 옵션 페이지)을 감시한다. 여러 번 불러도 한 번만 한다. signal은 콘텐츠 스크립트 컨텍스트의 것이다. */
export const initBlocksStore = lists.start;
