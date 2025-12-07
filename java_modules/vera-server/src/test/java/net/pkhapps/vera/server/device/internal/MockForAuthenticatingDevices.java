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

package net.pkhapps.vera.server.device.internal;

import net.pkhapps.vera.security.SecurityException;
import net.pkhapps.vera.server.device.DeviceId;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class MockForAuthenticatingDevices implements ForAuthenticatingDevices {

    private final ConcurrentMap<DeviceId, String> devices = new ConcurrentHashMap<>();
    private final ConcurrentMap<DeviceId, AtomicInteger> failedAuthenticationAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<DeviceId, AtomicInteger> successfulAuthenticationAttempts = new ConcurrentHashMap<>();

    public void addDevice(DeviceId deviceId, String token) {
        devices.put(deviceId, token);
    }

    @Override
    public DevicePrincipal authenticate(DeviceId deviceId, String authorizationToken) throws SecurityException {
        var expectedToken = devices.get(deviceId);
        if (expectedToken == null) {
            throw new SecurityException("No device with id " + deviceId + " was found");
        }
        if (!expectedToken.equals(authorizationToken)) {
            failedAuthenticationAttempts.computeIfAbsent(deviceId, k -> new AtomicInteger()).incrementAndGet();
            throw new SecurityException("Wrong authorization token");
        }
        successfulAuthenticationAttempts.computeIfAbsent(deviceId, k -> new AtomicInteger()).incrementAndGet();
        return new DevicePrincipal(deviceId);
    }

    public void assertSuccessfulAuthenticationAttempts(DeviceId deviceId, int expected) {
        var atomic = successfulAuthenticationAttempts.computeIfAbsent(deviceId, k -> new AtomicInteger());
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAtomic(atomic, actual -> assertThat(actual).isEqualTo(expected));
    }

    public void assertFailedAuthenticationAttempts(DeviceId deviceId, int expected) {
        var atomic = failedAuthenticationAttempts.computeIfAbsent(deviceId, k -> new AtomicInteger());
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAtomic(atomic, actual -> assertThat(actual).isEqualTo(expected));
    }
}
