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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.zip.CRC32C;

/// Low-level class for accessing a single WAL file.
///
/// ### Record Format
///
/// A WAL file record has the following format (name, size in bytes):
///
/// `[header:20][payload:n][record number:8]`
///
/// The record number is at the end to make it easier to find the latest record number when opening an existing file
/// for writing.
///
/// A WAL file record header has the following format (name, size in bytes):
///
/// `[magic:4][checksum:8][type:4][length:4]`
///
/// * `magic`: always {@value #MAGIC}; acts as a marker for new records.
/// * `checksum`: a CRC32C checksum of `type`, `length`, `payload`, and `record number`.
/// * `type`: an integer indicating the type of the payload. The caller decides what integers to use.
/// * `length`: the length of the payload in bytes.
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

    protected final Path file;

    protected WalFile(Path file) {
        this.file = file;
    }

    private static long calculateChecksum(int payloadTypeId, byte[] payload, int payloadOffset, int payloadLength,
                                          long recordNumber) {
        var crc = new CRC32C();
        crc.update(payloadTypeId);
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

    private static WalRecord tryReadRecord(FileChannel channel, ScratchBuffer scratchBuffer) {
        try {
            return tryReadRecord(channel, channel.position(), scratchBuffer);
        } catch (IOException ex) {
            log.error("Error reading file", ex);
            throw new WalIOException("Error reading file", ex);
        }
    }

    private static WalRecord tryReadRecord(FileChannel channel, long position, ScratchBuffer scratch) {
        try {
            channel.position(position);

            // Read header
            var headerBuf = ByteBuffer.allocate(
                    Integer.BYTES           // Magic
                            + Long.BYTES    // Checksum
                            + Integer.BYTES // Type ID
                            + Integer.BYTES // Payload length
            );
            if (readFully(channel, headerBuf) < headerBuf.capacity()) {
                log.warn("Incomplete header at file position {}", position);
                return null;
            }
            headerBuf.flip();

            if (MAGIC != headerBuf.getInt()) {
                log.error("Incorrect magic number at file position {}", position);
                throw new WalCorruptionException("Incorrect magic number");
            }

            var checksum = headerBuf.getLong();
            var payloadTypeId = headerBuf.getInt();
            var payloadLength = headerBuf.getInt();

            // Read payload
            byte[] payload = scratch.ensureCapacity(payloadLength);
            var payloadBuf = ByteBuffer.wrap(payload, 0, payloadLength);
            if (readFully(channel, payloadBuf) < 0) {
                log.error("Incomplete payload at file position {}", position);
                return null;
            }

            // Read record number
            var recordNumberBuf = ByteBuffer.allocate(Long.BYTES);
            if (readFully(channel, recordNumberBuf) < 0) {
                log.error("Incomplete record number at file position {}", position);
                return null;
            }
            recordNumberBuf.flip();
            var recordNumber = recordNumberBuf.getLong();

            // Verify checksum
            var actualChecksum = calculateChecksum(payloadTypeId, payload, 0, payloadLength, recordNumber);
            if (actualChecksum != checksum) {
                log.error("Checksum mismatch in record {} at file position {}. Expected checksum {}, actual was {}",
                        recordNumber, position, checksum, actualChecksum);
                throw new WalCorruptionException("Checksum mismatch");
            }

            return new WalRecord(payloadTypeId, payload, payloadLength, recordNumber);
        } catch (IOException ex) {
            log.error("Error reading file", ex);
            throw new WalIOException("Error reading file", ex);
        }
    }

    private static int readFully(FileChannel fileChannel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (fileChannel.read(buffer) < 0) {
                return -1;
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

        private WritableWalFile(Path file, long defaultNextRecordNumber) {
            super(file);
            log.info("Opening file {} for writing", file);
            try {
                fileChannel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ex) {
                log.error("Error opening file {}", file, ex);
                throw new WalIOException("Error opening file", ex);
            }

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
                close();
                throw new WalIOException("Error reading last record number", ex);
            }
        }

        private long readLastRecordNumber(Path file) throws IOException {
            // TODO Add support for corrupt last record!
            try (var readChannel = FileChannel.open(file, StandardOpenOption.READ)) {
                var size = readChannel.size();
                if (size == 0) {
                    log.debug("File {} is empty", file);
                    return -1;
                } else {
                    log.debug("File {} is {} bytes, reading last record number", file, size);
                    var buffer = ByteBuffer.allocate(Long.BYTES);
                    readChannel.read(buffer, size - Long.BYTES);
                    buffer.flip();
                    return buffer.getLong();
                }
            }
        }

        /// Writes the given payload to the WAL in a new record.
        ///
        /// @param payloadTypeId the type of the payload
        /// @param payload       the payload itself
        /// @return the number of the written record
        public long write(int payloadTypeId, byte[] payload) {
            return write(payloadTypeId, payload, 0, payload.length);
        }

        /// Writes the given payload to the WAL in a new record.
        ///
        /// @param payloadTypeId the type of the payload
        /// @param payload       an array containing the payload
        /// @param payloadOffset the offset of the payload inside the array
        /// @param payloadLength the length of the payload
        /// @return the number of the written record
        public long write(int payloadTypeId, byte[] payload, int payloadOffset, int payloadLength) {
            var size = Integer.BYTES  // Magic
                    + Long.BYTES      // Checksum
                    + Integer.BYTES   // Type ID
                    + Integer.BYTES   // Payload length
                    + payloadLength   // Payload
                    + Long.BYTES;     // Record number
            var buffer = ByteBuffer.allocate(size);
            var recordNumber = nextRecordNumber;
            try {
                var checksum = calculateChecksum(payloadTypeId, payload, payloadOffset, payloadLength, recordNumber);

                buffer.putInt(MAGIC);
                buffer.putLong(checksum);
                buffer.putInt(payloadTypeId);
                buffer.putInt(payload.length);
                buffer.put(payload, payloadOffset, payloadLength);
                buffer.putLong(recordNumber);
                buffer.flip();

                while (buffer.hasRemaining()) {
                    //noinspection ResultOfMethodCallIgnored
                    fileChannel.write(buffer);
                }
                fileChannel.force(false);
                nextRecordNumber++;
                return recordNumber;
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
            try {
                fileChannel.close();
            } catch (IOException ex) {
                log.error("Error closing file", ex);
                throw new WalIOException("Error closing file", ex);
            }
        }
    }

    /// Creates a new `WalFile` for both writing and reading.
    ///
    /// If the given `file` does not exist, it is created. In this case, or if the file is empty, the record number of
    /// the first record becomes the given `defaultNextRecordNumber`.
    /// If the file exists, the next record number is read from the file itself.
    ///
    /// @param file                    the WAL file to write to and read from
    /// @param defaultNextRecordNumber the record number to use for the first record if the file is empty
    /// @throws WriteAheadLogException if the file cannot be opened for writing or created
    public static WritableWalFile writable(Path file, long defaultNextRecordNumber) {
        return new WritableWalFile(file, defaultNextRecordNumber);
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
    /// @param payloadTypeId the type of the payload
    /// @param payload       an array containing the payload
    /// @param payloadLength the length of the payload in bytes
    /// @param recordNumber  the record number
    record WalRecord(int payloadTypeId, byte[] payload, int payloadLength, long recordNumber) {

    }

    private static class ScratchBuffer {
        private byte[] buffer = null;

        byte[] ensureCapacity(int length) {
            if (buffer == null || buffer.length < length) {
                buffer = new byte[length];
            }
            return buffer;
        }
    }
}
