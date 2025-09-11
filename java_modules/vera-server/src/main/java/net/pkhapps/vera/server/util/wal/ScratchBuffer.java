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

package net.pkhapps.vera.server.util.wal;

import org.jspecify.annotations.Nullable;

/// Scratch buffer used by various classes to avoid re-allocating memory in cases where only a single thread is using
/// the memory at a time.
final class ScratchBuffer {
    private byte @Nullable [] buffer = null;

    /// Ensures the scratch buffer has at least the specified `length` and returns it.
    ///
    /// @param length the minimum length of the scratch buffer
    /// @return an array that has at least the given length, but may be longer
    byte[] ensureCapacity(int length) {
        if (buffer == null || buffer.length < length) {
            buffer = new byte[length];
        }
        return buffer;
    }
}
