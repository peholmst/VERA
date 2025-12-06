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

package net.pkhapps.vera.server.device;

import java.util.function.Function;

/// Interface for the message type. Clients use the message type to determine how to interpret the message payload.
///
/// Clients need to define their own domain-specific message type implementations, or use [#fromString(String)].
public interface MessageType {

    /// Gets the name of the message type. This string is used for serializing and deserializing and so needs to be
    /// unique among all the `MessageType` implementations that are in use.
    ///
    /// @return the message type name
    String name();

    /// Casts this message type to a domain-specific implementation using the given factory.
    ///
    /// @param factory a factory for creating the domain-specific implementation from the name
    /// @return a domain-specific `MessageType` implementation with the given name
    default <T extends MessageType> T as(Function<String, T> factory) {
        return factory.apply(name());
    }

    /// Creates an anonymous `MessageType` with the given name. If needed, clients can use [#as(Function)] to cast
    /// the returned message type into the correct domain-specific type.
    ///
    /// @param name the unique name of the message type
    /// @return a new `MessageType` with the given name
    static MessageType fromString(String name) {
        return () -> name;
    }
}
