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

import net.pkhapps.vera.server.util.Registration;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/// Base class for repositories of aggregates.
///
/// The aggregates are stored in the WAL and loaded into memory at application startup. This makes lookups very fast,
/// but require the VM to have enough RAM at its disposal. Because of this, you can impose an upper limit on how
/// many aggregates a repository can contain at any given time.
///
/// Implementations of this class should define a public factory method for creating a new aggregat in a valid state.
/// This factory method should then call the [#insert(Aggregate)] method to add the aggregate to the repository and write
/// the necessary data to the WAL.
public abstract class Repository<T extends Aggregate<ID, S, E>, ID extends Identifier, S extends Record, E> implements AutoCloseable {

    private final Supplier<Integer> capacity;
    private final WriteAheadLog wal;
    private final Class<T> aggregateType;
    private final ConcurrentMap<ID, T> aggregates = new ConcurrentHashMap<>();
    private final Registration walRegistration;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /// Creates a new repository with "unimited" capacity (in practice {@value Integer#MAX_VALUE}).
    ///
    /// @param wal           the WAL to store aggregate in
    /// @param aggregateType the type of aggregates stored in this repository
    protected Repository(WriteAheadLog wal, Class<T> aggregateType) {
        this(wal, aggregateType, () -> Integer.MAX_VALUE);
    }

    /// Creates a new repository. The `capacity` is a function which makes it possible to fine-tune it during runtime.
    ///
    /// @param wal           the WAL to store aggregate in
    /// @param aggregateType the type of aggregates stored in this repository
    /// @param capacity      a function that returns the maximum number of aggregates you can store in the repository
    protected Repository(WriteAheadLog wal, Class<T> aggregateType, Supplier<Integer> capacity) {
        this.wal = wal;
        this.aggregateType = aggregateType;
        this.capacity = capacity;

        walRegistration = Registration.of(
                wal.registerEventConsumer(RepositoryWalEvent.class, this::supportsEvent, this::applyEvent),
                wal.registerEventConsumer(AggregateWalEvent.class, this::supportsEvent, this::applyEvent),
                wal.registerSnapshotConsumer(RepositoryWalSnapshot.class, this::supportsSnapshot, this::applySnapshot),
                wal.registerSnapshotProducer(this::createSnapshot)
        );
    }

    /// Unregisters the repository from the WAL and marks it as closed.
    @Override
    public synchronized final void close() {
        walRegistration.remove();
        closed.set(true);
    }

    /// Gets the WAL.
    ///
    /// @throws IllegalStateException if the repository is closed
    /// @see #close()
    protected final WriteAheadLog wal() {
        if (closed.get()) {
            throw new IllegalStateException("Repository is closed");
        }
        return wal;
    }

    /// Gets the aggregate with the given ID.
    ///
    /// @param id the ID of the aggregate to get
    /// @return the aggregate, or an empty `Optional` if not found
    /// @see #find(Predicate)
    /// @see #findSorted(Predicate, Comparator)
    public final Optional<T> get(ID id) {
        return Optional.ofNullable(aggregates.get(id));
    }

    /// Adds the specified aggregate to the repository, storing it in the WAL.
    /// Any additional processing, like maintaining indexes, should be done in the [#afterInsert(Aggregate)] method.
    ///
    /// @param aggregate the aggregate to add to the repository
    /// @throws DuplicateIdentifierException  if an aggregate with the same ID already exists in the repository
    /// @throws RepositoryAtCapacityException if the repository is at capacity and cannot accept more aggregates
    /// @see #remove(Identifier)
    protected synchronized final void insert(T aggregate) {
        if (aggregates.containsKey(aggregate.id())) {
            throw new DuplicateIdentifierException(aggregate.id());
        }
        int max = capacity.get();
        if (max == aggregates.size()) {
            throw new RepositoryAtCapacityException(max);
        }

        var event = new RepositoryWalEvent.AggregateInserted(aggregateType, aggregate);
        wal().append(event);
        doInsert(aggregate);
    }

    /// Creates a new aggregate with the given ID and state.
    ///
    /// This is used during snapshot and event replays. Because of this, this method must be fast. Any exceptions
    /// thrown will abort the replay.
    ///
    /// The implementation of this method *must not* call [#insert(Aggregate)] or change the state of the repository
    /// in any way.
    ///
    /// @param id    the ID of the aggregate
    /// @param state the state of the aggregate
    /// @return a new aggregate in a valid state
    protected abstract T createFromState(ID id, S state);

    /// Called after an aggregate has been added to the repository. The default implementation does nothing.
    /// Implementations can use this method to e.g. update additional in-memory indexes.
    ///
    /// @param aggregate the inserted aggregate
    protected void afterInsert(T aggregate) {
        // NOP
    }

    /// Called after an aggregate has been removed from the repository. The default implementation does nothing.
    /// Implementations can use this method to e.g. update additional in-memory indexes.
    ///
    /// @param id the ID of the removed aggregate
    protected void afterRemove(ID id) {
        //  NOP
    }

    /// Removes the aggregate with the given ID from the repository, updating the WAL.
    ///
    /// If the aggregate does not exist, nothing happens.
    ///
    /// @param id the ID of the aggregate to remove
    public synchronized final void remove(ID id) {
        if (!aggregates.containsKey(id)) {
            return;
        }
        var event = new RepositoryWalEvent.AggregateRemoved(aggregateType, id);
        wal().append(event);
        doRemove(id);
    }

    private void applyEvent(RepositoryWalEvent event) {
        switch (event) {
            case RepositoryWalEvent.AggregateInserted aggregateInserted -> {
                var aggregate = createFromState(aggregateInserted.aggregateId(), aggregateInserted.aggregateState());
                doInsert(aggregate);
            }
            case RepositoryWalEvent.AggregateRemoved aggregateRemoved -> {
                ID id = aggregateRemoved.aggregateId();
                doRemove(id);
            }
        }
    }

    private void doInsert(T aggregate) {
        if (aggregates.putIfAbsent(aggregate.id(), aggregate) != null) {
            // This should never happen unless the WAL is corrupt.
            throw new DuplicateIdentifierException(aggregate.id());
        }
        afterInsert(aggregate);
    }

    private void doRemove(ID id) {
        aggregates.remove(id);
        afterRemove(id);
    }

    private boolean supportsEvent(AggregateWalEvent event) {
        return event.aggregateType().equals(aggregateType);
    }

    private void applyEvent(AggregateWalEvent event) {
        ID id = event.id();
        T aggregate = aggregates.get(id);
        if (aggregate == null) {
            throw new NonExistentAggregateException(aggregateType, id);
        }
        event.forEach(aggregate::applyEvent);
    }

    private boolean supportsEvent(RepositoryWalEvent event) {
        return event.aggregateType().equals(aggregateType);
    }

    private void applySnapshot(RepositoryWalSnapshot snapshot) {
        aggregates.clear();
        snapshot.forEach((BiConsumer<ID, S>) (id, state) -> {
            var aggregate = createFromState(id, state);
            if (aggregates.putIfAbsent(id, aggregate) != null) {
                // This should never happen unless the WAL is corrupt.
                throw new DuplicateIdentifierException(id);
            }
        });
    }

    private boolean supportsSnapshot(RepositoryWalSnapshot snapshot) {
        return snapshot.aggregateType().equals(aggregateType);
    }

    private void createSnapshot(WriteAheadLog.SnapshotWriter<RepositoryWalSnapshot> snapshotWriter) {
        snapshotWriter.write(new RepositoryWalSnapshot(aggregateType, aggregates.values()));
    }

    /// Finds all aggregates that match the given filter.
    ///
    /// @param filter the filter to apply
    /// @return an unmodifiable collection of aggregates
    public final Collection<T> find(Predicate<T> filter) {
        return aggregates.values().stream().filter(filter).toList();
    }

    /// Finds all aggregates that match the given filter and sorts them using the given comparaator.
    ///
    /// @param filter     the filter to apply
    /// @param comparator the comparator to apply
    /// @return an unmodifiable list of aggregates
    public final List<T> findSorted(Predicate<T> filter, Comparator<T> comparator) {
        return aggregates.values().stream().filter(filter).sorted(comparator).toList();
    }
}
