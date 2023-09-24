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

package net.pkhapps.vera.gis.domain.wmts;

import net.pkhapps.vera.common.domain.base.BaseAggregateRoot;
import net.pkhapps.vera.common.domain.primitives.geo.BoundingBox;
import net.pkhapps.vera.common.domain.primitives.geo.CoordinateReferenceSystem;
import net.pkhapps.vera.common.domain.primitives.i18n.MultilingualString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Aggregate root representing a WMTS tile matrix.
 */
public final class TileMatrixSet extends BaseAggregateRoot<TileMatrixSetId> {

    private final MultilingualString title;
    private final BoundingBox boundingBox;

    /**
     * Initializing constructor for {@code TileMatrixSet}.
     *
     * @param tileMatrixSetId the ID of the tile matrix set.
     * @param title           the human-readable title of the tile matrix set.
     * @param boundingBox     the bounding box (including the CRS) of the tile matrix set.
     */
    public TileMatrixSet(@NotNull TileMatrixSetId tileMatrixSetId,
                         @Nullable MultilingualString title,
                         @NotNull BoundingBox boundingBox) {
        super(tileMatrixSetId);
        this.title = title;
        this.boundingBox = requireNonNull(boundingBox, "boundingBox must not be null");
    }

    /**
     * Deserializing constructor for {@code TileMatrixSet}.
     *
     * @param source the reader to read data from.
     */
    public TileMatrixSet(@NotNull TileMatrixSetReader source) {
        super(source);
        this.title = source.title().orElse(null);
        this.boundingBox = source.boundingBox();
    }

    /**
     * The minimum bounding rectangle surrounding the tile matrix set.
     */
    public @NotNull BoundingBox boundingBox() {
        return boundingBox;
    }

    /**
     * The title of this tile matrix set, used for display to a human.
     */
    public @NotNull Optional<MultilingualString> title() {
        return Optional.ofNullable(title);
    }

    /**
     * The coordinate reference system of the tile matrix set.
     */
    public @NotNull CoordinateReferenceSystem supportedCRS() {
        return boundingBox().crs();
    }
}
