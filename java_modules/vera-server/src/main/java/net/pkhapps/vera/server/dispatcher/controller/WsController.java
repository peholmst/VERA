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

package net.pkhapps.vera.server.dispatcher.controller;

import io.javalin.Javalin;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class WsController {

    private static final Logger log = LoggerFactory.getLogger(WsController.class);
    private final OidcSessionManager oidcSessionManager;
    private final ScheduledExecutorService executorService;
    private final ConcurrentMap<String, WsSession> sessions = new ConcurrentHashMap<>();

    WsController(OidcSessionManager oidcSessionManager) {
        this.oidcSessionManager = oidcSessionManager;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void registerRoutes(Javalin javalin) {
        javalin
                .ws("/dispatcher/ws", ws -> {
                    ws.onConnect(this::onConnect);
                    ws.onClose(this::onClose);
                    ws.onMessage(this::onMessage);
                    ws.onError(this::onError);
                })
                .events(event -> event.serverStopping(this::onServerStop));
    }

    void onConnect(WsConnectContext context) {
        final var sessionId = context.sessionId();
        log.info("{} Connection opened from {}", sessionId, context.session.getRemoteAddress());
        sessions.put(sessionId, new WsSession(oidcSessionManager, executorService, sessionId, context.session));
    }

    void onClose(WsCloseContext context) {
        final var sessionId = context.sessionId();
        log.info("{} Connection closed", sessionId);
        sessions.remove(sessionId).onClose();
    }

    void onMessage(WsMessageContext context) {
        final var sessionId = context.sessionId();
        WsRequestMessage message;
        try {
            message = context.messageAsClass(WsRequestMessage.class);
        } catch (Exception e) {
            log.error("{} Could not parse incoming message", sessionId, e);
            getSession(sessionId).onUnknownMessage();
            return;
        }
        getSession(sessionId).onMessage(message);
    }

    void onError(WsErrorContext context) {
        getSession(context.sessionId()).onError(context.error());
    }

    void onServerStop() {
        log.info("Server stopping, closing all connections");
        sessions.forEach((_, wsSession) -> wsSession.onServerStop());
    }

    private WsSession getSession(String sessionId) {
        var session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalStateException("Unknown session id " + sessionId);
        }
        return session;
    }
}
