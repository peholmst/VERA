import L from "leaflet";
import leaftletStyles from "leaflet/dist/leaflet.css?inline";
import { html } from "../util";
import proj4 from 'proj4';
import 'proj4leaflet';

const template = document.createElement("template");
template.innerHTML = html`
    <style>
        :host {
            display: flex;
            height: 100%;
            width: 100%;
        }

        ${leaftletStyles}

        #mapContainer {
            flex-grow: 1;
        }
    </style>
    <div id="mapContainer"></div>
`;

const crs3067 = new L.Proj.CRS('EPSG:3067', '+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs +type=crs',
    {
        origin: [-628651.6, 8198458],
        bounds: L.bounds([-628651.6, 8198458], [1468500.4, 6101306]),
        resolutions: [
            8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5
        ]
    }
);

export class MapView extends HTMLElement {

    private resizeObserver: ResizeObserver | null = null;
    private mapContainer: HTMLDivElement | null = null;

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        if (!this.shadowRoot!.hasChildNodes()) {
            this.shadowRoot!.appendChild(template.content.cloneNode(true));
            this.mapContainer = this.shadowRoot!.getElementById("mapContainer") as HTMLDivElement;
            const map = L.map(this.mapContainer, {
                center: [60.30669, 22.30100], // TODO Store center somewhere and fetch it from there
                zoom: 10,
                crs: crs3067,
                worldCopyJump: false,
            });
            this.resizeObserver = new ResizeObserver(() => map.invalidateSize());
            L.tileLayer('http://localhost:7070/gis/raster/taustakartta/{z}/{x}/{y}.png', {
                minZoom: 2,
                minNativeZoom: 2,
                maxZoom: 14,
                maxNativeZoom: 14,
                tileSize: 256,
                attribution: '&copy; Maanmittauslaitos'
            }).addTo(map);
        }
        if (this.resizeObserver && this.mapContainer) {
            this.resizeObserver.observe(this.mapContainer);
        }
    }

    disconnectedCallback() {
        this.resizeObserver?.disconnect();
    }
}

customElements.define("vera-map-view", MapView);
