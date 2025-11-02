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

import net.pkhapps.vera.gis.server.tile.domain.TileMatrix;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.tile.spi.ForStoringRasterTiles;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@NullMarked
class RasterTileImportServiceTest {

    @Test
    void split_raster_into_multiple_tiles() throws IOException {
        var id = TileMatrixSetId.of("test");
        var image = generateTestImage();
        var tileMatrix = TileMatrix.of(id, 12);
        var topLeft = tileMatrix.tile(10, 10).topLeft();

        var world = String.format(Locale.US, """
                        %.1f
                        0.0
                        0.0
                        -%.1f
                        %.2f
                        %.2f
                        """,
                tileMatrix.resolutionMetersPerPixel(),
                tileMatrix.resolutionMetersPerPixel(),
                topLeft.getX() - 50 * tileMatrix.resolutionMetersPerPixel(),
                topLeft.getY() + 50 * tileMatrix.resolutionMetersPerPixel()
        ).getBytes(StandardCharsets.UTF_8);
        var writtenTiles = new AtomicInteger(0);
        var importer = new RasterTileImportService(new ForStoringRasterTiles() {

            @Override
            public Optional<byte[]> readTile(TileMatrixSetId tileMatrixSet, int level, int x, int y) throws IOException {
                return Optional.empty();
            }

            @Override
            public void writeTile(TileMatrixSetId tileMatrixSet, int level, int x, int y, byte[] data) throws IOException {
                assertThat(tileMatrixSet).isEqualTo(id);
                assertThat(level).isEqualTo(tileMatrix.level());
                if (x == 9 && y == 9) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 206, 206, 50, 50));
                    writtenTiles.incrementAndGet();
                } else if (x == 10 && y == 9) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 0, 206, 256, 50));
                    writtenTiles.incrementAndGet();
                } else if (x == 11 && y == 9) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 0, 206, 50, 50));
                    writtenTiles.incrementAndGet();
                } else if (x == 9 && y == 10) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 206, 0, 50, 256));
                    writtenTiles.incrementAndGet();
                } else if (x == 10 && y == 10) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 0, 0, 256, 256));
                    writtenTiles.incrementAndGet();
                } else if (x == 11 && y == 10) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 0, 0, 50, 256));
                    writtenTiles.incrementAndGet();
                } else if (x == 9 && y == 11) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 206, 0, 50, 100));
                    writtenTiles.incrementAndGet();
                } else if (x == 10 && y == 11) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 0, 0, 256, 100));
                    writtenTiles.incrementAndGet();
                } else if (x == 11 && y == 11) {
                    assertThat(data).isEqualTo(generateTestImage(256, 256, 0, 0, 50, 100));
                    writtenTiles.incrementAndGet();
                }
            }
        });
        importer.importWorldFile(TileMatrixSetId.of("test"), new ByteArrayInputStream(world), new ByteArrayInputStream(image));
        assertThat(writtenTiles.get()).isEqualTo(9);
    }

    private byte[] generateTestImage() throws IOException {
        return generateTestImage(356, 406, 0, 0, 356, 406);
    }

    private byte[] generateTestImage(int width, int height, int fillX, int fillY, int fillW, int fillH) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.getGraphics().setColor(Color.RED);
        image.getGraphics().fillRect(fillX, fillY, fillW, fillH);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Imaging.writeImage(image, os, ImageFormats.PNG);

        return os.toByteArray();
    }
}
