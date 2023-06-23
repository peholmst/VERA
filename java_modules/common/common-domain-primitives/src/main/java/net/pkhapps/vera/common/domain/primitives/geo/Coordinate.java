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

    private Coordinate(double value, CoordinateUnit unit) {
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
    public CoordinateUnit unit() {
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
        return Objects.hash(value, unit);
    }

    @Override
    public String toString() {
        return "%s{value=%f, unit=%s}".formatted(getClass().getSimpleName(), value, unit);
    }

    /**
     * Value object representing a longitude (X) coordinate.
     *
     * @see #longitude(double, CoordinateUnit)
     */
    public static final class Longitude extends Coordinate {

        private Longitude(double value, CoordinateUnit unit) {
            super(value, unit);
        }
    }

    /**
     * Value object representing a latitude (Y) coordinate.
     *
     * @see #latitude(double, CoordinateUnit)
     */
    public static final class Latitude extends Coordinate {

        private Latitude(double value, CoordinateUnit unit) {
            super(value, unit);
        }
    }

    /**
     * Creates a new longitude (X-axis) coordinate.
     *
     * @param value the value of the coordinate.
     * @param unit  the unit of the coordinate, never {@code null}.
     * @return a new {@code Longitude} object.
     */
    public static Longitude longitude(double value, CoordinateUnit unit) {
        return new Longitude(value, unit);
    }

    /**
     * Creates a new latitude (Y-axis) coordinate.
     *
     * @param value the value of the coordinate.
     * @param unit  the unit of the coordinate, never {@code null}.
     * @return a new {@code Latitude} object.
     */
    public static Latitude latitude(double value, CoordinateUnit unit) {
        return new Latitude(value, unit);
    }
}
