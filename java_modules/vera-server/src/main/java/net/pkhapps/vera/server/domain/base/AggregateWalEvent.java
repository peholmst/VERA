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

import net.pkhapps.vera.server.util.wal.WalEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/// WAL event that acts as an envelope of events written by aggregates.
///
/// @see RepositoryWalEvent
/// @see RepositoryWalSnapshot
final class AggregateWalEvent<T extends Aggregate<ID, S, E>, ID extends Identifier, S extends Record, E> implements WalEvent {

    private final Class<T> aggregateType;
    private final ID aggregateId;
    private final List<E> events;

    /// Constructor used by the Serde and by factory methods. Clients should not call this method directly.
    ///
    /// **Note:** This constructor does *not* copy the `events` list for performance reasons. Callers must make sure the
    /// list is effectively immutable.
    ///
    /// @param aggregateType the type of the aggregate that wrote the events
    /// @param aggregateId   the ID of the aggregate that wrote the events
    /// @param events        the list of events written by the aggregate
    AggregateWalEvent(Class<T> aggregateType, ID aggregateId, List<E> events) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.events = events;
    }

    /// Creates a new `AggregateWalEvent`.
    ///
    /// @param aggregate the aggregate that wrote the events
    /// @param events    the events written by the aggregate
    @SuppressWarnings("unchecked")
    public static <T extends Aggregate<ID, S, E>, ID extends Identifier, S extends Record, E> AggregateWalEvent<T, ID, S, E> of(T aggregate, Iterable<E> events) {
        return new AggregateWalEvent<>((Class<T>) aggregate.getClass(), aggregate.id(), StreamSupport.stream(events.spliterator(), false).toList());
    }

    /// Creates a new `AggregateWalEvent`.
    ///
    /// @param aggregate the aggregate that wrote the events
    /// @param event     the event written by the aggregate
    @SuppressWarnings("unchecked")
    public static <T extends Aggregate<ID, S, E>, ID extends Identifier, S extends Record, E> AggregateWalEvent<T, ID, S, E> of(T aggregate, E event) {
        return new AggregateWalEvent<>((Class<T>) aggregate.getClass(), aggregate.id(), List.of(event));
    }

    @Override
    public String toString() {
        return "%s[aggregateType=%s, aggregateId=%s, size=%d]".formatted(getClass().getSimpleName(),
                aggregateType.getName(), aggregateId, events.size());
    }

    /// Performs the given `action` for each aggregate event in this WAL event.
    ///
    /// @param action the action to perform for each aggregate event
    public void forEach(Consumer<E> action) {
        events.forEach(action);
    }

    /// Returns the number of aggregate events in this WAL event.
    ///
    /// @return the number of aggregate events
    public int size() {
        return events.size();
    }

    /// Returns the type of the aggregate that owns this WAL event.
    ///
    /// @return the aggregate type
    public Class<T> aggregateType() {
        return aggregateType;
    }

    /// Returns the ID of the aggregate that owns this WAL event.
    ///
    /// @return the aggregate ID
    public ID aggregateId() {
        return aggregateId;
    }
}
