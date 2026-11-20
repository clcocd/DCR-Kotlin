import DOMPurify from "dompurify";
import {kotlinBridge} from "@/kotlin/bridge";

export const sanitizeHtml = kotlinBridge.createSanitizer(DOMPurify);
export const htmlToText = kotlinBridge.htmlToText;
