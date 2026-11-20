import {Ban, ExternalLink, NotebookPen, Search} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {DialogActions, ModalDialog} from "@/components/dialogs";
import {Button} from "@/components/ui/button";
import {DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Popover as PopoverPrimitive} from "@base-ui/react/popover";
import {Popover} from "@/components/ui/popover";
import {Separator} from "@/components/ui/separator";
import {Skeleton} from "@/components/ui/skeleton";
import {useReturnFocus} from "@/components/useReturnFocus";
import {blockingEntries} from "@/core/block";
import {databaseVersion, ipInfoOf, onDatabaseChange, subscribeDatabase} from "@/core/database";
import {urls} from "@/core/http/urls";
import {getModuleApi, subscribeModuleApis} from "@/core/module/registry";
import {useRunningModuleSettings} from "@/core/module/useModuleSettings";
import {dcconPackages, fetchDcconPackage} from "@/core/preview/request";
import {TYPE_NAMES} from "@/core/storage/items";
import {handleBlockRequest} from "@/stores/blockRequest";
import {blockLabel, useBlocksStore} from "@/stores/blocks";
import {useUserMemo} from "@/stores/memos";
import {copyText, type SelectedUser, selectedGallery, useUiStore} from "@/stores/ui";
import {SAVE_FAILED} from "@/utils/error";
import {useGallogActivity} from "@/components/overlay/gallogActivity";
import {cn} from "cn";

import {overlay} from "./shadow";

const OverlayPopoverContent = kotlinBridge.createOverlayPopover({react, cn, overlay, PopoverPrimitive});

interface BubbleActions {
    onBlockPackage: (target: SelectedUser) => void;
    onDcconInfo: (code: string) => void;
}

export const {BubbleHost, DcconPackageDialog} = kotlinBridge.createUserBubble<{
    BubbleHost: (props: BubbleActions) => react.ReactNode;
    DcconPackageDialog: (props: {target: SelectedUser; onClose: () => void}) => react.ReactNode;
}>({
    react, cn, overlay, OverlayPopoverContent, uiStore: useUiStore, blockStore: useBlocksStore, saveFailed: SAVE_FAILED,
    DialogActions, ModalDialog, Button, DialogDescription, DialogHeader, DialogTitle, Popover, Separator, Skeleton,
    useReturnFocus, blockingEntries, databaseVersion, ipInfoOf, onDatabaseChange, subscribeDatabase,
    getModuleApi, subscribeModuleApis, useRunningModuleSettings, dcconPackages, fetchDcconPackage, typeNames: TYPE_NAMES, imageBase: urls.dccon.image,
    handleBlockRequest, blockLabel, useUserMemo, copyText, selectedGallery, useGallogActivity, Ban, ExternalLink, NotebookPen, Search
});
