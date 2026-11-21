// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test"}
import {expect, it} from "vitest";

import {BOARD_PAGE} from "@/core/pagePatterns";
import meta, {BADGE_COLORS, settings} from "@/features/userinfo/meta";

it("Kotlin 유저 정보 메타에서 원본 URL·배지 스키마를 그대로 유지한다", () => {
    expect(meta.id).toBe("userinfo");
    expect(meta.urls?.[0]).toBe(BOARD_PAGE);
    expect(meta.settings).toBe(settings);
    expect(Object.keys(BADGE_COLORS)).toEqual([
        "uid", "ratio", "ratioAlarm", "korea", "japan", "china", "foreign", "vpn"
    ]);
    expect(Object.keys(settings)).toEqual([
        "showFixedNickUID", "showHalfFixedNickUID", "ipInfoFilter", "checkRatio",
        "alarmRatio", "lowActivityAction", ...Object.keys(BADGE_COLORS).map(k => `${k}Color`), "badgeOrder"
    ]);
});
