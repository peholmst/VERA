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

import java.nio.charset.StandardCharsets;

/// Implementation of [Output] that does not write anything anywhere, but calculates the size in bytes of the data
/// that would have been written.
///
/// This can be used for double-pass writes where the first pass calculates the size, and the second pass writes into
/// a buffer with the correct size.
public final class SizingOutput implements Output {

    private int size;

    @Override
    public void writeLong(long l) {
        size += Long.BYTES;
    }

    @Override
    public void writeInteger(int i) {
        size += Integer.BYTES;
    }

    @Override
    public void writeShort(short s) {
        size += Short.BYTES;
    }

    @Override
    public void writeDouble(double d) {
        size += Double.BYTES;
    }

    @Override
    public void writeBoolean(boolean b) {
        size += Byte.BYTES;
    }

    @Override
    public void writeString(String s) {
        size += s.getBytes(StandardCharsets.UTF_8).length + Integer.BYTES;
    }

    @Override
    public void writeNullableString(@Nullable String s) {
        if (s == null) {
            size += Byte.BYTES;
        } else {
            size += s.getBytes(StandardCharsets.UTF_8).length + Integer.BYTES + Byte.BYTES;
        }
    }

    @Override
    public void writeByte(byte b) {
        size += Byte.BYTES;
    }

    @Override
    public void writeBytes(byte[] bytes) {
        size += bytes.length;
    }

    /// Returns the size of the data that has been "written" so far.
    ///
    /// @return the size of the written data in bytes.
    public int size() {
        return size;
    }
}
