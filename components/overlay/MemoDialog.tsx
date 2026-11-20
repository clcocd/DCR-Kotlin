import {Shuffle} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {ColorInput} from "@/components/ColorInput";
import {ModalDialog, SubmitForm} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {Checkbox} from "@/components/ui/checkbox";
import {DialogClose, DialogDescription, DialogFooter, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Field, FieldLabel} from "@/components/ui/field";
import {InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput} from "@/components/ui/input-group";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import {MEMO_TYPE_NAMES, MEMO_TYPES} from "@/core/storage/items";
import {ownMemo, randomColor, useMemosStore} from "@/stores/memos";
import {selectedGallery, useUiStore} from "@/stores/ui";
import {SAVE_FAILED} from "@/utils/error";

export const MemoDialog = kotlinBridge.createMemoDialog({
    react, uiStore: useUiStore, memoStore: useMemosStore, types: MEMO_TYPES, typeNames: MEMO_TYPE_NAMES, selectedGallery,
    ownMemo, randomColor, saveFailed: SAVE_FAILED, ColorInput, ModalDialog, SubmitForm, Button, Checkbox,
    DialogClose, DialogDescription, DialogFooter, DialogHeader, DialogTitle, Field, FieldLabel,
    InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput, ToggleGroup, ToggleGroupItem, Shuffle
});
