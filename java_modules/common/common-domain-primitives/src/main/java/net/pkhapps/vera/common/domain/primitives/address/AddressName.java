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

import net.pkhapps.vera.common.domain.primitives.i18n.Locales;
import net.pkhapps.vera.common.domain.primitives.i18n.MultilingualString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * Value object representing an address name. According to <a href="https://termipankki.fi/tepa/fi/">TEPA</a>:
 * <ul>
 *   <li>fi: osoitenimi; osoitteen nimiosa</li>
 *   <li>sv: adressnamn n</li>
 *   <li>en: address name</li>
 * </ul>
 * <p><strong>tien, kadun tai aukion nimi tai muu sovittu nimi</strong></p>
 * <p>Muu sovittu osoitenimi voi olla esimerkiksi saaren nimi.</p>
 */
public final class AddressName extends MultilingualString {
    /**
     * The maximum allowed length of an address name ({@value } Unicode code units).
     */
    public static final int MAX_LENGTH = 300;
    private static final String BLACKLISTED_CHARACTERS = "<>;\"\t\n\r";

    private AddressName(@NotNull Map<Locale, String> values, @NotNull Locale defaultLocale) {
        super(values, defaultLocale);
    }

    /**
     * Creates an {@code AddressName} in one language only (unspecified). The address name must not be blank, must not
     * be longer than {@value #MAX_LENGTH}, and must not contain certain
     * {@link #BLACKLISTED_CHARACTERS blacklisted characters}.
     *
     * @param addressName the address name.
     * @return a new {@code AddressName}.
     * @throws IllegalArgumentException if the address name is invalid.
     */
    public static @NotNull AddressName monolingual(@NotNull String addressName) {
        return new AddressName(Map.of(Locale.ROOT, validate(addressName)), Locale.ROOT);
    }

    /**
     * Creates an {@code AddressName} in Finnish and Swedish.  The address names must not be blank, must not be longer
     * than {@value #MAX_LENGTH}, and must not contain certain {@link #BLACKLISTED_CHARACTERS blacklisted characters}.
     *
     * @param finnish the Finnish address name.
     * @param swedish the Swedish address name.
     * @return a new {@code AddressName}.
     * @throws IllegalArgumentException if any of the address names are invalid.
     */
    public static @NotNull AddressName bilingual(@NotNull String finnish, @NotNull String swedish) {
        return new AddressName(Map.of(Locales.FINNISH, validate(finnish), Locales.SWEDISH, validate(swedish)), Locales.FINNISH);
    }

    private static @NotNull String validate(@NotNull String addressName) {
        if (addressName.isBlank()) {
            throw new IllegalArgumentException("Address name must not be blank");
        }
        if (addressName.length() > AddressName.MAX_LENGTH) {
            throw new IllegalArgumentException("Address name is too long");
        }
        if (StringUtils.containsAny(addressName, BLACKLISTED_CHARACTERS)) {
            throw new IllegalArgumentException("Address name contains illegal characters");
        }
        return addressName;
    }
}
