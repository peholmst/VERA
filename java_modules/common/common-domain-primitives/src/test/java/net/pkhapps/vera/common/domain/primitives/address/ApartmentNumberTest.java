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

public class ApartmentNumberTest {

    @Test
    void apartment_numbers_can_consist_of_a_single_uppercase_letter() {
        assertThat(ApartmentNumber.of("A").value()).isEqualTo("A");
    }

    @Test
    void single_apartment_letters_are_automatically_converted_to_uppercase() {
        assertThat(ApartmentNumber.of("a").value()).isEqualTo("A");
    }

    @Test
    void single_apartment_letters_must_be_alphabetic_and_single() {
        assertThatThrownBy(() -> ApartmentNumber.of("1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.of("$")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.of("AB")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apartment_numbers_can_consist_of_a_single_number() {
        assertThat(ApartmentNumber.of(1).value()).isEqualTo("1");
        assertThat(ApartmentNumber.of(10).value()).isEqualTo("10");
        assertThat(ApartmentNumber.of(10, null).value()).isEqualTo("10");
        assertThat(ApartmentNumber.of(10, "").value()).isEqualTo("10");
    }

    @Test
    void apartment_numbers_cannot_be_negative() {
        assertThatThrownBy(() -> ApartmentNumber.of(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apartment_numbers_can_consist_of_a_number_and_a_lowercase_letter() {
        assertThat(ApartmentNumber.of(1, "a").value()).isEqualTo("1a");
        assertThat(ApartmentNumber.of(10, "b").value()).isEqualTo("10b");
    }

    @Test
    void apartment_numbers_can_contain_at_most_five_digits() {
        assertThatThrownBy(() -> ApartmentNumber.of(100000)).isInstanceOf(IllegalArgumentException.class);
        assertThat(ApartmentNumber.of(99999).value()).isEqualTo("99999");
    }

    @Test
    void apartment_numbers_with_letters_are_automatically_converted_to_lowercase() {
        assertThat(ApartmentNumber.of(10, "A").value()).isEqualTo("10a");
    }

    @Test
    void additional_letters_must_be_alphabetic_and_single() {
        assertThatThrownBy(() -> ApartmentNumber.of(10, "ab")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.of(10, "1")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apartment_numbers_can_be_parsed_from_a_string() {
        assertThat(ApartmentNumber.fromString("A").value()).isEqualTo("A");
        assertThat(ApartmentNumber.fromString("1").value()).isEqualTo("1");
        assertThat(ApartmentNumber.fromString("10").value()).isEqualTo("10");
        assertThat(ApartmentNumber.fromString("10a").value()).isEqualTo("10a");
        assertThat(ApartmentNumber.fromString("12345a").value()).isEqualTo("12345a");
    }

    @Test
    void fromString_accepts_null() {
        assertThat(ApartmentNumber.fromString(null)).isNull();
    }

    @Test
    void fromString_removes_leading_and_trailing_whitespace() {
        assertThat(ApartmentNumber.fromString(" 10a ").value()).isEqualTo("10a");
    }

    @Test
    void fromString_converts_to_the_correct_case() {
        assertThat(ApartmentNumber.fromString("a").value()).isEqualTo("A");
        assertThat(ApartmentNumber.fromString("10A").value()).isEqualTo("10a");
    }

    @Test
    void fromString_still_validates_the_input() {
        assertThatThrownBy(() -> ApartmentNumber.fromString("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("-10")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("10 a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("10 12")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("<script></script>")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("\"; DELETE FROM foo;")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("123456")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApartmentNumber.fromString("123456a")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apartment_number_is_a_value_object() {
        var an1 = ApartmentNumber.of(123, "A");
        var an2 = ApartmentNumber.of(123, "A");

        assertThat(an1).isNotSameAs(an2);
        assertThat(an1).isEqualTo(an2);
        assertThat(an1.hashCode()).isEqualTo(an2.hashCode());
    }
}
