/*
 * Copyright (c) 2023 Petter Holmström
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

package net.pkhapps.vera.common.domain.base;

import net.pkhapps.vera.common.domain.primitives.event.DomainEventId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for domain events.
 */
public abstract class BaseDomainEvent implements Identifiable<DomainEventId> {

    private final DomainEventId id;
    private final Instant timestamp;

    /**
     * Initializing constructor for {@link BaseDomainEvent}.
     *
     * @param id        the ID of the domain event.
     * @param timestamp the timestamp of the domain event.
     */
    protected BaseDomainEvent(@NotNull DomainEventId id, @NotNull Instant timestamp) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /**
     * Initializing constructor for {@link BaseDomainEvent} where the ID and timestamp are retrieved.
     *
     * @param aggregateRoot the aggregate root from which to generate the ID of the domain event.
     * @param context       the context from which to retrieve the timestamp of the domain event.
     */
    protected BaseDomainEvent(@NotNull BaseAggregateRoot<?> aggregateRoot, @NotNull DomainContext context) {
        this(aggregateRoot.generateDomainEventId(), context.clock().instant());
    }

    @Override
    public @NotNull DomainEventId id() {
        return id;
    }

    /**
     * Returns the instant on which this domain event was created.
     *
     * @return the domain event timestamp.
     */
    public @NotNull Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseDomainEvent that = (BaseDomainEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
