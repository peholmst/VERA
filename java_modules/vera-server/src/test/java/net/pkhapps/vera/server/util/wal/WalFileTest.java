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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WalFileTest {

    private static final Consumer<IOException> DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER = ex -> System.exit(1);
    static Path directory;

    @BeforeAll
    static void setupAll() throws IOException {
        directory = Files.createTempDirectory("wal-event-file-test");
    }

    @AfterAll
    static void cleanUpAll() throws IOException {
        try (var paths = Files.walk(directory)) {
            //noinspection ResultOfMethodCallIgnored
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    @Disabled
    void small_stress_test_batched_durability() {
        small_stress_test(Durability.BATCHED);
    }

    @Test
    @Disabled
    void small_stress_test_immediate_durability() {
        small_stress_test(Durability.IMMEDIATE);
    }

    private void small_stress_test(Durability durability) {
        var record_count = 1_000;
        try (var file = WalFile.writable(directory.resolve("stress"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            var start = System.currentTimeMillis();
            for (var i = 0; i < record_count; ++i) {
                file.write(("hello world!" + 1).getBytes(StandardCharsets.UTF_8), durability);
            }
            var end = System.currentTimeMillis();
            System.out.println("Wrote " + record_count + " records in " + (end - start) + "ms using " + durability + " mode");
        }
    }

    @Test
    void next_record_number_set_to_default_when_creating_new_file() {
        try (var file = WalFile.writable(directory.resolve("empty"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            assertEquals(1L, file.getNextRecordNumber());
        }
    }

    @Test
    void writing_records_increment_number() {
        try (var file = WalFile.writable(directory.resolve("increments"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            assertEquals(1L, file.write("hello".getBytes(StandardCharsets.UTF_8), Durability.NONE));
            assertEquals(2L, file.write("world!".getBytes(StandardCharsets.UTF_8), Durability.NONE));
            assertEquals(3L, file.getNextRecordNumber());
        }
    }

    @Test
    void next_record_number_read_from_existing_file() {
        try (var file = WalFile.writable(directory.resolve("next_record"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            file.write("hello".getBytes(StandardCharsets.UTF_8), Durability.NONE);
            file.write("world!".getBytes(StandardCharsets.UTF_8), Durability.NONE);
        }
        try (var file = WalFile.writable(directory.resolve("next_record"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            assertEquals(3L, file.getNextRecordNumber());
        }
    }

    @Test
    void records_can_be_replayed_from_the_beginning() {
        try (var file = WalFile.writable(directory.resolve("replay"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            file.write("hello".getBytes(StandardCharsets.UTF_8), Durability.NONE);
            file.write("world!".getBytes(StandardCharsets.UTF_8), Durability.NONE);

            var records = replay(file);
            assertEquals(List.of("hello:1", "world!:2"), records);
        }
    }

    @Test
    void records_can_be_replayed_from_readonly_file() {
        try (var file = WalFile.writable(directory.resolve("replay_readonly"), 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            file.write("hello".getBytes(StandardCharsets.UTF_8), Durability.NONE);
            file.write("world!".getBytes(StandardCharsets.UTF_8), Durability.NONE);
        }

        try (var file = WalFile.readOnly(directory.resolve("replay_readonly"))) {
            var records = replay(file);
            assertEquals(List.of("hello:1", "world!:2"), records);
        }
    }

    @Test
    void replaying_skips_incomplete_header_in_last_record() throws IOException {
        var path = directory.resolve("replay_incomplete_header");
        try (var channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
            writeCompleteRecord(channel, "hello", 1L);
            writeCompleteRecord(channel, "world!", 2L);
            writeIncompleteRecord(channel, "incomplete header", 3L, WalFile.HEADER_SIZE - 1);
        }

        try (var file = WalFile.readOnly(path)) {
            var records = replay(file);
            assertEquals(List.of("hello:1", "world!:2"), records);
        }
    }

    @Test
    void replaying_skips_incomplete_payload_in_last_record() throws IOException {
        var path = directory.resolve("replay_incomplete_payload");
        try (var channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
            writeCompleteRecord(channel, "hello", 1L);
            writeCompleteRecord(channel, "world!", 2L);
            writeIncompleteRecord(channel, "incomplete payload", 3L, WalFile.HEADER_SIZE + 1);
        }

        try (var file = WalFile.readOnly(path)) {
            var records = replay(file);
            assertEquals(List.of("hello:1", "world!:2"), records);
        }
    }

    @Test
    void writing_truncates_incomplete_last_record() throws IOException {
        var path = directory.resolve("truncate_incomplete_last_record");
        try (var channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
            writeCompleteRecord(channel, "hello", 1L);
            writeCompleteRecord(channel, "world!", 2L);
            writeIncompleteRecord(channel, "incomplete header", 3L, WalFile.HEADER_SIZE - 1);
        }

        try (var file = WalFile.writable(path, 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            file.write("complete header".getBytes(StandardCharsets.UTF_8), Durability.NONE);
            var records = replay(file);
            assertEquals(List.of("hello:1", "world!:2", "complete header:3"), records);
        }
    }

    @Test
    void writing_truncates_incomplete_only_record() throws IOException {
        var path = directory.resolve("truncate_incomplete_only_record");
        try (var channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
            writeIncompleteRecord(channel, "incomplete header", 1L, WalFile.HEADER_SIZE - 1);
        }

        try (var file = WalFile.writable(path, 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            file.write("complete header".getBytes(StandardCharsets.UTF_8), Durability.NONE);
            var records = replay(file);
            assertEquals(List.of("complete header:1"), records);
        }
    }

    private void writeCompleteRecord(FileChannel channel, String payload, long recordNumber) throws IOException {
        var payloadBuf = payload.getBytes(StandardCharsets.UTF_8);
        var recordBuf = WalFile.WritableWalFile.writeRecord(payloadBuf, payloadBuf.length, recordNumber);
        while (recordBuf.hasRemaining()) {
            //noinspection ResultOfMethodCallIgnored
            channel.write(recordBuf);
        }
    }

    private void writeIncompleteRecord(FileChannel channel, String payload, long recordNumber, int truncateAt) throws IOException {
        var payloadBuf = payload.getBytes(StandardCharsets.UTF_8);
        var recordBuf = WalFile.WritableWalFile.writeRecord(payloadBuf, payloadBuf.length, recordNumber);
        var truncatedBuf = ByteBuffer.wrap(recordBuf.array(), 0, truncateAt);
        while (truncatedBuf.hasRemaining()) {
            //noinspection ResultOfMethodCallIgnored
            channel.write(truncatedBuf);
        }
    }

    private List<String> replay(WalFile file) {
        var records = new ArrayList<String>();
        file.replayAll(record -> {
            var payload = new String(record.payload(), 0, record.payloadLength(), StandardCharsets.UTF_8);
            records.add("%s:%d".formatted(payload, record.recordNumber()));
        });
        return records;
    }

    @Test
    void can_write_only_a_part_of_the_payload_array() {
        var path = directory.resolve("can_write_only_a_part_of_the_payload_array");
        try (var file = WalFile.writable(path, 1L, DEFAULT_WAL_FLUSHER_EXCEPTION_HANDLER)) {
            var writePayload = "Hello World Ignore The Rest".getBytes(StandardCharsets.UTF_8);
            file.write(writePayload, 0, 11, Durability.NONE);
            file.write("Hello World".getBytes(StandardCharsets.UTF_8), Durability.NONE);

            var count = new AtomicInteger(0);
            file.replayAll(record -> {
                var readPayload = new String(record.payload(), 0, record.payloadLength(), StandardCharsets.UTF_8);
                assertThat(readPayload).isEqualTo("Hello World");
                count.getAndIncrement();
            });
            assertThat(count.get()).isEqualTo(2);
        }
    }

    // TODO Test reading corrupt files (i.e. bad checksum or bad magic)
}
