import {cn} from "cn";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {DialogCloseButton, ModalDialog} from "@/components/dialogs";
import {FOCUS_RING} from "@/components/focusRing";
import {Button} from "@/components/ui/button";
import {DialogTitle} from "@/components/ui/dialog";
import {Field, FieldLabel} from "@/components/ui/field";
import {Skeleton} from "@/components/ui/skeleton";
import {Switch} from "@/components/ui/switch";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import {fetchDcconList} from "@/core/preview/request";
import {cachedDcconList, setCachedDcconList, fetchRecentDccons} from "@/core/preview/dccon";
import type {DcinsideDccon} from "@/core/preview/types";
import {useUiStore} from "@/stores/ui";
import {smoothScroll} from "@/utils/dom";

interface DcconPopupProps {
    onSelect: (dccons: DcinsideDccon[], bigDccon: boolean) => void;
    onClose: () => void;
    onTxtcon?: () => void;
}

export const DcconPopup = kotlinBridge.createDcconPicker<DcconPopupProps>({
    react, cn, DialogCloseButton, ModalDialog, focusRing: FOCUS_RING, Button, DialogTitle, Field, FieldLabel, Skeleton,
    Switch, ToggleGroup, ToggleGroupItem, fetchDcconList, fetchRecentDccons, cachedDcconList, setCachedDcconList, uiStore: useUiStore, smoothScroll
});
