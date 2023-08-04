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

import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Value object representing a geographical location on a Cartesian plane projected by the given Coordinate Reference
 * System.
 */
public final class Location {

    private final CoordinateReferenceSystem crs;
    private final Coordinate.Longitude longitude;
    private final Coordinate.Latitude latitude;

    private Location(@NotNull CoordinateReferenceSystem crs,
                     @NotNull Coordinate.Longitude longitude,
                     @NotNull Coordinate.Latitude latitude) {
        this.crs = requireNonNull(crs, "crs must not be null");
        this.longitude = requireNonNull(longitude, "longitude must not be null");
        this.latitude = requireNonNull(latitude, "latitude must not be null");

        crs.validateCoordinate(longitude);
        crs.validateCoordinate(latitude);
    }

    /**
     * The CRS of the coordinates denoting the location.
     */
    public @NotNull CoordinateReferenceSystem crs() {
        return crs;
    }

    /**
     * The longitude (X-coordinate).
     */
    public @NotNull Coordinate.Longitude longitude() {
        return longitude;
    }

    /**
     * The latitude (Y-coordinate).
     */
    public @NotNull Coordinate.Latitude latitude() {
        return latitude;
    }

    /**
     * Creates a new {@code Location}.
     *
     * @param crs       the CRS of the coordinates, never {@code null}.
     * @param longitude the longitude (X-coordinate), never {@code null}.
     * @param latitude  the latitude (Y-coordinate), never {@code null}.
     * @throws IllegalArgumentException if the coordinates are invalid within the given CRS.
     * @see CoordinateReferenceSystem#createLocation(double, double)
     */
    public static @NotNull Location of(@NotNull CoordinateReferenceSystem crs,
                                       @NotNull Coordinate.Longitude longitude,
                                       @NotNull Coordinate.Latitude latitude) {
        return new Location(crs, longitude, latitude);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Location{crs=%d, lon=%.3f%s, lat=%.3f%s}",
                crs.srid().value(),
                longitude.value(), longitude.unit().symbol(),
                latitude.value(), latitude.unit().symbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(crs, location.crs) && Objects.equals(longitude, location.longitude) && Objects.equals(latitude, location.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crs, longitude, latitude);
    }
}
