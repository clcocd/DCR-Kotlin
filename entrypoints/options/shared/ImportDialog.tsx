import {FileUp} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {DialogActions, ModalDialog} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Spinner} from "@/components/ui/spinner";
import {Textarea} from "@/components/ui/textarea";

import {type Done, notify, notifyDone} from "../optionsStore";

interface ImportDialogProps {
    title: string;
    desc?: React.ReactNode;
    placeholder?: string;
    fromFile?: boolean;
    onClose: () => void;
    /** 성공 문구를 돌려주고 실패는 직접 알린다 - undefined면 창을 열어 둔다. */
    onSubmit: (text: string) => Promise<Done | undefined>;
}

export const ImportDialog = kotlinBridge.createImportDialog({
    react, FileUp, DialogActions, ModalDialog, Button, DialogDescription, DialogHeader,
    DialogTitle, Spinner, Textarea, notify, notifyDone
}) as (props: ImportDialogProps) => React.ReactNode;
