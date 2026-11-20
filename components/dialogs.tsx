import {AlertDialog as AlertDialogPrimitive} from "@base-ui/react/alert-dialog";
import {Dialog as DialogPrimitive} from "@base-ui/react/dialog";
import {X} from "lucide-react";
import * as react from "react";
import type {ReactNode, RefObject} from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {overlay} from "@/components/overlay/shadow";
import {useReturnFocus} from "@/components/useReturnFocus";
import {AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogFooter, AlertDialogOverlay, AlertDialogTitle} from "@/components/ui/alert-dialog";
import {Button} from "@/components/ui/button";
import {Dialog, DialogClose, DialogFooter, DialogOverlay} from "@/components/ui/dialog";
import {useModuleSettings} from "@/core/module/useModuleSettings";
import {cn} from "cn";

type AutoFocus = "first" | "keyboard" | RefObject<HTMLElement | null>;

interface DialogViews {
    ModalDialog(props: {onClose: () => void; focusOnOpen?: AutoFocus; dismissible?: boolean;
        disablePointerDismissal?: boolean; actionsRef?: RefObject<DialogPrimitive.Root.Actions | null>;
        className?: string; children: ReactNode}): ReactNode;
    DialogActions(props: {cancelLabel?: string | null; cancelDisabled?: boolean; children?: ReactNode}): ReactNode;
    SubmitForm(props: {onSubmit: () => unknown; className?: string; children: ReactNode}): ReactNode;
    DialogCloseButton(): ReactNode;
    ConfirmDialog(props: {title: string; confirmLabel?: string; cancelLabel?: string | null; danger?: boolean;
        onConfirm: () => void; onClose: () => void}): ReactNode;
    Notice(props: {message: string | null; onClose: () => void}): ReactNode;
}

export const keepTabInside = kotlinBridge.keepTabInside;
export const {ModalDialog, DialogActions, SubmitForm, DialogCloseButton, ConfirmDialog, Notice} =
    kotlinBridge.createDialogs<DialogViews>({
        react, cn, overlay, useReturnFocus, useModuleSettings, DialogPrimitive, AlertDialogPrimitive,
        Dialog, DialogClose, DialogFooter, DialogOverlay, AlertDialog, AlertDialogAction, AlertDialogCancel,
        AlertDialogFooter, AlertDialogOverlay, AlertDialogTitle, Button, X
    });
