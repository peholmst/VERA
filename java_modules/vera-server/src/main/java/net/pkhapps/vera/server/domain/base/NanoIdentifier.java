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

package net.pkhapps.vera.server.domain.base;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/// Implementation of [Identifier] that uses a Nano ID.
public abstract class NanoIdentifier implements Identifier {

    private static final Set<Integer> VALID_CHARS;

    private final String id;

    static {
        var chars = new HashSet<Integer>();
        for (var ch : NanoIdUtils.DEFAULT_ALPHABET) {
            chars.add((int) ch);
        }
        VALID_CHARS = Collections.unmodifiableSet(chars);
    }

    /// Creates a new `NanoIdentifier` with the given Nano ID.
    ///
    /// @param id the Nano ID
    /// @throws IllegalArgumentException if the given Nano ID is invalid
    protected NanoIdentifier(String id) {
        if (id.length() != NanoIdUtils.DEFAULT_SIZE) {
            throw new IllegalArgumentException("ID has invalid length");
        }
        if (id.chars().anyMatch(ch -> !VALID_CHARS.contains(ch))) {
            throw new IllegalArgumentException("Invalid character in ID");
        }
        this.id = id;
    }

    /// Creates a new `NanoIdentifier` with a random Nano ID.
    protected NanoIdentifier() {
        this.id = NanoIdUtils.randomNanoId();
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NanoIdentifier that = (NanoIdentifier) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
