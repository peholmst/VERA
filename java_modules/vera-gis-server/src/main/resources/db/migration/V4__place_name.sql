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

create sequence place_name_seq;

create table place_name
(
    id         bigint                   not null,
    name_fin   text,
    name_swe   text,
    gid_source bigint not null,
    geom       geometry(Point, 3067)    not null,
    updated_at timestamp with time zone not null default now(),
    primary key (id)
);

create index place_name_geom_idx on place_name using gist (geom);
create index place_name_fin_idx on place_name (name_fin);
create index place_name_swe_idx on place_name (name_swe);
create index place_name_gid_source on place_name (gid_source);
