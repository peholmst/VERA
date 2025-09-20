export type Wgs84Point = {
    latitude: number,
    longitude: number,
}

export namespace Wgs84 {

    export function toDDM(point: Wgs84Point): { latitude: { degrees: number, minutes: number }, longitude: { degrees: number, minutes: number } } {
        return {
            latitude: coordinateToDDM(point.latitude),
            longitude: coordinateToDDM(point.longitude),
        };
    }

    export function toDMS(point: Wgs84Point): { latitude: { degrees: number, minutes: number, seconds: number }, longitude: { degrees: number, minutes: number, seconds: number } } {
        return {
            latitude: coordinateToDMS(point.latitude),
            longitude: coordinateToDMS(point.longitude),
        };
    }

    function coordinateToDDM(coordinate: number): { degrees: number, minutes: number } {
        const deg = Math.trunc(coordinate) | 0;
        const frac = Math.abs(coordinate) - deg;
        return {
            degrees: deg,
            minutes: 60 * frac,
        };
    }

    function coordinateToDMS(coordinate: number): { degrees: number, minutes: number, seconds: number } {
        const deg = Math.trunc(coordinate) | 0;
        const frac = Math.abs(coordinate) - deg;
        const mins = Math.trunc(60 * frac);
        const secs = 3600 * frac - 60 * mins;
        return {
            degrees: deg,
            minutes: mins,
            seconds: secs
        };
    }

}