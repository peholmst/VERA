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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BoundingBoxTest {

    @Test
    void coordinates_can_be_in_any_order() {
        var bbs = List.of(
                BoundingBox.of(CoordinateReferenceSystems.WGS84, 0, 0, 1, 1),
                BoundingBox.of(CoordinateReferenceSystems.WGS84, 0, 1, 1, 0),
                BoundingBox.of(CoordinateReferenceSystems.WGS84, 1, 0, 0, 1),
                BoundingBox.of(CoordinateReferenceSystems.WGS84, 1, 1, 0, 0)

        );
        bbs.forEach(bb -> {
            assertThat(bb.lowerCorner()).isEqualTo(Point.of(CoordinateUnits.DEGREE, 0, 0));
            assertThat(bb.upperCorner()).isEqualTo(Point.of(CoordinateUnits.DEGREE, 1, 1));
            assertThat(bb.crs()).isEqualTo(CoordinateReferenceSystems.WGS84);
        });
    }

    @Test
    void coordinates_must_be_valid() {
        assertThatThrownBy(() -> BoundingBox.of(CoordinateReferenceSystems.WGS84,
                CoordinateUnits.METER.point(240106, 6697275),
                CoordinateUnits.DEGREE.point(0, 0))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bounding_box_is_a_value_object() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);

        var bb1 = BoundingBox.of(crs, p1, p2);
        var bb2 = BoundingBox.of(crs, p1, p2);

        assertThat(bb1).isNotSameAs(bb2);
        assertThat(bb1).isEqualTo(bb2);
        assertThat(bb1.hashCode()).isEqualTo(bb2.hashCode());
    }

    @Test
    void toString_has_been_overridden() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);
        assertThat(bb.toString()).isEqualTo("BoundingBox{crs=3067, lowerCorner=Point{lon=240106.000m, lat=6697275.000m}, upperCorner=Point{lon=240110.000m, lat=6697280.000m}}");
    }

    @Test
    void dimensions_can_be_calculated() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);
        assertThat(bb.dimensions().width()).isEqualTo(CoordinateUnits.METER.dimension(4));
        assertThat(bb.dimensions().height()).isEqualTo(CoordinateUnits.METER.dimension(5));
    }

    @Test
    void bounding_box_knows_which_points_are_inside() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);

        assertThat(bb.contains(CoordinateUnits.METER.point(240108, 6697276))).isTrue();
    }

    @Test
    void bounding_box_knows_which_points_are_outside() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);
        assertThat(bb.contains(CoordinateUnits.METER.point(240105, 6697276))).isFalse();
        assertThat(bb.contains(CoordinateUnits.METER.point(240111, 6697276))).isFalse();
        assertThat(bb.contains(CoordinateUnits.METER.point(240108, 6697274))).isFalse();
        assertThat(bb.contains(CoordinateUnits.METER.point(240108, 6697281))).isFalse();
    }

    @Test
    void a_point_in_the_corners_is_inside_the_box() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);

        assertThat(bb.contains(p1)).isTrue();
        assertThat(bb.contains(p2)).isTrue();
    }

    @Test
    void units_must_be_equal_when_checking_a_point() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);

        assertThatThrownBy(() -> bb.contains(CoordinateUnits.DEGREE.point(240108, 6697276))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bounding_box_knows_whether_another_bounding_box_is_inside_it() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240100, 6697270);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var p3 = CoordinateUnits.METER.point(240102, 6697272);
        var p4 = CoordinateUnits.METER.point(240108, 6697278);
        var outer = BoundingBox.of(crs, p1, p2);
        var inner = BoundingBox.of(crs, p3, p4);

        assertThat(outer.contains(inner)).isTrue();
        assertThat(inner.contains(outer)).isFalse();
    }

    @Test
    void a_bounding_box_contains_itself() {
        var crs = CoordinateReferenceSystems.ETRS89_TM35FIN;
        var p1 = CoordinateUnits.METER.point(240106, 6697275);
        var p2 = CoordinateUnits.METER.point(240110, 6697280);
        var bb = BoundingBox.of(crs, p1, p2);

        assertThat(bb.contains(bb)).isTrue();
    }
}
