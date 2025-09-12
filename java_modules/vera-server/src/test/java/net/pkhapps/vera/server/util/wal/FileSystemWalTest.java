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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemWalTest {

    @Test
    void append_and_replay_events_without_snapshot() throws IOException {
        var eventsToAdd = new ArrayList<WalEvent>();
        for (int i = 0; i < 100; ++i) {
            eventsToAdd.add(new TestEvent.MyFirstEvent("Hello World", 123));
            eventsToAdd.add(new TestEvent.MySecondEvent(Instant.now(), UUID.randomUUID()));
            eventsToAdd.add(new TestEvent.MyThirdEvent(987L, true));
        }
        var eventsReplayed = new ArrayList<WalEvent>();
        try (var wal = new FileSystemWal(Files.createTempDirectory("file-system-wal-test"), List.of(
                walRegistry -> {
                    walRegistry.registerWalSerde(new TestEventSerde(100));
                    walRegistry.registerWalSerde(new TestSnapshotSerde(200));
                }
        ))) {
            wal.registerEventConsumer(TestEvent.class, testEvent -> true, eventsReplayed::add);
            eventsToAdd.forEach(event -> wal.append(event, Durability.BATCHED));
            wal.replay();
        }
        assertThat(eventsReplayed).containsAll(eventsToAdd);
    }

    @Test
    void append_snapshot_and_replay() throws IOException {
        var eventsToAddBeforeSnapshot = new ArrayList<WalEvent>();
        var eventsToAddAfterSnapshot = new ArrayList<WalEvent>();
        for (int i = 0; i < 100; ++i) {
            eventsToAddBeforeSnapshot.add(new TestEvent.MyFirstEvent("Hello World", 123));
            eventsToAddAfterSnapshot.add(new TestEvent.MySecondEvent(Instant.now(), UUID.randomUUID()));
        }
        var snapshotsToAdd = new ArrayList<WalSnapshot>();
        for (int i = 0; i < 100; ++i) {
            snapshotsToAdd.add(new TestSnapshot(List.of("this is entry " + i)));
        }

        var eventsReplayed = new ArrayList<WalEvent>();
        var snapshotsReplayed = new ArrayList<WalSnapshot>();
        try (var wal = new FileSystemWal(Files.createTempDirectory("file-system-wal-test"), List.of(
                walRegistry -> {
                    walRegistry.registerWalSerde(new TestEventSerde(100));
                    walRegistry.registerWalSerde(new TestSnapshotSerde(200));
                }
        ))) {
            wal.registerEventConsumer(TestEvent.class, testEvent -> true, eventsReplayed::add);
            wal.registerSnapshotProducer(writer -> snapshotsToAdd.forEach(writer::write));
            wal.registerSnapshotConsumer(TestSnapshot.class, testSnapshot -> true, snapshotsReplayed::add);

            eventsToAddBeforeSnapshot.forEach(event -> wal.append(event, Durability.BATCHED));
            wal.takeSnapshot();
            eventsToAddAfterSnapshot.forEach(event -> wal.append(event, Durability.BATCHED));
            wal.replay();
        }
        assertThat(snapshotsReplayed).containsAll(snapshotsToAdd);
        assertThat(eventsReplayed).containsAll(eventsToAddAfterSnapshot);
    }
}
