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

package net.pkhapps.vera.server.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;


/// Defines a component responsible for recording audit events.
///
/// Implementations of this interface capture security-relevant or business-critical actions performed by a [Principal],
/// along with optional contextual metadata.
public interface AuditLogger {

    /// Records an audit event for the given [Principal] performing the specified action, including any associated
    /// metadata.
    ///
    /// @param principal the principal who performed the action
    /// @param action    a short description of identifier of the action performed
    /// @param metadata  additional contextual data related to the event
    void log(Principal principal, String action, Map<String, String> metadata);

    /// Records an audit event for the given [Principal] performing the specified action, without any additional
    /// metadata.
    ///
    /// This is a convenience method that delegates to [#log(Principal, String, Map)] with an empty metadata map.
    ///
    /// @param principal the principal who performed the action
    /// @param action    a short description of identifier of the action performed
    default void log(Principal principal, String action) {
        log(principal, action, Collections.emptyMap());
    }
}
