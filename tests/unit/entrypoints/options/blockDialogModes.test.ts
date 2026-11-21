import {expect, it} from "vitest";

import {DETECT_MODE_DESCRIPTIONS} from "@/entrypoints/options/tabs/BlockDialog";

it("차단 모드 설명 문구·순서·줄바꿈을 원본 6.2.2와 동일하게 표시한다", () => {
    expect(DETECT_MODE_DESCRIPTIONS).toEqual({
    SAME: "값과 똑같으면 가립니다.",
    CONTAIN: "값이 들어 있으면 가립니다.",
    NOT_SAME: "값과 똑같은 것만 남기고 나머지를 모두 가립니다.\n불일치, 불포함 항목이 여럿이면 하나에만 맞아도 남깁니다.",
    NOT_CONTAIN: "값이 들어 있는 것만 남기고 나머지를 모두 가립니다.\n불일치, 불포함 항목이 여럿이면 하나에만 맞아도 남깁니다."

    });
});
