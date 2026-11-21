import {expect, test} from "./fixtures";

test("답글 입력칸은 댓글 바로 아래에 뜨고 하단 일반 댓글과 초안을 분리한다", async ({listPage}) => {
    const frame = await listPage.openPreview();
    const general = frame.getByRole("textbox", {name: "댓글 입력", exact: true});
    await general.fill("일반 댓글 초안");

    const first = listPage.comments.nth(0);
    await first.locator(".refresher-comment-html").click();
    const inline = frame.locator(".refresher-inline-reply");
    await expect(inline).toHaveCount(1);
    await expect(first.locator("xpath=following-sibling::*[1]")).toHaveClass(/refresher-inline-reply/);
    const reply = frame.getByRole("textbox", {name: "답글 입력", exact: true});
    await expect(reply).toBeFocused();
    await reply.fill("첫 댓글 답글 초안");
    await expect(general).toHaveValue("일반 댓글 초안");

    await listPage.comments.nth(1).getByRole("button", {name: "ㅇㅇ 댓글에 답글", exact: true}).click();
    await expect(reply).toHaveValue("");
    await reply.fill("둘째 댓글 답글 초안");
    await first.getByRole("button", {name: "고닉 댓글에 답글", exact: true}).click();
    await expect(reply).toHaveValue("첫 댓글 답글 초안");
    await expect(general).toHaveValue("일반 댓글 초안");

    await first.locator(".refresher-comment-html").click();
    await expect(inline).toHaveCount(0);
    await expect(general).toHaveValue("일반 댓글 초안");
});
