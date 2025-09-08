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

import java.util.*;
import java.util.stream.Collectors;

// TODO document me
public final class MultiLingualString implements ValueObject {

    private final Map<Locale, String> values;

    private MultiLingualString(Map<Locale, String> values) {
        this.values = values;
    }

    public static MultiLingualString of(Locale locale, String value) {
        return new MultiLingualString(Map.of(locale, value));
    }

    public static MultiLingualString of(Locale locale1, String value1, Locale locale2, String value2) {
        return new MultiLingualString(Map.of(locale1, value1, locale2, value2));
    }

    public static MultiLingualString of(Map<Locale, String> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Map must contain at least one entry");
        }
        return new MultiLingualString(Map.copyOf(values));
    }

    public static MultiLingualString of(Collection<LocalizedString> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Collection must contain at least one entry");
        }
        return new MultiLingualString(values.stream().collect(Collectors.toMap(LocalizedString::locale, LocalizedString::value)));
    }

    public boolean contains(Locale locale) {
        return values.containsKey(locale);
    }

    public Optional<String> get(Locale locale) {
        return Optional.ofNullable(values.get(locale));
    }

    public int size() {
        return values.size();
    }

    public Collection<LocalizedString> entries() {
        // TODO This should be cached!
        return values.entrySet().stream().map(entry -> new LocalizedString(entry.getKey(), entry.getValue())).toList();
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

    public record LocalizedString(Locale locale, String value) {
    }
}
