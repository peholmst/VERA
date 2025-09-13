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

import org.jspecify.annotations.NullMarked;

@NullMarked
module vera.server {
    requires org.slf4j;
    requires org.jspecify;
    requires jnanoid;
    requires org.json;
    requires io.javalin;
    requires com.fasterxml.jackson.databind;

    // JSON mapping used by the REST APIs
    exports net.pkhapps.vera.server.domain.base to com.fasterxml.jackson.databind;
    exports net.pkhapps.vera.server.domain.model.geo to com.fasterxml.jackson.databind;
    exports net.pkhapps.vera.server.domain.model.station to com.fasterxml.jackson.databind;
    exports net.pkhapps.vera.server.port.admin to com.fasterxml.jackson.databind;

}