package src.factory;

import src.commands.Command;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CommandsFactory {
    private final Map<String, Class<? extends Command>> commandMap = new HashMap<>();

    public CommandsFactory() { loadConfig(); }

    void loadConfig() {
        try (InputStream input = getClass().getResourceAsStream("/home/archi/IdeaProjects/JavaLabs/LAB2/src/resources/commandsConfig.txt")) {
            if (input == null) {
                throw new RuntimeException("Error: File commands.config is not found!");
            }
            Properties properties = new Properties();
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                String className = properties.getProperty(key);
                Class<? extends Command> commandClass = (Class<? extends Command>) Class.forName(className);
                commandMap.put(key.toUpperCase(), commandClass);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error of uploading commands configuration: " + e.getMessage());
        }
    }

    public Command createCommand(String commandName) throws Exception {
        Class<? extends Command> commandClass = commandMap.get(commandName.toUpperCase());

        if (commandClass == null) {
            throw new IllegalArgumentException("Error: Command '" + commandName + "' is not found!");
        }
        return commandClass.getDeclaredConstructor().newInstance();
    }
}
