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

package net.pkhapps.vera.server.port.admin;

import net.pkhapps.vera.server.domain.model.i18n.MultiLingualString;
import net.pkhapps.vera.server.domain.model.station.Station;
import net.pkhapps.vera.server.domain.model.station.StationId;
import net.pkhapps.vera.server.domain.model.station.StationRepository;
import net.pkhapps.vera.server.security.AccessControl;
import net.pkhapps.vera.server.security.AuditLogger;
import net.pkhapps.vera.server.security.Permission;
import net.pkhapps.vera.server.util.Locales;
import org.jspecify.annotations.Nullable;

import java.security.Principal;
import java.util.List;
import java.util.function.Predicate;

/// Port for administrating [Station]s. This port is designed to be used by CRUD-like user interfaces.
public final class ForStationAdministration {

    private final StationRepository stationRepository;
    private final AccessControl accessControl;
    private final AdminAuditLogger auditLogger;

    /// Creates a new `ForStationAdministration`.
    ///
    /// @param stationRepository the station repository to use
    /// @param accessControl     the access control to use
    /// @param auditLogger       the audit logger to use
    public ForStationAdministration(StationRepository stationRepository,
                                    AccessControl accessControl,
                                    AuditLogger auditLogger) {
        this.stationRepository = stationRepository;
        this.accessControl = accessControl;
        this.auditLogger = new AdminAuditLogger(auditLogger);
    }

    /// Lists all stations matching the given `searchTerm`.
    ///
    /// @param searchTerm the search term, or `null` to include all stations
    /// @param limit      the maximum number of stations to return
    /// @param principal  the principal performing the operation
    /// @return a list of [StationDto]s
    /// @throws net.pkhapps.vera.server.security.AccessDeniedException if the principal lacks the [Permission#ADMIN_STATIONS] permission
    public List<StationDto> list(@Nullable String searchTerm, int limit, Principal principal) {
        accessControl.requirePermission(principal, Permission.ADMIN_STATIONS);
        return stationRepository.stream()
                .filter(nameContains(searchTerm)) // TODO Sort
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    private Predicate<Station> nameContains(@Nullable String searchTerm) {
        if (searchTerm == null) {
            return s -> true;
        } else {
            return station -> station.name().containsValue(s -> s.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }


    /// Fetches the station with the given ID, throwing an exception if the station does not exist.
    ///
    /// @param id        the ID of the station to fetch
    /// @param principal the principal performing the operation
    /// @return a [StationDto]
    /// @throws net.pkhapps.vera.server.domain.base.NonExistentAggregateException if no station with the given ID exists
    /// @throws net.pkhapps.vera.server.security.AccessDeniedException            if the principal lacks the [Permission#ADMIN_STATIONS] permission
    public StationDto get(StationId id, Principal principal) {
        accessControl.requirePermission(principal, Permission.ADMIN_STATIONS);
        return toDto(stationRepository.require(id));
    }

    /// Creates a new station.
    ///
    /// @param spec      a [CreateStationSpec] with initial data
    /// @param principal the principal performing the operation
    /// @return the created [StationDto]
    /// @throws net.pkhapps.vera.server.security.AccessDeniedException if the principal lacks the [Permission#ADMIN_STATIONS] permission
    public StationDto create(CreateStationSpec spec, Principal principal) {
        accessControl.requirePermission(principal, Permission.ADMIN_STATIONS);
        var aggregate = stationRepository.create(
                MultiLingualString.of(
                        Locales.SWEDISH, spec.nameSv(),
                        Locales.FINNISH, spec.nameFi()
                ),
                spec.location()
        );
        auditLogger.create(principal, aggregate);
        return toDto(aggregate);
    }

    /// Updates an existing station, throwing an exception if the station does not exist.
    ///
    /// @param id        the ID of the station to update
    /// @param spec      an [UpdateStationSpec] with changes to apply
    /// @param principal the principal performing the operation
    /// @return the updated [StationDto]
    /// @throws net.pkhapps.vera.server.domain.base.NonExistentAggregateException if no station with the given ID exists
    /// @throws net.pkhapps.vera.server.security.AccessDeniedException            if the principal lacks the [Permission#ADMIN_STATIONS] permission
    public StationDto update(StationId id, UpdateStationSpec spec, Principal principal) {
        accessControl.requirePermission(principal, Permission.ADMIN_STATIONS);
        var station = stationRepository.require(id);
        if (station.update(spec::applyTo)) {
            auditLogger.update(principal, station);
        }
        return toDto(station);
    }

    /// Deletes an existing station. If the station does not exist, nothing happens.
    ///
    /// @param id        the ID of the station to delete
    /// @param principal the principal performing the operation
    /// @throws net.pkhapps.vera.server.security.AccessDeniedException if the principal lacks the [Permission#ADMIN_STATIONS] permission
    public void delete(StationId id, Principal principal) {
        accessControl.requirePermission(principal, Permission.ADMIN_STATIONS);
        if (stationRepository.remove(id)) {
            auditLogger.delete(principal, Station.class, id);
        }
    }

    private StationDto toDto(Station station) {
        return new StationDto(
                station.id(),
                station.name().getOrDefault(Locales.SWEDISH, ""),
                station.name().getOrDefault(Locales.FINNISH, ""),
                station.location(),
                station.note()
        );
    }
}
