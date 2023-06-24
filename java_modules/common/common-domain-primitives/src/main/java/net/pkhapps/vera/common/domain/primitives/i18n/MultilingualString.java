/*
 * Copyright (c) 2023 Petter Holmström
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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.pkhapps.vera.common.domain.primitives.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for value objects that wrap strings in multiple languages.
 */
public abstract class MultilingualString {

    private final Map<Locale, String> values;
    private final Locale defaultLocale;

    /**
     * Initializing constructor.
     *
     * @param values        a map containing at least an entry for the default locale.
     * @param defaultLocale the default locale.
     * @throws IllegalArgumentException if there is no value for the default locale.
     */
    protected MultilingualString(@NotNull Map<Locale, String> values, @NotNull Locale defaultLocale) {
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(defaultLocale, "defaultLocale must not be null");
        if (!values.containsKey(defaultLocale)) {
            throw new IllegalArgumentException("values must contain an entry for the default locale");
        }
        this.values = Map.copyOf(values);
        this.defaultLocale = defaultLocale;
    }

    /**
     * Returns the value in the given locale. If there is no value for the given locale, the
     * {@linkplain #value() default value} is returned instead.
     *
     * @param locale the locale.
     * @return the value.
     */
    public @NotNull String value(@NotNull Locale locale) {
        return Optional.ofNullable(values.get(locale)).orElseGet(this::value);
    }

    /**
     * Returns the value in the default locale.
     *
     * @return the value.
     */
    public @NotNull String value() {
        return values.get(defaultLocale);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultilingualString that = (MultilingualString) o;
        return Objects.equals(values, that.values) && Objects.equals(defaultLocale, that.defaultLocale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, defaultLocale);
    }
}
