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

import java.net.URL;

public final class DispatcherControllerFactory {

    private DispatcherControllerFactory() {
    }

    public static void createController(Javalin javalin) {
        // TODO Get URIs from config
        try {
            var sessionManager = new OidcSessionManager(
                    new URL("https://saturn.pkhapps.net/auth/realms/vera-dev/protocol/openid-connect/certs"),
                    new URL("https://saturn.pkhapps.net/auth/realms/vera-dev"),
                    "https://saturn.pkhapps.net/auth/realms/vera-dev/protocol/openid-connect/token/introspect");
            var wsController = new WsController(sessionManager);
            wsController.registerRoutes(javalin);
            var oidcController = new OidcController(sessionManager);
            oidcController.registerRoutes(javalin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
