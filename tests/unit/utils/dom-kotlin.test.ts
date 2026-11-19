import {afterEach, describe, expect, it} from "vitest";
import {textWithoutScripts, writeStyle} from "@/utils/dom";
import {followDcAppearance} from "@/utils/appearance";

afterEach(() => {
    document.querySelectorAll("style#refresher-kotlin-test").forEach((style) => style.remove());
});

describe("Kotlin DOM helpers", () => {
    it("직접 자식 script는 제외하고 다른 텍스트는 순서대로 읽는다", () => {
        const container = document.createElement("div");
        container.innerHTML = " 앞 <script>무시</script><span>뒤</span><b> 끝 </b>";
        expect(textWithoutScripts(container)).toBe("앞 뒤 끝");
        expect(textWithoutScripts(undefined)).toBe("");
    });

    it("같은 style 노드를 재사용하고 정리 함수를 반환한다", () => {
        const removeFirst = writeStyle("refresher-kotlin-test", "body { color: red; }");
        const first = document.querySelector<HTMLStyleElement>("style#refresher-kotlin-test")!;
        expect(first.textContent).toBe("body { color: red; }");

        const removeSecond = writeStyle("refresher-kotlin-test", "body { color: blue; }");
        expect(document.querySelector("style#refresher-kotlin-test")).toBe(first);
        expect(first.textContent).toBe("body { color: blue; }");

        removeFirst();
        expect(first.isConnected).toBe(false);
        removeSecond();
    });

    it("다크 모드 추가·제거에 따라 class와 color-scheme을 같이 갱신한다", async () => {
        const root = document.createElement("div");
        const stop = followDcAppearance(root);
        try {
            expect(root.classList.contains("light")).toBe(true);
            expect(root.style.colorScheme).toBe("light");
            const link = Object.assign(document.createElement("link"), {id: "css-darkmode"});
            document.body.append(link);
            await Promise.resolve();
            expect(root.classList.contains("dark")).toBe(true);
            expect(root.classList.contains("light")).toBe(false);
            expect(root.style.colorScheme).toBe("dark");
            link.remove();
            await Promise.resolve();
            expect(root.style.colorScheme).toBe("light");
        } finally {
            stop();
            document.getElementById("css-darkmode")?.remove();
        }
    });
});
