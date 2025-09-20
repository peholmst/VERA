import { Wgs84, Wgs84Point } from "./data/Wgs84Point";
import { html } from "./util";

export type CoordinateFormat = "dd" | "dms" | "ddm";

export class CoordinateField extends HTMLElement {

    private _value: Wgs84Point = { latitude: 0, longitude: 0 };
    private _format: CoordinateFormat = "ddm";
    private shadow: ShadowRoot;
    private formatSelect!: HTMLSelectElement;
    private listenersAdded: boolean = false;

    private inputs: Record<CoordinateFormat, Record<string, HTMLInputElement | null>> = {
        dd: {
            lat: null,
            lon: null
        },
        ddm: {
            latDeg: null,
            latMin: null,
            lonDeg: null,
            lonMin: null
        },
        dms: {
            latDeg: null,
            latMin: null,
            latSec: null,
            lonDeg: null,
            lonMin: null,
            lonSec: null
        }
    };

    constructor() {
        super();
        this.shadow = this.attachShadow({ mode: "open" });
        this.onFormatChange = this.onFormatChange.bind(this);
        this.onInputChange = this.onInputChange.bind(this);
    }

    get value(): Wgs84Point {
        return this._value;
    }

    set value(val: Wgs84Point) {
        this._value = val;
        this.render();
    }

    get format(): CoordinateFormat {
        return this._format;
    }

    set format(val: CoordinateFormat) {
        this._format = val;
        this.render();
    }

    private render() {
        // Show corresponding format group
        Object.keys(this.inputs).forEach(fmt => {
            const div = this.shadow.querySelector(`#${fmt}`) as HTMLDivElement;
            div.style.display = fmt == this._format ? "flex" : "none";
        });

        // Fill inputs from _value
        switch (this._format) {
            case "dd": {
                const currentInputs = this.inputs[this._format];
                currentInputs.lat!.value = this._value.latitude.toFixed(6);
                currentInputs.lon!.value = this._value.longitude.toFixed(6);
                break;
            }
            case "ddm": {
                const ddmCoordinates = Wgs84.toDDM(this._value);
                const currentInputs = this.inputs[this._format];
                currentInputs.latDeg!.value = ddmCoordinates.latitude.degrees.toString();
                currentInputs.latMin!.value = ddmCoordinates.latitude.minutes.toFixed(3);
                currentInputs.lonDeg!.value = ddmCoordinates.longitude.degrees.toString();
                currentInputs.lonMin!.value = ddmCoordinates.longitude.minutes.toFixed(3);
                break;
            }
            case "dms":
                const dmsCoordinates = Wgs84.toDMS(this._value);
                const currentInputs = this.inputs[this._format];
                currentInputs.latDeg!.value = dmsCoordinates.latitude.degrees.toString();
                currentInputs.latMin!.value = dmsCoordinates.latitude.minutes.toString();
                currentInputs.latSec!.value = dmsCoordinates.latitude.seconds.toFixed(3);
                currentInputs.lonDeg!.value = dmsCoordinates.longitude.degrees.toString();
                currentInputs.lonMin!.value = dmsCoordinates.longitude.minutes.toString();
                currentInputs.lonSec!.value = dmsCoordinates.longitude.seconds.toFixed(3);
                break;
        }

        this.formatSelect.value = this._format;
    }

    private updateValueFromInput() {
        // TODO Validation? Smarter parser (accept both . and ,)?
        switch (this._format) {
            case "dd": {
                const currentInputs = this.inputs[this._format];
                const lat = parseFloat(currentInputs.lat!.value) || 0;
                const lon = parseFloat(currentInputs.lon!.value) || 0;
                this._value = { latitude: lat, longitude: lon };
                break;
            }
            case "ddm": {
                const currentInputs = this.inputs[this._format];
                const latDeg = parseInt(currentInputs.latDeg!.value) || 0;
                const latMin = parseFloat(currentInputs.latMin!.value) || 0;
                const lonDeg = parseInt(currentInputs.lonDeg!.value) || 0;
                const lonMin = parseFloat(currentInputs.lonMin!.value) || 0;
                this._value = { latitude: latDeg + latMin / 60, longitude: lonDeg + lonMin / 60 };
                break;
            }
            case "dms": {
                const currentInputs = this.inputs[this._format];
                const latDeg = parseInt(currentInputs.latDeg!.value) || 0;
                const latMin = parseInt(currentInputs.latMin!.value) || 0;
                const latSec = parseFloat(currentInputs.latSec!.value) || 0;
                const lonDeg = parseInt(currentInputs.lonDeg!.value) || 0;
                const lonMin = parseInt(currentInputs.lonMin!.value) || 0;
                const lonSec = parseFloat(currentInputs.lonSec!.value) || 0;
                this._value = { latitude: latDeg + latMin / 60 + latSec / 3600, longitude: lonDeg + lonMin / 60 + lonSec / 3600 };
                break;
            }
        }
        this.dispatchEvent(new CustomEvent("change", { detail: this._value, bubbles: true, composed: true }));
    }

    connectedCallback() {
        if (!this.shadow.innerHTML) {
            this.shadow.innerHTML = html`
                <style>
                    :host {
                        display: inline-flex;
                        flex-wrap: wrap;
                        gap: var(--vera-space-s);
                    }

                    .latitude, .longitude {
                        display: flex;
                        flex-wrap: nowrap;
                        gap: var(--vera-space-xs);
                    }

                    .latitude:before {
                        content: "Lat";
                    }
                    .longitude:before {
                        content: "Lon";
                    }

                    .degrees, .minutes, .seconds {
                        display: inline-flex;
                        position: relative;
                    }
                    .degrees input, .minutes input, .seconds input {
                        width: 100%;
                        box-sizing: border-box;                                                
                    }

                    .degrees:after, .minutes:after, .seconds:after {
                        position: absolute;
                        right: 0.2em;
                        top: -0.2em;
                        pointer-events: none;                        
                    }
                    .degrees:after {
                        content: '\\b0';
                    }
                    .minutes:after {
                        content: '\\2032';
                    }
                    .seconds:after {
                        content: '\\2033';
                    }

                    #dd, #ddm, #dms {
                        display: flex;
                        flex-wrap: wrap;
                        gap: var(--vera-space-s);        
                    }

                    #ddm .degrees, #dms .degrees, #dms .minutes {
                        width: 2.5em;
                    }

                    #ddm .minutes, #dms .seconds {
                        width: 4em;
                    }

                    #dd .degrees {
                        width: 6em;
                    }
                </style>

                <select id="format">
                    <option value="dd">DD</option>
                    <option value="ddm">DDM</option>
                    <option value="dms">DMS</option>
                </select>

                <div id="dd">
                    <div class="latitude">
                        <div class="degrees"><input type="text" id="dd-lat"/></div>
                    </div>
                    <div class="longitude">
                        <div class="degrees"><input type="text" id="dd-lon"/></div>
                    </div>
                </div>

                <div id="ddm">
                    <div class="latitude">
                        <div class="degrees"><input type="text" id="ddm-lat-deg"/></div>
                        <div class="minutes"><input type="text" id="ddm-lat-min"/></div>
                    </div>
                    <div class="longitude">
                        <div class="degrees"><input type="text" id="ddm-lon-deg"/></div>
                        <div class="minutes"><input type="text" id="ddm-lon-min"/></div>
                    </div>
                </div>

                <div id="dms">
                    <div class="latitude">
                        <div class="degrees"><input type="text" id="dms-lat-deg"/></div>
                        <div class="minutes"><input type="text" id="dms-lat-min"/></div>
                        <div class="seconds"><input type="text" id="dms-lat-sec"/></div>
                    </div>
                    <div class="longitude">
                        <div class="degrees"><input type="text" id="dms-lon-deg"/></div>
                        <div class="minutes"><input type="text" id="dms-lon-min"/></div>
                        <div class="seconds"><input type="text" id="dms-lon-sec"/></div>
                    </div>
                </div>
            `;

            this.formatSelect = this.shadow.querySelector<HTMLSelectElement>("#format")!;

            this.inputs["dd"].lat = this.shadow.querySelector<HTMLInputElement>("#dd-lat");
            this.inputs["dd"].lon = this.shadow.querySelector<HTMLInputElement>("#dd-lon");

            this.inputs["ddm"].latDeg = this.shadow.querySelector<HTMLInputElement>("#ddm-lat-deg");
            this.inputs["ddm"].latMin = this.shadow.querySelector<HTMLInputElement>("#ddm-lat-min");
            this.inputs["ddm"].lonDeg = this.shadow.querySelector<HTMLInputElement>("#ddm-lon-deg");
            this.inputs["ddm"].lonMin = this.shadow.querySelector<HTMLInputElement>("#ddm-lon-min");

            this.inputs["dms"].latDeg = this.shadow.querySelector<HTMLInputElement>("#dms-lat-deg");
            this.inputs["dms"].latMin = this.shadow.querySelector<HTMLInputElement>("#dms-lat-min");
            this.inputs["dms"].latSec = this.shadow.querySelector<HTMLInputElement>("#dms-lat-sec");
            this.inputs["dms"].lonDeg = this.shadow.querySelector<HTMLInputElement>("#dms-lon-deg");
            this.inputs["dms"].lonMin = this.shadow.querySelector<HTMLInputElement>("#dms-lon-min");
            this.inputs["dms"].lonSec = this.shadow.querySelector<HTMLInputElement>("#dms-lon-sec");
        }

        if (!this.listenersAdded) {
            this.formatSelect.addEventListener("change", this.onFormatChange);
            Object.values(this.inputs)
                .forEach(group => Object.values(group)
                    .forEach(input => input!.addEventListener("change", this.onInputChange)));
            this.listenersAdded = true;
        }

        this.render();
    }

    disconnectedCallback() {
        Object.values(this.inputs)
            .forEach(group => Object.values(group)
                .forEach(input => input!.removeEventListener("change", this.onInputChange)));                
        this.formatSelect.removeEventListener("change", this.onFormatChange);
        this.listenersAdded = false;
    }

    private onFormatChange() {
        this.format = this.formatSelect.value as CoordinateFormat; // Setter will call render()
    }

    private onInputChange() {
        this.updateValueFromInput(); // Will call render()
    }

    // TODO Focus? Label?
}

customElements.define("vera-coordinate-field", CoordinateField);
