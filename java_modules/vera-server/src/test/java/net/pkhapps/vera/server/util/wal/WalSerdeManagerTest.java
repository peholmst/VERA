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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WalSerdeManagerTest {

    WalSerdeManager serdeManager;

    @BeforeEach
    void setUp() {
        serdeManager = new WalSerdeManager(List.of(registry -> {
            registry.registerWalSerde(new TestEventSerde(64));
            registry.registerWalSerde(new TestSnapshotSerde(128));
        }));
    }

    @Test
    void serialize_and_deserialize_TestEvent() {
        // Loop a few times to test the caching
        for (int i = 0; i < 10; ++i) {
            assertSerializationAndDeserializationProducesEqualObject(new TestEvent.MyFirstEvent("Hello World", 123));
            assertSerializationAndDeserializationProducesEqualObject(new TestEvent.MySecondEvent(Instant.now(), UUID.randomUUID()));
            assertSerializationAndDeserializationProducesEqualObject(new TestEvent.MyThirdEvent(987L, true));
        }
    }

    @Test
    void serialize_and_deserialize_TestSnapshot() {
        // Loop a few times to test the caching
        for (int i = 0; i < 10; ++i) {
            assertSerializationAndDeserializationProducesEqualObject(new TestSnapshot(List.of("hello", "beautiful", "world")));
        }
    }

    private void assertSerializationAndDeserializationProducesEqualObject(Object object) {
        assertThat((Object) serdeManager.deserialize(serdeManager.serialize(object))).isEqualTo(object);
    }
}
