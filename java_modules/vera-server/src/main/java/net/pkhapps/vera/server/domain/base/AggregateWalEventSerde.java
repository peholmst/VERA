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

import java.util.ArrayList;

/// [WalSerde] for [AggregateWalEvent].
final class AggregateWalEventSerde<T extends Aggregate<ID, S, E>, ID extends Identifier, S extends Record, E> extends WalSerde<AggregateWalEvent<T, ID, S, E>> {

    private final Class<T> aggregateType;
    private final Serde<ID> idSerde;
    private final Serde<E> eventSerde;

    /// Creates a new `AggregateWalEventSerde`.
    ///
    /// @param serdeId       the globally unique ID of this [WalSerde] (will be recorded in the WAL)
    /// @param aggregateType the aggregate type
    /// @param idSerde       the [Serde] for aggregate IDs
    /// @param eventSerde    the [Serde] for aggregate events
    public AggregateWalEventSerde(int serdeId, Class<T> aggregateType, Serde<ID> idSerde, Serde<E> eventSerde) {
        super(serdeId);
        this.aggregateType = aggregateType;
        this.idSerde = idSerde;
        this.eventSerde = eventSerde;
    }

    @Override
    public void writeTo(AggregateWalEvent<T, ID, S, E> object, Output output) {
        writeHeader(output);
        idSerde.writeTo(object.aggregateId(), output);
        output.writeInteger(object.size());
        object.forEach(event -> eventSerde.writeTo(event, output));
    }

    @Override
    public AggregateWalEvent<T, ID, S, E> readFrom(Input input) {
        verifyHeader(input);
        var id = idSerde.readFrom(input);
        var size = input.readInteger();
        var events = new ArrayList<E>(size);
        for (int i = 0; i < size; i++) {
            events.add(eventSerde.readFrom(input));
        }
        return new AggregateWalEvent<>(aggregateType, id, events);
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof AggregateWalEvent<?, ?, ?, ?> walEvent
                && walEvent.aggregateType().equals(aggregateType);
    }
}
