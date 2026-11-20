import {kotlinBridge} from "@/kotlin/bridge";
import QuickLRU from "quick-lru";

import {ajax} from "@/core/http/client";
import {csrfBody} from "@/core/http/cookie";
import {sendMessage} from "@/core/messaging/protocol";

export interface GallogActivity {
    article: number;
    comment: number;
}

/** 글댓비 캐시(userinfo)의 한 사람 값. date는 받은 시각이다. */
export interface RatioInfo extends GallogActivity {
    date: number;
}

/** 글댓비 저장소 모양. */
export interface RatioData {
    ratio?: Record<string, RatioInfo>;
}

interface GallogService {
    trimRatios(all: Record<string, RatioInfo>): Record<string, RatioInfo>;
    getGallogActivity(uid: string): Promise<GallogActivity | undefined>;
    getGallogActivityUnlessBusy(uid: string): Promise<GallogActivity | null | undefined>;
    onGallogActivity(listener: (uid: string, activity: GallogActivity) => void): () => void;
}

export const {trimRatios, getGallogActivity, getGallogActivityUnlessBusy, onGallogActivity} =
    kotlinBridge.createGallog<GallogService>({
        ajax, csrfBody, sendMessage, cache: new QuickLRU<string, Promise<GallogActivity | undefined>>({maxSize: 500, maxAge: 3_600_000})
    });
