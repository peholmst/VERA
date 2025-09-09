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

import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.wal.WalSerde;
import net.pkhapps.vera.server.util.wal.WalSerdeRegistrator;

import java.util.function.Consumer;

/// Base class for [WalSerdeRegistrator]s for a specific aggregate.
///
/// @param <T>  the type of the aggregate
/// @param <ID> the type of the aggregate ID
/// @param <S>  the type of the aggregate state
/// @param <E>  the super type of the aggregate WAL events
public abstract class AggregateWalSerdeRegistrator<T extends Aggregate<ID, S, E>, ID extends Identifier, S extends Record, E> implements WalSerdeRegistrator {

    private final int aggregateSerdeGroupId;
    private final Class<T> aggregateType;
    private final Serde<ID> idSerde;
    private final Serde<S> stateSerde;
    private final Serde<E> eventSerde;

    /// Initializing constructor for `AggregateWalSerdeRegistrator`. Subclasses should call this constructor from their
    /// default constructor.
    ///
    /// @param aggregateSerdeGroupId a globally unique Serde ID whose value will be incremented for each [WalSerde] registered
    /// @param aggregateType         the aggregate type
    /// @param idSerde               the [Serde] for aggregate IDs
    /// @param stateSerde            the [Serde] for aggregate states
    /// @param eventSerde            the [Serde] for aggregate WAL events
    protected AggregateWalSerdeRegistrator(int aggregateSerdeGroupId, Class<T> aggregateType, Serde<ID> idSerde, Serde<S> stateSerde, Serde<E> eventSerde) {
        this.aggregateSerdeGroupId = aggregateSerdeGroupId;
        this.aggregateType = aggregateType;
        this.idSerde = idSerde;
        this.stateSerde = stateSerde;
        this.eventSerde = eventSerde;
    }

    @Override
    public final void registerSerde(Consumer<WalSerde<?>> serdeRegistry) {
        serdeRegistry.accept(new AggregateWalEventSerde<>(aggregateSerdeGroupId + 1, aggregateType, idSerde, eventSerde));
        serdeRegistry.accept(new RepositoryWalEventSerde<>(aggregateSerdeGroupId + 2, aggregateType, idSerde, stateSerde));
        serdeRegistry.accept(new RepositoryWalSnapshotSerde<>(aggregateSerdeGroupId + 3, aggregateType, idSerde, stateSerde));
    }
}
