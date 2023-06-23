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

import java.util.Objects;

/**
 * Base class for entities. In VERA, entities must have a known ID upon creation.
 *
 * @param <ID> the type of the entity ID.
 */
public abstract class BaseEntity<ID> implements Identifiable<ID> {

    private final ID id;

    /**
     * Initializing constructor for {@code BaseEntity}.
     *
     * @param id the ID of the entity.
     */
    protected BaseEntity(@NotNull ID id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    @Override
    public final @NotNull ID id() {
        return id;
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
