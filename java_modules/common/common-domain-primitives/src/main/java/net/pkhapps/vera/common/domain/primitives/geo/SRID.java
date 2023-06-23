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
package net.pkhapps.vera.common.domain.primitives.geo;

import java.util.Objects;

/**
 * Value object representing a Spatial Reference System Identifier.
 *
 * @author Petter Holmström
 */
public final class SRID {

    /**
     * The minimum SRID value supported by this value object ({@value}).
     */
    public static final int MIN = 1024;
    /**
     * The maximum SRID value supported by this value object ({@value}).
     */
    public static final int MAX = 32766;

    private final int value;

    /**
     * Creates a new {@code SRID}.
     *
     * @param srid the numeric SRID value.
     * @throws IllegalArgumentException if the numeric SRID value is invalid.
     */
    public SRID(int srid) {
        if (srid < MIN) {
            throw new IllegalArgumentException("SRID cannot be less than " + MIN);
        }
        if (srid > MAX) {
            throw new IllegalArgumentException("SRID cannot be greater than " + MAX);
        }
        this.value = srid;
    }

    /**
     * The numeric SRID value.
     */
    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return "SRID{%d}".formatted(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SRID srid = (SRID) o;
        return value == srid.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
