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

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility methods for testing {@link BaseAggregateRoot}s.
 */
public final class AggregateRootTestUtils {

    private AggregateRootTestUtils() {
    }

    /**
     * Asserts that at least one domain event of the given class, matching the given predicate, has been registered by
     * the given aggregate root.
     *
     * @param aggregateRoot    the aggregate root to check.
     * @param domainEventClass the class of the domain event.
     * @param predicate        the predicate that the domain event should match.
     * @param <T>              the type of the domain event.
     * @throws AssertionError if no domain events meeting the given requirements were found.
     */
    public static <T extends BaseDomainEvent> void assertDomainEventRegistered(@NotNull BaseAggregateRoot<?> aggregateRoot,
                                                                               @NotNull Class<T> domainEventClass,
                                                                               @NotNull Predicate<T> predicate) {
        assertThat(aggregateRoot.domainEvents()).anyMatch(e -> domainEventClass.isInstance(e) && predicate.test(domainEventClass.cast(e)));
    }

    /**
     * Asserts that no domain events of the given class have been registered by the given aggregate root.
     *
     * @param aggregateRoot    the aggregate root to check.
     * @param domainEventClass the class of the domain event.
     * @param <T>              the type of the domain event.
     * @throws AssertionError if at least one domain event of the given class had been registered by the given aggregate
     *                        root.
     */
    public static <T extends BaseDomainEvent> void assertNoDomainEventRegistered(@NotNull BaseAggregateRoot<?> aggregateRoot,
                                                                                 @NotNull Class<T> domainEventClass) {
        assertThat(aggregateRoot.domainEvents()).noneMatch(domainEventClass::isInstance);
    }
}
