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
 * Value object representing a dimension, such as width, height or length.
 */
public final class Dimension {

    private final double value;
    private final CoordinateUnit unit;

    private Dimension(double value, @NotNull CoordinateUnit unit) {
        this.value = value;
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
        this.unit = requireNonNull(unit, "unit must not be null");
    }

    /**
     * Creates a new dimension.
     *
     * @param value the magnitude of the dimension.
     * @param unit  the unit of the dimension, never {@code null}.
     * @return a new {@code Dimension}.
     */
    public static @NotNull Dimension of(double value, @NotNull CoordinateUnit unit) {
        return new Dimension(value, unit);
    }

    /**
     * The magnitude of the dimension.
     */
    public double value() {
        return value;
    }

    /**
     * The unit of the dimension.
     */
    public @NotNull CoordinateUnit unit() {
        return unit;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s{%f%s}", getClass().getSimpleName(), value, unit.symbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension dimension = (Dimension) o;
        return Double.compare(value, dimension.value) == 0 && Objects.equals(unit, dimension.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
}
