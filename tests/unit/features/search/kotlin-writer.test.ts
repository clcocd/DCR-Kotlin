// @vitest-environment-options {"url": "https://gall.dcinside.com/board/lists/?id=test&page=3&s_type=search_all&s_keyword=old"}
import {expect, it} from "vitest";

import {writerSearchUrl, keepWriterFilter} from "@/features/search/writer";

it("Kotlin 작성자 주소는 과거 검색 조건을 제거하고 닉네임과 uid 필터를 붙인다", () => {
    const url = new URL(writerSearchUrl({kind: "uid", value: "test.id"}, "한글 닉네임"));
    expect(url.pathname).toBe("/board/lists");
    expect(url.searchParams.get("id")).toBe("test");
    expect(url.searchParams.get("s_type")).toBe("search_name");
    expect(url.searchParams.get("s_keyword")).toBe("한글 닉네임");
    expect(url.searchParams.get("rf_uid")).toBe("test.id");
    expect(url.searchParams.has("page")).toBe(false);

    const withoutFilter = new URL(writerSearchUrl(null));
    expect(withoutFilter.searchParams.get("id")).toBe("test");
    expect(withoutFilter.searchParams.has("rf_uid")).toBe(false);
    expect(withoutFilter.searchParams.has("s_type")).toBe(false);
    expect(withoutFilter.searchParams.has("s_keyword")).toBe(false);
});

it("Kotlin 작성자 페이징은 https와 상대 경로만 수정하고 javascript 링크는 유지한다", () => {
    const paging = document.createElement("div");
    paging.innerHTML = '<a href="/board/lists/?id=test&page=2">다음</a><a href="javascript:void(0)">토글</a>';
    keepWriterFilter(paging, {kind: "ip", value: "123.45"});

    const [next, toggle] = paging.querySelectorAll<HTMLAnchorElement>("a");
    expect(new URL(next!.href).searchParams.get("rf_ip")).toBe("123.45");
    expect(toggle!.getAttribute("href")).toBe("javascript:void(0)");
});
