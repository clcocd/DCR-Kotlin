import {defineModuleMeta} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import {WRITE_PAGE} from "@/core/pagePatterns";

// 글쓰기의 이미지 버튼이 여는 이미지 올리기 팝업.
const UPLOAD_POPUP = /\/upload\/image/;

// WXT 페이지 정규식은 그대로 전달하고 글쓰기 메타 값은 Kotlin에서 관리한다.
export default defineModuleMeta(kotlinBridge.writeMeta([WRITE_PAGE, UPLOAD_POPUP]));
