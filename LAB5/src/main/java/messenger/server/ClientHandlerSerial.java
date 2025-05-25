package messenger.server;

import messenger.common.Message;
import org.w3c.dom.Document;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandlerSerial extends ClientHandler {
    private static final Logger logger = Logger.getLogger(ClientHandlerSerial.class.getName());
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean running = true;
    private long lastPingTime = System.currentTimeMillis();
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();


    public ClientHandlerSerial(Socket socket, Server server) throws IOException {
        super(socket, server);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        startMessageProcessor();
    }

    private void startMessageProcessor() {
        Thread.startVirtualThread(() -> {
            while (running) {
                try {
                    // Забираем сообщение из очереди
                    Message message = messageQueue.take();
                    sendEvent(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    server.log("[ERROR] Failed to send message: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void start() {
        Thread.ofVirtual().start(this);
    }

    @Override
    public void run() {
        try {
            while (running) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }
        } catch (EOFException e) {
            logger.log(Level.INFO, "Client disconnected: {0}", userName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling client: " + e.getMessage(), e);
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message message) throws IOException {
        switch (message.getType()) {
            case "login" -> handleLogin(message);
            case "signup" -> handleSignup(message);
            case "message" -> handleChatMessage(message);
            case "ping" -> handlePing(message);
            case "logout" -> handleLogout(message);
            case "list" -> handleListRequest(message);
            default -> sendError("Unknown command: " + message.getType());
        }
    }

    private void handleLogin(Message message) throws IOException {
        String name = message.getName();
        String password = message.getPassword();
        clientType = message.getClientType();

        synchronized (server) {
            if (!server.getUserCredentials().containsKey(name)) {
                sendError("User does not exist");
                return;
            }
            if (!server.getUserCredentials().get(name).equals(password)) {
                sendError("Incorrect password");
                return;
            }
            if (server.isNameTaken(name)) {
                sendError("Name already taken");
                return;
            }
        }

        this.userName = name;
        this.sessionId = Server.generateSessionId();
        server.log("User logged in: " + name + " (session id: " + sessionId + ")");
        sendSuccess("login OK", sessionId);
        server.sendHistoryTo(this);
        server.broadcastUserLogin(userName, sessionId);
    }

    private void handleSignup(Message message) throws IOException {
        String name = message.getName();
        String password = message.getPassword();
        clientType = message.getClientType();

        synchronized (server) {
            if (server.getUserCredentials().containsKey(name)) {
                sendError("User already exists");
                return;
            }
            server.getUserCredentials().put(name, password);
        }
        sendSuccess("Signup successful");
    }

    private void handleChatMessage(Message message) throws IOException {
        if (!validateSession(message)) return;

        String content = message.getContent();
        server.enqueueMessage(new ChatMessage(userName, content, sessionId));
    }

    private void handlePing(Message message) throws IOException {
        if (!validateSession(message)) return;
        updatePingTime();
    }

    private void handleLogout(Message message) throws IOException {
        if (!validateSession(message)) return;

        sendSuccess("bye");
        running = false;
    }

    private void handleListRequest(Message message) throws IOException {
        if (!validateSession(message)) return;

        Message response = new Message();
        response.setType("success");
        StringBuilder content = new StringBuilder();
        for (ClientHandler client : server.getLoggedInClients()) {
            content.append(client.getUserName()).append(" (").append(client.getClientType()).append(")\n");
        }
        response.setContent(content.toString());
        sendMessage(response);
    }

    private boolean validateSession(Message message) throws IOException {
        if (sessionId == null || !sessionId.equals(message.getSessionId())) {
            sendError("Invalid session");
            return false;
        }
        return true;
    }

    @Override
    public void sendUserLoginEvent(String name) {
        try{
            Message event = new Message();
            event.setType("event");
            event.setName(name);
            event.setContent("userlogin");
            messageQueue.put(event);
        }catch (Exception e) {
            sendError("Failed to send message");
        }
    }

    @Override
    public void sendUserLogoutEvent(String name) {
        try {
            Message event = new Message();
            event.setType("event");
            event.setName(name);
            event.setContent("userlogout");
            messageQueue.put(event);
        }catch (Exception e) {
            sendError("Failed to send message");
        }
    }

    @Override
    public void sendMessageEvent(String from, String message) {
        try {
            Message event = new Message();
            event.setType("event");
            event.setName(from);
            event.setContent(message);
            messageQueue.put(event);
        } catch (Exception e) {
            sendError("Failed to send message");
        }
    }

    @Override
    public void sendError(String msg) {
        try{
            Message response = new Message();
            response.setType("error");
            response.setContent(msg);
            messageQueue.put(response);
        }catch (Exception e) {
            sendError("Failed to send message");
        }
    }

    @Override
    public void sendSuccess(String msg) {
        try{
            Message response = new Message();
            response.setType("success");
            response.setContent(msg);
            messageQueue.put(response);
        }catch (Exception e) {
            sendError("Failed to send message");
        }
    }

    @Override
    public void sendSuccess(String msg, String sessionId) {
        try{
            Message response = new Message();
            response.setType("success");
            response.setContent(msg);
            response.setSessionId(sessionId);
            messageQueue.put(response);
        }catch (Exception e) {
            sendError("Failed to send message");
        }
    }

    @Override
    public void updatePingTime() {
        lastPingTime = System.currentTimeMillis();
    }

    @Override
    public long getLastPingTime() {
        return lastPingTime;
    }

    private void sendEvent(Message event) {
        try {
            sendMessage(event);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending event to client: " + e.getMessage(), e);
            running = false;
        }
    }

    private void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    private void cleanup() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
} 