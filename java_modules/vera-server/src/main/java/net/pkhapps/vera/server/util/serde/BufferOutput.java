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

import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/// Implementation of [Output] that writes to an underlying [ByteBuffer].
public final class BufferOutput implements Output {

    private final ByteBuffer byteBuffer;

    private BufferOutput(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    /// Returns a new [BufferOutput] with a new [ByteBuffer] with the given `capacity`.
    ///
    /// @param capacity the capacity of the buffer
    /// @return a new [BufferOutput]
    public static BufferOutput allocate(int capacity) {
        return wrap(ByteBuffer.allocate(capacity));
    }

    /// Returns a new [BufferOutput] that writes to the given [ByteBuffer].
    /// This method does not flip the buffer. Writing will start that the buffer's current
    /// position.
    ///
    /// @param buf the [ByteBuffer] to write to
    /// @return a new [BufferOutput]
    public static BufferOutput wrap(ByteBuffer buf) {
        return new BufferOutput(buf);
    }

    @Override
    public void writeLong(long l) {
        byteBuffer.putLong(l);
    }

    @Override
    public void writeInteger(int i) {
        byteBuffer.putInt(i);
    }

    @Override
    public void writeShort(short s) {
        byteBuffer.putShort(s);
    }

    @Override
    public void writeDouble(double d) {
        byteBuffer.putDouble(d);
    }

    @Override
    public void writeBoolean(boolean b) {
        byteBuffer.put(b ? (byte) 1 : (byte) 0);
    }

    @Override
    public void writeString(String s) {
        var b = s.getBytes(StandardCharsets.UTF_8);
        byteBuffer.putInt(b.length);
        byteBuffer.put(b);
    }

    @Override
    public void writeNullableString(@Nullable String s) {
        if (s == null) {
            byteBuffer.put((byte) 0);
        } else {
            byteBuffer.put((byte) 1);
            writeString(s);
        }
    }

    @Override
    public void writeByte(byte b) {
        byteBuffer.put(b);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        byteBuffer.put(bytes);
    }

    /// Returns the backing [ByteBuffer].
    ///
    /// @return the [ByteBuffer]
    public ByteBuffer buffer() {
        return byteBuffer;
    }
}
