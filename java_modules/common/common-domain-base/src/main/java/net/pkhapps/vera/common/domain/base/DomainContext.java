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

package net.pkhapps.vera.common.domain.base;

import net.pkhapps.vera.common.domain.primitives.identity.UserId;
import net.pkhapps.vera.common.domain.primitives.identity.UserIpAddress;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.Locale;
import java.util.Optional;

/**
 * Interface to be used for domain operations that may require access to the context in which they run, without
 * resorting to {@link ThreadLocal}. All contextual (or potentially contextual) operations should accept and instance of
 * this interface as a parameter unless the object itself contains an instance of this interface.
 */
public interface DomainContext {

    /**
     * Returns the current user, if there is one.
     */
    @NotNull Optional<UserId> currentUser();

    /**
     * Returns the IP-address of the current user, if available.
     */
    @NotNull Optional<UserIpAddress> currentUserIpAddress();

    /**
     * Returns the clock to use to get the current time and time zone.
     */
    @NotNull Clock clock();

    /**
     * Returns the locale to use to generate localized messages.
     */
    @NotNull Locale locale();
}
