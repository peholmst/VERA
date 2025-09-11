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

/// Interface used by [Serde]s to write data to some destination.
///
/// Data is written to the destination in order. Writing to a specific position in the destination is not supported.
public interface Output {

    /// Writes the given long integer to the output destination.
    ///
    /// @param l the long integer to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeLong(long l);

    /// Writes the given integer to the output destination.
    ///
    /// @param i the integer to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeInteger(int i);

    /// Writes the given short integer to the output destination.
    ///
    /// @param s the short to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeShort(short s);

    /// Writes the given double to the output destination.
    ///
    /// @param d the double to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeDouble(double d);

    /// Writes the given boolean to the output destination.
    ///
    /// @param b the boolean to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeBoolean(boolean b);

    /// Writes the given string to the output destination.
    ///
    /// @param s the string to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeString(String s);

    /// Writes the given nullable string to the output destination.
    ///
    /// @param s the string to write, may be `null`
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeNullableString(@Nullable String s);

    /// Writes the given byte to the output destination.
    ///
    /// @param b the byte to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeByte(byte b);

    /// Writes the given bytes to the output destination.
    ///
    /// @param bytes the bytes to write
    /// @throws OutputOverflowException if there is not enough room in the destination
    void writeBytes(byte[] bytes);
}
