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

import net.pkhapps.vera.common.domain.base.AggregateRootReader;
import net.pkhapps.vera.common.domain.primitives.geo.BoundingBox;
import net.pkhapps.vera.common.domain.primitives.i18n.MultilingualString;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Reader interface for {@link TileMatrixSet}.
 */
public interface TileMatrixSetReader extends AggregateRootReader<TileMatrixSetId> {

    /**
     * @see TileMatrixSet#title()
     */
    @NotNull Optional<MultilingualString> title();

    /**
     * @see TileMatrixSet#boundingBox()
     */
    @NotNull BoundingBox boundingBox();
}
