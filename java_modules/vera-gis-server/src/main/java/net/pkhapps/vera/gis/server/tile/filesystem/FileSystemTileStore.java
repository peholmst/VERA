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

package net.pkhapps.vera.gis.server.tile.filesystem;

import net.pkhapps.vera.gis.server.tile.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.tile.spi.ForStoringRasterTiles;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@NullMarked
public final class FileSystemTileStore implements ForStoringRasterTiles {

    private static final Logger log = LoggerFactory.getLogger(FileSystemTileStore.class);
    private final Path directory;

    public FileSystemTileStore(Path directory) {
        this.directory = directory;
        log.info("Using directory {}", directory);
    }

    @Override
    public Optional<byte[]> readTile(TileMatrixSetId tileMatrixSet, int level, int x, int y) throws IOException {
        var file = resolve(tileMatrixSet, level, x, y, false);
        if (Files.exists(file)) {
            log.trace("Reading tile {}", file);
            return Optional.of(Files.readAllBytes(file));
        } else {
            log.warn("Tile {} does not exist", file);
            return Optional.empty();
        }
    }

    @Override
    public synchronized void writeTile(TileMatrixSetId tileMatrixSet, int level, int x, int y, byte[] data) throws IOException {
        var path = resolve(tileMatrixSet, level, x, y, true);
        log.trace("Writing tile {}", path);
        Files.write(path, data);
    }

    private Path resolve(TileMatrixSetId tileMatrixSet, int level, int x, int y, boolean createDirectories) throws IOException {
        var dir = directory.resolve(tileMatrixSet.toString(), Integer.toString(level), Integer.toString(x));
        if (createDirectories) {
            Files.createDirectories(dir);
        }
        return dir.resolve("%d.png".formatted(y));
    }
}
