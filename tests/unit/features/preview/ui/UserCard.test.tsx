import {render, act} from "@/tests/helpers";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";

import {moduleSettingsStore, runningModulesStore} from "@/core/module/registry";
import {settingsOf} from "@/core/module/settings";
import type {SettingValue} from "@/core/storage/types";
import {UserCard} from "@/features/preview/ui/UserCard";
import userinfo from "@/features/userinfo/meta";
import {type Ratios, useUiStore} from "@/stores/ui";

// 읽기를 시작하는 구독. 미리보기는 부르지 않아야 한다.
const subscribeDatabase = vi.hoisted(() => vi.fn(() => () => {}));
vi.mock("@/core/database", async (importOriginal) => ({
    ...await importOriginal<typeof import("@/core/database")>(),
    databaseVersion: () => 1,
    subscribeDatabase,
    onDatabaseChange: () => () => {},
    ipInfoOf: () => undefined
}));

let container: HTMLElement;

/** 유저 정보 모듈을 patch 설정으로 돌린다 (running이 false면 꺼진 것). */
const setUserinfo = (patch: Record<string, SettingValue>, running = true): void => {
    moduleSettingsStore.setState({userinfo: settingsOf(userinfo, patch)});
    runningModulesStore.setState({userinfo: running});
};

const badges = (): string[] =>
    Array.from(container.querySelectorAll(".refresher-user > span"), (span) => span.textContent ?? "");

const renderCard = (): void => {
    act(() => render(<UserCard user={{nick: "닉", id: "member"}}/>, container));
};

beforeEach(() => {
    container = document.createElement("div");
    document.body.append(container);
});

afterEach(() => {
    act(() => render(null, container));
    container.remove();
    subscribeDatabase.mockClear();
    useUiStore.setState({ratios: null});
    moduleSettingsStore.setState({}, true);
    runningModulesStore.setState({}, true);
});

describe("UserCard DB 읽기", () => {
    it("유저 정보 모듈이 꺼져 있으면 IP DB를 읽기 시작하지 않는다", () => {
        setUserinfo({}, false);
        renderCard();
        expect(subscribeDatabase).not.toHaveBeenCalled();
    });
});

describe("UserCard 깡계 태그", () => {
    const setRatios = (action: Ratios["action"]): void => {
        setUserinfo({checkRatio: true, alarmRatio: 10, lowActivityAction: action});
        useUiStore.setState({ratios: {cache: {member: {article: 1, comment: 2, date: Date.now()}}, alarm: 10, action}});
    };

    it("처리가 태그면 배지 뒤에 [깡계]를 단다", () => {
        setRatios("tag");
        renderCard();
        expect(badges()).toEqual(["(member)", "[1/2]", "[깡계]"]);
    });

    it("흐리게, 숨기기면 태그를 달지 않는다", () => {
        setRatios("hide");
        renderCard();
        expect(badges()).toEqual(["(member)", "[1/2]"]);
    });
});
