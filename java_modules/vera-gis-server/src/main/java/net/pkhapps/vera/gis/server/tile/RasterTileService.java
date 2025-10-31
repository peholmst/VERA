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

package net.pkhapps.vera.gis.server.tile;

import net.pkhapps.vera.gis.server.security.AppPermission;
import net.pkhapps.vera.gis.server.tile.api.ForServingRasterTiles;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrix;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.tile.spi.ForStoringRasterTiles;
import net.pkhapps.vera.security.AccessControl;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;

@NullMarked
public final class RasterTileService implements ForServingRasterTiles {

    private static final Logger log = LoggerFactory.getLogger(RasterTileService.class);
    private final AccessControl accessControl;
    private final ForStoringRasterTiles forStoringRasterTiles;
    private final byte[] emptyTile;
    private final byte[] errorTile;

    public RasterTileService(AccessControl accessControl, ForStoringRasterTiles forStoringRasterTiles) {
        this.accessControl = accessControl;
        this.forStoringRasterTiles = forStoringRasterTiles;
        emptyTile = createEmptyTile(Color.GRAY);
        errorTile = createEmptyTile(Color.RED);
    }

    @Override
    public byte[] getTileAsPng(TileMatrixSetId tileMatrixSet, int level, int x, int y, Principal principal) {
        accessControl.requirePermission(principal, AppPermission.READ_TILES);
        if (level < 0) {
            throw new IllegalArgumentException("level cannot be negative");
        }
        if (x < 0) {
            throw new IllegalArgumentException("x cannot be negative");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y cannot be negative");
        }
        try {
            return forStoringRasterTiles.readTile(tileMatrixSet, level, x, y).orElse(emptyTile);
        } catch (IOException e) {
            log.error("Error reading tile tileMatrixSet={}, level={}, x={}, y={}", tileMatrixSet, level, x, y, e);
            return errorTile;
        }
    }

    private byte[] createEmptyTile(Color color) {
        var image = new BufferedImage(TileMatrix.TILE_SIZE, TileMatrix.TILE_SIZE, BufferedImage.TYPE_BYTE_GRAY);
        image.getGraphics().setColor(color);
        image.getGraphics().fillRect(0, 0, TileMatrix.TILE_SIZE, TileMatrix.TILE_SIZE);

        try (var os = new ByteArrayOutputStream()) {
            Imaging.writeImage(image, os, ImageFormats.PNG);
            return os.toByteArray();
        } catch (IOException e) {
            log.error("Error creating empty tile (should never happen)", e);
            throw new RuntimeException(e);
        }
    }
}
