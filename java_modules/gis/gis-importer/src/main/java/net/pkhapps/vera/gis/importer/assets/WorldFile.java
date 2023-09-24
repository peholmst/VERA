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

package net.pkhapps.vera.gis.importer.assets;

import net.pkhapps.vera.common.domain.primitives.geo.Location;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Record representing a world file for a raster map image.
 *
 * @param xScale  x-component of the pixel width (in map units/px).
 * @param ySkew   y-component of the pixel width (in map units/px).
 * @param xSkew   x-component of the pixel height (in map units/px).
 * @param yScale  y-component of the pixel height (in map units/px).
 * @param topLeft map coordinates of the center of the top-left pixel of the map image.
 */
public record WorldFile(double xScale,
                        double ySkew,
                        double xSkew,
                        double yScale,
                        @NotNull Location topLeft) {

    public WorldFile {
        requireNonNull(topLeft, "topLeft must not be null");
    }
}
