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

import net.pkhapps.vera.security.SecurityException;
import net.pkhapps.vera.server.dispatcher.internal.DispatcherPrincipal;
import net.pkhapps.vera.server.util.ScheduledJob;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

class WsSession {

    private static final Logger log = LoggerFactory.getLogger(WsSession.class);
    private final OidcSessionManager oidcSessionManager;
    private final String sessionId;
    private final Session session;
    private final ScheduledJob closeWithoutAuthentication;
    private @Nullable ScheduledJob ping;
    private @Nullable DispatcherPrincipal principal;

    public WsSession(OidcSessionManager oidcSessionManager, String sessionId, Session session) {
        this.oidcSessionManager = oidcSessionManager;
        this.sessionId = sessionId;
        this.session = session;
        this.closeWithoutAuthentication = ScheduledJob.schedule(this::accessDenied, Duration.ofSeconds(5));
    }

    public synchronized void onMessage(WsRequestMessage message) {
        switch (message) {
            case WsRequestMessage.Authenticate authenticate -> {
                closeWithoutAuthentication.cancel();
                try {
                    accessGranted(oidcSessionManager.verifyOidcToken(authenticate.token()));
                } catch (SecurityException e) {
                    accessDenied();
                }
            }
            default -> throw new IllegalStateException("Unexpected message: " + message);
        }
    }

    public void send(WsResponseMessage message) {

    }

    public synchronized void close() {
        closeWithoutAuthentication.cancel();
        if (ping != null) {
            ping.cancel();
        }
        session.close();
    }

    private void accessGranted(DispatcherPrincipal principal) {
        log.info("{} Access granted to {}", sessionId, principal);
        this.principal = principal;
        schedulePing();
    }

    private void schedulePing() {
        this.ping = ScheduledJob.schedule(this::ping, Duration.ofSeconds(20));
    }

    private synchronized void ping() {
        try {
            session.getRemote().sendPing(null);
            schedulePing();
        } catch (IOException e) {
            log.error("{} Error sending ping", sessionId, e);
            close();
        }
    }

    private synchronized void accessDenied() {
        log.info("{} Access denied", sessionId);
        session.close(StatusCode.POLICY_VIOLATION, "Access denied");
    }
}
