import { html } from "../util";
import "../component/Icon";
import "../component/MapView";
import "../component/ResourceDashboard";
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
        #primary-window {
            display: flex;
            flex-direction: column;
            height: 100%;
        }        

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

        .layout-0-1 #toggle-0-1,
        .layout-1-2 #toggle-1-2,
        .layout-1-1 #toggle-1-1,
        .layout-2-1 #toggle-2-1,
        .layout-1-0 #toggle-1-0 {
            background-color: var(--vera-toolbar-button-selected-color);
        }
    </style>
    <main id="primary-window" role="application" class="layout-1-1">
        <header class="app-header">
            <span class="app-name">VERA</span>
            <div class="toolbar">
                <button id="toggle-0-1" title="Show resources only">
                    <vera-icon name="layout-0-1"></vera-icon>
                </button>
                <button id="toggle-1-2" title="Show mostly resources">
                    <vera-icon name="layout-1-2"></vera-icon>
                </button>
                <button id="toggle-1-1" title="Show both map and resources">
                    <vera-icon name="layout-1-1"></vera-icon>
                </button>
                <button id="toggle-2-1" title="Show mostly map">
                    <vera-icon name="layout-2-1"></vera-icon>
                </button>
                <button id="toggle-1-0" title="Show map only">
                    <vera-icon name="layout-1-0"></vera-icon>
                </button>
            </div>
        </header>
        <div id="content-container">
            <vera-map-view id="map-view"></vera-map-view>
            <vera-resource-dashboard id="resource-dashboard"></vera-resource-dashboard>
        </div>
    </main>
`;

type LayoutClassName = "layout-0-1" | "layout-1-2" | "layout-1-1" | "layout-2-1" | "layout-1-0";

class PrimaryWindow extends HTMLElement {

    windowDiv: HTMLDivElement | null = null;

    constructor() {
        super();
        this.replaceChildren(template.content.cloneNode(true));

        // Lookup important elements
        this.windowDiv = this.$("primary-window") as HTMLDivElement;

        const toggle01 = this.$("toggle-0-1") as HTMLButtonElement;
        const toggle12 = this.$("toggle-1-2") as HTMLButtonElement;
        const toggle11 = this.$("toggle-1-1") as HTMLButtonElement;
        const toggle21 = this.$("toggle-2-1") as HTMLButtonElement;
        const toggle10 = this.$("toggle-1-0") as HTMLButtonElement;

        // Register listeners
        toggle01.addEventListener("click", () => this.setLayout("layout-0-1"));
        toggle12.addEventListener("click", () => this.setLayout("layout-1-2"));
        toggle11.addEventListener("click", () => this.setLayout("layout-1-1"));
        toggle21.addEventListener("click", () => this.setLayout("layout-2-1"));
        toggle10.addEventListener("click", () => this.setLayout("layout-1-0"));
    }

    connectedCallback() {
    }

    disconnectedCallback() {
    }

    $(id: string): HTMLElement | null {
        return this.querySelector("#" + id);
    }

    setLayout(layout: LayoutClassName) {
        this.windowDiv!.className = layout;
    }
}

customElements.define("vera-primary-window", PrimaryWindow);
