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

import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.SerdeContext;
import net.pkhapps.vera.server.util.serde.UnsupportedTypeIdException;

import java.util.Set;

/// [Serde] for [Wgs84Point].
public class Wgs84PointSerde implements Serde<Wgs84Point> {

    public static final int TYPE_ID = 10;

    @Override
    public void serialize(SerdeContext context, Wgs84Point object, Output output) {
        var writer = context.newWriter(TYPE_ID);
        writer.putDouble(object.latitude());
        writer.putDouble(object.longitude());
        writer.writeTo(output);
    }

    @Override
    public int computeSize(SerdeContext context, Wgs84Point object) {
        return context.newSizeCalculator()
                .addDouble() // Latitude
                .addDouble() // Longitude
                .size();
    }

    @Override
    public Wgs84Point deserialize(SerdeContext context, int typeId, byte[] payload) {
        if (typeId != TYPE_ID) {
            throw new UnsupportedTypeIdException(typeId);
        }
        var reader = context.newReader(payload);
        return new Wgs84Point(
                reader.getDouble(),
                reader.getDouble()
        );
    }

    @Override
    public Set<Integer> supportedTypeIds() {
        return Set.of(TYPE_ID);
    }
}
