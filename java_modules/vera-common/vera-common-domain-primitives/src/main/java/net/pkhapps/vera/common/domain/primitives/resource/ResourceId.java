/*
 * Copyright (c) 2024 Petter Holmström
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

package net.pkhapps.vera.common.domain.primitives.resource;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import net.pkhapps.vera.common.utils.StringValidationUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class ResourceId {

    private final String id;

    private ResourceId(@NotNull String id) {
        this.id = requireNonNull(id);
    }

    @Override
    @JsonValue
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceId that = (ResourceId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static @NotNull ResourceId randomId() {
        return new ResourceId(NanoIdUtils.randomNanoId());
    }

    @JsonCreator
    public static @NotNull ResourceId fromString(@NotNull String id) {
        if (id.length() != NanoIdUtils.DEFAULT_SIZE) {
            throw new IllegalArgumentException("id has incorrect length");
        }
        if (!StringValidationUtils.hasOnlyLegalChars(id, NanoIdUtils.DEFAULT_ALPHABET)) {
            throw new IllegalArgumentException("id contains illegal characters");
        }
        return new ResourceId(id);
    }
}
