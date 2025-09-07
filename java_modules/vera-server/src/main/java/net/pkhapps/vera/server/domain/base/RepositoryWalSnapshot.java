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
final class RepositoryWalSnapshot implements WalSnapshot {

    private final Class<? extends Aggregate<?, ?, ?>> aggregateType;
    private final Map<Identifier, Record> aggregateStates;

    public <T extends Aggregate<?, ?, ?>> RepositoryWalSnapshot(Class<T> aggregateType, Iterable<T> aggregates) {
        this.aggregateType = aggregateType;
        this.aggregateStates = StreamSupport.stream(aggregates.spliterator(), false)
                .collect(Collectors.toMap(
                        aggregate -> aggregate.id(),
                        aggregate -> aggregate.toState())
                );
    }

    @Override
    public String toString() {
        return "%s[aggregateType=%s, stateCount=%d]".formatted(getClass().getSimpleName(), aggregateType().getSimpleName(), aggregateStates.size());
    }

    @SuppressWarnings("unchecked")
    public <ID extends Identifier, S extends Record> void forEach(BiConsumer<ID, S> consumer) {
        aggregateStates.forEach((id, state) -> consumer.accept((ID) id, (S) state));
    }

    public Class<? extends Aggregate<?, ?, ?>> aggregateType() {
        return aggregateType;
    }
}
