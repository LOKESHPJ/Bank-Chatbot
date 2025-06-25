let ws;
let userName = null;

function appendMessage(message, sender = "ai") {
  const chat = document.getElementById('chat');
  const msgWrap = document.createElement('div');
  msgWrap.className = 'message';

  const bubble = document.createElement('div');
  bubble.className = 'bubble ' + (sender === "user" ? "user" : "ai");
  bubble.textContent = message;
  if (sender === "ai") {
    const aiIcon = document.createElement('span');
    aiIcon.className = "ai-icon";
    aiIcon.textContent = "🤖";
    msgWrap.append(aiIcon, bubble);
  } else {
    msgWrap.appendChild(bubble);
  }
  chat.appendChild(msgWrap);
  chat.scrollTop = chat.scrollHeight;
}

function showTypingIndicator() {
  const chat = document.getElementById('chat');
  let typingEl = document.getElementById('typing-indicator');
  if (!typingEl) {
    typingEl = document.createElement('div');
    typingEl.id = 'typing-indicator';
    typingEl.className = 'typing-indicator';
    typingEl.textContent = "Sebastian is typing...";
    chat.appendChild(typingEl);
    chat.scrollTop = chat.scrollHeight;
  }
}
function hideTypingIndicator() {
  const typingEl = document.getElementById('typing-indicator');
  if (typingEl) typingEl.remove();
}

function sendMessage() {
  const input = document.getElementById('input');
  if (!userName) {
    userName = "You";
  }
  const text = input.value.trim();
  if (text.length === 0) return;

  appendMessage(text, "user");
  ws.send(`[${userName}]: ${text}`);
  input.value = '';
  showTypingIndicator();
}

window.onload = function() {
  ws = new WebSocket('ws://localhost:8080');

  ws.onmessage = function(event) {
    hideTypingIndicator();
    // Show Sebastian replies as AI, and user as user
    if (event.data.startsWith("[Sebastian Bank]:")) {
      appendMessage(event.data.replace("[Sebastian Bank]:", "").trim(), "ai");
    } else if (!event.data.startsWith("[You]:")) {
      appendMessage(event.data, "ai");
    }
  };

  ws.onopen = function() {
    appendMessage("Welcome to Sebastian Bank! I am Sebastian. Ask me anything about banking, accounts, loans, and more.", "ai");
  };

  ws.onclose = function() {
    appendMessage("Connection closed.", "ai");
  };

  document.getElementById('chat-form').onsubmit = function(e) {
    e.preventDefault();
    sendMessage();
  };
};
