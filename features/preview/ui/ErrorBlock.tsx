import {CircleAlert, ExternalLink} from "lucide-react";

import {Alert, AlertDescription} from "@/components/ui/alert";
import {Button} from "@/components/ui/button";

import {type ErrorState, usePreviewStore} from "./previewStore";

import * as react from "react";
import {kotlinBridge} from "@/kotlin/bridge";

export const ErrorBlock = kotlinBridge.createPreviewError({
    react, CircleAlert, ExternalLink, Alert, AlertDescription, Button, usePreviewStore
}) as (props: {error: ErrorState}) => React.ReactNode;
