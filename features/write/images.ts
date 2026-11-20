import pLimit from "p-limit";

import {kotlinBridge} from "@/kotlin/bridge";

/** 올리는 이미지를 바꾸는 방법. 모듈이 <html>의 data 속성에 JSON으로 두고, 페이지에 넣은 hookUploads가 이벤트마다 읽는다. */
export interface ImageOptions {
    webp: boolean;
    /** WebP 품질 (0~1). */
    quality: number;
    rename: boolean;
    /** 한꺼번에 바꾸는 이미지 수. 원본 크기 캔버스가 이만큼 함께 살아 있다. */
    concurrency: number;
}

export const UPLOAD_OPTIONS_KEY = kotlinBridge.uploadOptionsKey;

export const hookUploads = (): void => kotlinBridge.hookUploads(pLimit);
