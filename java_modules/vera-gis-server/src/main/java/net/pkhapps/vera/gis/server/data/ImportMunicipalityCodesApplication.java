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

package net.pkhapps.vera.gis.server.data;

import net.pkhapps.vera.gis.server.data.postgis.PostGIS;
import net.pkhapps.vera.gis.server.data.service.MunicipalityCodeImportService;
import org.apache.commons.cli.ParseException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.List;

@NullMarked
public class ImportMunicipalityCodesApplication extends AbstractImportDataApplication {

    private static final Logger log = LoggerFactory.getLogger(ImportMunicipalityCodesApplication.class);

    @Override
    protected String getCommandLineSyntax() {
        return "ImportMunicipalityCodes [option]... <json-file>";
    }

    @Override
    protected void importData(List<String> arguments, PostGIS postgis) throws IOException {
        var importService = new MunicipalityCodeImportService(postgis.forStoringMunicipalityCodes());
        var jsonFile = Path.of(arguments.getFirst()).toAbsolutePath();
        log.info("Importing municipality codes from {}", jsonFile);
        importService.importJSONFile(() -> Files.newInputStream(jsonFile, StandardOpenOption.READ));
    }

    static void main(String[] args) throws ParseException, IOException, SQLException {
        new ImportMunicipalityCodesApplication().run(args);
    }
}
