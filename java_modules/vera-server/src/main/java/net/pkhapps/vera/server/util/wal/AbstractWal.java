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
import java.util.List;
import java.util.function.Predicate;

/// Base class for [WriteAheadLog] and [WriteAheadLogControl] implementations.
///
/// This class takes care of registering consumers and producers, while leaving the actual snapshot and event I/O
/// to subclasses.
public abstract class AbstractWal implements WriteAheadLog, WriteAheadLogControl {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final List<EventConsumerEntry<?>> eventConsumers = new ArrayList<>();
    private final List<SnapshotConsumerEntry<?>> snapshotConsumers = new ArrayList<>();
    private final List<SnapshotProducerEntry<?>> snapshotProducers = new ArrayList<>();

    @Override
    public final synchronized <E extends WalEvent> Registration registerEventConsumer(Class<E> eventType, Predicate<? super E> eventFilter, EventConsumer<? super E> eventConsumer) {
        var entry = new EventConsumerEntry<E>(eventType, eventFilter, eventConsumer);
        log.debug("Registering event consumer {} for event type {} under entry {}", eventConsumer, eventType, entry);
        eventConsumers.add(entry);
        return () -> {
            synchronized (AbstractWal.this) {
                log.debug("Removing event consumer {}", entry);
                eventConsumers.remove(entry);
            }
        };
    }

    @Override
    public final synchronized <S extends WalSnapshot> Registration registerSnapshotConsumer(Class<S> snapshotType, Predicate<? super S> snapshotFilter, SnapshotConsumer<? super S> snapshotConsumer) {
        var entry = new SnapshotConsumerEntry<S>(snapshotType, snapshotFilter, snapshotConsumer);
        log.debug("Registering snapshot consumer {} for snapshot type {} under entry {}", snapshotConsumer, snapshotType, entry);
        snapshotConsumers.add(entry);
        return () -> {
            synchronized (AbstractWal.this) {
                log.debug("Removing snapshot consumer {}", entry);
                snapshotConsumers.remove(entry);
            }
        };
    }

    @Override
    public final synchronized <S extends WalSnapshot> Registration registerSnapshotProducer(SnapshotProducer<S> snapshotProducer) {
        var entry = new SnapshotProducerEntry<S>(snapshotProducer);
        log.debug("Registering snapshot producer {} under entry {}", snapshotProducer, entry);
        snapshotProducers.add(entry);
        return () -> {
            synchronized (AbstractWal.this) {
                log.debug("Removing snapshot producer {}", entry);
                snapshotProducers.remove(entry);
            }
        };
    }

    /// Asks all registered [net.pkhapps.vera.server.util.wal.WriteAheadLog.SnapshotProducer]s to write their entries
    /// to the given `snapshotWriter`.
    ///
    /// @param snapshotWriter the writer to write snapshot entries to
    protected final void takeSnapshot(SnapshotWriter<WalSnapshot> snapshotWriter) {
        snapshotProducers.forEach(snapshotProducerEntry -> snapshotProducerEntry.tryWrite(snapshotWriter));
    }

    /// Notifies all [net.pkhapps.vera.server.util.wal.WriteAheadLog.SnapshotConsumer]s that a snapshot replay is about
    /// to start.
    ///
    /// @see SnapshotConsumer#onSnapshotReplayStart()
    protected final void notifySnapshotReplayStart() {
        snapshotConsumers.forEach(SnapshotConsumerEntry::onReplayStart);
    }

    /// Applies the given `snapshot` to all eligible [net.pkhapps.vera.server.util.wal.WriteAheadLog.SnapshotConsumer]s.
    ///
    /// @param snapshot the snapshot to apply
    protected final void applySnapshot(WalSnapshot snapshot) {
        snapshotConsumers.forEach(snapshotConsumerEntry -> snapshotConsumerEntry.tryApply(snapshot));
    }

    /// Notifies all [net.pkhapps.vera.server.util.wal.WriteAheadLog.EventConsumer]s that an event replay is about to
    /// start.
    ///
    /// @see EventConsumer#onEventReplayStart()
    protected final void notifyEventReplayStart() {
        eventConsumers.forEach(EventConsumerEntry::onReplayStart);
    }

    /// Applies the given `event` to all eligible [net.pkhapps.vera.server.util.wal.WriteAheadLog.EventConsumer]s.
    ///
    /// @param event the WAL event to apply
    protected final void applyEvent(WalEvent event) {
        eventConsumers.forEach(eventConsumerEntry -> eventConsumerEntry.tryApply(event));
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
