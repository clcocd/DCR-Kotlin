import {describe, expect, it} from "vitest";
import {submitResult} from "@/core/preview/response";

describe("원본 6.2.2 댓글 응답 객체 형식", () => {
    it("일반 문자열은 존재하는 message/detail 키의 값이 undefined다", () => {
        const parsed = submitResult(" success ");
        expect(parsed).toStrictEqual({result: "success", message: undefined, detail: undefined});
        expect(Object.keys(parsed)).toEqual(["result", "message", "detail"]);
    });

    it("두 칸과 세 칸 응답도 원본 필드 구조를 유지한다", () => {
        expect(submitResult("false||captcha")).toStrictEqual({
            result: "false", message: "captcha", detail: undefined
        });
        expect(submitResult("false||nomember||인증 필요||추가 값")).toStrictEqual({
            result: "false", message: "nomember", detail: "인증 필요"
        });
    });

    it("HTML 응답은 원본처럼 result 키만 돌려준다", () => {
        expect(submitResult(" <html>||script</html> ")).toStrictEqual({
            result: "<html>||script</html>"
        });
    });
});
