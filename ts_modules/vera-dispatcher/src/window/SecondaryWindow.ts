import { html } from "../util";
import { registerWindowLifecycle } from "../session/windowLifecycle";
import { registerSessionMessageHandler, SessionInfo } from "../session/sessionInfo";
import { Store } from "../store/Store";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
      
    </style>
    <main id="secondary-window" role="application">
        <header class="app-header">
            <span class="app-name">VERA</span>
        </header>
        <p>
            This is the secondary window.
        </p>
        <footer class="app-footer">
            <span id="app-user"></span>
            <span id="app-connection-status"></span>
        </footer>
        <dialog id="session-error-dialog" closedby="none">
        </dialog>
    </main>
`;

class SecondaryWindow extends HTMLElement {

    private unregisterWindowLifecycle?: () => void;
    private unregisterSessionMessageHandler?: () => void;
    private sessionIntoStore: Store<SessionInfo> = new Store({});
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
    }

    disconnectedCallback() {
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
        this.setSessionErrorMessage(undefined);
        if (sessionInfo.user) {
            const appUserSpan = this.byId<HTMLSpanElement>("app-user");
            appUserSpan.textContent = sessionInfo.user.displayName;
        }
    }

    private setSessionErrorMessage(message?: string) {
        const sessionErrorDialog = this.byId<HTMLDialogElement>("session-error-dialog");
        if (message) {
            sessionErrorDialog.textContent = message;
            sessionErrorDialog.showModal();
        } else {
            sessionErrorDialog.close();
        }
    }

    private byId<T extends HTMLElement>(id: string): T {
        return this.querySelector("#" + id)! as T;
    }
}

customElements.define("vera-secondary-window", SecondaryWindow);
