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

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalFileTest {

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
    void next_record_number_set_to_default_when_creating_new_file() {
        try (var file = WalFile.writable(directory.resolve("empty"), 1L)) {
            assertEquals(1L, file.getNextRecordNumber());
        }
    }

    @Test
    void writing_records_increment_number() {
        try (var file = WalFile.writable(directory.resolve("increments"), 1L)) {
            assertEquals(1L, file.write("hello".getBytes(StandardCharsets.UTF_8)));
            assertEquals(2L, file.write("world!".getBytes(StandardCharsets.UTF_8)));
            assertEquals(3L, file.getNextRecordNumber());
        }
    }

    @Test
    void next_record_number_read_from_existing_file() {
        try (var file = WalFile.writable(directory.resolve("next_record"), 1L)) {
            file.write("hello".getBytes(StandardCharsets.UTF_8));
            file.write("world!".getBytes(StandardCharsets.UTF_8));
        }
        try (var file = WalFile.writable(directory.resolve("next_record"), 1L)) {
            assertEquals(3L, file.getNextRecordNumber());
        }
    }

    @Test
    void records_can_be_replayed_from_the_beginning() {
        try (var file = WalFile.writable(directory.resolve("replay"), 1L)) {
            file.write("hello".getBytes(StandardCharsets.UTF_8));
            file.write("world!".getBytes(StandardCharsets.UTF_8));

            var records = replay(file);
            assertEquals(List.of("hello:1", "world!:2"), records);
        }
    }

    @Test
    void records_can_be_replayed_from_readonly_file() {
        try (var file = WalFile.writable(directory.resolve("replay_readonly"), 1L)) {
            file.write("hello".getBytes(StandardCharsets.UTF_8));
            file.write("world!".getBytes(StandardCharsets.UTF_8));
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

        try (var file = WalFile.writable(path, 1L)) {
            file.write("complete header".getBytes(StandardCharsets.UTF_8));
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

        try (var file = WalFile.writable(path, 1L)) {
            file.write("complete header".getBytes(StandardCharsets.UTF_8));
            var records = replay(file);
            assertEquals(List.of("complete header:1"), records);
        }
    }

    private void writeCompleteRecord(FileChannel channel, String payload, long recordNumber) throws IOException {
        var payloadBuf = payload.getBytes(StandardCharsets.UTF_8);
        var recordBuf = WalFile.WritableWalFile.writeRecord(payloadBuf, 0, payloadBuf.length, recordNumber);
        while (recordBuf.hasRemaining()) {
            //noinspection ResultOfMethodCallIgnored
            channel.write(recordBuf);
        }
    }

    private void writeIncompleteRecord(FileChannel channel, String payload, long recordNumber, int truncateAt) throws IOException {
        var payloadBuf = payload.getBytes(StandardCharsets.UTF_8);
        var recordBuf = WalFile.WritableWalFile.writeRecord(payloadBuf, 0, payloadBuf.length, recordNumber);
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

    // TODO Test reading corrupt files (i.e. bad checksum or bad magic)
}
