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

/**
 * Value object representing an address number. According to <a href="https://termipankki.fi/tepa/fi/">TEPA</a>:
 * <ul>
 *     <li>fi: osoitenumero; osoitteen numero-osa</li>
 *     <li>sv: adressnummer n</li>
 *     <li>en: address number</li>
 * </ul>
 * <p><strong>osoitenimeen liitettävä numero ja mahdollinen jakokirjain</strong></p>
 * <p>Osoitenumeroon lisätään jakokirjain silloin, kun ei ole käytettävissä sopivaa osoitenumeroa. Kirjoitusohjeen mukaan jakokirjain on pieni kirjain, ja se lisätään osoitenumeron perään ilman välilyöntiä.</p>
 * <p>Osoitteessa "Esimerkkikatu 1a A 2" osoitenumero on 1a, ja osoitteessa ”Esimerkkikatu 10–12 B 3” osoitenumero on 10–12.</p>
 */
public final class AddressNumber {

    /**
     * The maximum allowed length of an address number ({@value } Unicode code units).
     */
    public static final int MAX_LENGTH = 13;
    private static final Pattern REGEX = Pattern.compile("^\\d{1,5}\\D?(–\\d{1,5}\\D?)?$");
    private final String value;

    private AddressNumber(@NotNull String value) {
        this.value = value;
    }

    /**
     * Returns the address number as a formatted string.
     */
    public @NotNull String value() {
        return value;
    }

    /**
     * Creates an {@code AddressNu mber} consisting of a single number.
     *
     * @param addressNumber the address number.
     * @return a new {@code AddressNumber}.
     * @throws IllegalArgumentException if the address number is invalid.
     */
    public static @NotNull AddressNumber of(int addressNumber) {
        return AddressNumber.of(addressNumber, null);
    }

    /**
     * Creates an {@code AddressNumber} consisting of a single number and an optional letter.
     *
     * @param addressNumber the address number.
     * @param addressLetter the optional letter, or an empty string or {@code null} if not needed.
     * @return a new {@code AddressNumber}.
     * @throws IllegalArgumentException if the address number is invalid.
     */
    public static @NotNull AddressNumber of(int addressNumber, @Nullable String addressLetter) {
        validateAddressNumber(addressNumber);
        validateAddressLetter(addressLetter);
        return new AddressNumber("%d%s".formatted(addressNumber, addressLetter == null ? "" : addressLetter.toLowerCase()));
    }

    /**
     * Creates an {@code AddressNumber} consisting of a number pair.
     *
     * @param firstAddressNumber  the first address number.
     * @param secondAddressNumber the second address number.
     * @return a new {@code AddressNumber}.
     * @throws IllegalArgumentException if any of the address numbers is invalid.
     */
    public static @NotNull AddressNumber of(int firstAddressNumber, int secondAddressNumber) {
        return AddressNumber.of(firstAddressNumber, null, secondAddressNumber, null);
    }

    /**
     * Creates an {@code AddressNumber} consisting of a number pair, with optional letters.
     *
     * @param firstAddressNumber  the first address number.
     * @param firstAddressLetter  the first optional letter, or an empty string or {@code null} if not needed.
     * @param secondAddressNumber the second address number.
     * @param secondAddressLetter the second optional letter, or an empty string or {@code null} if not needed.
     * @return a new {@code AddressNumber}.
     * @throws IllegalArgumentException if any of the address numbers is invalid.
     */
    public static @NotNull AddressNumber of(int firstAddressNumber, @Nullable String firstAddressLetter,
                                            int secondAddressNumber, @Nullable String secondAddressLetter) {
        validateAddressNumber(firstAddressNumber);
        validateAddressLetter(firstAddressLetter);
        validateAddressNumber(secondAddressNumber);
        validateAddressLetter(secondAddressLetter);
        return new AddressNumber("%d%s–%d%s".formatted(
                firstAddressNumber,
                firstAddressLetter == null ? "" : firstAddressLetter.toLowerCase(),
                secondAddressNumber,
                secondAddressLetter == null ? "" : secondAddressLetter.toLowerCase()));
    }

    /**
     * Creates an {@link AddressNumber} from the given string.
     *
     * @param addressNumber a string representation of the address number, may be {@code null}.
     * @return a new {@code AddressNumber}, or {@code null} if the given {@code addressNumber} was {@code null}.
     * @throws IllegalArgumentException if the address number string is invalid.
     */
    @Contract("null -> null")
    public static AddressNumber fromString(@Nullable String addressNumber) {
        if (addressNumber == null) {
            return null;
        }
        var sanitized = addressNumber.strip().toLowerCase().replace('-', '–');
        if (sanitized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Address number is too long");
        }
        var splitterPos = sanitized.indexOf('–');
        if (splitterPos == -1) {
            if (!StringUtils.isAlphanumeric(sanitized)) {
                throw new IllegalArgumentException("Invalid address number characters");
            }
        } else {
            if (!StringUtils.isAlphanumeric(sanitized.substring(0, splitterPos)) || !StringUtils.isAlphanumeric(sanitized.substring(splitterPos + 1))) {
                throw new IllegalArgumentException("Invalid address number characters");
            }
        }
        if (!REGEX.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Invalid address number format");
        }
        return new AddressNumber(sanitized);
    }

    private static void validateAddressNumber(int addressNumber) {
        if (addressNumber < 0) {
            throw new IllegalArgumentException("Address number must not be negative");
        }
        if (addressNumber > 99999) {
            throw new IllegalArgumentException("Address number is too big");
        }
    }

    private static void validateAddressLetter(@Nullable String addressLetter) {
        if (addressLetter == null || addressLetter.isEmpty()) {
            return;
        }
        if (addressLetter.length() > 1) {
            throw new IllegalArgumentException("Address letter must be single");
        }
        if (!StringUtils.isAlpha(addressLetter)) {
            throw new IllegalArgumentException("Invalid address letter");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressNumber that = (AddressNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
