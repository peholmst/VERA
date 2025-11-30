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

import java.time.Instant;
import java.util.Set;

/// A message that has been scheduled for delivery to terminals.
///
/// @param messageId  the ID of the message
/// @param recipients a set of at least one terminal to deliver the message to
/// @param priority   the priority of the message
/// @param queuedOn   the instant at which the message was added to the message delivery queue
/// @param payload    the payload of the message
public record OutgoingMessage(
        OutgoingMessageId messageId,
        Set<TerminalId> recipients,
        MessagePriority priority,
        Instant queuedOn,
        MessagePayload payload
) {
}
