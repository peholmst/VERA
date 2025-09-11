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

package net.pkhapps.vera.server.util.serde;

/// Interface for a "serde" that can *serialize* and *deserialize* objects of a specific type.
///
/// @param <T> the type of objects to serialize and deserialize
public interface Serde<T> {

    /// Writes the given `object` to the given `output`.
    ///
    /// @param object the object to write (serialize)
    /// @param output the output destination to write to
    /// @throws SerdeException if something goes wrong
    void writeTo(T object, Output output);

    /// Reads an object from the given `input`.
    ///
    /// @param input the input source to read from
    /// @return the deserialized object
    /// @throws UnknownInputException if the input is unknown to this serde
    /// @throws SerdeException        if something else goes wrong
    T readFrom(Input input);
}
