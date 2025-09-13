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

package net.pkhapps.vera.server.domain.model.station;

import net.pkhapps.vera.server.domain.model.geo.Wgs84Point;
import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.util.Locales;
import net.pkhapps.vera.server.util.wal.TestInMemoryWal;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class StationAggregateAndRepositoryTest {

    private static final Wgs84Point POINT0 = new Wgs84Point(0, 0);
    private static final Wgs84Point POINT1 = new Wgs84Point(60.306738, 22.300907);
    private static final Wgs84Point POINT2 = new Wgs84Point(60.298351, 22.302190);
    private static final Wgs84Point POINT3 = new Wgs84Point(60.315892, 22.332546);

    @Test
    void created_stations_are_kept_in_memory() {
        var wal = new TestInMemoryWal();
        try (var repo = new StationRepository(wal)) {
            var station1 = repo.create(MultiLingualString.of(Locale.ENGLISH, "test1"), POINT1);
            var station2 = repo.create(MultiLingualString.of(Locale.FRENCH, "test2"), POINT2);

            assertThat(repo.get(station1.id())).containsSame(station1);
            assertThat(repo.get(station2.id())).containsSame(station2);
        }
    }

    @Test
    void stations_are_recreated_from_the_wal_on_replay() {
        var wal = new TestInMemoryWal();
        StationId id1;
        StationId id2;
        StationId id3;
        try (var repo = new StationRepository(wal)) {
            var station1 = repo.create(MultiLingualString.of(Locales.FINNISH, "test1"), POINT0);
            station1.update((_, mutator) -> mutator
                    .setName(MultiLingualString.of(Locales.FINNISH, "terve"))
                    .setLocation(POINT1)
                    .setNote("A note")
            );
            id1 = station1.id();

            var station2 = repo.create(MultiLingualString.of(Locales.SWEDISH, "test2"), POINT0);
            station2.update((_, mutator) -> mutator
                    .setName(MultiLingualString.of(Locales.SWEDISH, "hej"))
                    .setLocation(POINT2)
                    .setNote("Another note")
            );
            id2 = station2.id();

            var station3 = repo.create(MultiLingualString.of(Locale.GERMAN, "this will be deleted"), POINT3);
            id3 = station3.id();
            repo.remove(station3.id());
        }

        try (var repo = new StationRepository(wal)) {
            wal.replay();

            var station1 = repo.get(id1).orElseThrow();
            assertThat(station1.name().get(Locales.FINNISH)).contains("terve");
            assertThat(station1.location()).isEqualTo(POINT1);
            assertThat(station1.note()).isEqualTo("A note");

            var station2 = repo.get(id2).orElseThrow();
            assertThat(station2.name().get(Locales.SWEDISH)).contains("hej");
            assertThat(station2.location()).isEqualTo(POINT2);
            assertThat(station2.note()).isEqualTo("Another note");

            assertThat(repo.get(id3)).isEmpty();
        }
    }

    @Test
    void stations_are_recreated_from_the_wal_on_replay_of_snapshot() {
        var wal = new TestInMemoryWal();
        StationId id1;
        StationId id2;
        StationId id3;
        try (var repo = new StationRepository(wal)) {
            var station1 = repo.create(MultiLingualString.of(Locale.ENGLISH, "test1"), POINT1);
            station1.update((_, mutator) -> mutator.setNote("A note"));
            id1 = station1.id();

            var station2 = repo.create(MultiLingualString.of(Locale.FRENCH, "test2"), POINT2);
            station2.update((_, mutator) -> mutator.setNote("Another note"));
            id2 = station2.id();

            var station3 = repo.create(MultiLingualString.of(Locale.GERMAN, "this will be deleted"), POINT3);
            id3 = station3.id();
            repo.remove(station3.id());

            wal.takeSnapshot();
        }

        try (var repo = new StationRepository(wal)) {
            wal.replay();
            var station1 = repo.get(id1).orElseThrow();
            assertThat(station1.name().get(Locale.ENGLISH)).contains("test1");
            assertThat(station1.location()).isEqualTo(POINT1);
            assertThat(station1.note()).isEqualTo("A note");

            var station2 = repo.get(id2).orElseThrow();
            assertThat(station2.name().get(Locale.FRENCH)).contains("test2");
            assertThat(station2.location()).isEqualTo(POINT2);
            assertThat(station2.note()).isEqualTo("Another note");

            assertThat(repo.get(id3)).isEmpty();
        }
    }
}
