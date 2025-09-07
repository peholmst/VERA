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

import java.util.function.Predicate;

/// Interface for an application-scoped write-ahead-log.
///
/// General principles:
/// - Any component can append events to it, which will be written into the log serially.
/// - Replays and snapshots are handled centrally by the WAL implementation.
/// - Any component can register itself as an event consumer. When the log is replayed, the consumer receives a callback.
/// - Any component can register itself as a snapshot consumer. When the log is replayed, the consumer receives a callback.
/// - Any component can register itself as a snapshot producer. When the log decides to make a snapshot, the producer receives a callback.
public interface WriteAheadLog {

    /// Appends the given event to the WAL.
    ///
    /// @param event the event to append
    /// @param <E>   the type of event to append
    /// @throws WriteAheadLogException if the event could not be written
    <E extends WalEvent> void append(E event);

    /// Registers the given consumer to be called when the WAL is replayed.
    ///
    /// Only events that are assignable to the specified event type and pass the specified filter are sent to the
    /// consumer.
    ///
    /// Events are replayed in a single thread. Because of this, consumers must be *fast*.
    /// Any exception thrown by the consumer will stop the replay and be rethrown inside a [WalConsumerException].
    ///
    /// @param eventType     the type of events to consume
    /// @param eventFilter   a filter that further narrows down the events to consume
    /// @param eventConsumer the consumer that will receive the events
    /// @return a `Registration` handle for removing the registration
    <E extends WalEvent> Registration registerEventConsumer(Class<E> eventType, Predicate<? super E> eventFilter, EventConsumer<? super E> eventConsumer);

    /// Registers the given consumer to be called when the WAL is replayed and contains snapshots.
    ///
    /// Only snapshots that are assignable to the specified snapshot type and pass the specified filter are sent to the
    /// consumer.
    ///
    /// Snapshots are replayed in a single thread. Because of this, consumers must be *fast*.
    /// Any exception thrown by the consumer will stop the replay and be rethrown inside a [WalConsumerException].
    ///
    /// @param snapshotType     the type of snapshots to consume
    /// @param snapshotFilter   a filter that further narrows down the snapshots to consume
    /// @param snapshotConsumer the consumer that will receive the snapshots
    /// @return a `Registration` handle for removing the registration
    <S extends WalSnapshot> Registration registerSnapshotConsumer(Class<S> snapshotType, Predicate<? super S> snapshotFilter, SnapshotConsumer<? super S> snapshotConsumer);

    /// Registers the given producer to be called when the WAL is creating a new snapshot.
    ///
    /// When the WAL creates a snapshot, the producer will be called. The producer can write as many snapshot entries as
    /// it wants, and they will all be stored in the WAL. Example:
    ///
    /// ```
    /// private void createSnapshot(SnapshotWriter<MyWalSnapshot> snapshotWriter){
    ///     snapshotWriter.write(new MyWalSnapshot(myState));
    ///}
    ///// ...
    /// wal.registerSnapshotProducer(this::createSnapshot);
    ///```
    /// Snapshots are created in a single thread. Because of this, producers must be *fast*.
    /// Any exception thrown by the producer will stop the snapshot and be rethrown inside a [WalSnapshotProducerException]. This effectively
    /// invalidates the entire snapshot.
    ///
    /// @param snapshotProducer the producer that will be called when the WAL creates a snapshot
    /// @return a `Registration` handle for removing the registration
    <S extends WalSnapshot> Registration registerSnapshotProducer(SnapshotProducer<S> snapshotProducer);

    /// Functional interface for event consumers.
    ///
    /// @param <E> the type of event to replay
    @FunctionalInterface
    interface EventConsumer<E extends WalEvent> {
        /// Called when the event replay starts.
        default void onEventReplayStart() {
        }

        /// Replays the given event.
        ///
        /// @param event the event to apply
        void applyEvent(E event);
    }

    /// Functional interface for snapshot consumers.
    ///
    /// @param <S> the type of snapshot entry to replay
    @FunctionalInterface
    interface SnapshotConsumer<S extends WalSnapshot> {
        /// Called when the snapshot replay starts.
        default void onSnapshotReplayStart() {
        }

        /// Replays the given snapshot entry.
        ///
        /// @param snapshot the snapshot entry to apply.
        void applySnapshot(S snapshot);
    }

    /// Functional interface for snapshot producers.
    ///
    /// @param <S> the type of snapshot entry to write
    @FunctionalInterface
    interface SnapshotProducer<S extends WalSnapshot> {
        /// Writes one or more snapshot entries to the given writer.
        ///
        /// @param snapshotWriter the writer to write snapshots to.
        void createSnapshot(SnapshotWriter<S> snapshotWriter);
    }

    /// Functional interface for snapshot writers.
    ///
    /// @param <S> the type of snapshot entry to write
    @FunctionalInterface
    interface SnapshotWriter<S extends WalSnapshot> {
        /// Writes the given snapshot entry to the WAL.
        ///
        /// @param snapshot the snapshot entry to write
        /// @throws WriteAheadLogException if the entry could not be written
        void write(S snapshot);
    }
}
