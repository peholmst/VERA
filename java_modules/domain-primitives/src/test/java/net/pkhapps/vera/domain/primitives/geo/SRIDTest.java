/*
 * Copyright 2023 Petter Holmström
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
package net.pkhapps.vera.domain.primitives.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SRID}.
 *
 * @author Petter Holmström
 */
public class SRIDTest {

    @Test
    public void srids_cannot_be_below_minimum() {
        assertThatThrownBy(() -> new SRID(SRID.MIN - 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void srids_cannot_be_above_maximum() {
        assertThatThrownBy(() -> new SRID(SRID.MAX + 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void minimum_srid_can_be_created() {
        assertThat(new SRID(SRID.MIN).value()).isEqualTo(SRID.MIN);
    }

    @Test
    public void maximum_srid_can_be_created() {
        assertThat(new SRID(SRID.MAX).value()).isEqualTo(SRID.MAX);
    }

    @Test
    public void srids_with_same_value_are_considered_equal() {
        var srid1 = new SRID(4326);
        var srid2 = new SRID(4326);
        assertThat(srid1).isEqualTo(srid2);
        assertThat(srid1.hashCode()).isEqualTo(srid2.hashCode());
    }

    @Test
    public void srids_with_different_values_are_not_considered_equal() {
        var srid1 = new SRID(4326);
        var srid2 = new SRID(26717);
        assertThat(srid1).isNotEqualTo(srid2);
        assertThat(srid1.hashCode()).isNotEqualTo(srid2.hashCode());
    }
}
