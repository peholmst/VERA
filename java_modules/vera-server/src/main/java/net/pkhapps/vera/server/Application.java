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

package net.pkhapps.vera.server;

import io.javalin.Javalin;
import net.pkhapps.vera.server.adapter.AdapterFactory;
import net.pkhapps.vera.server.domain.model.DomainModel;
import net.pkhapps.vera.server.port.PrimaryPorts;
import net.pkhapps.vera.server.util.wal.FileSystemWal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/// Main entry point into the VERA server application.
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final FileSystemWal wal;
    private final int port;
    private final Javalin javalin;

    private Application(Path directory, int port) {
        this.port = port;
        wal = new FileSystemWal(directory, DomainModel.serdeRegistrators());
        javalin = AdapterFactory.createJavalin(PrimaryPorts.create(DomainModel.create(wal)));
    }

    /// Starts the application
    public void start() {
        log.info("Starting application");
        wal.replay();
        javalin.start(port);
        log.info("Application started");
    }

    /// Stops the application
    public void stop() {
        log.info("Shutting down application");
        javalin.stop();
        wal.close();
        log.info("Application shut down");
    }

    public static void main(String[] args) {
        var directory = Path.of("vera-server-data/").toAbsolutePath();
        var app = new Application(directory, 7070);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }
}
