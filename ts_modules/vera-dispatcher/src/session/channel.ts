import { SessionMessage } from "./messages";

const channel = new BroadcastChannel("vera-session");

const handlers = new Set<(msg: SessionMessage) => void>();

channel.onmessage = e => {
    const msg = e.data as SessionMessage;
    handlers.forEach(handler => handler(msg));
};

export function post(msg: SessionMessage) {
    channel.postMessage(msg);
}

export interface ChannelRegistration {
    remove(): void;
}

export function registerMessageHandler(handler: (msg: SessionMessage) => void): () => void {
    handlers.add(handler);
    return () => handlers.delete(handler);
}
