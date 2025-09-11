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

package net.pkhapps.vera.server.util.wal;

import net.pkhapps.vera.server.util.serde.BufferInput;
import net.pkhapps.vera.server.util.serde.BufferOutput;
import net.pkhapps.vera.server.util.serde.UnknownInputException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static net.pkhapps.vera.server.util.serde.SerdeTestUtils.assertSerializationAndDeserializationProducesEqualObject;

class WalSerdeTest {

    @Test
    void serialize_deserialize_with_correct_serde_id_with_sub_types() {
        var serde = new TestEventSerde(512);
        assertSerializationAndDeserializationProducesEqualObject(serde,
                new TestEvent.MyFirstEvent("Hello World", 123));
        assertSerializationAndDeserializationProducesEqualObject(serde,
                new TestEvent.MySecondEvent(Instant.now(), UUID.randomUUID()));
        assertSerializationAndDeserializationProducesEqualObject(serde,
                new TestEvent.MyThirdEvent(987L, true));
    }

    @Test
    void serialize_and_deserialize_with_correct_serde_id_without_sub_types() {
        var serde = new TestSnapshotSerde(128);
        assertSerializationAndDeserializationProducesEqualObject(serde,
                new TestSnapshot(List.of("hello", "beautiful", "world", "!")));
    }

    @Test
    void fails_on_incorrect_serde_id_with_sub_types() {
        var event = new TestEvent.MySecondEvent(Instant.now(), UUID.randomUUID());
        var output = BufferOutput.allocate(1024);
        var firstSerde = new TestEventSerde(512);
        firstSerde.writeTo(event, output);

        var input = BufferInput.wrap(output.buffer().array());
        var secondSerde = new TestEventSerde(256);
        Assertions.assertThatThrownBy(() -> secondSerde.readFrom(input)).isInstanceOf(UnknownInputException.class);
    }

    @Test
    void fails_on_incorrect_subtype_id() {
        // This will write an entry with a subtype.
        var event = new TestEvent.MySecondEvent(Instant.now(), UUID.randomUUID());
        var output = BufferOutput.allocate(1024);
        var firstSerde = new TestEventSerde(128);
        firstSerde.writeTo(event, output);

        // This will assume an entry without a subtype
        var input = BufferInput.wrap(output.buffer().array());
        var secondSerde = new TestSnapshotSerde(128);
        Assertions.assertThatThrownBy(() -> secondSerde.readFrom(input)).isInstanceOf(UnknownInputException.class);
    }
}
