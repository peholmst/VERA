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

public class UnnamedAddressTest {

    private final Location location = CoordinateReferenceSystems.ETRS_TM35FIN.createLocation(230905, 6686386);
    private final MunicipalityCode municipality = MunicipalityCode.of(445);
    private final Description description = Description.of("Grund nordöst om Mise");

    @Test
    void unnamed_address_at_minimum_contains_location_accuracy_and_municipality() {
        var ua = UnnamedAddress.builder(location, Accuracy.ACCURATE, municipality).build();
        assertThat(ua.location()).isEqualTo(location);
        assertThat(ua.accuracy()).isEqualTo(Accuracy.ACCURATE);
        assertThat(ua.municipality()).isEqualTo(municipality);
        assertThat(ua.description()).isEmpty();
    }

    @Test
    void unnamed_address_can_have_a_description() {
        var ua = UnnamedAddress.builder(location, Accuracy.ACCURATE, municipality)
                .withDescription(description)
                .build();
        assertThat(ua.description()).contains(description);
    }

    @Test
    void unnamed_address_is_a_value_object() {
        var builder = UnnamedAddress.builder(location, Accuracy.ACCURATE, municipality)
                .withDescription(description);
        var ua1 = builder.build();
        var ua2 = builder.build();

        assertThat(ua1).isNotSameAs(ua2);
        assertThat(ua1).isEqualTo(ua2);
        assertThat(ua1.hashCode()).isEqualTo(ua2.hashCode());
    }
}
