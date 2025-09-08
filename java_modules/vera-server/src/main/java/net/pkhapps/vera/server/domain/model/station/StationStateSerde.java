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
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.SerdeContext;
import net.pkhapps.vera.server.util.serde.UnsupportedTypeIdException;

import java.util.Set;

/// [Serde] for [net.pkhapps.vera.server.domain.model.station.Station.StationState].
class StationStateSerde implements Serde<Station.StationState> {

    static final int TYPE_ID = 100;

    @Override
    public void serialize(SerdeContext context, Station.StationState object, Output output) {
        var writer = context.newWriter(TYPE_ID);
        writer.putObject(object.name());
        writer.putObject(object.location());
        writer.putNullableString(object.note());
        writer.writeTo(output);
    }

    @Override
    public int computeSize(SerdeContext context, Station.StationState object) {
        return context.newSizeCalculator()
                .addObject(object.name())
                .addObject(object.location())
                .addNullableString(object.note())
                .size();
    }

    @Override
    public Station.StationState deserialize(SerdeContext context, int typeId, byte[] payload) {
        if (typeId != TYPE_ID) {
            throw new UnsupportedTypeIdException(typeId);
        }
        var reader = context.newReader(payload);
        return new Station.StationState(
                reader.getObject(MultiLingualString.class),
                reader.getObject(Wgs84Point.class),
                reader.getNullableString()
        );
    }

    @Override
    public Set<Integer> supportedTypeIds() {
        return Set.of(TYPE_ID);
    }
}
