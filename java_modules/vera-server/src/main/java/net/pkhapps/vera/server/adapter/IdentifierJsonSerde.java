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

package net.pkhapps.vera.server.adapter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.pkhapps.vera.server.domain.base.Identifier;

import java.io.IOException;
import java.util.function.Function;

/// Implements Jackson serializers and deserializes for [Identifier].
class IdentifierJsonSerde {

    private IdentifierJsonSerde() {
    }

    /// Adds a serializer and deserializer to the given `module` for the given `type`.
    ///
    /// @param module  the module to add a serializer and deserializer to
    /// @param type    the [Identifier] class
    /// @param factory a factory for converting a string to a `T`
    /// @param <T>     the [Identifier] type
    static <T extends Identifier> void registerIdentifier(SimpleModule module,
                                                          Class<T> type,
                                                          Function<String, T> factory) {
        module.addSerializer(type, new JsonSerializer<T>() {
            @Override
            public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(t.toString());
            }
        });
        module.addDeserializer(type, new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                return factory.apply(jsonParser.getValueAsString());
            }
        });
    }
}
