/*
 * Copyright (c) 2024 Petter Holmström
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

package net.pkhapps.vera.common.domain.primitives.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ResourceIdTest {

    @Test
    void random_resourceIds_can_be_generated() {
        var id1 = ResourceId.randomId();
        var id2 = ResourceId.randomId();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void resourceId_can_be_created_from_a_string() {
        var inputString = "M60QwA4vl9Si6Jr9SLzP2";
        var id = ResourceId.fromString(inputString);
        assertThat(id.toString()).isEqualTo(inputString);
    }

    @Test
    void resourceId_strings_must_be_21_characters_long() {
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP2z")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resourceId_strings_must_not_contain_illegal_characters() {
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP<")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP>")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP$")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzPÅ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP:")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP/")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP(")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP)")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP\\")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP.")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP\"")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP'")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resourceIds_can_be_serialized_to_json() throws Exception {
        var id = ResourceId.fromString("M60QwA4vl9Si6Jr9SLzP2");
        var jsonString = new ObjectMapper().writeValueAsString(id);
        assertThat(jsonString).isEqualTo(STR."\"\{id.toString()}\"");
    }

    @Test
    void resourceIds_can_be_deserialized_from_json() throws Exception {
        var jsonString = "\"M60QwA4vl9Si6Jr9SLzP2\"";
        var id = new ObjectMapper().readValue(jsonString, ResourceId.class);
        assertThat(id.toString()).isEqualTo("M60QwA4vl9Si6Jr9SLzP2");
    }
}
