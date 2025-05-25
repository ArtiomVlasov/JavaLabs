package messenger.client;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import messenger.common.Message;

public class SerialClientHandler extends ClientHandler {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String sessionId;


    @Override
    public void connect() throws IOException {
        Socket socket = new Socket(getServerIp(), getServerPort());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF("Serialisation");
        dos.flush();
        
        this.out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // Flush the header before creating ObjectInputStream
        this.in = new ObjectInputStream(socket.getInputStream());
    }
    @Override
    public String login(String nickname, String password) throws Exception {
        Message request = new Message();
        request.setType("login");
        request.setName(nickname);
        request.setPassword(password);
        request.setClientType("SerialClient");

        sendMessage(request);
        Message response = receive();

        if (response.getType().equals("error")) {
            throw new IOException(response.getContent());
        } else if (response.getType().equals("success")) {
            sessionId = response.getSessionId();
            return sessionId;
        }
        throw new IOException("Unexpected response");
    }
    @Override
    public void signup(String nickname, String password) throws Exception {
        Message request = new Message();
        request.setType("signup");
        request.setName(nickname);
        request.setPassword(password);
        request.setClientType("SerialClient");

        sendMessage(request);
        Message response = receive();

        if (response.getType().equals("error")) {
            throw new IOException(response.getContent());
        }
    }
    @Override
    public void sendMessage(String content) throws IOException {
        Message message = new Message();
        message.setType("message");
        message.setContent(content);
        message.setSessionId(sessionId);
        sendMessage(message);
    }
    @Override
    public void sendPing() throws IOException {
        Message message = new Message();
        message.setType("ping");
        message.setSessionId(sessionId);
        sendMessage(message);
    }
    @Override
    public void sendLogout() throws IOException {
        Message message = new Message();
        message.setType("logout");
        message.setSessionId(sessionId);
        sendMessage(message);
    }
    @Override
    public void sendListRequest() throws IOException {
        Message message = new Message();
        message.setType("list");
        message.setSessionId(sessionId);
        sendMessage(message);
    }
    @Override
    public Message receive() throws Exception {
        return (Message) in.readObject();
    }

    private void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    @Override
    public void close() {
        super.close();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException ignored) {}
    }
} 