import {describe, expect, it} from "vitest";
import {adaptPreviewColors} from "@/features/preview/postLoader";
import {sanitizeHtml} from "@/utils/sanitize";

const parse = (source: string): HTMLElement => {
    const root = document.createElement("div");
    root.innerHTML = adaptPreviewColors(sanitizeHtml(source));
    return root;
};

describe("미리보기 사용자 지정 색상", () => {
    it("font[color]과 인라인 글자색을 사용자 변수로 보존한다", () => {
        const root = parse('<font color="#ff0000">글자</font><span style="color:rgb(1, 2, 3)">다른 글자</span>');
        const font = root.querySelector("font")!;
        const span = root.querySelector("span")!;
        expect(font.dataset.refresherCustomColor).toBe("");
        expect(font.style.getPropertyValue("--refresher-user-text-color")).toBe("#ff0000");
        expect(span.dataset.refresherCustomColor).toBe("");
        expect(span.style.getPropertyValue("--refresher-user-text-color")).toBe("rgb(1, 2, 3)");
    });

    it("검정·흰색은 중립색으로 표시하고 배경색도 분리해 보존한다", () => {
        const root = parse('<span style="color:#000;background-color:#fff">무채색</span>'
            + '<strong style="color:#12ab34;background-color:rgb(100, 50, 20)">유채색</strong>');
        const neutral = root.querySelector("span")!;
        const colored = root.querySelector("strong")!;
        expect(neutral.hasAttribute("data-refresher-neutral-color")).toBe(true);
        expect(neutral.hasAttribute("data-refresher-neutral-background")).toBe(true);
        expect(colored.hasAttribute("data-refresher-neutral-color")).toBe(false);
        expect(colored.style.getPropertyValue("--refresher-user-background-color")).toBe("rgb(100, 50, 20)");
    });

    it("표 구조·OG 카드·스포일러와 일반 본문 내용은 변경하지 않는다", () => {
        const source = '<table><tbody><tr><td style="color:red">셀</td></tr></tbody></table>'
            + '<a class="og-wrap" href="https://example.com">OG</a><div id="spoiler_warning">경고</div><p>본문</p>';
        const root = parse(source);
        expect(root.querySelector("table tr td")?.textContent).toBe("셀");
        expect(root.querySelector("a.og-wrap")?.textContent).toBe("OG");
        expect(root.querySelector("#spoiler_warning")?.textContent).toBe("경고");
        expect(root.querySelector("p")?.textContent).toBe("본문");
    });
});
