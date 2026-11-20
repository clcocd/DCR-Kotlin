import {ClipboardCopy, CloudDownload, CloudUpload, FileDown, RefreshCw, Trash2, Undo2, Upload} from "lucide-react";
import * as react from "react";

import {ConfirmDialog, DialogActions} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog";
import {Field, FieldLabel} from "@/components/ui/field";
import {Switch} from "@/components/ui/switch";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import {focusPanel, panelOf} from "@/components/useReturnFocus";
import {clearCloudBackups, collectLocalData, readBackupTargets, readCloudBackup, readCloudBackupStatus, runBackup} from "@/core/backup";
import {backupStorage, dbMetaStorage} from "@/core/storage/items";
import {kotlinBridge} from "@/kotlin/bridge";
import {sendMessage} from "@/core/messaging/protocol";
import {mergeBackup, parseImport, writeSettings} from "@/core/settings-transfer";
import {friendlyMessage} from "@/utils/error";

import {notify, notifyDone} from "../optionsStore";
import {useStorageItem} from "../shared/hooks";
import {ImportDialog} from "../shared/ImportDialog";
import {Section} from "../shared/widgets";

export const DataTab = kotlinBridge.createDataTab({
    react, browser, ConfirmDialog, DialogActions, Button, Dialog, DialogContent, DialogDescription, DialogHeader,
    DialogTitle, DialogTrigger, Field, FieldLabel, Switch, ToggleGroup, ToggleGroupItem,
    Section, ImportDialog, ClipboardCopy, CloudDownload, CloudUpload, FileDown, RefreshCw, Trash2, Undo2, Upload,
    focusPanel, panelOf, clearCloudBackups, collectLocalData, readBackupTargets, readCloudBackup, readCloudBackupStatus,
    runBackup, backupStorage, dbMetaStorage, sendMessage, mergeBackup, parseImport, writeSettings,
    friendlyMessage, notify, notifyDone, useStorageItem
});
