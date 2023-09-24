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
 * Value object representing a point on a Cartesian plane.
 */
public sealed class Point permits Location {

    private final Coordinate.Longitude longitude;
    private final Coordinate.Latitude latitude;

    protected Point(@NotNull Coordinate.Longitude longitude,
                    @NotNull Coordinate.Latitude latitude) {
        this.longitude = requireNonNull(longitude, "longitude must not be null");
        this.latitude = requireNonNull(latitude, "latitude must not be null");

        if (!latitude.unit().equals(longitude().unit())) {
            throw new IllegalArgumentException("Longitude and latitude must have the same unit");
        }
    }

    /**
     * The unit of the coordinates.
     */
    public @NotNull CoordinateUnit unit() {
        return longitude.unit();
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
     * Creates a new {@code Point}.
     *
     * @param longitude the longitude (X-coordinate), never {@code null}.
     * @param latitude  the latitude (Y-coordinate), never {@code null}.
     * @throws IllegalArgumentException if the coordinates don't have the same unit.
     */
    public static @NotNull Point of(@NotNull Coordinate.Longitude longitude,
                                    @NotNull Coordinate.Latitude latitude) {
        return new Point(longitude, latitude);
    }

    /**
     * Creates a new {@code Point}.
     *
     * @param unit      the unit of the coordinates, never {@code null}.
     * @param longitude the longitude (X-coordinate).
     * @param latitude  the latitude (Y-coordinate).
     */
    public static @NotNull Point of(CoordinateUnit unit, double longitude, double latitude) {
        return of(Coordinate.longitude(longitude, unit), Coordinate.latitude(latitude, unit));
    }

    /**
     * Returns the distance to the given destination point, as calculated on a Cartesian plane. This means
     * that the result will be incorrect for long distances where the earth's curvature would have to be taken
     * into account.
     *
     * @param destination the point to calculate the distance to, never {@code null}.
     * @return the distance between the two points.
     */
    public @NotNull Dimension distanceTo(@NotNull Point destination) {
        if (!destination.unit().equals(unit())) {
            throw new IllegalArgumentException("Points must have the same unit");
        }
        var a2 = Math.pow(longitude().value() - destination.longitude.value(), 2);
        var b2 = Math.pow(latitude().value() - destination.latitude().value(), 2);
        var c = Math.sqrt(a2 + b2);
        return Dimension.of(c, unit());
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Point{lon=%.3f%s, lat=%.3f%s}",
                longitude.value(), longitude.unit().symbol(),
                latitude.value(), latitude.unit().symbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Objects.equals(longitude, point.longitude) && Objects.equals(latitude, point.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }
}
