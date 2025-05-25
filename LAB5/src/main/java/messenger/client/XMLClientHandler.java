package messenger.client;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class XMLClientHandler extends ClientHandler {
    private DataInputStream in;
    private DataOutputStream out;
    private final DocumentBuilder builder;
    private String sessionId;

    public XMLClientHandler() throws Exception {
        this.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    @Override
    public void connect() throws IOException {
        Socket socket = new Socket(getServerIp(), getServerPort());
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF("XML");
    }
    @Override
    public String login(String nickname, String password) throws Exception {
        Document doc = builder.newDocument();
        Element command = doc.createElement("command");
        command.setAttribute("name", "login");
        
        Element name = doc.createElement("name");
        name.setTextContent(escape(nickname));
        Element pass = doc.createElement("password");
        pass.setTextContent(escape(password));
        Element type = doc.createElement("type");
        type.setTextContent("XMLClient");
        
        command.appendChild(name);
        command.appendChild(pass);
        command.appendChild(type);
        doc.appendChild(command);
        
        sendXml(doc);
        Document response = receive();
        Element root = response.getDocumentElement();
        
        if (root.getTagName().equals("error")) {
            throw new IOException(getText(root, "message"));
        } else if (root.getTagName().equals("success")) {
            sessionId = getText(root, "session");
            return sessionId;
        }
        throw new IOException("Unexpected response");
    }
    @Override
    public void signup(String nickname, String password) throws Exception {
        Document doc = builder.newDocument();
        Element command = doc.createElement("command");
        command.setAttribute("name", "signup");
        
        Element name = doc.createElement("name");
        name.setTextContent(escape(nickname));
        Element pass = doc.createElement("password");
        pass.setTextContent(escape(password));
        Element type = doc.createElement("type");
        type.setTextContent("XMLClient");
        
        command.appendChild(name);
        command.appendChild(pass);
        command.appendChild(type);
        doc.appendChild(command);
        
        sendXml(doc);
        Document response = receive();
        Element root = response.getDocumentElement();
        
        if (root.getTagName().equals("error")) {
            throw new IOException(getText(root, "message"));
        }
    }
    @Override
    public void sendMessage(String message) throws IOException {
        Document doc = builder.newDocument();
        Element command = doc.createElement("command");
        command.setAttribute("name", "message");
        
        Element msg = doc.createElement("message");
        msg.setTextContent(escape(message));
        Element session = doc.createElement("session");
        session.setTextContent(sessionId);
        
        command.appendChild(msg);
        command.appendChild(session);
        doc.appendChild(command);
        
        sendXml(doc);
    }
    @Override
    public void sendPing() throws IOException {
        Document doc = builder.newDocument();
        Element command = doc.createElement("command");
        command.setAttribute("name", "ping");
        
        Element session = doc.createElement("session");
        session.setTextContent(sessionId);
        
        command.appendChild(session);
        doc.appendChild(command);
        
        sendXml(doc);
    }
    @Override
    public void sendLogout() throws IOException {
        Document doc = builder.newDocument();
        Element command = doc.createElement("command");
        command.setAttribute("name", "logout");
        
        Element session = doc.createElement("session");
        session.setTextContent(sessionId);
        
        command.appendChild(session);
        doc.appendChild(command);
        
        sendXml(doc);
    }
    @Override
    public void sendListRequest() throws IOException {
        Document doc = builder.newDocument();
        Element command = doc.createElement("command");
        command.setAttribute("name", "list");
        
        Element session = doc.createElement("session");
        session.setTextContent(sessionId);
        
        command.appendChild(session);
        doc.appendChild(command);
        
        sendXml(doc);
    }
    @Override
    public Document receive() throws Exception {
        int len = in.readInt();
        byte[] data = new byte[len];
        in.readFully(data);
        return builder.parse(new ByteArrayInputStream(data));
    }

    private void sendXml(Document doc) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(doc), new StreamResult(baos));
            byte[] xml = baos.toByteArray();
            out.writeInt(xml.length);
            out.write(xml);
            out.flush();
        } catch (TransformerException e) {
            throw new IOException("Failed to transform XML document", e);
        }
    }

    private String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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