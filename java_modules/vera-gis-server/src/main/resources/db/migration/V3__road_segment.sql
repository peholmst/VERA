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

create sequence road_segment_seq;

create table road_segment
(
    id                bigint                     not null,
    municipality_code varchar(3)                 not null,
    name_fin          text,
    name_swe          text,
    left_from         integer,
    left_to           integer,
    right_from        integer,
    right_to          integer,
    left_range        int4range generated always as (int4range(left_from, left_to, '[]')) stored,
    right_range       int4range generated always as (int4range(right_from, right_to, '[]')) stored,
    geom              geometry(LineString, 3067) not null,
    updated_at        timestamp with time zone   not null default now(),
    primary key (id),
    foreign key (municipality_code) references municipality_name (municipality_code)
);

create index road_segment_geom_idx on road_segment using gist (geom);
create index road_segment_municipality_code_idx on road_segment (municipality_code);
create index road_segment_name_fin_idx on road_segment (name_fin);
create index road_segment_name_swe_idx on road_segment (name_swe);
create index road_segment_left_range_gist on road_segment using gist (left_range);
create index road_segment_right_range_gist on road_segment using gist (right_range);
