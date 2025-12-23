import { html } from "../util";
import { post } from "../session/channel";
import { windowClosed, windowHeartbeat, windowReady } from "../session/messages";

const WINDOW_NAME = "secondary";

post(windowReady(WINDOW_NAME));

window.addEventListener("beforeunload", () => {
    post(windowClosed(WINDOW_NAME));
});

setInterval(() => {
    post(windowHeartbeat(WINDOW_NAME, Date.now()));
}, 2000);

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        :host {
            display: block;
            height: 100vh;
        }

        #secondary-window {
            display: grid;
            grid-template-columns: 1fr;
            grid-template-rows: 1fr;
        }         
    </style>
    <div id="secondary-window" role="application">
        This is the secondary window.
    </div>
`;

export class SecondaryWindow extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        if (!this.shadowRoot!.hasChildNodes()) {
            this.shadowRoot!.appendChild(template.content.cloneNode(true));
        }
    }

    disconnectedCallback() {
    }
}

customElements.define("vera-secondary-window", SecondaryWindow);
