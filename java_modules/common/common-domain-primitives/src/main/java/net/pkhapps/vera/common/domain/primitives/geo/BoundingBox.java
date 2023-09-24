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
 * Value object representing a bounding box on a Cartesian plane projected by the given Coordinate Reference System.
 */
public final class BoundingBox {

    private final CoordinateReferenceSystem crs;
    private final Point lowerCorner;
    private final Point upperCorner;
    private final Dimension2D dimensions;

    private BoundingBox(@NotNull CoordinateReferenceSystem crs,
                        @NotNull Coordinate.Longitude x1,
                        @NotNull Coordinate.Latitude y1,
                        @NotNull Coordinate.Longitude x2,
                        @NotNull Coordinate.Latitude y2) {
        this.crs = requireNonNull(crs, "crs must not be null");

        final var x1Smallest = x1.compareTo(x2) < 0;
        final var y1Smallest = y1.compareTo(y2) < 0;

        if (x1Smallest && y1Smallest) {
            lowerCorner = Point.of(x1, y1);
            upperCorner = Point.of(x2, y2);
        } else if (x1Smallest) {
            lowerCorner = Point.of(x1, y2);
            upperCorner = Point.of(x2, y1);
        } else if (y1Smallest) {
            lowerCorner = Point.of(x2, y1);
            upperCorner = Point.of(x1, y2);
        } else {
            lowerCorner = Point.of(x2, y2);
            upperCorner = Point.of(x1, y1);
        }

        crs.validatePoint(lowerCorner);
        crs.validatePoint(upperCorner);

        dimensions = Dimension2D.of(crs.unit(),
                upperCorner.longitude().value() - lowerCorner.longitude().value(),
                upperCorner.latitude().value() - lowerCorner.latitude().value()
        );
    }

    /**
     * The CRS of the bounding box.
     */
    public @NotNull CoordinateReferenceSystem crs() {
        return crs;
    }

    /**
     * The coordinates of the upper corner (north-east) of the bounding box.
     */
    public @NotNull Point upperCorner() {
        return upperCorner;
    }

    /**
     * The coordinates of the lower corner (south-west) of the bounding box.
     */
    public @NotNull Point lowerCorner() {
        return lowerCorner;
    }

    /**
     * The dimensions of the bounding box.
     */
    public @NotNull Dimension2D dimensions() {
        return dimensions;
    }

    /**
     * Creates a new {@code BoundingBox}.
     *
     * @param crs  the CRS of the bounding box, never {@code null}.
     * @param lon1 the first longitude (X-coordinate), never {@code null}.
     * @param lat1 the first latitude (Y-coordinate), never {@code null}.
     * @param lon2 the second longitude (X-coordinate), never {@code null}.
     * @param lat2 the second latitude (Y-coordinate), never {@code null}.
     * @throws IllegalArgumentException if the coordinates are invalid within the given CRS.
     */
    public static @NotNull BoundingBox of(@NotNull CoordinateReferenceSystem crs,
                                          @NotNull Coordinate.Longitude lon1,
                                          @NotNull Coordinate.Latitude lat1,
                                          @NotNull Coordinate.Longitude lon2,
                                          @NotNull Coordinate.Latitude lat2) {
        return new BoundingBox(crs, lon1, lat1, lon2, lat2);
    }

    /**
     * Creates a new {@code BoundingBox}.
     *
     * @param crs  the CRS of the bounding box, never {@code null}.
     * @param lon1 the first longitude (X-coordinate).
     * @param lat1 the first latitude (Y-coordinate).
     * @param lon2 the second longitude (X-coordinate).
     * @param lat2 the second latitude (Y-coordinate).
     * @throws IllegalArgumentException if the coordinates are invalid within the given CRS.
     */
    public static @NotNull BoundingBox of(@NotNull CoordinateReferenceSystem crs,
                                          double lon1,
                                          double lat1,
                                          double lon2,
                                          double lat2) {
        return of(crs,
                Coordinate.longitude(lon1, crs.unit()),
                Coordinate.latitude(lat1, crs.unit()),
                Coordinate.longitude(lon2, crs.unit()),
                Coordinate.latitude(lat2, crs.unit())
        );
    }

    /**
     * Creates a new {@code BoundingBox}.
     *
     * @param crs the CRS of the bounding box, never {@code null}.
     * @param p1  the first corner point (diagonal to {@code p2}), never {@code null}.
     * @param p2  the second corner point (diagonal to {@code p1}), never {@code null}.
     * @throws IllegalArgumentException if the coordinates are invalid within the given CRS.
     */
    public static @NotNull BoundingBox of(@NotNull CoordinateReferenceSystem crs,
                                          @NotNull Point p1,
                                          @NotNull Point p2) {
        return of(crs, p1.longitude(), p1.latitude(), p2.longitude(), p2.latitude());
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "BoundingBox{crs=%d, lowerCorner=%s, upperCorner=%s}",
                crs.srid().value(),
                lowerCorner, upperCorner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBox that = (BoundingBox) o;
        return Objects.equals(crs, that.crs) && Objects.equals(lowerCorner, that.lowerCorner) && Objects.equals(upperCorner, that.upperCorner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crs, lowerCorner, upperCorner);
    }
}
