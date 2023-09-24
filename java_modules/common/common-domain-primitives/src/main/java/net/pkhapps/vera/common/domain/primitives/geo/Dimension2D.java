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
 * Value object representing two dimensions of an object: width and height. The shape of the object is not known,
 * so this class cannot be used to calculate the area.
 */
public final class Dimension2D {
    private final Dimension width;
    private final Dimension height;

    private Dimension2D(@NotNull Dimension width, @NotNull Dimension height) {
        this.width = requireNonNull(width, "width must not be null");
        this.height = requireNonNull(height, "height must not be null");
        if (!width.unit().equals(height.unit())) {
            throw new IllegalArgumentException("Dimensions must have same unit");
        }
    }

    /**
     * Creates a new {@code Dimension2D}.
     *
     * @param width  the width, never {@code null}.
     * @param height the height, never {@code null}.
     * @return a new {@code Dimension2D} object.
     * @throws IllegalArgumentException if the width and height have different units.
     */
    public static @NotNull Dimension2D of(@NotNull Dimension width, @NotNull Dimension height) {
        return new Dimension2D(width, height);
    }

    /**
     * Creates a new {@code Dimension2D}.
     *
     * @param unit   the unit of the dimensions, never {@code null}.
     * @param width  the width.
     * @param height the height.
     * @return a new {@code Dimension2D} object.
     * @throws IllegalArgumentException if the width or the height is negative.
     */
    public static @NotNull Dimension2D of(@NotNull CoordinateUnit unit, double width, double height) {
        return of(unit.dimension(width), unit.dimension(height));
    }

    /**
     * The width of the object.
     */
    public @NotNull Dimension width() {
        return width;
    }

    /**
     * The height of the object.
     */
    public @NotNull Dimension height() {
        return height;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s{width=%f%s, height=%f%s}",
                getClass().getSimpleName(),
                width.value(), width.unit().symbol(),
                height.value(), height.unit().symbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension2D that = (Dimension2D) o;
        return Objects.equals(width, that.width) && Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}
