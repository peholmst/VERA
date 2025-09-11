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

import net.pkhapps.vera.server.util.serde.Input;
import net.pkhapps.vera.server.util.serde.Output;
import net.pkhapps.vera.server.util.serde.Serde;
import net.pkhapps.vera.server.util.wal.WalSerde;

import java.util.HashMap;

/// [WalSerde] for [RepositoryWalSnapshot].
final class RepositoryWalSnapshotSerde<T extends Aggregate<ID, S, ?>, ID extends Identifier, S extends Record> extends WalSerde<RepositoryWalSnapshot<T, ID, S>> {

    private final Class<T> aggregateType;
    private final Serde<ID> idSerde;
    private final Serde<S> stateSerde;

    /// Creates a new `RepositoryWalSnapshotSerde`.
    ///
    /// @param serdeId       the globally unique ID of this [WalSerde] (will be recorded in the WAL).
    /// @param aggregateType the aggregate type
    /// @param idSerde       the [Serde] for aggregate IDs
    /// @param stateSerde    the [Serde] for aggregate states
    public RepositoryWalSnapshotSerde(int serdeId, Class<T> aggregateType, Serde<ID> idSerde, Serde<S> stateSerde) {
        super(serdeId);
        this.aggregateType = aggregateType;
        this.idSerde = idSerde;
        this.stateSerde = stateSerde;
    }

    @Override
    public void writeTo(RepositoryWalSnapshot<T, ID, S> object, Output output) {
        writeHeader(output);
        output.writeInteger(object.size());
        object.forEach((id, state) -> {
            idSerde.writeTo(id, output);
            stateSerde.writeTo(state, output);
        });
    }

    @Override
    public RepositoryWalSnapshot<T, ID, S> readFrom(Input input) {
        verifyHeader(input);
        var size = input.readInteger();
        var aggregateStates = new HashMap<ID, S>(size);
        for (int i = 0; i < size; i++) {
            aggregateStates.put(idSerde.readFrom(input), stateSerde.readFrom(input));
        }
        return new RepositoryWalSnapshot<>(aggregateType, aggregateStates);
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof RepositoryWalSnapshot<?, ?, ?> walSnapshot
                && walSnapshot.aggregateType().equals(aggregateType);
    }
}
