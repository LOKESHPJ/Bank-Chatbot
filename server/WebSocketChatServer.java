import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.net.http.*;
import java.net.URI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class WebSocketChatServer extends WebSocketServer {
    private Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());
    private static final int PORT = 8080;
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "phi3";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ExecutorService OLLAMA_EXECUTOR = Executors.newCachedThreadPool();

    public WebSocketChatServer() {
        super(new InetSocketAddress(PORT));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        conn.send("[Sebastian Bank]: Welcome to Sebastian Bank! I am Sebastian, your AI assistant. Ask me anything about accounts, loans, security, cards, and more.");
        broadcast("[Sebastian Bank]: A new user joined the chat!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        broadcast("[Sebastian Bank]: A user left the chat.");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);
        broadcast(message);

        String userPrompt = message.replaceAll("^\\[.*?\\]:?\\s*", "");
        System.out.println("User prompt: " + userPrompt);

        askOllama(userPrompt).thenAccept(aiReply -> {
            System.out.println("AI reply: " + aiReply.trim());
            broadcast("[Sebastian Bank]: " + aiReply.trim());
        }).exceptionally(e -> {
            e.printStackTrace();
            broadcast("[Sebastian Bank]: (Error talking to AI: " + e.getMessage() + ")");
            return null;
        });
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket Error: " + ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Sebastian Bank chat server started on port " + PORT);
        setConnectionLostTimeout(60);
    }

    public static void main(String[] args) {
        WebSocketChatServer server = new WebSocketChatServer();
        server.start();
        System.out.println("Sebastian Bank chat server is up and running!");
    }

    public static CompletableFuture<String> askOllama(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String reqBody = "{\n" +
                        "  \"model\": \"" + MODEL + "\",\n" +
                        "  \"stream\": true,\n" +
                        "  \"messages\": [\n" +
                        "    {\"role\": \"system\", \"content\": \"You are Sebastian, an expert helpful assistant for Sebastian Bank. Answer as Sebastian, politely and concisely, and only about banking topics.\"},\n" +
                        "    {\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}\n" +
                        "  ]\n" +
                        "}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(OLLAMA_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(reqBody, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    System.err.println("Ollama API error: HTTP " + response.statusCode());
                    return "Sorry, Sebastian couldn't get a response from the local AI (HTTP " + response.statusCode() + ").";
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                StringBuilder aiReply = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    try {
                        JsonNode node = mapper.readTree(line);
                        if (node.has("message") && node.get("message").has("content")) {
                            aiReply.append(node.get("message").get("content").asText());
                        }
                    } catch (Exception e) {
                        System.err.println("Malformed Ollama line: " + line);
                    }
                }
                return aiReply.toString();
            } catch (Exception e) {
                System.err.println("Exception calling Ollama: " + e.getMessage());
                e.printStackTrace();
                return "Sorry, Sebastian couldn't get a response from the local AI (Exception).";
            }
        }, OLLAMA_EXECUTOR);
    }
}
