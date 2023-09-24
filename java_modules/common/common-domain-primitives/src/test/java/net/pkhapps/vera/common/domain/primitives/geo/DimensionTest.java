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

package net.pkhapps.vera.common.domain.primitives.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DimensionTest {

    @Test
    void dimensions_are_never_negative() {
        assertThatThrownBy(() -> Dimension.of(-1, CoordinateUnits.METER)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dimensions_can_be_zero() {
        var d = Dimension.of(0, CoordinateUnits.METER);
        assertThat(d.unit()).isEqualTo(CoordinateUnits.METER);
        assertThat(d.value()).isEqualTo(0);
    }

    @Test
    void dimension_is_a_value_object() {
        var d1 = CoordinateUnits.DEGREE.dimension(123.45);
        var d2 = CoordinateUnits.DEGREE.dimension(123.45);

        assertThat(d1).isNotSameAs(d2);
        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    void toString_has_been_overridden() {
        var d1 = CoordinateUnits.DEGREE.dimension(123.45);
        assertThat(d1.toString()).isEqualTo("Dimension{123.450000°}");

        var d2 = CoordinateUnits.METER.dimension(678);
        assertThat(d2.toString()).isEqualTo("Dimension{678.000000m}");
    }
}
