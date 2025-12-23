import { html } from "../util";
import "../component/MapView";
import "../component/AddressLookup";
import { post } from "../session/channel";
import { windowClosed, windowHeartbeat, windowReady } from "../session/messages";

const WINDOW_NAME = "primary";

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

        #primary-window {
            display: grid;
            grid-template-columns: 1fr;
            grid-template-rows: 1fr;
            height: 100%;
        }        

        #map-view {
        }
    </style>
    <div id="primary-window" role="application">
        <vera-map-view id="map-view"></vera-map-view>
    </div>
`;

export class PrimaryWindow extends HTMLElement {
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

customElements.define("vera-primary-window", PrimaryWindow);
