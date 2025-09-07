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
final class AggregateWalEvent implements WalEvent {

    private final Class<? extends Aggregate<?, ?, ?>> aggregateType;
    private final Identifier id;
    private final List<?> entries;

    public <T extends Aggregate<ID, ?, E>, ID extends Identifier, E> AggregateWalEvent(T aggregate, Iterable<E> entries) {
        this(aggregate.getClass(), aggregate.id(), entries);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AggregateWalEvent(Class<? extends Aggregate> aggregateType, Identifier id, Iterable<?> entries) {
        this.aggregateType = (Class<? extends Aggregate<?, ?, ?>>) aggregateType;
        this.id = id;
        this.entries = StreamSupport.stream(entries.spliterator(), false).map(Record.class::cast).toList();
    }

    @Override
    public String toString() {
        return "%s[aggregateType=%s, id=%s, entryCount=%d]".formatted(AggregateWalEvent.class.getSimpleName(),
                aggregateType.getSimpleName(), id, entries.size());
    }

    @SuppressWarnings("unchecked")
    public <S> void forEach(Consumer<S> consumer) {
        entries.forEach(entry -> consumer.accept((S) entry));
    }

    public Class<? extends Aggregate<?, ?, ?>> aggregateType() {
        return aggregateType;
    }

    @SuppressWarnings("unchecked")
    public <ID extends Identifier> ID id() {
        return (ID) id;
    }
}
