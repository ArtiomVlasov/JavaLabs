package messenger.client;

import org.w3c.dom.Document;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public abstract class ClientHandler {
    protected String sessionId;
    protected volatile boolean running = true;
    protected static final String CONFIG_FILE = "/config.properties";
    protected static final Properties properties = new Properties();

    static {
        try (InputStream input = ClientHandler.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    public static String getServerIp() {
        return properties.getProperty("server.ip");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port"));
    }

    public abstract void connect() throws IOException;
    public abstract String login(String nickname, String password) throws Exception;
    public abstract void signup(String nickname, String password) throws Exception;
    public abstract void sendMessage(String content) throws IOException;
    public abstract void sendPing() throws IOException;
    public abstract void sendLogout() throws IOException;
    public abstract void sendListRequest() throws IOException;
    public abstract Object receive() throws Exception;
    public void close() {
        running = false;
    }
} 