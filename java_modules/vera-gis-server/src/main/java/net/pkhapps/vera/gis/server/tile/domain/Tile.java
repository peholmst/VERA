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

package net.pkhapps.vera.gis.server.tile.domain;

import org.jspecify.annotations.NullMarked;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

@NullMarked
public final class Tile {

    private final TileMatrix tileMatrix;
    private final int x;
    private final int y;
    private final Coordinate topLeft;
    private final Coordinate bottomRight;
    private final double sizeMeters;

    public Tile(TileMatrix tileMatrix, int x, int y) {
        this.tileMatrix = tileMatrix;
        this.x = x;
        this.y = y;
        sizeMeters = tileMatrix.tileSizePixels() * tileMatrix.resolutionMetersPerPixel();
        var tileMatrixTopLeft = tileMatrix.topLeft();
        this.topLeft = new Coordinate(tileMatrixTopLeft.x + x * sizeMeters, tileMatrixTopLeft.y - y * sizeMeters);
        this.bottomRight = new Coordinate(topLeft.x + sizeMeters, topLeft.y - sizeMeters);
    }

    public TileMatrix tileMatrix() {
        return tileMatrix;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Coordinate topLeft() {
        return topLeft.copy();
    }

    public Coordinate bottomRight() {
        return bottomRight.copy();
    }

    public Envelope bounds() {
        return new Envelope(topLeft, bottomRight);
    }

    public double sizeMeters() {
        return sizeMeters;
    }

    @Override
    public String toString() {
        return "%s[tileMatrix=%s, x=%d, y=%d, sizeInMeters=%f, topLeft=%s]".formatted(getClass().getSimpleName(),
                tileMatrix, x, y, sizeMeters, topLeft);
    }
}
