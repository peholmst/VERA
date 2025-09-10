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

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

class BufferOutputInputTest {

    @Test
    void test_round_trip_of_all_types() {
        var output = BufferOutput.allocate(100);
        output.writeLong(123L);
        output.writeInteger(456);
        output.writeShort((short) 789);
        output.writeDouble(3.14);
        output.writeBoolean(true);
        output.writeString("hello");
        output.writeNullableString("world");
        output.writeNullableString(null);
        output.writeByte((byte) 4);
        output.writeBytes(new byte[]{1, 2, 3});

        var buffer = output.buffer();
        buffer.flip();

        var input = BufferInput.wrap(buffer);
        assertThat(input.readLong()).isEqualTo(123L);
        assertThat(input.readInteger()).isEqualTo(456);
        assertThat(input.readShort()).isEqualTo((short) 789);
        assertThat(input.readDouble()).isEqualTo(3.14);
        assertThat(input.readBoolean()).isEqualTo(true);
        assertThat(input.readString()).isEqualTo("hello");
        assertThat(input.readNullableString()).isEqualTo("world");
        assertThat(input.readNullableString()).isNull();
        assertThat(input.readByte()).isEqualTo((byte) 4);
        assertThat(input.readBytes(3)).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void reset_starts_from_the_beginning() {
        var buffer = ByteBuffer.allocate(64);
        buffer.putLong(123L);
        buffer.putLong(456L);
        buffer.flip();

        var input = BufferInput.wrap(buffer);
        assertThat(input.readLong()).isEqualTo(123L);
        input.reset();
        assertThat(input.readLong()).isEqualTo(123L);
    }

    @Test
    void reset_takes_offset_into_account() {
        var writeBuf = ByteBuffer.allocate(64);
        writeBuf.putLong(123L);
        writeBuf.putLong(456L);
        writeBuf.putLong(789L);

        var readBuf = ByteBuffer.wrap(writeBuf.array(), 8, 16);
        var input = BufferInput.wrap(readBuf);
        assertThat(input.readLong()).isEqualTo(456L);
        input.reset();
        assertThat(input.readLong()).isEqualTo(456L);
    }
}
