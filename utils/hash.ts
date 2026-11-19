import {kotlinBridge} from "@/kotlin/bridge";

/** SHA-256 (16진수 소문자). 클라우드 백업의 무결성 확인과 목록 변경 검사가 쓴다. */
export const sha256 = async (data: BufferSource): Promise<string> =>
    new Uint8Array(await crypto.subtle.digest("SHA-256", data)).toHex();

/** 빠른 53비트 해시(cyrb53). 실사용 구현은 Kotlin/JS. */
export const hash53 = kotlinBridge.hash53;
