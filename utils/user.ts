import {kotlinBridge} from "@/kotlin/bridge";

/** 작성자 닉콘 종류 구분은 Kotlin/JS에서 실행한다. */
export const nickType = kotlinBridge.nickType;

/** 갤러리 관리 권한이 있는지. 관리 버튼, 체크박스 열, 매니저 말머리 탭으로 판단한다(매니저 탭은 매니저에게만 보인다). */
export const isGalleryManager = (): boolean =>
    Boolean(document.querySelector(".useradmin_btnbox button, .gall_list .chkbox_th, a[onclick^=\"listSearchHead(999)\"]"));

/** 로그인한 계정 ID. 로그인 박스 갤로그 아이콘의 onclick(window.open('//gallog.dcinside.com/<id>'))에서 읽는다. */
export const loggedInUserId = (): string | undefined =>
    document.querySelector("#login_box .user_info .writer_nikcon")?.getAttribute("onclick")?.match(/gallog\.dcinside\.com\/([\w-]+)/)?.[1];
