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

package net.pkhapps.vera.gis.server.application;

import net.pkhapps.vera.gis.server.domain.Tile;
import net.pkhapps.vera.gis.server.domain.TileMatrix;
import net.pkhapps.vera.gis.server.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.domain.WorldFile;
import net.pkhapps.vera.gis.server.primaryport.ForImportingRasterTiles;
import net.pkhapps.vera.gis.server.secondaryport.ForStoringRasterTiles;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.jspecify.annotations.NullMarked;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

@NullMarked
public final class RasterTileImporter implements ForImportingRasterTiles {

    private static final Logger log = LoggerFactory.getLogger(RasterTileImporter.class);
    private final ForStoringRasterTiles forStoringRasterTiles;

    public RasterTileImporter(ForStoringRasterTiles forStoringRasterTiles) {
        this.forStoringRasterTiles = forStoringRasterTiles;
    }

    @Override
    public void importWorldFile(TileMatrixSetId tileMatrixSet, SeekableByteChannel worldFile, SeekableByteChannel rasterFile) throws IOException {
        var worldFileData = parseWorldFile(worldFile);
        if (worldFileData.xSkew() != 0 || worldFileData.ySkew() != 0) {
            throw new UnsupportedOperationException("Non-zero skew is not supported");
        }
        if (worldFileData.xScale() != -worldFileData.yScale()) {
            throw new UnsupportedOperationException("Different x and y scales are not supported");
        }
        var rasterFileData = parseRasterFile(rasterFile);
        var worldTopLeft = new Coordinate(worldFileData.x(), worldFileData.y());
        var worldBottomRight = new Coordinate(worldFileData.x() + rasterFileData.getWidth() * worldFileData.xScale(), worldFileData.y() + rasterFileData.getWidth() * worldFileData.yScale());
        var worldBounds = new Envelope(worldTopLeft, worldBottomRight);

        var tileMatrix = TileMatrix.findTileMatrixByResolution(tileMatrixSet, worldFileData.xScale());
        var topLeftTile = tileMatrix.findTileByCoordinate(worldTopLeft.getX(), worldTopLeft.getY());
        var bottomRightTile = tileMatrix.findTileByCoordinate(worldBottomRight.getX(), worldBottomRight.getY());

        for (int y = topLeftTile.y(); y <= bottomRightTile.y(); y++) {
            for (int x = topLeftTile.x(); x <= bottomRightTile.x(); x++) {
                extractTile(tileMatrix.tile(x, y), worldBounds, rasterFileData);
            }
        }
    }

    private void extractTile(Tile tile, Envelope rasterBounds, BufferedImage raster) throws IOException {
        log.info("Importing tile {}", tile);
        var tileBounds = tile.bounds();
        var tileSize = tile.tileMatrix().tileSizePixels();
        var resolution = tile.tileMatrix().resolutionMetersPerPixel();

        var tileOffsetX = (int) Math.max(0, Math.floor((rasterBounds.getMinX() - tileBounds.getMinX()) / resolution));
        var tileWidth = tileSize - tileOffsetX - (int) Math.max(0, Math.floor((tileBounds.getMaxX() - rasterBounds.getMaxX()) / resolution));
        var rasterOffsetX = (int) Math.max(0, Math.ceil((tileBounds.getMinX() - rasterBounds.getMinX()) / resolution));

        var tileOffsetY = (int) Math.max(0, Math.floor((tileBounds.getMaxY() - rasterBounds.getMaxY()) / resolution));
        var tileHeight = tileSize - tileOffsetY - (int) Math.max(0, Math.floor((rasterBounds.getMinY() - tileBounds.getMinY()) / resolution));
        var rasterOffsetY = (int) Math.max(0, Math.ceil((rasterBounds.getMaxY() - tileBounds.getMaxY()) / resolution));

        var tileImage = getTileImage(tile);
        var g = tileImage.createGraphics();
        try {
            g.drawImage(raster,
                    tileOffsetX, tileOffsetY, tileOffsetX + tileWidth, tileOffsetY + tileHeight,
                    rasterOffsetX, rasterOffsetY, rasterOffsetX + tileWidth, rasterOffsetY + tileHeight,
                    null);
        } finally {
            g.dispose();
        }
        setTileImage(tile, tileImage);
    }

    private BufferedImage getTileImage(Tile tile) throws IOException {
        var tileOptional = forStoringRasterTiles.readTile(tile.tileMatrix().tileMatrixSet(), tile.tileMatrix().level(),
                tile.x(), tile.y());
        if (tileOptional.isPresent()) {
            return Imaging.getBufferedImage(tileOptional.get());
        } else {
            return new BufferedImage(tile.tileMatrix().tileSizePixels(), tile.tileMatrix().tileSizePixels(), BufferedImage.TYPE_INT_RGB);
        }
    }

    private void setTileImage(Tile tile, BufferedImage image) throws IOException {
        forStoringRasterTiles.writeTile(tile.tileMatrix().tileMatrixSet(), tile.tileMatrix().level(), tile.x(),
                tile.y(), Imaging.writeImageToBytes(image, ImageFormats.PNG));
    }

    private WorldFile parseWorldFile(SeekableByteChannel worldFile) throws IOException {
        try (var reader = new BufferedReader(Channels.newReader(worldFile, StandardCharsets.UTF_8))) {
            double xScale = Double.parseDouble(reader.readLine());
            double ySkew = Double.parseDouble(reader.readLine());
            double xSkew = Double.parseDouble(reader.readLine());
            double yScale = Double.parseDouble(reader.readLine());
            double x = Double.parseDouble(reader.readLine());
            double y = Double.parseDouble(reader.readLine());
            return new WorldFile(xScale, ySkew, xSkew, yScale, x, y);
        } catch (NullPointerException | NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid world file", ex);
        }
    }

    private BufferedImage parseRasterFile(SeekableByteChannel rasterFile) throws IOException {
        var size = rasterFile.size();
        if (size > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("Raster file is too big");
        }
        var buf = ByteBuffer.allocate((int) size);
        while (buf.hasRemaining()) {
            if (rasterFile.read(buf) < 0) {
                break;
            }
        }
        return Imaging.getBufferedImage(buf.array());
    }
}
