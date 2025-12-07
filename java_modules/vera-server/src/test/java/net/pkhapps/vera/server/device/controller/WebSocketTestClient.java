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

import org.awaitility.Awaitility;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketTestClient implements AutoCloseable, WebSocket.Listener {

    private final HttpClient httpClient;
    private final WebSocket webSocket;
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final WebSocket.Listener listener = new WebSocket.Listener() {
        @Override
        public void onOpen(WebSocket webSocket) {
            opened.set(true);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            closed.set(true);
            opened.set(false);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    };

    public WebSocketTestClient(URI uri, Map<String, String> headers) {
        httpClient = HttpClient.newHttpClient();
        var builder = httpClient.newWebSocketBuilder();
        headers.forEach(builder::header);
        webSocket = builder.buildAsync(uri, listener).join();
    }

    public void assertClosed() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilTrue(closed);
    }

    public void assertOpen() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilTrue(opened);
    }

    public void sendTextMessage(String message) {
        webSocket.sendText(message, true).join();
    }

    @Override
    public void close() {
        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test Finished");
        } finally {
            httpClient.close();
        }
    }
}
