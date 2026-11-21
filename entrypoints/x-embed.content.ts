import {CONTENT_EXCLUDE_MATCHES, CONTENT_MATCHES} from "@/core/pages";

/** X 위젯은 MAIN world에서만 window.twttr에 접근할 수 있다. DOM 이벤트로 콘텐츠 미리보기와 통신한다. */
export default defineContentScript({
    matches: CONTENT_MATCHES,
    excludeMatches: CONTENT_EXCLUDE_MATCHES,
    runAt: "document_start",
    world: "MAIN",
    main() {
        type XWidgets = {createTweet: (id: string, element: HTMLElement, options: {
            theme: string; lang: string; dnt: boolean;
        }) => Promise<HTMLElement | undefined>};
        const page = window as Window & {__refresherXBridge?: boolean; twttr?: {widgets?: XWidgets}};
        if (page.__refresherXBridge) return;
        page.__refresherXBridge = true;

        let pending: Promise<XWidgets> | undefined;
        const widgets = (): Promise<XWidgets> => {
            if (page.twttr?.widgets?.createTweet) return Promise.resolve(page.twttr.widgets);
            if (pending) return pending;
            pending = new Promise<XWidgets>((resolve, reject) => {
                const deadline = Date.now() + 10_000;
                if (!document.querySelector('script[src*="platform.twitter.com/widgets.js"]')) {
                    const script = document.createElement("script");
                    script.src = "https://platform.twitter.com/widgets.js";
                    script.async = true;
                    script.addEventListener("error", () => reject(new Error("X widget script failed")), {once: true});
                    (document.head || document.documentElement).append(script);
                }
                const check = (): void => {
                    if (page.twttr?.widgets?.createTweet) resolve(page.twttr.widgets);
                    else if (Date.now() >= deadline) reject(new Error("X widget timeout"));
                    else setTimeout(check, 100);
                };
                check();
            }).catch((error: unknown) => {
                pending = undefined;
                throw error;
            });
            return pending;
        };

        document.addEventListener("refresher-x-render", (event: Event) => {
            const stage = event.composedPath()[0];
            if (!(stage instanceof HTMLElement) || !stage.classList.contains("refresher-x-stage")) return;
            const id = stage.dataset.refresherXId;
            if (!id || !/^[0-9]{1,24}$/.test(id)) return;
            const report = (name: string): void => {
                if (stage.isConnected) stage.dispatchEvent(new Event(name, {bubbles: true, composed: true}));
            };
            void widgets().then(async (api) => {
                if (!stage.isConnected) return;
                const tweet = await api.createTweet(id, stage, {
                    theme: stage.dataset.refresherXTheme === "dark" ? "dark" : "light",
                    lang: "ko",
                    dnt: true
                });
                report(tweet ? "refresher-x-done" : "refresher-x-error");
            }).catch(() => report("refresher-x-error"));
        });
    }
});
