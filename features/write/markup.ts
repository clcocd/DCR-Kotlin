import {kotlinBridge} from "@/kotlin/bridge";

/** 글쓰기 페이지의 원본 마크업 선택자. */
export const {submit: SUBMIT, editor: EDITOR, subject: SUBJECT, auto: AUTO, leave: LEAVE, posted: POSTED} =
    kotlinBridge.runtimeConstants.write;
