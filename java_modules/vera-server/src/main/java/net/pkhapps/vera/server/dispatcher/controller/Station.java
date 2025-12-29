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

package net.pkhapps.vera.server.dispatcher.controller;

import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.domain.model.station.StationId;
import net.pkhapps.vera.server.resource.ResourceId;

import java.util.List;

record Station(
        StationId id,
        MultiLingualString name,
        Wgs84Point location,
        List<ResourceId> resources
) {
}
