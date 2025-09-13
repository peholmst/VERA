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
import net.pkhapps.vera.server.util.Deferred;
import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.UnknownInputException;

/// [Serde] for [net.pkhapps.vera.server.domain.model.station.Station.StationWalEvent].
final class StationWalEventSerde implements Serde<Station.StationWalEvent> {

    private static final short TYPE_ID_SET_NAME = 1;
    private static final short TYPE_ID_SET_LOCATION = 2;
    private static final short TYPE_ID_SET_NOTE = 3;
    private static final Deferred<StationWalEventSerde> INSTANCE = new Deferred<>(StationWalEventSerde::new);

    public static StationWalEventSerde instance() {
        return INSTANCE.get();
    }

    private StationWalEventSerde() {
    }

    @Override
    public void writeTo(Station.StationWalEvent object, Output output) {
        switch (object) {
            case Station.StationWalEvent.SetName setName -> {
                output.writeShort(TYPE_ID_SET_NAME);
                MultiLingualStringSerde.instance().writeTo(setName.name(), output);
            }
            case Station.StationWalEvent.SetLocation setLocation -> {
                output.writeShort(TYPE_ID_SET_LOCATION);
                Wgs84PointSerde.instance().writeTo(setLocation.location(), output);
            }
            case Station.StationWalEvent.SetNote setNote -> {
                output.writeShort(TYPE_ID_SET_NOTE);
                output.writeString(setNote.note());
            }
        }
    }

    @Override
    public Station.StationWalEvent readFrom(Input input) {
        var typeId = input.readShort();
        switch (typeId) {
            case TYPE_ID_SET_NAME -> {
                return new Station.StationWalEvent.SetName(MultiLingualStringSerde.instance().readFrom(input));
            }
            case TYPE_ID_SET_LOCATION -> {
                return new Station.StationWalEvent.SetLocation(Wgs84PointSerde.instance().readFrom(input));
            }
            case TYPE_ID_SET_NOTE -> {
                return new Station.StationWalEvent.SetNote(input.readString());
            }
            default -> throw new UnknownInputException("Unknown typeId: " + typeId);
        }
    }
}
