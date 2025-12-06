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

package net.pkhapps.vera.server.device;

/// Enumeration of message priorities. The message priority determines in which order a message is sent.
public enum MessagePriority {
    // Do not change the order of this enum, as the ordinals are used for serialization and deserialization.
    // Adding new constants is OK, but you can't remove old ones.
    HIGH,
    NORMAL,
    LOW
}
