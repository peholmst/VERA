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

import net.pkhapps.vera.server.util.Deferred;
import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.UnknownInputException;

import java.util.function.Function;

/// [Serde] for [MessagePayload].
///
/// *Note*: [MessageType]s deserialized by this object use the [MessageType#fromString(String)] method. They have to be
/// cast to the domain-specific type using [MessageType#as(Function)] if necessary.
public final class MessagePayloadSerde implements Serde<MessagePayload> {

    private static final Deferred<MessagePayloadSerde> INSTANCE = new Deferred<>(MessagePayloadSerde::new);
    private static final short BINARY_PAYLOAD = 1;
    private static final short TEXT_PAYLOAD = 2;

    public static MessagePayloadSerde instance() {
        return INSTANCE.get();
    }

    private MessagePayloadSerde() {
    }

    @Override
    public void writeTo(MessagePayload object, Output output) {
        output.writeString(object.type().name());
        switch (object) {
            case MessagePayload.BinaryMessagePayload binary -> {
                output.writeShort(BINARY_PAYLOAD);
                output.writeInteger(binary.data().length);
                output.writeBytes(binary.data());
            }
            case MessagePayload.TextMessagePayload text -> {
                output.writeShort(TEXT_PAYLOAD);
                output.writeString(text.content());
            }
        }
    }

    @Override
    public MessagePayload readFrom(Input input) {
        var messageType = MessageType.fromString(input.readString());
        var payloadType = input.readShort();
        switch (payloadType) {
            case BINARY_PAYLOAD: {
                var length = input.readInteger();
                var data = input.readBytes(length);
                return new MessagePayload.BinaryMessagePayload(messageType, data);
            }
            case TEXT_PAYLOAD: {
                var content = input.readString();
                return new MessagePayload.TextMessagePayload(messageType, content);
            }
        }
        throw new UnknownInputException("Unknown payload type: " + payloadType);
    }
}
