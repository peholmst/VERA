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
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.serde.UnknownInputException;

/// Base class for [Serde]s that serialize and deserialize [WalEvent]s and [WalSnapshot]s.
public abstract class WalSerde<T> implements Serde<T> {

    private final int serdeId;

    protected WalSerde(int serdeId) {
        this.serdeId = serdeId;
    }

    /// Returns a globally unique serde ID. This ID will be written to the WAL and used to look up the correct
    /// [WalSerde] instance when deserializing.
    ///
    /// @return the globally unique serde ID
    public final int serdeId() {
        return serdeId;
    }

    /// Returns whether this [Serde] can serialize the specified `object`.
    ///
    /// @param object the object to check
    /// @return true if the object can be serialized by this [Serde], false otherwise
    public abstract boolean supports(Object object);

    /// Writes a header to the given `output` with the given `subTypeId`. This header is used by [WalSerde]s that
    /// serialize and deserialize multiple types (with a common base type).
    ///
    /// @param subTypeId the subtype ID
    /// @param output    the output to write the header to
    /// @see #verifyHeaderAndReadSubTypeId(Input)
    protected final void writeHeader(short subTypeId, Output output) {
        output.writeLong((long) serdeId << 32 | (long) subTypeId << 16);
    }

    /// Writes a header with *no* `subTypeId` to the given `output`. This header is used by [WalSerde]s that only
    /// serialize and deserialize a single type.
    ///
    /// @param output the output to write the header to
    /// @see #verifyHeader(Input)
    protected final void writeHeader(Output output) {
        writeHeader((short) 0, output);
    }

    /// Verifies the header from the given `input` and returns the `subTypeId`.
    ///
    /// @param input the input to read the header from
    /// @return the `subTypeId`
    /// @throws UnknownInputException if the header does not contain the correct `serdeId`
    /// @see #writeHeader(short, Output)
    /// @see #serdeId()
    protected final short verifyHeaderAndReadSubTypeId(Input input) {
        var header = input.readLong();
        var serdeId = (int) (header >> 32);
        if (serdeId != this.serdeId) {
            throw new UnknownInputException("Unknown serdeId: " + serdeId);
        }
        return (short) ((header >> 16) & 0x0000FFFF);
    }

    /// Verifies the header from the given `input`.
    ///
    /// @param input the input to read the header from
    /// @throws UnknownInputException if the header does not contain the correct `serdeId`, or contains a `subTypeId` that is not 0.
    /// @see #writeHeader(Output)
    /// @see #serdeId()
    protected final void verifyHeader(Input input) {
        var subTypeId = verifyHeaderAndReadSubTypeId(input);
        if (subTypeId != 0) {
            throw new UnknownInputException("Unknown subTypeId: " + subTypeId);
        }
    }

    /// Reads and returns the `serdeId` from the header of given `input`. This method does not reset the input and moves
    /// its internal position ahead by the length of a long integer.
    ///
    /// This method has package visibility because it is used by the [WalSerdeManager]. It is placed in this class to
    /// keep the methods dealing with the header format in the same file.
    ///
    /// @param input the input to read the header and serdeId from
    /// @return the serdeId
    static int readSerdeId(Input input) {
        var header = input.readLong();
        return (int) (header >> 32);
    }
}
