package messenger.client;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import messenger.common.Message;

public class SerialClientHandler {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String sessionId;
    private volatile boolean running = true;
    private static final String CONFIG_FILE = "/config.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = SerialClientHandler.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }
    //FIXME написать поддержку через ui выбор этого протокола

    public static String getServerIp() {
        return properties.getProperty("server.ip");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port"));
    }

    public void connect() throws IOException {
        Socket socket = new Socket(getServerIp(), getServerPort());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        out.writeUTF("SERIAL");
        out.flush();
    }

    public String login(String nickname, String password) throws Exception {
        Message request = new Message();
        request.setType("login");
        request.setName(nickname);
        request.setPassword(password);
        request.setClientType("SerialClient");

        sendMessage(request);
        Message response = receiveMessage();

        if (response.getType().equals("error")) {
            throw new IOException(response.getContent());
        } else if (response.getType().equals("success")) {
            sessionId = response.getSessionId();
            return sessionId;
        }
        throw new IOException("Unexpected response");
    }

    public void signup(String nickname, String password) throws Exception {
        Message request = new Message();
        request.setType("signup");
        request.setName(nickname);
        request.setPassword(password);
        request.setClientType("SerialClient");

        sendMessage(request);
        Message response = receiveMessage();

        if (response.getType().equals("error")) {
            throw new IOException(response.getContent());
        }
    }

    public void sendMessage(String content) throws IOException {
        Message message = new Message();
        message.setType("message");
        message.setContent(content);
        message.setSessionId(sessionId);
        sendMessage(message);
    }

    public void sendPing() throws IOException {
        Message message = new Message();
        message.setType("ping");
        message.setSessionId(sessionId);
        sendMessage(message);
    }

    public void sendLogout() throws IOException {
        Message message = new Message();
        message.setType("logout");
        message.setSessionId(sessionId);
        sendMessage(message);
    }

    public void sendListRequest() throws IOException {
        Message message = new Message();
        message.setType("list");
        message.setSessionId(sessionId);
        sendMessage(message);
    }

    public Message receiveMessage() throws Exception {
        return (Message) in.readObject();
    }

    private void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public void close() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException ignored) {}
    }
} 