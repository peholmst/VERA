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

import org.jetbrains.annotations.NotNull;

/**
 * Interface describing a Coordinate Reference System (CRS) or Spatial Reference System (SRS). The interface is not
 * intended to be a complete definition of a CRS as would be used in a dedicated GIS. Rather, the interface is defined
 * with the requirements of VERA in mind. Implementations can use enums, singletons or anonymous classes.
 */
public interface CoordinateReferenceSystem {

    /**
     * Checks if the given coordinate is valid within the context of this CRS.
     *
     * @param coordinate the coordinate to validate, never {@code null}.
     * @return true if the coordinate is valid, false if not.
     */
    boolean isCoordinateValid(@NotNull Coordinate coordinate);

    /**
     * Checks if the given coordinate is valid within the context of this CRS and throws an exception if not.
     *
     * @param coordinate the coordinate to validate, never {@code null}.
     * @throws IllegalArgumentException if the coordinate is invalid.
     */
    default void validateCoordinate(@NotNull Coordinate coordinate) {
        if (!isCoordinateValid(coordinate)) {
            throw new IllegalArgumentException("Coordinate is not valid");
        }
    }

    /**
     * Checks if the given point is valid within the context of this CRS.
     *
     * @param point the point to validate, never {@code null}.
     * @return true if the point is valid, false if not.
     */
    default boolean isPointValid(@NotNull Point point) {
        return isCoordinateValid(point.longitude()) && isCoordinateValid(point.latitude());
    }

    /**
     * Checks if the given point is valid within the context of this CRS and throws an exception if not.
     *
     * @param point the point to validate, never {@code null}.
     * @throws IllegalArgumentException if the point is invalid.
     */
    default void validatePoint(@NotNull Point point) {
        if (!isPointValid(point)) {
            throw new IllegalArgumentException("Point is not valid");
        }
    }

    /**
     * The unit of the coordinates in this CRS.
     */
    @NotNull CoordinateUnit unit();

    /**
     * The SRID of this CRS.
     */
    @NotNull SRID srid();

    /**
     * The human-readable name of this CRS.
     */
    @NotNull String name();

    /**
     * Creates a new {@link Location} with the given coordinates in this CRS.
     *
     * @param longitude the longitude coordinate (east-west) in the {@link #unit()} of this CRS.
     * @param latitude  the latitude coordinate (north-south) in the {@link #unit()} of this CRS.
     * @return a new {@link Location}.
     * @throws IllegalArgumentException if any of the coordinates are out of bounds.
     */
    default @NotNull Location createLocation(double longitude, double latitude) {
        return Location.of(this, unit().longitude(longitude), unit().latitude(latitude));
    }
}
