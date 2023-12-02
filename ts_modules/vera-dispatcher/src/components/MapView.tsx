'use client'

import { Map, View } from 'ol'
import OSM from 'ol/source/OSM'
import TileLayer from 'ol/layer/Tile'
import { useEffect, useMemo, useRef, useState } from 'react'

export default function MapView() {
    const [map, setMap] = useState<Map>();
    const mapElement = useRef<HTMLDivElement>(null)

    useEffect(() => {
        setMap(new Map({
            target: mapElement.current as any,
            layers: [
                new TileLayer({
                    source: new OSM(),
                }),
            ],
            view: new View({
                center: [0, 0],
                zoom: 2,
            }),
        }))
    }, [mapElement.current])

    return (
        <div style={{ height: '100%', width: '100%' }} ref={mapElement}></div>
    )
}