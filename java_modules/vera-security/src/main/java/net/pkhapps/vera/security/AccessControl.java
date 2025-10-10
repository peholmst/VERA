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

package net.pkhapps.vera.security;

import java.security.Principal;

/// Defines an access control mechanism that can determine whether a given [Principal] is granted specific [Permission]s.
public interface AccessControl {

    /// Determines whether the given `principal` has been granted the specified `permission`.
    ///
    /// @param principal  the [Principal] whose permissions are being checked
    /// @param permission the [Permission] to check for
    /// @return true if the principal has the specified permission, false otherwise
    boolean hasPermission(Principal principal, Permission permission);

    /// Verifies that the given `principal` has the specified `permission`, throwing an [AccessDeniedException] if not.
    ///
    /// @param principal  the [Principal] whose permissions are being verified
    /// @param permission the permission to require
    /// @throws AccessDeniedException if the principal does not have the specified permission
    default void requirePermission(Principal principal, Permission permission) {
        if (!hasPermission(principal, permission)) {
            throw new AccessDeniedException("Insufficient privileges");
        }
    }
}
