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

package net.pkhapps.vera.gis.server.data.postgis;

import org.jspecify.annotations.NullMarked;

import java.util.regex.Pattern;

@NullMarked
public record PostGISSettings(String hostname, int port, String database, String user, String password,
                              boolean runFlyway) {

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+$");
    private static final Pattern DATABASE_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public PostGISSettings {
        if (!HOSTNAME_PATTERN.matcher(hostname).matches()) {
            throw new IllegalArgumentException("Invalid hostname: " + hostname);
        }
        if (!DATABASE_PATTERN.matcher(database).matches()) {
            throw new IllegalArgumentException("Invalid database name: " + database);
        }
    }

    public String jdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", hostname, port, database);
    }
}
