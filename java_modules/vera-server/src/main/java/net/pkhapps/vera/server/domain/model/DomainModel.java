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

package net.pkhapps.vera.server.domain.model;

import net.pkhapps.vera.server.domain.model.station.StationRepository;
import net.pkhapps.vera.server.domain.model.station.StationWalSerdeRegistrator;
import net.pkhapps.vera.server.util.wal.WalSerdeRegistrator;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;

import java.util.List;

/// Creates and configures the domain model for this application.
public final class DomainModel {

    public final StationRepository stationRepository;

    private DomainModel(WriteAheadLog wal) {
        stationRepository = new StationRepository(wal);
    }

    /// Creates a new `DomainModel` that stores data in the given `wal`.
    ///
    /// @param wal the [WriteAheadLog] to use for data storage
    /// @return a new [DomainModel]
    public static DomainModel create(WriteAheadLog wal) {
        return new DomainModel(wal);
    }

    /// Returns a list of [WalSerdeRegistrator]s to pass to [net.pkhapps.vera.server.util.wal.FileSystemWal] when creating it.
    ///
    /// @return an unmodifiable list of [WalSerdeRegistrator]s
    public static List<WalSerdeRegistrator> serdeRegistrators() {
        return List.of(
                StationWalSerdeRegistrator.instance()
        );
    }
}
