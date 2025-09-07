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

/// Interface for controlling an application-scoped write-ahead-log.
///
/// Implementations typically implement both this interface and [WriteAheadLog]. However, clients of the WAL should only
/// access it through the [WriteAheadLog] interface.
public interface WriteAheadLogControl {

    /// Replays the WAL. This method never changes the state of the WAL.
    ///
    /// @throws WalConsumerException   if an event consumer or snapshot consumer throws an exception
    /// @throws WalIOException         if an I/O error occurs while reading the log
    /// @throws WalCorruptionException if the WAL is corrupted
    void replay();

    /// Takes a snapshot. This method is atomic, meaning it won't write anything to the WAL unless it completes successfully.
    ///
    /// @throws WalSnapshotProducerException if a snapshot producer throws an exception
    /// @throws WalIOException               if an I/O error occurs while writing the snapshot
    /// @throws WalStateException            if the WAL is read-only
    void takeSnapshot();
}
