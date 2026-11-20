import {defineModuleMeta} from "@/core/module/define";
import {kotlinBridge} from "@/kotlin/bridge";
import {WRITE_PAGE} from "@/core/pages";

// 글쓰기의 이미지 버튼이 여는 이미지 올리기 팝업.
const UPLOAD_POPUP = /\/upload\/image/;

export default defineModuleMeta({
    id: "write",
    urls: [WRITE_PAGE, UPLOAD_POPUP],
    defaultEnable: false,

    settings: kotlinBridge.staticFeatureSettings("write")
});
