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

package net.pkhapps.vera.common.domain.primitives.common;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DescriptionTest {

    @Test
    void description_must_not_be_blank() {
        assertThatThrownBy(() -> Description.of("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Description.of(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Description.of("\t")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void description_must_not_be_longer_than_3000_characters() {
        var maxLengthString = StringUtils.repeat('X', Description.MAX_LENGTH);
        assertThatThrownBy(() -> Description.of(maxLengthString + "y")).isInstanceOf(IllegalArgumentException.class);
        assertThat(Description.of(maxLengthString).value()).isEqualTo(maxLengthString);
    }

    @Test
    void description_is_a_value_object() {
        var input = "Hello World";
        var d1 = Description.of(input);
        var d2 = Description.of(input);

        assertThat(d1).isNotSameAs(d2);
        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }
}
