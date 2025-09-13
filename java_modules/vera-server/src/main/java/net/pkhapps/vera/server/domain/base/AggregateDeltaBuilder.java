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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// Helper class for [Aggregate]s that want to group multiple events together into a single WAL record, but only
/// if data has actually been modified.
///
/// This typically happens inside some kind of update-method, that calls [#update(Object, Object, Function)] multiple
/// times. At the end, if there are any updates, the list of event is built and appended to the WAL as a single record.
public final class AggregateDeltaBuilder<E> {

    private final List<E> events = new ArrayList<>();

    /// Creates a new event if `newValue` and `currentValue` are not equal. If they are equal, nothing happens.
    ///
    /// @param newValue     the new value
    /// @param currentValue the current value
    /// @param eventFactory a factory for creating a change event for the new value, should it be different from the current value
    /// @see #containsEvents()
    /// @see #build()
    public <V> void update(V newValue, V currentValue, Function<V, E> eventFactory) {
        if (!Objects.equals(newValue, currentValue)) {
            events.add(eventFactory.apply(newValue));
        }
    }

    /// Checks if this builder contains any events. If this method returns false after calling [#update(Object, Object, Function)],
    /// it means the new values are equal to the current values.
    ///
    /// @return true if the builder contains at least one event, false otherwise
    public boolean containsEvents() {
        return !events.isEmpty();
    }

    /// Returns a new list of the events in this builder.
    ///
    /// @return an unmodifiable list of events, may be empty
    public List<E> build() {
        return List.copyOf(events);
    }
}
