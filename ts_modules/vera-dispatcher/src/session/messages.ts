export type WindowRole = "launcher" | "primary" | "secondary";

export type SessionMessage =
    | {
        type: "WINDOW_READY";
        window: WindowRole;
    }
    | {
        type: "WINDOW_CLOSED";
        window: WindowRole;
    }
    | {
        type: "WINDOW_HEARTBEAT";
        window: WindowRole;
        ts: number;
    };

export function windowReady(role: WindowRole): SessionMessage {
    return { type: "WINDOW_READY", window: role };
}

export function windowClosed(role: WindowRole): SessionMessage {
    return { type: "WINDOW_CLOSED", window: role };
}

export function windowHeartbeat(role: WindowRole, ts: number): SessionMessage {
    return { type: "WINDOW_HEARTBEAT", window: role, ts: ts };
}
