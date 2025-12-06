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

import net.pkhapps.vera.server.domain.base.NanoIdentifier;

/// Identifier of a device that can send and receive messages (the "address" if you will).
public final class DeviceId extends NanoIdentifier {

    private DeviceId(String id) {
        super(id);
    }

    private DeviceId() {
    }

    /// Creates a new `DeviceId` from the given Nano ID.
    ///
    /// @param id the Nano ID
    /// @return a new `DeviceId`
    /// @throws IllegalArgumentException if the given Nano ID is invalid
    public static DeviceId of(String id) {
        return new DeviceId(id);
    }

    /// Creates a new `DeviceId` from a random Nano ID.
    ///
    /// @return a new `DeviceId`
    public static DeviceId random() {
        return new DeviceId();
    }
}
