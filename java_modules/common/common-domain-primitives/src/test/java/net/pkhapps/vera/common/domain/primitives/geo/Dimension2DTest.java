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

public class Dimension2DTest {

    @Test
    void dimensions_must_have_same_unit() {
        assertThatThrownBy(() -> Dimension2D.of(
                Dimension.of(1, CoordinateUnits.METER),
                Dimension.of(2, CoordinateUnits.DEGREE))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dimension2d_is_a_value_object() {
        var dd1 = Dimension2D.of(CoordinateUnits.METER, 1, 2);
        var dd2 = Dimension2D.of(CoordinateUnits.METER, 1, 2);

        assertThat(dd1).isNotSameAs(dd2);
        assertThat(dd1).isEqualTo(dd2);
        assertThat(dd1.hashCode()).isEqualTo(dd2.hashCode());
    }

    @Test
    void toString_has_been_overridden() {
        var dd1 = Dimension2D.of(CoordinateUnits.METER, 1, 2);
        assertThat(dd1.toString()).isEqualTo("Dimension2D{width=1.000000m, height=2.000000m}");
    }
}
