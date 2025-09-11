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

/// Interface used by [Serde]s to read data from some source.
///
/// Data is read from the source in order. Reading from a specific position in the source is not supported. Going back
/// to the beginning and starting over is possible through the [#reset()] method.
public interface Input {

    /// Resets the internal position so that the next call to a read method starts from the beginning of the source.
    void reset();

    /// Reads a long integer from the input source.
    ///
    /// @return the long integer
    /// @throws InputUnderflowException if there is not enough data left in the source
    long readLong();

    /// Reads an integer from the input source.
    ///
    /// @return the integer
    /// @throws InputUnderflowException if there is not enough data left in the source
    int readInteger();

    /// Reads a short integer from the input source.
    ///
    /// @return the short integer
    /// @throws InputUnderflowException if there is not enough data left in the source
    short readShort();

    /// Reads a double from the input source.
    ///
    /// @return the double
    /// @throws InputUnderflowException if there is not enough data left in the source
    double readDouble();

    /// Reads a boolean from the input source.
    ///
    /// @return the boolean
    /// @throws InputUnderflowException if there is not enough data left in the source
    boolean readBoolean();

    /// Reads a string from the input source.
    ///
    /// @return the string
    /// @throws InputUnderflowException if there is not enough data left in the source
    String readString();

    /// Reads a nullable string from the input source.
    ///
    /// @return the string, may be `null`
    /// @throws InputUnderflowException if there is not enough data left in the source
    @Nullable
    String readNullableString();

    /// Reads a single byte from the input source.
    ///
    /// @return the byte
    /// @throws InputUnderflowException if there is not enough data left in the source
    byte readByte();

    /// Reads `dst.length` bytes into the given `dst` array from the input source.
    ///
    /// @param dst the array to read bytes into
    /// @throws InputUnderflowException if there is not enough data left in the source
    void readBytes(byte[] dst);

    /// Reads `length` bytes from the input source into a newly allocated array.
    ///
    /// @param length the number of bytes to read
    /// @return a newly allocated array of bytes
    /// @throws InputUnderflowException if there is not enough data left in the source
    default byte[] readBytes(int length) {
        var b = new byte[length];
        readBytes(b);
        return b;
    }
}
