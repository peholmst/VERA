class Icon extends HTMLElement {

    svg: SVGElement;
    use: SVGUseElement;

    static get observedAttributes() {
        return ["name"];
    }

    constructor() {
        super();
        this.svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        this.use = document.createElementNS("http://www.w3.org/2000/svg", "use");
        this.svg.appendChild(this.use);
        
        this.appendChild(this.svg);

        this.style.display = "inline-block";
        this.style.verticalAlign = "middle";
        
        this.svg.style.display = "block";
        this.svg.style.fill = "transparent";
        this.svg.style.stroke = "currentcolor";
        this.svg.style.width = "100%";
        this.svg.style.height= "100%";
    }

    connectedCallback() {
        this._updateIcon();
    }

    attributeChangedCallback() {
        this._updateIcon();
    }

    _updateIcon() {
        const name = this.getAttribute("name");
        this.use.setAttribute("href", `/icons.svg#${name}`);
    }
}

customElements.define('vera-icon', Icon);