import {afterEach, describe, expect, it, vi} from "vitest";

import {supplementYoutubeEmbeds} from "@/features/preview/postLoader";
import {installExternalEmbeds} from "@/features/preview/ui/usePreviewMedia";
import {sanitizeHtml} from "@/utils/sanitize";

const wrap = (html: string): HTMLElement => {
    const root = document.createElement("div");
    root.innerHTML = html;
    document.body.append(root);
    return root;
};

afterEach(() => {
    vi.useRealTimers();
    document.body.replaceChildren();
});

describe("최종 YouTube 패치", () => {
    it("watch/live/shorts/youtu.be에서 빠진 iframe만 링크 앞에 보충한다", () => {
        const urls = [
            "https://www.youtube.com/watch?v=abcdefghijk",
            "https://www.youtube.com/live/ABCDEFGHIJK",
            "https://www.youtube.com/shorts/0123456789_",
            "https://youtu.be/1234567890-"
        ];
        const root = wrap(supplementYoutubeEmbeds(urls.map((url) => `<p><a href="${url}">링크</a></p>`).join("")));
        const frames = [...root.querySelectorAll("iframe")];
        expect(frames).toHaveLength(4);
        expect(frames.map((node) => node.getAttribute("src"))).toEqual([
            "https://www.youtube.com/embed/abcdefghijk",
            "https://www.youtube.com/embed/ABCDEFGHIJK",
            "https://www.youtube.com/embed/0123456789_",
            "https://www.youtube.com/embed/1234567890-"
        ]);
        expect(frames[2]?.classList.contains("dc-video-portrait")).toBe(true);
        expect(root.querySelectorAll("p a")).toHaveLength(4);
        expect(root.firstElementChild?.tagName).toBe("IFRAME");
    });

    it("기존 iframe과 같은 ID는 중복하지 않고 OG 카드 링크는 무시한다", () => {
        const html = '<div class="dc-external-video"><iframe src="https://www.youtube.com/embed/abcdefghijk"></iframe><a href="https://youtu.be/abcdefghijk">원본</a></div>'
            + '<div class="og-div"><a href="https://youtu.be/0123456789_">OG</a></div>'
            + '<a class="og-wrap" href="https://youtu.be/1234567890-">카드</a>'
            + '<p><a href="https://youtube.com.evil.example/watch?v=ABCDEFGHIJK">가짜</a></p>';
        const root = wrap(supplementYoutubeEmbeds(html));
        expect(root.querySelectorAll("iframe")).toHaveLength(1);
        expect(root.querySelector(".og-div")?.textContent).toBe("OG");
        expect(root.querySelector(".og-wrap")?.textContent).toBe("카드");
    });

    it("미디어 제거 모드에서는 보충된 iframe도 정화 단계에서 제거된다", () => {
        const source = supplementYoutubeEmbeds('<p><a href="https://youtu.be/abcdefghijk">원본</a></p>');
        const result = sanitizeHtml(source, {stripMedia: true});
        expect(result).not.toContain("<iframe");
        expect(result).toContain("원본");
    });
});

describe("X·Instagram 미리보기 임베드", () => {
    it("X 상태 링크는 중복 생성 없이 래퍼와 원본 링크를 만들며 종료 시 복원한다", () => {
        const root = wrap('<p><a href="https://x.com/test/status/1234567890123456789">게시물</a></p>'
            + '<div class="x-wrap"><a href="https://x.com/test/status/25">이미 있음</a></div>');
        const dispose = installExternalEmbeds(root);
        expect(root.querySelectorAll(".x-wrap")).toHaveLength(2);
        const stage = root.querySelector<HTMLElement>(".refresher-x-stage")!;
        expect(stage.dataset.refresherXId).toBe("1234567890123456789");
        expect(root.querySelector<HTMLAnchorElement>(".refresher-x-original")?.href).toBe("https://x.com/test/status/1234567890123456789");
        stage.dispatchEvent(new Event("refresher-x-error"));
        expect(root.querySelector<HTMLElement>(".x-wrap[data-refresher-x-state=error]")).not.toBeNull();
        dispose();
        expect(root.querySelectorAll(".refresher-x-stage")).toHaveLength(0);
        expect(root.querySelector("p a")?.textContent).toBe("게시물");
    });

    it("X 응답이 없으면 5초에 원본 링크를 남기고 안내를 표시한다", async () => {
        vi.useFakeTimers();
        const root = wrap('<a href="https://twitter.com/test/status/123456">게시물</a>');
        const dispose = installExternalEmbeds(root);
        await vi.advanceTimersByTimeAsync(5000);
        expect(root.querySelector(".x-wrap")?.getAttribute("data-refresher-x-state")).toBe("error");
        expect(root.querySelector(".refresher-x-notice")?.textContent).toContain("표시하지 못했습니다");
        expect(root.querySelector(".refresher-x-original")).not.toBeNull();
        dispose();
    });

    it("Instagram 인용문은 검증된 게시물 주소만 iframe으로 바꾸며 captioned를 유지한다", () => {
        const root = wrap('<blockquote class="instagram-media" data-instgrm-permalink="https://www.instagram.com/reel/Aa1_B2/" data-instgrm-captioned></blockquote>'
            + '<blockquote class="instagram-media"><a href="https://www.instagram.com/p/ABC123/">글</a></blockquote>'
            + '<blockquote class="instagram-media" data-instgrm-permalink="https://instagram.com.evil.example/p/FAIL/"></blockquote>');
        const dispose = installExternalEmbeds(root);
        expect([...root.querySelectorAll("iframe.refresher-instagram-embed")].map((f) => f.getAttribute("src"))).toEqual([
            "https://www.instagram.com/reel/Aa1_B2/embed/captioned/",
            "https://www.instagram.com/p/ABC123/embed/"
        ]);
        expect(root.querySelectorAll("blockquote.instagram-media")).toHaveLength(1);
        dispose();
        expect(root.querySelectorAll("iframe.refresher-instagram-embed")).toHaveLength(0);
        expect(root.querySelectorAll("blockquote.instagram-media")).toHaveLength(3);
    });
});
