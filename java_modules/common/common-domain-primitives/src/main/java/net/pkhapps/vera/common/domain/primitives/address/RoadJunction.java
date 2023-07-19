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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A road junction is where two or more roads meet. New junctions are created using the
 * {@link #builder(Location, Accuracy, MunicipalityCode, AddressName, AddressName) builder}.
 */
public final class RoadJunction extends Address {

    private final List<AddressName> roadNames;

    private RoadJunction(@NotNull Location location,
                         @NotNull Accuracy accuracy,
                         @NotNull MunicipalityCode municipality,
                         @Nullable Description description,
                         @NotNull List<AddressName> roadNames) {
        super(location, accuracy, municipality, description);
        requireNonNull(roadNames, "roadNames must not be null");
        assert roadNames.size() >= 2 : "must provide at least two road names"; // The builder guarantees that this is always true
        this.roadNames = List.copyOf(roadNames);
    }

    /**
     * Creates a builder for building new instances of {@link RoadJunction}. Required (non-{@code null}) address
     * attributes have to be given as parameters to this method. Optional attributes can be provided in the builder.
     *
     * @param location     the location of the address.
     * @param accuracy     the accuracy of the address.
     * @param municipality the municipality of the address.
     * @param firstRoad    the name of the first meeting road.
     * @param secondRoad   the name of the second meeting road.
     * @return a new builder.
     */
    public static @NotNull Builder builder(@NotNull Location location,
                                           @NotNull Accuracy accuracy,
                                           @NotNull MunicipalityCode municipality,
                                           @NotNull AddressName firstRoad,
                                           @NotNull AddressName secondRoad) {
        return new Builder(location, accuracy, municipality, firstRoad, secondRoad);
    }

    /**
     * Builder class for {@link RoadJunction}.
     */
    public static final class Builder extends AbstractBuilder<RoadJunction, Builder> {

        private final List<AddressName> roadNames = new ArrayList<>();

        private Builder(@NotNull Location location,
                        @NotNull Accuracy accuracy,
                        @NotNull MunicipalityCode municipality,
                        @NotNull AddressName firstRoadName,
                        @NotNull AddressName secondRoadName) {
            super(location, accuracy, municipality);
            roadNames.add(requireNonNull(firstRoadName, "firstRoadName must not be null"));
            roadNames.add(requireNonNull(secondRoadName, "secondRoadName must not be null"));
        }

        /**
         * Adds a road name to the junction, in addition to the two specified when the builder was created.
         *
         * @param roadName the road name to add.
         */
        public @NotNull Builder withAdditionalRoadName(@NotNull AddressName roadName) {
            this.roadNames.add(requireNonNull(roadName, "roadName must not be null"));
            return self();
        }

        @Override
        public @NotNull RoadJunction build() {
            return new RoadJunction(location, accuracy, municipality, description, roadNames);
        }
    }

    /**
     * Returns the names of the roads that meet in the junction.
     *
     * @return a non-modifiable list containing at least two {@link AddressName}s.
     */
    public @NotNull List<AddressName> roadNames() {
        return roadNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoadJunction that = (RoadJunction) o;
        return Objects.equals(roadNames, that.roadNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roadNames);
    }
}
