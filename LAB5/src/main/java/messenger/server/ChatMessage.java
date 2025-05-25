package messenger.server;
public record ChatMessage(String from, String message, String sessionId) {}