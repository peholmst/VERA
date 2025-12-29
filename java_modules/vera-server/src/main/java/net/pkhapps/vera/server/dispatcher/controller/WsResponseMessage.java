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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import net.pkhapps.vera.server.resource.ResourceFilterId;

import java.time.Instant;
import java.util.List;

@JsonTypeInfo(
        include = JsonTypeInfo.As.PROPERTY,
        use = JsonTypeInfo.Id.SIMPLE_NAME,
        property = "type"
)
sealed interface WsResponseMessage {

    @JsonTypeName("resource_filter_updated")
    record ResourceFilterUpdated(
            long sequenceId,
            Instant timestamp,
            ResourceFilterId filter
    ) implements WsResponseMessage {
    }

    @JsonTypeName("resource_filters_replaced")
    record ResourceFiltersReplaced(
            long sequenceId,
            Instant timestamp,
            List<ResourceFilter> filters
    ) implements WsResponseMessage {
    }

    @JsonTypeName("resource_updated")
    record ResourceUpdated(
            long sequenceId,
            Instant timestamp,
            Resource resource
    ) implements WsResponseMessage {
    }

    @JsonTypeName("resources_replaced")
    record ResourcesReplaced(
            long sequenceId,
            Instant timestamp,
            List<Resource> resources
    ) implements WsResponseMessage {
    }

    @JsonTypeName("station_updated")
    record StationUpdated(
            long sequenceId,
            Instant timestamp,
            Station station
    ) implements WsResponseMessage {
    }

    @JsonTypeName("stations_replaced")
    record StationsReplaced(
            long sequenceId,
            Instant timestamp,
            List<Station> stations
    ) implements WsResponseMessage {
    }
}
