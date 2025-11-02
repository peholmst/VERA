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

package net.pkhapps.vera.gis.server.data.service;

import net.pkhapps.vera.gis.server.data.api.ForImportingTerrainData;
import net.pkhapps.vera.gis.server.data.api.InputStreamSupplier;
import net.pkhapps.vera.gis.server.data.spi.ForStoringTerrainData;
import org.geotools.appschema.resolver.xml.AppSchemaConfiguration;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xml.resolver.SchemaCache;
import org.geotools.xml.resolver.SchemaResolver;
import org.geotools.xsd.PullParser;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@NullMarked
public final class TerrainDataImportService implements ForImportingTerrainData {

    private static final Logger log = LoggerFactory.getLogger(TerrainDataImportService.class);
    private static final String NAMESPACE_URI = "http://xml.nls.fi/XML/Namespace/Maastotietojarjestelma/SiirtotiedostonMalli/2011-02";
    private static final String SCHEMA_LOCATION = "http://xml.nls.fi/XML/Schema/Maastotietojarjestelma/MTK/201405/Maastotiedot.xsd";

    private final ForStoringTerrainData forStoringTerrainData;
    private final SchemaResolver schemaResolver;
    private final SchemaCache schemaCache;
    private final AppSchemaConfiguration appSchemaConfiguration;

    public TerrainDataImportService(ForStoringTerrainData forStoringTerrainData) throws IOException {
        this.forStoringTerrainData = forStoringTerrainData;

        var cacheDirectory = Files.createDirectories(Path.of("vera-gis-schema-cache"));
        schemaCache = new SchemaCache(cacheDirectory.toFile(), true);
        schemaResolver = new SchemaResolver(schemaCache);
        appSchemaConfiguration = new AppSchemaConfiguration(NAMESPACE_URI, SCHEMA_LOCATION, schemaResolver);
        appSchemaConfiguration.addDependency(new GMLConfiguration());
    }

    @Override
    public void importGMLFile(InputStreamSupplier inputStream) throws IOException {
        try (var is = inputStream.get()) {
            importMunicipalities(is);
        }
        try (var is = inputStream.get()) {
            importAddressPoints(is);
        }
        try (var is = inputStream.get()) {
            importPlaceNames(is);
        }
        try (var is = inputStream.get()) {
            importRoadSegments(is);
        }
    }

    private void importAddressPoints(InputStream inputStream) throws IOException {
        log.info("Importing AddressPoints");
        var parser = new PullParser(appSchemaConfiguration, inputStream, new QName(NAMESPACE_URI, "Osoitepiste"));
        // TODO municipality borders
        // TODO how to differentiate between the different features? They all show up as maps when parsed like this
        try {
            Object feature;
            while ((feature = parser.parse()) != null) {
                System.out.println(feature);
                //if (feature instanceof SimpleFeature simpleFeature) {
                //    System.out.println(simpleFeature);
                // }
            }
        } catch (Exception e) {

        }

    }

    private void importMunicipalities(InputStream inputStream) {
        var parser = new PullParser(appSchemaConfiguration, inputStream, new QName(NAMESPACE_URI, "Kunta"));

    }

    private void importPlaceNames(InputStream inputStream) {
        var parser = new PullParser(appSchemaConfiguration, inputStream, new QName(NAMESPACE_URI, "Paikannimi"));

    }

    private void importRoadSegments(InputStream inputStream) {
        var parser = new PullParser(appSchemaConfiguration, inputStream, new QName(NAMESPACE_URI, "Tieviiva"));
    }
}
