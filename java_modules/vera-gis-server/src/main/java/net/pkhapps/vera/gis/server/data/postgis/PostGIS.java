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

import net.pkhapps.vera.gis.server.data.spi.ForStoringMunicipalityCodes;
import net.pkhapps.vera.gis.server.data.spi.ForStoringTerrainData;
import org.flywaydb.core.Flyway;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NullMarked;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@NullMarked
public final class PostGIS implements AutoCloseable {

    private final Connection connection;
    private final TerrainDatabase terrainDatabase;
    private final MunicipalityCodeDatabase municipalityCodeDatabase;

    public PostGIS(PostGISSettings settings) throws SQLException {
        if (settings.runFlyway()) {
            var flyway = Flyway.configure().dataSource(settings.jdbcUrl(), settings.user(), settings.password()).load();
            flyway.migrate();
        }

        connection = DriverManager.getConnection(settings.jdbcUrl(), settings.user(), settings.password());

        var dsl = DSL.using(connection, SQLDialect.POSTGRES);
        terrainDatabase = new TerrainDatabase(dsl);
        municipalityCodeDatabase = new MunicipalityCodeDatabase(dsl);
    }

    public ForStoringTerrainData forStoringTerrainData() {
        return terrainDatabase;
    }

    public ForStoringMunicipalityCodes forStoringMunicipalityCodes() {
        return municipalityCodeDatabase;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
