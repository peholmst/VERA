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

import net.pkhapps.vera.server.domain.base.NanoIdentifier;

/// Identifier of a message that has been scheduled for delivery.
public final class OutgoingMessageId extends NanoIdentifier {

    // TODO Should this be a UUIDv7 instead so that you can tell which message is newer than another?

    private OutgoingMessageId(String id) {
        super(id);
    }

    private OutgoingMessageId() {
    }

    /// Creates a new `OutgoingMessageId` from the given Nano ID.
    ///
    /// @param id the Nano ID
    /// @return a new `OutgoingMessageId`
    /// @throws IllegalArgumentException if the given Nano ID is invalid
    public static OutgoingMessageId of(String id) {
        return new OutgoingMessageId(id);
    }

    /// Creates a new `OutgoingMessageId` from a random Nano ID.
    ///
    /// @return a new `OutgoingMessageId`
    public static OutgoingMessageId random() {
        return new OutgoingMessageId();
    }
}
