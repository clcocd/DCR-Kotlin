import {storage, type WxtStorageItem} from "wxt/utils/storage";

import {kotlinBridge} from "@/kotlin/bridge";
import {objectKeys} from "@/utils/typed";

import type {BlockType, DatabaseMeta, DetectMode, SettingValue} from "./types";

/** 차단 유형 → 이름. 유형, 모드 목록과 타입(core/storage/types.ts)은 이름표의 키 순서를 따른다. */
export const TYPE_NAMES = kotlinBridge.storageMetadata.typeNames;
export const DETECT_MODE_NAMES = kotlinBridge.storageMetadata.detectModeNames;
export const MEMO_TYPE_NAMES = kotlinBridge.storageMetadata.memoTypeNames;

export const BLOCK_TYPES = objectKeys(TYPE_NAMES);
export const DETECT_MODES = objectKeys(DETECT_MODE_NAMES);
export const MEMO_TYPES = objectKeys(MEMO_TYPE_NAMES);

export const DEFAULT_DETECT_MODE: Record<BlockType, DetectMode> = kotlinBridge.storageMetadata.defaultDetectMode;

export const {rawStorageKey: rawKey, blockListKey, memoMapKey, moduleSettingsKey, moduleDataKey,
    settingsKeyModule, moduleKeyModule, isBlockListKey} = kotlinBridge;
export const BLOCK_DEFAULTS_KEY = kotlinBridge.runtimeConstants.storage.blockDefaultsKey;
export const MODULES_KEY = kotlinBridge.runtimeConstants.storage.modulesKey;

/** 목록, 설정 쓰기의 Web Locks 이름. 스토어와 설정 옮기기가 같은 이름을 써야 직렬되므로 한곳에 둔다. */
export const LIST_LOCKS = kotlinBridge.runtimeConstants.storage.listLocks;

export const DB_META_KEY = kotlinBridge.runtimeConstants.storage.dbMetaKey;

/** IP 데이터는 큰 JSON 문자열이므로 저장소 항목 대신 키로 다룬다. */
export const DB_KEYS = kotlinBridge.runtimeConstants.storage.dbKeys;

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
