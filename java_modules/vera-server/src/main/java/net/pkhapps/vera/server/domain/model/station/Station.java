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
import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;
import org.jspecify.annotations.Nullable;

/// Aggregate representing a station.
///
/// @see StationRepository
public final class Station extends Aggregate<StationId, Station.StationState, Station.StationWalEvent> {

    private volatile MultiLingualString name;
    private volatile Wgs84Point location;
    private volatile @Nullable String note;

    Station(WriteAheadLog wal, StationId stationId, MultiLingualString name, Wgs84Point location) {
        super(wal, stationId);
        this.name = name;
        this.location = location;
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

    public void setName(MultiLingualString name) {
        appendToWal(new StationWalEvent.SetName(name));
    }

    public Wgs84Point location() {
        return location;
    }

    public void setLocation(Wgs84Point location) {
        appendToWal(new StationWalEvent.SetLocation(location));
    }

    public @Nullable String note() {
        return note;
    }

    public void setNote(@Nullable String note) {
        appendToWal(new StationWalEvent.SetNote(note));
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

    /// Super interface for WAL events written by the station aggregate.
    protected sealed interface StationWalEvent {

        record SetName(MultiLingualString name) implements StationWalEvent {
        }

        record SetLocation(Wgs84Point location) implements StationWalEvent {
        }

        record SetNote(@Nullable String note) implements StationWalEvent {
        }
    }

    /// Record representing the state of a [Station].
    protected record StationState(
            MultiLingualString name,
            Wgs84Point location,
            @Nullable String note
    ) {
    }
}
