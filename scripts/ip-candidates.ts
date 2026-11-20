import {kotlinBridge} from "../kotlin/bridge";

/** IPv4 주소 구간 [start, end]와 그 값. 목록은 start 순이고 겹치지 않는다. */
export interface Range<T> {
    start: number;
    end: number;
    value: T;
}

export interface Asn {
    asn: number;
    org: string;
}

export const {buildIpCandidates: buildCandidates} = kotlinBridge;
