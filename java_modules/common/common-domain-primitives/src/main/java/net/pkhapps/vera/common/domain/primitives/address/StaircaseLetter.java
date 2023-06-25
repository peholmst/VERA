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

/**
 * Value object representing a staircase letter. According to <a href="https://termipankki.fi/tepa/fi/">TEPA</a>:
 * <ul>
 *     <li>fi: porraskirjain</li>
 *     <li>sv: trappbokstav</li>
 *     <li>en: staircase letter</li>
 * </ul>
 * <p><strong>porrashuoneen yksilöivä, osoitenumeroon liitettävä kirjain</strong></p>
 * <p>Porraskirjain ja huoneiston numero muodostavat yhdessä osoitteissa käytettävän huoneistotunnuksen.</p>
 * <p>Kirjoitusohjeen mukaan porraskirjain merkitään isolla kirjaimella ja erotetaan osoitenumerosta välilyönnillä.</p>
 * <p>Osoitteessa "Esimerkkikatu 1 A 2" porraskirjain on A.</p>
 * <p>Osoitenumeron perässä oleva kirjain ei aina ole kerrostalon porraskirjain, vaan se voi merkitä muutakin, kuten rakennusta tai rivitalon huoneistoa.</p>
 */
public final class StaircaseLetter {

    private final String letter;

    private StaircaseLetter(@NotNull String letter) {
        if (letter.length() != 1) {
            throw new IllegalArgumentException("Staircase letter must be single");
        }
        if (!StringUtils.isAlpha(letter)) {
            throw new IllegalArgumentException("Staircase letter must be alphabetic");
        }
        this.letter = letter.toUpperCase();
    }

    /**
     * Returns the staircase letter as a string.
     */
    public @NotNull String value() {
        return letter;
    }

    /**
     * Creates a {@code StaircaseLetter}.
     *
     * @param staircaseLetter the letter.
     * @return a new {@code StaircaseLetter}.
     * @throws IllegalArgumentException if the staircase letter is invalid.
     */
    public static @NotNull StaircaseLetter of(char staircaseLetter) {
        return new StaircaseLetter(String.valueOf(staircaseLetter));
    }

    /**
     * Creates a {@code StaircaseLetter} from a string.
     *
     * @param staircaseLetter the string, may be {@code null}.
     * @return a new {@code StaircaseLetter}, or {@code null} if the given {@code staircaseLetter} string was
     * {@code null}.
     * @throws IllegalArgumentException if the staircase letter string is invalid.
     */
    @Contract("null -> null")
    public static StaircaseLetter fromString(@Nullable String staircaseLetter) {
        if (staircaseLetter == null) {
            return null;
        }
        return new StaircaseLetter(staircaseLetter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaircaseLetter that = (StaircaseLetter) o;
        return Objects.equals(letter, that.letter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(letter);
    }
}
