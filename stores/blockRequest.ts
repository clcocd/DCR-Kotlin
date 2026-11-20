// 유저 버블의 "차단" 처리. 차단 모듈이 꺼져 있어도 차단 목록(stores/blocks.ts)에는 넣으므로 모듈이 아닌 stores에 둔다.
import {runningModuleSettings} from "@/core/module/registry";
import {getDcconPackage} from "@/core/preview/request";
import {TYPE_NAMES} from "@/core/storage/items";
import {kotlinBridge} from "@/kotlin/bridge";
import {useBlocksStore} from "@/stores/blocks";
import {reportFailure} from "@/stores/notify";
import {type SelectedUser, useUiStore} from "@/stores/ui";

/** 패키지 전체 차단 방식. bundle: 정규식 한 항목으로 묶는다, each: 디시콘마다 한 항목 (따로 풀 수 있다). */
type DcconPackageMode = "bundle" | "each";

export type BlockRequestOptions = {
    target: "user" | "dccon";
    /** 디시콘이 든 패키지 전체를 차단한다. 없으면 그 디시콘 하나만. */
    dcconPackage?: DcconPackageMode;
    /** 유저 차단을 이 갤러리 id에서만 건다. 없으면 모든 갤러리. */
    gallery?: string;
};

export const handleBlockRequest = kotlinBridge.createBlockRequest({
    blockStore: useBlocksStore, uiStore: useUiStore, getDcconPackage, runningModuleSettings, typeNames: TYPE_NAMES, reportFailure
}) satisfies (options: BlockRequestOptions, selected: SelectedUser) => Promise<void>;
