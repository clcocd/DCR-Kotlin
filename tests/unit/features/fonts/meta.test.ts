import {describe, expect, it} from "vitest";

import fontsMeta, {fontFamilyOf, settings} from "@/features/fonts/meta";

describe("fontFamilyOf", () => {
    it("이름마다 따옴표로 감싸고 끝에 sans-serif를 붙인다", () => {
        expect(fontFamilyOf("Pretendard, 맑은 고딕")).toBe(`"Pretendard", "맑은 고딕", sans-serif`);
    });

    it("빈칸이면 기본 폰트를 쓴다", () => {
        expect(fontFamilyOf("")).toBe(`"Noto Sans CJK KR", "NanumGothic", sans-serif`);
        expect(fontFamilyOf("  ")).toBe(`"Noto Sans CJK KR", "NanumGothic", sans-serif`);
    });

    it("줄바꿈 같은 제어문자는 지운다", () => {
        expect(fontFamilyOf("A\nB,\tC\u0000")).toBe(`"AB", "C", sans-serif`);
    });

    it("따옴표, 역슬래시로 문자열을 끝내 규칙을 덧붙일 수 없다", () => {
        // 입력: a\"; } b{ - 역슬래시와 따옴표를 모두 이스케이프해 한 문자열 안에 남는다.
        expect(fontFamilyOf(String.raw`a\"; } b{`)).toBe(String.raw`"a\\\"; } b{", sans-serif`);
    });

    it("따옴표로 감싼 일반 글꼴군은 키워드가 아니라 이름으로 남는다", () => {
        expect(fontFamilyOf(`"serif"`)).toBe(`"serif", sans-serif`);
        expect(fontFamilyOf(`'Noto Sans'`)).toBe(`"Noto Sans", sans-serif`);
    });

    it("대소문자가 섞인 일반 글꼴군 키워드는 그대로 두고 sans-serif를 붙이지 않는다", () => {
        expect(fontFamilyOf("Arial, MonoSpace")).toBe(`"Arial", MonoSpace`);
    });
});

describe("fonts Kotlin 모듈 메타데이터", () => {
    it("옵션 화면과 본문이 같은 설정 스키마를 사용한다", () => {
        expect(fontsMeta.id).toBe("fonts");
        expect(fontsMeta.settings).toBe(settings);
        expect(Object.keys(settings)).toEqual(["customFonts", "changeDCFont", "bodyFontSize"]);
        expect(settings.customFonts).toEqual({type: "text", default: "Noto Sans CJK KR, NanumGothic"});
        expect(settings.changeDCFont).toEqual({type: "check", default: true});
        expect(settings.bodyFontSize).toEqual({type: "range", default: 13, min: 5, max: 30, step: 1, unit: "px"});
        expect(fontsMeta.extensionPageVars?.({customFonts: "Pretendard", changeDCFont: true, bodyFontSize: 13}))
            .toEqual({"--refresher-font": '"Pretendard", sans-serif'});
    });
});
