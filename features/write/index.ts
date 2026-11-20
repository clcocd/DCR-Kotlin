import {sendMessage} from "@/core/messaging/protocol";
import {defineModule} from "@/core/module/define";
import type {ModuleImpl} from "@/core/module/types";
import {WRITE_PAGE} from "@/core/pages";
import {kotlinBridge} from "@/kotlin/bridge";

import {UPLOAD_OPTIONS_KEY} from "./images";
import {AUTO, EDITOR, LEAVE, POSTED, SUBJECT, SUBMIT} from "./markup";
import meta from "./meta";

/** 메시징·셀렉터는 기존 TS 경계를 유지하고 글쓰기 보호/상태는 Kotlin이 관리한다. */
export default defineModule(meta, kotlinBridge.writeModuleImpl(
    {uploadOptionsKey: UPLOAD_OPTIONS_KEY, submit: SUBMIT, leave: LEAVE, posted: POSTED, subject: SUBJECT, editor: EDITOR, auto: AUTO},
    () => WRITE_PAGE.test(location.pathname),
    () => sendMessage("refresher:hookUploads")
) as ModuleImpl<typeof meta, void>);
