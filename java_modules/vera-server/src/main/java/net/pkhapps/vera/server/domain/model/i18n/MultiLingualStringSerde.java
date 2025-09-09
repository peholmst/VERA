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

import net.pkhapps.vera.server.util.Deferred;
import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;

import java.util.HashMap;
import java.util.Locale;

/// [Serde] for [MultiLingualString].
public final class MultiLingualStringSerde implements Serde<MultiLingualString> {

    private static final Deferred<MultiLingualStringSerde> INSTANCE = new Deferred<>(MultiLingualStringSerde::new);

    public static MultiLingualStringSerde instance() {
        return INSTANCE.get();
    }

    private MultiLingualStringSerde() {
    }

    @Override
    public void writeTo(MultiLingualString object, Output output) {
        output.writeInteger(object.size());
        object.forEach((locale, value) -> {
            output.writeString(locale.toLanguageTag());
            output.writeString(value);
        });
    }

    @Override
    public MultiLingualString readFrom(Input input) {
        var size = input.readInteger();
        var entries = new HashMap<Locale, String>(size);
        for (int i = 0; i < size; ++i) {
            entries.put(Locale.forLanguageTag(input.readString()), input.readString());
        }
        return new MultiLingualString(entries);
    }
}
