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

package net.pkhapps.vera.server.domain.model.i18n;

import net.pkhapps.vera.server.domain.base.ValueObject;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/// Value object representing a string in multiple languages.
public final class MultiLingualString implements ValueObject {

    private final Map<Locale, String> values;

    /// Constructor used by the Serde and by factory methods.
    ///
    /// **Note:** This constructor does *not* copy the `values` map for performance reasons. Callers must make sure the
    /// map is effectively immutable.
    ///
    /// @param values a map containing at least one entry
    MultiLingualString(Map<Locale, String> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Map must contain at least one entry");
        }
        this.values = values;
    }

    /// Creates a new [MultiLingualString] with a single entry.
    ///
    /// @param locale the locale
    /// @param value  the value
    public static MultiLingualString of(Locale locale, String value) {
        return new MultiLingualString(Map.of(locale, value));
    }

    /// Creates a new [MultiLingualString] with two entries.
    ///
    /// @param locale1 the locale of the first entry
    /// @param value1  the value of the first entry
    /// @param locale2 the locale of the second entry
    /// @param value2  the value of the second entry
    public static MultiLingualString of(Locale locale1, String value1, Locale locale2, String value2) {
        return new MultiLingualString(Map.of(locale1, value1, locale2, value2));
    }

    /// Creates a new [MultiLingualString] from the specified map.
    ///
    /// @param values a map of values
    /// @throws IllegalArgumentException if the `values` map is empty
    public static MultiLingualString of(Map<Locale, String> values) {
        return new MultiLingualString(Map.copyOf(values));
    }

    /// Checks whether this multilingual string contains a value for the given `locale`.
    ///
    /// @param locale the locale to check
    /// @return true if a value exists, false otherwise
    public boolean contains(Locale locale) {
        return values.containsKey(locale);
    }

    /// Returns the value of the given `locale`.
    ///
    /// @param locale the locale to fetch
    /// @return the value, or an empty `Optional` if not found
    public Optional<String> get(Locale locale) {
        return Optional.ofNullable(values.get(locale));
    }

    /// Returns the number of entries in this multilingual string (minimum 1).
    ///
    /// @return the number of entries
    public int size() {
        return values.size();
    }

    /// Performs the given `action` for each entry in this multilingual string.
    ///
    /// @param action the action to perform for each entry
    public void forEach(BiConsumer<Locale, String> action) {
        values.forEach(action);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MultiLingualString that = (MultiLingualString) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }
}
