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

package net.pkhapps.vera.common.domain.primitives.address;

import net.pkhapps.vera.common.domain.primitives.i18n.Locales;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AddressNameTest {

    @Test
    void address_names_can_be_monolingual() {
        var name = "Main street";
        var an = AddressName.monolingual(name);
        assertThat(an.value(Locales.FINNISH)).isEqualTo(name);
        assertThat(an.value(Locales.SWEDISH)).isEqualTo(name);
        assertThat(an.value()).isEqualTo(name);
    }

    @Test
    void address_names_can_be_bilingual() {
        var fi = "Pääkatu";
        var sv = "Huvudgatan";
        var an = AddressName.bilingual(fi, sv);
        assertThat(an.value(Locales.FINNISH)).isEqualTo(fi);
        assertThat(an.value(Locales.SWEDISH)).isEqualTo(sv);
    }

    @Test
    void finnish_is_the_default_locale() {
        var fi = "Pääkatu";
        var sv = "Huvudgatan";
        var an = AddressName.bilingual(fi, sv);
        assertThat(an.value()).isEqualTo(fi);
    }

    @Test
    void address_name_cannot_be_blank() {
        assertThatThrownBy(() -> AddressName.monolingual("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void address_names_have_a_max_length() {
        var input = StringUtils.repeat("A", AddressName.MAX_LENGTH);
        assertThat(AddressName.monolingual(input).value()).isEqualTo(input);
        assertThatThrownBy(() -> AddressName.monolingual(input + "B")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void address_names_cannot_contain_certain_special_characters() {
        assertThatThrownBy(() -> AddressName.monolingual("<")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual(">")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual(";")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual("\"")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual("\t")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual("\n")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AddressName.monolingual("\r")).isInstanceOf(IllegalArgumentException.class);
    }
}
