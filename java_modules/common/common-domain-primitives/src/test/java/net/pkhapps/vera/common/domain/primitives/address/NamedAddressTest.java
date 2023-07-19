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

public class NamedAddressTest {

    private final Location location = CoordinateReferenceSystems.ETRS_TM35FIN.createLocation(240471, 6694823);
    private final MunicipalityCode municipality = MunicipalityCode.of(445);
    private final AddressName name = AddressName.bilingual("Saaristotie", "Skärgårdsvägen");
    private final Description description = Description.of("A description");
    private final AddressNumber number = AddressNumber.of(123);
    private final StaircaseLetter staircaseLetter = StaircaseLetter.of('A');
    private final ApartmentNumber apartmentNumber = ApartmentNumber.of(10, "B");

    @Test
    void named_address_at_minimum_contains_location_accuracy_municipality_and_name() {
        var na = NamedAddress.builder(location, Accuracy.ACCURATE, municipality, name).build();
        assertThat(na.location()).isEqualTo(location);
        assertThat(na.accuracy()).isEqualTo(Accuracy.ACCURATE);
        assertThat(na.municipality()).isEqualTo(municipality);
        assertThat(na.addressName()).isEqualTo(name);
        assertThat(na.addressNumber()).isEmpty();
        assertThat(na.staircaseLetter()).isEmpty();
        assertThat(na.apartmentNumber()).isEmpty();
    }

    @Test
    void named_address_can_have_a_number() {
        var na = NamedAddress.builder(location, Accuracy.ACCURATE, municipality, name)
                .withAddressNumber(number)
                .build();
        assertThat(na.addressNumber()).contains(number);
    }

    @Test
    void named_address_can_have_a_staircase_letter() {
        var na = NamedAddress.builder(location, Accuracy.ACCURATE, municipality, name)
                .withStaircaseLetter(staircaseLetter)
                .build();
        assertThat(na.staircaseLetter()).contains(staircaseLetter);
    }

    @Test
    void named_address_can_have_an_apartment_number() {
        var na = NamedAddress.builder(location, Accuracy.ACCURATE, municipality, name)
                .withApartmentNumber(apartmentNumber)
                .build();
        assertThat(na.apartmentNumber()).contains(apartmentNumber);
    }

    @Test
    void named_address_can_have_a_description() {
        var na = NamedAddress.builder(location, Accuracy.ACCURATE, municipality, name)
                .withDescription(description)
                .build();
        assertThat(na.description()).contains(description);
    }

    @Test
    void named_address_is_a_value_object() {
        var builder = NamedAddress.builder(location, Accuracy.ACCURATE, municipality, name)
                .withAddressNumber(number)
                .withStaircaseLetter(staircaseLetter)
                .withApartmentNumber(apartmentNumber)
                .withDescription(description);
        var na1 = builder.build();
        var na2 = builder.build();

        assertThat(na1).isNotSameAs(na2);
        assertThat(na1).isEqualTo(na2);
        assertThat(na1.hashCode()).isEqualTo(na2.hashCode());
    }
}
