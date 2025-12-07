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

package net.pkhapps.vera.server.device.controller;

import io.javalin.Javalin;
import net.pkhapps.vera.server.device.internal.ForAuthenticatingDevices;
import net.pkhapps.vera.server.device.internal.ForReceivingFromDevices;
import net.pkhapps.vera.server.device.internal.ForSendingToDevices;

import java.time.Clock;

public final class DeviceControllerFactory {

    private DeviceControllerFactory() {
    }

    public static ForSendingToDevices createController(Javalin javalin,
                                                       ForReceivingFromDevices forReceivingFromDevices,
                                                       ForAuthenticatingDevices forAuthenticatingDevices,
                                                       Clock clock) {
        var controller = new DeviceController(
                forReceivingFromDevices,
                forAuthenticatingDevices,
                OutgoingMessageEnvelopeSerde.instance(),
                IncomingMessageEnvelopeSerde.instance(),
                clock
        );
        controller.registerRoutes(javalin);
        return controller;
    }
}
