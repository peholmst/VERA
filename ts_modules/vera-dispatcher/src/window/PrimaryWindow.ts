import { html } from "../util";
import "../component/Icon";
import "../component/MapView";
import "../component/ResourceDashboard";
import { registerWindowLifecycle } from "../session/windowLifecycle";

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        #content-container {
            flex-grow: 1;
            display: grid;
            grid-template-rows: 1fr;
        }

        .layout-0-1 #map-view {
            display: none;
        }

        .layout-0-1 #content-container {
            grid-template-columns: 0fr 1fr;
        }

        .layout-1-2 #content-container {
            grid-template-columns: 1fr 2fr;
        }

        .layout-1-1 #content-container {
            grid-template-columns: 1fr 1fr;
        }

        .layout-2-1 #content-container {
            grid-template-columns: 2fr 1fr;
        }

        .layout-1-0 #content-container {
            grid-template-columns: 1fr 0fr;
        }

        .layout-1-0 #resource-dashboard {
            display: none;
        }

        .layout-0-1 #toggle-0-1-btn,
        .layout-1-2 #toggle-1-2-btn,
        .layout-1-1 #toggle-1-1-btn,
        .layout-2-1 #toggle-2-1-btn,
        .layout-1-0 #toggle-1-0-btn {
            background-color: var(--vera-toolbar-button-selected-color);
        }
    </style>
    <main id="primary-window" role="application" class="layout-1-1">
        <header class="app-header">
            <span class="app-name">VERA</span>
            <div class="toolbar">
                <button id="toggle-0-1-btn" title="Show resources only (Ctrl+Alt+1)">
                    <vera-icon name="layout-0-1"></vera-icon>
                </button>
                <button id="toggle-1-2-btn" title="Show mostly resources (Ctrl+Alt+2)">
                    <vera-icon name="layout-1-2"></vera-icon>
                </button>
                <button id="toggle-1-1-btn" title="Show both map and resources (Ctrl+Alt+3)">
                    <vera-icon name="layout-1-1"></vera-icon>
                </button>
                <button id="toggle-2-1-btn" title="Show mostly map (Ctrl+Alt+4)">
                    <vera-icon name="layout-2-1"></vera-icon>
                </button>
                <button id="toggle-1-0-btn" title="Show map only (Ctrl+Alt+5)">
                    <vera-icon name="layout-1-0"></vera-icon>
                </button>
                <select id="resource-filter-select">
                </select>
            </div>
        </header>
        <div id="content-container">
            <vera-map-view id="map-view"></vera-map-view>
            <vera-resource-dashboard id="resource-dashboard"></vera-resource-dashboard>
        </div>
    </main>
`;

type LayoutClassName = "layout-0-1" | "layout-1-2" | "layout-1-1" | "layout-2-1" | "layout-1-0";

const LAYOUT_KEYS: Record<string, LayoutClassName> = {
    "Digit1": "layout-0-1",
    "Digit2": "layout-1-2",
    "Digit3": "layout-1-1",
    "Digit4": "layout-2-1",
    "Digit5": "layout-1-0",
};

class PrimaryWindow extends HTMLElement {

    private unregisterWindowLifecycle?: () => void;
    private windowDiv?: HTMLDivElement;

    constructor() {
        super();
    }

    connectedCallback() {
        if (!this.windowDiv) {
            this.replaceChildren(template.content.cloneNode(true));

            // Lookup important elements
            this.windowDiv = this.byId<HTMLDivElement>("primary-window");

            const toggle01 = this.byId<HTMLButtonElement>("toggle-0-1-btn");
            const toggle12 = this.byId<HTMLButtonElement>("toggle-1-2-btn");
            const toggle11 = this.byId<HTMLButtonElement>("toggle-1-1-btn");
            const toggle21 = this.byId<HTMLButtonElement>("toggle-2-1-btn");
            const toggle10 = this.byId<HTMLButtonElement>("toggle-1-0-btn");
            const resourceFilter = this.byId<HTMLSelectElement>("resource-filter-select");

            // Register listeners
            toggle01.addEventListener("click", () => this.setLayout("layout-0-1"));
            toggle12.addEventListener("click", () => this.setLayout("layout-1-2"));
            toggle11.addEventListener("click", () => this.setLayout("layout-1-1"));
            toggle21.addEventListener("click", () => this.setLayout("layout-2-1"));
            toggle10.addEventListener("click", () => this.setLayout("layout-1-0"));
        }
        this.unregisterWindowLifecycle = registerWindowLifecycle("primary");
        window.addEventListener("keydown", this.onKeyDown);
    }

    disconnectedCallback() {
        window.removeEventListener("keydown", this.onKeyDown);
        this.unregisterWindowLifecycle?.();
    }

    private onKeyDown = (e: KeyboardEvent) => {
        console.log(e);
        if (this.isTypingTarget(e.target)) return;

        if (!e.ctrlKey || !e.altKey) return;

        const layout = LAYOUT_KEYS[e.code];

        if (!layout) return;

        e.preventDefault();
        this.setLayout(layout);
    }

    private isTypingTarget(target: EventTarget | null): boolean {
        if (!(target instanceof HTMLElement)) return false;

        return (
            target.tagName === "INPUT" ||
            target.tagName === "TEXTAREA" ||
            target.isContentEditable
        );
    }

    private setLayout(layout: LayoutClassName) {
        this.windowDiv!.classList.remove(
            "layout-0-1",
            "layout-1-2",
            "layout-1-1",
            "layout-2-1",
            "layout-1-0"
        );
        this.windowDiv!.classList.add(layout);
    }

    private byId<T extends HTMLElement>(id: string): T {
        return this.querySelector("#" + id)! as T;
    }
}

customElements.define("vera-primary-window", PrimaryWindow);
