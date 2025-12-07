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

package net.pkhapps.vera.server.device.controller;

import io.javalin.Javalin;
import net.pkhapps.vera.server.adapter.AdapterFactory;
import net.pkhapps.vera.server.device.DeviceId;
import net.pkhapps.vera.server.device.OutgoingMessageId;
import net.pkhapps.vera.server.device.internal.ForSendingToDevices;
import net.pkhapps.vera.server.device.internal.MockForAuthenticatingDevices;
import net.pkhapps.vera.server.device.internal.MockForReceivingFromDevices;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

class DeviceControllerTest {

    private final MockForAuthenticatingDevices forAuthenticatingDevices = new MockForAuthenticatingDevices();
    private final MockForReceivingFromDevices forReceivingFromDevices = new MockForReceivingFromDevices();
    private final Javalin app;
    private final ForSendingToDevices forSendingToDevices;
    private final Clock clock = Clock.systemUTC();
    private final HttpClient client = HttpClient.newHttpClient();

    DeviceControllerTest() {
        app = AdapterFactory.createJavalin();
        forSendingToDevices = DeviceControllerFactory.createController(app, forReceivingFromDevices, forAuthenticatingDevices, clock);
    }

    @BeforeEach
    void setUp() {
        app.start(1234);
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void devices_authenticate_upon_connection() {
        var deviceId = DeviceId.random();
        forAuthenticatingDevices.addDevice(deviceId, "mytoken");

        try (var ws = openWebSocketClient(deviceId, "mytoken")) {
            forAuthenticatingDevices.assertSuccessfulAuthenticationAttempts(deviceId, 1);
            ws.assertOpen();
        }
    }

    @Test
    void invalid_token_disconnects_immediately() {
        var deviceId = DeviceId.random();
        forAuthenticatingDevices.addDevice(deviceId, "mytoken");

        try (var ws = openWebSocketClient(deviceId, "invalid_token")) {
            forAuthenticatingDevices.assertFailedAuthenticationAttempts(deviceId, 1);
            ws.assertClosed();
        }
    }

    @Test
    void missing_token_disconnects_immediately() {
        var deviceId = DeviceId.random();
        forAuthenticatingDevices.addDevice(deviceId, "mytoken");

        try (var ws = openWebSocketClient(deviceId, null)) {
            ws.assertClosed();
        }
    }

    @Test
    void old_session_disconnects_on_new_connection() {
        var deviceId = DeviceId.random();
        forAuthenticatingDevices.addDevice(deviceId, "mytoken");

        try (var firstWebSocket = openWebSocketClient(deviceId, "mytoken")) {
            firstWebSocket.assertOpen();
            try (var secondWebSocket = openWebSocketClient(deviceId, "mytoken")) {
                firstWebSocket.assertClosed();
                secondWebSocket.assertOpen();
            }
        }
    }

    @Test
    void message_acknowledgements_are_passed_on() {
        var deviceId = DeviceId.random();
        forAuthenticatingDevices.addDevice(deviceId, "mytoken");
        var messageId = OutgoingMessageId.random();

        try (var ws = openWebSocketClient(deviceId, "mytoken")) {
            ws.sendTextMessage("ACK " + messageId);
            forReceivingFromDevices.assertMessageAckFromDevice(deviceId, messageId);
        }
    }

    // TODO incoming messages are passed on and acknowledged
    // TODO outgoing messages are delivered (to multiple recipients if needed)
    // TODO errors result in disconnection


    private WebSocketTestClient openWebSocketClient(DeviceId deviceId, @Nullable String authorizationToken) {
        Map<String, String> headers = new HashMap<>();
        if (authorizationToken != null) {
            headers.put("Authorization", authorizationToken);
        }
        return new WebSocketTestClient(getDeviceUri(deviceId), headers);
    }

    private URI getDeviceUri(DeviceId deviceId) {
        return URI.create("ws://localhost:" + app.port() + "/device/ws/" + deviceId);
    }
}
