package io.quarkiverse.solr.devui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
    private CountDownLatch countDownLatch;
    private AtomicReference<String> response;
    private AtomicReference<Throwable> throwable;

    public JsonRpcClient() throws InterruptedException {
        String port = ConfigProvider.getConfig().getValue("quarkus.http.test-port", String.class);
        String url = "ws://localhost:" + port + "/q/dev-ui/json-rpc-ws";
        Request request = new Request.Builder().url(url).build();
        client = new OkHttpClient();
        countDownLatch = new CountDownLatch(1);
        webSocket = client.newWebSocket(request, this);
        assertTrue(countDownLatch.await(15, TimeUnit.SECONDS), "No response received within timeout");
    }

    public JsonValue send(String method) throws InterruptedException {
        return send(method, Map.of());
    }

    public JsonValue send(String method, Map<String, String> params) throws InterruptedException {
        countDownLatch = new CountDownLatch(1);
        response = new AtomicReference<>();
        throwable = new AtomicReference<>();
        webSocket.send(createMessageObject(method, params));
        assertTrue(countDownLatch.await(15, TimeUnit.SECONDS), "No response received within timeout");
        if (throwable.get() != null)
            throw new RuntimeException("WebSocket communication failed", throwable.get());
        return getResponse(response.get());
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

    private JsonValue getResponse(String responseStr) {
        try (JsonReader reader = Json.createReader(new StringReader(responseStr))) {
            JsonObject obj = reader.readObject();
            if (obj.containsKey("error")) {
                throw new RuntimeException("Error response received: " + obj.getJsonObject("error").getString("message"));
            }
            return obj.getJsonObject("result").get("object");
        }
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response res) {
        countDownLatch.countDown();
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        response.set(text);
        countDownLatch.countDown();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response res) {
        throwable.set(t);
        countDownLatch.countDown();
    }

    @Override
    public void close() {
        webSocket.close(1000, null);
        client.dispatcher().executorService().shutdown();
    }
}
