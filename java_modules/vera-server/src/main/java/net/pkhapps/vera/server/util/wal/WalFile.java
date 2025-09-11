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

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.CRC32C;

/// Low-level class for accessing a single WAL file.
///
/// ### Record Format
///
/// A WAL file record has the following format (name, size in bytes):
///
/// `[header:24][payload:n]`
///
/// A WAL file record header has the following format (name, size in bytes):
///
/// `[magic:4][record number:8][checksum:8][length:4]`
///
/// * `magic`: always {@value #MAGIC}; acts as a marker for new records.
/// * `record number`: a long containing the record number. Record numbers increment by one, but the caller decides what the first record number is when creating a new file.
/// * `checksum`: a CRC32C checksum of `length`, `payload`, and `record number`.
/// * `length`: the length of the payload in bytes.
///
/// There is no particular logic behind the ordering of the fields in the header, except the magic number at the start.
///
/// ### Thread Safety
///
/// **This class does not perform any thread-locking at all.** Callers are expected to make sure the proper locks are in
/// order when clients are writing to, and reading from the WAL.
///
/// ### Usage
///
/// For reading only, use [#readOnly(Path)].
///
/// For writing and reading, use [#writable(Path, long)]
///
/// This class has package visibility because it is not intended to be used by clients.
sealed abstract class WalFile implements AutoCloseable {

    // This code is optimized for performance first, readability second.

    private static final Logger log = LoggerFactory.getLogger(WalFile.class);

    /// A magic constant used to mark the beginning of a new record in the WAL.
    static final int MAGIC = 0x57414C30;
    /// The size of the header in bytes
    static final int HEADER_SIZE = Integer.BYTES // Magic
            + Long.BYTES                         // Record number
            + Long.BYTES                         // Checksum
            + Integer.BYTES;                     // Payload length

    protected final Path file;

    protected WalFile(Path file) {
        this.file = file;
    }

    private static long calculateChecksum(byte[] payload, int payloadOffset, int payloadLength,
                                          long recordNumber) {
        var crc = new CRC32C();
        crc.update(payloadLength);
        crc.update(payload, payloadOffset, payloadLength);
        crc.update((int) (recordNumber >>> 56) & 0xFF);
        crc.update((int) (recordNumber >>> 48) & 0xFF);
        crc.update((int) (recordNumber >>> 40) & 0xFF);
        crc.update((int) (recordNumber >>> 32) & 0xFF);
        crc.update((int) (recordNumber >>> 24) & 0xFF);
        crc.update((int) (recordNumber >>> 16) & 0xFF);
        crc.update((int) (recordNumber >>> 8) & 0xFF);
        crc.update((int) (recordNumber) & 0xFF);
        return crc.getValue();
    }

    /// Replays all records in the WAL from start to end, calling the given `consumer` for each record.
    ///
    /// The consumer is called synchronously. If the replay needs to be fast, so does the consumer.
    ///
    /// @param consumer the consumer to call for each record
    /// @throws WriteAheadLogException if there is an error replaying the WAL
    public void replayAll(Consumer<WalRecord> consumer) {
        final long startTimeMillis = System.currentTimeMillis();
        log.info("Replaying all records");
        int recordsReplayed = 0;
        try (var readChannel = FileChannel.open(file, StandardOpenOption.READ)) {
            var scratch = new ScratchBuffer();
            while (true) {
                // Read record
                var record = tryReadRecord(readChannel, scratch);
                if (record == null) {
                    break; // clean EOF
                }

                // Pass record to consumer
                try {
                    consumer.accept(record);
                    recordsReplayed++;
                } catch (Exception ex) {
                    log.debug("Error consuming record {}", record.recordNumber, ex);
                    throw new WalConsumerException(ex);
                }
            }
        } catch (IOException ex) {
            log.error("Error reading file", ex);
            throw new WalIOException("Error reading file", ex);
        } finally {
            log.info("Replayed {} record(s) in {} ms", recordsReplayed, System.currentTimeMillis() - startTimeMillis);
        }
    }

    private static @Nullable WalRecord tryReadRecord(FileChannel channel, ScratchBuffer scratchBuffer) {
        try {
            return tryReadRecord(channel, channel.position(), scratchBuffer);
        } catch (IOException ex) {
            log.error("Error reading file", ex);
            throw new WalIOException("Error reading file", ex);
        }
    }

    private static @Nullable WalRecord tryReadRecord(FileChannel channel, long position, ScratchBuffer scratch) {
        try {
            channel.position(position);

            // Read header
            byte[] header = scratch.ensureCapacity(HEADER_SIZE);
            var headerBuf = ByteBuffer.wrap(header, 0, HEADER_SIZE);
            var readHeaderBytes = readFully(channel, headerBuf);
            if (readHeaderBytes < headerBuf.limit()) {
                if (readHeaderBytes > 0) {
                    log.warn("Incomplete header at file position {}", position);
                } // Otherwise, we're just at the end of the file.
                return null;
            }
            headerBuf.flip();

            if (MAGIC != headerBuf.getInt()) {
                log.error("Incorrect magic number at file position {}", position);
                throw new WalCorruptionException("Incorrect magic number");
            }

            var recordNumber = headerBuf.getLong();
            var checksum = headerBuf.getLong();
            var payloadLength = headerBuf.getInt();

            // Read payload
            byte[] payload = scratch.ensureCapacity(payloadLength);
            var payloadBuf = ByteBuffer.wrap(payload, 0, payloadLength);
            if (readFully(channel, payloadBuf) < payloadBuf.limit()) {
                log.error("Incomplete payload at file position {}", position);
                return null;
            }

            // Verify checksum
            var actualChecksum = calculateChecksum(payload, 0, payloadLength, recordNumber);
            if (actualChecksum != checksum) {
                log.error("Checksum mismatch in record {} at file position {}. Expected checksum {}, actual was {}",
                        recordNumber, position, checksum, actualChecksum);
                throw new WalCorruptionException("Checksum mismatch");
            }

            return new WalRecord(payload, payloadLength, recordNumber);
        } catch (IOException ex) {
            log.error("Error reading file", ex);
            throw new WalIOException("Error reading file", ex);
        }
    }

    private static int readFully(FileChannel fileChannel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (fileChannel.read(buffer) < 0) {
                break;
            }
        }
        return buffer.position();
    }

    /// Provides read-only access to a WAL file.
    static final class ReadOnlyWalFile extends WalFile {

        private ReadOnlyWalFile(Path file) {
            super(file);
            // TODO Check that the file exists and is readable
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }

    /// Creates a new `WalFile` for reading only.
    ///
    /// @param file the WAL file to read from
    /// @throws WriteAheadLogException if the file does not exist or is not readable
    public static ReadOnlyWalFile readOnly(Path file) {
        return new ReadOnlyWalFile(file);
    }

    /// Provides both read and write access to a WAL file.
    static final class WritableWalFile extends WalFile {

        private final FileChannel fileChannel;
        private long nextRecordNumber;
        private final ScratchBuffer scratch = new ScratchBuffer();
        private final WalFlusher walFlusher;

        private WritableWalFile(Path file, long defaultNextRecordNumber, Consumer<IOException> walFlusherExceptionHandler) {
            super(file);
            log.info("Opening file {} for writing", file);

            try {
                var lastRecordNumber = readLastRecordNumber(file);
                if (lastRecordNumber < 0) {
                    nextRecordNumber = defaultNextRecordNumber;
                } else {
                    nextRecordNumber = lastRecordNumber + 1;
                }
                log.debug("Next record number: {}", nextRecordNumber);
            } catch (Exception ex) {
                log.error("Error reading last record number", ex);
                throw new WalIOException("Error reading last record number", ex);
            }

            try {
                fileChannel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                walFlusher = new WalFlusher(fileChannel, walFlusherExceptionHandler);
            } catch (IOException ex) {
                log.error("Error opening file {}", file, ex);
                throw new WalIOException("Error opening file", ex);
            }
        }

        private static long readLastRecordNumber(Path file) throws IOException {
            try (var channel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                var size = channel.size();
                var pos = size - Integer.BYTES;
                var magicBuf = ByteBuffer.allocate(Integer.BYTES);
                var scratch = new ScratchBuffer();

                while (pos >= 0) {
                    channel.position(pos);
                    channel.read(magicBuf);
                    magicBuf.flip();
                    if (magicBuf.getInt() == MAGIC) {
                        try {
                            var rec = WalFile.tryReadRecord(channel, pos, scratch);
                            if (rec != null) {
                                // Valid record found
                                var validSize = pos + rec.sizeOnDisk();
                                if (validSize < size) {
                                    log.warn("Truncating file {} to {} bytes because of corruption in last record", file, validSize);
                                    channel.truncate(pos + rec.sizeOnDisk());
                                    channel.force(false);
                                }
                                return rec.recordNumber();
                            }
                        } catch (WalCorruptionException _) {
                            // Not a valid record, keep scanning back
                        }
                    }
                    magicBuf.clear();
                    pos--;
                }
                log.warn("Found no valid records in file {}, truncating to 0", file);
                channel.truncate(0);
                channel.force(false);
            } catch (NoSuchFileException ex) {
                log.debug("File {} is empty", file);
            }
            return -1;
        }

        /// Writes the given payload to the WAL in a new record.
        ///
        /// @param payload    the payload itself
        /// @param durability the durability of the write operation
        /// @return the number of the written record
        public long write(byte[] payload, Durability durability) {
            return write(payload, 0, payload.length, durability);
        }

        /// Writes the given payload to the WAL in a new record.
        ///
        /// @param payload       an array containing the payload
        /// @param payloadOffset the offset of the payload inside the array
        /// @param payloadLength the length of the payload
        /// @param durability    the durability of the write operation
        /// @return the number of the written record
        public long write(byte[] payload, int payloadOffset, int payloadLength, Durability durability) {
            tryWriteRecord(fileChannel, payload, payloadOffset, payloadLength, nextRecordNumber, durability, scratch);
            return nextRecordNumber++;
        }

        /// Visible for testing, would otherwise be private.
        static ByteBuffer writeRecord(byte[] payload, int payloadLength, long recordNumber) {
            return writeRecord(payload, 0, payloadLength, recordNumber, new ScratchBuffer());
        }

        private static ByteBuffer writeRecord(byte[] payload, int payloadOffset, int payloadLength, long recordNumber, ScratchBuffer scratch) {
            var size = HEADER_SIZE   // Header
                    + payloadLength;  // Payload
            var buffer = ByteBuffer.wrap(scratch.ensureCapacity(size));
            var checksum = calculateChecksum(payload, payloadOffset, payloadLength, recordNumber);

            buffer.putInt(MAGIC);
            buffer.putLong(recordNumber);
            buffer.putLong(checksum);
            buffer.putInt(payload.length);
            buffer.put(payload, payloadOffset, payloadLength);
            buffer.flip();
            return buffer;
        }

        private void tryWriteRecord(FileChannel channel, byte[] payload, int payloadOffset, int payloadLength, long recordNumber, Durability durability, ScratchBuffer scratch) {
            var buffer = writeRecord(payload, payloadOffset, payloadLength, recordNumber, scratch);
            try {
                while (buffer.hasRemaining()) {
                    //noinspection ResultOfMethodCallIgnored
                    channel.write(buffer);
                }
                if (durability == Durability.IMMEDIATE) {
                    channel.force(false);
                } else if (durability == Durability.BATCHED) {
                    walFlusher.requestFlush();
                }
            } catch (IOException ex) {
                log.error("Error writing payload to file", ex);
                throw new WalIOException("Error writing payload to file", ex);
            }
        }

        /// Returns the number that the next written record will get.
        ///
        /// @return the next record number
        public long getNextRecordNumber() {
            return nextRecordNumber;
        }

        @Override
        public void close() {
            walFlusher.close();
            try {
                fileChannel.close();
            } catch (IOException ex) {
                log.error("Error closing file", ex);
                throw new WalIOException("Error closing file", ex);
            }

        }
    }

    private static final class WalFlusher implements AutoCloseable {

        private final FileChannel channel;
        private final Consumer<IOException> exceptionHandler;
        private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        private final Thread flusherThread;
        private volatile boolean running = true;

        WalFlusher(FileChannel channel, Consumer<IOException> exceptionHandler) {
            this.channel = channel;
            this.exceptionHandler = exceptionHandler;
            this.flusherThread = Thread.ofVirtual().start(this::run);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        void requestFlush() {
            queue.offer(new Object());
        }

        private void run() {
            log.debug("Starting WalFlusher thread");
            while (running) {
                try {
                    var ignored = queue.poll(5, TimeUnit.MILLISECONDS);
                    if (ignored != null || !queue.isEmpty()) {
                        channel.force(false);
                        queue.clear();
                    }
                } catch (IOException ex) {
                    log.error("WAL flush failed", ex);
                    exceptionHandler.accept(ex);
                } catch (InterruptedException ex) {
                    break;
                }
            }
            log.debug("Performing final WAL flush");
            try {
                channel.force(false);
            } catch (IOException ex) {
                log.error("Final WAL flush failed", ex);
                exceptionHandler.accept(ex);
            }
        }

        @Override
        public void close() {
            log.debug("Stopping WalFlusher thread");
            running = false;
            try {
                flusherThread.join();
            } catch (InterruptedException ex) {
                log.error("Interrupted while shutting down WAL flusher", ex);
            }
        }
    }

    /// Creates a new `WalFile` for both writing and reading.
    ///
    /// If the given `file` does not exist, it is created. In this case, or if the file is empty, the record number of
    /// the first record becomes the given `defaultNextRecordNumber`.
    /// If the file exists, the next record number is read from the file itself.
    ///
    /// @param file                       the WAL file to write to and read from
    /// @param defaultNextRecordNumber    the record number to use for the first record if the file is empty
    /// @param walFlusherExceptionHandler an exception handler for I/O errors occurring in the WAL flusher background thread
    /// @throws WriteAheadLogException if the file cannot be opened for writing or created
    public static WritableWalFile writable(Path file, long defaultNextRecordNumber, Consumer<IOException> walFlusherExceptionHandler) {
        return new WritableWalFile(file, defaultNextRecordNumber, walFlusherExceptionHandler);
    }

    /// Record for consumers replaying a WAL.
    ///
    /// **Note:** The `payload` array may be a reusable buffer, which means its contents may change for each record
    /// being consumed. Because of this, the given `payloadLength` should be used, as the length of the array may be
    /// larger than the actual payload.
    ///
    /// *The `payload` array must not be referenced outside the consumer!* Consumers should instead either process the
    /// data directly, or copy the payload into another array for later processing.
    ///
    /// @param payload       an array containing the payload
    /// @param payloadLength the length of the payload in bytes
    /// @param recordNumber  the record number
    record WalRecord(byte[] payload, int payloadLength, long recordNumber) {

        long sizeOnDisk() {
            return HEADER_SIZE        // Header
                    + payloadLength;   // Payload
        }
    }

    private static class ScratchBuffer {
        private byte @Nullable [] buffer = null;

        byte[] ensureCapacity(int length) {
            if (buffer == null || buffer.length < length) {
                buffer = new byte[length];
            }
            return buffer;
        }
    }
}
