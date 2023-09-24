/*
 * Copyright (c) 2023 Petter Holmström
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

package net.pkhapps.vera.gis.importer.assets;

import net.pkhapps.vera.common.domain.primitives.geo.CoordinateReferenceSystem;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.Objects.requireNonNull;

/**
 * Class for reading and parsing a single world file.
 *
 * @see WorldFile
 */
public final class WorldFileReader implements AutoCloseable {

    private final CoordinateReferenceSystem crs;
    private final InputStream is;

    private WorldFile worldFile;

    /**
     * Creates a new {@code WorldFileReader}.
     *
     * @param crs the CRS of the world file.
     * @param is  the input stream returning the world file contents.
     */
    public WorldFileReader(@NotNull CoordinateReferenceSystem crs, @NotNull InputStream is) {
        this.crs = requireNonNull(crs, "crs must not be null");
        this.is = requireNonNull(is, "is must not be null");
    }

    private @NotNull WorldFile doReadWorldFile() throws IOException {
        var bis = new BufferedReader(new InputStreamReader(is));
        try {
            var xScale = Double.parseDouble(bis.readLine());
            var ySkew = Double.parseDouble(bis.readLine());
            var xSkew = Double.parseDouble(bis.readLine());
            var yScale = Double.parseDouble(bis.readLine());
            var longitude = Double.parseDouble(bis.readLine());
            var latitude = Double.parseDouble(bis.readLine());
            return new WorldFile(xScale, ySkew, xSkew, yScale, crs.createLocation(longitude, latitude));
        } catch (Exception ex) {
            throw new IOException("Invalid world file", ex);
        }
    }

    /**
     * Reads the world file from the input stream. The contents will be cached, so this method can be called multiple
     * times even though the actual reading and parsing will only take place the first time.
     *
     * @return the {@link WorldFile} parsed from the input stream.
     * @throws IOException if the world file could not be read or parsed.
     */
    public @NotNull WorldFile read() throws IOException {
        if (worldFile == null) {
            worldFile = doReadWorldFile();
        }
        return worldFile;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
