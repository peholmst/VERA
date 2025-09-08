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

package net.pkhapps.vera.server.domain.model.i18n;

import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.SerdeContext;
import net.pkhapps.vera.server.util.serde.UnsupportedTypeIdException;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

/// [Serde] for [MultiLingualString].
public class MultiLingualStringSerde implements Serde<MultiLingualString> {

    public final int TYPE_ID = 11;

    @Override
    public void serialize(SerdeContext context, MultiLingualString object, Output output) {
        var writer = context.newWriter(TYPE_ID);
        writer.putInteger(object.size());
        object.entries().forEach(entry -> {
            writer.putString(entry.locale().toLanguageTag());
            writer.putString(entry.value());
        });
        writer.writeTo(output);
    }

    @Override
    public int computeSize(SerdeContext context, MultiLingualString object) {
        var calculator = context.newSizeCalculator();
        calculator.addInteger(); // Size
        object.entries().forEach(entry -> {
            calculator.addString(entry.locale().toLanguageTag());
            calculator.addString(entry.value());
        });
        return calculator.size();
    }

    @Override
    public MultiLingualString deserialize(SerdeContext context, int typeId, byte[] payload) {
        if (typeId != TYPE_ID) {
            throw new UnsupportedTypeIdException(typeId);
        }
        var reader = context.newReader(payload);
        var size = reader.getInteger();
        var entries = new ArrayList<MultiLingualString.LocalizedString>(size);
        for (int i = 0; i < size; ++i) {
            entries.add(new MultiLingualString.LocalizedString(
                    Locale.forLanguageTag(reader.getString()),
                    reader.getString())
            );
        }
        return MultiLingualString.of(entries);
    }

    @Override
    public Set<Integer> supportedTypeIds() {
        return Set.of(TYPE_ID);
    }
}
