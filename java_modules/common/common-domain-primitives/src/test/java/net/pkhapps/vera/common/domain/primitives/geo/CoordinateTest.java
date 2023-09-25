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

public class CoordinateTest {

    @Test
    void coordinates_are_value_objects() {
        var c1 = Coordinate.latitude(49, CoordinateUnits.DEGREE);
        var c2 = Coordinate.latitude(49, CoordinateUnits.DEGREE);

        assertThat(c1).isNotSameAs(c2);
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void coordinates_with_different_units_are_not_equal() {
        var c1 = Coordinate.latitude(49, CoordinateUnits.DEGREE);
        var c2 = Coordinate.latitude(49, CoordinateUnits.METER);

        assertThat(c1).isNotEqualTo(c2);
        assertThat(c1.hashCode()).isNotEqualTo(c2.hashCode());
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void coordinates_with_different_axes_are_not_equal() {
        var c1 = Coordinate.latitude(49, CoordinateUnits.DEGREE);
        var c2 = Coordinate.longitude(49, CoordinateUnits.DEGREE);

        assertThat(c1).isNotEqualTo(c2);
        assertThat(c1.hashCode()).isNotEqualTo(c2.hashCode());
    }

    @Test
    void toString_has_been_overridden_and_shows_coordinates_with_three_decimals() {
        var c1 = Coordinate.latitude(49.53, CoordinateUnits.DEGREE);
        var c2 = Coordinate.longitude(-23.45, CoordinateUnits.METER);

        assertThat(c1.toString()).isEqualTo("Latitude{49.530000°}");
        assertThat(c2.toString()).isEqualTo("Longitude{-23.450000m}");
    }

    @Test
    void latitudes_can_be_compared() {
        var c1 = Coordinate.latitude(5, CoordinateUnits.DEGREE);
        var c2 = Coordinate.latitude(-5, CoordinateUnits.DEGREE);
        assertThat(c2).isLessThan(c1);
        assertThat(c1).isGreaterThan(c2);
        assertThat(c1).isEqualByComparingTo(c1);
        assertThat(c2).isEqualByComparingTo(c2);
    }

    @Test
    void latitudes_must_have_same_unit_when_comparing() {
        var c1 = Coordinate.latitude(5, CoordinateUnits.DEGREE);
        var c2 = Coordinate.latitude(-5, CoordinateUnits.METER);
        assertThatThrownBy(() -> c1.compareTo(c2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> c2.compareTo(c1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void longitudes_can_be_compared() {
        var c1 = Coordinate.longitude(5, CoordinateUnits.DEGREE);
        var c2 = Coordinate.longitude(-5, CoordinateUnits.DEGREE);
        assertThat(c2).isLessThan(c1);
        assertThat(c1).isGreaterThan(c2);
        assertThat(c1).isEqualByComparingTo(c1);
        assertThat(c2).isEqualByComparingTo(c2);
    }

    @Test
    void longitudes_must_have_same_unit_when_comparing() {
        var c1 = Coordinate.longitude(5, CoordinateUnits.DEGREE);
        var c2 = Coordinate.longitude(-5, CoordinateUnits.METER);
        assertThatThrownBy(() -> c1.compareTo(c2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> c2.compareTo(c1)).isInstanceOf(IllegalArgumentException.class);
    }
}
