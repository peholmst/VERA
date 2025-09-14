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

/// Data structure containing the minimum data required to create a new station.
///
/// @param nameSv   the name of the station in Swedish
/// @param nameFi   the name of the station in Finnish
/// @param location the geographical location of the station
public record CreateStationSpec(
        String nameSv,
        String nameFi,
        Wgs84Point location
) {
}
