// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {afterEach, expect, it} from "vitest";

import manage from "@/features/manage/index";
import {runModule, type Running} from "../module";

let running: Running<void> | undefined;

afterEach(() => {
    running?.stop();
    running = undefined;
    document.body.innerHTML = "";
});

it("GIF 동영상의 기존 onmousedown를 보존하고 설정 해제와 모듈 종료 때 복원한다", async () => {
    document.body.innerHTML = `<div class="gallview_contents">
        <video id="gif" onmousedown="return false"></video>
        <video id="dccon" data-src="https://dcinside.com/dccon.php?no=1"></video>
    </div>`;
    running = await runModule(manage, {enableGifControl: true});
    const gif = document.querySelector<HTMLVideoElement>("#gif")!;
    const dccon = document.querySelector<HTMLVideoElement>("#dccon")!;

    expect(gif.hasAttribute("controls")).toBe(true);
    expect(gif.hasAttribute("onmousedown")).toBe(false);
    expect(dccon.hasAttribute("controls")).toBe(false);

    running.change({enableGifControl: false});
    expect(gif.hasAttribute("controls")).toBe(false);
    expect(gif.getAttribute("onmousedown")).toBe("return false");

    running.change({enableGifControl: true});
    expect(gif.hasAttribute("controls")).toBe(true);
    running.stop();
    running = undefined;
    expect(gif.getAttribute("onmousedown")).toBe("return false");
    expect(gif.hasAttribute("controls")).toBe(false);
});

it("Shift+마우스 드래그로 체크하고 Ctrl+댓글 체크박스로 대댓글도 체크한다", async () => {
    document.body.innerHTML = `<ul class="cmt_list">
        <li id="comment_li_22"><input class="article_chkbox" type="checkbox"></li>
        <li><ul id="reply_list_22"><li><input class="article_chkbox" type="checkbox"></li></ul></li>
    </ul>
    <table class="gall_list"><tbody><tr class="ub-content">
      <td><input id="drag" class="article_chkbox" type="checkbox"></td>
    </tr></tbody></table>`;
    running = await runModule(manage, {checkViaShift: true, checkCommentViaCtrl: true});

    const drag = document.querySelector<HTMLInputElement>("#drag")!;
    drag.dispatchEvent(new MouseEvent("mouseover", {bubbles: true, shiftKey: true, buttons: 1}));
    expect(drag.checked).toBe(true);

    const comment = document.querySelector<HTMLInputElement>("#comment_li_22 .article_chkbox")!;
    const reply = document.querySelector<HTMLInputElement>("#reply_list_22 .article_chkbox")!;
    comment.checked = true;
    comment.dispatchEvent(new MouseEvent("click", {bubbles: true, ctrlKey: true}));
    expect(reply.checked).toBe(true);
});
