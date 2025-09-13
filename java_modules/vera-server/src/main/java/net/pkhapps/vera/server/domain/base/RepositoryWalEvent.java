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

/// Base class for WAL events written by a repository.
abstract sealed class RepositoryWalEvent<T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> implements WalEvent {

    private final Class<T> aggregateType;

    RepositoryWalEvent(Class<T> aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Class<T> aggregateType() {
        return aggregateType;
    }

    /// WAL event written when an aggregate is inserted into the repository.
    static final class AggregateInserted<T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> extends RepositoryWalEvent<T, ID, S> {

        private final ID id;
        private final S state;

        public AggregateInserted(Class<T> aggregateType, T aggregate) {
            super(aggregateType);
            this.id = aggregate.id();
            this.state = aggregate.toState();
        }

        public AggregateInserted(Class<T> aggregateType, ID id, S state) {
            super(aggregateType);
            this.id = id;
            this.state = state;
        }

        @Override
        public String toString() {
            return "%s[aggregateType=%s, aggregateId=%s]".formatted(getClass().getSimpleName(), aggregateType().getName(), id);
        }

        /// Returns the ID of the inserted aggregate.
        ///
        /// @return the aggregate ID
        public ID aggregateId() {
            return id;
        }

        /// Returns the initial state of the inserted aggregate.
        ///
        /// @return the aggregate state
        public S aggregateState() {
            return state;
        }
    }

    /// WAL event written when an aggregate is removed from the repository.
    static final class AggregateRemoved<T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> extends RepositoryWalEvent<T, ID, S> {

        private final ID id;

        public AggregateRemoved(Class<T> aggregateType, ID aggregateId) {
            super(aggregateType);
            this.id = aggregateId;
        }

        @Override
        public String toString() {
            return "%s[aggregateType=%s, aggregateId=%s]".formatted(getClass().getSimpleName(), aggregateType().getName(), id);
        }

        /// Returns the ID of the removed aggregate.
        ///
        /// @return the aggregate ID
        public ID aggregateId() {
            return id;
        }
    }
}
