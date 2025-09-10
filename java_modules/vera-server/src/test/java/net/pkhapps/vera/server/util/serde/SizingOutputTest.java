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

package net.pkhapps.vera.server.util.serde;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SizingOutputTest {

    @Test
    void output_starts_empty() {
        assertThat(new SizingOutput().size()).isEqualTo(0);
    }

    @Test
    void sum_native_types() {
        var output = new SizingOutput();
        output.writeLong(1L);
        output.writeInteger(2);
        output.writeShort((short) 3);
        output.writeDouble(1.2);
        output.writeBoolean(false);
        assertThat(output.size()).isEqualTo(23);
    }

    @Test
    void sum_bytes() {
        var output = new SizingOutput();
        output.writeByte((byte) 1);
        output.writeBytes(new byte[2]);
        assertThat(output.size()).isEqualTo(3);
    }

    @Test
    void sum_string() {
        var output = new SizingOutput();
        output.writeString("hello");
        assertThat(output.size()).isEqualTo(9);
    }

    @Test
    void sum_nullable_string_with_null_value() {
        var output = new SizingOutput();
        output.writeNullableString(null);
        assertThat(output.size()).isEqualTo(1);
    }

    @Test
    void sum_nullable_string_with_nonnull_value() {
        var output = new SizingOutput();
        output.writeNullableString("hello");
        assertThat(output.size()).isEqualTo(10);
    }
}
