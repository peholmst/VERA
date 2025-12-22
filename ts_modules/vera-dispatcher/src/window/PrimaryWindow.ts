import { html } from "../util";

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
        }        
    </style>
    <div id="primary-window" role="application">
        This is the primary window.
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
