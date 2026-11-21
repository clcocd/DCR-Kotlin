import {DCCON, fakeComment} from "./dcinside";
import {expect, test} from "./fixtures";

test("글자콘 더블콘은 이미지와 글자 슬롯의 순서를 바꿔 전송한다", async ({listPage, site}) => {
    const frame = await listPage.openPreview();
    await frame.getByRole("button", {name: "글자콘", exact: true}).click();
    const input = frame.getByRole("textbox", {name: "글자콘 입력", exact: true});
    await input.fill("첫 콘");
    await frame.getByRole("button", {name: "더블콘 만들기", exact: true}).click();
    await listPage.dialog.getByRole("button", {name: DCCON.title, exact: true}).click();
    await frame.getByRole("button", {name: "위치 바꾸기"}).click();
    await expect(input).toHaveValue("첫 콘");
    await frame.getByRole("button", {name: "작성", exact: true}).click();

    await expect.poll(() => site.submitted.find(({path}) => path === "/txtcon/insert_double")?.body.get("slot1_type")).toBe("dccon");
    const {body} = site.submitted.find(({path}) => path === "/txtcon/insert_double")!;
    expect(Object.fromEntries(body)).toMatchObject({
        slot1_type: "dccon", package_idx_1: DCCON.package_idx, detail_idx_1: DCCON.detail_idx,
        slot2_type: "txtcon", txtcon_text_2: "첫 콘"
    });
});

for (const state of ["empty", "failed"] as const) {
    test(`글자콘 더블콘은 구매 목록이 ${state}여도 글자콘을 선택할 수 있다`, async ({listPage, site}) => {
        site.dcconListState = state;
        const frame = await listPage.openPreview();
        await frame.getByRole("button", {name: "글자콘", exact: true}).click();
        const input = frame.getByRole("textbox", {name: "글자콘 입력", exact: true});
        await input.fill("첫 콘");
        await frame.getByRole("button", {name: "더블콘 만들기", exact: true}).click();
        const picker = listPage.dialog;
        await expect(picker.getByText("사용 가능한 디시콘이 없습니다.")).toBeVisible();
        await picker.getByRole("button", {name: "글자콘", exact: true}).click();

        await expect(input).toHaveValue("");
        await frame.getByRole("button", {name: "작성", exact: true}).click();
        await expect(listPage.overlay.getByText("글자콘 내용을 입력해주세요.")).toBeVisible();
        expect(site.submitted).toHaveLength(0);

        await input.fill("둘째 콘");
        if (state === "empty") {
            await frame.getByRole("button", {name: "작성", exact: true}).click();
            await expect.poll(() => site.submitted.find(({path}) => path === "/txtcon/insert_double")?.body.get("txtcon_text_2")).toBe("둘째 콘");
            expect(site.submitted[0]!.body.get("txtcon_text_1")).toBe("첫 콘");
            return;
        }
        await frame.getByRole("button", {name: "1번 콘 삭제"}).click();
        await expect(input).toHaveValue("둘째 콘");
        await frame.getByRole("button", {name: "작성", exact: true}).click();
        await expect.poll(() => site.submitted.find(({path}) => path === "/txtcon/insert")?.body.get("txtcon_text")).toBe("둘째 콘");
    });
}

test("래퍼와 평탄화된 더블콘 댓글은 두 콘을 가로로 표시한다", async ({listPage, site}) => {
    const image = `<img class="written_dccon" src="${DCCON.list_img}" alt="${DCCON.title}">`;
    const text = '<div class="coment_dccon_txt"><p class="txtcon_txt">글자콘</p></div>';
    site.comments = [
        fakeComment(70, {memo: `<div class="double_txtcon"><div class="double_txtcon_item">${image}</div><div class="double_txtcon_item">${text}</div></div>`}),
        fakeComment(71, {memo: text + image})
    ];
    const frame = await listPage.openPreview();
    await expect(frame.locator(".refresher-comment-html .double_txtcon")).toHaveCSS("display", "flex");
    await expect(frame.locator(".refresher-comment-html:has(> .coment_dccon_txt + .written_dccon)")).toHaveCSS("display", "flex");
});
