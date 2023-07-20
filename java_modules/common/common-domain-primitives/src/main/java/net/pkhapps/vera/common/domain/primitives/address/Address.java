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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
 * A location that can be navigated to by a human. This can be a {@link NamedAddress named address},
 * an {@link UnnamedAddress unnamed address} or a {@link RoadJunction road junction}. An address is always in a
 * {@link #municipality() municipality} and it always has {@link #location() coordinates}. Additional
 * information needed in order to find the address can be provided in a {@link #description() description}.
 */
public sealed abstract class Address permits RoadJunction, NamedAddress, UnnamedAddress {

    private final Location location;
    private final Accuracy accuracy;
    private final MunicipalityCode municipality;
    private final Description description;

    protected Address(@NotNull Location location,
                      @NotNull Accuracy accuracy,
                      @NotNull MunicipalityCode municipality,
                      @Nullable Description description) {
        this.location = requireNonNull(location, "location must not be null");
        this.accuracy = requireNonNull(accuracy, "accuracy must not be null");
        this.municipality = requireNonNull(municipality, "municipality must not be null");
        this.description = description;
    }

    protected static abstract class AbstractBuilder<A extends Address, AB extends AbstractBuilder<A, AB>> {

        protected final Location location;
        protected final Accuracy accuracy;
        protected final MunicipalityCode municipality;
        protected Description description;

        protected AbstractBuilder(@NotNull Location location,
                                  @NotNull Accuracy accuracy,
                                  @NotNull MunicipalityCode municipality) {
            this.location = requireNonNull(location, "location must not be null");
            this.accuracy = requireNonNull(accuracy, "accuracy must not be null");
            this.municipality = requireNonNull(municipality, "municipality must not be null");
        }

        @SuppressWarnings("unchecked")
        protected @NotNull AB self() {
            return (AB) this;
        }

        /**
         * Sets the description of the address to build.
         *
         * @param description the description, may be {@code null}.
         */
        public @NotNull AB withDescription(@Nullable Description description) {
            this.description = description;
            return self();
        }

        /**
         * Creates a new address and returns it. This method can be called multiple times, and every time a new
         * address object will be returned.
         *
         * @return a new address object.
         */
        public abstract @NotNull A build();
    }

    /**
     * Returns the location of the address.
     */
    public @NotNull Location location() {
        return location;
    }

    /**
     * Returns the accuracy of the address (or more specifically, the {@link #location() location}).
     * An {@link Accuracy#INACCURATE inaccurate} address is to be considered "somewhere in the vicinity of the
     * {@link #location() location}" and can be off by even hundreds of meters.
     */
    public @NotNull Accuracy accuracy() {
        return accuracy;
    }

    /**
     * Returns the municipality of the address. All {@link #location() locations} in Finland should belong to some
     * municipality. However, if the municipality is not known for some reason, {@link MunicipalityCode#UNKNOWN}
     * can be used.
     */
    public @NotNull MunicipalityCode municipality() {
        return municipality;
    }

    /**
     * Returns the description of the address, if present.
     */
    public @NotNull Optional<Description> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(location, address.location)
                && accuracy == address.accuracy
                && Objects.equals(municipality, address.municipality)
                && Objects.equals(description, address.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, accuracy, municipality, description);
    }
}
