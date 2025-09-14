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

package net.pkhapps.vera.server.port.admin;

import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.station.Station;
import net.pkhapps.vera.server.util.Locales;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

/// Data structure containing updates to apply to a station. All non-null values will be applied.
///
/// @param nameSv   the new name of the station in Swedish, or `null` to leave unchanged
/// @param nameFi   the new name of the station in Finnish, or `null` to leave unchanged
/// @param location the new location of the station, or `null` to leave unchanged
/// @param note     the new station note, or `null` to leave unchanged
public record UpdateStationSpec(
        @Nullable String nameSv,
        @Nullable String nameFi,
        @Nullable Wgs84Point location,
        @Nullable String note
) {

    /// Applies the changes to the given `station` and `mutator`.
    ///
    /// @param station the station being updated
    /// @param mutator the mutator to use for making the changes
    /// @see Station#update(BiConsumer)
    void applyTo(Station station, Station.Mutator mutator) {
        mutator.setName(
                station.name()
                        .withIgnoringNull(Locales.SWEDISH, nameSv)
                        .withIgnoringNull(Locales.FINNISH, nameFi)
        );
        Optional.ofNullable(location).ifPresent(mutator::setLocation);
        Optional.ofNullable(note).ifPresent(mutator::setNote);
    }
}
