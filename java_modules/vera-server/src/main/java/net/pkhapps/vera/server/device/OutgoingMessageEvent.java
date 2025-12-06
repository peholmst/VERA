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

/// Interface for all events that concern an [OutgoingMessage].
public sealed interface OutgoingMessageEvent {

    /// Gets the outgoing message that this event concerns.
    ///
    /// @return the [OutgoingMessage]
    OutgoingMessage message();

    /// Gets the ID of the recipient that this event concerns.
    ///
    /// @return the [DeviceId]
    DeviceId recipient();

    /// Gets the instant at which the message was sent
    ///
    /// @return the [Instant]
    Instant sentOn();

    /// Event published when a message could not be delivered to a recipient within the expected time frame.
    /// In other words, it is possible the message was received but not acknowledged in time.
    ///
    /// The system may also have retried sending the message multiple times before publishing this event. After
    /// publishing this event the system will not attempt to deliver the message again.
    ///
    /// @param message   the message that could not be delivered to the recipient
    /// @param recipient the ID of the recipient that the message could not be delivered to
    /// @param sentOn    the instant of the first attempt at sending the message to the recipient
    /// @param failedOn  the instant at which the system determined the message was not successfully delivered
    record DeliveryFailed(OutgoingMessage message,
                          DeviceId recipient,
                          Instant sentOn,
                          Instant failedOn) implements OutgoingMessageEvent {
    }

    /// Event published when a message has been successfully delivered to and acknowledged by a recipient.
    ///
    /// @param message        the message that has been delivered
    /// @param recipient      the ID of the recipient who has successfully received the message
    /// @param sentOn         the instant of the first attempt at sending the message to the recipient
    /// @param acknowledgedOn the instant at which the message was acknowledged
    record DeliverySucceeded(OutgoingMessage message,
                             DeviceId recipient,
                             Instant sentOn,
                             Instant acknowledgedOn) implements OutgoingMessageEvent {
    }
}
