# Sebastian Bank Chatbot

This project is a local, privacy-first AI chatbot for banking queries. It uses a Java WebSocket server to connect a web-based chat UI with a locally running Ollama LLM API (e.g., Phi-3). No internet or cloud connection is required after setup—your data stays on your machine.

## Features

- **No cloud:** Entirely local, no 3rd-party data sharing.
- **Java WebSocket backend:** Fast, robust, and easy to extend.
- **Ollama integration:** Works with any Ollama-supported LLM (e.g., phi3, llama3).
- **Modern web chat UI:** Simple frontend for real-time conversations.

## How it works

1. **Ollama API** serves your chosen LLM at `localhost:11434`
2. **Java WebSocket server** bridges the chat UI and Ollama, handling all message streaming and formatting.
3. **Web chat UI** connects via `ws://localhost:8080` and interacts in real time.

## Quickstart
1. **Start the Java server:**
    ```bash
    javac -cp ".:Java-WebSocket-1.5.6.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar:jackson-core-2.15.2.jar:jackson-databind-2.15.2.jar:jackson-annotations-2.15.2.jar" WebSocketChatServer.java
    java -cp ".:Java-WebSocket-1.5.6.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar:jackson-core-2.15.2.jar:jackson-databind-2.15.2.jar:jackson-annotations-2.15.2.jar" WebSocketChatServer
    ```

3. **Serve the frontend:**
    ```bash
    cd web
    python3 -m http.server 8000
    ```
    Then open [http://localhost:8000/](http://localhost:8000/) in your browser.

## Directory structure

```
.
├── server/
│   ├── WebSocketChatServer.java
│   └── (Java dependencies: .jar files)
├── web/
│   ├── index.html
│   ├── chat.js
│   └── (static assets)
└── README.md
```

## Requirements

- Java 11+
- Python 3 (for simple web server)
- Ollama with a supported local model (e.g. phi3)
- Browser (for UI)

## Customization

- Edit the system prompt in `WebSocketChatServer.java` to change AI behavior.
- Swap Ollama models by changing the model name in the Java server.

## Sample Output
![WhatsApp Image 2025-06-24 at 22 23 40_40e92147](https://github.com/user-attachments/assets/b5a2195b-01db-471a-9c39-ae9be27aa040)
![WhatsApp Image 2025-06-24 at 22 25 22_9af7c77b](https://github.com/user-attachments/assets/55ea564d-714e-4f18-8a62-f624855988b4)


## License

MIT License (or your choice)
