import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";

interface RefresherSelectProps<T extends string> {
    value: T;
    options: Record<T, string>;
    disabled?: boolean;
    /** 옆 글자는 label로 연결되지 않으므로 반드시 따로 넘긴다. */
    "aria-label": string;
    "aria-describedby"?: string;
    onChange: (value: T) => void;
}

export const RefresherSelect = kotlinBridge.createRefresherSelect({
    react, Select, SelectContent, SelectItem, SelectTrigger, SelectValue
}) as <T extends string>(props: RefresherSelectProps<T>) => React.ReactNode;
