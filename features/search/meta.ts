import {defineModuleMeta} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import {LIST_PAGE, VIEW_PAGE} from "@/core/pages";

// 페이지 RegExp의 동일 참조를 유지하며 WXT 메타 등록만 TypeScript에서 수행한다.
export default defineModuleMeta(kotlinBridge.searchMeta([LIST_PAGE, VIEW_PAGE]));
