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
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

@NullMarked
public final class TileMatrixSetId {

    private static final int MAX_LENGTH = 100;
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private final String value;

    private TileMatrixSetId(String value) {
        if (value.length() > MAX_LENGTH || value.isEmpty()) {
            throw new IllegalArgumentException("TileMatrixId cannot be empty");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid tile matrix set ID");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TileMatrixSetId that = (TileMatrixSetId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public static TileMatrixSetId of(String value) {
        return new TileMatrixSetId(value);
    }
}
