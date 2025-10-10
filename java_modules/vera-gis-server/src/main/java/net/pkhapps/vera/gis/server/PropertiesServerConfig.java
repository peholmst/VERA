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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class PropertiesServerConfig implements ServerConfig {

    private static final String PROP_SERVER_PORT = "server.port";
    private static final String PROP_TILES_DIRECTORY = "tiles.directory";

    private final int serverPort;
    private final Path tilesDirectory;

    public PropertiesServerConfig(Properties properties) {
        serverPort = Integer.parseInt(properties.getProperty(PROP_SERVER_PORT));
        tilesDirectory = Paths.get(properties.getProperty(PROP_TILES_DIRECTORY));
    }

    @Override
    public int serverPort() {
        return serverPort;
    }

    @Override
    public Path tilesDirectory() {
        return tilesDirectory;
    }
}
