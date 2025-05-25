package messenger.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import org.w3c.dom.*;

public class ClientUI {
    private XMLClientHandler client;
    private volatile boolean running = true;

    private JFrame loginFrame;
    private JFrame signupFrame;
    private JFrame chatFrame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField nicknameField;
    private JPasswordField passwordField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().start());
    }

    public void start() {
        try {
            client = new XMLClientHandler();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "[ERROR] Failed to initialize XML parser: " + e.getMessage());
            return;
        }
        createLoginFrame();
    }

    private void createLoginFrame() {
        loginFrame = new JFrame();
        loginFrame.setUndecorated(true);
        loginFrame.setSize(350, 500);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Telegram killer", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        nicknameField = new JTextField();
        nicknameField.setMaximumSize(new Dimension(300, 40));
        nicknameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nicknameField.setBorder(BorderFactory.createTitledBorder("Nickname"));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginButton = new JButton("Log in");
        loginButton.setFont(new Font("Arial", Font.BOLD, 26));
        loginButton.setMaximumSize(new Dimension(300, 50));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel switchToSignup = new JLabel("I don't have an account");
        switchToSignup.setFont(new Font("Arial", Font.PLAIN, 16));
        switchToSignup.setForeground(Color.BLACK);
        switchToSignup.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchToSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchToSignup.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        switchToSignup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loginFrame.dispose();
                createSignupFrame();
            }
        });

        loginButton.addActionListener(e -> {
            String nickname = nicknameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (!nickname.isEmpty() && !password.isEmpty()) {
                loginFrame.dispose();
                connectAndLogin(nickname, password);
            }
        });

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(nicknameField);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(switchToSignup);

        loginFrame.setContentPane(mainPanel);
        loginFrame.setVisible(true);
    }

    private void connectAndLogin(String nickname, String password) {
        try {
            client.connect();
            client.login(nickname, password);
            JOptionPane.showMessageDialog(null, "[SUCCESS] login OK");
            createChatFrame();
            startBackgroundThreads();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Connection error: " + ex.getMessage());
            loginFrame.setVisible(true);
        }
    }

    private void createChatFrame() {
        chatFrame = new JFrame("Chat");
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setSize(500, 400);
        chatFrame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton listButton = new JButton("List Users");
        JButton logoutButton = new JButton("Logout");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(listButton);
        buttonPanel.add(logoutButton);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        chatFrame.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        listButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendListRequest();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendLogout();
            }
        });

        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendLogout();
            }
        });

        chatFrame.setLocationRelativeTo(null);
        chatFrame.setVisible(true);
    }

    private void startBackgroundThreads() {
        running = true;
        Thread listener = new Thread(this::listenForMessages);
        Thread pinger = new Thread(this::startPinger);
        listener.start();
        pinger.start();
    }

    private void listenForMessages() {
        try {
            while (running) {
                Document doc = client.receiveXml();
                if (doc == null) break;
                Element root = doc.getDocumentElement();
                SwingUtilities.invokeLater(() -> {
                    switch (root.getTagName()) {
                        case "event" -> {
                            String type = root.getAttribute("name");
                            switch (type) {
                                case "userlogin" -> appendToChat("[SYSTEM] " + getText(root, "name") + " joined the chat");
                                case "userlogout" -> appendToChat("[SYSTEM] " + getText(root, "name") + " left the chat");
                                case "message" -> {
                                    String from = getText(root, "name");
                                    String text = getText(root, "message");
                                    if (from.equals(nicknameField.getText())) appendToChat("You: " + text);
                                    else appendToChat(from + ": " + text);
                                }
                            }
                        }
                        case "success" -> {
                            if (root.getElementsByTagName("listusers").getLength() > 0) {
                                appendToChat("Users online:");
                                NodeList users = root.getElementsByTagName("user");
                                for (int i = 0; i < users.getLength(); i++) {
                                    Element user = (Element) users.item(i);
                                    String name = getText(user, "name");
                                    String type = getText(user, "type");
                                    appendToChat(" - " + name + " (" + type + ")");
                                }
                            }
                        }
                        case "error" -> appendToChat("[ERROR] " + getText(root, "message"));
                    }
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                appendToChat("[ERROR] Disconnected by server.");
                running = false;
                closeResources();
            });
        }
    }

    private void startPinger() {
        while (running) {
            try {
                Thread.sleep(1000);
                client.sendPing();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("[ERROR] Ping failed, server probably unreachable.");
                    running = false;
                    closeResources();
                });
                break;
            }
        }
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            try {
                client.sendMessage(msg);
                messageField.setText("");
            } catch (IOException e) {
                appendToChat("[ERROR] Failed to send message: " + e.getMessage());
            }
        }
    }

    private void sendLogout() {
        try {
            client.sendLogout();
            running = false;
            closeResources();
            chatFrame.dispose();
        } catch (IOException e) {
            appendToChat("[ERROR] Failed to logout: " + e.getMessage());
        }
    }

    private void sendListRequest() {
        try {
            client.sendListRequest();
        } catch (IOException e) {
            appendToChat("[ERROR] Failed to list users: " + e.getMessage());
        }
    }

    private void signup(String nickname, String password) {
        try {
            client.connect();
            client.signup(nickname, password);
            connectAndLogin(nickname, password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Connection error: " + ex.getMessage());
            signupFrame.setVisible(true);
        }
    }

    private String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent();
    }

    private void appendToChat(String message) {
        chatArea.append(message + "\n");
    }

    private void closeResources() {
        if (client != null) {
            client.close();
        }
    }

    private void createSignupFrame() {
        signupFrame = new JFrame();
        signupFrame.setUndecorated(true);
        signupFrame.setSize(350, 500);
        signupFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        signupFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Messenger name", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        nicknameField = new JTextField();
        nicknameField.setMaximumSize(new Dimension(300, 40));
        nicknameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nicknameField.setBorder(BorderFactory.createTitledBorder("Nickname"));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton signupButton = new JButton("Sign up");
        signupButton.setFont(new Font("Arial", Font.BOLD, 26));
        signupButton.setMaximumSize(new Dimension(300, 50));
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setBackground(Color.WHITE);
        signupButton.setFocusPainted(false);
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel switchToLogin = new JLabel("I already have an account");
        switchToLogin.setFont(new Font("Arial", Font.PLAIN, 16));
        switchToLogin.setForeground(Color.BLACK);
        switchToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchToLogin.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        switchToLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signupFrame.dispose();
                createLoginFrame();
            }
        });

        signupButton.addActionListener(e -> {
            String nickname = nicknameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (!nickname.isEmpty() && !password.isEmpty()) {
                signupFrame.dispose();
                signup(nickname, password);
            }
        });

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(nicknameField);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(signupButton);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(switchToLogin);

        signupFrame.setContentPane(mainPanel);
        signupFrame.setVisible(true);
    }
} 