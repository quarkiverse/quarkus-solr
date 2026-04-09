package io.quarkiverse.solr.devui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class JsonRpcClient extends WebSocketListener implements Closeable {
    private final WebSocket webSocket;
    private final OkHttpClient client;
    private int id = 0;
    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final AtomicReference<Throwable> failure = new AtomicReference<>();

    public JsonRpcClient() throws InterruptedException {
        String port = ConfigProvider.getConfig().getValue("quarkus.http.test-port", String.class);
        String url = "ws://localhost:" + port + "/q/dev-ui/json-rpc-ws";
        Request request = new Request.Builder().url(url).build();
        client = new OkHttpClient();
        webSocket = client.newWebSocket(request, this);
        assertTrue(connectionLatch.await(15, TimeUnit.SECONDS), "WebSocket connection not opened within timeout");
    }

    public JsonValue send(String method) throws InterruptedException {
        return send(method, Map.of());
    }

    /**
     * Sends a command and waits for any acknowledgement, ignoring the result value.
     * Use for fire-and-forget commands where only side effects matter.
     */
    public void sendVoid(String method) throws InterruptedException {
        messageQueue.clear();
        int sentId = id;
        webSocket.send(createMessageObject(method, Map.of()));
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline) {
            String text = messageQueue.poll(100, TimeUnit.MILLISECONDS);
            if (text == null)
                continue;
            try (JsonReader reader = Json.createReader(new StringReader(text))) {
                JsonObject obj = reader.readObject();
                if (obj.containsKey("id") && obj.getInt("id") == sentId) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // Timeout — ignore, command may not be available or already applied
    }

    public JsonValue send(String method, Map<String, String> params) throws InterruptedException {
        messageQueue.clear();
        webSocket.send(createMessageObject(method, params));
        long deadline = System.currentTimeMillis() + 15_000;
        java.util.List<String> received = new java.util.ArrayList<>();
        while (System.currentTimeMillis() < deadline) {
            if (failure.get() != null)
                throw new RuntimeException("WebSocket communication failed", failure.get());
            String text = messageQueue.poll(100, TimeUnit.MILLISECONDS);
            if (text == null)
                continue;
            received.add(text);
            JsonValue result = extractResult(text);
            if (result != null)
                return result;
        }
        throw new AssertionError(
                "No response received within timeout for method: " + method + ". Messages received: " + received);
    }

    /**
     * Returns the result object from a JSON-RPC response, or null if the message should be skipped
     * (e.g. server push notifications, acks without data).
     */
    private JsonValue extractResult(String text) {
        try (JsonReader reader = Json.createReader(new StringReader(text))) {
            JsonObject obj = reader.readObject();
            if (obj.containsKey("error")) {
                throw new RuntimeException("Error response received: " + obj.getJsonObject("error").getString("message"));
            }
            JsonObject result = obj.getJsonObject("result");
            if (result == null || !result.containsKey("object")) {
                return null; // ack or push without data, skip
            }
            JsonValue resultObj = result.get("object");
            assertNotNull(resultObj, "Response did not include result object, it was " + obj);
            return resultObj;
        }
    }

    private String createMessageObject(String method, Map<String, String> params) {
        JsonObjectBuilder paramsObjBuilder = Json.createObjectBuilder();
        params.forEach(paramsObjBuilder::add);
        JsonObject message = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("method", method)
                .add("params", paramsObjBuilder.build())
                .add("id", id)
                .build();
        id = id + 1;
        return message.toString();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response res) {
        connectionLatch.countDown();
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        messageQueue.offer(text);
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response res) {
        failure.set(t);
    }

    @Override
    public void close() {
        webSocket.close(1000, null);
        client.dispatcher().executorService().shutdown();
    }
}
