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

package net.pkhapps.vera.gis.server;

import net.pkhapps.vera.gis.server.tile.RasterTileImportService;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.tile.filesystem.FileSystemTileStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ImportTilesApplication {

    static void main(String[] args) throws IOException {
        if (args.length < 3) {
            IO.println("Usage: ImportTiles <tileMatrixSet> <source-dir> <destination-dir> (destination-scale)");
            System.exit(1);
        }
        var tileMatrixSet = TileMatrixSetId.of(args[0]);
        var source = Path.of(args[1]);
        if (!Files.exists(source) || !Files.isDirectory(source)) {
            IO.println("Source directory does not exist: " + source);
            System.exit(1);
        }
        var destination = Path.of(args[2]);
        if (!Files.exists(destination) || !Files.isDirectory(destination)) {
            IO.println("Destination directory does not exist: " + destination);
            System.exit(1);
        }

        var destinationScale = args.length > 3 ? Double.parseDouble(args[3]) : Double.NaN;

        IO.println("Importing tiles from: " + source.toAbsolutePath());
        IO.println("Storing tiles in: " + destination.toAbsolutePath());
        if (!Double.isNaN(destinationScale)) {
            IO.println("Destination scale: " + destinationScale);
        }

        var tileStore = new FileSystemTileStore(destination);
        var importer = new RasterTileImportService(tileStore);

        try (var files = Files.walk(source).filter(path -> path.getFileName().toString().endsWith(".png"))) {
            files.forEach(image -> {
                var worldFile = image.getParent().resolve(image.getFileName().toString().replace("png", "pgw"));
                try (var imageStream = Files.newInputStream(image, StandardOpenOption.READ);
                     var worldFileStream = Files.newInputStream(worldFile, StandardOpenOption.READ)) {
                    if (Double.isNaN(destinationScale)) {
                        importer.importWorldFile(tileMatrixSet, worldFileStream, imageStream);
                    } else {
                        importer.importWorldFile(tileMatrixSet, destinationScale, worldFileStream, imageStream);
                    }
                } catch (IOException e) {
                    IO.println("Error importing tile: " + image);
                    IO.println(e.getMessage());
                    System.exit(1);
                }
            });
        }
    }
}
