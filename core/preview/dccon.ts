import QuickLRU from "quick-lru";

import {ajax} from "@/core/http/client";
import {urls} from "@/core/http/urls";
import {csrfBody} from "@/core/http/cookie";
import {kotlinBridge} from "@/kotlin/bridge";

import type {DcinsideDccon, DcinsideDcconDetailList, DcinsideDcconPackage} from "./types";

/**
 * 디시콘 코드별 패키지 정보. Kotlin이 원본 용량·만료 시간 설정을 만들고
 * TypeScript에서는 QuickLRU 생성자만 전달한다.
 */
export const dcconPackages = kotlinBridge.createDcconPackageCache<DcinsideDcconPackage>(
    (options) => new QuickLRU<string, DcinsideDcconPackage>(options)
);

type DcconListResult = DcinsideDcconDetailList[] | "not_login" | "shop";

interface DcconRequests {
    cachedDcconList(): DcinsideDcconDetailList[] | null;
    setCachedDcconList(list: DcinsideDcconDetailList[]): void;
    clearDcconCaches(): void;
    fetchDcconPackage(code: string, signal?: AbortSignal): Promise<DcinsideDcconPackage>;
    getDcconPackage(code: string, signal?: AbortSignal): Promise<DcinsideDcconPackage>;
    addDcconPackage(id: string | number): Promise<"ok" | "fail" | "not_login">;
    fetchDcconList(signal: AbortSignal): Promise<DcconListResult>;
    fetchRecentDccons(signal: AbortSignal): Promise<(DcinsideDccon | DcinsideDcconDetailList)[] | "not_login">;
}
export const {cachedDcconList, setCachedDcconList, clearDcconCaches, fetchDcconPackage, getDcconPackage, addDcconPackage, fetchDcconList, fetchRecentDccons} =
    kotlinBridge.createDcconRequests<DcconRequests>({ajax, csrfBody, urls: urls.dccon, cache: dcconPackages});
