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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/// Class used by [FileSystemWal] to manage files and metadata.
final class FileSystemWalMetadataStore {

    private static final Logger log = LoggerFactory.getLogger(FileSystemWalMetadataStore.class);
    private final Path directory;
    private final Path metadataFile;
    private Metadata metadata;

    // TODO This current implementation forces the first snapshot to be empty, which is stupid. Make it possible to
    //  start without a snapshot at all.

    FileSystemWalMetadataStore(final Path directory) {
        if (!Files.exists(directory)) {
            try {
                this.directory = Files.createDirectories(directory);
            } catch (IOException ex) {
                log.error("Error creating WAL directory {}", directory, ex);
                throw new WalIOException("Error creating WAL directory", ex);
            }
        } else {
            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException("Path " + directory + " is not a directory");
            }
            this.directory = directory;
        }
        metadataFile = directory.resolve("metadata.json");
        this.metadata = readMetadata(metadataFile).orElseGet(() -> {
            var defaultMetadata = createDefaultMetadata();
            log.info("Creating new metadata file {}", metadataFile);
            writeMetadata(metadataFile, defaultMetadata);
            return defaultMetadata;
        });
    }

    /// Creates a new temporary file to write a snapshot to. This does not update the metadata or affect the existing
    /// WAL in any way. After successfully writing, call [#storeNewSnapshotFile(Path, long)] to add the snapshot to the
    /// WAL.
    ///
    /// @return a new, empty temporary file
    /// @throws WriteAheadLogException if the file could not be created
    public synchronized Path createTemporarySnapshotFile() {
        try {
            return Files.createTempFile(directory, "snapshot", ".bin.tmp");
        } catch (IOException ex) {
            log.error("Error creating temporary snapshot file", ex);
            throw new WalIOException("Error creating temporary snapshot file", ex);
        }
    }

    /// Returns the latest snapshot file that can be used for replays. If the file does not exist, it is created as
    /// an empty file.
    ///
    /// @return the latest, possibly empty, snapshot file
    /// @throws WriteAheadLogException if the file did not exist and could not be created
    public synchronized Path latestSnapshotFile() {
        var file = directory.resolve(metadata.latestSnapshot);
        try {
            if (!Files.exists(file)) {
                log.info("Creating empty snapshot file {}", file);
                Files.createFile(file);
            }
        } catch (IOException ex) {
            log.error("Error creating empty snapshot file {}", file, ex);
            throw new WalIOException("Error creating empty snapshot file", ex);
        }
        return file;
    }

    /// Returns the latest WAL file that can be used for writing. If the file does not exist, it is created as an empty
    /// file.
    ///
    /// @return the latest, possibly empty, WAL file
    /// @throws WriteAheadLogException if the file did not exist and could not be created
    public synchronized Path latestWalFile() {
        var file = directory.resolve(metadata.walFiles.getLast());
        try {
            if (!Files.exists(file)) {
                log.info("Creating empty WAL file {}", file);
                Files.createFile(file);
            }
        } catch (IOException ex) {
            log.error("Error creating empty WAL file {}", file, ex);
            throw new WalIOException("Error creating empty WAL file", ex);
        }
        return file;
    }

    /// Returns the default next record number to use as the first record when [#latestWalFile()] is empty.
    /// This number is read from the metadata, not from the actual WAL files.
    ///
    /// @return the first record number of the latest WAL file.
    public synchronized long firstRecordNumberOfLatestWalFile() {
        return metadata.firstRecordNumber;
    }

    /// Performs the specified `action` on each WAL file that has been created since the latest snapshot was taken.
    /// The files are read from the metadata; this method does not check whether they exist or not.
    ///
    /// @param action the action to perform on each WAL file
    public synchronized void forEachWalFileSinceLatestSnapshot(Consumer<Path> action) {
        metadata.walFiles.forEach(walFileName -> action.accept(directory.resolve(walFileName)));
    }

    /// Turns the `temporarySnapshotFile` into the [#latestSnapshotFile()], updating the metadata on disk. If this
    /// method fails, the existing WAL and metadata remain unchanged.
    ///
    /// @param temporarySnapshotFile the file containing the new snapshot
    /// @param nextRecordNumber      the number of the first record in the new WAL file that will be created
    /// @throws WriteAheadLogException if something went wrong.
    public synchronized void storeNewSnapshotFile(Path temporarySnapshotFile, long nextRecordNumber) {
        try {
            var nextSnapshotFileName = "snapshot-%06d.bin".formatted(extractNumberFromFileName(latestSnapshotFile()) + 1);
            var nextWalFileName = "wal-%06d.log".formatted(extractNumberFromFileName(latestWalFile()) + 1);
            var nextMetadata = new Metadata(1, nextSnapshotFileName, nextRecordNumber, List.of(nextWalFileName));

            var temporaryMetadataFile = Files.createTempFile(directory, "metadata", ".json.tmp");
            writeMetadata(temporaryMetadataFile, nextMetadata);

            var nextSnapshotFile = directory.resolve(nextSnapshotFileName);

            log.info("Moving snapshot {} into {}", temporarySnapshotFile, nextSnapshotFile);
            Files.move(temporarySnapshotFile, nextSnapshotFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            log.info("Moving metadata {} into {}", temporaryMetadataFile, metadataFile);
            Files.move(temporaryMetadataFile, metadataFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            this.metadata = nextMetadata;
        } catch (Exception ex) {
            log.error("Error storing new snapshot {}", temporarySnapshotFile, ex);
            throw new WalIOException("Error storing new snapshot", ex);
        }
    }

    private int extractNumberFromFileName(Path file) {
        var fileName = file.getFileName().toString();
        var numberPart = fileName.substring(fileName.lastIndexOf('-') + 1, fileName.length() - 4);
        try {
            var number = Integer.parseInt(numberPart);
            if (number <= 0) {
                throw new WalStateException("File number must be > 0: " + fileName);
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new WalStateException("Unexpected file name pattern: " + fileName);
        }
    }

    private Optional<Metadata> readMetadata(Path file) {
        if (!Files.exists(file)) {
            log.info("Metadata file {} does not exist", file);
            return Optional.empty();
        }
        try {
            var json = new JSONObject(Files.readString(file, StandardCharsets.UTF_8));
            var version = json.getInt("version");
            if (version != 1) {
                throw new WalStateException("Unsupported version: " + version);
            }
            var latestSnapshot = json.getString("latestSnapshot");
            var firstRecordNumber = json.getLong("firstRecordNumber");
            var walFiles = toStringList(json.getJSONArray("walFiles"));
            return Optional.of(new Metadata(version, latestSnapshot, firstRecordNumber, walFiles));
        } catch (Exception ex) {
            log.error("Could not read metadata from {}", file, ex);
            return Optional.empty();
        }
    }

    private List<String> toStringList(JSONArray array) {
        var list = new ArrayList<String>(array.length());
        array.forEach(e -> list.add(e.toString()));
        return Collections.unmodifiableList(list);
    }

    private void writeMetadata(Path file, Metadata metadata) {
        try {
            var json = new JSONObject();
            json.put("version", metadata.version);
            json.put("latestSnapshot", metadata.latestSnapshot);
            json.put("firstRecordNumber", metadata.firstRecordNumber);
            json.put("walFiles", metadata.walFiles);
            Files.writeString(file, json.toString(), StandardCharsets.UTF_8, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.SYNC);
        } catch (IOException ex) {
            log.error("Error writing metadata to file {}", file, ex);
            throw new WalIOException("Error writing metadata to file", ex);
        }
    }

    private Metadata createDefaultMetadata() {
        return new Metadata(1, "snapshot-000001.bin", 1L, List.of("wal-000001.log"));
    }

    private record Metadata(
            int version,
            String latestSnapshot,
            long firstRecordNumber,
            List<String> walFiles
    ) {
    }
}
