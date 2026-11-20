import {Dialog} from "@base-ui/react/dialog";
import {ChevronLeft, ChevronRight, ExternalLink, X} from "lucide-react";
import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

import {keepTabInside} from "@/components/dialogs";
import {overlay} from "@/components/overlay/shadow";
import {Button} from "@/components/ui/button";
import {dcinsideHref} from "@/core/http/urls";
import {hasModifier} from "@/utils/event";
import {cn} from "cn";

import {usePreviewStore, type ViewerImage} from "./previewStore";

export const ImageViewer = kotlinBridge.createImageViewer<{images: ViewerImage[]; index: number}>({
    react, overlay, Button, Dialog, ChevronLeft, ChevronRight, ExternalLink, X, keepTabInside, dcinsideHref, hasModifier,
    cn, store: usePreviewStore
});
