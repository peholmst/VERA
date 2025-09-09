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

/// Exception thrown by a [Repository] when an attempt is made to insert a new aggregate and the repository is already
/// at capacity (i.e., it has no room for more aggregates).
public class RepositoryAtCapacityException extends RepositoryException {

    public RepositoryAtCapacityException(int capacity) {
        super("The repository is at its capacity of " + capacity + " aggregates");
    }
}
