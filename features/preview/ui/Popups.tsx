import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {DialogActions, ModalDialog, SubmitForm} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {Checkbox} from "@/components/ui/checkbox";
import {DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Field, FieldLabel} from "@/components/ui/field";
import {Input} from "@/components/ui/input";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {Spinner} from "@/components/ui/spinner";
import {sendMessage, TURNSTILE_CANCEL_EVENT, TURNSTILE_HOST} from "@/core/messaging/protocol";
import {BLOCK_DAYS, BLOCK_REASONS} from "@/core/preview/types";
import {focusOnMount} from "@/utils/focus";

import {DcconInfoPopup} from "./DcconInfoPopup";
import {ImageViewer} from "./ImageViewer";
import {usePreviewStore} from "./previewStore";

export const {Popups} = kotlinBridge.createPreviewPopups<{
    Popups: () => react.ReactNode;
}>({
    react, store: usePreviewStore, DialogActions, ModalDialog, SubmitForm, Button, Checkbox, DialogDescription,
    DialogHeader, DialogTitle, Field, FieldLabel, Input, RadioGroup, RadioGroupItem, Spinner, sendMessage,
    turnstileHost: TURNSTILE_HOST, cancelEvent: TURNSTILE_CANCEL_EVENT, days: BLOCK_DAYS,
    reasons: BLOCK_REASONS, focusOnMount, DcconInfoPopup, ImageViewer
});
