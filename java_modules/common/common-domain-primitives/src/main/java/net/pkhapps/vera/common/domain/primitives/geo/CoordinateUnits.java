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
package net.pkhapps.vera.common.domain.primitives.geo;

import org.jetbrains.annotations.NotNull;

/**
 * Enumeration of common coordinate units used in VERA.
 */
public enum CoordinateUnits implements CoordinateUnit {

	DEGREE("°"),
	METER("m");

	private final String symbol;

	CoordinateUnits(@NotNull String symbol) {
        this.symbol = symbol;
    }

	@Override
	public @NotNull String symbol() {
		return symbol;
	}
}
