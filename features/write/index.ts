import {sendMessage} from "@/core/messaging/protocol";
import {defineModule} from "@/core/module/define";
import {WRITE_PAGE} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

import {AUTO, EDITOR, LEAVE, POSTED, SUBJECT, SUBMIT} from "./markup";
import meta from "./meta";

export default defineModule(meta, {
    setup: (ctx) => kotlinBridge.setupWrite(ctx, {
        writePage: WRITE_PAGE, auto: AUTO, editor: EDITOR, leave: LEAVE,
        posted: POSTED, subject: SUBJECT, submit: SUBMIT,
        requestHook: () => sendMessage("refresher:hookUploads")
    })
});
