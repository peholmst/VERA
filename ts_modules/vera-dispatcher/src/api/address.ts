import { MunicipalityCode, Point } from "./geo"
import { MultilingualString } from "./i18n"

type BaseLocation = {
    municipality: MunicipalityCode,
    coordinates: Point,
    description?: string
}

export type PhysicalAddress = BaseLocation &  {
    addressName: MultilingualString,
    addressNumber?: string,
    staircaseLetter?: string,
    apartmentNumber?: string
}

export type RoadLocation = BaseLocation & {
    roadName?: MultilingualString,
    roadNumber?: string,
}

export type RoadIntersection = BaseLocation &  {

}

export type UnnamedLocation = BaseLocation &  {

}

export type Location =  PhysicalAddress | RoadLocation | RoadIntersection | UnnamedLocation
