package messenger.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.util.logging.*;
import java.text.SimpleDateFormat;

public class Server {
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Queue<ChatMessage> messageQueue = new ConcurrentLinkedQueue<>();
    private final List<ChatMessage> messageHistory = new ArrayList<>();
    private final Object clientLock = new Object();
    private final Object historyLock = new Object();
    private ServerSocket serverSocket;
    private final Map<String, String> userCredentials = new HashMap<>();
    private static final String HISTORY_FILE = "chat_history.txt";
    private static final String CREDENTIALS_FILE = "user_credentials.txt";
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final String LOG_FILE = "server.log";
    private static final int MAX_LOG_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_LOG_FILES = 5;

    static {
        try {
            // Create a file handler that writes to server.log
            FileHandler fileHandler = new FileHandler(LOG_FILE, MAX_LOG_SIZE, MAX_LOG_FILES, true);
            fileHandler.setFormatter(new SimpleFormatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    return String.format("%s [%s] %s: %s%n",
                            dateFormat.format(new Date(record.getMillis())),
                            record.getLevel(),
                            record.getLoggerName(),
                            record.getMessage());
                }
            });
            
            // Add the file handler to the logger
            logger.addHandler(fileHandler);
            
            // Set the logging level
            logger.setLevel(Level.INFO);
            
            // Remove the default console handler
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = 12345;
        new Server(port).start();
    }

    public static String generateSessionId() {
        return "session-" + new Random().nextInt(999999);
    }

    public static List<ClientHandler> getClients() {
        return getClients();
    }

    public static void log(String message) {
        logger.info(message);
    }

    private void loadState() {
        // Load chat history
        try {
            if (Files.exists(Paths.get(HISTORY_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(HISTORY_FILE));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 3) {
                        messageHistory.add(new ChatMessage(parts[0], parts[1], parts[2]));
                    }
                }
                logger.info("Loaded " + messageHistory.size() + " messages from history");
            }
        } catch (IOException e) {
            logger.severe("Failed to load chat history: " + e.getMessage());
        }

        // Load user credentials
        try {
            if (Files.exists(Paths.get(CREDENTIALS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(CREDENTIALS_FILE));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
                        userCredentials.put(parts[0], parts[1]);
                    }
                }
                logger.info("Loaded " + userCredentials.size() + " user credentials");
            }
        } catch (IOException e) {
            logger.severe("Failed to load user credentials: " + e.getMessage());
        }
    }

    private void saveState() {
        // Save chat history
        try {
            List<String> lines = new ArrayList<>();
            synchronized (historyLock) {
                for (ChatMessage msg : messageHistory) {
                    lines.add(msg.from() + "|" + msg.message() + "|" + msg.sessionId());
                }
            }
            Files.write(Paths.get(HISTORY_FILE), lines);
            logger.info("Saved " + lines.size() + " messages to history");
        } catch (IOException e) {
            logger.severe("Failed to save chat history: " + e.getMessage());
        }

        // Save user credentials
        try {
            List<String> lines = new ArrayList<>();
            synchronized (userCredentials) {
                for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                    lines.add(entry.getKey() + "|" + entry.getValue());
                }
            }
            Files.write(Paths.get(CREDENTIALS_FILE), lines);
            logger.info("Saved " + lines.size() + " user credentials");
        } catch (IOException e) {
            logger.severe("Failed to save user credentials: " + e.getMessage());
        }
    }

    public void addClient(ClientHandler client) {
        synchronized (clientLock) {
            clients.add(client);
        }
    }

    public void removeClient(ClientHandler client) {
        synchronized (clientLock) {
            clients.remove(client);
        }
        if (client.getUserName() != null) {
            logger.info("Client removed: " + client.getUserName() + " (session id: " + client.getSessionId() + ")");
        }
    }

    public boolean isNameTaken(String name) {
        synchronized (clientLock) {
            for (ClientHandler c : clients) {
                if (c.isLoggedIn() && c.getUserName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public List<ClientHandler> getLoggedInClients() {
        synchronized (clientLock) {
            List<ClientHandler> result = new ArrayList<>();
            for (ClientHandler c : clients) {
                if (c.isLoggedIn()) {
                    result.add(c);
                }
            }
            return result;
        }
    }

    public void broadcastUserLogin(String userName, String excludeSession) {
        for (ClientHandler c : getLoggedInClients()) {
            if (!c.getSessionId().equals(excludeSession)) {
                c.sendUserLoginEvent(userName);
            }
        }
        logger.info("Broadcast userlogin: " + userName + " (session id: " + excludeSession + ")");
    }

    public void broadcastUserLogout(String userName, String excludeSession) {
        for (ClientHandler c : getLoggedInClients()) {
            if (!c.getSessionId().equals(excludeSession)) {
                c.sendUserLogoutEvent(userName);
            }
        }
        logger.info("Broadcast userlogout: " + userName + " (session id: " + excludeSession + ")");
    }

    public void broadcastMessage(String from, String message, String senderSessionId) {
        for (ClientHandler c : getLoggedInClients()) {
            if (c.getSessionId().equals(senderSessionId)) {
                c.sendMessageEvent("You", message);
            } else {
                c.sendMessageEvent(from, message);
            }
        }
        logger.info("Broadcast message from " + from + " (session id: " + senderSessionId + "): " + message);
    }

    public void enqueueMessage(ChatMessage msg) {
        messageQueue.add(msg);
        synchronized (historyLock) {
            messageHistory.add(msg);
            if (messageHistory.size() > 100) {
                messageHistory.removeFirst();
            }
            saveState();
        }
    }

    public void sendHistoryTo(ClientHandler client) {
        synchronized (historyLock) {
            for (ChatMessage msg : messageHistory) {
                client.sendMessageEvent(msg.from(), msg.message());
            }
        }
    }

    private void startMessageDispatcher() {
        Thread dispatcher = new Thread(() -> {
            while (true) {
                ChatMessage msg = messageQueue.poll();
                if (msg != null) {
                    broadcastMessage(msg.from(), msg.message(), msg.sessionId());
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    private void startPingMonitor() {
        Thread monitor = new Thread(() -> {
            while (true) {
                long now = System.currentTimeMillis();
                List<ClientHandler> snapshot;
                synchronized (clientLock) {
                    snapshot = new ArrayList<>(clients);
                }
                for (ClientHandler client : snapshot) {
                    if (now - client.getLastPingTime() > 10_000) {
                        if (client.getUserName() != null) {
                            logger.warning("[TIMEOUT] " + client.getUserName() + " (session id: " + client.getSessionId() + ") timed out.");
                        }
                        client.sendError("Disconnected due to inactivity");
                        removeClient(client);
                        if (client.getUserName() != null) {
                            broadcastUserLogout(client.getUserName(), client.getSessionId());
                        }
                        try {
                            client.socket.close();
                        } catch (IOException ignored) {}
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    public Server(int port) {
        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");
            serverSocket = new ServerSocket(port, 50, address);
            logger.info("Server started on " + address.getHostAddress() + ":" + port);
            loadState();
        } catch (IOException e) {
            logger.severe("Could not start server: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        startMessageDispatcher();
        startPingMonitor();

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("Client connected: " + socket.getInetAddress());

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                String protocol = dis.readUTF();

                ClientHandler handler;
                if (protocol.equalsIgnoreCase("XML")) {
                    handler = new ClientHandlerXML(socket, dis, dos, this);
                } else if (protocol.equalsIgnoreCase("Serialisation")) {
                    handler = new ClientHandlerSerial(socket, this);
                } else {
                    logger.warning("Unknown protocol: " + protocol);
                    socket.close();
                    continue;
                }

                addClient(handler);
                handler.start();

            } catch (IOException e) {
                logger.severe("Connection error: " + e.getMessage());
            }
        }
    }

    public synchronized Map<String, String> getUserCredentials() {
        return userCredentials;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Server shutting down, saving state...");
        }));
    }
}
