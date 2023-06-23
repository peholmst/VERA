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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.pkhapps.vera.common.domain.primitives.incident;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Value object for incident type codes. These codes consist of 3-4 alphanumeric characters.
 */
public final class IncidentTypeCode implements Serializable {

    /**
     * Constant representing an unknown incident type code ("Z000").
     */
    public static final IncidentTypeCode UNKNOWN = new IncidentTypeCode("Z000");

    private final String code;

    private IncidentTypeCode(@NotNull String code) {
        this.code = validate(requireNonNull(code, "code must not be null"));
    }

    /**
     * Creates a new {@code IncidentTypeCode} from a string.
     *
     * @param code the type code as a string, may be {@code null}.
     * @return an {@code IncidentTypeCode} or {@code null} if the given {@code code} was {@code null}.
     * @throws IllegalArgumentException if the type code was invalid.
     */
    @Contract("null -> null")
    public static IncidentTypeCode fromString(String code) {
        return code == null ? null : new IncidentTypeCode(code);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentTypeCode that = (IncidentTypeCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    private static @NotNull String validate(@NotNull String code) {
        if (code.length() < 3) {
            throw new IllegalArgumentException("Code is too short");
        }
        if (code.length() > 4) {
            throw new IllegalArgumentException("Code is too long");
        }
        if (!StringUtils.isAlphanumeric(code)) {
            throw new IllegalArgumentException("Code contains invalid characters");
        }
        return code;
    }
}
