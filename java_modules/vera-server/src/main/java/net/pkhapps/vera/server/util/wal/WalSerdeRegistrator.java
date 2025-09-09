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

/// Interface for registrators that makes [WalSerde]s available to the [WriteAheadLog].
///
/// This interface exists to make it possible to keep all [WalSerde] implementations package visible. This in turn
/// keeps the WAL mostly out of the public application API.
public interface WalSerdeRegistrator {

    /// Called by the [WriteAheadLog] to register the [WalSerde]s with the given `serdeRegistry`.
    ///
    /// @param serdeRegistry the registry to add [WalSerde]s to
    void registerSerdes(WalSerdeRegistry serdeRegistry);
}
