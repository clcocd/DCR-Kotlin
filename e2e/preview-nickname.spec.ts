import {fakeComment} from "./dcinside";
import {expect, test} from "./fixtures";

test("미리보기 댓글의 익명 HTML 닉네임 우선 표시와 기존 이름 폴백", async ({listPage, site}) => {
    site.comments.push(
        fakeComment(12, {name: "기본닉", gallog_icon: "<span class='nickname an'><em>HTML&amp;닉</em></span>"}),
        fakeComment(13, {name: "빈닉 대체", gallog_icon: "<span class='nickname an'></span>"}),
        fakeComment(14, {name: "일반닉 유지", gallog_icon: "<span class='nickname'><em>다른 이름</em></span>"})
    );
    await listPage.openPreview();
    await expect(listPage.comments).toHaveCount(5);
    await expect(listPage.comments.nth(2).locator(".refresher-user")).toContainText("HTML&닉");
    await expect(listPage.comments.nth(3).locator(".refresher-user")).toContainText("빈닉 대체");
    await expect(listPage.comments.nth(4).locator(".refresher-user")).toContainText("일반닉 유지");
});
