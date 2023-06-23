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

package net.pkhapps.vera.common.domain.primitives.event;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * ID type for domain events. In VERA, a domain event is always published by an aggregate root. Because of this, the
 * aggregate root ID is included in the domain event ID. Within the aggregate root, domain events are distinguished by a
 * long integer that should be unique within the aggregate root.
 */
public final class DomainEventId implements Serializable {

    private final Serializable aggregateRootId;
    private final long eventId;

    /**
     * Initializing constructor for {@code DomainEventId}.
     *
     * @param aggregateRootId the ID of the aggregate root that creates this ID.
     * @param eventId         a long integer unique within the aggregate root that creates this ID.
     */
    public DomainEventId(@NotNull Serializable aggregateRootId, long eventId) {
        this.aggregateRootId = requireNonNull(aggregateRootId, "aggregateRootId must not be null");
        this.eventId = eventId;
    }

    /**
     * Returns the ID of the aggregate root that created this ID.
     */
    public @NotNull Serializable aggregateRootId() {
        return aggregateRootId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEventId that = (DomainEventId) o;
        return eventId == that.eventId && Objects.equals(aggregateRootId, that.aggregateRootId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateRootId, eventId);
    }
}
