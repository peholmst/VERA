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

public class AddressNumberTest {

    @Test
    void address_numbers_can_consist_of_a_number_only() {
        var an = AddressNumber.of(123);
        assertThat(an.value()).isEqualTo("123");
    }

    @Test
    void address_numbers_can_have_an_extra_letter() {
        var an = AddressNumber.of(123, "a");
        assertThat(an.value()).isEqualTo("123a");
    }

    @Test
    void address_numbers_can_consist_of_two_numbers() {
        var an = AddressNumber.of(10, 12);
        assertThat(an.value()).isEqualTo("10–12");
    }

    @Test
    void double_address_numbers_can_also_have_extra_letters() {
        var an = AddressNumber.of(10, "a", 12, "b");
        assertThat(an.value()).isEqualTo("10a–12b");
    }

    @Test
    void address_letters_are_converted_to_lowercase() {
        assertThat(AddressNumber.of(123, "A").value()).isEqualTo("123a");
        assertThat(AddressNumber.of(10, "A", 12, "B").value()).isEqualTo("10a–12b");
    }

    @Test
    void address_letters_can_be_null_or_empty() {
        assertThat(AddressNumber.of(123, null).value()).isEqualTo("123");
        assertThat(AddressNumber.of(123, "").value()).isEqualTo("123");
        assertThat(AddressNumber.of(10, null, 12, null).value()).isEqualTo("10–12");
        assertThat(AddressNumber.of(10, "", 12, "").value()).isEqualTo("10–12");
    }

    @Test
    void address_numbers_cannot_be_negative() {
        assertThatThrownBy(() -> AddressNumber.of(-12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(-10, 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(10, -12)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void address_number_can_contain_at_most_five_digits() {
        assertThatThrownBy(() -> AddressNumber.of(100000)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(100000, 99999)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(99999, 100000)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void address_letters_must_be_alphabetic_and_single() {
        assertThatThrownBy(() -> AddressNumber.of(10, "1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(10, "ab")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(10, "12", 12, "a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(10, "a", 12, "10")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(10, "ab", 12, "a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.of(10, "a", 12, "ab")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void address_numbers_can_be_parsed_from_a_string() {
        assertThat(AddressNumber.fromString("10").value()).isEqualTo("10");
        assertThat(AddressNumber.fromString("10a").value()).isEqualTo("10a");
        assertThat(AddressNumber.fromString("10–12").value()).isEqualTo("10–12");
        assertThat(AddressNumber.fromString("10a–12").value()).isEqualTo("10a–12");
        assertThat(AddressNumber.fromString("10–12b").value()).isEqualTo("10–12b");
        assertThat(AddressNumber.fromString("10a–12b").value()).isEqualTo("10a–12b");
    }

    @Test
    void fromString_accepts_null() {
        assertThat(AddressNumber.fromString(null)).isNull();
    }

    @Test
    void fromString_converts_hyphens_to_dashes() {
        assertThat(AddressNumber.fromString("10-12").value()).isEqualTo("10–12");
    }

    @Test
    void fromString_removes_leading_and_trailing_whitespace() {
        assertThat(AddressNumber.fromString(" 10–12 ").value()).isEqualTo("10–12");
    }

    @Test
    void fromString_converts_uppercase_to_lowercase() {
        assertThat(AddressNumber.fromString("10A–12B").value()).isEqualTo("10a–12b");
    }

    @Test
    void fromString_still_validates_the_input() {
        assertThatThrownBy(() -> AddressNumber.fromString("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("-12")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("12ABC")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("10-12-13")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("10–")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("<script></script>")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("\"; DELETE FROM foo;")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("123456a-12345a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("123456a-1a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("1a-123456a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("123456")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressNumber.fromString("123456a")).isInstanceOf(IllegalArgumentException.class);
    }
}
