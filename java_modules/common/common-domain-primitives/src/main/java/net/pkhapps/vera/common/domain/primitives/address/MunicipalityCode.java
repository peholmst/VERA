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
package net.pkhapps.vera.common.domain.primitives.address;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * All Finnish municipalities are identified by integer numbers between 1 and 999. Not all numbers are in use
 * and this class does not validate that the number does in fact correspond to a past or present municipality.
 * Numbers less than 100 are zero-padded, e.g. "003" or "099".
 */
public final class MunicipalityCode {

    /**
     * The municipality code "000" indicating an unknown municipality.
     */
    public static final MunicipalityCode UNKNOWN = new MunicipalityCode(0);

    private final String code;

    private MunicipalityCode(int code) {
        if (code < 0 || code > 999) {
            throw new IllegalArgumentException("Code must be between 000 and 999");
        }
        this.code = "%03d".formatted(code);
    }

    /**
     * Returns the municipality code as a three-digit, zero-padded string.
     */
    public @NotNull String value() {
        return code;
    }

    /**
     * Creates a {@code MunicipalityCode}.
     *
     * @param code the municipality code.
     * @return a new {@code MunicipalityCode}.
     * @throws IllegalArgumentException if the municipality code is invalid.
     */
    public static @NotNull MunicipalityCode of(int code) {
        return new MunicipalityCode(code);
    }

    /**
     * Creates a {@code MunicipalityCode} from a string.
     *
     * @param code the string, may be {@code null}.
     * @return a new {@code MunicipalityCode}, or {@code null} if the given {@code code} was {@code null}.
     * @throws IllegalArgumentException if the municipality code string is invalid.
     */
    @Contract("null -> null")
    public static MunicipalityCode fromString(@Nullable String code) {
        if (code == null) {
            return null;
        }
        var sanitized = code.strip();
        if (sanitized.length() == 0 || sanitized.length() > 3) {
            throw new IllegalArgumentException("Input string must contain between 1 and 3 characters");
        }
        if (!StringUtils.isNumeric(sanitized)) {
            throw new IllegalArgumentException("Input string must consist of numbers only");
        }
        return new MunicipalityCode(Integer.parseInt(sanitized));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MunicipalityCode that = (MunicipalityCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
