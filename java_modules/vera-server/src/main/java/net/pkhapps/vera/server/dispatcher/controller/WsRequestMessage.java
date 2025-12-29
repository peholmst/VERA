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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import net.pkhapps.vera.server.domain.model.geo.Wgs84Bounds;
import net.pkhapps.vera.server.resource.ResourceFilterId;

@JsonTypeInfo(
        include = JsonTypeInfo.As.PROPERTY,
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes(@JsonSubTypes.Type(WsRequestMessage.Authenticate.class))
sealed interface WsRequestMessage {

    @JsonTypeName("AUTH")
    record Authenticate(String token) implements WsRequestMessage {
    }

    @JsonTypeName("sub_resource_filters")
    record SubscribeToResourceFilters() implements WsRequestMessage {
    }

    @JsonTypeName("sub_resource_updates")
    record SubscribeToResourceUpdates(
            ResourceFilterId id
    ) implements WsRequestMessage {
    }

    @JsonTypeName("sub_station_updates")
    record SubscribeToStationUpdates(
            Wgs84Bounds bounds
    ) implements WsRequestMessage {
    }
}
