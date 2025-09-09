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

package net.pkhapps.vera.server.domain.base;

import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;

import java.util.function.Function;

/// [Serde] for [NanoIdentifier]s that can be used either as-is (using [#of(Function)]) or subclassed.
///
/// @param <ID> the subtype of [NanoIdentifier]
public class NanoIdentifierSerde<ID extends NanoIdentifier> implements Serde<ID> {

    private final Function<String, ID> constructor;

    protected NanoIdentifierSerde(Function<String, ID> constructor) {
        this.constructor = constructor;
    }

    /// Creates a new [Serde] for `<ID>`.
    ///
    /// @param <ID>        the subtype of [NanoIdentifier]
    /// @param constructor a constructor for turning a string into an `<ID>`
    /// @return a new [Serde]
    public static <ID extends NanoIdentifier> Serde<ID> of(Function<String, ID> constructor) {
        return new NanoIdentifierSerde<>(constructor);
    }

    @Override
    public void writeTo(ID object, Output output) {
        output.writeString(object.toString());
    }

    @Override
    public ID readFrom(Input input) {
        return constructor.apply(input.readString());
    }
}
