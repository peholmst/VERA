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

public class PointTest {

    @Test
    void distance_is_calculated_on_the_cartesian_plane() {
        var p1 = CoordinateUnits.METER.point(0, 0);
        var p2 = CoordinateUnits.METER.point(3, 4);

        var d12 = p1.distanceTo(p2);
        assertThat(d12.unit()).isEqualTo(CoordinateUnits.METER);
        assertThat(d12.value()).isEqualTo(5.0);

        var d21 = p2.distanceTo(p1);
        assertThat(d21.unit()).isEqualTo(CoordinateUnits.METER);
        assertThat(d21.value()).isEqualTo(5.0);
    }

    @Test
    void distance_can_only_be_calculated_when_both_points_have_the_same_unit() {
        var p1 = CoordinateUnits.METER.point(0, 0);
        var p2 = CoordinateUnits.DEGREE.point(3, 4);
        assertThatThrownBy(() -> p1.distanceTo(p2)).isInstanceOf(IllegalArgumentException.class);
    }
}
