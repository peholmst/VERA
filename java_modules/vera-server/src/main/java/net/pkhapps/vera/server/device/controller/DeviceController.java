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
import io.javalin.websocket.*;
import net.pkhapps.vera.security.AuthenticationRequiredException;
import net.pkhapps.vera.security.SecurityException;
import net.pkhapps.vera.server.device.DeviceId;
import net.pkhapps.vera.server.device.IncomingMessage;
import net.pkhapps.vera.server.device.OutgoingMessage;
import net.pkhapps.vera.server.device.OutgoingMessageId;
import net.pkhapps.vera.server.device.internal.DevicePrincipal;
import net.pkhapps.vera.server.device.internal.ForAuthenticatingDevices;
import net.pkhapps.vera.server.device.internal.ForReceivingFromDevices;
import net.pkhapps.vera.server.device.internal.ForSendingToDevices;
import net.pkhapps.vera.server.util.serde.BufferInput;
import net.pkhapps.vera.server.util.serde.BufferOutput;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.SizingOutput;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// Websocket controller for communicating with devices. Devices can:
/// - authenticate themselves
/// - receive messages from the server
/// - send messages to the server
/// - send and receive keep alive messages (ping-pong)
final class DeviceController implements ForSendingToDevices {

    // TODO What happens with all the exceptions thrown by the handler methods?
    // TODO Keep-alive ping-pongs
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    private final ForReceivingFromDevices forReceivingFromDevices;
    private final ForAuthenticatingDevices forAuthenticatingDevices;
    private final ConcurrentMap<DeviceId, DeviceSession> activeSessions = new ConcurrentHashMap<>();
    private final Serde<OutgoingMessageEnvelope> outgoingMessageSerde;
    private final Serde<IncomingMessageEnvelope> incomingMessageSerde;
    private final Clock clock;

    DeviceController(ForReceivingFromDevices forReceivingFromDevices,
                     ForAuthenticatingDevices forAuthenticatingDevices,
                     Serde<OutgoingMessageEnvelope> outgoingMessageSerde,
                     Serde<IncomingMessageEnvelope> incomingMessageSerde,
                     Clock clock) {
        this.forReceivingFromDevices = forReceivingFromDevices;
        this.forAuthenticatingDevices = forAuthenticatingDevices;
        this.outgoingMessageSerde = outgoingMessageSerde;
        this.incomingMessageSerde = incomingMessageSerde;
        this.clock = clock;
    }

    public void registerRoutes(Javalin javalin) {
        javalin
                .ws("/device/ws/{deviceId}", ws -> {
                    ws.onConnect(this::onConnect);
                    ws.onClose(this::onCloseByClient);
                    ws.onMessage(this::onMessage);
                    ws.onBinaryMessage(this::onBinaryMessage);
                    ws.onError(this::onError);
                });
    }

    void onConnect(WsConnectContext context) {
        var deviceId = getDeviceId(context);
        var sessionId = context.sessionId();
        var remoteAddress = context.session.getRemoteAddress();
        log.debug("Connection from device {} (sessionId={}, remoteAddr={})", deviceId, sessionId, remoteAddress);
        try {
            var principal = authenticate(context);
            addSession(principal, context);
        } catch (SecurityException e) {
            log.warn("Closing connection from device {} due to authentication failure (sessionId={}, remoteAddr={})",
                    deviceId, sessionId, remoteAddress);
            context.closeSession(WsCloseStatus.POLICY_VIOLATION, "Access denied");
        }
    }

    void onCloseByClient(WsCloseContext context) {
        removeSession(getDeviceId(context));
    }

    void onMessage(WsMessageContext context) {
        var message = context.message();
        var deviceId = getDeviceId(context);
        if (message.startsWith("ACK ")) {
            var acknowledgedMessageId = OutgoingMessageId.of(message.substring("ACK ".length()));
            log.debug("Received acknowledgment of outgoing message {} from {}", acknowledgedMessageId, deviceId);
            forReceivingFromDevices.messageAckFromDevice(deviceId, acknowledgedMessageId);
        } else {
            log.warn("Received unknown message from {}", deviceId);
        }
    }

    void onBinaryMessage(WsBinaryMessageContext context) {
        log.trace("onBinaryMessage: {}", context.sessionId());
        var input = BufferInput.wrap(context.data(), context.offset(), context.length());
        var message = incomingMessageSerde.readFrom(input);
        var sender = getDeviceId(context);

        log.debug("Received message {} from {}", message.messageId(), sender);
        forReceivingFromDevices.messageFromDevice(new IncomingMessage(message.messageId(), sender,
                message.priority(), message.timestamp(), clock.instant(), message.payload()));

        log.debug("Acknowledging incoming message {}", message.messageId());
        context.send("ACK " + message.messageId().value());

        log.debug("Acknowledged incoming message {}", message.messageId());
    }

    void onError(WsErrorContext context) {
        var deviceId = getDeviceId(context);
        log.error("Disconnecting device {} because of an error", deviceId, context.error());
        // Always close on error, the client will automatically reconnect.
        var session = activeSessions.get(deviceId);
        if (session != null) {
            session.closeAndTryAgain();
        } else {
            context.closeSession(WsCloseStatus.TRY_AGAIN_LATER);
        }
    }

    private DevicePrincipal authenticate(WsConnectContext context) {
        var deviceId = getDeviceId(context);
        var token = context.header("Authorization");
        var sessionId = context.sessionId();
        var remoteAddress = context.session.getRemoteAddress();
        if (token == null) {
            log.warn("Device {} provided no authorization header (sessionId={}, remoteAddr={})",
                    deviceId, sessionId, remoteAddress);
            throw new AuthenticationRequiredException("No authorization header");
        }
        try {
            var principal = forAuthenticatingDevices.authenticate(deviceId, token);
            log.info("Device {} successfully authenticated (sessionId={}, remoteAddr={})",
                    deviceId, sessionId, remoteAddress);
            return principal;
        } catch (SecurityException e) {
            log.warn("Device {} failed to authenticate (sessionId={}, remoteAddr={})",
                    deviceId, sessionId, remoteAddress, e);
            throw e;
        }
    }

    private DeviceId getDeviceId(WsContext context) {
        return DeviceId.of(context.pathParam("deviceId"));
    }

    private void addSession(DevicePrincipal principal, WsConnectContext context) {
        var deviceId = principal.deviceId();
        var sessionId = context.sessionId();
        var remoteAddress = context.session.getRemoteAddress();
        var existingSession = activeSessions.put(deviceId, new DeviceSession(principal, sessionId, context.session,
                () -> removeSession(deviceId)));
        if (existingSession == null) {
            log.info("Added session for device {} (sessionId={}, remoteAddr={})",
                    deviceId, sessionId, remoteAddress);
        } else {
            log.info("Replaced session for device {} (sessionId={}, remoteAddr={})",
                    deviceId, sessionId, remoteAddress);
        }
        if (existingSession != null) {
            existingSession.close(WsCloseStatus.TRY_AGAIN_LATER, "Another client connected");
        }
    }

    private void removeSession(DeviceId deviceId) {
        var session = activeSessions.remove(deviceId);
        if (session != null) {
            log.info("Removed session for device {} (sessionId={}, remoteAddr={})", deviceId, session.sessionId,
                    session.session.getRemoteAddress());
        }
    }

    @Override
    public void sendToDevices(OutgoingMessage message) {
        var envelope = new OutgoingMessageEnvelope(message.messageId(), message.priority(), message.queuedOn(),
                message.payload());
        var sizing = new SizingOutput();
        outgoingMessageSerde.writeTo(envelope, sizing);
        var buffer = BufferOutput.allocate(sizing.size());
        outgoingMessageSerde.writeTo(envelope, buffer);

        message.recipients().forEach(recipient -> Thread.ofVirtual().start(() -> {
            var session = activeSessions.get(recipient);
            if (session != null) {
                log.debug("Sending message {} to {}", message.messageId(), recipient);
                try {
                    session.sendBytes(ByteBuffer.wrap(buffer.array()));
                } catch (IOException e) {
                    log.error("Error sending message {} to {}", message.messageId(), recipient, e);
                    session.closeAndTryAgain();
                }
            }
        }));
    }

    record DeviceSession(DevicePrincipal principal, String sessionId, Session session, Runnable onCloseCallback) {

        void closeAndTryAgain() {
            close(WsCloseStatus.TRY_AGAIN_LATER, WsCloseStatus.TRY_AGAIN_LATER.message());
        }

        void close(WsCloseStatus status, String reason) {
            log.debug("Closing session for device {} (sessionId={}, status={}, reason=[{}])",
                    principal.deviceId(), sessionId, status.getCode(), reason);
            try {
                synchronized (this) {
                    session.close(status.getCode(), reason);
                }
            } finally {
                onCloseCallback.run();
            }
        }

        synchronized void sendBytes(ByteBuffer buffer) throws IOException {
            session.getRemote().sendBytes(buffer);
        }
    }
}
