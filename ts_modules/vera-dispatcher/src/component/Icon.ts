import { html } from "../util";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        :host {
            display: inline-block;
            vertical-align: middle;
        }

        svg {
            display: block;
            fill: transparent;
            stroke: currentcolor;
            width: 100%;
            height: 100%;
        }
    </style>
    <svg><use></use></svg>
`;

class Icon extends HTMLElement {

    static get observedAttributes() {
        return ["name"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        if (!this.shadowRoot!.hasChildNodes()) {
            this.shadowRoot!.appendChild(template.content.cloneNode(true));
        }
        this._updateIcon();
    }

    attributeChangedCallback() {
        this._updateIcon();
    }

    _updateIcon() {
        const name = this.getAttribute("name");
        const use = this.shadowRoot!.querySelector("use");
        use?.setAttribute("href", `/icons.svg#${name}`);
    }
}

customElements.define('vera-icon', Icon);