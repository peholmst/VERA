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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StaircaseLetterTest {

    @Test
    void staircase_letters_must_be_letters() {
        assertThat(StaircaseLetter.of('A').value()).isEqualTo("A");
        assertThat(StaircaseLetter.of('Z').value()).isEqualTo("Z");
        assertThat(StaircaseLetter.of('Å').value()).isEqualTo("Å");
        assertThat(StaircaseLetter.of('Ä').value()).isEqualTo("Ä");
        assertThat(StaircaseLetter.of('Ö').value()).isEqualTo("Ö");
        assertThatThrownBy(() -> StaircaseLetter.fromString("1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StaircaseLetter.fromString("#")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StaircaseLetter.fromString("$")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void staircase_letters_must_consist_of_a_single_letter() {
        assertThat(StaircaseLetter.fromString("A").value()).isEqualTo("A");
        assertThatThrownBy(() -> StaircaseLetter.fromString("AB")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StaircaseLetter.fromString("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void staircase_letters_are_automatically_converted_to_uppercase() {
        assertThat(StaircaseLetter.fromString("a").value()).isEqualTo("A");
        assertThat(StaircaseLetter.of('a').value()).isEqualTo("A");
    }

    @Test
    void fromString_accepts_null() {
        assertThat(StaircaseLetter.fromString(null)).isNull();
    }
}
