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

package net.pkhapps.vera.server.domain.base;

import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.UnknownInputException;
import net.pkhapps.vera.server.util.wal.WalSerde;

/// [WalSerde] for [RepositoryWalEvent].
final class RepositoryWalEventSerde<T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> extends WalSerde<RepositoryWalEvent<T, ID, S>> {

    private static final short EVENT_TYPE_ID_AGGREGATE_INSERTED = 1;
    private static final short EVENT_TYPE_ID_AGGREGATE_REMOVED = 2;
    private final Class<T> aggregateType;
    private final Serde<ID> idSerde;
    private final Serde<S> stateSerde;

    /// Creates a new `RepositoryWalEventSerde`.
    ///
    /// @param serdeId       the globally unique ID of this [WalSerde] (will be recorded in the WAL).
    /// @param aggregateType the aggregate type
    /// @param idSerde       the [Serde] for aggregate IDs
    /// @param stateSerde    the [Serde] for aggregate states
    public RepositoryWalEventSerde(int serdeId, Class<T> aggregateType, Serde<ID> idSerde, Serde<S> stateSerde) {
        super(serdeId);
        this.aggregateType = aggregateType;
        this.idSerde = idSerde;
        this.stateSerde = stateSerde;
    }

    @Override
    public void writeTo(RepositoryWalEvent<T, ID, S> object, Output output) {
        switch (object) {
            case RepositoryWalEvent.AggregateInserted<T, ID, S> aggregateInserted -> {
                writeHeader(EVENT_TYPE_ID_AGGREGATE_INSERTED, output);
                idSerde.writeTo(aggregateInserted.aggregateId(), output);
                stateSerde.writeTo(aggregateInserted.aggregateState(), output);
            }
            case RepositoryWalEvent.AggregateRemoved<T, ID, S> aggregateRemoved -> {
                writeHeader(EVENT_TYPE_ID_AGGREGATE_REMOVED, output);
                idSerde.writeTo(aggregateRemoved.aggregateId(), output);
            }
        }
    }

    @Override
    public RepositoryWalEvent<T, ID, S> readFrom(Input input) {
        var subTypeId = verifyHeaderAndReadSubTypeId(input);
        switch (subTypeId) {
            case EVENT_TYPE_ID_AGGREGATE_INSERTED -> {
                return new RepositoryWalEvent.AggregateInserted<>(aggregateType, idSerde.readFrom(input), stateSerde.readFrom(input));
            }
            case EVENT_TYPE_ID_AGGREGATE_REMOVED -> {
                return new RepositoryWalEvent.AggregateRemoved<>(aggregateType, idSerde.readFrom(input));
            }
            default -> throw new UnknownInputException("Unknown subTypeId: " + subTypeId);
        }
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof RepositoryWalEvent<?, ?, ?> walEvent
                && walEvent.aggregateType().equals(aggregateType);
    }
}
