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

package net.pkhapps.vera.gis.server;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import net.pkhapps.vera.gis.server.tile.RasterTileService;
import net.pkhapps.vera.gis.server.tile.controller.TileRestController;
import net.pkhapps.vera.gis.server.tile.filesystem.FileSystemTileStore;
import net.pkhapps.vera.security.AccessControl;
import net.pkhapps.vera.security.AccessDeniedException;
import net.pkhapps.vera.security.AuthenticationRequiredException;
import net.pkhapps.vera.security.Permission;
import net.pkhapps.vera.security.javalin.PrincipalUtil;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Principal;
import java.util.Optional;
import java.util.Properties;

/// Main entry point into the VERA GIS server application.
@NullMarked
public class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);
    private static final String SYSTEM_PROPERTY_CONFIG_FILE_LOCATION = "configFileLocation";
    private static final String DEFAULT_CONFIG_FILE_NAME = "vera-gis-server.properties";

    private final ServerConfig config;
    private final Javalin javalin;

    private ServerApplication(ServerConfig config) {
        this.config = config;

        var accessControl = new AccessControl() {
            @Override
            public boolean hasPermission(Principal principal, Permission permission) {
                return true; // TODO Implement me!
            }
        };
        var tileStore = new FileSystemTileStore(config.tilesDirectory());
        var tileService = new RasterTileService(accessControl, tileStore);
        var tileController = new TileRestController(tileService);

        javalin = Javalin.create()
                .before(context -> {
                    // TODO Extract authentication token from header, turn into principal unless set already
                    PrincipalUtil.setPrincipal(context, () -> "not-implemented-yet");
                })
                .exception(AccessDeniedException.class, (e, ctx) -> ctx.status(HttpStatus.FORBIDDEN))
                .exception(AuthenticationRequiredException.class, (e, ctx) -> ctx.status(HttpStatus.UNAUTHORIZED))
                .exception(IllegalArgumentException.class, (e, ctx) -> ctx.status(HttpStatus.BAD_REQUEST));
        tileController.registerRoutes(javalin);
    }

    /// Starts the application
    public void start() {
        log.info("Starting application");
        javalin.start(config.serverPort());
        log.info("Application started");
    }

    /// Stops the application
    public void stop() {
        log.info("Shutting down application");
        javalin.stop();
        log.info("Application shut down");
    }

    private static Optional<ServerConfig> tryConfigFileFromSystemProperty() {
        log.debug("Looking for config file specified by system property {}", SYSTEM_PROPERTY_CONFIG_FILE_LOCATION);

        var configFileLocation = System.getProperty(SYSTEM_PROPERTY_CONFIG_FILE_LOCATION);
        if (configFileLocation == null) {
            log.debug("No system property configured");
            return Optional.empty();
        }

        var path = Path.of(configFileLocation);
        return tryConfigFileFromPath(path);
    }

    private static Optional<ServerConfig> tryConfigFileFromCurrentDirectory() {
        log.debug("Looking for config file in the current directory");
        var path = Paths.get(DEFAULT_CONFIG_FILE_NAME).toAbsolutePath();
        return tryConfigFileFromPath(path);
    }

    private static Optional<ServerConfig> tryConfigFileFromPath(Path path) {
        if (Files.notExists(path) || !Files.isReadable(path)) {
            log.debug("Config file {} does not exist or is not readable", path);
            return Optional.empty();
        }

        log.info("Loading properties from {}", path);
        var properties = new Properties();
        try (var is = Files.newInputStream(path, StandardOpenOption.READ)) {
            properties.load(is);
            return Optional.of(new PropertiesServerConfig(properties));
        } catch (IOException e) {
            log.error("Error loading properties from {}", path, e);
            return Optional.empty();
        }
    }

    static void main() {
        var config = tryConfigFileFromSystemProperty()
                .orElseGet(() ->
                        tryConfigFileFromCurrentDirectory()
                                .orElseThrow(() -> new IllegalStateException("No config file provided"))
                );
        var app = new ServerApplication(config);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }
}
