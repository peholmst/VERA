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

package net.pkhapps.vera.gis.server.secondaryport;

import net.pkhapps.vera.gis.server.domain.TileMatrixSetId;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.Optional;

@NullMarked
public interface ForStoringRasterTiles {

    Optional<byte[]> readTile(TileMatrixSetId tileMatrixSet, int level, int x, int y) throws IOException;

    void writeTile(TileMatrixSetId tileMatrixSet, int level, int x, int y, byte[] data) throws IOException;
}
