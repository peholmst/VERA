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

import static org.assertj.core.api.Assertions.assertThat;

public final class SerdeTestUtils {

    private SerdeTestUtils() {
    }

    public static <T> void assertSerializationAndDeserializationProducesEqualObject(Serde<T> serde, T original) {
        var sizingOutput = new SizingOutput();
        serde.writeTo(original, sizingOutput);
        var bufferOutput = BufferOutput.allocate(sizingOutput.size());
        serde.writeTo(original, bufferOutput);

        var bufferInput = BufferInput.wrap(bufferOutput.buffer().array());
        var copy = serde.readFrom(bufferInput);
        assertThat(copy).isEqualTo(original);
    }
}
