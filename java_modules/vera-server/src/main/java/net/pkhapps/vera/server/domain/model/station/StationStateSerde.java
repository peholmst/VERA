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

import net.pkhapps.vera.server.domain.model.geo.Wgs84PointSerde;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualStringSerde;
import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;

/// [Serde] for [net.pkhapps.vera.server.domain.model.station.Station.StationState].
class StationStateSerde implements Serde<Station.StationState> {

    private static final StationStateSerde INSTANCE = new StationStateSerde();

    public static StationStateSerde instance() {
        return INSTANCE;
    }

    @Override
    public void writeTo(Station.StationState object, Output output) {
        MultiLingualStringSerde.instance().writeTo(object.name(), output);
        Wgs84PointSerde.instance().writeTo(object.location(), output);
        output.writeNullableString(object.note());
    }

    @Override
    public Station.StationState readFrom(Input input) {
        return new Station.StationState(
                MultiLingualStringSerde.instance().readFrom(input),
                Wgs84PointSerde.instance().readFrom(input),
                input.readNullableString()
        );
    }
}
