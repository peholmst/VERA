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

package net.pkhapps.vera.common.domain.primitives.i18n;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MultilingualStringTest {

    static class TestString extends MultilingualString {

        TestString(@NotNull Map<Locale, String> values, @NotNull Locale defaultLocale) {
            super(values, defaultLocale);
        }
    }

    @Test
    void multilingual_string_must_always_contain_a_default_locale() {
        assertThat(new TestString(Map.of(Locales.FINNISH, "value"), Locales.FINNISH).value()).isEqualTo("value");
        assertThatThrownBy(() -> new TestString(Collections.emptyMap(), Locales.FINNISH)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TestString(Map.of(Locales.SWEDISH, "value"), Locales.FINNISH)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void multilingual_string_reverts_to_default_for_unknown_locales() {
        var s = new TestString(Map.of(Locales.FINNISH, "value"), Locales.FINNISH);
        assertThat(s.value(Locale.US)).isEqualTo("value");
    }

    @Test
    void multilingual_string_can_contain_different_values_for_different_locales() {
        var s = new TestString(Map.of(Locales.FINNISH, "finnish", Locales.SWEDISH, "swedish"), Locales.FINNISH);
        assertThat(s.value(Locales.SWEDISH)).isEqualTo("swedish");
        assertThat(s.value(Locales.FINNISH)).isEqualTo("finnish");
    }

    @Test
    void multilingual_string_cannot_contain_any_null_strings() {
        var map = new HashMap<Locale, String>();
        map.put(Locales.FINNISH, null);
        assertThatThrownBy(() -> new TestString(map, Locales.FINNISH)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void multilingual_strings_are_value_objects() {
        var input = Map.of(Locales.FINNISH, "finnish", Locales.SWEDISH, "swedish");
        var s1 = new TestString(input, Locales.FINNISH);
        var s2 = new TestString(input, Locales.FINNISH);

        assertThat(s1).isNotSameAs(s2);
        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }
}
