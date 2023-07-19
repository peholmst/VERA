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

/**
 * An unnamed address is an address that has to be navigated to based on either the {@link #location()} or the
 * {@link #description()}, such as in the middle of the forest. New addresses are created using the
 * {@link #builder(Location, Accuracy, MunicipalityCode) builder}.
 */
public final class UnnamedAddress extends Address {

    private UnnamedAddress(@NotNull Location location,
                           @NotNull Accuracy accuracy,
                           @NotNull MunicipalityCode municipality,
                           @Nullable Description description) {
        super(location, accuracy, municipality, description);
    }

    /**
     * Creates a builder for building new instances of {@link UnnamedAddress}. Required (non-{@code null}) address
     * attributes have to be given as parameters to this method. Optional attributes can be provided in the builder.
     *
     * @param location     the location of the address.
     * @param accuracy     the accuracy of the address.
     * @param municipality the municipality of the address.
     * @return a new builder.
     */
    public static @NotNull Builder builder(@NotNull Location location,
                                           @NotNull Accuracy accuracy,
                                           @NotNull MunicipalityCode municipality) {
        return new Builder(location, accuracy, municipality);
    }

    /**
     * Builder class for {@link UnnamedAddress}.
     */
    public static final class Builder extends AbstractBuilder<UnnamedAddress, Builder> {

        private Builder(@NotNull Location location,
                        @NotNull Accuracy accuracy,
                        @NotNull MunicipalityCode municipality) {
            super(location, accuracy, municipality);
        }

        @Override
        public @NotNull UnnamedAddress build() {
            return new UnnamedAddress(location, accuracy, municipality, description);
        }
    }
}
