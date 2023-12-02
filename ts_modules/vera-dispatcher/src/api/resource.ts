import { MultilingualString } from "./i18n"

export type ResourceId = string
export type CallSign = string
export type StationId = string

export type Station = {
    id: StationId,
    name: MultilingualString
}

export type Resource = {
    id: ResourceId,
    callSign: CallSign,
    station: StationId,
}