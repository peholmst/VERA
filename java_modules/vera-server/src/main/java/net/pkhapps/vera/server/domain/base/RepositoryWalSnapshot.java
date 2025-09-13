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

import net.pkhapps.vera.server.util.wal.WalSnapshot;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/// WAL snapshot for repositories.
final class RepositoryWalSnapshot<T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> implements WalSnapshot {

    private final Class<T> aggregateType;
    private final Map<ID, S> aggregateStates;

    /// Constructor used by the Serde and by factory methods. Clients should not call this method directly.
    ///
    /// **Note:** This constructor does *not* copy the `aggregateStates` map for performance reasons. Callers must make
    /// sure the map is effectively immutable.
    ///
    /// @param aggregateType   the type of the aggregate whose states are stored in this snapshot
    /// @param aggregateStates a map of aggregate ID and states to store in the snapshot
    RepositoryWalSnapshot(Class<T> aggregateType, Map<ID, S> aggregateStates) {
        this.aggregateType = aggregateType;
        this.aggregateStates = aggregateStates;
    }

    /// Creates a new `RepositoryWalSnapshot`.
    ///
    /// @param aggregateType the type of aggregates to store in this snapshot
    /// @param aggregates    the aggregates to store in this snapshot
    public static <T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> RepositoryWalSnapshot<T, ID, S> of(Class<T> aggregateType, Iterable<T> aggregates) {
        return new RepositoryWalSnapshot<>(
                aggregateType,
                StreamSupport.stream(aggregates.spliterator(), false).collect(Collectors.toMap(
                        aggregate -> aggregate.id(),
                        aggregate -> aggregate.toState())
                ));
    }

    @Override
    public String toString() {
        return "%s[aggregateType=%s, size=%d]".formatted(getClass().getSimpleName(), aggregateType().getName(), aggregateStates.size());
    }

    /// Performs the given `action` for each aggregate in this snapshot.
    ///
    /// The snapshot does not contain [Aggregate] objects, but pairs of the aggregate ID and state.
    ///
    /// @param action the action to perform for each aggregate in this snapshot
    public void forEach(BiConsumer<ID, S> action) {
        aggregateStates.forEach(action);
    }

    /// Returns the number of aggregates in this snapshot.
    ///
    /// @return the number of aggregates
    public int size() {
        return aggregateStates.size();
    }

    /// Returns the type of the aggregates in this snapshot
    ///
    /// @return the aggregate type
    public Class<T> aggregateType() {
        return aggregateType;
    }
}
