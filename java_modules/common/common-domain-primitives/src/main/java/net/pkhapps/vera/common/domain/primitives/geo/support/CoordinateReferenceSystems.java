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
package net.pkhapps.vera.common.domain.primitives.geo.support;

import net.pkhapps.vera.common.domain.primitives.geo.*;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link SRID}s and {@link CoordinateReferenceSystem}s used in VERA.
 */
public final class CoordinateReferenceSystems {

    private CoordinateReferenceSystems() {
    }

    /**
     * The SRID of <strong>WGS 84</strong> (4326).
     *
     * @see #WGS84
     */
    public static final SRID WGS84_SRID = SRID.of(4326);

    /**
     * The {@link CoordinateReferenceSystem} for <strong>WGS 84</strong>.
     *
     * @see #WGS84_SRID
     */
    public static final CoordinateReferenceSystem WGS84 = new CoordinateReferenceSystem() {

        @Override
        public boolean isCoordinateValid(@NotNull Coordinate coordinate) {
            return coordinate.unit().equals(unit())
                    && isBetween(coordinate, -90, 90, -180, 180);
        }

        @Override
        public @NotNull CoordinateUnit unit() {
            return CoordinateUnits.DEGREE;
        }

        @Override
        public @NotNull SRID srid() {
            return WGS84_SRID;
        }

        @Override
        public @NotNull String name() {
            return "WGS 84";
        }
    };

    /**
     * The SRID of <strong>ETRS89 / TM35FIN</strong> (3067).
     *
     * @see #ETRS89_TM35FIN
     */
    public static final SRID ETRS89_TM35FIN_SRID = SRID.of(3067);

    /**
     * THe {@link CoordinateReferenceSystem} for <strong>ETRS89 / TM35FIN</strong>.
     *
     * @see #ETRS89_TM35FIN_SRID
     */
    public static final CoordinateReferenceSystem ETRS89_TM35FIN = new CoordinateReferenceSystem() {

        @Override
        public boolean isCoordinateValid(@NotNull Coordinate coordinate) {
            return coordinate.unit().equals(unit())
                    && isBetween(coordinate, 1737349.49, 9567789.69, -3669433.9, 3638050.95);
        }

        @Override
        public @NotNull CoordinateUnit unit() {
            return CoordinateUnits.METER;
        }

        @Override
        public @NotNull SRID srid() {
            return ETRS89_TM35FIN_SRID;
        }

        @Override
        public @NotNull String name() {
            return "ETRS89 / TM35FIN(E,N)";
        }
    };

    private static boolean isBetween(@NotNull Coordinate coordinate, double latMin, double latMax, double lonMin, double lonMax) {
        return switch (coordinate) {
            case Coordinate.Latitude lat -> lat.value() >= latMin && lat.value() <= latMax;
            case Coordinate.Longitude lon -> lon.value() >= lonMin && lon.value() <= lonMax;
        };
    }
}
