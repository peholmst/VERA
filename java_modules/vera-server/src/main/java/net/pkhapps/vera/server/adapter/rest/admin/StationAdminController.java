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

package net.pkhapps.vera.server.adapter.rest.admin;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import net.pkhapps.vera.server.adapter.PrincipalUtil;
import net.pkhapps.vera.server.domain.model.station.StationId;
import net.pkhapps.vera.server.port.admin.CreateStationSpec;
import net.pkhapps.vera.server.port.admin.ForStationAdministration;
import net.pkhapps.vera.server.port.admin.UpdateStationSpec;

/// REST controller for the [ForStationAdministration] port.
public final class StationAdminController {

    private final ForStationAdministration forStationAdministration;

    /// Creates a new `StationAdminController`.
    ///
    /// @param forStationAdministration the port to adapt
    public StationAdminController(ForStationAdministration forStationAdministration) {
        this.forStationAdministration = forStationAdministration;
    }

    /// Registers the routes with the given [Javalin] instance.
    public void registerRoutes(Javalin javalin) {
        javalin
                .get("/admin/stations", this::list)
                .post("/admin/stations", this::create)
                .get("/admin/stations/{id}", this::get)
                .put("/admin/stations/{id}", this::update)
                .delete("/admin/stations/{id}", this::delete);
    }

    void list(Context context) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    void get(Context context) {
        var stationId = StationId.of(context.pathParam("id"));
        context.json(forStationAdministration.get(stationId, PrincipalUtil.getPrincipal(context)));
    }

    void create(Context context) {
        var spec = context.bodyAsClass(CreateStationSpec.class);
        var station = forStationAdministration.create(spec, PrincipalUtil.getPrincipal(context));
        context.status(HttpStatus.CREATED).json(station);
    }

    void update(Context context) {
        var stationId = StationId.of(context.pathParam("id"));
        var spec = context.bodyAsClass(UpdateStationSpec.class);
        context.json(forStationAdministration.update(stationId, spec, PrincipalUtil.getPrincipal(context)));
    }

    void delete(Context context) {
        var stationId = StationId.of(context.pathParam("id"));
        forStationAdministration.delete(stationId, PrincipalUtil.getPrincipal(context));
        context.status(HttpStatus.NO_CONTENT);
    }
}
