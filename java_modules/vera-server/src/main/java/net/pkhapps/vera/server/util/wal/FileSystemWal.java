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

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/// Production-ready, thread-safe [WriteAheadLog] that uses the file system to store snapshots and events.
///
/// **Note:** All exceptions are considered non-recoverable and will result in an immediate application exit.
public class FileSystemWal extends AbstractWal implements AutoCloseable {

    private final Consumer<Exception> nonRecoverableErrorHandler = exception -> {
        log.error("Nonrecoverable error WAL error, existing application", exception);
        System.exit(1);
    };
    private final WalSerdeManager serdeManager;
    private final FileSystemWalMetadataStore metadataStore;
    private WalFile.WritableWalFile currentWalFile;

    /// Creates a new `FileSystemWal`.
    ///
    /// Files will be stored in the given `directory`. If it does not exist, it will be created.
    ///
    /// Callers must make sure that `walSerdeRegistrators` contains [WalSerde]s for every snapshot and event that will
    /// be stored in the WAL. Otherwise, an exception will occur when either appending to the WAL, taking a snapshot,
    /// or replaying the WAL. This will be considered a non-recoverable error and result in an immediate application
    /// exit.
    ///
    /// @param directory            the directory to store data in
    /// @param walSerdeRegistrators a collection of [WalSerdeRegistrator]s that will be used to serialize and deserialize [WalSnapshot]s and [WalEvent]s
    public FileSystemWal(Path directory, Iterable<WalSerdeRegistrator> walSerdeRegistrators) {
        serdeManager = new WalSerdeManager(walSerdeRegistrators);
        metadataStore = new FileSystemWalMetadataStore(directory);
        currentWalFile = WalFile.writable(metadataStore.latestWalFile(), metadataStore.firstRecordNumberOfLatestWalFile(), nonRecoverableErrorHandler);
    }

    @Override
    public <E extends WalEvent> void append(E event, Durability durability) {
        var serialized = serdeManager.serialize(event);
        synchronized (this) {
            try {
                currentWalFile.write(serialized.bytes(), serialized.offset(), serialized.length(), durability);
            } catch (Exception ex) {
                nonRecoverableErrorHandler.accept(ex);
            }
        }
    }

    @Override
    public synchronized void close() {
        currentWalFile.close();
    }

    @Override
    public synchronized void replay() {
        log.info("Starting replay");
        try {
            try (var snapshot = WalFile.readOnly(metadataStore.latestSnapshotFile())) {
                notifySnapshotReplayStart();
                snapshot.replayAll(walRecord -> {
                    WalSnapshot walSnapshot = serdeManager.deserialize(walRecord.payload(), 0, walRecord.payloadLength());
                    applySnapshot(walSnapshot);
                });
            }
            notifyEventReplayStart();
            metadataStore.forEachWalFileSinceLatestSnapshot(walFile -> {
                try (var wal = WalFile.readOnly(walFile)) {
                    wal.replayAll(walRecord -> {
                        WalEvent walEvent = serdeManager.deserialize(walRecord.payload(), 0, walRecord.payloadLength());
                        applyEvent(walEvent);
                    });
                }
            });
        } catch (Exception ex) {
            nonRecoverableErrorHandler.accept(ex);
        }
    }

    @Override
    public synchronized void takeSnapshot() {
        try {
            Path tempSnapshotFile = metadataStore.createTemporarySnapshotFile();
            log.info("Taking new snapshot into {}", tempSnapshotFile);
            var count = new AtomicInteger(0);
            try (var file = WalFile.writable(tempSnapshotFile, 1L, nonRecoverableErrorHandler)) {
                takeSnapshot(snapshot -> {
                    var serialized = serdeManager.serialize(snapshot);
                    file.write(serialized.bytes(), serialized.offset(), serialized.length(), Durability.NONE);
                    count.incrementAndGet();
                });
            }
            log.info("Stored {} snapshot entries in {}", count.get(), tempSnapshotFile);
            metadataStore.storeNewSnapshotFile(tempSnapshotFile, currentWalFile.getNextRecordNumber());
            currentWalFile.close();
            currentWalFile = WalFile.writable(metadataStore.latestWalFile(), metadataStore.firstRecordNumberOfLatestWalFile(), nonRecoverableErrorHandler);
        } catch (Exception ex) {
            nonRecoverableErrorHandler.accept(ex);
        }
    }
}
