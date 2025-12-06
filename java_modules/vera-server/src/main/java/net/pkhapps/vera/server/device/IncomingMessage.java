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

import java.time.Instant;

/// A message that has been sent by a device to the server.
///
/// @param messageId  the ID of the message (set by the device)
/// @param sender     the ID of the device that sent the message
/// @param priority   the priority of the message (set by the device)
/// @param sentOn     the timestamp at which the message was sent (set by the device)
/// @param receivedOn the timestamp at which the message was received by the server
/// @param payload    the payload of the message
public record IncomingMessage(
        IncomingMessageId messageId,
        DeviceId sender,
        MessagePriority priority,
        Instant sentOn,
        Instant receivedOn,
        MessagePayload payload
) {
}
