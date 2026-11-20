import {Dialog} from "@base-ui/react/dialog";
import type {ReactNode, RefObject, WheelEventHandler} from "react";
import * as react from "react";

import {kotlinBridge} from "@/kotlin/bridge";
import {overlay} from "@/components/overlay/shadow";
import {useUiStore} from "@/stores/ui";
import {MODAL_OVERLAY_SELECTOR} from "@/utils/event";
import {cn} from "cn";

import {usePreviewStore} from "./previewStore";

/** 미리보기 창의 뿌리 - 비모달 다이얼로그, Esc 정책, 바깥 누르기 닫기, 배경, 화면 전체 스크롤 칸. 내용은 children으로 Frame이 그린다. */
interface PreviewDialogProps {
    fading: boolean;
    busy: boolean;
    blur: boolean;
    onWheel: WheelEventHandler;
    scroller: RefObject<HTMLDivElement | null>;
    children: ReactNode;
    after?: ReactNode;
}

export const PreviewDialogRoot = kotlinBridge.createPreviewDialogRoot({
    react, Dialog, overlay, cn, useUiStore, usePreviewStore, modalSelector: MODAL_OVERLAY_SELECTOR
}) as (props: PreviewDialogProps) => ReactNode;
