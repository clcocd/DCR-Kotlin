import type {ModuleSettings} from "@/core/module/types";
import type {BlockEntry, BlockType} from "@/core/storage/types";
import {kotlinBridge} from "@/kotlin/bridge";
import {useBlocksStore} from "@/stores/blocks";

type BlockLists = Pick<ReturnType<typeof useBlocksStore.getState>, "entries" | "defaults">;
type BlockValues = Partial<Record<BlockType, string | null | undefined>>;

/** 확장이 흐리게 가린 행 (차단 블러, userinfo의 깡계 흐림). 흐린 행은 보이므로 checkVisibility로 가릴 수 없다. */
export const BLURRED_ROW_SELECTOR = ".refresherBlur, .refresherLowActivityBlur";

/** 확장이 가린 행 (차단, 깡계). 클래스는 assets/styles/content.css가 그린다. */
export const HIDDEN_ROW_SELECTOR = `.refresherBlocked, .refresherLowActivityHide, ${BLURRED_ROW_SELECTOR}`;

/** userinfo가 깡계 상태를 바꿨을 때 보내는 이벤트. 차단 모듈이 같은 댓글을 다시 접는다. */
export const ROWS_HIDDEN_EVENT = "refresher:rowsHidden";

/** 본문 차단 안내 문구 (페이지, 미리보기 공용). */
export const BLOCKED_TEXT = "게시글 내용이 차단되었습니다.";

interface BlockRules {
    dcconCode(element: HTMLElement): string | undefined;
    blockMode(settings: ModuleSettings["block"]): "blur" | "hide";
    isBlockedHidden(element: Element): boolean;
    isBlocked(type: BlockType, content: string, gallery?: string): boolean;
    isAnyBlocked(values: BlockValues, gallery?: string): boolean;
    duplicateOf(settings: ModuleSettings["block"]): {count: number; minLength: number} | null;
    groupDuplicates<T>(items: T[], textOf: (item: T) => string, options: {count: number; minLength: number}): Map<T, number>;
    blockingEntries(values: BlockValues, gallery?: string, lists?: BlockLists): {type: BlockType; entry: BlockEntry}[];
}

export const {dcconCode, blockMode, isBlockedHidden, isBlocked, isAnyBlocked, duplicateOf, groupDuplicates, blockingEntries} =
    kotlinBridge.createBlockRules<BlockRules>({getLists: useBlocksStore.getState});
