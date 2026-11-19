import {kotlinBridge} from "@/kotlin/bridge";

/** 저장값이나 JSON에서 배열이 아닌 객체인지 Kotlin/JS로 판정한다. */
export const isRecord = (value: unknown): value is Record<string, unknown> => kotlinBridge.isJsRecord(value);
