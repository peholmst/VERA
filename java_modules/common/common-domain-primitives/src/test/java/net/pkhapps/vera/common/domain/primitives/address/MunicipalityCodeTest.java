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

public class MunicipalityCodeTest {

    @Test
    void municipality_code_must_be_between_000_and_999() {
        assertThatThrownBy(() -> MunicipalityCode.of(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.of(1000)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeros_are_prepended_to_numbers_below_100() {
        assertThat(MunicipalityCode.of(0).value()).isEqualTo("000");
        assertThat(MunicipalityCode.of(9).value()).isEqualTo("009");
        assertThat(MunicipalityCode.of(10).value()).isEqualTo("010");
        assertThat(MunicipalityCode.of(99).value()).isEqualTo("099");
        assertThat(MunicipalityCode.of(100).value()).isEqualTo("100");
    }

    @Test
    void codes_can_be_passed_as_strings() {
        assertThat(MunicipalityCode.fromString("0").value()).isEqualTo("000");
        assertThat(MunicipalityCode.fromString("1").value()).isEqualTo("001");
        assertThat(MunicipalityCode.fromString("02").value()).isEqualTo("002");
        assertThat(MunicipalityCode.fromString("003").value()).isEqualTo("003");
        assertThat(MunicipalityCode.fromString("999").value()).isEqualTo("999");
        assertThat(MunicipalityCode.fromString(" 888 ").value()).isEqualTo("888");
    }

    @Test
    void fromString_performs_input_validation() {
        assertThatThrownBy(() -> MunicipalityCode.fromString("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("-1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("+1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("0xFF")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("0b10")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("1000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("hello")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MunicipalityCode.fromString("1.0")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromString_accepts_null() {
        assertThat(MunicipalityCode.fromString(null)).isNull();
    }

    @Test
    void municipality_code_is_a_value_object() {
        var mc1 = MunicipalityCode.of(445);
        var mc2 = MunicipalityCode.of(445);

        assertThat(mc1).isNotSameAs(mc2);
        assertThat(mc1).isEqualTo(mc2);
        assertThat(mc1.hashCode()).isEqualTo(mc2.hashCode());
    }
}
