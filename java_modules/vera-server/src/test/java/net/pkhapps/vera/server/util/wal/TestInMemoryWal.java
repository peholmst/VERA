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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/// In-memory implementation of [WriteAheadLog] intended to be used in unit tests.
public final class TestInMemoryWal extends AbstractWal {
    private static final Logger log = LoggerFactory.getLogger(TestInMemoryWal.class);

    private List<WalEvent> events = new LinkedList<>();
    private List<WalSnapshot> snapshot = new LinkedList<>();

    @Override
    public synchronized <E extends WalEvent> void append(E event, Durability durability) {
        log.debug("Appending event {}", event);
        events.add(event);
    }

    @Override
    public synchronized void takeSnapshot() {
        log.debug("Starting new snapshot");
        List<WalSnapshot> newSnapshot = new LinkedList<>();
        takeSnapshot(snapshot -> {
            log.debug("Adding snapshot entry {}", snapshot);
            newSnapshot.add(snapshot);
        });
        this.snapshot = newSnapshot;
        this.events = new LinkedList<>();
        log.debug("Snapshot completed");
    }

    @Override
    public synchronized void replay() {
        log.debug("Starting replay");
        notifySnapshotReplayStart();
        snapshot.forEach(snapshot -> {
            log.debug("Replaying snapshot {}", snapshot);
            applySnapshot(snapshot);
        });
        notifyEventReplayStart();
        events.forEach(event -> {
            log.debug("Replaying event {}", event);
            applyEvent(event);
        });
        log.debug("Replay completed");
    }
}
