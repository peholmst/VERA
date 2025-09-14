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

package net.pkhapps.vera.server.adapter;

import io.javalin.http.Context;
import net.pkhapps.vera.server.security.AuthenticationRequiredException;

import java.security.Principal;

/// Utility class for getting and setting a [Principal] in a [Context].
public final class PrincipalUtil {

    private static final String ATTRIBUTE_KEY = "principal";

    private PrincipalUtil() {
    }

    /// Retrieves the principal stored in the given `context`.
    ///
    /// @param context the [Context] to fetch the principal from
    /// @return the [Principal]
    /// @throws AuthenticationRequiredException if the context did not contain a principal
    public static Principal getPrincipal(Context context) {
        Principal principal = context.attribute(ATTRIBUTE_KEY);
        if (principal == null) {
            throw new AuthenticationRequiredException("No principal in context");
        }
        return principal;
    }

    /// Stores the given `principal` in the given `context`.
    ///
    /// @param context   the [Context] to store the principal in
    /// @param principal the principal to store
    public static void setPrincipal(Context context, Principal principal) {
        assert principal != null;
        context.attribute(ATTRIBUTE_KEY, principal);
    }
}
