// @vitest-environment node
import {expect, it, vi} from "vitest";

import {kotlinBridge} from "@/kotlin/bridge";

it("Kotlin 이미지 검색 메뉴 클릭은 해당 엔진 URL을 만들고 원본 탭 바로 뒤에 연다", async () => {
    let click!: (
        info: {menuItemId: string | number; srcUrl?: string},
        tab?: {id?: number; index: number; windowId: number}
    ) => Promise<unknown>;
    const createTab = vi.fn(async (details: {url: string; index?: number; openerTabId?: number; windowId?: number}) => details);
    const impl = kotlinBridge.createImageSearch({
        engineIds: ["saucenao"],
        engines: {saucenao: {name: "SauceNAO", url: "https://saucenao.com/search.php?url="}},
        patterns: ["*://*.dcinside.com/viewimage.php*"],
        browser: {
            contextMenus: {onClicked: {addListener(listener: typeof click) { click = listener; }}},
            tabs: {create: createTab}
        }
    });

    impl.listen();
    const image = "https://dcimg1.dcinside.com/viewimage.php?id=test&no=1";
    const target = "https://image.dcinside.com/dccon.php?id=test&no=1";
    await click({menuItemId: "imagesearch:saucenao", srcUrl: image}, {id: 7, index: 3, windowId: 4});

    expect(createTab).toHaveBeenCalledWith({
        url: "https://saucenao.com/search.php?url=" + encodeURIComponent(target),
        index: 4, openerTabId: 7, windowId: 4
    });

    await click({menuItemId: "imagesearch:unknown", srcUrl: image});
    await click({menuItemId: "imagesearch:saucenao", srcUrl: "https://other.test/image.jpg"});
    expect(createTab).toHaveBeenCalledTimes(1);
});
