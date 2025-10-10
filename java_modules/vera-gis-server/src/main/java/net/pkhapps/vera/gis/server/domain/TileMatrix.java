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

@NullMarked
public final class TileMatrix {

    public static final int TILE_SIZE = 256;
    private static final double RESOLUTION_TOP_M_PER_PX = 8192;
    private static final double CENTER_X_M = 419_924.4;
    private static final double CENTER_Y_M = 7_149_882.0;
    private static final double TOP_M = CENTER_Y_M + (RESOLUTION_TOP_M_PER_PX * TILE_SIZE / 2);
    private static final double BOTTOM_M = CENTER_Y_M - (RESOLUTION_TOP_M_PER_PX * TILE_SIZE / 2);
    private static final double LEFT_M = CENTER_X_M - (RESOLUTION_TOP_M_PER_PX * TILE_SIZE / 2);
    private static final double RIGHT_M = CENTER_X_M + (RESOLUTION_TOP_M_PER_PX * TILE_SIZE / 2);
    private static final Coordinate TOP_LEFT = new Coordinate(LEFT_M, TOP_M);
    private static final Coordinate BOTTOM_RIGHT = new Coordinate(RIGHT_M, BOTTOM_M);

    private final TileMatrixSetId tileMatrixSet;
    private final int level;
    private final double resolution;
    private final double scaleDenominator;
    private final int matrixSize;

    private TileMatrix(TileMatrixSetId tileMatrixSet, int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Level cannot be negative");
        }
        this.tileMatrixSet = tileMatrixSet;
        this.level = level;
        var factor = 1 << level;
        resolution = RESOLUTION_TOP_M_PER_PX / factor;
        scaleDenominator = resolution / 0.00028;
        matrixSize = factor;
    }

    public TileMatrixSetId tileMatrixSet() {
        return tileMatrixSet;
    }

    public int level() {
        return level;
    }

    public double resolutionMetersPerPixel() {
        return resolution;
    }

    public double scaleDenominator() {
        return scaleDenominator;
    }

    public int matrixSize() {
        return matrixSize;
    }

    public Coordinate topLeft() {
        return TOP_LEFT.copy();
    }

    public Coordinate bottomRight() {
        return BOTTOM_RIGHT.copy();
    }

    public int tileSizePixels() {
        return TILE_SIZE;
    }

    public Tile tile(int x, int y) {
        if (x < 0 || y < 0 || x >= matrixSize || y >= matrixSize) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return new Tile(this, x, y);
    }

    public Tile findTileByCoordinate(double left, double top) {
        var offsetX = left - LEFT_M;
        var offsetY = TOP_M - top;
        var tileSizeMeters = TILE_SIZE * resolution;
        var x = (int) Math.floor(offsetX / tileSizeMeters);
        var y = (int) Math.floor(offsetY / tileSizeMeters);
        return new Tile(this, x, y);
    }

    @Override
    public String toString() {
        return "%s[tileMatrixSet=%s, level=%d, size=%dx%d, resolution=%f, topLeft=%s, bottomRight=%s]".formatted(
                getClass().getSimpleName(), tileMatrixSet, level, matrixSize, matrixSize, resolution, TOP_LEFT, BOTTOM_RIGHT);
    }

    public static TileMatrix of(TileMatrixSetId tileMatrixSet, int level) {
        return new TileMatrix(tileMatrixSet, level);
    }

    public static TileMatrix findTileMatrixByResolution(TileMatrixSetId tileMatrixSet, double resolution) {
        var level = (int) Math.floor(Math.log(RESOLUTION_TOP_M_PER_PX / resolution) / Math.log(2));
        return of(tileMatrixSet, level);
    }
}