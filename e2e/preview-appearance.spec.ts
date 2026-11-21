import {expect, test} from "./fixtures";

test("개인 패치: 원문 작성 기기 아이콘을 미리보기 제목에 복제한다", async ({listPage, site}) => {
    site.deviceHtml = '<span class="title_device"><span style="color:blue">PC</span></span>';
    const frame = await listPage.openPreview();
    await expect(frame.locator(".refresher-device-icon .title_device")).toContainText("PC");
    await listPage.closePreview();
});

test("개인 패치: 미리보기 사용자 지정 색상과 무채색을 보존한다", async ({listPage, site}) => {
    site.postExtra = '<div><font color="#ff0000">사용자 글자색</font></div>'
        + '<table><tbody><tr><td style="color:#000">표 내용</td></tr></tbody></table>';
    const frame = await listPage.openPreview();
    const body = frame.locator(".refresher-preview-contents");
    await expect(body.locator('font[color="#ff0000"]')).toHaveAttribute("data-refresher-custom-color", "");
    await expect(body.locator("td")).toHaveAttribute("data-refresher-neutral-color", "");
    await expect(body.locator("table")).toContainText("표 내용");
    await listPage.closePreview();
});

test("개인 패치: OG 카드 크기를 유지한다", async ({listPage, site}) => {
    site.postExtra = '<a class="og-wrap" href="https://example.com"><div class="og-img"><img src="https://dcimg1.dcinside.com/example.jpg"></div>'
        + '<div class="og-info"><strong class="og-tit">카드 제목</strong></div></a>';
    const frame = await listPage.openPreview();
    const body = frame.locator(".refresher-preview-contents");
    await expect(body.locator("a.og-wrap")).toHaveCSS("height", "105px");
    await expect(body.locator("div.og-img")).toHaveCSS("width", "104px");
    await listPage.closePreview();
});

test("개인 패치: 스포일러 경고 본체와 여백을 보존한다", async ({listPage, site}) => {
    site.postExtra = '<div id="spoiler_warning"><img src="https://dcimg1.dcinside.com/warning.png" alt="경고"></div>';
    const frame = await listPage.openPreview();
    const body = frame.locator(".refresher-preview-contents");
    await expect(body.locator("#spoiler_warning")).toHaveCSS("padding-top", "85px");
    await expect(body.locator("#spoiler_warning")).toHaveCSS("padding-bottom", "110px");
    await listPage.closePreview();
});

test("개인 패치: 지정 가로 여백은 25px이다", async ({listPage}) => {
    const frame = await listPage.openPreview();
    await expect(frame.locator(".px-8").first()).toHaveCSS("padding-left", "25px");
    await listPage.closePreview();
});
