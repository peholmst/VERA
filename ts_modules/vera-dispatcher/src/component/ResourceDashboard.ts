import { html } from "../util";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        :host {
            background: yellow;
        }
    </style>
    <div id="resource-dashboard">
        Resource Dashboard (PLACEHOLDER)
    </div>
`;

export class ResourceDashboard extends HTMLElement {
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

customElements.define("vera-resource-dashboard", ResourceDashboard);
