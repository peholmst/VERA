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

package net.pkhapps.vera.server.device.controller;

import net.pkhapps.vera.server.device.MessagePayloadSerde;
import net.pkhapps.vera.server.device.MessagePriority;
import net.pkhapps.vera.server.device.OutgoingMessageId;
import net.pkhapps.vera.server.util.Deferred;
import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;

import java.time.Instant;

/// [Serde] for [OutgoingMessageEnvelope].
final class OutgoingMessageEnvelopeSerde implements Serde<OutgoingMessageEnvelope> {

    private static final Deferred<OutgoingMessageEnvelopeSerde> INSTANCE = new Deferred<>(OutgoingMessageEnvelopeSerde::new);

    public static OutgoingMessageEnvelopeSerde instance() {
        return INSTANCE.get();
    }

    @Override
    public void writeTo(OutgoingMessageEnvelope object, Output output) {
        output.writeString(object.messageId().toString());
        output.writeInteger(object.priority().ordinal());
        output.writeLong(object.timestamp().toEpochMilli());
        MessagePayloadSerde.instance().writeTo(object.payload(), output);
    }

    @Override
    public OutgoingMessageEnvelope readFrom(Input input) {
        var messageId = OutgoingMessageId.of(input.readString());
        var priority = MessagePriority.values()[input.readInteger()];
        var timestamp = Instant.ofEpochMilli(input.readLong());
        var payload = MessagePayloadSerde.instance().readFrom(input);
        return new OutgoingMessageEnvelope(messageId, priority, timestamp, payload);
    }
}
