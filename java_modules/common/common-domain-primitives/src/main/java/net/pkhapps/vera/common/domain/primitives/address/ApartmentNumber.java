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

package net.pkhapps.vera.common.domain.primitives.address;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Value object representing an apartment number. According to <a href="https://termipankki.fi/tepa/fi/">TEPA</a>:
 * <ul>
 *     <li>fi: huoneiston numero; huoneistonumero</li>
 *     <li>sv: bostadsnummer n; bostadens nummer n; lägenhetens nummer n; lägenhetsnummer n /SE/</li>
 *     <li>en: apartment number</li>
 * </ul>
 * <p><strong>huoneiston yksilöivä numero ja mahdollinen jakokirjain</strong></p>
 * <p>Porraskirjain ja huoneiston numero muodostavat yhdessä osoitteissa käytettävän huoneistotunnuksen. Huoneiston numeron sijaan osoitteessa käytettävä huoneistotunnus voi rivi- ja paritaloissa olla iso kirjain taikka asunto- tai bostad-sanan lyhenne (suomeksi as., ruotsiksi bst.) ja asunnon numero.</p>
 * <p>Kirjoitusohjeen mukaan huoneiston numero erotetaan porraskirjaimesta välilyönnillä. Mahdollinen jakokirjain merkitään pienellä kirjaimella välittömästi numeron jälkeen.</p>
 * <p>Osoitteessa "Esimerkkikatu 1 A 2" huoneiston numero on 2.</p>
 */
public final class ApartmentNumber {

    public static final int MAX_LENGTH = 6;
    private static final Pattern REGEX = Pattern.compile("^[A-Za-zÅÄÖåäö]|([0-9]{1,5}[A-Za-zÅÄÖåäö]?)$");
    private final String value;

    private ApartmentNumber(@NotNull String value) {
        this.value = value;
    }

    /**
     * Returns the apartment number as a formatted string.
     */
    public @NotNull String value() {
        return value;
    }

    /**
     * Creates an {@code ApartmentNumber} consisting of a single letter.
     *
     * @param apartmentLetter the apartment letter.
     * @return a new {@code ApartmentNumber}.
     * @throws IllegalArgumentException if the apartment letter is invalid.
     */
    public static @NotNull ApartmentNumber of(@NotNull String apartmentLetter) {
        validateSingleApartmentLetter(apartmentLetter);
        return new ApartmentNumber(apartmentLetter.toUpperCase());
    }

    /**
     * Creates an {@code ApartmentNumber} consisting of a single number.
     *
     * @param apartmentNumber the apartment number.
     * @return a new {@code ApartmentNumber}.
     * @throws IllegalArgumentException if the apartment number is invalid.
     */
    public static @NotNull ApartmentNumber of(int apartmentNumber) {
        return of(apartmentNumber, null);
    }

    /**
     * Creates an {@code ApartmentNumber} consisting of a number and an optional additional letter.
     *
     * @param apartmentNumber  the apartment number.
     * @param additionalLetter the optional letter, or an empty string or {@code null}.
     * @return a new {@code ApartmentNumber}.
     * @throws IllegalArgumentException if the apartment number is invalid.
     */
    public static @NotNull ApartmentNumber of(int apartmentNumber, @Nullable String additionalLetter) {
        validateApartmentNumber(apartmentNumber);
        validateAdditionalLetter(additionalLetter);
        return new ApartmentNumber("%d%s".formatted(apartmentNumber, additionalLetter == null ? "" : additionalLetter.toLowerCase()));
    }

    /**
     * Creates an {@code ApartmentNumber} from the given string.
     *
     * @param apartmentNumber a string representation of the apartment number, may be {@code null}.
     * @return a new {@code ApartmentNumber}, or {@code null} if the given {@code apartmentNumber} string was
     * {@code null}.
     * @throws IllegalArgumentException if the apartment number string is invalid.
     */
    @Contract("null -> null")
    public static ApartmentNumber fromString(@Nullable String apartmentNumber) {
        if (apartmentNumber == null) {
            return null;
        }
        var sanitized = apartmentNumber.strip();
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Apartment number must not be empty");
        } else if (sanitized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Apartment number is too long");
        } else if (!StringUtils.isAlphanumeric(sanitized)) {
            throw new IllegalArgumentException("Apartment number contains invalid characters");
        } else if (!REGEX.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Apartment number has invalid format");
        }

        if (sanitized.length() == 1) {
            return new ApartmentNumber(sanitized.toUpperCase());
        } else {
            return new ApartmentNumber(sanitized.toLowerCase());
        }
    }

    private static void validateSingleApartmentLetter(@NotNull String apartmentLetter) {
        requireNonNull(apartmentLetter, "apartmentLetter must not be null");
        if (apartmentLetter.length() != 1) {
            throw new IllegalArgumentException("Apartment letter must be single");
        }
        if (!StringUtils.isAlpha(apartmentLetter)) {
            throw new IllegalArgumentException("Apartment letter must be alphabetic");
        }
    }

    private static void validateApartmentNumber(int apartmentNumber) {
        if (apartmentNumber < 0) {
            throw new IllegalArgumentException("Apartment number cannot be negative");
        }
        if (apartmentNumber > 99999) {
            throw new IllegalArgumentException("Apartment number is too big");
        }
    }

    private static void validateAdditionalLetter(@Nullable String additionalLetter) {
        if (additionalLetter == null || additionalLetter.isEmpty()) {
            return;
        }
        if (additionalLetter.length() != 1) {
            throw new IllegalArgumentException("Additional letter must be single");
        }
        if (!StringUtils.isAlpha(additionalLetter)) {
            throw new IllegalArgumentException("Additional letter must be alphabetic");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApartmentNumber that = (ApartmentNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
