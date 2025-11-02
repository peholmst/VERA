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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pkhapps.vera.gis.server.data.api.ForImportingMunicipalityCodes;
import net.pkhapps.vera.gis.server.data.api.InputStreamSupplier;
import net.pkhapps.vera.gis.server.data.domain.MunicipalityCode;
import net.pkhapps.vera.gis.server.data.spi.ForStoringMunicipalityCodes;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@NullMarked
public class MunicipalityCodeImportService implements ForImportingMunicipalityCodes {

    private static final Logger log = LoggerFactory.getLogger(MunicipalityCodeImportService.class);
    private final ForStoringMunicipalityCodes forStoringMunicipalityCodes;
    private final ObjectMapper objectMapper;

    public MunicipalityCodeImportService(ForStoringMunicipalityCodes forStoringMunicipalityCodes) {
        this.forStoringMunicipalityCodes = forStoringMunicipalityCodes;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void importJSONFile(InputStreamSupplier inputStream) throws IOException {
        // Format: https://koodistot.suomi.fi/codescheme;registryCode=jhs;schemeCode=kunta_1_20250101
        var jsonTree = objectMapper.readTree(inputStream.get());
        var codes = jsonTree.get("codes");
        var municipalities = codes.valueStream().map(code -> {
            var codeValue = code.get("codeValue").asText();
            var nameFin = code.get("prefLabel").get("fi").asText();
            var nameSwe = code.get("prefLabel").get("sv").asText();
            var active = code.get("status").asText().equals("VALID");
            return new MunicipalityCode(codeValue, nameFin, nameSwe, active);
        }).toList();
        log.info("Storing {} municipality codes", municipalities.size());
        forStoringMunicipalityCodes.storeMunicipalityCodes(municipalities);
    }
}
