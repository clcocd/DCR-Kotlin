/** IP 대역(a.b) → 후보 데이터. 인코더와 디코더를 한 파일에 둔다. */

import {kotlinBridge} from "@/kotlin/bridge";

/** 저장 형식 버전. 메타에 두고 바뀌면 같은 DB 버전이어도 다시 받는다. */
export const IP_FORMAT = 2;

export interface CompactIpData {
    /** 만든 시각. version 파일과 따로 캐시되어 저장 버전은 이것을 쓴다. */
    version?: string;
    /** 65536칸 표를 같은 값 구간으로 줄인 base64. 값-1이 meta 수 미만이면 meta 번호, 아니면 meta 수+lists 번호. */
    runs: string;
    /** 조직명. ""는 조직 없음 (국가만 아는 해외 대역 등). */
    orgs: string[];
    /** 국가명. ""는 한국 (배지에서 국가를 생략한다). */
    countries: string[];
    /** [조직 번호, 국가 번호, VPN(0|1)] × meta 개수. */
    meta: number[];
    /** 후보가 여럿인 대역의 meta 번호 목록 (같은 목록은 한 번만). */
    lists: number[][];
}

export interface IpCandidate {
    org?: string;
    /** 없으면 한국. */
    country?: string;
    vpn: boolean;
}

export const {ipCandidateKey: candidateKey, encodeIpData, parseIpData, parseStoredIpData, createIpLookup} = kotlinBridge;
