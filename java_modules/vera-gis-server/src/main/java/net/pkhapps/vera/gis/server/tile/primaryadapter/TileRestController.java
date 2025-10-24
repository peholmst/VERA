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

package net.pkhapps.vera.gis.server.tile.primaryadapter;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrix;
import net.pkhapps.vera.gis.server.tile.domain.TileMatrixSetId;
import net.pkhapps.vera.gis.server.tile.primaryport.ForServingRasterTiles;
import net.pkhapps.vera.security.javalin.PrincipalUtil;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public final class TileRestController {

    private final ForServingRasterTiles forServingRasterTiles;

    public TileRestController(ForServingRasterTiles forServingRasterTiles) {
        this.forServingRasterTiles = forServingRasterTiles;
    }

    public void registerRoutes(Javalin javalin) {
        javalin.get("/gis/raster/{tileMatrixSet}/{z}/metadata.json", this::getTileMatrixMetadata);
        javalin.get("/gis/raster/{tileMatrixSet}/{z}/{x}/{y}.png", this::getTile);
    }

    void getTile(Context context) {
        var tileMatrixSet = TileMatrixSetId.of(context.pathParam("tileMatrixSet"));
        var z = Integer.parseInt(context.pathParam("z"));
        var x = Integer.parseInt(context.pathParam("x"));
        var y = Integer.parseInt(context.pathParam("y"));
        var principal = PrincipalUtil.getPrincipal(context);
        context
                .contentType(ContentType.IMAGE_PNG)
                .result(forServingRasterTiles.getTileAsPng(tileMatrixSet, z, x, y, principal));
    }

    void getTileMatrixMetadata(Context context) {
        var tileMatrixSet = TileMatrixSetId.of(context.pathParam("tileMatrixSet"));
        var z = Integer.parseInt(context.pathParam("z"));
        var tileMatrix = TileMatrix.of(tileMatrixSet, z);
        context.json(Map.of(
                "bounds", Map.of(
                        "left", tileMatrix.topLeft().x,
                        "top", tileMatrix.topLeft().y,
                        "right", tileMatrix.bottomRight().x,
                        "bottom", tileMatrix.bottomRight().y
                ),
                "resolutionM", tileMatrix.resolutionMetersPerPixel(),
                "matrixSize", tileMatrix.matrixSize(),
                "tileSizePx", tileMatrix.tileSizePixels()
        ));
    }
}
