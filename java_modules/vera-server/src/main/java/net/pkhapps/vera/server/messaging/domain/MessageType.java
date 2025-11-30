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

package net.pkhapps.vera.server.messaging.domain;

/// Interface for the message type. Clients use the message type to determine how to interpret the message payload.
///
/// Clients need to define their own domain-specific message types.
public interface MessageType {

    /// Gets the name of the message type. This string is used for serializing and deserializing and so needs to be
    /// unique among all the `MessageType` implementations that are in use.
    ///
    /// @return the message type name
    String name();
}
