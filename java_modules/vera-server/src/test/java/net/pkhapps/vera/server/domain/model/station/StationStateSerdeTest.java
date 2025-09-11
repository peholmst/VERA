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

package net.pkhapps.vera.server.domain.model.station;

import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.util.Locales;
import net.pkhapps.vera.server.util.serde.SerdeTestUtils;
import org.junit.jupiter.api.Test;

class StationStateSerdeTest {

    @Test
    void serialize_deserialize() {
        var state = new Station.StationState(
                MultiLingualString.of(Locales.FINNISH, "Asema 91", Locales.SWEDISH, "Station 91"),
                new Wgs84Point(60.306738, 22.300907),
                "This is a note");
        SerdeTestUtils.assertSerializationAndDeserializationProducesEqualObject(StationStateSerde.instance(), state);
    }
}
