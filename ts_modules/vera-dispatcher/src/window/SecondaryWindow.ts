import { html } from "../util";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
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
