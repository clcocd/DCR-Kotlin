/** 클라우드 복원, 합치기, JSON 가져오기, 초기화의 저장 규칙. */
import {isBackupTarget, readBackupTargets} from "@/core/backup";
import {BLOCK_DEFAULTS_KEY, BLOCK_TYPES, backupStorage, blockListKey, isBlockListKey, LIST_LOCKS, MODULES_KEY, rawKey, settingsKeyModule} from "@/core/storage/items";
import {kotlinBridge} from "@/kotlin/bridge";

const transfer = kotlinBridge.createSettingsTransfer({
    storage: browser.storage.local,
    defaultsKey: rawKey(BLOCK_DEFAULTS_KEY),
    modulesKey: rawKey(MODULES_KEY),
    blockListTypes: new Map(BLOCK_TYPES.map((type) => [rawKey(blockListKey(type)), type])),
    lockNames: LIST_LOCKS,
    undo: backupStorage.undo,
    readBackupTargets, isBackupTarget, settingsKeyModule, isBlockListKey
});

export const pinDefaultMode = kotlinBridge.pinDefaultMode;
export const writeSettings = transfer.write;
export const mergeBackup = transfer.merge;
export const parseImport = transfer.parse;
