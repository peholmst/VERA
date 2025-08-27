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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.zip.CRC32C;

// TODO Javadocs

sealed abstract class WalFile implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(WalFile.class);
    static final int MAGIC = 0x57414C30;

    protected final Path file;

    public WalFile(Path file) {
        this.file = file;
    }

    protected static long calculateChecksum(int payloadTypeId, byte[] payload, int payloadLength,
                                            long recordNumber) {
        var crc = new CRC32C();
        crc.update(payloadTypeId);
        crc.update(payloadLength);
        crc.update(payload, 0, payloadLength);
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

    protected abstract void doWithReadLock(Runnable runnable);

    public void replayAll(Consumer<WalRecord> consumer) {
        log.info("Replaying all records");
        doWithReadLock(() -> {
            int recordsReplayed = 0;
            try (var readChannel = FileChannel.open(file, StandardOpenOption.READ)) {
                var headerBuffer = ByteBuffer.allocate(
                        Integer.BYTES           // Magic
                                + Long.BYTES    // Checksum
                                + Integer.BYTES // Type ID
                                + Integer.BYTES // Payload length
                );
                var recordNumberBuffer = ByteBuffer.allocate(Long.BYTES);

                long checksum;
                int payloadTypeId;
                int payloadLength;
                long recordNumber;
                ByteBuffer payloadBuffer = null;
                long filePosition;

                while (true) {
                    filePosition = readChannel.position();
                    // Read header
                    if (readFully(readChannel, headerBuffer) < headerBuffer.capacity()) {
                        break; // Clean EOF
                    }
                    headerBuffer.flip();
                    if (MAGIC != headerBuffer.getInt()) {
                        log.error("Magic number missing from record at file position {}", filePosition);
                        throw new WalCorruptionException("Magic number missing");
                    }
                    checksum = headerBuffer.getLong();
                    payloadTypeId = headerBuffer.getInt();
                    payloadLength = headerBuffer.getInt();
                    headerBuffer.clear();

                    // Read payload
                    if (payloadBuffer == null || payloadBuffer.capacity() < payloadLength) {
                        payloadBuffer = ByteBuffer.allocate(payloadLength);
                    }
                    if (readFully(readChannel, payloadBuffer) < 0) {
                        break;
                    }
                    payloadBuffer.flip();

                    // Read record number
                    if (readFully(readChannel, recordNumberBuffer) < 0) {
                        break;
                    }
                    recordNumberBuffer.flip();
                    recordNumber = recordNumberBuffer.getLong();
                    recordNumberBuffer.clear();

                    // Verify checksum
                    var actualChecksum = calculateChecksum(payloadTypeId, payloadBuffer.array(), payloadLength, recordNumber);
                    if (actualChecksum != checksum) {
                        log.error("Checksum mismatch in record {} at file position {}. Expected checksum {}, actual was {}",
                                recordNumber, filePosition, checksum, actualChecksum);
                        throw new WalCorruptionException("Checksum mismatch in record " + recordNumber);
                    }

                    // Pass record to consumer
                    try {
                        consumer.accept(new WalRecord(payloadTypeId, payloadBuffer.array(), payloadLength, recordNumber));
                        recordsReplayed++;
                    } catch (Exception ex) {
                        log.debug("Error consuming record {}", recordNumber, ex);
                        throw new WalConsumerException(ex);
                    }
                }
            } catch (IOException ex) {
                log.error("Error reading file", ex);
                throw new WalIOException("Error reading file", ex);
            } finally {
                log.info("Records replayed: {}", recordsReplayed);
            }
        });
    }

    private int readFully(FileChannel fileChannel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (fileChannel.read(buffer) < 0) {
                return -1;
            }
        }
        return buffer.position();
    }

    static final class ReadOnlyWalFile extends WalFile {

        ReadOnlyWalFile(Path file) {
            super(file);
        }

        @Override
        protected void doWithReadLock(Runnable runnable) {
            runnable.run(); // No need for a read lock
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }

    static final class WritableWalFile extends WalFile {

        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final FileChannel fileChannel;
        private long nextRecordNumber;

        public WritableWalFile(Path file, long defaultNextRecordNumber) {
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

        public void write(int payloadTypeId, byte[] payload) {
            var size = Integer.BYTES  // Magic
                    + Long.BYTES      // Checksum
                    + Integer.BYTES   // Type ID
                    + Integer.BYTES   // Payload length
                    + payload.length  // Payload
                    + Long.BYTES;     // Record number
            var buffer = ByteBuffer.allocate(size);
            lock.writeLock().lock();
            try {
                var checksum = calculateChecksum(payloadTypeId, payload, payload.length, nextRecordNumber);

                buffer.putInt(MAGIC);
                buffer.putLong(checksum);
                buffer.putInt(payloadTypeId);
                buffer.putInt(payload.length);
                buffer.put(payload);

                buffer.putLong(nextRecordNumber);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    //noinspection ResultOfMethodCallIgnored
                    fileChannel.write(buffer);
                }
                fileChannel.force(false);
                nextRecordNumber++;
            } catch (IOException ex) {
                log.error("Error writing payload to file", ex);
                throw new WalIOException("Error writing payload to file", ex);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public long getNextRecordNumber() {
            lock.readLock().lock();
            try {
                return nextRecordNumber;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void verify() {

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

        @Override
        protected void doWithReadLock(Runnable runnable) {
            lock.readLock().lock();
            try {
                runnable.run();
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    record WalRecord(int payloadTypeId, byte[] payload, int payloadLength, long recordNumber) {

    }
}
