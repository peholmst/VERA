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

create sequence address_point_seq;

create table address_point
(
    id                bigint                   not null,
    municipality_code varchar(3)               not null,
    name_fin          text,
    name_swe          text,
    number            text,
    gid_source        bigint,
    geom              geometry(Point, 3067)    not null,
    updated_at        timestamp with time zone not null default now(),
    primary key (id),
    foreign key (municipality_code) references municipality_name (municipality_code)
);

create index address_point_geom_idx on address_point using gist (geom);
create index address_point_municipality_code_idx on address_point (municipality_code);
create index address_point_name_fin_idx on address_point (name_fin);
create index address_point_name_swe_idx on address_point (name_swe);
