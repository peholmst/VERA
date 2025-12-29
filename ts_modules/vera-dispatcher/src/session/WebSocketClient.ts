import { SessionInfo, WebSocketConnectionState } from "./sessionInfo";
import { Store } from "../store/Store";
import { wsAuth } from "./messages";

const wsUri = "ws://127.0.0.1:7071/vera/server/dispatcher/ws" // TODO Fetch from configuration

const backoffSlots = [500, 1000, 2000, 4000, 8000, 16_000, 32_000];

export class WebSocketClient {

    private sessionInfoStore: Store<SessionInfo>;
    private ws?: WebSocket;
    private started = false;
    private backoffSlotsLimit = 0;

    constructor(sessionInfoStore: Store<SessionInfo>) {
        this.sessionInfoStore = sessionInfoStore;
        this.setConnectionState("disconnected");
    }

    start() {
        this.started = true;
        this.tryConnect();
    }

    stop() {
        this.started = false;
        if (this.ws) {
            this.ws.close();
        }
    }

    private tryConnect() {
        if (!this.started || this.ws) {
            return;
        }
        const token = this.sessionInfoStore.snapshot.user?.token;
        if (!token) {
            setTimeout(() => this.tryConnect(), 500);
            return;
        }
        this.ws = this.createSocket();
    }

    private createSocket(): WebSocket {
        this.setConnectionState("connecting");
        console.info("Creating WebSocket");
        const socket = new WebSocket(wsUri);
        socket.addEventListener("open", this.onOpen);
        socket.addEventListener("close", this.onClose);
        socket.addEventListener("error", this.onError);
        socket.addEventListener("message", this.onMessage);
        return socket;
    }

    private onClose = (e: CloseEvent) => {
        console.info("WebSocket closed:", e);
        this.ws = undefined;
        this.setConnectionState("disconnected");

        if (!this.started) {
            return;
        }

        const delay = this.getBackoffMs();
        console.info(`Reconnecting in ${delay} ms...`);
        this.setConnectionState("waiting_to_reconnect");
        setTimeout(() => this.tryConnect(), delay);
    }

    private onError = (e: Event) => {
        console.error("WebSocket error:", e);
    }

    private getBackoffMs(): number {
        const backoffMs = backoffSlots[Math.floor(Math.random() * this.backoffSlotsLimit)];
        this.backoffSlotsLimit = Math.min(this.backoffSlotsLimit + 2, backoffSlots.length);
        return backoffMs;
    }

    private resetBackoff() {
        this.backoffSlotsLimit = 0;
    }

    private onMessage = (e: MessageEvent) => {
        // TODO Implement me
        this.resetBackoff();
    }

    private onOpen = (e: Event) => {
        console.info("WebSocket open:", e);
        this.setConnectionState("connected");
        const token = this.sessionInfoStore.snapshot.user?.token;
        if (token) {
            this.ws?.send(JSON.stringify(wsAuth(token)));
        }
    }

    private setConnectionState(state: WebSocketConnectionState) {
        this.sessionInfoStore.update(s => s.webSocket = { state: state });
    }
}