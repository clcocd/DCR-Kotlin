import {kotlinBridge} from "@/kotlin/bridge";
import ky from "ky";

export const {ajax, http} = kotlinBridge.createHttpClient(ky);
export const formBody = kotlinBridge.formBody;
