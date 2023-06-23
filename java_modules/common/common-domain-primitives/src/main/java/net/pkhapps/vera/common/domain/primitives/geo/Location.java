/*
 * Copyright (c) 2023 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.pkhapps.vera.common.domain.primitives.geo;

import static java.util.Objects.requireNonNull;

/**
 * Value object representing a geographical location on a Cartesian plane projected by the given Coordinate Reference
 * System.
 *
 * @param crs       the CRS of the coordinates denoting the location.
 * @param longitude the longitude (X-coordinate).
 * @param latitude  the latitude (Y-coordinate).
 * @author Petter Holmström
 */
public record Location(
        CoordinateReferenceSystem crs,
        Coordinate.Longitude longitude,
        Coordinate.Latitude latitude) {

    /**
     * Creates a new {@code Location}.
     *
     * @param crs       the CRS of the coordinates, never {@code null}.
     * @param longitude the longitude (X-coordinate), never {@code null}.
     * @param latitude  the latitude (Y-coordinate), never {@code null}.
     * @throws IllegalArgumentException if the coordinates are invalid within the given CRS.
     */
    public Location {
        requireNonNull(crs, "crs must not be null");
        requireNonNull(longitude, "longitude must not be null");
        requireNonNull(latitude, "latitude must not be null");

        crs.validateCoordinate(longitude);
        crs.validateCoordinate(latitude);
    }
}
