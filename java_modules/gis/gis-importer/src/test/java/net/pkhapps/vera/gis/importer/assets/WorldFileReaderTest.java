/*
 * Copyright (c) 2023 Petter Holmström
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

package net.pkhapps.vera.gis.importer.assets;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.pkhapps.vera.common.domain.primitives.geo.support.CoordinateReferenceSystems.ETRS89_TM35FIN;
import static org.assertj.core.api.Assertions.assertThat;

class WorldFileReaderTest {

    @Test
    void read() throws IOException {
        try (var reader = new WorldFileReader(ETRS89_TM35FIN, getClass().getResourceAsStream("/K3423B.pgw"))) {
            var wf = reader.read();
            assertThat(wf.xScale()).isEqualTo(0.5);
            assertThat(wf.ySkew()).isEqualTo(0.0);
            assertThat(wf.xSkew()).isEqualTo(0.0);
            assertThat(wf.yScale()).isEqualTo(-0.5);
            assertThat(wf.topLeft()).isEqualTo(ETRS89_TM35FIN.createLocation(236000.25, 6653999.75));
        }
    }
}
