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

package net.pkhapps.vera.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public final class ScheduledJob {

    // TODO Document me

    private static final Logger log = LoggerFactory.getLogger(ScheduledJob.class);
    private final Thread thread;

    private ScheduledJob(Runnable task, Duration delay) {
        thread = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return; // Cancelled
            }
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error while running task", e);
            }
        });
    }

    public void cancel() {
        thread.interrupt();
    }

    public static ScheduledJob schedule(Runnable task, Duration delay) {
        return new ScheduledJob(task, delay);
    }
}
