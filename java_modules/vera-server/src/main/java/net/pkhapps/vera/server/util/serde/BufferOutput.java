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

import java.nio.BufferOverflowException;
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
        try {
            byteBuffer.putLong(l);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeInteger(int i) {
        try {
            byteBuffer.putInt(i);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeShort(short s) {
        try {
            byteBuffer.putShort(s);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeDouble(double d) {
        try {
            byteBuffer.putDouble(d);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeBoolean(boolean b) {
        try {
            byteBuffer.put(b ? (byte) 1 : (byte) 0);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeString(String s) {
        try {
            var b = s.getBytes(StandardCharsets.UTF_8);
            byteBuffer.putInt(b.length);
            byteBuffer.put(b);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeNullableString(@Nullable String s) {
        try {
            if (s == null) {
                byteBuffer.put((byte) 0);
            } else {
                byteBuffer.put((byte) 1);
                writeString(s);
            }
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeByte(byte b) {
        try {
            byteBuffer.put(b);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        try {
            byteBuffer.put(bytes);
        } catch (BufferOverflowException ex) {
            throw new OutputOverflowException(ex.getMessage(), ex);
        }
    }

    /// Returns the backing [ByteBuffer].
    ///
    /// @return the [ByteBuffer]
    public ByteBuffer buffer() {
        return byteBuffer;
    }
}
