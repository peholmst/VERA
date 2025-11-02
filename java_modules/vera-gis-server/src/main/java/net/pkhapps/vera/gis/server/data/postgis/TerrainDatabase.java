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

package net.pkhapps.vera.gis.server.data.postgis;

import net.pkhapps.vera.gis.server.data.domain.AddressPoint;
import net.pkhapps.vera.gis.server.data.domain.Municipality;
import net.pkhapps.vera.gis.server.data.domain.PlaceName;
import net.pkhapps.vera.gis.server.data.domain.RoadSegment;
import net.pkhapps.vera.gis.server.data.spi.ForStoringTerrainData;
import org.jooq.DSLContext;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class TerrainDatabase implements ForStoringTerrainData {

    private final DSLContext create;

    public TerrainDatabase(final DSLContext create) {
        this.create = create;
    }

    @Override
    public void saveAddressPoints(Iterable<AddressPoint> addressPoints) {

    }

    @Override
    public void saveMunicipalities(Iterable<Municipality> municipalities) {

    }

    @Override
    public void savePlaceNames(Iterable<PlaceName> placeNames) {

    }

    @Override
    public void saveRoadSegments(Iterable<RoadSegment> roadSegments) {

    }
}
