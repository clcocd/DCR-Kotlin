/** 클라우드(storage.sync) 백업. 설정을 gzip→base64로 묶어 8KB 조각으로 나눠 한 번에 쓴다 (전체 한도 100KB). */

import MODULE_IDS from "@/.wxt/module-ids";
import {
    BLOCK_DEFAULTS_KEY,
    BLOCK_TYPES,
    backupStorage,
    blockListKey,
    isBlockListKey,
    MEMO_TYPES,
    memoMapKey,
    MODULES_KEY,
    moduleSettingsKey,
    rawKey
} from "@/core/storage/items";
import {friendlyMessage} from "@/utils/error";
import {sha256} from "@/utils/hash";
import {kotlinBridge} from "@/kotlin/bridge";

export type BackupSlot = "manual" | "auto";

const BACKUP_KEYS = [
    rawKey(MODULES_KEY),
    rawKey(BLOCK_DEFAULTS_KEY),
    ...BLOCK_TYPES.map((type) => rawKey(blockListKey(type))),
    ...MEMO_TYPES.map((type) => rawKey(memoMapKey(type))),
    ...MODULE_IDS.map((id) => rawKey(moduleSettingsKey(id)))
];

interface SlotStatus {
    createdAt: number;
    size: number;
    /** 다른 기기가 올렸다 (기기 표시 없는 옛 백업 포함). */
    other: boolean;
}

export interface CloudBackupStatus {
    manual?: SlotStatus;
    auto?: SlotStatus;
}

interface BackupService {
    quota: number;
    isBackupTarget(key: string): boolean;
    readBackupTargets(): Promise<Record<string, unknown>>;
    collectLocalData(): Promise<Record<string, unknown>>;
    readCloudBackupStatus(): Promise<CloudBackupStatus>;
    clearCloudBackups(): Promise<void>;
    readCloudBackup(slot: BackupSlot): Promise<{data: Record<string, unknown>; createdAt: number} | null>;
    runBackup(slot: BackupSlot): Promise<void>;
}

export const {
    quota: CLOUD_QUOTA, isBackupTarget, readBackupTargets, collectLocalData, readCloudBackupStatus,
    clearCloudBackups, readCloudBackup, runBackup
} = kotlinBridge.createCloudBackup<BackupService>({
    storage: browser.storage, keys: BACKUP_KEYS, isBlockListKey, gzip: kotlinBridge.gzipBackup, gunzip: kotlinBridge.gunzipBackup, sha256, friendlyMessage,
    device: backupStorage.device, backupError: backupStorage.error
});
