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

import net.pkhapps.vera.server.domain.base.DuplicateIdentifierException;
import net.pkhapps.vera.server.domain.base.Repository;
import net.pkhapps.vera.server.domain.base.RepositoryAtCapacityException;
import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.util.UnexpectedException;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;

/// Repository of [Station] aggregates.
public final class StationRepository extends Repository<Station, StationId, Station.StationState, Station.StationWalEvent> {

    /// Creates a new `StationRepository`.
    ///
    /// @param wal the WAL to store stations in
    public StationRepository(WriteAheadLog wal) {
        // TODO add capacity
        super(wal, Station.class);
    }

    @Override
    protected Station createFromState(StationId stationId, Station.StationState state) {
        return new Station(wal(), stationId, state);
    }

    /// Creates a new [Station] and adds it to the repository.
    ///
    /// @param name     the name of the station
    /// @param location the location of the station
    /// @throws RepositoryAtCapacityException if the repository is at capacity and cannot accept more aggregates
    public Station create(MultiLingualString name, Wgs84Point location) {
        // TODO Extract this into a helper method somewhere
        int attemptsLeft = 5;
        StationId id = null;
        while (attemptsLeft > 0) {
            id = StationId.randomStationId();
            try {
                return insert(new Station(wal(), id, name, location));
            } catch (DuplicateIdentifierException ex) {
                log.debug("Duplicate StationId {}, retrying...", id);
                attemptsLeft--;
            }
        }
        throw new UnexpectedException("Failed to generate a unique StationId after multiple attempts. Last attempted: " + id);
    }
}
