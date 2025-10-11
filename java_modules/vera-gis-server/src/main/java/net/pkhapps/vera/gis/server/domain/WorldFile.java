/*
 * Copyright (c) 2025 Petter Holmström
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

package net.pkhapps.vera.gis.server.domain;

import org.jspecify.annotations.NullMarked;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

@NullMarked
public record WorldFile(double xScale, double ySkew, double xSkew, double yScale, double x, double y) {

    public Envelope worldBounds(int imageWidth, int imageHeight) {
        var worldTopLeft = new Coordinate(x, y);
        var worldBottomRight = new Coordinate(x + imageWidth * xScale, y + imageHeight * yScale);
        return new Envelope(worldTopLeft, worldBottomRight);
    }

    public WorldFile withScale(double xScale, double yScale) {
        return new WorldFile(xScale, ySkew, xSkew, yScale, x, y);
    }
}
