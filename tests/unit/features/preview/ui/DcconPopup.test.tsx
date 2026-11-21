import {render, act} from "@/tests/helpers";
import type {ReactNode} from "react";
import {afterEach, expect, it, vi} from "vitest";

import {fetchRecentDccons} from "@/core/preview/dccon";
import {fetchDcconList} from "@/core/preview/request";
import {DcconPopup} from "@/features/preview/ui/DcconPopup";

// Base UI는 변환되지 않아 진짜 React를 불러 그려지지 않으므로 창, 토글을 평범한 요소로 바꾼다.
vi.mock("@/components/dialogs", () => ({
    ModalDialog: ({children}: { children?: ReactNode }) => <div>{children}</div>,
    DialogCloseButton: () => null
}));
vi.mock("@/components/ui/button", () => ({
    Button: ({children, onClick}: {children?: ReactNode; onClick?: () => void}) => <button onClick={onClick}>{children}</button>
}));
vi.mock("@/components/ui/dialog", () => ({DialogTitle: () => null}));
vi.mock("@/components/ui/field", () => ({Field: () => null, FieldLabel: () => null}));
vi.mock("@/components/ui/skeleton", () => ({Skeleton: () => null}));
vi.mock("@/components/ui/switch", () => ({Switch: () => null}));
vi.mock("@/components/ui/toggle-group", () => ({
    ToggleGroup: ({children}: { children?: ReactNode }) => <div>{children}</div>,
    ToggleGroupItem: () => <button className="pack"/>
}));
vi.mock("@/core/preview/request", () => ({fetchDcconList: vi.fn()}));
vi.mock("@/core/preview/dccon", async (importOriginal) => ({
    ...await importOriginal<typeof import("@/core/preview/dccon")>(),
    fetchRecentDccons: vi.fn()
}));

const root = document.createElement("div");
document.body.append(root);
afterEach(() => render(null, root));

/** 패키지 줄. 넓이를 정해 옆으로 넘치게 한다(jsdom은 크기가 0이다). */
const openStrip = async (): Promise<HTMLElement> => {
    vi.mocked(fetchDcconList).mockResolvedValue([{package_idx: "1", title: "콘", main_img_url: "", detail: []}]);
    await act(() => render(<DcconPopup onSelect={vi.fn()} onClose={vi.fn()}/>, root));
    await vi.waitFor(() => expect(root.querySelector(".pack")).not.toBeNull());
    const strip = root.querySelector<HTMLElement>(".overflow-x-auto")!;
    // 굴린 곳은 scrollTo에 넘긴 left로 본다. jsdom은 굴리지 않는다.
    Object.defineProperties(strip, {scrollWidth: {value: 1000}, clientWidth: {value: 500}, scrollTo: {value: vi.fn()}});
    return strip;
};

const wheel = (el: Element, init: WheelEventInit): WheelEvent => {
    const ev = new WheelEvent("wheel", {bubbles: true, cancelable: true, ...init});
    el.dispatchEvent(ev);
    return ev;
};

const scrolledTo = (strip: HTMLElement, left: number): void => expect(strip.scrollTo).toHaveBeenLastCalledWith({left, behavior: "smooth"});

it("세로 휠로 패키지 줄을 옆으로 부드럽게 넘기고, 굴러가는 동안 이어진 휠은 굴러갈 곳에 더한다", async () => {
    const strip = await openStrip();
    const ev = wheel(strip, {deltaY: 100});
    expect(ev.defaultPrevented).toBe(true);
    scrolledTo(strip, 100);

    wheel(strip, {deltaY: 100});
    scrolledTo(strip, 200);
    wheel(strip, {deltaY: 1000});
    scrolledTo(strip, 500);

    strip.scrollLeft = 300;
    strip.dispatchEvent(new Event("scrollend"));
    wheel(strip, {deltaY: -100});
    scrolledTo(strip, 200);
});

it("Shift, Ctrl, 가로 휠은 브라우저에 맡긴다", async () => {
    const strip = await openStrip();
    for (const init of [{deltaY: 100, shiftKey: true}, {deltaY: 100, ctrlKey: true}, {deltaX: 100, deltaY: 10}]) {
        expect(wheel(strip, init).defaultPrevented).toBe(false);
    }
    expect(strip.scrollTo).not.toHaveBeenCalled();
});

const clickLabel = async (label: string) => {
    const button = [...root.querySelectorAll("button")].find((node) => node.textContent === label);
    expect(button).toBeDefined();
    await act(() => button!.click());
};

it("최근 목록을 불러와 기존 콘 선택 경로로 전달한다", async () => {
    const item = {package_idx: "8", detail_idx: "81", list_img: "/recent.png", title: "최근콘"};
    vi.mocked(fetchDcconList).mockResolvedValue([{package_idx: "1", title: "구매콘", main_img_url: "", detail: []}]);
    vi.mocked(fetchRecentDccons).mockResolvedValue([item]);
    const onSelect = vi.fn();
    await act(() => render(<DcconPopup onSelect={onSelect} onClose={vi.fn()}/>, root));
    await clickLabel("최근");
    await vi.waitFor(() => expect(root.querySelector('button[title="최근콘"]')).not.toBeNull());
    await act(() => root.querySelector<HTMLButtonElement>('button[title="최근콘"]')!.click());
    expect(onSelect).toHaveBeenCalledWith([item], false);
});

it("최근 요청 도중 패키지로 돌아가면 요청을 취소한다", async () => {
    let signal: AbortSignal | undefined;
    vi.mocked(fetchDcconList).mockResolvedValue([{package_idx: "1", title: "구매콘", main_img_url: "", detail: []}]);
    vi.mocked(fetchRecentDccons).mockImplementation((s) => {
        signal = s;
        return new Promise(() => {});
    });
    await act(() => render(<DcconPopup onSelect={vi.fn()} onClose={vi.fn()}/>, root));
    await clickLabel("최근");
    expect(signal?.aborted).toBe(false);
    await clickLabel("구매한 콘");
    expect(signal?.aborted).toBe(true);
});

it("최근 목록 비로그인 응답은 선택창을 닫지 않고 안내한다", async () => {
    vi.mocked(fetchDcconList).mockResolvedValue([{package_idx: "1", title: "구매콘", main_img_url: "", detail: []}]);
    vi.mocked(fetchRecentDccons).mockResolvedValue("not_login");
    const onClose = vi.fn();
    await act(() => render(<DcconPopup onSelect={vi.fn()} onClose={onClose}/>, root));
    await clickLabel("최근");
    await vi.waitFor(() => expect(root.textContent).toContain("최근 디시콘은 로그인한 뒤에 볼 수 있습니다."));
    expect(onClose).not.toHaveBeenCalled();
});
