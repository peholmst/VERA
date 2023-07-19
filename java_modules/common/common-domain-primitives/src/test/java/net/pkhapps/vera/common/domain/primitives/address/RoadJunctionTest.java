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

package net.pkhapps.vera.common.domain.primitives.address;

import net.pkhapps.vera.common.domain.primitives.common.Description;
import net.pkhapps.vera.common.domain.primitives.geo.Location;
import net.pkhapps.vera.common.domain.primitives.geo.support.CoordinateReferenceSystems;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RoadJunctionTest {

    private final Location location = CoordinateReferenceSystems.ETRS_TM35FIN.createLocation(240479, 6694823);
    private final MunicipalityCode municipality = MunicipalityCode.of(445);
    private final AddressName firstRoad = AddressName.bilingual("Saaristotie", "Skärgårdsvägen");
    private final AddressName secondRoad = AddressName.bilingual("Vapparintie", "Vapparvägen");
    private final AddressName thirdRoad = AddressName.bilingual("Rantatie", "Strandvägen");
    private final Description description = Description.of("Segelrondellen");

    @Test
    void road_junction_at_minimum_contains_location_accuracy_municipality_and_two_road_names() {
        var rj = RoadJunction.builder(location, Accuracy.ACCURATE, municipality, firstRoad, secondRoad).build();
        assertThat(rj.location()).isEqualTo(location);
        assertThat(rj.accuracy()).isEqualTo(Accuracy.ACCURATE);
        assertThat(rj.municipality()).isEqualTo(municipality);
        assertThat(rj.roadNames()).containsExactly(firstRoad, secondRoad);
        assertThat(rj.description()).isEmpty();
    }

    @Test
    void road_junction_can_have_more_than_two_road_names() {
        var rj = RoadJunction.builder(location, Accuracy.ACCURATE, municipality, firstRoad, secondRoad)
                .withAdditionalRoadName(thirdRoad)
                .build();
        assertThat(rj.roadNames()).containsExactly(firstRoad, secondRoad, thirdRoad);
    }

    @Test
    void road_junction_can_have_a_description() {
        var rj = RoadJunction.builder(location, Accuracy.ACCURATE, municipality, firstRoad, secondRoad)
                .withAdditionalRoadName(thirdRoad)
                .withDescription(description)
                .build();
        assertThat(rj.description()).contains(description);
    }

    @Test
    void road_junction_is_a_value_object() {
        var builder = RoadJunction.builder(location, Accuracy.ACCURATE, municipality, firstRoad, secondRoad)
                .withAdditionalRoadName(thirdRoad)
                .withDescription(description);
        var rj1 = builder.build();
        var rj2 = builder.build();

        assertThat(rj1).isNotSameAs(rj2);
        assertThat(rj1).isEqualTo(rj2);
        assertThat(rj1.hashCode()).isEqualTo(rj2.hashCode());
    }
}
