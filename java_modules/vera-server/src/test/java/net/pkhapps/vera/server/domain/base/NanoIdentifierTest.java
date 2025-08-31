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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class NanoIdentifierTest {

    static final class TestNanoIdentifier extends NanoIdentifier {

        public TestNanoIdentifier(String id) {
            super(id);
        }

        public TestNanoIdentifier() {
        }
    }

    @Test
    void can_create_random_ids() {
        var id = new TestNanoIdentifier();
        var id2 = new TestNanoIdentifier();
        assertNotEquals(id, id2);
        assertNotEquals(id.hashCode(), id2.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "lMP8oURT1u0gQYTpK316h",
            "olxliNQLl0iiFoO28JooR",
            "CfPaDebRuMZVjo8iI1M7l",
            "dpX2EX2K1om71h042YJln",
            "4-EobIp8tLo6IGfWvnW31",
            "2HiVUz6xF9MISR006CTTi",
            "HkYQKHBAPP_13tpg2TbhQ",
            "nkAcE4PdUNnjoJ0uqZHl8",
            "m40kVA4QKxio3AhjmTLH-",
            "6MB35R5uvc5r0iVtnpjWV",
    })
    void can_create_from_string(String valid) {
        var id = new TestNanoIdentifier(valid);
        assertEquals(valid, id.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "mFoqnPXA8EdgNHkwwYBC",  // To short
            "mFoqnPXA8EdgNHkwwYBC12" // To long
    })
    void verifies_length(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new TestNanoIdentifier(invalid));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "mFoqnPXA8EdgNHkwwYBC*",
            "Åäsdklökasdf243290asd",
            "£{sdf}_/:*£$kds891asd",
            " this is not a nanoid"
    })
    void verifies_alphabet(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new TestNanoIdentifier(invalid));
    }
}
