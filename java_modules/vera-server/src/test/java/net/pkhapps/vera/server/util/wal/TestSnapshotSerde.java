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

import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;

import java.util.ArrayList;

class TestSnapshotSerde extends WalSerde<TestSnapshot> {

    TestSnapshotSerde(int serdeId) {
        super(serdeId);
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof TestSnapshot;
    }

    @Override
    public String toString() {
        return "%s[serdeId=%d]".formatted(getClass().getSimpleName(), serdeId());
    }

    @Override
    public void writeTo(TestSnapshot object, Output output) {
        writeHeader(output);
        output.writeInteger(object.strings().size());
        object.strings().forEach(output::writeString);
    }

    @Override
    public TestSnapshot readFrom(Input input) {
        verifyHeader(input);
        var size = input.readInteger();
        var strings = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            strings.add(input.readString());
        }
        return new TestSnapshot(strings);
    }
}
