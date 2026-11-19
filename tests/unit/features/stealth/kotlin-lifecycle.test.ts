// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {afterEach, expect, it} from "vitest";
import stealth from "@/features/stealth/index";
import {runModule} from "../module";

afterEach(() => {
    document.documentElement.classList.remove("refresherStealth", "stlth");
    document.querySelectorAll(".stealth_control_button").forEach((button) => button.remove());
});

it("Kotlin 버튼은 재설치할 때 이전 버튼을 교체하고 종료 시 제거한다", async () => {
    const old = document.createElement("div");
    old.className = "stealth_control_button";
    document.body.append(old);

    const running = await runModule(stealth);
    const button = document.querySelector<HTMLButtonElement>(".stealth_control_button > #tempview")!;
    expect(button).not.toBeNull();
    expect(document.querySelectorAll(".stealth_control_button")).toHaveLength(1);
    expect(document.body.contains(old)).toBe(false);
    expect(button.closest<HTMLElement>(".stealth_control_button")?.hasAttribute("data-refresher-ui")).toBe(true);
    expect(document.documentElement.classList.contains("refresherStealth")).toBe(true);

    button.click();
    expect(document.documentElement.classList.contains("stlth")).toBe(true);
    expect(button.textContent).toBe("이미지 숨기기");

    running.stop();
    expect(document.documentElement.classList.contains("refresherStealth")).toBe(false);
    expect(document.documentElement.classList.contains("stlth")).toBe(false);
    expect(document.querySelectorAll(".stealth_control_button")).toHaveLength(0);
});
