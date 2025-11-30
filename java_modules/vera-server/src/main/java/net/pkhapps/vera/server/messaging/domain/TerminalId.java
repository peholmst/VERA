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

/// Identifier of a terminal that can send and receive messages (the "address" if you will).
public final class TerminalId extends NanoIdentifier {

    private TerminalId(String id) {
        super(id);
    }

    private TerminalId() {
    }

    /// Creates a new `TerminalId` from the given Nano ID.
    ///
    /// @param id the Nano ID
    /// @return a new `TerminalId`
    /// @throws IllegalArgumentException if the given Nano ID is invalid
    public static TerminalId of(String id) {
        return new TerminalId(id);
    }

    /// Creates a new `TerminalId` from a random Nano ID.
    ///
    /// @return a new `TerminalId`
    public static TerminalId random() {
        return new TerminalId();
    }
}
