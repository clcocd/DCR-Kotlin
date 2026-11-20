import {kotlinBridge} from "@/kotlin/bridge";
import {isBackupTarget, runBackup} from "@/core/backup";
import {backupStorage} from "@/core/storage/items";

export const startAutoBackup = (): void => kotlinBridge.startAutoBackup({
    browser, backupStorage, isBackupTarget, runBackup, firefox: import.meta.env.BROWSER === "firefox"
});
