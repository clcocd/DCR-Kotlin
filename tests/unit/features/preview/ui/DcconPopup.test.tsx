import {render, act} from "@/tests/helpers";
import type {ReactNode} from "react";
import {afterEach, expect, it, vi} from "vitest";

import {fetchDcconList} from "@/core/preview/request";
import {DcconPopup} from "@/features/preview/ui/DcconPopup";

// Base UI는 변환되지 않아 진짜 React를 불러 그려지지 않으므로 창, 토글을 평범한 요소로 바꾼다.
vi.mock("@/components/dialogs", () => ({
    ModalDialog: ({children}: { children?: ReactNode }) => <div>{children}</div>,
    DialogCloseButton: () => null
}));
vi.mock("@/components/ui/button", () => ({Button: () => null}));
vi.mock("@/components/ui/dialog", () => ({DialogTitle: () => null}));
vi.mock("@/components/ui/field", () => ({Field: () => null, FieldLabel: () => null}));
vi.mock("@/components/ui/skeleton", () => ({Skeleton: () => null}));
vi.mock("@/components/ui/switch", () => ({Switch: () => null}));
vi.mock("@/components/ui/toggle-group", () => ({
    ToggleGroup: ({children}: { children?: ReactNode }) => <div>{children}</div>,
    ToggleGroupItem: () => <button className="pack"/>
}));
vi.mock("@/core/preview/request", () => ({fetchDcconList: vi.fn()}));

const root = document.createElement("div");
document.body.append(root);
afterEach(() => render(null, root));

/** 패키지 줄. 넓이를 정해 옆으로 넘치게 한다(jsdom은 크기가 0이다). */
const openStrip = async (): Promise<HTMLElement> => {
    vi.mocked(fetchDcconList).mockResolvedValue([{package_idx: "1", title: "콘", main_img_url: "", detail: []}]);
    await act(() => render(<DcconPopup onSelect={vi.fn()} onClose={vi.fn()}/>, root));
    await vi.waitFor(() => expect(root.querySelector(".pack")).not.toBeNull());
    const strip = root.querySelector<HTMLElement>(".overflow-x-auto")!;
    strip.scrollLeft = 0;
    return strip;
};

const wheel = (el: Element, init: WheelEventInit): WheelEvent => {
    const ev = new WheelEvent("wheel", {bubbles: true, cancelable: true, ...init});
    el.dispatchEvent(ev);
    return ev;
};

it("검증된 방식대로 세로 deltaY를 scrollLeft에 직접 더하고 기본 스크롤을 막는다", async () => {
    const strip = await openStrip();
    expect(wheel(strip, {deltaY: 100}).defaultPrevented).toBe(true);
    expect(strip.scrollLeft).toBe(100);

    wheel(strip, {deltaY: 100});
    expect(strip.scrollLeft).toBe(200);

    strip.scrollLeft = 300;
    wheel(strip, {deltaY: -100});
    expect(strip.scrollLeft).toBe(200);
});

it("Shift, Ctrl, 가로 휠이 섞여도 세로 deltaY가 있으면 가로로 넘긴다", async () => {
    const strip = await openStrip();
    for (const init of [{deltaY: 100, shiftKey: true}, {deltaY: 100, ctrlKey: true}, {deltaX: 100, deltaY: 10}]) {
        expect(wheel(strip, init).defaultPrevented).toBe(true);
    }
    expect(strip.scrollLeft).toBe(210);
    expect(wheel(strip, {deltaX: 100, deltaY: 0}).defaultPrevented).toBe(false);
    expect(strip.scrollLeft).toBe(210);
});
