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

import net.pkhapps.vera.gis.server.data.domain.MunicipalityCode;
import net.pkhapps.vera.gis.server.data.postgis.jooq.tables.records.MunicipalityCodeRecord;
import net.pkhapps.vera.gis.server.data.spi.ForStoringMunicipalityCodes;
import org.jooq.DSLContext;
import org.jspecify.annotations.NullMarked;

import java.time.OffsetDateTime;
import java.util.stream.StreamSupport;

import static net.pkhapps.vera.gis.server.data.postgis.jooq.Tables.MUNICIPALITY_CODE;

@NullMarked
final class MunicipalityCodeDatabase implements ForStoringMunicipalityCodes {

    private final DSLContext create;

    public MunicipalityCodeDatabase(DSLContext create) {
        this.create = create;
    }

    @Override
    public void storeMunicipalityCodes(Iterable<MunicipalityCode> municipalityCodes) {
        create.transaction(tx -> {
            var now = OffsetDateTime.now();
            // Deactivate all active codes. The active ones will be re-activated in the next step.
            tx.dsl().update(MUNICIPALITY_CODE)
                    .set(MUNICIPALITY_CODE.ACTIVE, false)
                    .set(MUNICIPALITY_CODE.UPDATED_AT, now)
                    .where(MUNICIPALITY_CODE.ACTIVE.isTrue())
                    .execute();

            var records = StreamSupport
                    .stream(municipalityCodes.spliterator(), false)
                    .map(code -> {
                        var record = new MunicipalityCodeRecord();
                        record.setMunicipalityCode(code.code());
                        record.setMunicipalityNameFin(code.nameFin());
                        record.setMunicipalityNameSwe(code.nameSwe());
                        record.setActive(code.active());
                        record.attach(tx);
                        return record;
                    })
                    .toList();
            tx.dsl().batchMerge(records).execute();
        });
    }
}
