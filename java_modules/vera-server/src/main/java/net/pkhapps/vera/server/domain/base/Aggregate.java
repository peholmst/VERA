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

import net.pkhapps.vera.server.util.wal.Durability;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;

/// Base class for aggregates.
///
/// In this application, aggregates are active objects that are always stored in memory. Any changes made to their state
/// are stored in the application-wide WAL and all active aggregates are restored into memory on application startup.
///
/// @param <ID> the identifier type of the aggregate
/// @param <S>  the record that constitutes the _current_ aggregate state
/// @param <E>  the super type of events that this aggregate will write to the WAL
public abstract class Aggregate<ID extends Identifier, S extends Record, E> {

    private final WriteAheadLog wal;
    private final ID id;

    /// Creates a new aggregate. This method is typically called by a [Repository].
    ///
    /// @param wal the WAL to write changes to
    /// @param id  the ID of the aggregate
    protected Aggregate(WriteAheadLog wal, ID id) {
        this.wal = wal;
        this.id = id;
    }

    /// Gets the ID of the aggregate. This never changes once assigned.
    ///
    /// @return the ID
    public final ID id() {
        return id;
    }

    /// Writes the given events to the WAL. The aggregate should *never change its in-memory state in the same method
    /// that calls this method*. Instead, in-memory state changes should be implemented in [#applyEvent(Object)].
    ///
    /// @param events the events to write to the WAL
    /// @param durability the durability of the write operation
    /// @throws net.pkhapps.vera.server.util.wal.WriteAheadLogException if the events could not be written to the WAL
    /// @see #appendToWal(Object, Durability)
    protected synchronized final void appendToWal(Iterable<E> events, Durability durability) {
        var walEvent = AggregateWalEvent.of(this, events);
        wal.append(walEvent, durability);
        events.forEach(this::applyEvent);
    }

    /// Writes the given event to the WAL. The aggregate should *never change its in-memory state in the same method
    /// that calls this method*. Instead, in-memory state changes should be implemented in [#applyEvent(Object)].
    ///
    /// @param event the event to write to the WAL
    /// @param durability the durability of the write operation
    /// @throws net.pkhapps.vera.server.util.wal.WriteAheadLogException if the event could not be written to the WAL
    /// @see #appendToWal(Iterable, Durability)
    protected synchronized final void appendToWal(E event, Durability durability) {
        var walEvent = AggregateWalEvent.of(this, event);
        wal.append(walEvent, durability);
        applyEvent(event);
    }

    /// Creates a representation of the aggregate's *current state*. This is used by a [Repository] to create snapshots
    /// of the aggregate.
    ///
    /// This method must be fast. If it throws an exception, the entire snapshot is invalidated.
    ///
    /// @return the aggregate's current state
    protected abstract S toState();

    /// Applies the given WAL event, updating the aggregate's in-memory state. *This method must never write to the WAL*,
    /// i.e. call [#appendToWal(Object)] or [#appendToWal(Iterable)].
    ///
    /// This method must be fast. If it throws an exception, it aborts the WAL replay.
    ///
    /// @param event the event to replay.
    protected abstract void applyEvent(E event);
}
