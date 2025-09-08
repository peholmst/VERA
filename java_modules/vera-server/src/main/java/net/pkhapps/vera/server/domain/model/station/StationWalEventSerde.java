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

/// [Serde] for [net.pkhapps.vera.server.domain.model.station.Station.StationWalEvent].
class StationWalEventSerde implements Serde<Station.StationWalEvent> {

    static final int TYPE_ID_SET_NAME = StationStateSerde.TYPE_ID + 1;
    static final int TYPE_ID_SET_LOCATION = StationStateSerde.TYPE_ID + 2;
    static final int TYPE_ID_SET_NOTE = StationStateSerde.TYPE_ID + 3;

    @Override
    public void serialize(SerdeContext context, Station.StationWalEvent object, Output output) {
        switch (object) {
            case Station.StationWalEvent.SetName setName -> {
                var writer = context.newWriter(TYPE_ID_SET_NAME);
                writer.putObject(setName.name());
                writer.writeTo(output);
            }
            case Station.StationWalEvent.SetLocation setLocation -> {
                var writer = context.newWriter(TYPE_ID_SET_LOCATION);
                writer.putObject(setLocation.location());
                writer.writeTo(output);
            }
            case Station.StationWalEvent.SetNote setNote -> {
                var writer = context.newWriter(TYPE_ID_SET_NOTE);
                writer.putNullableString(setNote.note());
                writer.writeTo(output);
            }
        }
    }

    @Override
    public int computeSize(SerdeContext context, Station.StationWalEvent object) {
        switch (object) {
            case Station.StationWalEvent.SetName setName -> {
                return context.newSizeCalculator().addObject(setName.name()).size();
            }
            case Station.StationWalEvent.SetLocation setLocation -> {
                return context.newSizeCalculator().addObject(setLocation.location()).size();
            }
            case Station.StationWalEvent.SetNote setNote -> {
                return context.newSizeCalculator().addNullableString(setNote.note()).size();
            }
        }
    }

    @Override
    public Station.StationWalEvent deserialize(SerdeContext context, int typeId, byte[] payload) {
        var reader = context.newReader(payload);
        switch (typeId) {
            case TYPE_ID_SET_NAME -> {
                return new Station.StationWalEvent.SetName(reader.getObject(MultiLingualString.class));
            }
            case TYPE_ID_SET_LOCATION -> {
                return new Station.StationWalEvent.SetLocation(reader.getObject(Wgs84Point.class));
            }
            case TYPE_ID_SET_NOTE -> {
                return new Station.StationWalEvent.SetNote(reader.getNullableString());
            }
            default -> throw new UnsupportedTypeIdException(typeId);
        }
    }

    @Override
    public Set<Integer> supportedTypeIds() {
        return Set.of(TYPE_ID_SET_NAME, TYPE_ID_SET_LOCATION, TYPE_ID_SET_NOTE);
    }
}
