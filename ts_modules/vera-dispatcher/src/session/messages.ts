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
    }
    | {
        type: "SESSION_INFO";
        userFullName?: string;
        token?: string;
    }
    | {
        type: "SESSION_CLOSED";
    }
;

export function windowReady(role: WindowRole): SessionMessage {
    return { type: "WINDOW_READY", window: role };
}

export function windowClosed(role: WindowRole): SessionMessage {
    return { type: "WINDOW_CLOSED", window: role };
}

export function windowHeartbeat(role: WindowRole, ts: number): SessionMessage {
    return { type: "WINDOW_HEARTBEAT", window: role, ts: ts };
}

export function sessionInfo(userFullName?: string, token?: string): SessionMessage {
    return { type: "SESSION_INFO", userFullName: userFullName, token: token };
}

export function sessionClosed(): SessionMessage {
    return { type: "SESSION_CLOSED" };
}