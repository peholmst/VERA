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

package net.pkhapps.vera.server.port;

import net.pkhapps.vera.server.domain.model.DomainModel;
import net.pkhapps.vera.server.port.admin.ForStationAdministration;
import net.pkhapps.vera.server.security.AccessControl;
import net.pkhapps.vera.server.security.AuditLogger;
import org.slf4j.LoggerFactory;

/// Creates and configures the primary ports (driving ports) of this application.
public final class PrimaryPorts {

    public final ForStationAdministration forStationAdministration;
    public final AccessControl accessControl;
    public final AuditLogger auditLogger;

    private PrimaryPorts(DomainModel domainModel) {
        accessControl = (principal, permission) -> {
            return true; // TODO Implement real access control
        };
        auditLogger = (principal, action, metadata) -> {
            // TODO Implement real audit logger
            LoggerFactory.getLogger(AuditLogger.class).info("{}: {} (metadata: {})", principal.getName(), action, metadata);
        };
        forStationAdministration = new ForStationAdministration(domainModel.stationRepository, accessControl, auditLogger);
    }

    /// Creates a new `PrimaryPorts` that interacts with the given `domainModel`.
    ///
    /// @param domainModel the [DomainModel] to interact with
    /// @return a new `PrimaryPorts`
    public static PrimaryPorts create(DomainModel domainModel) {
        return new PrimaryPorts(domainModel);
    }
}
