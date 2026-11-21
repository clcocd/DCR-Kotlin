import {expect, test} from "./fixtures";

test("개인 패치 본문 표시: 기기 아이콘·OG 카드·스포일러·색상·25px 여백", async ({listPage, site}) => {
    site.deviceHtml = '<span class="title_device"><span style="color:blue">PC</span></span>';
    site.postExtra = '<div><font color="#ff0000">사용자 글자색</font></div>'
        + '<a class="og-wrap" href="https://example.com"><div class="og-img"><img src="https://dcimg1.dcinside.com/example.jpg"></div>'
        + '<div class="og-info"><strong class="og-tit">카드 제목</strong></div></a>'
        + '<div id="spoiler_warning"><img src="https://dcimg1.dcinside.com/warning.png" alt="경고"></div>'
        + '<table><tbody><tr><td style="color:#000">표 내용</td></tr></tbody></table>';

    const frame = await listPage.openPreview();
    const device = frame.locator(".refresher-device-icon .title_device");
    await expect(device).toContainText("PC");
    await expect(frame.locator(".gallview_head")).toHaveCount(0); // 원문 스타일 계산용 임시 노드는 화면에 남지 않는다.

    const body = frame.locator(".refresher-preview-contents");
    await expect(body.locator('font[color="#ff0000"]')).toHaveAttribute("data-refresher-custom-color", "");
    await expect(body.locator("td")).toHaveAttribute("data-refresher-neutral-color", "");
    await expect(body.locator("table")).toContainText("표 내용");
    await expect(body.locator("a.og-wrap")).toHaveCSS("height", "105px");
    await expect(body.locator("div.og-img")).toHaveCSS("width", "104px");
    await expect(body.locator("#spoiler_warning")).toHaveCSS("padding-top", "85px");
    await expect(body.locator("#spoiler_warning")).toHaveCSS("padding-bottom", "110px");
    await expect(frame.locator(".refresher-frame .px-8").first()).toHaveCSS("padding-left", "25px");
    await listPage.closePreview();
});
