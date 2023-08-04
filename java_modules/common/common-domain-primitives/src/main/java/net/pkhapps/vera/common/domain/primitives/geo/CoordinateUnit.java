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
 * Interface defining a coordinate unit (such as meters or degrees). Implementations can use enums, singletons or
 * anonymous classes.
 */
public interface CoordinateUnit {

    /**
     * The human-readable symbol for the unit (language agnostic).
     */
    @NotNull String symbol();

    /**
     * Creates a new longitude (X-axis / east-west) coordinate.
     *
     * @param value the value of the coordinate.
     * @return a new {@code Longitude} object.
     */
    default @NotNull Coordinate.Longitude longitude(double value) {
        return Coordinate.longitude(value, this);
    }

    /**
     * Creates a new latitude (Y-axis / north-south) coordinate.
     *
     * @param value the value of the coordinate.
     * @return a new {@code Latitude} object.
     */
    default @NotNull Coordinate.Latitude latitude(double value) {
        return Coordinate.latitude(value, this);
    }
}
