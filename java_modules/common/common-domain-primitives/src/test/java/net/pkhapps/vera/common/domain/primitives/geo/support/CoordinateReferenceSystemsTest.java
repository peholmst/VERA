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

package net.pkhapps.vera.common.domain.primitives.geo.support;

import net.pkhapps.vera.common.domain.primitives.geo.Coordinate;
import net.pkhapps.vera.common.domain.primitives.geo.CoordinateUnits;
import net.pkhapps.vera.common.domain.primitives.geo.SRID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoordinateReferenceSystemsTest {

    @Test
    void wgs84_parameters_are_valid() {
        var crs = CoordinateReferenceSystems.WGS84;
        assertThat(crs.unit()).isEqualTo(CoordinateUnits.DEGREE);
        assertThat(crs.srid()).isEqualTo(SRID.of(4326));
        assertThat(crs.srid()).isEqualTo(CoordinateReferenceSystems.WGS84_SRID);
        assertThat(crs.name()).isEqualTo("WGS 84");

        assertThat(crs.isCoordinateValid(Coordinate.longitude(-180, CoordinateUnits.DEGREE))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.longitude(180, CoordinateUnits.DEGREE))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.longitude(-180.1, CoordinateUnits.DEGREE))).isFalse();
        assertThat(crs.isCoordinateValid(Coordinate.longitude(180.1, CoordinateUnits.DEGREE))).isFalse();

        assertThat(crs.isCoordinateValid(Coordinate.latitude(-90, CoordinateUnits.DEGREE))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.latitude(90, CoordinateUnits.DEGREE))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.latitude(-90.1, CoordinateUnits.DEGREE))).isFalse();
        assertThat(crs.isCoordinateValid(Coordinate.latitude(90.1, CoordinateUnits.DEGREE))).isFalse();
    }

    @Test
    void etrs89_tm35fin_parameters_are_valid() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        assertThat(crs.unit()).isEqualTo(CoordinateUnits.METER);
        assertThat(crs.srid()).isEqualTo(SRID.of(3067));
        assertThat(crs.srid()).isEqualTo(CoordinateReferenceSystems.ETRS89_TM35FIN_SRID);
        assertThat(crs.name()).isEqualTo("ETRS89 / TM35FIN(E,N)");

        assertThat(crs.isCoordinateValid(Coordinate.longitude(-3669433.9, CoordinateUnits.METER))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.longitude(3638050.95, CoordinateUnits.METER))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.longitude(-3669433.91, CoordinateUnits.METER))).isFalse();
        assertThat(crs.isCoordinateValid(Coordinate.longitude(3638050.96, CoordinateUnits.METER))).isFalse();

        assertThat(crs.isCoordinateValid(Coordinate.latitude(1737349.49, CoordinateUnits.METER))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.latitude(9567789.69, CoordinateUnits.METER))).isTrue();
        assertThat(crs.isCoordinateValid(Coordinate.latitude(1737349.48, CoordinateUnits.METER))).isFalse();
        assertThat(crs.isCoordinateValid(Coordinate.latitude(9567789.70, CoordinateUnits.METER))).isFalse();
    }
}
