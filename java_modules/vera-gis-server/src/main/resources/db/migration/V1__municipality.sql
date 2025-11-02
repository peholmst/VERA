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

create table municipality_code
(
    municipality_code     varchar(3)               not null,
    municipality_name_fin text                     not null,
    municipality_name_swe text                     not null,
    active                boolean                  not null default true,
    updated_at            timestamp with time zone not null default now(),
    primary key (municipality_code)
);

create index municipality_name_fin_idx on municipality_code (municipality_name_fin);
create index municipality_name_swe_idx on municipality_code (municipality_name_swe);

create sequence municipality_border_seq;

create table municipality_border
(
    id                bigint                       not null,
    municipality_code varchar(3)                   not null,
    gid_source bigint not null,
    geom              geometry(MultiPolygon, 3067) not null,
    updated_at        timestamp with time zone     not null default now(),
    primary key (id),
    foreign key (municipality_code) references municipality_code (municipality_code)
);

create index municipality_border_code_idx on municipality_border (municipality_code);
create index municipality_border_geom_idx on municipality_border using gist (geom);
create index municipality_border_gid_source on municipality_border (gid_source);
