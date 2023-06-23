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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Interface used by the {@link BaseAggregateRoot#BaseAggregateRoot(AggregateRootReader)} constructor to instantiate an
 * aggregate root object from a data source. This is the preferred way of deserializing aggregate roots.
 *
 * @param <ID> the type of the ID.
 * @see AggregateRootWriter
 * @see BaseAggregateRoot#writeTo(AggregateRootWriter)
 */
public interface AggregateRootReader<ID extends Serializable> {

    /**
     * The value of {@link BaseAggregateRoot#id()}.
     */
    @NotNull ID id();

    /**
     * The value of {@link BaseAggregateRoot#optLockVersion()}.
     */
    long optLockVersion();

    /**
     * The next event ID that will be returned by {@link BaseAggregateRoot#generateDomainEventId()}.
     */
    long nextEventId();
}
