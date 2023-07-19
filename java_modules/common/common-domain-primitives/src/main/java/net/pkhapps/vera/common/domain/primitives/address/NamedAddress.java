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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A named address is an address that has a name (such as a road name, street name or address point) and optionally an
 * address number, staircase letter and apartment number. New addresses are created using the
 * {@link #builder(Location, Accuracy, MunicipalityCode, AddressName) builder}.
 */
public final class NamedAddress extends Address {
    private final AddressName addressName;
    private final AddressNumber addressNumber;
    private final StaircaseLetter staircaseLetter;
    private final ApartmentNumber apartmentNumber;

    private NamedAddress(@NotNull Location location,
                         @NotNull Accuracy accuracy,
                         @NotNull MunicipalityCode municipality,
                         @Nullable Description description,
                         @NotNull AddressName addressName,
                         @Nullable AddressNumber addressNumber,
                         @Nullable StaircaseLetter staircaseLetter,
                         @Nullable ApartmentNumber apartmentNumber) {
        super(location, accuracy, municipality, description);
        this.addressName = requireNonNull(addressName, "addressName must not be null");
        this.addressNumber = addressNumber;
        this.staircaseLetter = staircaseLetter;
        this.apartmentNumber = apartmentNumber;
    }

    /**
     * Creates a builder for building new instances of {@link NamedAddress}. Required (non-{@code null}) address
     * attributes have to be given as parameters to this method. Optional attributes can be provided in the builder.
     *
     * @param location     the location of the address.
     * @param accuracy     the accuracy of the address.
     * @param municipality the municipality of the address.
     * @param addressName  the name of the address.
     * @return a new builder.
     */
    public static @NotNull Builder builder(@NotNull Location location,
                                           @NotNull Accuracy accuracy,
                                           @NotNull MunicipalityCode municipality,
                                           @NotNull AddressName addressName) {
        return new Builder(location, accuracy, municipality, addressName);
    }

    /**
     * Builder class for {@link NamedAddress}.
     */
    public static final class Builder extends AbstractBuilder<NamedAddress, Builder> {

        private final AddressName addressName;
        private AddressNumber addressNumber;
        private StaircaseLetter staircaseLetter;
        private ApartmentNumber apartmentNumber;

        private Builder(@NotNull Location location,
                        @NotNull Accuracy accuracy,
                        @NotNull MunicipalityCode municipality,
                        @NotNull AddressName addressName) {
            super(location, accuracy, municipality);
            this.addressName = requireNonNull(addressName, "addressName must not be null");
        }

        /**
         * Sets the address number of the address to build.
         *
         * @param addressNumber the address number, may be {@code null}.
         */
        public @NotNull Builder withAddressNumber(@Nullable AddressNumber addressNumber) {
            this.addressNumber = addressNumber;
            return self();
        }

        /**
         * Sets the staircase letter of the address to build.
         *
         * @param staircaseLetter the staircase letter, may be {@code null}.
         */
        public @NotNull Builder withStaircaseLetter(@Nullable StaircaseLetter staircaseLetter) {
            this.staircaseLetter = staircaseLetter;
            return self();
        }

        /**
         * Sets the apartment number of the address to build.
         *
         * @param apartmentNumber the apartment number, may be {@code null}.
         */
        public @NotNull Builder withApartmentNumber(@Nullable ApartmentNumber apartmentNumber) {
            this.apartmentNumber = apartmentNumber;
            return self();
        }

        @Override
        public @NotNull NamedAddress build() {
            return new NamedAddress(location, accuracy, municipality, description, addressName, addressNumber, staircaseLetter, apartmentNumber);
        }
    }

    /**
     * Returns the name of the address.
     */
    public @NotNull AddressName addressName() {
        return addressName;
    }

    /**
     * Returns the number of the address, if present.
     */
    public @NotNull Optional<AddressNumber> addressNumber() {
        return Optional.ofNullable(addressNumber);
    }

    /**
     * Returns the staircase letter of the address, if present.
     */
    public @NotNull Optional<StaircaseLetter> staircaseLetter() {
        return Optional.ofNullable(staircaseLetter);
    }

    /**
     * Returns the apartment number of the address, if present.
     */
    public @NotNull Optional<ApartmentNumber> apartmentNumber() {
        return Optional.ofNullable(apartmentNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NamedAddress that = (NamedAddress) o;
        return Objects.equals(addressName, that.addressName)
                && Objects.equals(addressNumber, that.addressNumber)
                && Objects.equals(staircaseLetter, that.staircaseLetter)
                && Objects.equals(apartmentNumber, that.apartmentNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), addressName, addressNumber, staircaseLetter, apartmentNumber);
    }
}
