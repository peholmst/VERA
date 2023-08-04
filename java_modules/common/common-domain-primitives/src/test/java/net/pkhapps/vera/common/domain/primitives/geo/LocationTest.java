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

import net.pkhapps.vera.common.domain.primitives.geo.support.CoordinateReferenceSystems;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocationTest {

    @Test
    void coordinates_must_be_within_bounds() {
        assertThatThrownBy(() -> Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(181), CoordinateUnits.DEGREE.latitude(90))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(180), CoordinateUnits.DEGREE.latitude(91))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(-181), CoordinateUnits.DEGREE.latitude(-90))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(-180), CoordinateUnits.DEGREE.latitude(-91))).isInstanceOf(IllegalArgumentException.class);
        Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(180), CoordinateUnits.DEGREE.latitude(90));
        Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(-180), CoordinateUnits.DEGREE.latitude(-90));
    }

    @Test
    void coordinates_must_have_correct_unit() {
        assertThatThrownBy(() -> Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.METER.longitude(180), CoordinateUnits.DEGREE.latitude(90))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(180), CoordinateUnits.METER.latitude(90))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getters_return_correct_values() {
        var l = Location.of(CoordinateReferenceSystems.WGS84, CoordinateUnits.DEGREE.longitude(0), CoordinateUnits.DEGREE.latitude(10));
        assertThat(l.crs()).isEqualTo(CoordinateReferenceSystems.WGS84);
        assertThat(l.longitude().value()).isEqualTo(0);
        assertThat(l.latitude().value()).isEqualTo(10);
    }

    @Test
    void location_is_a_value_object() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var lon = CoordinateUnits.METER.longitude(240106);
        var lat = CoordinateUnits.METER.latitude(6697275);

        var l1 = Location.of(crs, lon, lat);
        var l2 = Location.of(crs, lon, lat);

        assertThat(l1).isNotSameAs(l2);
        assertThat(l1).isEqualTo(l2);
        assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
    }

    @Test
    void toString_has_been_overridden_and_shows_coordinates_with_three_decimals() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var lon = CoordinateUnits.METER.longitude(240106);
        var lat = CoordinateUnits.METER.latitude(6697275);
        var l = Location.of(crs, lon, lat);
        assertThat(l.toString()).isEqualTo("Location{crs=3067, lon=240106.000m, lat=6697275.000m}");
    }
}
