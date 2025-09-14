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

package net.pkhapps.vera.server.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;
import net.pkhapps.vera.server.adapter.rest.admin.StationAdminController;
import net.pkhapps.vera.server.domain.base.NonExistentAggregateException;
import net.pkhapps.vera.server.domain.model.station.StationId;
import net.pkhapps.vera.server.port.PrimaryPorts;
import net.pkhapps.vera.server.security.AccessDeniedException;
import net.pkhapps.vera.server.security.AuthenticationRequiredException;

import static net.pkhapps.vera.server.adapter.IdentifierJsonSerde.registerIdentifier;

/// Factory for creating the various adapters that connect the application to the outside world.
public final class AdapterFactory {

    private AdapterFactory() {
    }

    /// Creates and configures a new [Javalin] instance *with no routes*. This is mainly useful for testing.
    ///
    /// @return a new [Javalin]
    /// @see #createJavalin(PrimaryPorts)
    public static Javalin createJavalin() {
        return Javalin
                .create(config -> {
                    config.jsonMapper(new JavalinJackson(createObjectMapper(), true));
                })
                .before(context -> {
                    // TODO Extract authentication token from header, turn into principal unless set already
                    PrincipalUtil.setPrincipal(context, () -> "not-implemented-yet");
                })
                .exception(AccessDeniedException.class, (e, ctx) -> ctx.status(HttpStatus.FORBIDDEN))
                .exception(AuthenticationRequiredException.class, (e, ctx) -> ctx.status(HttpStatus.UNAUTHORIZED))
                .exception(NonExistentAggregateException.class, (e, ctx) -> ctx.status(HttpStatus.NOT_FOUND))
                .exception(IllegalArgumentException.class, (e, ctx) -> ctx.status(HttpStatus.BAD_REQUEST));
    }

    /// Creates and configures a new [Javalin] instance with routes and controllers for the given `primaryPorts`.
    ///
    /// @param primaryPorts the primary ports to adapt
    /// @return a new [Javalin]
    public static Javalin createJavalin(PrimaryPorts primaryPorts) {
        var javalin = createJavalin();
        new StationAdminController(primaryPorts.forStationAdministration).registerRoutes(javalin);
        return javalin;
    }

    /// Creates and configures a new [ObjectMapper].
    ///
    /// @return a new [ObjectMapper]
    public static ObjectMapper createObjectMapper() {
        var module = new SimpleModule();
        registerIdentifier(module, StationId.class, StationId::of);

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
