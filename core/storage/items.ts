import {storage, type WxtStorageItem} from "wxt/utils/storage";

import {kotlinBridge} from "@/kotlin/bridge";
import {objectKeys} from "@/utils/typed";

import type {BlockType, DatabaseMeta, DetectMode, SettingValue} from "./types";

/** 차단 유형 → 이름. 유형, 모드 목록과 타입(core/storage/types.ts)은 이름표의 키 순서를 따른다. */
export const TYPE_NAMES = {
    NICK: "닉네임",
    ID: "아이디",
    IP: "IP",
    TITLE: "제목",
    TEXT: "내용",
    COMMENT: "댓글",
    DCCON: "디시콘",
    TAB: "말머리"
};

export const DETECT_MODE_NAMES = {
    SAME: "일치",
    CONTAIN: "포함",
    NOT_SAME: "불일치",
    NOT_CONTAIN: "불포함"
};

export const MEMO_TYPE_NAMES = {
    UID: "아이디",
    NICK: "닉네임",
    IP: "IP"
};

export const BLOCK_TYPES = objectKeys(TYPE_NAMES);
export const DETECT_MODES = objectKeys(DETECT_MODE_NAMES);
export const MEMO_TYPES = objectKeys(MEMO_TYPE_NAMES);

export const DEFAULT_DETECT_MODE: Record<BlockType, DetectMode> = {
    NICK: "SAME",
    ID: "SAME",
    IP: "SAME",
    TITLE: "CONTAIN",
    TEXT: "CONTAIN",
    COMMENT: "CONTAIN",
    DCCON: "SAME",
    TAB: "SAME"
};

export const {rawStorageKey: rawKey, blockListKey, memoMapKey, moduleSettingsKey, moduleDataKey,
    settingsKeyModule, moduleKeyModule, isBlockListKey} = kotlinBridge;
export const BLOCK_DEFAULTS_KEY = "local:refresher:block:defaults";
export const MODULES_KEY = "local:refresher:modules";

/** 목록, 설정 쓰기의 Web Locks 이름. 스토어와 설정 옮기기가 같은 이름을 써야 직렬되므로 한곳에 둔다. */
export const LIST_LOCKS = {blocks: "refresher:blocks", memos: "refresher:memos", moduleSettings: "refresher:module-settings"} as const;

export const DB_META_KEY = "local:refresher:db:meta";

/** IP 데이터는 큰 JSON 문자열이므로 저장소 항목 대신 키로 다룬다. */
export const DB_KEYS = {
    ip: "local:refresher:db:ip"
} as const;

interface StorageDefinitions {
    blockDefaultsStorage(): WxtStorageItem<Record<BlockType, DetectMode>, {}>;
    modulesStorage(): WxtStorageItem<Record<string, boolean>, {}>;
    moduleSettingsStorage(id: string): WxtStorageItem<Record<string, SettingValue>, {}>;
    moduleDataStorage<T>(id: string, fallback: T): WxtStorageItem<T, {}>;
    dbMetaStorage(): WxtStorageItem<DatabaseMeta, {}>;
    writeDatabase(meta: DatabaseMeta, ip: string): Promise<void>;
    backupStorage: {
        auto(): WxtStorageItem<boolean, {}>;
        error(): WxtStorageItem<string, {}>;
        pending(): WxtStorageItem<boolean, {}>;
        undo(): WxtStorageItem<{data: Record<string, unknown>; createdAt: number} | null, {}>;
        device(): WxtStorageItem<string, {}>;
    };
}

export const {blockDefaultsStorage, modulesStorage, moduleSettingsStorage, moduleDataStorage, dbMetaStorage, writeDatabase, backupStorage} =
    kotlinBridge.createStorageDefinitions<StorageDefinitions>({
        storage, blocksKey: BLOCK_DEFAULTS_KEY, defaults: DEFAULT_DETECT_MODE, modulesKey: MODULES_KEY, metaKey: DB_META_KEY, keys: DB_KEYS
    });
