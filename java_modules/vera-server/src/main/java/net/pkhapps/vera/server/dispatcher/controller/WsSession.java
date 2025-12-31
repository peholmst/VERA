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
import net.pkhapps.vera.server.util.Registration;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class WsSession {

    private static final Logger log = LoggerFactory.getLogger(WsSession.class);
    private final OidcSessionManager oidcSessionManager;
    private final String sessionId;
    private final Session session;
    private final ScheduledFuture<?> closeWithoutAuthentication;
    private final ScheduledFuture<?> ping;
    private final AtomicBoolean shutdownStarted = new AtomicBoolean(false);
    private @Nullable DispatcherPrincipal principal;
    private @Nullable Registration principalRevocationHandlerRegistration;

    WsSession(OidcSessionManager oidcSessionManager, ScheduledExecutorService executorService, String sessionId, Session session) {
        this.oidcSessionManager = oidcSessionManager;
        this.sessionId = sessionId;
        this.session = session;
        closeWithoutAuthentication = executorService.schedule(this::accessDenied, 5, TimeUnit.SECONDS);
        ping = executorService.scheduleWithFixedDelay(this::ping, 20, 20, TimeUnit.SECONDS);
    }

    /// Called by [WsController] when a new message arrives for this session.
    ///
    /// @param message the incoming message
    void onMessage(WsRequestMessage message) {
        switch (message) {
            case WsRequestMessage.Authenticate authMsg -> authenticate(authMsg);
            // TODO Implement support for more messages
            default -> throw new IllegalStateException("Unexpected message: " + message);
        }
    }

    /// Called by [WsController] when the connection for this session has been closed.
    void onClose() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        shutdown(StatusCode.NORMAL, null);
    }

    /// Called by [WsController] when the server is stopping.
    void onServerStop() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        log.info("{} Service restarting, shutting down session", sessionId);
        shutdown(StatusCode.SERVICE_RESTART, null);
    }

    /// Called by [WsController] when an error has been detected in this session.
    void onError(@Nullable Throwable error) {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        if (error != null) {
            log.error("{} An error occurred, shutting down session", sessionId, error);
        } else {
            log.error("{} An error occurred, shutting down session", sessionId);
        }
        shutdown(StatusCode.SERVER_ERROR, null);
    }

    /// Called by [WsController] when it has received a message for this session that it can't parse.
    void onUnknownMessage() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        log.error("{} Unknown incoming message, shutting down session", sessionId);
        shutdown(StatusCode.BAD_DATA, null);
    }

    private void send(WsResponseMessage message) {
        // TODO Implement me
    }

    private void authenticate(WsRequestMessage.Authenticate authenticate) {
        closeWithoutAuthentication.cancel(false);
        try {
            synchronized (this) {
                if (shutdownStarted.get()) {
                    return;
                }
                if (principal != null) {
                    log.warn("{} Sent another authentication message, ignoring", sessionId);
                    return;
                }
                principal = oidcSessionManager.processAccessToken(authenticate.token());
                principalRevocationHandlerRegistration = oidcSessionManager
                        .registerPrincipalRevocationHandler(principal, this::accessDenied);
                log.info("{} Access granted to {}", sessionId, principal);
            }
        } catch (SecurityException e) {
            accessDenied();
        }
    }

    private void ping() {
        try {
            synchronized (this) {
                if (shutdownStarted.get()) {
                    return;
                }
                session.getRemote().sendPing(null);
            }
        } catch (IOException e) {
            unrecoverableServerError(e, "Error sending ping");
        }
    }

    private void unrecoverableServerError(Throwable error, String reason) {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        log.error("{} {}, shutting down session", sessionId, reason, error);
        shutdown(StatusCode.SERVER_ERROR, null);
    }

    private void accessDenied() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        log.warn("{} Access denied, shutting down session", sessionId);
        shutdown(StatusCode.POLICY_VIOLATION, "Access denied");
    }

    private void shutdown(int statusCode, @Nullable String reason) {
        closeWithoutAuthentication.cancel(false);
        ping.cancel(false);
        synchronized (this) {
            if (principalRevocationHandlerRegistration != null) {
                principalRevocationHandlerRegistration.remove();
            }
            session.close(statusCode, reason);
        }
    }
}
