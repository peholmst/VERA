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

import net.pkhapps.vera.server.device.DeviceId;
import net.pkhapps.vera.server.device.IncomingMessage;
import net.pkhapps.vera.server.device.OutgoingMessageId;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MockForReceivingFromDevices implements ForReceivingFromDevices {

    private final ConcurrentMap<DeviceId, List<OutgoingMessageId>> receivedAcks = new ConcurrentHashMap<>();

    @Override
    public void messageAckFromDevice(DeviceId deviceId, OutgoingMessageId acknowledgedMessageId) {
        receivedAcks.computeIfAbsent(deviceId, _ -> Collections.synchronizedList(new ArrayList<>()))
                .add(acknowledgedMessageId);
    }

    @Override
    public void messageFromDevice(IncomingMessage message) {

    }

    public void assertMessageAckFromDevice(DeviceId deviceId, OutgoingMessageId acknowledgedMessageId) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> receivedAcks.getOrDefault(deviceId, Collections.emptyList())
                        .contains(acknowledgedMessageId));
    }
}
