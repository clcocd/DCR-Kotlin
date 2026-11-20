import {kotlinBridge} from "@/kotlin/bridge";
import type {ScriptPublicPath} from "wxt/utils/inject-script";
import {storage} from "wxt/utils/storage";
import pLimit from "p-limit";

import {MODULES_KEY, moduleSettingsKey} from "@/core/storage/items";

import {isModuleEnabled, readModuleStorage, settingsOf} from "./settings";
import type {AnyModuleMeta, SchemaOf, SettingValues, SettingsSchema} from "./types";

/**
 * 모듈의 배경 쪽 동작. S는 메타의 설정 스키마라 apply가 받는 설정이 키마다 타입을 가진다.
 * 배경 번들에 React가 딸려 가지 않도록 모듈 파일(index.ts)은 import하지 않는다. meta.ts는 UI를 불러오지 않아 그대로 쓴다.
 */
interface BackgroundImpl<S extends SettingsSchema> {
    /**
     * 배경이 뜰 때마다 동기로 부른다. 서비스 워커를 깨우는 이벤트 리스너는 첫 실행 중에 걸어야 하므로 여기서 건다.
     * 모듈이 꺼져 있어도 부른다.
     */
    listen?(): void;

    /**
     * 켜기/끄기, 설정 변경, 설치, 브라우저 시작 때 그 시점의 상태에 맞춘다. 모듈마다 앞의 호출이 끝난 뒤에 다음을 부른다.
     * 맞출 상태가 없는(listen만 하는) 모듈은 apply를 두지 않는다.
     */
    apply?(state: { enabled: boolean; settings: SettingValues<S> }): Promise<void> | void;
}

/** 배경 스크립트가 모아 돌리는 모듈. on/off, 설정은 메타의 id, defaultEnable, settings로 읽는다. */
export interface BackgroundModule extends Pick<AnyModuleMeta, "id" | "defaultEnable" | "settings">, BackgroundImpl<SettingsSchema> {}

/** 모듈의 배경 쪽. features/<id>/background.ts가 default로 내보내면 배경 스크립트가 glob으로 모아 돌린다. */
export const defineBackgroundModule = <M extends AnyModuleMeta>(meta: M, impl: BackgroundImpl<SchemaOf<M>>): BackgroundModule =>
    kotlinBridge.defineBackgroundFeature(meta, impl);

/** 탭에서 온 메시지의 보낸 쪽. 팝업, 옵션 같은 확장 페이지는 tab이 없다. */
type TabSender = Browser.runtime.MessageSender & { tab: { id: number } };

export const hasTab = (sender: Browser.runtime.MessageSender): sender is TabSender => sender.tab?.id !== undefined;

/**
 * 메시지를 보낸 문서.
 * 크롬은 문서(documentId)로 집는다. 프레임 번호로 집으면 그사이 다른 페이지로 넘어갔거나 보낸 쪽이 프리렌더 중인 페이지일 때
 * 지금 보이는 다른 문서에서 돈다. 파이어폭스는 documentId를 주지 않아 프레임 번호로 집는다.
 */
const senderTarget = kotlinBridge.tabTarget;

/** 메시지를 보낸 문서의 페이지(MAIN world)에서 func를 실행한다. func는 직렬화되므로 바깥 변수, import를 쓸 수 없다. */
export const runInPage = <Args extends unknown[], Result>(sender: TabSender, func: (...args: Args) => Result, args: Args) =>
    browser.scripting.executeScript({target: senderTarget(sender), world: "MAIN", func, args});

/** import가 필요해 따로 번들한 스크립트(entrypoints/의 unlisted script)를 메시지를 보낸 문서의 페이지(MAIN world)에서 실행한다. */
export const runFileInPage = (sender: TabSender, file: ScriptPublicPath) =>
    browser.scripting.executeScript({target: senderTarget(sender), world: "MAIN", files: [file]});

export const preparePageRuntime = (sender: TabSender) => runFileInPage(sender, "/page-runtime.js");

export const startBackgroundModules = (modules: BackgroundModule[]): (() => Promise<void>) =>
    kotlinBridge.startBackgroundModules(modules, {
        storage, modulesKey: MODULES_KEY, moduleSettingsKey, isModuleEnabled, readModuleStorage, settingsOf, limit: pLimit
    });
