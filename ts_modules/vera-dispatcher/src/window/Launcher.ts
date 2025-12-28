import Keycloak, { KeycloakUserInfo } from "keycloak-js";
import { post, registerMessageHandler } from "../session/channel";
import { sessionClosed, sessionInfo, SessionMessage, WindowRole } from "../session/messages";
import { html } from "../util";

// Try to prevent multiple instances of the launcher from running

const LOCK_KEY = "vera-launcher-active";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        :host {
            display: block;
            height: 100vh;
        }

        #launcher {
            display: grid;
            grid-template-columns: 1fr;
            grid-template-rows: 1fr;
        }         
    </style>
    <main id="launcher" role="application">
        <header>VERA</header>
        <div>
            <button id="start-btn">Start Dispatcher Client</button>
        </div>
        <dialog id="session-error-dialog" closedby="none">
        </dialog>        
    </main>
`;

export class Launcher extends HTMLElement {

    private keycloak: Keycloak;
    private windows = new Map<WindowRole, number>();
    private unregisterMessageHandler?: () => void;
    private watchdogTimerId?: number;
    private tokenRefreshTimerId?: number;
    private userInfo?: KeycloakUserInfo;

    constructor() {
        super();
        this.keycloak = new Keycloak({
            url: "https://saturn.pkhapps.net/auth",
            realm: "vera-dev",
            clientId: "vera-dispatcher-client"
        });
        this.attachShadow({ mode: "open" });
    }

    async connectedCallback() {
        // Make sure there is only one launcher window running
        if (localStorage.getItem(LOCK_KEY)) {
            this.setSessionErrorMessage("VERA Dispatcher Client is already running");
            return;
        }
        localStorage.setItem(LOCK_KEY, "true");
        window.addEventListener("beforeunload", this.onBeforeUnload);

        // Authenticate
        try {
            console.log("Authenticating");
            const authenticated = await this.keycloak.init({
                onLoad: "login-required",
                checkLoginIframe: false,
                scope: "profile roles"
            });
            if (!authenticated) {
                this.setSessionErrorMessage("Authentication required");
                return;
            }
            if (!this.keycloak.hasRealmRole("dispatcher")) {
                this.setSessionErrorMessage("Access denied");
                return;
            }
            console.log("Loading user info");
            this.userInfo = await this.keycloak.loadUserInfo();
            this.postSessionInfo();

            // Refresh token
            this.tokenRefreshTimerId = window.setInterval(async () => {
                if (await this.keycloak.updateToken()) {
                    console.log("Token refreshed");
                    this.postSessionInfo();                    
                }
            }, 10_000);

        } catch (error) {
            console.error("Failed to initialize Keycloak adapter:", error);
            this.setSessionErrorMessage("Authentication error");
            return;
        }

        // Connect to channel
        this.unregisterMessageHandler = registerMessageHandler(this.onMessage);

        // Watchdog to look for dead windows
        this.watchdogTimerId = window.setInterval(() => {
            const now = Date.now();
            for (const [win, ts] of this.windows) {
                if (now - ts > 5000) {
                    this.windows.delete(win);
                    console.warn(`Window ${win} considered dead`);
                }
            }
        }, 3000);

        // Setup launcher UI
        if (!this.shadowRoot!.hasChildNodes()) {
            this.shadowRoot!.appendChild(template.content.cloneNode(true));

            const startButton = this.shadowRoot!.getElementById("start-btn") as HTMLButtonElement;
            startButton.addEventListener("click", () => {
                this.openWindows();
            });
        }
    }

    disconnectedCallback() {
        window.clearInterval(this.watchdogTimerId);
        window.clearInterval(this.tokenRefreshTimerId);
        this.unregisterMessageHandler?.();
        window.removeEventListener("beforeunload", this.onBeforeUnload);
    }    

    private onBeforeUnload = () => {
        localStorage.removeItem(LOCK_KEY);
        post(sessionClosed());
    };

    private onMessage = (msg: SessionMessage) => {
        switch (msg.type) {
            case "WINDOW_READY":
                console.info(`Window ${msg.window} is ready`);
                this.windows.set(msg.window, Date.now());
                this.postSessionInfo();
                break;

            case "WINDOW_CLOSED":
                console.info(`Window ${msg.window} is closed`);
                this.windows.delete(msg.window);
                break;

            case "WINDOW_HEARTBEAT":
                this.windows.set(msg.window, msg.ts);
                break;

            default:
                throw new Error(`Unhandled message: ${JSON.stringify(msg)}`);
        }
    };

    private openWindows() {
        if (!this.windows.has("primary")) {
            window.open("/primary.html", "vera-primary-window", "popup");
        }
        if (!this.windows.has("secondary")) {
            window.open("/secondary.html", "vera-secondary-window", "popup");
        }
    };

    private postSessionInfo() {
        if (this.userInfo) {
            console.log("Posting session info to channel");
            post(sessionInfo(this.userInfo.preferred_username, this.userInfo.name, this.keycloak.token));
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
        return this.shadowRoot!.getElementById(id)! as T;
    }    
}

customElements.define("vera-launcher", Launcher);
