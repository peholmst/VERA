/*
 * Copyright 2023 Petter Holmström
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
package net.pkhapps.vera.domain.primitives.geo;

import static java.util.Objects.requireNonNull;

/**
 * Value object representing a geographical location on a Cartesian plane
 * projected by the given Coordinate Reference System.
 *
 * @param crs the CRS of the coordinates denoting the location.
 * @param x the X-coordinate (longitude).
 * @param y the Y-coordinate (latitude).
 * @author Petter Holmström
 */
public record Location(
        CoordinateReferenceSystem crs,
        double x,
        double y) {

    /**
     * Creates a new {@code Location}.
     *
     * @param crs the CRS of the coordinates.
     * @param x the X coordinate (longitude).
     * @param y the Y coordinate (latitude).
     * @throws IllegalArgumentException if the coordinates are invalid within
     * the given CRS.
     */
    public Location(CoordinateReferenceSystem crs, double x, double y) {
        requireNonNull(crs, "crs must not be null").validateCoordinates(x, y);
        this.crs = crs;
        this.x = x;
        this.y = y;
    }
}
