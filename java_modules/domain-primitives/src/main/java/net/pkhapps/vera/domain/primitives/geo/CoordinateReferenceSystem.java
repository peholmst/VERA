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

/**
 * Interface describing a Coordinate Reference System (CRS) or Spatial Reference
 * System (SRS). The interface is not intended to be a complete definition of a
 * CRS as would be used in a dedicated GIS. Rather, the interface is defined
 * with the requirements of VERA in mind.
 *
 * @author Petter Holmström
 */
public interface CoordinateReferenceSystem {

    /**
     * Validates the given pair of coordinates.
     *
     * @param x the X-coordinate.
     * @param y the Y-coordinate.
     * @throws IllegalArgumentException if the coordinates are not valid within
     * the context of this CRS.
     */
    void validateCoordinates(double x, double y);

    /**
     * The SRID of this CRS.
     */
    SRID srid();

    /**
     * The human readable name of this CRS.
     */
    String name();
}
