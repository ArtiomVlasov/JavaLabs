package src.factory;

import src.commands.Command;
import src.commands.CommandInfo;
import src.gui.CalculatorGui;
import src.logs.CalculatorLogger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

public class CommandsFactory {
    private final Map<String, Class<? extends Command>> commandMap = new HashMap<>();
    private static final Logger logger = CalculatorLogger.setupLogger(CalculatorGui.class.getName());
    private static final String CONFIG_FILE = "commands.config";

    public CommandsFactory() {
        loadCommands();
    }

    private void loadCommands() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/" + CONFIG_FILE)))) {
            
            String jarPath;
            while ((jarPath = reader.readLine()) != null) {
                if (jarPath.trim().isEmpty() || jarPath.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }
                loadCommandsFromJar(jarPath.trim());
            }
        } catch (IOException e) {
            logger.severe("Failed to read commands configuration: " + e.getMessage());
            throw new RuntimeException("Failed to read commands configuration", e);
        }
    }

    private void loadCommandsFromJar(String jarPath) {
        try {
            URL jarUrl = new File(jarPath).toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());

            try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarPath))) {
                JarEntry entry;
                while ((entry = jarStream.getNextJarEntry()) != null) {
                    if (entry.getName().endsWith(".class")) {
                        // Extract the class name from the JAR entry path
                        String className = entry.getName()
                                .replace('/', '.')
                                .replace(".class", "");
                        
                        // Remove 'targetes.classes.' prefix if present
                        if (className.startsWith("targetes.classes.")) {
                            className = className.substring("targetes.classes.".length());
                        }
                        
                        // Skip META-INF entries
                        if (className.startsWith("META-INF.")) {
                            continue;
                        }
                        
                        try {
                            logger.info("Attempting to load class: " + className);
                            Class<?> clazz = classLoader.loadClass(className);
                            if (Command.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(CommandInfo.class)) {
                                CommandInfo info = clazz.getAnnotation(CommandInfo.class);
                                commandMap.put(info.name().toLowerCase(), (Class<? extends Command>) clazz);
                                logger.info("Successfully loaded command: " + info.name() + " from " + jarPath);
                            }
                        } catch (ClassNotFoundException e) {
                            logger.warning("Failed to load class " + className + " from " + jarPath + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load commands from JAR: " + jarPath + " - " + e.getMessage());
        }
    }

    public Command createCommand(String commandName) {
        Class<? extends Command> commandClass = commandMap.get(commandName.toLowerCase());
        if (commandClass == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }
        try {
            return commandClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create command instance: " + commandName, e);
        }
    }
}
