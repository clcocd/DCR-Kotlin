import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {ConfirmDialog, DialogCloseButton, ModalDialog} from "@/components/dialogs";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {DialogTitle} from "@/components/ui/dialog";
import {Skeleton} from "@/components/ui/skeleton";
import {Spinner} from "@/components/ui/spinner";
import {dcconCode, isBlockedHidden} from "@/core/block";
import {urls} from "@/core/http/urls";
import {addDcconPackage, dcconPackages, fetchDcconPackage} from "@/core/preview/request";
import {clearDcconCaches} from "@/core/preview/dccon";
import {useUiStore} from "@/stores/ui";

import {previewGallery, usePreviewStore} from "./previewStore";

export const {DcconInfoPopup, openDcconInfo} = kotlinBridge.createDcconInfoView<{
    DcconInfoPopup: (props: {code: string}) => react.ReactNode;
    openDcconInfo(event: react.MouseEvent<HTMLElement>): boolean;
}>({
    react, ConfirmDialog, DialogCloseButton, ModalDialog, Badge, Button, DialogTitle, Skeleton, Spinner,
    dcconCode, isBlockedHidden, imageBase: urls.dccon.image, shop: urls.dccon.shop, addDcconPackage, dcconPackages,
    fetchDcconPackage, clearDcconCaches, uiStore: useUiStore, previewGallery, store: usePreviewStore
});
