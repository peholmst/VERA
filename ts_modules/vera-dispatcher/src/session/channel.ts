import { SessionMessage } from "./messages";

const channel = new BroadcastChannel("vera-session");

export function post(msg: SessionMessage) {
    channel.postMessage(msg);
}

export function onMessage(handler: (msg: SessionMessage) => void) {
    channel.onmessage = e => handler(e.data as SessionMessage);
}
