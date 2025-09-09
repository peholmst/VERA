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

import net.pkhapps.vera.server.domain.base.AggregateWalSerdeRegistrator;
import net.pkhapps.vera.server.domain.base.NanoIdentifierSerde;
import net.pkhapps.vera.server.domain.model.GlobalSerdeIds;
import net.pkhapps.vera.server.util.Deferred;

/// [AggregateWalSerdeRegistrator] for [Station].
public final class StationWalSerdeRegistrator extends AggregateWalSerdeRegistrator<Station, StationId, Station.StationState, Station.StationWalEvent> {

    private static final Deferred<StationWalSerdeRegistrator> INSTANCE = new Deferred<>(StationWalSerdeRegistrator::new);

    public static StationWalSerdeRegistrator instance() {
        return INSTANCE.get();
    }

    private StationWalSerdeRegistrator() {
        super(GlobalSerdeIds.STATION_SERDE_GROUP_ID, Station.class, NanoIdentifierSerde.of(StationId::of),
                StationStateSerde.instance(), StationWalEventSerde.instance());
    }
}
