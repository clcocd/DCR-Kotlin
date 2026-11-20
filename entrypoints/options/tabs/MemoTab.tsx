import {ClipboardCopy, Smartphone} from "lucide-react";
import * as react from "react";

import {ColorInput} from "@/components/ColorInput";
import {DialogActions, ModalDialog, SubmitForm} from "@/components/dialogs";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Field, FieldError, FieldGroup, FieldLabel} from "@/components/ui/field";
import {Input} from "@/components/ui/input";
import {MEMO_TYPE_NAMES, MEMO_TYPES} from "@/core/storage/items";

import {kotlinBridge} from "@/kotlin/bridge";
import {normalizeMemoMap, ownMemo, randomColor, useMemosStore} from "@/stores/memos";
import {SAVE_FAILED} from "@/utils/error";

import {ImportDialog} from "../shared/ImportDialog";
import {ListRow, ListTabs} from "../lists/ListTabs";
import {notify} from "../optionsStore";
import {copyText} from "@/stores/ui";
import {RefresherSelect} from "../shared/RefresherSelect";

export const MemoTab = kotlinBridge.createMemoTab({
    react, types: MEMO_TYPES, names: MEMO_TYPE_NAMES, useMemosStore, normalizeMemoMap, ownMemo, randomColor,
    saveFailed: SAVE_FAILED, notify, copyText, ColorInput, DialogActions, ModalDialog, SubmitForm, Badge,
    Button, DialogHeader, DialogTitle, Field, FieldError, FieldGroup, FieldLabel, Input, ClipboardCopy,
    Smartphone, ImportDialog, ListRow, ListTabs, RefresherSelect
});
