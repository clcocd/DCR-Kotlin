import QuickLRU from "quick-lru";

import {ajax} from "@/core/http/client";
import {urls} from "@/core/http/urls";
import {csrfBody} from "@/core/http/cookie";
import {kotlinBridge} from "@/kotlin/bridge";

import type {DcinsideDcconDetailList, DcinsideDcconPackage} from "./types";

/**
 * 디시콘 관련 캐시의 수명. 창을 닫았다 열어도 다시 받지 않고, 새로 산 디시콘이 보이도록 이 뒤에는 다시 받는다.
 */
const DCCON_TTL = 10 * 60_000;

/**
 * 디시콘 코드별 패키지 정보. 같은 디시콘을 다시 눌러도 받지 않는다. 정보 창과 디시콘 버블이 같이 쓴다.
 */
export const dcconPackages = new QuickLRU<string, DcinsideDcconPackage>({maxSize: 100, maxAge: DCCON_TTL});

type DcconListResult = DcinsideDcconDetailList[] | "not_login" | "shop";

interface DcconRequests {
    cachedDcconList(): DcinsideDcconDetailList[] | null;
    setCachedDcconList(list: DcinsideDcconDetailList[]): void;
    clearDcconCaches(): void;
    fetchDcconPackage(code: string, signal?: AbortSignal): Promise<DcinsideDcconPackage>;
    getDcconPackage(code: string, signal?: AbortSignal): Promise<DcinsideDcconPackage>;
    addDcconPackage(id: string | number): Promise<"ok" | "fail" | "not_login">;
    fetchDcconList(signal: AbortSignal): Promise<DcconListResult>;
}
export const {cachedDcconList, setCachedDcconList, clearDcconCaches, fetchDcconPackage, getDcconPackage, addDcconPackage, fetchDcconList} =
    kotlinBridge.createDcconRequests<DcconRequests>({ajax, csrfBody, urls: urls.dccon, cache: dcconPackages});
