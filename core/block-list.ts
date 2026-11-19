import {kotlinBridge} from "@/kotlin/bridge";
/** 차단 목록 값의 검사, 정규화. core에서 stores를 불러오면 React가 배경 번들에 딸려 간다. */
import type {BlockEntry, BlockType, DetectMode} from "@/core/storage/types";

export type BlockInputFields = Omit<BlockEntry, "id">;

const isBlockEntry = (value: unknown): value is Omit<BlockEntry, "id"> & { id?: unknown } =>
    isRecord(value) &&
    // 빈 내용은 포함 검사에서 모든 글에 맞는다. 다이얼로그는 막지만 가져오기, 옛 데이터로 들어올 수 있다.
    typeof value.content === "string" &&
    value.content.trim() !== "" &&
    typeof value.isRegex === "boolean" &&
    (value.gallery === undefined || typeof value.gallery === "string") &&
    (value.extra === undefined || typeof value.extra === "string") &&
    (value.mode === undefined || arrayIncludes(DETECT_MODES, value.mode));

/** 항목 플래그 표시 형식은 Kotlin/JS에서 처리한다. */
export const composeExtra = (fields: {isRegex: boolean; gallery?: string; mode?: DetectMode}): string =>
    kotlinBridge.composeExtra(fields.isRegex, fields.gallery, fields.mode);

/** 같은 content+gallery는 한 항목이다. 키 직렬화는 Kotlin/JS에서 처리한다. */
export const blockKey = ({content, gallery}: BlockInputFields): string => kotlinBridge.blockKey(content, gallery);

/** 차단 목록 스키마 검사·ID 유지·중복 ID 교체는 Kotlin/JS에서 수행한다. */
export const normalizeBlockList = (value: unknown, known: BlockEntry[] = []): BlockEntry[] =>
    kotlinBridge.normalizeBlockList(value, known);

/** 유형별 기본 모드와 유효한 저장값 정규화는 Kotlin/JS에서 수행한다. */
export const normalizeDefaults = (value: unknown): Record<BlockType, DetectMode> =>
    kotlinBridge.normalizeDefaults(value);
