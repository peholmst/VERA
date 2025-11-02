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

package net.pkhapps.vera.gis.server.tile.service;

import net.pkhapps.vera.gis.server.tile.api.ForImportingRasterTiles;
import net.pkhapps.vera.gis.server.tile.domain.Tile;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrix;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.tile.domain.WorldFile;
import net.pkhapps.vera.gis.server.tile.spi.ForStoringRasterTiles;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.jspecify.annotations.NullMarked;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@NullMarked
public final class RasterTileImportService implements ForImportingRasterTiles {

    private static final Logger log = LoggerFactory.getLogger(RasterTileImportService.class);
    private final ForStoringRasterTiles forStoringRasterTiles;

    /**
     *
     * @param forStoringRasterTiles
     */
    public RasterTileImportService(ForStoringRasterTiles forStoringRasterTiles) {
        this.forStoringRasterTiles = forStoringRasterTiles;
    }

    @Override
    public void importWorldFile(TileMatrixSetId tileMatrixSet, InputStream worldFile, InputStream rasterFile) throws IOException {
        var world = parseWorldFile(worldFile);
        var rasterImage = parseRasterFile(rasterFile);
        var worldBounds = world.worldBounds(rasterImage.getWidth(), rasterImage.getHeight());
        importTiles(tileMatrixSet, worldBounds, rasterImage, world.xScale());
    }

    @Override
    public void importWorldFile(TileMatrixSetId tileMatrixSet, double scaleToResolution, InputStream worldFile, InputStream rasterFile) throws IOException {
        var world = parseWorldFile(worldFile);
        var rasterImage = parseRasterFile(rasterFile);

        if (scaleToResolution == world.xScale()) {
            var worldBounds = world.worldBounds(rasterImage.getWidth(), rasterImage.getHeight());
            importTiles(tileMatrixSet, worldBounds, rasterImage, world.xScale());
        } else {
            var scale = world.xScale() / scaleToResolution;
            int w = (int) (rasterImage.getWidth() * scale);
            int h = (int) (rasterImage.getHeight() * scale);
            log.info("Scaling source image, new width: {}, height: {}", w, h);
            var scaledImage = new BufferedImage(w, h, rasterImage.getType());
            var scaledWorld = world.withScale(scaleToResolution, -scaleToResolution);

            var scalingTransform = new AffineTransform();
            scalingTransform.scale(scale, scale);
            var scaleOp = new AffineTransformOp(scalingTransform, AffineTransformOp.TYPE_BILINEAR);
            scaledImage = scaleOp.filter(rasterImage, scaledImage);

            var worldBounds = scaledWorld.worldBounds(scaledImage.getWidth(), scaledImage.getHeight());
            importTiles(tileMatrixSet, worldBounds, scaledImage, scaleToResolution);
        }
    }

    private void importTiles(TileMatrixSetId tileMatrixSet, Envelope worldBounds, BufferedImage rasterImage, double resolution) throws IOException {
        log.debug("Image bounds: {}", rasterImage.getRaster().getBounds());
        log.debug("World bounds: {}", worldBounds);

        var tileMatrix = TileMatrix.findTileMatrixByResolution(tileMatrixSet, resolution);
        var topLeftTile = tileMatrix.findTileByCoordinate(worldBounds.getMinX(), worldBounds.getMaxY());
        var bottomRightTile = tileMatrix.findTileByCoordinate(worldBounds.getMaxX(), worldBounds.getMinY());

        for (int y = topLeftTile.y(); y <= bottomRightTile.y(); y++) {
            for (int x = topLeftTile.x(); x <= bottomRightTile.x(); x++) {
                extractTile(tileMatrix.tile(x, y), worldBounds, rasterImage);
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

    private WorldFile parseWorldFile(InputStream worldFile) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(worldFile, StandardCharsets.UTF_8))) {
            double xScale = Double.parseDouble(reader.readLine());
            double ySkew = Double.parseDouble(reader.readLine());
            double xSkew = Double.parseDouble(reader.readLine());
            double yScale = Double.parseDouble(reader.readLine());
            double x = Double.parseDouble(reader.readLine());
            double y = Double.parseDouble(reader.readLine());

            if (xSkew != 0 || ySkew != 0) {
                throw new UnsupportedOperationException("Non-zero skew is not supported");
            }
            if (xScale != -yScale) {
                throw new UnsupportedOperationException("Different x and y scales are not supported");
            }

            return new WorldFile(xScale, ySkew, xSkew, yScale, x, y);
        } catch (NullPointerException | NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid world file", ex);
        }
    }

    private BufferedImage parseRasterFile(InputStream rasterFile) throws IOException {
        return Imaging.getBufferedImage(rasterFile);
    }
}
