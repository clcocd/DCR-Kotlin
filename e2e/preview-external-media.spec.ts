import {expect, test} from "./fixtures";

test("개인 패치 외부 미디어: YouTube·X·Instagram을 본문 손실 없이 구성한다", async ({listPage, site}) => {
    site.postExtra = '<p><a href="https://www.youtube.com/shorts/abcdefghijk">YouTube Shorts 원본</a></p>'
        + '<div class="dc-external-video"><iframe src="https://www.youtube.com/embed/ABCDEFGHIJK"></iframe>'
        + '<a class="lnk" href="https://youtu.be/ABCDEFGHIJK">기존 플레이어 원본</a></div>'
        + '<div class="og-div"><a href="https://youtu.be/1234567890-">OG 링크</a></div>'
        + '<p><a href="https://x.com/example/status/1234567890123456789">X 원본</a></p>'
        + '<blockquote class="instagram-media" data-instgrm-permalink="https://www.instagram.com/p/ABC123/" data-instgrm-captioned></blockquote>'
        + '<p>보존할 본문</p>';
    const frame = await listPage.openPreview();
    const body = frame.locator(".refresher-preview-contents");
    await expect(body.locator('iframe[src="https://www.youtube.com/embed/abcdefghijk"]')).toHaveCount(1);
    await expect(body.locator('iframe[src="https://www.youtube.com/embed/ABCDEFGHIJK"]')).toHaveCount(1);
    await expect(body.locator("iframe.dc-video-portrait")).toHaveCount(1);
    await expect(body.locator(".dc-external-video a.lnk")).toContainText("기존 플레이어 원본");
    await expect(body.locator(".x-wrap")).toHaveCount(1);
    await expect(body.locator(".refresher-x-original")).toHaveAttribute("href", "https://x.com/example/status/1234567890123456789");
    await expect(body.locator(".refresher-instagram-embed")).toHaveAttribute("src", "https://www.instagram.com/p/ABC123/embed/captioned/");
    await expect(body).toContainText("보존할 본문");
    await listPage.closePreview();
});
