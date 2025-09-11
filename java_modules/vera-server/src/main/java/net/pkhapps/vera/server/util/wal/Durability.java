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

/// Durability modes available when writing a record to a [WriteAheadLog].
///
/// These modes control when (and if) the operating system is forced to flush its page cache to stable storage. The
/// choice is a tradeoff between write latency and the risk of data loss in the event of a crash or power failure.
public enum Durability {
    /// The record is appended to the WAL but not explicitly flushed.
    ///
    /// Durability depends entirely on the operating system’s normal cache flush behavior. This provides the lowest
    /// latency and highest throughput, but the record may be lost if the machine crashes before the OS flushes
    /// buffers to disk.
    NONE,

    /// The record is appended and scheduled to be flushed as part of a future batch.
    ///
    /// This mode balances performance and safety by amortizing the cost of a flush across multiple records. Records
    /// written in this mode may be lost if a crash occurs before the batch is flushed.
    BATCHED,

    /// The record is appended and the WAL is immediately forced to stable storage before returning.
    ///
    /// This mode provides the strongest durability guarantee: once the write operation completes, the record is
    /// guaranteed to survive a crash. The tradeoff is significantly higher latency and lower throughput.
    IMMEDIATE
}
