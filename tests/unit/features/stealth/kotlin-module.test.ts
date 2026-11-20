// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {afterEach, expect, it, vi} from "vitest";

import stealth from "@/features/stealth/index";
import meta from "@/features/stealth/meta";
import {useUiStore} from "@/stores/ui";
import {runModule} from "../module";

afterEach(() => {
    document.documentElement.classList.remove("refresherStealth", "stlth");
    document.querySelectorAll(".stealth_control_button").forEach((element) => element.remove());
    vi.restoreAllMocks();
});

it("스텔스 단축키·팝업 토글은 Kotlin API 상태를 사용한다", async () => {
    const showToast = vi.spyOn(useUiStore.getState(), "showToast").mockImplementation(() => {});
    const running = await runModule(stealth);
    try {
        const toggle = stealth.pageToggles?.find((entry) => entry.id === "reveal");
        expect(toggle?.desc).toBe("스텔스로 숨긴 이미지를 보입니다");
        expect(toggle?.isOn(running.api)).toBe(false);

        stealth.shortcuts?.stealthPause?.(running.api);
        expect(toggle?.isOn(running.api)).toBe(true);
        expect(showToast).toHaveBeenCalledWith("이미지를 보이게 했습니다.");

        toggle?.toggle(running.api);
        expect(toggle?.isOn(running.api)).toBe(false);
    } finally {
        running.stop();
    }
});

it("6.2.2 스텔스 기능 ID·기본 활성화·단축키·토글 메타데이터를 유지한다", () => {
    expect(meta).toEqual({
        id: "stealth",
        defaultEnable: false,
        toggles: ["reveal"],
        commands: {stealthPause: {key: "Alt+P"}}
    });
});
