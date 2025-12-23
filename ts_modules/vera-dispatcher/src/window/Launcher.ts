import { onMessage } from "../session/channel";
import { WindowRole } from "../session/messages";
import { html } from "../util";

// Try to prevent multiple instances of the launcher from running

const LOCK_KEY = "vera-launcher-active";

if (localStorage.getItem(LOCK_KEY)) {
    alert("VERA is already running");
    window.close();
}

localStorage.setItem(LOCK_KEY, "true");

window.addEventListener("beforeunload", () => {
    localStorage.removeItem(LOCK_KEY);
});

// Map of currently active windows
const windows = new Map<WindowRole, number>();

// Exhaustive message handler
onMessage(msg => {
    switch (msg.type) {
        case "WINDOW_READY":
            console.info(`Window ${msg.window} is ready`);
            windows.set(msg.window, Date.now());
            break;

        case "WINDOW_CLOSED":
            console.info(`Window ${msg.window} is closed`);
            windows.delete(msg.window);
            break;

        case "WINDOW_HEARTBEAT":
            windows.set(msg.window, msg.ts);
            break;

        default:
            throw new Error(`Unhandled message: ${JSON.stringify(msg)}`);
    }
});

// Watchdog to look for dead windows
setInterval(() => {
    const now = Date.now();
    for (const [win, ts] of windows) {
        if (now - ts > 5000) {
            windows.delete(win);
            console.warn(`Window ${win} considered dead`);
        }
    }
}, 3000);

function openWindows() {
    if (!windows.has("primary")) {
        window.open("/primary.html", "vera-primary-window", "popup");
    }
    if (!windows.has("secondary")) {
        window.open("/secondary.html", "vera-secondary-window", "popup");
    }
}

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
    </main>
`;

export class Launcher extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        if (!this.shadowRoot!.hasChildNodes()) {
            this.shadowRoot!.appendChild(template.content.cloneNode(true));

            const startButton = this.shadowRoot!.getElementById("start-btn") as HTMLButtonElement;
            startButton.addEventListener("click", () => {
                openWindows();
            });
        }
    }

    disconnectedCallback() {
    }
}

customElements.define("vera-launcher", Launcher);
