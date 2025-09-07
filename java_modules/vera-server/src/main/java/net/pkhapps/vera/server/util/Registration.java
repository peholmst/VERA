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

package net.pkhapps.vera.server.util;

import java.util.stream.Stream;

/// Functional interface for removing registrations.
@FunctionalInterface
public interface Registration {

    /// Removes the registration. This method is always idempotent, meaning it can be called multiple times. Only the
    /// first call removes the registration. Any subsequent calls do nothing.
    void remove();

    /// Groups the given registrations into a single `Registration` handle. The [#remove()] method removes all
    /// registrations.
    ///
    /// @param registrations the registrations to group together
    /// @return a new `Registration` handle that can be used to remove all the registrations
    static Registration of(Registration... registrations) {
        return () -> {
            Stream.of(registrations).forEach(Registration::remove);
        };
    }
}
