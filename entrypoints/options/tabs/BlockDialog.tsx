import * as react from "react";
import {Info} from "lucide-react";

import {TooltipButton} from "@/components/WithTooltip";
import {DialogActions, ModalDialog, SubmitForm} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {Checkbox} from "@/components/ui/checkbox";
import {DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Field, FieldError, FieldGroup, FieldLabel} from "@/components/ui/field";
import {Input} from "@/components/ui/input";
import {DETECT_MODE_NAMES, TYPE_NAMES} from "@/core/storage/items";
import type {BlockEntry, BlockType, DetectMode} from "@/core/storage/types";
import type {BlockInputFields} from "@/core/block-list";
import {kotlinBridge} from "@/kotlin/bridge";
import {useBlocksStore} from "@/stores/blocks";
import {messageOf, SAVE_FAILED} from "@/utils/error";

import {RefresherSelect} from "../shared/RefresherSelect";

/** 모드마다 무엇을 가리는지. NOT_*는 한 유형의 불일치, 불포함 항목을 묶은 허용 목록이라(core/block.ts) 나머지가 모두 가려진다. */
export const DETECT_MODE_DESCRIPTIONS: Record<DetectMode, string> = {
    SAME: "값과 똑같으면 가립니다.",
    CONTAIN: "값이 들어 있으면 가립니다.",
    NOT_SAME: "값과 똑같은 것만 남기고 나머지를 모두 가립니다.\n불일치, 불포함 항목이 여럿이면 하나에만 맞아도 남깁니다.",
    NOT_CONTAIN: "값이 들어 있는 것만 남기고 나머지를 모두 가립니다.\n불일치, 불포함 항목이 여럿이면 하나에만 맞아도 남깁니다."
};

interface BlockDialogProps {
    type: BlockType;
    initial?: BlockEntry | null;
    onClose: () => void;
    onSubmit: (fields: BlockInputFields) => Promise<void>;
}

export const BlockDialog = kotlinBridge.createBlockDialog({
    react, useBlocksStore, typeNames: TYPE_NAMES, modeNames: DETECT_MODE_NAMES,
    modeDescriptions: DETECT_MODE_DESCRIPTIONS, messageOf, saveFailed: SAVE_FAILED,
    TooltipButton, Info, DialogActions, ModalDialog, SubmitForm, Button, Checkbox, DialogDescription,
    DialogHeader, DialogTitle, Field, FieldError, FieldGroup, FieldLabel, Input, RefresherSelect
}) as (props: BlockDialogProps) => import("react").ReactNode;
