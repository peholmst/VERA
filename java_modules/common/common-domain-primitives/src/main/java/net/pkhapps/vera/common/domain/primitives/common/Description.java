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

package net.pkhapps.vera.common.domain.primitives.common;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Value object for any human readable description.
 */
public final class Description {


    /**
     * The maximum allowed length of a description ({@value } Unicode code units).
     */
    public static final int MAX_LENGTH = 3000;

    private final String description;

    private Description(@NotNull String description) {
        this.description = validate(description);
    }

    /**
     * Returns the description. Beware of injection attacks - this class does not perform any sanitation or validation
     * of the string.
     */
    public @NotNull String value() {
        return description;
    }

    private static @NotNull String validate(@NotNull String description) {
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (description.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Description is too long");
        }
        return description;
    }

    /**
     * Creates a {@code Description}.
     *
     * @param description the description string.
     * @return a new {@code Description}.
     * @throws IllegalArgumentException if the description string is blank or too long.
     */
    public static @NotNull Description of(@NotNull String description) {
        return new Description(description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Description that = (Description) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }
}
