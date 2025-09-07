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
abstract sealed class RepositoryWalEvent implements WalEvent {

    private final Class<? extends Aggregate<?, ?, ?>> aggregateType;

    protected RepositoryWalEvent(Class<? extends Aggregate<?, ?, ?>> aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Class<? extends Aggregate<?, ?, ?>> aggregateType() {
        return aggregateType;
    }

    static final class AggregateInserted extends RepositoryWalEvent {

        private final Identifier id;
        private final Record state;

        public <T extends Aggregate<?, ?, ?>> AggregateInserted(Class<T> aggregateType, T aggregate) {
            super(aggregateType);
            this.id = aggregate.id();
            this.state = aggregate.toState();
        }

        @Override
        public String toString() {
            return "%s[aggregateType=%s, id=%s]".formatted(getClass().getSimpleName(), aggregateType().getSimpleName(), id);
        }

        @SuppressWarnings("unchecked")
        public <ID extends Identifier> ID aggregateId() {
            return (ID) id;
        }

        @SuppressWarnings("unchecked")
        public <S extends Record> S aggregateState() {
            return (S) state;
        }
    }

    static final class AggregateRemoved extends RepositoryWalEvent {

        private final Identifier id;

        public <T extends Aggregate<ID, ?, ?>, ID extends Identifier> AggregateRemoved(Class<T> aggregateType, ID aggregateId) {
            super(aggregateType);
            this.id = aggregateId;
        }

        @Override
        public String toString() {
            return "%s[aggregateType=%s, id=%s]".formatted(getClass().getSimpleName(), aggregateType().getSimpleName(), id);
        }

        @SuppressWarnings("unchecked")
        public <ID extends Identifier> ID aggregateId() {
            return (ID) id;
        }
    }
}
