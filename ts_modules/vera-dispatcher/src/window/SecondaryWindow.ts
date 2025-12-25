import { html } from "../util";
import { registerWindowLifecycle } from "../session/windowLifecycle";

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
    </main>
`;

class SecondaryWindow extends HTMLElement {

    private unregisterWindowLifecycle?: () => void;
    private windowDiv?: HTMLDivElement;

    constructor() {
        super();
    }

    connectedCallback() {
        if (!this.windowDiv) {
            this.replaceChildren(template.content.cloneNode(true));

            // Lookup important elements
            this.windowDiv = this.byId<HTMLDivElement>("secondary-window");

        }
        this.unregisterWindowLifecycle = registerWindowLifecycle("secondary");
    }

    disconnectedCallback() {
        this.unregisterWindowLifecycle?.();
    }

    private byId<T extends HTMLElement>(id: string): T {
        return this.querySelector("#" + id)! as T;
    }    
}

customElements.define("vera-secondary-window", SecondaryWindow);
