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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/// Implementation of [Input] thar reads from an underlying [ByteBuffer].
public final class BufferInput implements Input {

    private final ByteBuffer byteBuffer;
    private final int initialPosition;

    private BufferInput(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.initialPosition = byteBuffer.position();
    }

    /// Returns a new [BufferInput] that reads from the given `array`.
    ///
    /// @param array the array to read from
    /// @return a new [BufferInput]
    public static BufferInput wrap(byte[] array) {
        return new BufferInput(ByteBuffer.wrap(array));
    }

    /// Returns a new [BufferInput] that reads from the given `array`, starting at `offset`
    /// and not going longer than `length` bytes.
    ///
    /// @param array  the array to read from
    /// @param offset the position to start reading from
    /// @param length the maximum number of bytes to read
    /// @return a new [BufferInput]
    public static BufferInput wrap(byte[] array, int offset, int length) {
        return new BufferInput(ByteBuffer.wrap(array, offset, length));
    }

    /// Returns a new [BufferInput] that reads from the given [ByteBuffer]. Reading will
    /// start at the buffer's current position and [#reset()] will return to this position.
    ///
    /// @param byteBuffer the [ByteBuffer] to read from
    /// @return a new [BufferInput]
    public static BufferInput wrap(ByteBuffer byteBuffer) {
        return new BufferInput(byteBuffer);
    }

    @Override
    public void reset() {
        byteBuffer.position(initialPosition);
    }

    @Override
    public long readLong() {
        try {
            return byteBuffer.getLong();
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public int readInteger() {
        try {
            return byteBuffer.getInt();
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public short readShort() {
        try {
            return byteBuffer.getShort();
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public double readDouble() {
        try {
            return byteBuffer.getDouble();
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public boolean readBoolean() {
        try {
            return byteBuffer.get() == 1;
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public String readString() {
        try {
            var size = byteBuffer.getInt();
            var bytes = new byte[size];
            byteBuffer.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public @Nullable String readNullableString() {
        try {
            var containsNull = byteBuffer.get() == 0;
            if (containsNull) {
                return null;
            } else {
                return readString();
            }
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public byte readByte() {
        try {
            return byteBuffer.get();
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }

    @Override
    public void readBytes(byte[] dst) {
        try {
            byteBuffer.get(dst);
        } catch (BufferUnderflowException ex) {
            throw new InputUnderflowException(ex.getMessage());
        }
    }
}
