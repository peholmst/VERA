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

import net.pkhapps.vera.server.util.API;
import net.pkhapps.vera.server.util.Registration;

import java.util.Set;
import java.util.function.Consumer;

/// API for sending messages to devices.
@API
public interface ForSendingMessages {

    /// Schedules the given message for sending based on its priority. This method returns immediately regardless of
    /// whether the message was delivered or not.
    ///
    /// Register a message listener with [#registerOutgoingMessageListener(Consumer)] to get updates on the status of
    /// the message. Every registered listener will receive a callback for every message and recipient, regardless of
    /// whether delivery succeeded or failed.
    ///
    /// @param recipients a set of at least one [DeviceId] to send the message to
    /// @param priority   the message priority
    /// @param payload    the message payload
    /// @return an [OutgoingMessage]
    OutgoingMessage sendMessage(Set<DeviceId> recipients, MessagePriority priority, MessagePayload payload);

    /// Registers a new listener to be notified of [OutgoingMessageEvent]s. Any exceptions thrown by the listener will
    /// be logged and ignored.
    ///
    /// @param listener the listener to call
    /// @return a [Registration] object for removing the listener
    Registration registerOutgoingMessageListener(Consumer<OutgoingMessageEvent> listener);
}
