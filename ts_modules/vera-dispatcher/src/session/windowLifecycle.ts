import { post } from "./channel";
import { windowClosed, windowHeartbeat, windowReady, WindowRole } from "./messages";

export function registerWindowLifecycle(
    role: WindowRole,
    heartbeatMs = 2000
): () => void {
    post(windowReady(role));

    const onUnload = () => post(windowClosed(role));
    window.addEventListener("beforeunload", onUnload);

    const heartbeatId = window.setInterval(() => {
        post(windowHeartbeat(role, Date.now()));
    }, heartbeatMs);

    return () => {
        window.removeEventListener("beforeunload", onUnload);
        clearInterval(heartbeatId);
    };
}