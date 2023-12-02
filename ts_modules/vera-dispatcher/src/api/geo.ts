import { MultilingualString } from "./i18n"

export type SRID = 3067 | 4326
export const WGS84 : SRID = 4326
export const TM35FIN : SRID = 3067

export type Point = {
    latitude: number,
    longitude: number,
    srid: SRID
}

export type MunicipalityCode = string;
export type Municipality = {
    code: MunicipalityCode,
    name: MultilingualString
}

export interface MunicipalityLookupService {
    findByPoint(point: Point): Promise<Municipality | null>   
    findByName(name: string): Promise<Municipality[]>
}

type BaseAddressLookupResult = {
    suggestedMapLocation: Point,
    suggestedMapZoomLevel: number,
    municipality: Municipality
}

export type AddressPoint = BaseAddressLookupResult & {
    kind: "AddressPoint",
    addressPointName: MultilingualString,
    addressPointNumber?: string
}

export type RoadSegment = BaseAddressLookupResult & {
    kind: "RoadSegment",
    roadName?: MultilingualString,
    roadNumber?: string,
    minAddressNumber?: number,
    maxAddressNumber?: number
}

export type NamedAddress = AddressPoint | RoadSegment

export type AddressLookupQueryParams = {
    municipality?: MunicipalityCode,
    name: string,
    number?: string
}

export interface AddressLookupService {
    findByPoint(point: Point): Promise<NamedAddress[]>
    findByParams(params: AddressLookupQueryParams): Promise<NamedAddress[]>
}
