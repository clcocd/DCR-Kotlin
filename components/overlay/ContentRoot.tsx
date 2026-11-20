import type {ComponentType} from "react";
import * as react from "react";

import {TooltipProvider} from "@/components/ui/tooltip";
import {usePreviewStore} from "@/features/preview/ui/previewStore";
import {kotlinBridge} from "@/kotlin/bridge";

import {MemoDialog} from "./MemoDialog";
import {ToastHost} from "./Toasts";
import {BubbleHost, DcconPackageDialog} from "./UserBubble";

// 기능별 오버레이 호스트 (features/<id>/overlay.tsx의 default). 스타일(feature-styles.ts)처럼 모아 그린다.
const hosts = import.meta.glob<{ default: ComponentType }>("../../features/*/overlay.tsx", {eager: true});

export const ContentRoot = kotlinBridge.createContentRoot({
    react, hosts, TooltipProvider, usePreviewStore, MemoDialog, ToastHost, BubbleHost, DcconPackageDialog
});
