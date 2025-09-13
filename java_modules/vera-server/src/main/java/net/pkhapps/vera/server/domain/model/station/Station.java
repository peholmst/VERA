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

import net.pkhapps.vera.server.domain.base.Aggregate;
import net.pkhapps.vera.server.domain.base.AggregateDeltaBuilder;
import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.util.wal.Durability;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;

import java.util.function.BiConsumer;

/// Aggregate representing a station.
///
/// @see StationRepository
public final class Station extends Aggregate<StationId, Station.StationState, Station.StationWalEvent> {

    private volatile MultiLingualString name;
    private volatile Wgs84Point location;
    private volatile String note;

    Station(WriteAheadLog wal, StationId stationId, MultiLingualString name, Wgs84Point location) {
        super(wal, stationId);
        this.name = name;
        this.location = location;
        this.note = "";
    }

    Station(WriteAheadLog wal, StationId stationId, StationState state) {
        super(wal, stationId);
        this.name = state.name();
        this.location = state.location();
        this.note = state.note();
    }

    public MultiLingualString name() {
        return name;
    }

    public Wgs84Point location() {
        return location;
    }

    public String note() {
        return note;
    }

    @Override
    protected StationState toState() {
        return new StationState(name, location, note);
    }

    @Override
    protected void applyEvent(StationWalEvent event) {
        switch (event) {
            case StationWalEvent.SetName setName -> this.name = setName.name();
            case StationWalEvent.SetLocation setLocation -> this.location = setLocation.location();
            case StationWalEvent.SetNote setNote -> this.note = setNote.note();
        }
    }

    public interface Mutator {
        Mutator setName(MultiLingualString name);

        Mutator setLocation(Wgs84Point location);

        Mutator setNote(String note);
    }

    public void update(BiConsumer<Station, Mutator> action) {
        var deltaBuilder = new AggregateDeltaBuilder<StationWalEvent>();
        action.accept(this, new Mutator() {
            @Override
            public Mutator setName(MultiLingualString name) {
                deltaBuilder.update(name, Station.this.name, StationWalEvent.SetName::new);
                return this;
            }

            @Override
            public Mutator setLocation(Wgs84Point location) {
                deltaBuilder.update(location, Station.this.location, StationWalEvent.SetLocation::new);
                return this;
            }

            @Override
            public Mutator setNote(String note) {
                deltaBuilder.update(note, Station.this.note, StationWalEvent.SetNote::new);
                return this;
            }
        });
        if (deltaBuilder.containsEvents()) {
            appendToWal(deltaBuilder.build(), Durability.IMMEDIATE);
        }
    }

    /// Super interface for WAL events written by the station aggregate.
    protected sealed interface StationWalEvent {

        record SetName(MultiLingualString name) implements StationWalEvent {
        }

        record SetLocation(Wgs84Point location) implements StationWalEvent {
        }

        record SetNote(String note) implements StationWalEvent {
        }
    }

    /// Record representing the state of a [Station].
    protected record StationState(
            MultiLingualString name,
            Wgs84Point location,
            String note
    ) {
    }
}
