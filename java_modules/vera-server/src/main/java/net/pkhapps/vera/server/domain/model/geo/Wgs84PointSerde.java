/*
 * Copyright (c) 2025 Petter Holmström
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

package net.pkhapps.vera.server.domain.model.geo;

import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;

/// [Serde] for [Wgs84Point].
public class Wgs84PointSerde implements Serde<Wgs84Point> {

    private static final Wgs84PointSerde INSTANCE = new Wgs84PointSerde();

    public static Wgs84PointSerde instance() {
        return INSTANCE;
    }

    @Override
    public void writeTo(Wgs84Point object, Output output) {
        output.writeDouble(object.latitude());
        output.writeDouble(object.longitude());
    }

    @Override
    public Wgs84Point readFrom(Input input) {
        return new Wgs84Point(
                input.readDouble(),
                input.readDouble()
        );
    }
}
