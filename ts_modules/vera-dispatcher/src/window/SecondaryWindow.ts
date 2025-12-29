import { html } from "../util";
import { registerWindowLifecycle } from "../session/windowLifecycle";
import { createSessionInfoStore, getWebSocketConnectionStateMessage, registerSessionMessageHandler, SessionInfo } from "../session/sessionInfo";
import { WebSocketClient } from "../session/WebSocketClient";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        #content-container {
            flex-grow: 1;
            display: flex;        
        }      
    </style>
    <main id="secondary-window" role="application">
        <header class="app-header">
            <span class="app-name">VERA</span>
        </header>
        <div id="content-container">
            This is the secondary window.
        </div>
        <footer class="app-footer">
            <span id="app-user"></span>
            <span id="app-connection-status" class="success">Connected</span>
        </footer>
        <dialog id="session-error-dialog" closedby="none">
        </dialog>
    </main>
`;

class SecondaryWindow extends HTMLElement {

    private unregisterWindowLifecycle?: () => void;
    private unregisterSessionMessageHandler?: () => void;
    private sessionIntoStore = createSessionInfoStore();
    private client = new WebSocketClient(this.sessionIntoStore);
    private windowDiv?: HTMLDivElement;

    constructor() {
        super();
    }

    connectedCallback() {
        if (!this.windowDiv) {
            this.replaceChildren(template.content.cloneNode(true));

            // Lookup important elements
            this.windowDiv = this.byId<HTMLDivElement>("secondary-window");

            // Subscribe to state stores
            this.sessionIntoStore.subscribe(this.onSessionInfoChanged);
        }
        this.unregisterSessionMessageHandler = registerSessionMessageHandler(this.sessionIntoStore);
        this.unregisterWindowLifecycle = registerWindowLifecycle("secondary");
        this.client.start();
    }

    disconnectedCallback() {
        this.client.stop();
        this.unregisterWindowLifecycle?.();
        this.unregisterSessionMessageHandler?.();
    }

    private onSessionInfoChanged = (sessionInfo: Readonly<SessionInfo>) => {
        if (sessionInfo.active === undefined) {
            this.setSessionErrorMessage("Waiting for session information...");
            return;
        } else if (sessionInfo.active === false) {
            this.setSessionErrorMessage("Session terminated");
            return;
        }

        if (sessionInfo.user) {
            const appUserSpan = this.byId<HTMLSpanElement>("app-user");
            appUserSpan.textContent = sessionInfo.user.displayName;
        }

        this.setSessionErrorMessage(getWebSocketConnectionStateMessage(sessionInfo.webSocket?.state));
    }

    private setSessionErrorMessage(message?: string) {
        const sessionErrorDialog = this.byId<HTMLDialogElement>("session-error-dialog");
        const appConnectionStateSpan = this.byId<HTMLSpanElement>("app-connection-status");
        if (message) {
            appConnectionStateSpan.hidden = true;
            sessionErrorDialog.textContent = message;
            sessionErrorDialog.showModal();
        } else {
            appConnectionStateSpan.hidden = false;
            sessionErrorDialog.textContent = "";
            sessionErrorDialog.close();
        }
    }

    private byId<T extends HTMLElement>(id: string): T {
        return this.querySelector("#" + id)! as T;
    }
}

customElements.define("vera-secondary-window", SecondaryWindow);
