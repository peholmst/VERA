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

public class SRIDTest {

    @Test
    void srids_cannot_be_below_minimum() {
        assertThatThrownBy(() -> SRID.of(SRID.MIN - 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void srids_cannot_be_above_maximum() {
        assertThatThrownBy(() -> SRID.of(SRID.MAX + 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void minimum_srid_can_be_created() {
        assertThat(SRID.of(SRID.MIN).value()).isEqualTo(SRID.MIN);
    }

    @Test
    void maximum_srid_can_be_created() {
        assertThat(SRID.of(SRID.MAX).value()).isEqualTo(SRID.MAX);
    }

    @Test
    void srids_with_same_value_are_considered_equal() {
        var srid1 = SRID.of(4326);
        var srid2 = SRID.of(4326);
        assertThat(srid1).isEqualTo(srid2);
        assertThat(srid1.hashCode()).isEqualTo(srid2.hashCode());
    }

    @Test
    void srids_with_different_values_are_not_considered_equal() {
        var srid1 = SRID.of(4326);
        var srid2 = SRID.of(26717);
        assertThat(srid1).isNotEqualTo(srid2);
        assertThat(srid1.hashCode()).isNotEqualTo(srid2.hashCode());
    }

    @Test
    void toString_has_been_overridden() {
        var srid = SRID.of(4326);
        assertThat(srid.toString()).isEqualTo("SRID{4326}");
    }
}
