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
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OidcController {

    private static final Logger log = LoggerFactory.getLogger(OidcController.class);
    private final OidcSessionManager oidcSessionManager;

    OidcController(OidcSessionManager oidcSessionManager) {
        this.oidcSessionManager = oidcSessionManager;
    }

    public void registerRoutes(Javalin javalin) {
        javalin.post("/dispatcher/logout", this::onLogout);
    }

    void onLogout(Context context) {
        var logoutToken = context.formParam("logout_token");
        if (logoutToken != null) {
            oidcSessionManager.processLogoutToken(logoutToken);
        } else {
            log.warn("Received unknown logout request from {}", context.req().getRemoteAddr());
        }
    }
}
