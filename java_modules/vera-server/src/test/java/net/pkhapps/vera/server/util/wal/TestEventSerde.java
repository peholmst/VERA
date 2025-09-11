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
import net.pkhapps.vera.server.util.serde.UnknownInputException;

import java.time.Instant;
import java.util.UUID;

class TestEventSerde extends WalSerde<TestEvent> {

    TestEventSerde(int serdeId) {
        super(serdeId);
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof TestEvent;
    }

    @Override
    public void writeTo(TestEvent object, Output output) {
        switch (object) {
            case TestEvent.MyFirstEvent myFirstEvent -> {
                writeHeader((short) 10, output);
                output.writeString(myFirstEvent.myString());
                output.writeInteger(myFirstEvent.myInt());
            }
            case TestEvent.MySecondEvent mySecondEvent -> {
                writeHeader((short) 20, output);
                output.writeLong(mySecondEvent.myInstant().getEpochSecond());
                output.writeInteger(mySecondEvent.myInstant().getNano());
                output.writeLong(mySecondEvent.myUUID().getMostSignificantBits());
                output.writeLong(mySecondEvent.myUUID().getLeastSignificantBits());
            }
            case TestEvent.MyThirdEvent myThirdEvent -> {
                writeHeader((short) 30, output);
                output.writeLong(myThirdEvent.myLong());
                output.writeBoolean(myThirdEvent.myBoolean());
            }
        }
    }

    @Override
    public TestEvent readFrom(Input input) {
        var subTypeId = verifyHeaderAndReadSubTypeId(input);
        switch (subTypeId) {
            case 10 -> {
                return new TestEvent.MyFirstEvent(
                        input.readString(),
                        input.readInteger()
                );
            }
            case 20 -> {
                return new TestEvent.MySecondEvent(
                        Instant.ofEpochSecond(input.readLong(), input.readInteger()),
                        new UUID(input.readLong(), input.readLong())
                );
            }
            case 30 -> {
                return new TestEvent.MyThirdEvent(
                        input.readLong(),
                        input.readBoolean()
                );
            }
            default -> throw new UnknownInputException("Unknown subTypeId: " + subTypeId);
        }
    }
}
