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
 * Value object representing a single coordinate on a Cartesian plane.
 *
 * @see #latitude(double, CoordinateUnit)
 * @see #longitude(double, CoordinateUnit)
 */
public abstract sealed class Coordinate permits Coordinate.Longitude, Coordinate.Latitude {

    private final double value;
    private final CoordinateUnit unit;

    private Coordinate(double value, @NotNull CoordinateUnit unit) {
        requireNonNull(unit, "unit must not be null");
        this.value = value;
        this.unit = unit;
    }

    /**
     * The numeric value of the coordinate.
     */
    public double value() {
        return value;
    }

    /**
     * The unit of the coordinate.
     */
    public @NotNull CoordinateUnit unit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Double.compare(that.value, value) == 0 && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), value, unit);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s{%f%s}", getClass().getSimpleName(), value, unit.symbol());
    }

    /**
     * Value object representing a longitude (X) coordinate.
     *
     * @see #longitude(double, CoordinateUnit)
     */
    public static final class Longitude extends Coordinate implements Comparable<Longitude> {

        private Longitude(double value, @NotNull CoordinateUnit unit) {
            super(value, unit);
        }

        @Override
        public int compareTo(@NotNull Coordinate.Longitude o) {
            return Double.compare(value(), o.value());
        }
    }

    /**
     * Value object representing a latitude (Y) coordinate.
     *
     * @see #latitude(double, CoordinateUnit)
     */
    public static final class Latitude extends Coordinate implements Comparable<Latitude> {

        private Latitude(double value, @NotNull CoordinateUnit unit) {
            super(value, unit);
        }

        @Override
        public int compareTo(@NotNull Coordinate.Latitude o) {
            return Double.compare(value(), o.value());
        }
    }

    /**
     * Creates a new longitude (X-axis / east-west) coordinate.
     *
     * @param value the value of the coordinate.
     * @param unit  the unit of the coordinate, never {@code null}.
     * @return a new {@code Longitude} object.
     * @see CoordinateUnit#longitude(double) 
     */
    public static @NotNull Longitude longitude(double value, @NotNull CoordinateUnit unit) {
        return new Longitude(value, unit);
    }

    /**
     * Creates a new latitude (Y-axis / north-south) coordinate.
     *
     * @param value the value of the coordinate.
     * @param unit  the unit of the coordinate, never {@code null}.
     * @return a new {@code Latitude} object.
     * @see CoordinateUnit#latitude(double)
     */
    public static @NotNull Latitude latitude(double value, @NotNull CoordinateUnit unit) {
        return new Latitude(value, unit);
    }
}
