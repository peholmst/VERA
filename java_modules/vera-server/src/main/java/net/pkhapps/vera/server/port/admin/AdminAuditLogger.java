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

import net.pkhapps.vera.server.domain.base.Aggregate;
import net.pkhapps.vera.server.domain.base.Identifier;
import net.pkhapps.vera.server.security.AuditLogger;

import java.security.Principal;
import java.util.Map;

final class AdminAuditLogger {

    private static final String KEY_AGGREGATE_TYPE = "aggregateType";
    private static final String KEY_AGGREGATE_ID = "aggregateId";

    private final AuditLogger auditLogger;

    AdminAuditLogger(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public <A extends Aggregate<?, ?, ?>> void create(Principal principal, A aggregate) {
        auditLogger.log(principal, "create", Map.of(
                KEY_AGGREGATE_TYPE, aggregate.getClass().getSimpleName(),
                KEY_AGGREGATE_ID, aggregate.id().toString()
        ));
    }

    public <A extends Aggregate<?, ?, ?>> void update(Principal principal, A aggregate) {
        auditLogger.log(principal, "update", Map.of(
                KEY_AGGREGATE_TYPE, aggregate.getClass().getSimpleName(),
                KEY_AGGREGATE_ID, aggregate.id().toString()
        ));
    }

    public <A extends Aggregate<ID, ?, ?>, ID extends Identifier> void delete(Principal principal,
                                                                              Class<A> aggregateType,
                                                                              ID aggregateId) {
        auditLogger.log(principal, "delete", Map.of(
                KEY_AGGREGATE_TYPE, aggregateType.getSimpleName(),
                KEY_AGGREGATE_ID, aggregateId.toString()
        ));
    }
}
