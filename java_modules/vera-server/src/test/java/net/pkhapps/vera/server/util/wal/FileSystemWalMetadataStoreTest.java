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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemWalMetadataStoreTest {

    private Path directory;

    @BeforeEach
    void setup() throws IOException {
        directory = Files.createTempDirectory("file-system-wal-metadata-store-test");
    }

    @AfterEach
    void cleanUp() throws IOException {
        try (var paths = Files.walk(directory)) {
            //noinspection ResultOfMethodCallIgnored
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    void creates_initial_files_in_empty_directory() {
        var store = new FileSystemWalMetadataStore(directory);
        assertThat(store.latestWalFile()).isEmptyFile();
        assertThat(store.latestWalFile()).hasFileName("wal-000001.log");
        assertThat(store.latestSnapshotFile()).isEmptyFile();
        assertThat(store.latestSnapshotFile()).hasFileName("snapshot-000001.bin");
        assertThat(store.firstRecordNumberOfLatestWalFile()).isEqualTo(1L);
    }

    @Test
    void reads_existing_metadata_file() throws IOException {
        Files.writeString(directory.resolve("metadata.json"),
                """
                        {
                          "version":1,
                          "latestSnapshot": "snapshot-000002.bin",
                          "firstRecordNumber": 1025,
                          "walFiles": [
                            "wal-000004.log",
                            "wal-000005.log",
                            "wal-000006.log"
                          ]
                        }
                        """,
                StandardCharsets.UTF_8);
        Files.createFile(directory.resolve("snapshot-000002.bin"));
        Files.createFile(directory.resolve("wal-000004.log"));
        Files.createFile(directory.resolve("wal-000005.log"));
        Files.createFile(directory.resolve("wal-000006.log"));
        var store = new FileSystemWalMetadataStore(directory);
        assertThat(store.latestSnapshotFile()).hasFileName("snapshot-000002.bin");
        assertThat(store.latestWalFile()).hasFileName("wal-000006.log");
        assertThat(store.firstRecordNumberOfLatestWalFile()).isEqualTo(1025L);
        var visitedPaths = new ArrayList<Path>();
        store.forEachWalFileSinceLatestSnapshot(visitedPaths::add);
        assertThat(visitedPaths).containsExactly(
                directory.resolve("wal-000004.log"),
                directory.resolve("wal-000005.log"),
                directory.resolve("wal-000006.log")
        );
    }

    @Test
    void creates_new_snapshot_file() throws IOException {
        var store = new FileSystemWalMetadataStore(directory);
        var temporaryFile = store.createTemporarySnapshotFile();
        Files.writeString(temporaryFile, "this is the new snapshot");
        store.storeNewSnapshotFile(temporaryFile, 512L);

        assertThat(store.latestSnapshotFile()).hasFileName("snapshot-000002.bin");
        assertThat(store.latestWalFile()).hasFileName("wal-000002.log");
        assertThat(store.firstRecordNumberOfLatestWalFile()).isEqualTo(512L);

        // Make sure the changes have been saved to file
        var newStore = new FileSystemWalMetadataStore(directory);
        assertThat(newStore.latestSnapshotFile()).isEqualTo(store.latestSnapshotFile());
        assertThat(newStore.latestWalFile()).isEqualTo(store.latestWalFile());
        assertThat(newStore.firstRecordNumberOfLatestWalFile()).isEqualTo(store.firstRecordNumberOfLatestWalFile());
    }
}
