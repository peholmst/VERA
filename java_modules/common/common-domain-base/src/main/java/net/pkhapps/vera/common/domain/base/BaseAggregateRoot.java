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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Base class for aggregate roots.
 *
 * @param <ID> the type of the aggregate root ID.
 */
public abstract class BaseAggregateRoot<ID extends Serializable> extends BaseEntity<ID> implements Persistable {

    private final boolean persistent;
    private final long optLockVersion;
    private final AtomicLong nextEventId;
    private final List<BaseDomainEvent> domainEvents;

    /**
     * Initializing constructor for new {@code BaseAggregateRoot}.
     *
     * @param id the ID of the entity.
     */
    protected BaseAggregateRoot(@NotNull ID id) {
        super(id);
        this.persistent = false;
        this.optLockVersion = 0;
        this.nextEventId = new AtomicLong(0);
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Deserializing constructor for {@code BaseAggregateRoot}. Aggregate roots created by this constructor will have
     * their {@link #isPersistent()} flags set to true.
     *
     * @param source the reader to read data from.
     * @see #writeTo(AggregateRootWriter)
     */
    protected BaseAggregateRoot(@NotNull AggregateRootReader<ID> source) {
        super(source.id());
        this.persistent = true;
        this.optLockVersion = source.optLockVersion();
        this.nextEventId = new AtomicLong(source.nextEventId());
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Serializes the aggregate root by writing its data to the given sink. Subclasses that want to support
     * serialization should re-implement this method and make it public.
     *
     * @param sink the writer to write data to.
     * @see #BaseAggregateRoot(AggregateRootReader)
     */
    protected void writeTo(@NotNull AggregateRootWriter<ID> sink) {
        sink.setId(id());
        sink.setOptLockVersion(optLockVersion);
        sink.setNextEventId(nextEventId.get());
    }

    @Override
    public final boolean isPersistent() {
        return persistent;
    }

    /**
     * Returns the optimistic locking version of the aggregate root. This version is incremented each time this
     * aggregate root is saved in a {@link Repository}.
     *
     * @return the optimistic locking version.
     */
    public final long optLockVersion() {
        return optLockVersion;
    }

    /**
     * Registers a new domain event that will be published the next time this aggregate root is saved in a
     * {@link Repository}.
     *
     * @param domainEvent the domain event to register.
     * @see #generateDomainEventId()
     */
    protected final void registerDomainEvent(@NotNull BaseDomainEvent domainEvent) {
        domainEvents.add(requireNonNull(domainEvent, "domainEvent must not be null"));
    }

    /**
     * Generates a new, unique {@link DomainEventId} scoped to this aggregate root. If this method is called on two
     * instances of the same aggregate root, they may return the same values which is why
     * {@link #optLockVersion() optimistic locking} must be used when saving the aggregate root.
     *
     * @return a new {@link DomainEventId}.
     */
    protected final DomainEventId generateDomainEventId() {
        return DomainEventId.of(id(), nextEventId.getAndIncrement());
    }

    /**
     * Returns the {@link BaseDomainEvent}s that should be published the next time this aggregate root is saved by a
     * {@link Repository}. This method is intended to be used by {@link Repository} only.
     */
    final @NotNull Stream<BaseDomainEvent> domainEvents() {
        return domainEvents.stream();
    }
}
