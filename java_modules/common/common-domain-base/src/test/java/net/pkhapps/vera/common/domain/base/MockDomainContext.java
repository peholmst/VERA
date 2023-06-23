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
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link DomainContext} intended to be used in unit tests.
 */
public class MockDomainContext implements DomainContext {

    private final UserId userId;
    private final UserIpAddress userIpAddress;
    private final Clock clock;
    private final Locale locale;

    private MockDomainContext(@Nullable UserId userId,
                              @Nullable UserIpAddress userIpAddress,
                              @NotNull Clock clock,
                              @NotNull Locale locale) {
        this.userId = userId;
        this.userIpAddress = userIpAddress;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.locale = Objects.requireNonNull(locale, "locale must not be null");
    }

    @Override
    public @NotNull Optional<UserId> currentUser() {
        return Optional.ofNullable(userId);
    }

    @Override
    public @NotNull Optional<UserIpAddress> currentUserIpAddress() {
        return Optional.ofNullable(userIpAddress);
    }

    @Override
    public @NotNull Clock clock() {
        return clock;
    }

    @Override
    public @NotNull Locale locale() {
        return locale;
    }

    /**
     * Creates a new domain context with a {@link Clock#fixed(Instant, ZoneId) fixed} clock, the default time zone,
     * default locale, no user and no IP-address.
     *
     * @param now the instant that the clock should be fixed to.
     * @return a new domain context.
     */
    public static @NotNull DomainContext fixedTime(@NotNull Instant now) {
        return new MockDomainContext(null, null,
                Clock.fixed(now, ZoneId.systemDefault()),
                Locale.getDefault()
        );
    }

    /**
     * Creates a new domain context with the default clock, default locale, no user and no IP-address.
     *
     * @return a new domain context.
     */
    public static @NotNull DomainContext currentTime() {
        return new MockDomainContext(null, null, Clock.systemDefaultZone(), Locale.getDefault());
    }
}
