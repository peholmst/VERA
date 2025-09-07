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

import net.pkhapps.vera.server.util.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/// In-memory implementation of [WriteAheadLog] intended to be used in unit tests.
public class TestInMemoryWal implements WriteAheadLog, WriteAheadLogControl {
    private static final Logger log = LoggerFactory.getLogger(TestInMemoryWal.class);

    private List<WalEvent> events = new LinkedList<>();
    private List<WalSnapshot> snapshot = new LinkedList<>();
    private final List<EventConsumerEntry<?>> eventConsumers = new ArrayList<>();
    private final List<SnapshotConsumerEntry<?>> snapshotConsumers = new ArrayList<>();
    private final List<SnapshotProducerEntry<?>> snapshotProducers = new ArrayList<>();

    public synchronized <E extends WalEvent> void append(E event) {
        log.debug("Appending event {}", event);
        events.add(event);
    }

    @Override
    public synchronized <E extends WalEvent> Registration registerEventConsumer(Class<E> eventType, Predicate<? super E> eventFilter, EventConsumer<? super E> eventConsumer) {
        var entry = new EventConsumerEntry<E>(eventType, eventFilter, eventConsumer);
        log.debug("Registering event consumer {} for event type {} under entry {}", eventConsumer, eventType, entry);
        eventConsumers.add(entry);
        return () -> {
            synchronized (TestInMemoryWal.this) {
                log.debug("Removing event consumer {}", entry);
                eventConsumers.remove(entry);
            }
        };
    }

    @Override
    public synchronized <S extends WalSnapshot> Registration registerSnapshotConsumer(Class<S> snapshotType, Predicate<? super S> snapshotFilter, SnapshotConsumer<? super S> snapshotConsumer) {
        var entry = new SnapshotConsumerEntry<S>(snapshotType, snapshotFilter, snapshotConsumer);
        log.debug("Registering snapshot consumer {} for snapshot type {} under entry {}", snapshotConsumer, snapshotType, entry);
        snapshotConsumers.add(entry);
        return () -> {
            synchronized (TestInMemoryWal.this) {
                log.debug("Removing snapshot consumer {}", entry);
                snapshotConsumers.remove(entry);
            }
        };
    }

    @Override
    public synchronized <S extends WalSnapshot> Registration registerSnapshotProducer(SnapshotProducer<S> snapshotProducer) {
        var entry = new SnapshotProducerEntry<S>(snapshotProducer);
        log.debug("Registering snapshot producer {} under entry {}", snapshotProducer, entry);
        snapshotProducers.add(entry);
        return () -> {
            synchronized (TestInMemoryWal.this) {
                log.debug("Removing snapshot producer {}", entry);
                snapshotProducers.remove(entry);
            }
        };
    }

    @Override
    public synchronized void takeSnapshot() {
        log.debug("Starting new snapshot");
        List<WalSnapshot> newSnapshot = new LinkedList<>();
        snapshotProducers.forEach(snapshotProducer -> snapshotProducer.tryWrite(snapshot -> {
            log.debug("Adding snapshot entry {}", snapshot);
            newSnapshot.add(snapshot);
        }));
        this.snapshot = newSnapshot;
        this.events = new LinkedList<>();
        log.debug("Snapshot completed");
    }

    @Override
    public synchronized void replay() {
        log.debug("Starting replay");
        snapshotConsumers.forEach(SnapshotConsumerEntry::onReplayStart);
        snapshot.forEach(snapshot -> {
            log.debug("Replaying snapshot {}", snapshot);
            snapshotConsumers
                    .forEach(snapshotConsumer -> snapshotConsumer.tryApply(snapshot));
        });
        eventConsumers.forEach(EventConsumerEntry::onReplayStart);
        events.forEach(event -> {
            log.debug("Replaying event {}", event);
            eventConsumers
                    .forEach(eventConsumer -> eventConsumer.tryApply(event));
        });
        log.debug("Replay completed");
    }

    private record SnapshotProducerEntry<S extends WalSnapshot>(SnapshotProducer<S> producer) {

        @SuppressWarnings("unchecked")
        public void tryWrite(SnapshotWriter<WalSnapshot> consumer) {
            producer.createSnapshot((SnapshotWriter<S>) consumer);
        }
    }

    private record EventConsumerEntry<E extends WalEvent>(Class<E> type, Predicate<? super E> filter,
                                                          EventConsumer<? super E> consumer) {

        public void onReplayStart() {
            consumer.onEventReplayStart();
        }

        @SuppressWarnings("unchecked")
        public void tryApply(WalEvent data) {
            if (type.isAssignableFrom(data.getClass()) && filter.test((E) data)) {
                try {
                    consumer.applyEvent((E) data);
                } catch (Exception ex) {
                    throw new WalConsumerException(ex);
                }
            }
        }
    }

    private record SnapshotConsumerEntry<E extends WalSnapshot>(Class<E> type, Predicate<? super E> filter,
                                                                SnapshotConsumer<? super E> consumer) {

        public void onReplayStart() {
            consumer.onSnapshotReplayStart();
        }

        @SuppressWarnings("unchecked")
        public void tryApply(WalSnapshot data) {
            if (type.isAssignableFrom(data.getClass()) && filter.test((E) data)) {
                try {
                    consumer.applySnapshot((E) data);
                } catch (Exception ex) {
                    throw new WalConsumerException(ex);
                }
            }
        }
    }
}
